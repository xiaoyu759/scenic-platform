package com.scenic.entity;

import lombok.Data;

/**
 * 路线景点关联实体
 * 对应表：route_spot
 */
@Data
public class RouteSpot {

    private Long id;

    /**
     * 路线ID
     */
    private Long routeId;

    /**
     * 景点ID
     */
    private Long spotId;

    /**
     * 游览顺序
     */
    private Integer sortOrder;

    /**
     * 停留时间（分钟）
     */
    private Integer stayTime;
}