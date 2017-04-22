import alertStore.AlertStore
import groovy.sql.Sql
import org.hibernate.Criteria
import org.hibernate.criterion.CriteriaSpecification
import rishiqing.notice.server.Alert
import rishiqing.notice.server.Clock
import rishiqing.notice.server.Todo
import threadPool.ThreadPoolUtil

/**
 * Created by Thinkpad on 2017/4/11.
 * 这个类负责提前检索到需要推送的人的日程
 */
class NewFetchJob {
    transient static final Integer LATER_MINUTES = 1;
    /** 获取提醒仓库 */
    static AlertStore alertStore = AlertStore.getInstance();
    /** 查询数量 : 每次最多查询200条记录 */
    static final def max = 200;
    /** 查询到下次要提醒的日程的时间 */
    static Date alertTime = null;
    /** 非小时分钟秒的时间格式　*/
    static Date taskDate = null;
    /** 字符格式的 alertTime */
    static String dates = null; // yyyyMMdd
    /** todoMap 的 键 */
    static String dateKey = null; // yyyy-MM-dd HH:mm:ss
    /** 触发器 */
    static triggers = {
        // 服务器启动后 1 分钟开始执行调度
        simple(startDelay: 1000*60);
        // 使用 cron 表达式进行触发操作
        cron(name:"todoFetchJob",cronExpression: "0 0/1 * * * ?");
    }

    /**
     * 初始化基本时间 <br>
     *     </p> 用来设置上述静态的基本时间变量 </p>
     */
    static void initTime(){
        // 获取实例(北京时间)
        Calendar calendar = Calendar.getInstance(Locale.CHINA);
        // 清空秒数
        calendar.set(Calendar.SECOND,0);
        // 千万记得清空毫秒，差点被毫秒坑死，查不出来数据
        calendar.set(Calendar.MILLISECOND,0);
        // 获取当前时间的后一分钟
        calendar.add(Calendar.MINUTE,LATER_MINUTES);
        // 提醒的时间
        alertTime = calendar.getTime();
        // 为数据仓库设置提醒时间
        AlertStore.alertTime = calendar.getTime();
        // 设置 alertTime 个字符串格式
        dates = alertTime.format("yyyyMMdd");
        // 设置键
        dateKey = alertTime.format("yyyy-MM-dd HH:mm:ss");
        // 设置 taskDate
        taskDate = new Date(alertTime.getTime()).clearTime();
    }

    /**
     * 获得提醒总数 <br>
     *     <p> 获取所有需要提醒的数量 </p>
     * @return
     */
    def getAlertCount () {
        def alertCount = Todo.createCriteria().count {
            and{
                //0：表示inner join，
                //1：表示left outer join ，
                //2：表示right outer join
                createAlias("repeatTag","trt",1)
                createAlias("todoDeploy","d",1)
                or{
                    isNull("repeatTag");
                    and{
                        isNotNull("repeatTag")
                        like("trt.repeatBaseTime","%${dates}%")
                    }
                }
                eq("isDeleted", false)
                eq("pIsDone", false)
                // 使用内链接查询
                clocks{
                    eq("isDeleted",false)
                    alert{
                        eq("alertTime", alertTime)
                    }
                }
            }
        }
        return alertCount;
    }

    /**
     * 获取指定数量的结果集 <br>
     *     <P> 查询逻辑：提醒时间是 alertTime 的，dates 中包含 alertTime 日期的，或者 dates
     *     为空 开始时间 小于等于 alertTime 且结束时间 大于等于 alertTime 的，没有被删除的，没有
     *     被完成的所有的日程被查询出来</p>
     * @param offset
     * @param max
     * @return
     */
    def getResult (def offset) {
        List<Todo> todoList = Todo.createCriteria().list(max:max,offset:offset) {
            and{
                //0：表示inner join，
                //1：表示left outer join ，
                //2：表示right outer join
                // 左连接 todoRepeatTag
                createAlias("repeatTag","trt",1)
                // 左连接 todoDeploy
                createAlias("todoDeploy","d",1)
                or{
                    // 如果重复标记不存在
                    isNull("repeatTag");
                    // 或者重复标记存在，且在重复基本时间中设置过提醒
                    and{
                        isNotNull("repeatTag")
                        like("trt.repeatBaseTime","%${dates}%")
                    }
                }
                eq("isDeleted", false)
                eq("pIsDone", false)
                // 使用内链接查询
                clocks{
                    eq("isDeleted",false)
                    alert{
                        eq("alertTime", alertTime)
                    }
                }
            }
        }
        return todoList
    }

    /**
     * 触发方法
     * @return
     */
    def execute() {
        try{
            // 初始化基本时间
            initTime();
            def count = this.getAlertCount();
            // 获取偏移量
            /*
             * 比如 ：
             * count = 13280 个提醒，则 addOffset = 13280 % 200 = 80>0? = 1，即 addOffset = 1;
             * maxOffset = 13280 > 200? 13280 / 200 = 66 + 1 : 1，即 maxOffset = 67;
             */
            def addOffset = count % max >0?1:0
            def maxOffset = count > max?( count / max ) + addOffset : 1
            // 启用线程
            ThreadPoolUtil.executeTread(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0 ; i<maxOffset; i++) {
                        // 分页
                        def offset = max * i;
                        // 获取结果 ( offset 为偏移量， max 为取出的最大值.即每次取 max ，共取 offset 页)
                        def list = getResult(offset);
                        // 存储进dataStore中
                        list.each { todo ->
                            todo.todoDeploy
                            //开关打开才会添加到仓库中
                            todo.insertDate = new Date();
                            todo.type = "newfetchJob";
                            alertStore.addTodo(dateKey,todo);
                        }
                    }
                    Map<Long,Todo> alertTodos = alertStore.getTodoMap(dateKey);
                    println "提醒时间 "+dateKey + "  已检索到提醒个数:" + alertTodos.size(); // 提醒时间
                    if(alertTodos){
                        alertTodos.entrySet().each { es -> // 提醒的日程
                            println es.key + " --> 标题是:" + es.value.getRealPTitle();
                        }
                    }

                }
            })
        } catch(Exception e){
            e.printStackTrace();
            return;
        }
    }
}
