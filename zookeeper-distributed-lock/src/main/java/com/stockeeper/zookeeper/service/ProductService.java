package com.stockeeper.zookeeper.service;

import com.stockeeper.zookeeper.domain.Product;
import com.stockeeper.zookeeper.mapper.ProductMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("productService")
public class ProductService {

    @Autowired
    private ProductMapper productMapper;

    public Product selectProduct(Long id) {
        return this.productMapper.selectProduct(id);
    }

    public void updateProduct(Product product) {
        this.productMapper.updateProduct(product);
    }

}
