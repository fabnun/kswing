package com.kreadi.swing;

import java.awt.Component;
import java.awt.Font;
import java.text.DecimalFormat;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

/**
 * Implementacion de un JTable mas sencillo de administrar
 */
public class KTable extends JTable {

    private DefaultTableModel dtm;
    private static final DecimalFormat decimalFormat = new DecimalFormat("#,###");

    public void resizeColumnWidth() {
        final TableColumnModel colModel = getColumnModel();
        for (int column = 0; column < getColumnCount(); column++) {
            int width = 50; // Min width
            for (int row = 0; row < getRowCount(); row++) {
                Component comp = prepareRenderer(getCellRenderer(row, column), row, column);
                width = Math.max(comp.getPreferredSize().width, width);
            }
            colModel.getColumn(column).setPreferredWidth(width);
        }
    }

    @Override
    public Class<?> getColumnClass(int column) {
        return super.getColumnClass(column); //To change body of generated methods, choose Tools | Templates.
    }

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

    @Override
    public void setModel(TableModel dataModel) {
        super.setModel(dataModel);
        this.dtm = (DefaultTableModel) dataModel;
    }

    public static DefaultTableModel buildModel(String[] colNames, final Class[] colClasses, final boolean[] editable, int[] maxChars, String[] regexp) {
        final Class[] colClasses2 = new Class[colClasses.length];
        System.arraycopy(colClasses, 0, colClasses2, 0, colClasses.length);
        return new DefaultTableModel(colNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return editable == null || editable[column];
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return colClasses == null ? String.class : colClasses2[columnIndex];
            }
        };
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
        dtm = buildModel(colNames, colClasses, editable, maxChars, regexp);
        setModel(dtm);
        TableColumnModel tcm = this.getColumnModel();
        if (colClasses != null) {
            for (int i = 0; i < colClasses.length; i++) {
                TableColumn col = tcm.getColumn(i);
                if (editable != null && editable[i]) {
                    col.setCellEditor(new KTableCellEditor(colClasses[i], maxChars != null ? maxChars[i] : 0, regexp != null ? regexp[i] : null));
                }
                col.setCellRenderer(new KCellRenderer(colClasses[i], decimalFormat));
            }
        }
        getTableHeader().setReorderingAllowed(false);
    }

    public KCellRenderer getCellRenderer(int col) {
        return (KCellRenderer) ((TableColumnModel) getColumnModel()).getColumn(col).getCellRenderer();
    }

    @Override
    public void changeSelection(int row, int column, boolean toggle, boolean extend) {
        super.changeSelection(row, column, toggle, extend);

        if (editCellAt(row, column)) {
            Component editor = getEditorComponent();
            editor.requestFocusInWindow();
        }
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
