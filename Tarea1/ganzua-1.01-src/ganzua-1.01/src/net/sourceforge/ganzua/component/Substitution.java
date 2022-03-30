/*
 * -- Substitution.java --
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
import java.text.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import net.sourceforge.ganzua.event.*;

/**
 * Component that handles the character substitutions for monoalphabetic and
 * polyalphabetic ciphers through <code>MonoAlphaSubst</code>s.
 *
 * @see MonoAlphaSubst
 * @author Jesús Adolfo García Pasquel
 * @version 0.01 August 2003
 */
public class Substitution extends JPanel{

    /**
     * Constant used to indicate that a substitution is monoalphabetic */
    public static final byte MONOALPHABETIC = 1;

    /**
     * Constant used to indicate that a substitution is polyalphabetic */
    public static final byte POLYALPHABETIC = 2;

    /**
     * <code>ResourceBundle</code> with localized labels */
    private ResourceBundle labelsRB;

    /**
     * <code>true</code> if the ignored characters of the different alphabets 
     * are independent (i.e. an alphabet can ignore certain characters and
     * those same characters can appear among those not ignored in a different
     * alphabet) and <code>false</code> if they are not (i.e. the ignored
     * characters are the same for all the alphabets) */
    protected boolean ignoredIndependent = false;

    /**
     * <code>Collator</code> used to generate the <code>CollationKey</code>s
     * in <code>cipherAlpha</code> and <code>plainAlpha</code> */
    protected Collator collator;

    /**
     * <code>Locale</code> used to identify the language 
     * <code>plainAlpha</code> belongs to.
     */
    protected Locale locale;

    /**
     * <code>ArrayList</code> that contains the <code>CollationKey</code>s
     * of the cipher alphabet's (user) characters */
    protected ArrayList cipherAlpha;

    /**
     * <code>ArrayList</code> that contains the <code>CollationKey</code>s
     * of the plain alphabet's (user) characters */
    protected ArrayList plainAlpha;

    /**
     * <code>List</code> of <code>MonoAlphaSubst</code>s with the
     * substitutions for each alphabet. */
    protected ArrayList monoSubsts;

    /**
     * Reference to the currently selected <code>MonoAlphaSubst</code>
     * in <code>monoSubsts</code>. */
    protected MonoAlphaSubst currMono;

    /**
     * <code>JScrollPane</code> in which the current 
     * <code>MonoAlphaSubst</code> is displayed. */
    protected JScrollPane scrollP;

    /**
     * <code>JSpinner</code> that lets the user select the number of the
     * alphabet to be displayed.<br/>
     * If the substitution is monoalphabetic, <code>spinner</code> displays
     * 1 and is disabled. */
    protected JSpinner spinner;

    /**
     * Check box that lets the user select if an injective substitution
     * should be enforced (two different characters may not be replaced
     * by the same character) or not. */
    protected JCheckBox injective;

    /**
     * Button that opens a window that displays the list of ignored characters
     * of the currently selected alphabet. */
    protected JButton ignored;

    /**
     * Frame used to display a <code>JList</code> with the ignored
     * characters and a <code>JButton</code> that lets the user put 
     * the characters back in. */
    protected JFrame igFrame;

    /**
     * <code>JList</code> used to display the ignored characters */
    protected JList igJList;

    /**
     * Button that lets the user put ignored characters back in the
     * <code>MonoAlphaSubst</code>. */
    protected JButton igBtn;

    /**
     * <code>ChangeListener</code> for the <code>MonoAlphaSubst</code>s*/
    protected ChangeListener masCL;

    /**
     * Used to register all the <code>ChangeListener</code>s interested
     * in the <code>Substitution</code> */
    private EventListenerList listenerList = new EventListenerList();

    /**
     * Creates a <code>MONOALPHABETIC</code> <code>Substitution</code><br/>.
     *
     * Note: <code>cipherAlpha</code> and <code>plainAlpha</code> are not
     * modified by this component. (The <code>MonoAlphaSubst</code>s are
     * passed coppies of these <code>ArrayList</code>s where the elements
     * themeselves are not copied.
     *
     * @param locale the <code>Locale</code> used to create
     *               <code>collator</code>. The locale is used to identify the
     *               language <code>plainAlpha</code> belongs to
     * @param collator the <code>Collator</code> used to generate the
     *                 <code>CollationKey</code>s in <code>cipherAlpha</code>
     *                 and </code>plainAlpha</code>
     * @param cipherAlpha the cipher alphabet in the form of an ordered
     *                    <code>ArrayList</code> of <code>CollationKey</code>s
     * @param plainAlpha the plain alphabet in the form of an ordered
     *                   <code>ArrayList</code> of <code>CollationKey</code>s
     * @throws NullPointerException if any of the arguments is <code>null</code>
     */
    public Substitution(Locale locale,
			Collator collator,
			ArrayList cipherAlpha,
			ArrayList plainAlpha) throws NullPointerException
    {
	super();
	if(locale == null || collator == null || 
	   cipherAlpha == null || plainAlpha == null){
	    throw new NullPointerException("null argument");
	}
	labelsRB = ResourceBundle.getBundle(Substitution.class.getName(),
					    getDefaultLocale());
	this.locale = locale;
	this.collator = collator;
	this.cipherAlpha = cipherAlpha;
	this.plainAlpha = plainAlpha;
	masCL = monoAlphaSubstChangeListener();
	monoSubsts = new ArrayList();
	monoSubsts.add(newMonoAlphaSubst());
	currMono = (MonoAlphaSubst)monoSubsts.get(0);
	initGUI();
	initIgnoreFrame();
	setCurrentAlphabet(0);
    }

    /**
     * Returns the <code>ChangeListener</code> to be used in all of
     * the <code>Substitution</code>'s <code>MonoAlphaSubst</code>s.
     */
    private final ChangeListener monoAlphaSubstChangeListener(){
	final Substitution thisSubstitution = this;
	ChangeListener cl = new ChangeListener(){
		public void stateChanged(ChangeEvent e){
		    SubstitutionEvent se = (SubstitutionEvent)e;
		    byte changeType = se.getChangeType();
		    MonoAlphaSubst src = (MonoAlphaSubst)se.getSource();
		    int srcIdx = thisSubstitution.monoSubsts.indexOf(src);
		    /* If an IGNORED_CHARACTERS event is fired, set the
		       ignored characters of the rest of the alphabets to be
		       the same as those of the MonoAlphaSubst that fired the
		       event and forward the IGNORED_CHARACTERS events.
		       Ignore cipher alphabet and plain alphabet changes and
		       forward any other event. */
		    if(changeType == SubstitutionEvent.IGNORED_CHARACTERS &&
		       !ignoredIndependent){
			int i=0;
			HashSet igSet = src.getIgnoredChars();
			for(Iterator iter=monoSubsts.iterator(); iter.hasNext(); i++){
			    MonoAlphaSubst tmpM = (MonoAlphaSubst)iter.next();
			    if(i !=srcIdx){
				tmpM.setIgnoredCharsNoEvent(igSet);
			    }
			    fireStateChanged(changeType, i);
			}
		    }else if(changeType != SubstitutionEvent.CIPHER_ALPHABET &&
			     changeType != SubstitutionEvent.CHARACTER_ADDED_TO_CIPHER_ALPHABET &&
			     changeType != SubstitutionEvent.CHARACTER_ADDED_TO_PLAIN_ALPHABET){
			thisSubstitution.fireStateChanged(changeType, srcIdx);
		    }
		}
	    };
	return cl;
    }

    /**
     * Returns a new instance of <code>MonoAlphaSubst</code> created with
     * copies of <code>cipherAlpha</code> and <code>plainAlpha</code>
     * where the elements themeselves are not copied. The new 
     * <code>MonoAlphaSubst</code> has a <code>ChangeListener</code>
     * registered that calls this <code>Substitution</code>'s 
     * <code>fireStateChanged()</code>.<br/>
     * Note that the <code>MonoAlphaSubst</code> is injective.
     *
     * @return a new instance of <code>MonoAlphaSubst</code> created with
     *         coppies of <code>cipherAlpha</code> and <code>plainAlpha</code>
     */
    private final MonoAlphaSubst newMonoAlphaSubst(){
	MonoAlphaSubst mas = new MonoAlphaSubst(locale, collator,
						(ArrayList)cipherAlpha.clone(),
						(ArrayList)plainAlpha.clone());
	mas.addChangeListener(masCL);
	return mas;
    }

    /**
     * Initializes the GUI.<br/>
     * Sets the layout of the components, initializes <code>spinner</code>
     * and sets it's current, minimum, maximum and step values to 
     * <code>1</code>, initializes <code>injective</code> and calls
     * <code>injective.setSelected(true)</code>.<br/> 
     * Adds listeners to <code>spinner</code>, <code>injective</code> and
     * <code>ignored</code>
     */
    private final void initGUI(){
	setLayout(new BorderLayout());
	scrollP = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_NEVER,
				  JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
	this.add(scrollP);
	JPanel eastPan = new JPanel();
	eastPan.setLayout(new BoxLayout(eastPan, BoxLayout.Y_AXIS));
	JPanel topEastPan = new JPanel();
	spinner = new JSpinner(new SpinnerNumberModel(1, 1, 1, 1));
	JFormattedTextField tf = ((JSpinner.NumberEditor)spinner.getEditor()).getTextField();
	tf.setEditable(false); // spinner should not be editable
	tf.setToolTipText(labelsRB.getString("spinnerTT"));
	spinner.setEnabled(false);
	spinner.addChangeListener(new ChangeListener(){
		public void stateChanged(ChangeEvent e){
		    SpinnerNumberModel spinnerMod = (SpinnerNumberModel)spinner.getModel();
		    setCurrentAlphabet(spinnerMod.getNumber().intValue()-1);
		}
	    });
	// set the spinner's preferred size
	Dimension prefSize = spinner.getPreferredSize();
	prefSize.width = Math.max(SwingUtilities.computeStringWidth(tf.getFontMetrics(tf.getFont()), "mmmm"),
				  prefSize.width);
	spinner.setPreferredSize(prefSize);
	// add the spinner to topEastPan
	topEastPan.add(spinner);
	ignored = new JButton(labelsRB.getString("ignored"));
	ignored.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent e){
		    showIgnoredCharacters();
		}
	    });
	ignored.setToolTipText(labelsRB.getString("ignoredTT"));
	topEastPan.add(ignored);
	injective = new JCheckBox(labelsRB.getString("injective"));
	injective.addActionListener(new ActionListener(){
		/* Sets all of the <code>MonoAlphaSubst</code>s to injective
		 * or not injective depending on the value of the JCheckBox */
		public void actionPerformed(ActionEvent e){
		    JCheckBox checkbox = (JCheckBox)e.getSource();
		    boolean selected = checkbox.isSelected();
		    Iterator iter = monoSubsts.iterator();
		    while(iter.hasNext()){
			((MonoAlphaSubst)iter.next()).setInjective(selected);
		    }
		}
	    });
	injective.setToolTipText(labelsRB.getString("injectiveTT"));
	injective.setSelected(true);
	injective.setAlignmentX(Component.CENTER_ALIGNMENT);
	eastPan.add(injective);
	eastPan.add(topEastPan);
	this.add(eastPan, BorderLayout.EAST);
    }

    /**
     * Initializes <code>igFrame</code> and its contents (<code>igJList</code>,
     * <code>igBtn</code>).
     */
    private final void initIgnoreFrame(){
	JScrollPane igScroll;
	igFrame = new JFrame(labelsRB.getString("igFrame"));
	igFrame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
	igJList = new JList();
	igBtn = new JButton(labelsRB.getString("igBtn"));
	igBtn.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent e){
		    currMono.includeIgnoredCharacters(igJList.getSelectedIndices());
		}
	    });
	igBtn.setToolTipText(labelsRB.getString("igBtnTT"));
	igScroll = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				   JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
	igScroll.setPreferredSize(new Dimension(150, 100));
	igScroll.setViewportView(igJList);
	igFrame.getContentPane().add(igScroll, BorderLayout.CENTER);
	if(!isSystemMacWithAquaLAF()){
	    igFrame.getContentPane().add(igBtn, BorderLayout.SOUTH);
	} else{ // leave space for the size control used in Aqua
	    JPanel panel = new JPanel(new BorderLayout());
	    panel.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));
	    panel.add(igBtn, BorderLayout.CENTER);
	    igFrame.getContentPane().add(panel, BorderLayout.SOUTH);
	}
	igFrame.pack();
    }

    /**
     * Returns <code>true</code> if the underlying operating system
     * is Mac OS X and the current Look And Feel is Aqua
     * (Mac OS X's system default LAF), and <code>false</code> otherwise.
     */
    private static boolean isSystemMacWithAquaLAF(){
	boolean clientMac; // true if the OS is Mac OS X
	boolean systemLAF; // true if the current LAF is the system's LAF
       	clientMac = System.getProperty("os.name").toLowerCase().startsWith("mac os x");
	systemLAF = UIManager.getSystemLookAndFeelClassName().equals(
			   UIManager.getLookAndFeel().getClass().getName());
	return clientMac && systemLAF;
    }

    /**
     * Sets the currently displayed (selected) alphabet to the one at position 
     * <code>i</code> in <code>monoSubsts</code>.
     *
     * @param i the index of the alphabet in <code>monoSubsts</code>
     * @throws IndexOutOfBoundsException if <code>i &lt; 0 || i &gt;= monoSubsts.size()</code>
     */
    public void setCurrentAlphabet(int i) throws IndexOutOfBoundsException
    {
	if(i<0 || i>=monoSubsts.size()){
	    throw new IndexOutOfBoundsException();
	}
	SpinnerNumberModel spinnerMod = (SpinnerNumberModel)spinner.getModel();
	// update the value of spinner if needed
	if(spinnerMod.getNumber().intValue()!=i+1){
	    spinnerMod.setValue(new Integer(i+1));
	} else{
	    /* if the value of spinner was changed in the if section,
	       spinner's ChangeListener will call this method again, so
	       don't change the scoll panel's viewport unless the value
	       passed equals the value of the spinner */
	    currMono = (MonoAlphaSubst)monoSubsts.get(i);
	    Point viewPos = scrollP.getViewport().getViewPosition();
	    scrollP.setViewportView(currMono);
	    scrollP.getViewport().setViewPosition(viewPos);
	    igJList.setModel(currMono.getIgnoredCharsListModel());
	}
    }

    /**
     * Returns the type of substitution being performed 
     * (<code>MONOALPHABETIC</code> or <code>POLYALPHABETIC</code>). The
     * substitution is <code>MONOALPHABETIC</code> if the substitution has
     * only one alphabet, and <code>POLYALPHABETIC</code> if it has more than
     * one.
     *
     * @return <code>MONOALPHABETIC</code> or <code>POLYALPHABETIC</code>
     */
    public byte getType(){
	return getNumberOfAlphabets() > 1 ? POLYALPHABETIC : MONOALPHABETIC;
    }

    /**
     * Returns the number of alphabets the <code>Substitution</code> has.
     * The minimum is 1.
     *
     * @return the number of alphabets the <code>Substitution</code> has
     */
    public int getNumberOfAlphabets(){
	return monoSubsts.size();
    }

    /**
     * Sets the number of alphabets to <code>num</code>. <code>num</code>
     * must be greater than 0.
     *
     * @param num an integer greater than 0
     * @throws IllegalArgumentException if <code>num &lt;=0</code>.
     */
    public void setNumberOfAlphabets(int num) throws IllegalArgumentException
    {
	if(num<=0){
	    throw new IllegalArgumentException("There can't be less than 1 alphabets");
	}
	int currNum = getNumberOfAlphabets();
	SpinnerNumberModel spinnerMod = (SpinnerNumberModel)spinner.getModel();
	/* the MonoAlphaSubst being displayed is the one at
	   spinnerMod.getNumber().intVaule()-1 */
	if(currNum == num){
	    return;
	}
	if(currNum < num){
	    for(int i=currNum; i<num; i++){
		MonoAlphaSubst tmpM = newMonoAlphaSubst();
		if(!ignoredIndependent){
		    tmpM.setIgnoredCharsNoEvent(((MonoAlphaSubst)monoSubsts.get(0)).getIgnoredChars());
		}
		monoSubsts.add(tmpM);
	    }
	    if(!spinner.isEnabled()){
		spinner.setEnabled(true);
	    }
	} else{
	    if(spinnerMod.getNumber().intValue()>num){
		spinnerMod.setValue(new Integer(num));
		// spinner's action listener calls setCurrentAlphabet(num-1)
	    }
	    for(int i=currNum-1; i>=num; i--){
		monoSubsts.remove(i);
	    }
	    if(num==1){
		spinner.setEnabled(false);
	    }
	}
	spinnerMod.setMaximum(new Integer(num));
	fireStateChanged(SubstitutionEvent.NUMBER_OF_ALPHABETS, num);
    }

    /**
     * Returns the <code>JFrame</code> the <code>Substitution</code> uses
     * to display the ignored characters in the currently selected
     * <code>MonoAlphaSubst</code>
     *
     * @return the <code>JFrame</code> used to display the ignored characters
     *         in the currently selected <code>MonoAlphaSubst</code>
     */
    public JFrame getIgnoredCharactersFrame(){
	return igFrame;
    }

    /**
     * Returns a <code>List</code> of <code>HashSet</code>s that contain the
     * characters (<code>CollationKey</code>s) to be ignored in each alphabet.
     *
     * @return <code>List</code> of <code>HashSet</code>s that contain the
     *         characters (<code>CollationKey</code>s) to be ignored in each
     *         alphabet.
     */
    public ArrayList getIgnoredCharacters(){
	ArrayList ignoredCharsLst = new ArrayList(getNumberOfAlphabets());
	Iterator monoIter = monoSubsts.iterator();
	while(monoIter.hasNext()){
	    ignoredCharsLst.add(((MonoAlphaSubst)monoIter.next()).getIgnoredChars());
	}
	return ignoredCharsLst;
    }

    /**
     * Returns <code>true</code> if the ignored characters of the different
     * alphabets are independent (i.e. an alphabet can ignore certain 
     * characters and those same characters can appear among those not
     * ignored in a different alphabet) and <code>false</code> if they are not 
     * (i.e. the ignored characters are the same for all the alphabets).<br/>
     * By default, <code>ignoredIndependent</code> is <code>false</code>.
     */
    public boolean areIgnoredIndependent(){
	return ignoredIndependent;
    }

    /**
     * Sets if the ignored characters of the different 
     * <code>MonoAlphaSubst</code>s are independent or not. When changing to
     * non-independent, the ignored characters of all the 
     * <code>MonoAlphaSubst</code>s are set to the union of the ignored
     * characters of the <code>MonoAlphaSubst</code>s.<br/>
     * By default, <code>ignoredIndependent</code> is <code>false</code>.
     *
     * @param independent <code>true</code> if they should be independent,
     *                    <code>false</code> otherwise.
     */
    public void setIgnoredIndependent(boolean independent){
	if(independent == ignoredIndependent){
	    return;
	}
	if(!independent){
	    HashSet igSet = new HashSet();
	    for(Iterator iter=monoSubsts.iterator(); iter.hasNext(); ){
		igSet.addAll(((MonoAlphaSubst)iter.next()).getIgnoredChars());
	    }
	    int i=0;
	    for(Iterator iter=monoSubsts.iterator(); iter.hasNext(); i++){
		((MonoAlphaSubst)iter.next()).setIgnoredCharsNoEvent(igSet);
		fireStateChanged(SubstitutionEvent.IGNORED_CHARACTERS, i);
	    }
	}
	ignoredIndependent = independent;
    }

    /**
     * Receives an <code>ArrayList</code> of <code>HashSet</code>s with the
     * characters (<code>CollationKey</code>s ) to be set as ignored in each
     * <code>MonoAlphaSubst</code> (alphabet) if 
     * <code>areIgnoredIndependent()</code> is <code>true</code>. If
     * <code>areIgnoredIndependent()</code> is <code>false</code>, then the
     * ignored characters of all the <code>MonoAlphaSubst</code>s are set to
     * the union of all the sets of ignored characters in 
     * <code>ignoredChars</code>.
     *
     * @param ignoredChars an <code>ArrayList</code> of <code>HashSet</code>s
     *                     of <code>CollationKey</code>s
     * @throws NullPointerException if <code>ignoredChars</code> is <code>null</code>
     * @throws IllegalArgumentException if the number of <code>HashSet</code>s
     *                  in <code>ignoredChars</code> is not equal to the number
     *                  of alphabets in this <code>Substitution</code>
     * @see #areIgnoredIndependent()
     * @see MonoAlphaSubst#setIgnoredChars(HashSet)
     */
    public void setIgnoredCharacters(ArrayList ignoredChars) throws NullPointerException,
								    IllegalArgumentException
    {
	if(ignoredChars.size() != getNumberOfAlphabets()){
	    throw new IllegalArgumentException();
	}
	Iterator ignoredIter = ignoredChars.iterator();
	Iterator monoIter = monoSubsts.iterator();
	int i=0;
	if(ignoredIndependent){
	    for( ; ignoredIter.hasNext(); i++){
		((MonoAlphaSubst)monoIter.next()).setIgnoredCharsNoEvent((HashSet)ignoredIter.next());
		fireStateChanged(SubstitutionEvent.IGNORED_CHARACTERS, i);
	    }
	}else{
	    HashSet igSet = new HashSet();
	    for( ; ignoredIter.hasNext();){
		igSet.addAll((HashSet)ignoredIter.next());
	    }
	    for( ; monoIter.hasNext(); i++){
		((MonoAlphaSubst)monoIter.next()).setIgnoredCharsNoEvent(igSet);
		fireStateChanged(SubstitutionEvent.IGNORED_CHARACTERS, i);
	    }
	}
    }

    /**
     * Receives a <code>HashSet</code> with the
     * characters (<code>CollationKey</code>s ) to be set as ignored in all 
     * of the <code>MonoAlphaSubst</code>s of the <code>Substitution</code>
     *
     * @param ignoredChars a <code>HashSet</code> of <code>CollationKey</code>s
     * @throws NullPointerException if <code>ignoredChars</code> is <code>null</code>
     * @see MonoAlphaSubst#setIgnoredChars(HashSet)
     */
    public void setIgnoredCharacters(HashSet ignoredChars) throws NullPointerException,
								    IllegalArgumentException
    {
	int i=0;
	for(Iterator iter=monoSubsts.iterator() ; iter.hasNext(); i++){
	    ((MonoAlphaSubst)iter.next()).setIgnoredCharsNoEvent(ignoredChars);
	    fireStateChanged(SubstitutionEvent.IGNORED_CHARACTERS, i);
	}
    }

    /**
     * Returns a <code>List</code> of <code>Set</code>s that contain the
     * characters (<code>CollationKey</code>s) in the cipher alphabet
     * minus those to be ignored in each alphabet.
     *
     * @return <code>List</code> of <code>Set</code>s that contain the
     *         characters (<code>CollationKey</code>s) in the cipher alphabet
     *         minus those to be ignored in each alphabet.
     */
    public ArrayList getCipherAlphaMinusIgnoredCharacters(){
	ArrayList alphaLst = new ArrayList(getNumberOfAlphabets());
	Iterator monoIter = monoSubsts.iterator();
	while(monoIter.hasNext()){
	    alphaLst.add(((MonoAlphaSubst)monoIter.next()).getSubstitution().keySet());
	}
	return alphaLst;
    }

    /**
     * Returns <code>true</code> if the substitution is injective and
     * <code>false</code> otherwise.
     *
     * @return <code>true</code> if the substitution is injective and
     *         <code>false</code> otherwise.
     */
    public boolean getInjective(){
	return injective.isSelected();
    }

    /**
     * Sets the substitution to injective or not injective.
     *
     * @param inj <code>true</code> if the substitution shoud be injective,
     *            <code>false</code> otherwise.
     */
    public void setInjective(boolean inj){
	if(injective.isSelected() != inj){
	    injective.doClick();
	}
    }

    /**
     * Returns a <code>List</code> of <code>HashMap</code>s that contain the
     * character-replacement pairs (<code>CollationKey</code>s) for each
     * alphabet.<br/>
     * If a character of the cipher alphabet is not present in a
     * <code>HashMap</code> as a key, then that character should be ignored;
     * if it is mapped to <code>null</code>, then no replacement character has
     * been set and should be replaced by <code>' '</code>; and if
     * it's mapped to another character, then it should be replaced by that
     * character.
     *
     * @return <code>List</code> of <code>Map</code>s that contain the
     *         character-replacement paris (<code>CollationKey</code>s) for
     *         each alphabet
     */
    public ArrayList getSubstitution(){
	ArrayList substLst = new ArrayList(getNumberOfAlphabets());
	Iterator monoIter = monoSubsts.iterator();
	while(monoIter.hasNext()){
	    substLst.add(((MonoAlphaSubst)monoIter.next()).getSubstitution());
	}
	return substLst;
    }

    /**
     * Receives an <code>ArrayList</code> of <code>HashMap</code>s that map 
     * characters from the cipher alphabet (<code>CollationKey</code>s ) to 
     * characters from the plain alphabet (<code>CollationKey</code>s ) and
     * sets the mappings as the <code>Substitution</code>'s 
     * <code>MonoAlphaSubst</code>s selections. The number of 
     * <code>HashMap</code>s must be equal to the number of alphabets in this
     * <code>Substitution</code>. The <code>HashMap</code> at index 1 is set
     * as the selection for the first <code>MonoAlphaSubst</code> (alphabet 1),
     * the one at index 2 for the second <code>MonoAlphaSubst</code> and
     * so on and so forth.<br/>
     * Note that the characters that are being ignored are left as ignored.
     *
     * @param substLst an <code>ArrayList</code> <code>HashMap</code>s of
     *                 <code>CollationKey</code>s
     * @throws NulllPointerException if <code>substLst</code> or any of the
     *                 <code>HashMap</code>s it contains is <code>null</code>
     * @throws IllegalArgumentException if the number of <code>HashMap</code>s
     *                 is not equal to the number of alphabets
     * @see MonoAlphaSubst#setSelection(HashMap)
     */
    public void setSubstitution(ArrayList substLst) throws NullPointerException,
							   IllegalArgumentException
    {
	if(substLst.size() != getNumberOfAlphabets()){
	    throw new IllegalArgumentException();
	}
	Iterator substIter = substLst.iterator();
	Iterator monoIter = monoSubsts.iterator();
	while(substIter.hasNext()){
	    ((MonoAlphaSubst)monoIter.next()).setSelection((HashMap)substIter.next());
	}
    }

    /**
     * Returns the <code>Locale</code> that identifies the language
     * the plain alphabet belongs to.
     */
    public Locale getPlainAlphaLocale(){
	return locale;
    }

    /**
     * Returns the instance of <code>Collator</code> currently being used
     * by the <code>Substitution</code>
     *
     * @return the instance of <code>Collator</code> currently being used by
     *         the <code>Substitution</code>
     */
    public Collator getCollator(){
	return collator;
    }

    /**
     * Clears the selected substitution characters in the currently selected
     * <code>MonoAlphaSubst</code>.
     */
    public void clearSelectionCurrMono(){
	currMono.clearSelections();
    }

    /**
     * Clears the selected substitution characters in the currently selected
     * <code>MonoAlphaSubst</code> and sets a substitution as close to the
     * identity substitution as the plain alphabet allows.
     *
     * @see MonoAlphaSubst#selectIdentity()
     */
    public void selectIdentityCurrMono(){
	currMono.selectIdentity();
    }

    /**
     * Takes the currently selected <code>MonoAlphaSubst</code>and assigns
     * characters from its plain alphabet that have not been
     * set to replace any of its cipher alphabet's characters as
     * substitution characters for those that don't have one. The
     * charcter are chosen in alphabetical order and are not repeated
     * (even if the substitution is not injective).
     *
     * @see MonoAlphaSubst#completeSelection()
     */
    public void completeSelectionCurrMono(){
	currMono.completeSelection();
    }

    /**
     * Reverses the order of the selected substitution characters in the
     * currently selected <code>MonoAlphaSubst</code> substitution.<br/>
     *
     * @see MonoAlphaSubst#reverseSelection()
     */
    public void reverseSelectionCurrMono(){
	currMono.reverseSelection();
    }

    /**
     * Clears the selected substitution characters and sets
     * a substitution as close to the inverse of the one previously set
     * as the plain and cipher alphabets allow. 
     *
     * @see MonoAlphaSubst#invertSelection()
     */
    public void invertSelectionCurrMono(){
	currMono.invertSelection();
    }

    /**
     * Shifts the selected substitution characters in the currently selected
     * <code>MonoAlphaSubst</code> to the <code>LEFT</code> or 
     * <code>RIGHT</code>.
     *
     * @param direction either <code>MonoAlphaSubst.LEFT</code> or
     *                  <code>MonoAlphaSubst.RIGHT</code>
     * @throws IllegalArgumentException if <code>direction</code> is not
     *                                  <code>MonoAlphaSubst.RIGHT</code> or
     *                                  <code>MonoAlphaSubst.LEFT</code>
     * @see MonoAlphaSubst#shiftSelection(int)
     * @see MonoAlphaSubst#LEFT
     * @see MonoAlphaSubst#RIGHT
     */
    public void shiftSelectionCurrMono(int direction) throws IllegalArgumentException
    {
	currMono.shiftSelection(direction);
    }

    /**
     * Method that sets the selection and ignored characters of the current 
     * <code>MonoAlphaSubst</code> to the ones the instance
     * at index <code>0</code> of<code>monoSubst</code> has.
     */
    public void copyFirstSelectionToCurrMono(){
	MonoAlphaSubst firstMono = (MonoAlphaSubst)monoSubsts.get(0);
	currMono.setIgnoredChars(firstMono.getIgnoredChars());
	currMono.setSelection(firstMono.getSubstitution());
    }

    /**
     * Sets the cipher alphabet to <code>cipherAlpha</code>.<br/>
     * Note that this method clears all the selected substitution characters
     * and ignored characters.<br/>
     * The <code>Collator</code> used to generate the 
     * <code>CollationKey</code>s in <code>cipherAlpha</code> must be
     * the same as the one currently being used by the 
     * <code>Substitution</code>.
     *
     * @param cipherAlpha the new cipher alphabet in the form of an ordered
     *                    <code>arrayList</code> of <code>CollationKey</code>s
     * @throws NullPointerException if any of the arguments is <code>null</code>
     */
    public void setCipherAlpha(ArrayList cipherAlpha) throws NullPointerException
    {
	if(cipherAlpha == null){
	    throw new NullPointerException();
	}
	this.cipherAlpha = new ArrayList(cipherAlpha);
	Iterator iter = monoSubsts.iterator();
	while(iter.hasNext()){
	    ArrayList cipherAlphabet = (ArrayList)cipherAlpha.clone();
	    ((MonoAlphaSubst)iter.next()).setCipherAlpha(cipherAlphabet);
	}
	fireStateChanged(SubstitutionEvent.CIPHER_ALPHABET, -1);
    }

    /**
     * Returns the cipher alphabet.
     *
     * @return the cipher alphabet.
     */
    public ArrayList getCipherAlpha(){
	return (ArrayList)cipherAlpha.clone();
    }

    /**
     * Sets the plain alphabet to <code>plainAlpha</code>.<br/>
     * Note that this method clears all the selected substitution characters.
     *
     * @param loc the <code>Locale</code> used to create <code>col</code>. The
     *            locale is used to identify the language
     *            <code>plainAlpha</code> belongs to
     * @param col the <code>Collator</code> used to generate the
     *            <code>CollationKey</code>s in <code>plainAlpha</code>
     * @param plainAlpha the new plain alphabet in the form of an ordered
     *                   <code>arrayList</code> of <code>CollationKey</code>s
     * @throws NullPointerException if any of the arguments is <code>null</code>
     */
    public void setPlainAlpha(Locale loc,
			      Collator col,
			      ArrayList plainAlpha) throws NullPointerException
    {
	if(loc == null || col == null || plainAlpha == null){
	    throw new NullPointerException();
	}
	this.plainAlpha = plainAlpha;
	locale = loc;
	collator = col;
	//change the collator used in cipherAlpha
	for(int i=0; i<cipherAlpha.size(); i++){
	    cipherAlpha.set(i,
			    collator.getCollationKey(((CollationKey)cipherAlpha.get(i)).getSourceString()));
	}
	Collections.sort(cipherAlpha);
	//set plainAlpha for the MonoAlphaSubsts
	Iterator iter = monoSubsts.iterator();
	while(iter.hasNext()){
	    ArrayList plainAlphabet = (ArrayList)plainAlpha.clone();
	    ((MonoAlphaSubst)iter.next()).setPlainAlpha(loc, col, 
							plainAlphabet);
	}
	fireStateChanged(SubstitutionEvent.CIPHER_ALPHABET, -1);
    }

    /**
     * Returns the plain alphabet.
     *
     * @return the plain alphabet.
     */
    public ArrayList getPlainAlpha(){
	return (ArrayList)plainAlpha.clone();
    }

    /**
     * Adds a <code>ChangeListener</code> to the <code>Substitution</code>.
     * <br/>
     * The <code>ChangeListener</code> will receive a 
     * <code>SubstitutionEvent</code> when any of the 
     * <code>MonoAlphaSubst</code>s in <code>monoSubsts</code> changes state 
     * (keys-values were added/removed or the values for one or more keys 
     * changed).
     *
     * @param l the <code>ChangeListener</code> that sould be notified
     */
    public void addChangeListener(ChangeListener l){
	listenerList.add(ChangeListener.class, l);
    }

    /**
     * Adds a user character to the cipher alphabet. If the character is in
     * the list of ignored characters, it is removed from the list of ignored
     * characters and added to the cipher alphabet.<br/>
     * <br/>
     * Note that this method fires a <code>SubstitutionEvent</code> of
     * change type <code>CHARACTER_ADDED_TO_CIPHER_ALPHABET</code> and not 
     * <code>CIPHER_ALPHABET</code> as <code>MonoAlphaSubst</code>s do.
     *
     * @param character the user character to be added to the cipher alphabet
     * @return <code>true</code> if <code>character</code> was added to any of
     *         the cipher alphabets of the monoalphabetic substitutions, and
     *         <code>false</code> otherwise
     * @throws NullPointerException if <code>character</code> is
     *                              <code>null</code> or an empty string.
     * @throws IllegalArgumentException if <code>character</code> is not a user character
     */
    public boolean addCharacterToCipherAlphabet(String character) 
	throws NullPointerException, IllegalArgumentException
    {
	boolean added = false;
	MonoAlphaSubst tmpMono = null;
	Iterator iterator = monoSubsts.iterator();
	while(iterator.hasNext()){
	    tmpMono = (MonoAlphaSubst)iterator.next();
	    if(tmpMono.addCharacterToCipherAlphabet(character)){
		added = true;
	    }
	}
	if(added){ // add the character to the Substitution's cipherAlpha
	    CollationKey characterCK = collator.getCollationKey(character);
	    int i = Collections.binarySearch(cipherAlpha, characterCK);
	    /* i>=0 if added==true because the character was included from an
	       ignore list. */
	    if(i<0){
		cipherAlpha.add(-i-1, characterCK);
	    }
	    fireStateChanged(SubstitutionEvent.CHARACTER_ADDED_TO_CIPHER_ALPHABET, -1);
	}
	return added;
    }

    /**
     * Adds a user character to the plain alphabet.
     *
     * @param character the user character to be added to the plain alphabet
     * @return <code>true</code> if <code>character</code> was added to any of
     *         the plain alphabets of the monoalphabetic substitutions, and
     *         <code>false</code> otherwise
     * @throws NullPointerException if <code>character</code> is
     *                              <code>null</code> or an empty string.
     * @throws IllegalArgumentException if <code>character</code> is not a user character
     */
    public boolean addCharacterToPlainAlphabet(String character) 
	throws NullPointerException, IllegalArgumentException
    {
	boolean added = false;
	MonoAlphaSubst tmpMono = null;
	Iterator iterator = monoSubsts.iterator();
	while(iterator.hasNext()){
	    tmpMono = (MonoAlphaSubst)iterator.next();
	    if(tmpMono.addCharacterToPlainAlphabet(character)){
		added = true;
	    }
	}
	if(added){ // add the character to the Substitution's plainAlpha
	    CollationKey characterCK = collator.getCollationKey(character);
	    int i = Collections.binarySearch(plainAlpha, characterCK);
	    if(i<0){
		plainAlpha.add(-i-1, characterCK);
	    }
	    fireStateChanged(SubstitutionEvent.CHARACTER_ADDED_TO_PLAIN_ALPHABET, -1);
	}
	return added;
    }

    /**
     * Opens a window that displays the list of ignored characters
     * of the currently selected alphabet.
     */
    public void showIgnoredCharacters(){
	igFrame.setVisible(true);
    }

    /**
     * Removes a <code>ChangeListener</code> from the 
     * <code>Substitution</code>.
     *
     * @param l the <code>ChangeListener</code> to be removed
     */
    public void removeChangeListener(ChangeListener l){
	listenerList.remove(ChangeListener.class, l);
    }

    /**
     * Returns an array of all the <code>ChangeListeners</code> added to 
     * this <code>Substitution</code> with <code>addChangeListener()</code>
     *
     * @return all of the <code>ChangeListeners</code> added or an empty array
     *         if no listeners have been added
     */
    public ChangeListener[] getChangeListeners(){
	return (ChangeListener[])listenerList.getListeners(ChangeListener.class);
    }

    /**
     * Notifies all listeners that have registered interest for notification
     * on this event type.<br/>
     *
     * The <code>ChangeListeners</code> are notified using a 
     * <code>SubstitutionEvent</code>.
     *
     * @param type a byte that indicates the kind of change that took place. It
     *             may be <code>SubstitutionEvent.UNKNOWN</code>,
     *             <code>SubstitutionEvent.SUBSTITUTION_PAIR</code>,
     *             <code>SubstitutionEvent.IGNORE_CHARACTERS</code> or
     *             <code>SubstitutionEvent.NUMBER_OF_ALPHABETS</code>.
     * @param index the index of the alphabet affected by the change.
     * @see SubstitutionEvent
     */
    protected void fireStateChanged(byte type, int index){
	// Guaranteed to return a non-null array
	Object[] listeners = listenerList.getListenerList();
	ChangeEvent changeEvent = new SubstitutionEvent(this, type, index);
	// Process the listeners last to first, notifying
	// those that are interested in this event
	for (int i = listeners.length-2; i>=0; i-=2) {
	    if (listeners[i]==ChangeListener.class) {
		((ChangeListener)listeners[i+1]).stateChanged(changeEvent);
	    }
	}
    }

    /**
     * A small program used to test the component
     */    
    public static void main(String[] args){
	JFrame frame = new JFrame("Test");
	Locale loc = JComponent.getDefaultLocale();
	Collator collator = Collator.getInstance(loc);
	/*CollationKey [] arr1 = new CollationKey[16];
	arr1[0] = collator.getCollationKey("a");
	arr1[1] = collator.getCollationKey("b");
	arr1[2] = collator.getCollationKey("c");
	arr1[3] = collator.getCollationKey("d");
	arr1[4] = collator.getCollationKey("e");
	arr1[5] = collator.getCollationKey("f");
	arr1[6] = collator.getCollationKey("g");
	arr1[7] = collator.getCollationKey("h");
	arr1[8] = collator.getCollationKey("i");
	arr1[9] = collator.getCollationKey("j");
	arr1[10] = collator.getCollationKey("k");
	arr1[11] = collator.getCollationKey("l");
	arr1[12] = collator.getCollationKey("m");
	arr1[13] = collator.getCollationKey("n");
	arr1[14] = collator.getCollationKey("o");
	arr1[15] = collator.getCollationKey("p");
	ArrayList lst1 = new ArrayList(Arrays.asList(arr1));*/
	CollationKey [] arr2 = new CollationKey[6];
	arr2[0] = collator.getCollationKey("á");
	arr2[1] = collator.getCollationKey("é");
	arr2[2] = collator.getCollationKey("í");
	arr2[3] = collator.getCollationKey("ó");
	arr2[4] = collator.getCollationKey("ú");
	arr2[5] = collator.getCollationKey("ü");
	ArrayList lst2 = new ArrayList(Arrays.asList(arr2));
	final Substitution subst = new Substitution(loc,
						    collator, 
						    lst2,
						    new ArrayList());
	ChangeListener change = new ChangeListener(){
		public void stateChanged(ChangeEvent e){
		    System.out.println("State changed");
		}
	    };
	subst.addChangeListener(change);
	subst.setNumberOfAlphabets(5);
	subst.setCurrentAlphabet(2);
	//subst.setNumberOfAlphabets(1);
	frame.getContentPane().add(subst, BorderLayout.NORTH);
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	frame.pack();
	frame.setVisible(true);
    }
}
/*
 * -- Substitution.java ends here --
 */
