package com.freelance.platform.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReviewVO {
    private Integer id;
    private Integer projectId;
    private String projectTitle;
    private Integer reviewerId;
    private String reviewerName;
    private String reviewerAvatar;
    private Integer revieweeId;
    private String revieweeName;
    private String revieweeAvatar;
    private Integer rating;
    private String comment;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
