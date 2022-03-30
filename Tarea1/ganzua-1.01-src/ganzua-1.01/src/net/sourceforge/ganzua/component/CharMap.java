/*
 * -- CharMap.java --
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

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.text.*;

/**
 * Component that lets the user choose a replacement character.<br/><br/>
 *
 * Note: Do <u>not</u> use the <code>add</code> or <code>remove</code>
 * methods inherited from <code>java.awt.Container</code>.
 * <br/><br/>
 * Known Bugs: At times the combo box does not display it's contents
 * properly on Mac OS X (a blank or nearly blank popup menu is displayed).
 *
 * @author Jesús Adolfo García Pasquel
 * @version 0.01 August 2003
 */
public class CharMap extends JPanel implements Comparable{

    //icons for the ignore button
    private static final ImageIcon ignoreIcn;

    private static final ImageIcon ignoreIcn_over;

    private static final ImageIcon ignoreIcn_pressed;

    private static final Dimension ignoreDim;

    /**
     * <code>ResourceBundle</code> with localized labels */
    private ResourceBundle labelsRB;

    /**
     * <code>JLabel</code> that displays the character to be substituted */
    protected JLabel uCharL;

    /**
     * The user character to be substituted */
    protected CollationKey uChar;

    /**
     * <code>JComboBox</code> that lets the user choose a replacement
     * character */
    protected JComboBox cbox;

    /**
     * The <code>ComboBoxModel</code> of the component's 
     * <code>JComboBox</code> */
    protected CharMapComboBoxModel model;

    /**
     * Used to register all the <code>ActionListeners</code> that have interest
     * on <code>cbox</code>.
     */
    private EventListenerList cboxListenerList = new EventListenerList();

    /**
     * Button that lets the user 'ignore' the character.<br/>
     * Note: the private method <code>initializeGUI()</code> adds an 
     * <code>ActionListener</code> that clears the selected replacement
     * character and makes the component invisible
     * (<code>setVisible(false)</code>);
     */
    protected JButton ignoreBtn;

    /**
     * Used to register all the <code>ActionListeners</code> that have interest
     * on <code>ignoreBtn</code>.
     */
    private EventListenerList ignoreBtnListenerList = new EventListenerList();

    /**
     * <code>ComboBoxModel</code> that allows many <code>JComboBoxes</code>
     * to share a list of choices (user characters) and may forbid any two
     * from having the same selection in <i>injective</i> mode (note that 
     * any two <code>JComboBoxes</code> may have 'no selection' even in 
     * injective mode).
     *
     * @author Jesús Adolfo García Pasquel
     * @version 0.01 August 2003
     */
    private class CharMapComboBoxModel extends AbstractListModel
	                               implements ComboBoxModel
    {
	/**
	 * Used to indicate if the selected user character should be
	 * removed from the selection list (to forbid other 
	 * <code>CharMap</code>s with the same list from selecting it) 
	 *  or not.
	 */
	protected boolean injective;

	/**
	 * Used to generate <code>CollationKey</code>s needed to identify
	 * the selection in <code>list</code>.
	 *
	 * @see #setSelectedItemNoEvent(Object)
	 */
	protected Collator collator;

	/**
	 * Ordered <code>List</code> of <code>CollationKey</code>s that can 
	 * be used in the substitution.
	 */
	protected ArrayList list;

	/**
	 * The selected user character */
	protected CollationKey selection;

	/**
	 * Constructor thar receives the <code>List</code> of
	 * <code>CollationKey</code>s and the <code>Collator</code>
	 * used to generate them.
	 *
	 * @param lst a sorted <code>ArrayList</code> of 
                      <code>CollationKey</code>s
	 * @param col the <code>Collator</code> used to generate the 
                      <code>CollationKey</code>s in <code>lst</code>
	 */
	public CharMapComboBoxModel(Collator col, ArrayList lst){
	    injective = true;
	    collator = col;
	    list =lst;
	    selection = null;
	}

	/**
	 * Returns the length of the selection list.
	 *
	 * @return the length of the selection list.
	 */
	public int getSize(){
	    return selection==null || !injective ? list.size()+1
		                                 : list.size()+2;
	}

	/**
	 * Returns the value at the specified index.
	 *
	 * @param index the index
	 * @return the value at the specified index.
	 */
	public Object getElementAt(int index){
	    Object ret = null;
	    switch(index){
	        case 0:
		    ret=""; break;
	        default: 
		    if(selection == null || !injective){
			ret = list.get(index-1);
		    } else{
		        int selIdx = -Collections.binarySearch(list, selection);
			if(selIdx <= 0){
			    selIdx = 1;
			}
			ret = selIdx>index ? list.get(index-1)
			    : selIdx<index ? list.get(index-2)
			    : selection;
		    }
		    ret = ((CollationKey)ret).getSourceString();
		    break;
	    }
	    return ret;
	}

	/**
	 * Sets the selected item, but does not notify any listener about
	 * the change.<br/>
	 * Note: Do not call this method unless you are certain that 
	 * <code>anItem</code> is among the selectable items.
	 *
	 * @param anItem the list object to select (a <code>String</code>) or
	 *               <code>null</code> to clear the selection
	 * @see #isSelectable(String)
	 */
	public void setSelectedItemNoEvent(Object anItem){
	    if(selection!=null && injective){
		int i = Collections.binarySearch(list, selection);
		i = i<0 ? -i-1 : i;
		list.add( i, selection);
	    }
	    selection= anItem=="" || anItem==null ? null 
		       : collator.getCollationKey((String)anItem);
	    if(injective){ // null can't be in a list of user characters
		list.remove(selection);
	    }
	}

	/**
	 * Sets the selected item and notifies the listeners about
	 * the change (<code>fireContentsChanged()</code>) as required
	 * by the <code>ComboBoxModel</code> interface.<br/>
	 * Note: Do not call this method unless you are certain that 
	 * <code>anItem</code> is among the selectable items.
	 *
	 * @param anItem the list object to select (a <code>String</code>) or 
         *               <code>null</code> to clear the selection
	 * @see #isSelectable(String)
	 */
	public void setSelectedItem(Object anItem){
	    setSelectedItemNoEvent(anItem);
	    /* notify of the selection as required by the ComboBoxModel
	       interface, but without specifying the selected index */
	    fireContentsChanged(this, 0, getSize()-1);
	}

	/**
	 * Returns <code>true</code> if <code>item</code> is among the 
	 * selectable items and <code>false</code> otherwise.
	 *
	 * @param item a <code>String</code>
	 * @return <code>true</code> if <code>item</code> is among the 
	 *         selectable items and <code>false</code> otherwise.
	 */
	public boolean isSelectable(String item){
	    return item==null || item=="" ? true :
		Collections.binarySearch(list, collator.getCollationKey(item)) >= 0;
	}

	/**
	 * Returns the selected item
	 *
	 * @return The selected item or <code>null</code> if there is no 
	 *         selection
	 */
	public Object getSelectedItem(){
	    return selection==null ? null : selection.getSourceString();
	}

	/**
	 * Returns the selected <code>CollationKey</code>
	 *
	 * @return The selected <code>CollationKey</code> or <code>null</code>
	 *         if there is no selection
	 */
	public CollationKey getSelectedCK(){
	    return selection;
	}

	/**
	 * Sets if the selected user character should be removed from the 
	 * selection list (to forbid other <code>CharMap</code>s with the
	 * same list from selecting it) or not.<br/>
	 *
	 * Note that two or more <code>CharMap</code>s may have no selection
	 * (<code>null</code>) even if <code>injective</code> is 
	 * <code>true</code>.Also note that this method does not notify 
	 * the combo box about the changes made (if any).<br/>
	 *
	 * To change <code>injective</code> from <code>true</code> to
	 * <code>false</code>, if the selected item is not <code>null</code>,
	 * it is added to <code>list</code>. To change from <code>false</code>
	 * to <code>true</code>, if the selected item is in <code>list</code>
	 * it is removed from it, if it is not, the selected item is changed
	 * to <code>null</code>.
	 * <br/><br/>
	 * <b>Note:</b> This method does <u>not</u> notify any listener about
	 * the change (i.e. does not call 
	 * <code>fireContentsChanged(Object, int, int)</code>).
	 * 
	 * @param inj the value <code>injective</code> should be set to
	 */
	public void setInjective(boolean inj){
	    if(injective == inj || selection == null){
		injective = inj;
		return;
	    }
	    if(inj == true){
		int i = Collections.binarySearch(list, selection);
		if(i>=0){
		    list.remove(i);
		}else{
		    selection = null;
		}
		// a repaint is needed since we are not notifying the
		// combo box about the change
		cbox.repaint();
	    }else{
		int i = Collections.binarySearch(list, selection);
		i = i<0 ? -i-1 : i;
		list.add( i, selection);
	    }
	    injective = inj;
	}

	/**
	 * Returns the instance of <code>Collator</code> being used.
	 *
	 * @return the instance of <code>Collator</code> being used.
	 */
	public Collator getCollator(){
	    return collator;
	}

	/**
	 * Sets the instance of <code>Collator</code> being used to the one 
	 * passed as argument.
	 *
	 * @param col the new <code>Collator</code>
	 */
	public void setCollator(Collator col){
	    if(collator == col){
		return;
	    }
	    collator = col;
	    selection = selection==null ? null :
		        collator.getCollationKey(selection.getSourceString());
	    String tmp = null;
	    for(int i=0; i<list.size(); i++){
		tmp = ((CollationKey)list.get(i)).getSourceString();
		list.set(i, collator.getCollationKey(tmp));
	    }
	    Collections.sort(list);
	}

	/**
	 * Clears the current selection and sets the list of choices to the
	 * sorted <code>ArrayList</code> <code>options</code> but does 
	 * not notify the listeners about the change.
	 *
	 * @param col the <code>Collator</code> used to generate the
	 *            <code>CollationKey</code>s in <code>options</code>
	 * @param options the sorted <code>ArrayList</code> of 
	 *                <code>CollationKey</code>s to be used as options
	 */
	public void setListNoEvent(Collator col, ArrayList options){
	    setSelectedItemNoEvent(null);
	    collator = col;
	    list = options;
	}

	/**
	 * Clears the current selection, sets the list of choices to the
	 * sorted <code>ArrayList</code> <code>options</code> and 
	 * notifies the listeners about the change.
	 *
	 * @param col the <code>Collator</code> used to generate the
	 *            <code>CollationKey</code>s in <code>options</code>
	 * @param options the sorted <code>ArrayList</code> of 
	 *                <code>CollationKey</code>s to be used as options
	 */
	public void setList(Collator col, ArrayList options){
	    setListNoEvent(col, options);
	    fireContentsChanged(this, 0, getSize()-1);
	}

	/**
	 * Since the contents of <code>list</code> may be changed by 
	 * other <code>CharMap</code>s that are sharing it, I needed a
	 * way to tell JComboBox to "reread" the contents before displaying
	 * the popup in order to avoid a problem in the GUI, in which the
	 * options in the <code>JComboBox</code> are not displayed. So I
	 * call this function in a <code>PopupMenuListener</code>.<br/>
	 *
	 * Kind of an ugly hack, but practical IMHO.
	 */
	public void fireContentsChanged(){
	    fireContentsChanged(this, 0, 0);
	}
    }

    // load the icons for the ignore button
    static{
	ignoreIcn = new ImageIcon(CharMap.class.getResource("images/X.png"));
	ignoreIcn_over = new ImageIcon(CharMap.class.getResource("images/X_ovr.png"));
	ignoreIcn_pressed = new ImageIcon(CharMap.class.getResource("images/X_prs.png"));
	ignoreDim = new Dimension(ignoreIcn.getIconWidth()+1, 
				  ignoreIcn.getIconHeight()+1);
    }

    /**
     * Constructor that receives the user character to be substituted,
     * a list of substitutions (user characters in the form of 
     * <code>CollationKey</code>s) and the <code>Collator</code> used
     * to generate the <code>CollationKey</code>s in the list
     * 
     * @param uChar the user character to be substituted
     * @param collator he <code>Collator</code> used to generate the
     *                 <code>CollationKey</code>s in <code>lst</code>
     * @param lst an <code>ArrayList</code> of <code>CollationKey</code>s
     */
    public CharMap(CollationKey uChar, Collator collator, ArrayList lst){
	super();
	labelsRB = ResourceBundle.getBundle(CharMap.class.getName(),
					    getDefaultLocale());
	this.uChar = uChar;
	model = new CharMapComboBoxModel(collator, lst);
	initializeGUI();
    }

    /**
     * Sets the component's border and layout, and adds the label, 
     * ignore button and combo box.<br/>
     * Also adds an <code>ActionListener</code> to the ignore button that
     * clears the selected replacement character, makes the component
     * invisible (<code>setVisible(false)</code>) and calls
     * <code>fireIgnoreBtnActionListener()</code>. Note that while clearing
     * the selected replacement character, no event is fired 
     * (<code>setSelectedItemNoEvent()</code> is used), so the combo box
     * listeners will not be notified of the change.
     */
    private final void initializeGUI(){
	final CharMap thisCharMap = this;
	setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	setBorder(new EmptyBorder(0, 2, 0, 2));
	JPanel topPan = new JPanel();
	uCharL = new JLabel(uChar.getSourceString());
	topPan.add(uCharL);
	ignoreBtn = new JButton();
	ignoreBtn.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent e){
		    thisCharMap.clearSelectionNoEvent();
		    thisCharMap.setVisible(false);
		    fireIgnoreBtnActionListener();
		}
	    });
	ignoreBtn.setIcon(ignoreIcn);
	ignoreBtn.setRolloverIcon(ignoreIcn_over);
	ignoreBtn.setPressedIcon(ignoreIcn_pressed);
	ignoreBtn.setToolTipText(labelsRB.getString("ignoreTT"));
	ignoreBtn.setPreferredSize(ignoreDim);
	ignoreBtn.setRolloverEnabled(true);
	ignoreBtn.setBorderPainted(false);
	ignoreBtn.setContentAreaFilled(false);
	ignoreBtn.setFocusPainted(false);
	topPan.add(ignoreBtn);
	add(topPan);
	/* construct the JComboBox and try to make it large enough to
	   display any character */
	String[] tmpArr = {"m", "W"};
	cbox = new JComboBox(tmpArr);
	cbox.setSelectedIndex(0);
	cbox.setPreferredSize(cbox.getPreferredSize());
	cbox.setModel(model);
	cbox.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent e){
		    fireCBoxActionListener();
		}
	    });
	cbox.addPopupMenuListener(new PopupMenuListener(){
		public void popupMenuCanceled(PopupMenuEvent e){}
		public void popupMenuWillBecomeInvisible(PopupMenuEvent e){}
		public void popupMenuWillBecomeVisible(PopupMenuEvent e){
		    model.fireContentsChanged();
		}
	    });
	add(cbox);
    }

    /**
     * Returns the user character to be replaced.<br/>
     * (The user character displayed in the <code>JLabel</code>)
     *
     * @return The user character to be replaced
     */
    public CollationKey getCharacter(){
	return uChar;
    }

    /**
     * Returns the replacement user character.
     *
     * @return The replacement character or <code>null</code> if there is no
     *         selection
     */
    public CollationKey getReplacement(){
	return model.getSelectedCK();
    }

    /**
     * Sets if the selected user character should be removed from the 
     * selection list (to forbid other <code>CharMap</code>s with the
     * same list from selecting it) or not.
     *
     * @see CharMapComboBoxModel#setInjective(boolean)
     * @param inj the value <code>injective</code> should be set to
     */
    public void setInjective(boolean inj){
	model.setInjective(inj);
    }

    /**
     * Sets the instance of <code>Collator</code> being used to the one 
     * passed as argument.
     *
     * @param col the new <code>Collator</code>
     * @throws NullPointerException if <code>col</code> is <code>null</code>
     */
    public void setCollator(Collator col) throws NullPointerException
    {
	if(col == null){
	    throw new NullPointerException();
	}
	if(col == model.getCollator()){
	    return;
	}
	uChar = col.getCollationKey(uChar.getSourceString());
	model.setCollator(col);
    }

    /**
     * Clears the current selection, sets the list of substitutions and
     * notifies the listeners registered using 
     * <code>addActionListenerToCBox(ActionListener)</code>
     *
     * @param col the <code>Collator</code> used to generate the 
     *            <code>CollationKey</code>s in <code>lst</code>
     * @param lst the sorted <code>ArrayList</code> of 
     *            <code>CollationKey</code>s to be used as options
     * @throws NullPointerException if any of the arguments is <code>null</code>
     * @see #addActionListenerToCBox(ActionListener)
     */
    public void setList(Collator col,
			ArrayList lst) throws NullPointerException
    {
	if(col == null || lst == null){
	    throw new NullPointerException();
	}
	uChar = col.getCollationKey(uChar.getSourceString());
	model.setList(col, lst);
    }

    /**
     * Clears the current selection and sets the list of substitutions but 
     * does not notify the listeners registered using 
     * <code>addActionListenerToCBox(ActionLIstener)</code> about the change.
     *
     * @param col the <code>Collator</code> used to generate the 
     *            <code>CollationKey</code>s in <code>lst</code>
     * @param lst the sorted <code>ArrayList</code> of 
     *            <code>CollationKey</code>s to be used as options
     * @throws NullPointerException if any of the arguments is <code>null</code>
     * @see #addActionListenerToCBox(ActionListener)
     */
    public void setListNoEvent(Collator col,
			ArrayList lst) throws NullPointerException
    {
	if(col == null || lst == null){
	    throw new NullPointerException();
	}
	uChar = col.getCollationKey(uChar.getSourceString());
	model.setListNoEvent(col, lst);
	repaint();
    }

    /**
     * Clears the selected item and notifies the listeners about the change.
     */
    public void clearSelection(){
	model.setSelectedItem(null);
    }

    /**
     * Clears the selected item but does not notify the listeners about the
     * change.
     */
    public void clearSelectionNoEvent(){
	model.setSelectedItemNoEvent(null);
	repaint();
    }

    /**
     * Sets the selected item, but does not notify the listeners about the 
     * change.
     *
     * @param item a <code>String</code> that is one of the selectable items.
     * @throws IllegalArgumentException if <code>item</code> is not among the 
     *                                  selectable items
     */
    public void setSelectedItemNoEvent(String item) throws IllegalArgumentException
    {
	if(!model.isSelectable(item)){
	    throw new IllegalArgumentException();
	}
	model.setSelectedItemNoEvent(item);
	repaint();
    }

    /**
     * Sets the selected item and notifies the listeners about the change
     * (<code>model.fireContentsChanged()</code>).
     *
     * @param item a <code>String</code> that is one of the selectable items.
     * @throws IllegalArgumentException if <code>item</code> is not among the
     *                                  selectable items
     */
    public void setSelectedItem(String item) throws IllegalArgumentException
    {
	if(!model.isSelectable(item)){
	    throw new IllegalArgumentException();
	}
	model.setSelectedItem(item);
    }

    /**
     * Compares this <code>CharMap</code> with the specified 
     * <code>Object</code>.
     * Returns a negative integer, zero, or a positive integer as this 
     * <code>CharMap</code> is less than, equal to, or greater than the 
     * given <code>Object</code>.<br/>
     *
     * Note: The comparison is made among the strings, ignoring the
     * frequencies.
     *
     * @param o an instance of <code>StringFreq</code>
     * @return A negative integer, zero, or a positive integer as this 
     *        <code>StringFreq</code> is less than, equal to, or greater than
     *        the given <code>Object</code>.
     * @throws ClassCastException if the specified <code>Object</code> is not a
     *                            <code>CharMap</code>
     */
    public int compareTo(Object o) throws ClassCastException
    {
	return uChar.compareTo(((CharMap)o).getCharacter());
    }

    /**
     * Adds an <code>ActionListener</code> to the combo box.<br/>
     * The <code>ActionListener</code> will receive an <code>ActionEvent</code>
     * when a selection has been made. Note that the <code>ActionEvent</code>'s
     * source will be the <code>CharMap</code> and not the
     * <code>JComboBox</code>
     *
     * @param l the <code>ActionListener</code> that should be notified
     */
    public void addActionListenerToCBox(ActionListener l){
	cboxListenerList.add(ActionListener.class, l);
    }

    /**
     * Removes an <code>ActionListener</code> from the combo box.
     *
     * @param l the <code>ActionListner</code> to be removed
     */
    public void removeActionListenerFromCBox(ActionListener l){
	cboxListenerList.remove(ActionListener.class, l);
    }

    /**
     * Adds an action listener to <code>ignoreBtn</code>.<br/>
     * Note that the <code>ActionEvent</code>'s source will be the 
     * <code>CharMap</code> and not the <code>JButton</code>
     *
     * @param l the <code>ActionListener</code> to be added
     */
    public void addActionListenerToIgnoreBtn(ActionListener l){
	ignoreBtnListenerList.add(ActionListener.class, l);
    }

    /**
     * Removes an <code>ActionListener</code> from <code>ignoreBtn</code>.
     *
     * @param l the <code>ActionListner</code> to be removed
     */
    public void removeActionListenerFromIgnoreBtn(ActionListener l){
	ignoreBtnListenerList.remove(ActionListener.class, l);
    }

    protected void fireCBoxActionListener(){
	// Guaranteed to return a non-null array
	Object[] listeners = cboxListenerList.getListenerList();
	ActionEvent actionEvent = new ActionEvent(this,
						  ActionEvent.ACTION_PERFORMED,
						  "JComboBox selection");
	// Process the listeners last to first, notifying
	// those that are interested in this event
	for (int i = listeners.length-2; i>=0; i-=2) {
	    if (listeners[i]==ActionListener.class) {
		((ActionListener)listeners[i+1]).actionPerformed(actionEvent);
	    }
	}
    }

    protected void fireIgnoreBtnActionListener(){
	// Guaranteed to return a non-null array
	Object[] listeners = ignoreBtnListenerList.getListenerList();
	ActionEvent actionEvent = new ActionEvent(this,
						  ActionEvent.ACTION_PERFORMED,
						  "JButton clicked");
	// Process the listeners last to first, notifying
	// those that are interested in this event
	for (int i = listeners.length-2; i>=0; i-=2) {
	    if (listeners[i]==ActionListener.class) {
		((ActionListener)listeners[i+1]).actionPerformed(actionEvent);
	    }
	}
    }

    /**
     * A small program used to test the component
     */
    public static void main(String[] args){
	JFrame frame = new JFrame("Test");
	JPanel panel = new JPanel();
	Locale loc = JComponent.getDefaultLocale();
	Collator collator = Collator.getInstance(loc);
	CollationKey [] arr = new CollationKey[17];
	arr[0] = collator.getCollationKey("a");
	arr[1] = collator.getCollationKey("b");
	arr[2] = collator.getCollationKey("c");
	arr[3] = collator.getCollationKey("d");
	arr[4] = collator.getCollationKey("e");
	arr[5] = collator.getCollationKey("f");
	arr[6] = collator.getCollationKey("g");
	arr[7] = collator.getCollationKey("h");
	arr[8] = collator.getCollationKey("i");
	arr[9] = collator.getCollationKey("j");
	arr[10] = collator.getCollationKey("k");
	arr[11] = collator.getCollationKey("l");
	arr[12] = collator.getCollationKey("m");
	arr[13] = collator.getCollationKey("n");
	arr[14] = collator.getCollationKey("ñ");
	arr[15] = collator.getCollationKey("o");
	arr[16] = collator.getCollationKey("p");

	ArrayList lst = new ArrayList(Arrays.asList(arr));
	panel.setLayout(new BoxLayout(panel,
				      BoxLayout.X_AXIS));
	panel.add(new CharMap(collator.getCollationKey("á"), collator, lst));
	panel.add(new JSeparator(SwingConstants.VERTICAL));
	panel.add(new CharMap(collator.getCollationKey("é"), collator, lst));
	panel.add(new JSeparator(SwingConstants.VERTICAL));
	panel.add(new CharMap(collator.getCollationKey("í"), collator, lst));
	panel.add(new JSeparator(SwingConstants.VERTICAL));
	panel.add(new CharMap(collator.getCollationKey("ú"), collator, lst));

	JSpinner spinner = new JSpinner(new SpinnerNumberModel(1, 1, 99, 1));
	((JSpinner.NumberEditor)spinner.getEditor()).getTextField().setEditable(false);
	((JSpinner.NumberEditor)spinner.getEditor()).getTextField().setToolTipText("Alphabet number selection");
	panel.add(spinner);
	frame.getContentPane().add(new JScrollPane(panel));
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	frame.pack();
	frame.setVisible(true);
    }
}
/*
 * -- CharMap.java ends here --
 */
