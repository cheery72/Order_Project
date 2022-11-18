package com.project.product.domain.product;

import com.project.product.domain.member.ShoppingBasket;
import com.project.product.domain.order.Order;
import com.project.product.dto.ProductRegisterDto;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String seller;

    private String name;

    private int price;

    private String category;

    private ProductStatus productStatus;

    @OneToMany(mappedBy = "product",cascade = CascadeType.ALL,orphanRemoval = true)
    private List<ProductImage> images = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shopping_basket_id")
    private ShoppingBasket shoppingBasket;

    @Builder
    public Product(Long id, String seller, String name, int price, String category, ProductStatus productStatus, List<ProductImage> images, Order order, ShoppingBasket shoppingBasket) {
        this.id = id;
        this.seller = seller;
        this.name = name;
        this.price = price;
        this.category = category;
        this.productStatus = productStatus;
        this.images = images;
        this.order = order;
        this.shoppingBasket = shoppingBasket;
    }

    public static Product productBuilder(ProductRegisterDto productRegisterDto){
        return Product.builder()
                .seller(productRegisterDto.getSeller())
                .name(productRegisterDto.getName())
                .price(productRegisterDto.getPrice())
                .category(productRegisterDto.getCategory())
                .images(ProductImage.productImageBuilder(productRegisterDto.getImages()))
                .productStatus(ProductStatus.VERIFICATION)
                .build();
    }
}
