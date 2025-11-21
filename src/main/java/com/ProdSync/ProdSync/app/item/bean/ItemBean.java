package com.ProdSync.ProdSync.app.item.bean;

import com.ProdSync.ProdSync.app.product.domain.Product;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemBean {
    private Integer id;
    private String name;
    private String altName;
    private Long serialNumber;
    private BigDecimal price;
    private BigDecimal weight;
    private Integer quantity;
    private Integer supplierId;
    private String supplierName;
}
