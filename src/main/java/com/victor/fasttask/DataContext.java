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

    /**
     * 任务执行耗时
     */
    private final Map<String, ExecuteTimeInfo> taskExecuteTimeMap = new ConcurrentHashMap<>();

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

    public Map<String, ExecuteTimeInfo> getTaskExecuteTimeMap() {
        return taskExecuteTimeMap;
    }

    public static class ExecuteTimeInfo {
        private long startTime;
        private long endTime;
        private long costTime;

        public ExecuteTimeInfo(long startTime, long endTime) {
            this.startTime = startTime;
            this.endTime = endTime;
            this.costTime = endTime - startTime;
        }

        public long getStartTime() {
            return startTime;
        }

        public void setStartTime(long startTime) {
            this.startTime = startTime;
        }

        public long getEndTime() {
            return endTime;
        }

        public void setEndTime(long endTime) {
            this.endTime = endTime;
        }

        public long getCostTime() {
            return costTime;
        }

        public void setCostTime(long costTime) {
            this.costTime = costTime;
        }
    }
}
