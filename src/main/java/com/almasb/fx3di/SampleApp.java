package com.almasb.fx3di;

import com.almasb.fx3di.obj.ObjImporter;
import javafx.application.Application;
import javafx.scene.*;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;

/**
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
public class SampleApp extends Application {

    private Rotate rotate = new Rotate(45, 0.5, 0.5, 0.5, Rotate.Y_AXIS);

    private Scene createScene() {
        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setTranslateY(0);
        camera.setTranslateZ(-4.5);

        Group model = new ObjImporter().load(getClass().getResource("obj/DukeKing.obj"));

        model.getTransforms().add(rotate);

        var light = new PointLight();
        light.setTranslateX(-2);
        light.setTranslateZ(-2);

        Group root = new Group(model, light);

        Scene scene = new Scene(root, 1280, 720, true);
        scene.setCamera(camera);

        return scene;
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(createScene());
        stage.getScene().setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case A:
                    rotate.setAngle(rotate.getAngle() + 2);
                    break;

                case D:
                    rotate.setAngle(rotate.getAngle() - 2);
                    break;
            }
        });

        stage.show();
    }

    public static class Launcher {
        public static void main(String[] args) {
            Application.launch(SampleApp.class, args);
        }
    }
}
