package com.kreadi.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

public abstract class KAutoComplete {

    public final JTextField field;
    private final KPlainDocument doc;
    private final JPopupMenu popup = new JPopupMenu();
    private final JList list = new JList();
    private final JScrollPane scroll = new JScrollPane(list);
    private int idx = -1;
    private String findMem = "";
    private final JTable table;

    public KAutoComplete(JTextField field) {
        this.field = field;
        Document d = field.getDocument();
        if (d instanceof KPlainDocument) {
            doc = (KPlainDocument) d;
        } else {
            doc = null;
        }
        table = null;
        constructor();
    }

    public KAutoComplete(JTable table, int column) {
        this.field = (JTextField) table.getCellEditor(0, column).getTableCellEditorComponent(table, null, false, 0, column);
        Document d = field.getDocument();
        if (d instanceof KPlainDocument) {
            doc = (KPlainDocument) d;
        } else {
            doc = null;
        }
        this.table = table;
        constructor();
    }

    public abstract List<Object[]> find(String text);
    private boolean doit = false;

    public abstract void select(String text);

    private synchronized void doit() {
        if (!doit) {
            doit = true;
            int selIdx = list.getSelectedIndex();
            if (selIdx != -1) {
                popup.setVisible(false);
                System.out.println("setVisible false");
                String t=list.getModel().getElementAt(selIdx).toString();
                field.setText(t);
                select(t);
            }
            doit = false;
        }
    }

    private void constructor() {
        popup.setFocusable(false);
        scroll.setFocusable(false);
        list.setFocusable(false);

        Border border = new EmptyBorder(0, 0, 0, 0);
        Border border2 = new LineBorder(Color.gray);
        popup.setBorder(border2);
        scroll.setBorder(border);
        list.setBorder(border);
        list.setFont(field.getFont());
        list.setFixedCellHeight(field.getHeight());

        popup.add(scroll);

        //Listener para seleccionar un item con el mouse
        list.addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                if (list.getSelectedValue() != null) {
                    doit();
                }
            }

        });

        //Listener de eventos key sobre el textfield
        field.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                if (popup.isVisible()) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        doit();
                        e.consume();
                    } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                        popup.setVisible(false);
                        System.out.println("setVisible false");
                        e.consume();
                    } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                        if (idx < list.getModel().getSize() - 1) {
                            idx++;
                            list.setSelectedIndex(idx);
                            list.updateUI();
                        }
                        e.consume();
                    } else if (e.getKeyCode() == KeyEvent.VK_UP) {
                        if (idx >= 0) {
                            idx--;
                            if (idx == -1) {
                                list.clearSelection();
                            } else {
                                list.setSelectedIndex(idx);
                            }
                        }
                        e.consume();
                    }
                }
            }
        });

        //Listener cuando el textfield pierde el foco
        field.addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(FocusEvent e) {
                idx = -1;
                if (popup.isVisible()) {
                    if (table == null) {
                        popup.setVisible(false);
                    } else {
                        table.editCellAt(0, 0);
                    }
                    System.out.println("setVisible false");
                }
            }
        });

        //Listener para cuando el texto del textfield se modifique
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

                if (!doit && field.hasFocus()) {
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
                                    list.setListData(data);
                                    idx = -1;
                                    Dimension dim = new Dimension(field.getWidth() + 2, 2 + Math.min(6, size) * field.getHeight());
                                    popup.setPreferredSize(dim);
                                    popup.setSize(dim);
                                    System.out.println("setVisible true " + lst);
                                    if (!popup.isVisible()) {
                                        try {
                                            popup.show(field, -1, field.getHeight());
                                        } catch (Exception e) {
                                        }
                                        
                                    }
                                } else {
                                    popup.setVisible(false);
                                    System.out.println("setVisible false");
                                }
                            } else {
                                popup.setVisible(false);
                                System.out.println("setVisible false");
                            }
                        }
                    }
                }
            }

        });
    }

}
