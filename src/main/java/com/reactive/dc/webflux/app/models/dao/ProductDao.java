package com.reactive.dc.webflux.app.models.dao;

import com.reactive.dc.webflux.app.models.document.Product;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface ProductDao extends ReactiveMongoRepository<Product, String> {
}
