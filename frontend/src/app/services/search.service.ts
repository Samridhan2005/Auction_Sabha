import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class SearchService {
  private q$ = new BehaviorSubject<string>('');
  query$ = this.q$.asObservable();

  setQuery(q: string): void { this.q$.next(q.trim()); }
  clear(): void { this.q$.next(''); }
  get current(): string { return this.q$.value; }
}
