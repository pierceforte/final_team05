package engine.leveldirectory.gameeventhandler;

import engine.leveldirectory.graphicsengine.GraphicsEngine;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;

/**
 * This class handles used keyboard and mouse inputs
 *
 * @author Jerry Huang
 */
public class UserInputEvent extends GameEvent {
    private KeyCode lastKeyPressed;
    private MouseButton lastMousePressed;
    private Point2D lastPressedCoordinates;
    private Scene gameScene;
    private GraphicsEngine graphicsEngine;

    private boolean keyPressed;
    private boolean keyReleased;
    private boolean mouseClicked;


    public UserInputEvent(Scene gameScene, GraphicsEngine graphicsEngine) {
        super();
        lastKeyPressed = null;
        lastMousePressed = null;
        lastPressedCoordinates = null;
        this.gameScene = gameScene;
        this.graphicsEngine = graphicsEngine;
    }

    public void connect() {
        gameScene.setOnKeyPressed(event -> {
            lastKeyPressed = event.getCode();
            keyPressed = true;
        });
        gameScene.setOnKeyReleased(event -> {
            lastKeyPressed = event.getCode();
            keyReleased = true;
        });
        graphicsEngine.getBorderPane().setOnMouseClicked(e -> {
            lastMousePressed = e.getButton();
            lastPressedCoordinates = new Point2D(e.getX(), e.getY());
            mouseClicked = true;
        });
    }

    public KeyCode getLastKeyPressed() { return lastKeyPressed; }
    public Point2D getLastPressedCoordinates() { return lastPressedCoordinates; }
    public MouseButton getLastMousePressed() { return lastMousePressed; }
    public void update() {}
}

