import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { LoginRequest, LoginResponse, RegisterRequest } from '../models/user.model';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly baseUrl = `${environment.apiBaseUrl}/api/auth`;
  private readonly TOKEN_KEY = 'auction_token';
  private readonly ROLE_KEY = 'auction_role';
  private readonly USER_ID_KEY = 'auction_user_id';
  private readonly EMAIL_KEY = 'auction_email';
  private readonly NAME_KEY = 'auction_name';

  constructor(private http: HttpClient) {}

  // --- Existing Authentication Methods ---

  login(credentials: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.baseUrl}/login`, credentials).pipe(
      tap(response => {
        localStorage.setItem(this.TOKEN_KEY, response.token);
        localStorage.setItem(this.ROLE_KEY, response.role);
        localStorage.setItem(this.USER_ID_KEY, response.userId.toString());
        if (response.name) {
          localStorage.setItem(this.NAME_KEY, response.name);
        }
        const email = this.decodeEmailFromToken(response.token);
        if (email) {
          localStorage.setItem(this.EMAIL_KEY, email);
        }
      })
    );
  }

  register(request: RegisterRequest): Observable<string> {
    return this.http.post(`${this.baseUrl}/register`, request, { responseType: 'text' }).pipe(
      tap((response: string) => {
        const match = response.match(/(\d+)\s*$/);
        if (match) {
          localStorage.setItem(this.USER_ID_KEY, match[1]);
        }
      })
    );
  }

  logout(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.ROLE_KEY);
    localStorage.removeItem(this.USER_ID_KEY);
    localStorage.removeItem(this.EMAIL_KEY);
    localStorage.removeItem(this.NAME_KEY);
  }

  // --- New Password Management Methods ---

  /**
   * Triggers a password reset email from the backend.
   * Expects the backend to handle the email sending logic.
   */
  forgotPassword(email: string): Observable<string> {
    return this.http.post(`${this.baseUrl}/forgot-password`, { email }, { responseType: 'text' });
  }

  /**
   * Submits the new password using the token received in the user's email.
   */
  resetPassword(token: string, newPassword: string): Observable<string> {
    return this.http.post(`${this.baseUrl}/reset-password`, { token, newPassword }, { responseType: 'text' });
  }

  /**
   * Updates the password for a user who is already logged in.
   * Uses the stored userId to identify the account.
   */
  changePassword(oldPassword: string, newPassword: string): Observable<string> {
    const userId = this.getUserId();
    return this.http.post(`${this.baseUrl}/change-password`, { 
      userId, 
      oldPassword, 
      newPassword 
    }, { responseType: 'text' });
  }

  // --- Helper Methods ---

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  getRole(): string | null {
    return localStorage.getItem(this.ROLE_KEY);
  }

  getUserId(): number | null {
    const id = localStorage.getItem(this.USER_ID_KEY);
    return id ? parseInt(id, 10) : null;
  }

  setUserId(id: number): void {
    localStorage.setItem(this.USER_ID_KEY, id.toString());
  }

  getEmail(): string | null {
    return localStorage.getItem(this.EMAIL_KEY);
  }

  getUserName(): string | null {
    return localStorage.getItem(this.NAME_KEY);
  }

  setUserName(name: string): void {
    localStorage.setItem(this.NAME_KEY, name);
  }

  isLoggedIn(): boolean {
    const token = this.getToken();
    if (!token) return false;
    return !this.isTokenExpired(token);
  }

  private decodeEmailFromToken(token: string): string | null {
    try {
      const payload = token.split('.')[1];
      const decoded = JSON.parse(atob(payload));
      return decoded['sub'] || decoded['email'] || null;
    } catch {
      return null;
    }
  }

  private isTokenExpired(token: string): boolean {
    try {
      const payload = token.split('.')[1];
      const decoded = JSON.parse(atob(payload));
      if (!decoded['exp']) return false;
      return decoded['exp'] * 1000 < Date.now();
    } catch {
      return true;
    }
  }
}