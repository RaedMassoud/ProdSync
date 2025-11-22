package com.ProdSync.ProdSync.app.product.bean;

import com.ProdSync.ProdSync.app.item.bean.ItemBean;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductBean {
    private Integer id;
    private String name;
    private String altName;
    private Long serialNumber;
    private BigDecimal price;
    private Integer stockQuantity;
    private List<ItemBean> items;

	@Builder.Default
	private BigDecimal overHead = BigDecimal.valueOf(1.1); // 10% overhead
	private BigDecimal landedCost;
	private BigDecimal finalCost;

	public void calculateLandedAndFinalCost() {
		this.landedCost = items.stream()
				.map(ItemBean::getLandedCost)
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		this.finalCost = landedCost
			.multiply(overHead)
			.setScale(3, RoundingMode.HALF_UP);
	}
}