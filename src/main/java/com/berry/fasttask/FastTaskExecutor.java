package com.berry.fasttask;

import com.google.common.graph.MutableGraph;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

/**
 * Created with IntelliJ IDEA.
 *
 * @author HiCooper.
 * @version 1.0
 * @date 2021/6/18
 * fileName：FastTaskExecutor
 * Use：
 */
public final class FastTaskExecutor {

    /**
     * 开始执行任务
     *
     * @param testTaskList       task
     * @param threadPoolExecutor threadPoolExecutor
     */
    public static void execute(List<? extends AbstractTask> testTaskList, DataContext dataContext, ExecutorService threadPoolExecutor) {
        DagGraphManager dagGraphManager = buildDagGraphManager(testTaskList, dataContext);
        dagGraphManager.executeTask(threadPoolExecutor);
    }

    /**
     * 构建dag图，获取任务执行管理器
     *
     * @param tasks       任务列表
     * @param dataContext 任务执行上下文
     * @return dagGraphManager
     */
    private static DagGraphManager buildDagGraphManager(List<? extends AbstractTask> tasks, DataContext dataContext) {
        DagGraphManager dagGraphManager = new DagGraphManager(dataContext);
        MutableGraph<AbstractTask> dagGraph = dagGraphManager.getDagGraph();
        tasks.forEach(dagGraph::addNode);
        Map<String, List<String>> taskIdWithDependenciesMap = tasks.stream()
                .filter(s -> null != s.getDependencies() && !s.getDependencies().isEmpty())
                .collect(Collectors.toMap(AbstractTask::getId, AbstractTask::getDependencies));
        Map<AbstractTask, List<AbstractTask>> nodeMap = new HashMap<>(16);
        taskIdWithDependenciesMap.forEach((k, v) -> tasks.stream().filter(s -> s.getId().equals(k)).findFirst()
                .ifPresent(task -> nodeMap.putIfAbsent(task, tasks.stream().filter(t -> v.contains(t.getId())).collect(Collectors.toList()))));
        nodeMap.forEach((nodeU, nodeVs) -> nodeVs.forEach(nodeV -> dagGraph.putEdge(nodeV, nodeU)));
        GraphPrintUtil.print(dagGraph);
        return dagGraphManager;
    }
}
