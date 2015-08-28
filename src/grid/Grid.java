package grid;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Arrays;

import edu.berkeley.boinc.rpc.RpcClient;
import edu.berkeley.boinc.utils.Logging;
import javafx.application.Application;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

public class Grid extends Application {

	static RpcClient rpcClient;
	static File projectsFolder;

	public static void main(String args[]) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		initialConfiguration();
		if(projectsFolder.getParentFile().exists() && projectsFolder.exists()) {
			if(Logging.INFO) System.out.println("GRID: Program data folder found");
			String[] list = projectsFolder.list();
			if(!((list.length == 1 && Arrays.asList(list).contains("placeholder.txt") ||
					list.length == 0))) {
				System.out.println("GRID: Found projects, initializing...");
				if(initiateBoincRpc()) {
					new MainWindow();
				}
			} else {
				//TODO: User has already used BOINC, so we shouldn't use Register again
				System.out.println("GRID: No projects found, prompting register...");
				new Register();
			}
		} else {
			System.out.println("GRID: No program data folder found, prompting register...");
			projectsFolder.getParentFile().mkdirs();
			new Register();
		}
	}
	
	/**
	 * Performs any initial setup
	 */
	private static void initialConfiguration() {
//		if(Logging.ERROR) {
//			new DebugConsole();
//			System.setOut(new PrintStream(new DebugConsole.DebugConsolePrintStream()));
//			System.setErr(new PrintStream(new DebugConsole.DebugConsolePrintStream()));
//		}
		if(System.getProperty("os.name").toLowerCase().contains("win")) {
			projectsFolder = new File("C:\\ProgramData\\BOINC\\projects");
		} else if(System.getProperty("os.name").toLowerCase().contains("mac")) {
			projectsFolder = new File("/Library/Application Support/BOINC Data/projects");
		}
	}
	
	/**
	 * Starts and connects to BOINC
	 * @return True for success, false otherwise
	 */
	public static boolean initiateBoincRpc() {
		ProcessBuilder boincBuilder = new ProcessBuilder("C:\\Program Files (x86)\\Grid\\BOINC\\boinc.exe");
		try {
			Process boinc = boincBuilder.start();
			if(Logging.ERROR) {
				Thread boincOutput = new Thread(new BoincOutput(boinc));
				boincOutput.setDaemon(true);
				boincOutput.start();
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
		rpcClient = new RpcClient();
		int failCount = 0;
		boolean failed = false;
		while(!rpcClient.open("localhost", 31416)) {
			failCount++;
			try {
				Thread.sleep(100);
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
			if(failCount > 50) {
				Alert alert = new Alert(Alert.AlertType.WARNING);
				alert.setContentText("Failed to connect to BOINC, please try again.");
				alert.showAndWait();
				failed = true;
				break;
			}
		}
		if(!failed) {
			failCount = 0;
			File guiRpcAuth = new File("C:\\ProgramData\\BOINC\\gui_rpc_auth.cfg");
			while(!guiRpcAuth.exists()) {
				failCount++;
				try {
					Thread.sleep(100);
				} catch(InterruptedException e) {
					e.printStackTrace();
				}
				if(failCount > 50) {
					Alert alert = new Alert(Alert.AlertType.WARNING);
					alert.setContentText("Failed to connect to BOINC, please try again.");
					alert.showAndWait();
					failed = true;
					break;
				}
			}
			if(!failed) {
				failCount = 0;
				try {
					BufferedReader reader = new BufferedReader(new FileReader(guiRpcAuth));
					String auth = reader.readLine();
					while(!rpcClient.authorize(auth)) {
						failCount++;
						try {
							Thread.sleep(100);
						} catch(InterruptedException e) {
							e.printStackTrace();
						}
						if(failCount > 50) {
							Alert alert = new Alert(Alert.AlertType.WARNING);
							alert.setContentText("Failed to connect to BOINC, please try again.");
							alert.showAndWait();
							failed = true;
							break;
						}
					}
					reader.close();
					if(!failed) {
						return true;
					}
				} catch(IOException e) {
					e.printStackTrace();
				}
			}
		}
		return false;
	}

	/**
	 * Handles BOINC output
	 */
	private static class BoincOutput extends Thread {

		Process boinc;
		BufferedReader reader;

		public BoincOutput(Process boinc) {
			super();
			this.boinc = boinc;
			reader = new BufferedReader(new InputStreamReader(this.boinc.getInputStream()));
		}

		@Override
		public void run() {
			String output;
			while(true) {
				try {
					//Sleep to avoid hogging system
					Thread.sleep(1000);
					output = reader.readLine();
					if(output != null) System.out.println(output);
				} catch(IOException e) {
					e.printStackTrace();
				} catch(InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
