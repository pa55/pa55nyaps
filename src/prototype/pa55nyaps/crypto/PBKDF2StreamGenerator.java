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

package prototype.pa55nyaps.crypto;

import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.SHA512Digest;
import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.bouncycastle.crypto.params.KeyParameter;

/**
 * A wrapper class for the PBKDF2 function with a SHA512 digest as provided by
 * BouncyCastle.
 * 
 * @author Anirban Basu
 *
 */
public class PBKDF2StreamGenerator {
	/**
	 * The number of rounds to be specified to the PBKDF2 function.
	 */
	public static final int PBKDF2Rounds = 25000;
	
	/**
	 * Generate a byte array of the specified length given the input password
	 * and salt.
	 * 
	 * @param password
	 * @param salt
	 * @param length
	 * @return derived key of the specified length
	 */
	public static byte[] generateStream(byte[] password, byte[] salt, int length) {
		return generateStream(password, salt, length, PBKDF2Rounds);
	}
	
	/**
	 * Generate a byte array of the specified length given the input password
	 * and salt. This method lets the user specify the number of rounds for the PBKDF2 function.
	 * 
	 * @param password
	 * @param salt
	 * @param length
	 * @param rounds 
	 * @return derived key of the specified length
	 */
	protected static byte[] generateStream(byte[] password, byte[] salt, int length, int rounds) {
		Digest digest = new SHA512Digest();
		PKCS5S2ParametersGenerator generator = new PKCS5S2ParametersGenerator(digest);
		generator.init(password, salt, rounds);
		return ((KeyParameter)generator.generateDerivedParameters(length*8)).getKey();
	}
}
