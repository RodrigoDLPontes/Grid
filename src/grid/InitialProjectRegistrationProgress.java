package grid;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import grid.InitialProjectRegistration.AvailableProject;
import edu.berkeley.boinc.rpc.AccountOut;
import edu.berkeley.boinc.rpc.GlobalPreferences;
import edu.berkeley.boinc.utils.Logging;
import insidefx.undecorator.Undecorator;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class InitialProjectRegistrationProgress {

	private static Stage stage;
	private static Label projectStatusLabel;
	private static ArrayList<AvailableProject> selectedProjects = new ArrayList<AvailableProject>();

	public InitialProjectRegistrationProgress(ArrayList<AvailableProject> selectedProjects) {
		InitialProjectRegistrationProgress.selectedProjects = selectedProjects;
		try {
			stage = new Stage();
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getClass().getResource("initial_project_registration_progress.fxml"));
			loader.setController(new Controller());
			Parent root = loader.load();
			Scene scene = new Scene(root, 300, 200);
			scene.getStylesheets().add(getClass().getResource("application.css").toString());
			stage.setScene(scene);
			stage.initStyle(StageStyle.UNDECORATED);
			stage.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static class Controller implements Initializable {

		@FXML
		private Label projectStatusLabel;

		@Override
		public void initialize(URL arg0, ResourceBundle arg1) {
			InitialProjectRegistrationProgress.projectStatusLabel = projectStatusLabel;
			Thread createAccounts = new Thread(new CreateAccounts());
			createAccounts.start();
		}
	}
	
	static class CreateAccounts extends Thread {
		@Override
		public void run() {
			if (Logging.INFO)
				System.out.println("GRID: Project registration beginning...");
			int failCount;
			boolean failed;
			for (AvailableProject project : selectedProjects) {
				if (Logging.INFO)
					System.out.println("GRID: Creating account for " + project.projectName + "...");
				Platform.runLater(new Runnable() {
					public void run() {
						projectStatusLabel.setText("Creating account for " + project.projectName + "...");
					}
				});
				Grid.rpcClient.createAccount(project.accountIn);
				failCount = 0;
				failed = false;
				while(Grid.rpcClient.createAccountPoll().error_num != 0) {
					if(Logging.INFO)
						System.out.println("GRID: error_num: " + Grid.rpcClient.createAccountPoll().error_num);
					failCount++;
					try {
						Thread.sleep(100);
					} catch(InterruptedException e) {
						e.printStackTrace();
					}
					if(failCount > 50) {
						//5 seconds have passed, so skip project registration
						if(Logging.ERROR)
							System.out.println("GRID: Account creation failed!");
						Platform.runLater(new Runnable() {
							public void run() {
								projectStatusLabel.setText("Account creation failed!");
							}
						});
						failed = true;
						break;
					}
				}
				if(!failed) {
					if(Logging.INFO)
						System.out.println("GRID: Account created succesfully! (error_num: " + Grid.rpcClient.createAccountPoll().error_num + ")");
					Platform.runLater(new Runnable() {
						public void run() {
							projectStatusLabel.setText("Account created succesfully!");
						}
					});
					AccountOut accountOut = Grid.rpcClient.createAccountPoll();
					if(Logging.INFO)
						System.out.println("GRID: Attaching host...");
					Platform.runLater(new Runnable() {
						public void run() {
							projectStatusLabel.setText("Attaching host...");
						}
					});
					Grid.rpcClient.projectAttach(project.accountIn.url, accountOut.authenticator, project.projectName);
					failCount = 0;
					failed = false;
					while(Grid.rpcClient.projectAttachPoll().error_num != 0) {
						if(Logging.INFO)
							System.out.println("GRID: error_num: " + Grid.rpcClient.projectAttachPoll().error_num);
						failCount++;
						try {
							Thread.sleep(100);
						} catch(InterruptedException e) {
							e.printStackTrace();
						}
						if(failCount > 50) {
							//5 seconds have passed, so we skip attaching
							if(Logging.ERROR)
								System.out.println("GRID: Host attaching failed!");
							Platform.runLater(new Runnable() {
								public void run() {
									projectStatusLabel.setText("Host attaching failed!");
								}
							});
							failed = true;
							break;
						}
					}
					if(!failed) {
						if(Logging.INFO)
							System.out.println("GRID: Host attached succesfully! (error_num: " + Grid.rpcClient.projectAttachPoll().error_num + ")");
						Platform.runLater(new Runnable() {
							public void run() {
								projectStatusLabel.setText("Host attached succesfully!");
							}
						});
					}
				}				
			}
			//Set up some default preferences
			if(Logging.INFO)
				System.out.println("GRID: Setting up default preferences...");
			//TODO: Improve settings
			GlobalPreferences defaultPreferences = new GlobalPreferences();
			defaultPreferences.battery_charge_min_pct = 90;
			defaultPreferences.battery_max_temperature = 40;
			defaultPreferences.run_on_batteries = false;
			defaultPreferences.run_if_user_active = true;
			defaultPreferences.idle_time_to_run = 0;
			defaultPreferences.suspend_cpu_usage = 100;
			defaultPreferences.leave_apps_in_memory = false;
			defaultPreferences.dont_verify_images = false;
			defaultPreferences.work_buf_min_days = 0.1;
			defaultPreferences.work_buf_additional_days = 0;
			defaultPreferences.max_ncpus_pct = 100;
			defaultPreferences.cpu_scheduling_period_minutes = 1440;
			defaultPreferences.disk_interval = 30;
			defaultPreferences.disk_max_used_gb = 10;
			defaultPreferences.disk_max_used_pct = 100;
			defaultPreferences.disk_min_free_gb = 0;
			defaultPreferences.ram_max_used_busy_frac = 25;
			defaultPreferences.ram_max_used_idle_frac = 50;
			defaultPreferences.max_bytes_sec_up = 0;
			defaultPreferences.max_bytes_sec_down = 0;
			defaultPreferences.cpu_usage_limit = 20;
			defaultPreferences.daily_xfer_limit_mb = 0;
			defaultPreferences.daily_xfer_period_days = 0;
			defaultPreferences.override_file_present = true;
			defaultPreferences.network_wifi_only = true;
			Grid.rpcClient.setGlobalPrefsOverrideStruct(defaultPreferences);
			Grid.rpcClient.readGlobalPrefsOverride();
			if(Logging.INFO)
				System.out.println("GRID: Default preferences set up successfully");
			Platform.runLater(new Runnable() {
				public void run() {
					new MainWindow();
					stage.close();
				}
			});
		}
	}
}
