import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { UserService } from '../../services/user.service'; // Added UserService

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css']
})
export class NavbarComponent {
  
  // Controls the profile modal visibility
  isProfileVisible: boolean = false;

  constructor(
    public authService: AuthService, 
    private router: Router,
    private userService: UserService // Injected UserService
  ) {}

  // Getter for Auth Role (Buyer, Seller, etc.)
  get role(): string | null {
    return this.authService.getRole();
  }

  // Getter for Auth Email
  get email(): string | null {
    return this.authService.getEmail();
  }

  /**
   * Expert Tip: Use a getter to pull the updated name from the Service.
   * If you use 'this.userService.getProfile().name' in your HTML,
   * it will update instantly whenever you click 'Update Profile'.
   */
  get userName(): string {
    return this.userService.getProfile().name;
  }

  // Toggles the Profile Overlay
  toggleProfile(): void {
    this.isProfileVisible = !this.isProfileVisible;
  }

  logout(): void {
    this.authService.logout();
    void this.router.navigate(['/login']);
  }
}