package application;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import edu.berkeley.boinc.rpc.AccountIn;
import edu.berkeley.boinc.utils.Logging;

public class InitialProjectRegistration {

	private static Stage stage;
	private static String username, email, password;
	private static ArrayList<AvailableProject> availableProjects = new ArrayList<AvailableProject>();
	private static ArrayList<AvailableProject> selectedProjects = new ArrayList<AvailableProject>();

	public InitialProjectRegistration(String username, String email, String password) {
		InitialProjectRegistration.username = username;
		InitialProjectRegistration.email = email;
		InitialProjectRegistration.password = password;
		try {
			stage = new Stage();
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getClass().getResource("initial_project_registration.fxml"));
			loader.setController(new Controller());
			Parent root = loader.load();
			Scene scene = new Scene(root, 525, 350);
			scene.getStylesheets().add(getClass().getResource("application.css").toString());
			stage.setScene(scene);
			stage.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static class Controller implements Initializable {
		
		@FXML private GridPane projectsPane;
		
		@Override
		public void initialize(URL location, ResourceBundle resources) {
			if (Logging.INFO) 
				System.out.println("GRID: Reading projects file...");
			BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream("projectsInfo.txt")));
			try {
				String readerLine = reader.readLine();
				while (readerLine != null) {
					AvailableProject availableProject = new AvailableProject();
					availableProject.projectName = readerLine;
					readerLine = reader.readLine();
					availableProject.category = readerLine;
					availableProject.accountIn = new AccountIn(reader.readLine(), email, username, false, password, null);
					readerLine = reader.readLine();
					availableProject.description = readerLine;
					availableProjects.add(availableProject);
					readerLine = reader.readLine();
				}
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (Logging.INFO) 
				System.out.println("GRID: Read projects file\nSetting up project panels...");
			int yRow = 0;
			for(AvailableProject availableProject : availableProjects) {
				//TODO: Change background colors
				GridPane projectPane = new GridPane();
				projectPane.setBackground(new Background(new BackgroundFill(Color.DARKGREY, CornerRadii.EMPTY, Insets.EMPTY)));
				ColumnConstraints column1 = new ColumnConstraints();
				column1.setPercentWidth(50);
				ColumnConstraints column2 = new ColumnConstraints();
				column2.setPercentWidth(50);
				projectPane.getColumnConstraints().addAll(column1, column2);
				projectPane.setPadding(new Insets(5));
				projectPane.setOnMouseClicked(new MouseEventHandler());
				Label projectName = new Label(availableProject.projectName);
				projectName.setFont(new Font(18));
				GridPane.setValignment(projectName, VPos.CENTER);
				projectPane.add(projectName, 0, 0);
				TextFlow description = new TextFlow(new Text(availableProject.description));
				description.setMaxSize(200, 100);
				GridPane.setValignment(description, VPos.CENTER);
				projectPane.add(description, 0, 1);
				Label category = new Label(availableProject.category);
				category.setFont(new Font(18));
				GridPane.setValignment(category, VPos.CENTER);
				GridPane.setHalignment(category, HPos.RIGHT);
				projectPane.add(category, 1, 0, 1, 2);
				projectsPane.add(projectPane, 0, yRow);
				yRow++;
				Pane pane = new Pane();
				pane.setMinHeight(7.5);
				projectsPane.addRow(yRow, pane);
				yRow++;
			}
		}
		
		@FXML
		public void proceedButtonClicked(ActionEvent event) {
			new InitialProjectRegistrationProgress(selectedProjects);
			stage.close();
		}
	}
	
	public static class AvailableProject {
		public String projectName;
		public String description;
		public String category;
		public AccountIn accountIn;
	}
	
	private static class MouseEventHandler implements EventHandler<MouseEvent> {
		
		@Override
		public void handle(MouseEvent event) {
			if(event.getClickCount() == 1 || event.getClickCount() == 2) {
				if(((GridPane)event.getSource()).getBackground().getFills().get(0).getFill().equals(Color.DARKGRAY)) {
					((GridPane)event.getSource()).setBackground(new Background(new BackgroundFill(Color.LIGHTGRAY, CornerRadii.EMPTY, Insets.EMPTY)));
					selectedProjects.add(availableProjects.get(GridPane.getRowIndex((GridPane)event.getSource()) / 2));
				} else {
					((GridPane)event.getSource()).setBackground(new Background(new BackgroundFill(Color.DARKGRAY, CornerRadii.EMPTY, Insets.EMPTY)));
					selectedProjects.remove(availableProjects.get(GridPane.getRowIndex((GridPane)event.getSource()) / 2));
				}
			}
		}	
	}
}
