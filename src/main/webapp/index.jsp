<%--
  Created by IntelliJ IDEA.
  User: araz
  Date: 5/31/12
  Time: 6:12 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<html>
<head>
    <title></title>
    <link type="text/css" rel="stylesheet" href="<c:url value="css/layout.css"/>">
    <script type="text/javascript" src="<c:url value="js/jquery-1.7.2.min.js"/>"></script>
</head>
<body>
<div id="control">
<form action="index.do" method="post" enctype="multipart/form-data">
    <div class="form">
        <div class="inputDiv">
            <label for="preOpeningRunTime"><fmt:message key="preopening_run_time"/>:</label>
            <input id="preOpeningRunTime" name="preOpeningRunTime" type="text" class="integer"/>
        </div>
        <div class="inputDiv">
            <label for="tradingRunTime"><fmt:message key="trading_run_time"/>:</label>
            <input id="tradingRunTime" name="tradingRunTime" type="text" class="integer"/>
        </div>

        <div class="inputDiv">
            <label for="totalBuyOrders"><fmt:message key="total_buy_orders"/>:</label>
            <input id="totalBuyOrders" name="totalBuyOrders" type="text" class="integer"/>
        </div>
        <div class="inputDiv">
            <label for="totalSellOrders"><fmt:message key="total_sell_orders"/>:</label>
            <input id="totalSellOrders" name="totalSellOrders" type="text" class="integer"/>
        </div>
        <div class="inputDiv">
            <label for="preOpeningOrders"><fmt:message key="pre_opening_orders"/>:</label>
            <input id="preOpeningOrders" name="preOpeningOrders" type="text" class="integer"/>
        </div>
        <div class="inputDiv">
            <label for="matchPercent"><fmt:message key="match_percent"/>:</label>
            <input id="matchPercent" name="matchPercent" type="text" class="integer"/>
        </div>
        <div class="inputDiv">
            <label for="subscribersFile"><fmt:message key="subscriber_file"/>:</label>
            <input type = "button" value="<fmt:message key="choose_file"/>"
                   onclick ="document.getElementById('subscribersFile').click();">
            <span id="subscribersFileName" style="float: left;"></span>
            <input id="subscribersFile" name="subscribersFile" type="file" class="fileUpload"
                   onchange="document.getElementById('subscribersFileName').innerHTML=document.getElementById('subscribersFile').value;"/>
        </div>
        <div class="inputDiv">
            <label for="symbolsFile"><fmt:message key="symbols_file"/>:</label>
            <input type = "button" value="<fmt:message key="choose_file"/>"
                   onclick ="document.getElementById('symbolsFile').click();">
            <span id="symbolsFileName" style="float: left;"></span>
            <input id="symbolsFile" name="subscribersFile" type="file" class="fileUpload"
                   onchange="document.getElementById('symbolsFileName').innerHTML=document.getElementById('symbolsFile').value;"/>
        </div>

        <div  class="inputDiv">
            <label>&nbsp;</label>
            <input type="submit" value="<fmt:message key="submit"/>">
        </div>
    </div>
</form>
    <button class="command" onclick="sendCommand('start');"><fmt:message key="start_process"/></button><br/>
    <button class="command" onclick="sendCommand('pause');"><fmt:message key="pause_process"/></button><br/>
    <button class="command" onclick="sendCommand('restart');"><fmt:message key="restart_process"/></button><br/>
    <button class="command" onclick="sendCommand('stop');"><fmt:message key="stop_process"/></button><br/>
    <script type="text/javascript">
        function sendCommand(command){
            $.ajax({
                type: "GET",
                dataType: "text",
                url: "<c:url value="command.do?action="/>" + command
            }).done(function(data) {
                alert(data);
            });
        }
    </script>
</div>
</body>
</html>