/*
 * -- StringFreq.java --
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

import java.text.Collator;
import java.text.CollationKey;

/**
 * Class that stores a string in the form of a <code>CollationKey</code>
 * and its frequency (an <code>int</code> that may represent the number of
 * occurrences of the string in some text).
 *
 * @author Jesús Adolfo García Pasquel
 * @version 0.01 May 2003
 */
public class StringFreq implements Comparable{

    /**
     * The string
     */
    protected CollationKey str;

    /**
     * The frequency
     */
    protected int freq;

    /**
     * Constructor that sets the string to <code>s</code> and the
     * frequency to <code>0</code>
     *
     * @param s the <code>CollationKey</code>
     */
    public StringFreq(CollationKey s){
	str = s;
	freq=0;
    }

    /**
     * Constructor that sets the string to <code>s</code> and the
     * frequency to <code>f</code>
     *
     * @param s the <code>CollationKey</code>
     * @param f the frequency
     */
    public StringFreq(CollationKey s, int f){
	str = s;
	freq= f;
    }

    /**
     * Increments the frequency.
     */
    public void increment(){
	freq++;
    }

    /**
     * Compares this <code>StringFreq</code> with the one passed as argument.
     * Returns a negative integer, zero, or a positive integer as this 
     * <code>StringFreq</code> is less than, equal to, or greater than the 
     * given <code>StringFreq</code>.<br/>
     *
     * Note: The comparison is made among the strings, ignoring the
     * frequencies.
     *
     * @param sf an instance of <code>StringFreq</code>
     * @return A negative integer, zero, or a positive integer as this
     *         <code>StringFreq</code> is less than, equal to, or greater than
     *         the given <code>StringFreq</code>.
     */
    public int compareTo(StringFreq sf)
    {
	return str.compareTo(sf.str);
    }

    /**
     * Compares this <code>StringFreq</code> with the specified 
     * <code>Object</code>.
     * Returns a negative integer, zero, or a positive integer as this 
     * <code>StringFreq</code> is less than, equal to, or greater than the 
     * given <code>Object</code>.<br/>
     *
     * Note: The comparison is made among the strings, ignoring the
     * frequencies.
     *
     * @param o an instance of <code>StringFreq</code>
     * @return A negative integer, zero, or a positive integer as this
     *         <code>StringFreq</code> is less than, equal to, or greater than
     *         the given <code>Object</code>.
     * @throws ClassCastException if the specified <code>Object</code> is not
     *                            a <code>StringFreq</code>
     */
    public int compareTo(Object o) throws ClassCastException
    {
	return compareTo((StringFreq)o);
    }

    /**
     * Compare this <code>StringFreq</code> and the <code>StringFreq</code>
     * <code>o</code> for equality. Two <code>StringFreq</code>s are
     * considered equal if their strings are equal.
     *
     * Note: The comparison is made among the strings, ignoring the
     * frequencies.
     *
     * @param o an instance of <code>StringFreq</code>
     * @return <code>true</code> if the strings of both 
     *         <code>StringFreq</code>s are equal, <code>false</code>
     *         otherwise.
     */
    public boolean equals(Object o){
	return o instanceof StringFreq ? compareTo((StringFreq)o)==0 : false;
    }

    /**
     * Creates a hash code for this <code>StringFreq</code>. The hash value 
     * is calculated on the string's <code>CollationKey</code>.
     *
     * @return the hash value of the string's <code>CollationKey</code>
     */
    public int hashCode(){
	return str.hashCode();
    }

    /**
     * Returns the <code>CollationKey</code>
     * @return The <code>CollationKey</code>
     */
    public CollationKey getStringCK(){
	return str;
    }

    /**
     * Sets <code>str</code> to <code>s</code>.
     *
     * @param s the <code>CollationKey</code> to be set as the string
     */
    public void setStringCK(CollationKey s){
	str = s;
    }

    /**
     * Returns the <code>String</code> that the <code>CollationKey</code>
     * <code>str</code> represents.
     */
    public String getString(){
	return str.getSourceString();
    }

    /**
     * Returns the frequency.
     *
     * @return The frequency
     */
    public int getFrequency(){
	return freq;
    }

    /**
     * Sets the frequency to <code>f</code>
     *
     * @param f the new frequency
     */
    public void setFrequency(int f){
	freq = f;
    }

    /**
     * Discards the <code>CollationKey</code> being used as 
     * <code>str</code> and generates a new one using the
     * <code>Collator</code> passed.
     *
     * @param collator the <code>Collator to use
     * @throws NullPointerException if <code>collator</code> is <code>null</code>
     */
    public void useCollator(Collator collator) throws NullPointerException
    {
	if(collator == null){
	    throw new NullPointerException();
	}
	str = collator.getCollationKey(str.getSourceString());
    }

    /**
     * Returns a <code>String</code> object representing this 
     * <code>StringFreq</code>. The <code>String</code> contains this
     * <code>StringFreq</code>'s string and the decimal representation
     * of the frequency separated by a <code>&quot;\t&quot;</code>.
     *
     * @return a <code>String</code> representation of the value of this
     *         <code>StringFreq</code>
     */
    public String toString(){
	return str.getSourceString()+ "\t" + freq;
    }
}
/*
 * -- StringFreq.java ends here --
 */
