import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { AuctionCard } from '../models/auction.model';

export interface LeaderboardEntry { rank: number; bidder: string; maxBid: number; }
export interface SlotInfo { slot: number; label: string; availability: 'AVAILABLE' | 'TENTATIVE' | 'UNAVAILABLE'; }

@Injectable({ providedIn: 'root' })
export class AuctionService {
  private readonly baseUrl = `${environment.apiBaseUrl}/api/auction`;

  constructor(private http: HttpClient) {}

  getFeaturedAuctions(): Observable<AuctionCard[]> {
    return this.http.get<AuctionCard[]>(`${this.baseUrl}/all`);
  }

  getLeaderboard(auctionId: number): Observable<LeaderboardEntry[]> {
    return this.http.get<LeaderboardEntry[]>(`${this.baseUrl}/${auctionId}/leaderboard`);
  }

  getAvailableSlots(date: string): Observable<SlotInfo[]> {
    return this.http.get<SlotInfo[]>(`${this.baseUrl}/slots?date=${date}`);
  }

  placeBidRest(productId: number, amount: number): Observable<string> {
    return this.http.post(
      `${environment.apiBaseUrl}/api/products/bid/${productId}?amount=${amount}`,
      {},
      { responseType: 'text' }
    );
  }
}
