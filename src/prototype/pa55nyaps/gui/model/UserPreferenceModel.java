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

import prototype.pa55nyaps.core.NYAPSEncoder;
import prototype.pa55nyaps.core.NYAPSEncoder.CharacterType;
import prototype.pa55nyaps.dataobjects.UserPreference;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class UserPreferenceModel {
	public UserPreferenceModel(NYAPSEncoder.CharacterType characterType,
			Integer minimum, Integer maximum) {
		this.characterType = new SimpleStringProperty(characterType.toString());
		this.minimum = new SimpleIntegerProperty(minimum);
		this.maximum = new SimpleIntegerProperty(maximum);
	}
	
	public UserPreferenceModel(NYAPSEncoder.CharacterType characterType,
			Integer minimum) {
		this.characterType = new SimpleStringProperty(characterType.toString());
		this.minimum = new SimpleIntegerProperty(minimum);
		this.maximum = new SimpleIntegerProperty(-1);
	}
	
	public UserPreferenceModel() { }
	
	public UserPreferenceModel(UserPreference preference) {
		this.characterType = new SimpleStringProperty(preference.characterType.toString());
		this.minimum = new SimpleIntegerProperty(preference.minimum);
		this.maximum = new SimpleIntegerProperty(preference.maximum);
	}
	
	private StringProperty characterType;
	private IntegerProperty minimum;
	private IntegerProperty maximum;
	
	public StringProperty getCharacterType() {
		return characterType;
	}
	public void setCharacterType(StringProperty characterType) {
		this.characterType = characterType;
	}
	public IntegerProperty getMinimum() {
		return minimum;
	}
	public void setMinimum(IntegerProperty minimum) {
		this.minimum = minimum;
	}
	public IntegerProperty getMaximum() {
		return maximum;
	}
	public void setMaximum(IntegerProperty maximum) {
		this.maximum = maximum;
	};
	
	public UserPreference dataObject() {
		return new UserPreference(CharacterType.valueOf(this.characterType.get()), this.getMinimum().intValue(), this.getMaximum().intValue());
	}
}
