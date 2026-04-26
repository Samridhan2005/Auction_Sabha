import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class AdminService {
  private readonly baseUrl = `${environment.apiBaseUrl}/api/admin/auction`;

  constructor(private http: HttpClient) {}

  startAuction(auctionId: number): Observable<string> {
    return this.http.post(`${this.baseUrl}/${auctionId}/start`, {}, { responseType: 'text' });
  }

  stopAuction(auctionId: number): Observable<string> {
    return this.http.post(`${this.baseUrl}/${auctionId}/stop`, {}, { responseType: 'text' });
  }
}
