package application;

import edu.berkeley.boinc.rpc.GlobalPreferences;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class Preferences {

    Stage stage;

    public Preferences() {
        try {
            stage = new Stage();
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("preferences.fxml"));
            loader.setController(new Controller());
            Parent root = loader.load();
            Scene scene = new Scene(root, 487.5, 325);
            scene.getStylesheets().add(getClass().getResource("application.css").toString());
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class Controller implements Initializable {

        @FXML
        private ComboBox workBetween1;
        @FXML
        private ComboBox workBetween2;
        @FXML
        private ComboBox internetBetween1;
        @FXML
        private ComboBox internetBetween2;
        @FXML
        private ComboBox hddSpace;
        @FXML
        private ComboBox cpuPercent;
        @FXML
        private CheckBox workOnBatteries;
        @FXML
        private ComboBox idleTime;

        @Override
        public void initialize(URL location, ResourceBundle resources) {
            GlobalPreferences preferences = Grid.rpcClient.getGlobalPrefsWorkingStruct();
            StringBuilder string = new StringBuilder();
            string.append((int) (preferences.cpu_times.start_hour / 1));
            string.append(":");
            if(preferences.cpu_times.start_hour % 1 == 0)
                string.append("00");
            else
                string.append("30");
            workBetween1.setValue(string.toString());
            string.setLength(0);
            string.append((int) (preferences.cpu_times.end_hour / 1));
            string.append(":");
            if(preferences.cpu_times.end_hour % 1 == 0)
                string.append("00");
            else
                string.append("30");
            workBetween2.setValue(string.toString());
            string.setLength(0);
            string.append((int) (preferences.net_times.start_hour / 1));
            string.append(":");
            if(preferences.net_times.start_hour % 1 == 0)
                string.append("00");
            else
                string.append("30");
            internetBetween1.setValue(string.toString());
            string.setLength(0);
            string.append((int) (preferences.net_times.end_hour / 1));
            string.append(":");
            if(preferences.net_times.end_hour % 1 == 0)
                string.append("00");
            else
                string.append("30");
            internetBetween2.setValue(string.toString());
            if(preferences.disk_max_used_gb == .1) {
                hddSpace.setValue("100MB");
            } else if(preferences.disk_max_used_gb == .25) {
                hddSpace.setValue("250MB");
            } else if(preferences.disk_max_used_gb == .5) {
                hddSpace.setValue("500MB");
            } else if(preferences.disk_max_used_gb == 1) {
                hddSpace.setValue("1GB");
            } else if(preferences.disk_max_used_gb == 2) {
                hddSpace.setValue("2GB");
            } else if(preferences.disk_max_used_gb == 5) {
                hddSpace.setValue("5GB");
            } else if(preferences.disk_max_used_gb == 10) {
                hddSpace.setValue("10GB");
            } else if(preferences.disk_max_used_gb == 25) {
                hddSpace.setValue("25GB");
            } else if(preferences.disk_max_used_gb == 50) {
                hddSpace.setValue("50GB");
            } else if(preferences.disk_max_used_gb == 100) {
                hddSpace.setValue("100GB");
            }
            cpuPercent.setValue((int)(preferences.cpu_usage_limit) + "%");
            workOnBatteries.setSelected(preferences.run_on_batteries);
            if(preferences.idle_time_to_run == 0)
                idleTime.setValue("0 (Run always)");
            else
                idleTime.setValue(String.valueOf((int) preferences.idle_time_to_run));
        }

        @FXML
        public void okClicked(ActionEvent event) {
            GlobalPreferences newPreferences = new GlobalPreferences();
            double time;
            time = Double.parseDouble(String.valueOf(((String)workBetween1.getValue()).substring(0, ((String)workBetween1.getValue()).indexOf(':'))));
            if(((String)workBetween1.getValue()).charAt(((String)workBetween1.getValue()).indexOf(':') + 1) == '3')
                time += .5;
            newPreferences.cpu_times.start_hour = time;
            time = Double.parseDouble(String.valueOf(((String)workBetween2.getValue()).substring(0, ((String) workBetween2.getValue()).indexOf(':'))));
            if(((String)workBetween2.getValue()).charAt(((String)workBetween2.getValue()).indexOf(':') + 1) == '3')
                time += .5;
            newPreferences.cpu_times.end_hour = time;
            time = Double.parseDouble(String.valueOf(((String)internetBetween1.getValue()).substring(0, ((String) internetBetween1.getValue()).indexOf(':'))));
            if(((String)internetBetween1.getValue()).charAt(((String)internetBetween1.getValue()).indexOf(':') + 1) == '3')
                time += .5;
            newPreferences.net_times.start_hour = time;
            time = Double.parseDouble(String.valueOf(((String)internetBetween2.getValue()).substring(0, ((String) internetBetween2.getValue()).indexOf(':'))));
            if(((String)internetBetween2.getValue()).charAt(((String)internetBetween2.getValue()).indexOf(':') + 1) == '3')
                time += .5;
            newPreferences.net_times.end_hour = time;
            if(hddSpace.getValue().equals("100MB")) {
                newPreferences.disk_max_used_gb = .1;
            } else if(hddSpace.getValue().equals("250MB")) {
                newPreferences.disk_max_used_gb = .25;
            } else if(hddSpace.getValue().equals("500MB")) {
                newPreferences.disk_max_used_gb = .5;
            } else if(hddSpace.getValue().equals("1GB")) {
                newPreferences.disk_max_used_gb = 1;
            } else if(hddSpace.getValue().equals("2GB")) {
                newPreferences.disk_max_used_gb = 2;
            } else if(hddSpace.getValue().equals("5GB")) {
                newPreferences.disk_max_used_gb = 5;
            } else if(hddSpace.getValue().equals("10GB")) {
                newPreferences.disk_max_used_gb = 10;
            } else if(hddSpace.getValue().equals("25GB")) {
                newPreferences.disk_max_used_gb = 25;
            } else if(hddSpace.getValue().equals("50GB")) {
                newPreferences.disk_max_used_gb = 50;
            } else if(hddSpace.getValue().equals("100GB")) {
                newPreferences.disk_max_used_gb = 100;
            }
            newPreferences.cpu_usage_limit = Double.parseDouble(((String) cpuPercent.getValue()).substring(0, ((String) cpuPercent.getValue()).indexOf('%')));
            newPreferences.run_on_batteries = workOnBatteries.isSelected();
            if(idleTime.getValue().equals("0 (Run always)"))
                newPreferences.idle_time_to_run = 0;
            else
                newPreferences.idle_time_to_run = Double.parseDouble((String)idleTime.getValue());
            Grid.rpcClient.setGlobalPrefsOverrideStruct(newPreferences);
            Grid.rpcClient.readGlobalPrefsOverride();
            stage.close();
        }

        @FXML
        public void cancelClicked(ActionEvent event) {
            stage.close();
        }
    }
}
