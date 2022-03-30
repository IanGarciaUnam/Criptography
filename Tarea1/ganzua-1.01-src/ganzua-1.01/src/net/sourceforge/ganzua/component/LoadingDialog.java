/*
 * -- LoadingDialog.java --
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
import java.awt.*;
import java.awt.event.*;
import java.util.ResourceBundle;

/**
 * Modal <code>JDialog</code> that displays a <code>JProgressBar</code> in
 * indeterminate mode and a <code>JLabel</code> that reads 
 * <code>&quot;Loading...&quot;</code>. The only way to close the dialog is by
 * programatically hiding it using <code>setVisible(false)</code>.
 *
 * @author Jesús Adolfo García Pasquel
 * @version 0.01 April 2004
 */
public class LoadingDialog extends JDialog{

    /**
     * The progress bar */
    protected JProgressBar progressBar;

    /**
     * The label that reads <code>&quot;Loading...&quot;</code> */
    protected JLabel label;

    /**
     * <code>ResourceBundle</code> with localized labels */
    private ResourceBundle labelsRB;

    /**
     * Creates a modal dialog without a title with the specified 
     * <code>Frame</code> as its owner. If <code>owner</code> is 
     * <code>null</code>, a shared hidden frame will be set as the owner of
     * the dialog.
     *
     * @param owner the <code>Frame</code> from which the dialog is displayed
     */
    public LoadingDialog(Frame owner){
	super(owner);
	setModal(true);
	setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
	labelsRB = ResourceBundle.getBundle(LoadingDialog.class.getName(),
					    JComponent.getDefaultLocale());
	label = new JLabel(labelsRB.getString("label"));
	progressBar = new JProgressBar();
	initGUI();
	setLocationRelativeTo(owner);
    }

    /**
     * Creates a modal dialog without a title with the <code>Frame</code> 
     * that contains the component as its owner.
     *
     * @param parentComponent the <code>Component</code> that is in the frame
     *                        from which the dialog is displayed
     * @see #LoadingDialog(Frame)
     */
    public LoadingDialog(Component parentComponent){
	this(JOptionPane.getFrameForComponent(parentComponent));
    }

    /**
     * Initializes the GUI of the <code>LoadingDialog</code>.
     */
    private void initGUI(){
	label.setAlignmentX(Component.CENTER_ALIGNMENT);
	label.setAlignmentY(Component.CENTER_ALIGNMENT);
	progressBar.setStringPainted(true);
	progressBar.setString("");
	progressBar.setIndeterminate(true);
	progressBar.setAlignmentX(Component.CENTER_ALIGNMENT);
	progressBar.setAlignmentY(Component.CENTER_ALIGNMENT);
	Box content = new Box(BoxLayout.Y_AXIS);
	content.add(Box.createVerticalGlue());
	content.add(label);
	content.add(Box.createVerticalStrut(3));
	content.add(progressBar);
	content.add(Box.createVerticalGlue());
	content.setBorder(BorderFactory.createEmptyBorder(5, 10, 15, 10));
	getContentPane().add(content);
	pack();
    }

    /**
     * Small program used to text the class.
     */
    public static void main(String[] args){
	JComponent.setDefaultLocale(new java.util.Locale("es", "MX"));
	javax.swing.SwingUtilities.invokeLater(new Runnable() {
		public void run(){
		    JFrame.setDefaultLookAndFeelDecorated(true);
		    //JDialog.setDefaultLookAndFeelDecorated(true);
		    final JFrame frame = new JFrame("Test");
		    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		    JButton dialog = new JButton("Dialog");
		    dialog.addActionListener(new ActionListener(){
			    public void actionPerformed(ActionEvent e){
				LoadingDialog ld = new LoadingDialog(frame);
				ld.setVisible(!ld.isVisible());
			    }
			});
		    frame.getContentPane().add(dialog);
		    frame.pack();
		    frame.setSize(new Dimension(320, 200));
		    frame.setVisible(true);
		}
	    });
    }
}
/*
 * -- LoadingDialog.java ends here --
 */
