package com.kreadi.swing;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.text.DecimalFormat;
import java.text.ParseException;
import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

public class KPlainDocument extends PlainDocument {

    private final int limit;
    private final String regExp;
    private final char to;
    public boolean lostFocus = false;

    public KPlainDocument(int limit) {
        this(limit, null, null, null);
    }

    public KPlainDocument(int limit, String regExp) {
        this(limit, regExp, null, null);
    }

    /**
     * PlainDocument si limit es > 0 limita cantidad de caracteres y usa expresiones regulares validas para los campos de texto Tambien tiene los prefijos toUpper y toLower que transforman el campo de
     * texto a mayusculas o minusculas
     *
     * @param limit caracteres maximos
     * @param regExp expresion regular valida... prefijos toLower y toUpper
     * @param df
     * @param field
     */
    public KPlainDocument(int limit, String regExp, final DecimalFormat df, final JTextField field) {
        super();
        this.limit = limit;
        if (regExp != null) {
            if (regExp.startsWith("toUpper")) {
                to = 'U';
                regExp = regExp.substring(7);
            } else if (regExp.startsWith("toLower")) {
                to = 'L';
                regExp = regExp.substring(7);
            } else {
                to = ' ';
            }
        } else {
            to = ' ';
        }
        this.regExp = regExp != null && regExp.trim().length() > 0 ? regExp : null;
        if (df != null && field != null) {
            field.addFocusListener(new FocusAdapter() {

                @Override
                public void focusLost(FocusEvent e) {
                    lostFocus = true;
                    try {
                        String old = field.getText().replaceAll("\\.|,", "").trim();
                        if (old.length() > 0) {
                            String lost = df.format(df.parse(old));
                            field.setText(lost);
                        }
                    } catch (ParseException ex) {
                        ex.printStackTrace();
                    }
                }

                @Override
                public void focusGained(FocusEvent e) {
                    try {
                        String focus=field.getText().replaceAll("\\.|,", "");
                        field.setText(focus);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    lostFocus = false;
                }

            });
        }
    }

    @Override
    public void replace(int offset, int length, String str, AttributeSet attrs) throws BadLocationException {
        if (lostFocus) {
            super.replace(offset, length, str, attrs);
        } else {
            if (str == null) {
                return;
            }
            if (to == 'L') {
                str = str.toLowerCase();
            } else if (to == 'U') {
                str = str.toUpperCase();
            }
            int size = getLength();
            String old = getText(0, size);
            old = old.substring(0, offset) + str + old.substring(offset + length);
            if (regExp == null || old.matches(regExp)) {
                super.replace(offset, length, str, attrs); //To change body of generated methods, choose Tools | Templates.
            }
        }
    }

    @Override
    public void insertString(int offset, String str, AttributeSet attr) throws BadLocationException {
        if (lostFocus) {
            super.insertString(offset, str, attr);
        } else {
            if (str == null) {
                return;
            }
            if (to == 'L') {
                str = str.toLowerCase();
            } else if (to == 'U') {
                str = str.toUpperCase();
            }
            int size = getLength();
            String old = getText(0, size);
            old = old.substring(0, offset) + str + old.substring(offset);
            if (regExp == null || old.matches(regExp)) {
                if ((getLength() + str.length()) <= limit) {
                    super.insertString(offset, str, attr);
                }
            }
        }
    }
}
