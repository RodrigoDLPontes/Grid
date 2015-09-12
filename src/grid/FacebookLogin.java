package grid;

import facebook4j.*;
import facebook4j.auth.AccessToken;
import insidefx.undecorator.Undecorator;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import javax.crypto.NoSuchPaddingException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class FacebookLogin {

	Stage stage;

	public FacebookLogin() {
		try {
			stage = new Stage();
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getClass().getResource("facebook_login.fxml"));
			loader.setController(new Controller());
			Parent root = loader.load();
			Undecorator frame = new Undecorator(stage, (Region) root);
			frame.getStylesheets().add("skin/undecorator.css");
			frame.getStylesheets().add("grid/grid_undecorator.css");
			Scene scene = new Scene(frame, 1087.5, 725);
			scene.getStylesheets().add(getClass().getResource("application.css").toString());
			scene.setFill(Color.TRANSPARENT);
			stage.setTitle("Facebook Login");
			stage.setScene(scene);
			stage.initStyle(StageStyle.TRANSPARENT);
			stage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	class Controller implements Initializable {

		@FXML
		private WebView webView;

		@Override
		public void initialize(URL location, ResourceBundle resources) {
			WebEngine webEngine = webView.getEngine();
			webEngine.load("https://facebook.com/dialog/oauth?client_id=858613480923574&redirect_uri=https://www.facebook.com/connect/login_success.html&response_type=token&scope=email");
			webEngine.getLoadWorker().stateProperty().addListener(new ChangeListener<Worker.State>() {
				@Override
				public void changed(ObservableValue<? extends Worker.State> observable, Worker.State oldValue, Worker.State newValue) {
					if(newValue.equals(Worker.State.SUCCEEDED)) {
						if(webEngine.getLocation().indexOf("https://www.facebook.com/connect/login_success.html") == 0) {
							stage.close();
							String url = webEngine.getLocation();
							String accessToken = url.substring(url.indexOf("#access_token=") + 14, url.indexOf("&expires_in"));
							if(Grid.initiateBoincRpc()) {
								try {
									Facebook facebook = new FacebookFactory().getInstance();
									facebook.setOAuthAppId("", "");
									facebook.setOAuthAccessToken(new AccessToken(accessToken));
									String username = facebook.getName();
									String email = facebook.getUser(facebook.getId(), new Reading().fields("email")).getEmail();
									File userInfo = new File("C:\\ProgramData\\Grid\\userInfo.cfg");
									userInfo.getParentFile().mkdirs();
									userInfo.createNewFile();
									PrintWriter printWriter = new PrintWriter(userInfo);
									printWriter.println("true");
									printWriter.println(username);
									printWriter.println(email);
									byte[] salt = PasswordEncryption.generateSalt();
									byte[] passwordBytes = PasswordEncryption.getEncryptedPassword(username, salt);
									String password = PasswordEncryption.toHex(passwordBytes);
									printWriter.println(password);
									printWriter.flush();
									FileOutputStream fileOutputStream = new FileOutputStream(userInfo, true);
									fileOutputStream.write(salt);
									fileOutputStream.flush();
									fileOutputStream.close();
									File accessTokenFile = new File("C:\\ProgramData\\Grid\\token.cfg");
									accessTokenFile.getParentFile().mkdirs();
									accessTokenFile.createNewFile();
									printWriter = new PrintWriter(accessTokenFile);
									printWriter.println(accessToken);
									printWriter.flush();
									printWriter.close();
									new ProjectRegistration(username, email, password, true, null);
								} catch(IOException e) {
									e.printStackTrace();
								} catch(NoSuchAlgorithmException e) {
									e.printStackTrace();
								} catch(InvalidKeySpecException e) {
									e.printStackTrace();
								} catch(FacebookException e) {
									e.printStackTrace();
								}
							}
						}
					}
				}
			});
		}
	}
}
