/*
 * -- TextFileFilter.java --
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
import javax.swing.filechooser.FileFilter;
import javax.swing.JComponent;
import java.util.ResourceBundle;

/**
 * Makes a <code>JFileChooser</code>'s directory listing display directories,
 * and files with extension <code>txt</code> or <code>text</code> exclusively.
 *
 * @author Jesús Adolfo García Pasquel
 * @version 0.01 March 2004
 */
public class TextFileFilter extends FileFilter{

    /**
     * <code>ResourceBundle</code> used to get localized labels
     */
    private ResourceBundle labelsRB;

    /**
     * Creates a new <code>TextFileFilter</code>
     */
    public TextFileFilter(){
	super();
	labelsRB = ResourceBundle.getBundle(TextFileFilter.class.getName(),
					    JComponent.getDefaultLocale());
    }

    /**
     * Returns <code>true</code> if <code>f</code> is 
     * a directory or has extension <code>txt</code> or <code>text</code>,
     * and <code>false</code> otherwise.
     *
     * @param f The file to accept/reject
     * @return <code>true</code> if <code>f</code> is a directory or has extension <code>txt</code> or <code>text</code>, and <code>false</code> otherwise.
     */
    public boolean accept(File f){
	if(f.isDirectory()){
	    return true;
	}
	String ext = getExtension(f);
	if(ext != null && (ext.equals("txt") || ext.equals("text"))){
	    return true;
	}
	return false;
    }

    /**
     * Returns the description of this filter.
     *
     * @return the description of this filter
     */
    public String getDescription(){
	return labelsRB.getString("Descr");
    }

    /**
     * Returns a string with the extension of file <code>f</code>.
     * If <code>f</code> has no extension, <code>null</code> is
     * returned.
     *
     * @param f The file to get the extension of
     */
    public static String getExtension(File f){
	String ext = null;
	String name = f.getName();
	int i = name.lastIndexOf('.');
	if(i>0 && i<name.length()-1){
	    ext = name.substring(i+1).toLowerCase();
	}
	return ext;
    }
}
/*
 * -- TextFileFilter.java ends here --
 */
