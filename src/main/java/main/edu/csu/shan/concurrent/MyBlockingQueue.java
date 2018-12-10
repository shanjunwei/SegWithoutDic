package main.edu.csu.shan.concurrent;

import java.util.concurrent.ArrayBlockingQueue;

/**
 * 消息生产消费的模型
 */
public class MyBlockingQueue {
    public static ArrayBlockingQueue<String> fairQueue = new ArrayBlockingQueue(600000, true);

    // 消息生产
    public static void produce(String msg) {
        fairQueue.add(msg);
    }

    // 消息消费
    public static String consume() {
        return fairQueue.poll();
    }
}



