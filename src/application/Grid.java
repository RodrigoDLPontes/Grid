package application;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.ProcessBuilder.Redirect;
import java.util.ArrayList;
import java.util.Arrays;

import application.InitialProjectRegistration.AvailableProject;
import edu.berkeley.boinc.rpc.RpcClient;
import edu.berkeley.boinc.utils.Logging;
import javafx.application.Application;
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
		//TODO: Handle empty program data folder
		if(projectsFolder.getParentFile().exists()) {
			if(Logging.INFO) System.out.println("GRID: Program data folder found");
			String[] list = projectsFolder.list();
			if (!(list.length == 1 && Arrays.asList(list).contains("placeholder.txt"))) {
				System.out.println("GRID: Found projects, initializing...");
				initiateBoincRpc();
				new MainWindow();
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
		if(Logging.ERROR) {
			new DebugConsole();
			System.setOut(new PrintStream(new DebugConsole.DebugConsolePrintStream()));
			System.setErr(new PrintStream(new DebugConsole.DebugConsolePrintStream()));
		}
		if(System.getProperty("os.name").toLowerCase().contains("win")) {
			projectsFolder = new File("C:\\ProgramData\\BOINC\\projects");
		} else if(System.getProperty("os.name").toLowerCase().contains("mac")) {
			projectsFolder = new File("/Library/Application Support/BOINC Data/projects");
		}
	}
	
	/**
	 * Starts and connects to BOINC
	 */
	public static void initiateBoincRpc() {
		//TODO: Handle potential errors (can't connect, can't find auth, etc.)
		ProcessBuilder boincBuilder = new ProcessBuilder("C:\\Program Files (x86)\\Grid\\BOINC\\boinc.exe");
		try {
			Process boinc = boincBuilder.start();
			Thread boincOutput = new Thread(new BoincOutput(boinc));
			boincOutput.setDaemon(true);
			boincOutput.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
		rpcClient = new RpcClient();
		while (!rpcClient.open("localhost", 31416));
		File guiRpcAuth = new File("C:\\ProgramData\\BOINC\\gui_rpc_auth.cfg");
		while(!guiRpcAuth.exists());
		try {
			BufferedReader reader = new BufferedReader(new FileReader(guiRpcAuth));
			String auth = reader.readLine();
			while (!rpcClient.authorize(auth));
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
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
					output = reader.readLine();
					if(output != null) System.out.println(output);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
