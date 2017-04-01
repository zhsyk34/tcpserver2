package task;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TimerTaskTest {

    private static TimerTask task = new TimerTask() {
        private int i = 0;

        @Override
        public void run() {
            System.out.println(i++);
        }
    };

    private static int k = 0;
    private static Runnable runnable = () -> System.out.println(k++);

    public static void simple() {
        Timer timer = new Timer();
        timer.schedule(task, 1000, 1000);
    }

    public static void schedule() {
        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
        service.scheduleAtFixedRate(runnable, 1, 1, TimeUnit.SECONDS);
    }

    public static void main(String[] args) throws Exception {
        schedule();
    }
}
