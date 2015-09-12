package grid;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import edu.berkeley.boinc.rpc.CcStatus;
import edu.berkeley.boinc.rpc.Project;
import edu.berkeley.boinc.rpc.Result;
import edu.berkeley.boinc.rpc.RpcClient;
import facebook4j.Facebook;
import facebook4j.FacebookException;
import facebook4j.FacebookFactory;
import facebook4j.PostUpdate;
import facebook4j.auth.AccessToken;
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
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class MainWindow {

	private static Stage stage;
	private boolean usesFacebook = false;
	private static Facebook facebook;

	private static GridPane tasksPane;
	private static GridPane projectsPane;
	private static GridPane profilePane;
	private static Menu activityMenu;
	private static ImageView activityButtonImageView;

	public MainWindow() {
//		try {
//			File tokenFile = new File("C:\\ProgramData\\Grid\\token.cfg");
//			if(tokenFile.exists()) {
//				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(tokenFile)));
//				usesFacebook = true;
//				facebook = new FacebookFactory().getInstance();
//				facebook.setOAuthAppId("", "");
//				facebook.setOAuthAccessToken(new AccessToken(bufferedReader.readLine()));
//				bufferedReader.close();
//			}
//		} catch(FileNotFoundException e) {
//			e.printStackTrace();
//		} catch(IOException e) {
//			e.printStackTrace();
//		}
		try {
			stage = new Stage();
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getClass().getResource("main_window.fxml"));
			loader.setController(new Controller());
			Parent root = loader.load();
			Undecorator frame = new Undecorator(stage, (Region) root);
			frame.getStylesheets().add("skin/undecorator.css");
			frame.getStylesheets().add("grid/grid_undecorator.css");
			Scene scene = new Scene(frame, 600, 400);
			scene.getStylesheets().add(getClass().getResource("application.css").toString());
			scene.setFill(Color.TRANSPARENT);
			stage.setTitle("Grid");
			stage.setScene(scene);
			stage.initStyle(StageStyle.TRANSPARENT);
			stage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	class Controller implements Initializable {

		@FXML
		private GridPane tasksPane;
		@FXML
		private GridPane projectsPane;
		@FXML
		private GridPane profilePane;
		@FXML
		private Menu activityMenu;
		@FXML
		private MenuItem close;
		@FXML
		private MenuItem exit;
		@FXML
		private MenuItem preferences;
		@FXML
		private Button activityButton;
		@FXML
		private Button addProjectButton;
//		@FXML
//		private Button shareButton;
		@FXML
		private ImageView activityButtonImageView;
		@FXML
		private Label usernameLabel;
		@FXML
		private Label totalCreditLabel;
		@FXML
		private Label rankLabel;
		@FXML
		private ImageView rankImage;

		@Override
		public void initialize(URL location, ResourceBundle resources) {
			MainWindow.tasksPane = tasksPane;
			MainWindow.projectsPane = projectsPane;
			MainWindow.profilePane = profilePane;
			MainWindow.activityMenu = activityMenu;
			MainWindow.activityButtonImageView = activityButtonImageView;
			close.setOnAction(new ActionEventHandler());
			exit.setOnAction(new ActionEventHandler());
			preferences.setOnAction(new ActionEventHandler());
			activityButton.setOnAction(new ActionEventHandler());
			addProjectButton.setOnAction(new ActionEventHandler());
			CcStatus ccStatus = Grid.rpcClient.getCcStatus();
			if(ccStatus.task_mode == 1 || ccStatus.task_mode == 2) {
				MainWindow.activityButtonImageView.setImage(new Image("resources/Pause.png"));
				MenuItem item = new MenuItem("Suspend activity");
				item.setOnAction(new ActionEventHandler());
				MainWindow.activityMenu.getItems().add(item);
			} else {
				MainWindow.activityButtonImageView.setImage(new Image("resources/Resume.png"));
				MenuItem item = new MenuItem("Resume activity");
				item.setOnAction(new ActionEventHandler());
				MainWindow.activityMenu.getItems().add(item);
			}
			if(ccStatus.network_mode == 1 || ccStatus.network_mode == 2) {
				MenuItem item = new MenuItem("Suspend network activity");
				item.setOnAction(new ActionEventHandler());
				MainWindow.activityMenu.getItems().add(item);

			} else {
				MenuItem item = new MenuItem("Resume network activity");
				item.setOnAction(new ActionEventHandler());
				MainWindow.activityMenu.getItems().add(item);
			}
			try {
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(
						new FileInputStream(new File("C:\\ProgramData\\Grid\\userInfo.cfg"))));
				bufferedReader.readLine();
				usernameLabel.setText(bufferedReader.readLine());
				bufferedReader.close();
			} catch(FileNotFoundException e) {
				e.printStackTrace();
			} catch(IOException e) {
				e.printStackTrace();
			}
			double totalCredit = 0;
			for(Project project : Grid.rpcClient.getProjectStatus()) {
				totalCredit += project.user_total_credit;
			}
			totalCreditLabel.setText("Total credit: " + new DecimalFormat("#.##").format(totalCredit));
//			if(usesFacebook) {
//				shareButton.setOnAction(new ActionEventHandler());
//			} else {
//				((VBox) shareButton.getParent()).getChildren().remove(shareButton);
//			}
			if(0 <= totalCredit && totalCredit < 1000) {
				rankLabel.setText("Rank 1");
				rankImage.setImage(new Image("/resources/1.png"));
			} else if(1000 <= totalCredit && totalCredit < 5000) {
				rankLabel.setText("Rank 2");
				rankImage.setImage(new Image("/resources/2.png"));
			} else if(5000 <= totalCredit && totalCredit < 10000) {
				rankLabel.setText("Rank 3");
				rankImage.setImage(new Image("/resources/3.png"));
			} else if(10000 <= totalCredit && totalCredit < 50000) {
				rankLabel.setText("Rank 4");
				rankImage.setImage(new Image("/resources/4.png"));
			} else if(50000 <= totalCredit && totalCredit < 100000) {
				rankLabel.setText("Rank 5");
				rankImage.setImage(new Image("/resources/5.png"));
			} else if(100000 <= totalCredit && totalCredit < 500000) {
				rankLabel.setText("Rank 6");
				rankImage.setImage(new Image("/resources/6.png"));
			} else if(500000 <= totalCredit && totalCredit < 1000000) {
				rankLabel.setText("Rank 7");
				rankImage.setImage(new Image("/resources/7.png"));
			} else if(1000000 <= totalCredit && totalCredit < 5000000) {
				rankLabel.setText("Rank 8");
				rankImage.setImage(new Image("/resources/8.png"));
			} else if(5000000 <= totalCredit && totalCredit < 10000000) {
				rankLabel.setText("Rank 9");
				rankImage.setImage(new Image("/resources/9.png"));
			} else if(10000000 <= totalCredit && totalCredit < 50000000) {
				rankLabel.setText("Rank 10");
				rankImage.setImage(new Image("/resources/10.png"));
			} else if(50000000 <= totalCredit && totalCredit < 100000000) {
				rankLabel.setText("Rank 11");
				rankImage.setImage(new Image("/resources/11.png"));
			} else if(100000000 <= totalCredit && totalCredit < 500000000) {
				rankLabel.setText("Rank 12");
				rankImage.setImage(new Image("/resources/12.png"));
			} else if(500000000 <= totalCredit && totalCredit < 1000000000) {
				rankLabel.setText("Rank 13");
				rankImage.setImage(new Image("/resources/13.png"));
			} else if(1000000000 <= totalCredit && totalCredit < 5000000000l) {
				rankLabel.setText("Rank 14");
				rankImage.setImage(new Image("/resources/14.png"));
			} else if(5000000000l <= totalCredit && totalCredit < 10000000000l) {
				rankLabel.setText("Rank 15");
				rankImage.setImage(new Image("/resources/15.png"));
			}
			Timer updateTasks = new Timer(true);
			Timer updateProjects = new Timer(true);
			updateTasks.schedule(new UpdateTasks(), 0, 1000);
			updateProjects.schedule(new UpdateProjects(), 0, 1000);
			updateStatistics();
		}
	}

	private void updateTasks() {
		Platform.runLater(new Runnable() {
			public void run() {
				int row = 0;
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
				for(Result task : tasks) {
					GridPane taskPane = new GridPane();
					if(task.active_task && !task.suspended_via_gui && task.active_task_state != 0)
						taskPane.setId("selected-pane");
					else
						taskPane.setId("unselected-pane");
					ColumnConstraints column1 = new ColumnConstraints();
					column1.setPercentWidth(50);
					ColumnConstraints column2 = new ColumnConstraints();
					column2.setPercentWidth(30);
					ColumnConstraints column3 = new ColumnConstraints();
					column3.setPercentWidth(20);
					taskPane.getColumnConstraints().addAll(column1, column2, column3);
					taskPane.setPadding(new Insets(0, 5, 3, 5));
					taskPane.setOnMouseClicked(new PaneEventHandler());
					GridPane.setHgrow(taskPane, Priority.ALWAYS);
					Label projectLabel = new Label(getProjectName(task.project_url));
					projectLabel.setFont(new Font(20));
					taskPane.add(projectLabel, 0, 0);
					Label taskName = new Label(task.name);
					taskName.setFont(new Font(10));
					taskPane.add(taskName, 0, 1);
					ProgressBar progressBar = new ProgressBar();
					progressBar.setPrefWidth(150);
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
					tasksPane.add(taskPane, 0, row);
					row++;
				}
			}
		});
	}

	private void updateProjects() {
		Platform.runLater(new Runnable() {
			public void run() {
				int row = 0;
				projectsPane.getChildren().remove(0, projectsPane.getChildren().size());
				for(Project project : Grid.rpcClient.getProjectStatus()) {
					GridPane projectPane = new GridPane();
					projectPane.setId("project-pane");
					ColumnConstraints column1 = new ColumnConstraints();
					column1.setPercentWidth(50);
					ColumnConstraints column2 = new ColumnConstraints();
					column2.setPercentWidth(50);
					projectPane.getColumnConstraints().addAll(column1, column2);
					projectPane.setPadding(new Insets(5));
					projectPane.setOnMouseClicked(new PaneEventHandler());
					GridPane.setHgrow(projectPane, Priority.ALWAYS);
					Label projectLabel = new Label(project.project_name);
					projectLabel.setFont(new Font(18));
					projectPane.add(projectLabel, 0, 0);
					Label creditLabel = new Label(String.valueOf((int) project.user_total_credit));
					creditLabel.setFont(new Font(18));
					GridPane.setHalignment(creditLabel, HPos.RIGHT);
					projectPane.add(creditLabel, 1, 0);
					projectsPane.add(projectPane, 0, row);
					row++;
				}
			}
		});
	}

	private void updateStatistics() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				ArrayList<Statistic> statistics = StatisticsParser.getStatistics();
				for(Statistic statistic : statistics) {
					if(statistics.indexOf(statistic) != 0) {
						for(int i = statistics.indexOf(statistic) - 1; i >= 0; i--) {
							if(statistic.project_name.compareToIgnoreCase(statistics.get(i).project_name) < 0) {
								Collections.swap(statistics, statistics.indexOf(statistic), i);
							}
						}
					}
				}
				int row1 = 2;
				for(Statistic statistic : statistics) {
					GridPane statisticPane = new GridPane();
					ColumnConstraints column = new ColumnConstraints();
					column.setPercentWidth(100);
					statisticPane.getColumnConstraints().add(column);
					GridPane.setHgrow(statisticPane, Priority.ALWAYS);
					Label projectName = new Label(statistic.project_name);
					projectName.setFont(new Font(18));
					statisticPane.add(projectName, 0, 0);
					CategoryAxis xAxis = new CategoryAxis();
					NumberAxis yAxis = new NumberAxis();
					AreaChart<String, Double> areaChart = new AreaChart((Axis) xAxis, (Axis) yAxis);
					areaChart.setMaxHeight(250);
					XYChart.Series<String, Double> xyChart = new XYChart.Series();
					DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault());
					for(Statistic.DailyStat dailyStat : statistic.dailyStats) {
						xyChart.getData().add(new XYChart.Data(dateFormat.format(new Date((long) (dailyStat.day) * 1000)),
								dailyStat.host_total_credit));
					}
					areaChart.getData().add(xyChart);
					GridPane.setHalignment(areaChart, HPos.CENTER);
					statisticPane.add(areaChart, 0, 2);
					profilePane.add(statisticPane, 0, row1);
					row1++;
				}
			}
		});
	}


	class UpdateTasks extends TimerTask {
		// TODO: Improve this
		public void run() {
			updateTasks();
		}
	}

	class UpdateProjects extends TimerTask {
		// TODO: Improve this
		public void run() {
			updateProjects();
		}
	}

	class PaneEventHandler implements EventHandler<MouseEvent> {
		// TODO: More user friendly UI response.
		public void handle(MouseEvent event) {
			if(event.getSource() instanceof GridPane) {
				if(((GridPane) event.getSource()).getId().equals("project-pane")) {
					if(event.isPopupTrigger()) {
						ContextMenu contextMenu = new ContextMenu();
						MenuItem item1 = new MenuItem("Remove");
						item1.setOnAction(new ActionEventHandler((GridPane) event.getSource()));
						contextMenu.getItems().addAll(item1);
						contextMenu.show((GridPane) event.getSource(), event.getScreenX(), event.getScreenY());
					}
				} else {
					if(event.getClickCount() == 2) {
						if(((GridPane) event.getSource()).getId().equals("selected-pane")) {
							((GridPane) event.getSource()).setId("unselected-pane");
							setTaskState(RpcClient.RESULT_SUSPEND, ((Label) ((GridPane) event.getSource()).getChildren().get(1)).getText());
						} else {
							((GridPane) event.getSource()).setId("selected-pane");
							setTaskState(RpcClient.RESULT_RESUME, ((Label) ((GridPane) event.getSource()).getChildren().get(1)).getText());
						}
					} else if(event.isPopupTrigger()) {
						ContextMenu contextMenu = new ContextMenu();
						MenuItem item1;
						if(((GridPane) event.getSource()).getId().equals("selected-pane")) {
							item1 = new MenuItem("Suspend");
						} else {
							item1 = new MenuItem("Resume");
						}
						item1.setOnAction(new ActionEventHandler((GridPane) event.getSource()));
						MenuItem item2 = new MenuItem("Abort");
						item2.setOnAction(new ActionEventHandler((GridPane) event.getSource()));
						MenuItem item3 = new MenuItem("Project's HomePage");
						item3.setOnAction(new ActionEventHandler((GridPane) event.getSource()));
						contextMenu.getItems().addAll(item1, item2, item3);
						contextMenu.show((GridPane) event.getSource(), event.getScreenX(), event.getScreenY());
					}
				}
			}
		}
	}

	class ActionEventHandler implements EventHandler<ActionEvent> {

		GridPane source;

		public ActionEventHandler() {
		}

		public ActionEventHandler(GridPane source) {
			this.source = source;
		}

		// TODO: Handle "Preferences"
		public void handle(ActionEvent event) {
			if(event.getSource() instanceof MenuItem) {
				//Activity menu
				if(((MenuItem) event.getSource()).getText().equals("Resume activity")) {
					((MenuItem) event.getSource()).setText("Suspend activity");
					Grid.rpcClient.setRunMode(2, 0);
				} else if(((MenuItem) event.getSource()).getText().equals("Suspend activity")) {
					((MenuItem) event.getSource()).setText("Resume activity");
					Grid.rpcClient.setRunMode(3, 0);
				} else if(((MenuItem) event.getSource()).getText().equals("Resume network activity")) {
					((MenuItem) event.getSource()).setText("Suspend network activity");
					Grid.rpcClient.setNetworkMode(2, 0);
				} else if(((MenuItem) event.getSource()).getText().equals("Suspend network activity")) {
					((MenuItem) event.getSource()).setText("Resume network activity");
					Grid.rpcClient.setNetworkMode(3, 0);
					//File menu
				} else if(((MenuItem) event.getSource()).getText().equals("Close")) {
					stage.close();
				} else if(((MenuItem) event.getSource()).getText().equals("Exit")) {
					while(!Grid.rpcClient.quit()) ;
					System.exit(0);
					//Preferences
				} else if(((MenuItem) event.getSource()).getText().equals("Preferences")) {
					new Preferences();
					//Task's popup menu
				} else if(((MenuItem) event.getSource()).getText().equals("Resume")) {
					source.setId("selected-pane");
					setTaskState(RpcClient.RESULT_RESUME, ((Label) source.getChildren().get(1)).getText());
				} else if(((MenuItem) event.getSource()).getText().equals("Suspend")) {
					source.setId("unselected-pane");
					setTaskState(RpcClient.RESULT_SUSPEND, ((Label) source.getChildren().get(1)).getText());
				} else if(((MenuItem) event.getSource()).getText().equals("Abort")) {
					source.setId("unselected-pane");
					setTaskState(RpcClient.RESULT_ABORT, ((Label) source.getChildren().get(1)).getText());
				} else if(((MenuItem) event.getSource()).getText().equals("Project's HomePage")) {
					try {
						Desktop.getDesktop().browse(getTaskURL(((Label) source.getChildren().get(1)).getText()));
					} catch(IOException e) {
						e.printStackTrace();
					}
				} else if(((MenuItem) event.getSource()).getText().equals("Remove")) {
					setProjectState(RpcClient.PROJECT_DETACH, ((Label) source.getChildren().get(0)).getText());
					updateProjects();
				}
			} else if(event.getSource() instanceof Button) {
				if(((Button) event.getSource()).getId().equals("activityButton")) {
					CcStatus ccStatus = Grid.rpcClient.getCcStatus();
					if(ccStatus.task_mode == 1 || ccStatus.task_mode == 2) {
						activityButtonImageView.setImage(new Image("resources/Resume.png"));
						Grid.rpcClient.setRunMode(3, 0);
					} else {
						activityButtonImageView.setImage(new Image("resources/Pause.png"));
						Grid.rpcClient.setRunMode(2, 0);
					}
				} else if(((Button) event.getSource()).getId().equals("addProjectButton")) {
					ArrayList<String> usedProjects = new ArrayList<>();
					for(Project project : Grid.rpcClient.getProjectStatus()) {
						usedProjects.add(project.master_url);
					}
					BufferedReader bufferedReader;
					Boolean usesFacebook = false;
					String username = null, email = null, password = null;
					try {
						bufferedReader = new BufferedReader(new InputStreamReader(
								new FileInputStream(new File("C:\\ProgramData\\Grid\\userInfo.cfg"))));
						usesFacebook = Boolean.parseBoolean(bufferedReader.readLine());
						username = bufferedReader.readLine();
						email = bufferedReader.readLine();
						password = bufferedReader.readLine();
					} catch(FileNotFoundException e) {
						e.printStackTrace();
					} catch(IOException e) {
						e.printStackTrace();
					}
					if(usesFacebook) {
						new ProjectRegistration(username, email, password, false, usedProjects);
					} else {
						new NewProjectRegistrationPrompt(stage, usedProjects);
					}
				}// else if(((Button) event.getSource()).getId().equals("shareButton")) {
//					try {
//						facebook.postFeed(new PostUpdate(new URL("https://www.facebook.com/Grid-Volunteer-Computing-569226836548850/"))
//								.picture(new URL("https://scontent-gru1-1.xx.fbcdn.net/hphotos-xaf1/v/t1.0-9/11954782_569227213215479_460999787230330965_n.jpg?oh=809f3f0ca26269a4dd59c47755ea696c&oe=5670A317"))
//								.name("Grid - Help change the world. Join the Grid.")
//								.caption("Use your computer to solve mankind's greatest challenges")
//								.description("Grid is a platform in which you can donate computer resources to help advance scientific research."));
//					} catch(FacebookException e) {
//						e.printStackTrace();
//					} catch(MalformedURLException e) {
//						e.printStackTrace();
//					}
//				}
			}
		}
	}

	/*
	Private helper methods
	 */

	/**
	 * Get's project's name given task's URL, since it's not provided by BOINC
	 *
	 * @param taskUrl Task's URL
	 * @return Project's name
	 */
	private String getProjectName(String taskUrl) {
		for(Project project : Grid.rpcClient.getProjectStatus()) {
			if(taskUrl.equals(project.master_url))
				return project.project_name;
		}
		return null;
	}

	/**
	 * Gets task's URL given task's name
	 *
	 * @param taskName Task's name
	 * @return Task's URL
	 */
	private URI getTaskURL(String taskName) {
		for(Result result : Grid.rpcClient.getResults()) {
			if(taskName.equals(result.name))
				try {
					return new URI(result.project_url);
				} catch(URISyntaxException e) {
					e.printStackTrace();
					return null;
				}
		}
		return null;
	}

	/**
	 * Sets task's state given task's name
	 *
	 * @param state    State to be set (as found in RpcClient)
	 * @param taskName Task's name
	 */
	private void setTaskState(int state, String taskName) {
		for(Result result : Grid.rpcClient.getResults()) {
			if(taskName.equals(result.name))
				Grid.rpcClient.resultOp(state, result.project_url, taskName);
			return;
		}
	}

	/**
	 * Sets project's state given project's name
	 *
	 * @param state       State to be set (as found in RpcClient)
	 * @param projectName Project's name
	 */
	private void setProjectState(int state, String projectName) {
		for(Project project : Grid.rpcClient.getProjectStatus()) {
			if(projectName.equals(project.project_name)) {
				Grid.rpcClient.projectOp(state, project.master_url);
				return;
			}
		}
	}
}
