/*
 * -- SubstitutionEvent.java --
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

package net.sourceforge.ganzua.event;

import javax.swing.event.ChangeEvent;
import net.sourceforge.ganzua.component.Substitution;
import net.sourceforge.ganzua.component.MonoAlphaSubst;

/**
 * <code>SubstitutionEvent</code> is used to notify interested parties that
 * the state has changed in the event source (an instance of 
 * <code>MonoAlphaSubst</code> or <code>Substitution</code>).<br/>
 *
 * <code>SubstitutionEvent</code> objects contain information about the
 * change that took place in the <code>MonoAlphaSubst</code> or 
 * <code>Substitution</code> object that
 * generated the event.
 *
 * @author Jesús Adolfo García Pasquel
 * @version 0.01 October 2003
 */
public class SubstitutionEvent extends ChangeEvent{

    /**
     * Indicates that the type of state change that took place is not known.
     * It could have been either <code>SUBSTITUTION_PAIR</code>, 
     * <code>IGNORED_CHARACTERS</code> or (in the case of 
     * <code>Substitution</code> objects) <code>NUMBER_OF_ALPHABETS</code>
     */
    public static final byte UNKNOWN = 0;

    /**
     * Indicates that the type of state change that took place was a 
     * <code>SUBSTITUTION_PAIR</code> change (i.e. one or more characters 
     * have a new replacement character).
     */
    public static final byte SUBSTITUTION_PAIR = 1;

    /**
     * Indicates that the type of state change that took place was an 
     * <code>IGNORED_CHARACTERS</code> change (i.e. one of the characters
     * was added to/removed from the set of ignored characters of that 
     * alphabet).
     */
    public static final byte IGNORED_CHARACTERS = 2;

    /**
     * Indicates that the type of state change that took place was a 
     * <code>NUMBER_OF_ALPHABETS</code> change (i.e. the number of alphabets
     * in the substitution changed).
     */
    public static final byte NUMBER_OF_ALPHABETS = 4;

    /**
     * Indicates that the type of state change that took place was a 
     * <code>CIPHER_ALPHABET</code> change (i.e. the cipher alphabet
     * changed, and so did the substitution pairs and ignored characters).
     */
    public static final byte CIPHER_ALPHABET = 11;

    /**
     * Indicates that the type of state change that took place was a 
     * <code>CHARACTER_ADDED_TO_CIPHER_ALPHABET</code> change 
     * (i.e. a character was added to the cipher alphabet using
     * <code>Substitution</code>'s 
     * <code>addCharacterToCipherAlphabet(String)</code> method).
     *
     * @see Substitution#addCharacterToCipherAlphabet(String)
     */
    public static final byte CHARACTER_ADDED_TO_CIPHER_ALPHABET = 16;

    /**
     * Indicates that the type of state change that took place was a 
     * <code>CHARACTER_ADDED_TO_PLAIN_ALPHABET</code> change 
     * (i.e. a character was added to the plain alphabet using
     * <code>Substitution</code>'s 
     * <code>addCharacterToPlainAlphabet(String)</code> method).
     *
     * @see Substitution#addCharacterToPlainAlphabet(String)
     */
    public static final byte CHARACTER_ADDED_TO_PLAIN_ALPHABET = 32;

    /**
     * Indicates the kind of state change that took place. Either 
     * <code>UNKNOWN</code>, <code>CIPHER_ALPHABET</code>,
     * <code>CHARACTER_ADDED_TO_CIPHER_ALPHABET<code>,
     * <code>SUBSTITUTION_PAIR</code>, <code>IGNORED_CHARACTERS</code> 
     * or <code>NUMBER_OF_ALPHABETS</code>
     */
    protected byte type;

    /**
     * Index of the alphabet in the substituion affected by the change.<br/>
     * Note: You might want to check the source <code>Substitution</code>'s
     * number of alphabets if you plan to use this.<br/>
     * Note: In the case of <code>NUMBER_OF_ALPHABETS</code>,
     * <code>CIPHER_ALPHABET</code>, 
     * <code>CHARACTER_ADDED_TO_CIPHER_ALPHABET</code> or
     * <code>UNKNOWN</code> changes, 
     * or if the source is a <code>MonoAlphaSubst</code> 
     * <code>numAlpha</code>'s value is  meaningless, 
     * so it's set to <code>-1</code>.
     */
    protected int numAlpha;

    /**
     * Constructs a <code>SubstitutionEvent</code> object with a type value
     * of <code>UNKNOWN</code>.
     *
     * @param source the <code>Object</code> that is the source of the event
     *               (an instance of <code>MonoAlphaSubst</code> or
     *               <code>Substitution</code>)
     */
    public SubstitutionEvent(Object source){
	super(source);
	type = UNKNOWN;
	numAlpha = -1;
    }

    /**
     * Constructs a <code>SubstitutionEvent</code> object with a type value
     * of <code>type</code> and an affected alphabet index of
     * <code>alphaIndex</code>
     *
     * @param source the <code>Object</code> that is the source of the event
     *               (an instance of <code>MonoAlphaSubst</code>
     *               <code>Substitution</code>)
     * @param type either <code>UNKNOWN</code>, <code>CIPHER_ALPHABET</code>,
     *             <code>CHARACTER_ADDED_TO_CIPHER_ALPHABET<code>,
     *             <code>SUBSTITUTION_PAIR</code>, 
     *             <code>IGNORED_CHARACTERS</code> or
     *             <code>NUMBER_OF_ALPHABETS</code>.
     * @param alphaIndex the index of the alphabet affected by the change.
     * @throws NullPointerException if <code>source</code> is null
     * @throws IllegalArgumentException if <code>source</code> is not an instance of <code>Substitution</code> or <code>MonoAlphaSubst</code> or if <code>type</code> or <code>alphaIndex</code> are not valid
     */
    public SubstitutionEvent(Object source,
			     byte type,
			     int alphaIndex) throws IllegalArgumentException,
	                                            NullPointerException
    {
	super(source);
	if(source == null){
	    throw new NullPointerException();
	}
	boolean isSubst = source instanceof Substitution ? true : false;
	boolean isMono  = source instanceof MonoAlphaSubst ? true : false;
	if(!(isSubst || isMono)){
	    throw new IllegalArgumentException("Invalid source");
	}
	switch(type){
	case SUBSTITUTION_PAIR:
	case IGNORED_CHARACTERS:
	    this.type = type;
	    if(isSubst && (alphaIndex<0 ||  alphaIndex>=((Substitution)source).getNumberOfAlphabets())){
		throw new IllegalArgumentException("alphaIndex is out of bounds");
	    }
	    numAlpha = alphaIndex;
	    break;
	case NUMBER_OF_ALPHABETS:
	    if(isMono){
		throw new IllegalArgumentException("Illegal type for the source");
	    }
	case CHARACTER_ADDED_TO_CIPHER_ALPHABET:
	case CHARACTER_ADDED_TO_PLAIN_ALPHABET:
	case CIPHER_ALPHABET:
	case UNKNOWN: 
	    this.type = type;
	    numAlpha = -1;
	    break;
	default: throw new IllegalArgumentException("Illegal value for type");
	}
    }

    /**
     * Constructs a <code>SubstitutionEvent</code> object with a type value
     * of <code>type</code> and an affected alphabet index of
     * <code>-1</code>.
     *
     * @param source the <code>Object</code> that is the source of the event
     *               (an instance of <code>MonoAlphaSubst</code> 
     *               <code>Substitution</code>)
     * @param type either <code>UNKNOWN</code>, <code>CIPHER_ALPHABET</code>,
     *             <code>CHARACTER_ADDED_TO_CIPHER_ALPHABET<code>,
     *             <code>SUBSTITUTION_PAIR</code>, 
     *             <code>IGNORED_CHARACTERS</code> or
     *             <code>NUMBER_OF_ALPHABETS</code>.
     * @throws NullPointerException if <code>source</code> is null
     * @throws IllegalArgumentException if <code>source</code> is not an instance of <code>Substitution</code> or <code>MonoAlphaSubst</code>, or if <code>type</code> or <code>alphaIndex</code> are not valid
     */
    public SubstitutionEvent(Object source,
			     byte type) throws IllegalArgumentException,
					       NullPointerException
    {
	this(source, type, -1);
    }

    /**
     * Returns the kind of state change that took place. Either 
     * <code>UNKNOWN</code>, <code>CIPHER_ALPHABET</code>,
     * <code>CHARACTER_ADDED_TO_CIPHER_ALPHABET<code>,
     * <code>SUBSTITUTION_PAIR</code>,  <code>IGNORED_CHARACTERS</code>
     * or <code>NUMBER_OF_ALPHABETS</code>.
     *
     * @return the kind of state change that took place. Either
     *         <code>UNKNOWN</code>, <code>CIPHER_ALPHABET</code>,
     *         <code>CHARACTER_ADDED_TO_CIPHER_ALPHABET<code>,
     *         <code>SUBSTITUTION_PAIR</code>, <code>IGNORED_CHARACTERS</code>
     *         or <code>NUMBER_OF_ALPHABETS</code>.
     * @see #UNKNOWN
     * @see #CIPHER_ALPHABET
     * @see #CHARACTER_ADDED_TO_CIPHER_ALPHABET
     * @see #SUBSTITUTION_PAIR
     * @see #IGNORED_CHARACTERS
     * @see #NUMBER_OF_ALPHABETS
     */
    public byte getChangeType(){
	return type;
    }

    /**
     * Returns the index of the alphabet in the <code>Substituion</code>
     * affected by the change.<br/>
     * Note: You might want to check the source <code>Substitution</code>'s
     * number of alphabets if you plan to use this.<br/>
     * Note: In the case of <code>CIPHER_ALPHABET</code>, 
     * <code>CHARACTER_ADDED_TO_CIPHER_ALPHABET<code>, 
     * <code>NUMBER_OF_ALPHABETS</code>,
     * <code>UNKNOWN</code> changes, or if the source is a 
     * <code>MonoAlphaSubst</code> this value is meaningless, so it's set
     * to <code>-1</code>.
     *
     * @return the index of the alphabet in the <code>Substituion</code>
     *         affected by the change or <code>-1</code>.
     */
    public int getAffectedAlphabetIndex(){
	return numAlpha;
    }
}
/*
 * -- SubstitutionEvent.java ends here --
 */
