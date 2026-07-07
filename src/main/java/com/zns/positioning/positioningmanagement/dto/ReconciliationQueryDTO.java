package com.zns.positioning.positioningmanagement.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class ReconciliationQueryDTO {
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate reportDateStart;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate reportDateEnd;
    private String status;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}
