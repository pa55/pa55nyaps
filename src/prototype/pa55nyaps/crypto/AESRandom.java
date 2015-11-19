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

package prototype.pa55nyaps.crypto;

import java.nio.ByteBuffer;
import java.security.Key;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;

/**
 * An 128-bit AES cipher based random number generator. The generator can be seeded by a byte stream
 * 128-bits long. This seed is used as the key for the AES cipher in counter mode while the initialisation
 * vector is set to 0. A plaintext of the same size as the cipher's block size (i.e., 128-bits) is also
 * initialised and set to zero. Post-initialisation, the random number generator can output random
 * positive integers between 0 and some upper bound.
 * 
 * This random number generator DOES NOT extend or conform to java.util.Random.
 * 
 * @author Anirban Basu, Juan Camilo Corena
 *
 */
public class AESRandom {
	private static final String AES_MODE = "AES/CTR/NoPadding";    
    public static int keySize = 128; //size in bits
    
    private Cipher cipher;
    private byte[] seed;
    private byte[] plaintext;
    private ByteBuffer buffer;
    
    public AESRandom(byte[] seed) {
        try {
        	if(seed.length*8!=keySize) {
        		throw new InvalidKeySpecException("The seed size should be exactly " + keySize + " bits.");
        	}
        	this.seed = seed;
        	cipher = Cipher.getInstance(AES_MODE);
			cipher.init(Cipher.ENCRYPT_MODE, initRawKey(), 
					new IvParameterSpec(ByteBuffer.allocate(keySize/8).array()));
			this.plaintext = ByteBuffer.allocate(cipher.getBlockSize()).array();
			this.buffer = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    private AESKey initRawKey() {
    	return new AESKey(this.seed);
    }
    
    /**
     * This call increments the counter of the AES cipher and outputs the 'encryption' of
     * the pre-initialised zeroed plaintext of length equal to the cipher's block size.
     * 
     * @return random bytes of length equal to the cipher's block size.
     */
    protected synchronized byte[] next() {
    	byte[] result = cipher.update(this.plaintext); //increase the counter, get the next block of the cipher and encrypt
    	return result;
    }
    
    private int nextIntFromBuffer() {
    	if (buffer == null || !buffer.hasRemaining()) {
    		buffer = ByteBuffer.wrap(next());
    	}
    	return Math.abs(buffer.getInt());
    }
    
    private int nextByteFromBuffer() {
    	if (buffer == null || !buffer.hasRemaining()) {
    		buffer = ByteBuffer.wrap(next());
    	}
    	// Get a byte that could be negative.
    	return (int) buffer.get() & 0xff;
    }
    
    private int getPowerOfTwoBound(int bound) {
    	if(bound<=1) {
    		throw new IllegalArgumentException("The bound should be a positive number between 2 and " + Integer.MAX_VALUE);
    	}
    	int hob = bound;
    	hob |= (hob >>  1);
    	hob |= (hob >>  2);
    	hob |= (hob >>  4);
    	hob |= (hob >>  8);
    	hob |= (hob >> 16);
    	hob = hob - (hob >>> 1);
    	return ((bound & (bound-1))==0)? bound : hob << 1;
    }
    
    /**
     * Given a positive integer bound between 2 and the maximum value of unsigned byte, this method outputs a random number
     * between 0 (inclusive) and the bound (exclusive). If the bound is not a power of two then the smallest power of two,
     * that is larger than the given bound is used.
     * 
     * @param bound a positive integer in the range [2 255]
     * @return random integer in the range [0 bound).
     */
    public int nextByte(int bound) {
    	if (bound < 2 || bound >= 256) {
    		throw new IllegalArgumentException("The bound for bytes should be between 2 and 255");
    	}
    	int actual_bound = getPowerOfTwoBound(bound);
    	int desired = 256;
    	while(desired >= bound) {
    		desired = nextByteFromBuffer() % actual_bound;
    	}
    	return desired; 
	}
    
    /**
     * Given a positive integer bound between 2 and the 2^31, this method outputs a random number
     * between 0 (inclusive) and the bound (exclusive). If the bound is not a power of two then the smallest power of two,
     * that is larger than the given bound is used.
     * 
     * @param bound a positive integer in the range [2 and 2^31]
     * @return
     */
    public int nextInt(int bound) {
    	if (bound < 2 || bound > (1<<30)) {
    		throw new IllegalArgumentException("The bound for nextInt should be between 2 and 2^31.");
    	}
    	int actual_bound = getPowerOfTwoBound(bound);
    	int desired = (1<<30);
    	while(desired >= bound) {
    		desired = nextIntFromBuffer() % actual_bound;
    	}
    	return desired; 
	}
    
    /**
     * A wrapper class for the RAW (unencoded in bytes) key for AES.
     *
     */
    public class AESKey implements Key {
		private static final long serialVersionUID = 1L;
		
		private byte[] rawKey;
		
		public AESKey(byte[] rawKey) {
			this.rawKey = rawKey;
		}

		@Override
		public String getAlgorithm() {
			return "AES";
		}

		@Override
		public String getFormat() {
			return "RAW";
		}

		@Override
		public byte[] getEncoded() {
			return rawKey;
		} 	
    }
   
    /*
     * 
    public static void main(String[] args) {
    	byte[] bytes = new byte[16];
    	Random random = new Random(823740);
    	random.nextBytes(bytes);
    	int samples = 16;
		AESRandom aesRandom = new AESRandom(bytes);
		List<Integer> list = new LinkedList<>(); 
		for (int i = 0; i < samples; ++i) {
			list.add(aesRandom.nextByteFromBuffer());
		}
		// Generate the same numbers using nextInt.
		int bound = 110;
		random = new Random(823740);
		random.nextBytes(bytes);
		aesRandom = new AESRandom(bytes);
		List<Integer> listBound = new LinkedList<>();
		for (int i = 0; i < samples; ++i) {
			listBound.add(aesRandom.nextByte(bound));
		}
		// Compare the two lists, to see we are actually discarding integers larger
		// than bound in the stream.
		Iterator<Integer> original = list.iterator();
		Iterator<Integer> other = listBound.iterator();
		while (original.hasNext() || other.hasNext()) {
			System.out.println(original.next()%128 + " " + other.next());
		}
	}
	
	*/
}
