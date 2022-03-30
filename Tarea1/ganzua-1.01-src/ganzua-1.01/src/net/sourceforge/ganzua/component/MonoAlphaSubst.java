/*
 * -- MonoAlphaSubst.java --
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
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.text.*;
import net.sourceforge.ganzua.event.*;

/**
 * Component that lets the user choose a monoalphabetic substitution.<br/><br/>
 *
 * Note: Do <u>not</u> use the <code>add</code> or <code>remove</code>
 * methods inherited from <code>java.awt.Container</code>.
 *
 * @author Jesús Adolfo García Pasquel
 * @version 0.01 September 2003
 */
public class MonoAlphaSubst extends JPanel
			    implements Scrollable
{
    /**
     * Constant used as a parameter by <code>shiftSelection(int)</code>
     * to shift the selections to the left.
     *
     * @see #shiftSelection(int)
     */
    public static final int LEFT = SwingConstants.LEFT;

    /**
     * Constant used as a parameter by <code>shiftSelection(int)</code>
     * to shift the selections to the right.
     *
     * @see #shiftSelection(int)
     */
    public static final int RIGHT = SwingConstants.RIGHT;

    /**
     * Indicates if the substitution is injective or not. By injective I
     * mean that two different characters may not be substituted by the
     * same character (unless it's the <code>' '</code> character). */
    protected boolean injective;

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
     * List of characters to be ignored in the ciphertext (they remain
     * unchanged in the plaintext).<br/>
     * A <code>ListModel</code> is used in order to be able to show the
     * ignored characters in a <code>JList</code> */
    protected IgnoredCharsListModel ignoredCharsLM;

    /**
     * The character substitutions in the form of a <code>HashMap</code>*/
    protected HashMap subst;

    /**
     * Action listener for the <code>CharMap</code>s' ignore buttons*/
    private ActionListener ignoreBtnAL;

    /**
     * Action listener for the <code>CharMap</code>s' combo boxes*/
    private ActionListener cboxAL;

    /**
     * Used to register all the <code>ChangeListener</code>s interested
     * in the <code>MonoAlphaSubst</code> */
    private EventListenerList listenerList = new EventListenerList();

    /**
     * Constructor that receives the plain alphabet (<code>plainAlpha</code>),
     * cipher alphabet (<code>cipherAlpha</code>) and the <code>Collator</code>
     * used to generate the <code>CollationKey</code>s in the lists and sets 
     * <code>injective</code> to <code>true</code>.<br/>
     *
     * Note that <code>cipherAlpha</code> and <code>plainAlpha</code> are
     * the objets this instance of <code>MonoAlphaSubst</code> will use, and
     * it may change their contents.
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
     *
     * @throws NullPointerException if any of the arguments is <code>null</code>
     */
    public MonoAlphaSubst(Locale locale,
			  Collator collator,
			  ArrayList cipherAlpha,
			  ArrayList plainAlpha) throws NullPointerException
    {
	super();
	if(locale==null || collator==null || 
	   cipherAlpha==null || plainAlpha==null){
	    throw new NullPointerException("null argument");
	}
	injective = true;
	this.locale = locale;
	this.collator = collator;
	this.cipherAlpha = cipherAlpha;
	this.plainAlpha = plainAlpha;
	ignoredCharsLM = new IgnoredCharsListModel();
	subst = new HashMap();
	initSubst();
	ignoreBtnAL = ignoreBtnActionListener();
	cboxAL = cboxActionListener();
	initGUI();
    }

    /**
     * Puts the <code>CollationKey</code>s in <code>cipherAlpha</code> as
     * keys in <code>subst</code> with a <code>null</code> associated value.
     */
    private final void initSubst(){
	Iterator iter = cipherAlpha.iterator();
	while(iter.hasNext()){
	    subst.put(iter.next(), null);
	}
    }

    /**
     * Creates and returns an <code>ActionListener</code> to be
     * used on the "ignore character" button of a <code>CharMap</code>.<br/>
     * The <code>ActionListener</code> removes the <code>CharMap</code>'s
     * character from <code>cipherAlpha</code> and <code>subst</code>, and
     * removes the <code>CharMap</code> from this <code>MonoAlphaSubst</code>
     *
     * @return an <code>ActionListener</code> for the ignore buttons
     */
    private final ActionListener ignoreBtnActionListener(){
	ActionListener al = new ActionListener(){
		public void actionPerformed(ActionEvent e){
		    CharMap sourceCM = (CharMap)e.getSource();
		    CollationKey character = sourceCM.getCharacter();
		    try{
			addToIgnoredListNoEvent(character);
		    }catch(IllegalArgumentException iae){
			System.err.println("ERROR: Unable to find '" +
					   character.getSourceString() +
					   "' in the cipher alphabet");
		    }
		    fireStateChanged(SubstitutionEvent.IGNORED_CHARACTERS);
		}
	    };
	return al;
    }

    /**
     * Creates and returns an <code>ActionListener</code> to be
     * used on the character replacement combo box of a <code>CharMap</code>
     */
    private final ActionListener cboxActionListener(){
	ActionListener al = new ActionListener(){
		public void actionPerformed(ActionEvent e){
		    CharMap sourceCM = (CharMap)e.getSource();
		    CollationKey character = sourceCM.getCharacter();
		    CollationKey replacement = sourceCM.getReplacement();
		    CollationKey prevRep = (CollationKey)subst.get(character);
		    if((prevRep!=null && !prevRep.equals(replacement)) ||
		       (prevRep==null && replacement!=null)){
			subst.put(character, replacement);
			fireStateChanged(SubstitutionEvent.SUBSTITUTION_PAIR);
		    }
		}
	    };
	return al;
    }

    /**
     * Initializes the GUI elements of the component.<br/> 
     * Sets the layout, adds a <code>CharMap</code> for every character in 
     * <code>cipherAlpha</code>, and puts <code>JSeparator</code>s
     * between them.
     */
    private final void initGUI(){
	setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
	addCharMaps();
    }

    /**
     * Adds a <code>CharMap</code> for every character in 
     * <code>cipherAlpha</code>, and puts <code>JSeparator</code>s
     * between them.<br/><br/>
     * Note: It is important for some methods of this class 
     * (e.g. <code>clearSelections()</code>) that all
     * the <code>CharMaps</code> are at pair indices
     * (<code>index%2==0</code>)
     */
    private final void addCharMaps(){
	Iterator iter = cipherAlpha.iterator();
	while(iter.hasNext()){
	    add(newCharMap((CollationKey)iter.next()));
	    if(iter.hasNext()){
		add(new JSeparator(SwingConstants.VERTICAL));
	    }
	}
    }

    /**
     * Returns a new <code>CharMap</code> with <code>ActionListener</code>s
     * registered (<code>ignoreBtnAL</code> and <code>cboxAL</code>) in the
     * current injective mode of the <code>MonoAlphaSubst</code>.
     * 
     * @param uChar a character in <code>cipherAlpha</code>
     */
    private final CharMap newCharMap(CollationKey uChar){
        CharMap cm = new CharMap(uChar, collator, plainAlpha);
	cm.addActionListenerToIgnoreBtn(ignoreBtnAL);
	cm.addActionListenerToCBox(cboxAL);
	cm.setInjective(injective);
	return cm;
    }

    /**
     * Adds the characters in <code>ignoredCharsLM</code> at the indices
     * specified back into <code>cipherAlpha</code>, and fires a 
     * <code>SubstitutionEvent</code> of type 
     * <code>SubstitutionEvent.IGNORED_CHARACTERS</code>.
     *
     * @param indices the indices of the characters to be added in increasing
     *                order and without repetitions
     * @throws NullPointerException if <code>indices</code> is <code>null</code>
     * @throws IndexOutOfBoundsException if any of the indices is out of bounds
     */
    public void includeIgnoredCharacters(int[] indices) throws NullPointerException, IndexOutOfBoundsException
    {
	CollationKey igChar;
	int index;
	if(indices.length==0){
	    return;
	}
	Arrays.sort(indices);
	for(int i=indices.length-1; i>=0; i--){
	    index = indices[i];
	    if(index<0 || index>=ignoredCharsLM.getSize()){
		throw new IndexOutOfBoundsException("Index "+i +
						    " is out of bounds ("+
						    index +")");
	    } else{
		igChar = ignoredCharsLM.getCharAt(index);
		includeIgnoredCharacterNoEvent(igChar);
	    }
	}
	fireStateChanged(SubstitutionEvent.IGNORED_CHARACTERS);
    }

    /**
     * Removes <code>character</code> from the list of ignored character
     * and adds it back to <code>cipherAlpha</code>, but does not fire
     * an event.
     *
     * @throws IllegalArgumentException if <code>character</code> is not in the
     *                                  list of ignored characters
     * @throws NullPointerException if <code>character</code> is <code>null</code>
     */
    public void includeIgnoredCharacterNoEvent(CollationKey character)throws NullPointerException, IllegalArgumentException {
	if(character == null){
	    throw new NullPointerException();
	}
	int i = ignoredCharsLM.binarySearch(character);
	if(i<0){
	    throw new IllegalArgumentException();
	}
	int j = Collections.binarySearch(cipherAlpha, character);
	assert j<0: "Impossible: The character '" + 
	            character.getSourceString() +
	            "' is among the ignored characters and "+
	            "in cipherAlpha as well";
	ignoredCharsLM.removeElementAt(i);
	i = -j-1;
	cipherAlpha.add(i, character);
	subst.put(character, null);
	if(i == cipherAlpha.size()-1){
	    if(cipherAlpha.size()>1){
		// add a JSeparator to the left of the CharMap
		this.add(new JSeparator(SwingConstants.VERTICAL), i*2-1);
	    }
	    //add the CharMap
	    this.add(newCharMap(character), i*2);
	}else{
	    //add the CharMap
	    this.add(newCharMap(character), i*2);
	    //add the JSeparator
	    this.add(new JSeparator(SwingConstants.VERTICAL), i*2+1);
	}
	revalidate(); // validates this container and all of its subcomponents
    }

    /**
     * Removes <code>character</code> from the list of ignored character
     * and adds it back to <code>cipherAlpha</code> and fires a 
     * <code>SubstitutionEvent</code> of type 
     * <code>SubstitutionEvent.IGNORED_CHARACTERS</code>.
     *
     * @throws IllegalArgumentException if <code>character</code> is not in the
     *                                  list of ignored characters
     * @throws NullPointerException if <code>character</code> is <code>null</code>
     */
    public void includeIgnoredCharacter(CollationKey character)throws NullPointerException, IllegalArgumentException {
	includeIgnoredCharacterNoEvent(character);
	fireStateChanged(SubstitutionEvent.IGNORED_CHARACTERS);
    }

    /**
     * Adds a user character to the cipher alphabet. If the character is in
     * the list of ignored characters, it is removed from the list of ignored
     * characters and added to the cipher alphabet. If the character is added,
     * a <code>SubstitutionEvent</code> of change type 
     * <code>CHARACTER_ADDED_TO_CIPHER_ALPHABET</code>is fired.
     *
     * @param character the user character to be added to the cipher alphabet
     * @return <code>true</code> if <code>character</code> was added, and
     *         <code>false</code> otherwise
     * @throws NullPointerException if <code>character</code> is 
     *                              <code>null</code> or an empty string.
     * @throws IllegalArgumentException if <code>character</code> is not a user
     *                                  character
     */
    public boolean addCharacterToCipherAlphabet(String character)
	throws NullPointerException, IllegalArgumentException
    {
	if(character == null || character.length() == 0){
	    throw new NullPointerException("null or empty string argument");
	}else if(!isUserCharacter(character, locale)){
	    throw new IllegalArgumentException("'"+ character +"'"+
					       " is not a user character");
	}
	CollationKey characterCK = collator.getCollationKey(character);
	int i = ignoredCharsLM.binarySearch(characterCK);
	if(i>=0){
	    ignoredCharsLM.removeElementAt(i);
	}
	i = Collections.binarySearch(cipherAlpha, characterCK);
	if(i>=0){
	    return false;
	}
	i = -i-1;
	cipherAlpha.add(i, characterCK);
	subst.put(characterCK, null);
	if(i == cipherAlpha.size()-1){
	    if(cipherAlpha.size()>1){
		// add a JSeparator to the left of the CharMap
		this.add(new JSeparator(SwingConstants.VERTICAL),
			 i*2-1);
	    }
	    //add the CharMap
	    this.add(newCharMap(characterCK), i*2);
	}else{
	    //add the CharMap
	    this.add(newCharMap(characterCK), i*2);
	    //add the JSeparator
	    this.add(new JSeparator(SwingConstants.VERTICAL),
		     i*2+1);
	}
	fireStateChanged(SubstitutionEvent.CHARACTER_ADDED_TO_CIPHER_ALPHABET);
	revalidate(); // validates this container and all of its subcomponents
	return true;
    }

    /**
     * Adds a user character to the plain alphabet if it is not already in it.
     * If the character is added, a <code>SubstitutionEvent</code> of change
     * type <code>CHARACTER_ADDED_TO_PLAIN_ALPHABET</code> is fired.
     *
     * @param character the user character to be added to the plain alphabet
     * @return <code>true</code> if <code>character</code> was added, and
     *         <code>false</code> otherwise
     * @throws NullPointerException if <code>character</code> is
     *                              <code>null</code> or an empty string.
     * @throws IllegalArgumentException if <code>character</code> is not a user
     *                                  character
     */
    public boolean addCharacterToPlainAlphabet(String character)
	throws NullPointerException, IllegalArgumentException
    {
	if(character == null || character.length() == 0){
	    throw new NullPointerException("null or empty string argument");
	}else if(!isUserCharacter(character, locale)){
	    throw new IllegalArgumentException("'"+ character +"'"+
					       " is not a user character");
	}
	CollationKey characterCK = collator.getCollationKey(character);
	HashSet plainAlphaSet = new HashSet(subst.values());
	plainAlphaSet.addAll(plainAlpha);
	if(plainAlphaSet.contains(characterCK)){
	    return false;
	}
	int i = Collections.binarySearch(plainAlpha, characterCK);
	assert i<0 : "The character is already in the plain alphabet " +
	             "but was not found in the HashSet";
	i = -i-1;
	plainAlpha.add(i, characterCK);
	fireStateChanged(SubstitutionEvent.CIPHER_ALPHABET);
	return true;
    }

    /**
     * Checks that the character is a user character (e.g. a, ñ, ü) and
     * not a string of user characters (e.g. ch, ll)
     *
     * @param userChar the <code>String</code> with the user character
     * @param locale the <code>Locale</code> the character belongs to
     * @return <code>true</code> if the specified <code>String</code> is a user
     *         character, <code>false</code> otherwise.
     */
    private final static boolean isUserCharacter(String userChar,
						 Locale locale){
	BreakIterator charIterator= BreakIterator.getCharacterInstance(locale);
	charIterator.setText(userChar);
	int boundary = charIterator.first();
	int numChars = -1;
	while(boundary != BreakIterator.DONE){
	    boundary = charIterator.next();
	    numChars ++;
	}
	return numChars == 1;
    }

    /**
     * Returns <code>true</code> if the substitution is injective and
     * <code>false</code> if it is not.
     *
     * @return <code>true</code> if the substitution is injective and
     *         <code>false</code> if it is not.
     */
    public boolean isInjective(){
	return injective;
    }

    /**
     * Sets the substitution to injective or not injective.
     *
     * @param inj <code>true</code> if the substitution shoud be injective,
     *            <code>false</code> otherwise.
     */
    public void setInjective(boolean inj){
	if(injective == inj){
	    return;
	}
	Component[] components = getComponents();
	for(int i=0; i<components.length; i+=2){
	    assert components[i] instanceof CharMap :
		"Component at index " + i + " is not an instance of CharMap";
	    assert subst.containsKey(((CharMap)components[i]).getCharacter()) :
		"The user character \""+
		((CharMap)components[i]).getCharacter().getSourceString() +
		"\" is not in the substitution HashMap";
	    CharMap tmpCM = (CharMap)components[i];
	    tmpCM.setInjective(inj);
	    subst.put(tmpCM.getCharacter(), tmpCM.getReplacement());
	}
	injective = inj;
	fireStateChanged(SubstitutionEvent.SUBSTITUTION_PAIR);
    }

    /**
     * Returns a <code>ListModel</code> that may be used to display the
     * list of ignored characters in a <code>JList</code>.
     */
    public ListModel getIgnoredCharsListModel(){
	return ignoredCharsLM;
    }

    /**
     * Receives a <code>HashSet</code> with the characters
     * (<code>CollationKey</code>s ) to be set as ignored. Any character 
     * in the ignored list that is not in <code>igSet</code> is removed from
     * the list and added to <code>cipherAlpha</code> and any character in
     * <code>cipherAlpha</code> that is in <code>igSet</code> is removed
     * from <code>cipherAlpha</code> and added to the list of ignored
     * characters. This method does not fire a <code>SubstitutionEvent</code>.
     *
     * @param igSet a <code>HashSet</code> of <code>CollationKey</code>s
     * @throws NullPointerException if <code>igSet</code> is <code>null</code>
     * @see #setIgnoredCharsNoEvent(HashSet)
     */
    public void setIgnoredCharsNoEvent(HashSet igSet) throws NullPointerException
    {
	HashSet currIgSet = ignoredCharsLM.getIgnoredChars();
	HashSet alphaSet = new HashSet(cipherAlpha);
	CollationKey ck;
	for(Iterator iter=currIgSet.iterator(); iter.hasNext(); ){
	    ck = (CollationKey)iter.next();
	    if(!igSet.contains(ck)){
		includeIgnoredCharacterNoEvent(ck);
	    }
	}
	alphaSet.retainAll(igSet);
	for(Iterator iter=alphaSet.iterator(); iter.hasNext(); ){
	    addToIgnoredListNoEvent((CollationKey)iter.next());
	}
    }

    /**
     * Receives a <code>HashSet</code> with the characters
     * (<code>CollationKey</code>s ) to be set as ignored. Any character 
     * in the ignored list that is not in <code>igSet</code> is removed from
     * the list and added to <code>cipherAlpha</code> and any character in
     * <code>cipherAlpha</code> that is in <code>igSet</code> is removed
     * from <code>cipherAlpha</code> and added to the list of ignored
     * characters. Fires a <code>SubstitutionEvent</code> of type 
     * <code>SubstitutionEvent.IGNORED_CHARACTERS</code>.
     *
     * @param igSet a <code>HashSet</code> of <code>CollationKey</code>s
     * @throws NullPointerException if <code>igSet</code> is <code>null</code>
     * @see #setIgnoredCharsNoEvent(HashSet)
     */
    public void setIgnoredChars(HashSet igSet) throws NullPointerException
    {
	setIgnoredCharsNoEvent(igSet);
	fireStateChanged(SubstitutionEvent.IGNORED_CHARACTERS);
    }

    /**
     * Adds a character that is currently in the <code>MonoAlphaSubst</code>'s
     * cipher alphabet to the list of ignored characters 
     * (<code>ignoredCharsLM</code>).
     *
     * @param character the <code>CollationKey</code> of a user character
     * @throws IllegalArgumentException if <code>character</code> is not in
     *                                  <code>cipherAlpha</code>
     */
    private void addToIgnoredListNoEvent(CollationKey character) 
	throws IllegalArgumentException
    {
	int i = Collections.binarySearch(cipherAlpha, character);
	if(i<0){
	    throw new IllegalArgumentException();
	}
	Component[] comps = getComponents();
	int cmIdx = i*2; //index of the CharMap that displays character
	assert comps[cmIdx] instanceof CharMap : 
	    "Component at index "+ cmIdx +" is not an instance of CharMap";
	CharMap tmpCM = (CharMap)comps[cmIdx];
	// clear the CharMap's replacement character if any
	if(tmpCM.getReplacement() != null){
	    tmpCM.clearSelectionNoEvent();
	}
	/* remove the CharMap from the MonoAlphaSubst as well as the JSeparator
	   next to it (if any) */
	if(i == cipherAlpha.size()-1){
	    //remove the last CharMap
	    remove(cmIdx);
	    if(cipherAlpha.size()-1>0){
		assert comps[cmIdx-1] instanceof JSeparator:
		    "JSeparator expected at index "+ (cmIdx-1);
		// remove the JSeparator to it's left if any
		remove(cmIdx-1);
	    }
	}else{
	    assert comps[cmIdx+1] instanceof JSeparator:
		"JSeparator expected at index "+ (cmIdx+1);
	    //remove the JSeparator
	    remove(cmIdx+1);
	    //remove the CharMap
	    remove(cmIdx);
	}
	cipherAlpha.remove(i);
	subst.remove(character);
	i = ignoredCharsLM.binarySearch(character);
	if(i<0){
	    int index = -i-1;
	    ignoredCharsLM.add(index, character);
	} else{
	    assert false : "Impossible: The character '" + 
		           character.getSourceString() +
			   "' is among the ignored characters and was "+
			   "in cipherAlpha as well";
	}
	revalidate(); // validates this container and all of its subcomponents
    }

    /**
     * Returns the characters to be ignored in a <code>HashSet</code>
     *
     * @return the characters to be ignored in a <code>HashSet</code>
     */
    public HashSet getIgnoredChars(){
	return ignoredCharsLM.getIgnoredChars();
    }

    /**
     * Returns a <code>HashMap</code>, that uses the characters of the
     * cipher alphabet as keys and maps them to the characters of the 
     * plain alphabet they should be replaced by. The characters of the
     * cipher alphabet and plain alphabet are <code>CollationKey</code>s.
     * <br/>
     * If a character of the cipher alphabet is not present in the
     * <code>HashMap</code> as a key, then that character should be ignored;
     * if it is mapped to <code>null</code>, then no replacement character has
     * been set and should be replaced by <code>' '</code>; and if
     * it's mapped to another character, then it should be replaced by that
     * character.
     *
     * @return a <code>HashMap</code>, that uses the characters of the cipher
     *         alphabet as keys and maps them to the characters of the plain
     *         alphabet they should be replaced by.
     */
    public HashMap getSubstitution(){
	return subst;
    }

    /**
     * Returns the instance of <code>Collator</code> currently being used
     * by the <code>MonoAlphaSubst</code>
     *
     * @return the instance of <code>Collator</code> currently being used by
     *         the <code>MonoAlphaSubst</code>
     */
    public Collator getCollator(){
	return collator;
    }

    /**
     * Clears the selected substitution characters, but does not
     * fire an event.
     */
    private void clearSelectionsNoEvent(){
	Iterator iterator = cipherAlpha.iterator();
	while(iterator.hasNext()){
	    subst.put(iterator.next(), null);
	}
	Component[] components = getComponents();
	for(int i=0; i<components.length; i+=2){
	    assert components[i] instanceof CharMap :
		"Component at index " + i + " is not an instance of CharMap";
	    ((CharMap)components[i]).clearSelectionNoEvent();
	}
    }

    /**
     * Clears the selected substitution characters and fires a 
     * <code>SubstitutionEvent</code> of type <code>SUBSTITUTION_PAIR</code>
     */
    public void clearSelections(){
	clearSelectionsNoEvent();
	fireStateChanged(SubstitutionEvent.SUBSTITUTION_PAIR);
    }

    /**
     * Clears the selected substitution characters and sets
     * a substitution as close to the identity substitution as the plain
     * alphabet allows.<br/>
     * The method tries to assign each character in the cipher alphabet
     * itself as the substitution, if it is not in the plain alphabet, then the
     * upper (or lower) case  character is used, and if it is not in the plain
     * alphabet either, the substitution character is <code>null</code>.
     * For example, if the cipher alphabet is <code>{A, a, ñ}</code> and
     * the plain alphabet is <code>{a}</code> the method would set the
     * substitution to <code>A->null, a->a, ñ->null</code> if the substitution
     * is injective, and to <code>A->a, a->a, ñ->null</code> if it's not
     * injective.
     */
    public void selectIdentity(){
	clearSelectionsNoEvent();
	Component[] components = getComponents();
	CollationKey tmpChar;
	CollationKey tmpSubst;
	String tmpSubstStr;
	for(int i=0; i<components.length; i+=2){
	    assert components[i] instanceof CharMap :
		"Component at index " + i + " is not an instance of CharMap";
	    assert subst.containsKey(((CharMap)components[i]).getCharacter()) :
		"The user character \""+
		((CharMap)components[i]).getCharacter().getSourceString() +
		"\" is not in the substitution HashMap";
	    tmpChar = ((CharMap)components[i]).getCharacter();
	    int idx = Collections.binarySearch(plainAlpha, tmpChar);
	    if(idx >= 0){
		subst.put(tmpChar, tmpChar);
		((CharMap)components[i]).setSelectedItemNoEvent(tmpChar.getSourceString());
	    }
	}
	for(int i=0; i<components.length; i+=2){
	    tmpChar = ((CharMap)components[i]).getCharacter();
	    tmpSubstStr = tmpChar.getSourceString().toLowerCase(locale);
	    tmpSubst = collator.getCollationKey(tmpSubstStr);
	    if(subst.get(tmpChar)==null && !tmpChar.equals(tmpSubst)){
		int idx = Collections.binarySearch(plainAlpha, tmpSubst);
		if(idx >= 0){
		    subst.put(tmpChar, tmpSubst);
		    ((CharMap)components[i]).setSelectedItemNoEvent(tmpSubstStr);
		}
	    }
	}
	for(int i=0; i<components.length; i+=2){
	    tmpChar = ((CharMap)components[i]).getCharacter();
	    tmpSubstStr = tmpChar.getSourceString().toUpperCase(locale);
	    tmpSubst = collator.getCollationKey(tmpSubstStr);
	    if(subst.get(tmpChar)==null && !tmpChar.equals(tmpSubst)){
		int idx = Collections.binarySearch(plainAlpha, tmpSubst);
		if(idx >= 0){
		    subst.put(tmpChar, tmpSubst);
		    ((CharMap)components[i]).setSelectedItemNoEvent(tmpSubstStr);
		}
	    }
	}
	fireStateChanged(SubstitutionEvent.SUBSTITUTION_PAIR);
    }

    /**
     * Assigns characters from the plain alphabet that have not been
     * set to replace any of the cipher alphabet's characters as
     * substitution characters for those that don't have one. The
     * charcter are chosen in alphabetical order and are not repeated
     * (even if the substitution is not injective).<br/>
     *
     * For example, if the cipher alphabet is <code>{A, B, C, D, E, F}</code>,
     * the plain alphabet is <code>{a, b, c, d, e, f}</code> and the current
     * substitution is <code>{A->f, B->a, C->d, D->null, E->null, 
     * F->null }</code> the method would set the substitution to <code>{A->f,
     * B->a, C->d, D->b, E->c, F->e }</code>.
     */
    public void completeSelection(){
	ArrayList unselectedChars;
	if(injective){
	    /* In injective mode, the selected characters are removed from
	     * plainalpha by the CharMaps */
	    unselectedChars = new ArrayList(plainAlpha);
	}else{
	    HashSet tmpSet = new HashSet(plainAlpha);
	    tmpSet.removeAll(subst.values());
	    unselectedChars = new ArrayList(tmpSet);
	    Collections.sort(unselectedChars);
	}
	Iterator unselCharsIter = unselectedChars.iterator();
	Component[] components = getComponents();
	CharMap tmpCM;
	CollationKey tmpChar;
	CollationKey tmpSubst;
	for(int i=0; i<components.length && unselCharsIter.hasNext(); i+=2){
	    assert components[i] instanceof CharMap :
		"Component at index " + i + " is not an instance of CharMap";
	    assert subst.containsKey(((CharMap)components[i]).getCharacter()) :
		"The user character \""+
		((CharMap)components[i]).getCharacter().getSourceString() +
		"\" is not in the substitution HashMap";
	    tmpCM = (CharMap)components[i];
	    if(tmpCM.getReplacement() == null){
		tmpChar = tmpCM.getCharacter();
		tmpSubst = (CollationKey)unselCharsIter.next();
		subst.put(tmpChar, tmpSubst);
		tmpCM.setSelectedItemNoEvent(tmpSubst.getSourceString());
	    }
	}
	fireStateChanged(SubstitutionEvent.SUBSTITUTION_PAIR);
    }

    /**
     * Reverses the order of the selected substitution characters in the
     * substitution.<br/>
     * For example, if the selection is <code>{A->a, B->b, C->c, 
     * D->null}</code>, the method would set the substitution to
     * <code>{A->null, B->c, C->b, D->a}</code>
     */
    public void reverseSelection(){
	HashMap tmpHM = (HashMap)subst.clone();
	clearSelectionsNoEvent();
	Component[] components = getComponents();
	ArrayList reverseCipherAlpha = new ArrayList(cipherAlpha);
	Collections.reverse(reverseCipherAlpha);
	Iterator revIter = reverseCipherAlpha.iterator();
	CharMap tmpCM;
	CollationKey tmpChar;
	CollationKey tmpSubst;
	for(int i=0; i<components.length; i+=2){
	    assert components[i] instanceof CharMap :
		"Component at index " + i + " is not an instance of CharMap";
	    assert subst.containsKey(((CharMap)components[i]).getCharacter()) :
		"The user character \""+
		((CharMap)components[i]).getCharacter().getSourceString() +
		"\" is not in the substitution HashMap";
	    assert revIter.hasNext() : "The number of character in cipherAlpha and the number of CharMaps are different";
	    tmpCM = (CharMap)components[i];
	    tmpChar = (CollationKey)revIter.next();
	    tmpSubst = (CollationKey)tmpHM.get(tmpChar);
	    subst.put(tmpCM.getCharacter(), tmpSubst);
	    tmpCM.setSelectedItemNoEvent(tmpSubst == null ? null :
					 tmpSubst.getSourceString());
	}
	fireStateChanged(SubstitutionEvent.SUBSTITUTION_PAIR);
    }

    /**
     * Clears the selected substitution characters and sets
     * a substitution as close to the inverse of the one previously set
     * as the plain and cipher alphabets allow.<br/>
     * The method takes each pair of the form <i>(cipher alphabet character, 
     * plain alphabet character)</i>  from the substitution, where <i>cipher
     * alphabet character</i> also appears in the plain alphabet and <i>plain 
     * alphabet character</i> also appears in the cipher alphabet, and inverts
     * the order in which they appear in the pair. If <i>cipher alphabet 
     * character</i> does not appear in the plain alphabet as such, but with 
     * a different casing (upper or lower case) and this new character does not
     * appear in the cipher alphabet, then it is used instead of the original.
     * Similarly if <i>plain alphabet character</i> does not appear in the 
     * cipher alphabet in its current casing, but the character that results
     * from changing the casing does, and does not appear in the plain 
     * alphabet.
     */
    public void invertSelection(){
	HashMap tmpHM = (HashMap)subst.clone();
	clearSelectionsNoEvent();
	HashMap invHM = new HashMap();
	CollationKey keyCK;
	CollationKey keyCKcase;
	CollationKey valCK;
	CollationKey valCKcase;
	Iterator iter;
	boolean kINpa; // to indicate if the key is in the plain alphabet
	boolean vINca; // to indicate if the value is in the cipher alphabet
	byte kINpa_vINca; // to store kINpa and vINca in its first two lsb
	for(iter = tmpHM.keySet().iterator(); iter.hasNext();){
	    keyCK = (CollationKey)iter.next();
	    valCK = (CollationKey)tmpHM.get(keyCK);
	    if(valCK != null && !invHM.containsKey(valCK)){
		kINpa_vINca = 0;
		kINpa = Collections.binarySearch(plainAlpha, keyCK)>=0;
		vINca= Collections.binarySearch(cipherAlpha, valCK)>=0;
		kINpa_vINca |= kINpa ? (byte)0x2 : 0x0;
		kINpa_vINca |= vINca ? (byte)0x1 : 0x0;
		switch(kINpa_vINca){
		case 3:
		    invHM.put(valCK, keyCK);
		    break;
		case 1:
		    keyCKcase = changeCase(keyCK);
		    if(keyCKcase != null &&
		       Collections.binarySearch(cipherAlpha, keyCKcase)<0 &&
		       Collections.binarySearch(plainAlpha, keyCKcase)>=0){
			invHM.put(valCK, keyCKcase);
		    }
		    break;
		case 2:
		    valCKcase = changeCase(valCK);
		    if(valCKcase != null && 
		       !invHM.containsKey(valCKcase) &&
		       Collections.binarySearch(plainAlpha, valCKcase)<0 &&
		       Collections.binarySearch(cipherAlpha, valCKcase)>=0){
			invHM.put(valCKcase, keyCK);
		    }
		    break;
		case 0:
		    keyCKcase = changeCase(keyCK);
		    valCKcase = changeCase(valCK);
		    if(keyCKcase != null && valCKcase != null &&
		       !invHM.containsKey(valCKcase) &&
		       Collections.binarySearch(cipherAlpha, keyCKcase)<0 &&
		       Collections.binarySearch(plainAlpha, keyCKcase)>=0 &&
		       Collections.binarySearch(plainAlpha, valCKcase)<0 &&
		       Collections.binarySearch(cipherAlpha, valCKcase)>=0){
			invHM.put(valCKcase, keyCKcase);
		    }
		    break;
		default:
		    assert false: "The value of kINpa_vINca is out of range";
		    break;
		}
	    }
	}
	Component[] components = getComponents();
	CharMap tmpCM;
	for(iter = invHM.keySet().iterator(); iter.hasNext(); ){
	    keyCK = (CollationKey)iter.next();
	    valCK = (CollationKey)invHM.get(keyCK);
	    int i = Collections.binarySearch(cipherAlpha, keyCK) * 2;

	    assert i>=0 : keyCK.getSourceString() + 
		          " is not in the cipher alphabet";
	    assert i<components.length: 
		"There are more characters in the cipher alphabet than "+
		"CharMaps in the component";
	    assert components[i] instanceof CharMap:
		"Component at index "+ i + "is not a CharMap";

	    tmpCM = (CharMap)components[i];

	    assert keyCK.equals(tmpCM.getCharacter()):
		"The CharMap does not hold the expected key";
	    assert Collections.binarySearch(plainAlpha, valCK)>=0 :
		"valCK is not in the plain alphabet";
	    assert valCK != null :
		"null value associated to "+ keyCK.getSourceString();

	    subst.put(keyCK, valCK);
	    tmpCM.setSelectedItemNoEvent(valCK.getSourceString());
	}
	fireStateChanged(SubstitutionEvent.SUBSTITUTION_PAIR);
    }

    /**
     * Receives a <code>HashMap</code> that maps characters from the cipher
     * alphabet (<code>CollationKey</code>s ) to characters from the plain
     * alphabet (<code>CollationKey</code>s ) and sets the mapping as this
     * <code>MonoAlphaSubst</code>'s substitution. First of all the 
     * substitution is  cleared, and then, the characters from the cipher
     * alphabet that are being mapped by this <code>MonoAlphaSubst</code>
     * are assigned the substitution characters indicated by
     * <code>substitution</code> (if they belong to the plain alphabet).
     * The characters that are being ignored are left as ignored.
     *
     * @param selection a <code>HashMap</code> of <code>CollationKey</code>s
     * @throws NullPointerException if <code>selection</code> is <code>null</code>
     */
    public void setSelection(HashMap selection) throws NullPointerException
    {
	if(selection == null){
	    throw new NullPointerException();
	}
	if(subst.equals(selection)){
	    return;
	}
	clearSelectionsNoEvent();
	Component[] components = getComponents();
	CharMap tmpCM;
	CollationKey tmpChar;
	CollationKey tmpSubst;
	for(int i=0; i<components.length; i+=2){
	    assert components[i] instanceof CharMap :
		"Component at index " + i + " is not an instance of CharMap";
	    assert subst.containsKey(((CharMap)components[i]).getCharacter()) :
		"The user character \""+
		((CharMap)components[i]).getCharacter().getSourceString() +
		"\" is not in the substitution HashMap";
	    tmpCM = (CharMap)components[i];
	    tmpChar = tmpCM.getCharacter();
	    tmpSubst = (CollationKey)selection.get(tmpChar);
	    if(tmpSubst != null && 
	       Collections.binarySearch(plainAlpha, tmpSubst) >= 0){
		subst.put(tmpChar, tmpSubst);
		tmpCM.setSelectedItemNoEvent(tmpSubst.getSourceString());
	    }
	}
	fireStateChanged(SubstitutionEvent.SUBSTITUTION_PAIR);
    }

    /**
     * Receives a string (<code>ck</code>) made up exclusively of upper or 
     * lower case characters and returns a <code>CollationKey</code>
     * representation of the string in lower case or upper case respectively.
     *
     * @param ck the <code>CollationKey</code> of a string of upper or lower
     *           characters. <code>ck</code> must have been generated with the
     *           current <code>collator</code>
     * @return the <code>CollationKey</code> of the string in the different
     *         casing if such string exists and <code>null</code> otherwise
     *         (e.g. if the string is <code>&quot;;&quot;</code>).
     */
    private final CollationKey changeCase(CollationKey ck){
	String srcStr = ck.getSourceString();
	CollationKey tmpCK;
	tmpCK = collator.getCollationKey(srcStr.toUpperCase(locale));
	if(!ck.equals(tmpCK)){
	    return tmpCK;
	}
	tmpCK = collator.getCollationKey(srcStr.toLowerCase(locale));
	if(!ck.equals(tmpCK)){
	    return tmpCK;
	}
	return null;
    }

    /**
     * Shifts the selected substitution characters one
     * position to the <code>LEFT</code> or <code>RIGHT</code>.<br/>
     * For example, if the selection is <code>{A->a, B->b, C->c}</code>,
     * shifting to the right would result in the selection 
     * <code>{A->c, B->a, C->b}</code> and shifting to the left in
     * <code>{A->b, B->c, C->a}</code>.
     * 
     * @param direction either <code>LEFT</code> or <code>RIGHT</code>
     * @throws IllegalArgumentException if <code>direction</code> is not
     *                                  <code>RIGHT</code> or <code>LEFT</code>
     * @see #LEFT
     * @see #RIGHT
     */
    public void shiftSelection(int direction) throws IllegalArgumentException
    {
	if(direction!=LEFT && direction!=RIGHT){
	    throw new IllegalArgumentException();
	}
	Component[] components = getComponents();
	CollationKey tmpChar;
	CollationKey tmpSubst;
	String tmpSubstStr;
	CollationKey tmpSubst2;
	String tmpSubst2Str;
	int i;
	if(components.length < 3){ //two CharMaps and a JSeparator
	    return;
	}
	if(direction == LEFT){
	    tmpSubst2 = ((CharMap)components[0]).getReplacement();
	    tmpSubst2Str = tmpSubst2 == null ? null :
		                               tmpSubst2.getSourceString();
	    for(i=0; i<components.length-2; i+=2){
		assert components[i] instanceof CharMap :
		    "Component at index "+i+" is not an instance of CharMap";
		assert subst.containsKey(((CharMap)components[i]).getCharacter()) :
		    "The user character \""+
		    ((CharMap)components[i]).getCharacter().getSourceString() +
		    "\" is not in the substitution HashMap";
		tmpChar = ((CharMap)components[i]).getCharacter();
		tmpSubst = ((CharMap)components[i+2]).getReplacement();
		tmpSubstStr = tmpSubst == null ? null :
		                                 tmpSubst.getSourceString();
		subst.put(((CharMap)components[i+2]).getCharacter(), null);
		subst.put(tmpChar, tmpSubst);
		((CharMap)components[i+2]).clearSelectionNoEvent();
		((CharMap)components[i]).setSelectedItemNoEvent(tmpSubstStr);
	    }
	    tmpChar = ((CharMap)components[i]).getCharacter();
	    subst.put(tmpChar, tmpSubst2);
	    ((CharMap)components[i]).setSelectedItemNoEvent(tmpSubst2Str);
	} else{
	    tmpSubst = ((CharMap)components[0]).getReplacement();
	    subst.put(((CharMap)components[0]).getCharacter(), null);
	    ((CharMap)components[0]).clearSelectionNoEvent();
	    for(i=2; i<components.length; i+=2){
		assert components[i] instanceof CharMap :
		    "Component at index "+i+" is not an instance of CharMap";
		assert subst.containsKey(((CharMap)components[i]).getCharacter()) :
		    "The user character \""+
		    ((CharMap)components[i]).getCharacter().getSourceString() +
		    "\" is not in the substitution HashMap";
		tmpChar = ((CharMap)components[i]).getCharacter();
		tmpSubstStr = tmpSubst == null ? null :
		                                   tmpSubst.getSourceString();
		subst.put(tmpChar, tmpSubst);
		tmpSubst = ((CharMap)components[i]).getReplacement();
		((CharMap)components[i]).setSelectedItemNoEvent(tmpSubstStr);
	    }
	    tmpChar = ((CharMap)components[0]).getCharacter();
	    tmpSubstStr = tmpSubst == null ? null :
		                             tmpSubst.getSourceString();
	    subst.put(tmpChar, tmpSubst);
	    ((CharMap)components[0]).setSelectedItemNoEvent(tmpSubstStr);
	}
	fireStateChanged(SubstitutionEvent.SUBSTITUTION_PAIR);
    }

    /**
     * Sets the cipher alphabet to <code>cipherAlpha</code>.<br/>
     * Note that this method clears all the selected substitution characters
     * and ignored characters.<br/>
     * The <code>Collator</code> used to generate the 
     * <code>CollationKey</code>s in <code>cipherAlpha</code> must be
     * the same as the one currently being used by the 
     * <code>MonoAlphaSubst</code>.
     *
     * @param cipherAlpha the new cipher alphabet in the form of an ordered
     *                    <code>arrayList</code> of <code>CollationKey</code>s.
     * @throws NullPointerException if the argument is <code>null</code>
     */
    public void setCipherAlpha(ArrayList cipherAlpha) throws NullPointerException
    {
	if(cipherAlpha == null){
	    throw new NullPointerException();
	}
	this.cipherAlpha = cipherAlpha;
	ignoredCharsLM.clear();
	subst.clear();
	initSubst();
	//remove all the selections so plainAlpha returns to it's original form
	Component[] components = getComponents();
	for(int i=0; i<components.length; i++){
	    if(components[i] instanceof CharMap){
		((CharMap)components[i]).clearSelectionNoEvent();
	    }
	}
	removeAll(); //remove all the CharMaps from the MonoAlphaSubst
	addCharMaps(); // add CharMaps for all the characters in cipherAlpha
	fireStateChanged(SubstitutionEvent.CIPHER_ALPHABET);
	revalidate(); // validates this container and all of its subcomponents
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
	//change the collator used in ignoredCharsLM
	ignoredCharsLM.setCollator(collator);
	//change the collator used in subst
	subst.clear();
	initSubst();
	/* remove all the CharMaps from the MonoAlphaSubst and add
	   CharMaps for all the characters in cipherAlpha, since their
	   order might have changed */
	removeAll();
	addCharMaps();
	fireStateChanged(SubstitutionEvent.CIPHER_ALPHABET);
	revalidate(); // validates this container and all of its subcomponents
    }

    /**
     * Adds a <code>ChangeListener</code> to the <code>MonoAlphaSubst</code>.
     * <br/>
     * The <code>ChangeListener</code> will receive a 
     * <code>SubstitutionEvent</code> when <code>subst</code>'s state changes 
     * (keys-values were added/removed or the values for one or more keys 
     * changed).
     *
     * @param l the <code>ChangeListener</code> that sould be notified
     * @see SubstitutionEvent
     */
    public void addChangeListener(ChangeListener l){
	listenerList.add(ChangeListener.class, l);
    }

    /**
     * Removes a <code>ChangeListener</code> from the 
     * <code>MonoAlphaSubst</code>.
     *
     * @param l the <code>ChangeListener</code> to be removed
     */
    public void removeChangeListener(ChangeListener l){
	listenerList.remove(ChangeListener.class, l);
    }

    /**
     * Returns an array of all the <code>ChangeListeners</code> added to 
     * this <code>MonoAlphaSubst</code> with <code>addChangeListener()</code>
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
     *             <code>SubstitutionEvent.SUBSTITUTION_PAIR</code> or
     *             <code>SubstitutionEvent.IGNORE_CHARACTERS</code>.
     * @see SubstitutionEvent
     */
    protected void fireStateChanged(byte type){
	// Guaranteed to return a non-null array
	Object[] listeners = listenerList.getListenerList();
	ChangeEvent changeEvent = new SubstitutionEvent(this, type);
	// Process the listeners last to first, notifying
	// those that are interested in this event
	for (int i = listeners.length-2; i>=0; i-=2) {
	    if (listeners[i]==ChangeListener.class) {
		((ChangeListener)listeners[i+1]).stateChanged(changeEvent);
	    }
	}
    }

    /**
     * Returns <code>getPreferredSize()</code>
     *
     * @return <code>getPreferredSize()</code>
     */
    public Dimension getPreferredScrollableViewportSize(){
	Component[] comps = getComponents();
	Dimension retDim;
	if(comps.length > 0){
	    retDim = getPreferredSize();
	} else{
	    CharMap cm = new CharMap(collator.getCollationKey("m"), 
				     collator, new ArrayList());
	    Dimension dim = cm.getPreferredSize();
	    retDim = new Dimension(getPreferredSize());
	    if(dim.width > retDim.width){
		retDim.width = dim.width;
	    }
	    if(dim.height > retDim.height){
		retDim.height = dim.height;
	    }
	}
	return retDim;
    }

    /**
     * Returns the scroll increment that will completely expose a new
     * <code>CharMap</code> if <code>orientation</code> equals
     * <code>SwingConstants.HORIZONTAL</code> and 1 otherwise.
     *
     * @param visibleRect The area visible within the viewport
     * @param orientation <code>SwingConstants.HORIZONTAL</code> or
     *                    <code>SwingConstants.VERTICAL</code>
     * @param direction Less than zero to scroll up/left, greater than zero for
     *                  down/right.
     * @return The "unit" increment for scrolling in the specified direction.
     */
    public int getScrollableUnitIncrement(Rectangle visibleRect,
					  int orientation,
					  int direction)
    {
	if(orientation == SwingConstants.VERTICAL){
	    return 1;
	}else if(cipherAlpha.size()==0){
	    return 0;
	}
	int unit = (int)Math.ceil((double)getPreferredSize().width/(double)cipherAlpha.size());
	int increment = 1;
	if(direction > 0){
	    increment = unit-(visibleRect.x%unit);
	    if(increment < unit){
		increment += unit;
	    }
	}else{
	    increment = visibleRect.x%unit;
	    if(increment == 0){
		increment = unit;
	    }
	}
	return increment;
    }

    /**
     * Returns the scroll increment that will completely expose one 
     * block of <code>CharMap</code>s if <code>orientation</code> equals
     * <code>SwingConstants.HORIZONTAL</code> and 1 otherwise.
     *
     * @param visibleRect The area visible within the viewport
     * @param orientation <code>SwingConstants.HORIZONTAL</code> or
     *                    <code>SwingConstants.VERTICAL</code>
     * @param direction Less than zero to scroll up/left, greater than zero for
     *                  down/right.
     * @return The "block" increment for scrolling in the specified direction.
     */
    public int getScrollableBlockIncrement(Rectangle visibleRect,
					   int orientation,
					   int direction)
    {
	int unit = (int)Math.ceil((double)getPreferredSize().width/(double)cipherAlpha.size());
	int increment = 1;
	if(unit > 0){
	    if(direction > 0){
		int offset = unit-(visibleRect.x%unit);
		offset = offset < unit ? offset : 0;
		increment = ((visibleRect.width-offset)/unit)*unit;
		increment += offset;
	    }else{
		int offset = unit-(visibleRect.x%unit);
		offset = offset < unit ? offset : 0;
		increment = (visibleRect.width/unit)*unit-offset;
	    }
	}
	return increment;
    }

    /**
     * Returns <code>false</code> so the viewport does not force width of
     * the component.
     *
     * @return <code>false</code>
     */
    public boolean getScrollableTracksViewportWidth(){
	return false;
    }

    /**
     * Returns <code>false</code> so the viewport does not force height of
     * the component.
     */
    public boolean getScrollableTracksViewportHeight(){
	return false;
    }

    /** 
     * <code>ListModel</code> used to store the characters to be ignored
     * in the ciphertext that that may be used to display them in a
     * <code>JList</code>.
     *
     * @author Jesús Adolfo García Pasquel
     * @version 0.01 September 2003
     * @see CollationKeyListModel
     */    
    private class IgnoredCharsListModel extends CollationKeyListModel{

	/**
	 * Creates an empty <code>IgnoredCharsListModel</code>.
	 */
	public IgnoredCharsListModel(){
	    super();
	}

	/**
	 * Returns the character at the specified index in the form of a
	 * <code>CollationKey</code>.
	 *
	 * @param index index of the element to return.
	 * @return character at the specified index in the form of a
	 *         <code>CollationKey</code>
	 */
	public CollationKey getCharAt(int index){
	    return (CollationKey)ckList.get(index);
	}

	/**
	 * Returns the characters to be ignored in a <code>HashSet</code>
	 *
	 * @return the characters to be ignored in a <code>HashSet</code>
	 */
	public HashSet getIgnoredChars(){
	    return new HashSet(ckList);
	}

	/**
	 * Searches for the specified <code>character</code> using the binary
	 * search algorithm. The list must be sorted in ascending order
	 * according to the natural ordering of its elements prior to making
	 * this call. If it is not sorted, the results are undefined. If the
	 * list contains multiple elements equal to the specified object,
	 * there is no guarantee which one will be found.
	 *
	 * @param character the <code>CollationKey</code> to look for.
	 * @return the index of the <code>character</code>, if it is contained;
	 *         otherwise, (-(<i>insertion point</i>) - 1)
	 * @throws ClassCastException if the list contains elements that are
	 *                   not mutually comparable, or the search key is not
	 *                   mutually comparable with the elements of the list.
	 */
	public int binarySearch(CollationKey character) throws ClassCastException
	{
	    return Collections.binarySearch(ckList, character);
	}

	/**
	 * Changes the <code>Collator</code> used to generate the 
	 * <code>CollationKey</code>s in <code>ckList</code>
	 * to the one passed as argument.
	 *
	 * @param col the <code>Collator</code> that will be used to generate
	 *            the <code>CollationKey</code>s in <code>ckList</code>.
	 * @throws NullPointerException if <code>col</code> is <code>null</code>
	 */
	public void setCollator(Collator col) throws NullPointerException
	{
	    if(col == null){
		throw new NullPointerException();
	    }
	    for(int i=0; i<ckList.size(); i++){
		ckList.set(i, col.getCollationKey(((CollationKey)ckList.get(i)).getSourceString()));
	    }
	    Collections.sort(ckList);
	    int last = ckList.size()-1;
	    last = last>=0 ? last : 0;
	    fireContentsChanged(this, 0, last);
	}
    }

    /**
     * A small program used to test the component
     */
    public static void main(String[] args){
	JFrame frame = new JFrame("Test");
	JScrollPane scrollP = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_NEVER,
					      JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
	//scrollP.setPreferredSize(new Dimension(200, 67));
	frame.getContentPane().add(scrollP);
	Locale loc = JComponent.getDefaultLocale();
	Collator collator = Collator.getInstance(loc);
	CollationKey [] arr1 = new CollationKey[16];
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
	ArrayList lst1 = new ArrayList(Arrays.asList(arr1));
	CollationKey [] arr2 = new CollationKey[6];
	arr2[0] = collator.getCollationKey("á");
	arr2[1] = collator.getCollationKey("é");
	arr2[2] = collator.getCollationKey("í");
	arr2[3] = collator.getCollationKey("ó");
	arr2[4] = collator.getCollationKey("ú");
	arr2[5] = collator.getCollationKey("ü");
	ArrayList lst2 = new ArrayList(Arrays.asList(arr2));
	final MonoAlphaSubst mas = new MonoAlphaSubst(loc, collator,
						      lst2, lst1);
	scrollP.setViewportView(mas);
	ChangeListener change = new ChangeListener(){
		public void stateChanged(ChangeEvent e){
		    System.out.println("State changed");
		}
	    };
	mas.addChangeListener(change);
	JCheckBox injective = new JCheckBox("injective");
	injective.setSelected(true);
	injective.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent e){
		    JCheckBox checkbox = (JCheckBox)e.getSource();
		    mas.setInjective(checkbox.isSelected());
		}
	    });
	frame.getContentPane().add(injective, BorderLayout.EAST);
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	frame.pack();
	frame.setVisible(true);

	JFrame igFrame = new JFrame("Ignored");
	igFrame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
	final JList ignoreJL = new JList(mas.getIgnoredCharsListModel());
	JScrollPane igScroll = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
					       JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
	igScroll.setPreferredSize(new Dimension(100, 150));
	igScroll.setViewportView(ignoreJL);
	igFrame.getContentPane().add(igScroll);
	JButton includeBtn = new JButton("include");
	includeBtn.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent e){
		    mas.includeIgnoredCharacters(ignoreJL.getSelectedIndices());
		}
	    });
	igFrame.getContentPane().add(includeBtn, BorderLayout.SOUTH);
	igFrame.pack();
	igFrame.setVisible(true);
    }
}
/*
 * -- MonoAlphaSubst.java ends here --
 */
