import java.io.*;
import java.net.*;
import java.util.Date;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

public class Server extends Application {
	private int sessionNo;

	@Override
	public void start(Stage primaryStage) {

		TextArea ta = new TextArea();
		Scene scene = new Scene(new ScrollPane(ta), 450, 200);
		primaryStage.setTitle("Server");
		primaryStage.setScene(scene);
		primaryStage.show();

		new Thread(() -> {
			try {
				ServerSocket serverSocket = new ServerSocket(8000);
				Platform.runLater(() -> ta.appendText("Server started at " + new Date() + '\n'));

				while (true) {
					Platform.runLater(
							() -> ta.appendText(new Date() + ": Wait for players to join session " + sessionNo + '\n'));
					Socket User1 = serverSocket.accept();

					Platform.runLater(() -> {
						ta.appendText(new Date() + ": User1 joined session " + sessionNo + '\n');
						ta.appendText("User1's IP address" + User1.getInetAddress().getHostAddress() + '\n');
					});

					Socket User2 = serverSocket.accept();

					Platform.runLater(() -> {
						ta.appendText(new Date() + ": User2 joined session " + sessionNo + '\n');
						ta.appendText("User2's IP address" + User1.getInetAddress().getHostAddress() + '\n');
					});

					new Thread(new HandleASession(User1, User2)).start();
					sessionNo++;
				}

			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}).start();
	}

	public static void main(String[] args) {
		launch(args);
	}

	private class HandleASession implements Runnable {
		private Socket User1, User2;

		public HandleASession(Socket User1, Socket User2) {
			this.User1 = User1;
			this.User2 = User2;
		}

		@Override
		public void run() {
			try {
				DataInputStream inputFromUser1 = new DataInputStream(User1.getInputStream());
				DataOutputStream outputToUser1 = new DataOutputStream(User1.getOutputStream());
				DataInputStream inputFromUser2 = new DataInputStream(User2.getInputStream());
				DataOutputStream outputToUser2 = new DataOutputStream(User2.getOutputStream());

				new Thread(() -> {
					String messageUser1;
					try {
						while (true) {
							messageUser1 = inputFromUser1.readUTF();
							outputToUser2.writeUTF(messageUser1);
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}).start();

				new Thread(() -> {
					String messageUser2;
					try {
						while (true) {
							messageUser2 = inputFromUser2.readUTF();
							outputToUser1.writeUTF(messageUser2);
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}).start();

			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

	}
}