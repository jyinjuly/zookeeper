package com.stockeeper.zookeeper;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.TimeUnit;

@Slf4j
@SpringBootTest
public class ClusterTests {

    private static final String ZOOKEEPER_SERVER_ADDRESS = "192.168.175.175:2181,192.168.175.175:2182,192.168.175.175:2183,192.168.175.175:2184";
    private static final int SESSION_TIMEOUT = 60 * 1000;
    private static final int CONNECTION_TIMEOUT = 5000;
    private static CuratorFramework curatorFramework = null;

    @BeforeEach
    void init() {
        // 每隔5s重试一次，最大重试30次
        ExponentialBackoffRetry retry = new ExponentialBackoffRetry(5000, 30);
        curatorFramework = CuratorFrameworkFactory.builder().connectString(ZOOKEEPER_SERVER_ADDRESS)
                .retryPolicy(retry)
                .sessionTimeoutMs(SESSION_TIMEOUT)
                .connectionTimeoutMs(CONNECTION_TIMEOUT)
                .canBeReadOnly(true)
                .build();

        curatorFramework.getConnectionStateListenable().addListener(((client, newState) -> {
            if (newState == ConnectionState.CONNECTED) {
                log.info("Zookeeper连接成功.");
            }
        }));
        log.info("Zookeeper连接中......");
        curatorFramework.start();
    }

    /**
     * 模拟客户端重连
     *
     * 使用Curator API 连接客户端，当客户端连接断开时会自动重连，不会抛异常（客户端无感知）
     *
     * @throws Exception
     */
    @Test
    void testReconnect() throws Exception {
        while (true) {
            byte[] data = curatorFramework.getData().forPath("/zookeeper/config");
            log.info("get data: {}", new String(data));
            TimeUnit.SECONDS.sleep(1);
        }
    }



}
