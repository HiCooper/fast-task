# fast-task

DAG任务编排执行

2022-05-29 更新日志
当一个节点执行完，找到下游第一个只有一个入度的节点，用当前线程继续执行

### 创建一个任务

- 继承：AbstractTask
- 初始化任务实例，设置任务唯一标识taskId（必须），任务名称（非必须），任务依赖其他任务taskId（非必须）

example

````
public static class TestTask extends AbstractTask {

        private final List<String> dependencies;

        private final String name;

        public TestTask(String name, String taskId, List<String> dependencies) {
            super(taskId);
            this.name = name;
            this.dependencies = dependencies;
        }

        public String getName() {
            return name;
        }

        @Override
        public void doAction(DataContext dataContext) {
            long start = System.currentTimeMillis();
            try {
                // do something amazing
                dataContext.getData().put(getId(), getName());
                Thread.sleep(new Random().nextInt(3000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            long end = System.currentTimeMillis();
            logger.info("take time: {} ms", (end - start));
        }

        @Override
        public List<String> getDependencies() {
            return this.dependencies;
        }
    }
````

### 开始执行任务

sample graph

![示例任务图](./doc/sample.png)

````
public void test() {
    List<TestTask> testTaskList = new ArrayList<>();
    TestTask task1 = new TestTask("任务1", "1", Lists.newArrayList());
    TestTask task2 = new TestTask("任务2", "2", Lists.newArrayList("1"));
    TestTask task3 = new TestTask("任务3", "3", Lists.newArrayList("1"));
    TestTask task4 = new TestTask("任务4", "4", Lists.newArrayList("2"));
    TestTask task5 = new TestTask("任务5", "5", Lists.newArrayList("3", "9"));
    TestTask task6 = new TestTask("任务6", "6", Lists.newArrayList());
    TestTask task7 = new TestTask("任务7", "7", Lists.newArrayList("5"));
    TestTask task8 = new TestTask("任务8", "8", Lists.newArrayList("6", "7"));
    TestTask task9 = new TestTask("任务9", "9", Lists.newArrayList("6"));
    TestTask task10 = new TestTask("任务10", "10", Lists.newArrayList("6"));
    TestTask task11 = new TestTask("任务11", "11", Lists.newArrayList("9"));
    TestTask task12 = new TestTask("任务12", "12", Lists.newArrayList("10"));
    TestTask task13 = new TestTask("任务13", "13", Lists.newArrayList("4", "10"));
    TestTask task14 = new TestTask("任务14", "14", Lists.newArrayList("11", "12"));
    testTaskList.add(task1);
    testTaskList.add(task2);
    testTaskList.add(task3);
    testTaskList.add(task4);
    testTaskList.add(task5);
    testTaskList.add(task6);
    testTaskList.add(task7);
    testTaskList.add(task8);
    testTaskList.add(task9);
    testTaskList.add(task10);
    testTaskList.add(task11);
    testTaskList.add(task12);
    testTaskList.add(task13);
    testTaskList.add(task14);
    DataContext dataContext = new DataContext();
    long start = System.currentTimeMillis();
    FastTaskExecutor.execute(testTaskList, dataContext, threadPoolExecutor);
    long end = System.currentTimeMillis();
    logger.info("all take time: {} ms",  (end - start));
    logger.info("dataContext: {}", new Gson().toJson(dataContext));
    logger.info("task8: {}", new Gson().toJson(task8.getData()));
}
````

result print

````log
15:43:42.544 [Test worker] INFO com.victor.fasttask.GraphPrintUtil - ==================================== Fast Task Graph(14) ====================================
15:43:42.552 [Test worker] INFO com.victor.fasttask.GraphPrintUtil -  (1:READY)  (6:READY) 
15:43:42.552 [Test worker] INFO com.victor.fasttask.GraphPrintUtil -  (2:READY,1)  (3:READY,1)  (9:READY,6)  (10:READY,6) 
15:43:42.552 [Test worker] INFO com.victor.fasttask.GraphPrintUtil -  (11:READY,9)  (12:READY,10)  (4:READY,2)  (5:READY,3,9) 
15:43:42.552 [Test worker] INFO com.victor.fasttask.GraphPrintUtil -  (13:READY,4,10)  (14:READY,11,12)  (7:READY,5) 
15:43:42.552 [Test worker] INFO com.victor.fasttask.GraphPrintUtil -  (8:READY,6,7) 
15:43:42.552 [Test worker] INFO com.victor.fasttask.GraphPrintUtil - ==================================== Fast Task Graph ====================================
15:43:42.553 [thread-pool-0] DEBUG com.victor.fasttask.DagGraphManager - task start: 1
15:43:42.553 [thread-pool-1] DEBUG com.victor.fasttask.DagGraphManager - task start: 6
15:43:43.465 [thread-pool-0] INFO com.victor.fasttask.TaskTest - 1 take time: 912 ms
15:43:43.465 [thread-pool-0] DEBUG com.victor.fasttask.DagGraphManager - task done: 1
15:43:43.465 [thread-pool-0] DEBUG com.victor.fasttask.DagGraphManager - task start: 2
15:43:44.107 [thread-pool-1] INFO com.victor.fasttask.TaskTest - 6 take time: 1554 ms
15:43:44.107 [thread-pool-1] DEBUG com.victor.fasttask.DagGraphManager - task done: 6
15:43:44.107 [thread-pool-1] DEBUG com.victor.fasttask.DagGraphManager - task start: 9
15:43:45.281 [thread-pool-1] INFO com.victor.fasttask.TaskTest - 9 take time: 1174 ms
15:43:45.281 [thread-pool-1] DEBUG com.victor.fasttask.DagGraphManager - task done: 9
15:43:45.281 [thread-pool-1] DEBUG com.victor.fasttask.DagGraphManager - task start: 11
15:43:45.653 [thread-pool-1] INFO com.victor.fasttask.TaskTest - 11 take time: 372 ms
15:43:45.653 [thread-pool-1] DEBUG com.victor.fasttask.DagGraphManager - task done: 11
15:43:45.653 [thread-pool-2] DEBUG com.victor.fasttask.DagGraphManager - task start: 10
15:43:46.019 [thread-pool-0] INFO com.victor.fasttask.TaskTest - 2 take time: 2554 ms
15:43:46.019 [thread-pool-0] DEBUG com.victor.fasttask.DagGraphManager - task done: 2
15:43:46.019 [thread-pool-0] DEBUG com.victor.fasttask.DagGraphManager - task start: 4
15:43:46.277 [thread-pool-0] INFO com.victor.fasttask.TaskTest - 4 take time: 258 ms
15:43:46.277 [thread-pool-0] DEBUG com.victor.fasttask.DagGraphManager - task done: 4
15:43:46.279 [thread-pool-3] DEBUG com.victor.fasttask.DagGraphManager - task start: 3
15:43:46.843 [thread-pool-2] INFO com.victor.fasttask.TaskTest - 10 take time: 1190 ms
15:43:46.843 [thread-pool-2] DEBUG com.victor.fasttask.DagGraphManager - task done: 10
15:43:46.843 [thread-pool-2] DEBUG com.victor.fasttask.DagGraphManager - task start: 12
15:43:47.462 [thread-pool-2] INFO com.victor.fasttask.TaskTest - 12 take time: 619 ms
15:43:47.462 [thread-pool-2] DEBUG com.victor.fasttask.DagGraphManager - task done: 12
15:43:47.462 [thread-pool-2] DEBUG com.victor.fasttask.DagGraphManager - task start: 14
15:43:48.460 [thread-pool-3] INFO com.victor.fasttask.TaskTest - 3 take time: 2181 ms
15:43:48.460 [thread-pool-3] DEBUG com.victor.fasttask.DagGraphManager - task done: 3
15:43:48.460 [thread-pool-3] DEBUG com.victor.fasttask.DagGraphManager - task start: 5
15:43:49.743 [thread-pool-2] INFO com.victor.fasttask.TaskTest - 14 take time: 2281 ms
15:43:49.743 [thread-pool-2] DEBUG com.victor.fasttask.DagGraphManager - task done: 14
15:43:49.743 [thread-pool-4] DEBUG com.victor.fasttask.DagGraphManager - task start: 13
15:43:50.935 [thread-pool-4] INFO com.victor.fasttask.TaskTest - 13 take time: 1192 ms
15:43:50.935 [thread-pool-4] DEBUG com.victor.fasttask.DagGraphManager - task done: 13
15:43:51.372 [thread-pool-3] INFO com.victor.fasttask.TaskTest - 5 take time: 2912 ms
15:43:51.372 [thread-pool-3] DEBUG com.victor.fasttask.DagGraphManager - task done: 5
15:43:51.372 [thread-pool-3] DEBUG com.victor.fasttask.DagGraphManager - task start: 7
15:43:52.580 [thread-pool-3] INFO com.victor.fasttask.TaskTest - 7 take time: 1208 ms
15:43:52.580 [thread-pool-3] DEBUG com.victor.fasttask.DagGraphManager - task done: 7
15:43:52.580 [thread-pool-3] DEBUG com.victor.fasttask.DagGraphManager - task start: 8
15:43:54.978 [thread-pool-3] INFO com.victor.fasttask.TaskTest - 8 take time: 2398 ms
15:43:54.978 [thread-pool-3] DEBUG com.victor.fasttask.DagGraphManager - task done: 8
15:43:54.978 [Test worker] INFO com.victor.fasttask.GraphPrintUtil - ==================================== Fast Task Graph(0) ====================================
15:43:54.978 [Test worker] INFO com.victor.fasttask.GraphPrintUtil - All Done.
15:43:54.978 [Test worker] INFO com.victor.fasttask.GraphPrintUtil - ==================================== Fast Task Graph ====================================
15:43:54.978 [Test worker] INFO com.victor.fasttask.TaskTest - total take time: 12448 ms
15:43:55.004 [Test worker] INFO com.victor.fasttask.TaskTest - dataContext: {"data":{"11":"任务11","12":"任务12","13":"任务13","14":"任务14","1":"任务1","2":"任务2","3":"任务3","4":"任务4","5":"任务5","6":"任务6","7":"任务7","8":"任务8","9":"任务9","10":"任务10"},"executeErrorLog":{}}
BUILD SUCCESSFUL in 31s
3 actionable tasks: 3 executed
15:43:55: Execution finished ':test --tests "com.victor.fasttask.TaskTest.test"'.


````

More detail @see com/victor/fasttask/Test.java
