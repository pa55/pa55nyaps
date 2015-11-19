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

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.util.encoders.Base64;

import prototype.pa55nyaps.dataobjects.Ciphertext;

/**
 * A 128-bit AES cryptosystem helper.
 * 
 * @author Juan Camilo Corena, Anirban Basu
 *
 */
public class AESCryptosystem {
	private static final String BYTE_ENCODING = "UTF-8";
	private static final String AES_MODE_CTR = "AES/CTR/NoPadding";
    private static int keySize = 16; //bytes
    private static int hmacKeySize = 32; //bytes
    private static int saltSize = 16; //bytes
    private static int ivSize = 16; //bytes
    private static final String SECRET_KEY_ALGO = "AES";
    private static final String HMAC_ALGO = "HmacSHA256";
    private static AESCryptosystem singletonInstance = null;
    
    public static synchronized AESCryptosystem getInstance() {
    	if(singletonInstance == null) {
    		singletonInstance = new AESCryptosystem();
    	}
    	return singletonInstance;
    }
    
    private AESCryptosystem() {};
    
    private byte[] generateRandomBytes(int size) { //size is in bytes
    	SecureRandom random = new SecureRandom();
        byte bytes[] = new byte[size]; 
        random.nextBytes(bytes);
        return bytes;
    }
    
    private String generateMacForData(byte[] data, byte[] key) throws NoSuchAlgorithmException,
    	InvalidKeyException, NoSuchProviderException {
    	SecretKeySpec keySpec = new SecretKeySpec(key, HMAC_ALGO);
    	Mac mac = Mac.getInstance(HMAC_ALGO);
    	mac.init(keySpec);
    	return Base64.toBase64String(mac.doFinal(data));
    }
   
    /**
     * Encrypts a plaintext with a password and generates the corresponding ciphertext with a 256-bit
     * HMAC. This method expands the given password using a password-based key derivation function to generate
     * two keys making use of a random salt. The first key is used the encrypt the plaintext in AES counter
     * mode using a random initialisation vector while the other key is used to generate the 256-bit HMAC.
     * 
     * @param plaintext
     * @param password
     * @return a ciphertext with HMAC
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws InvalidParameterSpecException
     * @throws UnsupportedEncodingException
     * @throws InvalidAlgorithmParameterException
     * @throws NoSuchProviderException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     */
    public Ciphertext encryptWithHmac(String plaintext, String password) throws NoSuchAlgorithmException, InvalidKeySpecException,
		NoSuchPaddingException, InvalidKeyException, InvalidParameterSpecException, UnsupportedEncodingException,
		InvalidAlgorithmParameterException, NoSuchProviderException, IllegalBlockSizeException, BadPaddingException {
		//get random salt
		byte[] salt = generateRandomBytes(saltSize);
		//get random iv
		byte[] randomIV = generateRandomBytes(ivSize);
		//generate concatenated keys and split them into relevant keys
    	byte[] concatenatedKeys = PBKDF2StreamGenerator.generateStream(
    			password.getBytes(BYTE_ENCODING), salt, (keySize + hmacKeySize));
    	byte[] encryptionKey = Arrays.copyOfRange(concatenatedKeys, 0, keySize);
	    byte[] hmacKey = Arrays.copyOfRange(concatenatedKeys, keySize, (keySize + hmacKeySize));
		//setup the encryption cipher to encrypt data
	    Cipher dataEncryptionCipher = Cipher.getInstance(AES_MODE_CTR);
	    dataEncryptionCipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(encryptionKey, SECRET_KEY_ALGO), new IvParameterSpec(randomIV));
	    //create empty result data structure
	    Ciphertext result = new Ciphertext();
	    //encrypt the data
	    byte[] rawCiphertext = dataEncryptionCipher.doFinal(plaintext.getBytes(BYTE_ENCODING));
	    result.setCiphertext(Base64.toBase64String(rawCiphertext));
	    result.setSalt(Base64.toBase64String(salt));
	    //encrypt the random iv
	    result.setIv(Base64.toBase64String(randomIV));
	    //generate and set the hmac of the ciphertext concatenated with the encrypted iv and the salt
	    byte[] concatenatedHmacData = new byte[salt.length + randomIV.length + rawCiphertext.length];
	    System.arraycopy(salt, 0, concatenatedHmacData, 0, salt.length);
	    System.arraycopy(randomIV, 0, concatenatedHmacData, salt.length, randomIV.length);
	    System.arraycopy(rawCiphertext, 0, concatenatedHmacData, salt.length + randomIV.length, rawCiphertext.length);
	    result.setHmac(generateMacForData(concatenatedHmacData, hmacKey));
	    result.setKeySize(keySize);
	    result.setSaltSize(saltSize);
	    result.setIvSize(ivSize);
	    result.setHmacKeySize(hmacKeySize);
	    return result;
    }
    
    /**
     * Given a ciphertext with its HMAC, this method decrypts it with the given password. The password is expanded using
     * the stored salt and a password-based key derivation function to generate two keys: the first one is used for the 
     * decryption while the second one is used to validate the HMAC. Decryption does not start until the HMAC is verified.
     * Decryption runs in AES counter mode with the initialisation vector stored in the ciphertext object.
     *  
     * @param ciphertext
     * @param password
     * @return the plaintext
     * @throws InvalidKeyException
     * @throws InvalidAlgorithmParameterException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws UnsupportedEncodingException
     * @throws InvalidKeySpecException
     * @throws NoSuchProviderException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     */
    public String decryptWithHmac(Ciphertext ciphertext, String password) throws InvalidKeyException, InvalidAlgorithmParameterException,
    	NoSuchAlgorithmException, NoSuchPaddingException, UnsupportedEncodingException, InvalidKeySpecException, NoSuchProviderException, 
    	IllegalBlockSizeException, BadPaddingException {
    	//decode from Base64
    	byte[] salt = Base64.decode(ciphertext.getSalt());
    	byte[] randomIV = Base64.decode(ciphertext.getIv());
    	byte[] rawCiphertext = Base64.decode(ciphertext.getCiphertext());
    	//generate concatenated keys and split them into relevant keys
    	byte[] concatenatedKeys = PBKDF2StreamGenerator.generateStream(
    			password.getBytes(BYTE_ENCODING), salt, (ciphertext.getKeySize() + ciphertext.getHmacKeySize()));
    	byte[] encryptionKey = Arrays.copyOfRange(concatenatedKeys, 0, ciphertext.getKeySize());
	    byte[] hmacKey = Arrays.copyOfRange(concatenatedKeys, ciphertext.getKeySize(), (ciphertext.getKeySize() + ciphertext.getHmacKeySize()));
    	//generate secret key
        SecretKeySpec secret = new SecretKeySpec(encryptionKey,SECRET_KEY_ALGO);
        if(ciphertext.getHmac().length()>0) {
        	//verify hmac if it is present
        	byte[] concatenatedHmacData = new byte[salt.length + randomIV.length + rawCiphertext.length];
        	System.arraycopy(salt, 0, concatenatedHmacData, 0, salt.length);
    	    System.arraycopy(randomIV, 0, concatenatedHmacData, salt.length, randomIV.length);
    	    System.arraycopy(rawCiphertext, 0, concatenatedHmacData, salt.length + randomIV.length, rawCiphertext.length);
	        String hmac = generateMacForData(concatenatedHmacData, hmacKey);
	        if(hmac.compareTo(ciphertext.getHmac())!=0) {
	        	//do not proceed if this happens
	        	throw new SecurityException("The ciphertext and its parameters fail the message integrity test. Decryption aborted.");
	        }
        }
        //setup the data and parameter decryption ciphers
        Cipher dataDecryptionCipher = Cipher.getInstance(AES_MODE_CTR);
    	dataDecryptionCipher.init(Cipher.DECRYPT_MODE, secret, new IvParameterSpec(randomIV));
    	//decrypt the message
        return new String(dataDecryptionCipher.doFinal(rawCiphertext), BYTE_ENCODING);
    }
}
