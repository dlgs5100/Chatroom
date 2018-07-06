package application;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.imageio.ImageIO;
import javax.swing.filechooser.FileSystemView;
import javax.xml.bind.DatatypeConverter;
import org.json.JSONObject;
import org.json.JSONException;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.scene.control.ScrollPane;

public class ChatroomLayoutController {
	@FXML
	private Button btnSelect;
	@FXML
	private TextField tfEnterMessage;
	@FXML
	private ScrollPane messagePane;
	
	private Stage stage;
	private ChatroomLayoutModel model;
	private VBox vbox = new VBox();
	private String imageFormat;
	private File file;
	private Canvas canvas;
	private WritableImage image;
	private GraphicsContext gc;
	private ByteArrayOutputStream baos;
	
	private static int MESSAGE_SIZE = 20;
	private static int TIME_SIZE = 10;
	private static int IMAGE_RATIO = 5;
	
	private String userName = "<Me>";
	private String targetName = "<Anonymous>";

	@FXML
	public void handleBtnSelect(ActionEvent event) {
		FileChooser fchooser = new FileChooser();
		fchooser.setTitle("Open Resource File");
		fchooser.getExtensionFilters().addAll(
			    new FileChooser.ExtensionFilter("All Images", "*.*"),
			    new FileChooser.ExtensionFilter("JPG", "*.jpg"),
			    new FileChooser.ExtensionFilter("GIF", "*.gif"),
			    new FileChooser.ExtensionFilter("BMP", "*.bmp"),
			    new FileChooser.ExtensionFilter("PNG", "*.png")
			);
		
		file = fchooser.showOpenDialog(null);
		//Determine the file format
		imageFormat = FileSystemView.getFileSystemView().getSystemTypeDescription(file).substring(0, 3);
		
		try{
			BufferedImage bufferedImage = ImageIO.read(file);
			//Image ByteArrayOutputStream
			baos = new ByteArrayOutputStream();
		    ImageIO.write(bufferedImage, imageFormat, baos);
		    
			image = SwingFXUtils.toFXImage(bufferedImage, null);
			image.isPreserveRatio();
			
		}catch(IOException ex){
			ex.printStackTrace();
		}
		//Show name myself
		Label labelName = new Label(userName);
		labelName.setFont(new Font(MESSAGE_SIZE));
		HBox hboxName = new HBox();
		hboxName.setAlignment(Pos.CENTER_RIGHT);
		hboxName.setPadding(new Insets(10, 0, 5, 0));	//(Up, Right, Down, Left)
		hboxName.getChildren().add(labelName);
		//Show image myself
		canvas = new Canvas(image.getWidth()/IMAGE_RATIO,image.getHeight()/IMAGE_RATIO);
		gc = canvas.getGraphicsContext2D();
		gc.drawImage(image,0,0,image.getWidth()/IMAGE_RATIO,image.getHeight()/IMAGE_RATIO);
		HBox hboxImage = new HBox();
		hboxImage.setAlignment(Pos.CENTER_RIGHT);
		hboxImage.setPadding(new Insets(0, 50, 0, 0));
		hboxImage.getChildren().add(canvas);
		//Show time myself
		Label labelTime = new Label(getDateTime());
		labelTime.setFont(new Font(TIME_SIZE));
		HBox hboxTime = new HBox();
		hboxTime.setAlignment(Pos.CENTER_RIGHT);
		hboxTime.setPadding(new Insets(0, 10, 10, 0));
		hboxTime.getChildren().add(labelTime);
	
		vbox.getChildren().add(hboxName);
		vbox.getChildren().add(hboxImage);
		vbox.getChildren().add(hboxTime);
		
		JSONObject jsonObject = new JSONObject();
		try {
			//Json doesn't support byte array, so encode to Base64
			jsonObject.put("image", DatatypeConverter.printBase64Binary(baos.toByteArray()));
			jsonObject.put("nowTime", getDateTime());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		model.sendServer(jsonObject);
	}

	@FXML
	public void handleEnterMessage(ActionEvent event) {
		if (!tfEnterMessage.getText().trim().isEmpty()) {
			String messageToServer = tfEnterMessage.getText().trim().toString();
			
			// Vbox always align to left, add hbox to align right
			//Show name myself
			Label labelName = new Label(userName);
			labelName.setFont(new Font(MESSAGE_SIZE));
			HBox hboxName = new HBox();
			hboxName.setAlignment(Pos.CENTER_RIGHT);
			hboxName.setPadding(new Insets(10, 0, 5, 0));	//(Up, Right, Down, Left)
			hboxName.getChildren().add(labelName);
			//Show message myself
			Label labelMessage = new Label(messageToServer);
			labelMessage.setFont(new Font(MESSAGE_SIZE));
			HBox hboxMessage = new HBox();
			hboxMessage.setAlignment(Pos.CENTER_RIGHT);
			hboxMessage.setPadding(new Insets(0, 50, 0, 0));
			hboxMessage.getChildren().add(labelMessage);
			//Show time myself
			Label labelTime = new Label(getDateTime());
			labelTime.setFont(new Font(TIME_SIZE));
			HBox hboxTime = new HBox();
			hboxTime.setAlignment(Pos.CENTER_RIGHT);
			hboxTime.setPadding(new Insets(0, 10, 10, 0));
			hboxTime.getChildren().add(labelTime);

			vbox.getChildren().add(hboxName);
			vbox.getChildren().add(hboxMessage);
			vbox.getChildren().add(hboxTime);
			tfEnterMessage.clear();

			JSONObject jsonObject = new JSONObject();
			try {
				jsonObject.put("message", messageToServer);
				jsonObject.put("nowTime", getDateTime());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			model.sendServer(jsonObject);
		}
	}
	public void setScroll() {
		// Avoid the blocking scorll bar
		vbox.prefWidthProperty().bind(stage.widthProperty().subtract(40));
		messagePane.setContent(vbox);
		// ScrollPane auto scroll
		messagePane.vvalueProperty().bind(vbox.heightProperty());
	}
	public void updateLabelLater(final String message, final String time) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				Label labelName = new Label(targetName);
				labelName.setFont(new Font(MESSAGE_SIZE));
				HBox hboxName = new HBox();
				hboxName.setAlignment(Pos.CENTER_LEFT);
				hboxName.setPadding(new Insets(10, 0, 5, 0));	//(Up, Right, Down, Left)
				hboxName.getChildren().add(labelName);
				
				Label labelMessage = new Label(message);
				labelMessage.setFont(new Font(MESSAGE_SIZE));
				HBox hboxMessage = new HBox();
				hboxMessage.setAlignment(Pos.CENTER_LEFT);
				hboxMessage.setPadding(new Insets(0, 0, 0, 50));
				hboxMessage.getChildren().add(labelMessage);
				
				Label labelTime = new Label(time);
				labelTime.setFont(new Font(TIME_SIZE));
				HBox hboxTime = new HBox();
				hboxTime.setAlignment(Pos.CENTER_LEFT);
				hboxTime.setPadding(new Insets(0, 0, 10, 10));
				hboxTime.getChildren().add(labelTime);

				vbox.getChildren().add(hboxName);
				vbox.getChildren().add(hboxMessage);
				vbox.getChildren().add(hboxTime);
			}
		});
	}
	public void updateImageLater(final String imageData, final String time) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				//Decode from Base64
				byte[] base64Decoded = DatatypeConverter.parseBase64Binary(imageData);			
				try {
					BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(base64Decoded));
					//A temp ByteArrayOutputStream(unuse)
					baos = new ByteArrayOutputStream();
					ImageIO.write(bufferedImage, "jpg", baos);
				    
					image = SwingFXUtils.toFXImage(bufferedImage, null);
					image.isPreserveRatio();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				canvas = new Canvas(image.getWidth()/IMAGE_RATIO,image.getHeight()/IMAGE_RATIO);
				gc = canvas.getGraphicsContext2D();
				gc.drawImage(image,0,0,image.getWidth()/IMAGE_RATIO,image.getHeight()/IMAGE_RATIO);
				HBox hboxImage = new HBox();
				hboxImage.setAlignment(Pos.CENTER_LEFT);
				hboxImage.setPadding(new Insets(0, 0, 0, 50));
				hboxImage.getChildren().add(canvas);
				
				Label labelName = new Label(targetName);
				labelName.setFont(new Font(MESSAGE_SIZE));
				HBox hboxName = new HBox();
				hboxName.setAlignment(Pos.CENTER_LEFT);
				hboxName.setPadding(new Insets(10, 0, 5, 0));	//(Up, Right, Down, Left)
				hboxName.getChildren().add(labelName);
				
				Label labelTime = new Label(time);
				labelTime.setFont(new Font(TIME_SIZE));
				HBox hboxTime = new HBox();
				hboxTime.setAlignment(Pos.CENTER_LEFT);
				hboxTime.setPadding(new Insets(0, 0, 10, 10));
				hboxTime.getChildren().add(labelTime);
				
				vbox.getChildren().add(hboxName);
				vbox.getChildren().add(hboxImage);
				vbox.getChildren().add(hboxTime);
			}
		});
	}
	public String getDateTime() {
		SimpleDateFormat sdFormat = new SimpleDateFormat("MM/dd hh:mm");
		Date date = new Date();
		String strDate = sdFormat.format(date);
		return strDate;
	}

	public void setStage(Stage stage) {
        this.stage = stage;
    }
	public void setModel(ChatroomLayoutModel model) {
		this.model = model;
	}
}
