package com.bourse.nms.web;

import com.bourse.nms.common.NMSException;
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
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: araz
 * Date: 5/31/12
 * Time: 2:05 PM
 */
public class MainServlet extends HttpServlet{

    private final Logger log = Logger.getLogger(MainServlet.class);

    public static final String PRE_OPENING_RUN_TIME = "preOpeningRunTime";
    public static final String TRADING_RUN_TIME = "tradingRunTime";
    public static final String TOTAL_BUY_ORDERS = "totalBuyOrders";
    public static final String TOTAL_SELL_ORDERS = "totalSellOrders";
    public static final String PRE_OPENING_ORDERS = "preOpeningOrders";
    public static final String MATCH_PERCENT = "matchPercent";
    public static final String SYMBOLS_FILE = "symbolsFile";
    public static final String SUBSCRIBERS_FILE = "subscribersFile";
    public static final String FILE_COLUMN_SEPARATOR = ",";

    private Generator generator;
    public void init(){
        log.info("Main Servlet Init...");
        final ApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
        generator = (Generator) context.getBean("generator");
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/index.jsp");
        dispatcher.forward(req, resp);
    }

    @SuppressWarnings("unchecked")
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        try {
            Map<String, Object> submitData = extractData(req);
            generator.setParameters((Integer)submitData.get(PRE_OPENING_RUN_TIME),
                    (Integer)submitData.get(TRADING_RUN_TIME),
                    (Integer)submitData.get(TOTAL_BUY_ORDERS),
                    (Integer)submitData.get(TOTAL_SELL_ORDERS),
                    (Integer)submitData.get(PRE_OPENING_ORDERS),
                    (Integer)submitData.get(MATCH_PERCENT),
                    (Set<Symbol>)submitData.get(SYMBOLS_FILE),
                    (Set<Subscriber>)submitData.get(SUBSCRIBERS_FILE));
        } catch (FileUploadException e) {
            log.warn("file upload exception!", e);
            throw new ServletException("Cannot parse multipart request.", e);
        } catch (NMSException e){
            log.warn("exception on setParameters", e);
        }

    }

    private Map<String, Object> extractData(HttpServletRequest req) throws FileUploadException, IOException {
        final List<FileItem> items = new ServletFileUpload(new DiskFileItemFactory()).parseRequest(req);
        final Map<String, Object> result = new HashMap<>();
        for (FileItem item : items) {
            if (item.isFormField()) {
                putFormField(result, item);
            } else {
                putFileData(result, item);
            }
        }

        return result;
    }

    private void putFileData(Map<String, Object> result, FileItem item) throws IOException {
        final String fieldName = item.getFieldName();
        final String fileName = FilenameUtils.getName(item.getName());
        final BufferedReader fileReader = new BufferedReader(new InputStreamReader(item.getInputStream()));
        switch (fieldName){
            case SYMBOLS_FILE:
                final Set<Symbol> symbols = new HashSet<>();
                while (fileReader.ready()){
                    final String line = fileReader.readLine();
                    if(StringUtils.isNotEmpty(line)){
                        final String[] lineArr = line.split(FILE_COLUMN_SEPARATOR);
                        if(lineArr.length < 12){
                            log.warn("invalid data line in symbols file: " + line);
                            continue;
                        }
                        symbols.add(new Symbol(lineArr[0], lineArr[1], lineArr[2],
                                Integer.parseInt(lineArr[3]),
                                Integer.parseInt(lineArr[4]),
                                Integer.parseInt(lineArr[5]),
                                Integer.parseInt(lineArr[6]),
                                Integer.parseInt(lineArr[7]),
                                Integer.parseInt(lineArr[8]),
                                Integer.parseInt(lineArr[9]),
                                Integer.parseInt(lineArr[10]),
                                Integer.parseInt(lineArr[11])));
                    }
                }
                result.put(SYMBOLS_FILE, symbols);
                break;
            case SUBSCRIBERS_FILE:
                final Set<Subscriber> subscribers = new HashSet<>();
                while (fileReader.ready()){
                    final String line = fileReader.readLine();
                    if(StringUtils.isNotEmpty(line)){
                        final String[] lineArr = line.split(FILE_COLUMN_SEPARATOR);
                        if(lineArr.length < 3){
                            log.warn("invalid data line in subscribers file: " + line);
                            continue;
                        }
                        subscribers.add(new Subscriber(Integer.parseInt(lineArr[0]),
                                Integer.parseInt(lineArr[1]),
                                Integer.parseInt(lineArr[2])));
                    }
                }
                result.put(SUBSCRIBERS_FILE, subscribers);
                break;
            default:
                log.warn("unknown file upload: " + fieldName + ": " + fileName);
                break;
        }
    }

    private void putFormField(Map<String, Object> result, FileItem item) {
        final String fieldName = item.getFieldName();
        final String fieldValue = item.getString();
        switch (fieldName) {
            case PRE_OPENING_RUN_TIME:
                result.put(PRE_OPENING_RUN_TIME, Integer.parseInt(fieldValue));
                break;
            case TRADING_RUN_TIME:
                result.put(TRADING_RUN_TIME, Integer.parseInt(fieldValue));
                break;
            case TOTAL_BUY_ORDERS:
                result.put(TOTAL_BUY_ORDERS, Integer.parseInt(fieldValue));
                break;
            case TOTAL_SELL_ORDERS:
                result.put(TOTAL_SELL_ORDERS, Integer.parseInt(fieldValue));
                break;
            case PRE_OPENING_ORDERS:
                result.put(PRE_OPENING_ORDERS, Integer.parseInt(fieldValue));
                break;
            case MATCH_PERCENT:
                result.put(MATCH_PERCENT, Integer.parseInt(fieldValue));
                break;
            default:
                log.warn("unknown parameter: " + fieldName + ": " + fieldValue);
                break;
        }
    }
}
