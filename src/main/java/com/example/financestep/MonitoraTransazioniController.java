package com.example.financestep;

import com.example.financestep.model.Transazione;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.time.LocalDate;

public class MonitoraTransazioniController {

    @FXML
    private Label lblTitolo;

    @FXML
    private TableView<Transazione> tableTransazioniJunior;
    @FXML
    private TableColumn<Transazione, LocalDate> colData;
    @FXML
    private TableColumn<Transazione, String> colDescrizione;
    @FXML
    private TableColumn<Transazione, String> colTipo;
    @FXML
    private TableColumn<Transazione, Double> colImporto;

    @FXML
    public void initialize() {
        colData.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getData()));

        colDescrizione.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDescrizione()));

        colTipo.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getClass().getSimpleName()));

        colImporto.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getImporto()));
    }

    public void caricaTransazioniDi(String username) {
        lblTitolo.setText("Transazioni di: " + username);

        var lista = FXCollections.observableArrayList(
                DatabaseManager.caricaTransazioni(username)
        );
        lista.sort(java.util.Comparator.comparing(Transazione::getData).reversed());
        tableTransazioniJunior.setItems(lista);
    }
}
