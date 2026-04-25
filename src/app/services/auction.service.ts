import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { AuctionCard } from '../models/auction.model';

@Injectable({ providedIn: 'root' })
export class AuctionService {
  private readonly baseUrl = `${environment.apiBaseUrl}/api/auction`;

  constructor(private http: HttpClient) {}

  getFeaturedAuctions(): Observable<AuctionCard[]> {
    return this.http.get<AuctionCard[]>(`${this.baseUrl}/all`);
  }

  placeBidRest(productId: number, amount: number): Observable<string> {
    return this.http.post(
      `${environment.apiBaseUrl}/api/products/bid/${productId}?amount=${amount}`,
      {},
      { responseType: 'text' }
    );
  }
}
