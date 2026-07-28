package com.hmall.trade.domain.enums;

/**
 * 订单状态枚举
 */
public enum OrderStatus {
    UNPAID(1, "未支付"),
    PAID(2, "已支付"),
    SHIPPED(3, "已发货"),
    COMPLETED(4, "已完成"),
    CANCELLED(5, "已取消");

    private final int value;
    private final String desc;

    OrderStatus(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public int getValue() { return value; }
    public String getDesc() { return desc; }

    public static OrderStatus of(int value) {
        for (OrderStatus s : values()) {
            if (s.value == value) return s;
        }
        throw new IllegalArgumentException("Unknown order status: " + value);
    }

    public boolean canCancel() {
        return this == UNPAID;
    }

    public boolean canPay() {
        return this == UNPAID;
    }
}
