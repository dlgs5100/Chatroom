package application;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import javafx.scene.control.Label;

public class ChatroomLayoutModel {
	
	private ChatroomLayoutController controller;
	private PrintWriter toServer;
	private BufferedReader fromServer;
	
	public void setConnection() {
		try {
			Socket socket = new Socket("localhost", 8000);
			DataInputStream is = new DataInputStream(socket.getInputStream());
			DataOutputStream os = new DataOutputStream(socket.getOutputStream());
			toServer = new PrintWriter(os);
			fromServer = new BufferedReader(new InputStreamReader(is, "UTF-8"));
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		
		new Thread(() -> {
			String messageFromServer = null;
			try {
				while (true) {
					messageFromServer = fromServer.readLine();
					JSONObject jsonObject = new JSONObject(messageFromServer);
					controller.updateLabelLater(jsonObject.get("message").toString(), 20, jsonObject.get("nowTime").toString(), 10);
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
		}).start();
    }
	public void setController(ChatroomLayoutController controller) {
		this.controller = controller;
	}
	public void sendServer(JSONObject jsonObject) {
		toServer.write(jsonObject.toString() + "\n");
		toServer.flush();
	}

}
