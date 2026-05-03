import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface AdminProduct {
  productId: number;
  productName: string;
  description: string;
  imageUrl: string | null;
  startingPrice: number;
  verificationStatus: string;
  adminRemarks: string | null;
  aiVerdict: string | null;
  submittedAt: string | null;
  preferredDate: string | null;
  preferredSlot: number;
  categoryName: string;
  sellerName: string;
  auctionId: number | null;
  auctionStatus: string | null;
  currentBid: number;
  highestBidder: string | null;
  confirmedStartTime: string | null;
  slotEndTime: string | null;
  startTime: string | null;
  endTime: string | null;
}

@Injectable({ providedIn: 'root' })
export class AdminService {
  private readonly baseUrl = `${environment.apiBaseUrl}/api/admin`;

  constructor(private http: HttpClient) {}

  startAuction(auctionId: number): Observable<string> {
    return this.http.post(`${this.baseUrl}/auction/${auctionId}/start`, {}, { responseType: 'text' });
  }

  stopAuction(auctionId: number): Observable<string> {
    return this.http.post(`${this.baseUrl}/auction/${auctionId}/stop`, {}, { responseType: 'text' });
  }

  getProductRegistry(): Observable<AdminProduct[]> {
    return this.http.get<AdminProduct[]>(`${this.baseUrl}/products`);
  }
}
