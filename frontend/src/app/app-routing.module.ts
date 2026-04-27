import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthGuard } from './guards/auth.guard';
import { RoleGuard } from './guards/role.guard';
import { LoginComponent } from './components/login/login.component';
import { RegisterComponent } from './components/register/register.component';
import { HomeComponent } from './components/home/home.component';
import { AuctionDetailComponent } from './components/auction-detail/auction-detail.component';
import { ProductsComponent } from './components/products/products.component';
import { ProductSubmitComponent } from './components/product-submit/product-submit.component';
import { AdminComponent } from './components/admin/admin.component';
import { WalletComponent } from './components/wallet/wallet.component';
import { VerifierComponent } from './components/verifier/verifier.component';
// Import the new components
import { ForgotPasswordComponent } from './components/forgot-password/forgot-password.component';
import { ChangePasswordComponent } from './components/change-password/change-password.component';

const routes: Routes = [
  { path: '', redirectTo: 'home', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'forgot-password', component: ForgotPasswordComponent }, // Public route
  {
    path: 'home',
    component: HomeComponent,
    canActivate: [AuthGuard]
  },
  {
    path: 'change-password',
    component: ChangePasswordComponent,
    canActivate: [AuthGuard] // Protected route
  },
  {
    path: 'auction/:id',
    component: AuctionDetailComponent,
    canActivate: [RoleGuard],
    data: { roles: ['BUYER'] }
  },
  {
    path: 'products',
    component: ProductsComponent,
    canActivate: [AuthGuard]
  },
  {
    path: 'submit-product',
    component: ProductSubmitComponent,
    canActivate: [RoleGuard],
    data: { roles: ['SELLER'] }
  },
  {
    path: 'admin',
    component: AdminComponent,
    canActivate: [RoleGuard],
    data: { roles: ['ADMIN'] }
  },
  {
    path: 'wallet',
    component: WalletComponent,
    canActivate: [AuthGuard]
  },
  {
    path: 'verifier',
    component: VerifierComponent,
    canActivate: [RoleGuard],
    data: { roles: ['VERIFIER'] }
  },
  { path: '**', redirectTo: 'home' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {}