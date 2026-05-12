import { User } from './user.model';

export type VerificationStatus = 'PENDING' | 'APPROVED' | 'REJECTED';
export interface Category {
  categoryId: number;
  categoryName: string;
}

export interface Product {
  productId: number;
  seller: User;
  category: Category;
  productName: string;
  description: string;
  imageUrl: string;
  documentsUrl: string | null;
  startingPrice: number;
  verificationStatus: VerificationStatus;
  adminRemarks: string | null;
  submittedAt: string;
  preferredDate: string | null;
}

export type AuctionStatus = 'CREATED' | 'LIVE' | 'ENDED';

export interface SellerProductSummary {
  productId: number;
  productName: string;
  description: string;
  imageUrl: string | null;
  startingPrice: number;
  verificationStatus: VerificationStatus;
  adminRemarks: string | null;
  submittedAt: string;
  categoryName: string;
  auctionId: number | null;
  auctionStatus: AuctionStatus | null;
  finalBid: number;
  winner: string | null;
  profit: number;
  endTime: string | null;
}

export interface ProductSubmitRequest {
  productName: string;
  description: string;
  imageUrl: string;
  documentsUrl: string;
  startingPrice: number;
  categoryId: number;
  preferredDate: string;
}

export interface AiRiskResult {
  riskScore: number;
  flags: string[];
  summary: string;
  error?: string;
}
