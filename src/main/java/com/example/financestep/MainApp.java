package com.example.financestep;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApp extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        // 1. Inizializza il file del Database SQLite e crea le tabelle se non esistono
        DatabaseManager.inizializzaDatabase();

        // 2. Caricamento della schermata iniziale (Auth)
        FXMLLoader fxmlLoader = new FXMLLoader(MainApp.class.getResource("auth.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("FinanceStep");
        stage.setScene(scene);
        IconUtil.applica(stage);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}