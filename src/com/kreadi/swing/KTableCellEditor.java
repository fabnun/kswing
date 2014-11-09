package com.kreadi.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.AbstractCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import javax.swing.table.TableCellEditor;

public class KTableCellEditor extends AbstractCellEditor implements TableCellEditor {

    public final JTextField textField;
    private final JCheckBox checkBox;
    private final Class cls;
    private static final LineBorder border = new LineBorder(Color.black);

    public KTableCellEditor(Class cls) {
        this(cls, 0);
    }

    public KTableCellEditor(Class cls, int maxChars) {
        this(cls, maxChars, null);
    }

    public KTableCellEditor(Class cls, int maxChars, String regExp) {
        this.cls = cls;
        if (maxChars > 0 || regExp != null || cls != Boolean.class) {
            textField = new JTextField();
            textField.addKeyListener(new KeyAdapter() {

                @Override
                public void keyPressed(KeyEvent e) {
                    int code = e.getKeyCode();
                    if (code == KeyEvent.VK_LEFT) {
                        if (textField.getCaretPosition() == 0) {
                            KSwingTools.fireShiftTab();
                        }
                    } else if (code == KeyEvent.VK_RIGHT) {
                        int pos = textField.getCaretPosition();
                        int max = textField.getText().length() - 1;
                        if (pos >= max) {
                            KSwingTools.fireTab();
                        }
                    }
                }
            });
            if (cls == Integer.class) {
                textField.setHorizontalAlignment(JTextField.RIGHT);
            }
            textField.setBorder(new LineBorder(Color.white, 1));
            textField.setDocument(new KPlainDocument(maxChars, regExp));
            checkBox = null;
        } else {
            textField = null;
            checkBox = new JCheckBox();
        }
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean selected, int row, int col) {
        JComponent comp = null;
        if (cls == Boolean.class) {
            checkBox.setSelected(value == null ? false : (Boolean) value);
            comp = checkBox;
        } else if (cls == Integer.class) {
            if (value != null) {
                textField.setText(value.toString());
            } else {
                textField.setText("");
            }
            comp = textField;
        } else if (cls == String.class) {
            textField.setText((String) value);
            comp = textField;
        }
        if (comp != null) {
            comp.setOpaque(true);
            comp.setBackground(table.getSelectionBackground().brighter().brighter());
        }
        return comp;
    }

    @Override
    public Object getCellEditorValue() {
        Object val = null;
        if (cls == Boolean.class) {
            val = checkBox.isSelected();
        } else if (cls == Integer.class) {
            try {
                val = Integer.parseInt(textField.getText());
            } catch (NumberFormatException e) {

            }
        } else {
            val = textField.getText();
        }
        return val;
    }
}
