import { Component, OnInit } from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup, ValidationErrors, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { ProductService } from '../../services/product.service';
import { AuthService } from '../../services/auth.service';
import { Category } from '../../models/product.model';

@Component({
  selector: 'app-product-submit',
  templateUrl: './product-submit.component.html',
  styleUrls: ['./product-submit.component.css']
})
export class ProductSubmitComponent implements OnInit {
  submitForm!: FormGroup;
  isLoading = false;
  errorMessage = '';
  successMessage = '';
  categories: Category[] = [];

  constructor(
    private fb: FormBuilder,
    private productService: ProductService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.productService.getCategories().subscribe({
      next: (cats) => { this.categories = cats; },
      error: () => { this.errorMessage = 'Could not load categories.'; }
    });

    this.submitForm = this.fb.group({
      productName: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(100)]],
      description: ['', [Validators.required, Validators.minLength(10), Validators.maxLength(500)]],
      imageUrl: ['', [Validators.required, Validators.pattern(/^https?:\/\/.+/)]],
      startingPrice: [null, [Validators.required, Validators.min(1)]],
      categoryId: [null, [Validators.required]],
      auctionStartDate: ['', [Validators.required]],
      auctionEndDate: ['', [Validators.required]],
      termsAccepted: [false, [Validators.requiredTrue]],
      verificationDoc: [null, [Validators.required]] // Mandatory File
    }, { validators: this.dateRangeValidator });
  }

  // Handle File selection
  onFileChange(event: any): void {
    if (event.target.files.length > 0) {
      const file = event.target.files[0];
      this.submitForm.patchValue({
        verificationDoc: file
      });
      this.submitForm.get('verificationDoc')?.updateValueAndValidity();
    }
  }

  dateRangeValidator(group: AbstractControl): ValidationErrors | null {
    const start = group.get('auctionStartDate')?.value;
    const end = group.get('auctionEndDate')?.value;
    if (!start || !end) return null;
    if (new Date(start) < new Date()) return { startInPast: true };
    if (new Date(end) <= new Date(start)) return { endBeforeStart: true };
    return null;
  }

  // Getters for template
  get productNameCtrl() { return this.submitForm.get('productName'); }
  get descriptionCtrl() { return this.submitForm.get('description'); }
  get imageUrlCtrl() { return this.submitForm.get('imageUrl'); }
  get startingPriceCtrl() { return this.submitForm.get('startingPrice'); }
  get categoryIdCtrl() { return this.submitForm.get('categoryId'); }
  get verificationDocCtrl() { return this.submitForm.get('verificationDoc'); }

  onSubmit(): void {
    if (this.submitForm.invalid) {
      this.submitForm.markAllAsTouched();
      return;
    }

    const sellerId = this.authService.getUserId();
    if (!sellerId) {
      this.errorMessage = 'Seller ID not found. Please login.';
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';
    
    // Preparation for API (Using FormData to handle File + Text)
    const formData = new FormData();
    formData.append('sellerId', sellerId.toString());
    formData.append('productName', this.submitForm.value.productName);
    formData.append('description', this.submitForm.value.description);
    formData.append('imageUrl', this.submitForm.value.imageUrl);
    formData.append('startingPrice', this.submitForm.value.startingPrice);
    formData.append('categoryId', this.submitForm.value.categoryId);
    formData.append('verificationDoc', this.submitForm.value.verificationDoc);

    this.productService.submitProduct(sellerId, this.submitForm.value).subscribe({
      next: () => {
        this.isLoading = false;
        this.successMessage = 'Product and Documents submitted for review!';
        this.submitForm.reset();
        setTimeout(() => void this.router.navigate(['/products']), 2500);
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = err.error?.message || 'Submission failed.';
      }
    });
  }

  get imagePreviewUrl(): string {
    const val = this.imageUrlCtrl?.value;
    return val && /^https?:\/\/.+/.test(val) ? val : '';
  }
}