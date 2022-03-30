/*
 * -- StringFreqFComparator.java --
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

package net.sourceforge.ganzua.text;

import java.util.Comparator;

/**
 * Class that implements <code>Comparator</code> and is used to compare
 * instances of <code>StringFreq</code> by their frequencies.
 *
 * @author Jesús Adolfo García Pasquel
 * @version 0.01 Mar 2004
 */
public class StringFreqFComparator implements Comparator{

    /**
     * Compares two <code>StringFreq</code>s using their frequencies. Returns a
     * negative integer, zero, or a positive integer as the <i>second</i>
     * argument is less than, equal to, or greater than the <i>first</i>. 
     *
     * @param o1 an instance of <code>StringFreq</code>
     * @param o2 an instance of <code>StringFreq</code>
     * @return  a negative integer, zero, or a positive integer as the
     *          <i>second</i> argument is less than, equal to, or greater than
     *          the <i>first</i>. 
     */
    public int compare(Object o1, Object o2) throws ClassCastException
    {
	return ((StringFreq)o2).getFrequency()-((StringFreq)o1).getFrequency();
    }
}
/*
 * -- StringFreqFComparator.java ends here --
 */
