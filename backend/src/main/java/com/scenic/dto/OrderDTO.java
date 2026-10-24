package com.scenic.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

/**
 * 订单请求DTO
 */
@Data
public class OrderDTO {

    @NotBlank(message = "票种不能为空")
    private String ticketType;

    @NotNull(message = "数量不能为空")
    @Min(value = 1, message = "数量至少为1")
    @Max(value = 10, message = "单笔订单最多10张")
    private Integer quantity;

    @NotNull(message = "游玩日期不能为空")
    private LocalDate visitDate;

    @NotBlank(message = "购票人姓名不能为空")
    private String name;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @NotBlank(message = "身份证号不能为空")
    @Pattern(regexp = "^[1-9]\\d{5}(18|19|20)\\d{2}((0[1-9])|(1[0-2]))(([0-2][1-9])|10|20|30|31)\\d{3}[0-9Xx]$",
            message = "身份证号格式不正确")
    private String idCard;
}