package grid;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import edu.berkeley.boinc.rpc.Project;
import insidefx.undecorator.Undecorator;
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
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import edu.berkeley.boinc.rpc.AccountIn;
import edu.berkeley.boinc.utils.Logging;
import javafx.stage.StageStyle;

public class ProjectRegistration {

	private static Stage stage;
	private static String username, email, password;
	private static boolean firstUse;
	private static ArrayList<AvailableProject> availableProjects = new ArrayList<>();
	private static ArrayList<AvailableProject> selectedProjects = new ArrayList<>();
	private static ArrayList<String> usedProjects = new ArrayList<>();

	public ProjectRegistration(String username, String email, String password, boolean firstUse, ArrayList<String> usedProjects) {
		ProjectRegistration.username = username;
		ProjectRegistration.email = email;
		ProjectRegistration.password = password;
		ProjectRegistration.firstUse = firstUse;
		ProjectRegistration.usedProjects = usedProjects;
		try {
			stage = new Stage();
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getClass().getResource("project_registration.fxml"));
			loader.setController(new Controller());
			Parent root = loader.load();
			Undecorator frame = new Undecorator(stage, (Region) root);
			frame.getStylesheets().add("skin/undecorator.css");
			frame.getStylesheets().add("grid/grid_undecorator.css");
			Scene scene = new Scene(frame, 725, 500);
			scene.getStylesheets().add(getClass().getResource("application.css").toString());
			scene.setFill(Color.TRANSPARENT);
			stage.setScene(scene);
			stage.initStyle(StageStyle.TRANSPARENT);
			stage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	private static class Controller implements Initializable {

		@FXML
		private Label title;
		@FXML
		private Label subtitle;
		@FXML
		private GridPane projectsPane;
		
		@Override
		public void initialize(URL location, ResourceBundle resources) {
			if(firstUse) {
				title.setText("Let's begin...");
				subtitle.setText("Please, select a few projects you would like to contribute to:");
			} else {
				title.setText("Add other projects");
				subtitle.setText("Please, choose the other projects you would like to contribute to:");
			}
			if(Logging.INFO)
				System.out.println("GRID: Reading projects file...");
			BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/resources/projectsInfo.txt")));
			try {
				String readerLine = reader.readLine();
				while(readerLine != null) {
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
			} catch(IOException e) {
				e.printStackTrace();
			}
			if(usedProjects != null) {
				for(AvailableProject availableProject : availableProjects) {
					for(String name : usedProjects) {
						if(name.equals(availableProject.projectName)) {
							availableProjects.remove(availableProject);
						}
					}
				}
			}
			if(Logging.INFO)
				System.out.println("GRID: Read projects file\nGRID: Setting up project panels...");
			int yRow = 0;
			for(AvailableProject availableProject : availableProjects) {
				GridPane projectPane = new GridPane();
				projectPane.setId("unselected-pane");
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
			}
		}
		
		@FXML
		public void proceedButtonClicked(ActionEvent event) {
			new ProjectRegistrationProgress(selectedProjects);
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
			if(((GridPane) event.getSource()).getId().equals("selected-pane")) {
				((GridPane) event.getSource()).setId("unselected-pane");
				selectedProjects.remove(availableProjects.get(GridPane.getRowIndex((GridPane) event.getSource())));
			} else {
				((GridPane) event.getSource()).setId("selected-pane");
				selectedProjects.add(availableProjects.get(GridPane.getRowIndex((GridPane) event.getSource())));
			}
		}
	}
}
