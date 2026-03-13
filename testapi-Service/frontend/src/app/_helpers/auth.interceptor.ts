import { HTTP_INTERCEPTORS, HttpEvent, HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpHandler, HttpRequest } from '@angular/common/http';

import { TokenStorageService } from '../_services/token-storage.service';
import { AuthService } from '../_services/auth.service';
import { Observable, throwError, BehaviorSubject } from 'rxjs';
import { catchError, filter, take, switchMap } from 'rxjs/operators';

const TOKEN_HEADER_KEY = 'Authorization';       // for Spring Boot back-end

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  private isRefreshing = false;
  private refreshTokenSubject: BehaviorSubject<string | null> = new BehaviorSubject<string | null>(null);

  constructor(
    private tokenService: TokenStorageService,
    private authService: AuthService
  ) { }

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    let authReq = req;
    const token = this.tokenService.getToken();
    if (token != null) {
      authReq = this.addTokenHeader(req, token);
    }

    return next.handle(authReq).pipe(
      catchError((error: HttpErrorResponse) => {
        // Only attempt refresh on 401 errors and if the request is not itself a refresh/auth request
        if (error.status === 401
            && !req.url.includes('/api/auth/')
            && !req.url.includes('/auth/api/')
            && !req.url.includes('/oauth2/')) {
          return this.handle401Error(authReq, next);
        }
        return throwError(() => error);
      })
    );
  }

  private handle401Error(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    if (!this.isRefreshing) {
      this.isRefreshing = true;
      this.refreshTokenSubject.next(null);

      const refreshToken = this.tokenService.getRefreshToken();

      if (refreshToken) {
        return this.authService.refreshToken(refreshToken).pipe(
          switchMap((response: any) => {
            this.isRefreshing = false;

            // Save new tokens
            const newAccessToken = response.token || response.accessToken;
            const newRefreshToken = response.refreshToken || response.refresh;

            this.tokenService.saveToken(newAccessToken);
            if (newRefreshToken) {
              this.tokenService.saveRefreshToken(newRefreshToken);
            }
            this.tokenService.saveUser(response);

            this.refreshTokenSubject.next(newAccessToken);

            // Retry the original request with the new token
            return next.handle(this.addTokenHeader(request, newAccessToken));
          }),
          catchError((err) => {
            // Refresh token is also invalid — force logout
            this.isRefreshing = false;
            this.tokenService.signOut();
            window.location.href = '/login';
            return throwError(() => err);
          })
        );
      } else {
        // No refresh token available — force logout
        this.isRefreshing = false;
        this.tokenService.signOut();
        window.location.href = '/login';
        return throwError(() => new Error('No refresh token available'));
      }
    }

    // If a refresh is already in progress, queue this request until refresh completes
    return this.refreshTokenSubject.pipe(
      filter(token => token !== null),
      take(1),
      switchMap((token) => next.handle(this.addTokenHeader(request, token!)))
    );
  }

  private addTokenHeader(request: HttpRequest<any>, token: string): HttpRequest<any> {
    return request.clone({ headers: request.headers.set(TOKEN_HEADER_KEY, 'Bearer ' + token) });
  }
}

export const authInterceptorProviders = [
  { provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true }
];