<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title></title>
</head>
<body>
میانگین تعداد سفارش در صف قرار گرفته در ثانیه:<%out.write(request.getParameter("meanPutOrder"));%>
<br>
حداکثر تعداد سفارش در صف قرار گرفته در ثانیه:<%out.write(request.getParameter("maxPutOrder"));%>
<br>
حداقل تعداد سفارش درصف قرار گرفته در ثانیه:<%out.write(request.getParameter("minPutOrder"));%>
<br>
میانگین معاملات انجام شده در ثانیه:<%out.write(request.getParameter("meanTrade"));%>
<br>
حداکثر معاملات انجام شده در ثانیه:<%out.write(request.getParameter("maxTrade"));%>
<br>
حداقل معاملات انجام شده در ثانیه:<%out.write(request.getParameter("minTrade"));%>
<br>
حجم کل ریالی معاملات انجام شده:<%out.write(request.getParameter("tradesCost"));%>
<br>
تعداد معاملات انجام شده:<%out.write(request.getParameter("tradeCount"));%>
<br>
تعداد کل سفارش‌ها:<%out.write(request.getParameter("putOrderCount"));%>
</body>
</html>