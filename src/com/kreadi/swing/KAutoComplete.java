package com.kreadi.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
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
    private final KTable list = new KTable(new String[]{""}, new Class[]{String.class}, new boolean[]{false}, new int[]{0}, new String[]{null});
    private final JScrollPane scroll = new JScrollPane(list);
    private int idx = -1;
    private String findMem = "";
    private final JTable table;
    private int rowHeight;

    public void setBackground(Color bg) {
        list.setBackground(bg);
    }

    public KAutoComplete(JTextField field) {
        this.field = field;
        Document d = field.getDocument();
        if (d instanceof KPlainDocument) {
            doc = (KPlainDocument) d;
        } else {
            doc = null;
        }
        table = null;
        rowHeight = field.getHeight();
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
            int selIdx = list.getSelectedRow();
            if (selIdx != -1) {
                popup.setVisible(false);
                String t = list.getValueAt(selIdx, 0).toString();
                field.setText(t);
                select(t);
            }
            doit = false;
        }
    }

    private Component getRootParent(Component c) {
        Component p = c.getParent();
        return p == null ? c : getRootParent(p);
    }

    private void constructor() {
        list.setFocusable(false);
        list.setTableHeader(null);
        popup.setFocusable(false);
        scroll.setFocusable(false);

        Border border = new EmptyBorder(0, 0, 0, 0);
        Border border2 = new LineBorder(Color.gray, 1);
        scroll.setBorder(border2);
        list.setBorder(border);
        list.setFont(field.getFont());
        popup.setBorder(border);
        popup.add(scroll);

        //Listener para seleccionar un item con el mouse
        list.addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                if (list.getSelectedRow() != -1) {
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
                        e.consume();
                    } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                        if (idx < list.getRowCount() - 1) {
                            idx++;
                            list.setRowSelectionInterval(idx, idx);
                            list.scrollRectToVisible(list.getCellRect(idx, 0, false));
                        }
                        e.consume();
                    } else if (e.getKeyCode() == KeyEvent.VK_UP) {
                        if (idx >= 0) {
                            idx--;
                            if (idx == -1) {
                                list.clearSelection();
                            } else {
                                list.setRowSelectionInterval(idx, idx);
                                list.scrollRectToVisible(list.getCellRect(idx, 0, false));
                            }
                        }
                        e.consume();
                    }
                }
            }
        });

        //Listener cuando el textfield pierde el foco
        popup.addFocusListener(new FocusAdapter() {

            @Override
            public void focusGained(FocusEvent e) {
                if (e.getOppositeComponent() == field) {
                    field.requestFocus();
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
                    if (rowHeight < 2) {
                        rowHeight = field.getHeight();
                    }
                    if (doc == null || (doc != null && !doc.lostFocus)) {
                        String find = field.getText().trim();
                        if (!find.equals(findMem)) {
                            findMem = find;
                            if (find.length() > 0) {
                                lst = find(find);
                                int size = lst == null ? 0 : lst.size();
                                if (size > 0) {
                                    if (popup.isVisible()) {
                                        popup.setVisible(false);
                                    }
                                    Object[][] data = new Object[lst.size()][];
                                    int cols=lst.get(0).length;
                                    String[] nams=new String[cols];
                                    Class[] clss=new Class[cols];
                                     boolean[] bols=new boolean[cols];
                                     int[] ints=new int[cols];
                                     String[] nams2=new String[cols];
                                     for(int i=0;i<cols;i++){
                                         nams[i]="";
                                         clss[i]=lst.get(0)[i].getClass();
                                         bols[i]=false;
                                         ints[i]=0;
                                         nams2[i]=null;
                                     }
                                    list.setModel(KTable.buildModel(nams,clss, bols, ints, nams2));
                                    while (list.getRowCount() > 0) {
                                        list.removeRow(0);
                                    }
                                    for (int i = 0; i < lst.size(); i++) {
                                        data[i] = lst.get(i);
                                        list.addRow(data[i]);
                                    }
                                    list.resizeColumnWidth();
                                    idx = -1;
                                    Dimension dim = new Dimension(field.getWidth(), 2 + Math.min(6, size) * field.getHeight());
                                    list.setRowHeight(rowHeight);
                                    list.updateUI();
                                    popup.setPreferredSize(dim);
                                    popup.setSize(dim);
                                    if (!popup.isVisible() && field.isVisible() && field.isEditable() && field.isEnabled()) {
                                        try {
                                            popup.show(field, 0, rowHeight);
                                            if (table != null) {
                                                Component editor = table.getEditorComponent();
                                                editor.requestFocusInWindow();
                                            }
                                        } catch (Exception e) {
                                        }
                                    }
                                } else {
                                    popup.setVisible(false);
                                }
                            } else {
                                popup.setVisible(false);
                            }
                        }
                    }
                }
            }

        });
    }

}
