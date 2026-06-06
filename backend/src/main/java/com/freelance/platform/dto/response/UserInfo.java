package com.freelance.platform.dto.response;

import com.freelance.platform.common.enums.UserRole;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UserInfo {
    private Integer id;
    private String email;
    private String nickname;
    private UserRole role;
    private String avatar;
    private BigDecimal balance;
    private BigDecimal rating;
    private String description;
    private String skills;
}
