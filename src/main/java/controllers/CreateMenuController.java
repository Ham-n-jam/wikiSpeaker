package controllers;

import java.io.File;
import java.io.IOException;

import app.WikiSpeaker;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Text;

public class CreateMenuController extends Controller {

    @FXML protected Text headerText;
    @FXML private Button mainMenuButton;
    @FXML private TextArea searchTextArea;
    @FXML private TextArea bottomTextArea;
    @FXML private Button overwriteButton;
    private int _numLines;
    private String _search;
    private String _videoName;
    private String _lines;
    private static creationStep _step = creationStep.enterSearch;

    private enum creationStep {
        enterSearch, enterLinesNum, enterCreationName, creatingCreation;
    }


    @FXML
    public void handleMainMenuButton() {
        //Reset step for next time
        _step = creationStep.enterSearch;
        //Back to main menu
        changeScene("/app/Home.fxml");
    }

    @FXML
    public void handleOverwriteButton(){
        //OVERWRITE

    	String file1 = "./Creations/" + _videoName + ".mp4";
    	String file2 = "./TempFiles/temp.mp3";
    	String file3 = "./TempFiles/temp.mp4";
    	ProcessBuilder deleteCreation = new ProcessBuilder("bash", "-c", "rm -f " + file1);
    	ProcessBuilder deleteMP3 = new ProcessBuilder("bash", "-c", "rm -f " + file2);
    	ProcessBuilder deleteMP4 = new ProcessBuilder("bash", "-c", "rm -f " + file3);
    	WikiSpeaker.doProcess(deleteCreation);
    	WikiSpeaker.doProcess(deleteMP3);
    	WikiSpeaker.doProcess(deleteMP4);
    	
        _step = creationStep.creatingCreation;
        overwriteButton.setVisible(false);
        overwriteButton.setDisable(true);

        searchTextArea.setVisible(false);
        searchTextArea.setDisable(true);
        //Start creating Creation
        headerText.setText("Creating Creation...");
        Thread thread = new Thread(new CreateCreationInBackground(this));
        thread.start();
        


    }

    private class CreateCreationInBackground extends Task<Void> {
    	
    	private CreateMenuController controller;
    	
    	public CreateCreationInBackground(CreateMenuController controller) {
    		this.controller = controller;
    	}
    	
    	
    	@Override
		protected Void call() throws Exception {
    		//Make MP3 file
        	controller.headerText.setText("Creating audio...");
        	String cmd = "text2wave ./TempFiles/final.txt | ffmpeg -f mp3 ./TempFiles/temp.mp3 -i pipe:0 &>/dev/null";
        	ProcessBuilder makeMP3 = new ProcessBuilder("bash", "-c", cmd);
        	WikiSpeaker.doProcess(makeMP3);
        	
        	//Make a video the same duration as the MP3
        	controller.headerText.setText("Creating video...");
        	String video = "LENGTH=$(ffprobe -i ./TempFiles/temp.mp3 -show_entries format=duration -v quiet -of csv=\"p=0\")\r\n" + 
        			"		ffmpeg -y -f lavfi -i color=c=red:s=320x240:d=$LENGTH -vf \"drawtext=fontfile=./font.ttf:fontsize=30: fontcolor=white:x=(w-text_w)/2:y=(h-text_h)/2:text='" + _search + "'\" \"./TempFiles/temp.mp4\" &>/dev/null";
        	ProcessBuilder makeVid = new ProcessBuilder("bash", "-c", video);
        	WikiSpeaker.doProcess(makeVid);
        	
        	//Combine the vid with the MP3
    		String combine = "ffmpeg -y -i ./TempFiles/temp.mp4 -i ./TempFiles/temp.mp3 -c copy -map 0:v:0 -map 1:a:0 ./Creations/" + _videoName + ".mp4 &>/dev/null";
    		ProcessBuilder combineVidAudio = new ProcessBuilder("bash", "-c", combine);
        	WikiSpeaker.doProcess(combineVidAudio);
        	
        	
        	//Remove temp files after creation is made
        	String remove = "rm -f ./TempFiles/temp.mp3" + 
        			"		rm -f ./TempFiles/temp.mp4";
        	ProcessBuilder removeTemps = new ProcessBuilder("bash", "-c", remove);
        	WikiSpeaker.doProcess(removeTemps);
			
        	return null;
    	}
    	
    	@Override
        protected void done(){
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                	controller.finshedMakingCreation(); 
                }
            });
    	}
    	
    }
    
    public void finshedMakingCreation() {
        headerText.setText("Creation Created!\nEnter a new search term:");
        bottomTextArea.setVisible(false); //hide bottom text
        searchTextArea.setVisible(true);
        searchTextArea.setDisable(false);
        _step = creationStep.enterSearch;
    }

    /**
     This method handles the main textArea and uses the entered text in different ways when the enter key is hit
     depending on what the current _step value is.
     */
    @FXML
    public void handleSearchAreaText() {
        searchTextArea.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                //Save the search term when the enter key is hit
                if (keyEvent.getCode() == KeyCode.ENTER) {



                    //If using the textarea for entering the search term
                    if(_step.equals(creationStep.enterSearch)) {
                        //Check for valid input here
                        if (!((searchTextArea.getText() == "\\n") || (searchTextArea.getText() == ""))) {
                            String searchTerm = searchTextArea.getText();
                            searchTerm = searchTerm.replaceAll("[\\n\\t]", ""); //Remove tabs and newlines
                            searchTextArea.setText(searchTerm); //remove the newline made from hitting enter from display

                            //Search the wiki for the entered text using wikit
                            headerText.setText("Searching " + searchTerm + "...");
                            wikitSearch(searchTerm);

                        }
                        searchTextArea.setText("");
                    }


                    //If using the textarea for entering the number of lines
                    else if(_step.equals(creationStep.enterLinesNum)) {
                        String numLines = searchTextArea.getText();
                        numLines = numLines.replaceAll("[^0-9]",""); //Remove everything other than numbers
                        searchTextArea.setText(numLines); //update textarea

                        if(numLines.equalsIgnoreCase("")){ //Avoid .parseInt errors if string is empty
                            headerText.setText("Enter a valid number of lines to be read in the Creation (between 1 and " + _numLines + "):");
                            searchTextArea.setText("");
                            //Check number of lines entered is a valid number
                        } else if((Integer.parseInt(numLines) < 1) || (Integer.parseInt(numLines) > _numLines)){
                            headerText.setText("Enter a valid number of lines to be read in the Creation (between 1 and " + _numLines + "):");
                            searchTextArea.setText("");
                        }else {
                            _numLines = Integer.parseInt(numLines);

                            
                            
                            //Save specified lines into final.txt
                            String cmd = "head -n " + _numLines + " ./TempFiles/temp.txt > ./TempFiles/final.txt";
                            ProcessBuilder saveLines = new ProcessBuilder("bash", "-c", cmd);
                        	WikiSpeaker.doProcess(saveLines);
                        	
                            
                            
                            headerText.setText(numLines + " lines chosen. Enter a name for your Creation:");
                            searchTextArea.setText("");

                            //Move on to entering a name for creation step
                            _step = creationStep.enterCreationName;
                        }
                    }

                    //If using the textarea for entering the name of the Creation
                    else if(_step.equals(creationStep.enterCreationName)) {
                        String creationName = searchTextArea.getText();
                        creationName = creationName.replaceAll("[!@#$%^&*()<>?:\\s]",""); //Remove invalid characters
                        searchTextArea.setText(creationName); //update textarea
                       
                        //Check if a creation by that name already exists
                    	boolean check = new File("./Creations/" + creationName + ".mp4").exists();
                        
                        if(check){
                            headerText.setText( creationName + " is already in use. Please type a new one, or click overwrite:");
                            overwriteButton.setVisible(true);
                            overwriteButton.setDisable(false);
                        }else{
                        	_videoName = creationName;
                            //Start making the creation;
                            handleOverwriteButton();//Disable overwrite button and start making creation
                        }
                    	_videoName = creationName;


                    }


                }
            }
        });
    }


    private void wikitSearch(String search){
        _search = search;        
        Thread thread = new Thread(new wikitInBackground(this));
    	thread.start();
    }
    
    public void wikitSearchComplete() {
        String lines = _lines;
    
        
    	//Split the output into lines
    	lines = lines.replace(". ", ".\n");
    	

        if (lines.contains("not found :^(")){
            headerText.setText("No results found for" + _search + ". Try another search:");
        }else{
            displayWikiLines(lines);
            _numLines = countLines(lines);
            
           
            //Save lines to file
           try {
			new ProcessBuilder("echo", lines).redirectOutput(new File("./TempFiles/temp.txt")).start();
           } catch (IOException e) {}
        	
        	            
            
            headerText.setText("Enter the number of lines to be read in the Creation (between 1 and " + _numLines + "):");
            _step = creationStep.enterLinesNum;

        }
    }
    
    
    private class wikitInBackground extends Task<Void> {
    	private CreateMenuController controller;
    	
    	public wikitInBackground(CreateMenuController controller) {
    		this.controller = controller;
    	}
    	
        @Override
        protected Void call() throws Exception {
        	String searchWiki = "wikit " + _search;
            ProcessBuilder wikit = new ProcessBuilder("bash", "-c", searchWiki);
        	controller._lines = WikiSpeaker.doProcess(wikit);
        	return null;
        }
        
        @Override
        protected void done(){
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                	controller.wikitSearchComplete(); 
                }
            });
        }

    }
    
    

    //Activate and display text on the bottom text box
    private void displayWikiLines(String lines) {
        bottomTextArea.setVisible(true);
        bottomTextArea.setText(lines);

    }

    private int countLines(String text){
        String[] lines = text.split("\r\n|\r|\n");
        return  lines.length;
    }



}
