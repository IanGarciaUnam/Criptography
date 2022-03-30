/*
 * -- CiphertextPanel.java --
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
import javax.swing.text.DefaultEditorKit;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

/**
 * Component used to display the ciphertext and plaintext intercalated
 * (one line of ciphertext, one line of plaintext) or in different
 * <code>JTextArea</code>s.
 *
 * @author Jesús Adolfo García Pasquel
 * @version 0.01 August 2003
 */
public class CiphertextPanel extends JPanel 
                             implements ActionListener
{

    /**
     * Name of the mode in which <code>CiphertextPanel</code>s show
     * intercaleted lines of ciphertext and plaintext. */
    public static final byte INTERCALATE = 0;

    /**
     * Name of the mode in which <code>CiphertextPanel</code>s show
     * the ciphertext and plaintext in different <code>JTextArea</code>s. */
    public static final byte SEPARATE = 1;

    /**
     * <code>ResourceBundle</code> with localized labels */
    private ResourceBundle labelsRB;

    /**
     * Indicates if the ciphertext and the plaintext are being displayed
     * in different <code>JTextArea</code>s (<code>INTERCALATE</code>) or in
     * the same intercalaing lines (<code>SEPARATE</code>) */
    protected byte mode;

    /**
     * The ciphertext */
    protected String ciphertext;

    /**
     * The plaintext */
    protected String plaintext;

    /**
     * <code>JTextArea</code> where the ciphertext is displayed*/
    protected JTextArea ciphertextArea;

    protected JScrollPane ciphertextScroll;

    /**
     * <code>JTextArea</code> where the plaintext is displayed*/
    protected JTextArea plaintextArea;

    protected JScrollPane plaintextScroll;

    /**
     * <code>JTextArea</code> where the ciphertext and the plaintext are
     * displayed intercalating their lines */
    protected JTextArea intercalatedArea;

    protected JScrollPane intercalatedScroll;

    /**
     * <code>JPanel</code> that contains <code>intercalatedArea</code> */
    protected JPanel intercalatedPanel;

    /**
     * <code>JPanel</code> that contains <code>ciphertextArea</code> and
     * <code>intercalatedArea</code> */
    protected JPanel separatedPanel;

    /**
     * Popup menu that lets the user change between <code>INTERCALATED</code>
     * and <code>SEPARATED</code> mode.<br/>
     * The menu also lets the user copy the selected text to the system's 
     * clipboard */
    protected JPopupMenu modeMenu;

    /**
     * <code>JMenuItem</code> that lets the user copy the selected
     * text to the clipboard.
     */
    protected JMenuItem copyMI;

    /**
     * <code>JRadioButtonMenuItem</code> that sets the mode to
     * <code>INTERCALATED</code>
     */
    protected JRadioButtonMenuItem interMI;

    /**
     * <code>JRadioButtonMenuItem</code> that sets the mode to
     * <code>SEPARATED</code>
     */
    protected JRadioButtonMenuItem separMI;

    /**
     * <code>ButtonGroup</code> that contains <code>interMI</code> and
     * <code>separMI</code> */
    protected ButtonGroup modeGroup;

    /**
     * Used to register all the <code>ChangeListener</code>s interested
     * in the <code>CiphertextPanel</code> */
    private EventListenerList listenerList = new EventListenerList();

    /**
     * <code>Action</code> to copy the selected region of text onto
     * the system's clipboard. */
    protected Action copyAction = null;

    /**
     * Creates a <code>CiphertextPanel</code> where the ciphertext and
     * plaintext are <code>&quot;&quot;</code>, and the mode is
     * <code>INTERCALATE</code>.
     */
    public CiphertextPanel(){
	super();
	mode = INTERCALATE;
	ciphertext = "";
	plaintext = "";
	labelsRB = ResourceBundle.getBundle(CiphertextPanel.class.getName(),
					    getDefaultLocale());
	initializeGUI();
    }

    /**
     * Creates a <code>CiphertextPanel</code> with ciphertext
     * <code>cipher</code> and plaintext <code>plain</code> in
     * <code>INTERCALATE</code> mode.
     *
     * @throws NullPointerException if <code>cipher</code> or 
     *                       <code>plain</code> are <code>null</code>.
     * @throws IllegalArgumentException if <code>cipher</code> and 
     *                       <code>plain</code> have different number of lines.
     */
    public CiphertextPanel(String cipher,
			   String plain) throws NullPointerException,
						IllegalArgumentException
    {
	this();
	setText(cipher, plain);
    }

    /**
     * Method that initializes the elements of the GUI.
     */
    private final void initializeGUI(){
	int policyV = JScrollPane.VERTICAL_SCROLLBAR_ALWAYS;
	int policyH = JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS;
	this.setLayout(new BorderLayout());
	intercalatedPanel = new JPanel();
	intercalatedPanel.setLayout(new BorderLayout());
	separatedPanel = new JPanel();
	separatedPanel.setLayout(new BoxLayout(separatedPanel,
					       BoxLayout.Y_AXIS));
	// initialize separatedPanel
	ciphertextArea = new JTextArea(ciphertext);
	ciphertextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
	ciphertextArea.setEditable(false);
	ciphertextArea.setDragEnabled(true);
	JPanel ciphertextPanel = new JPanel(new BorderLayout());
	ciphertextPanel.setBorder(BorderFactory.createTitledBorder(labelsRB.getString("cipher")));
	ciphertextScroll = new JScrollPane(ciphertextArea,
					   policyV, policyH);
	ciphertextPanel.add(ciphertextScroll);
	plaintextArea = new JTextArea(plaintext);
	plaintextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
	plaintextArea.setEditable(false);
	plaintextArea.setDragEnabled(true);
	JPanel plaintextPanel = new JPanel(new BorderLayout());
	plaintextPanel.setBorder(BorderFactory.createTitledBorder(labelsRB.getString("plain")));
	plaintextScroll = new JScrollPane(plaintextArea,
					  policyV, policyH);
	plaintextPanel.add(plaintextScroll);
	separatedPanel.add(ciphertextPanel);
	separatedPanel.add(plaintextPanel);
	//initialize intercalatedPanel
	intercalatedArea = new JTextArea("");
	intercalatedArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
	intercalatedArea.setEditable(false);
	intercalatedArea.setDragEnabled(true);
	intercalatedScroll = new JScrollPane(intercalatedArea,
					     policyV, policyH);
	intercalatedPanel.add(intercalatedScroll);
	intercalatedPanel.setBorder(BorderFactory.createTitledBorder(labelsRB.getString("interc")));
	if(mode == INTERCALATE){
	    add(intercalatedPanel);
	}else{
	    add(separatedPanel);
	}
	//initialize the popup menu
	modeMenu = new JPopupMenu();
	copyMI = new JMenuItem(getCopyAction());
	copyMI.setAccelerator(null); //to remove the shortcut from the menu
	modeMenu.add(copyMI);
	modeMenu.add(new JSeparator());
	modeGroup = new ButtonGroup();
	interMI = new JRadioButtonMenuItem(labelsRB.getString("interMI"));
	interMI.setToolTipText(labelsRB.getString("interMITT"));
	interMI.setActionCommand("INTERCALATE");
	interMI.addActionListener(this);
	interMI.setSelected(true);
	modeGroup.add(interMI);
	modeMenu.add(interMI);
	separMI = new JRadioButtonMenuItem(labelsRB.getString("separMI"));
	separMI.setToolTipText(labelsRB.getString("separMITT"));
	separMI.setActionCommand("SEPARATE");
	separMI.addActionListener(this);
	separMI.setSelected(false);
	modeGroup.add(separMI);
	modeMenu.add(separMI);
	MouseListener popupListener = new CiphertextPanelMouseListener();
	// add the mouse listeners
	addMouseListener(popupListener);
	ciphertextArea.addMouseListener(popupListener);
	plaintextArea.addMouseListener(popupListener);
	intercalatedArea.addMouseListener(popupListener);
	/* add an AdjustmentListener to the JScrollBars of ciphertextScroll
	   and plaintextScroll, so they show matching lines of ciphertext
	   and plaintext */
	final JScrollBar ctVBar=ciphertextScroll.getVerticalScrollBar();
	final JScrollBar ptVBar=plaintextScroll.getVerticalScrollBar();
	final JScrollBar itVBar=intercalatedScroll.getVerticalScrollBar();
	final JScrollBar ctHBar=ciphertextScroll.getHorizontalScrollBar();
	final JScrollBar ptHBar=plaintextScroll.getHorizontalScrollBar();
	final JScrollBar itHBar=intercalatedScroll.getHorizontalScrollBar();
	final int fontH = ciphertextArea.getFontMetrics(ciphertextArea.getFont()).getHeight();
	AdjustmentListener adVert =  new AdjustmentListener(){
		public void adjustmentValueChanged(AdjustmentEvent e){
		    int value = e.getValue();
		    JScrollBar sourceBar = (JScrollBar)e.getSource();
		    if(e.getAdjustmentType()==AdjustmentEvent.TRACK){
			if(sourceBar.getOrientation() == Adjustable.VERTICAL){
			    if(sourceBar == ctVBar){
				ptVBar.removeAdjustmentListener(this);
				itVBar.removeAdjustmentListener(this);
				if(!ptVBar.getValueIsAdjusting() &&
				   ptVBar.getValue()!=value){
				    ptVBar.setValue(value);
				}
				int itVal = (value/fontH)*3*fontH+value%fontH;
				if(!itVBar.getValueIsAdjusting() &&
				   itVBar.getValue()!=itVal){
				    itVBar.setValue(itVal);
				}
				ptVBar.addAdjustmentListener(this);
				itVBar.addAdjustmentListener(this);
			    }else if(sourceBar==ptVBar){
				ctVBar.removeAdjustmentListener(this);
				itVBar.removeAdjustmentListener(this);
				if(!ctVBar.getValueIsAdjusting() &&
				   ctVBar.getValue()!=value){
				    ctVBar.setValue(value);
				}
				int itVal = (value/fontH)*3*fontH+value%fontH;
				if(!itVBar.getValueIsAdjusting() &&
				   itVBar.getValue()!=itVal){
				    itVBar.setValue(itVal);
				}
				ctVBar.addAdjustmentListener(this);
				itVBar.addAdjustmentListener(this);
			    }else if(sourceBar == itVBar){
				ctVBar.removeAdjustmentListener(this);
				ptVBar.removeAdjustmentListener(this);
				int ctVal =(value/(3*fontH))*fontH+value%fontH;
				if(!ctVBar.getValueIsAdjusting() &&
				   ctVBar.getValue()!=ctVal){
				    ctVBar.setValue(ctVal);
				}
				if(!ptVBar.getValueIsAdjusting() &&
				   ptVBar.getValue()!=ctVal){
				    ptVBar.setValue(ctVal);
				}
				ctVBar.addAdjustmentListener(this);
				ptVBar.addAdjustmentListener(this);
			    }
			}else{
			    if(sourceBar == ctHBar){
				if(!ptHBar.getValueIsAdjusting() &&
				   ptHBar.getValue()!=value){
				    ptHBar.setValue(value);
				}
				if(itHBar.getValue()!=value){
				    itHBar.setValue(value);
				}
			    }else if(sourceBar==ptHBar &&
				     !ctHBar.getValueIsAdjusting() &&
				     ctHBar.getValue()!=value){
				ctHBar.setValue(value);
			    }else if(sourceBar == itHBar &&
				     !ctHBar.getValueIsAdjusting() &&
				     ctHBar.getValue()!=value){
				ctHBar.setValue(value);
			    }
			}
			moveCarets(); // to avoid flicker when using setText
		    }
		}
	    };
	ctVBar.addAdjustmentListener(adVert);
	ptVBar.addAdjustmentListener(adVert);
	itVBar.addAdjustmentListener(adVert);
	ctHBar.addAdjustmentListener(adVert);
	ptHBar.addAdjustmentListener(adVert);
	itHBar.addAdjustmentListener(adVert);
    }

    /**
     * Tries to place the carets of <code>ciphertextArea</code>, 
     * <code>plaintextArea</code> and <code>intercalatedArea</code> somewhere
     * in the visible area of their respective <code>JScrollPane</code>s.
     * Having the carets in the visible portion allows for a flickerless 
     * <code>setText()</code>
     */
    private void moveCarets(){
	moveCaret(ciphertextArea, ciphertextScroll);
	moveCaret(plaintextArea, plaintextScroll);
	moveCaret(intercalatedArea, intercalatedScroll);
    }

    /**
     * Tries to place the carets of <code>textArea</code>, 
     * somewhere in the visible area of its <code>JScrollPane</code>.
     * Having the caret in the visible portion allows for a flickerless 
     * <code>setText()</code>.
     *
     * @param textArea a <code>JTextArea</code>
     * @param scrollPane the <code>JScrollPane</code> textArea is in
     * @throws NullPointerException if any of the arguments is <code>null</code>
     */
    private void moveCaret(JTextArea textArea, 
			   JScrollPane scrollPane) throws NullPointerException
    {
	if(textArea.getSelectedText()!=null){
	    return;
	}
	int fh = textArea.getFontMetrics(textArea.getFont()).getHeight()/2;
	fh = fh > 0 ? fh : 1;
	int caretDot = textArea.getCaret().getDot();
	Rectangle mod2View = null;
	Rectangle viewRect = null;
	try{
	    mod2View = textArea.modelToView(caretDot);
	}catch(javax.swing.text.BadLocationException ble){
	    assert false: "getDot returned an invalid caret position";
	}
	viewRect = scrollPane.getViewport().getViewRect();
	if(mod2View!=null && viewRect.width>0 && viewRect.height>0 &&
	   !viewRect.contains(mod2View)){
	    int yMax = viewRect.y + viewRect.height;
	    Point midPnt = new Point(viewRect.width/2 + viewRect.x,
				     viewRect.height/2 + viewRect.y);
	    /* Look for a valid caret position inside viewRect from the middle
	       down. I start looking from the middle to avoid performing this
	       operation if the textArea is scrolled half a screen up or down*/
	    int i = midPnt.y;
	    while(i<yMax && !viewRect.contains(mod2View)){
		caretDot = textArea.viewToModel(midPnt);
		if(caretDot >= 0){
		    try{
			mod2View = textArea.modelToView(caretDot);
		    }catch(javax.swing.text.BadLocationException ble){
			assert false: 
			    "viewToModel returned an invalid position";
		    }
		}
		i+=fh;
		midPnt.y = i;
	    }
	    /* Look for a valid caret position inside viewRect from the middle
	       up. */
	    i = midPnt.y - fh;
	    while(i>viewRect.y && !viewRect.contains(mod2View)){
		caretDot = textArea.viewToModel(midPnt);
		if(caretDot >= 0){
		    try{
			mod2View = textArea.modelToView(caretDot);
		    }catch(javax.swing.text.BadLocationException ble){
			assert false: 
			    "viewToModel returned an invalid position";
		    }
		}
		i-=fh;
		midPnt.y = i;
	    }
	    if(mod2View != null && viewRect.contains(mod2View)){
		textArea.getCaret().setDot(caretDot);
	    }
	}
    }

    /**
     * Sets the view coordinates of the <code>JScrollPane</code>s'
     * viewports to <code>(0, 0)</code>, so they show the top of the
     * document.
     */
    public void viewTop(){
	ciphertextScroll.getViewport().setViewPosition(new Point(0,0));
    }

    /**
     * Sets the ciphertext and plaintext to those passed as argument.<br/>
     *
     * Note that this will not change the view coordinates of the viewports,
     * so you may want to call <code>viewTop()</code> before using this
     * method.
     *
     * @param cipher the new ciphertext
     * @param plain the plaintext
     * @throws NullPointerException if <code>cipher</code> or
     *                      <code>plain</code> are <code>null</code>
     * @throws IllegalArgumentException if <code>cipher</code> and 
     *                      <code>plain</code> have different number of lines.
     * @see #viewTop()
     */
    public void setText(String cipher,
			String plain) throws NullPointerException,
					     IllegalArgumentException
    {
	/* The method setText of the class JTextArea makes the parent
	   JScrollPane scroll to the last line. Since I do not know how
	   to avoid this, I store the current view position of the
	   JScrollPane and set it after calling setText */
	final Point ciphertextVP = new Point(ciphertextScroll.getViewport().getViewPosition());
	final Point plaintextVP = new Point(plaintextScroll.getViewport().getViewPosition());
	final Point intercalatedVP = new Point(intercalatedScroll.getViewport().getViewPosition());
	if(cipher == null || plain == null){
	    throw new NullPointerException();
	}
	ciphertext = cipher;
	plaintext = plain;
	String[] splitCipher = ciphertext.split("\n");
	String[] splitPlain = plaintext.split("\n");
	if(splitCipher.length != splitPlain.length){
	    throw new IllegalArgumentException("Plaintext and ciphertext have different number of lines");
	}
	StringBuffer sb = new StringBuffer();
	for(int i=0; i<splitCipher.length; i++){
	    /* The if was commented out to make intercalatedArea have
	       3*number_of_lines_plaintextArea_has.
	       This helps keep track of the lines the user is looking at
	       when changing from INTERCALATED to SEPARATE mode and
	       vice versa */
	    //if(!splitCipher[i].equals("")){
		sb.append(splitCipher[i]).append('\n');
		sb.append(splitPlain[i]).append("\n\n");
	    //}
	}
	/* Since this method will be called mostly to change the plaintext,
	   do not modify the contents of ciphertextArea when possible */
	int dot; // stores the position of the JTextAreas' carets
	if(!ciphertextArea.getText().equals(ciphertext)){
	    dot = ciphertextArea.getCaret().getDot();
	    ciphertextArea.setText(ciphertext);
	    ciphertextArea.getCaret().setDot(dot);
	}
	dot = plaintextArea.getCaret().getDot();
	plaintextArea.setText(plaintext);
	plaintextArea.getCaret().setDot(dot);
	dot = intercalatedArea.getCaret().getDot();
	intercalatedArea.setText(sb.toString());
	intercalatedArea.getCaret().setDot(dot);
	// Set the view positions back to where they were.
	SwingUtilities.invokeLater(new Runnable(){
		public void run(){
		    ciphertextScroll.getViewport().setViewPosition(ciphertextVP);
		    plaintextScroll.getViewport().setViewPosition(plaintextVP);
		    intercalatedScroll.getViewport().setViewPosition(intercalatedVP);
		}
	    });
	fireStateChanged();
    }

    /**
     * Returns an <code>Action</code> that copies the selected region of 
     * text onto the system's clipboard.
     *
     * @return an <code>Action</code> that copies the selected region of  
     *         text onto the system's clipboard.
     * @see javax.swing.text.DefaultEditorKit
     */
    public Action getCopyAction(){
	if(copyAction == null){
	    Action[] actions = ciphertextArea.getActions();
	    int i=0;
	    while(i<actions.length && 
		  actions[i].getValue(Action.NAME)!=DefaultEditorKit.copyAction){
		i++;
	    }
	    copyAction = actions[i];
	    copyAction.putValue(Action.NAME, labelsRB.getString("cpAtn"));
	    copyAction.putValue(Action.SHORT_DESCRIPTION, 
				labelsRB.getString("cpAtnTT"));
	    copyAction.putValue(Action.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_C,
						       Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
	}
	return copyAction;
    }

    /**
     * Returns the <code>CiphertextPanel</code>'s mode 
     * (<code>INTERCALATE<code> or <code>SEPARATE</code>).
     *
     * @see #INTERCALATE
     * @see #SEPARATE
     */
    public byte getMode(){
	return mode;
    }

    /**
     * Sets the <code>CiphertextPanel</code>'s mode
     *
     * @param mode the mode to set the <code>CiphertextPanel</code> to. It must
     *             be either <code>INTERCALATE</code> or <code>SEPARATE</code>
     * @throws IllegalArgumentException if <code>mode</code> is not
     *             <code>INTERCALTE</code> or <code>SEPARATE</code>
     */
    public void setMode(byte mode) throws IllegalArgumentException
    {
	if(this.mode == mode){
	    return;
	}
	switch(mode){
	    case INTERCALATE:
		this.mode = mode;
		interMI.setSelected(true);
		removeAll();
		add(intercalatedPanel);
		revalidate();
		repaint();
		break;
	    case SEPARATE:
		this.mode = mode;
		separMI.setSelected(true);
		removeAll();
		add(separatedPanel);
		revalidate();
		repaint();
		break;
	    default: throw new IllegalArgumentException("Invalid mode");
	}
	fireStateChanged();
    }

    /**
     * This method is public as an implementation side effect. Do not call or
     * override.
     * <br/>
     * If the <code>String</code> returned by <code>ActionEvent</code>'s 
     * <code>getActionCommand()</code> method equals 
     * <code>&quot;INTERCALATE&quot;</code> this method invoques
     * <code>setMode(INTERCALATE)</code> and if it equals
     * <code>&quot;SEPARATED&quot;</code> it invoques
     * <code>setMode(SEPARATE)</code>.
     *
     * @see #setMode
     */
    public void actionPerformed(ActionEvent e){
	if(e.getActionCommand().equals("INTERCALATE")){
	    setMode(INTERCALATE);
	}else if(e.getActionCommand().equals("SEPARATE")){
	    setMode(SEPARATE);
	}
    }

    /**
     * Adds a <code>ChangeListener</code> to the <code>CiphertextPanel</code>.
     * <br/>
     * The <code>ChangeListener</code> will receive a <code>ChangeEvent</code>
     * when the display mode or the text change.
     *
     * @param l the <code>ChangeListener</code> that sould be notified
     */
    public void addChangeListener(ChangeListener l){
	listenerList.add(ChangeListener.class, l);
    }

    /**
     * Removes a <code>ChangeListener</code> from the 
     * <code>CiphertextPanel</code>.
     *
     * @param l the <code>ChangeListener</code> to be removed
     */
    public void removeChangeListener(ChangeListener l){
	listenerList.remove(ChangeListener.class, l);
    }

    /**
     * Returns an array of all the <code>ChangeListeners</code> added to 
     * this <code>CiphertextPanel</code> with <code>addChangeListener()</code>
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
     * <code>ChangeEvent</code>.
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
     * <code>MouseListener</code> that makes <code>modeMenu</code>
     * visible.
     */
    private class CiphertextPanelMouseListener extends MouseAdapter{
	public void mousePressed(MouseEvent e){
	    maybeShowPopup(e);
	}

	public void mouseReleased(MouseEvent e){
	    maybeShowPopup(e);
	}

	private void maybeShowPopup(MouseEvent e){
	    if(e.isPopupTrigger()){
		modeMenu.show(e.getComponent(),
			      e.getX(), e.getY());
	    }
	}
    }

    /**
     * A small program used to test the component
     */
    public static void main(String[] args){
	JFrame frame = new JFrame("Test");
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	String ciphertext = "ABCDEFGHI\nJKLMNÑOPQ\nRSTUVWXYZ";
	String plaintext = "abcdefghi\njklmnñopq\nrstuvwxyz";
	final CiphertextPanel cipherPan = new CiphertextPanel(ciphertext, plaintext);
	frame.getContentPane().add(cipherPan);
	frame.pack();
	frame.setVisible(true);
    }
}
/*
 * -- CiphertextPanel.java ends here --
 */
