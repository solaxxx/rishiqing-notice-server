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

    def setReceiverTodoMap (Todo todo, int preFetchMinute) {
        if (!todo) return false
        String date1 = new Date().format('HH:mm')
        String date2 = DateUtil.clockFormatToHour24(todo.clockAlert)
        if (!DateUtil.inRange(date1, date2, preFetchMinute)) return false
        Map todoMap = this.getTodoMap(date2) // 查询某个时间点下的所有日程 key:21:03
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
