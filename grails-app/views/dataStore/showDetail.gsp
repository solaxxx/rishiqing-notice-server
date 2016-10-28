<%
    Map value = request.getAttribute("value");
    List valueList = request.getAttribute("valueList");
    String max = request.getAttribute("max");
    String key = request.getAttribute("key");
%>
<html>
<head>
    <title>查看value详情</title>
    <meta name="layout">
</head>
<body>
<g:if test="${null==value}">
    <div>
        通过key，没有找到对应的value，有可能该列数据已被发送出去。
        <g:link style="text-decoration: none;" controller="dataStore" action="show">返回</g:link>
    </div>
</g:if>
<g:else>
    <div>
        当前仓库数据总长度为：${valueList.size()}&nbsp; &nbsp;
        <g:if test="${!"all".equals(max)}">
            <g:link controller="dataStore" action="getDetail" params="[max: 'all',key: key]" style="text-decoration: none;">查看所有</g:link>
        </g:if>
        <g:else>
            <g:link controller="dataStore" action="getDetail" params="[max: '50',key: key]" style="text-decoration: none;">查看前50</g:link>
        </g:else>
        <g:link style="text-decoration: none;" controller="dataStore" action="show">返回</g:link>
    </div>
    <g:if test="${0==valueList.size()}">
        无详细
    </g:if>
    <g:else>
        <div>详细如下:</div>
        <ul>
            <g:each in="${valueList}">
                <div>
                    日程id："${it.id}"，日程内容："${it.toMap().toString()}"
                </div><br/>
            </g:each>
        </ul>
    </g:else>
</g:else>
</body>
</html>