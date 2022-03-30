/*
 * -- SpinnerDialog.java --
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
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

/**
 * Modal <code>JDialog</code> that prompts users for a positive integer value
 * using a <code>JSpinner</code>.
 *
 * @author Jesús Adolfo García Pasquel
 * @version 0.01 March 2004
 */
public class SpinnerDialog extends JDialog
                           implements PropertyChangeListener
{
    /**
     * <code>SpinnerModel</code> used by <code>spinner</code> with
     * a minimum of <code>1</code> and no upper bound. */
    protected SpinnerNumberModel spinMdl;

    /**
     * <code>JSpinner</code> used to get the user's input */
    protected JSpinner spinner;

    /**
     * <code>JOptionPane</code> used to generate the dialog's contents */
    private JOptionPane optionPane;

    /**
     * Constructor that creates a <code>SpinnerDialog</code> with the
     * <code>parentComponent</code>, <code>message</code> and
     * <code>title</code> specified.
     *
     * @param parentComponent determines the <code>Frame</code> in which the
     *               dialog is displayed; if <code>null</code>, or if the
     *               <code>parentComponent</code> has no <code>Frame</code>, a
     *               default <code>Frame</code> is used
     * @param message the <code>Object</code> to display
     * @param title the title string for the dialog
     */
    public SpinnerDialog(Component parentComponent, 
			 Object message, 
			 String title)
    {
	super(JOptionPane.getFrameForComponent(parentComponent), title, true);
	spinMdl = new SpinnerNumberModel(new Integer(5), 
					 new Integer(1),
					 null,
					 new Integer(1));
	spinner = new JSpinner(spinMdl);
	spinner.setAlignmentX(Component.CENTER_ALIGNMENT);
	spinner.setAlignmentY(Component.CENTER_ALIGNMENT);
	Box box = new Box(BoxLayout.X_AXIS);
	box.add(Box.createHorizontalGlue());
	box.add(spinner);
	box.add(Box.createHorizontalGlue());
	Object[] msg ={message, box};
	optionPane = new JOptionPane(msg,
				     JOptionPane.PLAIN_MESSAGE,
				     JOptionPane.OK_CANCEL_OPTION);
	setContentPane(optionPane);
	addListeners();
	pack();
	setLocationRelativeTo(parentComponent);
    }

    /**
     * Adds the event listeners the dialog needs to work as expected
     */
    private void addListeners(){
	//Handle window closing correctly.
	addWindowListener(new WindowAdapter(){
		public void windowClosing(WindowEvent e){
		    optionPane.setValue(new Integer(JOptionPane.CLOSED_OPTION));
		}
	    });
	//Ensure the spinner always gets the first focus.
	addComponentListener(new ComponentAdapter(){
		public void componentShown(ComponentEvent e){
		    spinner.requestFocusInWindow();
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
	if(this.isVisible() && (e.getSource() == optionPane)
	   && (prop.equals(JOptionPane.VALUE_PROPERTY))) {
	    this.setVisible(false);
	}
    }

    /**
     * Returns the <code>JSpinner</code>'s current value.
     */
    public int getSpinnerValue(){
	return ((SpinnerNumberModel)spinner.getModel()).getNumber().intValue();
    }

    /**
     * Sets the <code>JSpinner</code>'s value to <code>value</code>
     *
     * @param value the value to set the <code>JSpinner</code> to
     * @throws IllegalArgumentException if <code>value &lt; 1</code>
     */
    public void setSpinnerValue(int value) throws IllegalArgumentException
    {
	if(value < 1){
	    throw new IllegalArgumentException("value < 1");
	}
	((SpinnerNumberModel)spinner.getModel()).setValue(new Integer(value));
    }

    /**
     * Brings up a <code>SpinnerDialog</code> centered on 
     * <code>parentComponent</code> with the <code>title</code>,
     * <code>message</code> and a <code>JSpinner</code> with the 
     * <code>value</code> specified. Returns <code>-1</code> if the
     * user canceled the operation or the positive integer the user
     * chose.
     *
     * @param parentComponent determines the <code>Frame</code> in which the
     *              dialog is displayed; if <code>null</code>, or if the
     *              <code>parentComponent</code> has no <code>Frame</code>, a
     *              default <code>Frame</code> is used
     * @param message the <code>Object</code> to display
     * @param value the value the <code>JSpinner</code> should display
     * @param title the title string for the dialog
     */
    public static int showSpinnerDialog(Component parentComponent,
					Object message, 
					int value,
					String title) 
	throws IllegalArgumentException
    {
	int ret = -1;
	SpinnerDialog spinDlg = new SpinnerDialog(parentComponent,
						  message, title);
	spinDlg.setSpinnerValue(value);
	spinDlg.setVisible(true);
	Object val = spinDlg.optionPane.getValue();
	if(val != null && val != JOptionPane.UNINITIALIZED_VALUE &&
	   ((Number)val).intValue() == JOptionPane.OK_OPTION){
	    ret = spinDlg.getSpinnerValue();
	}
	return ret;
    }

    /**
     * Brings up a <code>SpinnerDialog</code> centered on 
     * <code>parentComponent</code> with the <code>title</code>,
     * <code>message</code> and a <code>JSpinner</code> with its value set to
     * <code>5</code>. Returns <code>-1</code> if the
     * user canceled the operation or the positive integer the user
     * chose.
     *
     * @param parentComponent determines the <code>Frame</code> in which the
     *              dialog is displayed; if <code>null</code>, or if the
     *              <code>parentComponent</code> has no <code>Frame</code>, a
     *              default <code>Frame</code> is used
     * @param message the <code>Object</code> to display
     * @param title the title string for the dialog
     */
    public static int showSpinnerDialog(Component parentComponent,
					Object message, String title)
    {
	return showSpinnerDialog(parentComponent, message, 5, title);
    }
}
/*
 * -- SpinnerDialog.java ends here --
 */
