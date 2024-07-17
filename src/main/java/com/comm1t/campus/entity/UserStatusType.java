package com.comm1t.campus.entity;

import lombok.Getter;
import jakarta.persistence.*;

@Entity
@Getter
public class UserStatusType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long statusTypeId;  // 상태 유형 ID

    private String statusType;  // 상태 유형
}
