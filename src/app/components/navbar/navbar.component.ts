import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css']
})
export class NavbarComponent {
  constructor(public authService: AuthService, private router: Router) {}

  get role(): string | null {
    return this.authService.getRole();
  }

  get email(): string | null {
    return this.authService.getEmail();
  }

  logout(): void {
    this.authService.logout();
    void this.router.navigate(['/login']);
  }
}
