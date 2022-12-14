package com.project.product.service.order;

import com.project.product.domain.event.Coupon;
import com.project.product.domain.member.Member;
import com.project.product.domain.order.Order;
import com.project.product.domain.order.OrderStatus;
import com.project.product.domain.payment.Card;
import com.project.product.domain.payment.CardStatus;
import com.project.product.domain.product.Product;
import com.project.product.domain.store.Store;
import com.project.product.dto.delivery.DeliveryPossibilityStoreOrderListDto;
import com.project.product.dto.order.OrderCreateRequest;
import com.project.product.dto.product.OrderProductListResponse;
import com.project.product.exception.NotPaymentCardException;
import com.project.product.exception.NotPaymentPointException;
import com.project.product.repository.event.CouponRepository;
import com.project.product.repository.member.MemberRepository;
import com.project.product.repository.order.OrderRepository;
import com.project.product.repository.payment.CardRepository;
import com.project.product.repository.product.ProductRepository;
import org.aspectj.weaver.ast.Or;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
class OrderServiceTest {

    @Mock
    private Order order;

    @Mock
    private Member member;

    @Mock
    private Product product;

    @Mock
    private Card card;

    @Mock
    private Coupon coupon;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private CardRepository cardRepository;

    @InjectMocks
    private OrderService orderService;


    public Order commonCardOrderProduct(OrderCreateRequest orderCreateRequest, Card card) throws Exception {

        when(productRepository.findAllById(orderCreateRequest.getProductId()))
                .thenReturn(List.of(product));

        when(couponRepository.findById(orderCreateRequest.getCouponId()))
                .thenReturn(Optional.of(coupon));

        when(cardRepository.findById(orderCreateRequest.getCardId()))
                .thenReturn(Optional.of(card));

       return orderService.createOrder(LocalDateTime.now(),orderCreateRequest);

    }

    public Order commonCardPointOrderProduct(OrderCreateRequest orderCreateRequest, Card card, Member member) throws Exception {

        when(productRepository.findAllById(orderCreateRequest.getProductId()))
                .thenReturn(List.of(product));

        when(couponRepository.findById(orderCreateRequest.getCouponId()))
                .thenReturn(Optional.of(coupon));

        when(cardRepository.findById(orderCreateRequest.getCardId()))
                .thenReturn(Optional.of(card));

        when(memberRepository.findById((orderCreateRequest.getPurchaser())))
                .thenReturn(Optional.of(member));

        return orderService.createOrder(LocalDateTime.now(),orderCreateRequest);

    }

    @Test
    @DisplayName("?????? ?????? ?????? ??????")
    public void orderCardProduct() throws Exception {
        OrderCreateRequest orderCreateRequest = new OrderCreateRequest(List.of(1L,2L),1L,15000,0,
                "??? ???", "CARD",1L,1L);
        Card card = new Card();
//        card.setCardStatus(CardStatus.TRANSACTION_POSSIBILITY);
//        card.setMoney(30000);

        Order newOrder = commonCardOrderProduct(orderCreateRequest, card);

        assertEquals(card.getMoney(),15000);
        assertEquals(newOrder.getOrderStatus(), OrderStatus.SHIPPING_PREPARATION);
    }

    @Test
    @DisplayName("????????? ?????? ?????? ??????")
    public void orderPointProduct() throws Exception {
        OrderCreateRequest orderCreateRequest = new OrderCreateRequest(List.of(1L,2L),1L,15000,15000,
                "??? ???", "POINT",1L,1L);
        Member member = Mockito.mock(Member.class);
//        member.setPoint(16000);

        Order newOrder = commonCardPointOrderProduct(orderCreateRequest, card, member);

        assertEquals(member.getPoint(),1000);
        assertEquals(member.getUsedPoint(),15000);
        assertEquals(newOrder.getOrderStatus(), OrderStatus.SHIPPING_PREPARATION);

    }

    @Test
    @DisplayName("????????? ?????? ?????? ?????? ??????")
    public void orderPointCardProduct() throws Exception {
        OrderCreateRequest orderCreateRequest = new OrderCreateRequest(List.of(1L,2L),1L,15000,12000,
                "??? ???", "ALL",1L,1L);
        Card card = new Card();
//        card.setCardStatus(CardStatus.TRANSACTION_POSSIBILITY);
//        card.setMoney(30000);

        Member member = Mockito.mock(Member.class);

//        member.setPoint(16000);

        Order newOrder = commonCardPointOrderProduct(orderCreateRequest, card, member);

        assertEquals(card.getMoney(),27000);
        assertEquals(member.getPoint(),4000);
        assertEquals(member.getUsedPoint(),12000);
        assertEquals(newOrder.getOrderStatus(), OrderStatus.SHIPPING_PREPARATION);
    }

    @Test
    @DisplayName("?????? ?????? ????????? ?????? ?????? ?????? ??????")
    public void orderCardProductFail(){
        OrderCreateRequest orderCreateRequest = new OrderCreateRequest(List.of(1L,2L),1L,15000,12000,
                "??? ???", "ALL",1L,1L);
        Card card = new Card();
//        card.setCardStatus(CardStatus.TRANSACTION_STOP);

        Assertions.assertThrows(NoSuchElementException.class,
                () ->  commonCardOrderProduct(orderCreateRequest, card)).printStackTrace();
    }

    @Test
    @DisplayName("????????? ??????????????? ?????? ?????? ??????")
    public void orderPointProductFail() throws Exception {
        OrderCreateRequest orderCreateRequest = new OrderCreateRequest(List.of(1L,2L),1L,15000,15000,
                "??? ???", "POINT",1L,1L);

        Member member = Mockito.mock(Member.class);
//        member.setPoint(14999);

        Assertions.assertThrows(NotPaymentPointException.class,
                () ->  commonCardPointOrderProduct(orderCreateRequest, card, member)).printStackTrace();
    }

    @Test
    @DisplayName("?????? ?????? ?????? ?????? ?????? ??????")
    public void orderCardMoneyFail() throws Exception {
        OrderCreateRequest orderCreateRequest = new OrderCreateRequest(List.of(1L,2L),1L,15000,0,
                "??? ???", "CARD",1L,1L);
        Card card = new Card();
//        card.setCardStatus(CardStatus.TRANSACTION_POSSIBILITY);
//        card.setMoney(14999);


        Assertions.assertThrows(NotPaymentCardException.class,
                () ->  commonCardOrderProduct(orderCreateRequest, card)).printStackTrace();
    }

    @Test
    @DisplayName("????????? ??? ?????? ?????? ????????? ?????? ?????? ??????")
    public void orderMoneyNoPointFail() throws Exception {
        OrderCreateRequest orderCreateRequest = new OrderCreateRequest(List.of(1L,2L),1L,15000,7000,
                "??? ???", "ALL",1L,1L);
        Card card = new Card();
//        card.setCardStatus(CardStatus.TRANSACTION_POSSIBILITY);
//        card.setMoney(18000);
        Member member = Mockito.mock(Member.class);
//        member.setPoint(6000);

        Assertions.assertThrows(NotPaymentPointException.class,
                () ->  commonCardPointOrderProduct(orderCreateRequest, card, member)).printStackTrace();
    }

    @Test
    @DisplayName("????????? ??? ?????? ?????? ????????? ?????? ?????? ??????")
    public void orderNoMoneyPointFail() throws Exception {
        OrderCreateRequest orderCreateRequest = new OrderCreateRequest(List.of(1L,2L),1L,15000,7000,
                "??? ???", "ALL",1L,1L);
        Card card = new Card();
//        card.setCardStatus(CardStatus.TRANSACTION_POSSIBILITY);
//        card.setMoney(6999);
        Member member = Mockito.mock(Member.class);

//        member.setPoint(8000);


        Assertions.assertThrows(NotPaymentCardException.class,()
                -> commonCardPointOrderProduct(orderCreateRequest, card, member)).printStackTrace();
    }

    @Test
    @DisplayName("?????? ?????? ?????? ????????? ??????")
    public void findMemberOrderProductList(){
        Long memberId = 1L;
        Pageable pageable = PageRequest.of(0,2);
        List<OrderProductListResponse> orderProductListResponse = new ArrayList<>();
        OrderProductListResponse orderProductListResponse1 =
                new OrderProductListResponse(1L,"?????????",15000,"????????????"
                        ,String.valueOf(OrderStatus.COMPLETED),"2022-11-22 02:51:54.881462");
        OrderProductListResponse orderProductListResponse2 = new OrderProductListResponse(2L,"?????????",15000,"????????????"
                ,String.valueOf(OrderStatus.COMPLETED),"2022-11-22 02:51:54.881462");
        orderProductListResponse.add(orderProductListResponse1);
        orderProductListResponse.add(orderProductListResponse2);
        Page<OrderProductListResponse> page = new PageImpl<>(orderProductListResponse,pageable, pageable.getOffset());

        when(memberRepository.findById(memberId))
                .thenReturn(Optional.of(member));

        when(orderRepository.findAllByMemberOrderList(memberId,pageable))
                .thenReturn(page);

        Page<OrderProductListResponse> memberOrderList = orderService.findMemberOrderList(memberId, pageable);

        assertEquals(memberOrderList.getTotalElements(),2);
        assertEquals(memberOrderList.getTotalPages(),1);
    }

    @Test
    @DisplayName("?????? ????????? ?????? ?????? ??? ?????? ?????? ?????? ??????")
    public void findStoreOrderListTest(){
        String city = "???";
        String gu = "???";
        String dong = "???";

        DeliveryPossibilityStoreOrderListDto.DeliveryPossibilityStoreOrderListResponse storeOrderListResponse =
                new DeliveryPossibilityStoreOrderListDto.DeliveryPossibilityStoreOrderListResponse(4,"??????1",1L,"???","???","???","??????");

        when(orderRepository.findAllByStoreOrderList(any(),any(),any()))
                .thenReturn(List.of(storeOrderListResponse));

        List<DeliveryPossibilityStoreOrderListDto.DeliveryPossibilityStoreOrderListResponse> newStoreOrderList = orderService.findStoreOrderList(city,gu,dong);
        
        assertEquals(newStoreOrderList.get(0).getCity(),"???");
        assertEquals(newStoreOrderList.get(0).getGu(),"???");
        assertEquals(newStoreOrderList.get(0).getDong(),"???");
        assertEquals(newStoreOrderList.get(0).getDetail(),"??????");
    }
}