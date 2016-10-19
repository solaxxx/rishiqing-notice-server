import com.rishiqing.PushCenter
import com.rishiqing.base.push.PushBean
import dataStore.DataStore
import grails.core.GrailsApplication
import rishiqing.notice.server.Todo
import threadPool.ThreadPoolUtil

/**
 * Created by solax on 2016/9/19.
 */
class SendJob {

    GrailsApplication grailsApplication

    static DataStore dataStore = DataStore.getInstance() // 数据仓库

    static Date   date    =  null         // 当前的时间

    static String dateStr =  null        // 当前时间的字符串 yyyyMMdd

    static String minutes =  null        // 当前时间的分钟字符串 HH:mm

    static triggers = {
        simple startDelay:1000*60,repeatInterval: 1000*60  // execute job once in 1 minute
    }

    def execute() {
        getDay()
        // 获取当前时间需要发送的提醒
        def todoMap = dataStore.getTodoMap(minutes)
        ThreadPoolUtil.executeTread(new Runnable() {
            @Override
            public void run() {
                todoMap.each { it ->
                    try {
                        def todo = it.value
                        pushMessage(todo)  //  发送推送
                        println('向userId为 ' + todo.pUserId +  ' 的用户发送了提醒,标题是:' + todo.pTitle)
                    } catch (Exception e) {
                        e.printStackTrace()
                    }
                }
                // 消除该时间的提醒数据
                dataStore.remove(minutes)
                println('key : ' + minutes + ', dataStore after send remove length ' + dataStore.getDataStore().size())
            }
        })
    }


     void pushMessage (Todo todo) {
        def push = PushCenter.createFactory()
        PushBean pushBean = new PushBean(todo.pTitle, todo.pNote)
        pushBean.setTargetValue(todo.pUserId)
        pushBean.setSoundURL(grailsApplication.config.soundURL)
        pushBean.addExtra('hrefA', todo.id)
        pushBean.addExtra('messageType', 100) // 闹钟提醒
        push.notice.push(pushBean)
    }
    /**
     * 数据处理
     */
    static void  getDay () {
        date    =  new Date()
        dateStr = date.format('yyyyMMdd')
        minutes = date.format('HH:mm')
    }
}
