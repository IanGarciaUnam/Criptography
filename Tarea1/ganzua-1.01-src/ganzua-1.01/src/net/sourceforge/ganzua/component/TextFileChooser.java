/*
 * -- TextFileChooser.java --
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

import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

/**
 * <code>TextFileChooser</code> provides a simple mechanism for the user
 * to choose a plain text file and it's encoding based on 
 * <code>JFileChooser</code>. Note that it does not check that the selected
 * file is indeed a plain text file.
 *
 * @see ConfirmFileChooser
 * @author Jesús Adolfo García Pasquel
 * @version 0.01 February 2003
 */
public class TextFileChooser extends ConfirmFileChooser{

    /**
     * Array of strings to use in the group of radio buttons. The
     * strings are the canonical names of the default and some of the standard
     * <code>Charset</code>s
     */
    protected static final  String[] CANONICAL_NAMES;

    static{
	String[] encodings = {"US-ASCII", "ISO-8859-1", "UTF-8", "UTF-16"};
	CANONICAL_NAMES = encodings;
	String defaultEnc = defaultEncoding();
	if(!Arrays.asList(CANONICAL_NAMES).contains(defaultEnc)){
	    CANONICAL_NAMES[3] = defaultEnc;
	}
    }

    /**
     * String with the selected encoding or null.
     */
    protected String encoding;

    /**
     * Vector with the supported encodings that are not in
     * <code>CANONICAL_NAMES</code>
     */
    protected Vector encodings;

    /**
     * JPanel that holds the accessory component that lets the user choose
     * the encoding of the file.
     */
    protected JPanel accessoryPan;

    /**
     * Button added to <code>rbGrp</code>, but not to the panel,
     * so it can be displayed as if no radio button was selected when
     * an option in the combo box is selected.
     */
    private JRadioButton rbNull;

    /**
     * <code>ButtonGroup</code> that holds the <code>JRadioButton</code>s
     * of the frequently used encodings.
     */
    private ButtonGroup rbGrp;

    /**
     * <code>JComboBox</code> that holds the rest of the available encodings.
     */
    private JComboBox charsetsBox;

    /**
     * String that holds the option "other" of the <code>JComboBox</code>.
     */
    private String cbOther;

    /**
     * <code>FileFilter</code> that makes the <code>TextFileChooser</code>'s
     * directory listing display directories and files with extension
     * <code>txt</code> or <code>text</code> exclusively. */
    private final TextFileFilter textFilter = new TextFileFilter();

    /**
     * <code>ResourceBundle</code> used to get the localized labels
     */
    private ResourceBundle labelsRB;

    /**
     * Constructs a <code>TextFileChooser</code> pointing to the user's 
     * default directory.
     */
    public TextFileChooser(){
	super();
	encoding = null;
	accessoryPan = new JPanel();
	rbGrp = new ButtonGroup();
	labelsRB = ResourceBundle.getBundle(TextFileChooser.class.getName(),
					    getDefaultLocale());
	setEncodingMenu();
	cbOther = labelsRB.getString("cbOther");
	addChoosableFileFilter(textFilter);
	setFileFilter(textFilter);
    }

    /**
     * Initializes the <code>TextFileChooser</code>'s accessory that
     * lets the users select the encoding of the text file.
     */
    private void setEncodingMenu(){
	accessoryPan.setLayout(new GridLayout(0, 1));
	accessoryPan.setBorder(new TitledBorder(labelsRB.getString("Encoding")));
	rbNull = new JRadioButton();
	rbGrp.add(rbNull);
	String defaultEnc = defaultEncoding();
	for(int i=0; i<CANONICAL_NAMES.length; i++){
	    JRadioButton encBtn = encBtn(CANONICAL_NAMES[i]);
	    addRB(encBtn);
	    if(CANONICAL_NAMES[i].equals(defaultEnc)){
		encBtn.setSelected(true);
		encoding = defaultEnc;
	    }
	}
	addCBox();
	super.setAccessory(accessoryPan);
    }

    /**
     * Creates a <code>JRadioButton</code> with the specified text
     * (the canonical name of an encoding).
     */
    private JRadioButton encBtn(String encodingName){
	JRadioButton tmpBtn = new JRadioButton(encodingName);
	tmpBtn.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent e){
		    encoding = ((JRadioButton)(e.getSource())).getText();
		    charsetsBox.setSelectedIndex(0);
		}
	    });
	return tmpBtn;
    }

    /**
     * Adds <code>radioButton</code> to <code>rbGrp</code> and
     * <code>accessoryPan</code>
     *
     * @param radioButton the <code>JRadioButton</code> to be added
     */
    private void addRB(JRadioButton radioButton){
	rbGrp.add(radioButton);
	accessoryPan.add(radioButton);
    }

    /**
     * Initializes and adds the <code>JComboBox</code> with the other
     * available encodings to <code>accessoryPan</code>
     */
    private void addCBox(){
	Map charSets = Charset.availableCharsets();
	encodings = new Vector(5, 10);
	encodings.add(labelsRB.getString("cbOther"));
	Iterator it = charSets.keySet().iterator();
	int i=1;
	while(it.hasNext()){
	    String tmpS = (String)it.next();
	    boolean ignore = false;
	    for(int j=0; j<CANONICAL_NAMES.length; j++){
		if(CANONICAL_NAMES[j].equals(tmpS)){
		    ignore = true;
		}
	    }
	    if(!ignore){
		encodings.add(tmpS);
		i++;
	    }
	}
        charsetsBox = new JComboBox(encodings);
	charsetsBox.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent e){
		    JComboBox cb = (JComboBox)(e.getSource());
		    if(!cbOther.equals((String)cb.getSelectedItem())){
			if(rbGrp.getSelection()!=null){
			    rbNull.setSelected(true);
			    accessoryPan.repaint();
			}
			encoding = (String)cb.getSelectedItem();
		    }else{
			int i = encodings.indexOf(encoding);
			cb.setSelectedIndex(i<0?0:i);
		    }
		}
	    });
	accessoryPan.add(charsetsBox);
    }

    /**
     * Called by the UI when the user hits the approve button.
     *
     * @see ConfirmFileChooser#approveSelection()
     */
    public void approveSelection(){
	if(encoding==null){
	    JOptionPane.showMessageDialog(this,
					  labelsRB.getString("EncErrMsg"),
					  labelsRB.getString("EncErrTtl"),
					  JOptionPane.ERROR_MESSAGE);
	}else{
	    super.approveSelection();
	}
    }

    /**
     * Returns the encoding selected for the chosen file.
     *
     * @return The encoding selected for the chosen file
     */
    public String getEncoding(){
	return encoding;
    }

    /**
     * Does nothing.
     */
    public void setAccessory(JComponent newAccessory){}

    /**
     * Returns the canonical name of the default character encoding.
     *
     * @return the name of the default character encoding
     */
    public static String defaultEncoding(){
	String name = System.getProperty("file.encoding");
	if(name == null){
	    OutputStreamWriter out = new OutputStreamWriter(System.out);
	    name = out.getEncoding();
	}
	name = Charset.forName(name).name();
	return name;
    }
}
/*
 * -- TextFileChooser.java ends here --
 */
