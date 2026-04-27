import { Product } from './product.model';

export type AuctionStatus = 'CREATED' | 'LIVE' | 'ENDED';

export interface Auction {
  auctionId: number;
  product: Product;
  currentBid: number;
  highestBidder: string | null;
  startTime: string | null;
  endTime: string | null;
  status: AuctionStatus;
  isFeatured: boolean;
  remainingTime?: {
    hours: number;
    minutes: number;
    seconds: number;
    isExpired: boolean;
  };
}

export interface AuctionCard {
  auctionId: number;
  status: AuctionStatus;
  currentBid: number;
  isFeatured: boolean;
  startTime: string | null;
  endTime: string | null;
  highestBidder: string | null;
  productId: number;
  productName: string;
  description: string;
  imageUrl: string | null;
  startingPrice: number;
  sellerName: string;
  categoryId: number;
  categoryName: string;
}

export interface BidMessage {
  auctionId: number;
  bidderId: number;
  amount: number;
}

export interface BidUpdate {
  auctionId?: number;
  bidderId?: number;
  amount?: number;
  currentBid?: number;
  highestBidder?: string;
  type?: 'AUCTION_STARTED' | 'AUCTION_STOPPED' | 'ERROR';
  message?: string;
}
