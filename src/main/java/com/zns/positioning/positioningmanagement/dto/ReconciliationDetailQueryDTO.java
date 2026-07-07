package com.zns.positioning.positioningmanagement.dto;

import lombok.Data;

@Data
public class ReconciliationDetailQueryDTO {
    private String diffType;
    private String processStatus;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}
