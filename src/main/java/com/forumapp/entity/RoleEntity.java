package com.forumapp.entity;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "roles")
public class RoleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @OneToMany(mappedBy = "roleEntity")
    private List<UserEntity> userEntities;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<UserEntity> getUsers() {
        return userEntities;
    }

    public void setUsers(List<UserEntity> userEntities) {
        this.userEntities = userEntities;
    }
}
