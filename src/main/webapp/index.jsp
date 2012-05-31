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
<form action="index.do" method="post">
<div>
    <label for="programRunTime"><fmt:message key="program_run_time"/></label>
    <input id="programRunTime" name="programRunTime" type="text"/>
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
    <input type="submit" value="<fmt:message key="submit"/>">
</div>
</form>

</body>
</html>