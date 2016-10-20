package rishiqing.notice.server

import Date.DateUtil
import dataStore.DataStore
import grails.core.GrailsApplication
import org.hibernate.criterion.CriteriaSpecification
import threadPool.ThreadPoolUtil

/**
 * 查询任务，提前10分钟查询出需要发送提醒的数据
 */
class FetchJob {

    GrailsApplication grailsApplication

    static DataStore dataStore = DataStore.getInstance() // 数据仓库

    static def    max     =  200           // 每次最多查询200条记录

    static Date   date    =  null         // 当前的时间

    static String dateStr =  null        // 当前时间的字符串 yyyyMMdd

    static String minutes =  null        // 当前时间的分钟字符串 HH:mm

    static String clockAlert = null

    static triggers = {
      simple repeatInterval: 1000*60  // execute job once in 1 minute
    }

    def execute() {
        getDay2()
        def count      =  this.getCount();
        def addOffset  =  count % max >0?1:0
        def maxOffset  =  count > max?( count / max ) + addOffset : 1
        ThreadPoolUtil.executeTread(new Runnable() {
            @Override
            public void run() {
                Date start = new Date()
                String m = DateUtil.clockFormatToHour24(minutes) // 过滤为 23:59 这种形式
                for (int i = 0 ; i<maxOffset; i++) {
                    def offset = max * i
                    def list   = getResult(offset)
                    // 存储进dataStore中
                    list.each { it->
                        dataStore.setTodoMap(m, it)
                    }
                }
                Date end = new Date()
                def sss = dataStore.getDataStore()
                println('key : ' + m + ' time:' + (end.getTime() - start.getTime()) + 'ms dataStore fetch length ' + dataStore.getDataStore().size())
            }
        })
    }

    /**
     * 获取指定数量的结果集
     * @param offset
     * @param max
     * @return
     */
    def getResult (def offset) {
        List<Todo> todoList = Todo.createCriteria().list(offset:offset,max:max){
            or {
                and {
                    ge("endDate", date)
                    le("startDate",   date)
                }
                and {
                    like('dates', '%'+ dateStr + '%')
                }
            }
            eq('clockAlert', clockAlert)
            eq('pIsDone', false)
            eq("isDeleted", false)
            eq("isArchived", false)
        }
        return todoList
    }
    /**
     * 获得总数
     * @return
     */
    def getCount () {
        def count = Todo.createCriteria().count{
            or {
                and {
                    ge("endDate", date)
                    le("startDate",   date)
                }
                and {
                    like('dates', '%'+ dateStr + '%')
                }
            }
            eq('clockAlert', clockAlert)
            eq('pIsDone', false)
            eq("isDeleted", false)
            eq("isArchived", false)
        }
        return count
    }
     void  getDay () {
        Calendar nowTime = Calendar.getInstance();
        nowTime.add(Calendar.MINUTE, grailsApplication.config.preFetchMinute);
        date    =  nowTime.getTime()
        dateStr = date.format('yyyyMMdd')
        minutes = date.format('HH:mm')
    }

    void getDay2 () {
        Calendar nowTime = Calendar.getInstance();
        nowTime.add(Calendar.MINUTE, grailsApplication.config.preFetchMinute);
        date    =  DateUtil.getDay().getTime()
        dateStr = date.format('yyyyMMdd')
        minutes = DateUtil.getClockFormat(nowTime)
        clockAlert =  minutes + '-1'
    }
}
