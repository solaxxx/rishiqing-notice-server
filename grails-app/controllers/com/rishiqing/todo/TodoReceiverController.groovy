package com.rishiqing.todo

import Date.DateUtil
import com.rishiqing.PushCenter
import com.rishiqing.base.push.PushBean
import dataStore.DataStore
import rishiqing.notice.server.Todo
import rishiqing.notice.server.TodoNotice

/**
 * 远程链接接收器
 */
class TodoReceiverController {

    static  DataStore dataStore = DataStore.getInstance()

    // http://localhost:8080/todoReceiver/index
    def index() {
        def ss = dataStore.getDataStore()
        render "dataStore length: " + dataStore.getDataStore().size()
    }
    // http://localhost:8080/todoReceiver/receive?id=3
    def receive () {
        // JSONObject objData = request.JSON
        def id = params.getLong('id')
        if (!id) {
            render (status: 412)
            return
        }
        TodoNotice todoNotice = TodoNotice.get(id)
        DateUtil.getClockFormat()
        if (!DateUtil.minuteMatch(todoNotice.clock)) {
            render (status: 412, text: 'clock miss match')
            return
        }
        dataStore.setReceiverTodoMap(todoNotice, grailsApplication.config.preFetchMinute)
        render (status: 200, text: 'success')
        return
    }
// http://localhost:8080/todoReceiver/rce?id=3
    def rce () {
        def id = params.getLong('id')
        if (!id) {
            render (status: 412)
            return
        }
        Todo todo = Todo.get(id)
        if (!todo) {
            render (status: 412, text: 'todo not found')
            return
        }
        if (!DateUtil.clockAlertMatch(todo.clockAlert)) {
            render (status: 412, text: 'clock miss match')
            return
        }
        boolean result = dataStore.setReceiverTodoMap(todo, grailsApplication.config.preFetchMinute)
        render (status: 200, text: result? 'success' : 'fail' )
        return
    }

    public static void main (String [] args) {
        def push = PushCenter.createFactory()
        PushBean pushBean = new PushBean('我的测试xiaomi', "sss")
        pushBean.targetValue = '282'
        pushBean.soundURL = 'pushsound'
        pushBean.addExtra('sss',11)
        pushBean.addExtra('ddd',22)
        push.notice.push(pushBean)
    }
}
