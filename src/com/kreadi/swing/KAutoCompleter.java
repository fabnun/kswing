package com.kreadi.swing;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.text.JTextComponent;

public abstract class KAutoCompleter {

    public JList list = new JList();
    JScrollPane scroll = new JScrollPane(list);
    public JPopupMenu popup = new JPopupMenu();
    public JTextComponent textComp;
    private static final String AUTOCOMPLETER = "AUTOCOMPLETER"; //NOI18N
    private final Font font;
    private static Boolean blockOthers = false;
    private int column = -1;
    private KTable table=null;

    public static boolean isBlockOthers() {
        synchronized (blockOthers) {
            return blockOthers;
        }
    }

    public static void blockOther(boolean block) {
        synchronized (blockOthers) {
            blockOthers = block;
        }
    }

    public KAutoCompleter(final JTextComponent textComp) {
        this.textComp=textComp;
        font = textComp.getFont();
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    acceptedListItem(list.getSelectedValue());
                    popup.setVisible(false);
                }
            }
        });
        list.setFont(font);
        textComp.putClientProperty(AUTOCOMPLETER, this);
        scroll.setBorder(null);
        list.setFocusable(false);
        scroll.getVerticalScrollBar().setFocusable(false);
        scroll.getHorizontalScrollBar().setFocusable(false);
        popup.add(scroll);

        if (textComp instanceof JTextField) {
            textComp.registerKeyboardAction(showAction, KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), JComponent.WHEN_FOCUSED);
            textComp.getDocument().addDocumentListener(documentListener);
        } else {
            textComp.registerKeyboardAction(showAction, KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, KeyEvent.CTRL_MASK), JComponent.WHEN_FOCUSED);
        }

        textComp.registerKeyboardAction(upAction, KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), JComponent.WHEN_FOCUSED);
        textComp.registerKeyboardAction(hidePopupAction, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_FOCUSED);

        popup.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                textComp.unregisterKeyboardAction(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
            }
        });
        list.setRequestFocusEnabled(false);
        textComp.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                if (!e.isTemporary()) {
                    popup.setVisible(false);
                }
            }
        });
    }

    public KAutoCompleter(KTable table, int column) {
        this.column = column;
        this.table=table;
        font = table.getFont();
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    acceptedListItem(list.getSelectedValue());
                    popup.setVisible(false);
                }
            }
        });
        list.setFont(font);
        textComp = (JTextComponent) table.getCellEditor(0, column).getTableCellEditorComponent(table, null, false, 0, column);
        textComp.putClientProperty(AUTOCOMPLETER, this);
        scroll.setBorder(null);
        list.setFocusable(false);
        scroll.getVerticalScrollBar().setFocusable(false);
        scroll.getHorizontalScrollBar().setFocusable(false);
        popup.add(scroll);

        if (textComp instanceof JTextField) {
            textComp.registerKeyboardAction(showAction, KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), JComponent.WHEN_FOCUSED);
            textComp.getDocument().addDocumentListener(documentListener);
        } else {
            textComp.registerKeyboardAction(showAction, KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, KeyEvent.CTRL_MASK), JComponent.WHEN_FOCUSED);
        }

        textComp.registerKeyboardAction(upAction, KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), JComponent.WHEN_FOCUSED);
        textComp.registerKeyboardAction(hidePopupAction, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_FOCUSED);

        popup.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                textComp.unregisterKeyboardAction(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
            }
        });
        list.setRequestFocusEnabled(false);
        textComp.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                if (!e.isTemporary()) {
                    popup.setVisible(false);
                }
            }
        });
    }

    static Action acceptAction = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            synchronized (blockOthers) {
                blockOthers = true;
                JComponent tf = (JComponent) e.getSource();
                KAutoCompleter completer = (KAutoCompleter) tf.getClientProperty(AUTOCOMPLETER);
                completer.acceptedListItem((Object) completer.list.getSelectedValue());
                completer.popup.setVisible(false);
                blockOthers = false;
            }
        }
    };
    DocumentListener documentListener = new DocumentListener() {
        @Override
        public void insertUpdate(DocumentEvent e) {
            try {
                showPopup();
            } catch (InterruptedException | InvocationTargetException ex) {
                Logger.getLogger(KAutoCompleter.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            try {
                showPopup();
            } catch (InterruptedException | InvocationTargetException ex) {
                Logger.getLogger(KAutoCompleter.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
        }
    };

    private void showPopup() throws InterruptedException, InvocationTargetException {
        synchronized (blockOthers) {
            if (!blockOthers) {
                if ((table == null && textComp.getText().length() > 0 && textComp.isEnabled() && updateListData() && list.getModel().getSize() != 0)
                        || (table!=null && column==table.getSelectedColumn() && textComp.getText().length() > 0 && textComp.isEnabled() && updateListData() && list.getModel().getSize() != 0)) {
                    if (!(textComp instanceof JTextField)) {
                        textComp.getDocument().addDocumentListener(documentListener);
                    }
                    textComp.registerKeyboardAction(acceptAction, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_FOCUSED);
                    int size = Math.min(list.getModel().getSize(),6);
                    list.setVisibleRowCount(size);
                    int x = 0;
                    try {
                        popup.pack();
                        popup.show(textComp, x, textComp.getHeight());
                    } catch (Exception e) {
                    }
                } else {
                    popup.setVisible(false);
                }
                textComp.requestFocus();

            }
        }
    }
    static AbstractAction showAction = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            JComponent tf = (JComponent) e.getSource();
            KAutoCompleter completer = (KAutoCompleter) tf.getClientProperty(AUTOCOMPLETER);
            if (tf.isEnabled()) {
                if (completer.popup.isVisible()) {
                    completer.selectNextPossibleValue();
                }
            }
        }
    };
    static Action upAction = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            JComponent tf = (JComponent) e.getSource();
            KAutoCompleter completer = (KAutoCompleter) tf.getClientProperty(AUTOCOMPLETER);
            if (tf.isEnabled()) {
                if (completer.popup.isVisible()) {
                    completer.selectPreviousPossibleValue();
                }
            }
        }
    };
    static Action hidePopupAction = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            JComponent tf = (JComponent) e.getSource();
            KAutoCompleter completer = (KAutoCompleter) tf.getClientProperty(AUTOCOMPLETER);
            if (tf.isEnabled()) {
                completer.popup.setVisible(false);
            }
        }
    };

    /**
     * Selects the next item in the list. It won't change the selection if the currently selected item is already the last item.
     */
    protected void selectNextPossibleValue() {
        int si = list.getSelectedIndex();

        if (si < list.getModel().getSize() - 1) {
            list.setSelectedIndex(si + 1);
            list.ensureIndexIsVisible(si + 1);
        }
    }

    /**
     * Selects the previous item in the list. It won't change the selection if the currently selected item is already the first item.
     */
    protected void selectPreviousPossibleValue() {
        int si = list.getSelectedIndex();

        if (si > 0) {
            list.setSelectedIndex(si - 1);
            list.ensureIndexIsVisible(si - 1);
        }
    }

    // update list model depending on the data in textfield
    protected abstract boolean updateListData();

    // user has selected some item in the list. update textfield accordingly...
    protected abstract void acceptedListItem(Object selected);
}
