<%
    List keyList = request.getAttribute("keyList");
    Map map = request.getAttribute("map");
    String max = request.getAttribute("max");
%>
<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<html>
<head>
    <title>推送仓库查看</title>
    <meta name="layout">
</head>
<body>
    <div>当前仓库数据总长度为：${map.size()}&nbsp; &nbsp;
        <g:if test="${!"all".equals(max)}">
            <g:link controller="dataStore" action="show" params="[max: 'all']" style="text-decoration: none;">查看所有</g:link>
        </g:if>
        <g:else>
            <g:link controller="dataStore" action="show" params="[max: '20']" style="text-decoration: none;">查看前20</g:link>
        </g:else>
    </div>
    <g:if test="${0==map.size()}">
        无详细
    </g:if>
    <g:else>
        <div>详细如下:</div>
        <ul>
            <g:each in="${keyList}">
                <div>
                    key："${it}"，value的长度为：${map.get(it)?map.get(it).keySet().size():0}
                    <g:link controller="dataStore" action="getDetail" params="[key: it]" style="text-decoration: none;">查看详情</g:link>
                </div>
            </g:each>
        </ul>
    </g:else>
</body>
</html>