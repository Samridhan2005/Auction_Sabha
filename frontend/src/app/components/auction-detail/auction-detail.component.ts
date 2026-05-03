import { Component, OnInit, OnDestroy, NgZone } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { WebsocketService } from '../../services/websocket.service';
import { AuthService } from '../../services/auth.service';
import { AuctionService, LeaderboardEntry } from '../../services/auction.service';
import { AuctionCard, BidUpdate } from '../../models/auction.model';

interface ToastMessage {
  type: 'success' | 'danger' | 'info' | 'warning';
  text: string;
  id: number;
}

@Component({
  selector: 'app-auction-detail',
  templateUrl: './auction-detail.component.html',
  styleUrls: ['./auction-detail.component.css']
})
export class AuctionDetailComponent implements OnInit, OnDestroy {
  auction: AuctionCard | null = null;
  auctionId!: number;
  currentBid = 0;
  highestBidder: string | null = null;
  highestBidderId: number | null = null;
  auctionStatus = '';
  bidAmount: number | null = null;
  isConnected = false;
  isSubmitting = false;
  toasts: ToastMessage[] = [];
  private toastCounter = 0;
  bidHistory: { amount: number; bidder: string; time: Date }[] = [];

  // Timer — starts on first bid, not when auction goes LIVE
  timeLeft = 100;
  readonly TIMER_SECONDS = 100;
  private timerHandle: ReturnType<typeof setInterval> | null = null;
  timerStarted = false;

  // Winner popup
  showWinnerPopup = false;

  // Leaderboard
  leaderboard: LeaderboardEntry[] = [];

  // Navigate countdown after auction ends
  navigateCountdown = 0;
  private navHandle: ReturnType<typeof setInterval> | null = null;

  // Quick bid increments
  quickBidAmounts = [100, 200, 500, 1000, 2000, 5000];

  private wsSub!: Subscription;
  private statusSub!: Subscription;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private wsService: WebsocketService,
    private authService: AuthService,
    private auctionService: AuctionService,
    private ngZone: NgZone
  ) {}

  ngOnInit(): void {
    this.auctionId = Number(this.route.snapshot.paramMap.get('id'));

    const state = history.state as { auction?: AuctionCard };
    if (state?.auction) {
      this.auction = state.auction;
      this.currentBid = this.auction.currentBid;
      this.highestBidder = this.auction.highestBidder;
      this.auctionStatus = this.auction.status;
      // Timer only starts on first bid — do NOT start here
    }

    this.connectWebSocket();
  }

  // ── Timer ────────────────────────────────────────────────────────────────────

  private startTimer(): void {
    this.timerStarted = true;
    this.clearTimer();
    this.timeLeft = this.TIMER_SECONDS;
    this.ngZone.runOutsideAngular(() => {
      this.timerHandle = setInterval(() => {
        this.ngZone.run(() => {
          this.timeLeft--;
          if (this.timeLeft <= 0) {
            this.timeLeft = 0;
            this.handleAuctionEnd();
          }
        });
      }, 1000);
    });
  }

  private clearTimer(): void {
    if (this.timerHandle !== null) {
      clearInterval(this.timerHandle);
      this.timerHandle = null;
    }
  }

  // ── Auction End ──────────────────────────────────────────────────────────────

  private handleAuctionEnd(): void {
    if (this.auctionStatus === 'ENDED') return;
    this.auctionStatus = 'ENDED';
    this.clearTimer();

    // Check if the current user is the winner
    const myId = this.authService.getUserId();
    if (myId && this.highestBidderId === myId) {
      this.showWinnerPopup = true;
    } else {
      const winnerMsg = this.highestBidder
        ? `Auction closed! Winner: ${this.highestBidder} — ${this.formatCurrency(this.currentBid)}`
        : 'Auction closed. No bids were placed.';
      this.showToast('info', winnerMsg);
    }

    // Load final leaderboard
    this.auctionService.getLeaderboard(this.auctionId).subscribe({
      next: (lb) => { this.leaderboard = lb; },
      error: () => {}
    });

    this.startNavigateCountdown();
  }

  closeWinnerPopup(): void {
    this.showWinnerPopup = false;
  }

  private startNavigateCountdown(): void {
    this.navigateCountdown = 5;
    this.ngZone.runOutsideAngular(() => {
      this.navHandle = setInterval(() => {
        this.ngZone.run(() => {
          this.navigateCountdown--;
          if (this.navigateCountdown <= 0) {
            this.clearNavHandle();
            void this.router.navigate(['/home']);
          }
        });
      }, 1000);
    });
  }

  private clearNavHandle(): void {
    if (this.navHandle !== null) {
      clearInterval(this.navHandle);
      this.navHandle = null;
    }
  }

  get timerUrgent(): boolean {
    return this.timeLeft <= 10 && this.auctionStatus === 'LIVE';
  }

  get timerDashOffset(): number {
    const circumference = 2 * Math.PI * 45;
    return circumference * (1 - this.timeLeft / this.TIMER_SECONDS);
  }

  // ── Quick Bid ────────────────────────────────────────────────────────────────

  setQuickBid(increment: number): void {
    this.bidAmount = this.currentBid + increment;
  }

  // ── WebSocket ────────────────────────────────────────────────────────────────

  private connectWebSocket(): void {
    this.wsService.connect();

    this.statusSub = this.wsService.connectionStatus$.subscribe(status => {
      this.isConnected = status === 'CONNECTED';
      if (status === 'ERROR') {
        this.showToast('danger', 'WebSocket connection failed. Real-time updates unavailable.');
      }
    });

    this.wsSub = this.wsService.messages$.subscribe((msg: BidUpdate) => {
      if (msg.type === 'ERROR') {
        this.showToast('danger', msg.message || 'An error occurred.');
        this.isSubmitting = false;
        return;
      }

      if (msg.type === 'AUCTION_STARTED') {
        this.auctionStatus = 'LIVE';
        this.timerStarted = false; // reset so first bid starts the timer
        this.showToast('success', 'Auction has started! Place your bid to start the 100s countdown.');
        return;
      }

      if (msg.type === 'AUCTION_STOPPED') {
        this.handleAuctionEnd();
        return;
      }

      // Regular bid update
      if (msg.auctionId === this.auctionId || !msg.auctionId) {
        if (msg.amount !== undefined && msg.amount > this.currentBid) {
          const prevBidder = this.highestBidder;
          this.currentBid = msg.amount;
          this.highestBidder = msg.highestBidder || (msg.bidderId?.toString() ?? null);
          if (msg.bidderId !== undefined) this.highestBidderId = msg.bidderId;

          // Start timer on first bid; reset on every subsequent bid
          if (this.auctionStatus === 'LIVE') {
            this.startTimer();
          }

          this.bidHistory.unshift({
            amount: msg.amount,
            bidder: this.highestBidder ?? 'Unknown',
            time: new Date()
          });

          const myId = this.authService.getUserId();
          if (myId && msg.bidderId === myId) {
            this.showToast('success', `Your bid of ${this.formatCurrency(msg.amount)} is now the highest!`);
          } else if (prevBidder) {
            this.showToast('info', `New highest bid: ${this.formatCurrency(msg.amount)}`);
          }
        }
        this.isSubmitting = false;
      }
    });
  }

  placeBid(): void {
    if (!this.bidAmount || this.bidAmount <= this.currentBid) {
      this.showToast('warning', `Bid must be greater than current bid of ${this.formatCurrency(this.currentBid)}.`);
      return;
    }

    const userId = this.authService.getUserId();
    if (!userId) {
      this.showToast('danger', 'User ID not found. Please set your User ID in Wallet settings.');
      return;
    }

    this.isSubmitting = true;
    this.wsService.sendBid({
      auctionId: this.auctionId,
      bidderId: userId,
      amount: this.bidAmount
    });

    this.bidAmount = null;
  }

  showToast(type: ToastMessage['type'], text: string): void {
    const id = ++this.toastCounter;
    this.toasts.push({ type, text, id });
    setTimeout(() => {
      this.toasts = this.toasts.filter(t => t.id !== id);
    }, 5000);
  }

  dismissToast(id: number): void {
    this.toasts = this.toasts.filter(t => t.id !== id);
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
      maximumFractionDigits: 0
    }).format(amount);
  }

  goBack(): void {
    void this.router.navigate(['/home']);
  }

  ngOnDestroy(): void {
    this.clearTimer();
    this.clearNavHandle();
    this.wsService.disconnect();
    if (this.wsSub) this.wsSub.unsubscribe();
    if (this.statusSub) this.statusSub.unsubscribe();
  }
}
