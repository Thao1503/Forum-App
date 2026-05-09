package com.forumapp.entity;

import com.forumapp.common.enums.RankStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;

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
    private String avatar = "https://res.cloudinary.com/dglnkljzx/image/upload/v1778276911/qqi9hsjrhabfl19a2box.jpg";

    private OffsetDateTime dob;
    private String phone;
}