/*
 * -- KasiskiEntry.java --
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

import java.text.CollationKey;
import java.util.Set;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Arrays;

/**
 * Class used to store the data entries that results from performing Kasiski's
 * test on a cryptogram (character sequence, frequency, distance between
 * an occurrence an the next, prime factors of those distances).
 *
 * @author Jesús Adolfo García Pasquel
 * @version 0.01 Feb 2004
 */
public class KasiskiEntry implements Comparable{

    /**
     * The sequence */
    protected CollationKey seq;

    /**
     * The sequence's length in user characters */
    protected int seqLen;

    /**
     * Stores the positions the sequence appears in */
    protected int[] positions;

    /**
     * Stores the distances between an occurrence of the sequence and the
     * next. */
    protected int[] distances;

    /**
     * Stores the prime factors of the distances between occurrences
     * <code>distances</code>. */
    protected int[][] distFactors;

    /**
     * Constructor that sets the sequence to <code>sequence</code>, its
     * length in user characters to <code>sequenceLength</code> and the
     * positions it appears in to the <code>Integers</code> in the
     * <code>Set</code> <code>positions</code>.
     *
     * @param sequence the sequence of characters in the form of a
     *                 <code>CollationKey</code>
     * @param sequenceLength the length of <code>sequence</code> in user
     *                       characters
     * @param positions the indices the sequence appears in (without
     *                  repetitions)
     * @throws NullPointerException if any of the arguments is <code>null</code>
     * @throws IllegalArgumentException if <code>positions.size() < 2</code> or <code>sequenceLength < 2</code>
     * @throws ClassClastException if <code>positions</code> contains an <code>Object</code> that is not an instance of <code>Integer</code>
     */
    public KasiskiEntry(CollationKey sequence, 
			int sequenceLength,
			Set positions) throws NullPointerException,
					      IllegalArgumentException,
					      ClassCastException
    {
	if(sequenceLength < 2){
	    throw new IllegalArgumentException("sequenceLength must be "+
					       "greater than or equal to 2");
	}else if(sequence == null || positions == null){
	    throw new NullPointerException();
	}else if(positions.size() < 2){
	    throw new IllegalArgumentException("positions.size() must be "+
					       "greater than or equal to 2");
	}
	seq = sequence;
	seqLen = sequenceLength;
	initPositions(positions);
	initDistances();
	initPrimeFactors();
    }

    /**
     * Initializes <code>positions</code> as an <code>int</code> array of
     * the same size as the <code>Set</code> of <code>Integers</code> 
     * <code>posSet</code> and copies the values in the <code>Set</code>
     * to <code>positions</code>.
     *
     * @param posSet a <code>Set</code> of <code>Integer</code>s
     */
    private final void initPositions(Set posSet) throws NullPointerException
    {
	positions = new int[posSet.size()];
	int i = 0;
	for(Iterator iter=posSet.iterator(); iter.hasNext(); i++){
	    positions[i] = ((Integer)iter.next()).intValue();
	}
	Arrays.sort(positions);
    }

    /**
     * Initializes <code>distances</code> using the numbers in
     * <code>positions</code>
     */
    private final void initDistances(){
	distances = new int[positions.length -1];
	for(int i=0; i<distances.length; i++){
	    distances[i] = positions[i+1] - positions[i];
	}
    }

    /**
     * Initializes <code>distFactors</code> using the numbers
     * in <code>distances</code>
     */
    private final void initPrimeFactors(){
	distFactors = new int[distances.length][];
	for(int i=0; i<distances.length; i++){
	    distFactors[i] = primeFactors(distances[i]);
	}
    }

    /**
     * Returns an array of integers with the prime factors of 
     * <code>n</code>.
     *
     * @param n an <code>int</code> greater or equal to <code>1</code>
     * @throws IllegalArgumentException if <code>n &lt; 1</code>
     */
    private static final int[] primeFactors(int n)
	throws IllegalArgumentException
    {
	int[] factorArr;
	if(n<1){
	    throw new IllegalArgumentException();
	}
	if(n == 1){
	    factorArr = new int[1];
	    factorArr[0] = 1;
	    return factorArr;
	}
	ArrayList factorLst = new ArrayList();
	int factor=2;
	while(n>1){
	    while(n%factor==0){
		factorLst.add(new Integer(factor));
		n/=factor;
	    }
	    factor+= factor==2?1:2;
	}
	factorArr = new int[factorLst.size()];
	int i=0;
	for(Iterator iter=factorLst.iterator(); iter.hasNext(); i++){
	    factorArr[i] = ((Integer)iter.next()).intValue();
	}
	return factorArr;
    }

    /**
     * Constructor that sets the sequence to <code>sequence</code>, its
     * length in user characters to <code>sequenceLength</code> and the
     * positions it appears in to <code>positions</code>.
     *
     * @param sequence the sequence of characters in the form of a
     *                 <code>CollationKey</code>
     * @param sequenceLength the length of <code>sequence</code> in user
     *                       characters
     * @param positions the indices the sequence appears in (without
     *                  repetitions)
     * @throws NullPointerException if any of the arguments is <code>null</code>
     * @throws IllegalArgumentException if <code>positions.length < 2</code> or <code>sequenceLength < 2</code>
     */
    public KasiskiEntry(CollationKey sequence, 
			int sequenceLength,
			int[] positions) throws NullPointerException,
						IllegalArgumentException
    {
	if(sequenceLength < 2){
	    throw new IllegalArgumentException("sequenceLength must be "+
					       "greater than or equal to 2");
	}else if(sequence == null || positions == null){
	    throw new NullPointerException();
	}else if(positions.length < 2){
	    throw new IllegalArgumentException("positions.length must be "+
					       "greater than or equal to 2");
	}
	seq = sequence;
	seqLen = sequenceLength;
	this.positions = positions;
	Arrays.sort(this.positions);
    }

    /**
     * Compares this <code>KasiskiEntry</code> with the specified 
     * <code>Object</code>.
     * Returns a negative integer, zero, or a positive integer as <i>the
     * given</i> <code>Object</code> is less than, equal to, or greater
     * than <i>this</i> <code>KasiskiEntry</code>
     *
     * Note: The comparison is made using the length and alphabetical order 
     * of the sequences. Sorting <code>KasiskiEntry</code>s using this
     * method orders them from the one with longest sequence to the 
     * one with the shortest.
     *
     * @param o an instance of <code>StringFreq</code>
     * @return A negative integer, zero, or a positive integer as this
     *         <code>StringFreq</code> is less than, equal to, or greater than
     *         the given <code>Object</code>.
     * @throws ClassCastException if the specified <code>Object</code> is not a <code>StringFreq</code>
     */
    public int compareTo(Object o) throws ClassCastException
    {
	KasiskiEntry param = (KasiskiEntry)o;
	int ret = param.seqLen - seqLen;
	return ret != 0 ? ret : seq.compareTo(param.seq);
    }

    /**
     * Returns the sequence.
     *
     * @return the sequence
     */
    public CollationKey getSequence(){
	return seq;
    }

    /**
     * Returns the sequence in the form of a <code>String</code>.
     *
     * @return the sequence in the form of a <code>String</code>.
     */
    public String getSequenceAsString(){
	return seq.getSourceString();
    }

    /**
     * Returns the length of the sequence in user characters
     *
     * @return the length of the sequence in user characters
     */
    public int getSequenceLength(){
	return seqLen;
    }

    /**
     * Returns the number of times the sequence appears in the ciphertext.
     *
     * @return the number of times the sequence appears in the ciphertext
     */
    public int getFrequency(){
	return positions.length;
    }

    /**
     * Returns the indices the sequence appears in. The array is sorted in
     * ascending order.
     *
     * @return the indices the sequence appears in
     */
    public int[] getPositions(){
	return positions;
    }

    /**
     * Returns an array with the distances between an occurrence of the 
     * sequence an the next
     *
     * @return the distance between an occurrence of the sequence an the next
     */
    public int[] getDistances(){
	return distances;
    }

    /**
     * Returns an array with the prime factors of the integers in 
     * <code>distances</code>.
     *
     * @return an array with the prime factors of the integers in
     *         <code>distances</code>
     */
    public int[][] getDistFactors(){
	return distFactors;
    }
}

/*
 * -- KasiskiEntry.java ends here --
 */
