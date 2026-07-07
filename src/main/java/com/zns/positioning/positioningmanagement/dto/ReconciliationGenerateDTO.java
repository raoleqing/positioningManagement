package com.zns.positioning.positioningmanagement.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

@Data
public class ReconciliationGenerateDTO {
    @NotNull @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate reportDate;
}
