package view;

import com.github.lgooddatepicker.components.DatePicker;
import controller.WebCrawler;
import model.EnergyDataset;

import javax.swing.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class WebCrawlUI extends JDialog {
    private JPanel contentPane;
    private JButton submitButton;
    private DatePicker datePickerFrom;
    private DatePicker datePickerTo;
    private JTextArea output;

    /**
     * Adding some listeners, setting Element-options
     */
    public WebCrawlUI() {
        setContentPane(contentPane);

        datePickerFrom.addDateChangeListener(dateChangeEvent -> System.out.println("from changed to " + datePickerFrom.getDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))));
        datePickerTo.addDateChangeListener(dateChangeEvent -> System.out.println("to changed to " + datePickerTo.getDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))));

        output.setEditable(false);
        submitButton.setText("GIB MIR DATEN!");
        submitButton.addActionListener(e -> getData());
        setModal(true);
    }

    /**
     * opens gui
     */
    public static void main(String[] args) {
        WebCrawlUI dialog = new WebCrawlUI();
        dialog.pack();
        dialog.setVisible(true);

        System.exit(0);
    }

    /**
     * Gets energy-data from webcrawler and displays results in output
     */
    private void getData() {
        WebCrawler crawler = new WebCrawler();
        LocalDate from = datePickerFrom.getDate();
        LocalDate to = datePickerTo.getDate();

        List<EnergyDataset> data = crawler.getEnergyData(from, to);

        output.setText("");
        data.forEach(line -> output.append(line + "\n"));
    }
}
