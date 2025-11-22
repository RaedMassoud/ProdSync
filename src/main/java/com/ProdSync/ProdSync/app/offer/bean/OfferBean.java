package com.ProdSync.ProdSync.app.offer.bean;

import com.ProdSync.ProdSync.app.costConstants.CostConstants;
import com.ProdSync.ProdSync.app.product.bean.ProductBean;
import com.ProdSync.ProdSync.execption.RestException;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OfferBean {
    private Integer id;
    private String name;

    private BigDecimal sellingPrice;
	private BigDecimal targetCAC;

	private Integer quantity;
	private ProductBean product;

	@Builder.Default
	private Double delivery = CostConstants.FIXED_DELIVERY_COST;

	// Calculated Fields
	private BigDecimal totalCost;
	private BigDecimal grossProfit;
	private BigDecimal grossMargin;
	private BigDecimal ROAS;
	private BigDecimal netProfit;
	private BigDecimal netMargin;


	public void calculateTotalCost() {
		if (product.getFinalCost() == null)
			throw RestException.INVALID("Offer final cost is not calculated.");

		this.totalCost = product.getFinalCost()
			.multiply(BigDecimal.valueOf(quantity))
			.add(BigDecimal.valueOf(delivery))
			.setScale(3, RoundingMode.HALF_UP);
	}

	public void calculateGrossProfitAndMargin() {
		if (totalCost == null)
			throw RestException.INVALID("Total cost is not calculated.");

		this.grossProfit = sellingPrice
			.subtract(totalCost)
			.setScale(3, RoundingMode.HALF_UP);;

		if (sellingPrice.compareTo(BigDecimal.ZERO) == 0) {
			this.grossMargin = BigDecimal.ZERO;
		} else {
			this.grossMargin = grossProfit
				.divide(sellingPrice, 3, RoundingMode.HALF_UP)
				.multiply(BigDecimal.valueOf(100));
		}
	}

	public void calculateROAS() {
		if (totalCost == null)
			throw RestException.INVALID("Total cost is not calculated.");

		if (totalCost.compareTo(BigDecimal.ZERO) == 0) {
			this.ROAS = BigDecimal.ZERO;
		} else {
			this.ROAS = sellingPrice
				.divide(grossProfit, 3, RoundingMode.HALF_UP);
		}
	}

	public void calculateNetProfitAndMargin() {
		if (grossProfit == null)
			throw RestException.INVALID("Gross profit is not calculated.");

		this.netProfit = grossProfit
			.subtract(targetCAC)
			.setScale(3, RoundingMode.HALF_UP);;

		if (sellingPrice.compareTo(BigDecimal.ZERO) == 0) {
			this.netMargin = BigDecimal.ZERO;
		} else {
			this.netMargin = netProfit
				.divide(sellingPrice, 3, RoundingMode.HALF_UP)
				.multiply(BigDecimal.valueOf(100));
		}
	}
}