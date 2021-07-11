package com.berry.fasttask;

import java.util.HashMap;
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

    private final Map<String, Object> data = new ConcurrentHashMap<>();

    /**
     * 任务错误执行日志
     * key => taskId, value => error msg
     */
    private final Map<String, String> executeErrorLog = new HashMap<>();

    public Map<String, Object> getData() {
        return data;
    }

    public Map<String, String> getExecuteErrorLog() {
        return executeErrorLog;
    }
}
