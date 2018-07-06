package application;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.activation.MimetypesFileTypeMap;
import javax.imageio.ImageIO;
import javax.swing.filechooser.FileSystemView;
import javax.xml.bind.DatatypeConverter;

import org.json.JSONObject;
import org.json.JSONArray;
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
	
	private static int IMAGE_RATIO = 5;

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
		imageFormat = FileSystemView.getFileSystemView().getSystemTypeDescription(file).substring(0, 3);
		
		try{
			BufferedImage bufferedImage = ImageIO.read(file);
			baos = new ByteArrayOutputStream();
		    ImageIO.write(bufferedImage, imageFormat, baos);
		    
			image = SwingFXUtils.toFXImage(bufferedImage, null);
			image.isPreserveRatio();
			
		}catch(IOException ex){
			ex.printStackTrace();
		}
		
		canvas = new Canvas(image.getWidth()/IMAGE_RATIO,image.getHeight()/IMAGE_RATIO);
		
		gc = canvas.getGraphicsContext2D();
		gc.drawImage(image,0,0,image.getWidth()/IMAGE_RATIO,image.getHeight()/IMAGE_RATIO);
		HBox hboxMessage = new HBox();
		hboxMessage.setAlignment(Pos.CENTER_RIGHT);
		hboxMessage.getChildren().add(canvas);
	
		vbox.getChildren().add(hboxMessage);
		
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("image", DatatypeConverter.printBase64Binary(baos.toByteArray()));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		model.sendServer(jsonObject);
	}

	@FXML
	public void handleEnterMessage(ActionEvent event) {
		if (!tfEnterMessage.getText().trim().isEmpty()) {
			String messageToServer = tfEnterMessage.getText().trim().toString();
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
			tfEnterMessage.clear();

			Map<String, String> map = new HashMap<String, String>();
			map.put("message", messageToServer);
			map.put("nowTime", getDateTime());
			JSONObject jsonObject = new JSONObject(map);
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
	public void updateLabelLater(final String text1, int size1, final String text2, int size2) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				Label label1 = new Label(text1);
				Label label2 = new Label(text2);
				label1.setFont(new Font(size1));
				label2.setFont(new Font(size2));

				vbox.getChildren().add(label1);
				vbox.getChildren().add(label2);
			}
		});
	}
	public void updateImageLater(final String imageData) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				byte[] base64Decoded = DatatypeConverter.parseBase64Binary(imageData);			
				try {
					BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(base64Decoded));
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
				vbox.getChildren().add(canvas);
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
