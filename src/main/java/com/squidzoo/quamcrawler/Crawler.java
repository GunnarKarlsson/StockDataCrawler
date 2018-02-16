package com.squidzoo.quamcrawler;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

public class Crawler {

    private static final int MIN_STOCK_CODE = 1;
    private static final int MAX_STOCK_CODE = 5;
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

    public static void main(String[] args) {
        System.out.println("Starting Crawler!");
        try {
            String result = createColumnNamesToFile();
            for (int k = MIN_STOCK_CODE; k <= MAX_STOCK_CODE; k++) {
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
                        String temp = handleSpan(spanValue, tds, i + 1);
                        if (temp != UNUSED_SPAN) {
                            subResult += temp;
                        }
                    }
                }
                if (!subResult.endsWith(NEW_LINE)) {
                    subResult += NEW_LINE;
                }
                result += subResult;
            }
            //System.out.println(result);
            createCsvFile(result);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
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

    private static String printResult(Elements elements, int indexForValue) {
        String temp = elements.get(indexForValue).select(SPAN_ELEMENT).text();
        temp = temp.replace(COMMA,EMPTY);
        temp = temp.replace(PERCENTAGE,EMPTY);
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

    private static void createCsvFile(String fileContent) {
        try {
            PrintWriter pw = new PrintWriter(new File(FILE_NAME + ".csv"));
            pw.write(fileContent);
            pw.close();
            System.out.println("File created!");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
