<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<html>
<head>
    <title></title>
    <link type="text/css" rel="stylesheet" href="<c:url value="css/layout.css"/>">
    <script type="text/javascript" src="<c:url value="js/jquery-1.7.2.min.js"/>"></script>
    <script type='text/javascript' src='<c:url value="js/ajaxfileupload.js"/>'></script>
    <script type='text/javascript' src='<c:url value="js/modal.popup.js"/>'></script>
    <script type='text/javascript' src='<c:url value="js/flotr2.min.js"/>'></script>
    <link rel="stylesheet" type="text/css" href="<c:url value="css/ajaxfileupload.css"/>"/>
    <link rel="stylesheet" type="text/css" href="<c:url value="css/flotr.css"/>"/>
</head>
<body style="width: 100%">
<div style="width:1353px; margin: 0px auto;">
<div id="header">
    <h1><fmt:message key="application.title"/></h1>
    <img src="<c:url value="/img/blogo.jpg"/>" alt="logo">
</div>
<img id="loading" style="display:none;position: fixed;left: 50%;top: 20%" src="<c:url value="img/loading.gif"/>"
     alt="loading">

<div id="control">
    <div class="inputDiv">
        <label for="subscribersFile"><fmt:message key="subscriber_file"/>:</label>
        <button onclick="document.getElementById('subscribersFile').click();return false;">
            <fmt:message key="choose_file"/>
        </button>
        <img class="help" src="<c:url value="/img/help.gif"/>" alt="help" id="subhelp">
        <span id="subscribersFileName" style="float: left;margin-right: 10px;"></span>
        <input id="subscribersFile" name="subscribersFile" type="file" class="fileUpload"
               onchange="fileUpload('<c:url value="index.do"/>','subscribersFile');
                       document.getElementById('subscribersFileName').innerHTML=document.getElementById('subscribersFile').value;
                       return false;"/>
    </div>
    <div class="inputDiv">
        <label for="symbolsFile"><fmt:message key="symbols_file"/>:</label>
        <button onclick="document.getElementById('symbolsFile').click();return false;">
            <fmt:message key="choose_file"/>
        </button>
        <img class="help" src="<c:url value="/img/help.gif"/>" alt="help" id="symhelp">
        <span id="symbolsFileName" style="float: left;margin-right: 10px;"></span>
        <input id="symbolsFile" name="symbolsFile" type="file" class="fileUpload"
               onchange="fileUpload('<c:url value="index.do"/>','symbolsFile');
                    document.getElementById('symbolsFileName').innerHTML=document.getElementById('symbolsFile').value;
                    return false;"/>
    </div>
    <form id="settingsForm" action="">
        <div class="form">
            <div class="inputDiv">
                <label for="preOpeningRunTime"><fmt:message key="preopening_run_time"/>:</label>
                <input id="preOpeningRunTime" name="preOpeningRunTime" type="text" class="integer"/>
                <span style="margin-right: 10px;"><fmt:message key="minute"/></span>
            </div>
            <div class="inputDiv">
                <label for="tradingRunTime"><fmt:message key="trading_run_time"/>:</label>
                <input id="tradingRunTime" name="tradingRunTime" type="text" class="integer"/>
                <span style="margin-right: 10px;"><fmt:message key="minute"/></span>
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
                <label>&nbsp;</label>
                <button><fmt:message key="submit"/></button>
            </div>
        </div>
    </form>
    <hr/>
    <button class="command" onclick="sendCommand('start');startGraph();"><fmt:message key="start_process"/></button>
    <br/>
    <button class="command" onclick="sendCommand('pause');"><fmt:message key="pause_process"/></button>
    <br/>
    <button class="command" onclick="sendCommand('restart');"><fmt:message key="restart_process"/></button>
    <br/>
    <button class="command" onclick="sendCommand('stop');"><fmt:message key="stop_process"/></button>
    <br/>
    <script type="text/javascript">

        $('#settingsForm').submit(function () {
            var values = {};
            $.each($('#settingsForm :input').serializeArray(), function (i, field) {
                if (!$.isNumeric(field.value)) {
                    alert("invalid value: " + field.value + " for field: " + field.name);
                    return false;
                }
                values[field.name] = parseInt(field.value);
            });
            if (values['preOpeningRunTime'] + values['tradingRunTime'] > 60) {
                if (!confirm("Run time is greater than an hour!")) {
                    return false;
                }
            }
            else if (((values['totalBuyOrders']+values['totalSellOrders']) /
                    ((values['preOpeningRunTime'] * 60) + (values['tradingRunTime'] * 60))) > 100000) {
                if (!confirm("Throughput will be more than 100,000 messages/second!")) {
                    return false;
                }
            }
            else if (values['preOpeningOrders'] > 50) {
                if (!confirm("Pre-opening orders more than 50% of total!")) {
                    return false;
                }
            }
            else if (values['matchPercent'] < 30) {
                if (!confirm("Match percent smaller than 30%!")) {
                    return false;
                }
            }

            $.post('<c:url value="index.do"/>', values, function (data) {
                alert(data);
            }, "text");
            return false;
        });
        var stopped=true;
        function sendCommand(command) {
            $.ajax({
                type:"GET",
                dataType:"text",
                url:"<c:url value="index.do?state=true"/>"
            }).done(function (data) {
                var engineState = $.parseJSON(data).state;
                switch (command){
                    case 'start':
                        stopped = false;
                        if(engineState == 'INITIALIZING' || engineState == 'WAITING'){
                            alert("<fmt:message key="settings_not_complete"/>");
                            return false;
                        }
                        break;
                    case 'pause':
                        if(!stopped)
                            togglePauseGraph();
                        else
                            return;
                        break;
                    case 'stop':
                        stopped = true;
                        pauseGraph();
                        break;
                    case 'restart':
                        stopped = false;
                        stopGraph();
                        break;

                }
                //alert(data);
                $.ajax({
                    type:"GET",
                    dataType:"text",
                    url:"<c:url value="command.do?action="/>" + command,
                    beforeSend: function() {
                        $("#loading").show();
                    },
                    complete: function(){
                        $("#loading").hide();
                    },
                    success: function(data) {
                        alert(data);
                    }
                });
            });
        }

        function fileUpload(url, fileElementId) {
            /*
             prepareing ajax file upload
             url: the url of script file handling the uploaded files
             fileElementId: the file type of input element id and it will be the index of  $_FILES Array()
             dataType: it support json, xml
             secureuri:use secure protocol
             success: call back function when the ajax complete
             error: callback function when the ajax failed
             */
            $.ajaxFileUpload
                    (
                            {
                                url:url,
                                secureuri:false,
                                fileElementId:fileElementId,
                                dataType:"text",
                                success:function (data) {
                                    alert(data.replace(/<pre>/g, '').replace(/<\/pre>/g, ''));
                                },
                                error:function (data, status, e) {
                                    alert(e);
                                }
                            }
                    );
            //return false;
        }

        $(document).ready(function () {

            //Change these values to style your modal popup
            var align = 'center';									//Valid values; left, right, center
            var top = 30; 											//Use an integer (in pixels)
            var width = 500; 										//Use an integer (in pixels)
            var padding = 10;										//Use an integer (in pixels)
            var backgroundColor = '#FFFFFF'; 						//Use any hex code
            var borderColor = '#333333'; 							//Use any hex code
            var borderWeight = 4; 									//Use an integer (in pixels)
            var borderRadius = 5; 									//Use an integer (in pixels)
            var fadeOutTime = 300; 									//Use any integer, 0 = no fade
            var disableColor = '#666666'; 							//Use any hex code
            var disableOpacity = 40; 								//Valid range 0-100
            var loadingImage = '<c:url value="/img/loading.gif"/>';		//Use relative path from this page

            //This method initialises the modal popup
            $("#subhelp").click(function () {
                modalPopup(align, top, width, padding, disableColor, disableOpacity, backgroundColor, borderColor, borderWeight, borderRadius, fadeOutTime, '<c:url value="subhelp.jsp"/>', loadingImage);
            });
            $("#symhelp").click(function () {
                modalPopup(align, top, width, padding, disableColor, disableOpacity, backgroundColor, borderColor, borderWeight, borderRadius, fadeOutTime, '<c:url value="symhelp.jsp"/>', loadingImage);
            });

        });

    </script>
</div>
<div id="stats"></div>

<div id="footer" style="float: left;margin-top: 10px;margin-left: 35px;"><fmt:message key="created_by_safa"/></div>
<script type="text/javascript">
    var d1 = [[0,0]],
        d2 = [[0,0]],
        d3 = [[0,0]],
        d4 = [[0,0]],
        options, graph, start, i;
    options = {
        xaxis:{
            min:0,
            max:20
        }
    };
    var container = document.getElementById("stats");

    function drawGraph(opts) {
        var o = Flotr._.extend(Flotr._.clone(options), opts || {});
        return Flotr.draw(container,
                [
                    {label:"<fmt:message key="PutOrderCount"/>", data:d1},
                    {label:"<fmt:message key="TradeCount"/>", data:d2},
                    {label:"<fmt:message key="BuyQueueSize"/>", data:d3},
                    {label:"<fmt:message key="SellQueueSize"/>", data:d4}
                ], o);
    }

    graph = drawGraph();

    //<drag>
    var drag = false;
    function initializeDrag(e) {
        drag = true;
        start = graph.getEventPosition(e);
        Flotr.EventAdapter.observe(document, "mousemove", move);
        Flotr.EventAdapter.observe(document, "mouseup", stopDrag);
    }
    function move(e) {
        var end = graph.getEventPosition(e),
                xaxis = graph.axes.x,
                offset = start.x - end.x;
        graph = drawGraph({
            xaxis:{
                min:xaxis.min + offset,
                max:xaxis.max + offset
            }
        });
        Flotr.EventAdapter.observe(graph.overlay, "mousedown", initializeDrag);
    }
    function stopDrag() {
        drag = false;
        Flotr.EventAdapter.stopObserving(document, "mousemove", move);
    }
    Flotr.EventAdapter.observe(graph.overlay, "mousedown", initializeDrag);
    //</drag>

    var index = 0;
    var paused = false;
    function startGraph() {
        paused = false;
        var graphIntervalHandle = setInterval(function () {
            if(paused)
                return;
            index++;
            $.ajax({
                type:"GET",
                dataType:"text",
                url:"<c:url value="index.do?info=1"/>",
                timeout:900
            }).done(function (data) {
                var info = $.parseJSON(data);
                //alert(info.toString());
                d1.push([index, info.putOrderCount]);
                d2.push([index, info.tradeCount]);
                d3.push([index, info.buyQueueSize]);
                d4.push([index, info.sellQueueSize]);
                var optional = {};
                if (!drag) {
                    optional = {xaxis:{
                        min:index < 20 ? 0 : Math.abs(20 - (index)),
                        max:index < 20 ? 20 : index+1
                    }}
                }
                drawGraph(optional);

            });
        }, 1000);

        var reportIntervalHandle = setInterval(function (){
            $.ajax({
                type:"GET",
                dataType:"text",
                url:"<c:url value="index.do?state=true"/>"
            }).done(function (data) {
                var engineState = $.parseJSON(data).state;
                if(engineState == "FINISHED"){
                    $.ajax({
                        type:"GET",
                        dataType:"text",
                        url:"<c:url value="index.do?report=1"/>",
                        timeout:900
                    }).done(function (data) {
                        paused=true;
                        var info = $.parseJSON(data);
                        var parameters = "?"+"meanPutOrder="+info.meanPutOrder+"&minPutOrder="+info.minPutOrder+"&maxPutOrder="+info.maxPutOrder+"&meanTrade="+info.meanTrade+"&minTrade="+info.minTrade+"&maxTrade="+info.maxTrade+"&tradeCount="+info.tradeCount+"&tradesCost="+info.tradesCost+"&putOrderCount="+info.putOrderCount;
                        var align = 'center';									//Valid values; left, right, center
                        var top = 30; 											//Use an integer (in pixels)
                        var width = 500; 										//Use an integer (in pixels)
                        var padding = 10;										//Use an integer (in pixels)
                        var backgroundColor = '#FFFFFF'; 						//Use any hex code
                        var borderColor = '#333333'; 							//Use any hex code
                        var borderWeight = 4; 									//Use an integer (in pixels)
                        var borderRadius = 5; 									//Use an integer (in pixels)
                        var fadeOutTime = 300; 									//Use any integer, 0 = no fade
                        var disableColor = '#666666'; 							//Use any hex code
                        var disableOpacity = 40; 								//Valid range 0-100
                        var loadingImage = '<c:url value="/img/loading.gif"/>';		//Use relative path from this page

                        modalPopup(align, top, width, padding, disableColor, disableOpacity, backgroundColor, borderColor, borderWeight, borderRadius, fadeOutTime, '<c:url value="report.jsp"/>'+parameters, loadingImage);
                        clearInterval(reportIntervalHandle);
                        clearInterval(graphIntervalHandle);
                    });
                }
            });
        }, 5000);
    }
    function togglePauseGraph(){
        paused=!paused;
    }
    function pauseGraph(){
        paused = false;
    }
    function stopGraph(){
        d1= [[0,0]];
        d2 = [[0,0]];
        d3 = [[0,0]];
        d4 = [[0,0]];
        index = 0;
        paused = true;
        drawGraph();
    }

</script>
</div>
</body>
</html>