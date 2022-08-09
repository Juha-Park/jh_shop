package com.shop.dto;


import com.shop.constant.ItemSellStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ItemSearchDto {

    //현재 시간과 상품 등록일을 비교해서 상품 데이터를 조회.(all, 1d, 1w, 1m, 6m)
    private String searchDateType;

    //상품의 판매상태를 기준으로 상품 데이터를 조회.
    private ItemSellStatus searchSellStatus;

    //상품을 조회할 때 어떤 유형으로 조회할지 선택.(itemNm(상품명), createdBy(상품 등록자 아이디))
    private String searchBy;

    //조회할 검색어를 저장할 변수 searchQuery를 생성.
    private String searchQuery = "";

}
