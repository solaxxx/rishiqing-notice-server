package rishiqing.notice.server

import groovy.sql.GroovyRowResult
import groovy.sql.Sql

class Todo {

    def dataSource;

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
    static belongsTo = [todoDeploy: TodoDeploy,kanbanItem: KanbanItem,repeatTag: TodoRepeatTag]

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

    /*
     * 这里使用纯 sql 的原因是各表之间无法建立直接关联导致的，提醒服务器禁止任何表之间建立关联
     */
    /**
     * 获取标题
     * @return
     */
    def getRealPTitle(){
        // sql 对象
        Sql sql = new Sql(dataSource);
        // 获取 todoDeployId
        GroovyRowResult idField = sql.firstRow("select t.todo_deploy_id as id from todo as t where t.id = ?",[this.id]);
        Long depId = idField[0];
        String title;
        // 获取标题
        if(depId){
            GroovyRowResult titleField = sql.firstRow("select dep.p_title as title from todo_deploy as dep where dep.id = ?",[depId]);
            title = titleField[0];
        } else {
            title = this.pTitle;
        }
        // 返回
        return title;
    }

    /**
     * 获取内容
     * @return
     */
    def getRealPNote(){
        // sql 对象
        Sql sql = new Sql(dataSource);
        // 获取 todoDeployId
        GroovyRowResult idField = sql.firstRow("select t.todo_deploy_id as id from todo as t where t.id = ?",[this.id]);
        Long depId = idField[0];
        // 获取内容
        String note;
        if(depId){
            GroovyRowResult noteField = sql.firstRow("select dep.p_note as note from todo_deploy as dep where dep.id = ?",[depId]);
            note = noteField[0];
        } else {
            note = this.pNote;
        }
        return note;
    }
    /**
     * 获取开始时间
     * @return
     */
    @Deprecated
    def getRealStartDate(){return this.kanbanItem&&!this.isChangeDate&&this.todoDeploy?this.todoDeploy.startDate:this.startDate}
    /**
     * 获取结束时间
     * @return
     */
    @Deprecated
    def getRealEndDate(){return this.kanbanItem&&!this.isChangeDate&&this.todoDeploy?this.todoDeploy.endDate:this.endDate}
    /**
     * 获取离散时间
     * @return
     */
    @Deprecated
    def getRealDates(){return this.kanbanItem&&!this.isChangeDate&&this.todoDeploy?this.todoDeploy.dates:this.dates}
}
