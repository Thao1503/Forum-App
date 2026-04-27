package com.forumapp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "user_profiles")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileEntity {
    @Id
    private Long userId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Builder.Default
    private Long points = 0L;

    @Column(name = "user_rank")
    private String rank;
    private String avatar;
    private String bio;

    private LocalDate dob;
    private String phone;
}