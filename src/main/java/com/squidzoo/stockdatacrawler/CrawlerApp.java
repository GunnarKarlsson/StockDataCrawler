package com.squidzoo.stockdatacrawler;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import com.apple.eawt.Application;

public class CrawlerApp {
    private JButton startCrawlButton;
    private JPanel mainPanel;
    private JTable table;
    private JButton populateTableButton;
    private JButton yieldButton;
    private JButton defaultButton;
    private JButton peButton;
    private JButton pbButton;
    private JTextField startStockCodeTextField;
    private JTextField endStockCodeTextField;
    private StockQuoteCrawler stockQuoteCrawler;

    private void setIcon() {
        Application.getApplication().setDockIconImage(new ImageIcon(getClass().getResource("/AppIcon.png")).getImage());
    }

    public CrawlerApp() {
        setIcon();
        JFrame frame = new JFrame("Stock Quote Crawler");
        frame.setContentPane(mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setIconImage(new ImageIcon("/AppIcon.png").getImage());
        frame.setVisible(true);
        this.stockQuoteCrawler = new StockQuoteCrawler();

        startCrawlButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                startCrawlButton.setEnabled(false);
                startCrawlButton.setText("Crawling in process...");

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            int startStockCode = Integer.valueOf(startStockCodeTextField.getText());
                            int endStockCode = Integer.valueOf(endStockCodeTextField.getText());
                            stockQuoteCrawler.start(startStockCode, endStockCode, new StockQuoteCrawler.CrawlCallback() {
                                @Override
                                public void onFinished(int result) {
                                    startCrawlButton.setEnabled(true);
                                    String message = result == -1 ? "Fail" : "Success";
                                    startCrawlButton.setText("Crawl Finished: " + message + ". Click to crawl again.");
                                    table.setModel(stockQuoteCrawler.getDataFromDb(StockQuoteCrawler.StockDataType.DEFAULT));
                                }
                            });
                        } catch (Exception e) {
                            startCrawlButton.setText("Error. Click to crawl again");
                        }
                    }
                }).start();
            }
        });

        populateTableButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                table.setModel(stockQuoteCrawler.getDataFromDb(StockQuoteCrawler.StockDataType.DEFAULT));
            }
        });
        yieldButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                table.setModel(stockQuoteCrawler.getDataFromDb(StockQuoteCrawler.StockDataType.YIELD));
            }
        });
        peButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                table.setModel(stockQuoteCrawler.getDataFromDb(StockQuoteCrawler.StockDataType.PE));
            }
        });
        pbButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                table.setModel(stockQuoteCrawler.getDataFromDb(StockQuoteCrawler.StockDataType.PB));
            }
        });
        defaultButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                table.setModel(stockQuoteCrawler.getDataFromDb(StockQuoteCrawler.StockDataType.DEFAULT));
            }
        });
    }
}