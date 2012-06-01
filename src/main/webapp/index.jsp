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
    <%--<fmt:setLocale value="fa"/>--%>
</head>
<body style="direction: rtl;">
<form action="index.do" method="post" enctype="multipart/form-data">
<div>
    <label for="preOpeningRunTime"><fmt:message key="preopening_run_time"/></label>
    <input id="preOpeningRunTime" name="preOpeningRunTime" type="text"/>
</div>
<div>
    <label for="tradingRunTime"><fmt:message key="trading_run_time"/></label>
    <input id="tradingRunTime" name="tradingRunTime" type="text"/>
</div>

<div>
    <label for="totalBuyOrders"><fmt:message key="total_buy_orders"/></label>
    <input id="totalBuyOrders" name="totalBuyOrders" type="text"/>
</div>
<div>
    <label for="totalSellOrders"><fmt:message key="total_sell_orders"/></label>
    <input id="totalSellOrders" name="totalSellOrders" type="text"/>
</div>
<div>
    <label for="preOpeningOrders"><fmt:message key="pre_opening_orders"/></label>
    <input id="preOpeningOrders" name="preOpeningOrders" type="text"/>
</div>
<div>
    <label for="matchPercent"><fmt:message key="match_percent"/></label>
    <input id="matchPercent" name="matchPercent" type="text"/>
</div>
<div>
    <label for="subscribersFile"><fmt:message key="subscriber_file"/></label>
    <input id="subscribersFile" name="subscribersFile" type="file"/>
</div>
<div>
    <label for="symbolsFile"><fmt:message key="symbols_file"/></label>
    <input id="symbolsFile" name="symbolsFile" type="file"/>
</div>

<div>
    <input type="submit" value="<fmt:message key="submit"/>">
</div>
</form>

</body>
</html>