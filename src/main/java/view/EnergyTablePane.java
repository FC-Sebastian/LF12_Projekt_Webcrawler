package view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.io.Serial;
import java.util.HashMap;
import java.util.List;

/**
 * Builds and returns scrollable pane containing table with row headers,
 * row headers are the hourly datetime strings, column headers types of energy data
 */
public class EnergyTablePane {
    public static JScrollPane getEnergyTable(String[] columnHeaderArray, List<String> rowHeaderArray, HashMap<String, HashMap<String,String>> data) {
        JTable dataTable = new JTable(rowHeaderArray.size(), columnHeaderArray.length);

        for (int i = 0; i < rowHeaderArray.size(); i++) {
            for (int o = 0; o < columnHeaderArray.length; o++) {
                String columValue = "0";
                if (data.containsKey(rowHeaderArray.get(i))) {
                    if (data.get(rowHeaderArray.get(i)).containsKey(columnHeaderArray[o])) {
                        columValue = data.get(rowHeaderArray.get(i)).get(columnHeaderArray[o]);
                    }
                }
                dataTable.setValueAt(columValue,i,o);
            }
        }

        DefaultTableModel rowModel = new DefaultTableModel() {
            @Serial
            private static final long serialVersionUID = 1L;

            @Override
            public int getColumnCount() {
                return 1;
            }

            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }

            @Override
            public int getRowCount() {
                return dataTable.getRowCount();
            }

            @Override
            public Class<?> getColumnClass(int colNum) {
                if (colNum == 0) {
                    return String.class;
                }
                return super.getColumnClass(colNum);
            }

        };

        JTable rowHeaderTable = new JTable(rowModel);

        for (int i = 0; i < rowHeaderArray.size(); i++) {
            rowHeaderTable.setValueAt(rowHeaderArray.get(i),i,0);
        }

        for (int i = 0; i < dataTable.getColumnCount(); i++) {
            TableColumn column = dataTable.getTableHeader().getColumnModel().getColumn(i);
            column.setHeaderValue(columnHeaderArray[i]);
        }

        JScrollPane scrollPane = new JScrollPane(dataTable);
        scrollPane.setRowHeaderView(rowHeaderTable);
        return scrollPane;
    }
}
