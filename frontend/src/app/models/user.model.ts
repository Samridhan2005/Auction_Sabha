export interface User {
  userId: number;
  name: string;
  email: string;
  password?: string;
  phone: string;
  role: 'BUYER' | 'SELLER' | 'ADMIN';
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  message: string;
  role: string;
  token: string;
  userId: number;
}

export interface RegisterRequest {
  name: string;
  email: string;
  password: string;
  role: 'BUYER' | 'SELLER' | 'ADMIN';
  phone: string;
}
