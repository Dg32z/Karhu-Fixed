package me.liwk.karhu.util.thread;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.Getter;
import me.liwk.karhu.Karhu;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Getter
public class Thread {
   private final ExecutorService executorService = Executors.newSingleThreadExecutor(
      new ThreadFactoryBuilder().setNameFormat("karhu-user-thread-" + Karhu.getInstance().getThreadManager().getUserThreads().size()).build()
   );
   public int count;

}
