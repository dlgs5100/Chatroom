import java.io.*;
import java.net.*;
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

		ScrollPane messagePane = new ScrollPane();
		VBox vbox = new VBox();
		vbox.prefWidthProperty().bind(primaryStage.widthProperty().subtract(20));
		messagePane.setContent(vbox);
		mainPane.setCenter(new ScrollPane(vbox));
		mainPane.setBottom(paneForTextField);

		Scene scene = new Scene(mainPane, 520, 215);
		primaryStage.setTitle("Client");
		primaryStage.setScene(scene);
		primaryStage.show();

		try {
			Socket socket = new Socket("localhost", 8000);
			fromServer = new DataInputStream(socket.getInputStream());
			toServer = new DataOutputStream(socket.getOutputStream());
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		tf.setOnAction(e -> {
			try {
				String messageToServer = tf.getText().trim().toString();
				Label label = new Label(messageToServer);
				HBox hbox = new HBox();
				hbox.getChildren().add(label);
				hbox.setAlignment(Pos.CENTER_RIGHT);
				vbox.getChildren().add(hbox);
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
					Label label = new Label();
					updateLabelLater(label,messageFromServer);
					updateVBoxLater(vbox,label);
				}
			} catch (IOException ex) {
				ex.printStackTrace();
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
            	//vbox.setAlignment(Pos.BASELINE_LEFT);
                vbox.getChildren().add(label);
            }
        });
    }
	public static void main(String[] args) {
		launch(args);
	}
}