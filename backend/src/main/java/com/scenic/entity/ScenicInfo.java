package com.scenic.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 景区信息实体
 * 对应表：scenic_info
 */
@Data
public class ScenicInfo {

    private Long id;

    /**
     * 景区名称
     */
    private String name;

    /**
     * 景区简介
     */
    private String description;

    /**
     * 景区地址
     */
    private String address;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 营业时间
     */
    private String businessHours;

    /**
     * 最大承载量（人）
     */
    private Integer maxCapacity;

    /**
     * Logo图片URL
     */
    private String logo;

    /**
     * 轮播图URL，多个用逗号分隔
     */
    private String bannerImages;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}