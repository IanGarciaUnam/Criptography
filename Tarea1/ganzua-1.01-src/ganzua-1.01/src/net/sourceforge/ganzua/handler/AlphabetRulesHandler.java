/*
 * -- AlphabetRulesHandler.java --
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
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;
import net.sourceforge.ganzua.text.*;

/**
 * SAX2 event handler that stores the instructions of an instance of the 
 * XML document class defined in the schema <code>AlphabetRules.xsd</code>.
 *
 * @author Jesús Adolfo García Pasquel
 * @version 0.01 May 2003
 */
public class AlphabetRulesHandler extends DefaultHandler
{
    /**
     * The namespace the instances of <code>AlphabetRules.xsd</code>
     * belong to. */
    public static final String NAMESPACE = "http://ganzua.sourceforge.net/rules";

    /**
     * <code>byte</code> used to indicate if a <code>&lt;character /&gt;</code>
     * element appeared in an <code>&lt;ignore /&gt;</code> (<code>0x4</code>),
     * <code>&lt;include /&gt;</code> (<code>0x2</code>) or 
     * <code>&lt;includeExclusively /&gt;</code> (<code>0x1</code>) element.
     */
    private byte ignoreIncludeExclusively;

    /**
     * Used to store the value of <code>&lt;alpabetRules /&gt;</code>'
     * <code>source</code> attribute.
     */
    private String sourceDocument;

    /**
     * Used to store the value of <code>&lt;alpabetRules /&gt;</code>'
     * <code>sourceEncoding</code> attribute.
     */
    private String sourceDocEncoding;

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
     * values of the <code>char</code> attributes of the 
     * <code>&lt;character /&gt;</code> elements inside the 
     * <code>&lt;includeEclusively /&gt;</code> element.
     */
    private Set includeExclusively;

    /**
     * <code>Set</code> of <code>StringFreq</code>s used to store the
     * values of the <code>char</code> attributes of the
     * <code>&lt;character /&gt;</code> elements inside the
     * <code>&lt;include /&gt;</code> element.
     */
    private Set include;

    /**
     * <code>Set</code> of <code>CollationKeys</code> used to store the
     * values of the <code>char</code> attributes of the
     * <code>&lt;character /&gt;</code> elements inside the
     * <code>&lt;ignore /&gt;</code> element.
     */
    private Set ignore;

    /**
     * <code>Map</code> with <i>character - replacement</i>, pairs
     * used to store the values of the <code>ofChar</code> 
     * and <code>byChar</code> attributes of the 
     * <code>&lt;occurrences /&gt;</code> elements inside the
     * <code>&lt;replace /&gt;</code> element as 
     * <code>CollationKey</code>s.
     */
    private Map replace;

    public AlphabetRulesHandler(){
	super();
	ignoreIncludeExclusively=0;
	locale = Locale.getDefault();
	collator = Collator.getInstance(locale);
	includeExclusively = new HashSet();
	include = new HashSet();
	ignore = new HashSet();
	replace = new HashMap();
    }

    /**
     * Returns a <code>Set</code> of <code>StringFreq</code>s with the
     * values of the <code>char</code> attributes of the
     * <code>&lt;character /&gt;</code> elements inside the
     * <code>&lt;include /&gt;</code> element.
     *
     * @return <code>include</code>
     */
    public Set getInclude(){
	return include;
    }

    /**
     * Returns a <code>Set</code> of <code>CollationKeys</code> with the
     * values of the <code>char</code> attributes of the
     * <code>&lt;character /&gt;</code> elements inside the
     * <code>&lt;ignore /&gt;</code> element.
     *
     * @return <code>ignore</code>
     */
    public Set getIgnore(){
	return ignore;
    }

    /**
     * Returns a <code>LinkedList</code> of <code>StringFreq</code>s with the
     * values of the <code>char</code> attributes of the 
     * <code>&lt;character /&gt;</code> elements inside the 
     * <code>&lt;includeEclusively /&gt;</code> element.
     */
    public Set getIncludeExclusively(){
	return includeExclusively;
    }

    /**
     * Returns a <code>Map</code> with <i>character - replacement</i> pairs
     * with the values of the <code>ofChar</code> 
     * and <code>byChar</code> attributes of the 
     * <code>&lt;occurrences /&gt;</code> elements inside the
     * <code>&lt;replace /&gt;</code> element as 
     * <code>CollationKey</code>s.
     */
    public Map getReplace(){
	return replace;
    }

    /**
     * Returns the value of <code>&lt;alpabetRules /&gt;</code>'
     * <code>source</code> attribute.
     */
    public String getSource(){
	return sourceDocument;
    }

    /**
     * Returns the value of <code>&lt;alpabetRules /&gt;</code>'
     * <code>sourceEncoding</code> attribute.
     */
    public String getSourceEncoding(){
	return sourceDocEncoding;
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
     * Checks that the character is a user character (e.g. a, ñ, ü) and
     * not a string of user characters (e.g. ch, ll)
     *
     * @param userChar the <code>String</code> with the user character
     * @return <code>true</code> if the specified <code>String</code> is a user
     *         character, <code>false</code> otherwise.
     */
    private final boolean isUserCharacter(String userChar){
	charIterator.setText(userChar);
	int boundary = charIterator.first();
	int numChars = -1;
	while(boundary != BreakIterator.DONE){
	    boundary = charIterator.next();
	    numChars ++;
	}
	return numChars == 1;
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
     * <code>&lt;alphabetRules /&gt;</code> is in the attributes of it's
     * elements, all the data is retrieved here and stored in the appropriate
     * <code>Collection</code> (<code>includeExclusively</code>, 
     * <code>include</code>, <code>ignore</code> or <code>replace</code>).
     *
     * @param namespaceURI the Namespace URI
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
	   SAXException sxe = new SAXException("The document's namespace is not that of <alphabetRules />");
	   throw sxe;
	}
	if(sName.equals("character")){
	    String userChar = attrs.getValue("", "char");
	    /* Check that the character is a user character (e.g. a, ñ, ü)
               and not a string of user characters (e.g. ch, ll) */
	    if(!isUserCharacter(userChar)){
		SAXException sxe = new SAXException(userChar+"\" is not a valid character");
		throw sxe;
	    }
	    boolean added = false;
	    switch((int)ignoreIncludeExclusively){
	      case 0x1: added=includeExclusively.add(new StringFreq(collator.getCollationKey(userChar))); break;
	      case 0x2: added=include.add(new StringFreq(collator.getCollationKey(userChar))); break;
	      case 0x4: added=ignore.add(collator.getCollationKey(userChar)); break;
	    }
	    if(!added){
		SAXException sxe = new SAXException("Duplicated entry: <character char=\""+
						    userChar+"\" \\>");
		throw sxe;
	    }
	} else if(sName.equals("occurrences")){
	    String [] replacement = new String [2];
	    replacement[0] = attrs.getValue("", "ofChar");
	    replacement[1] = attrs.getValue("", "byChar");
	    if(!isUserCharacter(replacement[0]) || 
	       !isUserCharacter(replacement[1])){
		SAXException sxe = new SAXException("error in attribute of <occurrences/>: \""+ (!isUserCharacter(replacement[0]) ? replacement[1] : replacement[0]) +"\" is not a valid character");
		throw sxe;
	    }
	    if(replace.put(collator.getCollationKey(replacement[0]), 
			   collator.getCollationKey(replacement[1]))!=null){
		SAXException sxe = new SAXException("\""+replacement[0]+"\" already has a replacement");
		throw sxe;
	    }
	} else if(sName.equals("include")){
	    ignoreIncludeExclusively = 0x2;
	} else if(sName.equals("ignore")){
	    ignoreIncludeExclusively = 0x4;
	} else if(sName.equals("includeExclusively")){
	    ignoreIncludeExclusively = 0x1;
	} else if(sName.equals("alphabetRules")){
	    String country = attrs.getValue("", "country");
	    if(country==null || country==""){
		locale = new Locale(attrs.getValue("", "language"));
	    } else{
		locale = new Locale(attrs.getValue("", "language"),
				    country);
	    }
	    collator = Collator.getInstance(locale);
	    collator.setStrength(Collator.IDENTICAL);
	    charIterator = BreakIterator.getCharacterInstance(locale);
	    sourceDocument = attrs.getValue("", "source");
	    sourceDocEncoding = attrs.getValue("", "sourceEncoding");
	}
    }

    /**
     * Receives notification of the end of an element and does nothing.
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
 * -- AlphabetRulesHandler.java ends here --
 */
