package com.forumapp.entity;

import com.forumapp.common.enums.RankStatus;
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
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private RankStatus rank = RankStatus.Newbie;

    @Builder.Default
    private String avatar = "res.cloudinary.com/dglnkljzx/image/upload/v1777365438/su0lhzdt4uxwpn28iitf.jpg";

    private LocalDate dob;
    private String phone;
}