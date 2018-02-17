package com.squidzoo.quamcrawler;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CrawlerApp {
    private JButton startCrawlButton;
    private JPanel mainPanel;
    private JTextField textField1;
    private StockQuoteCrawler stockQuoteCrawler;

    public CrawlerApp() {
        JFrame frame = new JFrame("Stock Quote Crawler");
        frame.setContentPane(mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        this.stockQuoteCrawler = new StockQuoteCrawler();

        startCrawlButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startCrawlButton.setEnabled(false);
                stockQuoteCrawler.start();
            }
        });
    }
}
