package com.zns.positioning.positioningmanagement.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class OrderLogVO {
    private Long id;
    private Long orderId;
    private String orderNo;
    private String logType;
    private String logLevel;
    private String title;
    private String content;
    private String operator;
    private LocalDateTime createTime;
}
