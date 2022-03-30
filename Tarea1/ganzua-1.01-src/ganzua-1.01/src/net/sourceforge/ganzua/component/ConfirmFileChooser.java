/*
 * -- ConfirmFileChooser.java --
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

import java.io.File;
import java.util.ResourceBundle;
import javax.swing.filechooser.FileSystemView;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

/**
 * <code>ConfirmFileChooser</code> makes the user confirm that s(he) wishes
 * to overwrite a file when the <code>ConfirmFileChooser</code>'s dialog type
 * is <code>SAVE_DIALOG</code> and the file specified already exists. For
 * everything else, <code>ConfirmFileChooser</code>s behave just like
 * <code>JFileChooser</code>s.
 *
 * @see JFileChooser
 * @author Jesús Adolfo García Pasquel
 * @version 0.01 April 2004
 */
public class ConfirmFileChooser extends JFileChooser{

    /**
     * <code>ResourceBundle</code> used to get the localized labels
     */
    private ResourceBundle labelsRB;

    /**
     * Initializes the <code>ResourceBundle</code> used to get the
     * localized lables used by the confirmation dialog.
     */
    private void initLabelsRB(){
	labelsRB = ResourceBundle.getBundle(ConfirmFileChooser.class.getName(),
					    getDefaultLocale());
    }

    /**
     * Constructs a <code>ConfirmFileChooser</code> pointing to the user's
     * default directory.
     *
     * @see JFileChooser#JFileChooser()
     */
    public ConfirmFileChooser(){
	super();
	initLabelsRB();
    }

    /**
     * Constructs a <code>ConfirmFileChooser</code> using the given path.
     * Passing in a <code>null</code> string causes the file chooser to point
     * to the user's default directory.
     *
     * @param currentDirectoryPath a <code>String</code> giving the path to a
     *                             file or directory.
     * @see JFileChooser#JFileChooser(String)
     */
    public ConfirmFileChooser(String currentDirectoryPath){
	super(currentDirectoryPath);
	initLabelsRB();
    }

    /**
     * Constructs a <code>ConfirmFileChooser</code> using the given 
     * <code>File</code> as the path. Passing in a <code>null</code> file
     * causes the file chooser to point to the user's default directory.
     *
     * @param currentDirectory a <code>File</code> object specifying the path
     *                         to a file or directory
     * @see JFileChooser#JFileChooser(File)
     */
    public ConfirmFileChooser(File currentDirectory){
	super(currentDirectory);
	initLabelsRB();
    }

    /**
     * Constructs a <code>ConfirmFileChooser</code> using the given
     * <code>FileSystemView</code>.
     *
     * @see JFileChooser#JFileChooser(FileSystemView)
     */
    public ConfirmFileChooser(FileSystemView fsv){
	super(fsv);
	initLabelsRB();
    }

    /**
     * Constructs a <code>ConfirmFileChooser</code> using the given current 
     * directory and <code>FileSystemView</code>.
     *
     * @see JFileChooser#JFileChooser(File, FileSystemView)
     */
    public ConfirmFileChooser(File currentDirectory, FileSystemView fsv){
	super(currentDirectory, fsv);
	initLabelsRB();
    }

    /**
     * Constructs a <code>ConfirmFileChooser</code> using the given current
     * directory path and <code>FileSystemView</code>.
     *
     * @see JFileChooser#JFileChooser(String, FileSystemView)
     */
    public ConfirmFileChooser(String currentDirectoryPath, FileSystemView fsv){
	super(currentDirectoryPath, fsv);
	initLabelsRB();
    }

    /**
     * Called by the UI when the user hits the approve button. If the
     * <code>ConfirmFileChooser</code>'s dialog type is 
     * <code>SAVE_DIALOG</code> and the selected file already exists a
     * dialog asks the user to confirm that he wants to replace the existing
     * file. If a <code>SecurityException</code> occurs while checking that
     * the file exists, the confirmation dialog is not displayed and
     * <code>JFileChooser</code>'s <code>approveSelection()</code> method
     * is called.
     *
     * @see JFileChooser#approveSelection()
     */
    public void approveSelection(){
	if(getDialogType() == SAVE_DIALOG){
	    boolean fileExists = false;
	    try{
		fileExists = getSelectedFile().exists();
	    }catch(SecurityException se){
		fileExists = false;
	    }
	    if(fileExists){
		if(JOptionPane.showConfirmDialog(this,
						 labelsRB.getString("Ovrwrt"),
						 labelsRB.getString("OvrwrtTtl"),
						 JOptionPane.YES_NO_OPTION)
		   == JOptionPane.YES_OPTION){
		    super.approveSelection();
		} else{
		    return;
		}
	    } else{
		super.approveSelection();
	    }
	} else{
	    super.approveSelection();
	}
    }
}
/*
 * -- ConfirmFileChooser.java ends here --
 */
