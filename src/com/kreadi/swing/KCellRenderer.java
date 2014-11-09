package com.kreadi.swing;

import java.awt.Color;
import java.awt.Component;
import java.text.DecimalFormat;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

public class KCellRenderer implements TableCellRenderer {

    private final Class cls;
    private final DecimalFormat formatter;
    private final JCheckBox check;
    private final JLabel label;
    private final Border border=new LineBorder(Color.BLACK);

    public KCellRenderer(Class cls, DecimalFormat formatter) {
        this.cls = cls;
        label = (cls == Integer.class || cls == String.class) ? new JLabel() : null;
        check = (cls == Boolean.class) ? new JCheckBox() : null;
        if (cls == Integer.class) {
            label.setHorizontalAlignment(JLabel.RIGHT);
        }
        this.formatter = formatter;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        JComponent comp = null;
        if (cls == Integer.class || cls == String.class) {
            if (cls == Integer.class) {
                try {
                    value = formatter.format((Integer) value);
                } catch (Exception e) {
                    value = null;
                }
            }

            label.setText((String) value);
            comp = label;
        } else if (cls == Boolean.class) {
            check.setSelected(value == null ? false : (Boolean) value);
            comp = check;
        }
        comp.setBackground(isSelected?table.getSelectionBackground():table.getBackground());
        comp.setOpaque(isSelected || row % 2 == 1);
        return comp;
    }

}
