package com.squidzoo.quamcrawler;


import com.sun.xml.internal.ws.api.streaming.XMLStreamReaderFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import javax.swing.table.DefaultTableModel;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.sql.*;
import java.util.Vector;


public class StockQuoteCrawler {

    public enum StockDataType {
        DEFAULT,
        YIELD,
        PE,
        PB
    }

    private static final int MIN_STOCK_CODE = 1;
    private static final int MAX_STOCK_CODE = 1000;
    private static final String FILE_NAME = "crawled_share_prices_"+ MIN_STOCK_CODE + "_to_" + MAX_STOCK_CODE + "_at_" + System.currentTimeMillis();
    private static final String QUAM_URL = "http://www.quamnet.com/Quote.action?request_locale=en_US&stockCode=";
    private static final int TIME_OUT_MS = 30000;
    private static final String MARKET_CAP_LABEL = "Mkt Cap";
    private static final String PE_LABEL = "P/E";
    private static final String PB_LABEL = "P/B";
    private static final String YIELD_LABEL = "Yield";
    private static final String TABLE_ELEMENT = "table";
    private static final String SPAN_ELEMENT = "span";
    private static final String TD_ELEMENT = "td";
    private static final String UNUSED_SPAN = "unused_span";
    private static final String QUAM_TABLE_ID = "div#chartSummaryLeft";
    private static final String COMMA = ",";
    private static final String NEW_LINE = "\n";
    private static final String EMPTY = "";
    private static final String PERCENTAGE = "%";
    private static Connection connection;

    private static DefaultTableModel buildTableModel(ResultSet rs)
            throws SQLException {

        ResultSetMetaData metaData = rs.getMetaData();

        // names of columns
        Vector<String> columnNames = new Vector<String>();
        int columnCount = metaData.getColumnCount();
        for (int column = 1; column <= columnCount; column++) {
            columnNames.add(metaData.getColumnName(column));
        }

        // data of the table
        Vector<Vector<Object>> data = new Vector<Vector<Object>>();
        while (rs.next()) {
            Vector<Object> vector = new Vector<Object>();
            for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
                vector.add(rs.getObject(columnIndex));
            }
            data.add(vector);
        }

        return new DefaultTableModel(data, columnNames);
    }

    public static DefaultTableModel getDataFromDb(StockDataType type){

        String query = "SELECT * FROM stock";
        switch (type) {
            case YIELD:
                query += " WHERE yield != 'N/A' ORDER BY yield desc";
                break;
            case PE:
                query += " WHERE pe != 'N/A' ORDER BY pe asc";
                break;
            case PB:
                query += " WHERE pb != 'N/A' ORDER BY pb asc";
                break;
            default:
                break;
        }

        try {
            connectDb();
            PreparedStatement ps = connection.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            return buildTableModel(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void start() {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                connectDb();
                crawl();
            }
        };
        r.run();
    }

    private static void connectDb() {
        connection = DbConnectionConfig.getConnection();
        if (connection != null) {
            System.out.println("Connection established to db");
        } else {
            System.out.println("No db connection");
            return;
        }
    }

    private static void crawl() {
        clearDb();
        System.out.println("Starting Crawler!");
        try {
            String result = createColumnNamesToFile();
            for (int k = MIN_STOCK_CODE; k <= MAX_STOCK_CODE; k++) {
                Stock stock = new Stock();
                stock.setCode(k);
                System.out.println("Crawling stock code: " + k);
                URL url = new URL(QUAM_URL + String.valueOf(k));
                Document doc = Jsoup.parse(url, TIME_OUT_MS);
                Elements elements = doc.select(QUAM_TABLE_ID);
                Elements tables = elements.select(TABLE_ELEMENT);
                Elements tds = tables.select(TD_ELEMENT);
                String subResult = String.valueOf(k) + COMMA;
                for (int i = 0; i < tds.size(); i++) {
                    String spanValue = tds.get(i).select(SPAN_ELEMENT).text().trim();
                    //System.out.println(spanValue);
                    int valueIndex = i + 1;
                    if (valueIndex < tds.size() - 1) {
                        stock = setStockAttribute(stock, spanValue, tds, i + 1);
                        String temp = handleSpan(spanValue, tds, i + 1);
                        if (temp != UNUSED_SPAN) {
                            subResult += temp;
                        }
                    }
                }
                System.out.print(stock.toString());
                writeStockToDb(stock);
                if (!subResult.endsWith(NEW_LINE)) {
                    subResult += NEW_LINE;
                }
                result += subResult;
            }
            //System.out.println(result);
            //createCsvFile(result);
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void clearDb() {
        try {
            String query = "DELETE FROM stock;";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void writeStockToDb(Stock stock) {
        try {
            String query = "INSERT iNTO stock (code, name, marketcap, pe, pb, yield) VALUES (?,?,?,?,?,?);";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, stock.getCode());
            statement.setString(2, stock.getName());
            statement.setString(3, stock.getMarketCap());
            statement.setFloat(4, stock.getPe());
            statement.setFloat(5, stock.getPb());
            statement.setFloat(6, stock.getYield());
            statement.executeUpdate();
            System.out.println("stock " + stock.getCode() +  " written to db");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static String handleSpan(String spanValue, Elements elements, int indexForValue) {
        String result = UNUSED_SPAN;
        switch (spanValue) {
            case MARKET_CAP_LABEL:
                result = printResult(elements, indexForValue);
                result += COMMA;
                break;
            case PE_LABEL:
                result = printResult(elements, indexForValue);
                result += COMMA;
                break;
            case PB_LABEL:
                result = printResult(elements, indexForValue);
                result += COMMA;
                break;
            case YIELD_LABEL:
                result = printResult(elements, indexForValue);
                result += NEW_LINE;
                break;
            default:
                break;
        }
        return result;
    }

    private static Stock setStockAttribute(Stock stock, String spanValue, Elements elements, int indexForValue) {
        switch (spanValue) {
            case MARKET_CAP_LABEL:
                stock.setMarketCap(printResult(elements, indexForValue));
                break;
            case PE_LABEL:
                float value = 0.0F;
                try {
                    value = Float.valueOf(printResult(elements, indexForValue));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                stock.setPe(value);
                break;
            case PB_LABEL:
                float valuePb = 0.0F;
                try {
                    valuePb = Float.valueOf(printResult(elements, indexForValue));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                stock.setPb(valuePb);
                break;
            case YIELD_LABEL:
                float valueYield = 0.0F;
                try {
                    valueYield = Float.valueOf(printResult(elements, indexForValue));
                } catch (Exception e) {
                    e.printStackTrace();;
                }
                stock.setYield(valueYield);
                break;
            default:
                break;
        }
        return stock;

    }

    private static String printResult(Elements elements, int indexForValue) {
        String temp = elements.get(indexForValue).select(SPAN_ELEMENT).text();
        temp = temp.replace(COMMA, EMPTY);
        temp = temp.replace(PERCENTAGE, EMPTY);
        return temp;
    }

    private static String createColumnNamesToFile() {
        StringBuilder sb = new StringBuilder();
        sb.append("Stock code");
        sb.append(COMMA);
        sb.append("Market Cap");
        sb.append(COMMA);
        sb.append("P/E");
        sb.append(COMMA);
        sb.append("P/B");
        sb.append(COMMA);
        sb.append("Yield");
        sb.append(NEW_LINE);
        return sb.toString();
    }
}

