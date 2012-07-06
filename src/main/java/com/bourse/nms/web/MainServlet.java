package com.bourse.nms.web;

import com.bourse.nms.common.NMSException;
import com.bourse.nms.engine.Engine;
import com.bourse.nms.entity.Settings;
import com.bourse.nms.entity.Subscriber;
import com.bourse.nms.entity.Symbol;
import com.bourse.nms.generator.Generator;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import org.apache.tomcat.util.http.fileupload.FileItem;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.apache.tomcat.util.http.fileupload.disk.DiskFileItemFactory;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * main servlet of the application
 */
public class MainServlet extends HttpServlet {

    private final Logger log = Logger.getLogger(MainServlet.class);

    public static final String PRE_OPENING_RUN_TIME = "preOpeningRunTime";
    public static final String TRADING_RUN_TIME = "tradingRunTime";
    public static final String TOTAL_BUY_ORDERS = "totalBuyOrders";
    public static final String TOTAL_SELL_ORDERS = "totalSellOrders";
    public static final String PRE_OPENING_ORDERS = "preOpeningOrders";
    public static final String MATCH_PERCENT = "matchPercent";
    public static final String SYMBOLS_FILE = "symbolsFile";
    public static final String SUBSCRIBERS_FILE = "subscribersFile";
    public static final String FILE_COLUMN_SEPARATOR = ";";

    private Generator generator;
    private Settings settings;
    private Engine engine;

    public void init() {
        log.debug("Main Servlet Init...");
        final ApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
        generator = (Generator) context.getBean("generator");
        settings = (Settings) context.getBean("settings");
        engine = (Engine) context.getBean("engine");
        settings.setStatus(Settings.EngineStatus.WAITING);
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        if(req.getParameter("info") != null){
            resp.setContentType("application/json");
            resp.getWriter().write("{" +
                    "\"putOrderCount\":" + engine.getPutOrderCount() + "," +
                    "\"tradeCount\":" + engine.getTradeCount() + ","+
                    "\"buyQueueSize\":" + engine.getBuyQueueSize() + "," +
                    "\"sellQueueSize\":" + engine.getSellQueueSize() +
                    "}");
            return;
        }
        else if(req.getParameter("state") != null){
            resp.setContentType("application/json");
            resp.getWriter().write("{" +
                    "\"state\":\"" + settings.getStatus().name() + "\""+
                    "}");
            return;
        }else if(req.getParameter("report") != null){
            resp.setContentType("application/json");
            resp.getWriter().write("{" +
                    "\"meanPutOrder\":\"" + engine.getMeanPutOrder() + "\", "+
                    "\"minPutOrder\":\"" + engine.getMinPutOrder() + "\", "+
                    "\"maxPutOrder\":\"" + engine.getMaxPutOrder() + "\", "+
                    "\"meanTrade\":\"" + engine.getMeanTrade() + "\", "+
                    "\"minTrade\":\"" + engine.getMinTrade() + "\", "+
                    "\"maxTrade\":\"" + engine.getMaxTrade() + "\", "+
                    "\"tradeCount\":\"" + engine.getTradeCount() + "\", "+
                    "\"tradesCost\":\"" + engine.getTradesCost() + "\", "+
                    "\"putOrderCount\":\"" + engine.getPutOrderCount() + "\""+
                    "}");
            return;

        }

        RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/index.jsp");
        dispatcher.forward(req, resp);
    }

    @SuppressWarnings("unchecked")
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            if (req.getContentType().contains("multipart/form-data")) {
                final List<FileItem> items = new ServletFileUpload(new DiskFileItemFactory()).parseRequest(req);
                for (FileItem item : items) {
                    putFileData(item);
                }
            } else {
                for (String fieldName : req.getParameterMap().keySet()) {
                    putFormField(fieldName, req.getParameter(fieldName));
                }
                try {
                    generator.setParameters(settings.getPreOpeningTime(),
                            settings.getTradingTime(),
                            settings.getBuyOrdersCount(),
                            settings.getSellOrdersCount(),
                            (int) ((float) ((settings.getPreOpeningOrdersPercent())) / 100 * settings.getBuyOrdersCount()),
                            (int) ((float) ((settings.getPreOpeningOrdersPercent())) / 100 * settings.getSellOrdersCount()),
                            settings.getMatchPercent(),
                            settings.getSymbols(),
                            settings.getCustomers());
                } catch (NMSException e) {
                    log.warn("exception on set parameters", e);
                    resp.getWriter().write(new AjaxResponse(e).toString());
                }
            }

            resp.getWriter().write(new AjaxResponse(0, "OK").toString());
        } catch (FileUploadException e) {
            log.warn("file upload exception!", e);
            resp.getWriter().write(new AjaxResponse(new NMSException(NMSException.ErrorCode.FILE_UPLOAD_EXCEPTION, "file upload failed")).toString());
            throw new ServletException("Cannot parse multipart request.", e);
        } catch (NMSException e) {
            log.warn("settings exception!", e);
            resp.getWriter().write(new AjaxResponse(e).toString());
            throw new ServletException("InternalServerError.", e);
        }

    }


    private void putFileData(FileItem item) throws IOException, NMSException {
        final String fieldName = item.getFieldName();
        final String fileName = FilenameUtils.getName(item.getName());
        final BufferedReader fileReader = new BufferedReader(new InputStreamReader(item.getInputStream()));
        switch (fieldName) {
            case SYMBOLS_FILE:
                final Set<Symbol> symbols = new HashSet<>();
                final AtomicInteger symbolsLineCounter = new AtomicInteger(0);
                while (fileReader.ready()) {
                    final String line = fileReader.readLine();
                    if (StringUtils.isNotEmpty(line)) {
                        symbolsLineCounter.incrementAndGet();
                        final String[] lineArr = line.split(FILE_COLUMN_SEPARATOR);
                        if (lineArr.length < 12) {
                            log.warn("invalid data line in symbols file: " + line);
                            throw new NMSException(NMSException.ErrorCode.INVALID_SYMBOLS_FILE, "invalid data line in symbols file. line: " + symbolsLineCounter.get() + " - "+ line);
                        }
                        try{
                            symbols.add(new Symbol(Integer.parseInt(lineArr[0]), lineArr[1], lineArr[2],
                                    Integer.parseInt(lineArr[3]),
                                    Integer.parseInt(lineArr[4]),
                                    Integer.parseInt(lineArr[5]),
                                    Integer.parseInt(lineArr[6]),
                                    Integer.parseInt(lineArr[7]),
                                    Integer.parseInt(lineArr[8]),
                                    Integer.parseInt(lineArr[9]),
                                    Integer.parseInt(lineArr[10]),
                                    Integer.parseInt(lineArr[11])));
                        }catch (Throwable t){
                            throw new NMSException(NMSException.ErrorCode.INVALID_SYMBOLS_FILE, "invalid data line in symbols file. line: " + symbolsLineCounter.get() + " - "+ line);
                        }
                    }
                }
                settings.setSymbols(symbols);
                break;
            case SUBSCRIBERS_FILE:
                final Set<Subscriber> subscribers = new HashSet<>();
                final AtomicInteger subscribersLineCounter = new AtomicInteger(0);
                while (fileReader.ready()) {
                    final String line = fileReader.readLine();
                    if (StringUtils.isNotEmpty(line)) {
                        subscribersLineCounter.incrementAndGet();
                        final String[] lineArr = line.split(FILE_COLUMN_SEPARATOR);
                        if (lineArr.length < 3) {
                            log.warn("invalid data line in subscribers file: " + line);
                            throw new NMSException(NMSException.ErrorCode.INVALID_SYMBOLS_FILE, "invalid data line in subscribers file. line: " + subscribersLineCounter.get() + " - "+ line);
                        }
                        try{
                            subscribers.add(new Subscriber(Integer.parseInt(lineArr[0]),
                                    Integer.parseInt(lineArr[1]),
                                    Integer.parseInt(lineArr[2])));
                        }catch (Throwable t){
                            throw new NMSException(NMSException.ErrorCode.INVALID_SYMBOLS_FILE, "invalid data line in subscribers file. line: " + subscribersLineCounter.get() + " - "+ line);
                        }
                    }
                }
                settings.setCustomers(subscribers);
                break;
            default:
                log.warn("unknown file upload: " + fieldName + ": " + fileName);
                break;
        }
    }

    private void putFormField(String fieldName, String fieldValue) throws NMSException {
        try{
            switch (fieldName) {
                case PRE_OPENING_RUN_TIME:
                    settings.setPreOpeningTime(Integer.parseInt(fieldValue));
                    break;
                case TRADING_RUN_TIME:
                    settings.setTradingTime(Integer.parseInt(fieldValue));
                    break;
                case TOTAL_BUY_ORDERS:
                    settings.setBuyOrdersCount(Integer.parseInt(fieldValue));
                    break;
                case TOTAL_SELL_ORDERS:
                    settings.setSellOrdersCount(Integer.parseInt(fieldValue));
                    break;
                case PRE_OPENING_ORDERS:
                    settings.setPreOpeningOrdersPercent(Integer.parseInt(fieldValue));
                    break;
                case MATCH_PERCENT:
                    settings.setMatchPercent(Integer.parseInt(fieldValue));
                    break;
                default:
                    log.warn("unknown parameter: " + fieldName + ": " + fieldValue);
                    break;
            }
        }catch (Throwable t){
            log.warn("exception on settings", t);
            throw new NMSException(NMSException.ErrorCode.SETTINGS_ERROR, "settings error: " + fieldName + ":" + fieldValue);
        }
    }
}
