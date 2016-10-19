package threadPool

import java.util.concurrent.ThreadPoolExecutor

/**
 * Created by Administrator on 2015/10/29.
 */
class ThreadPoolUtil {

    /**
     * 执行一个任务
     * @param runnable
     * @return
     */
    static def executeTread(Runnable runnable){
        ThreadPoolExecutor threadPool =  GetThreadPool.getInstance()
        threadPool.execute(runnable)
    }
}
