package com.stockeeper.zookeeper;

import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author jyinjuly
 * @version 2022-06-23 16:52
 * @description TODO
 */
@Slf4j
@SpringBootTest
public class ClusterTests {

    private static final String ZOOKEEPER_CLUSTER_ADDRESS = "192.168.175.175:2181,192.168.175.175:2182,192.168.175.175:2183,192.168.175.175:2184";
    private static final int SESSION_TIMEOUT = 60 * 1000;
    private static ZooKeeper zookeeper = null;

    @BeforeEach
    void init() throws IOException, InterruptedException {
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        zookeeper = new ZooKeeper(ZOOKEEPER_CLUSTER_ADDRESS, SESSION_TIMEOUT, event -> {
            if (event.getState() == Watcher.Event.KeeperState.SyncConnected && event.getType() == Watcher.Event.EventType.None) {
                log.info("Zookeeper连接成功.");
                countDownLatch.countDown();
            }
        });
        log.info("Zookeeper连接中......");
        countDownLatch.await();
    }

    /**
     * 模拟客户端重连
     *
     * 使用原生Zookeeper API 连接客户端，当客户端连接断开时会抛异常，需要手动捕获处理
     *
     * @throws InterruptedException
     * @throws KeeperException
     */
    @Test
    void testReconnect() throws InterruptedException, KeeperException {
        /*
         * 模拟每隔5秒获取一次数据
         */
        while (true) {
            try {
                Stat stat = new Stat();
                byte[] data = zookeeper.getData("/zookeeper/config", false, stat);
                log.info("get data: {}", new String(data));
                TimeUnit.SECONDS.sleep(5);
            } catch (KeeperException.ConnectionLossException ex) {
                /*
                 * 节点挂掉，客户端连接中断，进入异常处理
                 */
                ex.printStackTrace();
                log.info("开始自动重连......");
                while (true) {
                    log.info("zookeeper status: {}", zookeeper.getState().name());
                    if (zookeeper.getState().isConnected()) {
                        break;
                    }
                    /*
                     * 每隔一秒监控一次连接状态
                     */
                    TimeUnit.SECONDS.sleep(1);
                }
            }
        }

    }


}
