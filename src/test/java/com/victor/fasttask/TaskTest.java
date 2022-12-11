package com.victor.fasttask;

import com.google.common.collect.Sets;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.*;

/**
 * @author Victor.
 * @date 2021/6/18
 * test fast task
 */
class TaskTest {
    private static final Logger logger = LoggerFactory.getLogger(TaskTest.class);

    private static final ThreadFactory namedThreadFactory = new ThreadFactoryBuilder().setNameFormat("thread-pool-%d")
            .build();
    //Common Thread Pool
    private static final ExecutorService threadPoolExecutor = new ThreadPoolExecutor(8, 32,
            500L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(1024), namedThreadFactory, new ThreadPoolExecutor.AbortPolicy());

    @Test
    void testBindJob() {
        TestTask task1 = new TestTask("任务1", "1", Sets.newHashSet(), true);
        TestTask task2 = new TestTask("任务2", "2", Sets.newHashSet(task1), true);
        TestTask task3 = new TestTask("任务3", "3", Sets.newHashSet(task2), true);
        TestTask task4 = new TestTask("任务4", "4", Sets.newHashSet(task3), true);
        TestTask task5 = new TestTask("任务5", "5", Sets.newHashSet(task4), true);
        TestTask task6 = new TestTask("任务6", "6", Sets.newHashSet(task5), true);

        List<TestTask> testTaskList = new ArrayList<>();

        testTaskList.add(task6);

        DataContext dataContext = new DataContext();
        long start = System.currentTimeMillis();
        FastTaskExecutor.execute(testTaskList, dataContext, threadPoolExecutor, true);
        long end = System.currentTimeMillis();
        logger.info("total take time: {} ms", (end - start));
        logger.info("dataContext: {}", new Gson().toJson(dataContext));
    }

    @Test
    void test() {
        List<TestTask> testTaskList = new ArrayList<>();
        TestTask task1 = new TestTask("任务1", "1", Sets.newHashSet(), true);
        TestTask task2 = new TestTask("任务2", "2", Sets.newHashSet(task1), true);
        TestTask task3 = new TestTask("任务3", "3", Sets.newHashSet(task1), false);
        TestTask task4 = new TestTask("任务4", "4", Sets.newHashSet(task2), true);
        TestTask task5 = new TestTask("任务5", "5", Sets.newHashSet(task3), true);
        TestTask task6 = new TestTask("任务6", "6", Sets.newHashSet(), true);
        TestTask task7 = new TestTask("任务7", "7", Sets.newHashSet(task5), true);
        TestTask task8 = new TestTask("任务8", "8", Sets.newHashSet(task6, task7), true);
        TestTask task9 = new TestTask("任务9", "9", Sets.newHashSet(task6), true);
        TestTask task10 = new TestTask("任务10", "10", Sets.newHashSet(task6), true);
        TestTask task11 = new TestTask("任务11", "11", Sets.newHashSet(task9), true);
        TestTask task12 = new TestTask("任务12", "12", Sets.newHashSet(task10), true);
        TestTask task13 = new TestTask("任务13", "13", Sets.newHashSet(task4, task10), true);
        TestTask task14 = new TestTask("任务14", "14", Sets.newHashSet(task11, task12), true);
        task5.getDependencies().add(task9);

        testTaskList.add(task8);
        testTaskList.add(task9);
        testTaskList.add(task13);
        testTaskList.add(task14);

        DataContext dataContext = new DataContext();
        long start = System.currentTimeMillis();
        FastTaskExecutor.execute(testTaskList, dataContext, threadPoolExecutor, true);
        long end = System.currentTimeMillis();
        logger.info("total take time: {} ms", (end - start));
        logger.info("dataContext: {}", new Gson().toJson(dataContext));
    }

    public static class TestTask extends AbstractTask {

        private final Set<AbstractTask> dependencies;

        private final String name;

        public TestTask(String name, String taskId, Set<AbstractTask> dependencies, boolean failContinue) {
            super(taskId, 3000, failContinue);
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
                Thread.sleep(new Random().nextInt(3000));
                if (new Random().nextInt(11) > 9) {
                    throw new RuntimeException("some thing bad..." + getId());
                }
                // do something amazing
                dataContext.getData().put(getId(), getName());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            long end = System.currentTimeMillis();
            logger.info("{} take time: {} ms", getId(), (end - start));
        }

        @Override
        public Set<AbstractTask> getDependencies() {
            return this.dependencies;
        }
    }
}
