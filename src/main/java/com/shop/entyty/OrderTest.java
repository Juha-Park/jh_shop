package com.shop.entyty;

import com.shop.constant.ItemSellStatus;
import com.shop.repository.ItemRepository;
import com.shop.repository.MemberRepository;
import com.shop.repository.OrderItemRepository;
import com.shop.repository.OrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceContext;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
public class OrderTest {

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    ItemRepository itemRepository;

    //영속성 컨텍스트 사용을 위해 EntityManager Bean을 주입.
    @PersistenceContext
    EntityManager em;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    OrderItemRepository orderItemRepository;

    public Item createItem(){
        Item item = new Item();
        item.setPrice(10000);
        item.setItemNm("테스트 상품");
        item.setItemDetail("상세설명");
        item.setItemSellStatus(ItemSellStatus.SELL);
        item.setStockNumber(100);
        item.setRegTime(LocalDateTime.now());
        item.setUpdateTime(LocalDateTime.now());
        return item;
    }

    //Order 엔티티를 저장하면 OrderItem 엔티티도 함께 저장되는지를 테스트.
    @Test
    @DisplayName("영속성 전이 테스트")
    public void cascadeTest(){

        Order order = new Order();

        for(int i=0;i<3;i++){
            Item item = this.createItem();
            itemRepository.save(item);
            OrderItem orderItem = new OrderItem();
            orderItem.setItem(item);
            orderItem.setCount(10);
            orderItem.setOrderPrice(1000);
            orderItem.setOrder(order);
            //아직 영속성 컨텍스트에 저장되지 않은 orderItem 엔티티를 order 엔티티에 담아줌.
            order.getOrderItems().add(orderItem);
        }

        //order 엔티티를 저장하면서 강제로 flush를 호출하여 영속성 컨텍스트에 있는 객체들을 데이터베이스에 반영.
        orderRepository.saveAndFlush(order);
        //영속성 컨텍스트의 상태를 초기화.
        em.clear();

        //영속성 컨텍스트를 초기화했으므로 데이터베이스에서 Order 엔티티를 조회.
        //OrderItem 엔티티 3개가 실제로 데이터베이스에 저장되었는지 검사.
        Order savedOrder = orderRepository.findById(order.getId())
                .orElseThrow(EntityNotFoundException::new);
        assertEquals(3, savedOrder.getOrderItems().size());
    }

    public Order createOrder(){
        Order order = new Order();

        for(int i=0;i<3;i++){
            Item item = this.createItem();
            itemRepository.save(item);
            OrderItem orderItem = new OrderItem();
            orderItem.setItem(item);
            orderItem.setCount(10);
            orderItem.setOrderPrice(1000);
            orderItem.setOrder(order);
            order.getOrderItems().add(orderItem);
        }

        Member member = new Member();
        memberRepository.save(member);

        order.setMember(member);
        orderRepository.save(order);
        return order;
    }

    //Order 엔티티(부모 엔티티)에서 OrderItem(자식 엔티티)를 삭제했을 때 orderItem 엔티티도 함께 삭제되는지 테스트.
    @Test
    @DisplayName("고아객체 제거 테스트")
    public void orphanRemovalTest(){
        Order order = this.createOrder();
        //order 엔티티에서 관리하고 있는 orderItem 리스트의 0번째 요소를 제거.
        order.getOrderItems().remove(0);
        em.flush();
    }

    @Test
    @DisplayName("지연 로딩 테스트")
    public void lazyLoadingTest(){
        Order order = this.createOrder();
        Long orderItemId = order.getOrderItems().get(0).getId();
        em.flush();
        em.clear();

        //영속성 컨텍스트의 상태 초기화 후 order 엔티티에 저장했던 orderItemId를 이용하여 orderItem을 데이터베이스에서 다시 조회.
        OrderItem orderItem = orderItemRepository.findById(orderItemId)
                .orElseThrow(EntityNotFoundException::new);
        //orderItem 엔티티에 있는 order 객체의 클래스를 출력.
        System.out.println("Order class : " + orderItem.getOrder().getClass());
        System.out.println("===============================");
        orderItem.getOrder().getOrderDate();
        System.out.println("===============================");
    }
}
