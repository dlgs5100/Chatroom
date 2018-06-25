package application;
import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
public class LogIn extends Application {

    private Stage primaryStage;
    private BorderPane rootLogInLayout;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Client");
        initRootLayout();
        showLogInLayout();
    }

    public void initRootLayout() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(LogIn.class.getClassLoader().getResource("RootLogInLayout.fxml"));
            rootLogInLayout = (BorderPane) loader.load();

            Scene scene = new Scene(rootLogInLayout);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showLogInLayout() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(LogIn.class.getClassLoader().getResource("LogInLayout.fxml"));
            AnchorPane LogInLayout = (AnchorPane) loader.load();

            rootLogInLayout.setCenter(LogInLayout);
            
            LogInLayoutController controller = loader.getController();
            controller.setStage(primaryStage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
