package com.scenic.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 入园记录实体
 * 对应表：entry_record
 */
@Data
public class EntryRecord {

    private Long id;

    /**
     * 订单ID
     */
    private Long orderId;


    private Long spotId;

    /**
     * 游客姓名
     */
    private String visitorName;

    /**
     * 入园时间
     */
    private LocalDateTime entryTime;

    /**
     * 出园时间
     */
    private LocalDateTime exitTime;
}