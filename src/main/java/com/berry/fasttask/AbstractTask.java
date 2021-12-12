package com.berry.fasttask;

import java.util.List;
import java.util.Objects;

/**
 * Created with IntelliJ IDEA.
 *
 * @author HiCooper.
 * @version 1.0
 * @date 2021/6/18
 * fileName：AbstractTask
 * Use：
 */
public abstract class AbstractTask {

    /**
     * 每一个任务都有唯一的ID
     */
    private final String taskId;
    private boolean visited;
    /**
     * 任务执行后的返回数据
     */
    private Object data;
    /**
     * 任务执行状态，默认就绪
     */
    private volatile TaskStatus status = TaskStatus.READY;

    AbstractTask(String taskId) {
        this.taskId = taskId;
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
    abstract List<String> getDependencies();

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AbstractTask that = (AbstractTask)o;
        return taskId.equals(that.taskId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskId);
    }

    public enum TaskStatus {
        /**
         * 就绪，运行，失败，完成
         */
        READY,
        RUNNING,
        FAIL,
        DONE;
    }
}
