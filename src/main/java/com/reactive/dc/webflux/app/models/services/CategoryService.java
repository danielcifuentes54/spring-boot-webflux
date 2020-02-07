package com.reactive.dc.webflux.app.models.services;

import com.reactive.dc.webflux.app.models.document.Category;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CategoryService {

    Flux<Category> findAll();

    Mono<Category> findById(String id);

    Mono<Category> save(Category category);
}
