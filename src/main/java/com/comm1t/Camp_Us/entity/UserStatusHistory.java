package com.comm1t.Camp_Us.entity;

import lombok.Getter;
import jakarta.persistence.*;
import java.util.Date;

@Entity
@Getter
public class UserStatusHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long historyId;  // 이력 ID

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;  // 사용자 ID

    @ManyToOne
    @JoinColumn(name = "status_type_id")
    private UserStatusType userStatusType;  // 상태 유형 ID

    private Date modifiedDate;  // 수정 날짜
}
