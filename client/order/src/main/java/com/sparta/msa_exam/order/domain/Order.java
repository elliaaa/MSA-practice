package com.sparta.msa_exam.order.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long order_id;
    private LocalDateTime order_date;
    private BigDecimal total_amount;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderProduct> order_products_id = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (order_date == null) {
            order_date = LocalDateTime.now();
        }
    }

    public static Order createOrder(List<Long> orderProductIds,BigDecimal totalAmount) {
        List<OrderProduct> orderProducts = orderProductIds.stream()
                .map(productId -> new OrderProduct(productId))
                .collect(Collectors.toList());

        Order order = Order.builder()
                .order_products_id(orderProducts)
                .order_date(LocalDateTime.now())
                .total_amount(totalAmount)
                .build();

        return order;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.total_amount = totalAmount;
    }

}
