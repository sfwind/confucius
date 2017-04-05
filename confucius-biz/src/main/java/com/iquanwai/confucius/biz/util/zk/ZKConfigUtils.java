package com.iquanwai.confucius.biz.util.zk;

import com.google.common.collect.Maps;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Created by justin on 17/3/25.
 */
public class ZKConfigUtils {
    private RobustZooKeeper zooKeeper;

    private ZooKeeper zk;

    private Logger logger = LoggerFactory.getLogger(getClass());

    private String zkAddress = "106.14.26.18:2181";

    /* 每个项目的path不同 */
    private static final String CONFIG_PATH = "/quanwai/config/";

    private static final String ZK_CONFIG_PATH = "/data/static/config/zk";
    private static final String ZK_ADDRESS_KEY = "zk.address";

    public ZKConfigUtils(){
        init();
    }

    public void init(){
        config();
        try {
            zooKeeper = new RobustZooKeeper(zkAddress);
            zk = zooKeeper.getClient();
        } catch (IOException e) {
            logger.error("zk "+zkAddress+" is not connectible", e);
        }
    }

    private void config() {
        File file = new File(ZK_CONFIG_PATH);
        if(file.exists()){
            Properties p = new Properties();
            try {
                p.load(new FileReader(file));
                zkAddress = p.getProperty(ZK_ADDRESS_KEY);
            } catch (IOException e) {
                // ignore
            }
        }
    }

    public void destroy(){
        if(zooKeeper!=null){
            try {
                zooKeeper.shutdown();
            } catch (InterruptedException e) {
                logger.error("zk " + zkAddress + " is shutdown", e);
            }
        }
    }

    public String getValue(String projectId, String key){
        try {
            return new String(zk.getData(CONFIG_PATH.concat(projectId+"/").concat(key), false, null), "utf-8");
        } catch (Exception e) {
            logger.error("zk " + zkAddress + " get value", e);
        }

        return null;
    }

    public boolean getBooleanValue(String projectId, String key){
        String value = getValue(projectId, key);

        return Boolean.valueOf(value);
    }

    public int getIntValue(String projectId, String key){
        String value = getValue(projectId, key);
        try{
            Assert.notNull(value);
            return Integer.valueOf(value);
        }catch (NumberFormatException e){
            logger.error("zk " + zkAddress + " get int {}", value);
        }

        return Integer.MIN_VALUE;
    }


    public void updateValue(String projectId, String key, String value){
        try {
            zk.setData(CONFIG_PATH.concat(projectId+"/").concat(key), value.getBytes("utf-8"), -1);
        } catch (Exception e) {
            logger.error("zk " + zkAddress + " set key {} value {} failed", key, value);
        }
    }

    public void deleteValue(String projectId, String key){
        try {
            zk.delete(CONFIG_PATH.concat(projectId+"/").concat(key),  -1);
        } catch (Exception e) {
            logger.error("zk " + zkAddress + " delete key {} failed", key);
        }
    }

    public void createValue(String projectId, String key, String value){
        try {
            zk.create(CONFIG_PATH.concat(projectId+"/").concat(key), value.getBytes("utf-8"),
                    ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        } catch (Exception e) {
            logger.error("zk " + zkAddress + " create key {} value {} failed", key, value);
        }
    }

    public Map<String, String> getAllValue(String projectId){
        Map<String, String> maps = Maps.newHashMap();
        try {
            List<String> paths  = zk.getChildren(CONFIG_PATH.concat(projectId), null);

            paths.stream().forEach(path-> maps.put(path, getValue(projectId, path)));
        } catch (Exception e) {
            logger.error("zk " + zkAddress + " get path {} children failed", projectId);
        }

        return maps;
    }
}
