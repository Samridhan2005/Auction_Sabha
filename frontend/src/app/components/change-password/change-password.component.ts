import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-change-password',
  templateUrl: './change-password.component.html',
  styleUrls: ['./change-password.component.css']
})
export class ChangePasswordComponent {
  changeForm: FormGroup;
  isUpdating = false;

  constructor(private fb: FormBuilder, private authService: AuthService) {
    this.changeForm = this.fb.group({
      oldPassword: ['', Validators.required],
      newPassword: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', Validators.required]
    }, { validator: this.passwordMatchValidator });
  }

  passwordMatchValidator(g: FormGroup) {
    return g.get('newPassword')?.value === g.get('confirmPassword')?.value ? null : { mismatch: true };
  }

  onUpdate() {
    if (this.changeForm.invalid) return;

    this.isUpdating = true;
    const { oldPassword, newPassword } = this.changeForm.value;

    this.authService.changePassword(oldPassword, newPassword).subscribe({
      next: () => {
        this.isUpdating = false;
        alert("Password updated! Please login again.");
        this.authService.logout();
      },
      error: () => {
        this.isUpdating = false;
        alert("Incorrect current password.");
      }
    });
  }
}