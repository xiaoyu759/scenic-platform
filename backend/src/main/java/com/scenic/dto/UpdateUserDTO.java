package com.scenic.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 更新用户信息请求DTO
 */
@Data
public class UpdateUserDTO {

    private String nickname;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "邮箱格式不正确")
    private String email;

    private Integer gender;

    private Integer age;

    private String avatar;
}