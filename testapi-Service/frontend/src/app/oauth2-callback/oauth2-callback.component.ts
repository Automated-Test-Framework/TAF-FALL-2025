import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { TokenStorageService } from '../_services/token-storage.service';

@Component({
  selector: 'app-oauth2-callback',
  templateUrl: './oauth2-callback.component.html',
  styleUrls: ['./oauth2-callback.component.css']
})
export class OAuth2CallbackComponent implements OnInit {
  isLoading = true;
  isError = false;
  errorMessage = '';

  constructor(
    private route: ActivatedRoute,
    private tokenStorage: TokenStorageService
  ) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe((params: { [key: string]: string }) => {
      const token: string | undefined = params['token'];
      const userInfoBase64: string | undefined = params['userInfo'];

      if (!token) {
        this.isLoading = false;
        this.isError = true;
        this.errorMessage = 'Aucun token reçu de Google. Veuillez réessayer.';
        return;
      }

      try {
        // Save JWT token
        this.tokenStorage.saveToken(token);

        if (userInfoBase64) {
          // Decode base64url user info
          const json: string = atob(userInfoBase64.replace(/-/g, '+').replace(/_/g, '/'));
          const userInfo: Record<string, unknown> = JSON.parse(json);
          this.tokenStorage.saveUser(userInfo);
        } else {
          // Minimal user object if no userInfo provided
          this.tokenStorage.saveUser({
            accessToken: token,
            fullName: 'Google User',
            roles: ['ROLE_USER']
          });
        }

        // Redirect to home and reload to update navbar
        window.location.href = '/home';
      } catch (e) {
        this.isLoading = false;
        this.isError = true;
        this.errorMessage = 'Erreur lors du traitement de la connexion Google.';
        console.error('OAuth2 callback error:', e);
      }
    });
  }
}
