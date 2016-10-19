package threadPool

import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * Created by zjy on 2015/10/29.
 *
 * 获得当前线程池
 *
 */
class GetThreadPool {

    private static ThreadPoolExecutor threadsPool = null


    public static ThreadPoolExecutor getInstance(){
        if (threadsPool == null) {
            threadsPool = new ThreadPoolExecutor(
                100,//corePoolSize 核心池大小 int
                200,//maximumPoolSize 线程池最大线程数int
                1L,//keepAliveTime 每个线程空闲后的存货时间long
                TimeUnit.DAYS, // keepAliveTime的时间单位
                new LinkedBlockingQueue<Runnable>(), //workQueue 等待队列类型
                new ThreadPoolExecutor.CallerRunsPolicy() //拒绝处理任务时的策略
            );
        }
        return threadsPool
    }

    private GetThreadPool(){

    }


}
