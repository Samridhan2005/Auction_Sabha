import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  // This is the single source of truth for your user data
  private userData = {
    name: 'Chandrakanth',
    age: 24,
    place: 'Andhra Pradesh, India',
    about: 'I am an active bidder on Auction Sabha, looking for the best electronic deals.',
    reminders: 'Enabled'
  };

  // Method to get the current data
  getProfile() {
    return { ...this.userData }; // Returns a copy
  }

  // Method to update the data
  updateProfile(newData: any) {
    this.userData = { ...newData };
    console.log('Central Service Updated:', this.userData);
  }
}