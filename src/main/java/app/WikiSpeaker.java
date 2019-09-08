package app;

import controllers.Controller;
import javafx.application.Application;

import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class WikiSpeaker extends Application {
    private static Stage _mainStage = new Stage();
    
    public static Stage getMainStage(){
        return _mainStage;
    }

    @Override
    public void start(Stage mainStage) throws IOException {
        _mainStage = mainStage;
        _mainStage.setTitle("Wiki Speaker");

        //Load Home page
        Controller controller = new Controller();
        controller.changeScene("/app/Home.fxml");
    }



    /**
     * Does a bash command, and returns the standard output of that command
     * @param cmd
     */
    public static String doProcess(ProcessBuilder builder)  {
    	Process process;
    	String output = "";
		try {
			process = builder.start();
			process.waitFor();
    	
	    	InputStream stdout = process.getInputStream();
	    	BufferedReader stdoutBuffered = new BufferedReader(new InputStreamReader(stdout));
	    	String line = null;
	    	
    	while ((line = stdoutBuffered.readLine()) != null) {
    		output = output + line + "\n";
    		}
    	
    	} catch (IOException e) {
    		e.printStackTrace();
    	} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return output;
    }
    
    
    
    public static void main(String[] args) {
    	//Make directories for storing files made

    	doProcess(new ProcessBuilder("/bin/bash", "-c", "mkdir -p ./TempFiles >/dev/null"));
    	doProcess(new ProcessBuilder("/bin/bash", "-c", "mkdir -p ./Creations >/dev/null"));
    	launch(args);
    }

}

