import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { VerifierApplication, VerifierApplicationRequest, VerifierApprovalResponse } from '../models/verifier-application.model';

@Injectable({ providedIn: 'root' })
export class VerifierApplicationService {
  private readonly baseUrl = `${environment.apiBaseUrl}/api/verifier-applications`;

  constructor(private http: HttpClient) {}

  submit(request: VerifierApplicationRequest): Observable<string> {
    return this.http.post(this.baseUrl, request, { responseType: 'text' });
  }

  getAll(): Observable<VerifierApplication[]> {
    return this.http.get<VerifierApplication[]>(this.baseUrl);
  }

  getPending(): Observable<VerifierApplication[]> {
    return this.http.get<VerifierApplication[]>(`${this.baseUrl}/pending`);
  }

  approve(id: number, remarks: string): Observable<VerifierApprovalResponse> {
    const params = new HttpParams().set('remarks', remarks);
    return this.http.put<VerifierApprovalResponse>(`${this.baseUrl}/${id}/approve`, null, { params });
  }

  reject(id: number, remarks: string): Observable<VerifierApplication> {
    const params = new HttpParams().set('remarks', remarks);
    return this.http.put<VerifierApplication>(`${this.baseUrl}/${id}/reject`, null, { params });
  }
}
