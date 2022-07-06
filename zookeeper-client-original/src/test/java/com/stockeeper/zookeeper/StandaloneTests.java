package com.stockeeper.zookeeper;

import com.alibaba.fastjson.JSONObject;
import com.stockeeper.zookeeper.dto.ConfigDto;
import com.stockeeper.zookeeper.watcher.ConfigWatcher;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
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
public class StandaloneTests {

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

    /**
     * 模拟注册中心场景
     *
     * @throws KeeperException
     * @throws InterruptedException
     */
    @Test
    void testConfigCenter() throws KeeperException, InterruptedException {
        ConfigDto configDto = new ConfigDto("张三", 18);

        // 幂等操作，创建之前先删除
        Stat exists = zookeeper.exists(ConfigWatcher.CONFIG_NODE_PATH, null);
        if (exists != null) {
            zookeeper.delete(ConfigWatcher.CONFIG_NODE_PATH, exists.getVersion());
        }

        // 创建节点
        zookeeper.create(ConfigWatcher.CONFIG_NODE_PATH, JSONObject.toJSONBytes(configDto), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);

        // 获取节点数据并注册监听
        byte[] configData = zookeeper.getData(ConfigWatcher.CONFIG_NODE_PATH, new ConfigWatcher(zookeeper), null);

        ConfigDto originConfig = JSONObject.parseObject(configData, ConfigDto.class);
        log.info("初始化配置：{}", originConfig);

        Thread.sleep(Integer.MAX_VALUE);
    }

    /**
     * 模拟乐观锁机制
     *
     * @throws KeeperException
     * @throws InterruptedException
     */
    @Test
    void testOptimisticLocking() throws KeeperException, InterruptedException {
        String nodePath = "/optimistic-locking";
        Stat exists = zookeeper.exists(nodePath, null);
        if (exists == null) {
            zookeeper.create(nodePath, "optimistic-locking-data1".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        }

        // 先获取当前数据版本
        Stat stat = new Stat();
        zookeeper.getData(nodePath, false, stat);

        // 修改时与当前数据版本比较，如果版本一致，则允许修改
        int version = stat.getVersion();
        zookeeper.setData(nodePath, "optimistic-locking-data2".getBytes(), version);

        Thread.sleep(Integer.MAX_VALUE);
    }

    /**
     * 异步API调用
     *
     * @throws KeeperException
     * @throws InterruptedException
     */
    @Test
    void testAsyncAPI() throws KeeperException, InterruptedException {
        String nodePath = "/async-api";
        Stat exists = zookeeper.exists(nodePath, null);
        if (exists == null) {
            zookeeper.create(nodePath, "async-api-data".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        }

        // 模拟上下文
        JSONObject context = new JSONObject();
        context.put("userId", 1);

        zookeeper.getData(nodePath, false, (rc, path, ctx, data, stat) -> {
            // main-EventThread
            Thread thread = Thread.currentThread();
            log.info("Thread Name: {}, rc: {}, path: {}, ctx: {}, data: {}, stat: {}", thread.getName(), rc, path, ctx, new String(data), stat);
        }, context);

        log.info("Over......");

        Thread.sleep(Integer.MAX_VALUE);
    }


}
