package com.kreadi.swing;

import com.michaelbaranov.microba.calendar.CalendarPane;
import java.awt.AWTException;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.JRootPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.table.TableColumnModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;

public class KSwingTools {

    private static final KeyStroke escapeStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
    private static final String dispatchWindowClosingActionMapKey = "com.spodding.tackline.dispatch:WINDOW_CLOSING";
    

    /**
     * Obtiene un TTF Font indicando la url del archivo.ttf, el estilo y el tamaño
     *
     * @param url
     * @param style
     * @param size
     * @return
     */
    public static Font getTTFFont(URL url, int style, float size) {
        try {
            return Font.createFont(Font.TRUETYPE_FONT, url.openStream()).deriveFont(style, size);
        } catch (FontFormatException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Sale de la aplicacion al presionar escape... OJO!!! sin el evento window.clossin
     *
     * @param frame
     */
    public static void addEscapeListener(final JFrame frame) {
        ActionListener escListener = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        };

        frame.getRootPane().registerKeyboardAction(escListener,
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    /**
     * Cierra el dialogo al presionar escape... OJO!!! este si llama al evento window.clossin
     *
     * @param dialog
     */
    public static void addEscapeListener(final JDialog dialog) {

        Action dispatchClosing = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                dialog.dispatchEvent(new WindowEvent(
                        dialog, WindowEvent.WINDOW_CLOSING
                ));
            }
        };
        JRootPane root = dialog.getRootPane();
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                escapeStroke, dispatchWindowClosingActionMapKey
        );
        root.getActionMap().put(dispatchWindowClosingActionMapKey, dispatchClosing
        );
    }

    /**Establece los colores principales del look and feel nimbu*/
    public static void setNimbusLookAndFeel(Color... colors) {
        String[] keys = new String[]{"nimbusBlueGrey", "control", "Table.focusCellHighlightBorder"};
        try {
            for (int i = 0; i < colors.length; i++) {
                UIManager.put(keys[i], colors[i]);
            }
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }
    }

    public static void setIntegerField(final JTextField field) {
        field.setDocument(new KPlainDocument(9, "\\d*", new DecimalFormat("#,###"), field));
        field.setHorizontalAlignment(JTextField.RIGHT);
        //TODO cuando pierda el foco se formatee
    }

    public static CalendarPane calendar(final JLabel field, final SimpleDateFormat sdf, boolean allowClear) {
        field.setCursor(new Cursor(Cursor.HAND_CURSOR));
        final JPopupMenu menu = new JPopupMenu();
        final CalendarPane datePanel = new CalendarPane(CalendarPane.STYLE_CLASSIC);
        datePanel.setLabel(field);
        datePanel.setShowNoneButton(allowClear);
        try {
            String text = field.getText();
            Date date = sdf.parse(text);
            datePanel.setDate(date);
        } catch (ParseException | PropertyVetoException e) {
            // e.printStackTrace();
        }
        try {
            Date date = new Date();
            datePanel.setDate(date);
            datePanel.setCursor(field.getCursor());

            menu.add(datePanel);
            field.addMouseListener(new MouseAdapter() {

                @Override
                public void mousePressed(MouseEvent e) {
                    if (datePanel.isVisible() && datePanel.isEnabled() && e.getButton() == MouseEvent.BUTTON1) {
                        try {
                            String text = field.getText();
                            Date d = sdf.parse(text);
                            datePanel.setDate(d);
                        } catch (ParseException | PropertyVetoException ex) {
                            ex.printStackTrace();
                        }
                        menu.show(field, 0, 0);
                    }
                }

            });

        } catch (PropertyVetoException ex) {
            ex.printStackTrace();
        }
        datePanel.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Date d = datePanel.getDate();
                if (d != null) {
                    field.setText(sdf.format(d));
                    menu.setVisible(false);
                }
            }
        });
        return datePanel;
    }

    public static HashMap<String, Integer> getMapProperties(Component comp) {
        HashMap<String, Integer> map = new HashMap<>();
        if (comp instanceof JDialog || comp instanceof JFrame) {
            map.put("width", comp.getWidth());
            map.put("height", comp.getHeight());
            Point p = comp.getLocation();
            map.put("x", p.x);
            map.put("y", p.y);
        } else if (comp instanceof JTable) {
            JTable table = (JTable) comp;
            TableColumnModel model = table.getColumnModel();
            for (int i = 0; i < model.getColumnCount(); i++) {
                map.put("width." + i, model.getColumn(i).getWidth());
            }
        } else if (comp instanceof JSplitPane) {
            map.put("splitPos", ((JSplitPane) comp).getDividerLocation());
        } else {
            return null;
        }
        return map;
    }

    public static void fireTab() {

        try {
            Robot robot = new Robot();
            // Simulate a key press
            robot.keyPress(KeyEvent.VK_TAB);
            robot.keyRelease(KeyEvent.VK_TAB);
        } catch (AWTException e) {
            e.printStackTrace();
        }

    }

    public static void fireShiftTab() {

        try {
            Robot robot = new Robot();
            // Simulate a key press
            robot.keyPress(KeyEvent.VK_SHIFT);
            robot.keyPress(KeyEvent.VK_TAB);
            robot.keyRelease(KeyEvent.VK_TAB);
            robot.keyRelease(KeyEvent.VK_SHIFT);
        } catch (AWTException e) {
            e.printStackTrace();
        }

    }

    public static int getLineAtCaret(JTextComponent component) {
        int caretPosition = component.getCaretPosition();
        Element root = component.getDocument().getDefaultRootElement();
        return root.getElementIndex(caretPosition) + 1;
    }

    public static int getColumnAtCaret(JTextComponent component) {
        FontMetrics fm = component.getFontMetrics(component.getFont());
        int characterWidth = fm.stringWidth("0");
        int column = 0;

        try {
            Rectangle r = component.modelToView(component.getCaretPosition());
            if (r != null) {
                int width = r.x - component.getInsets().left;
                column = width / characterWidth;
            } else {
                column = 1;
            }
        } catch (BadLocationException ble) {
        }

        return column + 1;
    }

}
