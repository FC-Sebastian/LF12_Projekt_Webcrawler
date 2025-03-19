package view;

import EventListener.GetDataListener;
import com.github.lgooddatepicker.components.DatePicker;

import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;

public class InputPane extends JPanel {
    private JButton submitButton;
    private DatePicker datePickerFrom;
    private DatePicker datePickerTo;
    private GetDataListener listener;

    /**
     * sets event listener and builds/places input elements
     *
     * @param listener listens to data fetch request
     */
    public InputPane(GetDataListener listener) {
        this.listener = listener;
        setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        submitButton = new JButton();
        datePickerFrom = new DatePicker();
        datePickerTo = new DatePicker();

        datePickerFrom.addDateChangeListener(dateChangeEvent -> System.out.println("from changed to " + datePickerFrom.getDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))));
        datePickerTo.addDateChangeListener(dateChangeEvent -> System.out.println("to changed to " + datePickerTo.getDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))));

        submitButton.setText("GIB MIR DATEN!");
        submitButton.addActionListener(e -> listener.getData(datePickerFrom.getDate(), datePickerTo.getDate()));

        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 1;
        constraints.weighty = 1;
        add(datePickerFrom, constraints);

        constraints.gridx = 1;
        add(datePickerTo, constraints);

        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 2;
        constraints.weightx = 2;
        add(submitButton, constraints);
    }
}
