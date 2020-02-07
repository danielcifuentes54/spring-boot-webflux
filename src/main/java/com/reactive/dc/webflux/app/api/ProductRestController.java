package com.reactive.dc.webflux.app.api;

import com.reactive.dc.webflux.app.controllers.ProductController;
import com.reactive.dc.webflux.app.models.dao.ProductDao;
import com.reactive.dc.webflux.app.models.document.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/products")
public class ProductRestController {

    @Autowired
    private ProductDao productDao;

    private static final Logger log = LoggerFactory.getLogger(ProductController.class);

    @GetMapping()
    public Flux<Product> index(){
        Flux<Product> products = productDao.findAll().map(product -> {
            product.setName(product.getName().toUpperCase());
            return product;
        }).doOnNext(product -> log.info(product.getName()));

        return products;
    }

    @GetMapping("/{id}")
    public Mono<Product> findById(@PathVariable String id){

        //return productDao.findById(id);
        Flux<Product> products = productDao.findAll();
        Mono<Product> product = products
                .filter(product1 -> product1.getId().equals(id))
                .next()
                .doOnNext(product1 -> log.info(product1.getName()));
        return product;
    }
}
