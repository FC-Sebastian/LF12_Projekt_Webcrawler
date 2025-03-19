package controller;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DatabaseHandler {
    private String dbHost = "jdbc:mysql://localhost:3306/docker";
    private String dbUser = "docker";
    private String dbPass = "docker";
    private Connection con;

    public DatabaseHandler() {}

    /**
     * builds query string from preformatted list and executes it
     *
     * @param batchData formatted data to insert
     * @throws SQLException if connection problems occur
     */
    public void batchInsert(List<String[]> batchData) throws SQLException {
        System.out.println("batch insert");
        initiateDriverManager();
        String query = buildQuery(batchData);
        PreparedStatement stmt = con.prepareStatement(query);
        stmt.execute();
    }

    /**
     * initiates db connection
     *
     * @throws SQLException if connection problems occur
     */
    private void initiateDriverManager() throws SQLException {
        con = DriverManager.getConnection(dbHost, dbUser, dbPass);
    }

    /**
     * builds query from formatted data,
     * INSERT IGNORE is used to suppress duplicate entry warnings,
     * while not clean per se it comes with greater efficiency
     *
     * @param batchData formatted csv data
     * @return query string
     */
    private String buildQuery(List<String[]> batchData) {
        StringBuilder query = new StringBuilder("INSERT IGNORE INTO energy_data (datetime,type,gwh_per_hour) VALUES ");
        String prefix = "";
        for (String[] dataset: batchData) {
            if (dataset[1] == "Kernkraft" && isAfterNuclearExit(dataset[0])) { // skipping nuclear dataset if after nuclear exit
                continue;
            }

            query.append(prefix);
            prefix = ",";
            query
                    .append("('")
                    .append(dataset[0])
                    .append("','")
                    .append(dataset[1])
                    .append("','")
                    .append(dataset[2])
                    .append("')");
        }
        return query.toString();
    }

    /**
     * checks if a given date is after nuclear exit and returns bool
     *
     * @param datetime datetime string
     * @return true if after, false if before
     */
    private boolean isAfterNuclearExit(String datetime) {
        LocalDate date = LocalDate.parse(datetime, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
        LocalDate nuclearExitDate = LocalDate.parse("2023-04-16T00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
        return !date.isBefore(nuclearExitDate);
    }
}
