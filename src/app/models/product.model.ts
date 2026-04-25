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
  startingPrice: number;
  verificationStatus: VerificationStatus;
  adminRemarks: string | null;
  submittedAt: string;
}

export interface ProductSubmitRequest {
  productName: string;
  description: string;
  imageUrl: string;
  startingPrice: number;
  categoryId: number;
}
