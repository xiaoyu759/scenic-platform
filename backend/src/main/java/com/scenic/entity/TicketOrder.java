package com.scenic.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 门票订单实体
 * 对应表：ticket_order
 */
@Data
public class TicketOrder {

    private Long id;

    /**
     * 订单号（唯一）
     */
    private String orderNo;

    /**
     * 游客ID
     */
    private Long visitorId;

    /**
     * 票种：single单次票 combo套票
     */
    private String ticketType;

    /**
     * 数量
     */
    private Integer quantity;

    /**
     * 总价（元）
     */
    private BigDecimal totalPrice;

    /**
     * 游玩日期
     */
    private LocalDate visitDate;

    /**
     * 状态：pending待使用 paid已支付 used已使用 cancelled已取消
     */
    private String status;

    /**
     * 下单时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}