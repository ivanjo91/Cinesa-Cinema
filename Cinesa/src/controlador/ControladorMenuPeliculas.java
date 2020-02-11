package controlador;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.EventHandler;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import modelo.Cartelera;
import modelo.ConectarBD;

public class ControladorMenuPeliculas implements Initializable{
    
    ArrayList<Cartelera> listaCartelera = new ArrayList<>();
    public ImageView[] arrayImagenes;
    ConectarBD cnx = new ConectarBD();
    
    String cadenaQR = "";
    
    @FXML
    private AnchorPane ac;
    @FXML
    private AnchorPane acGestion;
    @FXML
    private TextField txtBarcode;
    @FXML
    private Label lbBandera;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        listaCartelera = cnx.mostrarCartelera();
        cargarCarteles();
    }
    
    private void cargarCarteles(){
        arrayImagenes = new ImageView[listaCartelera.size()];
        //Averiguar cuantas películas distinc hay en cartelera
        int nPeliculas = arrayImagenes.length;
        //Cuadricular
        int filas =(int)Math.ceil(Math.sqrt(nPeliculas));
        //k es el indice de peliculas
        int k=0;
        //Contruyo gridpane programatically
        GridPane gp = new GridPane();
        //Diseño del gridpane
        gp.setHgap(10);
        gp.setVgap(10);
        //Separación entre gp y ac
        gp.setPadding(new Insets(10, 10, 10, 10));
        gp.setAlignment(Pos.CENTER);
        for(int i=0; i<filas; i++){
            for(int j=0; j<filas; j++){
                if(k<nPeliculas){
                    InputStream entrada = new ByteArrayInputStream(listaCartelera.get(k).getCartel());
                    String titulo = listaCartelera.get(k).getTitulo();
                    arrayImagenes[k] = new ImageView();
                    arrayImagenes[k].setFitHeight(1500/nPeliculas);
                    arrayImagenes[k].setFitWidth(1500/nPeliculas);
                    arrayImagenes[k].setPreserveRatio(false);
                    arrayImagenes[k].setImage(new Image(entrada));
                    arrayImagenes[k].setOnMouseClicked(new EventHandler<MouseEvent>() {
                        @Override
                        public void handle(MouseEvent event) {
                            try {
                                //Cargar en un loader todos los componentes xml de
                                // la siguiente ventana
                                FXMLLoader loader = new FXMLLoader(getClass().getResource("/vista/FXMLSeleccionarSesion.fxml"));
                                AnchorPane ventanaDos = (AnchorPane)loader.load();
                                //Construyo el escenario
                                Stage ventana = new Stage();
                                ventana.setTitle("Seleccionar la sesión");
                                Scene escena = new Scene(ventanaDos);
                                ventana.setScene(escena);
                                ControladorSeleccionarSesion controlador2 = loader.getController();
                                controlador2.lbTitulo.setText(titulo);
                                controlador2.setCnx(cnx);
                                controlador2.setIdPelicula(titulo);
                                controlador2.mostrarSesiones();
                                controlador2.ventana2 = ventana;
                                ventana.show();
                            } catch (IOException ex) {
                                Logger.getLogger(ControladorMenuPeliculas.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    });
                    gp.add(arrayImagenes[k], j, i);
                    
                    k++;
                }
                
            }
        }
        //Extraer todos los componentes del gridpane y se agrega al ac
        ac.getChildren().add(gp);
        //Anclar los cuatro costados
        AnchorPane.setBottomAnchor(gp, 5d);
        AnchorPane.setLeftAnchor(gp, 5d);
        AnchorPane.setRightAnchor(gp, 5d);
        AnchorPane.setTopAnchor(gp, 5d);
    }

    @FXML
    private void KeyTyped_Barcode(KeyEvent event) {
        
        cadenaQR+=event.getCharacter();
        if (cadenaQR.length()>21){
            String codigo=cadenaQR.replace("'", "-").replaceFirst("Ñ", ":");
            String sesion=codigo.substring(0,16);
            int sala=Integer.valueOf(codigo.substring(16,18));
            int fila=Integer.valueOf(codigo.substring(18,20));
            int columna=Integer.valueOf(codigo.substring(20,22));
            if (cnx.comprobarAsiento(sesion, sala, fila, columna)){
                cnx.ocuparAsiento(sesion, sala, fila, columna);
                lbBandera.setText("Entrada correcta");
                lbBandera.setStyle("-fx-background-color:green");
               }
            else{
                lbBandera.setText("Entrada NO válida");
                lbBandera.setStyle("-fx-background-color:red");                     
               }
            cadenaQR="";
        txtBarcode.clear();
        txtBarcode.requestFocus();
        txtBarcode.setText("");
        }
        

    }

    @FXML
    private void GestionCartelera_ContextMenu(ContextMenuEvent event) {
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/vista/FXMLGestion.fxml"));
            Parent root = loader.load();
            ControladorGestion controladorGestion = loader.getController();
            Stage ventana = new Stage();
            ventana.setTitle("Gestión");
            Scene escena = new Scene(root);
            ventana.setScene(escena);
            ventana.show();
        } catch (IOException ex) {
            Logger.getLogger(ControladorMenuPeliculas.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }

    @FXML
    private void verPanelEntrada_OnScroll(ScrollEvent event) {
        acGestion.setVisible(true);
    }

}
