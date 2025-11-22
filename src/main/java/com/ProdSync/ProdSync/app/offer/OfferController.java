package com.ProdSync.ProdSync.app.offer;

import com.ProdSync.ProdSync.app.offer.bean.OfferBean;
import com.ProdSync.ProdSync.app.offer.param.OfferParam;
import com.ProdSync.ProdSync.app.offer.service.OfferService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/offer")
public class OfferController {

    private final OfferService offerService;

	// Offer APIs
    @GetMapping("/{id}")
    public ResponseEntity<OfferBean> getProduct(
		@PathVariable Integer id,
		@RequestParam(required = false) Boolean unitEconomics) {

        return ResponseEntity.ok(offerService.getOfferBean(id, unitEconomics));
    }

    @GetMapping
    public ResponseEntity<List<OfferBean>> getAllProducts() {
        return ResponseEntity.ok(offerService.getAllOfferBeans());
    }

    @PostMapping
    public ResponseEntity<String> createProduct(@RequestBody OfferParam param) {
        offerService.submit(param);
        return ResponseEntity.ok("Offer created successfully");
    }

    @PutMapping
    public ResponseEntity<String> updateProduct(@RequestBody OfferParam param) {
        offerService.update(param);
        return ResponseEntity.ok("Offer updated successfully");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable Integer id) {
        offerService.delete(id);
        return ResponseEntity.ok("Offer deleted successfully");
    }
}