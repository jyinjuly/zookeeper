package com.stockeeper.zookeeper;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@SpringBootTest
public class ZookeeperClientCuratorApplicationTests {

    private static final String ZOOKEEPER_SERVER_ADDRESS = "192.168.175.175:2181";
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
     * 递归创建子节点
     *
     * @throws Exception
     */
    @Test
    void testCreateWithParent() throws Exception {
        String path = curatorFramework.create().creatingParentsIfNeeded().forPath("/node-parent/sub-node-1");
        log.info("curator create node: {} successful.", path);
    }

    /**
     * Protection模式，防止由于异常原因，导致僵尸节点
     * 1、客户端向服务端发送创建节点请求
     * 2、服务端成功创建，向客户端发送响应
     * 3、由于种种原因，导致客户端接收响应失败
     * 4、客户端重试机制再次向服务端发送创建节点请求
     * 5、服务端判断之前的节点为受保护模式节点（/_c_96d4a62b-67f7-4087-a1b3-9ae5531f4987-curator-node0000000042），将不再创建节点
     *
     * @throws Exception
     */
    @Test
    void testCreateWithProtection() throws Exception {
        String path = curatorFramework.create()
                .withProtection()
                .withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
                .forPath("/curator-node", "curator-node-data".getBytes());
        log.info("curator create node: {} successful.", path);
    }

    /**
     * 获取数据
     *
     * @throws Exception
     */
    @Test
    void testGetData() throws Exception {
        Stat stat = curatorFramework.checkExists().forPath("/curator-node");
        if (stat == null) {
            curatorFramework.create().forPath("/curator-node", "curator-node-data".getBytes());
        }
        byte[] bytes = curatorFramework.getData().forPath("/curator-node");
        log.info("get data from node /curator-node successful: {}", new String(bytes));
    }

    /**
     * 设置数据
     *
     * @throws Exception
     */
    @Test
    void testSetData() throws Exception {
        Stat stat = curatorFramework.checkExists().forPath("/curator-node");
        if (stat == null) {
            curatorFramework.create().forPath("/curator-node", "curator-node-data".getBytes());
        }
        curatorFramework.setData().forPath("/curator-node", "curator-node-data-changed".getBytes());

        byte[] bytes = curatorFramework.getData().forPath("/curator-node");
        log.info("get data from node /curator-node successful: {}", new String(bytes));
    }

    /**
     * 递归删除节点
     *
     * @throws Exception
     */
    @Test
    void testDelete() throws Exception {
        String parentPath = "/node-parent";
        curatorFramework.delete().guaranteed().deletingChildrenIfNeeded().forPath(parentPath);
    }

    /**
     * 获取子节点列表
     *
     * @throws Exception
     */
    @Test
    void testListChildren() throws Exception {
        String parentPath = "/";
        List<String> strings = curatorFramework.getChildren().forPath(parentPath);
        strings.forEach(System.out::println);
    }

    /**
     * 使用自定义线程池代替main-EventThread线程，提升效率
     *
     * @throws Exception
     */
    @Test
    void testThreadPool() throws Exception {
        String path = "/zookeeper";
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        curatorFramework.getData()
                .inBackground(((client, event) -> log.info("event: {}", event)), executorService)
                .forPath(path);
    }

}
