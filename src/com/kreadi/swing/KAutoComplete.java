package com.kreadi.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

public abstract class KAutoComplete {

    private final JTextField field;
    private final KPlainDocument doc;
    private final JPopupMenu dialog = new JPopupMenu();
    private final JList list = new JList();
    private final JScrollPane scroll = new JScrollPane(list);
    private int idx = -1;
    private boolean visibleLostFocus = false;
    private String findMem = "";

    public KAutoComplete(JTextField field) {
        this.field = field;
        Document d = field.getDocument();
        if (d instanceof KPlainDocument) {
            doc = (KPlainDocument) d;
        } else {
            doc = null;
        }
        constructor();
    }

    public abstract List<Object[]> find(String text);

    public abstract void select(String text);

    private void doit() {
        int selIdx = list.getSelectedIndex();
        if (selIdx != -1) {
            select(list.getModel().getElementAt(selIdx).toString());
            dialog.setVisible(false);
        }
    }

    private void constructor() {
        list.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (list.getSelectedValue() != null) {
                    doit();
                }
            }

        });
        Border border=new EmptyBorder(0,0,0,0);
        dialog.setBorder(border);
        scroll.setBorder(border);
        list.setBorder(border);
        list.setFont(field.getFont());
        DefaultListCellRenderer renderer = (DefaultListCellRenderer) list.getCellRenderer();
        renderer.setHorizontalAlignment(field.getHorizontalAlignment());
        renderer.setBorder(new LineBorder(Color.gray));
        list.setFixedCellHeight(field.getHeight());
        dialog.add(scroll);

        field.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    doit();
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    dialog.setVisible(false);
                } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    if (idx < list.getModel().getSize() - 1) {
                        idx++;
                        list.setSelectedIndex(idx);
                    }
                } else if (e.getKeyCode() == KeyEvent.VK_UP) {
                    if (idx >= 0) {
                        idx--;
                        if (idx == -1) {
                            list.clearSelection();
                        } else {
                            list.setSelectedIndex(idx);
                        }
                    }
                }
            }
        });

        field.addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(FocusEvent e) {
                idx = -1;
                if (dialog.isVisible()) {
                    if (visibleLostFocus) {
                        SwingUtilities.invokeLater(new Runnable() {

                            @Override
                            public void run() {
                                field.requestFocus();
                                visibleLostFocus = false;
                                dialog.repaint();
                                dialog.updateUI();
                            }
                        });

                    } else {

                        SwingUtilities.invokeLater(new Runnable() {

                            @Override
                            public void run() {
                                dialog.setVisible(false);
                            }
                        });
                    }
                }
            }

        });

        field.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                change();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                change();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                change();
            }

            List<Object[]> lst;

            private void change() {
                if (field.hasFocus()) {
                    if (doc == null || (doc != null && !doc.lostFocus)) {
                        String find = field.getText().trim();
                        if (!find.equals(findMem)) {
                            findMem = find;
                            if (find.length() > 0) {
                                lst = find(find);
                                int size = lst == null ? 0 : lst.size();
                                if (size > 0) {
                                    Object[] data = new Object[lst.size()];
                                    for (int i = 0; i < lst.size(); i++) {
                                        data[i] = lst.get(i)[0];
                                    }
                                    list.clearSelection();
                                    list.setListData(data);
                                    idx = -1;
                                    Point p = field.getLocationOnScreen();
                                    p.translate(0, field.getHeight() - 4);
                                    dialog.setLocation(p);
                                    Dimension dim = new Dimension(field.getWidth(), size * field.getHeight() );
                                   
                                    dialog.setPreferredSize(dim);
                                    visibleLostFocus = true;
                                    dialog.pack();
                                    dialog.show(field, 0, field.getHeight());
                                    dialog.updateUI();
                                    dialog.repaint();
                                } else {
                                    dialog.setVisible(false);
                                }
                            } else {
                                dialog.setVisible(false);
                            }
                        }
                    }
                }
            }

        });
    }

}
