import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subscription } from 'rxjs';
import { AuctionService } from '../../services/auction.service';
import { AdminService, AdminProduct } from '../../services/admin.service';
import { WebsocketService } from '../../services/websocket.service';
import { AuctionCard, BidUpdate } from '../../models/auction.model';
import { VerifierApplicationService } from '../../services/verifier-application.service';
import { VerifierApplication, VerifierApprovalResponse } from '../../models/verifier-application.model';

type AdminTab = 'overview' | 'auctions' | 'products' | 'schedule' | 'verifiers';

interface ToastMessage  { type: 'success' | 'danger' | 'info'; text: string; id: number; }
interface LiveBid       { auctionId: number; productName: string; amount: number; bidder: string; time: Date; }
interface ScheduleGroup { label: string; dateKey: string; items: AdminProduct[]; }

@Component({
  selector: 'app-admin',
  templateUrl: './admin.component.html',
  styleUrls: ['./admin.component.css']
})
export class AdminComponent implements OnInit, OnDestroy {

  activeTab: AdminTab = 'overview';

  // ── Featured auctions (Manage tab + live monitor) ──────────────────────────
  auctions: AuctionCard[] = [];
  isLoading = false;
  errorMessage = '';
  auctionActionLoading: { [key: number]: boolean } = {};

  // ── Product registry ────────────────────────────────────────────────────────
  adminProducts: AdminProduct[] = [];
  isLoadingProducts = false;
  productsError = '';

  // Product tab filters
  productSearch   = '';
  verifFilter     = 'ALL';
  auctionFilter   = 'ALL';

  // ── Live monitoring ─────────────────────────────────────────────────────────
  liveBidFeed: LiveBid[] = [];
  isWsConnected = false;
  private wsSub?: Subscription;
  private statusSub?: Subscription;

  // ── Verifier Applications ────────────────────────────────────────────────────
  verifierApplications: VerifierApplication[] = [];
  isLoadingApps = false;
  appsError = '';
  appRemarks:      { [key: number]: string  } = {};
  approveLoading:  { [key: number]: boolean } = {};
  rejectLoading:   { [key: number]: boolean } = {};

  // ── Credentials modal ────────────────────────────────────────────────────────
  credentialsModal = { visible: false, name: '', email: '', password: '', emailSent: false };
  passwordVisible = false;

  toasts: ToastMessage[] = [];
  private toastCounter = 0;

  constructor(
    private auctionService: AuctionService,
    private adminService: AdminService,
    private wsService: WebsocketService,
    private verifierAppService: VerifierApplicationService
  ) {}

  ngOnInit(): void {
    this.loadAuctions();
    this.loadProductRegistry();
    this.loadVerifierApplications();
    this.connectWebSocket();
  }

  ngOnDestroy(): void {
    this.wsService.disconnect();
    this.wsSub?.unsubscribe();
    this.statusSub?.unsubscribe();
  }

  switchTab(tab: AdminTab): void { this.activeTab = tab; }

  // ── WebSocket ────────────────────────────────────────────────────────────────
  private connectWebSocket(): void {
    this.wsService.connect();
    this.statusSub = this.wsService.connectionStatus$.subscribe(s => { this.isWsConnected = s === 'CONNECTED'; });
    this.wsSub = this.wsService.messages$.subscribe((msg: BidUpdate) => {
      if (msg.type === 'AUCTION_STARTED') {
        const a = this.auctions.find(x => x.auctionId === msg.auctionId);
        if (a) a.status = 'LIVE';
        const p = this.adminProducts.find(x => x.auctionId === msg.auctionId);
        if (p) p.auctionStatus = 'LIVE';
        return;
      }
      if (msg.type === 'AUCTION_STOPPED') {
        const a = this.auctions.find(x => x.auctionId === msg.auctionId);
        if (a) a.status = 'ENDED';
        const p = this.adminProducts.find(x => x.auctionId === msg.auctionId);
        if (p) p.auctionStatus = 'ENDED';
        return;
      }
      if (msg.type === 'ERROR' || !msg.auctionId || !msg.amount) return;

      const auction = this.auctions.find(x => x.auctionId === msg.auctionId);
      if (auction) {
        auction.currentBid = msg.amount;
        auction.highestBidder = msg.highestBidder ?? auction.highestBidder;
      }
      const prod = this.adminProducts.find(x => x.auctionId === msg.auctionId);
      if (prod) { prod.currentBid = msg.amount; prod.highestBidder = msg.highestBidder ?? prod.highestBidder; }

      this.liveBidFeed.unshift({
        auctionId: msg.auctionId,
        productName: auction?.productName ?? `Auction #${msg.auctionId}`,
        amount: msg.amount,
        bidder: msg.highestBidder ?? 'Unknown',
        time: new Date()
      });
      if (this.liveBidFeed.length > 20) this.liveBidFeed.pop();
    });
  }

  // ── Auctions ─────────────────────────────────────────────────────────────────
  loadAuctions(): void {
    this.isLoading = true;
    this.errorMessage = '';
    this.auctionService.getFeaturedAuctions().subscribe({
      next: (data) => { this.auctions = data; this.isLoading = false; },
      error: (err: { status: number }) => {
        this.isLoading = false;
        this.errorMessage = err.status === 0 ? 'Cannot connect to server.' : 'Failed to load auctions.';
      }
    });
  }

  startAuction(auctionId: number): void {
    this.auctionActionLoading[auctionId] = true;
    this.adminService.startAuction(auctionId).subscribe({
      next: (msg) => { this.auctionActionLoading[auctionId] = false; this.showToast('success', msg || 'Auction started.'); this.loadAuctions(); },
      error: (err: { error?: { message?: string } }) => { this.auctionActionLoading[auctionId] = false; this.showToast('danger', err.error?.message || 'Failed to start.'); }
    });
  }

  stopAuction(auctionId: number): void {
    this.auctionActionLoading[auctionId] = true;
    this.adminService.stopAuction(auctionId).subscribe({
      next: (msg) => { this.auctionActionLoading[auctionId] = false; this.showToast('success', msg || 'Auction stopped.'); this.loadAuctions(); this.loadProductRegistry(); },
      error: (err: { error?: { message?: string } }) => { this.auctionActionLoading[auctionId] = false; this.showToast('danger', err.error?.message || 'Failed to stop.'); }
    });
  }

  // ── Product Registry ─────────────────────────────────────────────────────────
  loadProductRegistry(): void {
    this.isLoadingProducts = true;
    this.productsError = '';
    this.adminService.getProductRegistry().subscribe({
      next: (data) => { this.adminProducts = data; this.isLoadingProducts = false; },
      error: (err: { status: number }) => {
        this.isLoadingProducts = false;
        this.productsError = err.status === 0 ? 'Cannot connect to server.' : 'Failed to load products.';
      }
    });
  }

  get filteredProducts(): AdminProduct[] {
    const q = this.productSearch.toLowerCase();
    return this.adminProducts.filter(p => {
      const verifOk  = this.verifFilter   === 'ALL' || p.verificationStatus === this.verifFilter;
      const auctionOk = this.auctionFilter === 'ALL' || (p.auctionStatus ?? 'NONE') === this.auctionFilter;
      const searchOk = !q || p.productName.toLowerCase().includes(q)
                          || p.sellerName.toLowerCase().includes(q)
                          || p.categoryName.toLowerCase().includes(q);
      return verifOk && auctionOk && searchOk;
    });
  }

  // ── Schedule ─────────────────────────────────────────────────────────────────
  get scheduleGroups(): ScheduleGroup[] {
    const withAuction = this.adminProducts.filter(p => p.auctionId !== null);
    const map = new Map<string, AdminProduct[]>();

    withAuction.forEach(p => {
      const timeStr = p.confirmedStartTime || p.startTime;
      const dateKey = timeStr ? timeStr.substring(0, 10) : 'unscheduled';
      if (!map.has(dateKey)) map.set(dateKey, []);
      map.get(dateKey)!.push(p);
    });

    return Array.from(map.entries())
      .sort(([a], [b]) => a === 'unscheduled' ? 1 : b === 'unscheduled' ? -1 : a.localeCompare(b))
      .map(([dateKey, items]) => ({
        label: this.formatScheduleDate(dateKey),
        dateKey,
        items: items.sort((a, b) => {
          const ta = a.confirmedStartTime || a.startTime || '';
          const tb = b.confirmedStartTime || b.startTime || '';
          return ta.localeCompare(tb);
        })
      }));
  }

  formatScheduleDate(dateKey: string): string {
    if (dateKey === 'unscheduled') return 'Unscheduled';
    const today    = new Date().toISOString().substring(0, 10);
    const tomorrow = new Date(Date.now() + 86_400_000).toISOString().substring(0, 10);
    if (dateKey === today)    return 'Today';
    if (dateKey === tomorrow) return 'Tomorrow';
    const d = new Date(dateKey + 'T00:00:00');
    return d.toLocaleDateString('en-IN', { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' });
  }

  formatTime(isoStr: string | null): string {
    if (!isoStr) return '—';
    return new Date(isoStr).toLocaleTimeString('en-IN', { hour: '2-digit', minute: '2-digit', hour12: true });
  }

  // ── Overview Stats ────────────────────────────────────────────────────────────
  get stats() {
    const p = this.adminProducts;
    return {
      totalProducts:  p.length,
      pendingVerif:   p.filter(x => x.verificationStatus === 'PENDING').length,
      approved:       p.filter(x => x.verificationStatus === 'APPROVED').length,
      rejected:       p.filter(x => x.verificationStatus === 'REJECTED').length,
      totalAuctions:  p.filter(x => x.auctionId !== null).length,
      live:           p.filter(x => x.auctionStatus === 'LIVE').length,
      scheduled:      p.filter(x => x.auctionStatus === 'CREATED').length,
      ended:          p.filter(x => x.auctionStatus === 'ENDED').length,
      totalVolume:    p.filter(x => x.auctionStatus === 'ENDED').reduce((s, x) => s + x.currentBid, 0)
    };
  }

  get liveAuctions(): AuctionCard[] { return this.auctions.filter(a => a.status === 'LIVE'); }

  // ── Verifier Applications ─────────────────────────────────────────────────────
  loadVerifierApplications(): void {
    this.isLoadingApps = true;
    this.appsError = '';
    this.verifierAppService.getAll().subscribe({
      next: (data) => { this.verifierApplications = data; this.isLoadingApps = false; },
      error: (err: { status: number }) => { this.isLoadingApps = false; this.appsError = err.status === 0 ? 'Cannot connect.' : 'Failed to load applications.'; }
    });
  }

  approveApplication(id: number): void {
    this.approveLoading[id] = true;
    this.verifierAppService.approve(id, this.appRemarks[id] || '').subscribe({
      next: (response: VerifierApprovalResponse) => {
        this.approveLoading[id] = false;
        this.credentialsModal = { visible: true, name: response?.application?.name || 'Verifier', email: response?.application?.email || '', password: response?.tempPassword || '', emailSent: response?.emailSent || false };
        this.passwordVisible = false;
        if (response?.application) this.updateAppInList(response.application);
      },
      error: (err: any) => { this.approveLoading[id] = false; this.appsError = err?.error || 'Failed to approve.'; }
    });
  }

  rejectApplication(id: number): void {
    const remarks = this.appRemarks[id] || '';
    if (!remarks.trim()) { this.showToast('info', 'Please enter a reason before rejecting.'); return; }
    this.rejectLoading[id] = true;
    this.verifierAppService.reject(id, remarks).subscribe({
      next: (updated) => { this.rejectLoading[id] = false; this.updateAppInList(updated); this.showToast('success', `Application rejected.`); },
      error: (err: { error?: string }) => { this.rejectLoading[id] = false; this.showToast('danger', err.error || 'Failed to reject.'); }
    });
  }

  private updateAppInList(updated: VerifierApplication): void {
    const idx = this.verifierApplications.findIndex(a => a.applicationId === updated.applicationId);
    if (idx !== -1) this.verifierApplications[idx] = updated;
  }

  closeCredentialsModal(): void { this.credentialsModal.visible = false; }
  copyPassword(): void { navigator.clipboard.writeText(this.credentialsModal.password).then(() => this.showToast('success', 'Password copied.')); }
  copyEmail():    void { navigator.clipboard.writeText(this.credentialsModal.email).then(()    => this.showToast('success', 'Email copied.')); }

  get pendingAppsCount(): number { return this.verifierApplications.filter(a => a.status === 'PENDING').length; }

  showToast(type: ToastMessage['type'], text: string): void {
    const id = ++this.toastCounter;
    this.toasts.push({ type, text, id });
    setTimeout(() => { this.toasts = this.toasts.filter(t => t.id !== id); }, 5000);
  }
  dismissToast(id: number): void { this.toasts = this.toasts.filter(t => t.id !== id); }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(amount);
  }
}
