package eus.ehu.presentation;

import eus.ehu.business_logic.AeroplofFlightBooker;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javafx.event.ActionEvent;


import eus.ehu.business_logic.FlightBooker;
import eus.ehu.domain.ConcreteFlight;

public class FlightBookingController {

    @FXML
    private Label output;

    // create conFlightInfo JAvaFX observable list
    private ObservableList<ConcreteFlight> conFlightInfo = FXCollections.observableArrayList();

    @FXML
    private ListView<ConcreteFlight> conFlightList;


    @FXML
    private Button bookSelectedConFlightButton;

    @FXML
    private ComboBox<String> monthCombo;

    @FXML
    private TextField dayInput;

    @FXML
    private TextField yearInput;

    @FXML
    private TextField arrivalInput;

    @FXML
    private TextField departureInput;

    @FXML
    private Label searchResultAnswer;

    @FXML
    private RadioButton economyRB;

    @FXML
    private RadioButton firstRB;

    @FXML
    private RadioButton businessRB;

    @FXML
    private TextField no_tickets;

    @FXML
    private ComboBox<ConcreteFlight> flightlist;

    private FlightBooker businessLogic;
    private ConcreteFlight selectedConFlight;

    /**
     * setupInputComponents method
     * <p>
     * It configures and adds to the GUI's panel all elements needed to
     * capture the input options of the user (flight route, date and fare)
     */
    private void setupInputComponents() {

        String[] monthNames = {"January", "February", "March", "April", "May",
                "June", "July", "August", "September", "October", "November",
                "December"};

        monthCombo.getItems().addAll(monthNames);
        // The first month of the year in the Gregorian and Julian calendars is JANUARY which is 0;
        final int JULY = 6;
        monthCombo.getSelectionModel().select(JULY);

        //flightlist.setItems(conFlightInfo);
        bookSelectedConFlightButton.setDisable(true);

        /**
         *
         * When the user selects a flight the "bookSelectedConFlightButton" is
         * enabled and displays an invitation to book it
         */

        flightlist.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                selectedConFlight = newValue;
                bookSelectedConFlightButton.setDisable(false);
                bookSelectedConFlightButton.setText("Book a ticket in selected flight");
            }
        });
    }

    /**
     * The button sends a request to the business logic to fetch a list of
     * concrete flights, which then is loaded into "conFlightInfo", the content
     * of the ListView "conFlightList" used to display the flights and allow
     * their selection by the user.
     */
    @FXML
    void searchConFlightsButton(ActionEvent event) {
        int modo;
        int cantidad = 0;
        conFlightInfo.clear();

        String chosenDateString = monthCombo.getValue() + " " +
                dayInput.getText() + " " + yearInput.getText();
        SimpleDateFormat format = new SimpleDateFormat("MMMM' 'd' 'yyyy",
                Locale.ENGLISH);
        format.setLenient(false);

        if(economyRB.isSelected()){
            modo = 1;
        } else if (firstRB.isSelected()) {
            modo = 2;
        }else if (businessRB.isSelected()) {
            modo = 3;
        }else{
            modo = -1;
        }


        try {
            Date chosenDate = format.parse(chosenDateString);
            List<ConcreteFlight> foundConFlights = businessLogic.getMatchingConFlights(departureInput.getText(),
                            arrivalInput.getText(), chosenDate, modo, cantidad);
            for (ConcreteFlight v : foundConFlights)
                conFlightInfo.add(v);
            if (foundConFlights.isEmpty()){
                searchResultAnswer.setText("No matching flights found. " +
                        "Please change your options");
            }
            else if(no_tickets.getText().isEmpty()){
                searchResultAnswer.setText("Choose a valid number of tickets please");
            }
            else {
                try{
                    cantidad = Integer.parseInt(no_tickets.getText());
                    searchResultAnswer.setText("Choose an available flight" + " in the following list:");
                    flightlist.setItems(conFlightInfo);
                }catch(NumberFormatException e){
                    searchResultAnswer.setText("Choose a valid number of tickets please");
                }
            }
        } catch (ParseException pe) {
            searchResultAnswer.setText("The chosen date " + chosenDateString +
                    " is not valid. Please correct it");
        }

    }

    /**
     * Book the concrete flight selected by the user. Normally
     * disabled, excepting when the user's choice takes place.
     */

    @FXML
    void selectConFlight(ActionEvent event) {
        int remaining = 0;
        int amount = Integer.parseInt(no_tickets.getText());
        if (firstRB.isSelected()) {
            remaining = businessLogic.bookSeat(selectedConFlight, "First", amount);
        } else if (businessRB.isSelected()) {
            remaining = businessLogic.bookSeat(selectedConFlight, "Business", amount);
        } else if (economyRB.isSelected()) {
            remaining = businessLogic.bookSeat(selectedConFlight, "Economy", amount);
        }
        if (remaining < 0)
            bookSelectedConFlightButton.setText("Error: This flight had no "
                    + "ticket for the requested fare!");
        else
            bookSelectedConFlightButton.
                    setText("Your ticket has been booked. Remaining tickets = " + remaining);
        bookSelectedConFlightButton.setDisable(true);
        flightlist.getItems().clear();
    }

    public void setBusinessLogic(FlightBooker g) {
        businessLogic = g;
    }

    @FXML
    void initialize() {
        assert output != null : "fx:id=\"output\" was not injected: check your FXML file 'hello-view.fxml'.";

        setupInputComponents();
        setBusinessLogic(new AeroplofFlightBooker());

    }


}
