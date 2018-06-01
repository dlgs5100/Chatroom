import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Date;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;


public class Client extends Application {

	PrintWriter toServer;
	BufferedReader fromServer;

	@Override
	public void start(Stage primaryStage) throws IOException {
		BorderPane paneForTextField = new BorderPane();
		paneForTextField.setPadding(new Insets(5, 5, 5, 5));
		paneForTextField.setStyle("-fx-border-color: green");
		paneForTextField.setLeft(new Label("Enter a message: "));

		TextField tf = new TextField();
		tf.setAlignment(Pos.BOTTOM_RIGHT);
		paneForTextField.setCenter(tf);

		BorderPane mainPane = new BorderPane();

		ScrollPane messagePane = new ScrollPane();
		VBox vbox = new VBox();
		vbox.prefWidthProperty().bind(primaryStage.widthProperty().subtract(40));
		messagePane.setContent(vbox);
		mainPane.setCenter(new ScrollPane(vbox));
		mainPane.setBottom(paneForTextField);

		Scene scene = new Scene(mainPane, 520, 215);
		primaryStage.setTitle("Client");
		primaryStage.setScene(scene);
		primaryStage.show();

		try {
			Socket socket = new Socket("localhost", 8000);
			DataInputStream is = new DataInputStream(socket.getInputStream());
			DataOutputStream os = new DataOutputStream(socket.getOutputStream());
			toServer= new PrintWriter(os);
			fromServer = new BufferedReader(new InputStreamReader(is, "UTF-8"));
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		tf.setOnAction(e -> {
			String messageToServer = tf.getText().trim().toString();
			Label label = new Label(messageToServer);
			HBox hbox = new HBox();
			hbox.getChildren().add(label);
			hbox.setAlignment(Pos.CENTER_RIGHT);
			vbox.getChildren().add(hbox);
			tf.clear();
			
			Map<String, String> map = new HashMap<String, String>();
			map.put("message", messageToServer);
			map.put("nowTime", getDateTime());
			JSONObject jsonObject = new JSONObject(map);
			toServer.write(jsonObject.toString()+"\n");
			toServer.flush();
		});
		
		new Thread(() -> {
			String messageFromServer = null;
			try {
				while (true) {
					messageFromServer = fromServer.readLine();
					JSONObject jsonObject = new JSONObject(messageFromServer);
					Label label = new Label();
					updateLabelLater(label,jsonObject.get("message").toString());
					updateVBoxLater(vbox,label);
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
		}).start();
	}
	public void updateLabelLater(final Label label, final String text) {
        Platform.runLater(new Runnable() {
            @Override public void run() {
                label.setText(text);
            }
        });
    }
	public void updateVBoxLater(final VBox vbox, final Label label) {
        Platform.runLater(new Runnable() {
            @Override public void run() {
                vbox.getChildren().add(label);
            }
        });
    }
	public static void main(String[] args) {
		launch(args);
	}
	public String getDateTime(){
		SimpleDateFormat sdFormat = new SimpleDateFormat("MM/dd hh:mm");
		Date date = new Date();
		String strDate = sdFormat.format(date);
		return strDate;
	}
}