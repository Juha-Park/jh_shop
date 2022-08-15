## ⭐ 프로젝트명
온라인 쇼핑몰 웹사이트

---
## ⭐ 개발 기간
2022.07.01 ~ 08.11 (42일)

---
## ⭐ 개발 환경
- 운영체제: Window 10
- 통합개발환경(IDE): IntelliJ
-	JDK 1.8
-	Spring Boot 2.7.1
-	데이터베이스: MySQL
-	빌드: Maven
-	배포: AWS EC2, RDS, S3

---
## ⭐ 구현 기능
- Spring Security를 이용한 회원 가입 및 로그인/로그아웃
- 상품 등록/수정 및 관리
- 상품 주문/주문 취소 및 주문 목록 조회
- 장바구니 관리(상품 담기, 수량 변경, 상품 삭제), 장바구니 상품 주문


---
## 📄 프로젝트 내용

### 1. 프로젝트 전체 구조
![패키지 구조11](https://user-images.githubusercontent.com/95207932/184290004-3b17250b-c0af-41ab-aecf-cf1599266ecf.png)
![패키지 구조22](https://user-images.githubusercontent.com/95207932/184289997-c2eb33c3-62eb-458f-bcdc-9b5c1cf8ddf6.png)


### 2. ERD Diagram

![ERD Diagram](https://user-images.githubusercontent.com/95207932/184289152-39827d33-08b0-4dc5-9543-7aa6dad39311.png)

### 3. Use Case Diagram

![Use Case Diagram](https://user-images.githubusercontent.com/95207932/184289194-04ae75a4-a21f-4a4d-ad88-c73e23b98247.png)

### 4. 핵심 비즈니스 로직
### &nbsp;&nbsp;&nbsp;4.1.MemberService & SecurityConfig – 회원 가입 및 로그인/로그아웃, 암호화 클래스

1. 회원 가입 시 입력한 정보를 저장하는 메소드.  
validateDuplicateMember 메소드를 통해 중복된 회원인지를 먼저 검사한 후 이상이 없으면 입력한 정보를 저장.  
현재 Member 클래스의 createMember 메소드에 member.setRole(Role.ADMIN);으로 입력해 놓았기 때문에 회원 가입 시 ADMIN ROLE로 설정됨.
```java
public Member saveMember(Member member){
        validateDuplicateMember(member);
        return memberRepository.save(member);
}
```

2. 회원 가입 시 입력한 정보가 중복될 경우, IllegalStateException 예외를 발생시키고 에러 메시지를 보여줌.
```java
public void validateDuplicateMember(Member member){
        Member findMember = memberRepository.findByEmail(member.getEmail());
        if(findMember != null){
            throw new IllegalStateException("이미 가입된 회원입니다.");
        }
}
```

3. UserDetailsService 인터페이스의 loadUserByUsername() 메소드를 오버라이딩하여 UserDetails를 구현하고 있는 User 객체를 반환하는 메소드.
로그인 창에 입력한 email 정보가 없을 경우 UsernameNotFoundException 예외를 발생시킴. 

```java
@Override
public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Member member = memberRepository.findByEmail(email);
        if(member == null){
            throw new UsernameNotFoundException(email);
        }
        return User.builder()
                .username(member.getEmail())
                .password(member.getPassword())
                .roles(member.getRole().toString())
                .build();
}
```

4. 3에서 반환된 User 객체 정보를 받아 이를 암호화하여 http에 요청하는 메소드.  
logoutRequestMatcher()로 로그아웃 url을 설정해 준 후, 로그아웃 성공 시 main 페이지로 이동.

```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.formLogin()
                .loginPage("/members/login")
                .usernameParameter("email")
                .defaultSuccessUrl("/")
                .failureUrl("/members/login/error")
                .and()
                .logout()
                .logoutRequestMatcher(new AntPathRequestMatcher("/members/logout"))
                .logoutSuccessUrl("/");

        //Security 처리에 HttpServletRequest를 이용. 
        //permitAll()를 통해 모든 사용자가 로그인(인증) 없이 해당 경로에 접근할 수 있도록 설정.
        //해당 계정이 ADMIN ROLE일 경우에만 접근 가능하도록 설정.
        //위에서 설정한 경로를 제외한 나머지 경로들은 모두 로그인(인증)을 요구하도록 설정.
        http.authorizeRequests()
                .mvcMatchers("/", "/members/**", "/item/**", "/images/**").permitAll()
                .mvcMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated();

        //인증되지 않은 사용자가 리소스에 접근하였을 때 수행되는 핸들러를 등록.
        http.exceptionHandling()
                .authenticationEntryPoint(new CustomAuthenticationEntryPoint());
        return http.build();
}

//BCryptPasswordEncoder의 해시 함수를 이용하여 비밀번호를 암호화하여 저장한 뒤 이를 Bean에 저장.
@Bean
public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
}

@Bean
public WebSecurityCustomizer configure() {
        return (web) -> web.ignoring().mvcMatchers("/css/**", "/js/**", "/img/**");
}
```

### &nbsp;&nbsp;&nbsp;4.2. ItemService – 상품 등록/수정 클래스

1. 상품 정보와 이미지를 등록하는 메소드.  
   상품 이미지 등록 시, 첫 번째 이미지만 대표 상품 이미지 값인 ‘Y’로, 나머지는 ‘N’로 설정해서 메인 화면에 대표 상품 이미지만 올라가게 함. 
```java
public Long saveItem(ItemFormDto itemFormDto, List<MultipartFile> itemImgFileList) throws Exception{

        Item item = itemFormDto.createItem();
        itemRepository.save(item);

        for(int i=0;i<itemImgFileList.size();i++){
            ItemImg itemImg = new ItemImg();
            itemImg.setItem(item);

            if(i == 0)
                itemImg.setRepImgYn("Y");
            else
                itemImg.setRepImgYn("N");

            itemImgService.saveItemImg(itemImg, itemImgFileList.get(i));
        }
        return item.getId();
```

2. 상품 수정 메소드.  
수정한 상품 정보를 파라미터로 받아 Item 클래스의 updateItem 메소드와 ItemImgService 클래스의 updateItemImg 메소드를 사용하여 변경 내용을 저장.
```java
public Long updateItem(ItemFormDto itemFormDto, List<MultipartFile> itemImgFileList) throws Exception{

        //상품 수정
        Item item = itemRepository.findById(itemFormDto.getId())
                .orElseThrow(EntityNotFoundException::new);
        item.updateItem(itemFormDto);
        //상품 이미지 아이디 리스트를 조회.
        List<Long> itemImgIds = itemFormDto.getItemImgIds();

        //상품 이미지 등록
        for(int i=0;i<itemImgFileList.size();i++){
            itemImgService.updateItemImg(itemImgIds.get(i), itemImgFileList.get(i));
        }

        return item.getId();
}
```

### &nbsp;&nbsp;&nbsp;4.3. OrderService – 상품 주문/취소 및 주문 목록 조회 클래스

1. 상품 주문 메소드. orderDto의 주문 정보와 user의 email을 파라미터로 받아 주문할 상품과 회원 정보를 조회.  
회원 정보와 주문할 상품 리스트 정보를 이용해서 order 엔티티를 생성한 후, 이를 DB에 저장.
```java
public Long order(OrderDto orderDto, String email){

        Item item = itemRepository.findById(orderDto.getItemId())
                .orElseThrow(EntityNotFoundException::new);
        Member member = memberRepository.findByEmail(email);
        List<OrderItem> orderItemList = new ArrayList<>(); 
        OrderItem orderItem = OrderItem.createOrderItem(item, orderDto.getCount());
        orderItemList.add(orderItem);

        Order order = Order.createOrder(member, orderItemList);
        orderRepository.save(order);

        return order.getId();
}
```

2. 상품 취소 메소드. 주문 취소 상태로 변경할 경우, 변경 감지 기능에 의해서 트랜잭션이 끝날 때 update 쿼리가 실행됨.
```java
public void cancelOrder(Long orderId){
        Order order = orderRepository.findById(orderId)
                .orElseThrow(EntityNotFoundException::new); 
        order.cancelOrder();
}
```

3. 상품 주문 목록을 조회하는 메소드.
```java
@Transactional(readOnly = true)
public Page<OrderHistDto> getOrderList(String email, Pageable pageable){

        //User id와 페이징 조건을 이용하여 주문 목록을 조회.
        List<Order> orders = orderRepository.findOrders(email, pageable);
        
        //User의 주문 총 갯수 조회.
        Long totalCount = orderRepository.countOrder(email);

        List<OrderHistDto> orderHistDtos = new ArrayList<>();

        //주문 리스트를 순회하면서 구매 이력 페이지에 전달할 dto를 생성.
        for(Order order : orders){
            OrderHistDto orderHistDto = new OrderHistDto(order);
            List<OrderItem> orderItems = order.getOrderItems();
            for(OrderItem orderItem : orderItems){
                //주문한 상품의 대표 이미지를 조회.
                ItemImg itemImg = itemImgRepository.findByItemIdAndRepImgYn(orderItem.getItem().getId(), "Y");
                OrderItemDto orderItemDto = new OrderItemDto(orderItem, itemImg.getImgUrl());
                orderHistDto.addOrderItemDto(orderItemDto);
            }

            orderHistDtos.add(orderHistDto);
}

        //페이지 구현 객체를 생성하여 반환.
        return new PageImpl<OrderHistDto>(orderHistDtos, pageable, totalCount);
}
```

### &nbsp;&nbsp;&nbsp;4.4. CartService – 장바구니 관리(상품 담기&수량 변경&상품 삭제), 장바구니 상품 주문 클래스

1. 장바구니에 상품을 담는 메소드.
```java
public Long addCart(CartItemDto cartItemDto, String email){
        Item item = itemRepository.findById(cartItemDto.getItemId())
                .orElseThrow(EntityNotFoundException::new);
        Member member = memberRepository.findByEmail(email);

        Cart cart = cartRepository.findByMemberId(member.getId());
        //장바구니에 상품을 처음으로 담을 경우, 해당 회원의 장바구니 엔티티를 생성.
        if(cart == null){
            cart = Cart.createCart(member);
            cartRepository.save(cart);
        }

        //선택한 상품이 장바구니에 이미 들어가 있는지를 조회.
        CartItem savedCartItem = cartItemRepository.findByCartIdAndItemId(cart.getId(), item.getId());

        //장바구니에 이미 있던 상품일 경우, 기존 수량에 현재 장바구니에 담을 수량 만큼을 더해줌.
        //장바구니에 없는 상품일 경우, CartItem 엔티티를 생성.
        if(savedCartItem != null){
            savedCartItem.addCount(cartItemDto.getCount());
            return savedCartItem.getId();
        } else{ 
            CartItem cartItem = CartItem.createCartItem(cart, item, cartItemDto.getCount());
            cartItemRepository.save(cartItem);
            return cartItem.getId();
        }
}
```

2. 장바구니 상품의 수량을 업데이트하는 메소드.
```java
public void updateCartItemCount(Long cartItemId, int count){
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(EntityNotFoundException::new);
        cartItem.updateCount(count);
}
```

3. 장바구니 상품을 제거하는 메소드. JpaRepository를 상속받은 CartItemRepository 클래스의 delete 메소드를 이용.
```java
public void deleteCartItem(Long cartItemId){
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(EntityNotFoundException::new);
        cartItemRepository.delete(cartItem);
}
```

4. 장바구니 상품을 주문하는 메소드.  
장바구니 페이지에서 전달받은 주문 상품 번호를 이용하여 주문 로직으로 전달할 orderDto 객체를 만든 후, 장바구니에 담은 상품을 주문하는 OrderService클래스의 orders 메소드를 호출.
주문한 상품은 장바구니에서 제거함.

```java
public Long orderCartItem(List<CartOrderDto> cartOrderDtoList, String email){

        List<OrderDto> orderDtoList = new ArrayList<>();
          for (CartOrderDto cartOrderDto : cartOrderDtoList) {
            CartItem cartItem = cartItemRepository
                    .findById(cartOrderDto.getCartItemId())
                    .orElseThrow(EntityNotFoundException::new);

            OrderDto orderDto = new OrderDto();
            orderDto.setItemId(cartItem.getItem().getId());
            orderDto.setCount(cartItem.getCount());
            orderDtoList.add(orderDto);
        }
        
        Long orderId = orderService.orders(orderDtoList, email);
          for (CartOrderDto cartOrderDto : cartOrderDtoList) {
            CartItem cartItem = cartItemRepository
                    .findById(cartOrderDto.getCartItemId())
                    .orElseThrow(EntityNotFoundException::new);
            cartItemRepository.delete(cartItem);
        }

        return orderId;
}
```

---
## ⭐ 프로젝트 후기
### - 힘들었던 점
1. 자잘한 오류들이 너무 많이 발생했다. 몇 시간 동안 한 오류만 붙잡고 있었는데 알고 보니 기호 하나, 글자 하나 오타 등과 같은 어이없는 실수가 원인이었던 적이 한 두번이 아니었다.  
이러한 자잘한 오류들 외에 기억나는 것은 SecurityConfig 클래스 코드를 수정한 후 메인, 회원가입, 로그인 페이지의 css가 모두 적용되지 않았던 문제였다.  
스프링 부트 2.7대에서 WebSecurity ConfigurerAdapter가 deprecated 되어서 Security Config 클래스의 코드를 수정해 주어야 했는데, 이 과정에서 빠진 코드가 있었다.  
알아보니 아래 코드가 오류 원인이었다.  
```java
http.authorizeRequests()
                .mvcMatchers("/", "/members/**", "/item/**", "/images/**").permitAll()
                .mvcMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated();
```
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;나는 부트 스트랩 파일들을 /resources/static/에 위치시켰기 때문에 비로그인 접근 시 Authorization 에러가 났던  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;것이었고, 이를 해결하기 위해서 /css, /js, /img의 하위 폴더에 위치한 정적 자원으로의 접근에는 보안상의 제한을  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;무시한다는 설정을 해 주어야 했다.  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;따라서 SecurityConfig 클래스에 아래의 코드를 추가해 줌으로써 정적 자원에 대한 보안 설정이 무시되고 템플릿이  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;잘 적용되었다.
```java
@Bean
public WebSecurityCustomizer configure() {
        return (web) -> web.ignoring().mvcMatchers("/css/**", "/js/**", "/img/**");
}
```
2. 나는 빌드 툴로 Maven을 사용했는데, 이 때문에 프로젝트를 배포하는 과정에서 애를 많이 먹었다.  
프로젝트를 혼자 진행하다 보니 궁금하거나 문제가 생기면 주로 구글링으로 해결했는데, 스프링 부트 프로젝트는 빌드 툴을 Gradle로 설정한 경우가 대부분이라서 Maven으로 AWS EC2 인스턴스에 배포하는 방법을 알아내는 과정이 꽤나 힘들었다.  
특히 오류가 뜰 때마다 오류 메시지를 하나하나 구글링해야 했던 부분이 시간을 많이 잡아먹어 번거로웠다.

### - 개선할 점
1. id/password 로그인 방식 외에 추가로 구글, 네이버, 페이스북 로그인과 같은 간편 소셜 로그인 방식을 구현하면 좋을 것 같다.  
이러한 OAuth 로그인 구현 시 보안, 인증, 아이디 및 비밀번호 찾기, 회원정보 변경 등의 기능을 따로 구현할 필요가 없어 서비스 개발에 집중할 수 있다고 알고 있다.
2. Vue.js 프레임워크의 경우 데이터가 변하면 해당 데이터를 보여주는 영역의 뷰도 자동으로 바뀌고, 데이터가 변하는 걸 감시하고 있다가 이벤트를 발생하기 쉽다고 들었다.  
내 프로젝트가 이를 걱정할 만큼 그리 복잡한 프로젝트는 아니지만, 상품 등록 페이지처럼 화면 구성이 많고 서로 얽혀있는 데이터가 많은 경우 Vue.js 프레임워크를 도입하는 것도 좋은 방법이 될 것 같다.
3. 구매 이력 페이지에서 배송 조회(택배 운송장 조회 사이트와 연결), 반품 및 교환 신청 기능을 구현해 보고 싶다. 
4. 카카오, 구글, 네이버 등 오픈 api를 활용하여 등록된 상품을 공유하는 기능을 추가하면 좋을 것 같다.

### - 느낀 점
1. 프로젝트를 진행하면서 되도록 주석을 꼼꼼히 달아 놓으려고 노력했었는데, 포트폴리오를 만들면서 당시 어떤 의도로 코드를 작성했는지 떠올리고 빠르게 복습할 수 있어서 주석의 중요성을 다시 한번 깨달았다.
2. 예상치 못한 자잘한 오류들이 너무 많이 발생하는 바람에 이를 해결하느라 계획한 일정보다 개발 시간이 더 오래 걸렸다.  
혼자서 프로젝트를 진행하는 것은 이번이 처음이라 개발 일정을 제대로 계획하지 못했는데, 다음 프로젝트 진행 시에는 오류 발생 상황을 미리 감안하고 개발 계획을 세워야겠다.
3. 아직 부족한 부분도 너무 많고, 한창 배워나가는 과정이기 때문에 다른 사람들과 함께 협업을통해 프로젝트를 진행했더라면 지금보다 더 나은 방식으로 마무리할 수 있었을 거라는 아쉬움이 남는다.  
다음 프로젝트 진행 시에는 협업을 통해 소통하면서 함께 배워가고 싶다.

