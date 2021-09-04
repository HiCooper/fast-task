package com.berry.fasttask;

import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

/**
 * Created with IntelliJ IDEA.
 *
 * @author HiCooper.
 * @version 1.0
 * @date 2021/6/19
 * fileName：DagGraph
 * Use：
 */
public final class DagGraphManager {
    private static final Logger logger = LoggerFactory.getLogger(DagGraphManager.class);

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
                if (task.getStatus().equals(AbstractTask.TaskStatus.READY)) {
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
        if (task.getStatus().equals(AbstractTask.TaskStatus.READY)) {
            // doJob before
            try {
                if (logger.isDebugEnabled()) {
                    logger.info("task start: {}", task.getId());
                }
                task.setStatus(AbstractTask.TaskStatus.RUNNING);
                task.doRun(dataContext);
                task.setStatus(AbstractTask.TaskStatus.DONE);
                removeTask(task);
                // doJob done
                if (logger.isDebugEnabled()) {
                    logger.info("task done: {}", task.getId());
                }
            } catch (Exception e) {
                logger.error("do task: {} fail, ", task.getId(), e);
                task.setStatus(AbstractTask.TaskStatus.FAIL);
                dataContext.getExecuteErrorLog().put(task.getId(), e.getMessage());
            } finally {
                // doJob after
                this.stateChange = true;
            }
        }
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
        return getZeroInNodeList().isEmpty() || getZeroInNodeList().stream().allMatch(s -> s.getStatus().equals(AbstractTask.TaskStatus.FAIL));
    }
}
