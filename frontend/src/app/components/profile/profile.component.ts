import { Component, Output, EventEmitter, OnInit } from '@angular/core';
import { UserService } from '../../services/user.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-profile',
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.css']
})
export class ProfileComponent implements OnInit {
  @Output() closeRequested = new EventEmitter<void>();

  user: any = {};
  loading = true;
  saving = false;
  errorMsg: string | null = null;
  successMsg: string | null = null;

  constructor(
    private userService: UserService,
    private authService: AuthService
  ) {}

  ngOnInit() {
    const userId = this.authService.getUserId();
    if (!userId) {
      this.errorMsg = 'Could not identify user. Please log in again.';
      this.loading = false;
      return;
    }
    this.userService.getProfile(userId).subscribe({
      next: (data) => {
        this.user = data;
        this.loading = false;
      },
      error: (err) => {
        this.errorMsg = 'Failed to load profile.';
        this.loading = false;
      }
    });
  }

  close() {
    this.closeRequested.emit();
  }

  save() {
    const userId = this.authService.getUserId();
    if (!userId) return;
    this.saving = true;
    this.errorMsg = null;
    this.successMsg = null;
    this.userService.updateProfile(userId, this.user).subscribe({
      next: (updated) => {
        this.user = updated;
        this.authService.setUserName(updated.name);
        this.saving = false;
        this.successMsg = 'Profile updated successfully!';
        setTimeout(() => this.successMsg = null, 3000);
      },
      error: (err) => {
        this.saving = false;
        this.errorMsg = err?.error || 'Failed to update profile. Please try again.';
      }
    });
  }
}
