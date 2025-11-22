package com.ProdSync.ProdSync.app.offer.respository;

import com.ProdSync.ProdSync.app.offer.domain.Offer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OfferRepository extends JpaRepository<Offer, Integer> {
	Optional<Offer> findByName(String name);
	List<Offer> findAllByProductId(Integer productId);
}