import { Component, OnInit } from '@angular/core';
import { ProductService } from '../../services/product.service';
import { Product, Category } from '../../models/product.model';

@Component({
  selector: 'app-products',
  templateUrl: './products.component.html',
  styleUrls: ['./products.component.css']
})
export class ProductsComponent implements OnInit {
  products: Product[] = [];
  categories: Category[] = [];
  isLoading = false;
  errorMessage = '';
  searchQuery = '';
  selectedCategoryId: number | null = null;
  searchMode: 'all' | 'category' | 'search' = 'all';

  constructor(private productService: ProductService) {}

  ngOnInit(): void {
    this.productService.getCategories().subscribe({
      next: (cats) => { this.categories = cats; },
      error: () => {}
    });
    this.loadAll();
  }

  loadAll(): void {
    this.selectedCategoryId = null;
    this.searchMode = 'all';
    this.searchQuery = '';
    this.isLoading = true;
    this.errorMessage = '';
    this.products = [];

    this.productService.getAllProducts().subscribe({
      next: (data: Product[]) => { this.products = data; this.isLoading = false; },
      error: (err: { status: number }) => {
        this.isLoading = false;
        this.errorMessage = err.status === 0
          ? 'Cannot connect to server. Please try again later.'
          : 'Failed to load products. Please try again.';
      }
    });
  }

  loadByCategory(categoryId: number): void {
    this.selectedCategoryId = categoryId;
    this.searchMode = 'category';
    this.searchQuery = '';
    this.isLoading = true;
    this.errorMessage = '';
    this.products = [];

    this.productService.getProductsByCategory(categoryId).subscribe({
      next: (data: Product[]) => { this.products = data; this.isLoading = false; },
      error: (err: { status: number }) => {
        this.isLoading = false;
        this.errorMessage = err.status === 0
          ? 'Cannot connect to server. Please try again later.'
          : 'Failed to load products. Please try again.';
      }
    });
  }

  onSearch(): void {
    const query = this.searchQuery.trim();
    if (!query) return;

    this.searchMode = 'search';
    this.selectedCategoryId = null;
    this.isLoading = true;
    this.errorMessage = '';
    this.products = [];

    this.productService.searchProducts(query).subscribe({
      next: (data: Product[]) => { this.products = data; this.isLoading = false; },
      error: (err: { status: number }) => {
        this.isLoading = false;
        this.errorMessage = err.status === 0
          ? 'Cannot connect to server. Please try again later.'
          : 'Search failed. Please try again.';
      }
    });
  }

  clearSearch(): void {
    this.searchQuery = '';
    this.loadAll();
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
      maximumFractionDigits: 0
    }).format(amount);
  }

  getCategoryName(categoryId: number | null): string {
    if (categoryId === null) return 'All Products';
    const cat = this.categories.find(c => c.categoryId === categoryId);
    return cat ? cat.categoryName : 'Products';
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'APPROVED': return 'bg-success';
      case 'PENDING': return 'bg-warning text-dark';
      case 'REJECTED': return 'bg-danger';
      default: return 'bg-secondary';
    }
  }
}
