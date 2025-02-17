
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventType;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

/**
 * This class acts as an intermediary between an OpenJFX application and TestFX.
 *
 * It adds support for some extra UI components and employs a hack to work around an existing bug in
 * the current version to get them all to work properly.
 *
 * @author Robert C. Duvall
 */
public class DukeApplicationTest extends ApplicationTest {
    // standard steps to do for all test applications so factor it out here
    @BeforeAll
    public static void setUpClass () {
        // explicitly use the most stable robot implementation to avoid some older versions
        //   https://stackoverflow.com/questions/52605298/simple-testfx-example-fails
        System.setProperty("testfx.robot", "glass");
    }

    @AfterEach
    public void tearDown () throws Exception {
        // remove stage of running app
        FxToolkit.cleanupStages();
        // clear any key or mouse presses left unreleased
        release(new KeyCode[] {});
        release(new MouseButton[] {});
    }


    // utility methods for getting mouse and keyboard interactions to work
    protected void press (Scene scene, KeyCode key) {
        javafxRun(() -> scene.getOnKeyPressed().handle(new KeyEvent(KeyEvent.KEY_PRESSED, key.getChar(), key.getName(), key,
                false, false, false, false)));
    }

    protected void release (Scene scene, KeyCode key) {
        javafxRun(() -> scene.getOnKeyReleased().handle(new KeyEvent(KeyEvent.KEY_RELEASED, key.getChar(), key.getName(), key,
                false, false, false, false)));
    }

    protected void push (Scene scene, int x, int y) {
        javafxRun(() -> scene.getOnMousePressed().handle(new MouseEvent(MouseEvent.MOUSE_CLICKED, x, y, x, y, MouseButton.PRIMARY, 1,
                false, false, false, false, true, false, false, true, false, false, null)));
    }

    // extra utility methods for different UI components
    protected void clickOn (ButtonBase b) {
        simulateAction(b, () -> b.fire());
    }

    protected void setValue (Slider s, double value) {
        simulateAction(s, () -> s.setValue(value));
    }

    protected void setValue (ColorPicker cp, Color value) {
        simulateAction(cp, () -> { cp.setValue(value); cp.fireEvent(new ActionEvent()); });
    }

    protected void select (ComboBox<String> cb, String value) {
        // FIXME: duplicated code - but no common ancestor defines getSelectionModel()
        simulateAction(cb, () -> cb.getSelectionModel().select(value));
    }

    protected void select (ChoiceBox<String> cb, String value) {
        // FIXME: duplicated code - but no common ancestor defines getSelectionModel()
        simulateAction(cb, () -> cb.getSelectionModel().select(value));
    }

    protected void select (ListView<String> lv, String value) {
        // FIXME: duplicated code - but no common ancestor defines getSelectionModel()
        simulateAction(lv, () -> lv.getSelectionModel().select(value));
    }

    protected void javafxRun (Runnable action) {
        // fire event using given action on the given node
        Platform.runLater(action);
        // make it "later" so the requested event has time to run
        WaitForAsyncUtils.waitForFxEvents();
    }

    protected void fireButtonEvent(Button button) {
        javafxRun(() -> button.fireEvent(new ActionEvent()));
    }

    protected void fireMouseClick(Node node) {
        fireMouseEvent(node, MouseEvent.MOUSE_CLICKED);
    }

    protected void fireMouseEvent(Node node, EventType<MouseEvent> eventType) {
        javafxRun(() -> node.fireEvent(new MouseEvent(eventType,
                node.getLayoutX(), node.getLayoutY(), node.getLayoutX(), node.getLayoutY(),
                MouseButton.PRIMARY, 1, true, true,
                true, true, true, true,
                true, true, true, true, null)));
    }

    protected void fireMouseEvent(Node node, MouseEvent mouseEvent) {
        javafxRun(() -> node.fireEvent(mouseEvent));
    }

    // HACKs: needed to get simulating an UI action working :(
    private void simulateAction (Node n, Runnable action) {
        // simulate robot motion, not strictly necessary but helps show what test is being run
        moveTo(n);
        // fire event using given action on the given node
        javafxRun(action);
    }
}
