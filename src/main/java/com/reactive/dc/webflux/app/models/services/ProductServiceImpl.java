package com.reactive.dc.webflux.app.models.services;

import com.reactive.dc.webflux.app.models.dao.ProductDao;
import com.reactive.dc.webflux.app.models.document.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ProductServiceImpl implements  ProductService {

    @Autowired
    private ProductDao productDao;

    private static final Logger log = LoggerFactory.getLogger(ProductServiceImpl.class);

    @Override
    public Flux<Product> findAll() {
        return productDao.findAll();
    }

    @Override
    public Flux<Product> findAllWithUpperCaseName() {
        return findAll().map(product -> {
                    product.setName(product.getName().toUpperCase());
                    return product;
                });
    }

    @Override
    public Flux<Product> findAllWithUpperCaseNameAndRepeat(long repeat) {
        return findAllWithUpperCaseName().repeat();
    }

    @Override
    public Mono<Product> findById(String id) {
        return productDao.findById(id);
    }

    @Override
    public Mono<Product> save(Product product) {
        return productDao.save(product);
    }

    @Override
    public Mono<Void> delete(Product product) {
        return productDao.delete(product);
    }

    @Override
    public Mono<Product> insert(Product product) {
        return productDao.insert(product);
    }
}
