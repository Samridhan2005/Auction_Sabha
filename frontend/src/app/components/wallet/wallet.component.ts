import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { WalletService } from '../../services/wallet.service';
import { AuthService } from '../../services/auth.service';
import { Wallet } from '../../models/wallet.model';
import { Transaction } from '../../models/transaction.model';

@Component({
  selector: 'app-wallet',
  templateUrl: './wallet.component.html',
  styleUrls: ['./wallet.component.css']
})
export class WalletComponent implements OnInit {
  wallet: Wallet | null = null;
  transactions: Transaction[] = [];
  walletLoading = false;
  txLoading = false;
  walletError = '';
  txError = '';
  depositSuccess = '';
  depositError = '';
  withdrawSuccess = '';
  withdrawError = '';
  depositLoading = false;
  withdrawLoading = false;

  depositForm!: FormGroup;
  withdrawForm!: FormGroup;

  // For users who don't have userId stored (login-only users)
  manualUserId: number | null = null;
  showUserIdInput = false;

  constructor(
    private fb: FormBuilder,
    private walletService: WalletService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.depositForm = this.fb.group({
      amount: [null, [Validators.required, Validators.min(1)]]
    });
    this.withdrawForm = this.fb.group({
      amount: [null, [Validators.required, Validators.min(1)]]
    });

    const userId = this.authService.getUserId();
    if (userId) {
      // userId found automatically — don't ask the user to re-enter it
      this.loadWallet(userId);
      this.loadTransactions(userId);
    } else {
      // userId missing from localStorage — ask the user to enter it manually
      this.showUserIdInput = true;
    }
  }

  get role(): string | null {
    return this.authService.getRole();
  }

  get effectiveUserId(): number | null {
    return this.authService.getUserId() ?? this.manualUserId;
  }

  applyManualUserId(): void {
    if (!this.manualUserId || this.manualUserId < 1) return;
    this.authService.setUserId(this.manualUserId);
    this.showUserIdInput = false;
    this.loadWallet(this.manualUserId);
    this.loadTransactions(this.manualUserId);
  }

  loadWallet(userId: number): void {
    this.walletLoading = true;
    this.walletError = '';
    this.walletService.getWallet(userId).subscribe({
      next: (data: Wallet) => {
        this.wallet = data;
        this.walletLoading = false;
      },
      error: (err: { status: number; error?: { message?: string } }) => {
        this.walletLoading = false;
        if (err.status === 404) {
          if (this.showUserIdInput) {
            // User manually entered an ID — it's likely wrong
            this.walletError = 'No wallet found for that user ID. Please check the ID and try again.';
          } else {
            // userId was auto-detected from localStorage — ID is correct, wallet is missing
            this.walletError = 'Your wallet could not be found. Please contact the admin.';
          }
          // Do NOT set showUserIdInput = true here — that misleads the user into thinking
          // their auto-detected user ID is wrong when it is actually correct.
        } else if (err.status === 0) {
          this.walletError = 'Cannot connect to server. Please try again.';
        } else {
          this.walletError = err.error?.message || 'Failed to load wallet.';
        }
      }
    });
  }

  loadTransactions(userId: number): void {
    this.txLoading = true;
    this.txError = '';
    this.walletService.getTransactions(userId).subscribe({
      next: (data: Transaction[]) => {
        this.transactions = data;
        this.txLoading = false;
      },
      error: (err: { status: number }) => {
        this.txLoading = false;
        this.txError = err.status === 0
          ? 'Cannot connect to server.'
          : 'Failed to load transaction history.';
      }
    });
  }

  onDeposit(): void {
    if (this.depositForm.invalid) {
      this.depositForm.markAllAsTouched();
      return;
    }
    const userId = this.effectiveUserId;
    if (!userId) return;

    this.depositLoading = true;
    this.depositSuccess = '';
    this.depositError = '';

    const amount = this.depositForm.value as { amount: number };
    this.walletService.deposit(userId, { amount: amount.amount }).subscribe({
      next: (data: Wallet) => {
        this.wallet = data;
        this.depositLoading = false;
        this.depositSuccess = `Successfully deposited ₹${amount.amount.toLocaleString('en-IN')}.`;
        this.depositForm.reset();
        this.loadTransactions(userId);
      },
      error: (err: { status: number; error?: { message?: string } }) => {
        this.depositLoading = false;
        this.depositError = err.error?.message || 'Deposit failed. Please try again.';
      }
    });
  }

  onWithdraw(): void {
    if (this.withdrawForm.invalid) {
      this.withdrawForm.markAllAsTouched();
      return;
    }
    const userId = this.effectiveUserId;
    if (!userId) return;

    this.withdrawLoading = true;
    this.withdrawSuccess = '';
    this.withdrawError = '';

    const amount = this.withdrawForm.value as { amount: number };
    this.walletService.withdraw(userId, { amount: amount.amount }).subscribe({
      next: (data: Wallet) => {
        this.wallet = data;
        this.withdrawLoading = false;
        this.withdrawSuccess = `Successfully withdrew ₹${amount.amount.toLocaleString('en-IN')}.`;
        this.withdrawForm.reset();
        this.loadTransactions(userId);
      },
      error: (err: { status: number; error?: { message?: string } }) => {
        this.withdrawLoading = false;
        this.withdrawError = err.error?.message || 'Withdrawal failed. Ensure sufficient balance.';
      }
    });
  }

  refreshAll(): void {
    const userId = this.effectiveUserId;
    if (!userId) return;
    this.loadWallet(userId);
    this.loadTransactions(userId);
  }

  getTransactionTypeClass(type: string): string {
    switch (type) {
      case 'DEPOSIT': return 'text-success';
      case 'WITHDRAWAL': return 'text-danger';
      case 'BID_FREEZE': return 'text-warning';
      case 'BID_REFUND': return 'text-info';
      case 'BID_DEDUCT': return 'text-danger';
      case 'WIN': return 'text-success';
      default: return 'text-secondary';
    }
  }

  getTransactionIcon(type: string): string {
    switch (type) {
      case 'DEPOSIT': return 'bi-arrow-down-circle-fill text-success';
      case 'WITHDRAWAL': return 'bi-arrow-up-circle-fill text-danger';
      case 'BID_FREEZE': return 'bi-lock-fill text-warning';
      case 'BID_REFUND': return 'bi-arrow-counterclockwise text-info';
      case 'BID_DEDUCT': return 'bi-dash-circle-fill text-danger';
      case 'WIN': return 'bi-trophy-fill text-success';
      default: return 'bi-circle text-secondary';
    }
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
      maximumFractionDigits: 2
    }).format(amount);
  }
}
