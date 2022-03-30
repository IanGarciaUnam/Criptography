/*
 * -- LoadWorker.java --
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
import java.lang.reflect.InvocationTargetException;

/**
 * This is a class used to perform "loding" tasks that should block the user's
 * actions on the GUI.
 *
 * @see Worker
 * @author Jesús Adolfo García Pasquel
 * @version 0.01 April 2004
 */
public class LoadWorker extends Worker{

    /**
     * Modal dialog displayed while performing the <code>Worker</code>'s 
     * tasks. */
    LoadingDialog dialog;

    /**
     * Creates a new <code>LoadWorker</code> that should execute a loading 
     * task on a new thread while blocking input by displaying a modal
     * "Loading..." dialog.
     *
     * @param work non-GUI-related loading task that should be executed in a
     *             thread other than the event dispatching thread
     * @param gui GUI-related task that should be executed after
     *            <code>work</code> is finished (may be <code>null</code>).
     * @param owner the <code>Frame</code> from which the 
     *              <code>LoadingDialog</code> is displayed
     * @throws NullPointerException if <code>work</code> is <code>null</code>
     */
    public LoadWorker(Runnable work,
		      Runnable gui,
		      final Frame owner) throws NullPointerException
    {
	super(work, gui);
	if(SwingUtilities.isEventDispatchThread()){
	    dialog = new LoadingDialog(owner);
	} else{
	    try{
		SwingUtilities.invokeAndWait(new Runnable(){
			public void run(){
			    dialog = new LoadingDialog(owner);
			}
		    });
	    }catch(InterruptedException ie){
		ie.printStackTrace();
	    }catch(InvocationTargetException ite){
		ite.printStackTrace();
	    }
	}
    }

    /**
     * Creates a new <code>LoadWorker</code> that should execute a loading 
     * task on a new thread while blocking input by displaying a modal
     * "Loading..." dialog.
     *
     * @param work non-GUI-related loading task that should be executed in a
     *             thread other than the event dispatching thread
     * @param gui GUI-related task that should be executed after
     *            <code>work</code> is finished (may be <code>null</code>).
     * @param parentComponent a <code>Component</code> that is in the frame
     *                        from which the dialog should be displayed
     * @throws NullPointerException if <code>work</code> is <code>null</code>
     */
    public LoadWorker(Runnable work,
		      Runnable gui,
		      final Component parentComponent) 
	throws NullPointerException
    {
	super(work, gui);
	if(SwingUtilities.isEventDispatchThread()){
	    dialog = new LoadingDialog(parentComponent);
	} else{
	    try{
		SwingUtilities.invokeAndWait(new Runnable(){
			public void run(){
			    dialog = new LoadingDialog(parentComponent);
			}
		    });
	    }catch(InterruptedException ie){
		ie.printStackTrace();
	    }catch(InvocationTargetException ite){
		ite.printStackTrace();
	    }
	}
    }

    /**
     * Execute the tasks specified when the instance of <code>Worker</code>
     * was  created.
     */
    public void start(){
	thread = new Thread(new Runnable(){
		public void run(){
		    setDialogVisible(true);
		    workTask.run();
		    if(eventTask != null){
			SwingUtilities.invokeLater(eventTask);
		    }
		    setDialogVisible(false);
		}
	    });
	thread.start();
    }

    public void setDialogVisible(final boolean visible){
	if(SwingUtilities.isEventDispatchThread()){
	    dialog.setVisible(visible);
	} else{
	    SwingUtilities.invokeLater(new Runnable(){
		    public void run(){
			dialog.setVisible(visible);
		    }
		});
	}
    }
}
/*
 * -- LoadWorker.java ends here --
 */
