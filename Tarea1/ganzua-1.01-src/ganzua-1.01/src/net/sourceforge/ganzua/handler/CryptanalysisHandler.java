/*
 * -- CryptanalysisHandler.java --
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

package net.sourceforge.ganzua.handler;

import java.util.Locale;
import java.text.Collator;
import java.text.BreakIterator;
import java.util.Collections;
import java.util.Collection;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;
import net.sourceforge.ganzua.text.*;
import net.sourceforge.ganzua.component.CipherToolsPane;

/**
 * SAX2 event handler that stores the data of an instance of the 
 * XML document class defined in the schema 
 * <code>Cryptanalysis.xsd</code>.
 *
 * @author Jesús Adolfo García Pasquel
 * @version 0.01 Apr 2004
 */
public class CryptanalysisHandler extends DefaultHandler
{
    /**
     * The namespace the instances of <code>Cryptanalysis.xsd</code>
     * belong to. */
    public static final String NAMESPACE = "http://ganzua.sourceforge.net/cryptanalysis";

    /**
     * Used to identify the parent tag as 
     * <code>&lt;cipherAlphabet /&gt;</code> */
    private static final int CIPHER_ALPHABET = 1;

    /**
     * Used to identify the parent tag as 
     * <code>&lt;plainAlphabet /&gt;</code> */
    private static final int PLAIN_ALPHABET = 2;

    /**
     * Used to identify the parent tag as 
     * <code>&lt;substitution /&gt;</code> */
    private static final int SUBSTITUTION = 3;

    /**
     * Used to identify the parent as
     * <code>&lt;languageFrequencies /&gt;</code> */
    private static final int LANGUAGE_FREQUENCIES = 4;

    /**
     * Used to identify the parent tag as 
     * <code>&lt;ciphertext /&gt;</code> */
    private static final int CIPHERTEXT = 5;

    /**
     * Used by the SAX <code>DocumentHandler</code> methods
     * to identify the parent tag. */
    private int parentTag = 0;

    /**
     * Stores the cipher believed to have been used to generate the
     * ciphertext. */
    private String cipher;

    /**
     * Stores the contents of <code>&lt;cryptanalysis /&gt;</code>'
     * ciphertext tag.*/
    private StringBuffer ciphertext;

    /**
     * <code>Set</code> of <code>CollationKey</code>s used to store the
     * values of the <code>&lt;character /&gt;</code> elements inside the
     * <code>&lt;cipherAlphabet /&gt;</code> element. */
    private HashSet cipherAlphabet;

    /**
     * <code>Set</code> of <code>CollationKey</code>s used to store the
     * values of the <code>&lt;character /&gt;</code> elements inside the
     * <code>&lt;plainAlphabet /&gt;</code> element. */
    private HashSet plainAlphabet;

    /**
     * <code>Set</code> of <code>CollationKey</code>s used to store the
     * values of the <code>&lt;ignore /&gt;</code> element inside the
     * <code>&lt;substitution /&gt;</code> element. The characters from the
     * ciphertext to be ignored. */
    private HashSet ignored;

    /**
     * <code>List</code> of <code>HashMap</code>s (one for each alphabet)
     * that contains the data in the tag <code>&lt;substitution /&gt;</code>.*/
    private ArrayList substitution;

    /**
     * <code>boolean</code> that indicates if the substitution is injective
     * or not. */
    private boolean substInjective;

    /**
     * Used to store the data in the element 
     * <code>&lt;languageFrequencies /&gt;</code>. */
    private LanguageFrequenciesHandler langFreqsH;

    /**
     * Used to store the value of <code>&lt;languageFrequencies /&gt;</code>'
     * <code>source</code> attribute. */
    private String langSourceDocument;

    /**
     * Used to store the value of <code>&lt;languageFrequencies /&gt;</code>'
     * <code>rules</code> attribute.  */
    private String langRules;

    /**
     * Used to indicate if the instance of <code>Cryptanalysis.xsd</code>
     * contains <code>&lt;languageFrequencies /&gt;</code> or not. */
    private boolean containsLangFreqs = false;

    /**
     * Stores the language/country.  */
    private Locale locale;

    /**
     * Used to create the <code>CollationKey</code>s needed to compare
     * <code>String</code>s efficiently. */
    private Collator collator;

    /**
     * Used to verify that the user characters are indeed user characters. */
    private BreakIterator charIterator;

    /**
     * <code>Set</code> of <code>StringFreq</code>s used to store the
     * values of the <code>&lt;character /&gt;</code> elements inside the 
     * <code>&lt;alphabet /&gt;</code> element of 
     * <code>&lt;languageFrequencies /&gt;</code>. */
    private HashSet langAlphabet;

    /**
     * <code>Set</code> of <code>StringFreq</code>s used to store the
     * values of the <code>&lt;bigram /&gt;</code> elements inside the
     * <code>&lt;bigrams /&gt;</code> element of
     * <code>&lt;languageFrequencies /&gt;</code>. */
    private HashSet langBigrams;

    /**
     * <code>Set</code> of <code>StringFreq</code>s used to store the
     * values of <code>&lt;trigram /&gt;</code> elements inside the
     * <code>&lt;trigrams /&gt;</code> element of
     * <code>&lt;languageFrequencies /&gt;</code>. */
    private HashSet langTrigrams;

    /**
     * <code>double</code> used to store the coincidence index of texts
     * written in the language. The coincidence index is calculated using
     * the frequencies of the <code>&lt;character /&gt;</code> elements inside
     * the element <code>&lt;alphabet /&gt;</code> of 
     * <code>&lt;languageFrequencies /&gt;</code>. */
    private double coincidenceIndex;

    public CryptanalysisHandler(){
	super();
	cipher = "";
	ciphertext = new StringBuffer();
	cipherAlphabet = new HashSet();
	plainAlphabet = new HashSet();
	ignored = new HashSet();
	substitution = new ArrayList();
	langFreqsH = null;
	locale = Locale.getDefault();
	collator = Collator.getInstance(locale);
	langAlphabet = new HashSet();
	langBigrams = new HashSet();
	langTrigrams = new HashSet();
    }

    /**
     * Returns the value of <code>&lt;cryptanalysis /&gt;</code>' cipher
     * attribute.
     *
     * @return <code>CipherToolsPane.CAESAR</code>,
     *         <code>CipherToolsPane.MONOALPHABETIC</code>,
     *         <code>CipherToolsPane.VIGENERE</code> or
     *         <code>CipherToolsPane.ALBERTI</code>
     * @see CipherToolsPane#CAESAR
     * @see CipherToolsPane#MONOALPHABETIC
     * @see CipherToolsPane#VIGENERE
     * @see CipherToolsPane#ALBERTI
     */
    public String getCipher(){
	return cipher;
    }

    /**
     * Returns the <code>&lt;cryptanalysis /&gt;</code>' ciphertext.
     *
     * @return the ciphertext
     */
    public String getCiphertext(){
	return ciphertext.toString();
    }

    /**
     * Returns a <code>Set</code> with the characters of the cipher alphabet
     * in the form of <code>CollationKey</code>s.
     *
     * @return a <code>Set</code> of <code>CollationKey</code>s
     */
    public HashSet getCipherAlphabet(){
	return cipherAlphabet;
    }

    /**
     * Returns a <code>Set</code> with the characters of the plain alphabet
     * in the form of <code>CollationKey</code>s.
     *
     * @return a <code>Set</code> of <code>CollationKey</code>s
     */
    public HashSet getPlainAlphabet(){
	return plainAlphabet;
    }

    /**
     * Returns a <code>Set</code> with the characters from the ciphertext
     * to be ignored in the form of <code>CollationKey</code>s.
     *
     * @return a <code>Set</code> of <code>CollationKey</code>s
     */
    public HashSet getIgnored(){
	return ignored;
    }

    /**
     * Returns a <code>List</code> of <code>HashMap</code>s that contain the
     * character-replacement pairs (<code>CollationKey</code>s) for each
     * alphabet.<br/>
     *
     * @return <code>List</code> of <code>HashMap</code>s that contain the
     *         character-replacement paris (<code>CollationKey</code>s) for
     *         each alphabet
     */
    public ArrayList getSubstitution(){
	return substitution;
    }

    /**
     * Returns <code>true</code> if the substitutions of all the alphabets
     * are injective and <code>false</code> if there is at least one
     * alphabet in which more than one character has the same replacement
     * character.
     */
    public boolean getSubstInjective(){
	return substInjective;
    }

    /**
     * Returns a <code>LanguageFrequenciesHandler</code> with all of the
     * data found in the element <code>&lt;languageFrequencies /&gt;</code>.
     */
    public LanguageFrequenciesHandler getLanguageFrequencies(){
	return langFreqsH;
    }

    /**
     * Returns a <code>Set</code> of <code>StringFreq</code>s with the
     * values of the <code>&lt;character /&gt;</code> elements inside the
     * <code>&lt;alphabet /&gt;</code> element of 
     * <code>&lt;languageFrequencies /&gt;</code>.
     *
     * @return <code>langAlphabet</code>
     */
    public HashSet getLangAlphabet(){
	return langAlphabet;
    }

    /**
     * Returns an <code>ArrayList</code> of <code>CollationKey</code>s with the
     * value of the <code>char</code> attribute of the 
     * <code>&lt;character /&gt;</code> elements inside the
     * <code>&lt;alphabet /&gt;</code> element of 
     * <code>&lt;languageFrequencies /&gt;</code>.
     *
     * @return an <code>ArrayList</code> of user characters in the form of
     *         <code>CollationKey</code>s
     */
    public ArrayList getLangAlphabetCK(){
	ArrayList retAL = new ArrayList(langAlphabet);
	for(int i=0; i<retAL.size(); i++){
	    retAL.set(i,
		      ((StringFreq)retAL.get(i)).getStringCK());
	}
	Collections.sort(retAL);
	return retAL;
    }

    /**
     * Returns a <code>Set</code> of <code>StringFreq</code>s with the
     * values of the <code>&lt;bigram /&gt;</code> elements inside the
     * <code>&lt;bigram /&gt;</code> element of 
     * <code>&lt;languageFrequencies /&gt;</code>.
     *
     * @return <code>langBigrams</code>
     */
    public HashSet getLangBigrams(){
	return langBigrams;
    }

    /**
     * Returns a <code>Set</code> of <code>StringFreq</code>s with the
     * values of the <code>&lt;trigram /&gt;</code> elements inside the 
     * <code>&lt;trigrams /&gt;</code> element of 
     * <code>&lt;languageFrequencies /&gt;</code>.
     *
     * @return <code>langTrigrams</code>
     */
    public HashSet getLangTrigrams(){
	return langTrigrams;
    }

    /**
     * Returns the coincidence index calculated using the frequencies of the
     * <code>&lt;character /&gt;</code> elements inside the element 
     * <code>&lt;alphabet /&gt;</code> of 
     * <code>&lt;languageFrequencies /&gt;</code>. If there are no
     * <code>&lt;languageFrequencies /&gt;</code> in the instance,
     * <code>-1</code> is returned.
     *
     * @return <code>coincidenceIndex</code>
     */
    public double getCoincidenceIndex(){
	return coincidenceIndex;
    }

    /**
     * Returns the value of <code>&lt;languageFrequencies /&gt;</code>'
     * <code>source</code> attribute.
     */
    public String getLangSource(){
	return langSourceDocument;
    }

    /**
     * Returns the value of <code>&lt;languageFrequencies /&gt;</code>'
     * <code>rules</code> attribute.
     */
    public String getLangRules(){
	return langRules;
    }

    /**
     * Returns a <code>Locale</code> created using the values of the 
     * attributes <code>language</code> and <code>country</code> of
     * of the elements <code>&lt;cryptanalysis /&gt;</code> and
     * <code>&lt;languageFrequencies /&gt;</code> (they must be the
     * same in the case <code>&lt;languageFrequencies /&gt;</code>
     * appears in the document).
     */
    public Locale getLocale(){
	return locale;
    }

    /**
     * Returns the <code>Collator</code> created using <code>locale</code>
     */
    public Collator getCollator(){
	return collator;
    }

    /**
     * Method that returns the number of user characters in the
     * string <code>str</code> according to <code>charIterator</code>
     *
     * @param str a <code>String</code>
     * @return the number of user characters in <code>str</code> according to
     *         <code>charIterator</code>
     */
    private final int numChars(String str){
	charIterator.setText(str);
	int boundary = charIterator.first();
	int numChars = -1;
	while(boundary != BreakIterator.DONE){
	    boundary = charIterator.next();
	    numChars++;
	}
	return numChars;
    }

    /* ===========================================================
                       SAX DocumentHandler methods
       =========================================================== */

    /**
     * Receives a Locator object for document events and does nothing.
     *
     * @param l a <code>Locator</code> for all SAX document events.
     */
    public void setDocumentLocator(Locator l){
    }

    /**
     * Receives notification of the beginning of the document and does
     * nothing.
     *
     * @throws SAXException Any SAX exception, possibly wrapping another exception.
     */
    public void startDocument() throws SAXException
    {
    }

    /**
     * Receives notification of the end of the document. At this point
     * <code>langFreqsH</code>, and <code>substInjective</code> are
     * initialized and it is checked  that the number of alphabets in the 
     * substitution checks with the kind of cipher (Caesar, Monoalphabetic,
     * Vigenère or Alberti).
     *
     * @throws SAXException Any SAX exception, possibly wrapping another exception.
     */
    public void endDocument() throws SAXException
    {
	// initialize langFreqsH
	if(containsLangFreqs){
	    langFreqsH = new LanguageFrequenciesHandler(this);
	}else{
	    langFreqsH = null;
	    coincidenceIndex = -1;
	}
	// initialize substInjective
	substInjective = true;
	Collection values;
	HashSet valuesSet;
	for(Iterator iter=substitution.iterator();
	    substInjective && iter.hasNext(); ){
	    values = ((HashMap)iter.next()).values();
	    valuesSet = new HashSet(values);
	    if(values.size() != valuesSet.size()){
		substInjective = false;
	    }
	}
	/* check that the number of alphabets in the file checks with the 
	   kind of cipher it claims to be for */
	if((cipher.equals(CipherToolsPane.CAESAR) ||
	    cipher.equals(CipherToolsPane.MONOALPHABETIC)) && 
	   substitution.size() > 1){
	    throw new SAXException("Too many alphabets for a monoalphabetic "+
				   "cipher");
	}else if((cipher.equals(CipherToolsPane.VIGENERE) ||
		  cipher.equals(CipherToolsPane.ALBERTI)) && 
		 substitution.size() < 2){
	    throw new SAXException("Too few alphabets for a polyalphabetic "+
				   "cipher");
	}
    }


    /**
     * Receive notification of the start of an element and it's attributes.
     * Since most of the relevant information in the instances of 
     * <code>&lt;languageFrequencies /&gt;</code> is in the attributes of it's
     * elements, almost all of the data is retrieved here and stored in the
     * appropriate <code>Collection</code>.
     *
     * @param sName simple (local) name
     * @param qName qualified name
     * @param attrs The specified or defaulted attributes.
     */
    public void startElement(String namespaceURI,
                             String sName, /* simple name */
                             String qName, /* qualified name */
                             Attributes attrs)
	throws SAXException
    {
	if(!namespaceURI.equals(NAMESPACE)){
	   SAXException sxe = new SAXException("The document's namespace is not that of <cryptanalysis />");
	   throw sxe;
	}
	String userChar;
	int freq;
	if(sName.equals("character")){
	    boolean added = false;
	    userChar = attrs.getValue("", "char");
	    freq = 0;
	    /* Check that the character is a user character (e.g. a, ñ, ü)
	       and not a string of user characters (e.g. ch, ll) */
	    if(numChars(userChar)!=1){
		SAXException sxe = new SAXException("\""+userChar+"\" is not a valid character");
		throw sxe;
	    }
	    switch(parentTag){
	    case CIPHER_ALPHABET:
		added = cipherAlphabet.add(collator.getCollationKey(userChar));
		break;
	    case PLAIN_ALPHABET:
		added = plainAlphabet.add(collator.getCollationKey(userChar));
		break;
	    case SUBSTITUTION:
		added = ignored.add(collator.getCollationKey(userChar));
		break;
	    case LANGUAGE_FREQUENCIES:
		try{
		    freq = Integer.parseInt(attrs.getValue("", "frequency"));
		}catch(NumberFormatException nfe){
		    SAXException sxe = new SAXException("\""+attrs.getValue("", "frequency")+"\" is not a valid frequency");
		    throw sxe;
		}
		added=langAlphabet.add(new StringFreq(collator.getCollationKey(userChar), freq));
		break;
	    }
	    if(!added){
		SAXException sxe = new SAXException("Duplicated entry: <character char=\"" + userChar+"\"");
		throw sxe;
	    }
	}else if(sName.equals("bigram")){
	    userChar = attrs.getValue("", "sequence");
	    freq = 0;
	    try{
		freq = Integer.parseInt(attrs.getValue("", "frequency"));
	    }catch(NumberFormatException nfe){
		SAXException sxe = new SAXException("\""+attrs.getValue("", "frequency")+"\" is not a valid frequency");
		throw sxe;
	    }
	    // Check that the string is a bigram
	    if(numChars(userChar)!=2){
		SAXException sxe = new SAXException("\""+userChar+"\" is not a valid bigram");
		throw sxe;
	    }
	    boolean added = false;
	    added=langBigrams.add(new StringFreq(collator.getCollationKey(userChar), freq));
	    if(!added){
		SAXException sxe = new SAXException("Duplicated entry: <bigram sequence=\"" + userChar+"\" frequency=\"...\" \\>");
		throw sxe;
	    }
	}else if(sName.equals("trigram")){
	    userChar = attrs.getValue("", "sequence");
	    freq = 0;
	    try{
		freq = Integer.parseInt(attrs.getValue("", "frequency"));
	    }catch(NumberFormatException nfe){
		SAXException sxe = new SAXException("\""+attrs.getValue("", "frequency")+"\" is not a valid frequency");
		throw sxe;
	    }
	    // Check that the string is a trigram
	    if(numChars(userChar)!=3){
		SAXException sxe = new SAXException("\""+userChar+"\" is not a valid trigram");
		throw sxe;
	    }
	    boolean added = false;
	    added=langTrigrams.add(new StringFreq(collator.getCollationKey(userChar), freq));
	    if(!added){
		SAXException sxe = new SAXException("Duplicated entry: <trigram sequence=\"" + userChar+"\" frequency=\"...\" \\>");
		throw sxe;
	    }
	}else if(sName.equals("occurrences")){
	    assert substitution.size()>0 : "Replacement for characters of a non-existen alphabet !";
	    HashMap replace = (HashMap)substitution.get(substitution.size()-1);
	    String [] replacement = new String [2];
	    replacement[0] = attrs.getValue("", "ofChar");
	    replacement[1] = attrs.getValue("", "byChar");
	    if(numChars(replacement[0]) != 1 || 
	       numChars(replacement[1]) != 1 ){
		SAXException sxe = new SAXException("error in attribute of <occurrences/>: \""+ (numChars(replacement[0])!=1 ? replacement[1] : replacement[0]) +"\" is not a valid character");
		throw sxe;
	    }
	    if(replace.put(collator.getCollationKey(replacement[0]), 
			   collator.getCollationKey(replacement[1]))!=null){
		SAXException sxe = new SAXException("\""+replacement[0]+"\" already has a replacement");
		throw sxe;
	    }
	}else if(sName.equals("alphabet")){
	    if(parentTag == SUBSTITUTION){
		substitution.add(new HashMap());
	    }
	}else if(sName.equals("cryptanalysis")){
	    cipher = attrs.getValue("", "cipher");
	    if(cipher.equals(CipherToolsPane.CAESAR)){
		cipher = CipherToolsPane.CAESAR;
	    }else if(cipher.equals(CipherToolsPane.MONOALPHABETIC)){
		cipher = CipherToolsPane.MONOALPHABETIC;
	    }else if(cipher.equals(CipherToolsPane.VIGENERE)){
		cipher = CipherToolsPane.VIGENERE;
	    }else if(cipher.equals(CipherToolsPane.ALBERTI)){
		cipher = CipherToolsPane.ALBERTI;
	    }else{
		assert false: "invalid value for cipher: " + cipher;
	    }
	    String country = attrs.getValue("", "country");
	    if(country==null || country==""){
		locale = new Locale(attrs.getValue("", "language"));
	    } else{
		locale = new Locale(attrs.getValue("", "language"),
				    country);
	    }
	    collator = Collator.getInstance(locale);
	    charIterator = BreakIterator.getCharacterInstance(locale);
	}else if(sName.equals("languageFrequencies")){
	    containsLangFreqs = true;
	    parentTag = LANGUAGE_FREQUENCIES;
	    /* The language and country attributes must equal those in
	       the tag <cryptogram/> */
	    Locale langLocale = null;
	    String country = attrs.getValue("", "country");
	    if(country==null || country==""){
		langLocale = new Locale(attrs.getValue("", "language"));
	    } else{
		langLocale = new Locale(attrs.getValue("", "language"),
					country);
	    }
	    if(!locale.equals(langLocale)){
		throw new SAXException("The language and country attributes of the tags cryptogram and languageFrequencies must be equal");
	    }
	    langSourceDocument = attrs.getValue("", "source");
	    langRules = attrs.getValue("", "rules");
	}else if(sName.equals("cipherAlphabet")){
	    parentTag = CIPHER_ALPHABET;
	}else if(sName.equals("plainAlphabet")){
	    parentTag = PLAIN_ALPHABET;
	}else if(sName.equals("substitution")){
	    parentTag = SUBSTITUTION;
	}else if(sName.equals("ciphertext")){
	    parentTag = CIPHERTEXT;
	}
    }

    /**
     * Receives notification of the end of an element. If the element is
     * the child element of <code>&lt;languageFrequencies /&gt;</code>,
     * <code>&lt;alphabet /&gt;</code>, then the coincidence index is
     * calculated and set as the value of <code>coincidenceIndex</code>.
     *
     * @param sName simple (local) name
     * @param qName qualified name
     *
     * @throws SAXException Any SAX exception, possibly wrapping another exception.
     */
    public void endElement(String namespaceURI,
                           String sName, /* simple name */
                           String qName  /* qualified name*/ )
	throws SAXException
    {
	if(sName.equals("ciphertext")){
	    parentTag = 0;
	}else if(parentTag==LANGUAGE_FREQUENCIES && sName.equals("alphabet")){
	    coincidenceIndex = 0;
	    int cf; //used to store temporarily a character's frequency
	    int totalChars = 0;
	    for(Iterator iter = langAlphabet.iterator(); iter.hasNext(); ){
		totalChars += ((StringFreq)iter.next()).getFrequency();
	    }
	    double N2 = (double)totalChars * (double)(totalChars - 1);
	    for(Iterator iter = langAlphabet.iterator(); iter.hasNext(); ){
		cf = ((StringFreq)iter.next()).getFrequency();
		coincidenceIndex += ((double)cf * (double)(cf - 1))/N2;
	    }
	}
    }

    /**
     * Receives notification of character data inside an element and does
     * nothing (unless the text is inside the <code>&lt;ciphertext /&gt;</code>
     * tag).
     *
     * @param buf the characters
     * @param offset the start position in the character array
     * @param len the number of characters to use from the character array
     *
     * @throws SAXException Any SAX exception, possibly wrapping another exception.
     */
    public void characters(char buf[], int offset, int len)
	throws SAXException
    {
	if(parentTag == CIPHERTEXT){
	    ciphertext.append(buf, offset, len);
	}
    }

    /**
     * Receive notification of ignorable whitespace in element content and
     * ignore them.
     *
     * @param buf the whitespace characters
     * @param offset the start position in the character array
     * @param len the number of characters to use from the character array
     *
     * @throws SAXException Any SAX exception, possibly wrapping another exception.
     */
    public void ignorableWhitespace(char buf[], int offset, int len)
    throws SAXException
    {
    }

    /**
     * Receives notification of a processing instruction and does nothing.
     *
     * @param target the processing instruction target.
     * @param data the processing instruction data, or null if none is supplied.
     * @throws SAXException  Any SAX exception, possibly wrapping another exception.
     */
    public void processingInstruction(String target, String data)
	throws SAXException
    {
    }

    /* ===========================================================
                         SAX ErrorHandler methods
       =========================================================== */

    /**
     * Receives notification of a recoverable parser errors (validation 
     * errors) and treats them as fatal.
     *
     * @param e the validation error information encoded as an exception
     * @throws SAXParseException any SAX exception, possibly wrapping another exception.
     */
    public void error(SAXParseException e)
	throws SAXParseException
    {
        throw e;
    }

    /**
     * Receives notification of a parser warnings and treats them as
     * fatal errors.
     *
     * @param err The warning information encoded as an exception.
     * @throws SAXException any SAX exception, possibly wrapping another exception.
     */
    public void warning(SAXParseException err)
	throws SAXException
    {
	SAXException sxe = new SAXException("line: "+ err.getLineNumber() +
					     "\n\tURI: "+ err.getSystemId() +
					     "\n\t" + err.getMessage());
	throw sxe;
    }
}
/*
 * -- CryptanalysisHandler.java ends here --
 */
