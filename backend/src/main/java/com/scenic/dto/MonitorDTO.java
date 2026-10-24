package com.scenic.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 监控数据DTO
 */
@Data
public class MonitorDTO {

    /**
     * 今日入园人数
     */
    private Integer todayVisitors;

    /**
     * 当前在园人数
     */
    private Integer currentVisitors;

    /**
     * 承载率（%）
     */
    private BigDecimal capacityRate;

    /**
     * 各景点实时数据
     */
    private List<SpotMonitor> spotList;

    @Data
    public static class SpotMonitor {
        private Long id;
        private String name;
        private Integer currentCount;
        private Integer maxCapacity;
        private BigDecimal rate;  // 拥挤度百分比
    }
}