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
import java.util.ArrayList;
import java.util.List;

import prototype.pa55nyaps.core.NYAPSEncoder;

public class PasswordDatabaseEntry implements Serializable {
	public static final int DEFAULT_ISSUE = 1;
	public static final int DEFAULT_LENGTH = 16;
	
	
	public PasswordDatabaseEntry(String id,
			List<UserPreference> characterTypes,
			String userDefinedCharacters, ExtendedNotes notes,
			int issue, int length) {
		this.id = id;
		this.characterTypes = characterTypes;
		this.userDefinedCharacters = userDefinedCharacters;
		this.notes = notes;
		this.issue = issue;
		this.length = length;
	}
	
	public PasswordDatabaseEntry(String id) {
		this.id = id;
		this.notes = new ExtendedNotes(id);
		this.issue = NYAPSEncoder.DEFAULT_ISSUE;
		this.length = NYAPSEncoder.DEFAULT_LENGTH;
		characterTypes.add(new UserPreference(NYAPSEncoder.CharacterType.digits, 1));
		characterTypes.add(new UserPreference(NYAPSEncoder.CharacterType.lowercase, 1));
		characterTypes.add(new UserPreference(NYAPSEncoder.CharacterType.special, 1));
		characterTypes.add(new UserPreference(NYAPSEncoder.CharacterType.uppercase, 1));
	}
	
	private static final long serialVersionUID = 1L;

	private String id;
	private List<UserPreference> characterTypes = new ArrayList<UserPreference>();
	private String userDefinedCharacters;
	private ExtendedNotes notes;
	private int issue;
	private int length;
	
	public PasswordDatabaseEntry() { }
	
	public List<UserPreference> getCharacterTypes() {
		return characterTypes;
	}
	public void setCharacterTypes(List<UserPreference> characterTypes) {
		this.characterTypes = characterTypes;
	}
	public String getUserDefinedCharacters() {
		return userDefinedCharacters;
	}
	public void setUserDefinedCharacters(String userDefinedCharacters) {
		this.userDefinedCharacters = userDefinedCharacters;
	}
	public ExtendedNotes getNotes() {
		return notes;
	}
	public void setNotes(ExtendedNotes notes) {
		this.notes = notes;
	}
	public int getIssue() {
		return issue;
	}
	public void setIssue(int issue) {
		this.issue = issue;
	}
	public int getLength() {
		return length;
	}
	public void setLength(int length) {
		this.length = length;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}
