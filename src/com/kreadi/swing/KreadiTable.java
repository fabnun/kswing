package com.kreadi.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.text.DecimalFormat;
import java.util.HashMap;
import javax.swing.AbstractCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

/**
 * Implementacion de un JTable mas sencillo de administrar
 */
public class KreadiTable extends JTable {

    private final DefaultTableModel dtm;
    private final DecimalFormat formatter = new DecimalFormat("#,###");

    /**
     * CellRender para formatear enteros
     */
    private class IntegerFormatRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            try {
                value = formatter.format((Integer) value);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                value = "";
            }
            IntegerFormatRenderer c = (IntegerFormatRenderer) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            c.setHorizontalAlignment(IntegerFormatRenderer.RIGHT);
            return c;
        }
    }

    /**
     * CellEditor Generico para Booleanos, Enteros y Strings
     */
    public class KreadiTableCellEditor extends AbstractCellEditor implements TableCellEditor {

        private final JComponent component;
        public final JTextField textField;
        private final JCheckBox checkBox;
        private final Class cls;

        public KreadiTableCellEditor(Class cls) {
            this(cls, 0);
        }

        public KreadiTableCellEditor(Class cls, int maxChars) {
            this(cls, maxChars, null);
        }

        public KreadiTableCellEditor(Class cls, int maxChars, String regExp) {
            this.cls = cls;
            textField = new JTextField();
            if (maxChars > 0 || regExp != null) {
                textField.setDocument(new KPlainDocument(maxChars, regExp));
            }
            checkBox = new JCheckBox();
            textField.setBorder(new LineBorder(Color.white, 1));
            if (cls == Integer.class) {
                textField.setHorizontalAlignment(JTextField.RIGHT);
            }
            if (cls == Boolean.class) {
                component = checkBox;
            } else {
                component = textField;
            }
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean selected, int row, int col) {
            if (cls == Boolean.class) {
                checkBox.setSelected((Boolean) value);
            } else if (cls == Integer.class) {

                if (value != null) {
                    try {
                        value = formatter.format((Integer) value);
                        textField.setText(value.toString());
                    } catch (Exception e) {
                        dtm.setValueAt(null, row, col);
                        textField.setText("");
                    }
                } else {
                    dtm.setValueAt(null, row, col);
                    textField.setText("");
                }

            } else {

                textField.setText((String) value);

            }

            return component;
        }

        @Override
        public Object getCellEditorValue() {
            if (cls == Boolean.class) {
                return checkBox.isSelected();
            } else if (cls == Integer.class) {
                try {
                    return Integer.parseInt(textField.getText());
                } catch (NumberFormatException e) {
                    return null;
                }
            } else {
                return textField.getText();
            }
        }
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
                    ((KreadiTableCellEditor) getColumnModel().getColumn(i).getCellEditor()).textField.setFont(font);
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
    public KreadiTable(String[] colNames, final Class[] colClasses, final boolean[] editable, int[] maxChars, String[] regexp) {
        dtm = new DefaultTableModel(colNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return editable == null || editable[column];
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return colClasses == null ? String.class : colClasses[columnIndex];
            }
        };
        setModel(dtm);
        TableColumnModel tcm = this.getColumnModel();
        if (colClasses != null) {
            for (int i = 0; i < colClasses.length; i++) {
                TableColumn col = tcm.getColumn(i);
                col.setCellEditor(new KreadiTableCellEditor(colClasses[i], maxChars != null ? maxChars[i] : 0, regexp != null ? regexp[i] : null));
                if (colClasses[i] == Integer.class) {
                    col.setCellRenderer(new IntegerFormatRenderer());
                }
            }
        }
    }

    public KreadiTable(String[] colNames, final Class[] colClasses, final boolean[] editable, int[] maxChars) {
        this(colNames, colClasses, editable, maxChars, null);
    }

    public KreadiTable(String[] colNames, final Class[] colClasses, final boolean[] editable) {
        this(colNames, colClasses, editable, null);
    }

    public KreadiTable(String[] colNames, final Class[] colClasses) {
        this(colNames, colClasses, null);
    }

    public KreadiTable(String[] colNames) {
        this(colNames, null);
    }

    @Override
    public TableModel getModel() {
        return dtm;
    }

}
