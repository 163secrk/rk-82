package com.freelance.platform.dto.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.freelance.platform.common.LocalDateTimeDeserializer;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateMilestoneRequest {
    @NotBlank(message = "里程碑名称不能为空")
    private String name;

    private String description;

    @NotNull(message = "预计完成时间不能为空")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime expectedDate;

    private Integer sortOrder;
}
