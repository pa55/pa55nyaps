/*
PA55 NYAPS Java Reference Implementation

Copyright 2015 Anirban Basu

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package prototype.pa55nyaps.core;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import prototype.pa55nyaps.crypto.AESRandom;
import prototype.pa55nyaps.dataobjects.UserPreference;

/**
 * This class exposes the functionality to convert a random (yet deterministic, depending on user input)
 * byte stream into string of characters drawn from one or more predetermined or user-specified character
 * sets.
 * 
 * @author Anirban Basu
 *
 */
public class NYAPSEncoder {
	public static final int MIN_LENGTH = 3;
	public static final int DEFAULT_LENGTH = 16;
	public static final int MAX_LENGTH = 64;
	public static final int MIN_ISSUE = 1;
	public static final int DEFAULT_ISSUE = 1;
	public static final int MAX_ISSUE = Integer.MAX_VALUE;
	
	private static final String CHARS_LOWERCASE = "abcdefghijkmnopqrstuvwxyz";
	private static final String CHARS_UPPERCASE = "ABCDEFGHJKLMNPQRSTUVWXYZ";
	private static final String CHARS_DIGITS = "0123456789";
	private static final String CHARS_SPECIAL = "!=+@#$?%^&/:*_,";
	private static final String CHARS_BRACKETS = "(){}[]<>";
	private static final int MAX_CHARS_SIZE = CHARS_LOWERCASE.length() + 
											 CHARS_UPPERCASE.length() +
											 CHARS_DIGITS.length() +
											 CHARS_SPECIAL.length() +
											 CHARS_BRACKETS.length();
	
	/**
	 * This is an enumeration of the types of characters to be used during the encoding.
	 * The implicit ordering of the enum elements must be consistent across implementations.
	 *
	 */
	public enum CharacterType {
		brackets,
		digits,
		lowercase,
		special,
		uppercase,
		user;
	}
	
	private Map<CharacterType, String> charsets = new HashMap<CharacterType, String>();
	private List<UserPreference> userPreferences = new ArrayList<UserPreference>();
	private long shuffleSeed = 0;
	private String assignedUserCharset = null;
	
	/**
	 * Initialises the encoder.
	 */
	public NYAPSEncoder() {
		charsets.put(CharacterType.lowercase, CHARS_LOWERCASE);
		charsets.put(CharacterType.uppercase, CHARS_UPPERCASE);
		charsets.put(CharacterType.digits, CHARS_DIGITS);
		charsets.put(CharacterType.special, CHARS_SPECIAL);
		charsets.put(CharacterType.brackets, CHARS_BRACKETS);
	}

	private String removeCharacters(String from, String in) {
		StringBuilder result = new StringBuilder();
		for(char c : from.toCharArray()) {
			if(in.indexOf(c)==-1) {
				result.append(c);
			}
		}
		return result.toString();
	}
	
	/**
	 * Helps assign a user-defined character set as a string of characters. This character set is
	 * checked against the existing ones for the repetition(s) of any character(s). Only characters non-existent
	 * in the other character sets are added.
	 * 
	 * @param characters
	 * @deprecated user-defined character sets are discouraged due to various problems of portability
	 * across different platforms; however, they may be enabled in future.
	 */
	public boolean assignUserCharacterSet(String characters) {
		if(characters!=null) {
			String fixedCharset = "";
			for(String str : charsets.values()) {
				fixedCharset += str;
			}
			String userCharacters = removeCharacters(characters,fixedCharset);
			if(userCharacters.length()>0) {
				this.assignedUserCharset = userCharacters;
				charsets.put(CharacterType.user, assignedUserCharset);
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Given a list of user preferences, this method sorts them according to the ordinal partial order
	 * of the enum elements representing the character types.
	 * 
	 * @param preferences
	 * @return
	 */
	public static List<UserPreference> userPreferencesSortedByCharacterType(List<UserPreference> preferences) {
		Collections.sort(preferences, new Comparator<UserPreference>() {
			@Override
			public int compare(UserPreference o1,
					UserPreference o2) {
				return o1.characterType.ordinal() - o2.characterType.ordinal();
			}
		});
		return preferences;
	}
	
	
	/**
	 * The function to generate a string of a desired length with characters picked from the 
	 * preferred character types satisfying minimum and maximum constraints of each character type.
	 * @param randomSeeds
	 * @param length
	 * @return
	 */
	public String encode(byte[] randomSeeds, int length) {
		StringBuilder intermediate = new StringBuilder(length); //intermediate password string
		StringBuilder concatenatedCharsetsBuilder = new StringBuilder(MAX_CHARS_SIZE);
		int byteOffset = AESRandom.keySize/8;
		//first keySize bits
		byte[] characterSelectorSeed = ByteBuffer.allocate(byteOffset).put(randomSeeds, 0, byteOffset).array();
		//second keySize bits
		byte[] shuffleSeed = ByteBuffer.allocate(byteOffset).put(randomSeeds, byteOffset, byteOffset).array();
		//character type indices cumulative count, which is used to determine the type of character for a picked index
		List<CharacterTypeCount> typeIndexCounts = new ArrayList<CharacterTypeCount>();
		//current maximum constraints for each character type
		Map<CharacterType, Integer> typeMaximumCounts = new HashMap<CharacterType, Integer>();
		//sum of maximum constraints
		int sumOfMaximumConstraints = 0; //set this to -1 to ignore under allocation check
		
		//AES deterministic random bits generator
		AESRandom characterSelector = new AESRandom(characterSelectorSeed);
		//Satisfy the minimum constraints
		for(int i=0; i<userPreferences.size(); i++) {
			UserPreference up = userPreferences.get(i);
			if(up.minimum > 0) {
				String charset = charsets.get(up.characterType);
				concatenatedCharsetsBuilder.append(charset);
				//set the end index (cumulative indices) for each character type
				typeIndexCounts.add(new CharacterTypeCount(up.characterType, concatenatedCharsetsBuilder.length()));
				//set the maximum counts to expected minimum constraints for each character type
				typeMaximumCounts.put(up.characterType, up.minimum);
				//sum the maximum constraints to check for under allocation
				if(up.minimum <= up.maximum && sumOfMaximumConstraints!=-1) {
					sumOfMaximumConstraints += up.maximum;
				}
				else if(up.maximum < 0) {
					sumOfMaximumConstraints = -1;
				}
				//pick until minimum constraint is satisfied
				for(int minConstraint = 0; minConstraint < up.minimum; minConstraint++) {
					int pos = characterSelector.nextByte(charset.length());
					intermediate.append(charset.charAt(pos));
				}
			}
		}
		
		//Check for over allocation: required length is too small to accommodate the minimum allowed characters
		if(intermediate.length() > length) {
			throw new IllegalArgumentException("The required password length " + length + 
					" is too short to accommodate the minimum number of characters from the selected character classes.");
		}
		
		//Check for under allocation: required length is too long to accommodate the maximum allowed characters
		if(sumOfMaximumConstraints > 0 && sumOfMaximumConstraints < length) {
			throw new IllegalArgumentException("The required password length " + length + 
					" is longer than the total number of maximum number of characters from the selected character classes.");
		}
		
		//Keep picking characters at random from the concatenated characters, satisfying maximum constraints if any
		while(intermediate.length() < length) {
			//pick the next position to select a character from the concatenated character set
			int position = characterSelector.nextByte(concatenatedCharsetsBuilder.length());
			UserPreference prefForCharTypeAtSelectedPosition = null;
			CharacterType positionPickedType = null;
			//Determine the character type for the character at the selected position
			for(int i=0; i<typeIndexCounts.size(); i++) {
				CharacterTypeCount ctc = typeIndexCounts.get(i);
				if(position < ctc.count) {
					positionPickedType = ctc.characterType;
					break;
				}
			}
			//This is really not supposed to happen!
			if(positionPickedType == null) {
				throw new NullPointerException("The character type for the selected character cannot be determined.");
			}
			//find the corresponding user preference
			for(int i=0; i<userPreferences.size(); i++) {
				UserPreference up = userPreferences.get(i);
				if(up.characterType == positionPickedType) {
					prefForCharTypeAtSelectedPosition = up;
				}
			}
			//This is really not supposed to happen either!
			if(prefForCharTypeAtSelectedPosition == null) {
				throw new NullPointerException("The user preference for the selected character cannot be determined.");
			}
			//If the minimum constraint is less than or equal to the maximumConstraint, otherwise ignore maximum constraints
			if(prefForCharTypeAtSelectedPosition.minimum <= prefForCharTypeAtSelectedPosition.maximum) {
				//Find or set, if necessary, the current number of counts for the selected character type
				Integer maxCount = typeMaximumCounts.get(positionPickedType);
				//Pick a character unless the maximum constraints for this type of characters has been satisfied
				if(maxCount < prefForCharTypeAtSelectedPosition.maximum) {
					intermediate.append(concatenatedCharsetsBuilder.charAt(position));
					maxCount++;
				}
				//update the current maximum counts for this character type
				typeMaximumCounts.put(positionPickedType, maxCount);
			}
			else {
				intermediate.append(concatenatedCharsetsBuilder.charAt(position));
			}
			//System.out.println(concatenatedCharsets.charAt(position) + " is of type " + prefForCharTypeAtSelectedPosition.characterType.toString());
		}
		
		//The shuffle method uses a Knuth's shuffle seeded with a AES DRBG
		return shuffleString(intermediate, shuffleSeed);
	}
	
	
	/**
	 * An implementation of the Knuth's shuffle.
	 * 
	 * @param data
	 * @param shuffleSeed
	 * @return
	 */
	public String shuffleString(StringBuilder data, byte[] shuffleSeed) {
		if (shuffleSeed == null) {
			return data.toString();
		}
		AESRandom rnd = new AESRandom(shuffleSeed);
		int n = data.length();
		//Knuth's shuffle
		while(n>1) {
			int k = rnd.nextInt(n--);
			char t = data.charAt(n);
			data.setCharAt(n, data.charAt(k));
			data.setCharAt(k, t);
		}
		return data.toString();
	}
	
	public List<UserPreference> getUserPreferences() {
		return userPreferences;
	}

	/**
	 * Set the user preferences making sure that no duplicates exist and that the
	 * final list of user preferences is sorted by the consistent partial order of the enum
	 * elements representing the character types.
	 * 
	 * @param userPreferences
	 */
	public void setUserPreferences(List<UserPreference> userPreferences) {
		this.userPreferences.clear();
		
		//remove duplicates from user preferences
		Hashtable<CharacterType, Boolean> seen = new Hashtable<CharacterType, Boolean>();
		for(int i=0; i<userPreferences.size(); i++) {
			UserPreference up = userPreferences.get(i);
			if(up.minimum > 0) {
				if(!seen.containsKey(up.characterType)) {
					this.userPreferences.add(up);
					seen.put(up.characterType, true);
				}
			}
		}
		
		//enforce a consistent ordering
		this.userPreferences = userPreferencesSortedByCharacterType(this.userPreferences);
		
	}

	public Map<CharacterType, String> getCharsets() {
		return charsets;
	}

	public long getShuffleSeed() {
		return shuffleSeed;
	}

}
