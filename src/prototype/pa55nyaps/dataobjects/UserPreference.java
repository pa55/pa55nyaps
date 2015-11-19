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

import prototype.pa55nyaps.core.NYAPSEncoder;
import prototype.pa55nyaps.core.NYAPSEncoder.CharacterType;

/**
 * A preference of minimum and maximum constraints (number of characters)
 * for a specific character type.
 * 
 * @author Anirban Basu
 *
 */
public class UserPreference {
	
	public UserPreference(CharacterType characterType, Integer minimum, Integer maximum) {
		super();
		this.characterType = characterType;
		if(minimum>1) {
			this.minimum = minimum;
		}
		else {
			this.minimum = 1;
		}
		this.maximum = maximum;
	}
	public UserPreference(CharacterType characterType, Integer minimum) {
		super();
		this.characterType = characterType;
		if(minimum > 1) {
			this.minimum = minimum;
		}
		else {
			this.minimum = 1;
		}
		this.maximum = -1;
	}
	public NYAPSEncoder.CharacterType characterType;
	public Integer minimum;
	public Integer maximum;
}
