import com.github.lgooddatepicker.components.DatePicker;

import javax.swing.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

public class WebCrawlUI extends JDialog {
    private JPanel contentPane;
    private JButton submitButton;
    private DatePicker datePickerFrom;
    private DatePicker datePickerTo;
    private JTextArea output;

    public WebCrawlUI() {
        setContentPane(contentPane);

        datePickerFrom.addDateChangeListener(dateChangeEvent -> System.out.println("from changed to " + datePickerFrom.getDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))));
        datePickerTo.addDateChangeListener(dateChangeEvent -> System.out.println("to changed to " + datePickerTo.getDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))));

        output.setEditable(false);
        submitButton.setText("GIB MIR DATEN!");
        submitButton.addActionListener(e -> getData());
        setModal(true);
    }

    public static void main(String[] args) {
        WebCrawlUI dialog = new WebCrawlUI();
        dialog.pack();
        dialog.setVisible(true);

        System.exit(0);
    }

    private void getData() {
        WebCrawler crawler = new WebCrawler();
        LocalDate from = datePickerFrom.getDate();
        LocalDate to = datePickerTo.getDate();

        List<String[]> data = crawler.getEnergyData(from, to);

        output.setText("");
        data.forEach(line -> output.append(Arrays.toString(line) + "\n"));
    }
}
