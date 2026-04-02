package Classes.model;

import javafx.scene.PointLight;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;

public class LightEngine{
    private PointLight sun;

    public LightEngine() {
        sun = new PointLight(Color.WHITE);
        sun.setTranslateX(0);
        sun.setTranslateY(0);
        sun.setTranslateZ(0);
    }

    //setTranslateX
    public void setTranslateX(double x) {
        sun.setTranslateX(x);
    }

    //setTranslateY
    public void setTranslateY(double y) {
        sun.setTranslateY(y);
    }

    //setTranslateZ
    public void setTranslateZ(double z) {
        sun.setTranslateZ(z);
    }

    public PointLight getSun() {
        return sun;
    }
}
