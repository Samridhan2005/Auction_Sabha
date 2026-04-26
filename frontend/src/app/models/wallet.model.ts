export interface Wallet {
  walletId: number;
  userId: number;
  availableBalance: number;
  frozenBalance: number;
  lastUpdated: string;
}

export interface WalletDepositRequest {
  amount: number;
}

export interface WalletWithdrawRequest {
  amount: number;
}
