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

package prototype.pa55nyaps.dataobjects;

import java.io.Serializable;

/**
 * A class that encapsulates a ciphertext along with its
 * HMAC, password salt and AES counter mode initialisation vector.
 * 
 * @author Anirban Basu
 *
 */
public class Ciphertext implements Serializable {
	private static final long serialVersionUID = 1L;
	private String salt;
	private String iv;
	private String ciphertext;
	private String hmac;
	
	private int keySize;
	private int saltSize;
	private int ivSize;
	private int hmacKeySize;
	
	public String getSalt() {
		return salt;
	}
	public void setSalt(String salt) {
		this.salt = salt;
	}
	public String getIv() {
		return iv;
	}
	public void setIv(String iv) {
		this.iv = iv;
	}
	public String getCiphertext() {
		return ciphertext;
	}
	public void setCiphertext(String ciphertext) {
		this.ciphertext = ciphertext;
	}
	public String getHmac() {
		return hmac;
	}
	public void setHmac(String hmac) {
		this.hmac = hmac;
	}
	public int getKeySize() {
		return keySize;
	}
	public void setKeySize(int keySize) {
		this.keySize = keySize;
	}
	public int getSaltSize() {
		return saltSize;
	}
	public void setSaltSize(int saltSize) {
		this.saltSize = saltSize;
	}
	public int getIvSize() {
		return ivSize;
	}
	public void setIvSize(int ivSize) {
		this.ivSize = ivSize;
	}
	public int getHmacKeySize() {
		return hmacKeySize;
	}
	public void setHmacKeySize(int hmacKeySize) {
		this.hmacKeySize = hmacKeySize;
	}
}
