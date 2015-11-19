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

package prototype.pa55nyaps.gui.model;

import java.util.ArrayList;
import java.util.List;

import prototype.pa55nyaps.core.NYAPSEncoder;
import prototype.pa55nyaps.dataobjects.ExtendedNotes;
import prototype.pa55nyaps.dataobjects.PasswordDatabaseEntry;
import prototype.pa55nyaps.dataobjects.UserPreference;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class PasswordDatabaseEntryModel {
	public PasswordDatabaseEntryModel(StringProperty id,
			List<UserPreferenceModel> characterTypes,
			StringProperty userDefinedCharacters, ExtendedNotesModel notes,
			IntegerProperty issue, IntegerProperty length) {
		this.id = id;
		this.characterTypes = characterTypes;
		this.userDefinedCharacters = userDefinedCharacters;
		this.notes = notes;
		this.issue = issue;
		this.length = length;
	}
	
	public PasswordDatabaseEntryModel(String id) {
		this.id = new SimpleStringProperty(id);
		this.characterTypes = new ArrayList<UserPreferenceModel>();
		this.characterTypes.add(new UserPreferenceModel(NYAPSEncoder.CharacterType.digits, 1));
		this.characterTypes.add(new UserPreferenceModel(NYAPSEncoder.CharacterType.lowercase, 1));
		this.characterTypes.add(new UserPreferenceModel(NYAPSEncoder.CharacterType.special, 1));
		this.characterTypes.add(new UserPreferenceModel(NYAPSEncoder.CharacterType.uppercase, 1));
		this.notes = new ExtendedNotesModel(id);
		this.issue = new SimpleIntegerProperty(PasswordDatabaseEntry.DEFAULT_ISSUE);
		this.length = new SimpleIntegerProperty(PasswordDatabaseEntry.DEFAULT_LENGTH);
	}
	
	public PasswordDatabaseEntryModel() { }
	
	public PasswordDatabaseEntryModel(PasswordDatabaseEntry entry) {
		this.id = new SimpleStringProperty(entry.getId());
		this.characterTypes = new ArrayList<UserPreferenceModel>();
		for(UserPreference up : entry.getCharacterTypes()) {
			this.characterTypes.add(new UserPreferenceModel(up));
		}
		this.notes = new ExtendedNotesModel(entry.getNotes());
		this.issue = new SimpleIntegerProperty(entry.getIssue());
		this.length = new SimpleIntegerProperty(entry.getLength());
	}
	
	private StringProperty id;
	private List<UserPreferenceModel> characterTypes;
	private StringProperty userDefinedCharacters;
	private ExtendedNotesModel notes;
	private IntegerProperty issue;
	private IntegerProperty length;
	
	public StringProperty getId() {
		return id;
	}
	public void setId(StringProperty id) {
		this.id = id;
	}
	public List<UserPreferenceModel> getCharacterTypes() {
		return characterTypes;
	}
	public void setCharacterTypes(List<UserPreferenceModel> characterTypes) {
		this.characterTypes = characterTypes;
	}
	public StringProperty getUserDefinedCharacters() {
		return userDefinedCharacters;
	}
	public void setUserDefinedCharacters(StringProperty userDefinedCharacters) {
		this.userDefinedCharacters = userDefinedCharacters;
	}
	public ExtendedNotesModel getNotes() {
		return notes;
	}
	public void setNotes(ExtendedNotesModel notes) {
		this.notes = notes;
	}
	public IntegerProperty getIssue() {
		return issue;
	}
	public void setIssue(IntegerProperty issue) {
		this.issue = issue;
	}
	public IntegerProperty getLength() {
		return length;
	}
	public void setLength(IntegerProperty length) {
		this.length = length;
	}
	
	public PasswordDatabaseEntry dataObject() {
		ExtendedNotes notes = this.getNotes().dataObject();
		List<UserPreference> charTypes = new ArrayList<>();
		for(UserPreferenceModel upm : this.characterTypes) {
			charTypes.add(upm.dataObject());
		}
		PasswordDatabaseEntry retVal;
		if(this.userDefinedCharacters==null) {
			retVal = new PasswordDatabaseEntry(this.getId().get(),
					charTypes,
					null, notes,
					this.getIssue().intValue(), this.getLength().intValue());
		}
		else {
			retVal = new PasswordDatabaseEntry(this.getId().get(),
				charTypes,
				this.userDefinedCharacters.get(), notes,
				this.getIssue().intValue(), this.getLength().intValue());
		}
		return retVal;
	}
}
