/*
 * -- StringFreqCollationKeyComparator.java --
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
import java.text.CollationKey;

/**
 * Class that implements <code>Comparator</code> and is used to compare
 * instances of <code>StringFreq</code> to instances of 
 * <code>CollationKey</code>
 *
 * @author Jesús Adolfo García Pasquel
 * @version 0.01 May 2003
 */
public class StringFreqCollationKeyComparator implements Comparator{

    /**
     * Compares a <code>CollationKey</code> to a <code>StringFreq</code> by 
     * ignoring the <code>StringFreq</code>'s frequency 
     *
     * @param o1 an instance of <code>CollationKey</code> or
     *           <code>StringFreq</code>
     * @param o2 an instancd of <code>StringFreq</code> if <code>o1</code> is
     *           an instance of <code>CollationKey</code> or an instance of
     *           <code>CollationKey</code> if <code>o1</code> is an instance of
     *           <code>StringFreq</code>
     */
    public int compare(Object o1, Object o2) throws ClassCastException
    {
	return o1 instanceof CollationKey ? 
	    ((CollationKey)o1).compareTo(((StringFreq)o2).getStringCK()):
	    ((StringFreq)o1).getStringCK().compareTo((CollationKey)o2);
    }    
}
/*
 * -- StringFreqCollationKeyComparator.java ends here --
 */
