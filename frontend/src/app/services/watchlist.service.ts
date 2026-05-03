import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class WatchlistService {
  private readonly KEY = 'as_watchlist';
  private ids$ = new BehaviorSubject<number[]>(this.load());

  watchlist$ = this.ids$.asObservable();

  private load(): number[] {
    try { return JSON.parse(localStorage.getItem(this.KEY) || '[]'); }
    catch { return []; }
  }

  isWatched(id: number): boolean {
    return this.ids$.value.includes(id);
  }

  toggle(id: number): void {
    const list = [...this.ids$.value];
    const idx = list.indexOf(id);
    if (idx >= 0) list.splice(idx, 1);
    else list.push(id);
    localStorage.setItem(this.KEY, JSON.stringify(list));
    this.ids$.next(list);
  }

  get count(): number {
    return this.ids$.value.length;
  }
}
