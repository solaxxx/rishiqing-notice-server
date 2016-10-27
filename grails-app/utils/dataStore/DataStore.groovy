package dataStore

import Date.DateUtil
import grails.core.GrailsApplication
import rishiqing.notice.server.Todo
import rishiqing.notice.server.TodoNotice

/**
 * Created by solax on 2016/9/18.
 *
 * 数据仓库格式说明：
 * dataMap = {
 *      '19:20' : todoMap = {todoId(1111): todoDomain}
 * }
 */
class DataStore {

    GrailsApplication grailsApplication

    private  static DataStore singleton = null
    // 数据仓库
    private  def dataStore = [:]

    private  def dataStoreIndex = [:]

    private DataStore () {}

    static def getInstance () {
        if (singleton) {
            return singleton
        } else {
            singleton = new DataStore ()
            return singleton
        }
    }
    // get
    def getDataStore () {
        return dataStore
    }
    def getDataStoreIndex () {
        return dataStoreIndex
    }

    // set
    def setDataStore (String key, Map todoMap) {
        dataStore[key] = todoMap
    }

    // getTodoMap
    def getTodoMap (String key) {
        def todoMap = dataStore[key]
        if (todoMap) return todoMap;
        return  dataStore[key] = [:];
    }

    def setTodoMap (String dataStoreKey, Todo todo) {
        todo.todoDeploy
        def todoMap = this.getTodoMap(dataStoreKey);
        todoMap[todo.id] = todo
        dataStoreIndex[todo.id] = todo.clockAlert
    }

    def remove (key) {
        try {
            def map = dataStore[key]
            if (map == null)  return false
            dataStore.remove(key)
            return true
        } catch (Exception) {
            return false
        }
    }

    def setRTodoMap (Todo todo) {
        if (!todo || todo.isDeleted || todo.isArchived || todo.pIsDone || todo.isRepeatTodo || !checkTodayAlert(todo)) return false
        // 当前分钟数
//        String date1 = new Date().format('HH:mm')
        // 日程的提醒时间
        String date2 = DateUtil.clockFormatToHour24(todo.clockAlert)
        // 查询某个时间点下的所有日程 date2:21:03
        todo.todoDeploy
        Map todoMap  = this.getTodoMap(date2)
        todoMap[todo.id] = todo
        dataStoreIndex[todo.id] = todo.clockAlert

    }

    /**
     * 判断是否需要今天提醒
     */
    def checkTodayAlert (Todo todo) {
        Date now = new Date()
        String nowStr = now.format("yyyyMMdd")
        String dates = todo.getRealDates()
        Date startDate = todo.getRealStartDate()
        if(dates){
            /*
            * 最大的时间小于当前时间则为延期
            * dates包含了nowStr则一定会显示在今天
            * */
            return DateUtil.getMaxDate(dates).getTime()<now.getTime()||dates.split(",").contains(nowStr)
        }else if(startDate){
            //开始时间在今天之前的都会显示
            return startDate.getTime()<=now.getTime()
        }
        return false
    }

    def setReceiverTodoMap (Todo todo, int preFetchMinute) {
        if (!todo) return false
        //查看索引里之前是否已经保存该日程
        String clockAlert = dataStoreIndex[todo.id]
        if(clockAlert){
            String date2 = DateUtil.clockFormatToHour24(clockAlert)
            // 查询某个时间点下的所有日程 date2:21:03
            Map todoMap  = this.getTodoMap(date2)
            todoMap.remove(todo.id)
            dataStoreIndex.remove(todo.id)
        }


        // 当前分钟数
        String date1 = new Date().format('HH:mm')
        // 日程的提醒时间
        String date2 = DateUtil.clockFormatToHour24(todo.clockAlert)
        // 当提醒时间和当前分钟数差值大于X分钟后，才会被记录到数据仓库中
        if(!date2) return false
        if (!DateUtil.inRange(date1, date2, preFetchMinute)) return false
        setRTodoMap(todo)
        return true
    }
}
