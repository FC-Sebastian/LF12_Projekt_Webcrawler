package model;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class EnergyDataset {
    private String dbHost = "jdbc:mysql://localhost:3306/docker";
    private String dbUser = "docker";
    private String dbPass = "docker";
    private Connection con;

    // columns
    private String datetime;
    private String type;
    private String gwhPerHour;

    public EnergyDataset() {}

    public EnergyDataset(String datetime, String type, String gwhPerHour) {
        this.datetime = datetime;
        this.type = type;
        this.gwhPerHour = gwhPerHour;
    }

    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getGwhPerHour() {
        return gwhPerHour;
    }

    public void setGwhPerHour(String gwhPerHour) {
        this.gwhPerHour = gwhPerHour;
    }

    public boolean load(String datetime, String type) throws SQLException {
        initiateDriverManager();
        PreparedStatement stmt = con.prepareStatement("SELECT * FROM energy_data WHERE datetime=? AND type=?");
        stmt.setString(1, datetime);
        stmt.setString(2,type);
        if (stmt.execute()) {
            ResultSet result = stmt.getResultSet();
            this.datetime = result.getString("datetime");
            this.type = result.getString("type");
            gwhPerHour = result.getString("gwh_per_hour");
            return  true;
        }
        return false;
    }

    public void save() throws SQLException {
        initiateDriverManager();
        if (datasetExists(datetime, type)) {
            update();
        } else {
            insert();
        }
    }

    public void batchInsert(List<String[]> batchData) throws SQLException {
        System.out.println("batch insert");
        initiateDriverManager();
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
        PreparedStatement stmt = con.prepareStatement(query.toString());
        stmt.execute();
    }

    private boolean isAfterNuclearExit(String datetime) {
        LocalDate date = LocalDate.parse(datetime, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
        LocalDate nuclearExitDate = LocalDate.parse("2023-04-16T00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
        return !date.isBefore(nuclearExitDate);
    }

    private void update() throws SQLException {
        PreparedStatement stmt = con.prepareStatement("UPDATE energy_data SET gwh_per_hour=? WHERE datetime=? AND type=?");
        stmt.setString(1, gwhPerHour);
        stmt.setString(2, datetime);
        stmt.setString(3, type);
        stmt.execute();
    }

    private void insert() throws SQLException {
        PreparedStatement stmt = con.prepareStatement("INSERT INTO energy_data (datetime,type,gwh_per_hour) VALUES (?,?,?)");
        stmt.setString(1,datetime);
        stmt.setString(2,type);
        stmt.setString(3,gwhPerHour);
        stmt.execute();
    }

    private boolean datasetExists(String datetime, String type) throws SQLException {
        PreparedStatement stmt = con.prepareStatement("SELECT * FROM energy_data WHERE datetime=? AND type=?");
        stmt.setString(1, datetime);
        stmt.setString(2,type);
        stmt.execute();
        return stmt.getResultSet().isBeforeFirst();
    }

    private void initiateDriverManager() throws SQLException {
        con = DriverManager.getConnection(dbHost, dbUser, dbPass);
    }

    public String toString() {
        return "time: " + datetime + " type: " + type + " gwh/h: "+gwhPerHour;
    }
}
