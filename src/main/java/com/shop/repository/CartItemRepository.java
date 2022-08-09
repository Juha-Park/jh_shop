package com.shop.repository;

import com.shop.dto.CartDetailDto;
import com.shop.entyty.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    CartItem findByCartIdAndItemId(Long cartId, Long itemId);

    //CartDetailDto 리스트를 쿼리 하나로 조회하는 JPQL문.
    /*연관 관계 매핑을 지연 로딩으로 설정했기 때문에 매핑된 다른 엔티티를 조회할 때 추가적으로 쿼리문이 실행되므로,
      성능 최적화가 필요한 경우 아래 코드와 같이 dto의 생성자를 이용하여 반환값으로 dto 객체를 생성.*/
    //장바구니에 담겨있는 상품의 대표 이미지만 가지고 오도록 조건문을 작성.
    @Query("select new com.shop.dto.CartDetailDto(ci.id, i.itemNm, i.price, ci.count, im.imgUrl) "
            +"from CartItem ci, ItemImg im "
            +"join ci.item i "
            +"where ci.cart.id = :cartId "
            +"and im.item.id = ci.item.id "
            +"and im.repImgYn = 'Y' "
            +"order by ci.regTime desc")
    List<CartDetailDto> findCartDetailDtoList(Long cartId);
}
