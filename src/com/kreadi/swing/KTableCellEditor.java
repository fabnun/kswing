package com.kreadi.swing;

import java.awt.Color;
import java.awt.Component;
import java.text.DecimalFormat;
import javax.swing.AbstractCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import javax.swing.table.TableCellEditor;

public class KTableCellEditor extends AbstractCellEditor implements TableCellEditor {

    private final JComponent component;
    public final JTextField textField;
    private final JCheckBox checkBox;
    private final Class cls;
    private final DecimalFormat formatter;

    public KTableCellEditor(Class cls) {
        this(cls, 0);
    }

    public KTableCellEditor(Class cls, int maxChars) {
        this(cls, maxChars, null, null);
    }

    public KTableCellEditor(Class cls, int maxChars, String regExp) {
        this(cls, maxChars, regExp, null);
    }

    public KTableCellEditor(Class cls, int maxChars, String regExp, DecimalFormat formatter) {
        this.cls = cls;
        this.formatter = formatter;
        if (maxChars > 0 || regExp != null || cls != Integer.class) {
            textField = new JTextField();
            if (cls == Integer.class) {
                textField.setHorizontalAlignment(JTextField.RIGHT);
            }
            textField.setBorder(new LineBorder(Color.white, 1));
            textField.setDocument(new KPlainDocument(maxChars, regExp));
            component = textField;
            checkBox = null;
        } else {
            textField = null;
            checkBox = new JCheckBox();
            component = checkBox;
        }
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean selected, int row, int col) {
        if (cls == Boolean.class) {
            checkBox.setSelected((Boolean) value);
        } else if (cls == Integer.class) {
            if (value != null) {
                textField.setText(value.toString());
            } else {
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
