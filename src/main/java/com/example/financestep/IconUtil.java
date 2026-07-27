package com.example.financestep;

import javafx.scene.control.Dialog;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class IconUtil {
    private static final Image ICONA = new Image(
            IconUtil.class.getResourceAsStream("/com/example/financestep/icon.png")
    );

    public static void applica(Stage stage) {
        stage.getIcons().add(ICONA);
    }

    public static void applica(Dialog<?> dialog) {
        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        stage.getIcons().add(ICONA);
    }
}
