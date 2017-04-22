package rishiqing.notice.server

class Alert {

    /** 删除提醒时推送的类型 */
    transient static final String REMOVE_ALERT = "remove";
    /** 创建提醒时推送的类型 */
    transient static final String ADD_ALERT = "add";
    /**
     * 提醒调度时间 <br>
     * <p> 目前的格式有：（注意，时间值不固定，前缀固定）
     *  begin_-30_min   ：开始前30分钟响铃
     *  begin_30_min    ：开始后30分钟响铃
     *  end_-30_min     ：结束前30分钟提醒
     *  end_30_hour      ：结束后30小时提醒
     */
    String schedule;
    /** 闹钟响铃时间　*/
    Date alertTime;   // 这个值需要后台自己计算并存入

    /** 惯例配置 */
    static mapping = {
        version(false);
    }
    /** 一对一 */
    static belongsTo = [
            clock:Clock  // 一个提醒归属于一个闹钟
    ]
    /** 约束配置 */
    static constraints = {
    }

    Map toMap(){
        return [
                id:this.id,
                alertTime:this.alertTime,
                schedule:this.schedule
        ]
    }
}
