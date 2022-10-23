package com.stockeeper.zookeeper;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan(basePackages = "com.stockeeper.zookeeper.mapper")
public class ZookeeperDistributedLockApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZookeeperDistributedLockApplication.class, args);
    }

}
