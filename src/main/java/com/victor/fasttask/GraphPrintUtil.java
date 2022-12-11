package com.victor.fasttask;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Victor.
 * @date 2021/9/3
 */

public class GraphPrintUtil {
    private static final Logger logger = LoggerFactory.getLogger(GraphPrintUtil.class);

    private GraphPrintUtil() {
    }

    public static void printPlantUml(Set<AbstractTask> allTaskSet, Map<AbstractTask, Set<AbstractTask>> nodeMap, Map<String, DataContext.ExecuteTimeInfo> taskExecuteTimeMap) {
        if (allTaskSet == null || allTaskSet.isEmpty() || nodeMap == null) {
            return;
        }
        // 有入度节点，边关系，node1 -> node2 (2依赖1)
        List<Map<AbstractTask, AbstractTask>> edgeList = Lists.newArrayList();
        try {
            nodeMap.forEach((nodeU, nodeVs) -> nodeVs.forEach(nodeV -> {
                Map<AbstractTask, AbstractTask> line = Maps.newHashMap();
                line.put(nodeV, nodeU);
                edgeList.add(line);
            }));
            Set<AbstractTask> rootNodes = allTaskSet.stream()
                    .filter(node -> !nodeMap.containsKey(node))
                    .collect(Collectors.toSet());
            printUml(rootNodes, edgeList, taskExecuteTimeMap);
        } catch (Exception e) {
            logger.error("printPlantUml error, msg:{}", e.getMessage(), e);
        }
    }

    private static void printUml(Set<AbstractTask> rootNodes, List<Map<AbstractTask, AbstractTask>> edgeList, Map<String, DataContext.ExecuteTimeInfo> taskExecuteTimeMap) {
        StringBuilder plantUmlBuilder = new StringBuilder();
        plantUmlBuilder.append("======== Graph Start(PlantUML) ========\n");
        plantUmlBuilder.append("@startuml\n");

        rootNodes.forEach(rootNode -> plantUmlBuilder
                .append("(*) --> ")
                .append(getNodeDesc(rootNode, taskExecuteTimeMap.get(rootNode.getId())))
                .append("\n"));

        edgeList.forEach(edge -> edge.forEach((key, val) -> plantUmlBuilder
                .append(getNodeDesc(key, taskExecuteTimeMap.get(key.getId())))
                .append(" --> ")
                .append(getNodeDesc(val, taskExecuteTimeMap.get(val.getId())))
                .append("\n")));

        plantUmlBuilder.append("@enduml\n");
        plantUmlBuilder.append("======== Graph End(PlantUML) ========\n");
        if (logger.isInfoEnabled()) {
            logger.info(plantUmlBuilder.toString());
        }
    }

    private static String getNodeDesc(AbstractTask rootNode, DataContext.ExecuteTimeInfo executeTimeInfo) {
        if (executeTimeInfo != null) {
            return rootNode.getId() + "_" + executeTimeInfo.getCostTime();
        }
        return rootNode.getId();
    }
}
