package controllers;


import app.WikiSpeaker;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.text.Text;

public class ListMenuController extends Controller {

    @FXML Button mainMenuButton;
    @SuppressWarnings("rawtypes")
	@FXML ListView creationList;
    @FXML Text text;
    @FXML Button playButton;
    @FXML Button deleteButton;
    private boolean confirmDelete;
    private String previousSelection;
    private String _selection;
    
    @FXML
    public void handleMainMenuButton(){
        //Go back to main menu
        changeScene("/app/Home.fxml");
    }

    @FXML
    public void handlePlayButton(){
        String selection = (getSelectedCreation());
        if (selection == null) {
            playButton.setDisable(true);
            deleteButton.setDisable(true);
        }else {
        	_selection = selection;
        	Thread thread = new Thread(new playVidInBackground());
        	thread.start();
        	
        }
    }
    
   
    
    /**
     * Plays the video on a worker thread using ffplay
     *
     */
    private class playVidInBackground extends Task<Void> {

        @Override
        protected Void call() throws Exception {
        	String file = "./Creations/" + _selection;
        	ProcessBuilder playCreation = new ProcessBuilder("bash", "-c", "ffplay -autoexit " + file);
        	WikiSpeaker.doProcess(playCreation);            
        	return null;
        }

    }
    
    
   
    
    
    @FXML
    public void handleDeleteButton(){
        String selection = (getSelectedCreation());
        
        if (selection == null) {
            playButton.setDisable(true);
            deleteButton.setDisable(true);
        }else if (confirmDelete){
        	String file = "./Creations/" + selection;
        	ProcessBuilder deleteCreation = new ProcessBuilder("bash", "-c", "rm -f " + file);
        	WikiSpeaker.doProcess(deleteCreation);
        	
        	changeScene("/app/ListMenu.fxml"); //reload scene to rebuild the list of Creations after deletion
        }else {
        	deleteButton.setText("Really DELETE?");
        	confirmDelete = true;
        	previousSelection = selection;
        }
            	
    }
    /**
     * Gets the selected String from the listView
     * @return Selected String
     */
    public String getSelectedCreation(){
        return (String) creationList.getSelectionModel().getSelectedItem();
    }

    @FXML
    public void listClicked(){
        playButton.setDisable(false);
        deleteButton.setDisable(false);
        
        //If another creation is clicked while in the confirmDelete stage, revert the confirmDelete
        if(!(getSelectedCreation()==(previousSelection))) {
        	confirmDelete = false;
        	deleteButton.setText("Delete Creation");
        }
    }

    @FXML
    private void initialize() {
        //Preload things before the scene
        getCreationsList();
        confirmDelete = false;
    }

    @SuppressWarnings("unchecked")
	public void getCreationsList(){
    	
    	ProcessBuilder listCreations = new ProcessBuilder("bash", "-c", "ls ./Creations");
    	String string = WikiSpeaker.doProcess(listCreations);
    
        if((string == null)||(string.isEmpty())) {
            text.setText("No creations avaliable.");

        }else{
        	String[] list = string.split("\n"); //Split string into list array
            text.setText(list.length + " creation(s) avaliable.");

            //Display items in the listView
            for (int i = 0; i < list.length; i++) {
                creationList.getItems().add(list[i]);
            }

        }
    }



}
