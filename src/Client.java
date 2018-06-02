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
import javafx.scene.text.Font;

public class Client extends Application {
	private static final String EMPTY_STRING = "";
	private PrintWriter toServer;
	private BufferedReader fromServer;

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
		messagePane.setStyle("-fx-border-color: red");
		VBox vbox = new VBox();
		// Avoid the blocking scorll bar
		vbox.prefWidthProperty().bind(primaryStage.widthProperty().subtract(40));
		messagePane.setContent(vbox);
		// ScrollPane auto scroll
		messagePane.vvalueProperty().bind(vbox.heightProperty());
		mainPane.setCenter(messagePane);
		mainPane.setBottom(paneForTextField);

		Scene scene = new Scene(mainPane, 520, 215);
		primaryStage.setTitle("Client");
		primaryStage.setScene(scene);
		primaryStage.show();

		try {
			Socket socket = new Socket("localhost", 8000);
			DataInputStream is = new DataInputStream(socket.getInputStream());
			DataOutputStream os = new DataOutputStream(socket.getOutputStream());
			toServer = new PrintWriter(os);
			fromServer = new BufferedReader(new InputStreamReader(is, "UTF-8"));
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		tf.setOnAction(e -> {
			if (!tf.getText().trim().isEmpty()) {
				String messageToServer = tf.getText().trim().toString();
				Label labelMessage = new Label(messageToServer);
				Label labelTime = new Label(getDateTime());
				labelMessage.setFont(new Font(20));
				labelTime.setFont(new Font(10));

				// Vbox always align to left, add hbox to align right
				HBox hboxMessage = new HBox();
				hboxMessage.setAlignment(Pos.CENTER_RIGHT);
				hboxMessage.getChildren().add(labelMessage);
				HBox hboxTime = new HBox();
				hboxTime.setAlignment(Pos.CENTER_RIGHT);
				hboxTime.getChildren().add(labelTime);

				vbox.getChildren().add(hboxMessage);
				vbox.getChildren().add(hboxTime);
				tf.clear();

				Map<String, String> map = new HashMap<String, String>();
				map.put("message", messageToServer);
				map.put("nowTime", getDateTime());
				JSONObject jsonObject = new JSONObject(map);
				toServer.write(jsonObject.toString() + "\n");
				toServer.flush();
			}
		});

		new Thread(() -> {
			String messageFromServer = null;
			try {
				while (true) {
					messageFromServer = fromServer.readLine();
					JSONObject jsonObject = new JSONObject(messageFromServer);
					Label labelMessage = new Label();
					Label labelTime = new Label();
					updateLabelLater(labelMessage, jsonObject.get("message").toString(), 20);
					updateLabelLater(labelTime, jsonObject.get("nowTime").toString(), 10);
					updateVBoxLater(vbox, labelMessage, labelTime);
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
		}).start();
	}

	// Update without UI thread, if using Platform.runLater in lambda, variables
	// should be final.
	// So using another function to implement Platform.runLater
	public void updateLabelLater(final Label label, final String text, int size) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				label.setText(text);
				label.setFont(new Font(size));
			}
		});
	}

	public String getDateTime() {
		SimpleDateFormat sdFormat = new SimpleDateFormat("MM/dd hh:mm");
		Date date = new Date();
		String strDate = sdFormat.format(date);
		return strDate;
	}

	public void updateVBoxLater(final VBox vbox, final Label label1, final Label label2) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				vbox.getChildren().add(label1);
				vbox.getChildren().add(label2);
			}
		});
	}

	public static void main(String[] args) {
		launch(args);
	}
}