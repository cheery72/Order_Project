package com.project.product.service.order;

import com.project.product.domain.order.Order;
import com.project.product.domain.order.OrderStatus;
import com.project.product.dto.delivery.DeliveryPossibilityStoreOrderListDto.DeliveryPossibilityStoreOrderListResponse;
import com.project.product.dto.order.OrderCreateRequest;
import com.project.product.dto.order.OrderPurchaserAddressResponse;
import com.project.product.dto.order.StoreOrderProductNameListResponse;
import com.project.product.dto.product.OrderProductListResponse;
import com.project.product.exception.NotFoundMemberException;
import com.project.product.repository.member.MemberRepository;
import com.project.product.repository.order.OrderRepository;
import com.project.product.repository.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public Order createOrder(LocalDateTime paymentTime, OrderCreateRequest orderCreateRequest) {
        return orderRepository.save(Order.orderBuilder(paymentTime, orderCreateRequest,
                productRepository.findAllById(orderCreateRequest.getProductId())));
    }

    //Todo: 배달 가능지 가게 정보 및 가게 주문 건수 조회
    public List<DeliveryPossibilityStoreOrderListResponse> findStoreOrderList(String city, String gu, String dong){
        return orderRepository.findAllByStoreOrderList(city, gu, dong);
    }

    //Todo: 주문한 물품 전체 조회
    public Page<OrderProductListResponse> findMemberOrderList(Long memberId, Pageable pageable){
        memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundMemberException("요청한 멤버를 찾을 수 없습니다."));

        return orderRepository.findAllByMemberOrderList(memberId, pageable);
    }

    @Transactional
    public void deleteOrder(Long orderId){
        orderRepository.deleteById(orderId);
    }

    //Todo: 가게 전체 주문 정보 조회
    public List<StoreOrderProductNameListResponse> findStoreOrderProductNameList(Long storeId){
        return StoreOrderProductNameListResponse.toStoreOrderListResponse(orderRepository
                .findByStoreIdAndOrderStatusIs(storeId, OrderStatus.SHIPPING_PREPARATION));
    }


    //Todo: 주문 구매자 주소 조회
    public OrderPurchaserAddressResponse findOrderPurchaserAddress(Long orderId){
        return orderRepository.findByOrderIdPurchaserAddress(orderId);
    }

    //Todo: 배송 중으로 변경하기 위해서는 택배사랑 연결이 되어야되는 로직 구성
}

