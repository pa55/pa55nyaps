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

package prototype.pa55nyaps.gui;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.util.Iterator;
import java.util.Optional;
import java.util.Scanner;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import prototype.pa55nyaps.crypto.AESCryptosystem;
import prototype.pa55nyaps.dataobjects.Ciphertext;
import prototype.pa55nyaps.dataobjects.PasswordDatabase;
import prototype.pa55nyaps.dataobjects.PasswordDatabaseEntry;
import prototype.pa55nyaps.gui.model.PasswordDatabaseEntryModel;
import prototype.pa55nyaps.gui.view.NYAPSViewController;
import prototype.pa55nyaps.gui.view.PasswordDatabaseEntryController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;


public class PA55NYAPSApp extends Application {
	
	private static final String APP_NAME = "pa55 nyaps";
	
	private Stage primaryStage;
    private BorderPane rootLayout;
    
    private File fileSelectedPasswordDb = null;
    private PasswordDatabase passwordDatabase = null;
    private String openDatabasePassword = null;
    
    public boolean shouldSave = false;
    
    public static Gson gson = new GsonBuilder()
	.enableComplexMapKeySerialization()
	.serializeNulls()
	.setDateFormat(DateFormat.LONG)
	.disableHtmlEscaping()
	.setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
	.setPrettyPrinting()
	.setVersion(1.0)
	.create();

	@Override
	public void start(Stage primaryStage) {
		this.primaryStage = primaryStage;
        this.primaryStage.setTitle(APP_NAME);
        this.primaryStage.setResizable(false);

        initRootLayout();

        showNYAPSWindow();
	}
	
	public void initRootLayout() {
		try {
            // Load root layout from fxml file.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(PA55NYAPSApp.class.getResource("view/RootLayout.fxml"));
            rootLayout = (BorderPane) loader.load();

            // Show the scene containing the root layout.
            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
	}
	
	public void showNYAPSWindow() {
		try {
            // Load person overview.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(PA55NYAPSApp.class.getResource("view/NYAPSView.fxml"));
            AnchorPane nyapsWindow = (AnchorPane) loader.load();

            rootLayout.setCenter(nyapsWindow);
            
            NYAPSViewController controller = loader.getController();
            controller.setMainApp(this);
            
            passwordDatabase = new PasswordDatabase();
            
            primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                public void handle(WindowEvent we) {
                	updateDatabaseWithUIModel(controller.getUictrlDatabaseEntryTable().getItems());
                	saveAndClearLoadedDatabase();
                }
            });
            
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
	}
	
	public PasswordDatabaseEntry showEditEntryDialog(PasswordDatabaseEntry dbEntry, String title) {
		try {
	        // Load the fxml file and create a new stage for the popup dialog.
	        FXMLLoader loader = new FXMLLoader();
	        loader.setLocation(PA55NYAPSApp.class.getResource("view/PasswordDatabaseEntry.fxml"));
	        AnchorPane page = (AnchorPane) loader.load();

	        // Create the dialog Stage.
	        Stage dialogStage = new Stage();
	        dialogStage.setTitle(title);
	        dialogStage.initModality(Modality.APPLICATION_MODAL);
	        dialogStage.initOwner(primaryStage);
	        Scene scene = new Scene(page);
	        dialogStage.setScene(scene);

	        // Set the person into the controller.
	        PasswordDatabaseEntryController controller = loader.getController();
	        controller.setDialogStage(dialogStage);
	        controller.setMainApp(this);
	        controller.setPasswordDbEntry(new PasswordDatabaseEntryModel(dbEntry));

	        // Show the dialog and wait until the user closes it
	        dialogStage.showAndWait();

	        return (controller.getPasswordDbEntry() == null? null : controller.getPasswordDbEntry().dataObject());
	    } catch (IOException ex) {
	    	ex.printStackTrace(System.err);
			showAlert(AlertType.ERROR, "Edit dialog error", ex.getClass().getName(), ex.getLocalizedMessage());
			return null;
	    }
	}
	
	private void saveAndClearLoadedDatabase() {
		if(shouldSave) {
			//save silently but existing file name is required
			if(fileSelectedPasswordDb!=null) {
				saveDatabaseToFile(passwordDatabase, fileSelectedPasswordDb, openDatabasePassword, true);
			}
			else {
				saveLoadedDatabase();
			}
			fileSelectedPasswordDb = null;
			passwordDatabase = null;
			openDatabasePassword = null;
			this.primaryStage.setTitle(APP_NAME);
		}
	}
	
	public void saveLoadedDatabase() {
		if(passwordDatabase!=null && passwordDatabase.getDatabase().size() > 0) { //no point of trying to save an empty database
			FileChooser fileChooser = new FileChooser();
			fileChooser.getExtensionFilters().add(new ExtensionFilter("PA55 NYAPS file (*.pa55)", "*.pa55"));
			fileChooser.setTitle("Save to a PA55 NYAPS file");
			if(fileSelectedPasswordDb!=null) {
				if(fileSelectedPasswordDb.getParentFile()!=null) {
					fileChooser.setInitialDirectory(fileSelectedPasswordDb.getParentFile());
				}
				fileChooser.setInitialFileName(fileSelectedPasswordDb.getName());
			}
			File fileToSaveTo = fileChooser.showSaveDialog(primaryStage);
			if(fileToSaveTo!=null) {
				fileSelectedPasswordDb = fileToSaveTo;
				String password = obtainPasswordFromUser("Change file write password", 
						"Saving " + fileSelectedPasswordDb.getName(), null,
						"Enter password", openDatabasePassword, "OK", "Cancel");
				if(password!=null) {
					openDatabasePassword = password;
					saveDatabaseToFile(passwordDatabase, fileSelectedPasswordDb, openDatabasePassword, false);
				}
			}
		}
	}
	
	public void updateDatabaseWithUIModel(ObservableList<PasswordDatabaseEntryModel> data) {
		if(passwordDatabase != null && data!=null) {
			passwordDatabase.getDatabase().clear();
			for(PasswordDatabaseEntryModel uiEntry : data) {
				PasswordDatabaseEntry dbEntry = uiEntry.dataObject();
				passwordDatabase.getDatabase().put(dbEntry.getId(), dbEntry);
			}
		}
	}
	
	public ObservableList<PasswordDatabaseEntryModel> loadPasswordDatabase() {
		saveAndClearLoadedDatabase();
		FileChooser fileChooser = new FileChooser();
		fileChooser.getExtensionFilters().add(new ExtensionFilter("PA55 NYAPS file (*.pa55)", "*.pa55"));
		fileChooser.setTitle("Open a PA55 NYAPS file");
		fileSelectedPasswordDb = fileChooser.showOpenDialog(primaryStage);
		if(fileSelectedPasswordDb==null) {
			return null;
		}
		openDatabasePassword = obtainPasswordFromUser("File read password", 
				"Opening " + fileSelectedPasswordDb.getName(), null,
				"Enter password", null, "OK", "Cancel");
		if(openDatabasePassword==null) {
			return null;
		}
		if(!readDatabaseFromFile(fileSelectedPasswordDb, openDatabasePassword)) {
			fileSelectedPasswordDb = null;
			return null;
		}
		ObservableList<PasswordDatabaseEntryModel> data = FXCollections.observableArrayList();
		Iterator<PasswordDatabaseEntry> dbEntryIterator = passwordDatabase.getDatabase().values().iterator();
		while(dbEntryIterator.hasNext()) {
			PasswordDatabaseEntry dbEntry = dbEntryIterator.next();
			data.add(new PasswordDatabaseEntryModel(dbEntry));
		}
		return data;
	}
	
	private String obtainPasswordFromUser(String title, String header, String placeholder, 
			String labelText, String defaultValue, String okButtonTitle, String cancelButtonTitle) {
		// Create the custom dialog.
		Dialog<String> dialog = new Dialog<>();
		dialog.setTitle(title);
		dialog.setHeaderText(header);

		// Set the button types.
		ButtonType okButtonType = new ButtonType(okButtonTitle, ButtonData.OK_DONE);
		dialog.getDialogPane().getButtonTypes().addAll(okButtonType, new ButtonType(cancelButtonTitle, ButtonData.CANCEL_CLOSE));

		// Create the username and password labels and fields.
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(10, 10, 10, 10));

		PasswordField password = new PasswordField();
		if(placeholder!=null) {
			password.setPromptText(placeholder);
		}
		

		grid.add(new Label(labelText), 0, 0);
		grid.add(password, 1, 0);

		Node okButton = dialog.getDialogPane().lookupButton(okButtonType);
		if(defaultValue!=null) {
			password.setText(defaultValue);
		}
		else {
			okButton.setDisable(true);
		}

		password.textProperty().addListener((observable, oldValue, newValue) -> {
			if(newValue!=null) {
				okButton.setDisable(newValue.trim().isEmpty());
			}
		});

		dialog.getDialogPane().setContent(grid);

		Platform.runLater(() -> password.requestFocus());

		dialog.setResultConverter(dialogButton -> {
		    if (dialogButton == okButtonType) {
		        return password.getText();
		    }
		    return null;
		});

		Optional<String> result = dialog.showAndWait();
		
		if(result.isPresent()) {
			return result.get();
		}
		return null;
	}
	
	private boolean readDatabaseFromFile(File file, String password) {
		boolean retVal = false;
		Scanner is;
		StringBuilder jsonct = new StringBuilder();
		try {
			is = new Scanner(file);
			while(is.hasNextLine()) {
				jsonct.append(is.nextLine());
			}
			is.close();
			Type ctType = new TypeToken<Ciphertext>(){}.getType();
			Ciphertext ciphertext = gson.fromJson(jsonct.toString(), ctType);
			String json = AESCryptosystem.getInstance().decryptWithHmac(ciphertext, password);
			Type dbType = new TypeToken<PasswordDatabase>(){}.getType();
			passwordDatabase = gson.fromJson(json.toString(), dbType);
			this.primaryStage.setTitle(APP_NAME + " - " + file.getName());
			retVal = true;
		} catch (Exception ex) {
			ex.printStackTrace(System.err);
			showAlert(AlertType.ERROR, "File save error", ex.getClass().getName(), ex.getLocalizedMessage());
		}
		return retVal;
	}
	
	public void saveDatabaseToFile(PasswordDatabase database, File file, String password, boolean silent) {
		PrintStream pos;
		try {
			pos = new PrintStream(file.getAbsolutePath());
			String json = gson.toJson(database);
			//System.out.println(json);
			Ciphertext ciphertext = AESCryptosystem.getInstance().encryptWithHmac(json, new String(password));
			String jsonct = gson.toJson(ciphertext);
			pos.print(jsonct);
			pos.flush();
			pos.close();
			this.primaryStage.setTitle(APP_NAME + " - " + file.getName());
		} catch (Exception ex) {
			ex.printStackTrace(System.err);
			if(!silent) {
				showAlert(AlertType.ERROR, "File save error", ex.getClass().getName(), ex.getLocalizedMessage());
			}
		}
	}
	
	public void showAlert(AlertType type, String title, String header, String message) {
		Alert alert = new Alert(type);
		alert.setTitle(title);
		alert.setHeaderText(header);
		alert.setContentText(message);
		alert.showAndWait();
	}

	public static void main(String[] args) {
		launch(args);
	}
}
