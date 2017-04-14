package rishiqing.notice.server

class Todo {

    transient Date insertDate
    transient String type

    String pTitle // 待办事项名称
    String pNote  // 待办事项备注
    String clockAlert
    int pUserId
    Date endDate
    Date pPlanedTime
    Date startDate
    String dates
    String pContainer
    boolean pIsDone
    boolean isDeleted
    boolean isArchived
    Boolean isChangeDate = false
    Boolean isRepeatTodo = false

    static hasMany = [clocks: Clock]
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
                pTitle:  this.getRealPTitle(),
                PNote:  this.getRealPNote(),
                pUserId:  this.pUserId,
                clockAlert: this.clockAlert,
                startDate: this.startDate?.format("yyyy-MM-dd HH:mm:ss"),
                endDate: this.endDate?.format("yyyy-MM-dd HH:mm:ss"),
                dates: this.dates,
                pContainer: this.pContainer,
                pIsDone: this.pIsDone,
                isDeleted: this.isDeleted,
                isArchived: this.isArchived,
                isChangeDate: this.isChangeDate,
                isRepeatTodo: this.isRepeatTodo,
                insertDate: this.insertDate?.format("yyyy-MM-dd HH:mm:ss"),
                type: this.type
        ]
    }

//    // 改成这样的格式，看是否能解决懒加载问题。因为在提醒服务器中，todo 和 todoDeploy 和 kanbanItem 是无关联的。
//    def getRealPTitle() {
//        TodoDeploy dep = this.todoDeploy?TodoDeploy.findById(this.todoDeployId):null;
//        return dep ? dep.pTitle : this.pTitle;
//    }
//    def getRealPNote(){
//        TodoDeploy dep = this.todoDeploy?TodoDeploy.findById(this.todoDeployId):null;
//        return dep? dep.pNote :this.pNote;
//    }
//    def getRealStartDate(){
//        KanbanItem ki = this.kanbanItemId?KanbanItem.findById(this.kanbanItemId):null;
//        TodoDeploy dep = this.todoDeploy?TodoDeploy.findById(this.todoDeployId):null;
//        return ki&&!this.isChangeDate&&dep?dep.startDate:this.startDate
//    }
//    def getRealEndDate(){
//        KanbanItem ki = this.kanbanItemId?KanbanItem.findById(this.kanbanItemId):null;
//        TodoDeploy dep = this.todoDeploy?TodoDeploy.findById(this.todoDeployId):null;
//        return ki&&!this.isChangeDate&&dep?dep.endDate:this.endDate
//    }
//    def getRealDates(){
//        KanbanItem ki = this.kanbanItemId?KanbanItem.findById(this.kanbanItemId):null;
//        TodoDeploy dep = this.todoDeploy?TodoDeploy.findById(this.todoDeployId):null;
//        return ki&&!this.isChangeDate&&dep?dep.dates:this.dates
//    }

    def getRealPTitle(){return this.todoDeploy?this.todoDeploy.pTitle:this.pTitle}
    def getRealPNote(){return this.todoDeploy?this.todoDeploy.pNote:this.pNote}
    def getRealStartDate(){return this.kanbanItem&&!this.isChangeDate&&this.todoDeploy?this.todoDeploy.startDate:this.startDate}
    def getRealEndDate(){return this.kanbanItem&&!this.isChangeDate&&this.todoDeploy?this.todoDeploy.endDate:this.endDate}
    def getRealDates(){return this.kanbanItem&&!this.isChangeDate&&this.todoDeploy?this.todoDeploy.dates:this.dates}
}
