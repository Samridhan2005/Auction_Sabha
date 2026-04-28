import { Injectable } from '@angular/core';
import { AuctionCard } from '../models/auction.model';

@Injectable({ providedIn: 'root' })
export class RecentlyViewedService {
  private readonly KEY = 'as_recently_viewed';
  private readonly MAX = 8;

  add(auction: AuctionCard): void {
    const list = this.getAll().filter(a => a.auctionId !== auction.auctionId);
    list.unshift({ ...auction });
    localStorage.setItem(this.KEY, JSON.stringify(list.slice(0, this.MAX)));
  }

  getAll(): AuctionCard[] {
    try { return JSON.parse(localStorage.getItem(this.KEY) || '[]'); }
    catch { return []; }
  }

  clear(): void {
    localStorage.removeItem(this.KEY);
    this._version++;
  }

  private _version = 0;
  get version(): number { return this._version; }
}
