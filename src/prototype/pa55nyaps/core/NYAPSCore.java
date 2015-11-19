/*
PA55 NYAPS Java Reference Implementation

Copyright 2015 Anirban Basu, Juan Camilo Corena

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

import java.io.UnsupportedEncodingException;
import java.util.List;

import prototype.pa55nyaps.crypto.AESRandom;
import prototype.pa55nyaps.crypto.PBKDF2StreamGenerator;
import prototype.pa55nyaps.dataobjects.UserPreference;


/**
 * The central class for PA55 NYAPS that is used to generate passwords, which are
 * deterministic based on user preferences.
 * 
 * @author Anirban Basu, Juan Camilo Corena
 *
 */
public class NYAPSCore {
	public static final String CHAR_ENCODING = "UTF-8";
	
	/**
	 * Generates a password deterministically from the user supplied parameters.
	 * 
	 * @param phrase the master secret
	 * @param hint the password hint
	 * @param length the desired length of the generated password
	 * @param userPreferences the user preferences containing the character types with minimum and maximum constraints on each type
	 * @param userCharset user-defined character set -- refrain from using this: it is okay to pass null to this
	 * @return generated password
	 * @throws UnsupportedEncodingException
	 */
	@SuppressWarnings("deprecation")
	public static String generatePasswordWithAESDRBG(String phrase, String hint, int length, List<UserPreference> userPreferences, String userCharset) throws UnsupportedEncodingException {
		NYAPSEncoder encoder = new NYAPSEncoder();
		if(userCharset!=null) {
			String trimmed = userCharset.trim();
			if(trimmed.length()>0) {
				if(encoder.assignUserCharacterSet(trimmed)) {
					userPreferences.add(new UserPreference(NYAPSEncoder.CharacterType.user, 1));
				}
			}
		}
		//must set it here, not later, to enforce consistent ordering of the user preferences when generating the concatenated sequence
		encoder.setUserPreferences(userPreferences);
		String concatenatedCharSequence = "";
		for(UserPreference up : encoder.getUserPreferences()) {
			if(up.minimum > 0) {
				concatenatedCharSequence += encoder.getCharsets().get(up.characterType);
			}
		}
		//modify the hint to make sure that any changes to the length of the password or the user preferences will have
		//significant impact on the generated password
		hint += length + concatenatedCharSequence;
		
		//generate two seeds (as concatenated byte arrays) to initialise the two AES random number generators 
		byte[] hPass = PBKDF2StreamGenerator.generateStream(phrase.getBytes(CHAR_ENCODING), hint.getBytes(CHAR_ENCODING), (AESRandom.keySize/8)*2);
		
		return encoder.encode(hPass, length);
	}
	
	/*
	public static void main(String[] args) {
		
		NYAPSCore pa55v2 = new NYAPSCore();
		String phrase = "my test master secret sentence";
		String hint = "ServiceName=facebook;ServiceLink=http://www.facebook.com;UserID=myself;AdditionalInfo=nothing1"; //the last '1' indicates issue number
		int minLength = 269;
		int maxLength = 270;
		
		List<UserPreference> userPreferences = new ArrayList<UserPreference>();
		userPreferences.add(new UserPreference(NYAPSEncoder.CharacterType.brackets, 1));
		userPreferences.add(new UserPreference(NYAPSEncoder.CharacterType.digits, 1));
		userPreferences.add(new UserPreference(NYAPSEncoder.CharacterType.lowercase, 1));
		userPreferences.add(new UserPreference(NYAPSEncoder.CharacterType.special, 1));
		userPreferences.add(new UserPreference(NYAPSEncoder.CharacterType.uppercase, 1));
		String userCharset = null;
		
		for(int i=minLength; i<=maxLength; i++) {
			String password = "";
			try {
				password = pa55v2.generatePasswordWithAESDRBG(phrase, hint, i, userPreferences, userCharset);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			if(i==password.length()) { //the generated password is of the desired length
				System.out.println("Password (" + password.length() + " chars): " + password);
			}
		}
	}
	*/
}
