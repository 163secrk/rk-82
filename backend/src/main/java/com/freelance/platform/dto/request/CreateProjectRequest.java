package com.freelance.platform.dto.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.freelance.platform.common.LocalDateTimeDeserializer;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CreateProjectRequest {
    @NotBlank(message = "项目标题不能为空")
    private String title;

    @NotBlank(message = "项目描述不能为空")
    private String description;

    private String category;

    @NotNull(message = "最低预算不能为空")
    private BigDecimal budgetMin;

    @NotNull(message = "最高预算不能为空")
    private BigDecimal budgetMax;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime deadline;

    private String requirements;
}
