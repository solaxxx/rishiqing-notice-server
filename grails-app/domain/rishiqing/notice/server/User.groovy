package rishiqing.notice.server

class User {
    //自动注册公司记录的第三方数据
    String outerId
    static constraints = {
        outerId nullable: true
    }
}