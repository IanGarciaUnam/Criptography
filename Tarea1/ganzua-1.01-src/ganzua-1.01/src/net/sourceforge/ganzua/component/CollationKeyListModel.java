/*
 * -- CollationKeyListModel.java --
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

package net.sourceforge.ganzua.component;

import java.text.CollationKey;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.AbstractListModel;

/**
 * <code>ListModel</code> used to display <code>CollationKey</code>s as
 * <code>String</code>s in components like <code>JList</code>.
 *
 * @author Jesús Adolfo García Pasquel
 * @version 0.01 September 2003
 */
public class CollationKeyListModel extends AbstractListModel{

    /**
     * <code>ArrayList</code> that contains the <code>CollationKey</code>s
     */
    protected ArrayList ckList;

    /**
     * Creates an empty <code>CollationKeyListModel</code>.
     */
    public CollationKeyListModel(){
	ckList = new ArrayList();
    }

    /**
     * Creates a <code>CollationKeyListModel</code> that contains the 
     * elements of the specified <code>List</code> of 
     * <code>CollationKey</code>s. If <code>list</code> is <code>null</code>
     * an empty <code>CollationKeyListModel</code> is created.
     *
     * @param list the <code>List</code> of <code>CollationKeys</code> whose
     *             elements are to be placed into this model.
     * @throws IllegalArgumentException if any of the elements in 
     *             <code>list</code> is not a <code>CollationKey</code>
     */
    public CollationKeyListModel(List list) throws IllegalArgumentException
    {
	if(list == null){
	    ckList = new ArrayList();
	}else{
	    for(Iterator iter = list.iterator(); iter.hasNext(); ){
		if(!(iter.next() instanceof CollationKey)){
		    throw new IllegalArgumentException("Not a list of CollationKeys");
		}
	    }
	    ckList = new ArrayList(list);
	}
    }

    /**
     * Returns the length of the list.
     *
     * @return the length of the list.
     */
    public int getSize(){
	return ckList.size();
    }

    /**
     * Returns the <code>String</code> that the <code>CollationKey</code> at 
     * the specified index represents.
     *
     * @param index the requested index
     * @return the <code>String</code> that the <code>CollationKey</code> at
     *         the specified index represents.
     */
    public Object getElementAt(int index){
	return ((CollationKey)ckList.get(index)).getSourceString();
    }

    /**
     * Returns the <code>CollationKey</code> at the specified index.
     *
     * @param index the requested index
     * @return the <code>CollationKey</code> at the specified index.
     * @throws IndexOutOfBoundsException if <code>index</code> is out of range
     *                      (<code>index &lt; 0 || index &gt;= size()</code>).
     */
    public CollationKey getCollationKeyAt(int index) 
	throws IndexOutOfBoundsException
    {
	return (CollationKey)ckList.get(index);
    }

    /**
     * Removes the <code>CollationKey</code> at the specified index from
     * the model.
     *
     * @param index the index of the element to remove
     * @return the <code>CollationKey</code> removed from the model
     */
    public CollationKey removeElementAt(int index){
	CollationKey ret = (CollationKey)ckList.remove(index);
	fireIntervalRemoved(this, index, index);
	return ret;
    }

    /**
     * Removes all the elements from the model.
     */
    public void clear(){
	int last = ckList.size()-1;
	last = last>=0 ? last : 0;
	ckList.clear();
	fireIntervalRemoved(this, 0, last);
    }

    /**
     * Inserts the specified <code>CollationKey</code> at the specified 
     * position. Shifts the element currently at that position (if any)
     * and any subsequent elements to the right (adds one to their indices).
     *
     * @param index index at which the specified element is to be inserted
     * @param ck <code>CollationKey</code> to be inserted
     * @throws IndexOutOfBoundsException if index is out of range 
     *                      (<code>index &lt; 0 || index &gt; getSize()</code>)
     */
    public void add(int index, 
		    CollationKey ck) throws IndexOutOfBoundsException
    {
	ckList.add(index, ck);
	fireIntervalAdded(this, index, index);
    }
}
/*
 * -- CollationKeyListModel.java ends here --
 */
