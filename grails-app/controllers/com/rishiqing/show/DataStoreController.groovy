package com.rishiqing.show

import dataStore.DataStore

class DataStoreController {

    def show(){
        String max = params.max

        Map map = DataStore.getInstance().getDataStore()

        List list = []
        map.keySet().each{
            list.add(Date.parse("HH:mm",it))
        }

        Collections.sort(list,new Comparator(){
            @Override
            int compare(Object o1, Object o2) {
                return -o1.getTime().compareTo(o2.getTime())
            }
        });

        List keyList = []
        int num = "all".equals(max)?list.size():(list.size()>20?20:list.size())
        for(int i=0;i<num;i++){
            String time = list.get(i).format("HH:mm")
            keyList.add(time)
        }

        render(view: "showKey",model: [keyList: keyList,map: map,max: max])
    }

    def getDetail(){
        String max = params.max

        Map m = DataStore.getInstance().getDataStore()

        String key = params.key
        if(!key){
            render(error: "没有接收到key值-->'${key}'")
            return
        }

        Map map = m.get(key)
        if(!map){
            render(view: "showDetail",model: [value: null])
            return
        }

        List list = []
        map.values().each{
            list.add(it)
        }

        Collections.sort(list,new Comparator(){
            @Override
            int compare(Object o1, Object o2) {
                return -o1.insertDate?.getTime().compareTo(o2.insertDate?.getTime())
            }
        });

        List valueList = []
        int num = "all".equals(max)?list.size():(list.size()>50?50:list.size())
        for(int i=0;i<num;i++){
            def todo = list.get(i)
            valueList.add(todo)
        }

        render(view: "showDetail",model: [value:[:], valueList: valueList, max: max,key: key])
    }
}
