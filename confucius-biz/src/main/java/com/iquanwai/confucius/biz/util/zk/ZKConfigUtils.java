package com.iquanwai.confucius.biz.util.zk;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by justin on 17/3/25.
 */
@Service
public class ZKConfigUtils {
    private RobustZooKeeper zooKeeper;

    private ZooKeeper zk;

    private Logger logger = LoggerFactory.getLogger(getClass());

    private String zkAddress = "106.14.26.18:2181";

    private static Cache<String, String> CONFIG_CACHE;

    /* 每个项目的path不同 */
    private static final String CONFIG_PATH = "/quanwai/config/";
    /* 体喜欢path */
    private static final String COURSE_CONFIG_PATH = "/quanwai/config/course/";
    /* 架构类型的path */
    private static final String ARCH_PATH = "/quanwai/config/arch/";
    /* zk本地配置文件路径 */
    private static final String ZK_CONFIG_PATH = "/data/config/zk";
    /* zk服务器地址配置key */
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
        CONFIG_CACHE = CacheBuilder.newBuilder()
                .expireAfterWrite(1L, TimeUnit.MINUTES)
                .build();
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




    public String getArchValue(String key){
        return getValue(key,ARCH_PATH);
    }

    public String getValue(String key){
        String fullPath = COURSE_CONFIG_PATH.concat(key);
        try {
            if (zk.exists(fullPath, false) != null) {
                return getValue(key, COURSE_CONFIG_PATH);
            } else {
                return getArchValue(key);
            }
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            return null;
        }
    }

    public String getValue(String key,String prePath){
        try {
            String value = CONFIG_CACHE.getIfPresent(key);
            if(value!=null){
                return value;
            }
            logger.info("get {} from zk", key);
            String fullPath = prePath.concat(key);
            if (zk.exists(fullPath, false) == null) {
                logger.error("the full path node is none : {}", fullPath);
                return null;
            }
            String json = new String(zk.getData(fullPath, false, null), "utf-8");
            ConfigNode configNode = new Gson().fromJson(json, ConfigNode.class);
            value = configNode.getValue();
            CONFIG_CACHE.put(key, value);
            return value;
        } catch (Exception e) {
            logger.error("zk " + zkAddress + " get value", e);
        }

        return null;
    }

    public List<Integer> getIntList(String key){
        String value = getValue(key);
        try{
            Assert.notNull(value);

        } catch (Exception e){
            logger.error("zk" + zkAddress + " get int {}", value);
        }
        return null;
    }

    public Boolean getBooleanValue(String key){
        String value = getValue(key);
        try{
            Assert.notNull  (value);
            return Boolean.valueOf(value);
        }catch (Exception e){
            logger.error("zk" + zkAddress + " get int {}", value);
        }
        return null;
    }

    public Integer getIntValue(String key){
        String value = getValue(key);
        try{
            Assert.notNull  (value);
            return Integer.valueOf(value);
        }catch (Exception e){
            logger.error("zk" + zkAddress + " get int {}", value);
        }

        return null;
    }

    public Double getDoubleValue(String key){
        String value = getValue(key);
        try{
            Assert.notNull  (value);
            return Double.valueOf(value);
        }catch (Exception e){
            logger.error("zk" + zkAddress + " get int {}", value);
        }

        return null;
    }

    public void updateValue(String projectId, String key, String value){
        try {
            String json = new String(zk.getData(CONFIG_PATH.concat(projectId+"/").concat(key), false, null), "utf-8");
            ConfigNode configNode = new Gson().fromJson(json, ConfigNode.class);
            configNode.setValue(value);
            configNode.setM_time(System.currentTimeMillis());
            json = new Gson().toJson(configNode);
            zk.setData(CONFIG_PATH.concat(projectId+"/").concat(key), json.getBytes("utf-8"), -1);
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
            ConfigNode configNode = new ConfigNode();
            configNode.setValue(value);
            configNode.setC_time(System.currentTimeMillis());
            configNode.setM_time(System.currentTimeMillis());
            String json = new Gson().toJson(configNode);
            zk.create(CONFIG_PATH.concat(projectId+"/").concat(key), json.getBytes("utf-8"),
                    ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        } catch (Exception e) {
            logger.error("zk " + zkAddress + " create key {} value {} failed", key, value);
        }
    }

    public List<ConfigNode> getAllValue(String projectId){
        Map<ConfigNode, String> temp = Maps.newHashMap();
        List<ConfigNode> list = Lists.newArrayList();
        try {
            List<String> paths  = zk.getChildren(CONFIG_PATH.concat(projectId), null);

            paths.stream().forEach(path-> {
                    String json = "";
                    try {
                        json = new String(zk.getData(CONFIG_PATH.concat(projectId + "/").concat(path), false, null), "utf-8");
                    } catch (Exception e) {
                        logger.error("zk " + zkAddress + " get value", e);
                    }
                    ConfigNode configNode = new Gson().fromJson(json, ConfigNode.class);
                    temp.put(configNode, path);
                }
            );
            //按创建时间排序
            list = temp.keySet().stream().collect(Collectors.toList());
            list.sort((o1, o2) -> (int) ((o1.getC_time() - o2.getC_time()) / 1000L));
            //组装结果map
            list.stream().forEach(configNode -> configNode.setKey(temp.get(configNode)));
        } catch (Exception e) {
            logger.error("zk " + zkAddress + " get path {} children failed", projectId);
        }

        return list;
    }
}
