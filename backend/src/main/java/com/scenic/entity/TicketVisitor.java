package com.scenic.entity;

import lombok.Data;

/**
 * 购票人信息实体
 * 对应表：ticket_visitor
 */
@Data
public class TicketVisitor {

    private Long id;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 姓名
     */
    private String name;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 身份证号
     */
    private String idCard;
}