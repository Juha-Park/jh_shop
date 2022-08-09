package com.shop.entyty;


import com.shop.constant.OrderStatus;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
//h2 예약어로 정렬할 때 사용하는 "order" 키워드가 있기 때문에 Order 엔티티에 매핑되는 테이블 이름을 "orders"로 지정.
@Table(name = "orders")
@Getter
@Setter
public class Order extends BaseEntity{

    @Id
    @GeneratedValue
    @Column(name = "order_id")
    private Long id;

    //한 명의 member는 여러번 order 가능하므로 order 엔티티 기준에서 다대일 단방향 매핑.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    private LocalDateTime orderDate; //주문일

    //자바의 enum 타입을 엔티티의 속성으로 지정.
    //enum의 순서 변화로 인하여 문제가 발생할 수 있으므로 "EnumType.STRING" 옵션을 사용하여 String 형식으로 저장.
    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus; //주문상태

    //OrderItem 엔티티와 일대다 매핑.
    //외래키(order_id)가 order_item 테이블에 있으므로 연관 관계의 주인은 OrderItem 엔티티.
    //연관 관계의 주인 필드인 order를 mappedBy의 값으로 세팅.
    //부모 엔티티(Order)의 영속성 상태 변화를 자식 엔티티(OrderItem)에 모두 전이하는 CascadeType.All 옵션 설정.
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    //하나의 order에 여러 개의 orderItem을 담을 수 있으므로 List 자료형으로 매핑.
    private List<OrderItem> orderItems = new ArrayList<>();


    public void addOrderItem(OrderItem orderItem){
        //orderItems에 주문 상품 정보들을 담아줌.
        orderItems.add(orderItem);
        //Order 엔티티와 OrderItem 엔티티가 양방향 참조 관계이므로, orderItem 객체에도 order 객체를 세팅.
        orderItem.setOrder(this);
    }

    public static Order createOrder(Member member, List<OrderItem> orderItemList){

        Order order = new Order();
        order.setMember(member);
        /*상품 페이지에서는 하나의 상품만을 주문하지만, 장바구니 페이지에서는 한 번에 여러 개의 상품을 주문할 수 있으므로,
          여러 개의 주문 상품을 담을 수 있도록 List 형태로 파라미터 값을 받으며 Order 객체에 orderItem 객체를 추가함.*/
        for(OrderItem orderItem : orderItemList){
            order.addOrderItem(orderItem);
        }
        order.setOrderStatus(OrderStatus.ORDER);
        order.setOrderDate(LocalDateTime.now());
        return order;
    }

    //총 주문 금액을 구하는 메소드.
    public int getTotalPrice() {
        int totalPrice = 0;
        for(OrderItem orderItem : orderItems){
            totalPrice += orderItem.getTotalPrice();
        }
        return totalPrice;
    }

    //주문 취소 시 주문 상태를 취소 상태로 바꿔주고, 주문 수량을 상품의 재고에 더해주는 로직을 구현.
    public void cancelOrder(){
        this.orderStatus = OrderStatus.CANCEL;

        for(OrderItem orderItem : orderItems){
            orderItem.cancel();
        }
    }
}
