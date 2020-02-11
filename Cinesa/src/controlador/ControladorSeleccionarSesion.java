/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controlador;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import modelo.Cartelera;
import modelo.ConectarBD;
import modelo.Facturado;
import modelo.Sala;

/**
 * FXML Controller class
 *
 * @author David
 */
public class ControladorSeleccionarSesion implements Initializable {
    
    ArrayList<Cartelera> listaCartelera = new ArrayList<>();
    Cartelera e;
    ObservableList <Cartelera> listaDatos = FXCollections.observableArrayList();
    ConectarBD cnx;
    ConectarBD cnx2 = new ConectarBD();
    String idPelicula;
    public Stage ventana2;

    public String getIdPelicula() {
        return idPelicula;
    }

    public void setIdPelicula(String idPelicula) {
        this.idPelicula = idPelicula;
    }
    
    public ConectarBD getCnx() {
        return cnx;
    }

    public void setCnx(ConectarBD cnx) {
        this.cnx = cnx;
    }

    @FXML
    private ImageView imgCartel;
    @FXML
    private ListView<Cartelera> listSesiones;
    @FXML
    public Label lbTitulo;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    
    
    public void mostrarSesiones(){
        //Listado de todas las sesiones de la pelicula seleccionada
        listaCartelera = cnx2.mostrarTodasSesiones(idPelicula);
        System.out.println(listaCartelera.get(0).getSesion());
        
        InputStream entrada = new ByteArrayInputStream(listaCartelera.get(0).getCartel());
        imgCartel.setImage(new Image(entrada));
        listaDatos = cnx2.mostrarCarteleraDos(idPelicula);
        listSesiones.setItems(listaDatos);
    }

    @FXML
    private void seleccionarSesion_OnMouseClicked(MouseEvent event) {
        //Posición en el list, que coincide con posicion en listaCartelera
        int posicion = listSesiones.getSelectionModel().getSelectedIndex();
        //Con posicion extraigo los datos en listaCartelera
        int sala = listaCartelera.get(posicion).getSala();
        String sesion = listaCartelera.get(posicion).getSesion();
        ArrayList<Facturado> listaFacturadoSesion = cnx.cargarOcupacionSala(sala, sesion);
        Sala salaX = cnx.cargarSala(sala);
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/vista/FXMLSala.fxml"));
            Parent root = loader.load();
            ControladorSala controlSala = loader.getController();
            controlSala.setSalaX(salaX);
            controlSala.setListaFacturado(listaFacturadoSesion);
            controlSala.lbSala.setText(sala+"");
            controlSala.idSala = sala;
            controlSala.lbSesion.setText(sesion);
            controlSala.sesion = sesion;
            controlSala.lbPelicula.setText(listaCartelera.get(posicion).getTitulo());
            controlSala.cargarButacas(salaX, listaFacturadoSesion);
            Stage ventana3 = new Stage();
            controlSala.ventanaSala = ventana3;
            ventana3.setFullScreenExitHint("Pulsa ESC");
            ventana3.setFullScreen(true);
            Scene escena = new Scene(root);
            ventana3.setScene(escena);
            ventana3.show();
            ventana2.close();
            
        } catch (IOException ex) {
            Logger.getLogger(ControladorSeleccionarSesion.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
