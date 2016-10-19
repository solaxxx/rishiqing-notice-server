package rishiqing.notice.server

class Todo {

    String pTitle // 待办事项名称
    String pNote  // 待办事项备注
    String clockAlert
    int pUserId
    Date endDate
    Date startDate
    String dates


    static constraints = {
    }

    def toMap () {
        return [
                pTitle  :  this.pTitle,
                pNote   :  this.pNote,
                pUserId :  this.pUserId,
                clockAlert : this.clockAlert
        ]
    }
}
