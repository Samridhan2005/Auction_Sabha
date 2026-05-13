import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { VerifierApplicationService } from '../../services/verifier-application.service';

@Component({
  selector: 'app-verifier-register',
  templateUrl: './verifier-register.component.html',
  styleUrls: ['./verifier-register.component.css']
})
export class VerifierRegisterComponent implements OnInit {
  form!: FormGroup;
  isLoading = false;
  errorMessage = '';
  successMessage = '';

  readonly domainOptions = [
    'Electronics', 'Vehicles', 'Real Estate', 'Antiques',
    'Jewellery', 'Art', 'Fashion', 'Other'
  ];

  readonly qualificationOptions = [
    'High School / 12th', 'Diploma', 'B.Sc / B.A / B.Com',
    'B.Tech / B.E', 'M.Sc / M.A / M.Com', 'M.Tech / M.E',
    'MBA', 'Ph.D', 'Professional Certification', 'Other'
  ];

  constructor(
    private fb: FormBuilder,
    private service: VerifierApplicationService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2)]],
      email: ['', [Validators.required, Validators.email]],
      phone: ['', [Validators.required, Validators.pattern(/^[0-9]{10,15}$/)]],
      age: ['', [Validators.required, Validators.min(18), Validators.max(75)]],
      qualification: ['', Validators.required],
      experienceYears: ['', [Validators.required, Validators.min(0), Validators.max(50)]],
      domainsInterested: ['', Validators.required],
      motivation: ['', [Validators.required, Validators.minLength(50)]],
      portfolioUrl: ['']
    });
  }

  get f() { return this.form.controls; }

  private selectedDomains: string[] = [];

  onDomainChange(event: Event): void {
    const checkbox = event.target as HTMLInputElement;
    if (checkbox.checked) {
      this.selectedDomains.push(checkbox.value);
    } else {
      this.selectedDomains = this.selectedDomains.filter(d => d !== checkbox.value);
    }
    const joined = this.selectedDomains.join(', ');
    this.form.get('domainsInterested')?.setValue(joined || '');
    this.form.get('domainsInterested')?.markAsTouched();
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.service.submit(this.form.value).subscribe({
      next: () => {
        this.isLoading = false;
        this.successMessage = 'Your application has been submitted. The admin will review it shortly. If approved, your verifier account will be created and the admin will share your login credentials with you.';
        this.form.reset();
      },
      error: (err: { status: number; error?: string }) => {
        this.isLoading = false;
        if (err.status === 0) {
          this.errorMessage = 'Cannot connect to server. Please try again later.';
        } else {
          this.errorMessage = err.error || 'Submission failed. Please try again.';
        }
      }
    });
  }
}
