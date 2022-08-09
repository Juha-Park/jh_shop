package com.shop.repository;

import com.shop.entyty.ItemImg;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemImgRepository extends JpaRepository<ItemImg, Long> {

    //매개변수로 상품 아이디를 가지며, 상품 이미지 아이디의 오름차순으로 검색.
    List<ItemImg> findByItemIdOrderByIdAsc(Long itemId);

    ItemImg findByItemIdAndRepImgYn(Long itemId, String repImgYn);
}
