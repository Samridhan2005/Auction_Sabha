import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { AuthInterceptor } from './interceptors/auth.interceptor';

// Core Layout & Shared Components
import { NavbarComponent } from './components/navbar/navbar.component';

// Authentication & Identity Components
import { LoginComponent } from './components/login/login.component';
import { RegisterComponent } from './components/register/register.component';
import { ForgotPasswordComponent } from './components/forgot-password/forgot-password.component';
import { ChangePasswordComponent } from './components/change-password/change-password.component';

// Dashboard & Auction Feature Components
import { HomeComponent } from './components/home/home.component';
import { AuctionDetailComponent } from './components/auction-detail/auction-detail.component';
import { ProductsComponent } from './components/products/products.component';
import { ProductSubmitComponent } from './components/product-submit/product-submit.component';

// User & Admin Management Components
import { AdminComponent } from './components/admin/admin.component';
import { WalletComponent } from './components/wallet/wallet.component';
import { VerifierComponent } from './components/verifier/verifier.component';
import { ProfileComponent } from './components/profile/profile.component';

@NgModule({
  declarations: [
    AppComponent,
    NavbarComponent,
    LoginComponent,
    RegisterComponent,
    HomeComponent,
    AuctionDetailComponent,
    ProductsComponent,
    ProductSubmitComponent,
    AdminComponent,
    WalletComponent,
    VerifierComponent,
    ForgotPasswordComponent,
    ChangePasswordComponent,
    ProfileComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule,
    ReactiveFormsModule, // Required for your custom validation and sign-in logic
    FormsModule           // Required for [(ngModel)] support in forms
  ],
  providers: [
    {
      provide: HTTP_INTERCEPTORS,
      useClass: AuthInterceptor,
      multi: true
    }
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }