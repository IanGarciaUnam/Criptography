/*
 * -- StringFreqTableModel.java --
 *
 * Version       Changes
 * 0.01          First implementation
 *
 *
 * Copyright (C) 2003, 2004  Jesús Adolfo García Pasquel
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package net.sourceforge.ganzua.text;

import java.util.List;
import java.util.Iterator;
import java.util.Collections;
import java.text.Collator;
import java.text.NumberFormat;
import java.text.DecimalFormat;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.InputEvent;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.event.TableModelListener;
import javax.swing.event.TableModelEvent;

/**
 * <code>TableModel</code> used to create <code>JTables</code> that display the
 * data in a <code>List</code> of <code>StringFreq</code>s and lets the user
 * sort the data by string or frequency.
 *
 * @author Jesús Adolfo García Pasquel
 * @version 0.01 June 2003
 */
public class StringFreqTableModel extends AbstractTableModel
                                  implements TableModelListener{

    protected static final byte UNSORTED = 0;

    protected static final byte SORTED_BY_STRING = 4;

    protected static final byte SORTED_BY_FREQUENCY = 2;

    /**
     * Mask used to indicate if the data is sorted in reverse order.
     * Used along with <code>SORTED_BY_STRING</code> or
     * <code>SORTED_BY_FREQUENCY</code>. */
    protected static final byte REVERSED = 1;

    protected static final DecimalFormat df;

    /**
     * Comparator used to sort <code>sfLst</code> by frequency in descending
     * order.
     */
    protected static final StringFreqFComparator sfFreqComp = new StringFreqFComparator();

    /**
     * <code>List</code> of <code>StringFreq</code>s that stores the data to be
     * displayed in the table.
     */
    protected List sfLst;

    /**
     * <code>long</code> that stores the sum of the frequencies in
     * <code>sfLst</code>. It is updated if the data is changed using the 
     * methods provided by this class, but if <code>sfLst</code> is altered in
     * some other way, <code>updateTotal()</code> should be called.
     */
    protected long total;

    /**
     * Array of <code>Strings</code> with the names of the two columns
     */
    protected String[] colNames;

    /**
     * <code>byte</code> used to store the state of the table
     * (<code>UNSORTED</code>, <code>SORTED_BY_STRING</code>,
     * <code>SORTED_BY_FREQUENCY</code>, <code>REVERSED</code>)
     */
    protected byte state = UNSORTED;

    static{
	NumberFormat nf = DecimalFormat.getInstance(JComponent.getDefaultLocale());
	if(nf instanceof DecimalFormat){
	    df = (DecimalFormat)nf;
	} else{
	    df = new DecimalFormat();
	}
	df.applyPattern("0.##########");
    }

    /**
     * Constructor that sets the list of <code>StringFreq</code>s that stores 
     * the table's data (<code>sfLst</code>) to <code>data</code>.
     * <code>sfLst</code> is a shallow copy of <code>data</code>.
     *
     * @param data a <code>List</code> of <code>StringFreq</code>s
     * @param columnNames an array of <code>String</code>s of length 2 with
     *                    the names of the columns
     * @throws NullPointerException if <code>data</code> or <code>columnNames</code> is <code>null</code>
     */
    public StringFreqTableModel(List data, String[] columnNames){
	if(data==null || columnNames==null){
	    throw new NullPointerException();
	}
	sfLst = data;
	colNames = columnNames;
	updateTotal();
	sort(0, true);
    }

    /**
     * Sets <code>total</code> to the sum of the frequencies in the list.
     * This method should be called when the list of data is modified
     * directly (not using the methods provided by this class).
     */
    public void updateTotal(){
	Iterator it = sfLst.iterator();
	total = 0;
	while(it.hasNext()){
	    total += ((StringFreq)it.next()).getFrequency();
	}
	if(total == 0){ // avoid division by 0 in method getValueAt()
	    total = 1;
	}
    }

    /**
     * Returns the number of columns in the model (<code>2</code>).
     *
     * @return the number of columns in the model
     */
    public int getColumnCount(){
	return 2;
    }

    /**
     * Returns the name of the column <code>col</code>
     *
     * @param col the column being queried
     */
    public String getColumnName(int col){
	return colNames[col];
    }

    /**
     * Returns the number of rows in the model
     *
     * @return the number of rows in the model
     */
    public int getRowCount(){
	return sfLst.size();
    }

    /**
     * Returns the value for the cell at <code>col</code> and <code>row</code>
     *
     * @param row the row's index
     * @param col the column's index
     */
    public Object getValueAt(int row, int col){
	if(col == 0){
	    return ((StringFreq)sfLst.get(row)).getString();
	}else{
	    return df.format((double)((StringFreq)sfLst.get(row)).getFrequency()/(double)total);
	}
    }

    /**
     * The class of all the cell values in the column.
     */
    public Class getColumnClass(int col){
	return String.class;
    }

    /**
     * Method that creates a <code>TableModelEvent</code> and fires
     * and notifies all the listeners
     */
    private final void tableChanged(){
	tableChanged(new TableModelEvent(this));
    }

    /**
     * Method that forwards the event <code>e</code> to all the listeners.
     */
    public void tableChanged(TableModelEvent e){
	fireTableChanged(e);
    }

    /**
     * Sorts the data by <code>column</code> in ascending or descending order.
     * Note that for column 1 (the column with the frequencies), ascending
     * actually means descending (from the most frequent to the least frequent).
     *
     * @param column the column to order by
     * @param ascending <code>true</code> if the data should be sorded in 
     *                  ascending order, <code>false</code> otherwise
     */
    protected void sort(int column, boolean ascending){
	if(column < 0){
	    return;
	}
	byte prevState = state;
	if(column==0){ 
	    if((state&SORTED_BY_STRING) != SORTED_BY_STRING){
		Collections.sort(sfLst);
		state = SORTED_BY_STRING;
	    }
	}else if((state&SORTED_BY_FREQUENCY) != SORTED_BY_FREQUENCY){
	    Collections.sort(sfLst, sfFreqComp);
	    state = SORTED_BY_FREQUENCY;
	}
	if(!ascending && (state&REVERSED)!=REVERSED){
	    Collections.reverse(sfLst);
	    state |= REVERSED;
	}else if(ascending && (state&REVERSED)==REVERSED){
		Collections.reverse(sfLst);
		state &= ~REVERSED;
	}
	if(state != prevState){
	    tableChanged();
	}
    }

    /**
     * Sets the table's data to that in <code>data</code>.
     * (<code>sfLst</code> becomes a shallow copy of <code>data</code>)
     *
     * @param data a <code>List</code> of <code>StringFreq</code>s
     * @throws NullPointerException if <code>data</code> is <code>null</code>
     */
    public void setData(List data) throws NullPointerException
    {
	if(data == null){
	    throw new NullPointerException();
	}
	byte sorted = state;
	boolean ascending = !((sorted&REVERSED)==REVERSED);
	state = UNSORTED;
	sfLst = data;
	updateTotal();
	if((sorted&SORTED_BY_FREQUENCY)==SORTED_BY_FREQUENCY){
	    sort(1, ascending);
	}else{
	    sort(0, ascending);
	}
    }

    /**
     * Changes the <code>Collator</code> that the <code>StringFreq</code>s
     * in the <code>StringFreqTableModel</code> use as their
     * <code>CollationKey</code> generator.<br/>
     * The <code>CollationKey</code>s used by the <code>StringFreq</code>s
     * are discarded and new ones are generated using this 
     * <code>Collator</code>.
     *
     * @param collator the new <code>Collator</code> to use
     * @throws NullPointerException if <code>collator</code> is <code>null</code>
     */
    public void useCollator(Collator collator) throws NullPointerException
    {
	Iterator it = sfLst.iterator();
	while(it.hasNext()){
	    ((StringFreq)it.next()).useCollator(collator);
	}
	byte sorted = state;
	boolean ascending = !((sorted&REVERSED)==REVERSED);
	state = UNSORTED;
	if((sorted&SORTED_BY_FREQUENCY)==SORTED_BY_FREQUENCY){
	    sort(1, ascending);
	}else{
	    sort(0, ascending);
	}
    }

    /**
     * Adds the <code>MouseListener</code> needed to let the user sort the
     * data.
     *
     * @param table the <code>JTable</code> created using this instance of
     *              <code>StringFreqTableModel</code>
     */
    public void addMouseListenerToHeaderInTable(final JTable table){
	table.setColumnSelectionAllowed(false);
	MouseAdapter mouseListener = new MouseAdapter(){
		public void mouseClicked(MouseEvent e){
		    int viewCol = table.getColumnModel().getColumnIndexAtX(e.getX());
		    int column = table.convertColumnIndexToModel(viewCol);
		    if(e.getClickCount() == 1 && column != -1){
			boolean ascending = (e.getModifiers()&InputEvent.SHIFT_MASK)==0;
			sort(column, ascending);
		    }
		}
	    };
	table.getTableHeader().addMouseListener(mouseListener);
    }
}
/*
 * -- StringFreqTableModel.java ends here --
 */
