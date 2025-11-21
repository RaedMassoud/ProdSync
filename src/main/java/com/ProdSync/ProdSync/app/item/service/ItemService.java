package com.ProdSync.ProdSync.app.item.service;

import com.ProdSync.ProdSync.app.item.bean.ItemBean;
import com.ProdSync.ProdSync.app.item.dao.ItemRepository;
import com.ProdSync.ProdSync.app.item.domain.Item;
import com.ProdSync.ProdSync.app.item.param.ItemParam;
import com.ProdSync.ProdSync.app.supplier.domain.Supplier;
import com.ProdSync.ProdSync.app.supplier.repository.SupplierRepository;
import com.ProdSync.ProdSync.execption.RestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class ItemService {

    private final ItemRepository itemRepository;
    private final SupplierRepository supplierRepository;

    private void validateDuplicateSerialNumber(Long serialNumber, Integer id) {
        Optional<Item> existingItem = itemRepository.findBySerialNumber(serialNumber);
        if (existingItem.isPresent() && !existingItem.get().getId().equals(id))
            throw RestException.INVALID("An item with this serial number already exists");
    }

    public ItemBean getItemBean(Integer id) {
        if (id == null || id <= 0)
            throw RestException.INVALID("Item ID is required");

        Item item = itemRepository.findById(id)
                .orElseThrow(() -> RestException.INVALID("Item not found"));

        return ItemBean.builder()
                .id(item.getId())
                .name(item.getName())
                .altName(item.getAltName())
                .serialNumber(item.getSerialNumber())
                .price(item.getPrice())
                .weight(item.getWeight())
                .supplierId(item.getSupplier().getId())
                .supplierName(item.getSupplier().getName())
                .build();
    }

    public List<ItemBean> getAllItemBeans() {
        return itemRepository.findAll().stream()
                .map(item ->
                        ItemBean.builder()
                                .id(item.getId())
                                .name(item.getName())
                                .altName(item.getAltName())
                                .serialNumber(item.getSerialNumber())
                                .price(item.getPrice())
                                .weight(item.getWeight())
                                .supplierId(item.getSupplier().getId())
                                .supplierName(item.getSupplier().getName())
                                .build()).toList();
    }

    public void submit(ItemParam param) {
        validateDuplicateSerialNumber(param.getSerialNumber(), null);

        Item item = Item.builder()
                .name(param.getName())
                .altName(param.getAltName())
                .serialNumber(param.getSerialNumber())
                .price(param.getPrice())
                .weight(param.getWeight())
                .supplier(Supplier.builder().id(param.getSupplierId()).build())
                .build();

        itemRepository.save(item);
    }

    public void update(ItemParam param) {
        if (param.getId() == null || param.getId() <= 0)
            throw RestException.INVALID("Item ID is required");

        Item item = itemRepository.findById(param.getId())
                .orElseThrow(() -> RestException.INVALID("Item not found"));

        validateDuplicateSerialNumber(param.getSerialNumber(), param.getId());

        item.setName(param.getName());
        item.setAltName(param.getAltName());
        item.setSerialNumber(param.getSerialNumber());
        item.setPrice(param.getPrice());
        item.setWeight(param.getWeight());
        item.setSupplier(Supplier.builder().id(param.getSupplierId()).build());

        itemRepository.save(item);
    }

    public void delete(Integer id) {
        if (id == null || id <= 0)
            throw RestException.INVALID("Item ID is required");

        itemRepository.deleteById(id);
    }
}
