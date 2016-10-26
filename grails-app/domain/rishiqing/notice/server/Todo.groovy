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
    Boolean isChangeDate = false


    static belongsTo = [todoDeploy: TodoDeploy,kanbanItem: KanbanItem]

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

    def getRealPTitle(){return this.todoDeploy?this.todoDeploy.pTitle:this.pTitle}
    def getRealPNote(){return this.todoDeploy?this.todoDeploy.pNote:this.pNote}
    def getRealStartDate(){return this.kanbanItem&&!this.isChangeDate&&this.todoDeploy?this.todoDeploy.startDate:this.endDate}
    def getRealEndDate(){return this.kanbanItem&&!this.isChangeDate&&this.todoDeploy?this.todoDeploy.endDate:this.endDate}
    def getRealDates(){return this.kanbanItem&&!this.isChangeDate&&this.todoDeploy?this.todoDeploy.dates:this.dates}
}
