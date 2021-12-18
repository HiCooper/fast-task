package com.victor.fasttask;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    void repeat() {
        for (int i = 0; i < 10; i++) {
            test();
        }
    }

    @Test
    void doJob() {
        test();
    }

    void test() {
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
        logger.info("total take time: {} ms", (end - start));
        logger.info("dataContext: {}", new Gson().toJson(dataContext));
    }

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
        public List<String> getDependencies() {
            return this.dependencies;
        }
    }
}
