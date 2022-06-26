package com.stockeeper.zookeeper;

import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.server.auth.DigestAuthenticationProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.CountDownLatch;

/**
 * @author jyinjuly
 * @version 2022-06-23 16:52
 * @description TODO
 */
@Slf4j
@SpringBootTest
public class ZookeeperClientOriginalApplicationTests {

    private static final String ZOOKEEPER_SERVER_ADDRESS = "192.168.175.175:2181";
    private static final int SESSION_TIMEOUT = 5000;
    private static ZooKeeper zookeeper = null;

    @BeforeEach
    void init() throws IOException, InterruptedException {
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        zookeeper = new ZooKeeper(ZOOKEEPER_SERVER_ADDRESS, SESSION_TIMEOUT, event -> {
            if (event.getState() == Watcher.Event.KeeperState.SyncConnected && event.getType() == Watcher.Event.EventType.None) {
                log.info("Zookeeper连接成功.");
                countDownLatch.countDown();
            }
        });
        log.info("Zookeeper连接中......");
        countDownLatch.await();
    }

    /**
     * 生成 Digest 密钥
     */
    @Test
    void testGenerateDigest() throws NoSuchAlgorithmException {
        log.info(DigestAuthenticationProvider.generateDigest("admin:admin123"));
    }




}
