import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { AuctionService } from '../../services/auction.service';
import { AuthService } from '../../services/auth.service';
import { WebsocketService } from '../../services/websocket.service';
import { AuctionCard, BidUpdate } from '../../models/auction.model';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit, OnDestroy {
  auctions: AuctionCard[] = [];
  filteredAuctions: AuctionCard[] = [];
  isLoading = true;
  errorMessage = '';

  categories: string[] = [];
  selectedCategory = '';
  selectedStatus = '';
  minPrice = 0;
  maxPrice = 1000000;
  priceRangeMax = 1000000;

  private timerInterval: ReturnType<typeof setInterval> | null = null;
  timerTick = 0;

  updatedBidIds: Set<number> = new Set();

  private wsSub!: Subscription;

  constructor(
    private auctionService: AuctionService,
    public authService: AuthService,
    private wsService: WebsocketService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadAuctions();
    this.timerInterval = setInterval(() => { this.timerTick++; }, 1000);
    this.wsService.connect();
    this.wsSub = this.wsService.messages$.subscribe((msg: BidUpdate) => {
      if (msg.type === 'AUCTION_STARTED' && msg.auctionId) {
        const auction = this.auctions.find(a => a.auctionId === msg.auctionId);
        if (auction) { auction.status = 'LIVE'; this.applyFilters(); }
      } else if (msg.type === 'AUCTION_STOPPED' && msg.auctionId) {
        const auction = this.auctions.find(a => a.auctionId === msg.auctionId);
        if (auction) { auction.status = 'ENDED'; this.applyFilters(); }
      } else if (!msg.type && msg.auctionId && msg.amount) {
        const auctionId = msg.auctionId;
        const auction = this.auctions.find(a => a.auctionId === auctionId);
        if (auction && msg.amount > auction.currentBid) {
          auction.currentBid = msg.amount;
          if (msg.highestBidder) auction.highestBidder = msg.highestBidder;
          this.updatedBidIds.add(auctionId);
          setTimeout(() => { this.updatedBidIds.delete(auctionId); }, 1200);
          this.applyFilters();
        }
      }
    });
  }

  loadAuctions(): void {
    this.isLoading = true;
    this.errorMessage = '';
    this.auctionService.getFeaturedAuctions().subscribe({
      next: (data: AuctionCard[]) => {
        this.auctions = data;
        const catSet = new Set<string>();
        data.forEach(a => { if (a.categoryName) catSet.add(a.categoryName); });
        this.categories = Array.from(catSet);
        const prices = data.map(a => a.currentBid || a.startingPrice).filter(p => p > 0);
        this.priceRangeMax = prices.length > 0 ? Math.ceil(Math.max(...prices) * 2) : 1000000;
        this.maxPrice = this.priceRangeMax;
        this.applyFilters();
        this.isLoading = false;
      },
      error: (err: { status: number }) => {
        this.isLoading = false;
        this.errorMessage = err.status === 0 ? 'Cannot connect to server.' : 'Failed to load auctions.';
      }
    });
  }

  applyFilters(): void {
    this.filteredAuctions = this.auctions.filter(a => {
      const catMatch = !this.selectedCategory || a.categoryName === this.selectedCategory;
      const statusMatch = !this.selectedStatus || a.status === this.selectedStatus;
      const price = a.currentBid || a.startingPrice || 0;
      const priceMatch = price >= this.minPrice && price <= this.maxPrice;
      return catMatch && statusMatch && priceMatch;
    });
  }

  setStatus(status: string): void {
    this.selectedStatus = status;
    this.applyFilters();
  }

  onMinPriceChange(value: number): void {
    this.minPrice = value;
    if (this.minPrice > this.maxPrice) this.maxPrice = this.minPrice;
    this.applyFilters();
  }

  onMaxPriceChange(value: number): void {
    this.maxPrice = value;
    if (this.maxPrice < this.minPrice) this.minPrice = this.maxPrice;
    this.applyFilters();
  }

  resetFilters(): void {
    this.selectedCategory = '';
    this.selectedStatus = '';
    this.minPrice = 0;
    this.maxPrice = this.priceRangeMax;
    this.applyFilters();
  }

  getCountdown(auction: AuctionCard): string {
    void this.timerTick;
    if (auction.status === 'ENDED') return 'Ended';
    if (!auction.endTime || auction.status === 'CREATED') return 'Upcoming';
    const remaining = new Date(auction.endTime).getTime() - Date.now();
    if (remaining <= 0) return 'Ending...';
    const h = Math.floor(remaining / 3600000);
    const m = Math.floor((remaining % 3600000) / 60000);
    const s = Math.floor((remaining % 60000) / 1000);
    if (h > 0) return `${h}h ${m}m ${s}s`;
    if (m > 0) return `${m}m ${s}s`;
    return `${s}s`;
  }

  isJustUpdated(auctionId: number): boolean {
    return this.updatedBidIds.has(auctionId);
  }

  goToAuction(auction: AuctionCard): void {
    if (this.authService.getRole() === 'BUYER') {
      void this.router.navigate(['/auction', auction.auctionId], { state: { auction } });
    }
  }

  get role(): string | null { return this.authService.getRole(); }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(amount);
  }

  get liveCount(): number {
    return this.auctions.filter(a => a.status === 'LIVE').length;
  }

  ngOnDestroy(): void {
    if (this.timerInterval) clearInterval(this.timerInterval);
    if (this.wsSub) this.wsSub.unsubscribe();
  }
}
