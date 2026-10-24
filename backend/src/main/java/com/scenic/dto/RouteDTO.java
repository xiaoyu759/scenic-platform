package com.scenic.dto;

import lombok.Data;

import java.util.List;

/**
 * 路线请求DTO
 */
@Data
public class RouteDTO {

    private Long id;

    /**
     * 路线名称
     */
    private String name;

    /**
     * 景点ID列表（按顺序）
     */
    private List<Long> spotIds;

    /**
     * 每个景点的停留时间
     */
    private List<Integer> stayTimes;

    /**
     * 总时长（分钟）
     */
    private Integer totalTime;

    /**
     * 总距离（公里）
     */
    private java.math.BigDecimal totalDistance;
}