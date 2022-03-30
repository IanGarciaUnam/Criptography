/*
 * -- CiphertextManager.java --
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

import java.util.*;
import java.text.*;
import net.sourceforge.ganzua.component.*;

//used by main()
import java.io.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import net.sourceforge.ganzua.event.*;

/**
 * Class used to handle the ciphertext in monoalphabetic and polyalphabetic
 * substitutions.
 *
 * @author Jesús Adolfo García Pasquel
 * @version 0.01 September 2003
 */
public class CiphertextManager{

    /**
     * <code>Locale</code> used to identify the language in which the 
     * ciphertext was (or is thought to be) written.
     */
    protected Locale locale;

    /**
     * Used to create the <code>CollationKey</code>s used to sort
     * the character, bigram and trigram lists.
     */
    protected Collator collator;

     /**
     * Used to determine the limits of user characters in the input file, 
     * since they can span more than one Unicode character, e.g. ü may be
     * a combination of u and ¨.
     */
    protected BreakIterator charIterator;

    /**
     * The ciphertex in its original form.
     */
    protected String ciphertext;

    /**
     * The ciphertext in the form of an <code>ArrayList</code> of 
     * <code>CollationKey</code>s of user characters
     */
    protected ArrayList ciphertextAL;

    /**
     * Indicates if white spaces (characters that return <code>true</code>
     * to <code>java.lang.Character.isWhitespace()</code>) should be ignored
     * when getting the character, bigram and trigram frequencies. 
     * (<code>true</code> by default)
     */
    protected boolean ignoreWhite = true;

    /**
     * Indicates if control characters (characters that return 
     * <code>true</code> to <code>java.lang.Character.isISOControl()</code>)
     * should be ignored when getting the character, bigram and trigram 
     * frequencies. (<code>true</code> by default)
     */
    protected boolean ignoreControl = true;

    /**
     * Used to register all the <code>ChangeListener</code>s interested
     * in the <code>CiphertextManager</code> */
    private EventListenerList listenerList = new EventListenerList();

    /**
     * Creates a new <code>CiphertextManager</code> for the 
     * <code>ciphertext</code> that uses the <code>Collator</code> 
     * <code>col</code>.
     *
     * @param loc the <code>Locale</code> used to identify the language
     * @param col the <code>Collator</code> used to generate
     *            <code>CollationKeys</code>
     * @param ciphertext the ciphertext
     * @throws NullPointerException if any of the arguments is <code>null</code>
     */
    public CiphertextManager(Locale loc,
			     Collator col,
			     String ciphertext) throws NullPointerException
    {
	if(loc ==null || col == null || ciphertext==null){
	    throw new NullPointerException();
	}
	this.ciphertext = new String(ciphertext);
	this.locale = loc;
	this.collator = col;
	charIterator = BreakIterator.getCharacterInstance(locale);
	ciphertextAL = new ArrayList();
	initCiphertextAL();
    }

    /**
     * Method that initializes <code>ciphertextAL</code>.
     */
    private final void initCiphertextAL(){
	CollationKey uc; // the CollationKey of a user character
	charIterator.setText(ciphertext);
	int start = charIterator.first();
	for (int end = charIterator.next(); 
	     end != BreakIterator.DONE;
	     start = end, end = charIterator.next()) {
	    uc = collator.getCollationKey(ciphertext.substring(start, end));
	    ciphertextAL.add(uc);
	}
    }

    /**
     * Returns a sorted <code>ArrayList</code> with the user characters that
     * appear in the ciphertext in the form of <code>CollationKey</code>s. 
     */
    public ArrayList getCipherAlphabet(){
	ArrayList ret = null;
	HashSet alphaSet = new HashSet(100);
	alphaSet.addAll(ciphertextAL);
	CollationKey uCharKey = null;
	String uChar = null;
	boolean isCtrl;    // true if the current character isISOControl
	boolean isWhite;   // true if the current character isWhitespace
	for(Iterator iter=alphaSet.iterator(); iter.hasNext(); ){
	    uCharKey = (CollationKey)iter.next();
	    uChar = uCharKey.getSourceString();
	    isCtrl = Character.isISOControl(uChar.charAt(0));
	    isWhite = Character.isWhitespace(uChar.charAt(0));
	    if((isCtrl && ignoreControl) || (isWhite && ignoreWhite)){
		iter.remove();
	    }
	}
	ret = new ArrayList(alphaSet);
	Collections.sort(ret);
	return ret;
    }

    /**
     * Method that gets the relative frequencies of the characters, digrams and
     * trigrams in the ciphertext in the case of a monoalphabetic cipher
     * (<code>numAlpha == 1</code>) or the frequencies of characters in the
     * different alphabets of a polyalphabetic cipher 
     * (<code>numAlpha &gt; 1</code>). The caracters contained in the
     * lists of <code>StringFreq</code>s are only those that appear in
     * the ciphertext.<br/>
     *
     * Note that the characters in the <code>Set</code>s of 
     * <code>ignoredChars</code> will be ignored and that 'white' and 'control'
     * charactesrs will be ignored according to the values of 
     * <code>ignoreWhite</code> and <code>ignoreControl</code> respectively.
     *
     * @param numAlpha the number of alphabets 
     * @param ignoredChars a <code>List</code> of <code>Set</code>s that
     *                     contain the characters (<code>CollationKey</code>s)
     *                     to be ignored in each alphabet.
     * @return a fixed-size <code>List</code> of <code>ArrayList</code>s of
     *         <code>StringFreq</code>s. If <code>numAlpha==1</code> the list's
     *         size is 3 and at index <code>0</code> is the list of characters
     *         frequencies, at <code>1</code> the bigram frequencies and at
     *         <code>2</code> the trigram frequencies. If
     *         <code>numAlpha &gt; 1</code> the list's size is 
     *         <code>numAlpha</code> and each index has the relative
     *         frequencies of the characters in those alphabets.
     * @throws IllegalArgumentException if <code>numAlpha &lt; 1</code> or if
     *                            <code>ignoredChars.size() != numAlpha</code>
     * @throws NullPointerException if <code>ignoredChars</code> is <code>null</code>
     */
    public java.util.List getFrequencies(int numAlpha,
					 java.util.List ignoredChars) throws IllegalArgumentException, NullPointerException
    {
	if(numAlpha < 1){
	    throw new IllegalArgumentException("Number of alphabets less than 1");
	}else if(ignoredChars == null){
	    throw new NullPointerException("ignoredChars can not be null");
	}else if(ignoredChars.size() != numAlpha){
	    throw new IllegalArgumentException("ignoredChars.size() does not match numAlpha");
	}
	Iterator iter = ciphertextAL.iterator();
	Set igSet = null; //set of ignored characters
	/* sb is used to avoid the creation of StringBuffers caused by the 
	   String operator + */
	StringBuffer sb = new StringBuffer();
	CollationKey uCharKey = null;
	String uChar = null;
	boolean isCtrl;    // true if the current character isISOControl
	boolean isWhite;   // true if the current character isWhitespace
	boolean isIgnored; // true if the current character is in igSet
	ArrayList[] freqs; // used to store the relative frequencies
	if(numAlpha == 1){ // monoalphabetic
	    String prevUChar = null; //Used in the creation of the trigram list
	    String lastUChar = null; //Used in the creation of the bigram list
	    igSet = (Set)ignoredChars.get(0);
	    freqs = new ArrayList[3];
	    for(int i=0; i<3; i++){
		freqs[i] = new ArrayList();
	    }
	    while(iter.hasNext()){
		uCharKey = (CollationKey)iter.next();
		uChar = uCharKey.getSourceString();
		isCtrl = Character.isISOControl(uChar.charAt(0));
		isWhite = Character.isWhitespace(uChar.charAt(0));
		isIgnored = igSet.contains(uCharKey);
		if(!isIgnored && 
		   (!(isCtrl || isWhite) ||
		    (isCtrl && !ignoreControl) || (isWhite && !ignoreWhite))){
		    add(uCharKey, freqs[0]);
		    if(prevUChar != null){
			sb.delete(0, sb.length());
			String tmp = sb.append(prevUChar).append(lastUChar).append(uChar).toString();
			add(collator.getCollationKey(tmp), freqs[2]);
		    }
		    if(lastUChar != null){
			sb.delete(0, sb.length());
			String tmp = sb.append(lastUChar).append(uChar).toString();
			add(collator.getCollationKey(tmp), freqs[1]);
			prevUChar = lastUChar;
		    }
		    lastUChar = uChar;
		}
	    }
	}else{ // polyalphabetic
	    int i; // the alphabet the current character is in
	    freqs = new ArrayList[numAlpha];
	    for(i=0; i<numAlpha; i++){
		freqs[i] = new ArrayList();
	    }
	    i=0;
	    while(iter.hasNext()){
		igSet = (Set)ignoredChars.get(i);
		uCharKey = (CollationKey)iter.next();
		uChar = uCharKey.getSourceString();
		isCtrl = Character.isISOControl(uChar.charAt(0));
		isWhite = Character.isWhitespace(uChar.charAt(0));
		isIgnored = igSet.contains(uCharKey);
		if(!isIgnored && 
		   (!(isCtrl || isWhite) ||
		    (isCtrl && !ignoreControl) || (isWhite && !ignoreWhite))){
		    add(uCharKey, freqs[i]);
		    i++;
		    i = i%numAlpha;
		}
	    }
	}
	return Arrays.asList(freqs);
    }

    /**
     * Adds a <code>StringFreq</code> with <code>strKey</code> and frequency 1
     * to the ordered <code>List</code> <code>list</code> if it is not already
     * in it and increments the number of occurrences otherwise.
     *
     * @param strKey the string's <code>CollationKey</code>
     * @param list the ordered <code>List</code> of arrays of <code>Object</code>s
     */
    private static final void add(CollationKey strKey, java.util.List list){
	StringFreqCollationKeyComparator csComp = new StringFreqCollationKeyComparator();
	int idx = Collections.binarySearch(list, strKey, csComp);
	if(idx>=0){ // increment the number of occurrences
	    ((StringFreq)list.get(idx)).increment();
	} else{ // add it to the list
	    list.add(-(idx+1), new StringFreq(strKey, 1));
	}
    }

    /**
     * Method that gets the relative frequencies of the characters, digrams and
     * trigrams in the ciphertext in the case of a monoalphabetic cipher
     * (<code>numAlpha == 1</code>) or the frequencies of characters in the
     * different alphabets of a polyalphabetic cipher 
     * (<code>numAlpha &gt; 1</code>). The caracters contained in the
     * lists of <code>StringFreq</code>s are those that appear in
     * the <code>List</code> <code>alphabets</code>.<br/>
     *
     * Note that the characters that do not appear in their respective
     * entry in <code>alphabets</code>s will be ignored and that
     * 'white' and 'control' charactesrs will be ignored according to th
     * values of <code>ignoreWhite</code> and <code>ignoreControl</code>
     * respectively.
     *
     * @param alphabets <code>List</code> of <code>Set</code>s that contain the
     *                  characters (<code>CollationKey</code>s) in the cipher
     *                  alphabet minus those to be ignored in each alphabet.
     *                  <code>alphabets.size()</code> must be equal to the
     *                  number of alphabets in the substitution.
     * @return a fixed-size <code>List</code> of <code>ArrayList</code>s of
     *         <code>StringFreq</code>s. If <code>numAlpha==1</code> the list's
     *         size is 3 and at index <code>0</code> is the list of characters
     *         frequencies, at <code>1</code> the bigram frequencies and at
     *         <code>2</code> the trigram frequencies. If
     *         <code>numAlpha &gt; 1</code> the list's size is
     *         <code>numAlpha</code> and each index has the relative
     *         frequencies of the characters at those alphabets.
     * @throws IllegalArgumentException if <code>alphabets.size() &lt; 1</code>
     * @throws NullPointerException if <code>alphabets</code> is <code>null</code>
     */
    public java.util.List getFrequencies(java.util.List alphabets) throws IllegalArgumentException, NullPointerException
    {
	if(alphabets == null){
	    throw new NullPointerException("alphabets can not be null");
	}else if(alphabets.size() < 1){
	    throw new IllegalArgumentException("alphabets must contain at least one alphabet");
	}
	int numAlpha = alphabets.size();
	Iterator iter = ciphertextAL.iterator();
	Set alphaSet = null; //set of characters not to be ignored
	/* sb is used to avoid the creation of StringBuffers caused by the 
	   String operator + */
	StringBuffer sb = new StringBuffer();
	CollationKey uCharKey = null;
	String uChar = null;
	boolean isCtrl;    // true if the current character isISOControl
	boolean isWhite;   // true if the current character isWhitespace
	boolean isInAlpha; // true if the current character is in alphaSet
	ArrayList[] freqs; // used to store the relative frequencies
	if(numAlpha == 1){ // monoalphabetic
	    String prevUChar = null; //Used in the creation of the trigram list
	    String lastUChar = null; //Used in the creation of the bigram list
	    alphaSet = (Set)alphabets.get(0);
	    freqs = new ArrayList[3];
	    for(int i=0; i<3; i++){
		freqs[i] = new ArrayList();
	    }
	    addAsStringFreqs((Set)alphabets.get(0), freqs[0]);
	    while(iter.hasNext()){
		uCharKey = (CollationKey)iter.next();
		uChar = uCharKey.getSourceString();
		isCtrl = Character.isISOControl(uChar.charAt(0));
		isWhite = Character.isWhitespace(uChar.charAt(0));
		isInAlpha = alphaSet.contains(uCharKey);
		if(isInAlpha && 
		   (!(isCtrl || isWhite) ||
		    (isCtrl && !ignoreControl) || (isWhite && !ignoreWhite))){
		    add(uCharKey, freqs[0]);
		    if(prevUChar != null){
			sb.delete(0, sb.length());
			String tmp = sb.append(prevUChar).append(lastUChar).append(uChar).toString();
			add(collator.getCollationKey(tmp), freqs[2]);
		    }
		    if(lastUChar != null){
			sb.delete(0, sb.length());
			String tmp = sb.append(lastUChar).append(uChar).toString();
			add(collator.getCollationKey(tmp), freqs[1]);
			prevUChar = lastUChar;
		    }
		    lastUChar = uChar;
		}
	    }
	}else{ // polyalphabetic
	    int i; // the alphabet the current character is in
	    freqs = new ArrayList[numAlpha];
	    for(i=0; i<numAlpha; i++){
		freqs[i] = new ArrayList();
		addAsStringFreqs((Set)alphabets.get(i), freqs[i]);
	    }
	    i=0;
	    while(iter.hasNext()){
		alphaSet = (Set)alphabets.get(i);
		uCharKey = (CollationKey)iter.next();
		uChar = uCharKey.getSourceString();
		isCtrl = Character.isISOControl(uChar.charAt(0));
		isWhite = Character.isWhitespace(uChar.charAt(0));
		isInAlpha = alphaSet.contains(uCharKey);
		if(isInAlpha && 
		   (!(isCtrl || isWhite) ||
		    (isCtrl && !ignoreControl) || (isWhite && !ignoreWhite))){
		    add(uCharKey, freqs[i]);
		    i++;
		    i = i%numAlpha;
		}
	    }
	}
	return Arrays.asList(freqs);
    }

    /**
     * Method that adds all the <code>CollationKey</code>s in a
     * <code>Set</code> to a <code>List</code> as <code>StringFreq</code>s
     * of frequency <code>0</code> and sorts the <code>List</code>.
     *
     * @param ckSet a <code>Set</code> that contains <code>CollationKey</code>s
     *              exclusively
     * @param sfLst the <code>List</code> where the <code>StringFreq</code>s
     *              will be added
     * @throws NullPointerException if any of the arguments is <code>null</code>
     */
    private static void addAsStringFreqs(Set ckSet, java.util.List sfLst) 
	throws NullPointerException
    {
	for(Iterator iter=ckSet.iterator(); iter.hasNext(); ){
	    sfLst.add(new StringFreq((CollationKey)iter.next(), 0));
	}
	Collections.sort(sfLst);
    }

    /**
     * Returns the ciphertext's coincidence index.
     *
     * @param ignoredChars a <code>Set</code> that contains the
     *                     <code>CollationKey</code>s of the characters to
     *                     ignore (do as if they did not appear in the
     *                     ciphertext) while calculating the coincidence index.
     * @return the ciphertext's coincidence index
     */
    public double getCoincidenceIndex(Set ignoredChars) throws NullPointerException
    {
	if(ignoredChars == null){
	    throw new NullPointerException();
	}
	int numChars = 0; // stores the number of characters not to be ignored
	double ci = 0; // coincidence index
	ArrayList freqs = new ArrayList();
	Iterator iter = ciphertextAL.iterator();
	CollationKey uCharKey = null;
	String uChar = null;
	boolean isCtrl;    // true if the current character isISOControl
	boolean isWhite;   // true if the current character isWhitespace
	boolean isIgnored; // true if the current character is in ignoredChars
	while(iter.hasNext()){
	    uCharKey = (CollationKey)iter.next();
	    uChar = uCharKey.getSourceString();
	    isCtrl = Character.isISOControl(uChar.charAt(0));
	    isWhite = Character.isWhitespace(uChar.charAt(0));
	    isIgnored = ignoredChars.contains(uCharKey);
	    if(!isIgnored && 
	       (!(isCtrl || isWhite) ||
		(isCtrl && !ignoreControl) || (isWhite && !ignoreWhite))){
		add(uCharKey, freqs);
		numChars++;
	    }
	}
	double N2 = (double)numChars * (double)(numChars - 1);
	int charFreq; // frequency of a user character
	iter = freqs.iterator();
	while(iter.hasNext()){
	    charFreq = ((StringFreq)iter.next()).getFrequency();
	    ci += ((double)charFreq * (double)(charFreq -1))/N2;
	}
	return ci;
    }

    /**
     * Gets an estimate of the number of alphabets that were used
     * to get the ciphertext given the coincidence index of a
     * text, number of characters the plain alphabet has and the
     * coincidence index of representative text in the language.<br/>
     * <br/>
     * Note that the method assumes that the number of characters in the
     * cipheralphabet is the same as the one in the plainalphabet. If that
     * is not the case, the estimate will be meaningless.
     *
     * @param ignoredChars a <code>Set</code> that contains the
     *                     <code>CollationKey</code>s of the cipher alphabet's
     *                     characters to ignore (do as if they did not appear
     *                     in the ciphertext) while calculating ciphertext's
     *                     coincidence index (needed to get the estimate).
     * @param langCI coincidence index of a representative text in the language
     * @param numCharsLang number of characters the plain alphabet has
     * @throws IllegalArgumentException if <code>langCI &lt;= 0</code> or <code><code>langCI &gt;= 1</code>
     * @throws NullPointerException if <code>ignoredChars</code> is <code>null</code>
     */
    public double getNumberOfAlphabetsEstimate(Set ignoredChars,
					       double langCI,
					       int numCharsLang)
	throws NullPointerException, IllegalArgumentException
    {
	if(langCI <= 0 || langCI>=1){
	    throw new IllegalArgumentException("langCI out of range");
	}else if(numCharsLang <= 0){
	    throw new IllegalArgumentException("numCharsLang out of range");
	}
	double textCI = getCoincidenceIndex(ignoredChars);
	double randCI = 1.0/(double)numCharsLang;
	double diff = langCI - randCI;
	int numCharsText = 0; //number of characters not to be ignored
	CollationKey uCharKey = null;
	String uChar = null;
	boolean isCtrl;    // true if the current character isISOControl
	boolean isWhite;   // true if the current character isWhitespace
	boolean isIgnored; // true if the current character is in ignoredChars
	for(Iterator iter = ciphertextAL.iterator(); iter.hasNext(); ){
	    uCharKey = (CollationKey)iter.next();
	    uChar = uCharKey.getSourceString();
	    isCtrl = Character.isISOControl(uChar.charAt(0));
	    isWhite = Character.isWhitespace(uChar.charAt(0));
	    isIgnored = ignoredChars.contains(uCharKey);
	    if(!isIgnored && 
	       (!(isCtrl || isWhite) ||
		(isCtrl && !ignoreControl) || (isWhite && !ignoreWhite))){
		numCharsText++;
	    }
	}
	double estimate = (diff*(double)numCharsText) / (textCI*(double)(numCharsText-1) - randCI*numCharsText + langCI);
	return estimate;
    }

    /**
     * Returns a <code>String</code> with this <code>CiphertextManager</code>'s
     * ciphertext
     *
     * @return <code>String</code> with the ciphertext
     */
    public String getCiphertext(){
	return ciphertext;
    }

    /**
     * Returns a <code>String</code> with a copy of the ciphertext
     * where all the instances of the user characters in 
     * <code>toBeRemoved</code> have been removed.
     *
     * @param toBeRemoved the user characters to be removed
     */
    public String getCiphertextMinus(CollationKey[] toBeRemoved)
    {
	if(toBeRemoved == null || toBeRemoved.length == 0){
	    return ciphertext;
	}
	HashSet remHS = new HashSet(Arrays.asList(toBeRemoved));
	StringBuffer sb = new StringBuffer();
	CollationKey tmpCK;
	for(Iterator iter=ciphertextAL.iterator(); iter.hasNext(); ){
	    tmpCK = (CollationKey)iter.next();
	    if(!remHS.contains(tmpCK)){
		sb.append(tmpCK.getSourceString());
	    }
	}
	return sb.toString();
    }

    /**
     * Method that groups the ciphertext's characters in blocks of
     * the specified size.
     * 
     * @param blockSize the number of characters each block should have
     * @throws IllegalArgumentException if <code>blockSize &lt; 1</code> 
     */
    public void setCiphertextInBlocksOf(int blockSize) 
	throws IllegalArgumentException
    {
	if(blockSize < 1){
	    throw new IllegalArgumentException();
	}
	StringBuffer sb = new StringBuffer(); //stores the new ciphertext
	CollationKey uCharKey = null;
	String uChar = null;
	int maxCharsLine = 60; // maximum number of characters per line
	int blocksInLine = 0;  // blocks that have been put in the line
	String newline = System.getProperty("line.separator");
	int i=0;
	for(Iterator iter=ciphertextAL.iterator(); iter.hasNext(); ){
	    uCharKey = (CollationKey)iter.next();
	    uChar = uCharKey.getSourceString();
	    if(!Character.isWhitespace(uChar.charAt(0))){
		if(i<blockSize){
		    sb.append(uChar);
		    i++;
		}else{
		    blocksInLine++;
		    if((blocksInLine+1)*blockSize+blocksInLine < maxCharsLine){
			sb.append(" ").append(uChar);
		    }else{
			blocksInLine = 0;
			sb.append(" ").append(newline).append(uChar);
		    }
		    i=1;
		}
	    }
	}
	if(ciphertextAL.size()>0){
	    sb.append(newline);
	}
	ciphertext = sb.toString();
	ciphertextAL.clear();
	initCiphertextAL();
	fireStateChanged();
    }

    /**
     * Sets the <code>CiphertextManager</code>'s ciphertext to the
     * one passed.
     *
     * @param ciphertext the new ciphertext
     * @throws NullPointerException if the argument is <code>null</code>
     */
    public void setCiphertext(String ciphertext) throws NullPointerException
    {
	if(ciphertext == null){
	    throw new NullPointerException();
	}
	this.ciphertext = ciphertext;
	ciphertextAL.clear();
	initCiphertextAL();
	fireStateChanged();
    }

    /**
     * Method that returns a <code>String</code> where the characters of the
     * ciphertext have been replaced according to <code>subst</code>.
     *
     * @param subst an <code>ArrayList</code> of <code>HashMap</code>s. That
     *              contains a <code>HashMap</code> for every alphabet the
     *              substitution has.
     * @throws NullPointerException if <code>subst</code> is <code>null</code>
     * @throws IllegalArgumentException if <code>subst</code>'s size is <code>0</code>
     * @see Substitution#getSubstitution()
     */
    public String getPlaintext(ArrayList subst) throws NullPointerException,
						       IllegalArgumentException
    {
	if(subst == null){
	    throw new NullPointerException();
	}else if(subst.size() == 0){
	    throw new IllegalArgumentException();
	}
	int numAlpha = subst.size();
	Iterator iter = ciphertextAL.iterator();
	StringBuffer sb = new StringBuffer();
	HashMap substMono = null;
	CollationKey ciphChar = null;
	CollationKey plnChar = null;
	int i=0; // the alphabet the current character is in
	while(iter.hasNext()){
	    substMono = (HashMap)subst.get(i);
	    ciphChar = (CollationKey)iter.next();
	    plnChar = (CollationKey)substMono.get(ciphChar);
	    if(plnChar == null){
		if(substMono.containsKey(ciphChar)){
		    sb.append(' ');
		    i++;
		}else{
		    sb.append(ciphChar.getSourceString());
		}
	    }else{
		sb.append(plnChar.getSourceString());
		i++;
	    }
	    i = i%numAlpha;
	}
	return sb.toString();
    }

    /**
     * Performs the Kasiski Test on the ciphertext an returns a 
     * <code>List</code> of <code>KasiskiEntries</code> with the data.
     *
     * @param ignoreSet a <code>Set</code> that contains the
     *                  <code>CollationKey</code>s of the characters to ignore
     *                  (do as if they did not appear in the ciphertext) while
     *                  applying the Kasiski Test.
     */
    public ArrayList getKasiski(Set ignoreSet) throws NullPointerException
    {
	if(ignoreSet == null){
	    throw new NullPointerException();
	}
	/* maps the character sequences to an array of Objects of length 2 that
	   contans their length in user characters (according to charIterator)
	   and positions in the text */
	HashMap seqPosHM = new HashMap();
	StringBuffer sb = new StringBuffer();
	ArrayList ctxtMinusIgnAL= (ArrayList)ciphertextAL.clone();
	CollationKey uCharKey;
	String uChar;
	boolean isCtrl;    // true if the character isISOControl
	boolean isWhite;   // true if the character isWhitespace
	//remove the characters to be ignored from ctxtMinusIgnAl
	for(Iterator iter=ctxtMinusIgnAL.iterator(); iter.hasNext(); ){
	    uCharKey = (CollationKey)iter.next();
	    uChar = uCharKey.getSourceString();
	    isCtrl = Character.isISOControl(uChar.charAt(0));
	    isWhite = Character.isWhitespace(uChar.charAt(0));
	    if(ignoreSet.contains(uCharKey) || 
	       (isCtrl && ignoreControl) || (isWhite && ignoreWhite)){
		iter.remove();
	    }
	}
	// put the characters in ctxtMinusIgnAl in an array
	CollationKey[] ciphertextArr = {};
	ciphertextArr = (CollationKey[])ctxtMinusIgnAL.toArray(ciphertextArr);
	/* Find the repeated sequences by "moving" the array to the left (i)
	   and comparing the shifted characters to those that were not (j).
	   Then add the substrings of the long sequences, since they are 
	   repeated sequences too. */
	for(int i=1; i<ciphertextArr.length; i++){
	    for(int j=0; i+j<ciphertextArr.length; j++){
		int k;
		for(k=j; i+k<ciphertextArr.length &&
			 ciphertextArr[i+k].equals(ciphertextArr[k]); k++);
		if(k>j+1){
		    int seqLen = k-j;
		    //get substrings and  positions
		    for(int m=2; m<=seqLen; m++){
			for(int n=j; n+m<=k; n++){
			    String str = substring(ciphertextArr, n, n+m, sb);
			    sb.delete(0, sb.length());
			    CollationKey seqCK = collator.getCollationKey(str);
			    //stores the positions the sequence appears in
			    HashSet hs;
			    /* since the length of the string in user 
			       characters depends on the locale and that number
			       is needed later on, we store it at
			       strLen_hs[0] and put hs in strLen_hs[1]. */
			    Object[] strLen_hs;
			    if(seqPosHM.containsKey(seqCK)){
				hs=(HashSet)((Object[])seqPosHM.get(seqCK))[1];
				hs.add(new Integer(n));
				hs.add(new Integer(i+n));
			    }else{
				hs = new HashSet();
				hs.add(new Integer(n));
				hs.add(new Integer(i+n));
				strLen_hs = new Object[2];
				strLen_hs[0] = new Integer(m);
				strLen_hs[1]= hs;
				seqPosHM.put(seqCK, strLen_hs);
			    }
			}
		    }
		}
		if(k>j+1){
		    j=k-1;
		}
	    }
	}
	ArrayList retLst = new ArrayList();
	for(Iterator iter=seqPosHM.keySet().iterator(); iter.hasNext(); ){
	    CollationKey seq = (CollationKey)iter.next();
	    Object[] strLen_hs = (Object[])seqPosHM.get(seq);
	    retLst.add(new KasiskiEntry(seq,
					((Integer)strLen_hs[0]).intValue(),
					(HashSet)strLen_hs[1]));
	}
	return retLst;
    }

    /**
     * Receives an array of <code>CollationKey</code>s, and stores the sequence
     * of <code>String</code> represented by those between the indices
     * <code>start</code> (inclusive) and <code>end</code> (exclusive)
     * in the <code>StringBuffer</code>. If <code>sb</code> is not empty, the
     * the substring is appended.
     *
     * @throws NullPointerException if <code>str</code> or <code>sb</code> are <code>null</code>
     * @throws IndexOutOfBoundsException if <code>start</code> or <code>end</code> are out of bounds.
     */
    private static String substring(CollationKey[] str, 
				    int start,
				    int end,
				    StringBuffer sb)
	throws IndexOutOfBoundsException, NullPointerException
    {
	for(int i=start; i<end; i++){
	    sb.append(str[i].getSourceString());
	}
	return sb.toString();
    }

    /**
     * Sets the value of the member variable <code>ignoreWhite</code> to 
     * the one passed as argument.
     *
     * @param iw the new value of <code>ignoreWhite</code>
     * @see #ignoreWhite
     */
    public void setIgnoreWite(boolean iw){
	this.ignoreWhite = iw;
    }

     /**
     * Sets the value of the member variable <code>ignoreControl</code> to 
     * the one passed as argument.
     *
     * @param ic the new value of <code>ignoreControl</code>
     * @see #ignoreControl
     */
    public void setIgnoreControl(boolean ic){
	this.ignoreControl = ic;
    }

    /**
     * Returns the instance of <code>Locale</code> being used by this
     * <code>CiphertextManager</code>
     *
     * @return the instance of <code>Locale</code> being used by this
     *         <code>CiphertextManager</code> 
     */
    public Locale getLocale(){
	return locale;
    }

    /**
     * Sets the <code>Locale</code> that the <code>CiphertextManager</code>
     * should use.<br/>
     * If the <code>Locale</code> passed is not equal to the one being used
     * then the <code>BreakIterator</code> being used 
     * (<code>charIterator</code>) is replaced with an instance that uses
     * the new <code>Locale</code> and <code>ciphertextAL</code> is
     * recalculated.<br/>
     * Note that the <code>Collator</code> being used is not changed.
     *
     * @param loc the <code>Locale</code> the <code>CiphertextManager</code>
     *            should use.
     * @throws NullPointerException if <code>loc</code> is <code>null</code>
     * @see #setCollator(Collator)
     */
    public void setLocale(Locale loc) throws NullPointerException
    {
	if(loc == null){
	    throw new NullPointerException();
	}
	if(locale.equals(loc)){
	    return;
	}
	locale = loc;
	charIterator = BreakIterator.getCharacterInstance(locale);
	ciphertextAL.clear();
	initCiphertextAL();
    }

    /**
     * Returns the instance of <code>Collator</code> being used by this
     * <code>CiphertextManager</code>
     *
     * @return the instance of <code>Collator</code> being used by this
     *         <code>CiphertextManager</code> 
     */
    public Collator getCollator(){
	return collator;
    }

    /**
     * Sets the <code>Collator</code> that the <code>CiphertextManager</code>
     * should use and generates new <code>CollationKey</code>s for
     * <code>ciphertextAL</code>.<br/>
     * Note that the <code>Locale</code> being used is not changed.
     *
     * @param col the <code>Collator</code> the <code>CiphertextManager</code>
     *            should use.
     * @throws NullPointerException if <code>col</code> is <code>null</code>
     * @see #setLocale(Locale)
     */
    public void setCollator(Collator col) throws NullPointerException
    {
	if(col == null){
	    throw new NullPointerException();
	}
	collator = col;
	for(int i=0; i<ciphertextAL.size(); i++){
	    ciphertextAL.set(i,
			     collator.getCollationKey(((CollationKey)ciphertextAL.get(i)).getSourceString()));
	}
    }

    /**
     * Adds a <code>ChangeListener</code> to the <code>Substitution</code>.
     * <br/>
     * The <code>ChangeListener</code> will receive a 
     * <code>ChangeEvent</code> when the <code>ciphertext</code> changes.
     *
     * @param l the <code>ChangeListener</code> that sould be notified
     */
    public void addChangeListener(ChangeListener l){
	listenerList.add(ChangeListener.class, l);
    }

    /**
     * Notifies all listeners that have registered interest for notification
     * on this event type.
     */
    protected void fireStateChanged(){
	// Guaranteed to return a non-null array
	Object[] listeners = listenerList.getListenerList();
	ChangeEvent changeEvent = new ChangeEvent(this);
	// Process the listeners last to first, notifying
	// those that are interested in this event
	for (int i = listeners.length-2; i>=0; i-=2) {
	    if (listeners[i]==ChangeListener.class) {
		((ChangeListener)listeners[i+1]).stateChanged(changeEvent);
	    }
	}
    }

    /**
     * Small program used to test the class
     */
    public static void main(String[] args){
	if(args.length!=2){
	    System.out.println("Usage:\n\n\tjava Text.CiphertextManager "+
			       "<text-file> <encoding>\n");
	    System.exit(1);
	}
	BufferedReader in = null;
	try{
	    FileInputStream fis = new FileInputStream(args[0]);
	    InputStreamReader isr = new InputStreamReader(fis, args[1]);
	    in = new BufferedReader(isr);
	}catch(FileNotFoundException fnfe){
	    System.err.println("File not found "+fnfe.getMessage());
	    System.exit(2);
	}catch(UnsupportedEncodingException uee){
	    System.err.println("Unsupported Encoding "+uee.getMessage());
	    System.exit(3);
	}
	StringBuffer sb = new StringBuffer();
	try{
	    int read = in.read();
	    while(read>=0){
		sb.append((char)read);
		read = in.read();
	    }
	}catch(IOException ioe){
	    System.err.println("I/O Exception " + ioe.getMessage());
	    System.exit(1);
	}
	Locale loc = Locale.getDefault();
	Collator collator = Collator.getInstance(loc);
	final CiphertextManager cipherMan = new CiphertextManager(loc,
								  collator,
								  sb.toString());
	// ignore set test
	ArrayList emptyIgn = new ArrayList();
	final int numAlpha = 1; //number of alphabets of the cipher
	for(int i=0; i<numAlpha; i++){
	    emptyIgn.add(new HashSet());
	}
	java.util.List freqs = cipherMan.getFrequencies(numAlpha, emptyIgn );

	//GUI
	JFrame frame = new JFrame("Substitution");
	final JFrame statsFrame = new JFrame("Stats");
	final StatsPanel statsPan = new StatsPanel(freqs, 
						   numAlpha == 1 ? Substitution.MONOALPHABETIC : Substitution.POLYALPHABETIC);
	statsPan.setPreferredSize(new Dimension(338, 422));
	statsFrame.getContentPane().add(statsPan);
	statsFrame.pack();
	statsFrame.setVisible(true);
	ArrayList alphaLst = cipherMan.getCipherAlphabet();
	final Substitution subst = new Substitution(loc,
						    collator,
						    alphaLst,
						    (ArrayList)alphaLst.clone());
	subst.setNumberOfAlphabets(numAlpha);
	ChangeListener change = new ChangeListener(){
		public void stateChanged(ChangeEvent e){
		    SubstitutionEvent se = (SubstitutionEvent)e;
		    if(se.getChangeType()==SubstitutionEvent.IGNORED_CHARACTERS){
			System.out.println("State changed");
			java.util.List stats = cipherMan.getFrequencies(numAlpha, subst.getIgnoredCharacters());
			int i=0;
			Iterator iter = stats.iterator();
			while(iter.hasNext()){
			    statsPan.setTableData((ArrayList)iter.next(), i);
			    i++;
			}
		    }else{
			System.out.println("I don't care about this change");
		    }
		}
	    };
	subst.addChangeListener(change);
	subst.setPreferredSize(new Dimension(400,
					     subst.getPreferredSize().height));
	frame.getContentPane().add(subst);
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	frame.pack();
	frame.setVisible(true);
    }
}
/*
 * -- CiphertextManager.java ends here --
 */
