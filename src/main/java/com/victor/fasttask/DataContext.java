package com.victor.fasttask;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Victor.
 * @date 2021/6/18
 * <p>
 * 任务执行结果上下文
 */
public final class DataContext {

    /**
     * key: taskId, val: data
     */
    private final Map<String, Object> data = new ConcurrentHashMap<>();

    /**
     * 任务错误执行日志
     * key => taskId, value => error msg
     */
    private final Map<String, String> executeErrorLog = new ConcurrentHashMap<>();

    public Map<String, Object> getData() {
        return data;
    }

    public Map<String, String> getExecuteErrorLog() {
        return executeErrorLog;
    }

    public Object getDataByTaskId(String taskId) {
        return data.get(taskId);
    }

    public String getErrorLogByKey(String taskKey) {
        return executeErrorLog.get(taskKey);
    }
}
