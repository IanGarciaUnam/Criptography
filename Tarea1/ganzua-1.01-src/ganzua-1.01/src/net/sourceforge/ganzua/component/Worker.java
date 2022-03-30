/*
 * -- Worker.java --
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

import javax.swing.SwingUtilities;

/**
 * This is a class used to perform non-GUI-related work in a dedicated thread
 * that also lets you specify GUI-related work to perform once the former is
 * finished.
 *
 * @author Jesús Adolfo García Pasquel
 * @version 0.01 April 2004
 */
public class Worker{

    /**
     * A time-consuming task that should be executed in a thread other than
     * the AWT event dispatching thread (non GUI code, e.g. reading a file). */
    protected Runnable workTask;

    /**
     * A task that should be executed in the AWT dispatching thread after
     * <code>workTask</code> has been executed (GUI code). */
    protected Runnable eventTask;

    /**
     * <code>Thread</code> used to execute the tasks */
    protected Thread thread;

    /**
     * Creates a new <code>Worker</code> that should execute 
     * <code>work</code> on a new thread and <code>gui</code> on the AWT event
     * dispatching thread after <code>work</code> has finished executing
     * (<code>gui</code> may be <code>null</code>).<br/>
     * Once the <code>Worker</code> has been created, you must call its
     * <code>start()</code> method to put it to work.
     *
     * @param work non-GUI-related task that should be executed in a thread other than the event dispatching thread
     * @param gui GUI-related task that should be executed after <code>work</code> is finished.
     * @throws NullPointerException if <code>work</code> is <code>null</code>
     * @see #start()
     */
    public Worker(Runnable work, Runnable gui) throws NullPointerException
    {
	if(work == null){
	    throw new NullPointerException();
	}
	workTask = work;
	eventTask = gui;
    }

    /**
     * Execute the tasks specified when the instance of <code>Worker</code>
     * was  created.
     */
    public void start(){
	thread = new Thread(new Runnable(){
		public void run(){
		    workTask.run();
		    if(eventTask != null){
			SwingUtilities.invokeLater(eventTask);
		    }
		}
	    });
	thread.start();
    }
}
/*
 * -- Worker.java ends here --
 */
