import { Component, Output, EventEmitter, OnInit } from '@angular/core';
import { UserService } from '../../services/user.service'; // Import service

@Component({
  selector: 'app-profile',
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.css']
})
export class ProfileComponent implements OnInit {
  @Output() closeRequested = new EventEmitter<void>();
  
  user: any; // Data will be loaded here

  constructor(private userService: UserService) {}

  ngOnInit() {
    // When the profile opens, get the latest data from the service
    this.user = this.userService.getProfile();
  }

  close() {
    this.closeRequested.emit();
  }

  save() {
    // 1. Save the local form data into the Central Service
    this.userService.updateProfile(this.user);
    
    // 2. Close the modal
    this.close();
    
    // Optional: Show success message
    alert('Profile updated successfully!');
  }
}