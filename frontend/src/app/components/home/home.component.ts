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

  // Timer properties
  private timerInterval: ReturnType<typeof setInterval> | null = null;
  timerTick = 0; // This increments every second to refresh the UI

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
    
    // 1. Initialize the countdown ticker
    this.timerInterval = setInterval(() => { 
      this.timerTick++; 
    }, 1000);

    this.wsService.connect();
    
    this.wsSub = this.wsService.messages$.subscribe((msg: BidUpdate) => {
      if (msg.type === 'AUCTION_STARTED' && msg.auctionId) {
        const auction = this.auctions.find(a => a.auctionId === msg.auctionId);
        if (auction) { 
          auction.status = 'LIVE'; 
          this.applyFilters(); 
        }
      } else if (msg.type === 'AUCTION_STOPPED' && msg.auctionId) {
        const auction = this.auctions.find(a => a.auctionId === msg.auctionId);
        if (auction) { 
          auction.status = 'ENDED'; 
          this.applyFilters(); 
        }
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

  // --- Countdown Logic ---
  getCountdown(auction: AuctionCard): string {
    // Referencing timerTick ensures the view refreshes every second
    void this.timerTick; 
    
    if (auction.status === 'ENDED') return 'Ended';
    if (!auction.endTime || auction.status === 'CREATED') return 'Upcoming';

    const endTimeDate = new Date(auction.endTime).getTime();
    const now = Date.now();
    const remaining = endTimeDate - now;

    if (remaining <= 0) {
      // Logic to handle auto-expiry in UI
      if (auction.status === 'LIVE') {
          auction.status = 'ENDED';
          this.applyFilters();
      }
      return 'Ended';
    }

    const h = Math.floor(remaining / 3600000);
    const m = Math.floor((remaining % 3600000) / 60000);
    const s = Math.floor((remaining % 60000) / 1000);

    // Format the string (e.g., 01h 05m 20s)
    const hoursStr = h > 0 ? `${h}h ` : '';
    const minsStr = m > 0 || h > 0 ? `${m.toString().padStart(2, '0')}m ` : '';
    const secsStr = `${s.toString().padStart(2, '0')}s`;

    return `${hoursStr}${minsStr}${secsStr}`;
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
    // CRITICAL: Clean up to prevent memory leaks
    if (this.timerInterval) {
      clearInterval(this.timerInterval);
    }
    if (this.wsSub) {
      this.wsSub.unsubscribe();
    }
  }
}