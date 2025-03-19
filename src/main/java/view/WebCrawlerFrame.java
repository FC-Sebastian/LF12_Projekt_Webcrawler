package view;

import EventListener.GetDataListener;
import controller.WebCrawler;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.HashMap;

public class WebCrawlerFrame extends JFrame {
    private JScrollPane contentPane;
    private InputPane inputPane;

    /**
     * starts crawler-gui
     */
    public static void main(String[] args) {
        new WebCrawlerFrame();
    }

    /**
     * Adding some listeners, setting Element-options
     */
    public WebCrawlerFrame() {
        super("Energiedaten-Crawler");
        setLayout(new BorderLayout());
        setMinimumSize(new Dimension(1280,720));

        contentPane = new JScrollPane();
        inputPane = new InputPane(new GetDataListener() {
            @Override
            public void getData(LocalDate from, LocalDate to) {
                WebCrawler crawler = new WebCrawler();
                HashMap<String, HashMap<String,String>> data = crawler.getEnergyData(from, to);

                getContentPane().remove(contentPane);
                contentPane = EnergyTablePane.getEnergyTable(crawler.getHeaders(), crawler.getDateTimes(), data);
                getContentPane().add(contentPane, BorderLayout.CENTER);
                revalidate();
            }
        });

        add(contentPane, BorderLayout.CENTER);
        add(inputPane, BorderLayout.SOUTH);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
    }
}
