/*
 * -- ToolPane.java --
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

/**
 * <code>Component</code> that displays a set of tools 
 * (<code>Component</code>s ) vertically. The name of the set of tools  is
 * displayed in a <code>TitledBorder</code> that surrounds the tools. If
 * the <code>ToolPane</code> is not high enough to display all of the
 * tools, a <code>ScrollBar</code> will appear.
 *
 * @author Jesús Adolfo García Pasquel
 * @version 0.01 April 2004
 */
public class ToolPane extends JPanel{

    /**
     * The size of the vertical structure to put between components
     * added to the <code>ToolPane</code>. */
    private static final int TOOL_SPACING = 3;

    /**
     * The panel's name. */
    protected String name;

    /**
     * <code>Box</code> used to put the buttons and other components
     * of the <code>ToolPane</code> in. */
    protected Box buttonPane;

    /**
     * <code>JScrollPane</code> in which <code>buttonPane</code> is placed
     * so the user can see and use the tools even if there are too
     * many to display all at once. */
    private JScrollPane scrollP;

    /**
     * The amount to change the scrollbar's value by, given a unit 
     * up/down request. */
    private static final int UNIT_INCREMENT;

    static{
	UNIT_INCREMENT = (new JButton("X")).getPreferredSize().height +
	                 TOOL_SPACING;
    }

    /**
     * Creates a new <code>ToolPane</code> with the specified 
     * <code>name</code>.<br/>
     * The name is used in the border and returned by the
     * <code>getName()</code> and <code>toString()</code> methods.
     *
     * @param name the <code>ToolPane</code>'s name
     */
    public ToolPane(String name){
	super(new BorderLayout());
	if(name == null){
	    name = "";
	}else{
	    this.name = name;
	}
	setBorder(BorderFactory.createTitledBorder(name));
	buttonPane = new Box(BoxLayout.Y_AXIS);
	buttonPane.add(Box.createVerticalStrut(TOOL_SPACING));
	scrollP = new JScrollPane(buttonPane,
				  JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				  JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
	scrollP.getVerticalScrollBar().setUnitIncrement(UNIT_INCREMENT);
	scrollP.getViewport().setOpaque(false);
	scrollP.setOpaque(false);
	add(scrollP, BorderLayout.CENTER);	    
    }

    /**
     * Appends the specified <code>Component</code> to the end of
     * <code>buttonPane</code>.
     *
     * @return the <code>Component</code> <code>comp</code>
     */
    public Component add(Component comp){
	buttonPane.add(comp);
	buttonPane.add(Box.createVerticalStrut(TOOL_SPACING));
	return comp;
    }

    /**
     * Returns the <code>ToolPane</code>'s name.
     *
     * @return the <code>ToolPane</code>'s name
     */
    public String getName(){
	return name;
    }

    /**
     * Returns the <code>ToolPane</code>'s name.
     *
     * @return the <code>ToolPane</code>'s name
     */
    public String toString(){
	return name;
    }
}
/*
 * -- ToolPane.java ends here --
 */
