package application;
import javafx.fxml.FXML;

import javafx.scene.control.Button;

import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LogInLayoutController {
	@FXML
	private Button btnSignIn;
	@FXML
	private Button btnAnonymous;
	@FXML
	private TextField tfAccount;
	@FXML
	private TextField tfPassword;
	
	private Stage stage;
	private ClientLogIn clientLogIn;
	
	@FXML
	private void handleAnonymous() {
		try {  
            Client main = new Client();  
            main.start(new Stage()); 
            stage.hide();
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
	}
	@FXML
	private void handleSignIn() {
		try {  
            Client main = new Client();  
            main.start(new Stage()); 
            stage.hide();
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
	}
	

	public void setStage(Stage stage) {
        this.stage = stage;
    }
}
