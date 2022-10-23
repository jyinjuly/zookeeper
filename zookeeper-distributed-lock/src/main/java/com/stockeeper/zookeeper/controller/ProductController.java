package com.stockeeper.zookeeper.controller;

import com.stockeeper.zookeeper.domain.Product;
import com.stockeeper.zookeeper.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CuratorFramework curatorFramework;

    @GetMapping("/deduct")
    public void test() throws Exception {
        // zookeeper分布式锁
        InterProcessMutex processMutex = new InterProcessMutex(curatorFramework, "/product_1");

        try {
            processMutex.acquire();
            Product product = productService.selectProduct(1L);
            log.info("当前库存：{}", product.getNumber());
            if (product.getNumber() > 0) {
                product.setNumber(product.getNumber()-1);
                productService.updateProduct(product);
                log.info("出库成功，剩余库存：{}", product.getNumber());
            }
        } finally {
            processMutex.release();
        }

        // 单机版加锁
//        synchronized (ProductController.class) {
//            Product product = productService.selectProduct(1L);
//            log.info("当前库存：{}", product.getNumber());
//            if (product.getNumber() > 0) {
//                product.setNumber(product.getNumber()-1);
//                productService.updateProduct(product);
//                log.info("出库成功，剩余库存：{}", product.getNumber());
//            }
//        }
    }

}
