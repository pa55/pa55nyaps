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
import java.util.HashMap;

public class PasswordDatabase implements Serializable {
	private static final long serialVersionUID = 1L;
	private String version = "pa55v2_fs1.0";
	private String randomBits;
	private int randomBitsSize;
	private HashMap<String, PasswordDatabaseEntry> database = new HashMap<String, PasswordDatabaseEntry>();
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public HashMap<String, PasswordDatabaseEntry> getDatabase() {
		return database;
	}
	public void setDatabase(HashMap<String, PasswordDatabaseEntry> database) {
		this.database = database;
	}
	public String getRandomBits() {
		return randomBits;
	}
	public void setRandomBits(String randomBits) {
		this.randomBits = randomBits;
	}
	public int getRandomBitsSize() {
		return randomBitsSize;
	}
	public void setRandomBitsSize(int randomBitsSize) {
		this.randomBitsSize = randomBitsSize;
	}
}
