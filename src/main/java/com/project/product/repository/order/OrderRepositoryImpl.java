package com.project.product.repository.order;

import com.project.product.domain.member.QMember;
import com.project.product.domain.order.Order;
import com.project.product.domain.order.OrderStatus;
import com.project.product.domain.order.QOrder;
import com.project.product.domain.product.QProduct;
import com.project.product.domain.store.QStore;
import com.project.product.dto.delivery.DeliveryPossibilityStoreOrderListDto.DeliveryPossibilityStoreOrderListResponse;
import com.project.product.dto.delivery.QDeliveryPossibilityStoreOrderListDto_DeliveryPossibilityStoreOrderListResponse;
import com.project.product.dto.order.OrderPurchaserAddressResponse;
import com.project.product.dto.order.QOrderPurchaserAddressResponse;
import com.project.product.dto.product.OrderProductListResponse;
import com.project.product.dto.product.QOrderProductListResponse;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import javax.persistence.EntityManager;
import java.util.List;

public class OrderRepositoryImpl implements OrderRepositoryCustom{

    private static final QOrder qOrder = QOrder.order;
    private static final QProduct qProduct = QProduct.product;
    private static final QStore qStore = QStore.store;
    private static final QMember qMember = QMember.member;

    private final JPAQueryFactory queryFactory;

    public OrderRepositoryImpl(EntityManager entityManager) {
        this.queryFactory = new JPAQueryFactory(entityManager);
    }

    @Override
    public Page<OrderProductListResponse> findAllByMemberOrderList(Long memberId, Pageable pageable) {
        List<OrderProductListResponse> orderProductListDtoList = queryFactory
                .select(new QOrderProductListResponse(
                        qProduct.id,
                        qProduct.name,
                        qProduct.price,
                        qProduct.category,
                        qOrder.orderStatus.stringValue(),
                        qOrder.paymentDateTime.stringValue()
                ))
                .from(qOrder)
                .leftJoin(qProduct)
                .on(qProduct.order.id.eq(qOrder.id))
                .where(
                        qOrder.member.id.eq(memberId),
                        qOrder.orderStatus.notIn(OrderStatus.CANCELLATION)
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        return new PageImpl<>(orderProductListDtoList,pageable,pageable.getOffset());
    }

    @Override
    public List<DeliveryPossibilityStoreOrderListResponse> findAllByStoreOrderList(String city, String gu, String dong) {
        return queryFactory
                .select(new QDeliveryPossibilityStoreOrderListDto_DeliveryPossibilityStoreOrderListResponse(
                        qStore.id.count().intValue(),
                        qStore.name,
                        qStore.id,
                        qStore.city,
                        qStore.gu,
                        qStore.dong,
                        qStore.detail
                ))
                .from(qStore)
                .leftJoin(qOrder)
                .on(qOrder.store.id.eq(qStore.id))
                .where(
                        qOrder.orderStatus.eq(OrderStatus.SHIPPING_PREPARATION),
                        qStore.city.eq(city),
                        qStore.gu.eq(gu),
                        qStore.dong.eq(dong)
                )
                .groupBy(qStore.id)
                .fetch();
    }

    @Override
    public List<Order> findByStoreIdAndOrderStatusIs(Long storeId, OrderStatus orderStatus) {
        return queryFactory
                .selectFrom(qOrder)
                .leftJoin(qOrder.products,qProduct)
                .fetchJoin()
                .where(qOrder.store.id.eq(storeId).and(qOrder.orderStatus.eq(orderStatus)))
                .fetch();
    }

    @Override
    public OrderPurchaserAddressResponse findByOrderIdPurchaserAddress(Long orderId) {
        return queryFactory
                .select(new QOrderPurchaserAddressResponse(
                        qOrder.id,
                        qMember.addressCity,
                        qMember.addressGu,
                        qMember.addressDong,
                        qMember.addressDetail
                ))
                .from(qOrder)
                .leftJoin(qOrder.member,qMember)
                .where(qOrder.id.eq(orderId))
                .fetchOne();
    }
}
