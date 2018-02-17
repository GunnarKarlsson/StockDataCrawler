package com.squidzoo.quamcrawler;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import com.apple.eawt.Application;

public class CrawlerApp {
    private JButton startCrawlButton;
    private JPanel mainPanel;
    private JProgressBar crawlProgressBar;
    private JTable table;
    private JButton populateTableButton;
    private JButton yieldSortButton;
    private JButton peSortButton;
    private JButton pbSortButton;
    private StockQuoteCrawler stockQuoteCrawler;

    private void setIcon() {
        Application.getApplication().setDockIconImage(new ImageIcon(getClass().getResource("/AppIcon.png")).getImage());
    }

    public CrawlerApp() {
        setIcon();
        JFrame frame = new JFrame("Stock Quote Crawler");
        frame.setContentPane(mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 600);
        frame.setIconImage(new ImageIcon("/AppIcon.png").getImage());
        frame.setVisible(true);
        this.stockQuoteCrawler = new StockQuoteCrawler();

        startCrawlButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                crawlProgressBar.setIndeterminate(true);
                startCrawlButton.setEnabled(false);
                stockQuoteCrawler.start();
            }
        });

        populateTableButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                table.setModel(stockQuoteCrawler.getDataFromDb(StockQuoteCrawler.StockDataType.DEFAULT));
            }
        });
        yieldSortButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                table.setModel(stockQuoteCrawler.getDataFromDb(StockQuoteCrawler.StockDataType.YIELD));
            }
        });
        peSortButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                table.setModel(stockQuoteCrawler.getDataFromDb(StockQuoteCrawler.StockDataType.PE));
            }
        });
        pbSortButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                table.setModel(stockQuoteCrawler.getDataFromDb(StockQuoteCrawler.StockDataType.PB));
            }
        });
    }
}