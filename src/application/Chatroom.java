package application;
import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
public class Chatroom extends Application {

    private Stage primaryStage;
    private BorderPane rootChatroomLayout;
    private ChatroomLayoutModel model;
    private ChatroomLayoutController controller;
    

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Client");
        model = new ChatroomLayoutModel();
        model.setConnection();
        initRootLayout();
        showChatroomLayout();
    }

    public void initRootLayout() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Chatroom.class.getClassLoader().getResource("RootChatroomLayout.fxml"));
            rootChatroomLayout = (BorderPane) loader.load();

            Scene scene = new Scene(rootChatroomLayout);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showChatroomLayout() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Chatroom.class.getClassLoader().getResource("ChatroomLayout.fxml"));
            AnchorPane ChatroomLayout = (AnchorPane) loader.load();

            rootChatroomLayout.setCenter(ChatroomLayout);
            
            controller = loader.getController();
            model.setController(controller);
            controller.setStage(primaryStage);
            controller.setScroll();
            controller.setModel(model);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

}
