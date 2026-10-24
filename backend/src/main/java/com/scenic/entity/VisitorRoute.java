package com.scenic.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 游览路线实体
 * 对应表：visitor_route
 */
@Data
public class VisitorRoute {

    private Long id;

    /**
     * 游客ID
     */
    private Long visitorId;

    /**
     * 路线名称
     */
    private String name;

    /**
     * 总时长（分钟）
     */
    private Integer totalTime;

    /**
     * 总距离（公里）
     */
    private BigDecimal totalDistance;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}