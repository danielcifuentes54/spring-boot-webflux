package com.reactive.dc.webflux.app.controllers;

import com.reactive.dc.webflux.app.models.document.Category;
import com.reactive.dc.webflux.app.models.document.Product;
import com.reactive.dc.webflux.app.models.services.CategoryService;
import com.reactive.dc.webflux.app.models.services.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.thymeleaf.spring5.context.webflux.ReactiveDataDriverContextVariable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDate;
import java.util.UUID;

@SessionAttributes("product")
@Controller
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    private static final Logger log = LoggerFactory.getLogger(ProductController.class);

    @Value("${config.uploads.path}")
    private String path;

    @ModelAttribute("categories")
    public Flux<Category> getCategories(){
        return categoryService.findAll();
    }

    @GetMapping({"/list", "/"})
    public String list(Model model){
        Flux<Product> products = productService.findAllWithUpperCaseName();

        products.subscribe(product -> log.info(product.getName()));
        model.addAttribute("products", products);
        model.addAttribute("title", "List of Products");
        return "list";
    }

    @GetMapping("/form")
    public Mono<String> create(Model model){
        model.addAttribute("product", new Product());
        model.addAttribute("title", "New Product");
        return Mono.just("form");
    }

    @PostMapping("/form")
    public Mono<String> save(@Valid Product product, BindingResult result, Model model, @RequestPart FilePart file, SessionStatus status){

        if(result.hasErrors()){
            model.addAttribute("title", "Errors in the form");
            return Mono.just("form");
        }

        status.setComplete();

        Mono<Category> category = categoryService.findById(product.getCategory().getId());

        return category
                .flatMap(cat -> {
                    if(product.getCreateAt() == null){
                        product.setCreateAt(LocalDate.now());
                    }
                    if(!file.filename().isEmpty()){
                        product.setPhoto(UUID.randomUUID() + "-" + file.filename()
                                .replaceAll(" ", ""));
                    }
                    product.setCategory(cat);
                    return productService.save(product); })
                .doOnNext(pro -> {
                    log.info("product saved id: {} name:{} category:{}", pro.getId(), pro.getName(), pro.getCategory());
                })
                .flatMap(p -> {
                    if(!file.filename().isEmpty()){
                        return file.transferTo(new File(path + p.getPhoto()));
                    }
                    return Mono.empty();
                })
                .thenReturn("redirect:/list?success=The+product+has+been+saved");
    }

    @GetMapping("/form-V2/{id}")
    public Mono<String> editV2(@PathVariable String id, Model model){

        return productService.findById(id)
                .doOnNext(pro -> {
                    log.info("Product to Edit id: {} name: {}", pro.getId(), pro.getName());
                    model.addAttribute("title", "Edit Product");
                    model.addAttribute("product", pro); })
                .defaultIfEmpty(new Product())
                .flatMap(pro -> {
                    if (pro.getId() == null){
                        return Mono.error(new InterruptedException("Product not exist"));
                    }
                    return Mono.just(pro);
                })
                .then(Mono.just("form"))
                .onErrorResume(ex -> Mono.just("redirect:/list?error=Product+no+exist"));
    }

    @GetMapping("/form/{id}")
    public Mono<String> edit(@PathVariable String id, Model model){

        Mono<Product> productMono = productService.findById(id).doOnNext(pro -> {
            log.info("Product to Edit id: {} name: {}", pro.getId(), pro.getName());
        }).defaultIfEmpty(new Product());

        model.addAttribute("title", "Edit Product");
        model.addAttribute("product", productMono);
        return Mono.just("form");
    }

    @GetMapping("/delete/{id}")
    public Mono<String> delete(@PathVariable String id){
        return productService.findById(id)
                .defaultIfEmpty(new Product())
                .flatMap(p -> {
                    if (p.getId() == null){
                        new InterruptedException("Product to delete not exist");
                    }
                    return Mono.just(p);
                })
                .flatMap(productService::delete)
                .then(Mono.just("redirect:/list?success=product+eliminated"))
                .onErrorResume(ex -> Mono.just("redirect:/list?error=product+to+delete+not+exist"));
    }

    @GetMapping("/detail/{id}")
    public Mono<String> detail(@PathVariable String id, Model model){
        return productService.findById(id)
                .doOnNext(pro -> {
                    model.addAttribute("product", pro);
                    model.addAttribute("title", "Product Detail - " + pro.getName());
                })
                .flatMap(p -> {
                    if (p.getId() == null){
                        new InterruptedException("Product not exist");
                    }
                    return Mono.just(p);
                }).thenReturn("detail")
                .onErrorResume(ex -> Mono.just("redirect:/list?error=product+not+exist"));
    }

    @GetMapping("/get/photo/{namePhoto:.+}")
    public Mono<ResponseEntity<Resource>> getPhoto(@PathVariable String namePhoto) throws MalformedURLException {
        Path pathPhoto = Paths.get(path).resolve(namePhoto).toAbsolutePath();
        Resource resource = new UrlResource(pathPhoto.toUri());
        return Mono.just(
                ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + resource.getFilename() + "\"" )
                .body(resource)
        );
    }

    @GetMapping("list-datadriver")
    public String listDataDriver(Model model){
        Flux<Product> products = productService.findAllWithUpperCaseName()
                .delayElements(Duration.ofSeconds(1));
        products.subscribe(product -> log.info(product.getName()));
        model.addAttribute("products", new ReactiveDataDriverContextVariable(products, 1));
        model.addAttribute("title", "List of Products");
        return "list";
    }

    @GetMapping("/full-list")
    public String fullList(Model model){
        Flux<Product> products = productService.findAllWithUpperCaseNameAndRepeat(500);
        model.addAttribute("products", products);
        model.addAttribute("title", "List of Products");
        return "list";
    }

    @GetMapping("/full-list-chunked")
    public String fullListChunked(Model model){
        Flux<Product> products = productService.findAllWithUpperCaseNameAndRepeat(500);
        model.addAttribute("products", products);
        model.addAttribute("title", "List of Products");
        return "list-chunked";
    }

}
