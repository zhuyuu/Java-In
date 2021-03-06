package concurrent.ProducerConsumer;

import java.util.Random;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by liur on 17-4-26.
 */
public class Producer implements Runnable {
    private volatile boolean isRunning = true;
    private BlockingDeque<PCData> queue;    //内存缓冲区
    private static AtomicInteger count = new AtomicInteger();   //总数,原子操作
    private static final int SLEEPING = 1000;

    public Producer(BlockingDeque<PCData> queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
        PCData data = null;
        Random r = new Random();

        System.out.println("start producer id=" + Thread.currentThread().getId());


        try {
            while (isRunning) {
                Thread.sleep(r.nextInt(SLEEPING));
                data = new PCData(count.incrementAndGet());
                System.out.println(data+" is put into queue");
                if (!queue.offer(data,2, TimeUnit.SECONDS)){
                    System.out.println("failed to put data: "+data);
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }

    }

    public void stop() {
        isRunning = false;
    }
}
