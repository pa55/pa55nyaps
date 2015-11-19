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

package prototype.pa55nyaps.core;

import prototype.pa55nyaps.core.NYAPSEncoder.CharacterType;

/**
 * A tuple consisting of a character type and a corresponding
 * count.
 * 
 * @author Anirban Basu
 *
 */

public class CharacterTypeCount {
	public CharacterTypeCount(CharacterType characterType, int count) {
		super();
		this.characterType = characterType;
		this.count = count;
	}
	public NYAPSEncoder.CharacterType characterType;
	public int count;
}
