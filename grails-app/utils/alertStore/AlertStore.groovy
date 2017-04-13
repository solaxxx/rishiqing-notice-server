package alertStore

import rishiqing.notice.server.Todo

/**
 * Created by Thinkpad on 2017/4/11.
 * 提醒仓库，默认的格式
 * ["2017-01-01 15:30:00": {1:todo1 , 2:todo2 , 3:todo3 } ]
 */
class AlertStore {
    /** 创建一个数据仓库（单例模式）*/
    private static AlertStore singleton = null
    /** 日程列表 */
    private Map<Long,Todo> todoMap = [:];
    /** 提醒映射 */
    private Map<String,Map<Long,Todo>> alertStore = [:];
    /** 默认构造 */
    private AlertStore (){
    }
    /** 获取一个数据仓库（以单例的方式） */
    public static def getInstance () {
        try {
            if (singleton) {
                return singleton
            } else {
                singleton = new AlertStore ()
                return singleton
            }
        } catch (Exception e){
            e.printStackTrace();
            return;
        }
    }

    /** 向 todoMap 中添加值*/
    public void addTodo(String key,Todo todo){
        // 获取 todoMap
        todoMap = this.getTodoMap(key);
        // 向 todoMap 中添加日程
        todoMap.put(todo.id,todo);
    }
    /** 通过key 获取数据库中的 todoMap */
    public Map<Long,Todo> getTodoMap(String key){
        // 查看对应的键值是否存在，存在则返回，不存在则创建
        todoMap = alertStore.get(key)
        if(todoMap) {
            return todoMap;
        }
        return alertStore.put(key,[:]);
    }

    /** 从 alertStore 中移除 todoMap */
    public void removeTodoMap(String dateKey){
        alertStore.remove(dateKey);
    }

    /** 从 todoMap 中移除值*/
    public void removeTodo(String key,Todo todo){
        todoMap = alertStore.get(key);
        todoMap.remove(todo.id);
    }
}
