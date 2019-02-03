package com.almasb.fx3di;

import com.almasb.fx3di.obj.ObjImporter;
import javafx.application.Application;
import javafx.scene.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;

/**
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
public class SampleApp extends Application {

    private Rotate rotate = new Rotate(45, 0.5, 0.5, 0.5, Rotate.Y_AXIS);

    private Scene createScene() {
        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setTranslateY(-1.1);
        camera.setTranslateZ(-4.5);

        String[] modelNames = new String[] {
                "DukeKing.obj"
        };

        Group root = new Group();

        int i = 0;
        for (var name : modelNames) {
            Group model = new ObjImporter().load(getClass().getResource("obj/" + name));

            model.getTransforms().addAll(rotate, new Rotate(-180, Rotate.X_AXIS), new Translate(i*2, 0, 0));

            root.getChildren().add(model);

            i++;
        }

        var light = new PointLight();
        light.setTranslateX(-2);
        light.setTranslateZ(-2);

        root.getChildren().add(light);

        SubScene scene = new SubScene(root, 1280, 720, true, SceneAntialiasing.BALANCED);
        scene.setCamera(camera);

        return new Scene(new Group(new Rectangle(1280, 720, Color.LIGHTGRAY), scene));
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(createScene());
        stage.getScene().setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case A:
                    rotate.setAngle(rotate.getAngle() - 2);
                    break;

                case D:
                    rotate.setAngle(rotate.getAngle() + 2);
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
