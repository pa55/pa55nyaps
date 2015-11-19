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

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

import org.junit.Test;

import prototype.pa55nyaps.crypto.AESCryptosystem;
import prototype.pa55nyaps.dataobjects.Ciphertext;

/**
 * A class to test the consistency of the AES cryptosystem.
 * 
 * @author Anirban Basu
 *
 */
public class AESCryptosystemTest {

	/**
	 * Generate a random password and test the decryption consistency with one fixed plaintext.
	 */
	@Test
	public void testSingleDecryptionConsistency() {
		System.out.println("Executing: " + Thread.currentThread().getStackTrace()[1].getMethodName());
		AESCryptosystem cryptosystem = AESCryptosystem.getInstance();
		SecureRandom srand = new SecureRandom();
    	String password = Long.toHexString(srand.nextLong()); //just to use a different password every time
    	String testPlaintext = "Not the typical hello world message! As you can see, it is a big longer than usual.";
    	System.out.println("\tPassword used: " + password);
    	System.out.println("\tPlaintext used: " + testPlaintext);
    	try {
    		Ciphertext ciphertext = cryptosystem.encryptWithHmac(testPlaintext, password);
    		assertEquals(cryptosystem.decryptWithHmac(ciphertext, password), 
    				testPlaintext);
    	}
    	catch(Exception e) {
    		e.printStackTrace(System.err);
    	}
    	finally {
    		System.out.println("Finished: " + Thread.currentThread().getStackTrace()[1].getMethodName());
    	}
	}
	
	/**
	 * Generate a number of random passwords and test the decryption consistencies with shuffled plaintexts.
	 */
	@Test
	public void testMultipleDecryptionConsistencies() {
		int repeat = 10;
		System.out.println("Executing: " + Thread.currentThread().getStackTrace()[1].getMethodName());
		System.out.println("\tWill run the decryption consistency check " + repeat + " times.");
		AESCryptosystem cryptosystem = AESCryptosystem.getInstance();
		SecureRandom srand = new SecureRandom();
    	String fixedPlaintext = "lorem ipsum dolor sit amet consectetur adipiscing elit";
    	try {
    		for(int i=0; i<repeat; i++) {
	    		String password = Long.toHexString(srand.nextLong());
	    		StringBuilder shuffledParts = new StringBuilder();
	    		String singleDelimiter = " ";
	    		StringTokenizer st = new StringTokenizer(fixedPlaintext, singleDelimiter);
	    		List<String> wordList = new ArrayList<String>();
	    		while(st.hasMoreTokens()) {
	    			wordList.add(st.nextToken());
	    		}
	    		Collections.shuffle(wordList, srand);
	    		for(int j=0; j<wordList.size(); j++) {
	    			shuffledParts.append(wordList.get(j));
	    			if(j!=(wordList.size()-1)) {
	    				shuffledParts.append(singleDelimiter);
	    			}
	    		}
	    		String testPlaintext = shuffledParts.toString();
	    		System.out.println("\tIteration: " + (i+1));
	    		System.out.println("\t\tPassword used: " + password);
	    		System.out.println("\t\tPlaintext used: " + testPlaintext);
	    		Ciphertext ciphertext = cryptosystem.encryptWithHmac(testPlaintext, password);
	    		assertEquals(cryptosystem.decryptWithHmac(ciphertext, password), 
	    			testPlaintext);
    		}
    	}
    	catch(Exception e) {
    		e.printStackTrace(System.err);
    	}
    	finally {
    		System.out.println("Finished: " + Thread.currentThread().getStackTrace()[1].getMethodName());
    	}
	}
}
