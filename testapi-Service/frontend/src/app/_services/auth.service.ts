import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

//const AUTH_API = `${environment.apiUrl}/api/auth/`;
const AUTH_API = `${environment.apiUrl}/auth/api/`;

// Refresh token calls go directly to the backend (not through gateway auth path)
const BACKEND_AUTH_API = `${environment.oauth2BackendUrl}/api/auth/`;

const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  constructor(private http: HttpClient) { }

  login(username: string, password: string): Observable<any> {
    return this.http.post(AUTH_API + 'signin', {
      username,
      password
    }, httpOptions);
  }

  register(fullName: string,username: string, email: string, password: string): Observable<any> {
    return this.http.post(AUTH_API + 'signup', {
      fullName,
      username,
      email,
      password
    }, httpOptions);
  }

  refreshToken(refreshToken: string): Observable<any> {
    return this.http.post(BACKEND_AUTH_API + 'refresh-token', {
      refreshToken
    }, httpOptions);
  }
}
