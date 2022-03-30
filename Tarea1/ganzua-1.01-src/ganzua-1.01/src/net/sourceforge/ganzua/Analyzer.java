/*
 * -- Analyzer.java --
 *
 * Version       Changes
 * 0.01          First implementation
 * 0.02          Errata E0-28 for the W3C XML Schema specification states that
 *               the hint for the location of the schema document in the 
 *               schemaLocation attribute must be a URI. The program used to
 *               write a canonical path instead in method save(File).
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

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.prefs.*;
import java.text.*;
import java.io.*;
import java.net.URL;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import net.sourceforge.ganzua.text.*;
import net.sourceforge.ganzua.component.*;
import net.sourceforge.ganzua.event.*;
//XML
import org.xml.sax.*;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.ParserConfigurationException;
import net.sourceforge.ganzua.handler.*;

/**
 * Provides an environment for solving classical (monoalphabetic and
 * polyalphabetic) ciphers.
 *
 * @author Jesús Adolfo García Pasquel
 * @version 0.02 May 2004
 */
public class Analyzer extends JPanel
                      implements ActionListener{

    private static final String CAN_NOT_ESTIMATE = "---";

    /**
     * Used by the method <code>parseXML</code> */
    private static final byte LANGUAGE_FREQUENCIES = 1;

    /**
     * Used by the method <code>parseXML</code> */
    private static final byte CRYPTANALYSIS_PROJECT = 2;

    /**
     * The program's logo. Used in <code>aboutFrame</code> */
    private static final ImageIcon LOGO;

    static{
	LOGO=new ImageIcon(Analyzer.class.getResource("images/ganzua128.png"));
    }

    /**
     * The <code>Locale</code> <code>collator</code> is using */
    protected Locale locale;

    /**
     * The collator used to generate the <code>CollationKey</code>s
     * needed by many of the components used by <code>Analyzer</code>s */
    protected Collator collator;

    /**
     * Used to display the numbers using a localized format */
    protected DecimalFormat df;

    /**
     * Component that lets the user select the character substitutions */
    protected Substitution substitution;

    /**
     * Applies the selected substitution to the ciphertext */
    protected CiphertextManager cipherManager;

    /**
     * Displays the ciphertext and plaintext */
    protected CiphertextPanel cipherPanel;

    /**
     * Displays the relative frequencies of the characters in
     * the ciphertext */
    protected StatsPanel cipherStats;

    /**
     * Displays the coincidence index of the ciphertext */
    protected JTextField ciphertextCIField;

    /**
     * Displays an estimate of the number of alphabets used to generate the
     * ciphertext. The estimate is calculated using the ciphertext's 
     * coincidence index, <code>langCI</code> and the number of characters
     * that make up the plain alphabet. If <code>langCI</code> is not available
     * or if the number of characters in the plain alphabet and the cipher
     * alphabet do not match, <code>&quot;---&quot;</code> is displayed. */
    protected JTextField numAlphaEstField;

    /**
     * <code>JPanel</code> in which <code>cipherStats</code> is displayed. */
    protected JPanel cipherStatsPanel;

    /**
     * Displays the relative frequencies of the characters in
     * a language.<br/>
     * The relative frequencies are read from an instance of
     * the XML schema <code>LanguageFrequencies.xsd</code> */
    protected StatsPanel langStats;

    /**
     * <code>JFrame</code> in which <code>langStats</code> is displayed. */
    protected JFrame langStatsFrame;

    /**
     * The coincidence index calculated from the data read from an
     * instance of the XML schema <code>LanguageFrequencies.xsd</code> */
    protected double langCI;

    /**
     * Displays <code>langCI</code>. */
    protected JTextField langCIField;

    /**
     * Provides tools to manipulate monoalphabetic and polyalphabetic
     * substitutions.
     */
    protected CipherToolsPane toolsPane;

    /**
     * Displays the cipher alphabet and lets the user remove characters
     * from it */
    protected JFrame remFromCipherAlphaFrame;

    /**
     * <code>ListModel</code> used to display the cipher alphabet in a
     * <code>JList</code> in <code>remFromCipherAlphaFrame</code> */
    protected CipherAlphabetListModel cipherAlphaLM;

    /**
     * Used to let the user choose a text file to open as the
     * ciphertext */
    protected TextFileChooser cipherChooser;

    /**
     * Used to let the user choose an instance of 
     * <code>LanguageFrequencies.xsd</code>. */
    protected JFileChooser langChooser;

    /**
     * Used to let the user choose a file to save his
     * cryptanalysis work in. The file will be an instance of
     * <code>Cryptanalysis.xsd</code> */
    protected ConfirmFileChooser cryptanalysisFileChooser;

    /**
     * Used to let the user add characters to the cipher and plain
     * alphabets. */
    protected CharacterAdditionDialog charAddDlg = null;

    /**
     * <code>JFrame</code> used to display information about the program.
     * this variable is initialized if and when the method
     * <code>getMenuBar()</code> is called. */
    private JFrame aboutFrame = null;

    /**
     * <code>LanguageFrequenciesHandler</code> used hold the language 
     * statistics so they can be stored in an instance of 
     * <code>Cryptanalysis.xsd</code> if the user chooses to save the
     * cryptanalysis project. */
    private LanguageFrequenciesHandler freqsH = null;

    /**
     * <code>LanguageFrequenciesHandler</code> used to parse language
     * frequencies. */
    private LanguageFrequenciesHandler freqsH_tmp = null;

    /**
     * <code>CryptanalysisHandler</code> used to parse instances of
     * <code>Cryptanalysis.xsd</code> */
    private CryptanalysisHandler cryptHan;

    /**
     * Used to store the file the user saved his current project to
     * so he does not have to be promted on the following times. */
    private File savedProject = null;

    /**
     * Used to identify if the current cryptanalysis project has
     * unsaved changes (<code>true</code> if that is the case, 
     * <code>false</code> otherwise). */
    protected boolean unsavedChanges = false;

    /**
     * <code>ResourceBundle</code> with labels */
    private ResourceBundle labelsRB;

    /**
     * <code>ResourceBundle</code> with messages related to the command line 
     * arguments */
    private static final ResourceBundle commandMsgs;

    static{
	commandMsgs = ResourceBundle.getBundle(Analyzer.class.getName() + "Command",
					       Locale.getDefault());
    }

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
	URL conf =Analyzer.class.getResource(AnalyzerConstants.CONFIG_FILE);
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
     * Used to store the directory the program is installed in. The path is
     * needed to find the directory where the schemata and language frequencies
     * are if a configuration properties file is not found. Do <u>not</u> use
     * this variable directly, use the <code>findRootDir()</code> method. */
    private File rootDir = null;

    /**
     * Used to store the directory the schemata can be found in.
     * Do <u>not</u> use this variable directly, use the 
     * <code>findSchemataDir()</code> method.*/
    private File schemataDir = null;

    /**
     * Used to store the directory the language frequencies can be found in.
     * Do <u>not</u> use this variable directly, use the 
     * <code>findLangFreqDir()</code> method.*/
    private File langFreqDir = null;

    public Analyzer(){
	super();
	setLayout(new BorderLayout());
	locale = JComponent.getDefaultLocale();
	collator = Collator.getInstance(locale);
	df = getLocalizedDecimalFormat();
	cipherManager = new CiphertextManager(locale, collator, "");
	labelsRB = ResourceBundle.getBundle(Analyzer.class.getName(),
					    JComponent.getDefaultLocale());
	initializeGUI();
	langCI = -1;
    }

    /**
     * Returns a <code>DecimalFormat</code> localized using the
     * <code>JComponent</code>'s default <code>Locale</code>.
     *
     * @return a localized <code>DecimalFormat</code>
     */
    private static DecimalFormat getLocalizedDecimalFormat(){
	DecimalFormat decF = null;
	NumberFormat numF = DecimalFormat.getInstance(JComponent.getDefaultLocale());
	if(numF instanceof DecimalFormat){
	    decF = (DecimalFormat)numF;
	} else{
	    decF = new DecimalFormat();
	}
	decF.applyPattern("#.##########");
	return decF;
    }

    /**
     * Method that initializes the elements of the GUI.
     */
    private final void initializeGUI(){
	//initialize mainPanel and its contents
	JPanel mainPanel = new JPanel(new BorderLayout());
	JPanel centerPanel = new JPanel(new BorderLayout());
	JPanel eastPanel = new JPanel(new BorderLayout());
	cipherPanel = new CiphertextPanel();
	centerPanel.add(cipherPanel, BorderLayout.CENTER);
	substitution = new Substitution(locale, collator,
					cipherManager.getCipherAlphabet(),
					new ArrayList());
	substitution.setBorder(BorderFactory.createEtchedBorder());
	centerPanel.add(substitution, BorderLayout.NORTH);
	toolsPane = new CipherToolsPane(substitution,
					cipherManager);
	eastPanel.add(toolsPane, BorderLayout.EAST);
	mainPanel.add(centerPanel, BorderLayout.CENTER);
	mainPanel.add(eastPanel, BorderLayout.EAST);
	JTabbedPane tabbedPanel = new JTabbedPane();
	tabbedPanel.addTab(labelsRB.getString("mainPanel"), mainPanel);
	tabbedPanel.setToolTipTextAt(0, labelsRB.getString("mainPanelTT"));
	//initialize cipherStats
	cipherStatsPanel = new JPanel(new BorderLayout());
	cipherStats = new StatsPanel(cipherManager.getFrequencies(substitution.getNumberOfAlphabets(),
								  substitution.getIgnoredCharacters()),
				     substitution.getNumberOfAlphabets()==1? Substitution.MONOALPHABETIC : Substitution.POLYALPHABETIC);
	cipherStats.setBorder(BorderFactory.createTitledBorder(labelsRB.getString("relFreqBorder")));
	cipherStatsPanel.add(cipherStats, BorderLayout.CENTER);
	addTopPanelToStatsContainer(cipherStatsPanel);
	tabbedPanel.addTab(labelsRB.getString("cipherStats"),
			   cipherStatsPanel);
	tabbedPanel.setToolTipTextAt(1, labelsRB.getString("cipherStatsTT"));
	//add change listeners to Substitution
	substitution.addChangeListener(newSubstitutionChangeListener());
	//add ChangeListener to cipherManager
	cipherManager.addChangeListener(new ChangeListener(){
		public void stateChanged(ChangeEvent e){
		    cipherPanel.setText(cipherManager.getCiphertext(),
					cipherManager.getPlaintext(substitution.getSubstitution()));
		}
	    });
	//add tabbedPanel to the Analyzer
	add(tabbedPanel, BorderLayout.CENTER);
	//initialize the standard frequencies panel and frame
	langStats = new StatsPanel(new LanguageFrequenciesHandler());
	langStatsFrame = new JFrame(labelsRB.getString("langStatsFrame"));
	addTopPanelToStatsContainer(langStatsFrame.getContentPane());
	langStats.setBorder(BorderFactory.createTitledBorder(labelsRB.getString("relFreqBorder")));
	langStatsFrame.getContentPane().add(langStats, BorderLayout.CENTER);
	langStatsFrame.pack();
	// initialize remFromCipherAlphaFrame and cipherAlphaLM
	initRemFromCipherAlphaFrame();
    }

    /**
     * Returns a new <code>ChangeListener</code> to be used in
     * <code>substitution</code>. This <code>ChangeListener</code>
     * handles most of the interaction between the different components
     * of an <code>Analyzer</code>.
     */
    private ChangeListener newSubstitutionChangeListener(){
	ChangeListener substCL = new ChangeListener(){
		public void stateChanged(ChangeEvent e){
		    unsavedChanges = true;
		    SubstitutionEvent se = (SubstitutionEvent)e;
		    byte changeType = se.getChangeType();
		    switch(changeType){
		    case SubstitutionEvent.NUMBER_OF_ALPHABETS:
			updateCipherStats();
			break;
		    case SubstitutionEvent.IGNORED_CHARACTERS:
		    case SubstitutionEvent.CIPHER_ALPHABET:
			updateCiphertextCoincidenceIndex();
		    case SubstitutionEvent.CHARACTER_ADDED_TO_CIPHER_ALPHABET:
			if(changeType != SubstitutionEvent.IGNORED_CHARACTERS){
			    updateCipherAlphaLM();
			}
			updateCipherStatsData();
		    case SubstitutionEvent.CHARACTER_ADDED_TO_PLAIN_ALPHABET:
			updateNumberOfAlphabetsEstimate();
		    }
		    cipherPanel.setText(cipherManager.getCiphertext(),
					cipherManager.getPlaintext(substitution.getSubstitution()));
		}
	    };
	return substCL;
    }

    /**
     * Update the contents of the <code>ListModel</code> that is
     * used to display the cipher alphabet (copy the cipher alphabet
     * from <code>substitution</code> to the <code>ListModel</code>).
     */
    private void updateCipherAlphaLM(){
	cipherAlphaLM.setData(substitution.getCipherAlpha());
    }

    /**
     * Updates the data the <code>StatsPanel</code> that displays the
     * ciphertext's relative frequencies has on its tables.
     */
    private void updateCipherStatsData(){
	java.util.List stats = cipherManager.getFrequencies(substitution.getCipherAlphaMinusIgnoredCharacters());
	int i;
	Iterator iter = stats.iterator();
	i=0;
	while(iter.hasNext()){
	    cipherStats.setTableData((ArrayList)iter.next(), i);
	    i++;
	}
    }

    /**
     * Updates the value of the estimate of the number of alphabets that
     * were used in the cipher to get the cryptogram (displayed in
     * the <code>JTextField</code> <code>numAlphaEstField</code>).
     */
    private void updateNumberOfAlphabetsEstimate(){
	int plainAlphaSize = substitution.getPlainAlpha().size();
	if(langCI>0 && langCI<1 && plainAlphaSize>0 &&
	   plainAlphaSize>=((Set)substitution.getCipherAlphaMinusIgnoredCharacters().get(0)).size()){
	    numAlphaEstField.setText(df.format(cipherManager.getNumberOfAlphabetsEstimate((Set)substitution.getIgnoredCharacters().get(0), langCI, plainAlphaSize)));
	} else{
	    numAlphaEstField.setText(CAN_NOT_ESTIMATE);
	}
    }

    /**
     * Gets the ciphertext's coincidence index and sets it as the value of
     * the <code>JTextField</code> <code>ciphertextCIField</code>.
     */
    private void updateCiphertextCoincidenceIndex(){
	ciphertextCIField.setText(df.format(cipherManager.getCoincidenceIndex((Set)substitution.getIgnoredCharacters().get(0))));
    }

    /**
     * Updates the data the <code>StatsPanel</code> that displays the
     * ciphertext's relative frequencies has, adding or removing tables
     * as needed by the number of alphabets in the substitution.
     */
    private void updateCipherStats(){
	int numAlpha = substitution.getNumberOfAlphabets();
	cipherStats.setData(cipherManager.getFrequencies(substitution.getCipherAlphaMinusIgnoredCharacters()),
			    numAlpha==1 ? Substitution.MONOALPHABETIC : Substitution.POLYALPHABETIC);
    }

    /**
     * Adds a top pannel to the <code>Container</code>s 
     * <code>cipherStatsPanel</code> and the one that contains 
     * <code>langStats</code>. In the case of <code>cipherStatsPanel</code>
     * the top panel contains the ciphertext's coincidence index and
     * the estimated number of alphabets (<code>ciphertextCIField</code> and
     * <code>numAlphaEstField</code>), and if it is the one that contains
     * <code>langStats</code>, <code>langCIField</code>.
     *
     * @param container the <code>Container</code> to add the top panel to
     */
    private void addTopPanelToStatsContainer(Container container){
	JPanel langStatsTopPanel = new JPanel();
	langStatsTopPanel.setLayout(new GridLayout(1, 2, 10, 5));
	langStatsTopPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
	JPanel ciPanel = new JPanel();
	ciPanel.setLayout(new BorderLayout());
	JLabel ciLabel = new JLabel(labelsRB.getString("ciLabel"));
	ciLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 3));
	ciLabel.setToolTipText(labelsRB.getString("ciLabelTT"));
	ciPanel.add(ciLabel, BorderLayout.WEST);
	JTextField ciField = null;
	if(container == cipherStatsPanel){
	    ciphertextCIField = new JTextField(df.format(0));
	    ciField = ciphertextCIField;
	} else{
	    langCIField = new JTextField(df.format(0));
	    ciField = langCIField;
	}
	ciField.setToolTipText(labelsRB.getString("ciLabelTT"));
	ciField.setEditable(false);
	ciField.setHorizontalAlignment(JTextField.RIGHT);
	ciField.setDragEnabled(true); // enable automatic drag handling
	adjustTextFieldSize(ciField);
	ciPanel.add(ciField, BorderLayout.CENTER);
	JPanel estPanel = new JPanel();
	if(container == cipherStatsPanel){
	    estPanel.setLayout(new BorderLayout());
	    JLabel estLabel = new JLabel(labelsRB.getString("estLabel"));
	    estLabel.setToolTipText(labelsRB.getString("estLabelTT"));
	    estLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 3));
	    estPanel.add(estLabel, BorderLayout.WEST);
	    numAlphaEstField = new JTextField(CAN_NOT_ESTIMATE);
	    numAlphaEstField.setToolTipText(labelsRB.getString("estLabelTT"));
	    numAlphaEstField.setEditable(false);
	    numAlphaEstField.setHorizontalAlignment(JTextField.RIGHT);
	    numAlphaEstField.setDragEnabled(true); // automatic drag handling
	    adjustTextFieldSize(numAlphaEstField);
	    estPanel.add(numAlphaEstField, BorderLayout.CENTER);
	}
	langStatsTopPanel.add(ciPanel);
	langStatsTopPanel.add(estPanel);
	container.add(langStatsTopPanel, BorderLayout.NORTH);
    }

    /**
     * Method that changes a <code>JTextField</code>'s preferred size
     * so that a number formatted using the <code>DecimalFormat</code>
     * <code>df</code> can be displayed in its entirety.
     *
     * @param tf the <code>JTextField</code>
     */
    private void adjustTextFieldSize(JTextField tf){
	Dimension prefSize = tf.getPreferredSize();
	prefSize.width = Math.max(SwingUtilities.computeStringWidth(tf.getFontMetrics(tf.getFont()), "##"+df.toPattern()),
				  prefSize.width);
	tf.setPreferredSize(prefSize);
    }

    /**
     * Initializes the <code>JFrame</code> <code>remFromCipherAlphaFrame</code>
     * and the <code>ListModel</code> <code>cipherAlphaLM</code>.<br/>
     * This method is called by <code>initializeGUI()</code>.
     *
     * @see #initializeGUI()
     */
    private final void initRemFromCipherAlphaFrame(){
	remFromCipherAlphaFrame = new JFrame(labelsRB.getString("rmCiAlFrm"));
	cipherAlphaLM = new CipherAlphabetListModel();
	cipherAlphaLM.setData(substitution.getCipherAlpha());
	final JList cipherAlphaJL = new JList(cipherAlphaLM);
	JScrollPane ciphAlScrl; // JScrollPane to put cipherAlphaJL in
	JButton removeBtn = new JButton(labelsRB.getString("removeBtn"));
	removeBtn.setToolTipText(labelsRB.getString("removeBtnTT"));
	removeBtn.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent e){
		    int[] selIndices = cipherAlphaJL.getSelectedIndices();
		    if(selIndices!=null && selIndices.length>0){
			/* make a copy of the current substitution and
			   ignored characters so they can be set back,
			   after changing the ciphertext */
			ArrayList currSubst = substitution.getSubstitution();
			ArrayList currIgn = substitution.getIgnoredCharacters();
			/* the contents of currSubst and currIgn needs to be 
			   cloned so changes made in the MonoAlphaSubts are not
			   reflected by them */
			for(int i=0; i<currSubst.size(); i++){
			    currSubst.set(i, 
					  ((HashMap)currSubst.get(i)).clone());
			}
			for(int i=0; i<currIgn.size(); i++){
			    currIgn.set(i, ((HashSet)currIgn.get(i)).clone());
			}
			// get the characters to remove
			CollationKey[] selChars = cipherAlphaLM.getIndices(selIndices);
			HashSet added = getAddedCharsMinus(selChars);
			setCiphertext(cipherManager.getCiphertextMinus(selChars), false);
			cipherAlphaJL.clearSelection();
			for(Iterator iter=added.iterator(); iter.hasNext(); ){
			    substitution.addCharacterToCipherAlphabet(((CollationKey)iter.next()).getSourceString());
			}
			substitution.setIgnoredCharacters(currIgn);
			substitution.setSubstitution(currSubst);
		    }
		}

		/**
		 * Returns a <code>Set</code> with the characters that were
		 * added to the cipher alphabet using 
		 * <code>Substitution</code>'s 
		 * <code>addCharacterToCipherAlphabet(String)</code> method
		 * and that do not appear in <code>remChars</code>.
		 *
		 * @param remChars the characters to disregard
		 */
		private HashSet getAddedCharsMinus(CollationKey[] remChars){
		    java.util.List lst = Arrays.asList(remChars);
		    HashSet retSet = new HashSet(substitution.getCipherAlpha());
		    retSet.removeAll(lst);
		    retSet.removeAll(cipherManager.getCipherAlphabet());
		    return retSet;
		}
	    });
	ciphAlScrl = new JScrollPane(cipherAlphaJL,
				     JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				     JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
	remFromCipherAlphaFrame.getContentPane().add(ciphAlScrl,
						     BorderLayout.CENTER);
	if(!isSystemMacWithAquaLAF()){
	    remFromCipherAlphaFrame.getContentPane().add(removeBtn,
							 BorderLayout.SOUTH);
	} else{ // leave space for the size control used in Aqua
	    JPanel panel = new JPanel(new BorderLayout());
	    panel.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));
	    panel.add(removeBtn, BorderLayout.CENTER);
	    remFromCipherAlphaFrame.getContentPane().add(panel,
							 BorderLayout.SOUTH);
	}
	remFromCipherAlphaFrame.pack();
    }

    /**
     * Initializes the <code>JFrame</code> <code>aboutFrame</code>, which is
     * used to display information about the program.<br/>
     * This method is called by <code>addHelpMenu(JMenuBar)</code>.
     *
     * @see #addHelpMenu(JMenuBar)
     */
    private void initAboutFrame(){
	if(aboutFrame != null){ //if aboutFrame has already been initialized
	    return;
	}
	aboutFrame = new JFrame(labelsRB.getString("aboutFrame"));
	JPanel contentPanel = new JPanel();
	contentPanel.setLayout(new BoxLayout(contentPanel,
					     BoxLayout.Y_AXIS));
	contentPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
	contentPanel.add(Box.createGlue());
	JLabel logo = new JLabel(LOGO);
	logo.setAlignmentX(Component.CENTER_ALIGNMENT);
	logo.setAlignmentY(Component.CENTER_ALIGNMENT);
	contentPanel.add(logo);
	contentPanel.add(Box.createVerticalStrut(3));
	JLabel progName = new JLabel(labelsRB.getString("progTitl"));
	progName.setFont(new Font("Serif", Font.BOLD, 25));
	progName.setAlignmentX(Component.CENTER_ALIGNMENT);
	progName.setAlignmentY(Component.CENTER_ALIGNMENT);
	contentPanel.add(progName);
	contentPanel.add(Box.createVerticalStrut(3));
	JLabel version = new JLabel(labelsRB.getString("version"));
	version.setFont(new Font("SansSerif", Font.PLAIN, 12));
	version.setAlignmentX(Component.CENTER_ALIGNMENT);
	version.setAlignmentY(Component.CENTER_ALIGNMENT);
	contentPanel.add(version);
	contentPanel.add(Box.createVerticalStrut(10));
	Font sans10 = new Font("SansSerif", Font.PLAIN, 10);
	JLabel info = new JLabel(labelsRB.getString("info"));
	info.setFont(sans10);
	info.setAlignmentX(Component.CENTER_ALIGNMENT);
	info.setAlignmentY(Component.CENTER_ALIGNMENT);
	contentPanel.add(info);
	contentPanel.add(Box.createVerticalStrut(15));
	JLabel copyright = new JLabel(labelsRB.getString("copyright"));
	copyright.setFont(sans10);
	copyright.setAlignmentX(Component.CENTER_ALIGNMENT);
	copyright.setAlignmentY(Component.CENTER_ALIGNMENT);
	contentPanel.add(copyright);
	contentPanel.add(Box.createVerticalStrut(4));
	contentPanel.add(Box.createGlue());
	aboutFrame.getContentPane().add(contentPanel,
					BorderLayout.CENTER);
	aboutFrame.pack();
    }

    /**
     * Returns a <code>JMenuBar</code> that lets the user access some
     * of the <code>Analyzer</code>'s methods.<br/>
     *
     * This method is used to get the <code>JMenuBar</code> that will
     * be plaeced in the <code>JFrame</code> that contains the
     * <code>Analyzer</code>.
     */
    public JMenuBar getMenuBar(){
	JMenuBar menuBar = new JMenuBar();
	addFileMenu(menuBar);
	addEditMenu(menuBar);
	addViewMenu(menuBar);
	addWindowMenu(menuBar);
	addHelpMenu(menuBar);
	return menuBar;
    }

    /**
     * Initializes the <i>File</i> <code>JMenu</code> and its items.
     * Called by <code>getMenuBar()</code>.
     *
     * @param menuBar the <code>JMenuBar</code> to add the file menu to
     * @see #getMenuBar()
     */
    private final void addFileMenu(JMenuBar menuBar){
	cipherChooser = new TextFileChooser();
	XMLFileFilter xmlFilter = new XMLFileFilter();
	cryptanalysisFileChooser = new ConfirmFileChooser();
	cryptanalysisFileChooser.addChoosableFileFilter(xmlFilter);
	cryptanalysisFileChooser.setFileFilter(xmlFilter);
	JMenu fileMenu = new JMenu(labelsRB.getString("fileMenu"));
	fileMenu.setToolTipText(labelsRB.getString("fileMenuTT"));
	JMenuItem openMI = new JMenuItem(labelsRB.getString("openMI"));
	openMI.setToolTipText(labelsRB.getString("openMITT"));
	openMI.setActionCommand("OPEN");
	openMI.addActionListener(this);
	openMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
						     Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
	fileMenu.add(openMI);
	JMenuItem openCipherMI = new JMenuItem(labelsRB.getString("openCipherMI"));
	openCipherMI.setToolTipText(labelsRB.getString("openCipherMITT"));
	openCipherMI.setActionCommand("OPEN_CIPHERTEXT");
	openCipherMI.addActionListener(this);
	fileMenu.add(openCipherMI);
	JMenuItem openLangMI = new JMenuItem(labelsRB.getString("openLangMI"));
	openLangMI.setToolTipText(labelsRB.getString("openLangMITT"));
	openLangMI.setActionCommand("OPEN_LANGUAGE");
	openLangMI.addActionListener(this);
	fileMenu.add(openLangMI);
	fileMenu.add(new JSeparator(SwingConstants.HORIZONTAL));
	JMenuItem saveMI = new JMenuItem(labelsRB.getString("saveMI"));
	saveMI.setToolTipText(labelsRB.getString("saveMITT"));
	saveMI.setActionCommand("SAVE");
	saveMI.addActionListener(this);
	saveMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
						     Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
	fileMenu.add(saveMI);
	JMenuItem saveAsMI = new JMenuItem(labelsRB.getString("saveAsMI"));
	saveAsMI.setToolTipText(labelsRB.getString("saveAsMITT"));
	saveAsMI.setActionCommand("SAVE_AS");
	saveAsMI.addActionListener(this);
	saveAsMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
						       Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | java.awt.event.InputEvent.SHIFT_MASK));
	fileMenu.add(saveAsMI);
	JMenuItem saveCipherMI = new JMenuItem(labelsRB.getString("saveCipherMI"));
	saveCipherMI.setToolTipText(labelsRB.getString("saveCipherMITT"));
	saveCipherMI.setActionCommand("SAVE_CIPHERTEXT");
	saveCipherMI.addActionListener(this);
	fileMenu.add(saveCipherMI);
	JMenuItem savePlainMI = new JMenuItem(labelsRB.getString("savePlainMI"));
	savePlainMI.setToolTipText(labelsRB.getString("savePlainMITT"));
	savePlainMI.setActionCommand("SAVE_PLAINTEXT");
	savePlainMI.addActionListener(this);
	fileMenu.add(savePlainMI);
	if(!isSystemMacWithAquaLAF()){
	    fileMenu.add(new JSeparator(SwingConstants.HORIZONTAL));
	    JMenuItem quitMI = new JMenuItem(labelsRB.getString("quitMI"));
	    quitMI.addActionListener(new ActionListener(){
		    public void actionPerformed(ActionEvent e){
			Window window = SwingUtilities.getWindowAncestor(substitution);
			window.dispatchEvent(new WindowEvent(window,
							     WindowEvent.WINDOW_CLOSING));
		    }
		});
	    quitMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q,
							 Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
	    fileMenu.add(quitMI);
	}
	menuBar.add(fileMenu);
    }

    /**
     * Initializes the <i>Edit</i> <code>JMenu</code> and its items.
     * Called by <code>getMenuBar()</code>.
     *
     * @param menuBar the <code>JMenuBar</code> to add the edit menu to
     * @see #getMenuBar()
     */
    private final void addEditMenu(JMenuBar menuBar){
	JMenu editMenu = new JMenu(labelsRB.getString("editMenu"));
	editMenu.setToolTipText(labelsRB.getString("editMenuTT"));
	JMenuItem copyMI = new JMenuItem(cipherPanel.getCopyAction());
	editMenu.add(copyMI);
	editMenu.add(new JSeparator(SwingConstants.HORIZONTAL));
	JMenuItem addCiMI = new JMenuItem(labelsRB.getString("addCiMI"));
	addCiMI.setToolTipText(labelsRB.getString("addCiMITT"));
	addCiMI.setActionCommand("ADD_TO_CIPHER_ALPHABET");
	addCiMI.addActionListener(this);
	editMenu.add(addCiMI);
	JMenuItem addPlMI = new JMenuItem(labelsRB.getString("addPlMI"));
	addPlMI.setToolTipText(labelsRB.getString("addPlMITT"));
	addPlMI.setActionCommand("ADD_TO_PLAIN_ALPHABET");
	addPlMI.addActionListener(this);
	editMenu.add(addPlMI);
	JMenuItem rmCiMI = new JMenuItem(labelsRB.getString("rmCiMI"));
	rmCiMI.setToolTipText(labelsRB.getString("rmCiMITT"));
	rmCiMI.setActionCommand("REMOVE_FROM_CIPHER_ALPHABET");
	rmCiMI.addActionListener(this);
	editMenu.add(rmCiMI);
	editMenu.add(new JSeparator(SwingConstants.HORIZONTAL));
	JMenuItem groupChars = new JMenuItem(labelsRB.getString("groupChars"));
	groupChars.setToolTipText(labelsRB.getString("groupCharsTT"));
	groupChars.setActionCommand("GROUP_CIPHERTEXT_CHARACTERS");
	groupChars.addActionListener(this);
	editMenu.add(groupChars);
	editMenu.add(new JSeparator(SwingConstants.HORIZONTAL));
	JMenuItem ctUprCase = new JMenuItem(labelsRB.getString("ctUprCase"));
	ctUprCase.setToolTipText(labelsRB.getString("ctUprCaseTT"));
	ctUprCase.setActionCommand("UPPER_CASE_CIPHERTEXT");
	ctUprCase.addActionListener(this);
	editMenu.add(ctUprCase);
	JMenuItem ctLwrCase = new JMenuItem(labelsRB.getString("ctLwrCase"));
	ctLwrCase.setToolTipText(labelsRB.getString("ctLwrCaseTT"));
	ctLwrCase.setActionCommand("LOWER_CASE_CIPHERTEXT");
	ctLwrCase.addActionListener(this);
	editMenu.add(ctLwrCase);
	editMenu.add(new JSeparator(SwingConstants.HORIZONTAL));
	JMenuItem ptAsCt = new JMenuItem(labelsRB.getString("ptAsCt"));
	ptAsCt.setToolTipText(labelsRB.getString("ptAsCtTT"));
	ptAsCt.setActionCommand("SET_PLAINTEXT_AS_CIPHERTEXT");
	ptAsCt.addActionListener(this);
	editMenu.add(ptAsCt);
	menuBar.add(editMenu);
    }

    /**
     * Initializes the <i>View</i> <code>JMenu</code> and its items.
     * Called by <code>getMenuBar()</code>.
     *
     * @param menuBar the <code>JMenuBar</code> to add the view menu to
     * @see #getMenuBar()
     */
    private final void addViewMenu(JMenuBar menuBar){
	JMenu viewMenu = new JMenu(labelsRB.getString("viewMenu"));
	viewMenu.setToolTipText(labelsRB.getString("viewMenuTT"));
	ButtonGroup modeGroup = new ButtonGroup();
	final JRadioButtonMenuItem interMI = new JRadioButtonMenuItem(labelsRB.getString("interMI"));
	interMI.setToolTipText(labelsRB.getString("interMITT"));
	interMI.setSelected(true);
	interMI.setActionCommand("INTERCALATE");
	interMI.addActionListener(cipherPanel);
	final JRadioButtonMenuItem separMI = new JRadioButtonMenuItem(labelsRB.getString("separMI"));
	separMI.setToolTipText(labelsRB.getString("separMITT"));
	separMI.setActionCommand("SEPARATE");
	separMI.addActionListener(cipherPanel);
	cipherPanel.addChangeListener(new ChangeListener(){
		public void stateChanged(ChangeEvent e){
		    switch(cipherPanel.getMode()){
		    case CiphertextPanel.INTERCALATE:
			interMI.setSelected(true);
			break;
		    case CiphertextPanel.SEPARATE:
			separMI.setSelected(true);
			break;
		    default: break;
		    }
		}
	    });
	modeGroup.add(interMI);
	modeGroup.add(separMI);
	viewMenu.add(interMI);
	viewMenu.add(separMI);
	menuBar.add(viewMenu);
    }

    /**
     * Initializes the <i>Window</i> <code>JMenu</code> and its items.
     * Called by <code>getMenuBar()</code>.
     *
     * @param menuBar the <code>JMenuBar</code> to add the window menu to
     * @see #getMenuBar()
     */
    private void addWindowMenu(JMenuBar menuBar){
	JMenu windowMenu = new JMenu(labelsRB.getString("windowMenu"));
	windowMenu.setToolTipText(labelsRB.getString("windowMenuTT"));
	JMenuItem showLangStats = new JMenuItem(labelsRB.getString("showLangStats"));
	showLangStats.setToolTipText(labelsRB.getString("showLangStatsTT"));
	showLangStats.setActionCommand("SHOW_LANG_STATS");
	showLangStats.addActionListener(this);
	windowMenu.add(showLangStats);
	JMenuItem showIgCrs= new JMenuItem(labelsRB.getString("showIgCrs"));
	showIgCrs.setToolTipText(labelsRB.getString("showIgCrsTT"));
	showIgCrs.setActionCommand("SHOW_IGNORED_CHARACTERS");
	showIgCrs.addActionListener(this);
	windowMenu.add(showIgCrs);
	menuBar.add(windowMenu);
    }

    /**
     * Initializes the <i>Help</i> <code>JMenu</code> and its items.
     * Called by <code>getMenuBar()</code>.<br/>
     * Note that if <code>isSystemMacWithAquaLAF()</code> returns 
     * <code>true</code>, the help menu is not added, since it only
     * contains an 'About' menu item and it belongs elsewhere in 
     * Mac OS X.
     *
     * @param menuBar the <code>JMenuBar</code> to add the help menu to
     * @see #getMenuBar()
     */
    private void addHelpMenu(JMenuBar menuBar){
	if(isSystemMacWithAquaLAF()){
	    /* Since the help meny contains just an about menu-item, and that
	     * item is found elsewhere in Mac OS X, don't add the help menu */
	    return;
	}
	JMenu helpMenu = new JMenu(labelsRB.getString("helpMenu"));
	helpMenu.setToolTipText(labelsRB.getString("helpMenuTT"));
	// initialize aboutFrame
	initAboutFrame();
	JMenuItem aboutMI = new JMenuItem(labelsRB.getString("aboutMI"));
	aboutMI.setToolTipText(labelsRB.getString("aboutMITT"));
	aboutMI.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent e){
		    aboutFrame.setVisible(true);
		}
	    });
	helpMenu.add(aboutMI);
	menuBar.add(helpMenu);
    }

    /**
     * Adds a <code>WindowListener</code> that warns the user when he
     * tries to close the window that contains the <code>Analyzer</code> to
     * quit the program and there are unsaved changes. If the 
     * <code>Analyzer</code> does not hava a Window ancestor, the method
     * does nothing.<br/>
     *
     * This method is used to add the <code>WindowListener</code> that will
     * perform the operation that is executed when the user initiates a 
     * "close" on the frame that contains the <code>Analyzer</code>. The
     * listener is not added automatically when the <code>Analyzer</code>
     * is put in a frame, the user of this class must add it by calling this
     * method after the <code>Analyzer</code> has been added to a frame.
     */
    public void addWindowListener(){
	final Window window = SwingUtilities.getWindowAncestor(this);
	if(window == null){
	    return;
	}
	WindowAdapter adapter = new WindowAdapter(){
		public void windowClosing(WindowEvent e){
		    if(areThereUnsavedChanges()){
			int val = JOptionPane.showConfirmDialog(window,
								labelsRB.getString("confSavQt"),
								labelsRB.getString("confSavTitl"),
								JOptionPane.YES_NO_CANCEL_OPTION);
			if(val == JOptionPane.YES_OPTION && saveProject()){
			    System.exit(0);
			}else if(val == JOptionPane.NO_OPTION){
			    System.exit(0);
			}
		    }else{
			System.exit(0);
		    }
		}
	    };
	window.addWindowListener(adapter);
    }

    /**
     * This method is public as an implementation side effect. Do not call or
     * override.<br/>
     * <br/>
     * If the <code>String</code> returned by the <code>ActionEvent</code>'s 
     * <code>getActionCommand()</code> method equals:<br>
     * <ul>
     * <li><code>&quot;OPEN_CIPHERTEXT&quot;</code></li> a 
     * <code>TextFileChooser</code> is displayed and the selected ciphertext
     * file is opened.
     * <li><code>&quot;SAVE_CIPHERTEXT&quot;</code></li> a
     * <code>TextFileChooser</code> is displayed and the ciphertext of the
     * current cryptanalysis project is saved to the selected file.
     * <li><code>&quot;SAVE_PLAINTEXT&quot;</code></li> a
     * <code>TextFileChooser</code> is displayed and the plaintext of the
     * current cryptanalysis project is saved to the selected file.
     * <li><code>&quot;SAVE&quot;</code></li> the current cryptanalysis
     * project is saved to the file if was loaded from or most recently saved
     * to, or if has never been saved, a <code>JFileChooser</code> is opened
     * to let the user select a file to save the project to.
     * <li><code>&quot;SAVE_AS&quot;</code></li> a <code>JFileChooser</code>
     * is displayed and the current cryptanalysis project is saved to the
     * selected file.
     * <li><code>&quot;OPEN&quot;</code></li> a <code>JFileChooser</code>
     * is displayed and the selected cryptanalys project is opened.
     * <li><code>&quot;OPEN_LANGUAGE&quot;</code></li> a 
     * <code>JFileChooser</code> is displayed and the selected language
     * frequencies file is opened.
     * <li><code>&quot;SHOW_LANG_STATS&quot;</code></li> makes the
     * <code>JFrame</code> with the language's relative frequencies visible.
     * <li><code>&quot;SHOW_IGNORED_CHARACTERS&quot;</code></li> makes the
     * <code>JFrame</code> with the selected alphabet's ignored characters
     * be displayed.
     * <li><code>&quot;ADD_TO_CIPHER_ALPHABET&quot;</code></li> displays a
     * dialog that lets the user add characters to the cipher alphabet.
     * <li><code>&quot;ADD_TO_PLAIN_ALPHABET&quot;</code></li> displays a
     * dialog that lets the user add characters to the plain alphabet.
     * <li><code>&quot;UPPER_CASE_CIPHERTEXT&quot;</code></li> changes the
     * ciphertext the user is working with to a version where all the 
     * characters in it have been changed to capital letters.
     * <li><code>&quot;LOWER_CASE_CIPHERTEXT&quot;</code></li> changes the
     * ciphertext the user is working with to a version where all the 
     * characters in it have been changed to lower case letters.
     * <li><code>&quot;REMOVE_FROM_CIPHER_ALPHABET&quot;</code></li> shows
     * the <code>JFrame</code> that contains a <code>JList</code> with all
     * the characters in the cipher alphabet and a <code>JButton</code>
     * that lets the user remove the characters he selected from the
     * ciphertext (and thus, from the cipher alphabet).
     * <li><code>&quot;SET_PLAINTEXT_AS_CIPHERTEXT&quot;</code></li>
     * sets the current projects's plain text as the ciphertext.
     * <li><code>&quot;GROUP_CIPHERTEXT_CHARACTERS&quot;</code></li>
     * shows a <code>JDialog</code> that lets the user choose the number
     * of characters each group must have and grupos the characters in the
     * ciphertext separating them with the character <code>' '</code>
     * </ul>
     *
     * @param e an <code>ActionEvent</code>
     */
    public void actionPerformed(ActionEvent e){
	String actnCmnd = e.getActionCommand();
	if(actnCmnd.equals("OPEN_CIPHERTEXT")){
	    if(areThereUnsavedChanges()){
		int val = JOptionPane.showConfirmDialog(this,
							labelsRB.getString("confSavOpnCT"),
							labelsRB.getString("confSavTitl"),
							JOptionPane.YES_NO_CANCEL_OPTION);
		if(val == JOptionPane.YES_OPTION && saveProject()){
		    openCiphertext();
		}else if(val == JOptionPane.NO_OPTION){
		    openCiphertext();
		}
	    } else{
		openCiphertext();
	    }
	}else if(actnCmnd.equals("SAVE_CIPHERTEXT")){
	    cipherChooser.setDialogTitle(labelsRB.getString("cipherChooserSavCT"));
	    cipherChooser.setSelectedFile(new File(labelsRB.getString("cipherSavFN")));
	    saveText(cipherManager.getCiphertext());
	}else if(actnCmnd.equals("SAVE_PLAINTEXT")){
	    cipherChooser.setDialogTitle(labelsRB.getString("cipherChooserSavPT"));
	    cipherChooser.setSelectedFile(new File(labelsRB.getString("plainSavFN")));
	    saveText(cipherManager.getPlaintext(substitution.getSubstitution()));
	}else if(actnCmnd.equals("SAVE")){
	    saveProject();
	}else if(actnCmnd.equals("SAVE_AS")){
	    save();
	}else if(actnCmnd.equals("OPEN")){
	    if(areThereUnsavedChanges()){
		int val = JOptionPane.showConfirmDialog(this,
							labelsRB.getString("confSavOpnPj"),
							labelsRB.getString("confSavTitl"),
							JOptionPane.YES_NO_CANCEL_OPTION);
		if(val == JOptionPane.YES_OPTION && saveProject()){
		    open();
		}else if(val == JOptionPane.NO_OPTION){
		    open();
		}
	    } else{
		open();
	    }
	}else if(actnCmnd.equals("OPEN_LANGUAGE")){
	    openLanguage();
	}else if(actnCmnd.equals("SHOW_LANG_STATS")){
	    langStatsFrame.setVisible(true);
	}else if(actnCmnd.equals("SHOW_IGNORED_CHARACTERS")){
	    substitution.showIgnoredCharacters();
	}else if(actnCmnd.equals("ADD_TO_CIPHER_ALPHABET")){
	    if(charAddDlg == null){
		charAddDlg = new CharacterAdditionDialog(this);
	    }
	    charAddDlg.setMode(CharacterAdditionDialog.ADD_TO_CIPHER_ALPHABET);
	    charAddDlg.setLocationRelativeTo(this);
	    charAddDlg.setVisible(true);
	}else if(actnCmnd.equals("ADD_TO_PLAIN_ALPHABET")){
	    if(charAddDlg == null){
		charAddDlg = new CharacterAdditionDialog(this);
	    }
	    charAddDlg.setMode(CharacterAdditionDialog.ADD_TO_PLAIN_ALPHABET);
	    charAddDlg.setLocationRelativeTo(this);
	    charAddDlg.setVisible(true);
	}else if(actnCmnd.equals("UPPER_CASE_CIPHERTEXT")){
	    String upr = cipherManager.getCiphertext().toUpperCase(locale);
	    if(!upr.equals(cipherManager.getCiphertext())){
		setCiphertext(upr, false);
	    }
	}else if(actnCmnd.equals("LOWER_CASE_CIPHERTEXT")){
	    String lwr = cipherManager.getCiphertext().toLowerCase(locale);
	    if(!lwr.equals(cipherManager.getCiphertext())){
		setCiphertext(lwr, false);
	    }
	}else if(actnCmnd.equals("REMOVE_FROM_CIPHER_ALPHABET")){
	    remFromCipherAlphaFrame.setVisible(true);
	}else if(actnCmnd.equals("SET_PLAINTEXT_AS_CIPHERTEXT")){
	    setCiphertext(cipherManager.getPlaintext(substitution.getSubstitution()), true);
	}else if(actnCmnd.equals("GROUP_CIPHERTEXT_CHARACTERS")){
	    int blkSize = substitution.getNumberOfAlphabets();
	    blkSize = blkSize == 1 ? 5 : blkSize;
	    blkSize = SpinnerDialog.showSpinnerDialog(this, labelsRB.getString("groupCharsDlgMsg"), blkSize, labelsRB.getString("groupCharsDlgTtl"));
	    if(blkSize > 0){
		cipherManager.setCiphertextInBlocksOf(blkSize);
	    }
	}
    }

    /**
     * Method that displays the <code>TextFileChooser</code> 
     * <code>cipherChooser</code> and lets the user open a text file
     * as the ciphertext.
     *
     * @return <code>true</code> if the ciphertext was opened and
     *         <code>false</code> otherwise
     */
    private final void openCiphertext(){
	final boolean[] openedCipher = {false}; // to check if it was opened 
	final StringBuffer ctBuff = new StringBuffer(); //to store the text
	cipherChooser.setSelectedFile(new File("")); //clear selected file
	cipherChooser.setDialogTitle(labelsRB.getString("cipherChooserOpn"));
	int retVal = cipherChooser.showOpenDialog(this);
	if(retVal != TextFileChooser.APPROVE_OPTION){
	    return;
	}
	final File source = cipherChooser.getSelectedFile();
	Runnable loadTextFile = new Runnable(){
		public void run(){
		    Exception ex = null;
		    /* message to be displayed in a dialog if an exception
		       occurs */
		    String exMessage = null;
		    String exTitle = null; // the dialog's title
		    try{
			if(!source.exists()){
			    throw new FileNotFoundException();
			}else if(!source.canRead()){
			    throw new SecurityException();
			}
			FileInputStream fis = new FileInputStream(source);
			InputStreamReader isr = new InputStreamReader(fis,
								      cipherChooser.getEncoding());
			BufferedReader in = new BufferedReader(isr);
			try{
			    int charRead = in.read();
			    while(charRead!=-1){
				ctBuff.append((char)charRead);
				charRead = in.read();
			    }
			    in.close();
			    isr.close();
			    fis.close();
			} catch(IOException ioe){
			    ex = ioe;
			    exMessage = replace("FN", source.getName(),
						labelsRB.getString("ieText"));
			    exTitle = labelsRB.getString("ieTitle");
			}
		    }catch(SecurityException se){
			ex = se;
			exMessage = replace("FN", source.getName(),
					    labelsRB.getString("seText"));
			exTitle = labelsRB.getString("seTitle");
		    } catch(FileNotFoundException fnfe){
			ex = fnfe;
			exMessage = replace("FN", source.getName(),
					    labelsRB.getString("fnfeText"));
			exTitle = labelsRB.getString("fnfeTitle");
		    } catch(UnsupportedEncodingException uee){
			ex = uee;
			exMessage = labelsRB.getString("ueeText") + "\n\""+ 
			            cipherChooser.getEncoding() +"\"";
			exTitle = labelsRB.getString("ueeTitle");
		    } finally{
			if(ex != null){
			    showErrorMessageDialog(exMessage, exTitle);
			} else{
			    openedCipher[0] = true;
			}
		    }
		}
	    };
	Runnable loadNewCiphertext = new Runnable(){
		public void run(){
		    if(openedCipher[0]){
			setCiphertext(ctBuff.toString(), true);
			savedProject = null;
			unsavedChanges = false;
			setProjectNameOnFrameTitle(savedProject);
		    }
		}
	    };
	LoadWorker worker = new LoadWorker(loadTextFile,
					   loadNewCiphertext,
					   this);
	worker.start();
    }

    /**
     * Method that displays the <code>TextFileChooser</code> 
     * <code>cipherChooser</code> and lets the user save <code>txt</code>
     * to a text file.
     *
     * @param txt a <code>String</code>
     * @throws NullPointerException if <code>txt</code> is <code>null</code>
     */
    public void saveText(String txt) throws NullPointerException{
	int retVal = cipherChooser.showSaveDialog(this);
	if(retVal != TextFileChooser.APPROVE_OPTION){
	    return;
	}
	File dest = cipherChooser.getSelectedFile();
	File destParent = dest.getParentFile();
	Exception ex = null; // != null if an exception occurred
	// message to be displayed in a dialog if an exception occurs
	String exMessage = null;
	String exTitle = null;
	try{
	    if(dest.exists()){
		if(!dest.canWrite()){
		    throw new SecurityException();
		}
	    }else if(destParent!=null && !destParent.canWrite()){
		throw new SecurityException();
	    }
	    FileOutputStream fos = new FileOutputStream(dest);
	    OutputStreamWriter osw = new OutputStreamWriter(fos,
							    cipherChooser.getEncoding());
	    BufferedWriter out = new BufferedWriter(osw);
	    try{
		out.write(txt);
		out.flush();
		out.close();
		osw.close();
		fos.close();
	    } catch(IOException ioe){
		ex = ioe;
		exMessage = replace("FN", dest.getName(),
				    labelsRB.getString("oeText"));
		exTitle = labelsRB.getString("oeTitle");
	    }
	}catch(SecurityException se){
	    ex = se;
	    exMessage = replace("FN", dest.getName(),
				labelsRB.getString("seWrtText"));
	    exTitle = labelsRB.getString("seWrtTitle");
	}catch(FileNotFoundException fnfe){ // thrown by FileOutputStream
	    ex = fnfe;
	    exMessage = replace("FN", dest.getName(),
				labelsRB.getString("fnfeWrtText"));
	    exTitle = labelsRB.getString("fnfeWrtTitle");
	}catch(UnsupportedEncodingException uee){
	    ex = uee;
	    exMessage = labelsRB.getString("ueeText") + "\n\""+ 
		        cipherChooser.getEncoding() +"\"";
	    exTitle = labelsRB.getString("ueeTitle");
	}finally{
	    if(ex != null){
		showErrorMessageDialog(exMessage, exTitle);
	    }
	}
    }

    /**
     * Saves the current cryptanalysis project to its file (where it was
     * last saved to/opened from), or opens a file chooser to let the user
     * select a file to save the cryptanalysis project to and then saves it.
     *
     * @return <code>true</code> if the project was successfully saved and 
     *         <code>false</code> otherwise
     * @see #save()
     * @see #save(File)
     */
    public boolean saveProject(){
	boolean savedRet = false; // return value
	if(savedProject == null){
	    savedRet = save();
	}else{
	    savedRet = save(savedProject);
	}
	return savedRet;
    }

    /**
     * Saves the current cryptanalysis project to the file <code>dest</code>.
     * The cryptanalysis project files are instances of
     * <code>Cryptanalysis.xsd</code>. If there are any problems writing
     * the file, a dialog with a description of the error 
     * (<code>IOException</code> or <code>SecurityException</code>) will 
     * be opened and <code>false</code> will be returned.
     *
     * @param dest the file to save the cryptanalysis project to
     * @return <code>true</code> if the cryptanalysis project was successfully
     *         saved to <code>dest</code> and <code>false</code> otherwise
     */
    public boolean save(File dest){
	boolean successful = false;
	File destParent = dest.getParentFile();
	Exception ex = null; // != null if an exception occurs
	// message to be displayed in a dialog if an exception occurs
	String exMessage = null;
	String exTitle = null; // the dialog's title
	try{
	    if(dest.exists()){
		if(!dest.canWrite()){
		    throw new SecurityException();
		}
	    }else if(destParent!=null && !destParent.canWrite()){
		throw new SecurityException();
	    }
	    FileOutputStream fos = new FileOutputStream(dest);
	    OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF8");
	    BufferedWriter out = new BufferedWriter(osw);
	    try{
		/* Write the cryptanalysis project file avoiding the use of
		   the String operator '+' */
		out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<cryptanalysis xmlns=\"");
		out.write(CryptanalysisHandler.NAMESPACE);
		out.write("\"\n               xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n               xsi:schemaLocation=\"");
		out.write(CryptanalysisHandler.NAMESPACE);
		out.write("\n                                   ");
		out.write((new File(findSchemataDir().getCanonicalPath(),
				    AnalyzerConstants.CRYPTANALYSIS_SCHEMA_FILE)).toURI().toString());
		out.write("\"\n               cipher=\"");
		out.write(toolsPane.getCipher());
		out.write("\"\n               language=\"");
		out.write(locale.getLanguage());
		if(!locale.getCountry().equals("")){
		    out.write("\" country=\"");
		    out.write(locale.getCountry());
		    out.write("\" >\n");
		} else{
		    out.write("\" >\n");
		}
		out.write(" <ciphertext>");
		out.write(toXMLString(cipherManager.getCiphertext()));
		out.write("</ciphertext>\n");
		out.write(" <cipherAlphabet>\n");
		for(Iterator iter=substitution.getCipherAlpha().iterator();
		    iter.hasNext(); ){
		    out.write("  <character char=\"");
		    out.write(toXMLString(((CollationKey)iter.next()).getSourceString()));
		    out.write("\" />\n");
		}
		out.write(" </cipherAlphabet>\n");
		out.write(" <plainAlphabet>\n");
		for(Iterator iter=substitution.getPlainAlpha().iterator();
		    iter.hasNext(); ){
		    out.write("  <character char=\""); 
		    out.write(toXMLString(((CollationKey)iter.next()).getSourceString()));
		    out.write("\" />\n");
		}
		out.write(" </plainAlphabet>\n");
		out.write(" <substitution>\n");
		assert !substitution.areIgnoredIndependent() :
		    "substitution's ignored characters must not be " +
		    "independent";
		Iterator ignoreIter = ((HashSet)substitution.getIgnoredCharacters().get(0)).iterator();
		if(ignoreIter.hasNext()){
		    out.write("  <ignore>\n");
		    while(ignoreIter.hasNext()){
			out.write("   <character char=\"");
			out.write(toXMLString(((CollationKey)ignoreIter.next()).getSourceString()));
			out.write("\" />\n");
		    }
		    out.write("  </ignore>\n");
		}
		Iterator substIter = substitution.getSubstitution().iterator();
		while(substIter.hasNext()){
		    out.write("  <alphabet>\n");
		    HashMap subHM = (HashMap)((HashMap)substIter.next()).clone();
		    // remove those that have a null substitution
		    for(Iterator iter=subHM.keySet().iterator();
			iter.hasNext(); ){
			if(subHM.get(iter.next()) == null){
			    iter.remove();
			}
		    }
		    if(!subHM.isEmpty()){
			out.write("   <replace>\n");
			for(Iterator iter=subHM.keySet().iterator();
			    iter.hasNext(); ){
			    CollationKey key = (CollationKey)iter.next();
			    out.write("    <occurrences ofChar=\"");
			    out.write(key.getSourceString());
			    out.write("\" byChar=\"");
			    out.write(toXMLString(((CollationKey)subHM.get(key)).getSourceString()));
			    out.write("\" />\n");
			}
			out.write("   </replace>\n");
		    }
		    out.write("  </alphabet>\n");
		}
		out.write(" </substitution>\n");
		if(freqsH != null){
		    out.write(" <languageFrequencies language=\"");
		    out.write(freqsH.getLocale().getLanguage());
		    if(!freqsH.getLocale().getCountry().equals("")){
			out.write("\" country=\"");
			out.write(freqsH.getLocale().getCountry());
			out.write("\"\n");
		    } else{
			out.write("\"\n");
		    }
		    out.write("                      source=\"");
		    out.write(freqsH.getSource());
		    out.write("\"\n");
		    out.write("                      rules=\"");
		    out.write(freqsH.getRules());
		    out.write("\" >\n");
		    out.write("  <alphabet>\n");
		    StringFreq tmpSF;
		    for(Iterator iter=freqsH.getAlphabet().iterator(); 
			iter.hasNext(); ){
			tmpSF = (StringFreq)iter.next();
			out.write("   <character char=\"");
			out.write(toXMLString(tmpSF.getString()));
			out.write("\" frequency=\"");
			out.write(Integer.toString(tmpSF.getFrequency()));
			out.write("\" />\n");
		    }
		    out.write("  </alphabet>\n  <bigrams>\n");
		    for(Iterator iter=freqsH.getBigrams().iterator();
			iter.hasNext(); ){
			tmpSF = (StringFreq)iter.next();
			out.write("   <bigram sequence=\"");
			out.write(toXMLString(tmpSF.getString()));
			out.write("\" frequency=\"");
			out.write(Integer.toString(tmpSF.getFrequency()));
			out.write("\" />\n");
		    }
		    out.write("  </bigrams>\n  <trigrams>\n");
		    for(Iterator iter=freqsH.getTrigrams().iterator();
			iter.hasNext(); ){
			tmpSF = (StringFreq)iter.next();
			out.write("   <trigram sequence=\"");
			out.write(toXMLString(tmpSF.getString()));
			out.write("\" frequency=\"");
			out.write(Integer.toString(tmpSF.getFrequency()));
			out.write("\" />\n");
		    }
		    out.write("  </trigrams>\n");
		    out.write(" </languageFrequencies>\n");
		}
		out.write("</cryptanalysis>\n");
		out.flush();
		out.close();
		osw.close();
		fos.close();
	    } catch(IOException ioe){
		ex = ioe;
		exMessage = replace("FN", dest.getName(),
				    labelsRB.getString("oeText"));
		exTitle = labelsRB.getString("oeTitle");
	    }
	}catch(SecurityException se){
	    ex = se;
	    exMessage = replace("FN", dest.getName(),
				labelsRB.getString("seWrtText"));
	    exTitle = labelsRB.getString("seWrtTitle");
	}catch(FileNotFoundException fnfe){ // thrown by FileOutputStream
	    ex = fnfe;
	    exMessage = replace("FN", dest.getName(),
				labelsRB.getString("fnfeWrtText"));
	    exTitle = labelsRB.getString("fnfeWrtTitle");
	}catch(UnsupportedEncodingException uee){
	    /* Impossible since every implementation of the Java 
	       platform is required to support UTF8 
	       Note: UTF8 is the name of the charset in Java's basic
	       encoding set, and not UTF-8 */
	    ex = uee;
	    exMessage = labelsRB.getString("ueeText") + "\n\"UTF-8\"";
	    exTitle = labelsRB.getString("ueeTitle");
	    assert false : "UTF8 is not supported by this implementation "+
		           "of the Java platform";
	} catch(NullPointerException npe){ // if the schemata were not found
	    ex = npe;
	    exMessage = labelsRB.getString("npeText");
	    exTitle = labelsRB.getString("npeTitle");
	}finally{
	    if(ex != null){
		showErrorMessageDialog(exMessage, exTitle);
	    }else{
		successful = true;
		unsavedChanges = false;
	    }
	}
	return successful;
    }

     /**
     * Opens a file chooser to let the user select a file to save the
     * cryptanalysis project to and then saves it. The cryptanalysis project
     * files are instances of <code>Cryptanalysis.xsd</code>. If there are
     * any problems writing the file, a dialog with a description of the error 
     * (<code>IOException</code> or <code>SecurityException</code>) will 
     * be opened and <code>false</code> will be returned.
     *
     * @return <code>true</code> if the cryptanalysis project was successfully
     *         saved to a file and <code>false</code> otherwise
     * @see #save(File)
     */
    public boolean save(){
	boolean savedRet = false; // return value
	if(savedProject == null){
	    cryptanalysisFileChooser.setSelectedFile(new File(labelsRB.getString("cryptanSavFN")));
	}else{
	    cryptanalysisFileChooser.setSelectedFile(savedProject);
	}
	cryptanalysisFileChooser.setDialogTitle(labelsRB.getString("cryptanalysisChooserSav"));
	int retVal = cryptanalysisFileChooser.showSaveDialog(this);
	if(retVal == JFileChooser.APPROVE_OPTION){
	    savedRet = save(cryptanalysisFileChooser.getSelectedFile());
	    if(savedRet){
		savedProject = cryptanalysisFileChooser.getSelectedFile();
		unsavedChanges = false;
		setProjectNameOnFrameTitle(savedProject);
	    }
	}
	return savedRet;
    }

    /**
     * Returns a string with the contents of <code>str</code> that
     * can be used in a string element of an XML document, i.e. transforms
     * all the special or control characters to predefined entities 
     * and Unicode character references respectively, e.g. <code>"<\n>"</code>
     * becomes <code>"&amp;lt;&amp;#10;&amp;gt;"</code>, <code>"&amp;"</code>
     * bemodes <code>"&amp;amp;"</code>
     *
     * @param str a <code>String</code>
     */
    private static String toXMLString(String str){
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

    /**
     * Opens a file chooser to let the user select a file to open a
     * cryptanalysis project from (an instance of
     * <code>Cryptanalysis.xsd</code>). If there is any problem parsing the
     * file (if it is not a valid instance of <code>Cryptanalysis.xsd</code>)
     * or reading the file (<code>FileNotFoundException</code>,
     * <code>SecurityException</code> or </code>IOException</code>), a dialog
     * with a description of the error will be opened and the current project
     * will remain unchanged.
     *
     * @return the <code>File</code> that the user opened or <code>null</code>
     *         if no file was opened.
     */
    private final void open(){
	cryptanalysisFileChooser.setSelectedFile(new File(""));
	cryptanalysisFileChooser.setDialogTitle(labelsRB.getString("cryptanalysisChooserOpn"));
	int retVal = cryptanalysisFileChooser.showOpenDialog(this);
	if(retVal != JFileChooser.APPROVE_OPTION){
	    return;
	}
	final File cryptanF = cryptanalysisFileChooser.getSelectedFile();
	Runnable parseFile = new Runnable(){
		public void run(){
		    parseXML(cryptanF, CRYPTANALYSIS_PROJECT);
		}
	    };
	Runnable loadNewCryptProject = new Runnable(){
		public void run(){
		    if(cryptHan == null){
			return;
		    }
		    setCiphertext("", true);//avoid extra work setting language
		    freqsH = cryptHan.getLanguageFrequencies();
		    if(freqsH != null){
		    langStats.setData(freqsH);
		    }else{
			langStats.setData(new LanguageFrequenciesHandler());
		    }
		    locale = cryptHan.getLocale();
		    collator = cryptHan.getCollator();
		    cipherManager.setLocale(locale);
		    cipherManager.setCollator(collator);
		    substitution.setPlainAlpha(locale, collator, 
					       cryptHan.getLangAlphabetCK());
		    cipherStats.setData(cipherManager.getFrequencies(substitution.getCipherAlphaMinusIgnoredCharacters()),
					substitution.getNumberOfAlphabets()==1? Substitution.MONOALPHABETIC : Substitution.POLYALPHABETIC);
		    // set the language coincidence index
		    langCI = cryptHan.getCoincidenceIndex();
		    langCIField.setText(langCI<=0 ? df.format(0) : df.format(langCI));
		    if(langCI>0 && substitution.getPlainAlpha().size()>=((Set)substitution.getCipherAlphaMinusIgnoredCharacters().get(0)).size()){
			numAlphaEstField.setText(df.format(cipherManager.getNumberOfAlphabetsEstimate((Set)substitution.getIgnoredCharacters().get(0), langCI, substitution.getPlainAlpha().size())));
		    } else{
			numAlphaEstField.setText(CAN_NOT_ESTIMATE);
		    }
		    // Display the language in langStatsFrame's title bar
		    setLangStatsFrameTitle(freqsH==null ? null : locale);
		    // Add the characters that are in the cipher alphabet but
		    // not among those in the ciphertext
		    ArrayList subst = cryptHan.getSubstitution();
		    HashSet cipherAl = cryptHan.getCipherAlphabet();
		    HashSet plainAl = cryptHan.getPlainAlphabet();
		    toolsPane.setCipher(cryptHan.getCipher());
		    setCiphertext(cryptHan.getCiphertext(), true);
		    toolsPane.setNumberOfAlphabets(subst.size());
		    for(Iterator iter=substitution.getCipherAlpha().iterator();
			iter.hasNext(); ){
			cipherAl.remove(iter.next());
		    }
		    for(Iterator iter=cipherAl.iterator(); iter.hasNext(); ){
			substitution.addCharacterToCipherAlphabet(((CollationKey)iter.next()).getSourceString());
		    }
		    // Add the characters that are in the plain alphabet but
		    // not among those in the alphabet of the language frequencies
		    for(Iterator iter=substitution.getPlainAlpha().iterator();
			iter.hasNext(); ){
			plainAl.remove(iter.next());
		    }
		    for(Iterator iter=plainAl.iterator(); iter.hasNext(); ){
			substitution.addCharacterToPlainAlphabet(((CollationKey)iter.next()).getSourceString());
		    }
		    // set the ignored characters and substitution
		    substitution.setIgnoredCharacters(cryptHan.getIgnored());
		    substitution.setInjective(cryptHan.getSubstInjective());
		    substitution.setSubstitution(subst);
		    savedProject = cryptanF;
		    unsavedChanges = false;
		    setProjectNameOnFrameTitle(savedProject);
		}
	    };
	LoadWorker worker = new LoadWorker(parseFile,
					   loadNewCryptProject,
					   this);
	worker.start();
    }

    /**
     * Method used to parse instances of the XML schemata
     * <code>LanguageFrequencies.xsd</code> and <code>Cryptanalysis.xsd</code>
     * when loading the language frequencies or a cryptanalysis project
     * respectively. When loading language frequencies, the resulting
     * <code>LanguageFrequenciesHandler</code> will be stored in
     * <code>freqsH_tmp</code> and when loading a cryptanalysis project the
     * resulting <code>CryptanalysisHandler</code> will be stored in
     * <code>cryptHan</code>. If an <code>Exception</code> is thrown while
     * parsing, a dialog with a description of the problem will be shown and
     * the resulting <code>DefaultHandler</code> will be <code>null</code>.
     *
     * @param document an XML document
     * @param type the type of document that wants to be parsed 
     *             (<code>LANGUAGE_FREQUENCIES</code> or 
     *              <code>CRYPTANALYSIS_PROJECT</code>)
     * @throws IllegalArgumentException if <code>type</code> is not 
     *                                  <code>LANGUAGE_FREQUENCIES</code> or
     *                                  <code>CRYPTANALYSIS_PROJECT</code>
     */
    private void parseXML(File document,
			  byte type)      throws IllegalArgumentException
    {
	Exception ex = null;
	// message to be displayed in a dialog if an exception occurs
	String exMessage = null;
	// message to be displayed in the dialog's title if an exception occurs
	String exTitle = null;
	try{
	    switch(type){
	    case LANGUAGE_FREQUENCIES:
		freqsH_tmp = null;
		freqsH_tmp = parseLangFreqs(document);
		break;
	    case CRYPTANALYSIS_PROJECT:
		cryptHan = null;
		cryptHan = parseCryptanalysis(document);
		break;
	    default:
		throw new IllegalArgumentException();
	    }
	}catch(SAXNotRecognizedException nre){
	    ex = nre;
	    String msg = ex.getMessage();
	    if(msg.indexOf(AnalyzerConstants.JAXP_SCHEMA_LANGUAGE)>=0){
		exMessage = labelsRB.getString("nreLangText");
		exTitle = labelsRB.getString("nreTitle");
	    }else if(msg.indexOf(AnalyzerConstants.JAXP_SCHEMA_SOURCE)>=0){
		exMessage = labelsRB.getString("nreSrcText");
		exTitle = labelsRB.getString("nreTitle");
	    }else{
		exMessage = labelsRB.getString("nreUnkText");
		exTitle = labelsRB.getString("nreTitle");
	    }
	    nre.printStackTrace(System.err);
	}catch(SAXParseException pe){
	    ex = pe;
	    StringBuffer msgBuf = new StringBuffer(labelsRB.getString("peText"));
	    replace("LN", Integer.toString(pe.getLineNumber()), msgBuf);
	    replace("FN", document.getName(), msgBuf);
	    exMessage = msgBuf.toString();
	    exTitle = labelsRB.getString("peTitle");
	    pe.printStackTrace(System.err);
	}catch(SAXException sxe){
	    ex = sxe;
	    exMessage = replace("FN", document.getName(),
				labelsRB.getString("sxeText"));
	    exTitle = labelsRB.getString("sxeTitle");
	    sxe.printStackTrace(System.err);
	}catch(ParserConfigurationException pce){
	    ex = pce;
	    exMessage = labelsRB.getString("nreUnkText");
	    exTitle = labelsRB.getString("pceTitle");
	    pce.printStackTrace(System.err);
	}catch(SecurityException se){
	    ex = se;
	    exMessage = replace("FN", document.getName(), 
				labelsRB.getString("seText"));
	    exTitle = labelsRB.getString("seTitle");
	}catch(FileNotFoundException fnfe){
	    ex = fnfe;
	    exMessage = replace("FN", document.getName(),
				labelsRB.getString("fnfeText"));
	    exTitle = labelsRB.getString("fnfeTitle");
	}catch(IOException ioe){
	    ex = ioe;
	    exMessage = replace("FN", document.getName(),
				labelsRB.getString("ieText"));
	    exTitle = labelsRB.getString("ieTitle");
	} catch(NullPointerException npe){ // if the schemata were not found
	    ex = npe;
	    exMessage = labelsRB.getString("npeText");
	    exTitle = labelsRB.getString("npeTitle");
	}finally{
	    if(ex != null){
		showErrorMessageDialog(exMessage, exTitle);
	    }
	}
    }

    /**
     * Method that displays an error message in a dialog. May be called
     * from any thread.
     *
     * @param message the error message
     * @param title the title of the dialog
     */
    private void showErrorMessageDialog(final String message, 
					final String title){
	final Analyzer thisAnalyzer = this;
	if(SwingUtilities.isEventDispatchThread()){
	    JOptionPane.showMessageDialog(thisAnalyzer,
					  message,
					  title,
					  JOptionPane.ERROR_MESSAGE);
	}else{
	    SwingUtilities.invokeLater(new Runnable(){
		    public void run(){
			JOptionPane.showMessageDialog(thisAnalyzer, 
						      message,
						      title,
						      JOptionPane.ERROR_MESSAGE);
		    }
		});
	}
    }

    /**
     * Parses an instance of <code>Cryptanalysis.xsd</code>
     * and returns the <code>CryptanalysisHandler</code> used to parse it.
     *
     * @param crypt an instance of <code>Cryptanalysis.xsd</code>
     * @throws SAXNotRecognizedException if W3C schemas or a schema source are not supported by the version of JAXP the user has.
     * @throws SAXParseException if there was an error or warning while parsing the instance of <code>Cryptanalysis.xsd</code>
     * @throws SAXException if there was an error or warning while parsing the instance of <code>Cryptanalysis.xsd</code>
     * @throws ParserConfigurationException if a serious configuration error took place (the features needed to validate using a W3C schema are not present).
     * @throws SecurityException if <code>crypt</code> cannot be read due to security constrains
     * @throws FileNotFoundException if <code>crypt</code> could not be found
     * @throws IOException if an I/O exception of some sort occurred
     * @throws NullPointerException if <code>crypt</code> is <code>null</code>
     */
    private final CryptanalysisHandler parseCryptanalysis(File crypt)
	throws SAXNotRecognizedException, SAXParseException,
	       SAXException, ParserConfigurationException,
	       SecurityException, FileNotFoundException, IOException,
	       NullPointerException
    {
	if(crypt == null){
	    throw new NullPointerException();
	}else if(!crypt.exists()){
	    throw new FileNotFoundException("File not found");
	}else if(!crypt.canRead()){
	    throw new SecurityException("Permission denied");
	}
	CryptanalysisHandler handler = new CryptanalysisHandler();
	// Use the validating namespace aware parser
	SAXParserFactory saxFactory = SAXParserFactory.newInstance();
	saxFactory.setValidating(true);
	saxFactory.setNamespaceAware(true);
	// Parse the input
	SAXParser saxParser = saxFactory.newSAXParser();
	// use W3C schemata
	saxParser.setProperty(AnalyzerConstants.JAXP_SCHEMA_LANGUAGE, 
			      AnalyzerConstants.W3C_XML_SCHEMA);
	// Set schema source
	String schemataPath = findSchemataDir().getCanonicalPath() +
	                      System.getProperty("file.separator");
	saxParser.setProperty(AnalyzerConstants.JAXP_SCHEMA_SOURCE,
			      new File(schemataPath +
				       AnalyzerConstants.CRYPTANALYSIS_SCHEMA_FILE));
	FileInputStream cryptIS = new FileInputStream(crypt);
	saxParser.parse(cryptIS, handler, schemataPath);
	cryptIS.close();
	return handler;
    }

    /**
     * Sets the current ciphertext to the string passed. This method
     * also sets the statistics and can show the new document from the top.
     *
     * @param ciphertext the <code>String</code> to use as ciphertext
     * @param resetView <code>true</code> if the new document should be displayed
     *                  from the top, and <code>false</code> to try preserve the
     *                  current view position
     */
    private final void setCiphertext(String ciphertext, 
				     boolean resetView)
    {
	if(resetView){ 	//show the new document from the top
	    cipherPanel.viewTop();
	}
	// cipherManager's  ChangeListener updates cipherPanel
	cipherManager.setCiphertext(ciphertext);
	substitution.setCipherAlpha(cipherManager.getCipherAlphabet());
	int numAlpha = substitution.getNumberOfAlphabets();
	cipherStats.setData(cipherManager.getFrequencies(substitution.getCipherAlphaMinusIgnoredCharacters()),
			    numAlpha==1 ? Substitution.MONOALPHABETIC : Substitution.POLYALPHABETIC);
    }

    /**
     * Method that displays the <code>JFileChooser</code> 
     * <code>langChooser</code> and lets the user open an instance
     * of the XML schema <code>LanguageFrequencies.xsd</code>.<br/>
     *
     * If the instance is valid, the current <code>locale</code> and
     * <code>collator</code> change accordingly. The affected member
     * variables are <code>language</code>, <code>collator</code>,
     * <code>langStats</code>, <code>cipherManager</code>,
     * <code>cipherStats</code> y <code>substitution</code>
     */
    private final void openLanguage(){
	if(langChooser == null){
	    initLangChooser();
	}
	int retVal = langChooser.showOpenDialog(this);
	if(retVal != JFileChooser.APPROVE_OPTION){
	    return;
	}
	final File freqs = langChooser.getSelectedFile();
	// non-GUI related task (parse the XML file)
	Runnable parseFile = new Runnable(){
		public void run(){
		    parseXML(freqs, LANGUAGE_FREQUENCIES);
		}
	    };
	/* GUI related task (check if the file was parsed, if so, set its
	   contents as the current language frequencies, update the locale,
	   collators, etc. and the GUI */
	Runnable loadNewLangFreqs = new Runnable(){
		public void run(){
		    if(freqsH_tmp == null){
			return;
		    }
		    freqsH = freqsH_tmp;
		    langStats.setData(freqsH);
		    locale = freqsH.getLocale();
		    collator = freqsH.getCollator();
		    cipherManager.setLocale(locale);
		    cipherManager.setCollator(collator);
		    substitution.setPlainAlpha(locale, collator, 
					       freqsH.getAlphabetCK());
		    cipherStats.setData(cipherManager.getFrequencies(substitution.getCipherAlphaMinusIgnoredCharacters()),
					substitution.getNumberOfAlphabets()==1? Substitution.MONOALPHABETIC : Substitution.POLYALPHABETIC);
		    // set the language coincidence index
		    langCI = freqsH.getCoincidenceIndex();
		    langCIField.setText(df.format(langCI));
		    if(langCI>0 && substitution.getPlainAlpha().size()>=((Set)substitution.getCipherAlphaMinusIgnoredCharacters().get(0)).size()){
			numAlphaEstField.setText(df.format(cipherManager.getNumberOfAlphabetsEstimate((Set)substitution.getIgnoredCharacters().get(0), langCI, substitution.getPlainAlpha().size())));
		    } else{
			numAlphaEstField.setText(CAN_NOT_ESTIMATE);
		    }
		    // Display the language in langStatsFrame's title bar
		    setLangStatsFrameTitle(locale);
		}
	    };
	LoadWorker worker = new LoadWorker(parseFile, loadNewLangFreqs, this);
	worker.start();
    }

    /**
     * Initializes the <code>JFileChooser</code> <code>langChooser</code> and
     * sets its current directory to <code>findLangFreqDir()</code>.
     *
     * @see #findLangFreqDir()
     */
    private final void initLangChooser(){
	File langPath = null;
	XMLFileFilter xmlFilter = new XMLFileFilter();
	langChooser = new JFileChooser();
	langChooser.addChoosableFileFilter(xmlFilter);
	langChooser.setFileFilter(xmlFilter);
	langChooser.setDialogTitle(labelsRB.getString("langChooser"));
	try{
	    langPath = findLangFreqDir();
	} catch(IOException ioe){
	    JOptionPane.showMessageDialog(this,
					  labelsRB.getString("ieCnfText"),
					  labelsRB.getString("ieCnfTitle"),
					  JOptionPane.ERROR_MESSAGE);
	    return;
	}
	if(langPath == null || !langPath.exists()){
	    JOptionPane.showMessageDialog(this,
					  labelsRB.getString("langNFText"),
					  labelsRB.getString("langNFTitle"),
					  JOptionPane.ERROR_MESSAGE);
	}else{
	    langChooser.setCurrentDirectory(langPath);
	}
    }

    /**
     * Adds the name of the language in the <code>Locale</code> 
     * <code>loc</code>, to the title bar of <code>langStatsFrame</code>.
     *
     * @param loc the language to set the name of on the title bar
     */
    private void setLangStatsFrameTitle(Locale loc){
	if(loc == null || loc.getLanguage().equals("")){
	    langStatsFrame.setTitle(labelsRB.getString("langStatsFrame"));
	} else{
	    Locale defLoc = JComponent.getDefaultLocale();
	    langStatsFrame.setTitle(labelsRB.getString("langStatsFrame")+
				    " - " +
				    (loc.getDisplayLanguage(defLoc)).toUpperCase(defLoc));
	}
    }

    /**
     * Replaces the occurrences of <code>str</code> in <code>strBuf</code>
     * by <code>rep</code>.
     *
     * @param str the sequence to be replaced
     * @param rep the replacement sequence
     * @param strBuf the <code>StringBuffer</code> to operate on
     *
     * @throws NullPointerException if any of the arguments is <code>null</code>
     */
    private static final void replace(String str,
				      String rep,
				      StringBuffer strBuf)
	throws NullPointerException
    {
	if(str==null || rep==null || strBuf==null){
	    throw new NullPointerException();
	}
	int i = strBuf.indexOf(str);
	int strLen = str.length();
	int last = 0;
	while(i >= 0){
	    last = i+strLen;
	    strBuf.replace(i, last, rep);
	    i = strBuf.indexOf(str, last);
	}
    }

    /**
     * Replaces the occurrences of <code>str</code> in <code>string</code>
     * by <code>rep</code> and returns the resulting <code>String</code>.
     *
     * @param str the sequence to be replaced
     * @param rep the replacement sequence
     * @param string the <code>String</code> to operate on
     * @return <code>string</code> with all the occurrences of <code>str</code>
     *         replaced by <code>rep</code>.
     *
     * @throws NullPointerException if any of the arguments is <code>null</code>
     */
    private static final String replace(String str,
					String rep,
					String string)
	throws NullPointerException
    {
	StringBuffer strBuf = new StringBuffer(string);
	replace(str, rep, strBuf);
	return strBuf.toString();
    }

    /**
     * Parses the instance of <code>LanguageFrequencies.xsd</code>
     * <code>freqs</code> and returns the 
     * <code>LanguageFrequenciesHandler</code> used to parse it.
     *
     * @param freqs an instance of <code>LanguageFrequencies.xsd</code>
     * @throws SAXNotRecognizedException if W3C schemas or a schema source are not supported by the version of JAXP the user has.
     * @throws SAXParseException if there was an error or warning while parsing the instance of <code>LanguageFrequencies.xsd</code>
     * @throws SAXException if there was an error or warning while parsing the instance of <code>LanguageFrequencies.xsd</code>
     * @throws ParserConfigurationException if a serious configuration error took place (the features needed to validate using a W3C schema are not present).
     * @throws SecurityException if <code>freqs</code> cannot be read due to security constrains
     * @throws FileNotFoundException if <code>freqs</code> could not be found
     * @throws IOException if an I/O exception of some sort occurred
     * @throws NullPointerException if <code>freqs</code> is <code>null</code>
     */
    private final LanguageFrequenciesHandler parseLangFreqs(File freqs)
	throws SAXNotRecognizedException, SAXParseException,
	       SAXException, ParserConfigurationException,
	       SecurityException, FileNotFoundException, IOException,
	       NullPointerException
    {
	if(freqs == null){
	    throw new NullPointerException();
	}else if(!freqs.exists()){
	    throw new FileNotFoundException("File not found");
	}else if(!freqs.canRead()){
	    throw new SecurityException("Permission denied");
	}
	LanguageFrequenciesHandler handler = new LanguageFrequenciesHandler();
	// Use the validating namespace aware parser
	SAXParserFactory saxFactory = SAXParserFactory.newInstance();
	saxFactory.setValidating(true);
	saxFactory.setNamespaceAware(true);
	// Parse the input
	SAXParser saxParser = saxFactory.newSAXParser();
	// use W3C schemata
	saxParser.setProperty(AnalyzerConstants.JAXP_SCHEMA_LANGUAGE, 
			      AnalyzerConstants.W3C_XML_SCHEMA);
	// Set schema source
	String schemataPath = findSchemataDir().getCanonicalPath() +
	                      System.getProperty("file.separator");
	saxParser.setProperty(AnalyzerConstants.JAXP_SCHEMA_SOURCE,
			      new File(schemataPath +
				       AnalyzerConstants.LANGUAGE_FREQUENCIES_SCHEMA_FILE));
	FileInputStream freqsIS = new FileInputStream(freqs);
	saxParser.parse(freqsIS, handler, schemataPath);
	freqsIS.close();
	return handler;
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
     * @throws IOException if an I/O error occurs.
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
     * @throws IOException if an I/O error occurs.
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
     * @throws IOException if an I/O error occurs.
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
     * <code>JDialog</code> that is used to add characters to the
     * cipher or plain alphabets.
     */
    private class CharacterAdditionDialog extends JDialog
                                          implements PropertyChangeListener{
	/**
	 * Mode in which the characters are added to the cipher alphabet*/
	public static final int ADD_TO_CIPHER_ALPHABET = 1;

	/**
	 * Mode in which the characters are added to the plain alphabet*/
	public static final int ADD_TO_PLAIN_ALPHABET = 2;

	private JTextField textField;
	private JOptionPane optionPane;
	private String addOptn;
	private String cancelOptn;
	private int mode;

	/**
	 * Creates a modal <code>CharacterAdditionDialog</code> in 
	 * <code>ADD_TO_CIPHER_ALPHABET</code> mode.
	 *
	 * @param parentComponent determines the <code>Frame</code> in which the dialog is displayed; if <code>null</code>, or if the parentComponent has no <code>Frame</code>, a default <code>Frame</code> is used.
	 */
	public CharacterAdditionDialog(Component parentComponent){
	    super(JOptionPane.getFrameForComponent(parentComponent), true);
	    addOptn = labelsRB.getString("addOptn");
	    cancelOptn = labelsRB.getString("cancelOptn");
	    textField = new JTextField(10);
	    Object[] message = {labelsRB.getString("instr"),
				textField};
	    Object[] options = {addOptn, cancelOptn};
	    optionPane = new JOptionPane(message,
					 JOptionPane.PLAIN_MESSAGE,
					 JOptionPane.YES_NO_OPTION,
					 null,
					 options,
					 null);
	    setContentPane(optionPane);
	    addListeners();
	    setMode(ADD_TO_CIPHER_ALPHABET);
	    pack();
	}

	/**
	 * Creates a modal <code>CharacterAdditionDialog</code> in 
	 * the specified mode.
	 *
	 * @param parentComponent determines the <code>Frame</code> in which the dialog is displayed; if <code>null</code>, or if the parentComponent has no <code>Frame</code>, a default <code>Frame</code> is used.
	 * @param mode either <code>ADD_TO_PLAIN_ALPHABET</code> or <code>ADD_TO_CIPHER_ALPHABET</code>
	 * @throws IllegalArgumentException if <code>mode</code> is no valid
	 * @see #ADD_TO_PLAIN_ALPHABET
	 * @see #ADD_TO_CIPHER_ALPHABET
	 */
	public CharacterAdditionDialog(Component parentComponent,
				       int mode)
	    throws IllegalArgumentException
	{
	    this(parentComponent);
	    setMode(mode);
	}

	/**
	 * Adds the event listeners the dialog needs to add characters to
	 * the cipher and plain alphabets.
	 */
	private void addListeners(){
	    //Handle window closing correctly.
	    addWindowListener(new WindowAdapter(){
		    public void windowClosing(WindowEvent e){
			optionPane.setValue(new Integer(JOptionPane.CLOSED_OPTION));
		    }
		});
	    //Ensure the text field always gets the first focus.
	    addComponentListener(new ComponentAdapter(){
		    public void componentShown(ComponentEvent e){
			textField.requestFocusInWindow();
		    }
		});
	    textField.addActionListener(new ActionListener(){
		    public void actionPerformed(ActionEvent e){
			optionPane.setValue(addOptn);
		    }
		});
	    optionPane.addPropertyChangeListener(this);
	}

	/**
	 * This method is public as an implementation side effect. Do not
	 * call or override.
	*/
	public void propertyChange(PropertyChangeEvent e){
	    String prop = e.getPropertyName();
	    if(isVisible() &&
	       e.getSource() == optionPane &&
	       (JOptionPane.VALUE_PROPERTY.equals(prop) ||
		JOptionPane.INPUT_VALUE_PROPERTY.equals(prop))){
		Object value = optionPane.getValue();
		// ignore reset
		if(value == JOptionPane.UNINITIALIZED_VALUE){
		    return;
		}
		// Reset optionPane's value
		optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);
		
		if(addOptn.equals(value)){
		    // validation code would go here
		    boolean added = false;
		    switch(mode){
			case ADD_TO_CIPHER_ALPHABET: 
			    try{
				added = substitution.addCharacterToCipherAlphabet(textField.getText());
				if(added){
				    textField.setText(null); // clear textField
				} else{
				    JOptionPane.showMessageDialog(this,
								  labelsRB.getString("cnaText"),
								  labelsRB.getString("cnaTitle"),
								  JOptionPane.INFORMATION_MESSAGE);
				    textField.selectAll();
				}
			    }catch(NullPointerException npe){
				JOptionPane.showMessageDialog(this,
							      labelsRB.getString("eptText"),
							      labelsRB.getString("eptTitle"),
							      JOptionPane.WARNING_MESSAGE);
				textField.selectAll();
			    }catch(IllegalArgumentException iae){
				JOptionPane.showMessageDialog(this,
							      labelsRB.getString("iaeText"),
							      labelsRB.getString("iaeTitle"),
							      JOptionPane.WARNING_MESSAGE);
				textField.selectAll();
			    }
			    break;
		        case ADD_TO_PLAIN_ALPHABET:
			    try{
				added = substitution.addCharacterToPlainAlphabet(textField.getText());
				if(added){
				    textField.setText(null); // clear textField
				} else{
				    JOptionPane.showMessageDialog(this,
								  labelsRB.getString("cnaText"),
								  labelsRB.getString("cnaTitle"),
								  JOptionPane.INFORMATION_MESSAGE);
				    textField.selectAll();
				}
			    }catch(NullPointerException npe){
				JOptionPane.showMessageDialog(this,
							      labelsRB.getString("eptText"),
							      labelsRB.getString("eptTitle"),
							      JOptionPane.WARNING_MESSAGE);
				textField.selectAll();
			    }catch(IllegalArgumentException iae){
				JOptionPane.showMessageDialog(this,
							      labelsRB.getString("iaeText"),
							      labelsRB.getString("iaeTitle"),
							      JOptionPane.WARNING_MESSAGE);
				textField.selectAll();
			    }
			    break;
		        default:
			    assert false : "Invalid mode";
			    break;
		    }
		    textField.requestFocusInWindow();
		} else{ //user closed the dialog or clicked cancel
		    textField.setText(null);
		    setVisible(false);
		}
	    }
	}

	/**
	 * Makes the dialog invisible and then sets the current mode
	 * to the one passed.
	 *
	 * @param mode <code>ADD_TO_CIPHER_ALPHABET</code> or 
	 *             <code>ADD_TO_PLAIN_ALPHABET</code>
	 * @throws IllegalArgumentException if <code>mode</code> is not a valid mode
	 * @see #ADD_TO_CIPHER_ALPHABET
	 * @see #ADD_TO_PLAIN_ALPHABET
	 */
	public void setMode(int mode) throws IllegalArgumentException
	{
	    if(isVisible()){
		setVisible(false);
	    }
	    optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE); // reset
	    textField.setText(null);
	    switch(mode){
	        case ADD_TO_CIPHER_ALPHABET:
		    this.mode = ADD_TO_CIPHER_ALPHABET;
		    setTitle(labelsRB.getString("toCipherTitl"));
		    break;
	        case ADD_TO_PLAIN_ALPHABET:
		    this.mode = ADD_TO_PLAIN_ALPHABET;
		    setTitle(labelsRB.getString("toPlainTitl"));
		    break;
	        default:
		    throw new IllegalArgumentException();
	    }
	}
    }

    /**
     * <code>ListModel</code> used to display the cipher alphabet in a
     * <code>JList</code>.
     *
     * @see CollationKeyListModel
     */
    private class CipherAlphabetListModel extends CollationKeyListModel{

	/**
	 * Creates an empty <code>CipherAlphabetListModel</code>.
	 */
	public CipherAlphabetListModel(){
	    super();
	}

	/**
	 * Sets <code>list</code> as the <code>ArrayList</code> of
	 * <code>CollationKey</code>s that stores the model's data.
	 *
	 * @param list an <code>ArrayList</code> of <code>CollationKey</code>s
	 */
	public void setData(ArrayList list) throws NullPointerException
	{
	    if(list == null){
		throw new NullPointerException();
	    }
	    ckList = list;
	    fireContentsChanged(this, 0, ckList.size()-1);
	}

	/**
	 * Returns the <code>CollationKey</code>s at the specified indices.
	 *
	 * @param indices the requested indices
	 * @return an array of <code>CollationKey</code>s in the same order as the requests.
	 * @throws NullPointerException if <code>indices</code> is <code>null</code>
	 * @throws IndexOutOfBoundsException if any of the indices is out of bounds
	 * @see CollationKeyListModel#getCollationKeyAt(int)
	 */
	public CollationKey[] getIndices(int[] indices) 
	    throws NullPointerException, IndexOutOfBoundsException
	{
	    if(indices == null){
		throw new NullPointerException();
	    }
	    CollationKey[] ckArr = new CollationKey[indices.length];
	    for(int i=0; i<indices.length; i++){
		ckArr[i]=getCollationKeyAt(indices[i]);
	    }
	    return ckArr;
	}
    }

    /**
     * Sets the name of the cryptanalysis project on the title of the
     * <code>Frame</code> that contains the <code>Analyzer</code>.
     * If the <code>Analyzer</code> is not contained in a <code>Frame</code>
     * the method does nothing. If the containing <code>Frame</code> exists
     * then, its title is set to the name of the project if the operating
     * system the program is being executed on is Mac OS X, and to
     * <code>&quot;program_name - project_name&quot;</code> otherwise.
     * If <code>name</code> is <code>null</code> the title is set to
     * &quot;untitled&quot;.
     *
     * @param name the name of the cryptanalysis project
     */
    protected void setProjectNameOnFrameTitle(String name){
	Frame frame = (Frame)SwingUtilities.getAncestorOfClass(Frame.class,
							       this);
	if(frame == null){
	    return;
	}
	if(name == null){
	    name = labelsRB.getString("unttldProj");
	}
	if(isSystemMacWithAquaLAF()){
	    frame.setTitle(name);
	}else{
	    frame.setTitle(labelsRB.getString("progTitl") + " - " + name);
	}
    }

    /**
     * Sets the name of the cryptanalysis project on the title of the
     * <code>Frame</code> that contains the <code>Analyzer</code> using
     * <code>setProjectNameOnFrameTitle(String)</code> with
     * <code>file.getName()</code> as parameter.
     *
     * @param file the <code>File</code> to put the name of on the title
     * @see #setProjectNameOnFrameTitle(String)
     */
    private void setProjectNameOnFrameTitle(File file){
	String name = null;
	if(file != null){
	    name = file.getName();
	}
	setProjectNameOnFrameTitle(name);
    }

    /**
     * Returns <code>true</code> if the current cryptanalysis project contains
     * unsaved changes and <code>false</code> otherwise.
     */
    public boolean areThereUnsavedChanges(){
	return unsavedChanges;
	
    }

    /**
     * Returns <code>true</code> if the underlying operating system
     * is Mac OS X and <code>false</code> otherwise.
     */
    protected static boolean isSystemMac(){
        return System.getProperty("os.name").toLowerCase().startsWith("mac os x");
    }

    /**
     * Returns <code>true</code> if the underlying operating system
     * is Mac OS X and the current Look And Feel is Aqua
     * (Mac OS X's system default LAF), and <code>false</code> otherwise.
     */
    protected static boolean isSystemMacWithAquaLAF(){
	boolean systemLAF; // true if the current LAF is the system's LAF
	systemLAF = UIManager.getSystemLookAndFeelClassName().equals(
			   UIManager.getLookAndFeel().getClass().getName());
	return isSystemMac() && systemLAF;
    }

    /**
     * Program that displays an <code>Analyzer</code> in a window with
     * a menu bar that gives the users access to the
     * <code>Analyzer</code>'s methods.
     *
     * @param args an array of <code>String</code>s that may contain the
     *             options specified in the method
     *             <code>parseArguments(String[])</code> (<code>-l</code>,
     *             <code>-c</code>, <code>-help</code>)
     * @see #parseArguments(String[])
     */
    public static void main(String[] args){
	parseArguments(args);
	// if in Mac OS, display the menu bar in the appropriate way.
	if(isSystemMacWithAquaLAF()){
	    System.setProperty("apple.laf.useScreenMenuBar", "true");
	}
	// show a splash screen while the Analyzer is initialized
	final JFrame splashFrame = new JFrame();
	splashFrame.getContentPane().add(new JLabel(new ImageIcon(Analyzer.class.getResource("images/ganzuaSp.png"))), BorderLayout.CENTER);
	splashFrame.setUndecorated(true);
	splashFrame.pack();
	splashFrame.setLocationRelativeTo(null);
	splashFrame.setVisible(true);
	try{
	    Thread.sleep(500);
	}catch(InterruptedException ie){}
	// initialize a new Analyzer
	javax.swing.SwingUtilities.invokeLater(new Runnable() {
		public void run(){
		    JFrame.setDefaultLookAndFeelDecorated(true);
		    JDialog.setDefaultLookAndFeelDecorated(true);
		    final JFrame frame = new JFrame();
		    final Image icon = LOGO.getImage();
		    final Analyzer ana = new Analyzer();
		    frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		    frame.getContentPane().add(ana,
					       BorderLayout.CENTER);
		    frame.addWindowListener(new WindowAdapter(){
			    public void windowClosing(WindowEvent e){
				try{
				    savePreferredWindowSettings(ana);
				}catch(BackingStoreException bse){}
			    }
			});
		    ana.setProjectNameOnFrameTitle((String)null);
		    ana.addWindowListener();
		    frame.setJMenuBar(ana.getMenuBar());
		    if(isSystemMacWithAquaLAF()){
			addMenuBarToAnalyzerWindows(ana);
		    }
		    frame.setIconImage(icon);
		    setIconImageOfAnalyzerWindows(ana, icon);
		    frame.pack();
		    loadPreferredWindowSettings(ana);
		    splashFrame.setVisible(false); // close the splash screen
		    splashFrame.dispose(); // and release its resources
		    frame.setVisible(true);
		}
	    });
    }

    /**
     * Parses the arguments passed to the program using the POSIX conventions
     * for command line arguments. It looks for the options <code>-l</code>,
     * <code>-c</code> and <code>-help</code>.  <code>-l</code> must be
     * followed by the language (the ISO 366 language code) to use in the GUI
     * and <code>-c</code> must be followed by the country (the ISO 3166
     * country code) to use conventions of. If the arguments of <code>-l</code>
     * or <code>-c</code> are invalid, the program is forced to exit. If
     * <code>-help</code> or <code>--help</code> are found among the arguments
     * a help screen is displayed and the program is forced to exit.<br/>
     * If <code>-l</code> or <code>-c</code> are used, this method sets the
     * GUI's <code>Locale</code> with a call to
     * <code>JComponent.setDefaultLocale(Locale)</code>
     *
     * @param args the array of <code>Strings</code> that was passed to the 
     *             <code>main(String[])</code> method.
     * @see #main(String[])
     * @see JComponent#setDefaultLocale(Locale)
     */
    protected static void parseArguments(String[] args){
	if(args == null || args.length == 0){
	    return;
	}
	final String HELP_OPN = "-help"; // ask the program usage info and exit
	final String HELP_OPN2= "--help";// ask the program usage info and exit
	final String LANG_OPN = "-l"; // option used to specify the language
	final String COUNTRY_OPN = "-c"; // option used to specify the country
	// array of options that require arguments
	String[] opnsArr = {LANG_OPN, COUNTRY_OPN};
	// unmodifiable list of options that require arguments
	java.util.List options = Collections.unmodifiableList(Arrays.asList(opnsArr));
	//option-argument pairs of the arguments provided in the command line
	HashMap opnArg = new HashMap();
	String option = null;
	boolean validOpn = false;
	for(int i=0; i<args.length; i++){
	    if(args[i].equals(HELP_OPN) || args[i].equals(HELP_OPN2)){
		validOpn = true;
		System.out.println(commandMsgs.getString("usage"));
		System.exit(0);
	    }
	    for(Iterator iter=options.iterator(); iter.hasNext(); ){
		option = (String)iter.next();
		if(args[i].length() >= option.length() && 
		   args[i].substring(0, option.length()).equals(option)){
		    validOpn = true;
		    if(args[i].length() == option.length()){
			i++;
			try{
			    opnArg.put(option, args[i]);
			} catch(ArrayIndexOutOfBoundsException aob){
			    String msg = replace("OP", option,
						 commandMsgs.getString("misArg"));
			    System.err.println(msg);
			    System.exit(1);
			}
		    } else{
			opnArg.put(option, args[i].substring(option.length(),
							     args[i].length()));
		    }
		}
	    }
	    if(!validOpn){
		String msg = replace("OP", args[i],
				     commandMsgs.getString("ilOpn"));
		System.err.println(msg);
		System.exit(4);
	    }
	}
	String lang =  (String)opnArg.get(LANG_OPN);
	lang = lang == null ? null : lang.toLowerCase();
	String country = (String)opnArg.get(COUNTRY_OPN);
	country = country == null? null : country.toUpperCase();
	HashSet languages = new HashSet(Arrays.asList(Locale.getISOLanguages()));
	HashSet countries = new HashSet(Arrays.asList(Locale.getISOCountries()));
	Locale defLoc = Locale.getDefault();
	if(lang != null){
	    if(!languages.contains(lang)){
		System.err.println(replace("LN", lang,
					   commandMsgs.getString("ilLang")));
		System.exit(2);
	    }
	    if(country != null){
		if(countries.contains(country)){
		    JComponent.setDefaultLocale(new Locale(lang, country));
		} else{
		    System.err.println(replace("CT", country,
					       commandMsgs.getString("ilCtry")));
		    System.exit(3);
		}
	    } else{
		JComponent.setDefaultLocale(new Locale(lang,
						       defLoc.getCountry()));
	    }
	}else if(country != null){
	    if(countries.contains(country)){
		JComponent.setDefaultLocale(new Locale(defLoc.getLanguage(),
						       country));
	    } else{
		System.err.println(replace("CT", country,
					   commandMsgs.getString("ilCtry")));
		System.exit(3);
	    }
	}
    }

    /**
     * Changes the icon of an <code>Analyzer</code>'s <code>Frame</code>s
     * (the ones it uses, not the one that contains it) to the 
     * <code>Image</code> passed.
     *
     * @param ana an <code>Analyzer</code>
     * @param icon an <code>Image</code>
     * @throws NullPointerException if any of the arguments is <code>null</code>
     * @see #getMenuBar()
     */
    protected static void setIconImageOfAnalyzerWindows(Analyzer ana,
							Image icon){
	if(ana == null || icon == null){
	    throw new NullPointerException();
	}
	ana.langStatsFrame.setIconImage(icon);
	ana.remFromCipherAlphaFrame.setIconImage(icon);
	ana.substitution.getIgnoredCharactersFrame().setIconImage(icon);
	ana.toolsPane.getKasiskiFrame().setIconImage(icon);
	if(ana.aboutFrame != null){
	    ana.aboutFrame.setIconImage(icon);
	}
    }

    /**
     * Adds a <code>JMenuBar</code> to the windows the <code>Analyzer</code>
     * <code>ana</code> uses except the one that contains it. All of the
     * <code>JMenuBar</code>s are created using <code>ana</code>'s
     * <code>getMenuBar()</code> method.
     *
     * @param ana an <code>Analyzer</code>
     * @throws NullPointerException if <code>ana</code> is <code>null</code>
     * @see #getMenuBar()
     */
    protected static void addMenuBarToAnalyzerWindows(Analyzer ana)
	throws NullPointerException
    {
	ana.langStatsFrame.setJMenuBar(ana.getMenuBar());
	ana.remFromCipherAlphaFrame.setJMenuBar(ana.getMenuBar());
	ana.substitution.getIgnoredCharactersFrame().setJMenuBar(ana.getMenuBar());
	ana.toolsPane.getKasiskiFrame().setJMenuBar(ana.getMenuBar());
	if(ana.aboutFrame != null){
	    ana.aboutFrame.setJMenuBar(ana.getMenuBar());
	}
    }

    /**
     * Saves the current size of the windows related to the
     * <code>Analyzer</code> <code>ana</code> as the user's preferred size for
     * windows related to <code>Analyzer</code>s.<br/>
     *
     * Note that <code>ana</code> must be contained by a resolved window.
     *
     * @param ana an <code>Analyzer</code> contained by a resolved window
     * @see #saveWindowPreferences(Window, String, Preferences)
     * @throws NullPointerException if <code>ana</code> is <code>null</code> or is not contained by a window
     * @throws BackingStoreException if the settings can not be saved due to a a failure in <code>Preferences</code>' backing store, or inability to communicate with it.
     */
    protected static void savePreferredWindowSettings(Analyzer ana)
	throws NullPointerException, BackingStoreException
    {
	Preferences uPref = Preferences.userNodeForPackage(Analyzer.class);
	// load the main window's settings
	Window window = SwingUtilities.getWindowAncestor(ana);
	saveWindowPreferences(window, "MainWindow", uPref);
	saveWindowPreferences(ana.langStatsFrame, "LangStatsWindow", uPref);
	saveWindowPreferences(ana.remFromCipherAlphaFrame,
			      "RemFromCipherAlphaWindow", uPref);
	saveWindowPreferences(ana.substitution.getIgnoredCharactersFrame(),
			      "IgnoredCharactersWindow", uPref);
	saveWindowPreferences(ana.toolsPane.getKasiskiFrame(),
			      "KasiskiTestWindow", uPref);
	if(ana.aboutFrame != null){
	    saveWindowPreferences(ana.aboutFrame, "AboutWindow", uPref);
	}
	uPref.flush();
    }

    /**
     * Saves the current size and location of <code>window</code> in the
     * preference node <code>pref</code> using the name
     * <code>windowName</code> to generate the keys. The keys used are
     * <code>windowName + "_x"</code> for the location's <i>x</i> coordinate,
     * <code>windowName + "_y"</code> for the location's <i>y</i> coordinate,
     * <code>windowName + "_width"</code> for the window's width and
     * <code>windowName + "_height"</code> for the window's width.
     *
     * @param window a <code>Window</code>
     * @param windowName a <code>String</code> to be used to generate the keys
     *                   to which the window's data is associated
     * @param pref a <code>Preferences</code> node to save the window's
     *             location an size to.
     * @throws NullPointerException if any of the arguments is <code>null</code>
     */
    private static void saveWindowPreferences(Window window,
					      String windowName,
					      Preferences pref)
	throws NullPointerException
    {
	Rectangle winBounds = window.getBounds();
	pref.putInt(windowName + "_x", winBounds.x);
	pref.putInt(windowName + "_y", winBounds.y);
	pref.putInt(windowName + "_width", winBounds.width);
	pref.putInt(windowName + "_height", winBounds.height);
    }

    /**
     * Loads the user's preferred size for the windows related to
     * <code>Analyzer</code>s windows related to <code>Analyzer</code>s.<br/>
     *
     * Note that <code>ana</code> must be contained by a resolved window.
     *
     * @param ana an <code>Analyzer</code> contained by a resolved window
     * @see #loadWindowPreferences(Window, String, Preferences)
     * @throws NullPointerException if <code>ana</code> is <code>null</code> or is not contained by a window
     */
    protected static void loadPreferredWindowSettings(Analyzer ana)
	throws NullPointerException
    {
	Preferences uPref = Preferences.userNodeForPackage(Analyzer.class);
	Rectangle winBounds = new Rectangle();
	// load the main window's settings
	Window window = SwingUtilities.getWindowAncestor(ana);
	loadWindowPreferences(window, "MainWindow", uPref);
	loadWindowPreferences(ana.langStatsFrame, "LangStatsWindow", uPref);
	loadWindowPreferences(ana.remFromCipherAlphaFrame,
			      "RemFromCipherAlphaWindow", uPref);
	loadWindowPreferences(ana.substitution.getIgnoredCharactersFrame(),
			      "IgnoredCharactersWindow", uPref);
	loadWindowPreferences(ana.toolsPane.getKasiskiFrame(),
			      "KasiskiTestWindow", uPref);
	if(ana.aboutFrame != null){
	    loadWindowPreferences(ana.aboutFrame, "AboutWindow", uPref);
	}
    }

    /**
     * Loads the current size and location of <code>window</code> from the
     * preference node <code>pref</code> using the name
     * <code>windowName</code> to generate the keys. The keys used are the
     * same <code>saveWindowPreferences(Window, String, Preferences)</code>
     * uses to save the location and size of the windows.
     *
     * @param window a <code>Window</code>
     * @param windowName a <code>String</code> to be used to generate the keys
     *                   to which the window's data is associated
     * @param pref a <code>Preferences</code> node to load the window's 
     *             location an size from.
     * @throws NullPointerException if any of the arguments is <code>null</code>
     * @see #saveWindowPreferences(Window, String, Preferences)
     */
    private static void loadWindowPreferences(Window window,
					      String windowName,
					      Preferences pref){
	Rectangle winBounds = window.getBounds();
	winBounds.x = pref.getInt(windowName + "_x", winBounds.x);
	winBounds.y = pref.getInt(windowName + "_y", winBounds.y);
	winBounds.width = pref.getInt(windowName + "_width", winBounds.width);
	winBounds.height = pref.getInt(windowName + "_height",
					winBounds.height);
	window.setBounds(winBounds);
    }
}
/*
 * -- Analyzer.java ends here --
 */
