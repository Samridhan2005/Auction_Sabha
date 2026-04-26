import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { WebsocketService } from '../../services/websocket.service';
import { AuthService } from '../../services/auth.service';
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
  auctionStatus = '';
  bidAmount: number | null = null;
  isConnected = false;
  isSubmitting = false;
  toasts: ToastMessage[] = [];
  private toastCounter = 0;
  bidHistory: { amount: number; bidder: string; time: Date }[] = [];

  private wsSub!: Subscription;
  private statusSub!: Subscription;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private wsService: WebsocketService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.auctionId = Number(this.route.snapshot.paramMap.get('id'));

    const state = history.state as { auction?: AuctionCard };
    if (state?.auction) {
      this.auction = state.auction;
      this.currentBid = this.auction.currentBid;
      this.highestBidder = this.auction.highestBidder;
      this.auctionStatus = this.auction.status;
    }

    this.connectWebSocket();
  }

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
        this.showToast('success', 'Auction has started! Place your bids now.');
        return;
      }

      if (msg.type === 'AUCTION_STOPPED') {
        this.auctionStatus = 'ENDED';
        this.showToast('info', 'Auction has ended.');
        return;
      }

      // Regular bid update
      if (msg.auctionId === this.auctionId || !msg.auctionId) {
        if (msg.amount !== undefined && msg.amount > this.currentBid) {
          const prevBidder = this.highestBidder;
          this.currentBid = msg.amount;
          this.highestBidder = msg.highestBidder || (msg.bidderId?.toString() ?? null);

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
    this.wsService.disconnect();
    if (this.wsSub) this.wsSub.unsubscribe();
    if (this.statusSub) this.statusSub.unsubscribe();
  }
}
