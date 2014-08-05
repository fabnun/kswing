package com.kreadi.swing;

import java.awt.Component;
import java.text.DecimalFormat;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class KCellRenderer extends DefaultTableCellRenderer {

    private final Class cls;
    private final DecimalFormat formatter;

    public KCellRenderer(Class cls, DecimalFormat formatter) {
        this.cls = cls;
        if (cls == Integer.class) {
            setHorizontalAlignment(RIGHT);
        }
        this.formatter = formatter;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (cls == Integer.class) {
            try {
                value = formatter.format((Integer) value);
            } catch (Exception e) {
                value = null;
            }
        }
        return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    }

}
