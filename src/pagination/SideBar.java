package pagination;

import builder.bank.view.BankView;
import data.user.User;
import engine.general.Game;
import engine.leveldirectory.hud.HUDView;
import javafx.scene.Scene;
import javafx.scene.control.Button;

import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.io.IOException;
import java.util.ResourceBundle;

public class SideBar extends Pane {

    private Scene myScene;
    private String STYLESHEET;
    private Button lightbutton;
    private Button back;
    private PageController pageController;

    private Game game;

    private ResourceBundle myResource = ResourceBundle.getBundle("text.MenuButtons");
    private final static String Light = "css/light.css";
    private final static String Dark = "css/dark.css";

    public SideBar(Scene scene, PageController pc) {
        this.setId("hudView");

        this.game = game;
        setDefaultDisplay(scene, pc);

        ImageView img = new ImageView(pc.getUser().getAvatar());
        img.setId("center");
        this.getChildren().add(img);
    }

    public SideBar(Scene scene, PageController pc, Game game) {
        this.setId("hudView");

        this.game = game;
        setDefaultDisplay(scene, pc);

        ImageView img = new ImageView(pc.getUser().getAvatar());
        img.setId("center");
        this.getChildren().add(img);
    }

    private void setDefaultDisplay(Scene scene, PageController pc) {
        myScene = scene;
        STYLESHEET = Light;
        pageController = pc;

        buildLightButton();
        buildBackButton();

        this.getChildren().add(displayUserStats());
    }

    public SideBar(Scene scene, PageController pc, HUDView myView, Game game) {
        this.setId("hudView");
        this.game = game;

        setDefaultDisplay(scene, pc);
        this.getChildren().add(myView);
    }

    public SideBar(Scene scene, PageController pc, BankView bankView, Game game) {
        this.setId("hudView");

        this.game = game;
        setDefaultDisplay(scene, pc);
        this.getChildren().add(bankView);

    }

    private void buildLightButton() {
        lightbutton = new Button();
        lightbutton.setId("LightButton");
        lightbutton.setOnMouseClicked(event -> switchStyle());
        this.getChildren().add(lightbutton);
    }

    private void switchStyle() {
        myScene.getStylesheets().removeAll(this.getClass().getResource(STYLESHEET).toExternalForm());
        if (STYLESHEET.equals(Light)) { STYLESHEET = Dark; }
        else { STYLESHEET = Light; }
        myScene.getStylesheets().addAll(this.getClass().getResource(STYLESHEET).toExternalForm());
        return;
    }

    private void buildBackButton() {
        back = new Button(myResource.getString("Back"));
        back.setId("back");
        back.setOnMouseClicked(this::handle);
        this.getChildren().add(back);
    }

    private void goBack() throws IOException {
        pageController.setLastLevel(game.getLevelContainer().getLevelNum());
        LevelDirectory ld = new LevelDirectory(pageController.getMyStage(), Pages.LevelDirectory, pageController);

    }

    private void handle(MouseEvent event) {
        try {
            goBack();
        } catch (IOException e) {
        }
    }


    private TextFlow displayUserStats() {
        TextFlow textFlow = new TextFlow();
        User myU = pageController.getUser();

        Text userDisplay = new Text(myResource.getString("User") + myU.getId());
        userDisplay.setId("userDisplay");

        Text scoreDisplay = new Text(myResource.getString("Score")+myU.getScore());
        scoreDisplay.setId("scoreSBDisplay");

        textFlow.getChildren().addAll(userDisplay, scoreDisplay);

        return textFlow;
    }
}
