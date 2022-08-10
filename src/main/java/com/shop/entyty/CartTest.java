package com.shop.entyty;


import com.shop.dto.MemberFormDto;
import com.shop.repository.CartRepository;
import com.shop.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceContext;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
@TestPropertySource(locations = "classpath:application-test.properties")
public class CartTest {

    @Autowired
    CartRepository cartRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    //영속성 컨텍스트 사용을 위해 EntityManager Bean을 주입.
    @PersistenceContext
    EntityManager em;

    // 테스트에 사용될 회원 엔티티를 생성.
    public Member createMember(){
        MemberFormDto memberFormDto = new MemberFormDto();
        memberFormDto.setEmail("tiger@email.com");
        memberFormDto.setName("호랑이");
        memberFormDto.setAddress("백두산");
        memberFormDto.setPassword("12345678");
        return Member.createMember(memberFormDto, passwordEncoder);
    }

    @Test
    @DisplayName("장바구니 회원 엔티티 매핑 조회 테스트")
    public void findCartAndMemberTest(){
        Member member = this.createMember(); //this 생략 가능.
        memberRepository.save(member);

        Cart cart = new Cart();
        cart.setMember(member);
        cartRepository.save(cart);

        //JPA는 영속성 컨텍스트에 데이터를 저장 후 트랜잭션이 끝날 때 flush()를 호출하여 데이터베이스에 반영.
        //Member 엔티티와 Cart 엔티티를 영속성 컨텍스트에 저장 후 Entity Manager로부터 강제로 flush()를 호출하여 데이터베이스에 반영.
        em.flush();

        //JPA는 영속성 컨텍스트로부터 엔티티를 조회한 후 영속성 컨텍스트에 엔티티가 없을 경우 데이터베이스를 조회함.
        //실제 데이터베이스에서 Cart 엔티티를 가지고 올 때 Member 엔티티도 같이 가지고 오는지 보기 위해서 영속성 컨텍스트를 비워줌.
        em.clear();

        //저장된 Cart 엔티티를 조회.
        Cart savedCart = cartRepository.findById(cart.getId())
                .orElseThrow(EntityNotFoundException::new);
        //처음에 저장한 member 엔티티의 id와 savedCart에 매핑된 member 엔티티의 id를 비교.
        assertEquals(savedCart.getMember().getId(), member.getId());
    }

}
