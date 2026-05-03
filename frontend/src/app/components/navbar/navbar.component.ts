import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { SearchService } from '../../services/search.service';

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css']
})
export class NavbarComponent {

  isProfileVisible: boolean = false;
  navSearchText: string = '';

  constructor(
    public authService: AuthService,
    private router: Router,
    private searchService: SearchService
  ) {}

  get role(): string | null {
    return this.authService.getRole();
  }

  get email(): string | null {
    return this.authService.getEmail();
  }

  get userName(): string {
    return this.authService.getUserName() || this.authService.getEmail() || '';
  }

  toggleProfile(): void {
    this.isProfileVisible = !this.isProfileVisible;
  }

  onNavSearch(value: string): void {
    this.navSearchText = value;
    this.searchService.setQuery(value);
    if (!this.router.url.startsWith('/home')) {
      void this.router.navigate(['/home']);
    }
  }

  clearNavSearch(): void {
    this.navSearchText = '';
    this.searchService.clear();
  }

  logout(): void {
    this.authService.logout();
    void this.router.navigate(['/login']);
  }
}