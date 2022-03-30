/*
 * -- KasiskiTableModel.java --
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

import java.util.ResourceBundle;
import java.util.List;
import java.util.Collections;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.InputEvent;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.event.TableModelListener;
import javax.swing.event.TableModelEvent;

/**
 * <code>TableModel</code> used to create <code>JTables</code> that display
 * the data contained in a <code>List</code> of <code>KasiskiEntry</code>s.
 *
 * @author Jesús Adolfo García Pasquel
 * @version 0.01 Mar 2004
 */
public class KasiskiTableModel extends AbstractTableModel
                               implements TableModelListener
{
    protected static final byte UNSORTED = 0;

    protected static final byte SORTED_BY_SEQUENCE_LENGTH = 4;

    protected static final byte SORTED_BY_FREQUENCY = 2;

    protected static final byte REVERSED = 1;

    /**
     * Comparator used to sort <code>keLst</code> by frequency in descending
     * order.
     */
    protected static final KasiskiEntryFreqComparator kefComp = new KasiskiEntryFreqComparator();

    /**
     * <code>List</code> of <code>KasiskiEntry</code>s that stores the data 
     * to be displayed in the table. */
    protected List keLst;

    /**
     * <code>ResourceBundle</code> with the localized names of the columns */
    protected ResourceBundle labelsRB;

    /**
     * <code>byte</code> used to store the state of the table 
     * (<code>UNSORTED</code>, <code>SORTED_BY_SEQUENCE_LENGTH</code>, 
     * <code>SORTED_BY_FREQUENCY</code>, <code>REVERSED</code>) */
    protected byte state = UNSORTED;

    /**
     * Constructor that sets the list of <code>KasiskiEntry</code>s that
     * stores the table's data (<code>keLst</code>) to <code>data</code>.
     * <code>keLst</code> is a shallow copy of <code>data</code>.
     *
     * @param data a <code>List</code> of <code>KasiskiEntry</code>s
     * @throws NullPointerException if <code>data</code> is <code>null</code>
     */
    public KasiskiTableModel(List data){
	if(data == null){
	    throw new NullPointerException();
	}
	keLst = data;
	labelsRB = ResourceBundle.getBundle(KasiskiTableModel.class.getName(),
					    JComponent.getDefaultLocale());
	sort(0, true);
    }

    /**
     * Returns the number of columns in the model (<code>4</code>).
     *
     * @return the number of columns in the model (<code>4</code>)
     */
    public int getColumnCount(){
	return 4;
    }

   /**
     * Returns the name of the column <code>col</code> or <code>null</code>
     * if no such column exists.
     *
     * @param col the column being queried
     * @return the name of the column or <code>null</code> if no such column
     *         exists
     */
    public String getColumnName(int col){
	String ret;
	switch(col){
	case 0: 
	    ret = labelsRB.getString("seq");
	    break;
	case 1: 
	    ret = labelsRB.getString("freq");
	    break;
	case 2: 
	    ret = labelsRB.getString("dist");
	    break;
	case 3: 
	    ret = labelsRB.getString("fact");
	    break;
	default: 
	    ret = null;
	    break;
	}
	return ret;
    }

    /**
     * Returns the number of rows in the model
     *
     * @return the number of rows in the model
     */
    public int getRowCount(){
	return keLst.size();
    }

    /**
     * Returns the value for the cell at <code>col</code> and <code>row</code>
     *
     * @param row the row's index
     * @param col the column's index
     */
    public Object getValueAt(int row, int col){
	String ret = null;
	StringBuffer sb;
	switch(col){
	case 0:
	    ret = ((KasiskiEntry)keLst.get(row)).getSequenceAsString();
	    break;
	case 1:
	    ret = Integer.toString(((KasiskiEntry)keLst.get(row)).getFrequency());
	    break;
	case 2:
	    sb = new StringBuffer();
	    int[] distances = ((KasiskiEntry)keLst.get(row)).getDistances();
	    int last = distances.length-1;
	    for(int i=0; i<last; i++){
		sb.append(distances[i]).append(", ");
	    }
	    sb.append(distances[last]);
	    ret = sb.toString();
	    break;
	case 3:
	    sb = new StringBuffer();
	    int[][] distFactors = ((KasiskiEntry)keLst.get(row)).getDistFactors();
	    int i;
	    int j;
	    for(i=0; i<distFactors.length-1; i++){
		for(j=0; j<distFactors[i].length-1; j++){
		    sb.append(distFactors[i][j]).append(", ");
		}
		sb.append(distFactors[i][j]).append(" / ");
	    }
	    for(j=0; j<distFactors[i].length-1; j++){
		sb.append(distFactors[i][j]).append(", ");
	    }
	    sb.append(distFactors[i][j]);
	    ret = sb.toString();
	    break;
	default:
	    break;
	}
	return ret;
    }

    /**
     * The class of all the cell values in the column.
     *
     * @return <code>String</code>
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
     * actually means descending (from the most frequent to the least 
     * frequent).
     *
     * @param column the column to order by
     * @param ascending true if the data should be sorded in ascending order,
     *                  false otherwise
     */
    protected void sort(int column, boolean ascending){
	if(column < 0 || column > 1){
	    return;
	}
	byte prevState = state;
	if(column==0){ 
	    if((state&SORTED_BY_SEQUENCE_LENGTH) != SORTED_BY_SEQUENCE_LENGTH){
		Collections.sort(keLst);
		state = SORTED_BY_SEQUENCE_LENGTH;
	    }
	}else if((state&SORTED_BY_FREQUENCY) != SORTED_BY_FREQUENCY){
	    Collections.sort(keLst, kefComp);
	    state = SORTED_BY_FREQUENCY;
	}
	if(!ascending && (state&REVERSED)!=REVERSED){
	    Collections.reverse(keLst);
	    state |= REVERSED;
	}else if(ascending && (state&REVERSED)==REVERSED){
		Collections.reverse(keLst);
		state &= ~REVERSED;
	}
	if(state != prevState){
	    tableChanged();
	}
    }

    /**
     * Sets the table's data to that in <code>data</code>.
     * (<code>keLst</code> becomes a shallow copy of <code>data</code>)
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
	keLst = data;
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
 * -- KasiskiTableModel.java ends here --
 */
