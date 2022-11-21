package com.project.product.repository.coupon;

import com.project.product.domain.event.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponRepository extends JpaRepository<Coupon,Long> {
}