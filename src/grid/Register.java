package grid;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ResourceBundle;

import insidefx.undecorator.Undecorator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class Register {

	private static Stage stage;

	public Register() {
		try {
			stage = new Stage();
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getClass().getResource("register.fxml"));
			loader.setController(new Controller());
			Parent root = loader.load();
			Undecorator frame = new Undecorator(stage, (Region)root);
			frame.getStylesheets().add("skin/undecorator.css");
			frame.getStylesheets().add("grid/grid_undecorator.css");
			Scene scene = new Scene(frame, 525, 350);
			scene.getStylesheets().add(getClass().getResource("application.css").toString());
			scene.setFill(Color.TRANSPARENT);
			stage.setTitle("Register");
			stage.setScene(scene);
			stage.initStyle(StageStyle.TRANSPARENT);
			stage.show();
			root.requestFocus();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static class Controller implements Initializable {

		@FXML
		private TextField usernameTextField;
		@FXML
		private TextField emailTextField;
		@FXML
		private PasswordField passwordField;
		@FXML
		private PasswordField confirmPasswordField;
		@FXML
		private CheckBox rememberMeCheckBox;

		@Override
		public void initialize(URL location, ResourceBundle resources) {
		}

		@FXML
		public void registerButtonClicked(ActionEvent event) {
			//TODO: Check if other fields are valid
			if (passwordField.getText().equals(confirmPasswordField.getText())) {
				if(Grid.initiateBoincRpc()) {
					try {
						File userInfo = new File("C:\\ProgramData\\Grid\\userInfo.cfg");
						userInfo.getParentFile().mkdirs();
						userInfo.createNewFile();
						PrintWriter printWriter = new PrintWriter(userInfo);
						printWriter.println(usernameTextField.getText());
						printWriter.println(emailTextField.getText());
						printWriter.flush();
						printWriter.close();
						FileOutputStream fileOutputStream = new FileOutputStream(userInfo, true);
						byte[] salt = PasswordEncryption.generateSalt();
						fileOutputStream.write(salt);
						fileOutputStream.write(PasswordEncryption.getEncryptedPassword(passwordField.getText(), salt));
						fileOutputStream.flush();
						fileOutputStream.close();
						new ProjectRegistration(usernameTextField.getText(), emailTextField.getText(), passwordField.getText(), true, null);
					} catch(IOException e) {
						e.printStackTrace();
					} catch(NoSuchAlgorithmException e) {
						e.printStackTrace();
					} catch(InvalidKeySpecException e) {
						e.printStackTrace();
					}
				}
				Register.stage.close();
			} else {
				Alert alert = new Alert(Alert.AlertType.WARNING);
				alert.setContentText("\"Password\" and \"Confirm Password\" do no match!");
				alert.showAndWait();
			}
		}
	}
}