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

package prototype.pa55nyaps.gui.view;

import prototype.pa55nyaps.core.NYAPSEncoder;
import prototype.pa55nyaps.dataobjects.UserPreference;
import prototype.pa55nyaps.gui.PA55NYAPSApp;
import prototype.pa55nyaps.gui.model.PasswordDatabaseEntryModel;
import prototype.pa55nyaps.gui.model.UserPreferenceModel;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class PasswordDatabaseEntryController {
	
	
	@FXML
	private TextField uictrlId;
	
	@FXML
	private Spinner<Integer> uictrlLength;
	
	@FXML
	private Spinner<Integer> uictrlIssue;
	
	@FXML
	private TextField uictrlServiceName;
	
	@FXML
	private TextField uictrlServiceLink;
	
	@FXML
	private TextField uictrlUserId;
	
	@FXML
	private TextField uictrlAdditionalInfo;
	
	@FXML
	private CheckBox uictrlIncludeBrackets;
	
	@FXML
	private CheckBox uictrlIncludeDigits;
	
	@FXML
	private CheckBox uictrlIncludeLowercase;
	
	@FXML
	private CheckBox uictrlIncludeSpecial;
	
	@FXML
	private CheckBox uictrlIncludeUppercase;
	
	@FXML
	private Button uictrlOKButton;
	
	@FXML
	private Button uictrlCancelButton;
	
	private Stage dialogStage;
	
	private PA55NYAPSApp mainApp;
	
	private PasswordDatabaseEntryModel passwordDbEntry;
	
	/**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
     */
    @FXML
    private void initialize() {
    	uictrlLength.setValueFactory(
    			new SpinnerValueFactory.IntegerSpinnerValueFactory(
    					NYAPSEncoder.MIN_LENGTH, NYAPSEncoder.MAX_LENGTH, NYAPSEncoder.DEFAULT_LENGTH, 1));
    	uictrlIssue.setValueFactory(
    			new SpinnerValueFactory.IntegerSpinnerValueFactory(
    					NYAPSEncoder.MIN_ISSUE, NYAPSEncoder.MAX_ISSUE, 1, 1));
    }

    private void updateUIWithModel() {
    	if(passwordDbEntry!=null) {
	    	uictrlId.setText(passwordDbEntry.getId().get());
	
	    	uictrlLength.getValueFactory().setValue(passwordDbEntry.getLength().intValue());
	    	uictrlIssue.getValueFactory().setValue(passwordDbEntry.getIssue().intValue());
	    	
	    	uictrlServiceName.setText(passwordDbEntry.getNotes().getServiceName().get());
	    	if(passwordDbEntry.getNotes().getServiceLink()!=null) {
	    		uictrlServiceLink.setText(passwordDbEntry.getNotes().getServiceLink().get());
	    	}
	    	if(passwordDbEntry.getNotes().getUserID()!=null) {
	    		uictrlUserId.setText(passwordDbEntry.getNotes().getUserID().get());
	    	}
	    	if(passwordDbEntry.getNotes().getAdditionalInfo()!=null) {
	    		uictrlAdditionalInfo.setText(passwordDbEntry.getNotes().getAdditionalInfo().get());
	    	}
	    	
	    	for(int i=0; i<passwordDbEntry.getCharacterTypes().size(); i++) {
	    		UserPreference up = passwordDbEntry.getCharacterTypes().get(i).dataObject();
	    		switch(up.characterType) {
	    		case brackets:
	    			uictrlIncludeBrackets.setSelected(up.minimum > 0);
	    			break;
	    		case digits:
	    			uictrlIncludeDigits.setSelected(up.minimum > 0);
	    			break;
	    		case lowercase:
	    			uictrlIncludeLowercase.setSelected(up.minimum > 0);
	    			break;
	    		case special:
	    			uictrlIncludeSpecial.setSelected(up.minimum > 0);
	    			break;
	    		case uppercase:
	    			uictrlIncludeUppercase.setSelected(up.minimum > 0);
	    			break;
	    		default:
	    			break;
	    		}
	    	}
    	}
    }
    
    private void updateModelWithUI() {
    	if(passwordDbEntry!=null) {
	    	passwordDbEntry.setId(new SimpleStringProperty(uictrlId.getText().trim()));
	    	
	    	passwordDbEntry.setLength(new SimpleIntegerProperty(uictrlLength.getValue()));
	    	
	    	passwordDbEntry.setIssue(new SimpleIntegerProperty(uictrlIssue.getValue()));
	    	
	    	passwordDbEntry.getNotes().setServiceName(new SimpleStringProperty(uictrlServiceName.getText().trim()));
	    	
	    	if(uictrlServiceLink.getText()!=null) {
	    		passwordDbEntry.getNotes().setServiceLink(new SimpleStringProperty(uictrlServiceLink.getText().trim()));
	    	}
	    	else {
	    		passwordDbEntry.getNotes().setServiceLink(new SimpleStringProperty(""));
	    	}
	    	
	    	if(uictrlUserId.getText()!=null) {
	    		passwordDbEntry.getNotes().setUserID(new SimpleStringProperty(uictrlUserId.getText().trim()));
	    	}
	    	else {
	    		passwordDbEntry.getNotes().setUserID(new SimpleStringProperty(""));
	    	}
	    	
	    	if(uictrlAdditionalInfo.getText()!=null) {
	    		passwordDbEntry.getNotes().setAdditionalInfo(new SimpleStringProperty(uictrlAdditionalInfo.getText().trim()));
	    	}
	    	else {
	    		passwordDbEntry.getNotes().setAdditionalInfo(new SimpleStringProperty(""));
	    	}
	    	
	    	passwordDbEntry.getCharacterTypes().clear();
	    	
	    	if(uictrlIncludeBrackets.isSelected())  {
	    		passwordDbEntry.getCharacterTypes().add(new UserPreferenceModel(NYAPSEncoder.CharacterType.brackets, 1));
	    	}
	    	
	    	if(uictrlIncludeDigits.isSelected())  {
	    		passwordDbEntry.getCharacterTypes().add(new UserPreferenceModel(NYAPSEncoder.CharacterType.digits, 1));
	    	}
	    	
	    	if(uictrlIncludeLowercase.isSelected())  {
	    		passwordDbEntry.getCharacterTypes().add(new UserPreferenceModel(NYAPSEncoder.CharacterType.lowercase, 1));
	    	}
	    	
	    	if(uictrlIncludeSpecial.isSelected())  {
	    		passwordDbEntry.getCharacterTypes().add(new UserPreferenceModel(NYAPSEncoder.CharacterType.special, 1));
	    	}
	    	
	    	if(uictrlIncludeUppercase.isSelected())  {
	    		passwordDbEntry.getCharacterTypes().add(new UserPreferenceModel(NYAPSEncoder.CharacterType.uppercase, 1));
	    	}
    	}
    }

	public PasswordDatabaseEntryModel getPasswordDbEntry() {
		updateModelWithUI();
		return passwordDbEntry;
	}

	public void setPasswordDbEntry(PasswordDatabaseEntryModel passwordDbEntry) {
		this.passwordDbEntry = passwordDbEntry;
		updateUIWithModel();
	}


	public void setDialogStage(Stage dialogStage) {
		this.dialogStage = dialogStage;
	}
	
	private String validateInput() {
		String validityMessage = null;
		
		//check the id
		if(uictrlId.getText()==null && !uictrlId.getText().matches("^(?!\\s*$).+")) {
			validityMessage = "Settings ID is invalid: " + uictrlId.getText();
			uictrlId.setText(passwordDbEntry.getId().get());
			return validityMessage;
		}
		
		//check the service name
		if(uictrlServiceName.getText()==null && !uictrlServiceName.getText().matches("^(?!\\s*$).+")) {
			validityMessage = "Service name is invalid: " + uictrlServiceName.getText();
			uictrlServiceName.setText(passwordDbEntry.getNotes().getServiceName().get());
			return validityMessage;
		}
		
		
		//check the service link
		if(uictrlServiceLink.getText()!=null && !uictrlServiceLink.getText().matches("^$|^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]")) {
			validityMessage = "Invalid service link URL: " + uictrlServiceLink.getText();
			uictrlServiceLink.setText(passwordDbEntry.getNotes().getServiceLink().get());
			return validityMessage;
		}
		
		//check the character classes
		int selectedCharacterClasses = 0;
		if(uictrlIncludeBrackets.isSelected()) {
			selectedCharacterClasses++;
		}
		if(uictrlIncludeDigits.isSelected()) {
			selectedCharacterClasses++;
		}
		if(uictrlIncludeLowercase.isSelected()) {
			selectedCharacterClasses++;
		}
		if(uictrlIncludeSpecial.isSelected()) {
			selectedCharacterClasses++;
		}
		if(uictrlIncludeUppercase.isSelected()) {
			selectedCharacterClasses++;
		}
		if(selectedCharacterClasses==0) {
			validityMessage = "At least one character type should be chosen.";
			return validityMessage;
		}
		if(selectedCharacterClasses>uictrlLength.getValue()) {
			validityMessage = "The desired length of the password must be more than or equal to the number of character types that you have chosen.";
			return validityMessage;
		}
		
		return validityMessage;
	}
	
	@FXML
	private void handleOKButton() {
		String validityMessage = validateInput();
		if(validityMessage==null) {
			dialogStage.close();
		}
		else {
			mainApp.showAlert(AlertType.WARNING, "Input validation", "Your input is not valid", validityMessage);
		}
	}
	
	@FXML
	private void handleCancelButton() {
		passwordDbEntry = null;
		dialogStage.close();
	}

	public void setMainApp(PA55NYAPSApp mainApp) {
		this.mainApp = mainApp;
	}

}
