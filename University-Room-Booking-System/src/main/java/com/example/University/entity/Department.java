package com.example.University.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "departments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Department {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String code;

    @Column
    private String description;

    @OneToMany(mappedBy = "department", fetch = FetchType.LAZY)
    private List<User> users;

    @OneToMany(mappedBy = "department", fetch = FetchType.LAZY)
    private List<Building> buildings;
}