import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-forgot-password',
  templateUrl: './forgot-password.component.html',
  // Reusing login styles ensures the split-screen branding looks identical
  styleUrls: ['../login/login.component.css', './forgot-password.component.css']
})
export class ForgotPasswordComponent {
  forgotForm: FormGroup;
  isLoading = false;
  message = '';
  errorMessage = '';

  constructor(private fb: FormBuilder, private authService: AuthService) {
    this.forgotForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]]
    });
  }

  onSendLink(): void {
    if (this.forgotForm.invalid) {
      this.forgotForm.markAllAsTouched();
      return;
    }

    this.isLoading = true;
    this.message = '';
    this.errorMessage = '';

    const email = this.forgotForm.value.email;

    this.authService.forgotPassword(email).subscribe({
      next: () => {
        this.isLoading = false;
        this.message = `A secure reset link has been sent to ${email}.`;
      },
      error: (err) => {
        this.isLoading = false;
        // Logic to handle "Email not found" vs general server errors
        if (err.status === 404) {
          this.errorMessage = 'Email address not found in our system.';
        } else {
          this.errorMessage = 'An error occurred on the server. Please try again later.';
        }
      }
    });
  }
}