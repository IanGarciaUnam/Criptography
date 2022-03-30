/*
 * -- LanguageFrequenciesHandler.java --
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
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Iterator;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;
import net.sourceforge.ganzua.text.*;

/**
 * SAX2 event handler that stores the data of an instance of the 
 * XML document class defined in the schema 
 * <code>LanguageFrequencies.xsd</code>.
 *
 * @author Jesús Adolfo García Pasquel
 * @version 0.01 May 2003
 */
public class LanguageFrequenciesHandler extends DefaultHandler
{
    /**
     * The namespace the instances of <code>LanguageFrequencies.xsd</code>
     * belong to. */
    public static final String NAMESPACE = "http://ganzua.sourceforge.net/frequencies";

    /**
     * Used to store the value of <code>&lt;languageFrequencies /&gt;</code>'
     * <code>source</code> attribute.
     */
    private String sourceDocument;

    /**
     * Used to store the value of <code>&lt;languageFrequencies /&gt;</code>'
     * <code>rules</code> attribute.
     */
    private String rules;

    /**
     * Stores the language/country 
     */
    private Locale locale;

    /**
     * Used to create the <code>CollationKey</code>s needed to compare
     * <code>String</code>s efficiently.
     */
    private Collator collator;

    /**
     * Used to verify that the user characters are indeed user characters.
     */
    private BreakIterator charIterator;

    /**
     * <code>Set</code> of <code>StringFreq</code>s used to store the
     * values of the <code>&lt;character /&gt;</code> elements inside the 
     * <code>&lt;alphabet /&gt;</code> element.
     */
    private Set alphabet;

    /**
     * <code>Set</code> of <code>StringFreq</code>s used to store the
     * values of the <code>&lt;bigram /&gt;</code> elements inside the
     * <code>&lt;bigrams /&gt;</code> element.
     */
    private Set bigrams;

    /**
     * <code>Set</code> of <code>StringFreq</code>s used to store the
     * values of <code>&lt;trigram /&gt;</code> elements inside the
     * <code>&lt;trigrams /&gt;</code> element.
     */
    private Set trigrams;

    /**
     * <code>double</code> used to store the coincidence index of texts
     * written in the language. The coincidence index is calculated using
     * the frequencies of the <code>&lt;character /&gt;</code> elements inside
     * the element <code>&lt;alphabet /&gt;</code>.
     */
    private double coincidenceIndex;

    public LanguageFrequenciesHandler(){
	super();
	locale = Locale.getDefault();
	collator = Collator.getInstance(locale);
	alphabet = new HashSet();
	bigrams = new HashSet();
	trigrams = new HashSet();
    }

    /**
     * Creates a new <code>LanguageFrequenciesHandler</code> and sets its
     * data to that of <code>crypHan</code>.
     *
     * @param crypHan a <code>CryptanalysisHandler</code> that has been used to
     *                parse an instance of <code>Cryptanalysis.xsd</code>
     */
    public LanguageFrequenciesHandler(CryptanalysisHandler crypHan){
	super();
	sourceDocument = crypHan.getLangSource();
	rules = crypHan.getLangRules();
	locale = crypHan.getLocale();
	collator = crypHan.getCollator();
	charIterator = BreakIterator.getCharacterInstance(locale);
	alphabet = crypHan.getLangAlphabet();
	bigrams = crypHan.getLangBigrams();
	trigrams = crypHan.getLangTrigrams();
	coincidenceIndex = crypHan.getCoincidenceIndex();
    }

    /**
     * Returns a <code>Set</code> of <code>StringFreq</code>s with the
     * values of the <code>&lt;character /&gt;</code> elements inside the
     * <code>&lt;alphabet /&gt;</code> element.
     *
     * @return <code>alphabet</code>
     */
    public Set getAlphabet(){
	return alphabet;
    }

    /**
     * Returns an <code>ArrayList</code> of <code>CollationKey</code>s with the
     * value of the <code>char</code> attribute of the 
     * <code>&lt;character /&gt;</code> elements inside the
     * <code>&lt;alphabet /&gt;</code> element.
     *
     * @return an <code>ArrayList</code> of user characters in the form of
     *         <code>CollationKey</code>s
     */
    public ArrayList getAlphabetCK(){
	ArrayList retAL = new ArrayList(alphabet);
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
     * <code>&lt;bigram /&gt;</code> element.
     *
     * @return <code>bigrams</code>
     */
    public Set getBigrams(){
	return bigrams;
    }

    /**
     * Returns a <code>Set</code> of <code>StringFreq</code>s with the
     * values of the <code>&lt;trigram /&gt;</code> elements inside the 
     * <code>&lt;trigrams /&gt;</code> element.
     *
     * @return <code>trigrams</code>
     */
    public Set getTrigrams(){
	return trigrams;
    }

    /**
     * Returns the coincidence index calculated using the frequencies of the
     * <code>&lt;character /&gt;</code> elements inside the element 
     * <code>&lt;alphabet /&gt;</code>.
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
    public String getSource(){
	return sourceDocument;
    }

    /**
     * Returns the value of <code>&lt;languageFrequencies /&gt;</code>'
     * <code>rules</code> attribute.
     */
    public String getRules(){
	return rules;
    }

    /**
     * Returns a <code>Locale</code> created using the values of the 
     * attributes <code>language</code> and <code>country</code>.
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
     * Function that returns the number of user characters in the
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
     * Receives notification of the end of the document and does nothing.
     *
     * @throws SAXException Any SAX exception, possibly wrapping another exception.
     */
    public void endDocument() throws SAXException
    {
    }


    /**
     * Receive notification of the start of an element and it's attributes.
     * Since all the relevant information in the instances of 
     * <code>&lt;languageFrequencies /&gt;</code> is in the attributes of it's
     * elements, all the data is retrieved here and stored in the appropriate
     * <code>Collection</code>.
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
	   SAXException sxe = new SAXException("The document's namespace is not that of <languageFrequencies />");
	   throw sxe;
	}
	String userChar;
	int freq;
	if(sName.equals("character")){
	    userChar = attrs.getValue("", "char");
	    freq = 0;
	    try{
		freq = Integer.parseInt(attrs.getValue("", "frequency"));
	    }catch(NumberFormatException nfe){
		SAXException sxe = new SAXException("\""+attrs.getValue("", "frequency")+"\" is not a valid frequency");
		throw sxe;
	    }
	    /* Check that the character is a user character (e.g. a, ñ, ü)
               and not a string of user characters (e.g. ch, ll) */
	    if(numChars(userChar)!=1){
		SAXException sxe = new SAXException("\""+userChar+"\" is not a valid character");
		throw sxe;
	    }
	    boolean added = false;
	    added=alphabet.add(new StringFreq(collator.getCollationKey(userChar), freq));
	    if(!added){
		SAXException sxe = new SAXException("Duplicated entry: <character char=\"" + userChar+"\" frequency=\"...\" \\>");
		throw sxe;
	    }
	} else if(sName.equals("bigram")){
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
	    added=bigrams.add(new StringFreq(collator.getCollationKey(userChar), freq));
	    if(!added){
		SAXException sxe = new SAXException("Duplicated entry: <bigram sequence=\"" + userChar+"\" frequency=\"...\" \\>");
		throw sxe;
	    }
	} else if(sName.equals("trigram")){
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
	    added=trigrams.add(new StringFreq(collator.getCollationKey(userChar), freq));
	    if(!added){
		SAXException sxe = new SAXException("Duplicated entry: <trigram sequence=\"" + userChar+"\" frequency=\"...\" \\>");
		throw sxe;
	    }
	} else if(sName.equals("languageFrequencies")){
	    String country = attrs.getValue("", "country");
	    if(country==null || country==""){
		locale = new Locale(attrs.getValue("", "language"));
	    } else{
		locale = new Locale(attrs.getValue("", "language"),
				    country);
	    }
	    collator = Collator.getInstance(locale);
	    charIterator = BreakIterator.getCharacterInstance(locale);
	    sourceDocument = attrs.getValue("", "source");
	    rules = attrs.getValue("", "rules");
	}
    }

    /**
     * Receives notification of the end of an element. If the element is
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
	if(sName.equals("alphabet")){
	    coincidenceIndex = 0;
	    int cf; //used to store temporarily a character's frequency
	    int totalChars = 0;
	    for(Iterator iter = alphabet.iterator(); iter.hasNext(); ){
		totalChars += ((StringFreq)iter.next()).getFrequency();
	    }
	    double N2 = (double)totalChars * (double)(totalChars - 1);
	    for(Iterator iter = alphabet.iterator(); iter.hasNext(); ){
		cf = ((StringFreq)iter.next()).getFrequency();
		coincidenceIndex += ((double)cf * (double)(cf - 1))/N2;
	    }
	}
    }

    /**
     * Receives notification of character data inside an element and does
     * nothing.
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
 * -- LanguageFrequenciesHandler.java ends here --
 */
