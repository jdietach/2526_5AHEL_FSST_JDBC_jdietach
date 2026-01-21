package com.example.jdbcdatenvisualisierung;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Tooltip;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HelloController {

    @FXML
    private ScatterChart<Number, Number> scatterChart;

    @FXML
    private Slider populationSlider;

    @FXML
    private Label sliderValueLabel;


    private static class CountryData {
        String name;
        double lifeExpectancy;
        long population;

        CountryData(String name, double lifeExpectancy, long population) {
            this.name = name;
            this.lifeExpectancy = lifeExpectancy;
            this.population = population;
        }
    }

    private final List<CountryData> allCountries = new ArrayList<>();
    private XYChart.Series<Number, Number> countrySeries;


    @FXML
    public void initialize() {

        loadDataFromDatabase();

        countrySeries = new XYChart.Series<>();
        countrySeries.setName("Länder");

        scatterChart.setLegendVisible(true);
        scatterChart.getData().add(countrySeries);

        setupSlider();
        updateChart(0);
    }

    private void loadDataFromDatabase() {

        try (Connection conn = DriverManager.getConnection(
                "jdbc:postgresql://xserv:5432/world2",
                "reader",
                "reader"
        );
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("""
                SELECT name, lifeexpectancy, population
                FROM country
                WHERE lifeexpectancy IS NOT NULL
                  AND population > 0
             """)
        ) {
            while (rs.next()) {
                allCountries.add(new CountryData(
                        rs.getString("name"),
                        rs.getDouble("lifeexpectancy"),
                        rs.getLong("population")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setupSlider() {
        updateSliderLabel(populationSlider.getValue());

        populationSlider.valueProperty().addListener((obs, o, n) -> {
            updateSliderLabel(n.doubleValue());
            updateChart(n.longValue());
        });
    }

    private void updateSliderLabel(double value) {
        sliderValueLabel.setText("≥ " + String.format("%,d", (long) value));
    }

    private void updateChart(long minPopulation) {

        countrySeries.getData().clear();

        for (CountryData c : allCountries) {
            if (c.population >= minPopulation) {
                countrySeries.getData().add(
                        new XYChart.Data<>(c.lifeExpectancy, c.population)
                );
            }
        }

        Platform.runLater(this::Tooltip);
    }


    private void Tooltip() {

        for (int i = 0; i < countrySeries.getData().size(); i++) {

            XYChart.Data<Number, Number> data = countrySeries.getData().get(i);
            CountryData country = getCountryForData(data);

            Node node = data.getNode();
            if (node == null || country == null) continue;


            Tooltip tooltip = new Tooltip(
                    country.name +
                            "\nLebenserwartung: " + country.lifeExpectancy + " Jahre" +
                            "\nBevölkerung: " + String.format("%,d", country.population)
            );
            Tooltip.install(node, tooltip);

            node.setOnMouseEntered(e ->
                    node.setStyle("-fx-scale-x:1.8; -fx-scale-y:1.8;")
            );
            node.setOnMouseExited(e ->
                    node.setStyle("-fx-scale-x:1.0; -fx-scale-y:1.0;")
            );

            node.setOnMouseClicked(e -> showCountryDetails(country));
        }
    }

    private CountryData getCountryForData(XYChart.Data<Number, Number> data) {

        for (CountryData c : allCountries) {
            if (c.lifeExpectancy == data.getXValue().doubleValue()
                    && c.population == data.getYValue().longValue()) {
                return c;
            }
        }
        return null;
    }

    private void showCountryDetails(CountryData c) {

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Länderdetails");
        alert.setHeaderText(c.name);
        alert.setContentText(
                "Lebenserwartung: " + c.lifeExpectancy + " Jahre\n" +
                        "Bevölkerung: " + String.format("%,d", c.population)
        );
        alert.showAndWait();
    }
}
