package grid;

import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import edu.berkeley.boinc.rpc.CcStatus;
import edu.berkeley.boinc.rpc.Project;
import edu.berkeley.boinc.rpc.Result;
import edu.berkeley.boinc.rpc.RpcClient;
import insidefx.undecorator.Undecorator;
import javafx.application.Platform;
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
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class MainWindow {

	private static GridPane tasksPane;
	private static GridPane projectsPane;
	private static Menu activityMenu;
	private Stage stage;

	public MainWindow() {
		try {
			stage = new Stage();
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getClass().getResource("main_window.fxml"));
			loader.setController(new Controller());
			Parent root = loader.load();
			Undecorator frame = new Undecorator(stage, (Region)root);
			frame.getStylesheets().add("skin/undecorator.css");
			frame.getStylesheets().add("grid/grid_undecorator.css");
			Scene scene = new Scene(frame, 600, 400);
			scene.getStylesheets().add(getClass().getResource("application.css").toString());
			scene.setFill(Color.TRANSPARENT);
			stage.setScene(scene);
			stage.initStyle(StageStyle.TRANSPARENT);
			stage.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	class Controller implements Initializable {

		@FXML
		private GridPane tasksPane;
		@FXML
		private GridPane projectsPane;
		@FXML
		private Menu activityMenu;
		@FXML
		private MenuItem close;
		@FXML
		private MenuItem exit;
		@FXML
		private MenuItem preferences;

		@Override
		public void initialize(URL location, ResourceBundle resources) {
			MainWindow.tasksPane = tasksPane;
			MainWindow.projectsPane = projectsPane;
			MainWindow.activityMenu = activityMenu;
			close.setOnAction(new MenuItemEventHandler());
			exit.setOnAction(new MenuItemEventHandler());
			preferences.setOnAction(new MenuItemEventHandler());
			CcStatus ccStatus = Grid.rpcClient.getCcStatus();
			if(ccStatus.task_mode == 1 || ccStatus.task_mode == 2) {
				MenuItem item = new MenuItem("Suspend activity");
				item.setOnAction(new MenuItemEventHandler());
				MainWindow.activityMenu.getItems().add(item);
			} else {
				MenuItem item = new MenuItem("Resume activity");
				item.setOnAction(new MenuItemEventHandler());
				MainWindow.activityMenu.getItems().add(item);
			}
			if(ccStatus.network_mode == 1 || ccStatus.network_mode == 2) {
				MenuItem item = new MenuItem("Suspend network activity");
				item.setOnAction(new MenuItemEventHandler());
				MainWindow.activityMenu.getItems().add(item);

			} else {
				MenuItem item = new MenuItem("Resume network activity");
				item.setOnAction(new MenuItemEventHandler());
				MainWindow.activityMenu.getItems().add(item);
			}
			Timer updateTasks = new Timer(true);
			Timer updateProjects = new Timer(true);
			updateTasks.schedule(new UpdateTasks(), 0, 1000);
			updateProjects.schedule(new UpdateProjects(), 0, 1000);
		}
	}

	class UpdateTasks extends TimerTask {
		// TODO: Improve this
		public void run() {
			Platform.runLater(new Runnable() {
				public void run() {
					int yRow = 0;
					tasksPane.getChildren().remove(0, tasksPane.getChildren().size());
					ArrayList<Result> tasks = Grid.rpcClient.getResults();
					//Simple alphabetical ordering
					for(Result task : tasks) {
						if(tasks.indexOf(task) != 0) {
							for(int i = tasks.indexOf(task) - 1; i >= 0; i--) {
								if(task.name.compareToIgnoreCase(tasks.get(i).name) < 0) {
									Collections.swap(tasks, tasks.indexOf(task), i);
								}
							}
						}
					}
					//Place active tasks first
					int position = 5;
					for(Result task : tasks) {
						if(task.active_task && !task.suspended_via_gui && task.active_task_state != 0) {
							for(int i = tasks.indexOf(task) - 1; i >= 0; i--) {
								if(!(tasks.get(i).active_task && !tasks.get(i).suspended_via_gui && tasks.get(i).active_task_state != 0)) {
									Collections.swap(tasks, tasks.indexOf(task), i);
								}
							}
						}
					}
					//Display tasks
					for (Result task : tasks) {
						GridPane taskPane = new GridPane();
						if(task.active_task && !task.suspended_via_gui && task.active_task_state != 0)
							taskPane.setId("selected-pane");
						else
							taskPane.setId("unselected-pane");
						ColumnConstraints column1 = new ColumnConstraints();
						column1.setPercentWidth(50);
						ColumnConstraints column2 = new ColumnConstraints();
						column2.setPercentWidth(25);
						ColumnConstraints column3 = new ColumnConstraints();
						column3.setPercentWidth(25);
						taskPane.getColumnConstraints().addAll(column1, column2, column3);
						taskPane.setPadding(new Insets(5));
						taskPane.setOnMouseClicked(new PaneEventHandler());
						GridPane.setHgrow(taskPane, Priority.ALWAYS);
						Label projectLabel = new Label(getProjectName(task.project_url));
						projectLabel.setFont(new Font(18));
						taskPane.add(projectLabel, 0, 0);
						Label taskName = new Label(task.name);
						taskPane.add(taskName, 0, 1);
						ProgressBar progressBar = new ProgressBar();
						progressBar.setProgress(task.fraction_done);
						GridPane.setValignment(progressBar, VPos.CENTER);
						GridPane.setHalignment(progressBar, HPos.CENTER);
						taskPane.add(progressBar, 1, 0, 1, 2);
						Label progressLabel = new Label(new DecimalFormat("#.##%").format(task.fraction_done));
						progressLabel.setId("progress-bar-label");
						GridPane.setValignment(progressLabel, VPos.CENTER);
						GridPane.setHalignment(progressLabel, HPos.CENTER);
						GridPane.setMargin(progressLabel, new Insets(0, 0, 1, 0));
						taskPane.add(progressLabel, 1, 0, 1, 2);
						SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMMM dd");
						Label deadlineLabel = new Label(simpleDateFormat.format(new Date(task.report_deadline * 1000)));
						GridPane.setValignment(deadlineLabel, VPos.CENTER);
						GridPane.setHalignment(deadlineLabel, HPos.RIGHT);
						taskPane.add(deadlineLabel, 2, 0, 1, 2);
						tasksPane.add(taskPane, 0, yRow);
						yRow++;
					}
				}
			});
		}
	}

	class UpdateProjects extends TimerTask {
		// TODO: Improve this
		public void run() {
			Platform.runLater(new Runnable() {
				public void run() {
					int yRow = 0;
					projectsPane.getChildren().remove(0, projectsPane.getChildren().size());
					for (Project project : Grid.rpcClient.getProjectStatus()) {
						GridPane projectPane = new GridPane();
						projectPane.setBackground(new Background(new BackgroundFill(Color.DARKGREY, CornerRadii.EMPTY, Insets.EMPTY)));
						ColumnConstraints column1 = new ColumnConstraints();
						column1.setPercentWidth(50);
						ColumnConstraints column2 = new ColumnConstraints();
						column2.setPercentWidth(50);
						projectPane.getColumnConstraints().addAll(column1, column2);
						projectPane.setPadding(new Insets(5));
						GridPane.setHgrow(projectPane, Priority.ALWAYS);
						Label projectLabel = new Label(project.project_name);
						projectLabel.setFont(new Font(18));
						projectPane.add(projectLabel, 0, 0);
						Label creditLabel = new Label(String.valueOf((int)project.user_total_credit));
						creditLabel.setFont(new Font(18));
						GridPane.setHalignment(creditLabel, HPos.RIGHT);
						projectPane.add(creditLabel, 1, 0);
						projectsPane.add(projectPane, 0, yRow);
						yRow++;
					}
				}
			});
		}
	}

	class PaneEventHandler implements EventHandler<MouseEvent> {
		// TODO: More user friendly UI response.
		public void handle(MouseEvent event) {
			if(event.getSource() instanceof GridPane) {
				if(event.getClickCount() == 2) {
					if(((GridPane)event.getSource()).getId().equals("selected-pane")) {
						((GridPane)event.getSource()).setId("unselected-pane");
						setTaskState(RpcClient.RESULT_SUSPEND, ((Label)((GridPane)event.getSource()).getChildren().get(1)).getText());
					} else {
						((GridPane)event.getSource()).setId("selected-pane");
						setTaskState(RpcClient.RESULT_RESUME, ((Label) ((GridPane) event.getSource()).getChildren().get(1)).getText());
					}
				} else if(event.isPopupTrigger()) {
					ContextMenu contextMenu = new ContextMenu();
					MenuItem item1;
					if(((GridPane)event.getSource()).getBackground().getFills().get(0).getFill().equals(Color.LIGHTGRAY)) {
						item1 = new MenuItem("Suspend");
					} else {
						item1 = new MenuItem("Resume");
					}
					item1.setOnAction(new MenuItemEventHandler());
					MenuItem item2 = new MenuItem("Abort");
					item2.setOnAction(new MenuItemEventHandler());
					MenuItem item3 = new MenuItem("Project's HomePage");
					item3.setOnAction(new MenuItemEventHandler());
					contextMenu.getItems().addAll(item1, item2, item3);
					contextMenu.show((GridPane)event.getSource(), event.getScreenX(), event.getScreenY());
				}
			}
		}
	}

	class MenuItemEventHandler implements EventHandler<ActionEvent> {
		// TODO: Handle "Preferences"
		public void handle(ActionEvent event) {
			if(event.getSource() instanceof MenuItem) {
				if(((MenuItem)event.getSource()).getText().equals("Resume activity")) {
					((MenuItem)event.getSource()).setText("Suspend activity");
					Grid.rpcClient.setRunMode(2, 0);
				} else if(((MenuItem)event.getSource()).getText().equals("Suspend activity")) {
					((MenuItem)event.getSource()).setText("Resume activity");
					Grid.rpcClient.setRunMode(3, 0);
				} else if(((MenuItem)event.getSource()).getText().equals("Resume network activity")) {
					((MenuItem)event.getSource()).setText("Suspend network activity");
					Grid.rpcClient.setNetworkMode(2, 0);
				} else if(((MenuItem)event.getSource()).getText().equals("Suspend network activity")) {
					((MenuItem)event.getSource()).setText("Resume network activity");
					Grid.rpcClient.setNetworkMode(3, 0);
				} else if(((MenuItem)event.getSource()).getText().equals("Close")) {
					stage.close();
				} else if(((MenuItem)event.getSource()).getText().equals("Exit")) {
					while (!Grid.rpcClient.quit()) ;
					System.exit(0);
				} else if(((MenuItem)event.getSource()).getText().equals("Preferences")) {
					new Preferences();
				}
			}
		}
	}

	/*
	Private helper methods
	 */

	/**
	 * Get's project's name given task's URL, since it's not provided by BOINC
	 * @param taskUrl Task's URL
	 * @return Project's name
	 */
	private String getProjectName(String taskUrl) {
		for (Project project : Grid.rpcClient.getProjectStatus()) {
			if(taskUrl.equals(project.master_url))
				return project.project_name;
		}
		return null;
	}

	/**
	 * Sets task state
	 * @param state State to be set (as found in RpcClient)
	 * @param taskName Task's name
	 */
	private void setTaskState(int state, String taskName) {
		for (Result result : Grid.rpcClient.getResults()) {
			if(taskName.equals(result.name))
				Grid.rpcClient.resultOp(state, result.project_url, taskName);
		}
	}
}
