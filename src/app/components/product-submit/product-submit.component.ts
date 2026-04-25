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
      error: () => { this.errorMessage = 'Could not load categories. Please refresh.'; }
    });

    this.submitForm = this.fb.group({
      productName: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(100)]],
      description: ['', [Validators.required, Validators.minLength(10), Validators.maxLength(500)]],
      imageUrl: ['', [Validators.required, Validators.pattern(/^https?:\/\/.+/)]],
      startingPrice: [null, [Validators.required, Validators.min(1)]],
      categoryId: [null, [Validators.required]],
      auctionStartDate: ['', [Validators.required]],
      auctionEndDate: ['', [Validators.required]],
      termsAccepted: [false, [Validators.requiredTrue]]
    }, { validators: this.dateRangeValidator });
  }

  dateRangeValidator(group: AbstractControl): ValidationErrors | null {
    const start = group.get('auctionStartDate')?.value as string;
    const end = group.get('auctionEndDate')?.value as string;
    if (!start || !end) return null;
    if (new Date(start) < new Date()) return { startInPast: true };
    if (new Date(end) <= new Date(start)) return { endBeforeStart: true };
    return null;
  }

  get productNameCtrl() { return this.submitForm.get('productName'); }
  get descriptionCtrl() { return this.submitForm.get('description'); }
  get imageUrlCtrl() { return this.submitForm.get('imageUrl'); }
  get startingPriceCtrl() { return this.submitForm.get('startingPrice'); }
  get categoryIdCtrl() { return this.submitForm.get('categoryId'); }
  get auctionStartDateCtrl() { return this.submitForm.get('auctionStartDate'); }
  get auctionEndDateCtrl() { return this.submitForm.get('auctionEndDate'); }
  get termsAcceptedCtrl() { return this.submitForm.get('termsAccepted'); }

  onSubmit(): void {
    if (this.submitForm.invalid) {
      this.submitForm.markAllAsTouched();
      return;
    }

    const sellerId = this.authService.getUserId();
    if (!sellerId) {
      this.errorMessage = 'Seller ID not found. Please set your User ID in the Wallet page first.';
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';
    this.successMessage = '';

    const formVal = this.submitForm.value as {
      productName: string;
      description: string;
      imageUrl: string;
      startingPrice: number;
      categoryId: number;
    };

    this.productService.submitProduct(sellerId, {
      productName: formVal.productName,
      description: formVal.description,
      imageUrl: formVal.imageUrl,
      startingPrice: formVal.startingPrice,
      categoryId: formVal.categoryId
    }).subscribe({
      next: () => {
        this.isLoading = false;
        this.successMessage = 'Product submitted for review! Your item will be reviewed by our team within 24–48 hours.';
        this.submitForm.reset();
        setTimeout(() => void this.router.navigate(['/products']), 2500);
      },
      error: (err: { status: number; error?: { message?: string } }) => {
        this.isLoading = false;
        if (err.status === 0) {
          this.errorMessage = 'Cannot connect to server. Please try again later.';
        } else if (err.status === 403) {
          this.errorMessage = 'You are not authorized to submit products.';
        } else {
          this.errorMessage = err.error?.message || 'Failed to submit product. Please try again.';
        }
      }
    });
  }

  get imagePreviewUrl(): string {
    const val = this.imageUrlCtrl?.value as string;
    return val && /^https?:\/\/.+/.test(val) ? val : '';
  }
}
