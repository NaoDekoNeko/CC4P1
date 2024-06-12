package salesserver;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Connection;

public class Server {
    Connection postgresConnection;

    public void connectToPostgres() {
        try {
            Class.forName("org.postgresql.Driver");
            String url = "jdbc:postgresql://localhost:5432/BD1";
            String username = "admin";
            String password = "admin";

            postgresConnection = DriverManager.getConnection(url, username, password);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    private int getActualSale() throws SQLException {
        String query = "SELECT MAX(id_sales) FROM sales";
        java.sql.PreparedStatement preparedStatement = postgresConnection.prepareStatement(query);
        java.sql.ResultSet resultSet = preparedStatement.executeQuery();
        if (resultSet.next()) {
            return resultSet.getInt(1) + 1;
        }
        return 1; // start from 1 if no records are present
    }

    private int createSale(String name, String ruc, double total) throws SQLException {
        int saleID = getActualSale();
        String query = "INSERT INTO sales (id_sales, ruc, name, cost_total) VALUES (?, ?, ?, ?)";
        java.sql.PreparedStatement preparedStatement = postgresConnection.prepareStatement(query);
        preparedStatement.setInt(1, saleID);
        preparedStatement.setString(2, ruc);
        preparedStatement.setString(3, name);
        preparedStatement.setDouble(4, total);
        preparedStatement.executeUpdate();
        return saleID;
    }

    private void createDetail(int sale, Detail detail) throws SQLException {
        String query = "INSERT INTO details (id_sales, id_ruta, nombre, name_prod, descripcion_ruta, lugar_de_compra, asiento, cost, total) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        java.sql.PreparedStatement preparedStatement = postgresConnection.prepareStatement(query);
        preparedStatement.setInt(1, sale);
        preparedStatement.setInt(2, detail.idRuta);
        preparedStatement.setString(3, detail.nombre);
        preparedStatement.setString(4, detail.nameProd);
        preparedStatement.setString(5, detail.descripcionRuta);
        preparedStatement.setString(6, detail.lugarDeCompra);
        preparedStatement.setInt(7, detail.asiento);
        preparedStatement.setDouble(8, detail.cost);
        preparedStatement.setDouble(9, detail.total);
        preparedStatement.executeUpdate();
    }

    public void parseData(String data) throws SQLException {
        double total = 0;
        String[] dataSplit = data.split(";");
        String name = dataSplit[0];
        String ruc = dataSplit[1];
        Detail[] details = new Detail[dataSplit.length - 2];
        for (int i = 2; i < dataSplit.length; i++) {
            String[] detailData = dataSplit[i].split(",");
            Detail detail = new Detail();
            detail.idRuta = Integer.parseInt(detailData[0]);
            detail.nombre = detailData[1];
            detail.nameProd = detailData[2];
            detail.descripcionRuta = detailData[3];
            detail.lugarDeCompra = detailData[4];
            detail.asiento = Integer.parseInt(detailData[5]);
            detail.cost = Double.parseDouble(detailData[6]);
            detail.total = Double.parseDouble(detailData[7]);
            total += detail.total;
            details[i - 2] = detail;
        }
        int actualSale = createSale(name, ruc, total);
        for (Detail detail : details) {
            createDetail(actualSale, detail);
        }
    }
}