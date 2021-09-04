package com.berry.fasttask;

import com.google.common.graph.MutableGraph;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created with IntelliJ IDEA.
 *
 * @author HiCooper.
 * @version 1.0
 * @date 2021/9/3
 * fileName：GraphPrintUtil
 * Use：
 */
public class GraphPrintUtil {


    public static void print(MutableGraph<AbstractTask> dagGraph) {

        System.out.println("==================================== Graph ====================================");

        List<AbstractTask> rootNodes = dagGraph.nodes().stream().filter(s -> s != null && dagGraph.inDegree(s) == 0).collect(Collectors.toList());

        List<Line> lines = new ArrayList<>();

        process(dagGraph, rootNodes, lines);

        resetLines(lines);

        lines.forEach(Line::print);

        System.out.println("==================================== Graph ====================================");
    }

    private static void resetLines(List<Line> lines) {
        for (int i = lines.size() - 1; i >= 0; i--) {
            Line line = lines.get(i);
            List<Node> lineNodes = new ArrayList<>(line.nodesMap.values());
            // 检查在其他行是否存在
            if (i > 1) {
                checkExistThenRemove(lines, i - 1, lineNodes);
            }
        }
    }

    private static void checkExistThenRemove(List<Line> lines, int end, List<Node> lineNodes) {
        List<String> currentLineNodeIds = lineNodes.stream().map(Node::getId).collect(Collectors.toList());
        for (int i = 0; i <= end; i++) {
            Line line = lines.get(i);
            line.nodesMap.values().removeIf(s -> currentLineNodeIds.contains(s.getId()));
        }
    }

    private static void process(MutableGraph<AbstractTask> dagGraph, List<AbstractTask> nodes, List<Line> lines) {
        Line line = new Line();
        List<AbstractTask> nextLineNodes = new ArrayList<>();
        for (AbstractTask r : nodes) {
            String id = r.getId();
            String data = getNodeData(r);
            Node n = new Node(id, data);
            line.nodesMap.put(id, n);
            Set<AbstractTask> successors = dagGraph.successors(r);
            nextLineNodes.addAll(successors);
        }
        lines.add(line);
        if (nextLineNodes.size() > 0) {
            process(dagGraph, nextLineNodes, lines);
        }
    }

    private static String getNodeData(AbstractTask r) {
        String join = String.join(",", r.getDependencies());
        if (!join.equals("")) {
            join = "," + join;
        }
        return String.format("(%s:%s%s)", r.getId(), r.getStatus().name(), join);
    }

    private static class Line {

        private static final String GAP = " ";

        Map<String, Node> nodesMap = new HashMap<>();

        public void print() {
            List<Node> nodes = new ArrayList<>(nodesMap.values());
            printNodes(nodes);
        }

        public void addPrefix(String key, int prefixLength) {
            if (key == null || prefixLength == 0) {
                return;
            }
            Node node = nodesMap.get(key);
            if (node == null) {
                return;
            }
            StringBuilder appendPrefix = new StringBuilder();
            for (int i = 0; i < prefixLength; i++) {
                appendPrefix.append(GAP);
            }
            node.setPrefix(node.prefix + appendPrefix);
        }

        private void printNodes(List<Node> nodes) {
            if (nodes == null || nodes.size() == 0) {
                return;
            }
            StringBuilder lineStr = new StringBuilder();
            for (Node node : nodes) {
                lineStr.append(node.prefix).append(node.data).append(node.suffix);
            }
            System.out.println(lineStr);
        }
    }

    private static class Node {

        Node(String id, String data) {
            this.id = id;
            this.data = data;
        }

        private String id;
        private String prefix = " ";
        private String suffix = " ";

        private String data;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getPrefix() {
            return prefix;
        }

        public void setPrefix(String prefix) {
            this.prefix = prefix;
        }

        public String getSuffix() {
            return suffix;
        }

        public void setSuffix(String suffix) {
            this.suffix = suffix;
        }

        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }
    }

}
