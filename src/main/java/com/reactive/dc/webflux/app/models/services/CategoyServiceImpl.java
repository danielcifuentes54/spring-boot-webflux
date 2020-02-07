package com.reactive.dc.webflux.app.models.services;

import com.reactive.dc.webflux.app.models.dao.CategoryDao;
import com.reactive.dc.webflux.app.models.document.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class CategoyServiceImpl implements CategoryService {

    @Autowired
    CategoryDao categoryDao;

    @Override
    public Flux<Category> findAll() {
        return categoryDao.findAll();
    }

    @Override
    public Mono<Category> findById(String id) {
        return categoryDao.findById(id);
    }

    @Override
    public Mono<Category> save(Category category) {
        return categoryDao.save(category);
    }
}
