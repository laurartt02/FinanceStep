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
                + "ultimo_id_notificato INTEGER NOT NULL DEFAULT 0"
                + "ultimo_id_premio_notificato INTEGER NOT NULL DEFAULT 0" + ");";

        String sqlSalvadanai = "CREATE TABLE IF NOT EXISTS salvadanaio (\n" +
                "    proprietario TEXT PRIMARY KEY,\n" +
                "    nome_obiettivo TEXT,\n" +
                "    somma_target REAL NOT NULL,\n" +
                "    somma_versata REAL NOT NULL\n" +
                ");";

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
                + "mittente TEXT NOT NULL, "
                + "stato TEXT NOT NULL"
                + ");";

        String sqlRichieste = "CREATE TABLE IF NOT EXISTS richieste ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "data TEXT NOT NULL, "
                + "importo REAL NOT NULL, "
                + "motivazione TEXT NOT NULL, "
                + "richiedente TEXT NOT NULL, "
                + "concedente TEXT NOT NULL, "
                + "stato TEXT NOT NULL"
                + ");";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(sqlUtenti);
            stmt.execute(sqlSalvadanai);
            stmt.execute(sqlTransazioni);
            stmt.execute(sqlTask);
            stmt.execute(sqlRichieste);

            System.out.println("Database SQLite inizializzato con successo!");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Migrazione per database già esistenti creati con lo schema vecchio
        eseguiAlterSeNecessario("ALTER TABLE compiti ADD COLUMN mittente TEXT NOT NULL DEFAULT ''");

        // Rimozione dei flag di notifica (schema abbandonato) dal DB già esistente
        eseguiAlterSeNecessario("ALTER TABLE compiti DROP COLUMN notifica_nuovo_compito_vista");
        eseguiAlterSeNecessario("ALTER TABLE compiti DROP COLUMN notifica_completamento_gestita");
        eseguiAlterSeNecessario("ALTER TABLE compiti DROP COLUMN notifica_premio_vista_junior");
        eseguiAlterSeNecessario("ALTER TABLE compiti DROP COLUMN sollecito_tutor_mostrato");
        eseguiAlterSeNecessario("ALTER TABLE compiti DROP COLUMN junior_scaduto_notificato");

        eseguiAlterSeNecessario("ALTER TABLE richieste ADD COLUMN concedente TEXT NOT NULL DEFAULT ''");

        // Aggiunta della colonna per memorizzare l'ultimo id che ha ricevuto una notifica
        eseguiAlterSeNecessario("ALTER TABLE utenti ADD COLUMN ultimo_id_notificato INTEGER NOT NULL DEFAULT 0");

        // Aggiunta della colonna per memorizzare l'ultimo id che ha ricevuto una notifica
        eseguiAlterSeNecessario("ALTER TABLE utenti ADD COLUMN ultimo_id_premio_notificato INTEGER NOT NULL DEFAULT 0");
    }

    private static void eseguiAlterSeNecessario(String sql) {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            // colonna già esistente: si ignora l'errore
        }
    }

    // --- OPERAZIONI UTENTI ---

    public static boolean utenteEsiste(String username) {
        String sql = "SELECT 1 FROM utenti WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);

            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next(); // true se esiste già una riga
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean salvaUtente(String username, String password, String ruolo) {
        if (utenteEsiste(username)) {
            return false; // username già registrato
        }

        String sql = "INSERT INTO utenti(username, password, ruolo) VALUES(?,?,?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, ruolo);

            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static Persona verificaUtente(String username, String password) {
        String sql = "SELECT * FROM utenti WHERE username = ? AND password = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String ruolo = rs.getString("ruolo");
                    if ("Tutor".equalsIgnoreCase(ruolo)) {
                        return new Tutor(username);
                    } else {
                        return new Junior(username);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null; // credenziali errate o utente inesistente
    }

    public static String recuperaRuolo(String username) {
        String sql = "SELECT ruolo FROM utenti WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("ruolo");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // utente inesistente
    }

    public static int getUltimoIdNotificato(String username) {
        String sql = "SELECT ultimo_id_notificato FROM utenti WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("ultimo_id_notificato");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static void aggiornaUltimoIdNotificato(String username, int nuovoId) {
        String sql = "UPDATE utenti SET ultimo_id_notificato = ? WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, nuovoId);
            pstmt.setString(2, username);

            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static int getUltimoIdPremioNotificato(String username) {
        String sql = "SELECT ultimo_id_premio_notificato FROM utenti WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("ultimo_id_premio_notificato");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static void aggiornaUltimoIdPremioNotificato(String username, int nuovoId) {
        String sql = "UPDATE utenti SET ultimo_id_premio_notificato = ? WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, nuovoId);
            pstmt.setString(2, username);

            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // --- OPERAZIONI SALVADANAIO ---

    public static void salvaSalvadanaio(Salvadanaio s, String proprietario) {
        String sql = "INSERT OR REPLACE INTO salvadanaio(proprietario, nome_obiettivo, somma_target, somma_versata) VALUES(?,?,?,?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, proprietario);
            pstmt.setString(2, s.getNomeObiettivo());
            pstmt.setDouble(3, s.getSommaTarget());
            pstmt.setDouble(4, s.getSommaVersata());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Salvadanaio caricaSalvadanaio(String proprietario) {
        String sql = "SELECT * FROM salvadanaio WHERE proprietario = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, proprietario);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Salvadanaio s = new Salvadanaio(
                            rs.getString("nome_obiettivo"),
                            rs.getDouble("somma_target")
                    );
                    s.setSommaVersata(rs.getDouble("somma_versata"));
                    return s;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // nessun salvadanaio ancora creato per questo utente
    }

    // --- OPERAZIONI TRANSAZIONI ---

    public static void salvaTransazione(Transazione t, String proprietario) {
        String sql = "INSERT INTO transazioni(data, descrizione, categoria, tipo, importo, proprietario) VALUES(?,?,?,?,?,?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
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
        String sql = "INSERT INTO compiti(titolo, premio, scadenza, destinatario, mittente, stato) VALUES(?,?,?,?,?,?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, t.getTitolo());
            pstmt.setDouble(2, t.getPremio());
            pstmt.setString(3, t.getScadenza().toString());
            pstmt.setString(4, t.getDestinatario());
            pstmt.setString(5, t.getMittente());
            pstmt.setString(6, t.getStato().name());

            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    t.setId(rs.getInt(1));
                }
            }
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
                        rs.getString("destinatario"),
                        rs.getString("mittente")
                );
                t.setId(rs.getInt("id"));
                t.setStato(Task.StatoTask.valueOf(rs.getString("stato")));
                lista.add(t);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public static void aggiornaStatoTask(Task t) {
        String sql = "UPDATE compiti SET stato = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, t.getStato().name());
            pstmt.setInt(2, t.getId());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    // --- OPERAZIONI RICHIESTE EXTRA ---

    public static void salvaRichiesta(RichiestaExtra r) {
        String sql = "INSERT INTO richieste(data, importo, motivazione, richiedente, concedente, stato) VALUES(?,?,?,?,?,?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, r.getData().toString());
            pstmt.setDouble(2, r.getImporto());
            pstmt.setString(3, r.getMotivazione());
            pstmt.setString(4, r.getRichiedente());
            pstmt.setString(5, r.getConcedente());
            pstmt.setString(6, r.getStato().name());

            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    r.setId(rs.getInt(1));
                }
            }

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
                        rs.getString("richiedente"),
                        rs.getString("concedente")
                );
                r.setId(rs.getInt("id"));
                r.setData(LocalDate.parse(rs.getString("data")));
                r.setStato(RichiestaExtra.StatoRichiesta.valueOf(rs.getString("stato")));
                lista.add(r);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public static void aggiornaStatoRichiesta(RichiestaExtra r) {
        String sql = "UPDATE richieste SET stato = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, r.getStato().name());
            pstmt.setInt(2, r.getId());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


}
