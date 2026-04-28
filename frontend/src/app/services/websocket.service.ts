import { Injectable, OnDestroy } from '@angular/core';
import { Subject, BehaviorSubject, Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { BidMessage, BidUpdate } from '../models/auction.model';

@Injectable({ providedIn: 'root' })
export class WebsocketService implements OnDestroy {
  private socket: WebSocket | null = null;
  private messageSubject = new Subject<BidUpdate>();
  private connectionStatusSubject = new BehaviorSubject<'CONNECTED' | 'DISCONNECTED' | 'ERROR'>('DISCONNECTED');
  private intentionalDisconnect = false;
  private reconnectTimeout: ReturnType<typeof setTimeout> | null = null;

  get messages$(): Observable<BidUpdate> {
    return this.messageSubject.asObservable();
  }

  get connectionStatus$(): Observable<'CONNECTED' | 'DISCONNECTED' | 'ERROR'> {
    return this.connectionStatusSubject.asObservable();
  }

  connect(): void {
    if (this.socket && this.socket.readyState === WebSocket.OPEN) {
      // Already connected — re-emit so late subscribers (e.g. navigating to a new page) get the current state
      this.connectionStatusSubject.next('CONNECTED');
      return;
    }
    if (this.socket && this.socket.readyState === WebSocket.CONNECTING) {
      return;
    }

    this.intentionalDisconnect = false;
    this.socket = new WebSocket(environment.wsUrl);

    this.socket.onopen = () => {
      this.connectionStatusSubject.next('CONNECTED');
    };

    this.socket.onmessage = (event: MessageEvent) => {
      try {
        const data: BidUpdate = JSON.parse(event.data as string);
        this.messageSubject.next(data);
      } catch {
        this.messageSubject.next({ type: 'ERROR', message: 'Invalid message format from server.' });
      }
    };

    this.socket.onerror = () => {
      this.connectionStatusSubject.next('ERROR');
      this.messageSubject.next({ type: 'ERROR', message: 'WebSocket connection error.' });
    };

    this.socket.onclose = () => {
      this.connectionStatusSubject.next('DISCONNECTED');
      if (!this.intentionalDisconnect) {
        this.reconnectTimeout = setTimeout(() => this.connect(), 3000);
      }
    };
  }

  sendBid(bid: BidMessage): void {
    if (this.socket && this.socket.readyState === WebSocket.OPEN) {
      this.socket.send(JSON.stringify(bid));
    } else {
      this.messageSubject.next({ type: 'ERROR', message: 'WebSocket is not connected. Please refresh and try again.' });
    }
  }

  disconnect(): void {
    this.intentionalDisconnect = true;
    if (this.reconnectTimeout !== null) {
      clearTimeout(this.reconnectTimeout);
      this.reconnectTimeout = null;
    }
    if (this.socket) {
      this.socket.close();
      this.socket = null;
    }
  }

  isConnected(): boolean {
    return this.socket !== null && this.socket.readyState === WebSocket.OPEN;
  }

  ngOnDestroy(): void {
    this.disconnect();
    this.messageSubject.complete();
    this.connectionStatusSubject.complete();
  }
}
