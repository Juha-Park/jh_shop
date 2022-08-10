package com.shop.entyty;

import com.shop.entity.Cart;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@Table(name = "cart_item")
public class CartItem extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_item_id")
    private Long id;

    //하나의 cart에는 여러 개의 item을 담을 수 있으므로 @ManyToOne 어노테이션을 사용하여 다대일 관계로 매핑.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id")
    private Cart cart;

    //cart에 담을 item의 정보를 알아야 하므로 CartItem 엔티티를 매핑.
    //하나의 item은 서로 다른 cart의 cartitem으로 담길 수 있으므로 @ManyToOne 어노테이션을 이용하여 다대일 관계로 매핑.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    //같은 상품을 장바구니에 몇 개 담을지 저장.
    private int count;

    public static CartItem createCartItem(Cart cart, Item item, int count){
        CartItem cartItem = new CartItem();
        cartItem.setCart(cart);
        cartItem.setItem(item);
        cartItem.setCount(count);
        return cartItem;
    }

    //장바구니에 이미 상품이 담겨 있을 때, 장바구니에 새로 추가할 상품의 수량을 기존 수량에 더함.
    public void addCount(int count){
        this.count += count;
    }

    public void updateCount(int count){ this.count = count; }
}
