package com.iquanwai.confucius.biz.domain.log;

import com.google.common.collect.Maps;
import com.iquanwai.confucius.biz.po.ActionLog;
import com.iquanwai.confucius.biz.po.OperationLog;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Created by justin on 16/9/3.
 */
public interface OperationLogService {

    void log(OperationLog operationLog);

    void log(ActionLog actionLog);

    void trace(Integer profileId, String eventName);

    void trace(Supplier<Integer> supplier, String eventName);

    void trace(Integer profileId, String eventName, Supplier<Prop> supplier);

    void trace(Supplier<Integer> profileIdSupplier, String eventName, Supplier<Prop> supplier);

    static Prop props() {
        return new OperationLogServiceImpl.Prop();
    }

    void profileSet(Integer profileId, String key, Object value);

    void profileSet(Supplier<Integer> supplier, String key, Object value);

    void profileSet(Supplier<Integer> supplier, Supplier<Prop> propSupplier);

    void refreshProfiles(List<Integer> profileIds);

    class Prop {
        private Map<String, Object> map = Maps.newHashMap();

        public Prop add(String key, Object value) {
            this.map.put(key, value);
            return this;
        }

        public Map<String, Object> build() {
            return map;
        }
    }
}
