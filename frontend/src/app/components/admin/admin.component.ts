import { Component, OnInit } from '@angular/core';
import { AuctionService } from '../../services/auction.service';
import { AdminService } from '../../services/admin.service';
import { AuctionCard } from '../../models/auction.model';

interface ToastMessage { type: 'success' | 'danger' | 'info'; text: string; id: number; }

@Component({
  selector: 'app-admin',
  templateUrl: './admin.component.html',
  styleUrls: ['./admin.component.css']
})
export class AdminComponent implements OnInit {
  auctions: AuctionCard[] = [];
  isLoading = false;
  errorMessage = '';
  auctionActionLoading: { [key: number]: boolean } = {};
  toasts: ToastMessage[] = [];
  private toastCounter = 0;

  constructor(
    private auctionService: AuctionService,
    private adminService: AdminService
  ) {}

  ngOnInit(): void { this.loadAuctions(); }

  loadAuctions(): void {
    this.isLoading = true;
    this.errorMessage = '';
    this.auctionService.getFeaturedAuctions().subscribe({
      next: (data: AuctionCard[]) => { this.auctions = data; this.isLoading = false; },
      error: (err: { status: number }) => {
        this.isLoading = false;
        this.errorMessage = err.status === 0 ? 'Cannot connect to server.' : 'Failed to load auctions.';
      }
    });
  }

  startAuction(auctionId: number): void {
    this.auctionActionLoading[auctionId] = true;
    this.adminService.startAuction(auctionId).subscribe({
      next: (msg: string) => {
        this.auctionActionLoading[auctionId] = false;
        this.showToast('success', msg || 'Auction started.');
        this.loadAuctions();
      },
      error: (err: { error?: { message?: string } }) => {
        this.auctionActionLoading[auctionId] = false;
        this.showToast('danger', err.error?.message || 'Failed to start auction.');
      }
    });
  }

  stopAuction(auctionId: number): void {
    this.auctionActionLoading[auctionId] = true;
    this.adminService.stopAuction(auctionId).subscribe({
      next: (msg: string) => {
        this.auctionActionLoading[auctionId] = false;
        this.showToast('success', msg || 'Auction stopped.');
        this.loadAuctions();
      },
      error: (err: { error?: { message?: string } }) => {
        this.auctionActionLoading[auctionId] = false;
        this.showToast('danger', err.error?.message || 'Failed to stop auction.');
      }
    });
  }

  showToast(type: ToastMessage['type'], text: string): void {
    const id = ++this.toastCounter;
    this.toasts.push({ type, text, id });
    setTimeout(() => { this.toasts = this.toasts.filter(t => t.id !== id); }, 4000);
  }

  dismissToast(id: number): void { this.toasts = this.toasts.filter(t => t.id !== id); }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(amount);
  }
}
