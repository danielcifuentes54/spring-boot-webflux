package com.reactive.dc.webflux.app.models.services;

import com.reactive.dc.webflux.app.models.document.Product;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductService {

    Flux<Product> findAll();

    Flux<Product> findAllWithUpperCaseName();

    Flux<Product> findAllWithUpperCaseNameAndRepeat(long repeat);

    Mono<Product> findById(String id);

    Mono<Product> save(Product product);

    Mono<Void> delete(Product product);

    Mono<Product> insert(Product product);

}
