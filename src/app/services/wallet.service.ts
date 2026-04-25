import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Wallet, WalletDepositRequest, WalletWithdrawRequest } from '../models/wallet.model';
import { Transaction } from '../models/transaction.model';

@Injectable({ providedIn: 'root' })
export class WalletService {
  private readonly baseUrl = `${environment.apiBaseUrl}/api/wallet`;

  constructor(private http: HttpClient) {}

  getWallet(userId: number): Observable<Wallet> {
    return this.http.get<Wallet>(`${this.baseUrl}/${userId}`);
  }

  deposit(userId: number, request: WalletDepositRequest): Observable<Wallet> {
    return this.http.post<Wallet>(`${this.baseUrl}/${userId}/deposit`, request);
  }

  withdraw(userId: number, request: WalletWithdrawRequest): Observable<Wallet> {
    return this.http.post<Wallet>(`${this.baseUrl}/${userId}/withdraw`, request);
  }

  getTransactions(userId: number): Observable<Transaction[]> {
    return this.http.get<Transaction[]>(`${this.baseUrl}/${userId}/transactions`);
  }
}
