package com.shop.entyty;

import com.shop.constant.Role;
import com.shop.dto.MemberFormDto;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.persistence.*;

@Entity
@Table(name="member")
@Getter
@Setter
@ToString
public class Member extends BaseEntity{

    @Id
    @Column(name="member_id")
    @GeneratedValue(strategy = GenerationType.AUTO) //기본키를 생성하는 전략
    private Long id;

    private String name;

    @Column(unique = true)
    private String email;

    private String password;

    private String address;

    //자바의 enum 타입을 엔티티의 속성으로 지정.
    //enum의 순서 변화로 인하여 문제가 발생할 수 있으므로 "EnumType.STRING" 옵션을 사용하여 String 형식으로 저장.
    @Enumerated(EnumType.STRING)
    private Role role;

    public static Member createMember(MemberFormDto memberFormDto, PasswordEncoder passwordEncoder){

        Member member = new Member();

        member.setName(memberFormDto.getName());
        member.setEmail(memberFormDto.getEmail());
        member.setAddress(memberFormDto.getAddress());

        //SecurityConfig 클래스에 등록한 BCryptPasswordEncoder Bean을 파라미터로 넘겨서 비밀번호를 암호화.
        String password = passwordEncoder.encode(memberFormDto.getPassword());
        member.setPassword(password);

        member.setRole(Role.ADMIN);

        return member;
    }
}
