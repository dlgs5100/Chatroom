import java.io.*;
import java.net.*;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class Client extends Application {
	DataOutputStream toServer = null;
	DataInputStream fromServer = null;

	@Override
	public void start(Stage primaryStage) {
		BorderPane paneForTextField = new BorderPane();
		paneForTextField.setPadding(new Insets(5, 5, 5, 5));
		paneForTextField.setStyle("-fx-border-color: green");
		paneForTextField.setLeft(new Label("Enter a message: "));

		TextField tf = new TextField();
		tf.setAlignment(Pos.BOTTOM_RIGHT);
		paneForTextField.setCenter(tf);

		BorderPane mainPane = new BorderPane();

		TextArea ta = new TextArea();
		mainPane.setCenter(new ScrollPane(ta));
		mainPane.setTop(paneForTextField);


		Scene scene = new Scene(mainPane, 450, 200);
		primaryStage.setTitle("Client"); 
		primaryStage.setScene(scene);
		primaryStage.show();

		try {
			Socket socket = new Socket("localhost", 8000);
			fromServer = new DataInputStream(socket.getInputStream());
			toServer = new DataOutputStream(socket.getOutputStream());
		} catch (IOException ex) {
			ta.appendText(ex.toString() + '\n');
		}

		tf.setOnAction(e -> {
			try {
				String messageToServer = tf.getText().trim().toString();
				tf.clear();
				toServer.writeUTF(messageToServer);
				toServer.flush();

			} catch (IOException ex) {
				System.err.println(ex);
			}
		});

		new Thread(() -> {
			String messageFromServer = null;
			try {
				while (true) {
					messageFromServer = fromServer.readUTF();
					ta.appendText(messageFromServer + "\n");
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}).start();

	}

	public static void main(String[] args) {
		launch(args);
	}
}