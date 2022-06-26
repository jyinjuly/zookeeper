package com.stockeeper.zookeeper.watcher;

import com.alibaba.fastjson.JSONObject;
import com.stockeeper.zookeeper.dto.ConfigDto;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

@Slf4j
public class ConfigWatcher implements Watcher {

    public static final String CONFIG_NODE_PATH = "/config";
    private ZooKeeper zooKeeper;

    public ConfigWatcher(ZooKeeper zooKeeper) {
        this.zooKeeper = zooKeeper;
    }

    @SneakyThrows
    @Override
    public void process(WatchedEvent event) {
        if (event.getType() == Event.EventType.NodeDataChanged && CONFIG_NODE_PATH.equals(event.getPath())) {
            log.info("PATH【{}】发生了数据变化", event.getPath());
            byte[] data = zooKeeper.getData(CONFIG_NODE_PATH, this, null);

            ConfigDto configDto = JSONObject.parseObject(data, ConfigDto.class);
            log.info("最新数据：{}", configDto);
        }
    }

}
