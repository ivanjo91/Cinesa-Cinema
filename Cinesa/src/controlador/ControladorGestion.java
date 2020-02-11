/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controlador;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import modelo.ConectarBD;
import modelo.Facturado;
import modelo.Sala;

/**
 * FXML Controller class
 *
 * @author David
 */
public class ControladorGestion implements Initializable {
    
    File ficheroImagen;
    ConectarBD cnx = new ConectarBD();
    ArrayList<Facturado> facturacionSemanal = new ArrayList<>();
    ObservableList<String> listaSalas = FXCollections.observableArrayList();

    @FXML
    private TabPane tabPane;
    @FXML
    private Tab tabCartelera;
    @FXML
    private AnchorPane acCartelera;
    @FXML
    private TextField txtTitulo;
    @FXML
    private DatePicker dateCalendar;
    @FXML
    private TextField txtHora;
    @FXML
    private TextField txtSala;
    @FXML
    private Pane paneImagenCartel;
    @FXML
    private ImageView imagenCartel;
    @FXML
    private Button btnInsertar;
    @FXML
    private Button btnModificar;
    @FXML
    private Button btnResetearCartelera;
    @FXML
    private Tab tabSalas;
    @FXML
    private AnchorPane acSalas;
    @FXML
    private Tab tabFacturacion;
    @FXML
    private AnchorPane acFacturacion;
    @FXML
    private Button btnFacturacionSemanal;
    @FXML
    private Label lbFacturacionSemanal;
    @FXML
    private Label lbFacturacionSemanalEuros;
    @FXML
    private Button btnInsertarSala;
    @FXML
    private Button btnModificarSala;
    @FXML
    private ComboBox<String> comboSalas;
    @FXML
    private TextField txtIdSala;
    @FXML
    private TextField txtFilas;
    @FXML
    private TextField txtColumnas;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        listaSalas = cnx.obtenerSalas();
        comboSalas.setItems(listaSalas);
    }    

    @FXML
    private void cargarImagen_OnMouseClicked(MouseEvent event) {
        try {
            FileChooser filechooser = new FileChooser();
            FileChooser.ExtensionFilter filtroJPG = new FileChooser.ExtensionFilter("fichero JPG(*.JPG)", "*.JPG");
            FileChooser.ExtensionFilter filtroPNG = new FileChooser.ExtensionFilter("fichero PNG(*.PNG)", "*.PNG");
            filechooser.getExtensionFilters().addAll(filtroJPG, filtroPNG);
            ficheroImagen = filechooser.showOpenDialog(null);
            Image imagen = new Image(ficheroImagen.toURI().toURL().toString());
            imagenCartel.setImage(imagen);
        } catch (MalformedURLException ex) {
            Logger.getLogger(ControladorGestion.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    private void insertarPelicula_OnAction(ActionEvent event) {
        
        LocalDate date = dateCalendar.getValue();
    
        if (!txtTitulo.getText().isEmpty() && !txtHora.getText().isEmpty() && !txtSala.getText().isEmpty() && ficheroImagen.exists()){
            String sesion=date.getYear()+"-"+date.getMonthValue()+"-"+date.getDayOfMonth()+"-"+txtHora.getText();
            if(cnx.comprobarSesionSala(sesion, Integer.valueOf(txtSala.getText()))){
                cnx.insertarPelicula(txtTitulo.getText(), sesion, Integer.valueOf(txtSala.getText()), ficheroImagen);
            }
            else{
                Alert alerta=new Alert(AlertType.ERROR,"Sala y sesión ocupados");
                alerta.show();
            }
            
        }else{
            Alert alerta=new Alert(AlertType.ERROR,"Faltan datos");
            alerta.show();
        }
        txtHora.setText("");
        txtSala.setText("");
        txtTitulo.setText("");
        txtTitulo.requestFocus();
    }

    @FXML
    private void modificarPelicula_OnAction(ActionEvent event) {
        
        LocalDate date = dateCalendar.getValue();
    
        if (!txtTitulo.getText().isEmpty() && !txtHora.getText().isEmpty() && !txtSala.getText().isEmpty()){
            String sesion=date.getYear()+"-"+date.getMonthValue()+"-"+date.getDayOfMonth()+"-"+txtHora.getText();
            if(cnx.comprobarSesionSala(sesion, Integer.valueOf(txtSala.getText()))){
                cnx.modificarSalaPelicula(txtTitulo.getText(), sesion, Integer.parseInt(txtSala.getText()));
            }
            else{
                Alert alerta=new Alert(AlertType.ERROR,"Sala y sesión ocupados");
                alerta.show();
            }
            
        }else{
            Alert alerta=new Alert(AlertType.ERROR,"Faltan datos");
            alerta.show();
        }
    }

    @FXML
    private void resetearCartelera_OnAction(ActionEvent event) {
        //cnx.resetearCartelera();
        facturacionSemanal();
    }

    @FXML
    private void btnFacturacionSemana_OnAction(ActionEvent event) {
        facturacionSemanal();
    }
    
    private void facturacionSemanal(){
        facturacionSemanal = cnx.obtenerFacturacionSemanal();
        int contador = 0;
        double precio = 5;
        for(int i=0; i<facturacionSemanal.size(); i++){
            cnx.insertarFacturacionHistorial(facturacionSemanal.get(i));
            contador++;
        }
        double total = contador*precio;
        lbFacturacionSemanal.setText("Total de entradas vendidas: " + contador);
        lbFacturacionSemanalEuros.setText("Total facturado: " + total + " €");
        cnx.limpiarFacturacionSemanal();
    }

    @FXML
    private void btnInsertarSala_OnAction(ActionEvent event) {
        cnx.insertarSala(txtIdSala.getText(), txtFilas.getText(), txtColumnas.getText());
        
        comboSalas.getItems().clear();
        listaSalas = cnx.obtenerSalas();
        comboSalas.setItems(listaSalas);
    }

    @FXML
    private void btnModificarSala_OnAction(ActionEvent event) {
        cnx.modificarSala(txtIdSala.getText(), txtFilas.getText(), txtColumnas.getText());
    }

    @FXML
    private void comboSalas_OnAction(ActionEvent event) {
        String sala = comboSalas.getValue();
        int idSala = Integer.parseInt(sala.substring(5));
        Sala s = new Sala();
        s = cnx.obtenerSala(idSala);
        txtIdSala.setText(String.valueOf(s.getIdSala()));
        txtFilas.setText(String.valueOf(s.getFilas()));
        txtColumnas.setText(String.valueOf(s.getColumnas()));
        
    }
    
}
