package com.kreadi.swing;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Font;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

/**
 * Implementacion de un JTable mas sencillo de administrar
 */
public class KTable extends JTable {

    private final DefaultTableModel dtm;

    /**
     * Aplica la fuente tambien a los CellEditor
     */
    @Override
    public void setFont(Font font) {
        super.setFont(font);
        if (dtm != null) {
            for (int i = 0; i < dtm.getColumnCount(); i++) {
                try {
                    ((KTableCellEditor) getColumnModel().getColumn(i).getCellEditor()).textField.setFont(font);
                } catch (Exception e) {
                }
            }
        }
    }

    /**
     * Instancia una tabla
     *
     * @param colNames array de nombres de las columnas
     * @param colClasses array de clases de las columnas
     * @param editable array de columnas editables
     * @param maxChars array de cantidad maxima de caracteres
     * @param regexp expresion regular valida
     */
    public KTable(String[] colNames, final Class[] colClasses, final boolean[] editable, int[] maxChars, String[] regexp) {
        final Class[] colClasses2 = new Class[colClasses.length];
        for (int i = 0; i < colClasses.length; i++) {
            colClasses2[i] = colClasses[i] == Integer.class ? String.class : colClasses[i];
        }
        dtm = new DefaultTableModel(colNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return editable == null || editable[column];
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return colClasses == null ? String.class : colClasses2[columnIndex];
            }
        };
        setModel(dtm);
        TableColumnModel tcm = this.getColumnModel();
        if (colClasses != null) {
            for (int i = 0; i < colClasses.length; i++) {
                TableColumn col = tcm.getColumn(i);
                if (editable[i]) {
                    col.setCellEditor(new KTableCellEditor(colClasses[i], maxChars != null ? maxChars[i] : 0, regexp != null ? regexp[i] : null, KSwingTools.decimalFormat));
                }
                col.setCellRenderer(new KCellRenderer(colClasses[i], KSwingTools.decimalFormat));
            }
        }
    }

    public KTable(String[] colNames, final Class[] colClasses, final boolean[] editable, int[] maxChars) {
        this(colNames, colClasses, editable, maxChars, null);
    }

    public KTable(String[] colNames, final Class[] colClasses, final boolean[] editable) {
        this(colNames, colClasses, editable, null);
    }

    public KTable(String[] colNames, final Class[] colClasses) {
        this(colNames, colClasses, null);
    }

    public KTable(String[] colNames) {
        this(colNames, null);
    }

    @Override
    public TableModel getModel() {
        return dtm;
    }

    public void removeRow(int idx) {
        dtm.removeRow(idx);
    }

    public void addRow(Object[] row) {
        dtm.addRow(row);
    }

    public void setRow(Object[] row, int idx) {
        for (int i = 0; i < row.length; i++) {
            dtm.setValueAt(row[i], idx, i);
        }
    }

    public void addRow(Object[] row, int idx) {
        dtm.insertRow(idx, row);
    }

    public Object[] getRow(int idx) {
        Object[] row = new Object[dtm.getColumnCount()];
        for (int i = 0; i < row.length; i++) {
            row[i] = dtm.getValueAt(idx, i);
        }
        return row;
    }



}
