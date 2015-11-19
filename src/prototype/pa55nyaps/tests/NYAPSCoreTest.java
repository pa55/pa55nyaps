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

package prototype.pa55nyaps.tests;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import prototype.pa55nyaps.core.NYAPSCore;
import prototype.pa55nyaps.core.NYAPSEncoder;
import prototype.pa55nyaps.dataobjects.UserPreference;


/**
 * Tests the NYAPSCore for consistency (with Objective C) of password generation.
 *  
 * @author Anirban Basu
 *
 */
public class NYAPSCoreTest {

	@Test
	public void testSinglePasswordGenerationConsistency() {
		System.out.println("Executing: " + Thread.currentThread().getStackTrace()[1].getMethodName());
		String phrase = "hello world";
		String hint = "test";
		String expectedPassword = "dQ#gkSB{=xo$1<Gn0)v*&6BA9BG(RpA{";
		
		try {
			List<UserPreference> userPreferences = new ArrayList<UserPreference>();
			userPreferences.add(new UserPreference(NYAPSEncoder.CharacterType.brackets, 1));
			userPreferences.add(new UserPreference(NYAPSEncoder.CharacterType.digits, 1));
			userPreferences.add(new UserPreference(NYAPSEncoder.CharacterType.lowercase, 1));
			userPreferences.add(new UserPreference(NYAPSEncoder.CharacterType.special, 1));
			userPreferences.add(new UserPreference(NYAPSEncoder.CharacterType.uppercase, 1));
			
			String password = NYAPSCore.generatePasswordWithAESDRBG(phrase, hint, 32, userPreferences, null);
			assertEquals(expectedPassword, password);
			System.out.println("\tPassword (" + password.length() + " chars): " + password);
		}
		catch(Exception e) {
			e.printStackTrace(System.err);
		}
		finally {
			System.out.println("Executing: " + Thread.currentThread().getStackTrace()[1].getMethodName());
		}
	}

}
