package com.commit.camp_reserve.dto;

import lombok.Data;

@Data
public class AvailableReservationStatusDto {
    private String campFacsId;
    private String date;
    private String currentAvailableCount;
    private String totalCount;
}
