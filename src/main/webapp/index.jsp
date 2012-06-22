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
    <script type='text/javascript' src='<c:url value="js/ajaxfileupload.js"/>'></script>
    <script type='text/javascript' src='<c:url value="js/modal.popup.js"/>'></script>
    <script type='text/javascript' src='<c:url value="js/flotr2.min.js"/>'></script>
    <link rel="stylesheet" type="text/css" href="<c:url value="css/ajaxfileupload.css"/>"/>
    <link rel="stylesheet" type="text/css" href="<c:url value="css/flotr.css"/>"/>
</head>
<body>
<div id="header">
<h1><fmt:message key="application.title"/></h1>
<img src="<c:url value="/img/blogo.jpg"/>" alt="logo">
</div>
<img id="loading" style="display:none;position: fixed;left: 50%;top: 20%" src="<c:url value="img/loading.gif"/>" alt="loading">
<div id="control">
    <div class="inputDiv">
        <label for="subscribersFile"><fmt:message key="subscriber_file"/>:</label>
        <button onclick ="document.getElementById('subscribersFile').click();return false;">
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
        <button onclick ="document.getElementById('symbolsFile').click();return false;">
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

            <div  class="inputDiv">
                <label>&nbsp;</label>
                <button><fmt:message key="submit"/></button>
            </div>
        </div>
    </form>
    <hr/>
    <button class="command" onclick="sendCommand('start');"><fmt:message key="start_process"/></button><br/>
    <button class="command" onclick="sendCommand('pause');"><fmt:message key="pause_process"/></button><br/>
    <button class="command" onclick="sendCommand('restart');"><fmt:message key="restart_process"/></button><br/>
    <button class="command" onclick="sendCommand('stop');"><fmt:message key="stop_process"/></button><br/>
    <script type="text/javascript">

        $('#settingsForm').submit(function() {
            var values = {};
            $.each($('#settingsForm :input').serializeArray(), function(i, field) {
                values[field.name] = field.value;
            });
            $.post('<c:url value="index.do"/>', values, function(data){alert(data);}, "text");
            return false;
        });

        function sendCommand(command){
            $.ajax({
                type: "GET",
                dataType: "text",
                url: "<c:url value="command.do?action="/>" + command
            }).done(function(data) {
                alert(data);
            });
        }

        function fileUpload(url, fileElementId, successFunction)
        {
            $("#loading")
                .ajaxStart(function(){
                    $(this).center();
                    $(this).show();
                })
                .ajaxComplete(function(){
                    $(this).hide();
                });

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
                    //dataType: 'json',
                    success: function(data){
                        alert(data.replace(/<pre>/g,'').replace(/<\/pre>/g,''));
                    },
                    error: function (data, status, e)
                    {
                        alert(e);
                    }
                }
            );
            return false;
        }

        $(document).ready(function() {

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
            $("#subhelp").click(function() {
                modalPopup(align, top, width, padding, disableColor, disableOpacity, backgroundColor, borderColor, borderWeight, borderRadius, fadeOutTime, '<c:url value="subhelp.jsp"/>', loadingImage);
            });
            $("#symhelp").click(function() {
                modalPopup(align, top, width, padding, disableColor, disableOpacity, backgroundColor, borderColor, borderWeight, borderRadius, fadeOutTime, '<c:url value="symhelp.jsp"/>', loadingImage);
            });

        });

    </script>
</div>
<div id="stats"></div>
<script type="text/javascript">
    function drawGraph(container, d1, d2, d3, d4) {

        var options,
            graph,
            start,
            i;

        for (i = 0; i < 4000; i += 1) {
            d1.push([i, Math.random()]);
            d2.push([i, Math.random()]);
            d3.push([i, Math.random()]);
        }

        options = {
            xaxis: {min: 0, max: 10, minorTickFreq: 4},
            grid: {minorVerticalLines: true},
            title : 'Time',
            spreadsheet : {show : true,tickFormatter : function (e) { return e+''; }}
        };

        // Draw graph with default options, overwriting with passed options
        function drawGraph (opts) {

            // Clone the options, so the 'options' variable always keeps intact.
            var o = Flotr._.extend(Flotr._.clone(options), opts || {});

            // Return a new graph.
            return Flotr.draw(
                    container,
                    [
                        { data : d1, label : 'Serie 1' },
                        { data : d2, label : 'Serie 2' },
                        { data : d3, label : 'Serie 3' },
                        { data : d4, label : 'Serie 4' }
                    ],
                    o
            );
        }

        graph = drawGraph();

        function initializeDrag (e) {
            start = graph.getEventPosition(e);
            Flotr.EventAdapter.observe(document, 'mousemove', move);
            Flotr.EventAdapter.observe(document, 'mouseup', stopDrag);
        }

        function move (e) {
            var
                    end     = graph.getEventPosition(e),
                    xaxis   = graph.axes.x,
                    offset  = start.x - end.x;

            graph = drawGraph({
                xaxis : {
                    min : xaxis.min + offset,
                    max : xaxis.max + offset
                }
            });
            // @todo: refector initEvents in order not to remove other observed events
            Flotr.EventAdapter.observe(graph.overlay, 'mousedown', initializeDrag);
        }

        function stopDrag () {
            Flotr.EventAdapter.stopObserving(document, 'mousemove', move);
        }

        Flotr.EventAdapter.observe(graph.overlay, 'mousedown', initializeDrag);
    }

    setInterval(function(){
        $.ajax({
            type: "GET",
            dataType: "text",
            url: "<c:url value="index.do?info=1"/>"
        }).done(function(data) {
            alert(data);
        });
    }, 1000);
    drawGraph(document.getElementById("stats"));
</script>
</body>
</html>