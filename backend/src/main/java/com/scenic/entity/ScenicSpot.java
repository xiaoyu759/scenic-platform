package com.scenic.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 景点实体
 * 对应表：scenic_spot
 */
@Data
public class ScenicSpot {

    private Long id;

    /**
     * 景点名称
     */
    private String name;

    /**
     * 景点介绍
     */
    private String description;

    /**
     * 详细地址（用于地理编码）
     */
    private String address;

    /**
     * 景点图片URL
     */
    private String imageUrl;

    /**
     * 纬度
     */
    private BigDecimal latitude;

    /**
     * 经度
     */
    private BigDecimal longitude;

    /**
     * 建议停留时间（分钟）
     */
    private Integer stayTime;

    /**
     * 最大容量（人）
     */
    private Integer maxCapacity;

    /**
     * 当前人数（实时更新）
     */
    private Integer currentCount;

    /**
     * 排序号（越小越靠前）
     */
    private Integer sortOrder;

    /**
     * 状态：open开放 closed关闭
     */
    private String status;

    /**
     * 分类：transport/culture/scenery/nature/beach/hotel
     */
    private String category;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}