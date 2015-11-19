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

import prototype.pa55nyaps.dataobjects.ExtendedNotes;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ExtendedNotesModel {
	
	public ExtendedNotesModel(String serviceName,
			String serviceLink, String userID,
			String additionalInfo) {
		this.serviceName = new SimpleStringProperty(serviceName);
		this.serviceLink = new SimpleStringProperty(serviceLink);
		this.userID = new SimpleStringProperty(userID);
		this.additionalInfo = new SimpleStringProperty(additionalInfo);
	}
	
	public ExtendedNotesModel(String serviceName) {
		this.serviceName = new SimpleStringProperty(serviceName);
	}
	
	public ExtendedNotesModel() { }
	
	public ExtendedNotesModel(ExtendedNotes notes) {
		this.serviceName = new SimpleStringProperty(notes.getServiceName());
		this.serviceLink = new SimpleStringProperty(notes.getServiceLink());
		this.userID = new SimpleStringProperty(notes.getUserID());
		this.additionalInfo = new SimpleStringProperty(notes.getAdditionalInfo());
	}
	
	private StringProperty serviceName;
	private StringProperty serviceLink;
	private StringProperty userID;
	private StringProperty additionalInfo;
	public StringProperty getServiceName() {
		return serviceName;
	}
	public void setServiceName(StringProperty serviceName) {
		this.serviceName = serviceName;
	}
	public StringProperty getServiceLink() {
		return serviceLink;
	}
	public void setServiceLink(StringProperty serviceLink) {
		this.serviceLink = serviceLink;
	}
	public StringProperty getUserID() {
		return userID;
	}
	public void setUserID(StringProperty userID) {
		this.userID = userID;
	}
	public StringProperty getAdditionalInfo() {
		return additionalInfo;
	}
	public void setAdditionalInfo(StringProperty additionalInfo) {
		this.additionalInfo = additionalInfo;
	}
	
	public StringProperty getExcerpts() {
		return new SimpleStringProperty(this.serviceName.get() + " " + this.userID.get());
	}
	
	public ExtendedNotes dataObject() {
		return new ExtendedNotes(serviceName.get(), serviceLink.get(), userID.get(), additionalInfo.get());
	}
}
