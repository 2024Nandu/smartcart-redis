package com.smart.smartcart.service;

import com.smart.smartcart.model.Product;
import com.smart.smartcart.repository.ProductRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final RedisTemplate<String, Product> redisTemplate;

    public ProductServiceImpl(
            ProductRepository productRepository,
            RedisTemplate<String, Product> redisTemplate) {

        this.productRepository = productRepository;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Product createProduct(Product product) {
        return productRepository.save(product);
    }

    @Override
    public Product getProductById(Long id) {

        String key = "product:" + id;

        // 1. Check Redis
        Product cachedProduct = redisTemplate.opsForValue().get(key);

        if (cachedProduct != null) {
            System.out.println("Product found in Redis");
            return cachedProduct;
        }

        // 2. If not found, get from PostgreSQL
        System.out.println("Product found in PostgreSQL");

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Product not found with id: " + id));

        // 3. Store in Redis
        redisTemplate.opsForValue().set(
                key,
                product,
                10,
                TimeUnit.MINUTES
        );

        return product;
    }

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public Product updateProduct(Long id, Product product) {

        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Product not found with id: " + id));

        existingProduct.setName(product.getName());
        existingProduct.setPrice(product.getPrice());
        existingProduct.setCategory(product.getCategory());

        Product updatedProduct =
                productRepository.save(existingProduct);

        // Remove old cached data
        String key = "product:" + id;
        redisTemplate.delete(key);

        return updatedProduct;
    }

    @Override
    public void deleteProduct(Long id) {

        if (!productRepository.existsById(id)) {
            throw new RuntimeException(
                    "Product not found with id: " + id);
        }

        productRepository.deleteById(id);

        // Remove product from Redis
        String key = "product:" + id;
        redisTemplate.delete(key);
    }
}