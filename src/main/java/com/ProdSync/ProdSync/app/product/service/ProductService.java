package com.ProdSync.ProdSync.app.product.service;

import com.ProdSync.ProdSync.app.item.bean.ItemBean;
import com.ProdSync.ProdSync.app.item.dao.ItemRepository;
import com.ProdSync.ProdSync.app.item.domain.Item;
import com.ProdSync.ProdSync.app.product.bean.ProductBean;
import com.ProdSync.ProdSync.app.product.domain.Product;
import com.ProdSync.ProdSync.app.product.param.ProductParam;
import com.ProdSync.ProdSync.app.product.respository.ProductRepository;
import com.ProdSync.ProdSync.app.productItemMapping.ProductItemMapping;
import com.ProdSync.ProdSync.execption.RestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ItemRepository itemRepository;

    private void validateDuplicateSerialNumber(Long serialNumber, Integer id) {
        Optional<Product> existingProduct = productRepository.findBySerialNumber(serialNumber);
        if (existingProduct.isPresent() && !existingProduct.get().getId().equals(id))
            throw RestException.INVALID("A product with this serial number already exists");
    }

    public ProductBean getProductBean(Integer id) {
        if (id == null || id <= 0)
            throw RestException.INVALID("Product ID is required");

        Product product = productRepository.findById(id)
                .orElseThrow(() -> RestException.INVALID("Product not found"));

        return toBean(product);
    }

    public List<ProductBean> getAllProductBeans() {
        return productRepository.findAll()
                .stream().map(this::toBean).toList();
    }

    public void submit(ProductParam param) {
        validateDuplicateSerialNumber(param.getSerialNumber(), null);

        Product product = Product.builder()
                .name(param.getName())
                .altName(param.getAltName())
                .serialNumber(param.getSerialNumber())
                .price(param.getPrice())
                .stockQuantity(param.getStockQuantity())
                .build();

//        List<ProductItemMapping> mappings = param.getItems().stream()
//                .map(i -> {
//                    Item item = itemRepository.findById(i.getItemId())
//                            .orElseThrow(() -> RestException.INVALID("Item not found: " + i.getItemId()));
//
//                    return ProductItemMapping.builder()
//                            .product(product)
//                            .item(item)
//                            .quantity(i.getQuantity())
//                            .build();
//                }).toList();
//        product.setProductItems(mappings);

        productRepository.save(product);
    }

    public void update(ProductParam param) {
        if (param.getId() == null || param.getId() <= 0)
            throw RestException.INVALID("Product ID is required");

        Product product = productRepository.findById(param.getId())
                .orElseThrow(() -> RestException.INVALID("Product not found"));

        validateDuplicateSerialNumber(param.getSerialNumber(), param.getId());

        product.setName(param.getName());
        product.setAltName(param.getAltName());
        product.setSerialNumber(param.getSerialNumber());
        product.setPrice(param.getPrice());
        product.setStockQuantity(param.getStockQuantity());

        product.getProductItems().clear();
        List<ProductItemMapping> newMappings = param.getItems().stream().map(i -> {
            Item item = itemRepository.findById(i.getItemId())
                    .orElseThrow(() -> RestException.INVALID("Item not found: " + i.getItemId()));

            return ProductItemMapping.builder()
                    .product(product)
                    .item(item)
                    .quantity(i.getQuantity())
                    .build();
        }).toList();

        product.getProductItems().addAll(newMappings);
        productRepository.save(product);
    }

    public void delete(Integer id) {
        if (id == null || id <= 0)
            throw RestException.INVALID("Product ID is required");

        productRepository.deleteById(id);
    }

    private ProductBean toBean(Product product) {
        List<ItemBean> itemBeans = product.getProductItems().stream()
                .map(i -> ItemBean.builder()
                        .id(i.getItem().getId())
                        .name(i.getItem().getName())
                        .altName(i.getItem().getAltName())
                        .serialNumber(i.getItem().getSerialNumber())
                        .price(i.getItem().getPrice())
                        .weight(i.getItem().getWeight())
                        .quantity(i.getQuantity())
                        .build()
                ).toList();

        return ProductBean.builder()
                .id(product.getId())
                .name(product.getName())
                .altName(product.getAltName())
                .serialNumber(product.getSerialNumber())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .items(itemBeans)
                .build();
    }
}
