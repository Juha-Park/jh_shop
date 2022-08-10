package com.shop.entity;

import com.shop.entyty.BaseEntity;
import com.shop.entyty.Member;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import javax.persistence.*;

@Entity
@Table(name = "cart")
@Getter @Setter
@ToString
public class Cart extends BaseEntity {

    @Id
    @Column(name = "cart_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY) // 일대일 매핑.
    @JoinColumn(name="member_id") //매핑할 외래키를 지정.
    private Member member;

    //회원 한 명당 1개의 장바구니를 가지므로, 처음 창바구니에 상품을 담을 때는 해당 회원의 장바구니를 생성해야 함.
    public static Cart createCart(Member member){
        Cart cart = new Cart();
        cart.setMember(member);
        return cart;
    }

}