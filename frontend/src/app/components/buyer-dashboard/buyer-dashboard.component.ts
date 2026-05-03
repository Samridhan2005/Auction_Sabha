import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../../services/auth.service';
import { environment } from '../../../environments/environment';

export interface BuyerBidSummary {
  auctionId: number;
  productName: string;
  description: string;
  imageUrl: string | null;
  categoryName: string;
  startingPrice: number;
  sellerName: string;
  myMaxBid: number;
  finalBid: number;
  auctionStatus: 'CREATED' | 'LIVE' | 'ENDED';
  won: boolean;
  winner: string | null;
}

type BidFilter = 'ALL' | 'WON' | 'OUTBID' | 'LIVE';

@Component({
  selector: 'app-buyer-dashboard',
  templateUrl: './buyer-dashboard.component.html',
  styleUrls: ['./buyer-dashboard.component.css']
})
export class BuyerDashboardComponent implements OnInit {
  bids: BuyerBidSummary[] = [];
  isLoading = false;
  errorMessage = '';
  activeFilter: BidFilter = 'ALL';

  constructor(
    private http: HttpClient,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadBidHistory();
  }

  loadBidHistory(): void {
    const buyerId = this.authService.getUserId();
    if (!buyerId) { this.errorMessage = 'Could not identify your account.'; return; }
    this.isLoading = true;
    this.errorMessage = '';
    this.http.get<BuyerBidSummary[]>(`${environment.apiBaseUrl}/api/bids/my-bids?buyerId=${buyerId}`).subscribe({
      next: (data) => { this.bids = data; this.isLoading = false; },
      error: () => { this.isLoading = false; this.errorMessage = 'Failed to load bid history. Please try again.'; }
    });
  }

  setFilter(f: BidFilter): void { this.activeFilter = f; }

  get filteredBids(): BuyerBidSummary[] {
    switch (this.activeFilter) {
      case 'WON':    return this.bids.filter(b => b.won);
      case 'OUTBID': return this.bids.filter(b => !b.won && b.auctionStatus === 'ENDED');
      case 'LIVE':   return this.bids.filter(b => b.auctionStatus === 'LIVE');
      default:       return this.bids;
    }
  }

  // ── Stats ─────────────────────────────────────────────────────────────────
  get totalParticipated(): number { return this.bids.length; }
  get wonCount(): number          { return this.bids.filter(b => b.won).length; }
  get liveCount(): number         { return this.bids.filter(b => b.auctionStatus === 'LIVE').length; }
  get outbidCount(): number       { return this.bids.filter(b => !b.won && b.auctionStatus === 'ENDED').length; }

  get totalSpent(): number {
    return this.bids.filter(b => b.won).reduce((s, b) => s + b.myMaxBid, 0);
  }

  get totalRefunded(): number {
    return this.bids
      .filter(b => !b.won && b.auctionStatus === 'ENDED')
      .reduce((s, b) => s + b.myMaxBid, 0);
  }

  // ── Helpers ───────────────────────────────────────────────────────────────
  isLeading(b: BuyerBidSummary): boolean {
    return b.auctionStatus === 'LIVE' && b.winner !== null && b.myMaxBid === b.finalBid;
  }

  outcomeLabel(b: BuyerBidSummary): string {
    if (b.won) return 'WON';
    if (b.auctionStatus === 'ENDED') return 'OUTBID';
    if (b.auctionStatus === 'LIVE') return this.isLeading(b) ? 'LEADING' : 'OUTBID';
    return 'UPCOMING';
  }

  goToAuction(b: BuyerBidSummary): void {
    if (b.auctionStatus === 'LIVE') {
      void this.router.navigate(['/auction', b.auctionId], { state: { auction: b } });
    }
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency', currency: 'INR', maximumFractionDigits: 0
    }).format(amount);
  }
}
