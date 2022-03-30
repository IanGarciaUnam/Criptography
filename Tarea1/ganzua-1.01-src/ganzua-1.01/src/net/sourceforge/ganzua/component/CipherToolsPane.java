/*
 * -- CipherToolsPane.java --
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

import java.util.ResourceBundle;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Set;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import javax.swing.tree.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import net.sourceforge.ganzua.text.*;
import net.sourceforge.ganzua.event.*;
// used in main(String[])
import java.util.Locale;
import java.util.ArrayList;
import java.text.Collator;

/**
 * Component used in <code>Analyzer</code>s to provide tools for
 * monoalphabetic and polyalphabetic ciphers, and to control the mode (cipher)
 * in which it is working (Caesar, monoalphabetic, Vigenère or Alberti).
 *
 * @author Jesús Adolfo García Pasquel
 * @version 0.01 April 2004
 */
public class CipherToolsPane extends JPanel{

    /**
     * Constant used to set the cipher for which the tools are displayed.
     * The value of the constant is the one used by instances of the schema
     * <code>Cryptanalysis.xsd</code> to identify the Caesar cipher. */
    public static final String CAESAR = "Caesar";

    /**
     * Constant used to set the cipher for which the tools are displayed.
     * The value of the constant is the one used by instances of the schema
     * <code>Cryptanalysis.xsd</code> to identify the Monoalphabetic cipher. */
    public static final String MONOALPHABETIC = "Monoalphabetic";

    /**
     * Constant used to set the cipher for which the tools are displayed.
     * The value of the constant is the one used by instances of the schema
     * <code>Cryptanalysis.xsd</code> to identify the Vigenère cipher. */
    public static final String VIGENERE = "Vigenère";

    /**
     * Constant used to set the cipher for which the tools are displayed.
     * The value of the constant is the one used by instances of the schema
     * <code>Cryptanalysis.xsd</code> to identify the Alberti cipher. */
    public static final String ALBERTI = "Alberti";

    /**
     * The <code>Substitution</code> that belongs to the 
     * <code>Analyzer</code> this <code>CipherToolsPane</code> is part of.*/
    protected Substitution subst;

    /**
     * The <code>CiphertextManger</code> that belongs to the
     * <code>Analyzer</code> this <code>CipherToolsPane</code> is part of.*/
    protected CiphertextManager cipherM;

    /**
     * <code>JTree</code> that contains the kinds of ciphers
     * for which this class provides tools */
    protected JTree cipherTree;

    protected DefaultMutableTreeNode root;

    protected DefaultMutableTreeNode caesar;

    protected DefaultMutableTreeNode mono;

    protected DefaultMutableTreeNode vigenere;

    protected DefaultMutableTreeNode alberti;

    /**
     * <code>false</code> by default.  If <code>autoCollapse</code> is 
     * <code>true</code>, the branches of <code>cipherTree</code> are 
     * collapsed when the newly selected path does not include them. */
    protected boolean autoCollapse = false;

    /**
     * <code>TableModel</code> that stores the data that results from
     * applying the Kasiski Test.
     */
    protected KasiskiTableModel kasiskiMdl;
    
    /**
     * <code>JTable</code> used to display the data in <code>kasiskiMdl</code>
     */
    protected JTable kasiskiJT;

    /**
     * <code>JScrollPane</code> used to display <code>kasiskiJT</code>
     */
    protected JScrollPane kasiskiScroll;

    /**
     * <code>JFrame</code> used to display <code>kasiskiScroll</code>
     */
    protected JFrame kasiskiFrame;

    /**
     * Boolean used to indicate if the data in <code>kasiskiMdl</code>
     * is up to date. If it is, then it does not have to be recalculated. 
     */
    private boolean kasiskiDataCurrent = false;

    /**
     * The node selected after the last <code>TreeSelectionEvent</code>. */
    private DefaultMutableTreeNode selNode;

    /**
     * <code>Action</code> that clears the selected replacement characters 
     * in the monoalphabetic substitution being displayed by 
     * <code>subst</code> */
    private ClearSelectionAction clearSelActn;

    /**
     * <code>Action</code> that clears the selected replacement characters
     * and sets a substitution as close to the identity substitution as the
     * plain alphabet permits. */
    private SelectIdentityAction selectIdActn;

    /**
     * <code>Action</code> that completes the current selection with
     * the remaining characters of the plain alphabet. */
    private CompleteSelectionAction compltSelActn;

    /**
     * <code>Action</code> that reverses the order of the selected substitution
     * characters in the currently selected */
    private ReverseSelectionAction revSelActn;

    /**
     * <code>Action</code> that clears the selected replacement characters
     * and sets a substitution as close to the inverse of the substitution
     * present before clearing as the plain alphabet and cipher alphabet
     * permit. */
    private InvertSelectionAction invSelActn;

    /**
     * <code>Action</code> that shifts the replacement characters
     * to the right. */
    private ShiftSelectionAction shiftRightActn;

    /**
     * <code>Action</code> that shifts the selected replacement characters
     * to the left. */
    private ShiftSelectionAction shiftLeftActn;

    /**
     * <code>Action</code> that performs the Kasiski Test and makes the
     * results visible in <code>kasiskiFrame</code> */
    private KasiskiAction kasiskiActn;

    /**
     * <code>Action</code> that brings up a dialog that lets the user
     * group the ciphertext's characters in blocks. */
    private GroupCharsAction groupActn;


    /**
     * <code>Action</code> that copies the selection and ignored characters
     * of the first monoalphabetic substitution to the one currently selected.
     */
    private CopyFirstSelectionAction copySelActn;

    /**
     * <code>ResourceBundle</code> with localized labels */
    private ResourceBundle labelsRB;

    /**
     * Constructor that receives the <code>Substitution</code> and
     * <code>CiphertextManager</code> the <code>CipherToolsPane</code>
     * operates on and gives tools to.<br/>
     *
     * @param subst a <code>Substitution</code>
     * @param cipherM a <code>CiphertextManager</code>
     */
    public CipherToolsPane(Substitution subst, 
			   CiphertextManager cipherM) throws NullPointerException
    {
	super();
	if(subst == null || cipherM == null){
	    throw new NullPointerException();
	}
	labelsRB = ResourceBundle.getBundle(CipherToolsPane.class.getName(),
					    getDefaultLocale());
	this.subst = subst;
	this.cipherM = cipherM;
	initActions();
	root = new DefaultMutableTreeNode(labelsRB.getString("Ciphers"));
	buildTree(root);
	cipherTree = new JTree(root);
	addTreeSelectionListener();
	initGUI();
	cipherTree.setSelectionPath(new TreePath(mono.getPath()));
	addChangeListenerToSubst();
    }

    /**
     * Initializes the <code>Action</code>s used by the 
     * <code>CipherToolsPane</code>'s controls.
     */
    private void initActions(){
	clearSelActn = new ClearSelectionAction();
	clearSelActn.putValue(Action.NAME, labelsRB.getString("clearSelActn"));
	clearSelActn.putValue(Action.SHORT_DESCRIPTION,
			      labelsRB.getString("clearSelActnTT"));
	selectIdActn = new SelectIdentityAction();
	selectIdActn.putValue(Action.NAME, labelsRB.getString("selectIdActn"));
	selectIdActn.putValue(Action.SHORT_DESCRIPTION,
			      labelsRB.getString("selectIdActnTT"));
	compltSelActn = new CompleteSelectionAction();
	compltSelActn.putValue(Action.NAME, 
			       labelsRB.getString("compltSelActn"));
	compltSelActn.putValue(Action.SHORT_DESCRIPTION,
			       labelsRB.getString("compltSelActnTT"));
	revSelActn = new ReverseSelectionAction();
	revSelActn.putValue(Action.NAME, 
			    labelsRB.getString("revSelActn"));
	revSelActn.putValue(Action.SHORT_DESCRIPTION,
			    labelsRB.getString("revSelActnTT"));
	invSelActn = new InvertSelectionAction();
	invSelActn.putValue(Action.NAME, 
			    labelsRB.getString("invSelActn"));
	invSelActn.putValue(Action.SHORT_DESCRIPTION,
			    labelsRB.getString("invSelActnTT"));
	shiftRightActn = new ShiftSelectionAction(MonoAlphaSubst.RIGHT);
	shiftRightActn.putValue(Action.NAME, labelsRB.getString("shiftRightActn"));
	shiftRightActn.putValue(Action.SHORT_DESCRIPTION,
				labelsRB.getString("shiftRightActnTT"));
	shiftLeftActn = new ShiftSelectionAction(MonoAlphaSubst.LEFT);
	shiftLeftActn.putValue(Action.NAME, labelsRB.getString("shiftLeftActn"));
	shiftLeftActn.putValue(Action.SHORT_DESCRIPTION,
			       labelsRB.getString("shiftLeftActnTT"));
	kasiskiActn = new KasiskiAction();
	kasiskiActn.putValue(Action.NAME, labelsRB.getString("kasiskiActn"));
	kasiskiActn.putValue(Action.SHORT_DESCRIPTION,
			     labelsRB.getString("kasiskiActnTT"));
	groupActn = new GroupCharsAction();
	groupActn.putValue(Action.NAME, labelsRB.getString("groupActn"));
	groupActn.putValue(Action.SHORT_DESCRIPTION,
			   labelsRB.getString("groupActnTT"));
	copySelActn = new CopyFirstSelectionAction();
	copySelActn.putValue(Action.NAME, labelsRB.getString("copySelActn"));
	copySelActn.putValue(Action.SHORT_DESCRIPTION,
			     labelsRB.getString("copySelActnTT"));
    }

    /**
     * Builds the tree with the different ciphers the <code>JTree</code> 
     * <code>cipherTree</code> displays.
     *
     * @param root the root node of the tree
     */
    private void buildTree(DefaultMutableTreeNode root){
	DefaultMutableTreeNode tmp = new DefaultMutableTreeNode(labelsRB.getString("MonoC"));
	caesar = new DefaultMutableTreeNode(new CaesarToolsPane(labelsRB.getString("Caesar")));
	mono = new DefaultMutableTreeNode(new MonoToolsPane(labelsRB.getString("Mono")));
	vigenere = new DefaultMutableTreeNode(new VigenereToolsPane(labelsRB.getString("Vigenere")));
	alberti = new DefaultMutableTreeNode(new AlbertiToolsPane(labelsRB.getString("Alberti")));
	tmp.add(caesar);
	tmp.add(mono);
	root.add(tmp);
	tmp = new DefaultMutableTreeNode(labelsRB.getString("Poly"));
	tmp.add(vigenere);
	tmp.add(alberti);
	root.add(tmp);
    }

    /**
     * Add the listener that is needed to let the user change ciphers to 
     * <code>cipherTree</code>.
     */
    private void addTreeSelectionListener(){
	cipherTree.addTreeSelectionListener(new TreeSelectionListener(){
		public void valueChanged(TreeSelectionEvent e){
		    DefaultMutableTreeNode node = (DefaultMutableTreeNode)cipherTree.getLastSelectedPathComponent();

		    if(node == null) return;

		    if(node.isLeaf()){
			ToolPane tPane = (ToolPane)node.getUserObject();
			if(selNode != null){
			    remove((ToolPane)selNode.getUserObject());
			    boolean prevMono; // privious node monoalphabetic
			    boolean currMono; // current nodoe monoalphabetic
			    prevMono = selNode==caesar || selNode==mono;
			    currMono = node==caesar || node==mono;
			    if(prevMono ^ currMono){
				if(currMono){
				    subst.setNumberOfAlphabets(1);
				} else{
				    int val = ((AlbertiToolsPane)tPane).getSpinnerValue();
				    subst.setNumberOfAlphabets(val);
				}
			    }else if(!prevMono && !currMono){
				int al = subst.getNumberOfAlphabets();
				((AlbertiToolsPane)tPane).setSpinnerValue(al);
			    }
			    //collapse previously selected branch
			    if(autoCollapse){
				DefaultMutableTreeNode prevParent = (DefaultMutableTreeNode)selNode.getParent();
				if(prevParent != null &&
				   !prevParent.isNodeChild(node)){
				    cipherTree.collapsePath(new TreePath(prevParent.getPath()));
				}
			    }
			}
			add(tPane, BorderLayout.CENTER);
			revalidate();
			repaint();
			selNode = node;
		    } else if(selNode != null){
			cipherTree.setSelectionPath(new TreePath(selNode.getPath()));
		    }
		}
	    });
    }

    /**
     * Initializes the component's GUI.
     */
    private void initGUI(){
	setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0, 3, 0, 0),
						     BorderFactory.createEtchedBorder()));
	cipherTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
	cipherTree.setExpandsSelectedPaths(true);
	cipherTree.setBorder(BorderFactory.createEtchedBorder());
	// Set cipherTree's peferredSize
	Enumeration children = root.children();
	ArrayList childrenPaths = new ArrayList();
	while(children.hasMoreElements()){
	    DefaultMutableTreeNode tn = (DefaultMutableTreeNode)children.nextElement();
	    TreePath tPath = new TreePath(tn.getPath());
	    cipherTree.expandPath(tPath);
	    childrenPaths.add(tPath);
	}
	Dimension dim = cipherTree.getPreferredSize();
	Iterator iterator = childrenPaths.iterator();
	while(iterator.hasNext()){
	    cipherTree.collapsePath((TreePath)iterator.next());
	}
	cipherTree.setPreferredSize(dim);
	// set the layout and add the JTree
	setLayout(new BorderLayout());
	add(cipherTree, BorderLayout.NORTH);
	// initialize Kasiski's JFrame, JTable and JTableModel
	kasiskiFrame = new JFrame(labelsRB.getString("kasiskiFrame"));
	kasiskiMdl = new KasiskiTableModel(new ArrayList());
	kasiskiJT = new JTable(kasiskiMdl);
	kasiskiJT.setDragEnabled(true); // enable automatic drag handling
	// set AUTO_RESIZE_OFF so the table can be scrolled horizontally
	kasiskiJT.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	kasiskiMdl.addMouseListenerToHeaderInTable(kasiskiJT);
	kasiskiScroll = new JScrollPane(kasiskiJT,
					JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
					JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
	kasiskiFrame.getContentPane().add(kasiskiScroll);
	kasiskiFrame.pack();
	resizeTable(kasiskiJT, 
		    kasiskiScroll.getViewport().getExtentSize().width);
	//set the CipherToolsPane's preferred size
	Dimension prefSize = getPreferredSize();
	Dimension treeDim = cipherTree.getPreferredSize();
	Dimension panDims[] = new Dimension[4];
	panDims[0] = ((ToolPane)mono.getUserObject()).getPreferredSize();
	panDims[1] =((ToolPane)caesar.getUserObject()).getPreferredSize();
	panDims[2] =  ((ToolPane)vigenere.getUserObject()).getPreferredSize();
	panDims[3] = ((ToolPane)alberti.getUserObject()).getPreferredSize();
	int prefWidthToolPanes;
	//set the preferred with
	prefWidthToolPanes = panDims[0].width;
	for(int i=1; i<panDims.length; i++){
	    prefWidthToolPanes = Math.max(prefWidthToolPanes,
					  panDims[i].width);
	}
	/* add the width of a vertical JScrollBar so no part of the tools gets
	   chopped off if the ToolPane has to show a scrollbar */
	prefWidthToolPanes += kasiskiScroll.getVerticalScrollBar().getPreferredSize().width;
	prefSize.width = Math.max(prefSize.width, treeDim.width);
	prefSize.width = Math.max(prefSize.width, prefWidthToolPanes);
	// set the preferred height
	for(int i=0; i<panDims.length; i++){
	    prefSize.height = Math.max(prefSize.height, 
				       panDims[i].height + treeDim.height);
	}
	prefSize.width += 5;
	prefSize.height += 5;
	setPreferredSize(prefSize);
    }

    /**
     * Adds a <code>ChangeListener</code> to <code>subst</code> that
     * listens for <code>SubstitutionEvent</code>s of type 
     * <code>IGNORED_CHARACTERS</code> (that originate form alphabet 0
     * or an unknown alphabet) or <code>CIPHER_ALPHABET</code>. The
     * <code>ChangeListener</code> updates <code>kasiskiMdl</code>'s data if
     * <code>kasiskiFrame</code> is visible and sets 
     * <code>kasiskiDataCurrent</code> to <code>false</code> otherwise. In the
     * case of <code>IGNORED_CHARACTERS</code>, we only consider those
     * in alphabet 0 because those are the only ignored characters we use
     * when applying the Kasiski Test.
     */
    private void addChangeListenerToSubst(){
	subst.addChangeListener(new ChangeListener(){
		public void stateChanged(ChangeEvent e){
		    SubstitutionEvent se = (SubstitutionEvent)e;
		    byte changeType = se.getChangeType();
		    if(changeType == SubstitutionEvent.CIPHER_ALPHABET ||
		       (changeType == SubstitutionEvent.IGNORED_CHARACTERS &&
			se.getAffectedAlphabetIndex() < 1)){
			kasiskiDataCurrent = false;
			if(kasiskiFrame.isVisible()){
			    SwingUtilities.invokeLater(kasiskiActn);
			}
		    }
		}
	    });
    }

    /**
     * <code>Action</code> used by the "Clear" button. The action clears
     * the substitution's selections in the currently selected alphabet.
     *
     * @see Substitution#clearSelectionCurrMono()
     */
    private class ClearSelectionAction extends AbstractAction{
	public void actionPerformed(ActionEvent e){
	    subst.clearSelectionCurrMono();
	}
    }

    /**
     * <code>Action</code> used by the "Identity" button. The action selects
     * the ideintity on the substitution's currently selected alphabet.
     *
     * @see Substitution#selectIdentityCurrMono()
     */
    private class SelectIdentityAction extends AbstractAction{
	public void actionPerformed(ActionEvent e){
	    subst.selectIdentityCurrMono();
	}
    }

    /**
     * <code>Action</code> used by the "Complete" button. The action completes
     * the selections in the substitution's currently selected alphabet.
     *
     * @see Substitution#completeSelectionCurrMono()
     */
    private class CompleteSelectionAction extends AbstractAction{
	public void actionPerformed(ActionEvent e){
	    subst.completeSelectionCurrMono();
	}
    }

    /**
     * <code>Action</code> used by the "Reverse" button. The action sets
     * the selection in the substitution's currently selected alphabet in
     * reverse order.
     *
     * @see Substitution#reverseSelectionCurrMono()
     */
    private class ReverseSelectionAction extends AbstractAction{
	public void actionPerformed(ActionEvent e){
	    subst.reverseSelectionCurrMono();
	}
    }

    /**
     * <code>Action</code> used by the "Invert" button. The action inverts
     * the selection in the substitution's currently selected alphabet.
     *
     * @see Substitution#invertSelectionCurrMono()
     */
    private class InvertSelectionAction extends AbstractAction{
	public void actionPerformed(ActionEvent e){
	    subst.invertSelectionCurrMono();
	}
    }

    /**
     * <code>Action</code> used by the shift controls. The controls shift
     * the selection in the substitution's currently selected alphabet to
     * the right or left.
     */
    private class ShiftSelectionAction extends AbstractAction{
	int direction;

	/**
	 * Constructor that receives the direction to which the
	 * <code>ShiftSelectionAction</code> shifts the selection. 
	 * 
	 * @param direction either <code>MonoAlphaSubst.LEFT</code> or
	 *                  <code>MonoAlphaSubst.RIGHT</code>
	 */
	public ShiftSelectionAction(int direction) throws IllegalArgumentException
	{
	    super();
	    if(direction!=MonoAlphaSubst.RIGHT && 
	       direction!=MonoAlphaSubst.LEFT){
		throw new IllegalArgumentException();
	    }
	    this.direction = direction;
	}

	/**
	 * Shift the selection in the substitution's currently selected
	 * alphabet to the right or left.
	 *
	 * @see Substitution#shiftSelectionCurrMono(int)
	 */
	public void actionPerformed(ActionEvent e){
	    subst.shiftSelectionCurrMono(direction);
	}
    }

    /**
     * <code>Action</code> used by the "Kasiski" button. Performs the Kasiski
     * Test and displays the results in a frame.
     */
    private class KasiskiAction extends AbstractAction
				implements Runnable
    {
	/**
	 * Performs the Kasiski Test.
	 *
	 * @see CiphertextManager#getKasiski(Set)
	 */
	public void run(){
	    ArrayList tmpLst = subst.getIgnoredCharacters();
	    Set tmpSet = (Set)tmpLst.get(0);
	    kasiskiMdl.setData(cipherM.getKasiski(tmpSet));
	    resizeTable(kasiskiJT, 
			kasiskiScroll.getViewport().getExtentSize().width);
	    kasiskiScroll.getViewport().setViewPosition(new Point());
	    kasiskiDataCurrent = true;
	}

	/**
	 * Calls <code>run</code> (if the data in the frame that displays
	 * the results is not current) and makes the frame visible.
	 *
	 * @param e an <code>ActionEvent</code>
	 */
	public void actionPerformed(ActionEvent e){
	    if(!kasiskiDataCurrent){
		run();
	    }
	    kasiskiFrame.setVisible(true);
	}
    }

    /**
     * <code>Action</code> used by the "Group" button. The action displays
     * a dialog that lets the user set the number of characters each block
     * should have, and groups the characters.
     *
     * @see CiphertextManager#setCiphertextInBlocksOf(int)
     */
    private class GroupCharsAction extends AbstractAction{
	public void actionPerformed(ActionEvent e){
	    int blkSize = SpinnerDialog.showSpinnerDialog(subst.getParent().getParent(), labelsRB.getString("groupCharsDlgMsg"), subst.getNumberOfAlphabets(), labelsRB.getString("groupCharsDlgTtl"));
	    if(blkSize > 0){
		cipherM.setCiphertextInBlocksOf(blkSize);
	    }
	}
    }

    /**
     * <code>Action</code> used by the "Subst 1" button. The action copies
     * the selections from first alphabet's substitution to the one of the
     * currently selected alphabet.
     *
     * @see Substitution#copyFirstSelectionToCurrMono()
     */
    private class CopyFirstSelectionAction extends AbstractAction{
	public void actionPerformed(ActionEvent e){
	    subst.copyFirstSelectionToCurrMono();
	}
    }

    /**
     * <code>ToolPane</code> that contains the tools for the monoalphabetic
     * ciphers.
     */
    private class MonoToolsPane extends ToolPane{

	public MonoToolsPane(String name){
	    super(name);
	    JButton clear = new JButton(clearSelActn);
	    clear.setAlignmentX(Component.CENTER_ALIGNMENT);
	    clear.setAlignmentY(Component.CENTER_ALIGNMENT);
	    add(clear);
	    JButton identity = new JButton(selectIdActn);
	    identity.setAlignmentX(Component.CENTER_ALIGNMENT);
	    identity.setAlignmentY(Component.CENTER_ALIGNMENT);
	    add(identity);
	    JButton complete = new JButton(compltSelActn);
	    complete.setAlignmentX(Component.CENTER_ALIGNMENT);
	    complete.setAlignmentY(Component.CENTER_ALIGNMENT);
	    add(complete);
	    JButton reverse = new JButton(revSelActn);
	    reverse.setAlignmentX(Component.CENTER_ALIGNMENT);
	    reverse.setAlignmentY(Component.CENTER_ALIGNMENT);
	    add(reverse);
	    JButton invert = new JButton(invSelActn);
	    invert.setAlignmentX(Component.CENTER_ALIGNMENT);
	    invert.setAlignmentY(Component.CENTER_ALIGNMENT);
	    add(invert);
	}
    }

    /**
     * <code>MonoToolsPane</code> that contains the tools for Caesar's 
     * monoalphabetic cipher.
     */
    private class CaesarToolsPane extends MonoToolsPane{
	public CaesarToolsPane(String name){
	    super(name);
	    JPanel shiftPan = new JPanel();
	    shiftPan.setLayout(new BoxLayout(shiftPan, BoxLayout.X_AXIS));
	    TitledBorder border = BorderFactory.createTitledBorder(labelsRB.getString("shiftPan"));
	    border.setTitlePosition(TitledBorder.BOTTOM);
	    border.setTitleJustification(TitledBorder.CENTER);
	    shiftPan.setBorder(border);
	    JButton shiftLeft = new JButton(shiftLeftActn);
	    JButton shiftRight = new JButton(shiftRightActn);
	    if(shiftLeft.getFont().canDisplayUpTo(shiftLeft.getText()) != -1 ||
	       shiftRight.getFont().canDisplayUpTo(shiftRight.getText())!=-1){
		shiftLeft.setText("<<");
		shiftRight.setText(">>");
	    }
	    shiftPan.add(shiftLeft);
	    shiftPan.add(shiftRight);
	    shiftPan.setAlignmentX(Component.CENTER_ALIGNMENT);
	    shiftPan.setAlignmentY(Component.CENTER_ALIGNMENT);
	    add(shiftPan);
	}
    }

    /**
     * <code>ToolPane</code> that contains the tools for Alberti's 
     * polyalphabetic cipher.
     */
    private class AlbertiToolsPane extends ToolPane{

	protected SpinnerNumberModel spinMdl;

	protected JSpinner numAlpha;

	public AlbertiToolsPane(String name){
	    super(name);
	    spinMdl = new SpinnerNumberModel(new Integer(2),
					     new Integer(2),
					     null,
					     new Integer(1));	    
	    numAlpha = new JSpinner(spinMdl);
	    numAlpha.setAlignmentX(Component.CENTER_ALIGNMENT);
	    numAlpha.setAlignmentY(Component.CENTER_ALIGNMENT);
	    numAlpha.setMaximumSize(new Dimension(115, numAlpha.getPreferredSize().height));
	    numAlpha.addChangeListener(new ChangeListener(){
		    public void stateChanged(ChangeEvent e){
			SpinnerNumberModel spinMdl = (SpinnerNumberModel)numAlpha.getModel();
			subst.setNumberOfAlphabets(spinMdl.getNumber().intValue());
		    }
		});
	    ((JSpinner.NumberEditor)numAlpha.getEditor()).getTextField().setToolTipText(labelsRB.getString("numAlphaTT"));
	    JPanel numAlphaJP = new JPanel();
	    numAlphaJP.setLayout(new BoxLayout(numAlphaJP, BoxLayout.X_AXIS));
	    numAlphaJP.add(numAlpha);
	    TitledBorder border = BorderFactory.createTitledBorder(labelsRB.getString("numAlphaBdr"));
	    border.setTitlePosition(TitledBorder.TOP);
	    border.setTitleJustification(TitledBorder.CENTER);
	    numAlphaJP.setBorder(border);
	    add(numAlphaJP);

	    JButton kasiskiBtn = new JButton(kasiskiActn);
	    kasiskiBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
	    kasiskiBtn.setAlignmentY(Component.CENTER_ALIGNMENT);
	    add(kasiskiBtn);
	    JButton groupBtn = new JButton(groupActn);
	    groupBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
	    groupBtn.setAlignmentY(Component.CENTER_ALIGNMENT);
	    add(groupBtn);
	    JButton copySubst = new JButton(copySelActn);
	    copySubst.setAlignmentX(Component.CENTER_ALIGNMENT);
	    copySubst.setAlignmentY(Component.CENTER_ALIGNMENT);
	    add(copySubst);
	    // From here down the constructor is equal to MonoToolsPane's
	    JButton clear = new JButton(clearSelActn);
	    clear.setAlignmentX(Component.CENTER_ALIGNMENT);
	    clear.setAlignmentY(Component.CENTER_ALIGNMENT);
	    add(clear);
	    JButton identity = new JButton(selectIdActn);
	    identity.setAlignmentX(Component.CENTER_ALIGNMENT);
	    identity.setAlignmentY(Component.CENTER_ALIGNMENT);
	    add(identity);
	    JButton complete = new JButton(compltSelActn);
	    complete.setAlignmentX(Component.CENTER_ALIGNMENT);
	    complete.setAlignmentY(Component.CENTER_ALIGNMENT);
	    add(complete);
	    JButton reverse = new JButton(revSelActn);
	    reverse.setAlignmentX(Component.CENTER_ALIGNMENT);
	    reverse.setAlignmentY(Component.CENTER_ALIGNMENT);
	    add(reverse);
	    JButton invert = new JButton(invSelActn);
	    invert.setAlignmentX(Component.CENTER_ALIGNMENT);
	    invert.setAlignmentY(Component.CENTER_ALIGNMENT);
	    add(invert);
	}

	/**
	 * Returns the number being displayed by the <code>JSpinner</code>.
	 *
	 * @return the <code>JSpinner</code>'s current value.
	 */
	public int getSpinnerValue(){
	    return ((SpinnerNumberModel)numAlpha.getModel()).getNumber().intValue();
	}

	/**
	 * Sets the value of the <code>JSpinner</code>.
	 * The method assumes <code>numAlpha</code> has a
	 * <code>SpinnerNumberModel</code> with a non <code>null</code>
	 * <code>minimum</code> of type <code>Integer</code>, a
	 * <code>null</code> <code>maximum</code> and a 
	 * <code>stepSize</code> of <code>1</code>.
	 *
	 * @param val the value to set it to
	 * @throws IllegalArgumentException if <code>val</code> is less than the <code>JSpinners</code>'s minimum.
	 * @see SpinnerNumberModel
	 */
	public void setSpinnerValue(int val) throws IllegalArgumentException
	{
	    SpinnerNumberModel spinMod = (SpinnerNumberModel)numAlpha.getModel();
	    assert spinMod.getMaximum() == null: "The spinner has a maximum";
	    assert spinMod.getMinimum() != null: "The spinner has no mimimum";
	    assert (new Integer(1)).equals(spinMod.getStepSize()): "The spinner's step size is not 1";
	    if(((Integer)spinMod.getMinimum()).intValue() <= val){
		spinMod.setValue(new Integer(val));
	    }else{
		throw new IllegalArgumentException();
	    }
	}
    }

    /**
     * <code>AlbertiToolsPane</code> that contains the tools for Vigenère's 
     * polyalphabetic cipher. The only difference being that 
     * <code>VigenereToolsPane</code>s have controls to shift the 
     * selections to the left or right.
     */
    private class VigenereToolsPane extends AlbertiToolsPane{

	public VigenereToolsPane(String name){
	    super(name);
	    JPanel shiftPan = new JPanel();
	    shiftPan.setLayout(new BoxLayout(shiftPan, BoxLayout.X_AXIS));
	    TitledBorder border = BorderFactory.createTitledBorder(labelsRB.getString("shiftPan"));
	    border.setTitlePosition(TitledBorder.BOTTOM);
	    border.setTitleJustification(TitledBorder.CENTER);
	    shiftPan.setBorder(border);
	    JButton shiftLeft = new JButton(shiftLeftActn);
	    JButton shiftRight = new JButton(shiftRightActn);
	    if(shiftLeft.getFont().canDisplayUpTo(shiftLeft.getText()) != -1 ||
	       shiftRight.getFont().canDisplayUpTo(shiftRight.getText())!=-1){
		shiftLeft.setText("<<");
		shiftRight.setText(">>");
	    }
	    shiftPan.add(shiftLeft);
	    shiftPan.add(shiftRight);
	    shiftPan.setAlignmentX(Component.CENTER_ALIGNMENT);
	    shiftPan.setAlignmentY(Component.CENTER_ALIGNMENT);
	    add(shiftPan);
	}
    }

    /**
     * Sets the value of <code>autoCollapse</code> to that of the
     * argument.
     *
     * @param collapse the value to set <code>autoCollapse</code> to
     * @see #autoCollapse
     */
    public void setAutoCollapse(boolean collapse){
	autoCollapse = collapse;
    }

    /**
     * Sets the value of the <code>JSpinner</code> that displays the
     * number of alphabets the currently selected polyalphabetic substitution
     * has. If the current substitution is not polyalphabetic, the method
     * does nothing.
     *
     * @param numAl the number of alphabets
     * @throws IllegalArgumentException if the current substitution is polyalphabetic and <code>numAl &lt; 2</code>
     */
    public void setNumberOfAlphabets(int numAl) throws IllegalArgumentException
    {
	if(selNode == vigenere || selNode == alberti){
	    if(numAl < 2){
		throw new IllegalArgumentException();
	    }else{
		((AlbertiToolsPane)selNode.getUserObject()).setSpinnerValue(numAl);
	    }
	}
    }

    /**
     * Returns the <code>JFrame</code> used to display the data that results
     * from applying the Kasiski Test.
     *
     * @return the <code>JFrame</code> used to display the results of the Kasiski Test
     */
    public JFrame getKasiskiFrame(){
	return kasiskiFrame;
    }

    /**
     * Returns the cipher for which the tools are currently being displayed.
     *
     * @return <code>CAESAR</code>, <code>MONOALPHABETIC</code>, 
     *         <code>VIGENERE</code> or <code>ALBERTI</code>
     * @see #CAESAR
     * @see #MONOALPHABETIC
     * @see #VIGENERE
     * @see #ALBERTI
     */
    public String getCipher(){
	assert selNode!=null : "selNode is null";
	String cipher = null;
	if(selNode == caesar){
	    cipher = CAESAR;
	}else if(selNode == mono){
	    cipher = MONOALPHABETIC;
	}else if(selNode == vigenere){
	    cipher = VIGENERE;
	}else if(selNode == alberti){
	    cipher = ALBERTI;
	}
	return cipher;
    }

    /**
     * Sets the cipher for which the tools should be displayed.
     *
     * @param cipher <code>CAESAR</code>, <code>MONOALPHABETIC</code>,
     *               <code>VIGENERE</code> or <code>ALBERTI</code>
     * @throws NullPointerException if <code>cipher</code> is <code>null</code>
     * @throws IllegalArgumentException if <code>cipher</code> is not 
     *                 <code>CAESAR</code>, <code>MONOALPHABETIC</code>, 
     *                 <code>VIGENERE</code> or <code>ALBERTI</code>
     * @see #CAESAR
     * @see #MONOALPHABETIC
     * @see #VIGENERE
     * @see #ALBERTI
     */
    public void setCipher(String cipher) throws NullPointerException,
						IllegalArgumentException
    {
	if(cipher.equals(CAESAR)){
	    cipherTree.setSelectionPath(new TreePath(caesar.getPath()));
	}else if(cipher.equals(MONOALPHABETIC)){
	    cipherTree.setSelectionPath(new TreePath(mono.getPath()));
	}else if(cipher.equals(VIGENERE)){
	    cipherTree.setSelectionPath(new TreePath(vigenere.getPath()));
	}else if(cipher.equals(ALBERTI)){
	    cipherTree.setSelectionPath(new TreePath(alberti.getPath()));
	}else{
	    throw new IllegalArgumentException();
	}
    }

    /**
     * Resizes a <code>JTable</code> making its columns large enought
     * for the headers and data in them to fit. After this, if the table's
     * width is smaller than <code>minWidth</code>, the columns are widened
     * to cover <code>minWidth</code>.
     *
     * @param table a <code>JTable</code> that contains <code>String</code>s
     *              exclusively and with all of its columns named
     * @param minWidth a minimum width the table should have
     * @throws NullPointerException if <code>table</code> is <code>null</code>
     * @throws IllegalArgumentException if <code>table</code> does not have columns
     */
    private static void resizeTable(JTable table, int minWidth){
	if(table == null){
	    throw new NullPointerException();
	}
	int numCols = table.getColumnCount();
	if(numCols == 0){
	    throw new IllegalArgumentException("table must have at least "+
					       " one column");
	}
	int tableWidth = 0;
	int tmpColWidth = 0;
	int maxColWidth = 0;
	int colWidth[] = new int[numCols];
	JTableHeader header = table.getTableHeader();
	FontMetrics headerFM = header.getFontMetrics(header.getFont());
	TableColumnModel tableCM = table.getColumnModel();
	TableColumn column;
	String colName;
	for(int i=0; i<numCols; i++){
	    colName= table.getColumnName(i);
	    column = tableCM.getColumn(i);
	    tmpColWidth = SwingUtilities.computeStringWidth(headerFM, colName) + 10;
	    maxColWidth = Math.max(tmpColWidth, column.getWidth());
	    for(int j=0; j<table.getRowCount(); j++){
		TableCellRenderer cellRen = table.getCellRenderer(j, i);
		Component comp = cellRen.getTableCellRendererComponent(table, table.getValueAt(j, i), false, false, j, i);
		tmpColWidth = comp.getPreferredSize().width + 10;
		maxColWidth = Math.max(maxColWidth, tmpColWidth);
	    }
	    colWidth[i] = maxColWidth;
	    tableWidth += maxColWidth;
	}
	if(tableWidth < minWidth){
	    int widthLacking = minWidth - tableWidth; // the width lacking
	    int colWidthLacking = widthLacking/numCols;
	    int i;
	    for(i=0; i<numCols-1; i++){
		colWidth[i] += colWidthLacking;
	    }
	    colWidth[i] += widthLacking - colWidthLacking*(numCols-1);
	}
	tableWidth = 0;
	for(int i=0; i<numCols; i++){
	    column = tableCM.getColumn(i);
	    column.setPreferredWidth(colWidth[i]);
	    tableWidth += colWidth[i];
	}
	table.revalidate();
	table.repaint();
    }


    /**
     * A small program used to test the component.
     */
    public static void main(String[] args){
	//JComponent.setDefaultLocale(new Locale("es", "MX"));
	JFrame frame = new JFrame("Test");
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	Locale loc = Locale.getDefault();
	Collator collator = Collator.getInstance(loc);
	CipherToolsPane toolsP = new CipherToolsPane(new Substitution(loc,
								      collator,
							    new ArrayList(),
							    new ArrayList()),
					   new CiphertextManager(loc,
								 collator,
								 ""));
	toolsP.setAutoCollapse(true);
	frame.getContentPane().add(toolsP);
	frame.pack();
	frame.setVisible(true);
    }
}
/*
 * -- CipherToolsPane.java ends here --
 */
