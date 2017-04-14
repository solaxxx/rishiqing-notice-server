import alertStore.AlertStore
import com.rishiqing.PushCenter
import com.rishiqing.base.push.PushBean
import com.xiaomi.xmpush.server.Constants
import grails.core.GrailsApplication
import rishiqing.notice.server.Todo
import threadPool.ThreadPool
import threadPool.ThreadPoolUtil

/**
 * Created by Thinkpad on 2017/4/11.
 * 用来发送推送的类，这个类负责查询到所有需要闹钟推送的人的日程，然后进行推送操作
 */
class NewSendJob {

    GrailsApplication grailsApplication;
    /** 数据仓库 */
    static AlertStore alertStore = AlertStore.getInstance();
    /** 查询到下次要提醒的日程的时间 */
    static Date alertTime = null;
    /** 字符格式的 alertTime */
    static String dates = null; // yyyyMMdd
    /** todoMap 的 键 */
    static String dateKey = null; // yyyy-MM-dd HH:mm:ss
    /** 设置一个触发器 */
    /*
     * 关于 triggers 的说明：
     *
     *      class MyJob {
     *          static triggers = {
     *              // 定义一个触发器，name是 mySimpleTrigger，在服务器启动 60000 mill 后开始运行，并每隔 1000 mill 运行一次
     *              // 可以不给定 startDelay 和 repeatInterval ：
     *              // 如果这两个属性不指定，则使用默认值（repeatInterval为1分钟，startDelay为30秒）
     *              simple name: 'mySimpleTrigger', startDelay: 60000, repeatInterval: 1000
     *          }
     *          // 触发器会默认每隔  1000  mill 运行一次 execute 方法
     *          def execute(){
     *              print "Job run!"
     *          }
     *      }
     *
     * 通过 cron 表达式调度任务:
     *
     *      class MyJob  {
     *            static triggers = {
     *                // 每天 6 点调度任务
     *                cron name: 'myTrigger', cronExpression: "0 0 6 * * ?"
     *            }
     *            def execute(){ print "Job run!" }
     *       }
     *
     * 关于 cron 表达式:
     *
     * 具体可见博客:
     * @link http://www.cnblogs.com/junrong624/p/4239517.html#undefined
     * cron 表达式在线生成网址：
     * @link http://cron.qqe2.com/
     *
     * 两种格式：
     *      秒       份       时   月中的哪一天   月     周中的哪一天   年
     *      Seconds Minutes Hours DayofMonth   Month  DayofWeek   Year 或
     *      Seconds Minutes Hours DayofMonth   Month  DayofWeek
     *
     * 几个例子：
     * 每隔5秒执行一次：0/5 * * * * ?  （第一个 0 也可以使用 * 表示，意思相同）
     * 每隔1分钟执行一次：0 0/1 * * * ?
     * 每天23点执行一次：0 0 23 * * ?
     * 每天凌晨1点执行一次：0 0 1 * * ?
     * 每月1号凌晨1点执行一次：0 0 1 1 * ?
     * 每月最后一天23点执行一次：0 0 23 L * ?
     * 每周星期天凌晨1点实行一次：0 0 1 ? * L
     * 在26分、29分、33分执行一次：0 26,29,33 * * * ?
     * 每天的0点、13点、18点、21点都执行一次：0 0 0,13,18,21 * * ?
     *
     * */
    static triggers = {
        // 使用 cron 表达式进行控制：每隔2分钟进行一次调度
//        cron(name:"todoSendJob",cronExpression: "0 0/1 * * * ?");
        cron(name:"todoSendJob",cronExpression: "0 0/1 * * * ?");
    }

    /**
     * 初始化基本时间 <br>
     *     </p> 用来设置上述静态的基本时间变量 </p>
     */
    void initTime(){
        // 获取实例(北京时间)
        Calendar calendar = Calendar.getInstance(Locale.CHINA);
        // 清空秒数
        calendar.set(Calendar.SECOND,0);
        // 提醒的时间
        alertTime = calendar.getTime();
        // 设置 alertTime 个字符串格式
        dates = alertTime.format("yyyyMMdd");
        // 设置键
        dateKey = alertTime.format("yyyy-MM-dd HH:mm:ss");
    }

    /** 触发条件下直接执行 execute 方法　*/
    def execute() {
        try{
            // 获取当前时间(为 dateStr 和 minutes 赋值)
            initTime()
            def a = alertTime;
            def b = dates;
            def c = dateKey;
            Map<Long,Todo> alertTodos = alertStore.getTodoMap(dateKey);
            // 获取当前时间需要发送的提醒
            def todoMap = alertStore.getTodoMap(dateKey);
            // 启动线程
            ThreadPoolUtil.executeTread(new Runnable() {
                @Override
                public void run() {
                    todoMap.each { it ->
                        try {
                            // 获取要提醒的日程
                            def todo = it.value
                            if(todo){
                                //  发送推送
                                pushMessage(todo);
                                // 控制台打印测试
                                println('向userId为 ' + todo.pUserId +  ' 的用户发送了提醒,id:' + todo.id + '标题是:' + todo.pTitle)
                            }
                        } catch (Exception e) {
                            e.printStackTrace()
                        }
                    }
                    // 删除数据仓库索引中对应的实例
                    alertStore.removeDataStore(dateKey);
                    println("移除dataStore中的实例:"+dateKey + "\n")
                    // 控制台输出打印发送个数
                    println('dateKey : ' + dateKey + ', dataStore after send remove length ' + alertStore.getTodoMap()?.size())
                }
            })
        } catch (Exception e){
            e.printStackTrace();
            return;
        }
    }

    /**
     * 进行铃铛推送
     * @param todo
     */
    void pushMessage (Todo todo) {
        String t = todo.getRealPTitle()
        String pTitle = t&&t.length()>20?t.substring(0,20):t
        PushCenter.setConfigRootPath('push')
        /************************移动端推送***************************/
        def push = PushCenter.createFactory(ThreadPool.getInstance())
        // 设置推送类型
        push.addAndroidPush(PushCenter.MI_PUSH)
        push.addAndroidPush(PushCenter.J_PUSH)
        // push.addAndroidPush(PushCenter.ALI_PUSH)
//        push.addAndroidPush(PushCenter.HW_PUSH)
        push.addIosPush(PushCenter.J_PUSH)
        push.addIosPush(PushCenter.MI_PUSH)

        // 设置推送内容
        PushBean pushBean = new PushBean(pTitle, todo.getRealPNote()?:"点击查看")
        pushBean.setTargetValue(todo.pUserId)
        pushBean.setSoundURL(grailsApplication.config.soundURL)
        pushBean.addExtra('hrefB', todo.pContainer)
        pushBean.addExtra('hrefC', todo.id)
        pushBean.addExtra('messageType', 100) // 闹钟提醒
        pushBean.addExtra('alertTime', "21:21") // 提醒时间
        pushBean.addExtra('pTitle', pTitle) // 提醒时间

        pushBean.addExtra(Constants.EXTRA_PARAM_SOUND_URI, grailsApplication.config.androidSoundURL) // 提醒时间
        push.notice.push(pushBean)
        /************************web端推送***************************/
        def webPush = PushCenter.createFactory(PushCenter.WEB,ThreadPool.getInstance())
        webPush.webPush('userId' + todo.pUserId, 'todoAlert',
                [pTitle:pTitle, id:todo.id, clock:"21:21"])
    }
}
