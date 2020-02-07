package com.reactive.dc.webflux.app.models.dao;

import com.reactive.dc.webflux.app.models.document.Category;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface CategoryDao extends ReactiveMongoRepository<Category, String> {
}
