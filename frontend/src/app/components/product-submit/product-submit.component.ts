import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { ProductService } from '../../services/product.service';
import { AuthService } from '../../services/auth.service';
import { AuctionService, SlotInfo } from '../../services/auction.service';
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
  today = new Date().toISOString().split('T')[0];
  slots: SlotInfo[] = [];
  slotsLoading = false;

  constructor(
    private fb: FormBuilder,
    private productService: ProductService,
    private authService: AuthService,
    private auctionService: AuctionService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.productService.getCategories().subscribe({
      next: (cats) => { this.categories = cats; },
      error: () => { this.errorMessage = 'Could not load categories.'; }
    });

    this.submitForm = this.fb.group({
      productName:   ['', [Validators.required, Validators.minLength(3), Validators.maxLength(100)]],
      description:   ['', [Validators.required, Validators.minLength(10), Validators.maxLength(500)]],
      imageUrl:      ['', [Validators.required, Validators.pattern(/^https?:\/\/.+/)]],
      documentsUrl:  ['', [Validators.required, Validators.pattern(/^https?:\/\/.+/)]],
      startingPrice: [null, [Validators.required, Validators.min(1)]],
      categoryId:    [null, [Validators.required]],
      preferredDate: ['', [Validators.required]],
      preferredSlot: [null, [Validators.required]],
      termsAccepted: [false, [Validators.requiredTrue]]
    });
  }

  get productNameCtrl()   { return this.submitForm.get('productName'); }
  get descriptionCtrl()   { return this.submitForm.get('description'); }
  get imageUrlCtrl()      { return this.submitForm.get('imageUrl'); }
  get documentsUrlCtrl()  { return this.submitForm.get('documentsUrl'); }
  get startingPriceCtrl() { return this.submitForm.get('startingPrice'); }
  get categoryIdCtrl()    { return this.submitForm.get('categoryId'); }
  get preferredDateCtrl() { return this.submitForm.get('preferredDate'); }
  get preferredSlotCtrl() { return this.submitForm.get('preferredSlot'); }

  onDateChange(): void {
    const dateVal = this.preferredDateCtrl?.value;
    this.preferredSlotCtrl?.reset();
    this.slots = [];
    if (!dateVal) return;
    this.slotsLoading = true;
    this.auctionService.getAvailableSlots(dateVal).subscribe({
      next: (s) => {
        this.slots = this.markPastSlotsUnavailable(s, dateVal);
        this.slotsLoading = false;
      },
      error: () => { this.slotsLoading = false; }
    });
  }

  // Backend may report slots as AVAILABLE if its clock is in a different timezone
  // than the user. Use the user's local clock as the source of truth for "past".
  private markPastSlotsUnavailable(slots: SlotInfo[], selectedDate: string): SlotInfo[] {
    const todayIso = new Date().toISOString().split('T')[0];
    if (selectedDate !== todayIso) return slots;
    const now = new Date();
    const currentMinutes = now.getHours() * 60 + now.getMinutes();
    return slots.map(s => {
      const slotEndMinutes = (9 + s.slot) * 60;
      return slotEndMinutes <= currentMinutes
        ? { ...s, availability: 'UNAVAILABLE' as const }
        : s;
    });
  }

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

    this.productService.submitProduct(sellerId, this.submitForm.value).subscribe({
      next: () => {
        this.isLoading = false;
        this.successMessage = 'Product submitted for review! A verifier will approve and assign a slot.';
        this.submitForm.reset();
        setTimeout(() => void this.router.navigate(['/products']), 2500);
      },
      error: (err) => {
        this.isLoading = false;
        if (err.status === 0) {
          this.errorMessage = 'Cannot connect to server. Make sure the backend is running.';
        } else {
          this.errorMessage = err.error?.message || err.error?.error || `Submission failed (${err.status}).`;
        }
      }
    });
  }

}
