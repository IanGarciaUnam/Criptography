/*
 * -- AnalyzerConstants.java --
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

package net.sourceforge.ganzua;

/**
 * A collection of constants used by <code>Analyzer</code> and
 * <code>AlaphaGen</code>.
 *
 * @see Analyzer
 * @see LangFreq
 * @author Jesús Adolfo García Pasquel
 * @version 0.01 November 2003
 */
public interface AnalyzerConstants{

    /**
     * Name of the property used to set the schema language to be used by
     * an implementation of <code>XMLReader</code>.
     */
    public static final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";

    /**
     * XML schema language used by <code>Analyzer</code> and 
     * <code>LangFreq</code>.
     */
    public static final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";

    /**
     * Name of the property used to set the XML schema file to be used.
     */
    public static final String JAXP_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";

    /**
     * Name of  the XML schema file that defines
     * <code>&lt;alphabetRules/&gt;</code> documents.
     */
    public static final String ALPHABET_RULES_SCHEMA_FILE = "AlphabetRules.xsd";

     /**
     * Name of  the XML schema file that defines
     * <code>&lt;languageFrequencies/&gt;</code> documents.
     */
    public static final String LANGUAGE_FREQUENCIES_SCHEMA_FILE = "LanguageFrequencies.xsd";

    /**
     * Name of  the XML schema file that defines
     * <code>&lt;cryptanalysis/&gt;</code> documents.
     */
    public static final String CRYPTANALYSIS_SCHEMA_FILE = "Cryptanalysis.xsd";

    /**
     * Relative path to the directory that usually contains the schemata.
     */
    public static final String SCHEMATA_PATH = "schemata" + System.getProperty("file.separator");

    /**
     * Relative path to the directory where the language frequencies files
     * (instances of <code>LanguageFrequencies.xsd</code>) are usually
     * stored. 
     */
    public static final String FREQUENCIES_PATH = "frequencies" + System.getProperty("file.separator");

    /**
     * Name of the <code>Properties</code> file that may contain the
     * directory the program was installed to, where to find the
     * schemata, frequency files, etc. The file may not exist.
     */
    public static final String CONFIG_FILE = "config.properties";

    /**
     * Property key used to get the directory the program was installed to,
     * from <code>CONFIG_FILE</code> if it exists.
     */
    public static final String ROOT_DIR_KEY = "rootDir";

    /**
     * Property key used to get the directory that contains the schemata files,
     * from <code>CONFIG_FILE</code> if it exists.
     */
    public static final String SCHEMATA_DIR_KEY = "schemataDir";

    /**
     * Property key used to get the directory that contains the language
     * frequencies, from <code>CONFIG_FILE</code> if it exists.
     */
    public static final String LANGUAGE_FREQUENCIES_DIR_KEY = "langFreqDir";
}
/*
 * -- AnalyzerConstants.java ends here --
 */
