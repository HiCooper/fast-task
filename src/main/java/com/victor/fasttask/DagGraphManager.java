package com.victor.fasttask;

import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;

import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

/**
 * @author Victor.
 * @date 2021/6/19
 */
public final class DagGraphManager {
    /**
     * 批量任务执行上下文
     * 核心 Map<String, Object> data, key 为 task 唯一 id
     */
    private final DataContext dataContext;
    /**
     * 初始化 构建有向无环图
     */
    private final MutableGraph<AbstractTask> dagGraph = GraphBuilder.directed().allowsSelfLoops(false).build();
    /**
     * 任务执行状态改变
     * 如有任何一个任务完成 =》 set true
     * 开始执行后 set false
     */
    private volatile boolean stateChange = true;

    public DagGraphManager(DataContext dataContext) {
        this.dataContext = dataContext;
    }

    /**
     * 设置任务提交状态
     *
     * @param task
     */
    private static void setTaskSubmitStatus(AbstractTask task) {
        task.setStatus(AbstractTask.TaskStatus.SUBMIT);
        task.setSubmitExecuteTime(System.currentTimeMillis());
    }

    public MutableGraph<AbstractTask> getDagGraph() {
        return this.dagGraph;
    }

    public void executeTask(ExecutorService threadPoolExecutor) {
        while (!isDone()) {
            if (!this.stateChange) {
                continue;
            }
            this.stateChange = false;
            Set<AbstractTask> zeroInNodeList = getZeroInNodeList();
            zeroInNodeList.forEach(task -> {
                // task 提交
                if (task.getStatus().equals(AbstractTask.TaskStatus.INIT)) {
                    setTaskSubmitStatus(task);
                    threadPoolExecutor.execute(() -> doJob(task));
                }
                // task 执行中、已提交
                if (task.getStatus().equals(AbstractTask.TaskStatus.RUNNING) || task.getStatus().equals(AbstractTask.TaskStatus.SUBMIT)) {
                    long costTime = System.currentTimeMillis() - task.getSubmitExecuteTime();
                    String errMsg = String.format("task:%s execute timeout, config:%s, cost:%s", task.getId(), task.getTimeout(), costTime);
                    if (costTime > task.getTimeout()) {
                        dataContext.getExecuteErrorLog().put(task.getId(), errMsg);
                        task.setStatus(AbstractTask.TaskStatus.FAIL);
                        if (!task.isFailContinue()) {
                            dagGraph.nodes().forEach(node -> node.setStatus(AbstractTask.TaskStatus.CANCEL));
                            throw new RuntimeException(errMsg);
                        }
                    }
                    threadPoolExecutor.execute(() -> doJob(task));
                }
            });
        }
    }

    /**
     * 获取 0 入度 节点 list
     *
     * @return Set Nodes
     */
    private Set<AbstractTask> getZeroInNodeList() {
        synchronized (this) {
            Set<AbstractTask> nodes = dagGraph.nodes();
            return nodes.stream().filter(s -> s != null && dagGraph.inDegree(s) == 0).collect(Collectors.toSet());
        }
    }

    /**
     * 指定节点
     *
     * @param task task
     */
    private void doJob(AbstractTask task) {
        if (task.getStatus().equals(AbstractTask.TaskStatus.SUBMIT)) {
            try {
                long startTime = System.currentTimeMillis();
                // doJob before
                task.setStatus(AbstractTask.TaskStatus.RUNNING);
                task.doRun(dataContext);
                task.setStatus(AbstractTask.TaskStatus.DONE);
                long endTime = System.currentTimeMillis();
                dataContext.getTaskExecuteTimeMap().put(task.getId(), new DataContext.ExecuteTimeInfo(startTime, endTime));
                // doJob done
                // 获取下一个可直接执行节点
                AbstractTask nextExecutableNode = getNextExecutableNode(task);
                // 移除当前节点
                removeTask(task);
                // 执行下个节点
                if (nextExecutableNode != null) {
                    setTaskSubmitStatus(nextExecutableNode);
                    doJob(nextExecutableNode);
                }
            } catch (Exception e) {
                dataContext.getExecuteErrorLog().put(task.getId(), e.getMessage());
                task.setStatus(AbstractTask.TaskStatus.FAIL);
                // 判断失败是否继续，不继续，将其他任务状态设置为cancel, 抛出异常
                if (!task.isFailContinue()) {
                    dagGraph.nodes().forEach(node -> node.setStatus(AbstractTask.TaskStatus.CANCEL));
                    task.setStatus(AbstractTask.TaskStatus.FAIL);
                    throw e;
                }
            } finally {
                // doJob after
                if (task.isFailContinue()) {
                    this.stateChange = true;
                }
            }
        }
    }

    /**
     * 获取下个可直接执行节点
     * 当前节点只有一个出度，且下个节点只有一个入度，返回下个节点
     *
     * @param task 当前节点
     * @return
     */
    private AbstractTask getNextExecutableNode(AbstractTask task) {
        if (dagGraph.nodes().contains(task)) {
            Set<AbstractTask> successors = dagGraph.successors(task);
            if (successors.size() == 1) {
                AbstractTask nextNode = successors.stream().findFirst().orElse(null);
                if (dagGraph.inDegree(nextNode) == 1) {
                    return nextNode;
                }
            }
        }
        return null;
    }

    /**
     * 移除节点
     *
     * @param task 节点
     */
    private void removeTask(AbstractTask task) {
        synchronized (this) {
            dagGraph.removeNode(task);
        }
    }

    /**
     * 图是否执行完成
     *
     * @return true or false
     */
    private boolean isDone() {
        return getZeroInNodeList().isEmpty() || getZeroInNodeList().stream().allMatch(
                s -> s.getStatus().equals(AbstractTask.TaskStatus.FAIL) || s.getStatus().equals(AbstractTask.TaskStatus.CANCEL));
    }
}
