package com.kreadi.swing;

import java.awt.event.*;
import javax.swing.*;
import java.beans.*;

/*
 *  This class listens for changes made to the data in the table via the
 *  TableCellEditor. When editing is started, the value of the cell is saved
 *  When editing is stopped the new value is saved. When the oold and new
 *  values are different, then the provided Action is invoked.
 *
 *  The source of the Action is a KTableCellListener instance.
 */
public class KTableCellListener implements PropertyChangeListener, Runnable {

    public final JTable table;
    private Action action;

    private int row;
    private int column;
    public Object oldValue;
    public Object newValue;

    /**
     * Create a TableCellListener.
     *
     * @param table the table to be monitored for data changes
     * @param action the Action to invoke when cell data is changed
     */
    public KTableCellListener(JTable table, AbstractAction action) {
        this.table = table;
        this.action = action;
        this.table.addPropertyChangeListener(this);
    }

    /**
     * Create a TableCellListener with a copy of all the data relevant to the change of data for a given cell.
     *
     * @param row the row of the changed cell
     * @param column the column of the changed cell
     * @param oldValue the old data of the changed cell
     * @param newValue the new data of the changed cell
     */
    private KTableCellListener(JTable table, int row, int column, Object oldValue, Object newValue) {
        this.table = table;
        this.row = row;
        this.column = column;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    /**
     * Get the column that was last edited
     *
     * @return the column that was edited
     */
    public int getColumn() {
        return column;
    }

    /**
     * Get the new value in the cell
     *
     * @return the new value in the cell
     */
    public Object getNewValue() {
        return newValue;
    }

    /**
     * Get the old value of the cell
     *
     * @return the old value of the cell
     */
    public Object getOldValue() {
        return oldValue;
    }

    /**
     * Get the row that was last edited
     *
     * @return the row that was edited
     */
    public int getRow() {
        return row;
    }

    /**
     * Get the table of the cell that was changed
     *
     * @return the table of the cell that was changed
     */
    public JTable getTable() {
        return table;
    }

    @Override
    public void propertyChange(PropertyChangeEvent e) {
        //  A cell has started/stopped editing
        if ("tableCellEditor".equals(e.getPropertyName())) {
            if (table.isEditing()) {
                processEditingStarted();
            } else {
                processEditingStopped();
            }
        }
    }

    /*
     *  Save information of the cell about to be edited
     */
    private void processEditingStarted() {
        //  The invokeLater is necessary because the editing row and editing
        //  column of the table have not been set when the "tableCellEditor"
        //  PropertyChangeEvent is fired.
        //  This results in the "run" method being invoked

        SwingUtilities.invokeLater(this);
    }

    @Override
    public void run() {
        row = table.convertRowIndexToModel(table.getEditingRow());
        column = table.convertColumnIndexToModel(table.getEditingColumn());
        if (column != -1 && row != -1) {
            oldValue = table.getModel().getValueAt(row, column);
            newValue = null;
        }
    }

    /*
     *	Update the Cell history when necessary
     */
    private void processEditingStopped() {
        newValue = table.getModel().getValueAt(row, column);
        //  The data has changed, invoke the supplied Action
        if ((newValue != null && !newValue.equals(oldValue)) || (oldValue != null && !oldValue.equals(newValue))) {
            //  Make a copy of the data in case another cell starts editing
            //  while processing this change
            KTableCellListener tcl = new KTableCellListener(getTable(), getRow(), getColumn(), getOldValue(), getNewValue());
            ActionEvent event = new ActionEvent(tcl, ActionEvent.ACTION_PERFORMED, "");
            action.actionPerformed(event);
        }
    }
}
