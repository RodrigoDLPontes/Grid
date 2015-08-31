package grid;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.berkeley.boinc.rpc.RpcClient;
import edu.berkeley.boinc.utils.Logging;
import javafx.application.Application;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

public class Grid extends Application {

    static boolean useDebugConsole = false;
    static RpcClient rpcClient;
    static File projectsFolder;

    public static void main(String args[]) {
        if (Arrays.asList(args).contains("-dc")) {
            useDebugConsole = true;
        }
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        initialConfiguration();
        if (projectsFolder.getParentFile().exists() && projectsFolder.exists()) {
            if (Logging.INFO) System.out.println("GRID: Program data folder found");
            ArrayList<String> list = new ArrayList<String>(Arrays.asList(projectsFolder.list()));
            list.remove("placeholder.txt");
            list.remove("virtualbox");
            list.remove(".DS_Store");
            if (list.size() != 0) {
                if (Logging.INFO) System.out.println("GRID: Found projects, initializing...");
                if (initiateBoincRpc()) {
                    new MainWindow();
                }
            } else {
                //TODO: User has already used BOINC, so we shouldn't use Register again
                if (Logging.INFO) System.out.println("GRID: No projects found, prompting register...");
                new Register();
            }
        } else {
            if (Logging.INFO) System.out.println("GRID: No program data folder found, prompting register...");
            projectsFolder.getParentFile().mkdirs();
            new Register();
        }
    }

    /**
     * Performs any initial setup
     */
    private static void initialConfiguration() {
        if (useDebugConsole) {
            new DebugConsole();
            System.setOut(new PrintStream(new DebugConsole.DebugConsolePrintStream()));
            System.setErr(new PrintStream(new DebugConsole.DebugConsolePrintStream()));
        }
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            projectsFolder = new File("C:\\ProgramData\\BOINC\\projects");
        } else if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            projectsFolder = new File("/Library/Application Support/BOINC Data/projects");
        }
    }

    /**
     * Starts and connects to BOINC
     *
     * @return True for success, false otherwise
     */
    public static boolean initiateBoincRpc() {
        rpcClient = new RpcClient();
        ProcessBuilder boincBuilder;
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            boincBuilder = new ProcessBuilder("C:\\Program Files (x86)\\Grid\\BOINC\\boinc.exe");
        } else if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            boincBuilder = new ProcessBuilder("/Library/Application Support/Grid/BOINC/boinc", "--dir", "/Library/Application Support/BOINC Data/");
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setContentText("Could not find BOINC Client, please try again.");
            alert.showAndWait();
            return false;
        }
        try {
            Process boinc = boincBuilder.start();
            if (Logging.ERROR) {
                Thread boincOutput = new Thread(new BoincOutput(boinc));
                boincOutput.setDaemon(true);
                boincOutput.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        int failCount = 0;
        while (!rpcClient.open("localhost", 31416)) {
            failCount++;
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (failCount > 50) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setContentText("Failed to connect to BOINC Client, please try again.");
                alert.showAndWait();
                return false;
            }
        }
        failCount = 0;
        File guiRpcAuth;
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            guiRpcAuth = new File("C:\\ProgramData\\BOINC\\gui_rpc_auth.cfg");
        } else if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            guiRpcAuth = new File("/Library/Application Support/BOINC Data/gui_rpc_auth.cfg");
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setContentText("Could not find BOINC Client, please try again.");
            alert.showAndWait();
            return false;
        }
        while (!guiRpcAuth.exists()) {
            failCount++;
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (failCount > 50) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setContentText("Failed to find BOINC's authorization file, please try again.");
                alert.showAndWait();
                return false;
            }
        }
        failCount = 0;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(guiRpcAuth));
            String auth = reader.readLine();
            while (!rpcClient.authorize(auth)) {
                failCount++;
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (failCount > 50) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setContentText("Failed to authorize with BOINC, please try again.");
                    alert.showAndWait();
                    return false;
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
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
            while (true) {
                try {
                    //Sleep to avoid hogging system
                    Thread.sleep(1000);
                    output = reader.readLine();
                    while(output != null) {
                        System.out.println(output);
                        output = reader.readLine();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
