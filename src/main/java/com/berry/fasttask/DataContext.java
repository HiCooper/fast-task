package com.berry.fasttask;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created with IntelliJ IDEA.
 *
 * @author HiCooper.
 * @version 1.0
 * @date 2021/6/18
 * fileName：DataContext
 * Use：任务执行结果上下文
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
