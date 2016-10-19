package rishiqing.notice.server

class TodoNotice {


    Date dateCreated
    Date lastUpdated
    // 离散日期
    String dates
    //开始时间
    Date startDate
    //结束时间
    Date endDate
    // 提醒的时间
    String clock

    static constraints = {
        clock nullable: false
        startDate nullable: false
        endDate nullable: false
        dates nullable: false
    }
    static belongsTo = [
            todo:Todo
    ]
    static mapping = {
        startDate index:  'startDate'
        endDate index: 'endDate'
        dates index:'dates'
    }
}
