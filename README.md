# fast-task
DAG任务编排执行


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
````java
    public void test() {
        List<TestTask> testTaskList = new ArrayList<>();
        TestTask task1 = new TestTask("任务1", "1", Lists.newArrayList(""));
        TestTask task2 = new TestTask("任务2", "2", Lists.newArrayList("1"));
        TestTask task3 = new TestTask("任务3", "3", Lists.newArrayList("1"));
        TestTask task4 = new TestTask("任务4", "4", Lists.newArrayList("2"));
        TestTask task5 = new TestTask("任务5", "5", Lists.newArrayList("3"));
        TestTask task6 = new TestTask("任务6", "6", Lists.newArrayList(""));
        TestTask task7 = new TestTask("任务7", "7", Lists.newArrayList("5"));
        TestTask task8 = new TestTask("任务8", "8", Lists.newArrayList("7", "6"));
        testTaskList.add(task1);
        testTaskList.add(task2);
        testTaskList.add(task3);
        testTaskList.add(task4);
        testTaskList.add(task5);
        testTaskList.add(task6);
        testTaskList.add(task7);
        testTaskList.add(task8);
        DataContext dataContext = new DataContext();
        long start = System.currentTimeMillis();
        FastTaskExecutor.execute(testTaskList, dataContext, threadPoolExecutor);
        long end = System.currentTimeMillis();
        logger.info("all take time: {} ms",  (end - start));
        logger.info("dataContext: {}", new Gson().toJson(dataContext));
        logger.info("task8: {}", new Gson().toJson(task8.getData()));
    }
````

More detail @see com/berry/fasttask/Test.java
