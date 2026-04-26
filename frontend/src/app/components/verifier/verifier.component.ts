import { Component, OnInit } from '@angular/core';
import { ProductService } from '../../services/product.service';
import { Product } from '../../models/product.model';

interface Toast { type: 'success' | 'danger' | 'info'; text: string; id: number; }

@Component({
  selector: 'app-verifier',
  templateUrl: './verifier.component.html',
  styleUrls: ['./verifier.component.css']
})
export class VerifierComponent implements OnInit {
  products: Product[] = [];
  isLoading = false;
  errorMessage = '';
  productActionLoading: { [key: number]: boolean } = {};
  productRemarks: { [key: number]: string } = {};
  toasts: Toast[] = [];
  private toastCounter = 0;

  constructor(private productService: ProductService) {}

  ngOnInit(): void { this.loadPending(); }

  loadPending(): void {
    this.isLoading = true;
    this.errorMessage = '';
    this.productService.getPendingProducts().subscribe({
      next: (data: Product[]) => { this.products = data; this.isLoading = false; },
      error: (err: { status: number }) => {
        this.isLoading = false;
        this.errorMessage = err.status === 0 ? 'Cannot connect to server.' : 'Failed to load pending products.';
      }
    });
  }

  approve(productId: number): void {
    const remarks = (this.productRemarks[productId] || '').trim() || 'Approved';
    this.productActionLoading[productId] = true;
    this.productService.reviewProduct(productId, 'APPROVED', remarks).subscribe({
      next: () => {
        this.productActionLoading[productId] = false;
        this.showToast('success', 'Product approved. Seller has been notified.');
        this.products = this.products.filter(p => p.productId !== productId);
      },
      error: () => { this.productActionLoading[productId] = false; this.showToast('danger', 'Failed to approve product.'); }
    });
  }

  reject(productId: number): void {
    const remarks = (this.productRemarks[productId] || '').trim();
    if (!remarks) { this.showToast('info', 'Please enter a rejection reason before rejecting.'); return; }
    this.productActionLoading[productId] = true;
    this.productService.reviewProduct(productId, 'REJECTED', remarks).subscribe({
      next: () => {
        this.productActionLoading[productId] = false;
        this.showToast('success', 'Product rejected. Seller has been notified.');
        this.products = this.products.filter(p => p.productId !== productId);
      },
      error: () => { this.productActionLoading[productId] = false; this.showToast('danger', 'Failed to reject product.'); }
    });
  }

  showToast(type: Toast['type'], text: string): void {
    const id = ++this.toastCounter;
    this.toasts.push({ type, text, id });
    setTimeout(() => { this.toasts = this.toasts.filter(t => t.id !== id); }, 4000);
  }

  dismissToast(id: number): void { this.toasts = this.toasts.filter(t => t.id !== id); }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(amount);
  }
}
