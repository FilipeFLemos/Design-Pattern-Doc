package detection;

import utils.Utils;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class PatternDetectionScheduler implements Runnable{
    @Override
    public void run() {
        ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1);
        while(true) {
            ScheduledFuture<?> scheduledFuture = scheduledThreadPoolExecutor.schedule(new PatternDetection(), 0, TimeUnit.SECONDS);

            try {
                Thread.sleep(Utils.PATTERN_DETECTION_DELAY * 1000);
                scheduledFuture.cancel(true);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
