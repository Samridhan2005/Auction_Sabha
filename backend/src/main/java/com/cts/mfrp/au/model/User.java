package com.cts.mfrp.au.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int userId;

    private String name;
    @Column(unique = true)
    private String email;
    private String password;
    private String phone;
    private String role;
}