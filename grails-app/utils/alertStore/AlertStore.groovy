package alertStore

import rishiqing.notice.server.Todo

/**
 * Created by Thinkpad on 2017/4/11.
 * 提醒仓库，默认的格式
 * ["2017-01-01 15:30:00": {1:todo1 , 2:todo2 , 3:todo3 } ]
 */
class AlertStore {
    public static Date alertTime = null;
    /** 创建一个数据仓库（单例模式）*/
    private static AlertStore singleton = null
    /** 提醒映射 */
    private Map<String,Map<Long,Todo>> dataStore = [:];
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
        if(Date.parse("yyyy-MM-dd HH:mm:ss",key)>= alertTime){
            // 获取 todoMap
            def todoMap = this.getTodoMap(key);
            // 向 todoMap 中添加日程
            todoMap.put(todo.id,todo);
        }
    }
    /** 通过key 获取数据库中的 todoMap */
    public Map<Long,Todo> getTodoMap(String key){
        // 查看对应的键值是否存在，存在则返回，不存在则创建
        def todoMap = dataStore.get(key)
        if(todoMap) {
            return todoMap;
        }
        dataStore.put(key,[:])
        return dataStore.get(key);
    }

    /** 从 dataStore 中移除 todoMap */
    public void removeTodoMap(String dateKey){
        dataStore.remove(dateKey);
    }

    /** 从 todoMap 中移除值*/
    public void removeTodo(String key,Todo todo){
        def a = dataStore
        def todoMap = dataStore.get(key);
        if(todo && todoMap && todoMap.get(todo.id))
            todoMap.remove(todo.id)

    }

    /** 移除当前时间的提醒 */
    public void removeDataStore(String key){
        dataStore.remove(key);
    }
}
