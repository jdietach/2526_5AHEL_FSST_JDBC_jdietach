package com.example.jdbcdatenvisualisierung;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.ScatterChart;
import javafx.scene.control.Slider;
import java.sql.*;

public class HelloController {

    public ScatterChart scatterChart;
    public Slider populationSlider;

    public void initialize() {

        Connection conn = null;
        Statement st = null;
        ResultSet rs = null;

        try {
            conn = DriverManager.getConnection(
                    "jdbc:postgresql://xserv:5432/world2",
                    "reader",
                    "reader"
            );

            st = conn.createStatement();


            rs = st.executeQuery("""
                SELECT c.name, c.lifeexpectancy, c.population
                FROM country c
            """);


            while (rs.next()) {

                String country = rs.getString("name");
                double lifeExpectancy = rs.getDouble("lifeexpectancy");
                long population = rs.getLong("population");

                System.out.println(
                        country + " | Lebenserwartung: "
                                + lifeExpectancy + " | Bevölkerung: "
                                + population
                );
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {


            try { if (rs != null) rs.close(); } catch (SQLException ignored) {}
            try { if (st != null) st.close(); } catch (SQLException ignored) {}
            try { if (conn != null) conn.close(); } catch (SQLException ignored) {}
        }
    }

    public void onHelloButtonClick(ActionEvent actionEvent) {
    }
}
