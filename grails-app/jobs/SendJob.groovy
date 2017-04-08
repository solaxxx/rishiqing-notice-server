import com.rishiqing.PushCenter
import com.rishiqing.base.push.PushBean
import com.xiaomi.xmpush.server.Constants
import dataStore.DataStore
import grails.core.GrailsApplication
import rishiqing.notice.server.Todo
import threadPool.ThreadPool
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
                        dataStore.getDataStoreIndex().remove(todo.id)
                        // println('向userId为 ' + todo.pUserId +  ' 的用户发送了提醒,id:' + todo.id + '标题是:' + todo.getRealPTitle())
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
        pushBean.addExtra('alertTime', minutes) // 提醒时间
        pushBean.addExtra('pTitle', pTitle) // 提醒时间

        pushBean.addExtra(Constants.EXTRA_PARAM_SOUND_URI, grailsApplication.config.androidSoundURL) // 提醒时间
        push.notice.push(pushBean)
        /************************web端推送***************************/
         def webPush = PushCenter.createFactory(PushCenter.WEB,ThreadPool.getInstance())
         webPush.webPush('userId' + todo.pUserId, 'todoAlert',
                 [pTitle:pTitle, id:todo.id, clock:minutes])
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
