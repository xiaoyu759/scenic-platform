package com.scenic.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理员实体
 * 对应表：admin_user
 */
@Data
public class AdminUser {

    private Long id;

    /**
     * 用户名（唯一）
     */
    private String username;

    /**
     * 密码（BCrypt加密）
     */
    private String password;

    /**
     * 角色：admin/operator
     */
    private String role;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}