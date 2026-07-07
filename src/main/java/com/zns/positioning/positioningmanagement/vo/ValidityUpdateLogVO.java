package com.zns.positioning.positioningmanagement.vo;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ValidityUpdateLogVO {
    private Long id;
    private Long deviceId;
    private String deviceNo;
    private LocalDate oldValidFrom;
    private LocalDate oldValidTo;
    private LocalDate newValidFrom;
    private LocalDate newValidTo;
    private String reason;
    private String operator;
    private LocalDateTime createTime;
}
