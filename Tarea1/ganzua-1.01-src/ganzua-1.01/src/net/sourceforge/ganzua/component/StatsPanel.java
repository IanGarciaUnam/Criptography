/*
 * -- StatsPanel.java --
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

import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.text.Collator;
import net.sourceforge.ganzua.handler.*;
import net.sourceforge.ganzua.text.*;

/**
 * Component that displays relative frequencies of characters, digrams and
 * trigrams (in the case of monoalphabetic statistics) or frequencies of
 * characters in different columns (polyalphabetic statistics). The relative
 * frequencies are displayed in tables available through different tabs.
 *
 * @author Jesús Adolfo García Pasquel
 * @version 0.01 June 2003
 */
public class StatsPanel extends JTabbedPane{

    /**
     * <code>List</code> of <code>StringFreqTableModel</code>s for the 
     * tables of relative frequencies.
     */
    protected ArrayList models;

    /**
     * The type of statistics the component holds (either 
     * <code>Substitution.MONOALPHABETIC</code> or 
     * <code>Substitution.POLYALPHABETIC</code>) */
    protected byte type;

    /**
     * <code>ResourceBundle</code> used to get localized labels
     */
    protected ResourceBundle labelsRB;


    /**
     * Creates a <code>StatsPanel</code> that reflects the data (relative
     * frequencies) in the <code>List</code> of <code>Collection</code>s 
     * of <code>StringFreq</code>s passed.<br/>
     *
     * In the case of monoalphabetic statistics, <code>collections</code> 
     * should be a <code>List</code> of size 3, in which, at index 1 the
     * <code>Collection</code> of <code>StringFreq</code>s represents the
     * relative frequencies of the characters, the one at index 2 the relative
     * frequencies of the bigrams and that at index 3 the relative frequencies
     * of the trigrams.<br/>
     *
     * For polyalphabetic statistics, <code>collections</code> should be a
     * <code>List</code> of size greater or equal to 2, in which at every
     * index is a <code>Collection</code> of </code>StringFreq</code>s that
     * represents the relative frequencies of the characters at the column of
     * the same index.
     *
     * @param collections a <code>List</code> of <code>Collection</code>s of
     *    <code>StringFreq</code>s that represent the relative frequencies
     * @param type <code>Substitution.MONOALPHABETIC</code> if
     *    <code>collections</code> or <code>Substitution.POLYALPHABETIC</code>,
     *    depending on the statistics <code>collections</code> contains
     * @throws NullPointerException if <code>collections</code> is <code>null</code>
     * @throws IllegalArgumentException if the contents of <code>collections</code> does not match the <code>type</code>
     * @see Substitution#MONOALPHABETIC
     * @see Substitution#POLYALPHABETIC
     */
    public StatsPanel(java.util.List collections, 
		      byte type) throws NullPointerException, 
					IllegalArgumentException
    {
	super();
	this.type = type;
	models = new ArrayList();
	if(collections == null){
	    throw new NullPointerException("collections can't be null");
	}
	if(!(type == Substitution.MONOALPHABETIC ||
	     type == Substitution.POLYALPHABETIC)){
	    throw new IllegalArgumentException("Invalid type");
	}
	labelsRB = ResourceBundle.getBundle(StatsPanel.class.getName(), 
					    getDefaultLocale());
	if(type == Substitution.MONOALPHABETIC){
	    if(collections.size()!=3){
		throw new IllegalArgumentException("collections' size must be 3");
	    }
	    Iterator it = collections.iterator();
	    int i = 0;
	    while(it.hasNext()){
		Collection col = (Collection)it.next();
		if(col == null){
		    throw new IllegalArgumentException("null Collection at "+
						       "index "+ i +
						       " of collections");
		}
		models.add(new StringFreqTableModel(new ArrayList(col),
						    labelsRB.getString("T"+i).split(":")));
		i++;
	    }
	}else{
	    if(collections.size()<2){
		throw new IllegalArgumentException("collections' size must be greater or equal to 2");
	    }
	    Iterator it = collections.iterator();
	    int i = 0;
	    while(it.hasNext()){
		Collection col = (Collection)it.next();
		if(col == null){
		    throw new IllegalArgumentException("null Collection at "+
						       "index "+ i +
						       " of collections");
		}
		models.add(new StringFreqTableModel(new ArrayList(col),
						    labelsRB.getString("T0").split(":")));
		i++;
	    }
	}
	initializeGUI();
    }

    /**
     * Creates a <code>StatsPanel</code> that reflects the data (relative
     * frequencies) in <code>handler</code>
     *
     * @param handler a <code>LanguageFrequenciesHandler</code> with data read
     *                from an instance of <code>LanguageFrequencies.xsd</code>
     * @throws NullPointerException if <code>handler</code> is <code>null</code>
     */
    public StatsPanel(LanguageFrequenciesHandler handler) throws NullPointerException
    {
	this(putFreqsInList(handler), Substitution.MONOALPHABETIC);
    }

    /**
     * Puts <code>h.getAlphabet()</code>, <code>h.getBigrams()</code> and
     * <code>h.getTrigrams()</code> in a <code>List</code> */
    private static final ArrayList putFreqsInList(LanguageFrequenciesHandler h) throws NullPointerException
    {
	if(h == null){
	    throw new NullPointerException();
	}
	ArrayList lst = new ArrayList();
	lst.add(h.getAlphabet());
	lst.add(h.getBigrams());
	lst.add(h.getTrigrams());
	return lst;
    }

    /**
     * Method that sets the component's GUI
     */
    private final void initializeGUI(){
	if(type == Substitution.MONOALPHABETIC){
	    Iterator it = models.iterator();
	    int i=0;
	    while(it.hasNext()){
		StringFreqTableModel model = (StringFreqTableModel)it.next();
		JTable table = new JTable(model);
		table.setDragEnabled(true); // enable automatic drag handling
		JScrollPane sp = newJScrollPane(table);
		model.addMouseListenerToHeaderInTable(table);
		this.addTab(labelsRB.getString("Tab"+i), sp);
		this.setToolTipTextAt(i, labelsRB.getString("TT"+i));
		i++;
	    }
	}else{
	    Iterator it = models.iterator();
	    int i=0;
	    while(it.hasNext()){
		StringFreqTableModel model = (StringFreqTableModel)it.next();
		JTable table = new JTable(model);
		table.setDragEnabled(true); // enable automatic drag handling
		JScrollPane sp = newJScrollPane(table);
		model.addMouseListenerToHeaderInTable(table);
		this.addTab(replace("NUM", Integer.toString(i+1),
				    labelsRB.getString("TabP")),
			    sp);
		this.setToolTipTextAt(i, 
				      replace("NUM", Integer.toString(i+1),
					      labelsRB.getString("TTP")));
		i++;
	    }
	}
    }

    /**
     * Replaces the occurrences of <code>str</code> in <code>string</code>
     * by <code>rep</code>.
     *
     * @param str the sequence to be replaced
     * @param rep the replacement sequence
     * @param string the <code>String</code> to operate on
     * @return <code>string</code> with all the occurrences of <code>str</code>
     *         replaced by <code>rep</code>.
     * @throws NullPointerException if any of the arguments is <code>null</code>
     */
    private static final String replace(String str,
					String rep,
					String string)
	throws NullPointerException
    {
	if(str==null || rep==null || string==null){
	    throw new NullPointerException();
	}
	StringBuffer strBuf = new StringBuffer(string);
	int i = strBuf.indexOf(str);
	int strLen = str.length();
	int last = 0;
	while(i >= 0){
	    last = i+strLen;
	    strBuf.replace(i, last, rep);
	    i = strBuf.indexOf(str, last);
	}
	return strBuf.toString();
    }

    /**
     * Convenience method that creates a <code>JScollPane</code>
     * that contains the <code>Component</code> <code>c</code> and
     * has <code>VERTICAL_SCROLLBAR_ALWAYS</code> and 
     * <code>HORIZONTAL_SCROLLBAR_NEVER</code> policies.
     *
     * @param c the <code>Component</code> to display in the
     *          <code>JScrollPane</code>
     */
    private static final JScrollPane newJScrollPane(Component c){
	return new JScrollPane(c,
			       JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
			       JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    }

    /**
     * Set the data in the table at index <code>tableIdx</code> to that
     * in the <code>Collection</code> of <code>StringFreq</code>s 
     * <code>data</code>.<br/>
     *
     * In <code>StatPanel</code>s that contain monoalphabetic stats,
     * the table with the character frequencies is at index 0, bigrams 
     * at 1, and trigrams at 2. <code>StatPanel</code>s that contain 
     * polyalphabetic stats, have the stats of each alphabet at
     * <code>columnIndex-1</code> (e.g. the stats for the first column are
     * at index 0)
     *
     * @param data a <code>Collection</code> of <code>StringFreq</code>s
     * @param tableIdx the index of the table to change the data of
     * @throws NullPointerException if <code>data</code> is <code>null</code>
     * @throws IllegalArgumentException if tableIdx is out of bounds
     */
    public void setTableData(Collection data,
			     int tableIdx) throws NullPointerException, 
						  IllegalArgumentException
    {
	if(data == null){
	    throw new NullPointerException();
	}
	if(tableIdx<0 || tableIdx>=models.size()){
	    throw new IllegalArgumentException();
	}
	StringFreqTableModel sftm = (StringFreqTableModel)models.get(tableIdx);
	sftm.setData(new ArrayList(data));
    }

    /**
     * Makes this <code>StatsPanel</code> reflect the data (relative
     * frequencies) in the <code>List</code> of <code>Collection</code>s 
     * of <code>StringFreq</code>s passed.<br/>
     *
     * In the case of monoalphabetic statistics, <code>collections</code> 
     * should be a <code>List</code> of size 3, in which, at index 1 the
     * <code>Collection</code> of <code>StringFreq</code>s represents the
     * relative frequencies of the characters, the one at index 2 the relative
     * frequencies of the bigrams and that at index 3 the relative frequencies
     * of the trigrams.<br/>
     *
     * For polyalphabetic statistics, <code>collections</code> should be a
     * <code>List</code> of size greater or equal to 2, in which at every
     * index is a <code>Collection</code> of </code>StringFreq</code>s that
     * represents the relative frequencies of the characters at the column of
     * the same index.
     *
     * @param collections a <code>List</code> of <code>Collection</code>s of
     *                   <code>StringFreq</code>s that represent the relative
     *                   frequencies
     * @param type <code>Substitution.MONOALPHABETIC</code> or
     *             <code>Substitution.POLYALPHABETIC</code>, depending on the
     *             statistics <code>collections</code> contains
     * @throws NullPointerException if <code>collections</code> is <code>null</code>
     * @throws IllegalArgumentException if the contents of <code>collections</code> does not match the <code>type</code>
     * @see Substitution#MONOALPHABETIC
     * @see Substitution#POLYALPHABETIC
     */
    public void setData(java.util.List collections, 
			byte type) throws IllegalArgumentException
    {
	if(collections == null){
	    throw new NullPointerException("collections can't be null");
	}
	if(!(type == Substitution.MONOALPHABETIC ||
	     type == Substitution.POLYALPHABETIC)){
	    throw new IllegalArgumentException("Invalid type");
	}
	this.type = type;
	models.clear();
	if(type == Substitution.MONOALPHABETIC){
	    if(collections.size()!=3){
		throw new IllegalArgumentException("collections' size must be 3");
	    }
	    Iterator it = collections.iterator();
	    int i = 0;
	    while(it.hasNext()){
		Collection col = (Collection)it.next();
		if(col == null){
		    throw new IllegalArgumentException("null Collection at "+
						       "index "+ i +
						       " of collections");
		}
		models.add(new StringFreqTableModel(new ArrayList(col),
						    labelsRB.getString("T"+i).split(":")));
		i++;
	    }
	}else{
	    if(collections.size()<2){
		throw new IllegalArgumentException("collections' size must be greater or equal to 2");
	    }
	    Iterator it = collections.iterator();
	    int i = 0;
	    while(it.hasNext()){
		Collection col = (Collection)it.next();
		if(col == null){
		    throw new IllegalArgumentException("null Collection at "+
						       "index "+ i +
						       " of collections");
		}
		models.add(new StringFreqTableModel(new ArrayList(col),
						    labelsRB.getString("T0").split(":")));
		i++;
	    }
	}
	removeAll(); //remove all tabs and their components
	initializeGUI();
	revalidate();
    }

    /**
     * Makes this <code>StatsPanel</code> reflect the data (character,
     * bigram and trigram relative frequencies) in the 
     * <code>LanguageFrequenciesHandler</code> passed.<br/>
     *
     * @param handler a <code>LanguageFrequenciesHandler</code>
     *
     * @throws NullPointerException if <code>handler</code> is <code>null</code>
     */
    public void setData(LanguageFrequenciesHandler handler) throws NullPointerException
    {
	setData(putFreqsInList(handler), Substitution.MONOALPHABETIC);
    }

    /**
     * Used to change the <code>Collator</code> the <code>StringFreq</code>s
     * in the <code>StringFreqTableModel</code>s use as their
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
	Iterator it = models.iterator();
	while(it.hasNext()){
	    ((StringFreqTableModel)it.next()).useCollator(collator);
	}
    }
}
/*
 * -- StatsPanel.java ends here --
 */
