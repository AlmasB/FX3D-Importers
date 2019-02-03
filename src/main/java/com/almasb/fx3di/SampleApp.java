package com.almasb.fx3di;

import com.almasb.fx3di.obj.ObjImporter;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.application.Application;
import javafx.scene.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
public class SampleApp extends Application {

    private Rotate rotate = new Rotate(45, 0.5, 0.5, 0.5, Rotate.Y_AXIS);
    private Rotate rotate2 = new Rotate(0, 12, 3, 0, Rotate.Z_AXIS);

    private Scene createScene() {
        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setFarClip(500);
        camera.setTranslateY(-1.1);
        camera.setTranslateZ(-14.5);

        String[] modelNames = new String[] {
                "cooper.obj"
        };

        Group root = new Group();

        int i = 0;
        for (var name : modelNames) {
            Group model = new ObjImporter().load(getClass().getResource("obj/" + name));
            model.setCacheHint(CacheHint.SPEED);
            model.getTransforms().addAll(rotate, new Rotate(-180, Rotate.X_AXIS), new Translate(i*3, 0, 0));

            root.getChildren().add(model);

//            model.getChildren()
//                    .stream()
//                    .filter(group -> ((String)group.getProperties().get("name")).contains("Z3_wheel"))
//                    .forEach(group -> {
//                        RotateTransition rt = new RotateTransition(Duration.seconds(0.66), group);
//                        rt.setInterpolator(Interpolator.LINEAR);
//                        rt.setByAngle(360);
//                        rt.setCycleCount(Integer.MAX_VALUE);
//                        rt.setAxis(Rotate.Z_AXIS);
//                        rt.play();
//
//                        System.out.println(group.getTranslateX());
//                        System.out.println(group.getLayoutBounds());
//                        System.out.println(group.getBoundsInParent());
//                        System.out.println(group.getLocalToParentTransform());
//
//                        //group.getTransforms().add(rotate2);
//                    });

            i++;
        }

        var light = new PointLight();
        light.setTranslateX(-15);
        light.setTranslateZ(-15);

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
