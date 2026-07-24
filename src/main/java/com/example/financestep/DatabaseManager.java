package com.example.financestep;

import com.example.financestep.model.*;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {

    private static final String URL = "jdbc:sqlite:financestep.db";

    public static Connection getConnection() throws SQLException {
        System.out.println("DB path: " + new java.io.File("financestep.db").getAbsolutePath());
        return DriverManager.getConnection(URL);
    }

    // Inizializza il database creando le tabelle se non esistono ancora
    public static void inizializzaDatabase() {
        String sqlUtenti = "CREATE TABLE IF NOT EXISTS utenti ("
                + "username TEXT PRIMARY KEY, "
                + "password TEXT NOT NULL, "
                + "ruolo TEXT NOT NULL" // "Junior" o "Tutor"
                + ");";

        String sqlTransazioni = "CREATE TABLE IF NOT EXISTS transazioni ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "data TEXT NOT NULL, "
                + "descrizione TEXT NOT NULL, "
                + "categoria TEXT NOT NULL, "
                + "tipo TEXT NOT NULL, "        // "Spesa" o "Entrata"
                + "importo REAL NOT NULL, "
                + "proprietario TEXT NOT NULL"  // a chi/quale Portafoglio appartiene
                + ");";

        String sqlTask = "CREATE TABLE IF NOT EXISTS compiti ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "titolo TEXT NOT NULL, "
                + "premio REAL NOT NULL, "
                + "scadenza TEXT NOT NULL, "
                + "destinatario TEXT NOT NULL, "
                + "stato TEXT NOT NULL"
                + ");";

        String sqlRichieste = "CREATE TABLE IF NOT EXISTS richieste ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "data TEXT NOT NULL, "
                + "importo REAL NOT NULL, "
                + "motivazione TEXT NOT NULL, "
                + "richiedente TEXT NOT NULL, "
                + "stato TEXT NOT NULL"
                + ");";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(sqlUtenti);
            stmt.execute(sqlTransazioni);
            stmt.execute(sqlTask);
            stmt.execute(sqlRichieste);

            System.out.println("Database SQLite inizializzato con successo!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // --- OPERAZIONI TRANSAZIONI ---

    public static void salvaTransazione(Transazione t, String proprietario) {
        String sql = "INSERT INTO transazioni(data, descrizione, categoria, tipo, importo, proprietario) VALUES(?,?,?,?,?,?)";
        try (Connection conn = getConnection();
        PreparedStatement pstmt=conn.prepareStatement(sql)){
            pstmt.setString(1, t.getData().toString());
            pstmt.setString(2, t.getDescrizione());

            if (t instanceof Spesa spesa) {
                pstmt.setString(3, spesa.getCategoria());
                pstmt.setString(4, "Spesa");
            } else if (t instanceof Entrata entrata) {
                pstmt.setString(3, entrata.getSorgente());
                pstmt.setString(4, "Entrata");
            }

            pstmt.setDouble(5, t.getImporto());
            pstmt.setString(6, proprietario);

            pstmt.executeUpdate();
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    public static List<Transazione> caricaTransazioni(String proprietario) {
        List<Transazione> lista = new ArrayList<>();
        String sql = "SELECT * FROM transazioni WHERE proprietario = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, proprietario);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Transazione t;
                    String tipo = rs.getString("tipo");
                    LocalDate data = LocalDate.parse(rs.getString("data"));

                    if ("Spesa".equalsIgnoreCase(tipo)) {
                        t = new Spesa(
                                rs.getDouble("importo"),
                                data,
                                rs.getString("descrizione"),
                                rs.getString("categoria")
                        );
                    } else {
                        t = new Entrata(
                                rs.getDouble("importo"),
                                data,
                                rs.getString("descrizione"),
                                rs.getString("categoria") // qui è la "sorgente" salvata nella stessa colonna
                        );
                    }

                    lista.add(t);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return lista;
    }


    // --- OPERAZIONI COMPITI (TASK) ---

    public static void salvaTask(Task t) {
        String sql = "INSERT INTO compiti(titolo, premio, scadenza, destinatario, stato) VALUES(?,?,?,?,?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, t.getTitolo());
            pstmt.setDouble(2, t.getPremio());
            pstmt.setString(3, t.getScadenza().toString());
            pstmt.setString(4, t.getDestinatario());
            pstmt.setString(5, t.isCompletato() ? "COMPLETATO" : "IN_CORSO");

            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<Task> caricaTask() {
        List<Task> lista = new ArrayList<>();
        String sql = "SELECT * FROM compiti";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Task t = new Task(
                        rs.getString("titolo"),
                        rs.getDouble("premio"),
                        LocalDate.parse(rs.getString("scadenza")),
                        rs.getString("destinatario")
                );
                if ("COMPLETATO".equalsIgnoreCase(rs.getString("stato"))) {
                    t.confermaEsecuzione();
                }
                lista.add(t);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public static void aggiornaStatoTask(Task t) {
        String sql = "UPDATE compiti SET stato = ? WHERE titolo = ? AND destinatario = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, t.isCompletato() ? "COMPLETATO" : "IN_CORSO");
            pstmt.setString(2, t.getTitolo());
            pstmt.setString(3, t.getDestinatario());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    // --- OPERAZIONI RICHIESTE EXTRA ---

    public static void salvaRichiesta(RichiestaExtra r) {
        String sql = "INSERT INTO richieste(data, importo, motivazione, richiedente, stato) VALUES(?,?,?,?,?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, r.getData().toString());
            pstmt.setDouble(2, r.getImporto());
            pstmt.setString(3, r.getMotivazione());
            pstmt.setString(4, r.getRichiedente());
            pstmt.setString(5, r.getStato());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<RichiestaExtra> caricaRichieste() {
        List<RichiestaExtra> lista = new ArrayList<>();
        String sql = "SELECT * FROM richieste";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                RichiestaExtra r = new RichiestaExtra(
                        rs.getDouble("importo"),
                        rs.getString("motivazione"),
                        rs.getString("richiedente")
                );
                r.setStato(rs.getString("stato"));
                lista.add(r);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public static void aggiornaStatoRichiesta(RichiestaExtra r) {
        String sql = "UPDATE richieste SET stato = ? WHERE richiedente = ? AND motivazione = ? AND importo = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, r.getStato());
            pstmt.setString(2, r.getRichiedente());
            pstmt.setString(3, r.getMotivazione());
            pstmt.setDouble(4, r.getImporto());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


}
