package com.shop.dto;

import com.shop.constant.OrderStatus;
import com.shop.dto.OrderItemDto;
import com.shop.entyty.Order;
import lombok.Getter;
import lombok.Setter;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class OrderHistDto {

    private Long orderId; //주문 아이디

    private String orderDate; //주문 날짜

    private OrderStatus orderStatus; //주문 상태

    private List<OrderItemDto> orderItemDtoList = new ArrayList<>(); //주문 상품 리스트

    public OrderHistDto(Order order){
        this.orderId = order.getId();
        //화면에 "yyyy-MM-dd HH:mm:ss" 형태로 전달하기 위해 포맷 수정.
        this.orderDate = order.getOrderDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        this.orderStatus = order.getOrderStatus();
    }

    //orderItemDto 객체를 주문 상품 리스트에 추가.
    public void addOrderItemDto(OrderItemDto orderItemDto){
        orderItemDtoList.add(orderItemDto);
    }
}
