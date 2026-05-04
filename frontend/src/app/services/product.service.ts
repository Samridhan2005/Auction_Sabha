import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Category, Product, ProductSubmitRequest, SellerProductSummary } from '../models/product.model';

@Injectable({ providedIn: 'root' })
export class ProductService {
  private readonly baseUrl = `${environment.apiBaseUrl}/api/products`;

  constructor(private http: HttpClient) {}

  getCategories(): Observable<Category[]> {
    return this.http.get<Category[]>(`${this.baseUrl}/categories`);
  }

  getAllProducts(): Observable<Product[]> {
    return this.http.get<Product[]>(`${this.baseUrl}/all`);
  }

  getProductsByCategory(categoryId: number): Observable<Product[]> {
    return this.http.get<Product[]>(`${this.baseUrl}/category/${categoryId}`);
  }

  searchProducts(name: string): Observable<Product[]> {
    const params = new HttpParams().set('name', name);
    return this.http.get<Product[]>(`${this.baseUrl}/search`, { params });
  }

  getPendingProducts(): Observable<Product[]> {
    return this.http.get<Product[]>(`${this.baseUrl}/pending`);
  }

  reviewProduct(productId: number, status: 'APPROVED' | 'REJECTED', remarks: string): Observable<Product> {
    const params = new HttpParams()
      .set('status', status)
      .set('remarks', remarks);
    return this.http.put<Product>(`${this.baseUrl}/review/${productId}`, null, { params });
  }

  submitProduct(sellerId: number, request: ProductSubmitRequest): Observable<Product> {
    return this.http.post<Product>(`${this.baseUrl}/submit?sellerId=${sellerId}`, request);
  }

  getMyProducts(sellerId: number): Observable<SellerProductSummary[]> {
    return this.http.get<SellerProductSummary[]>(`${this.baseUrl}/my-products?sellerId=${sellerId}`);
  }
}
