package me.liwk.karhu.util.thread;

import lombok.Getter;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.util.MathUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Getter
public class ThreadManager {
   private final int maxThreads = Runtime.getRuntime().availableProcessors() * 2;
   private final List<Thread> userThreads = new ArrayList<>();

   public Thread generate() {
      if (this.isThreadsOver()) {
         Thread randomThread = this.getUserThreads()
            .stream()
            .min(Comparator.comparing(Thread::getCount))
            .orElse(MathUtil.randomElement(this.getUserThreads()));
         if (randomThread != null) {
            ++randomThread.count;
            return randomThread;
         } else {
            Thread thread = new Thread();
            ++thread.count;
            this.userThreads.add(thread);
            return thread;
         }
      } else {
         Thread randomThread = this.getUserThreads()
            .stream()
            .min(Comparator.comparing(Thread::getCount))
            .orElse(MathUtil.randomElement(this.getUserThreads()));
         if (randomThread != null && randomThread.getCount() < 15) {
            ++randomThread.count;
            return randomThread;
         } else {
            Thread thread = new Thread();
            ++thread.count;
            this.userThreads.add(thread);
            return thread;
         }
      }
   }

   public void shutdownThread(KarhuPlayer data) {
      --data.getThread().count;
      if (data.getThread().getCount() < 1 && this.userThreads.size() > 1) {
         data.getThread().getExecutorService().shutdownNow();
         this.userThreads.remove(data.getThread());
      }
   }

   public boolean isThreadsOver() {
      return this.userThreads.size() > this.maxThreads - 1;
   }

   public ScheduledExecutorService generateServiceScheduled() {
      return Executors.newSingleThreadScheduledExecutor();
   }
}
