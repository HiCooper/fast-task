package com.victor.fasttask;

import com.google.common.collect.Sets;
import com.google.common.graph.MutableGraph;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Victor.
 * @date 2021/6/18
 */
public final class FastTaskExecutor {

    public static void execute(List<? extends AbstractTask> taskList, DataContext dataContext,
                               ExecutorService threadPoolExecutor) {
        execute(taskList, dataContext, threadPoolExecutor, false);
    }

    /**
     * 开始执行任务
     *
     * @param taskList           task
     * @param threadPoolExecutor threadPoolExecutor
     */
    public static void execute(List<? extends AbstractTask> taskList, DataContext dataContext,
                               ExecutorService threadPoolExecutor, boolean printGraph) {
        if (taskList == null || taskList.isEmpty()) {
            return;
        }
        Set<AbstractTask> allTaskSet = traverseDependencies(Sets.newHashSet(taskList));
        Map<AbstractTask, Set<AbstractTask>> nodeMap = allTaskSet.stream()
                .filter(s -> null != s.getDependencies() && !s.getDependencies().isEmpty())
                .collect(Collectors.toMap(Function.identity(), AbstractTask::getDependencies));
        DagGraphManager dagGraphManager = buildDagGraphManager(allTaskSet, nodeMap, dataContext);
        dagGraphManager.executeTask(threadPoolExecutor);
        if (printGraph) {
            GraphPrintUtil.printPlantUml(allTaskSet, nodeMap, dataContext.getTaskExecuteTimeMap());
        }
    }

    /**
     * 遍历task依赖，返回所有节点
     *
     * @param taskSets 目标执行节点集合
     * @return 目标节点及其依赖节点集合（all）
     */
    private static Set<AbstractTask> traverseDependencies(Set<AbstractTask> taskSets) {
        Set<AbstractTask> resultSets = Sets.newHashSet();
        if (taskSets != null && !taskSets.isEmpty()) {
            resultSets.addAll(taskSets);
            taskSets.forEach(task -> {
                Set<AbstractTask> dependencies = task.getDependencies();
                if (dependencies != null && !dependencies.isEmpty()) {
                    resultSets.addAll(traverseDependencies(dependencies));
                }
            });
        }
        return resultSets;
    }

    /**
     * 构建dag图，获取任务执行管理器
     *
     * @param tasks       任务列表
     * @param nodeMap
     * @param dataContext 任务执行上下文
     * @return dagGraphManager
     */
    private static DagGraphManager buildDagGraphManager(Set<AbstractTask> tasks, Map<AbstractTask, Set<AbstractTask>> nodeMap, DataContext dataContext) {
        DagGraphManager dagGraphManager = new DagGraphManager(dataContext);
        MutableGraph<AbstractTask> dagGraph = dagGraphManager.getDagGraph();
        tasks.forEach(dagGraph::addNode);
        nodeMap.forEach((nodeU, nodeVs) -> nodeVs.forEach(nodeV -> dagGraph.putEdge(nodeV, nodeU)));
        return dagGraphManager;
    }
}
