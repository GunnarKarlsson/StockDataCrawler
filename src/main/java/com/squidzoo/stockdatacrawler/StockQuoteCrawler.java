package com.squidzoo.stockdatacrawler;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import javax.swing.table.DefaultTableModel;
import java.io.IOException;

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

    private static final String STOCK_QUOTE_URL = "https://example.com/quote?stockCode=";
    private static final int TIME_OUT_MS = 30000;
    private static final String MARKET_CAP_LABEL = "Mkt Cap";
    private static final String PE_LABEL = "P/E";
    private static final String PB_LABEL = "P/B";
    private static final String YIELD_LABEL = "Yield";
    private static final String TABLE_ELEMENT = "table";
    private static final String SPAN_ELEMENT = "span";
    private static final String TD_ELEMENT = "td";
    private static final String STOCK_TABLE_ID = "div#chartSummaryLeft";
    private static final String COMMA = ",";
    private static final String EMPTY = "";
    private static final String PERCENTAGE = "%";
    private static final String NOT_AVAILABLE = "N/A";
    private static Connection connection;

    private static DefaultTableModel buildTableModel(ResultSet rs)
            throws SQLException {

        ResultSetMetaData metaData = rs.getMetaData();

        // names of columns
        Vector<String> columnNames = new Vector<>();
        int columnCount = metaData.getColumnCount();
        for (int column = 1; column <= columnCount; column++) {
            columnNames.add(metaData.getColumnName(column));
        }

        // data of the table
        Vector<Vector<Object>> data = new Vector<>();
        while (rs.next()) {
            Vector<Object> vector = new Vector<>();
            for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
                vector.add(rs.getObject(columnIndex));
            }
            data.add(vector);
        }

        return new DefaultTableModel(data, columnNames);
    }

    public static DefaultTableModel getDataFromDb(StockDataType type) {

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
            //Do nothing
        }
        return null;
    }

    public interface CrawlCallback {
        void onFinished(int result);
    }

    public void start(int start, int end, CrawlCallback callback) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                connectDb();
                int result = crawl(start, end);
                if (callback != null) {
                    callback.onFinished(result);
                }
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

    private static int crawl(int start, int end) {
        clearDb();
        try {
            for (int k = start; k <= end; k++) {
                Stock stock = new Stock();
                stock.setCode(k);
                URL url = new URL(STOCK_QUOTE_URL + String.valueOf(k));
                Document doc = Jsoup.parse(url, TIME_OUT_MS);

                Elements els = doc.getElementsByClass("qtxt_s_blue_b");
                for (int p = 0; p < els.size(); p++) {
                    if (els.get(p).text().contains("YEAR CHART")) {
                        String text = els.get(p).text();
                        int endIndex = text.indexOf("(");
                        String name = text.substring(0, endIndex);
                        stock.setName(name);
                    }
                }

                Elements elements = doc.select(STOCK_TABLE_ID);
                Elements tables = elements.select(TABLE_ELEMENT);
                Elements tds = tables.select(TD_ELEMENT);
                for (int i = 0; i < tds.size(); i++) {
                    String spanValue = tds.get(i).select(SPAN_ELEMENT).text().trim();
                    int valueIndex = i + 1;
                    if (valueIndex < tds.size() - 1) {
                        stock = setStockAttribute(stock, spanValue, tds, i + 1);
                    }
                }
                if (NOT_AVAILABLE.equals(stock.getMarketCap()) && 0.0 == stock.getPe() && 0.0 == stock.getPb() && 0.0 == stock.getYield()) {
                    continue;
                }
                writeStockToDb(stock);
            }
            try {
                connection.close();
                return 0;
            } catch (SQLException e) {
                e.printStackTrace();
                return -1;
            }
        } catch (MalformedURLException e) {
            return -1;
        } catch (UnknownHostException e) {
            return -1;
        } catch (IOException e) {
            return -1;
        }
    }

    private static void clearDb() {
        try {
            String query = "DELETE FROM stock;";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.executeUpdate();
        } catch (SQLException e) {
            //Do nothing
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
            //System.out.println("stock " + stock.getCode() + " written to db");
        } catch (SQLException e) {
            //Do nothing
        }
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
                    //Do nothing
                }
                stock.setPe(value);
                break;
            case PB_LABEL:
                float valuePb = 0.0F;
                try {
                    valuePb = Float.valueOf(printResult(elements, indexForValue));
                } catch (Exception e) {
                    //Do nothing
                }
                stock.setPb(valuePb);
                break;
            case YIELD_LABEL:
                float valueYield = 0.0F;
                try {
                    valueYield = Float.valueOf(printResult(elements, indexForValue));
                } catch (Exception e) {
                    //Do nothing
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
}

