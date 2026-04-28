import { Component, OnInit } from '@angular/core';
import { ProductService } from '../../services/product.service';
import { AuthService } from '../../services/auth.service';
import { SellerProductSummary, AuctionStatus } from '../../models/product.model';

type DashboardFilter = 'ALL' | 'PENDING' | 'APPROVED' | 'LIVE' | 'SOLD' | 'REJECTED';

@Component({
  selector: 'app-seller-dashboard',
  templateUrl: './seller-dashboard.component.html',
  styleUrls: ['./seller-dashboard.component.css']
})
export class SellerDashboardComponent implements OnInit {
  products: SellerProductSummary[] = [];
  isLoading = false;
  errorMessage = '';
  filterStatus: DashboardFilter = 'ALL';

  constructor(
    private productService: ProductService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loadMyProducts();
  }

  loadMyProducts(): void {
    const sellerId = this.authService.getUserId();
    if (!sellerId) { this.errorMessage = 'Could not identify your account.'; return; }
    this.isLoading = true;
    this.errorMessage = '';
    this.productService.getMyProducts(sellerId).subscribe({
      next: (data) => { this.products = data; this.isLoading = false; },
      error: () => { this.isLoading = false; this.errorMessage = 'Failed to load your products. Please try again.'; }
    });
  }

  setFilter(f: DashboardFilter): void { this.filterStatus = f; }

  get filteredProducts(): SellerProductSummary[] {
    switch (this.filterStatus) {
      case 'PENDING':  return this.products.filter(p => p.verificationStatus === 'PENDING');
      case 'APPROVED': return this.products.filter(p => p.verificationStatus === 'APPROVED' && p.auctionStatus === 'CREATED');
      case 'LIVE':     return this.products.filter(p => p.auctionStatus === 'LIVE');
      case 'SOLD':     return this.products.filter(p => p.auctionStatus === 'ENDED');
      case 'REJECTED': return this.products.filter(p => p.verificationStatus === 'REJECTED');
      default:         return this.products;
    }
  }

  get pendingCount():  number { return this.products.filter(p => p.verificationStatus === 'PENDING').length; }
  get approvedCount(): number { return this.products.filter(p => p.verificationStatus === 'APPROVED' && p.auctionStatus === 'CREATED').length; }
  get liveCount():     number { return this.products.filter(p => p.auctionStatus === 'LIVE').length; }
  get soldCount():     number { return this.products.filter(p => p.auctionStatus === 'ENDED').length; }
  get rejectedCount(): number { return this.products.filter(p => p.verificationStatus === 'REJECTED').length; }

  get totalEarnings(): number {
    return this.products
      .filter(p => p.auctionStatus === 'ENDED')
      .reduce((sum, p) => sum + p.finalBid, 0);
  }

  get totalProfit(): number {
    return this.products
      .filter(p => p.auctionStatus === 'ENDED')
      .reduce((sum, p) => sum + p.profit, 0);
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency', currency: 'INR', maximumFractionDigits: 0
    }).format(amount);
  }

  auctionStatusLabel(status: AuctionStatus | null): string {
    switch (status) {
      case 'LIVE':    return 'LIVE NOW';
      case 'ENDED':   return 'SOLD';
      case 'CREATED': return 'SCHEDULED';
      default:        return '';
    }
  }
}
