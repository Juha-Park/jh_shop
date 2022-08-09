package com.shop.entyty;

import com.shop.constant.ItemSellStatus;
import com.shop.dto.ItemFormDto;
import com.shop.exception.OutOfStockException;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;

@Entity
@Table(name="item")
@Getter
@Setter
@ToString
public class Item extends BaseEntity{

    @Id
    @Column(name="item_id")
    @GeneratedValue(strategy=GenerationType.AUTO) //기본키를 생성하는 전략
    private Long id; //상품 코드

    @Column(nullable=false, length = 50)
    private String itemNm; //상품명

    @Column(name="price", nullable=false)
    private int price; //가격

    @Column(nullable=false)
    private int stockNumber; //재고수량

    @Lob
    @Column(nullable=false)
    private String itemDetail; //상품 상세 설명

    @Enumerated(EnumType.STRING) //enum 타입 매핑
    private ItemSellStatus itemSellStatus; //상품 판매 상태

    public void updateItem(ItemFormDto itemFormDto){
        this.itemNm = itemFormDto.getItemNm();
        this.price = itemFormDto.getPrice();
        this.stockNumber = itemFormDto.getStockNumber();
        this.itemDetail = itemFormDto.getItemDetail();
        this.itemSellStatus = itemFormDto.getItemSellStatus();
    }

    public void removeStock(int stockNumber){
        //상품의 재고 수량에서 주문 후 남은 재고 수량을 구함.
        int restStock = this.stockNumber - stockNumber;
        //상품의 재고가 주문 수량보다 작을 경우 OutOfStockException 예외를 발생시킴.
        if(restStock<0){
            throw new OutOfStockException("상품의 재고가 부족합니다. (현재 재고 수량: " + this.stockNumber + ")");
        }
        //주문 후 남은 재고 수량을 상품의 현재 재고 값으로 할당.
        this.stockNumber = restStock;
    }

    //주문을 취소할 경우 주문 수량만큼 상품의 재고를 증가시키는 메소드.
    public void addStock(int stockNumber){
        this.stockNumber += stockNumber;
    }
}
