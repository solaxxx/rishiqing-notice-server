package Date

import java.util.regex.Pattern

/**
 * Created by solax on 2016/9/18.
 */
class DateUtil {

    // static String minutePattern = "^\\d{2}\\s*:\\s*\\d{2}$";

    static def parseDate(String orgDate, String format){
        if(orgDate==null||"null".equals(orgDate)||"".equals(orgDate)){
            return null
        }else{
            return Date.parse(format,orgDate)
        }
    }

    static def inRange (String date1, String date2, int maxRange) {
        int range = calculateMinute(date1, date2);
        if (range <= maxRange) return  true
        return false
    }

    static def calculateMinute (String date1, String date2) {
        def dateStr =  new Date ().format('yyyy-MM-dd')
        Date day1   =  parseDate( dateStr + ' ' + date1, 'yyyy-MM-dd HH:mm')
        Date day2   =  parseDate( dateStr + ' ' + date2, 'yyyy-MM-dd HH:mm')
        def range  =   Math.abs(day1.getTime() - day2.getTime())
        int range_minute =  range / 1000 / 60
        return range_minute
    }
    // 10:13-PM-1
    static def clockAlertMatch (String date) {
        def ma =/(?:1[01]|[1-9]):[0-5][0-9]-(PM|AM)-(0|1)$/
        def matcher = (date =~ ma);
        if(matcher.getCount() >0) return  true
        return false
    }

    static def getDay() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.SECOND,0);
        calendar.set(Calendar.MINUTE,0);
        return calendar
    }

    static def minuteMatch (String date) {
        def ma = /[0-2][0-9]\:[0-5][0-9]/;
        def matcher = (date =~ ma);
        if(matcher.getCount() >0) return  true
        return false
    }


    static def hour12To24 (Calendar calender) {
        int hours = calender.get(Calendar.HOUR_OF_DAY)
    }

    // 9:00-AM
    //  11:00-PM
    static def getClockFormat (Calendar calender) {
        Date date = calender.getTime()
        int h = calender.get(Calendar.HOUR)
        String m =  date.format('mm')
        String pm_am = calender.get(Calendar.AM_PM) == 1 ? 'PM' : 'AM'
        return   h + ":" + m + "-" + pm_am
    }

    // 13:00
    // 23:59
    static def clockFormatToHour24 (String clockAlert) {
        if (!clockAlert) return null
        String [] args = clockAlert.split('-')
        String [] hm = args[0].split(':')
        String h  = hm[0]
        String m  = hm[1]
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR, Integer.parseInt(h))
        calendar.set(Calendar.MINUTE, Integer.parseInt(m))
        calendar.set(Calendar.AM_PM, args[1] == 'PM' ? 1 : 0 )
        Date date    =  calendar.getTime()
        String minutes = date.format('HH:mm')
        return minutes
    }

    static Boolean isOpenClock(String clockAlert){
        if (!clockAlert) return false
        String [] args = clockAlert.split('-')
        String o  = args[2]
        return "1".equals(o)
    }



    static def hour24To12 (String date) {
        if (!minuteMatch(date))  return null
        String [] args = date.split(':')
        int hour = Integer.parseInt(args[0])
        return hour >= 12 ? '下午 ' + formatHour(hour - 12) + ':' + args[1] : '上午 ' + formatHour(hour) + ':' + args[1]

    }
    static def formatHour (int hour) {
        if (hour >= 10) return String.valueOf(hour)
        else {
            return '0' + hour
        }
    }

    static def formatHour (String hour) {

    }

    static Date getMaxDate(String dates){
        if(!dates){
            return null
        }
        String[] str = dates.split(",")
        Date maxDate = Date.parse("yyyyMMdd",str[0])
        for(int i=1;i<str.length;i++){
            Date d = Date.parse("yyyyMMdd",str[i])
            maxDate = maxDate.getTime()>=d.getTime()?maxDate:d
        }
        return maxDate
    }

    static Date getMinDate(String dates){
        if(!dates){
            return null
        }
        String[] str = dates.split(",")
        Date minDate = Date.parse("yyyyMMdd",str[0])
        for(int i=1;i<str.length;i++){
            Date d = Date.parse("yyyyMMdd",str[i])
            minDate = minDate.getTime()<=d.getTime()?minDate:d
        }
        return minDate
    }
}
