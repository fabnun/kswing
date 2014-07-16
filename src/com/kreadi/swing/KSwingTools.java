package com.kreadi.swing;

import com.michaelbaranov.microba.calendar.CalendarPane;
import java.awt.AWTException;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.synth.SynthLookAndFeel;
import javax.swing.table.TableColumnModel;

public class KSwingTools {

    public static DecimalFormat decimalFormat = new DecimalFormat("#,###");

    public static Font getTTFFont(URL url, int style, float size) {
        try {
            return Font.createFont(Font.TRUETYPE_FONT, url.openStream()).deriveFont(style, size);
        } catch (FontFormatException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void addEscapeListener(final JFrame dialog) {
        ActionListener escListener = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        };

        dialog.getRootPane().registerKeyboardAction(escListener,
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    public static void addEscapeListener(final JDialog dialog) {
        ActionListener escListener = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.setVisible(false);
            }
        };

        dialog.getRootPane().registerKeyboardAction(escListener,
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    public static void setSynthLookAndFeel(URL xmlconfig) throws IOException, ParseException, UnsupportedLookAndFeelException {
        SynthLookAndFeel synth = new SynthLookAndFeel();
        synth.load(xmlconfig);
        UIManager.setLookAndFeel(synth);
    }

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

    public static CalendarPane calendar(final JLabel field, final SimpleDateFormat sdf, String titulo, boolean allowClear) {
        field.setCursor(new Cursor(Cursor.HAND_CURSOR));
        final JPopupMenu menu = new JPopupMenu(titulo);
        final CalendarPane datePanel = new CalendarPane(CalendarPane.STYLE_CLASSIC);
        datePanel.setLabel(field);
        datePanel.setShowNoneButton(allowClear);
        try {
            datePanel.setDate(sdf.parse(field.getText()));
        } catch (ParseException | PropertyVetoException e) {
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
                            datePanel.setDate(sdf.parse(field.getText()));
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

}
