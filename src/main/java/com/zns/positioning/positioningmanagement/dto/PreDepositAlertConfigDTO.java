package com.zns.positioning.positioningmanagement.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class PreDepositAlertConfigDTO {
    @NotNull private Long accountId;
    @NotNull private BigDecimal alertThreshold;
    @NotNull private Integer alertEnabled;
    private String alertPhone;
    private String alertEmail;
}
