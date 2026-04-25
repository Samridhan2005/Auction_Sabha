export type TransactionType = 'DEPOSIT' | 'WITHDRAWAL' | 'BID_FREEZE' | 'BID_REFUND' | 'BID_DEDUCT' | 'WIN';
export type TransactionStatus = 'SUCCESS' | 'FAILED' | 'PENDING';

export interface Transaction {
  transactionId: number;
  walletId: number;
  auctionId: number | null;
  type: TransactionType;
  amount: number;
  status: TransactionStatus;
  createdAt: string;
}
