package com.ProdSync.ProdSync.app.offer.service;

import com.ProdSync.ProdSync.app.item.bean.ItemBean;
import com.ProdSync.ProdSync.app.offer.bean.OfferBean;
import com.ProdSync.ProdSync.app.offer.domain.Offer;
import com.ProdSync.ProdSync.app.offer.param.OfferParam;
import com.ProdSync.ProdSync.app.offer.respository.OfferRepository;
import com.ProdSync.ProdSync.app.product.bean.ProductBean;
import com.ProdSync.ProdSync.app.product.domain.Product;
import com.ProdSync.ProdSync.execption.RestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class OfferService {

    private final OfferRepository offerRepository;

    private void validateDuplicateName(String name, Integer id) {
        Optional<Offer> existing = offerRepository.findByName(name);
		if (existing.isPresent() && !existing.get().getId().equals(id))
			throw RestException.INVALID("An offer with this name already exists");
    }

    public OfferBean getOfferBean(Integer id, Boolean unitEconomics) {
        if (id == null || id <= 0)
            throw RestException.INVALID("Offer ID is required");

        Offer offer = offerRepository.findById(id)
                .orElseThrow(() -> RestException.INVALID("Offer not found"));

        return toBean(offer, unitEconomics != null ? unitEconomics : false);
    }

    public List<OfferBean> getAllOfferBeans() {
        return offerRepository.findAll().stream()
				.map(o -> toBean(o, false)).toList();
    }

    public void submit(OfferParam param) {
		if (param.getProductId() == null || param.getProductId() <= 0)
			throw RestException.INVALID("Product ID is required");

		if (param.getQuantity() == null || param.getQuantity() <= 0)
			throw RestException.INVALID("Quantity is required and must be greater than zero");

	    validateDuplicateName(param.getName(), null);

        Offer offer = Offer.builder()
	        .name(param.getName())
	        .sellingPrice(param.getSellingPrice())
	        .targetCAC(param.getTargetCAC() != null ? param.getTargetCAC() : BigDecimal.ZERO)
	        .quantity(param.getQuantity())
	        .product(new Product(param.getProductId()))
	        .build();

		offerRepository.save(offer);
    }

    public void update(OfferParam param) {
        if (param.getId() == null || param.getId() <= 0)
            throw RestException.INVALID("Offer ID is required");

		if (param.getProductId() == null || param.getProductId() <= 0)
			throw RestException.INVALID("Product ID is required");

		if (param.getQuantity() == null || param.getQuantity() <= 0)
			throw RestException.INVALID("Quantity is required and must be greater than zero");

        Offer offer = offerRepository.findById(param.getId())
                .orElseThrow(() -> RestException.INVALID("Offer not found"));

	    validateDuplicateName(param.getName(), param.getId());

        offer.setName(param.getName());
		offer.setSellingPrice(param.getSellingPrice());
		offer.setTargetCAC(param.getTargetCAC() != null ? param.getTargetCAC() : BigDecimal.ZERO);
		offer.setQuantity(param.getQuantity());
		offer.setProduct(new Product(param.getProductId()));

        offerRepository.save(offer);
    }

    public void delete(Integer id) {
        if (id == null || id <= 0)
            throw RestException.INVALID("Offer ID is required");

        offerRepository.deleteById(id);
    }

	public List<OfferBean> getOfferBeansByProductId(Integer id, Boolean unitEconomics) {
		if (id == null || id <= 0)
			throw RestException.INVALID("Product ID is required");

		return offerRepository.findAllByProductId(id).stream()
			.map(o -> toBean(o, unitEconomics != null ? unitEconomics : false))
			.toList();
	}

    private OfferBean toBean(Offer offer, boolean unitEconomics) {
		OfferBean bean = OfferBean.builder()
			.id(offer.getId())
			.name(offer.getName())
			.sellingPrice(offer.getSellingPrice())
			.targetCAC(offer.getTargetCAC())
			.quantity(offer.getQuantity())
			.build();

		if (!unitEconomics) {
			bean.setProduct(ProductBean.builder()
				.id(offer.getProduct().getId())
				.name(offer.getProduct().getName())
				.price(offer.getProduct().getPrice())
				.stockQuantity(offer.getProduct().getStockQuantity())
				.build());
			return bean;
		}

	    // Convert product → ProductBean with items
	    Product product = offer.getProduct();

	    ProductBean productBean = ProductBean.builder()
		    .id(product.getId())
		    .name(product.getName())
		    .price(product.getPrice())
		    .stockQuantity(product.getStockQuantity())
		    .items(
			    product.getProductItems().stream()
				    .map(i -> {
					    ItemBean item = ItemBean.builder()
						    .price(i.getItem().getPrice())
						    .weight(i.getItem().getWeight())
						    .quantity(i.getQuantity())
						    .build();

					    item.calculateCosts();
					    return item;
				    })
				    .toList()
		    )
		    .build();

	    productBean.calculateLandedAndFinalCost();
	    bean.setProduct(productBean);

	    bean.calculateTotalCost();
	    bean.calculateGrossProfitAndMargin();
	    bean.calculateROAS();
	    bean.calculateNetProfitAndMargin();

	    return bean;
    }
}