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

import java.awt.image.BufferedImage;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import prototype.pa55nyaps.core.NYAPSCore;
import prototype.pa55nyaps.crypto.AESCryptosystem;
import prototype.pa55nyaps.dataobjects.PasswordDatabaseEntry;
import prototype.pa55nyaps.gui.PA55NYAPSApp;
import prototype.pa55nyaps.gui.model.PasswordDatabaseEntryModel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

public class NYAPSViewController {
	
	@FXML
	private TableView<PasswordDatabaseEntryModel> uictrlDatabaseEntryTable;
	
	@FXML
	private TableColumn<PasswordDatabaseEntryModel, String> uictrlIdColumn;
	
	@FXML
	private TableColumn<PasswordDatabaseEntryModel, Integer> uictrlLengthColumn;
	
	@FXML
	private TableColumn<PasswordDatabaseEntryModel, Integer> uictrlIssueColumn;
	
	@FXML
	private TableColumn<PasswordDatabaseEntryModel, String> uictrlNotesColumn;
	
	
	@FXML
	private PasswordField uictrlMasterSecret;
	
	@FXML
	private CheckBox uictrlProtectQRCode;
	
	@FXML
	private TextField uictrlGeneratedPassword;
	
	@FXML
	private TextField uictrlFilterField;
	
	@FXML
	private ImageView uictrlQRCode;
	
	@FXML
	private Label uictrlGeneratePasswordHeader;
	
	@FXML
	private Button uictrlEditButton;
	
	@FXML
	private Button uictrlDeleteButton;
	
	@FXML
	private Button uictrlGeneratePasswordButton;
	
	@FXML
	private Button uictrlCopyPasswordButton;
	
	private ObservableList<PasswordDatabaseEntryModel> items = FXCollections.observableArrayList();
	
	private PasswordDatabaseEntry dbEntry = null;
	
	private PA55NYAPSApp mainApp;
	
	private QRCodeWriter qrcw = new QRCodeWriter();
	
	
	private Image appLogo;
	private Image appLogoSmall;

    /**
     * The constructor.
     * The constructor is called before the initialize() method.
     */
    public NYAPSViewController() {
    	appLogo = new Image(getClass().getResource("resources/appicon320p_transparent.png").toString());
    	appLogoSmall = new Image(getClass().getResource("resources/appicon60p_transparent.png").toString());
    }

    /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
     */
    @FXML
    private void initialize() {
        // Initialize the person table with the two columns.
    	uictrlIdColumn.setCellValueFactory(cellData -> cellData.getValue().getId());
    	uictrlLengthColumn.setCellValueFactory(cellData -> cellData.getValue().getLength().asObject());
    	uictrlIssueColumn.setCellValueFactory(cellData -> cellData.getValue().getIssue().asObject());
    	uictrlNotesColumn.setCellValueFactory(cellData -> cellData.getValue().getNotes().getExcerpts());
    	
    	uictrlDatabaseEntryTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> loadSelectedPasswordSettingsEntry(newValue));
    	
    	uictrlProtectQRCode.selectedProperty().addListener(
    			(observable, oldValue, newValue) -> generatePassword());
    	
    	uictrlEditButton.setDisable(true);
    	uictrlDeleteButton.setDisable(true);
    	uictrlGeneratePasswordButton.setDisable(true);
    	uictrlCopyPasswordButton.setDisable(true);
    	
    	uictrlMasterSecret.textProperty().addListener((observable, oldValue, newValue) -> {
    		if(newValue!=null) {
    			uictrlGeneratePasswordButton.setDisable(newValue.trim().isEmpty());
    		}
		});
    	
    	uictrlGeneratedPassword.textProperty().addListener((observable, oldValue, newValue) -> {
    		if(newValue!=null) {
    			uictrlCopyPasswordButton.setDisable(newValue.trim().isEmpty());
    		}
		});
    	
    	uictrlQRCode.setPreserveRatio(true);
    	
    	clearGeneratedPassword();
    	
    }
    
    private void loadSelectedPasswordSettingsEntry(PasswordDatabaseEntryModel uiEntry) {
    	dbEntry = null; //clear the current one, if any
		uictrlEditButton.setDisable(true);
    	uictrlDeleteButton.setDisable(true);
    	clearGeneratedPassword();
    	if(uiEntry!=null) {
    		dbEntry = uiEntry.dataObject();
        	uictrlGeneratePasswordHeader.setText("Generate password for " + dbEntry.getId());
        	uictrlEditButton.setDisable(false);
        	uictrlDeleteButton.setDisable(false);
    	}
    }
    
    @FXML
    private void generatePassword() {
    	if(dbEntry!=null) {
    		try {
    			String masterSecret = uictrlMasterSecret.getText();
        		String dynamicHint = dbEntry.getNotes().toString();
    			dynamicHint += dbEntry.getIssue();
    			String password = NYAPSCore.generatePasswordWithAESDRBG(masterSecret, dynamicHint, dbEntry.getLength(), 
    					dbEntry.getCharacterTypes(), dbEntry.getUserDefinedCharacters());
    			if(password!=null) {
    				uictrlGeneratedPassword.setText(password);
    				generateQRCode(masterSecret, password);
    			}
    		}
    		catch(Exception ex) {
    			ex.printStackTrace(System.err);
    			mainApp.showAlert(AlertType.ERROR, "Password generation error", ex.getClass().getName(), ex.getLocalizedMessage());
    		}
    	}
    	else {
    		mainApp.showAlert(AlertType.ERROR, "Password generation error", "Invalid selection",
					"You must select a password setting to generate its corresponding password.");
    	}
    }
    
    @FXML
    private void copyPassword() {
    	String password = uictrlGeneratedPassword.getText();
    	if(password!=null && password.trim().length()>0) {
			ClipboardContent data = new ClipboardContent();
			Clipboard clipboard = Clipboard.getSystemClipboard();
			data.putString(password.trim());
			clipboard.setContent(data);
			mainApp.showAlert(AlertType.INFORMATION, "Copy confirmation", "Confirmation of password copy to clipboard",
					"The generated password has been copied as is to the system clipboard. You can now paste it in other applications.");
		}
    }
    
    private void generateQRCode(String masterSecret, String password) {
    	if(password != null) {
			try {
				String qrData = null;
	    		if(uictrlProtectQRCode.isSelected()) {
	    			//protect
	    			qrData = PA55NYAPSApp.gson.toJson(AESCryptosystem.getInstance().encryptWithHmac(password, masterSecret));
	    		}
	    		else {
	    			//no need to protect
	    			qrData = password;
	    		}
	    		if(qrData != null && qrData.length() > 0) {
					Map<EncodeHintType, Object> qrcHints = new HashMap<EncodeHintType, Object>();
					qrcHints.put(EncodeHintType.CHARACTER_SET, NYAPSCore.CHAR_ENCODING);
					qrcHints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
					MatrixToImageConfig matrixToImageConfig = new MatrixToImageConfig(0xFF000000, 0); //the off colour set to 0 sets transparency to on.
					BitMatrix qrBits = qrcw.encode(qrData, BarcodeFormat.QR_CODE, 320, 320);
					if(qrBits!=null) {
						BufferedImage qrCodeBI = MatrixToImageWriter.toBufferedImage(qrBits, matrixToImageConfig);
						WritableImage wImage = null;
				        if (qrCodeBI != null) {
				        	wImage = new WritableImage(qrCodeBI.getWidth(), qrCodeBI.getHeight());
				            PixelWriter pw = wImage.getPixelWriter();
				            for (int x = 0; x < qrCodeBI.getWidth(); x++) {
				                for (int y = 0; y < qrCodeBI.getHeight(); y++) {
				                    pw.setArgb(x, y, qrCodeBI.getRGB(x, y)); 
				                }
				            }
				        }
				        uictrlQRCode.setImage(wImage);
					}
	    		}
			} catch (Exception ex) {
				ex.printStackTrace(System.err);
    			mainApp.showAlert(AlertType.ERROR, "QR code generation error", ex.getClass().getName(), ex.getLocalizedMessage());
			}
    	}
    	else {
    		uictrlQRCode.setImage(appLogo);
    	}
    }
    
    private void clearGeneratedPassword() {
    	uictrlGeneratedPassword.setText(null);
    	uictrlQRCode.setImage(appLogo);
    	uictrlCopyPasswordButton.setDisable(true);
    	uictrlGeneratePasswordHeader.setText("Generate password");
    }
    
    @FXML
    private void openPasswordDatabase() {
    	uictrlFilterField.clear();
    	mainApp.updateDatabaseWithUIModel(items);
    	
    	items.clear();
    	clearGeneratedPassword();
    	
    	items = mainApp.loadPasswordDatabase();
    	if(items!=null) {
    		FilteredList<PasswordDatabaseEntryModel> filteredItems = new FilteredList<>(items, p -> true);
    		
    		uictrlFilterField.textProperty().addListener((observable, oldValue, newValue) -> {
    			filteredItems.setPredicate(pdbEntry -> {
    				if(newValue == null || newValue.isEmpty()) {
    					return true;
    				}
    				
    				String lowerCaseFilter = newValue.toLowerCase();
    				
    				if(pdbEntry.getNotes().getServiceName().getValue().toLowerCase().contains(lowerCaseFilter)) {
    					return true;
    				}
    				else if(pdbEntry.getNotes().getUserID().getValue().toLowerCase().contains(lowerCaseFilter)) {
    					return true;
    				}
    				else if(pdbEntry.getNotes().getAdditionalInfo().getValue().toLowerCase().contains(lowerCaseFilter)) {
    					return true;
    				}
    				else if(pdbEntry.getNotes().getExcerpts().getValue().toLowerCase().contains(lowerCaseFilter)) {
    					return true;
    				}
    				else if(pdbEntry.getNotes().getServiceLink().getValue().toLowerCase().contains(lowerCaseFilter)) {
    					return true;
    				}
    				else if(pdbEntry.getId().getValue().toLowerCase().contains(lowerCaseFilter)) {
    					return true;
    				}
    				
    				return false;
    			});
    		});
    		
    		SortedList<PasswordDatabaseEntryModel> sortedItems = new SortedList<>(filteredItems);
    		sortedItems.comparatorProperty().bind(uictrlDatabaseEntryTable.comparatorProperty());
    		
    		uictrlDatabaseEntryTable.setItems(sortedItems);
    		mainApp.shouldSave = false;
    	}
    	else {
    		items = FXCollections.observableArrayList();
    	}
	}
    
    @FXML
    private void savePasswordDatabase() {
    	uictrlFilterField.clear();
    	mainApp.updateDatabaseWithUIModel(items);
    	mainApp.saveLoadedDatabase();
    }

	public void setMainApp(PA55NYAPSApp mainApp) {
		this.mainApp = mainApp;
	}
	
	@FXML
	private void deleteSelectedPasswordEntry() {
		if(uictrlDatabaseEntryTable.getSelectionModel().getSelectedIndex() >= 0) {
			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setTitle("Delete confirmation");
			alert.setHeaderText("Delete the selected password settings entry?");
			alert.setContentText("Are you sure you want to delete the password settings entry '" + dbEntry.getId() + "'? This action cannot be undone.");

			Optional<ButtonType> result = alert.showAndWait();
			if (result!=null && result.get() == ButtonType.OK){
				items.remove(uictrlDatabaseEntryTable.getSelectionModel().getSelectedItem());
				uictrlDatabaseEntryTable.getSelectionModel().clearSelection();
				dbEntry = null;
				mainApp.shouldSave = true;
			}
		}
	}
	
	@FXML
	private void editSelectedPasswordEntry() {
		if(uictrlDatabaseEntryTable.getSelectionModel().getSelectedIndex() >= 0) {
			PasswordDatabaseEntry editedEntry = mainApp.showEditEntryDialog(dbEntry, "Edit " + dbEntry.getId());
			if(editedEntry!=null) {
				items.remove(uictrlDatabaseEntryTable.getSelectionModel().getSelectedItem());
				items.add(new PasswordDatabaseEntryModel(editedEntry));
				uictrlDatabaseEntryTable.getSelectionModel().clearSelection();
				dbEntry = null;
				mainApp.shouldSave = true;
			}
		}
	}
	
	@FXML
	private void newPasswordEntry() {		
		dbEntry = new PasswordDatabaseEntry("new entry " + Calendar.getInstance().getTimeInMillis());
		PasswordDatabaseEntry editedEntry = mainApp.showEditEntryDialog(dbEntry, "Edit " + dbEntry.getId());
		if(editedEntry!=null) {
			items.add(new PasswordDatabaseEntryModel(editedEntry));
			uictrlDatabaseEntryTable.getSelectionModel().clearSelection();
			dbEntry = null;
			mainApp.shouldSave = true;
		}
	}
	
	@FXML
	private void showAbout() {
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setGraphic(new ImageView(this.appLogoSmall));
		alert.setTitle("About PA55 NYAPS");
		alert.setHeaderText("PA55 NYAPS (Not Yet Another Password Store) 1.1");
		
		GridPane content = new GridPane();
		content.setMaxWidth(400);
		
		content.add(new Label("PA55 NYAPS lets you securely store settings using which complex passwords can be generated."), 0, 0);
		
		content.add(new Label("For detailed help or for reporting bugs/issues, please see the open-source project website."), 0, 1);
		Hyperlink projectLink = new Hyperlink("https://github.com/pa55/pa55nyaps");
		projectLink.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				mainApp.getHostServices().showDocument(projectLink.getText());
			}});
		content.add(projectLink, 0, 2);
		
		
		alert.getDialogPane().setContent(content);

		String licenseText = "LICENSE: PA55 NYAPS Java Reference Implementation\n\nCopyright 2015-2016 Anirban Basu.\nWith portions copyright 2015, Juan Camilo Corena.\n\nLicensed under the Apache License, Version 2.0 (the \"License\"); you may not use this file except in compliance with the License. You may obtain a copy of the License at\n\nhttp://www.apache.org/licenses/LICENSE-2.0\n\nUnless required by applicable law or agreed to in writing, software distributed under the License is distributed on an \"AS IS\" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.\n\n\nAPPENDIX A - LICENSE: Bouncy Castle\n\nCopyright (c) 2000 - 2015 The Legion of the Bouncy Castle Inc. (http://www.bouncycastle.org)\n\nPermission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the \"Software\"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:\n\nThe above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.\n\nTHE SOFTWARE IS PROVIDED \"AS IS\", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.\n\n\nAPPENDIX B - OTHER SOFTWARE\n\nPA55 NYAPS makes use of Google Gson (https://github.com/google/gson) and Zxing (https://github.com/zxing/zxing) open-source projects. Please refer to the individual project websites for further information about their specific licenses.";
		

		TextArea licenseTextArea = new TextArea(licenseText);
		licenseTextArea.setEditable(false);
		licenseTextArea.setWrapText(true);

		licenseTextArea.setMaxWidth(Double.MAX_VALUE);
		licenseTextArea.setMaxHeight(Double.MAX_VALUE);
		GridPane.setVgrow(licenseTextArea, Priority.ALWAYS);
		GridPane.setHgrow(licenseTextArea, Priority.ALWAYS);

		GridPane expContent = new GridPane();
		expContent.setMaxWidth(400);
		expContent.add(new Label("Software license"), 0, 0);
		expContent.add(licenseTextArea, 0, 1);

		alert.getDialogPane().setExpandableContent(expContent);
		alert.getDialogPane().setExpanded(true);

		alert.showAndWait();
	}

	public ObservableList<PasswordDatabaseEntryModel> getItems() {
		uictrlFilterField.clear();
		return items;
	}

}
