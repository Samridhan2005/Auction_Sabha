import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { AuctionService } from '../../services/auction.service';
import { AuthService } from '../../services/auth.service';
import { WebsocketService } from '../../services/websocket.service';
import { WatchlistService } from '../../services/watchlist.service';
import { SearchService } from '../../services/search.service';
import { RecentlyViewedService } from '../../services/recently-viewed.service';
import { AuctionCard, BidUpdate } from '../../models/auction.model';

export interface UpcomingGroup {
  label: string;
  icon: string;
  color: string;
  auctions: AuctionCard[];
}

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
  maxPrice = 1000000;
  priceRangeMax = 1000000;
  searchText = '';
  sortBy = 'default';

  recentlyViewedList: AuctionCard[] = [];

  private timerInterval: ReturnType<typeof setInterval> | null = null;
  timerTick = 0;
  updatedBidIds: Set<number> = new Set();

  private wsSub!: Subscription;
  private searchSub!: Subscription;

  constructor(
    private auctionService: AuctionService,
    public authService: AuthService,
    private wsService: WebsocketService,
    public watchlist: WatchlistService,
    private searchService: SearchService,
    public recentlyViewed: RecentlyViewedService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.recentlyViewedList = this.recentlyViewed.getAll();
    this.loadAuctions();

    this.timerInterval = setInterval(() => { this.timerTick++; }, 1000);

    this.wsService.connect();

    this.wsSub = this.wsService.messages$.subscribe((msg: BidUpdate) => {
      if (msg.type === 'AUCTION_STARTED' && msg.auctionId) {
        const a = this.auctions.find(x => x.auctionId === msg.auctionId);
        if (a) { a.status = 'LIVE'; this.applyFilters(); }
      } else if (msg.type === 'AUCTION_STOPPED' && msg.auctionId) {
        const a = this.auctions.find(x => x.auctionId === msg.auctionId);
        if (a) { a.status = 'ENDED'; this.applyFilters(); }
      } else if (!msg.type && msg.auctionId && msg.amount) {
        const a = this.auctions.find(x => x.auctionId === msg.auctionId);
        if (a && msg.amount > a.currentBid) {
          a.currentBid = msg.amount;
          if (msg.highestBidder) a.highestBidder = msg.highestBidder;
          a.bidCount = (a.bidCount || 0) + 1;
          this.updatedBidIds.add(msg.auctionId);
          setTimeout(() => { this.updatedBidIds.delete(msg.auctionId!); }, 1200);
          this.applyFilters();
        }
      }
    });

    this.searchSub = this.searchService.query$.subscribe(q => {
      this.searchText = q;
      this.applyFilters();
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
        this.categories = Array.from(catSet).sort();

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
    const q = this.searchText.toLowerCase();
    let result = this.auctions.filter(a => {
      const catMatch    = !this.selectedCategory || a.categoryName === this.selectedCategory;
      const statusMatch = !this.selectedStatus   || a.status === this.selectedStatus;
      const price       = a.currentBid || a.startingPrice || 0;
      const priceMatch  = price <= this.maxPrice;
      const searchMatch = !q || a.productName.toLowerCase().includes(q)
                             || a.categoryName.toLowerCase().includes(q)
                             || a.sellerName.toLowerCase().includes(q)
                             || (a.description || '').toLowerCase().includes(q);
      return catMatch && statusMatch && priceMatch && searchMatch;
    });

    // Sort
    switch (this.sortBy) {
      case 'price-asc':
        result = result.sort((a, b) => (a.currentBid || a.startingPrice) - (b.currentBid || b.startingPrice));
        break;
      case 'price-desc':
        result = result.sort((a, b) => (b.currentBid || b.startingPrice) - (a.currentBid || a.startingPrice));
        break;
      case 'bids':
        result = result.sort((a, b) => (b.bidCount || 0) - (a.bidCount || 0));
        break;
      case 'ending':
        result = result.sort((a, b) => {
          const ta = a.endTime ? new Date(a.endTime).getTime() : Infinity;
          const tb = b.endTime ? new Date(b.endTime).getTime() : Infinity;
          return ta - tb;
        });
        break;
      case 'newest':
        result = result.sort((a, b) => {
          const ta = a.confirmedStartTime ? new Date(a.confirmedStartTime).getTime() : 0;
          const tb = b.confirmedStartTime ? new Date(b.confirmedStartTime).getTime() : 0;
          return tb - ta;
        });
        break;
    }

    this.filteredAuctions = result;
  }

  setSortBy(sort: string): void {
    this.sortBy = sort;
    this.applyFilters();
  }

  // Auctions ending in ≤ 5 minutes
  get endingSoon(): AuctionCard[] {
    void this.timerTick;
    return this.auctions
      .filter(a => {
        if (a.status !== 'LIVE' || !a.endTime) return false;
        const ms = new Date(a.endTime).getTime() - Date.now();
        return ms > 0 && ms <= 5 * 60 * 1000;
      })
      .sort((a, b) => new Date(a.endTime!).getTime() - new Date(b.endTime!).getTime());
  }

  // CREATED auctions grouped by date (Auction Calendar)
  get upcomingGroups(): UpcomingGroup[] {
    const now = new Date();
    const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());
    const tomorrow = new Date(today.getTime() + 86_400_000);
    const dayAfter = new Date(today.getTime() + 2 * 86_400_000);
    const weekEnd = new Date(today.getTime() + 7 * 86_400_000);

    const upcoming = this.auctions.filter(a => a.status === 'CREATED' && a.confirmedStartTime);

    const bucket = (from: Date, to: Date) =>
      upcoming.filter(a => {
        const d = new Date(a.confirmedStartTime!);
        return d >= from && d < to;
      });

    const groups: UpcomingGroup[] = [];
    const todayList    = bucket(today,    tomorrow);
    const tomorrowList = bucket(tomorrow, dayAfter);
    const weekList     = bucket(dayAfter, weekEnd);
    const laterList    = bucket(weekEnd,  new Date('2100-01-01'));

    if (todayList.length)    groups.push({ label: 'Today',       icon: 'bi-calendar-check-fill', color: '#10b981', auctions: todayList });
    if (tomorrowList.length) groups.push({ label: 'Tomorrow',    icon: 'bi-calendar-plus-fill',  color: '#2563eb', auctions: tomorrowList });
    if (weekList.length)     groups.push({ label: 'This Week',   icon: 'bi-calendar-week-fill',  color: '#7c3aed', auctions: weekList });
    if (laterList.length)    groups.push({ label: 'Coming Soon', icon: 'bi-calendar-event-fill', color: '#f59e0b', auctions: laterList });

    return groups;
  }

  // Count auctions per category (for category strip badges)
  get categoryCounts(): Map<string, number> {
    const map = new Map<string, number>();
    this.auctions.forEach(a => {
      if (a.categoryName) map.set(a.categoryName, (map.get(a.categoryName) || 0) + 1);
    });
    return map;
  }

  formatSlotTime(isoStr: string | null): string {
    if (!isoStr) return '';
    const d = new Date(isoStr);
    return d.toLocaleTimeString('en-IN', { hour: '2-digit', minute: '2-digit', hour12: true });
  }

  formatSlotDate(isoStr: string | null): string {
    if (!isoStr) return '';
    const d = new Date(isoStr);
    const now = new Date();
    // Compare CALENDAR days, not raw 24-hour gaps — otherwise "tomorrow 9 AM"
    // viewed at 10 PM tonight rounds to 0 days diff and shows as "Today".
    const startOfToday = new Date(now.getFullYear(), now.getMonth(), now.getDate()).getTime();
    const startOfSlotDay = new Date(d.getFullYear(), d.getMonth(), d.getDate()).getTime();
    const diff = Math.round((startOfSlotDay - startOfToday) / 86_400_000);
    if (diff === 0) return `Today at ${this.formatSlotTime(isoStr)}`;
    if (diff === 1) return `Tomorrow at ${this.formatSlotTime(isoStr)}`;
    return d.toLocaleDateString('en-IN', { weekday: 'short', month: 'short', day: 'numeric' })
           + ' at ' + this.formatSlotTime(isoStr);
  }

  getCountdown(auction: AuctionCard): string {
    void this.timerTick;
    if (auction.status === 'ENDED') return 'Ended';
    if (!auction.endTime || auction.status === 'CREATED') return 'Upcoming';

    const remaining = new Date(auction.endTime).getTime() - Date.now();
    if (remaining <= 0) {
      if (auction.status === 'LIVE') { auction.status = 'ENDED'; this.applyFilters(); }
      return 'Ended';
    }
    const h = Math.floor(remaining / 3600000);
    const m = Math.floor((remaining % 3600000) / 60000);
    const s = Math.floor((remaining % 60000) / 1000);
    return `${h > 0 ? h + 'h ' : ''}${(m > 0 || h > 0) ? m.toString().padStart(2,'0') + 'm ' : ''}${s.toString().padStart(2,'0')}s`;
  }

  getSecondsLeft(auction: AuctionCard): number {
    void this.timerTick;
    if (!auction.endTime) return 0;
    return Math.max(0, Math.floor((new Date(auction.endTime).getTime() - Date.now()) / 1000));
  }

  setStatus(status: string): void   { this.selectedStatus = status; this.applyFilters(); }
  setCategory(cat: string): void    { this.selectedCategory = cat; this.applyFilters(); }
  onMaxPriceChange(value: number)   { this.maxPrice = value; this.applyFilters(); }

  resetFilters(): void {
    this.selectedCategory = '';
    this.selectedStatus   = '';
    this.maxPrice         = this.priceRangeMax;
    this.searchText       = '';
    this.sortBy           = 'default';
    this.searchService.clear();
    this.applyFilters();
  }

  onSearchInput(value: string): void {
    this.searchText = value;
    this.applyFilters();
  }

  isJustUpdated(id: number): boolean { return this.updatedBidIds.has(id); }

  clearRecentlyViewed(): void {
    this.recentlyViewed.clear();
    this.recentlyViewedList = [];
  }

  goToAuction(auction: AuctionCard): void {
    if (auction.status !== 'LIVE') return;
    const role = this.authService.getRole();
    const userId = this.authService.getUserId();
    const isOwnProduct = role === 'SELLER' && auction.sellerId === userId;
    if (isOwnProduct) return;
    if (role !== 'BUYER' && role !== 'SELLER') return;
    this.recentlyViewed.add(auction);
    this.recentlyViewedList = this.recentlyViewed.getAll();
    void this.router.navigate(['/auction', auction.auctionId], { state: { auction } });
  }

  scrollToAuctions(): void {
    document.getElementById('featured-auctions')?.scrollIntoView({ behavior: 'smooth' });
  }

  get role(): string | null { return this.authService.getRole(); }
  get currentUserId(): number | null { return this.authService.getUserId(); }
  get liveCount(): number   { return this.auctions.filter(a => a.status === 'LIVE').length; }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(amount);
  }

  ngOnDestroy(): void {
    if (this.timerInterval) clearInterval(this.timerInterval);
    if (this.wsSub)         this.wsSub.unsubscribe();
    if (this.searchSub)     this.searchSub.unsubscribe();
  }
}
