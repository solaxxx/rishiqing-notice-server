package com.rishiqing.todo

import Date.DateUtil
import alertStore.AlertStore
import com.rishiqing.PushCenter
import com.rishiqing.base.push.PushBean
import dataStore.DataStore
import rishiqing.notice.server.Alert
import rishiqing.notice.server.Todo
import rishiqing.notice.server.TodoNotice

/**
 * 远程链接接收器
 */
class TodoReceiverController {

    static  DataStore dataStore = DataStore.getInstance()
    static AlertStore alertStore = AlertStore.getInstance();

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
        todo.insertDate = new Date()
        todo.type = "call"
        if (!DateUtil.clockAlertMatch(todo.clockAlert)) {
            render (status: 412, text: 'clock miss match')
            return
        }
        boolean result = dataStore.setReceiverTodoMap(todo, grailsApplication.config.preFetchMinute)
        render (status: 200, text: result? 'success' : 'fail' )
        return
    }

    // http://192.168.31.14:8177/todoReceiver/pushAlert
    def pushAlert(){
        try{
            // 日程 id
            String id = params.id;
            // 提醒时间
            String alertTimeStr = params.alertTime; // 2017-01-01 23:30:00
            // 类型
            String type = params.type; // "add"/"remove"
            // 获取日程
            Todo todo = Todo.findById(id.toLong());
            if(!todo){
                throw new Exception("todo not found");
            }
            // 判断 type 类型
            if(Alert.ADD_ALERT.equals(type)){
                // 添加到某个提醒里面
                alertStore.addTodo(alertTimeStr,todo);
                println "添加到 "+alertTimeStr + " --> 日程" + todo.id
            } else if(Alert.REMOVE_ALERT.equals(type)){
                // 从某个提醒中移除
                alertStore.removeTodo(alertTimeStr,todo);
                println "删除 "+alertTimeStr + "中的 --> 日程 " + todo.id
            } else {
                throw new Exception("type error ")
            }
            return;
        } catch (Exception e){
            e.printStackTrace();
            return;
        }
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
