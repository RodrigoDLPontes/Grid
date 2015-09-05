package grid;

import edu.berkeley.boinc.rpc.Project;
import insidefx.undecorator.Undecorator;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ResourceBundle;

public class NewProjectRegistrationPrompt {

	static Stage stage;
	static Stage mainStage;
	static ArrayList<String> usedProjects;

	public NewProjectRegistrationPrompt(Stage mainStage, ArrayList<String> usedProjects) {
		this.usedProjects = usedProjects;
		this.mainStage = mainStage;
		try {
			stage = new Stage();
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getClass().getResource("new_project_password_prompt.fxml"));
			loader.setController(new Controller());
			Parent root = loader.load();
			Undecorator frame = new Undecorator(stage, (Region)root);
			frame.getStylesheets().add("skin/undecorator.css");
			frame.getStylesheets().add("grid/grid_undecorator.css");
			Scene scene = new Scene(frame, 350, 200);
			scene.getStylesheets().add(getClass().getResource("application.css").toString());
			scene.setFill(Color.TRANSPARENT);
			stage.setTitle("Enter Password");
			stage.setScene(scene);
			stage.initStyle(StageStyle.TRANSPARENT);
			stage.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static class Controller implements Initializable {

		@FXML
		private PasswordField passwordField;
		@FXML
		private Button enterButton;

		@Override
		public void initialize(URL location, ResourceBundle resources) {
			enterButton.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					try {
						File userInfo = new File("C:\\ProgramData\\Grid\\userInfo.cfg");
						BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(userInfo)));
						String username = bufferedReader.readLine();
						String email = bufferedReader.readLine();
						if(PasswordEncryption.authenticate(passwordField.getText())) {
							new ProjectRegistration(username, email, passwordField.getText(), false, usedProjects);
							stage.close();
							mainStage.close();
						} else {
							Alert alert = new Alert(Alert.AlertType.WARNING);
							alert.setContentText("Password incorrect!");
							alert.showAndWait();
						}
					} catch(FileNotFoundException e) {
						e.printStackTrace();
					} catch(IOException e) {
						e.printStackTrace();
					} catch(InvalidKeySpecException e) {
						e.printStackTrace();
					} catch(NoSuchAlgorithmException e) {
						e.printStackTrace();
					}
				}
			});
		}
	}
}
