package application;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import javafx.fxml.Initializable;


public class DebugConsole {
	
	private static TextArea console;
	
	public DebugConsole() {
		try {
			Stage stage = new Stage();
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getClass().getResource("debug_console.fxml"));
			loader.setController(new Controller());
			Parent root = loader.load();
			Scene scene = new Scene(root, 375, 250);
			stage.setScene(scene);
			stage.setX(50);
			stage.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	class Controller implements Initializable {
		
		@FXML private TextArea console;
		
		@Override
		public void initialize(URL location, ResourceBundle resources) {
			DebugConsole.console = console;
		}
	}
	
	static class DebugConsolePrintStream extends OutputStream {
		@Override
		public void write(int b) throws IOException {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					console.appendText(String.valueOf((char)b));
				}
			});
		}
	}
}
