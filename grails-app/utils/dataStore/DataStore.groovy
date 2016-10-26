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
        def todoMap = this.getTodoMap(dataStoreKey);
        todoMap[todo.id] = todo
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

    def setRTodoMap (Todo todo, int preFetchMinute) {
        if (!todo) return false
        // 当前分钟数
        String date1 = new Date().format('HH:mm')
        // 日程的提醒时间
        String date2 = DateUtil.clockFormatToHour24(todo.clockAlert)
        // 查询某个时间点下的所有日程 date2:21:03
        Map todoMap  = this.getTodoMap(date2)
        //  从数据仓库里获得该日程
        Todo existTodo = todoMap[todo.id]
        // 判断日程是否已存在于数据仓库中
        if (existTodo) {

        } else {

        }
    }

    /**
     * 判断是否需要今天提醒
     */
    def checkTodayAlert (Todo todo) {
        String dates = todo.dates  // 离散日期
        String datesArgs = dates.split(',')
        Date todoDay   = new Date()
        Date startDate = todo.startDate, endDate = todo.endDate; // 范围日期
        // 如果在时间段内
        if ( !startDate.after(todoDay) && !endDate.before(todoDay)) {

        } else if (true) { // 如果在离散时间内

        } else if (true) { //如果属于延期任务

        }

    }

    def setReceiverTodoMap (Todo todo, int preFetchMinute) {
        if (!todo) return false
        // 当前分钟数
        String date1 = new Date().format('HH:mm')
        // 日程的提醒时间
        String date2 = DateUtil.clockFormatToHour24(todo.clockAlert)
        // 当提醒时间和当前分钟数差值大于X分钟后，才会被记录到数据仓库中
        if (!DateUtil.inRange(date1, date2, preFetchMinute)) return false
        //  获得 HH:mm 这个分钟的日程map
        Map todoMap = this.getTodoMap(date2) // 查询某个时间点下的所有日程 key:21:03
        //  从数据仓库里获得该日程
        Todo existTodo = todoMap[todo.id]
        // 判断日程如果是 “已完成” “已删除” “已归档”“已关闭提醒” 则从数据仓库中剔除
        boolean isRemove = todo.pIsDone ||  todo.isDeleted || todo.isArchived || todo.isAlertClose()
        if (existTodo && isRemove) { // 如果日程已存在数据仓库中
            todoMap.remove(existTodo)
        } else if (existTodo && !isRemove) { // 如果不存在数据仓库中
            todoMap[todo.id] = todo
        } else if (!existTodo && !isRemove) {
            todoMap[todo.id] = todo
        }
        return true
    }
}
