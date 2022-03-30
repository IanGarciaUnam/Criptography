/*
 * -- LangFreq.java --
 *
 * Version       Changes
 * 0.01          First implementation
 * 0.02          Errata E0-28 for the W3C XML Schema specification states that
 *               the hint for the location of the schema document in the 
 *               schemaLocation attribute must be a URI. The program used to
 *               write a canonical path instead in method 
 *               writeFrequencies(LangFreq, File).
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

package net.sourceforge.ganzua;

import java.net.URL;
import java.util.*;
import java.text.Collator;
import java.text.CollationKey;
import java.text.BreakIterator;
import java.io.*;
import org.xml.sax.*;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.ParserConfigurationException;
import net.sourceforge.ganzua.handler.AlphabetRulesHandler;
import net.sourceforge.ganzua.handler.LanguageFrequenciesHandler;
import net.sourceforge.ganzua.text.*;
import net.sourceforge.ganzua.exception.*;

/**
 * Class used to get the relative frequencies of user characters from plain
 * text files encoded in some supported encoding 
 * (<a href="http://java.sun.com/j2se/1.4/docs/guide/intl/encoding.doc.html">
 * http://java.sun.com/j2se/1.4/docs/guide/intl/encoding.doc.html</a> )
 * using the rules specified in an instance of the XML document type
 * defined in the schema <code>AlphabetRules.xsd</code>.<br/><br/>
 *
 * Note: When I say <i>user characters</i> or <i>characters</i>, 
 * I'm talking about <code>String</code>s. A character like ü may be 
 * represented as a combination of the Unicode character u and ¨, but 
 * is seen as a character by the user.
 *
 * @see AnalyzerConstants
 *
 * @author Jesús Adolfo García Pasquel
 * @version 0.01 May 2003
 */
public class LangFreq
{
    /**
     * Used to compare <code>CollationKey</code>s and 
     * <code>StringFreq</code>s  by ignoring the 
     * <code>StringFreq</code>'s frequency */
    protected static final StringFreqCollationKeyComparator csComp = new StringFreqCollationKeyComparator();

    /**
     * <code>Locale</code> used to identify the language the input file is in.
     */
    protected Locale locale;
    
    /**
     * Used to create the <code>CollationKey</code>s used to sort
     * the character lists, bigram list and trigram list. */
    protected Collator collator;

    /**
     * Used to determine the limits of user characters in the input file, 
     * since they can span more than one Unicode character, e.g. ü may be
     * a combination of u and ¨. */
    protected BreakIterator charIterator;

    /**
     * <code>List</code> of <code>StringFreq</code>s that contains the 
     * characters that must be included and their frequencies.
     * (If <code>&lt;include/&gt;</code> was used in the instance of
     * <code>&lt;alphabetRules/&gt;</code>, otherwise it's an empty list). */
    protected List includeLst;

    /**
     * <code>Set</code> of <code>CollationKey</code>s that contains the 
     * characters that must be ignored.
     * (If <code>&lt;ignore/&gt;</code> was used in the instance of
     * <code>&lt;alphabetRules/&gt;</code>, otherwise it's an empty set). */
    protected Set ignoreSet;

    /**
     * <code>List</code> of <code>StringFreq</code>s that contains the
     * characters that must be included exclusively and their frequencies.
     * (If <code>&lt;includeExclusively/&gt;</code> was used in the instance of
     * <code>&lt;alphabetRules/&gt;</code>, otherwise it's an empty list). */
    protected List includeExLst;

    /**
     * <code>Map</code> that contains the character replacement pairs,
     * if any were specified, in the form of <code>CollationKey</code>s. */
    protected Map replaceMap;

    /**
     * <code>List</code> of <code>StringFreq</code>s that contains the bigrams 
     * found in the source document and their frequencies. */
    protected List bigramLst;

    /**
     * <code>List</code> of <code>StringFreq</code>s that contains the trigrams
     * found in the source document and their frequencies. */
    protected List trigramLst;

    /**
     * <code>String</code> used to store the absolute path of the instance of
     * <code>&lt;alphabetRules /&gt;</code> used. */
    protected String rulesPath;

    /**
     * Set of configuration properties. <code>null</code> if a
     * configuration properties file is not provided. The configuration
     * properties tell the program the directory it is installed in, the
     * directory where the schemata can be found and where the language
     * frequencies files are stored. */
    private static final Properties configProp;

    /* Initialize <code>configProp</code>. If the configuration properties file
     * exists, set <code>configProp</code> to a new <code>Properties</code>
     * with the file contents, otherwise, set it to <code>null</code>. */
    static{
	Properties configProperties = null;
	URL conf =LangFreq.class.getResource(AnalyzerConstants.CONFIG_FILE);
	if(conf != null){
	    try{
		InputStream is = conf.openStream();
		configProperties= new Properties();
		configProperties.load(is);
		is.close();
	    }catch(IOException ioe){}
	}
        configProp = configProperties;
    }

    /**
     * Used to store the directory the program is installed to. Unless
     * specified in <code>configProp</code> the directory that contains
     * the language frequencies is a subdirctory of <code>rootDir</code>
     * named <code>AnalyzerConstants.FREQUENCIES_PATH</code> and the
     * schemata used by the program are in one named
     * <code>AnalyzerConstants.SCHEMATA_PATH</code>. Do <u>not</u> use
     * this variable directly, use the <code>findRootDir()</code> method.*/
    private File rootDir;

    /**
     * Used to store the directory the schemata can be found in. Unless
     * specified in <code>configProp</code>, <code>schemataDir</code> is
     * a subdirectory of <code>rootDir</code>. Do <u>not</u> use this variable
     * directly, use the <code>findSchemataDir()</code> method.*/
    private File schemataDir;

    /**
     * Used to store the directory the language frequencies can be found in.
     * Unless specified in <code>configProp</code>, <code>langFreqDir</code>
     * is a subdirctory of <code>rootDir</code> named 
     * <code>AnalyzerConstants.FREQUENCIES_PATH</code>. Do <u>not</u> use this
     * variable directly, use the <code>findLangFreqDir()</code> method.*/
    private File langFreqDir;

    /**
     * Used to store the path of the file from which the relative frequencies
     * will be obtained. */
    protected String source;

    /**
     * Used to store the encoding of the file from which the relative
     * frequencies will be obtained (e.g. <code>UTF-8</code>, 
     * <code>ISO-8859-15</code>). */
    protected String sourceEncoding;

    /**
     * Constructor that receives an XML document (as a <code>File</code>) that
     * is an instance of <code>&lt;alphabetRules /&rt;</code>. The resulting
     * instance has the rules and source document specified in the
     * <code>&lt;alphabetRules /&rt;</code> document. To get the relative 
     * frequencies of the user characters, bigrams and trigrams in the source
     * document, you must call <code>getFrequencies()</code> after the
     * instance of <code>LangFreq</code> is created.
     *
     * @param xmlFile the path to an instance of the XML document type
     *                defined in the schema <code>AlphabetRules.xsd</code>
     *
     * @throws NullPointerException if <code>xmlFile</code> is <code>null</code>
     * @throws LangFreqException if the program can not determine the directory it is in and where the schemata are.
     * @throws SAXNotRecognizedException if W3C schemata, the schema source property or some other feature is not supported by the implementation of JAXP
     * @throws SAXParseException if there is an error parsing <code>xmlFile</code>
     * @throws SAXException if the parser reports an error
     * @throws ParserConfigurationException if there is an error configuring the parser (the implementation of JAXP lacks freatures required by <code>LangFreq</code>).
     * @throws SecurityException if the user is not allowed to read <code>xmlFile</code>
     * @throws FileNotFoundException if <code>xmlFile</code> could not be found
     * @throws IOException if an IOException occurs
     *
     * @see #getFrequencies()
     */
    public LangFreq(File xmlFile)
	throws NullPointerException, LangFreqException, 
	       SAXNotRecognizedException, SAXParseException,
	       SAXException, ParserConfigurationException, SecurityException,
	       FileNotFoundException, IOException
    {
	if(xmlFile == null){
	    throw new NullPointerException();
	}
	checkRootDirAvailable();
	// Use an instance of AlphabetRulesHandler as the SAX event handler
	AlphabetRulesHandler handler = this.parseAlphabetRules(xmlFile);
	this.setLocale(handler.getLocale());
	this.setCollator(handler.getCollator());
	this.setSource(handler.getSource());
	this.setSourceEncoding(handler.getSourceEncoding());
	includeLst = new ArrayList(handler.getInclude());
	ignoreSet = handler.getIgnore();
	includeExLst = new ArrayList(handler.getIncludeExclusively());
	replaceMap = handler.getReplace();
	/* ArrayLists are used because efficient random access is needed 
	   more than efficient editing in this case */
	bigramLst = new ArrayList();
	trigramLst = new ArrayList();
	handler = null; // 'destroy' the handler
	sortLists(); // sort lists
    }

    /**
     * Constructor that receives the path to an XML document that
     * is an instance of <code>&lt;alphabetRules /&rt;</code>. The resulting
     * instance has the rules and source document specified in the
     * <code>&lt;alphabetRules /&rt;</code> document. To get the relative 
     * frequencies of the user characters, bigrams and trigrams in the source
     * document, you must call <code>getFrequencies()</code> after the
     * instance of <code>LangFreq</code> is created.
     *
     * @param xmlFile the path to an instance of the XML document type
     *                defined in the schema <code>AlphabetRules.xsd</code>
     * @throws NullPointerException if <code>xmlFile</code> is <code>null</code>
     * @throws LangFreqException if the program can not determine the directory it is in and where the schemata are.
     * @throws SAXNotRecognizedException if W3C schemata, the schema source property or some other feature is not supported by the implementation of JAXP
     * @throws SAXParseException if there is an error parsing <code>xmlFile</code>
     * @throws SAXException if the parser reports an error
     * @throws ParserConfigurationException if there is an error configuring the parser (the implementation of JAXP lacks freatures required by <code>LangFreq</code>).
     * @throws SecurityException if the user is not allowed to read <code>xmlFile</code>
     * @throws FileNotFoundException if <code>xmlFile</code> could not be found
     * @throws IOException if an IOException occurs
     *
     * @see #getFrequencies()
     */
    public LangFreq(String xmlFile)
	throws NullPointerException, LangFreqException, 
	       SAXNotRecognizedException, SAXParseException,
	       SAXException, ParserConfigurationException, SecurityException,
	       FileNotFoundException, IOException
    {
	this(xmlFile == null ? null : new File(xmlFile));
    }

    /**
     * Tries to find the directory the program is in using 
     * <code>findRootDir</code>. If it can not be determined, then an
     * exception is thrown.
     *
     * @throws LangFreqException if the program can not determine the directory it is in.
     * @throws IOException if an IOException occurs
     */
    private final void checkRootDirAvailable() throws LangFreqException,
						      IOException
    {
	if(findRootDir() == null){
	    throw new LangFreqException("ERROR: Unable to find schemata. "+
					"Use the JAR or check "+
					AnalyzerConstants.CONFIG_FILE);
	}
    }

    /**
     * Parses the instance of <code>&lt;alphabetRules /&rt;</code> 
     * <code>pathname</code> and returns the <code>AlphabetRulesHandler</code>
     * used to parse.
     *
     * @param xmlFile  an instance of the XML document type defined in the 
     *                 schema <code>AlphabetRules.xsd</code>
     *
     * @throws SAXNotRecognizedException if the version of JAXP being used does not support W3 schemata
     */
    private final AlphabetRulesHandler parseAlphabetRules(File xmlFile)
	throws SAXNotRecognizedException, SAXParseException,
	       SAXException, ParserConfigurationException, SecurityException,
	       FileNotFoundException, IOException
    {
	AlphabetRulesHandler handler = new AlphabetRulesHandler();
	// Use the validating namespace aware parser
	SAXParserFactory saxFactory = SAXParserFactory.newInstance();
	saxFactory.setValidating(true);
	saxFactory.setNamespaceAware(true);
	// Parse the input
	File rulesF = null;
	try{
	    SAXParser saxParser = saxFactory.newSAXParser();
	    saxParser.setProperty(AnalyzerConstants.JAXP_SCHEMA_LANGUAGE,
				  AnalyzerConstants.W3C_XML_SCHEMA);
	    String schemataPath = findSchemataDir().getCanonicalPath() +
		                  System.getProperty("file.separator");
	    // Set schema source
	    saxParser.setProperty(AnalyzerConstants.JAXP_SCHEMA_SOURCE,
				  new File(schemataPath +
					   AnalyzerConstants.ALPHABET_RULES_SCHEMA_FILE));
	    rulesF = xmlFile;
	    this.setRulesPath(rulesF.getCanonicalPath());
	    rulesF = new File(getRulesPath());
	    FileInputStream rulesIS = new FileInputStream(rulesF);
	    saxParser.parse(rulesIS, handler, schemataPath);
	    rulesIS.close();
        } catch(SecurityException se){
	    throw new SecurityException("Unable to read\"" + rulesF.getPath() +
					"\". Permission denied.");
        } catch(FileNotFoundException fnfe){
	    throw new FileNotFoundException("File \"" + rulesF.getPath() + 
					    "\" not found");
	}
	return handler;
    }

    /**
     * Returns the instace of <code>Locale</code> being used.
     *
     * @return the instance of <code>Locale</code> being used.
     */
    public Locale getLocale(){
	return locale;
    }

    /**
     * Sets <code>locale</code> to <code>loc</code> and
     * sets <code>charIterator</code> to an instance for
     * the new <code>Locale</code>.<br/>
     * Note that it does not change the <code>collator</code>.
     *
     * @param loc The <code>Locale</code>
     */
    private void setLocale(Locale loc){
	locale = loc;
	charIterator = BreakIterator.getCharacterInstance(locale);
    }

    /**
     * Sets <code>colator</code> to <code>c</code>
     *
     * @param c the <code>Collator</code>
     */
    private void setCollator(Collator c){
	collator = c;
    }

    /**
     * Returns the instance of <code>Collator</code> being used to
     * generate the <code>CollationKey</code>s.
     *
     * @return the instance of <code>Collator</code> being used
     */
    public Collator getCollator(){
	return collator;
    }

    /**
     * Returns the <code>List</code> of <code>StringFreq</code>s that contains
     * the characters that must be included and their frequencies.
     *
     * @return <code>includeLst</code>
     */
    public List getIncludeLst(){
	return includeLst;
    }

    /**
     * Returns the <code>Set</code> of <code>CollationKey</code>s that stores 
     * the characters that must be ignored.
     *
     * @return <code>ignoreSet</code>
     */
    public Set getIgnoreSet(){
	return ignoreSet;
    }

    /**
     * Returns the <code>List</code> of <code>StringFreq</code>s that 
     * contains the characters that must be included exclusively and 
     * their frequencies.
     *
     * @return <code>includeExLst</code>
     */
    public List getIncludeExLst(){
	return includeExLst;
    }

    /**
     * Returns the <code>Map</code> that contains the character 
     * replacement pairs in the form of <code>CollationKey</code>s.
     *
     * @return <code>replaceMap</code>
     */
    public Map getReplaceMap(){
	return replaceMap;
    }

    /**
     * Returns the <code>List</code> of <code>StringFreq</code>s that contains 
     * the bigrams and their frequencies.
     * 
     * @return <code>bigramLst</code>
     */
    public List getBigramLst(){
	return bigramLst;
    }

    /**
     * Returns the <code>List</code> of <code>StringFreq</code>s that contains 
     * the trigrams and their frequencies.
     * 
     * @return <code>trigramLst</code>
     */
    public List getTrigramLst(){
	return trigramLst;
    }

    /**
     * Returns a <code>String</code> with the path of the file from which the
     * relative frequencies are obtained or an empty <code>String</code> 
     * if it has not been set.
     *
     * @return a <code>String</code> with the path of the file from which the
     *         relative frequencies are obtained.
     */
    public String getSource(){
	return source;
    }

    /**
     * Sets the path of the file from which the relative frequencies are
     * obtained to <code>src</code>.
     *
     * @param src The path of the file from which the relative frequencies
     *            should be obtained.
     */
    private void setSource(String src){
	source = src;
    }

    /**
     * Returns the encoding of the file from which the relative frequencies
     * will be obtained or an empty <code>String</code> if it has not been
     * set.
     *
     * @return the encoding of the file from which the relative frequencies
     *         will be obtained.
     */
    public String getSourceEncoding(){
	return sourceEncoding;
    }

    /**
     * Sets the encoding of the file from which the relative frequencies
     * will be obtained to <code>srcEnc</code>.
     *
     * @param srcEnc the encoding of the file from which the relative
     *               frequencies will be obtained.
     */
    private void setSourceEncoding(String srcEnc){
	sourceEncoding = srcEnc;
    }

    /**
     * Returns the absolute path of the instance of 
     * <code>&lt;alphabetRules /&gt;</code> used or the empty 
     * <code>String</code> if it has not been set.
     *
     * @return the absolute path of the instance of
     *         <code>&lt;alphabetRules /&gt;</code> used.
     */
    public String getRulesPath(){
	return rulesPath;
    }

    /**
     * Sets the absolute path of the instance of 
     * <code>&lt;alphabetRules /&gt;</code> used to <code>path</code>
     *
     * @param path the absolute path of the instance of
     *             <code>&lt;alphabetRules /&gt;</code> used.
     */
    private void setRulesPath(String path){
	rulesPath = path;
    }

    /**
     * Returns the path to the directory containing  the schemata
     * as a <code>File</code> or <code>null</code> if it could not be
     * found.<br/>
     * Note that this method does not guarantee the existance of such 
     * directory.
     *
     * @return the path to the directory containing the schemata as a 
     *         <code>File</code> or <code>null</code> if it could not be found.
     * @throws IOException if an I/O error occurs
     */
    private final File findSchemataDir() throws IOException
    {
	if(schemataDir != null){
	    return schemataDir;
	}
	String scProperty = configProp==null ? null
	                    : configProp.getProperty(AnalyzerConstants.SCHEMATA_DIR_KEY);
	schemataDir =  scProperty==null ? null : new File(scProperty);
	if(schemataDir == null){
	    File rootDir = findRootDir();
	    if(rootDir != null){
		schemataDir = new File(rootDir.getCanonicalPath() + 
				       System.getProperty("file.separator") + 
				       AnalyzerConstants.SCHEMATA_PATH);
	    }
	}
	return schemataDir;
    }

    /**
     * Returns the path to the directory containing  the language frequencies
     * files as a <code>File</code> or <code>null</code> if it could not be
     * found.<br/>
     * Note that this method does not guarantee the existance of such 
     * directory.
     *
     * @return the path to the directory containing the language frequencies as
     *         a <code>File</code> or <code>null</code> if it could not be
     *         found.
     * @throws IOException if an I/O error occurs
     */
    private final File findLangFreqDir() throws IOException
    {
	if(langFreqDir != null){
	    return langFreqDir;
	}
	String lfProperty = configProp==null ? null
	                    : configProp.getProperty(AnalyzerConstants.LANGUAGE_FREQUENCIES_DIR_KEY);
	langFreqDir = lfProperty==null ? null : new File(lfProperty);
	if(langFreqDir == null){
	    File rootDir = findRootDir();
	    if(rootDir != null){
		langFreqDir = new File(rootDir.getCanonicalPath() + 
				       System.getProperty("file.separator") + 
				       AnalyzerConstants.FREQUENCIES_PATH);
	    }
	}
	return langFreqDir;
    }

    /**
     * Returns the path to the application's root directory if
     * it is sepecified in the the configuration property file or
     * if the program is being executed from a JAR file, and <code>null</code>
     * otherwise. <br/>
     * Note that this method does not guarantee the existance of such 
     * directory.
     *
     * @return the canonical path of the application's root directory if  it is
     *         sepecified in the the configuration property file or if the 
     *         program is being executed from a JAR file.
     *
     * @throws IOException if an I/O error occurs
     */
    private final File findRootDir() throws IOException
    {
	if(rootDir != null){
	    return rootDir;
	}
	String rdProperty = configProp==null ? null
	                  : configProp.getProperty(AnalyzerConstants.ROOT_DIR_KEY);
	rootDir = configProp==null || rdProperty==null ? null 
	          : new File(rdProperty);
	if(rootDir == null){
	    String classpath = System.getProperty("java.class.path");
	    StringTokenizer st = new StringTokenizer(classpath, System.getProperty("path.separator"));
	    if(st.countTokens()<=1 && 
	       classpath.toLowerCase().endsWith(".jar")){
		rootDir = new File(classpath);
		rootDir = new File(rootDir.getCanonicalPath());
		rootDir = rootDir.getParentFile();
	    }
	}
	return rootDir;
    }

    /**
     * Program that receives an instance of the XML document type defined
     * in the schema <code>AlphabetRules.xsd</code> and writes a UTF-8 encoded
     * XML document with the relative frequencies of the user characters 
     * found in the source file specified in the instance of 
     * <code>&lt;alphabetRules/&gt;</code>.<br/>
     *
     * The instance specifies either all the characters to count and get 
     * bigrams and trigrams of, from the source file (ignoring any other 
     * character that may be found in it), or a set of characters to count 
     * and a set of characters to ignore. In the later case any character
     * that is not in any of the lists will be considered. In both cases, a
     * list of replacement pairs may be given.<br/>
     *
     * When a user character is found in the source file, the program looks for
     * it in the replacement list and replaces it if found, then that character
     * is counted or ignored following the rules explained in the previos <br/>
     * paragraph.
     *
     * Note that when a character <code>a</code> has a replacement character
     * <code>b</code> and the cracter <code>b</code> has a replacement
     * <code>c</code>, the character <code>a</code> is replaced by 
     * <code>b</code>, <b>not</b> by <code>c</code>.
     */
    public static void main(String [] args){
	final String usage = "\nUsage:\n"+ 
	    "\tjava -jar langFreq.jar [-o <file>] <alphabet rules>\n\n"+
	    "  where <alphabet rules> is an instance of the XML document type\n"+
	    "  defined in the schema AlphabetRules.xsd and the options are:\n\n"+
	    "    -o <file>   write the language frequencies to the specified file.\n"+
	    "                If this option is not used, the output of the program\n"+
	    "                is written to a file named using the ISO 639 code\n"+
	    "                of the language and an arbitrary number, and placed\n"+
	    "                inside the language frequencies directory or in the\n"+
	    "                user's home directory if the user lacks permission to\n"+
	    "                write in the former.\n\n"+
	    "    -help       display this help and exit. Also --help";
	String errorMsg = null; // !=null if some exception is thrown
	String xmlFile = null;
	String outputFile = null;
	if(args.length < 1){
	    System.out.println(usage);
	    System.exit(1);
	}else{ // parse arguments
	    try{
		for(int i=0; i<args.length; i++){
		    if(i == args.length-1 && args[i].charAt(0) != '-'){
			xmlFile = args[i];
		    }else if(args[i].substring(0, 2).equals("-o")){
			if(args[i].length() == 2){
			    i++;
			    outputFile = args[i];
			}else{
			    outputFile = args[i].substring(2);
			}
		    }else{
			System.out.println(usage);
			System.exit(1);
		    }
		}
	    }catch(IndexOutOfBoundsException ob){
		/* If there is no argument to -o or if an option has a length
		   smaller than 2 */
		System.out.println(usage);
		System.exit(1);
	    }
	}
	if(xmlFile == null){
	    System.out.println(usage);
	    System.exit(1);
	}
	// parse the xmlFile and write the relative frequencies file
	try{
	    System.out.println("\nParsing " + xmlFile);
	    /* Create an instance of LangFreq to generate the 
	       relative frequencies file */
	    LangFreq generator = new LangFreq(xmlFile);
	    // read the input file and get the character frequencies
	    System.out.println("\nGetting character, bigram and trigram frequencies. Please be patient.");
	    generator.getFrequencies();
	    // write the relative frequencies XML document
	    File f = outputFile == null ? generator.getDefaultOutputFile()
		                        : new File(outputFile);
	    System.out.println("\nWriting to " + f.getPath());
	    LangFreq.writeFrequencies(generator, f);
	}catch(SAXNotRecognizedException snre){
	    errorMsg = snre.getMessage();
	    if(errorMsg.indexOf(AnalyzerConstants.JAXP_SCHEMA_LANGUAGE)>=0){
		errorMsg = "ERROR: W3C schemata are not supported.";
	    }else if(errorMsg.indexOf(AnalyzerConstants.JAXP_SCHEMA_SOURCE)>=0){
		errorMsg = "ERROR: Schema source property is not supported.";
	    }else{
		errorMsg = "Some required parser features are not supported.";
	    }
	}catch(SAXParseException pe){
	    errorMsg = "ERROR: Parsing line: "+ pe.getLineNumber() +
		       "\n\t" + pe.getMessage();
	}catch(SAXException sxe){
	    errorMsg = "ERROR: " + sxe.getMessage();
	}catch(ParserConfigurationException pce){
	    errorMsg ="ERROR: Configuring the parser."+
		      " Update your version of JAXP";
	}catch(SecurityException se){
	    errorMsg = "ERROR: " + se.getMessage();
	}catch(FileNotFoundException fnfe){
	    errorMsg = "ERROR: " + fnfe.getMessage();
	}catch(UnsupportedEncodingException uee){
	    errorMsg = "ERROR: " + uee.getMessage();
	}catch(IOException ioe){
	    errorMsg = "ERROR: An input/output error occurred";
	}catch(LangFreqException e){
	    errorMsg = e.getMessage();
	}catch(NullPointerException npe){
	    errorMsg = usage;
	}finally{
	    if(errorMsg != null){
		System.err.println(errorMsg);
		System.exit(1);
	    }
	}
    }

    /**
     * Method that sorts <code>includeLst</code> and 
     * <code>includeExLst</code>
     */
    private void sortLists(){
	Collections.sort(includeLst);
	Collections.sort(includeExLst);
    }

    /**
     * Adds a <code>StringFreq</code> with <code>strKey</code> and frequency 1
     * to the ordered <code>List</code> <code>list</code> if it is not already
     * in it and increments the number of occurrences otherwise.
     *
     * @param strKey the string's <code>CollationKey</code>
     * @param list the ordered <code>List</code> of arrays of
     *             <code>Object</code>s
     */
    private final void add(CollationKey strKey, List list){
	int idx = Collections.binarySearch(list, strKey, csComp);
	if(idx>=0){ // increment the number of occurrences
	    ((StringFreq)list.get(idx)).increment();
	} else{ // add it to the list
	    list.add(-(idx+1), new StringFreq(strKey, 1));
	}
    }

    /**
     * Method that reads the input file <code>source</code> and gets the
     * frequencies of the user characters, bigrams and trigrams.<br/>
     *
     * The input file <code>source</code> is the file from which the
     * language's relative frequencies are calculated.
     *
     * @throws FileNotFoundException if <code>source</code>  can not be found
     * @throws SecurityException if the user is not allowed to read <code>source</code>
     * @throws UnsupportedEncodingException if the encoding of <code>source</code> is not supported
     * @throws IOException if an I/O error occurs
     */
    public final void getFrequencies() throws FileNotFoundException,
					      SecurityException,
					      UnsupportedEncodingException,
					      IOException
    {
	try{
	    File f = new File(source);
	    source = f.getCanonicalPath();
	    FileInputStream fis = new FileInputStream(source);
	    InputStreamReader isr = new InputStreamReader(fis,
							  sourceEncoding);
	    BufferedReader in = new BufferedReader(isr);
	    String uChar = getNextUserChar(in);
	    // Used in the creation of the trigram list
	    String prevUChar = null;
	    // Used in the creation of the bigram list
	    String lastUChar = null;
	    /* Used to avoid the creation of StringBuffers caused by the 
	       use of the String operator + */
	    StringBuffer sb = new StringBuffer();
	    CollationKey repCharKey; // to store the replacement CollationKey
	    if(includeExLst.size() != 0){ // use includeExLst
		while(uChar != null){
		    CollationKey uCharKey = collator.getCollationKey(uChar);
		    // Replace the user character if so indicated in <replace>
		    repCharKey = (CollationKey)replaceMap.get(uCharKey);
		    if(repCharKey!=null){
			uCharKey = repCharKey;
			uChar = repCharKey.getSourceString();
		    }
		    /* Increment the number of occurrences if the caracter
		       appears in <includeExclusively> */
		    int idx = Collections.binarySearch(includeExLst, uCharKey, csComp);
		    if(idx >= 0){
			((StringFreq)includeExLst.get(idx)).increment();

			if(prevUChar!=null){
			    sb.delete(0, sb.length());
			    String tmp = sb.append(prevUChar).append(lastUChar).append(uChar).toString();
			    add(collator.getCollationKey(tmp), trigramLst);
			}
			if(lastUChar!=null){
			    sb.delete(0, sb.length());
			    String tmp = sb.append(lastUChar).append(uChar).toString();
			    add(collator.getCollationKey(tmp), bigramLst);
			    prevUChar = lastUChar;
			}
			lastUChar = uChar;
		    }
		    uChar = getNextUserChar(in);
		}
	    } else{ // use includeLst and ignoreSet
		while(uChar != null){
		    CollationKey uCharKey = collator.getCollationKey(uChar);
		    // Replace the user character if so indicated in <replace>
		    repCharKey = (CollationKey)replaceMap.get(uCharKey);
		    if(repCharKey!=null){
			uCharKey = repCharKey;
			uChar = repCharKey.getSourceString();
		    }
		    /* If the character is not in <ignore>, add it or
		       increment the number of occurences in includeLst */
		    if(!ignoreSet.contains(uCharKey)){
			add(uCharKey, includeLst);
			if(prevUChar!=null){
			    sb.delete(0, sb.length());
			    String tmp = sb.append(prevUChar).append(lastUChar).append(uChar).toString();
			    add(collator.getCollationKey(tmp), trigramLst);
			}
			if(lastUChar!=null){
			    sb.delete(0, sb.length());
			    String tmp = sb.append(lastUChar).append(uChar).toString();
			    add(collator.getCollationKey(tmp), bigramLst);
			    prevUChar = lastUChar;
			}
			lastUChar = uChar;
		    }
		    uChar = getNextUserChar(in);
		}
	    }
	    in.close();
	    isr.close();
	    fis.close();
	} catch(FileNotFoundException fnfe){
	    throw new FileNotFoundException("File \""+ source +
					    "\" not found");
	} catch(SecurityException se){
	    throw new SecurityException("Unable to read " + source +
					"\". Permission denied.");
	}catch(UnsupportedEncodingException uee){
	    throw new UnsupportedEncodingException(sourceEncoding +" is not " +
						   "a supported encoding");
	}
    }

    /**
     * Receives a <code>BufferedReader</code>, reads a user character and
     * returns a string with that user character, or <code>null</code> if
     * the end of the stream has been reached.
     *
     * @param in the <code>BufferedReader</code> to read the user character
     *           from
     * @return a <code>String</code> with the next user character in
     *         <code>in</code> or <code>null</code> if the end of the stream
     *         has been reached
     */
    private String getNextUserChar(BufferedReader in) throws IOException
    {
	in.mark(2);
	int read = in.read();
	StringBuffer uChar = new StringBuffer();
	if(read == -1){
	    return null;
	} else{
	    while(numChars(uChar.append((char)read).toString())<2 && 
		  read != -1){
		in.mark(2);
		read = in.read();
	    }
	    if(read != -1){
		in.reset();
	    }
	    uChar.deleteCharAt(uChar.length()-1);
	    return uChar.toString();
	}
    }

    /**
     * Function that returns the number of user characters in the
     * string <code>str</code> according to <code>charIterator</code>
     *
     * @param str a <code>String</code>
     * @return the number of user characters in <code>str</code> according to
     *         <code>charIterator</code>
     *
     * @see #setLocale
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

    /**
     * Returns a file named using the ISO 639 language code of the language and
     * an arbitrary number where the language frequencies can be written to.
     * If the user has write permission to the directory that contains the
     * language frequencies files the file is placed there, otherwise the
     * file is place in the user's home directory.
     */
    private File getDefaultOutputFile() throws IOException
    {
	File f = null;
	String freqDir;
	int i=1;
	f = findLangFreqDir();
	freqDir = f.getCanonicalPath() + System.getProperty("file.separator");
	if(!f.canWrite()){
	    f = new File(System.getProperty("user.home"));
	    freqDir =  f.getCanonicalPath() +
		       System.getProperty("file.separator");
	}
	freqDir += locale.getLanguage();
	f = new File(freqDir + i + ".xml");
	while(f.exists()){
	    i++;
	    f = new File(freqDir + i + ".xml");
	}
	return f;
    }

    /**
     * Writes a UTF-8 encoded XML document with the relative frequencies of
     * the user characters, bigram and trigrams contained in <code>ag</code>
     * to the <code>File</code> <code>f</code>.
     *
     * @param ag an <code>LangFreq</code> that has been used to get the
     *           relative frequencies of the characters, bigrams and
     *           trigrams of a text file.
     * @param f the <code>File</code> to write the relative frequencies to
     * @throws SecurityException if the user is not allowed to write the file <code>f</code>
     * @throws FileNotFoundException if the file <code>f</code> can not be written
     * @throws IOException if an <code>IOException</code> occurs
     * @throws NullPointerException if any of the parameters is <code>null</code>
     */
    public static void writeFrequencies(LangFreq ag,
					File f) throws SecurityException,
						       FileNotFoundException,
						       IOException,
						       NullPointerException
    {
	if(ag == null || f == null){
	    throw new NullPointerException();
	}
	try{
	    FileOutputStream fos = new FileOutputStream(f);
	    OutputStreamWriter out = new OutputStreamWriter(fos, "UTF8");
	    // wite the relative frequencies file avoiding the operator '+'
	    out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<languageFrequencies xmlns=\"");
	    out.write(LanguageFrequenciesHandler.NAMESPACE);
	    out.write("\"\n                     xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n                     xsi:schemaLocation=\"");
	    out.write(LanguageFrequenciesHandler.NAMESPACE);
	    out.write("\n                                         ");
	    out.write((new File(ag.findSchemataDir().getCanonicalPath(),
				AnalyzerConstants.LANGUAGE_FREQUENCIES_SCHEMA_FILE)).toURI().toString());
	    out.write("\"\n                     language=\"");
	    out.write(ag.getLocale().getLanguage());
	    if(!ag.getLocale().getCountry().equals("")){
		out.write("\" country=\"");
		out.write(ag.getLocale().getCountry());
		out.write("\"\n");
	    } else{
		out.write("\"\n");
	    }
	    out.write("                     source=\"");
	    out.write(ag.getSource());
	    out.write("\"\n");
	    out.write("                     rules=\"");
	    out.write(ag.getRulesPath());
	    out.write("\" >\n");
	    out.write(" <alphabet>\n");
	    Iterator iterator = null;
	    if(ag.getIncludeExLst().size() != 0){
		iterator = ag.getIncludeExLst().iterator();
	    } else {
		iterator = ag.getIncludeLst().iterator();
	    }
	    StringFreq tmpSF;
	    while(iterator.hasNext()){
		tmpSF = (StringFreq)iterator.next();
		out.write("  <character char=\"");
		out.write(toAttributeString(tmpSF.getString()));
		out.write("\" frequency=\"");
		out.write(Integer.toString(tmpSF.getFrequency()));
		out.write("\" />\n");
	    }
	    out.write(" </alphabet>\n <bigrams>\n");
	    iterator = ag.getBigramLst().iterator();
	    while(iterator.hasNext()){
		tmpSF = (StringFreq)iterator.next();
		out.write("  <bigram sequence=\"");
		out.write(toAttributeString(tmpSF.getString()));
		out.write("\" frequency=\"");
		out.write(Integer.toString(tmpSF.getFrequency()));
		out.write("\" />\n");
	    }
	    out.write(" </bigrams>\n <trigrams>\n");
	    iterator = ag.getTrigramLst().iterator();
	    while(iterator.hasNext()){
		tmpSF = (StringFreq)iterator.next();
		out.write("  <trigram sequence=\"");
		out.write(toAttributeString(tmpSF.getString()));
		out.write("\" frequency=\"");
		out.write(Integer.toString(tmpSF.getFrequency()));
		out.write("\" />\n");
	    }
	    out.write(" </trigrams>\n</languageFrequencies>\n");
	    out.flush();
	    out.close();
	    fos.close();
	} catch(SecurityException se){
	    throw new SecurityException("Unable to write to \""+ f.getPath() +
					"\". Permission denied");
	} catch(FileNotFoundException fnfe){
	    throw new FileNotFoundException("File \"" + f.getPath() +
					    "\" could not be written");
	}
    }

    /**
     * Returns a string with the contents of <code>str</code> that
     * can be used as an attribute in an XML document, i.e. transforms
     * all the special or control characters to predefined entities 
     * and Unicode character references respectively, e.g. <code>"<\n>"</code>
     * becomes <code>"&amp;lt;&amp;#10;&amp;gt;"</code>
     *
     * @param str a <code>String</code>
     */
    private static String toAttributeString(String str){
	StringBuffer ret = new StringBuffer();
	char [] arr = str.toCharArray();
	for(int i=0; i<arr.length; i++){
	    if(Character.getType(arr[i]) == Character.CONTROL){
		ret.append("&#").append((int)arr[i]).append(";");
	    } else{
		switch((int)arr[i]){
		  case (int)'&': ret.append("&amp;"); break;
		  case (int)'<': ret.append("&lt;"); break;
		  case (int)'>': ret.append("&gt;"); break;
		  case (int)'"': ret.append("&quot;"); break;
		  case (int)'\'': ret.append("&apos;"); break;
		  default: ret.append(arr[i]);
		}
	    }
	}
	return ret.toString();
    }
}
/*
 * -- LangFreq.java ends here --
 */
