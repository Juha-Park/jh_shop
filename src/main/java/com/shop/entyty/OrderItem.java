package com.shop.entyty;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@Table(name = "order_item")
public class OrderItem extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_item_id")
    private Long id;

    //하나의 item은 여러 orderitem에 들어갈 수 있으므로 orderitem 기준으로 다대일 단방향 매핑.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    //한 번의 order에 여러 개의 orderitem이 포함될 수 있으므로 orderitem 기준으로 다대일 단방향 매핑.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    private int orderPrice; //주문 가격

    private int count; //수량

    public static OrderItem createOrderItem(Item item, int count){
        //주문할 상품과 주문 수량, 주문 가격을 세팅.
        OrderItem orderItem = new OrderItem();
        orderItem.setItem(item);
        orderItem.setCount(count);
        orderItem.setOrderPrice(item.getPrice());

        //주문 수량만큼 상품의 재고 수량을 감소시킴.
        item.removeStock(count);
        return orderItem;
    }

    //주문 가격과 주문 수량을 곱해서 해당 상품을 주문한 총 가격을 계산.
    public int getTotalPrice(){

        return orderPrice*count;
    }

    //주문 취소 시 주문 수량만큼 상품의 재고를 더해줌.
    public void cancel(){
        this.getItem().addStock(count);
    }
}
