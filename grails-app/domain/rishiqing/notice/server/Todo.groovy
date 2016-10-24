package rishiqing.notice.server

class Todo {

    String pTitle // 待办事项名称
    String pNote  // 待办事项备注
    String clockAlert
    int pUserId
    Date endDate
    Date startDate
    String dates
    String pContainer
    boolean pIsDone
    boolean isDeleted
    boolean isArchived


    static constraints = {
    }

    //  当前的日程是否已关闭提醒
     def isAlertClose () {
        if (!this.clockAlert) return true;
        String [] args = this.clockAlert.split('-')
        String toggle = args[2]
        return toggle == "1" ? false : true
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
