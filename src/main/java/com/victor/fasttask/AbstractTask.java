package com.victor.fasttask;

import java.util.Objects;
import java.util.Set;

/**
 * @author Victor.
 * @date 2021/6/18
 */
public abstract class AbstractTask {

    /**
     * 每一个任务都有唯一的ID
     */
    private final String taskId;
    private boolean visited;
    /**
     * 失败继续
     */
    private boolean failContinue;
    /**
     * 任务执行后的返回数据
     */
    private Object data;
    /**
     * 任务提交执行时间
     * 提交，并不会马上执行，等待线程池调度
     */
    private long submitExecuteTime;

    /**
     * task执行 超时时间
     */
    private long timeout;

    /**
     * 任务执行状态，默认INIT
     */
    private volatile TaskStatus status = TaskStatus.INIT;

    AbstractTask(String taskId, long timeout, boolean failContinue) {
        this.taskId = taskId;
        this.timeout = timeout;
        this.failContinue = failContinue;
    }

    public boolean isFailContinue() {
        return failContinue;
    }

    public void setFailContinue(boolean failContinue) {
        this.failContinue = failContinue;
    }

    public boolean isVisited() {
        return visited;
    }

    public void setVisited(boolean visited) {
        this.visited = visited;
    }

    public TaskStatus getStatus() {
        synchronized (this) {
            return status;
        }
    }

    public void setStatus(TaskStatus status) {
        synchronized (this) {
            this.status = status;
        }
    }

    public Object getData() {
        return data;
    }

    /**
     * 节点唯一ID
     *
     * @return str id
     */
    public String getId() {
        return this.taskId;
    }

    public long getSubmitExecuteTime() {
        return submitExecuteTime;
    }

    public void setSubmitExecuteTime(long submitExecuteTime) {
        this.submitExecuteTime = submitExecuteTime;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    /**
     * node 可执行动作
     */
    void doRun(DataContext dataContext) {
        doAction(dataContext);
        this.data = dataContext.getData().get(getId());
    }

    /**
     * 执行 task 动作
     *
     * @param dataContext 所有 task 上下文
     */
    abstract void doAction(DataContext dataContext);

    /**
     * 任务依赖的其他任务id
     *
     * @return taskId list
     */
    abstract Set<AbstractTask> getDependencies();

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AbstractTask that = (AbstractTask) o;
        return taskId.equals(that.taskId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskId);
    }

    public enum TaskStatus {
        /**
         * 初始化，已提交，运行中，失败（超时，异常），取消（其他关键节点失败，后续任务取消），完成
         */
        INIT,
        SUBMIT,
        RUNNING,
        FAIL,
        CANCEL,
        DONE
    }
}
