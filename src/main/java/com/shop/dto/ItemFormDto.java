package com.shop.dto;

import com.shop.constant.ItemSellStatus;
import com.shop.entyty.Item;
import lombok.Getter;
import lombok.Setter;
import org.modelmapper.ModelMapper;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ItemFormDto {

    private Long id;

    @NotBlank(message = "상품명은 필수 입력값입니다.")
    private String itemNm;

    @NotNull(message = "가격은 필수 입력값입니다.")
    private int price;

    @NotBlank(message = "상품 상세 내용은 필수 입력값입니다.")
    private String itemDetail;

    @NotNull(message = "재고는 필수 입력값입니다.")
    private int stockNumber;

    private ItemSellStatus itemSellStatus;

    //상품 저장 후 수정할 때 상품 이미지 정보를 저장하는 리스트.
    private List<ItemImgDto> itemImgDtoList = new ArrayList<>();

    //상품의 이미지 아이디를 저장하는 리스트.
    //상품 등록 시에는 아직 상품의 이미지를 저장하지 않았기 때문에 아무 값도 들어가 있지 않고, 수정 시에 이미지 아이디를 담아둘 용도로 사용.
    private List<Long> itemImgIds = new ArrayList<>();

    private static ModelMapper modelMapper = new ModelMapper();

    //ItemFormDto 객체의 자료형과 멤버변수의 이름이 Item 객체와 같을 때 Item 으로 값을 복사해서 반환.
    public Item createItem(){
        return modelMapper.map(this, Item.class);
    }

    //Item 엔티티 객체를 파라미터로 받아서 Item 객체의 자료형과 멤버변수의 이름이 ItemFormDto 객체와 같을 때 ItemFormDto로 값을 복사해서 반환.
    //static 메소드로 선언해 ItemFormDto 객체를 생성하지 않아도 호출할 수 있도록 함.
    public static ItemFormDto of(Item item){
        return modelMapper.map(item, ItemFormDto.class);
    }


}
