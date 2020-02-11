/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controlador;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.BarcodeQRCode;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;
import java.awt.Desktop;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import modelo.ConectarBD;
import modelo.Facturado;
import modelo.Sala;

/**
 * FXML Controller class
 *
 * @author David
 */
public class ControladorSala implements Initializable {
    
    ArrayList<Facturado> listaFacturado = new ArrayList<>();
    ArrayList<Sala> listaCesta = new ArrayList<>();
    Sala salaX = new Sala();
    double precio = 5.5;
    ConectarBD cnx = new ConectarBD();
    Button[] arrayBotones;
    public int idSala;
    public String sesion;
    
    public Stage ventanaSala;

    public ArrayList<Facturado> getListaFacturado() {
        return listaFacturado;
    }

    public void setListaFacturado(ArrayList<Facturado> listaFacturado) {
        this.listaFacturado = listaFacturado;
    }

    public Sala getSalaX() {
        return salaX;
    }

    public void setSalaX(Sala salaX) {
        this.salaX = salaX;
    }

    @FXML
    private AnchorPane ac;
    @FXML
    private AnchorPane acBotonera;
    @FXML
    private Button btnPagar;
    @FXML
    public Label lbSala;
    @FXML
    public Label lbPelicula;
    @FXML
    public Label lbSesion;
    @FXML
    private AnchorPane acButacas;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    

    public void cargarButacas(Sala salaX, ArrayList<Facturado> listaFacturadoSesion) {
        GridPane gp = new GridPane();
        gp.setHgap(5);
        gp.setVgap(5);
        gp.setAlignment(Pos.CENTER);
        gp.setPadding(new Insets(10, 10, 10, 10));
        arrayBotones = new Button[(salaX.getFilas())*(salaX.getColumnas())];
        int k=0;
        for(int i=0; i<salaX.getFilas(); i++){
            for(int j=0; j<salaX.getColumnas(); j++){
                arrayBotones[k] = new Button();
                arrayBotones[k].setText("F"+i+":C"+j);
                arrayBotones[k].setStyle("-fx-background-color:green");
                for(int v=0; v<listaFacturadoSesion.size(); v++){
                    int fila = listaFacturadoSesion.get(v).getFila();
                    int columna = listaFacturadoSesion.get(v).getColumna();
                    if(fila==i && columna==j){
                        arrayBotones[k].setStyle("-fx-background-color:yellow");
                        arrayBotones[k].setDisable(true);
                    }
                }
                Sala butaca = new Sala();
                butaca.setFilas(i);
                butaca.setColumnas(j);
                
                arrayBotones[k].setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        Button botonX = (Button)event.getSource();
                        if(botonX.getStyle().contains("-fx-background-color:red")){
                            botonX.setStyle("-fx-background-color:green");
                            listaCesta.remove(butaca);
                        }
                        else{
                            botonX.setStyle("-fx-background-color:red");
                            listaCesta.add(butaca);
                        }
                    }
                });
                gp.add(arrayBotones[k], j, i);
                k++;
            } //Cierre del segundo for (columnas)
        } //Cierre del primer for (filas)
        acButacas.getChildren().add(gp);
    }

    @FXML
    private void btnPagar_OnAction(ActionEvent event) {
        double totalPagar = listaCesta.size()*precio;
        System.out.println(totalPagar);
        //Insertar datos de la cesta en la tabla facturacionCine
        for(int i=0; i<listaCesta.size(); i++){
            cnx.insertarButacas(listaCesta.get(i).getFilas(), listaCesta.get(i).getColumnas(), idSala, sesion);
            imprimirTicket(idSala, sesion, listaCesta.get(i).getFilas(), listaCesta.get(i).getColumnas());
        }
        //Actualizar la interfaz de butacas desactivando las butacas que acabo de vender
        for(int i=0; i<arrayBotones.length; i++){
            if(arrayBotones[i].getStyle().contains("-fx-background-color:red")){
                arrayBotones[i].setDisable(true);
            }
        }
        
    }
    
    private void imprimirTicket(int sala, String sesion, int filas, int columnas) {
        /*
        try {

            System.out.println("Imprimir ticket");

            //el nombre del fichero no puede llevar caracteres NO válidos
            String cadenaFile=sesion.replaceAll("-", "_").replaceFirst(":", "_");

            //la composición del fichero debe contener además de la sesión + fila + columna
            OutputStream file=new FileOutputStream(new File(cadenaFile+"_"+filas+"_"+columnas+".pdf"));
            Document document = new Document();
            PdfWriter pdfWriter = PdfWriter.getInstance(document, file);
            document.open();
            PdfContentByte pdfContentByte = pdfWriter.getDirectContent();
            PdfWriter.getInstance(document, file);
            document.open();
            //generar el códido QR
            
            String cadenaSala = ""+filas;
            String cadenaFilas = ""+columnas;
            String cadenaColumnas = ""+sala;
            if(filas<10) cadenaFilas = "0" + filas;
            if(columnas<10) cadenaColumnas = "0" + columnas;
            if(sala<10) cadenaSala = "0" + sala;
            String cadena=sesion+String.valueOf(cadenaSala)+String.valueOf(cadenaFilas)+String.valueOf(cadenaColumnas);
            BarcodeQRCode barcodeQrcode = new BarcodeQRCode(cadena, 1, 1, null);
            Image qrcodeImage = barcodeQrcode.getImage();
            // qrcodeImage.setAbsolutePosition(50, 50);
            qrcodeImage.scalePercent(300);
            document.add(qrcodeImage);
            //generar contenido del pdf

            document.add(new Paragraph("Sala: "+sala));
            document.add(new Paragraph("Sesion: "+sesion));
            document.add(new Paragraph("Fila: "+filas));
            document.add(new Paragraph("Columna: "+columnas));

            document.add(new Paragraph(new Date().toString()));
            String nombreFichero=cadenaFile+"_"+filas+"_"+columnas+".pdf";
            File ficheroPDF=new File(nombreFichero);
            //mostrar en pantalla el pdf
            Desktop.getDesktop().open(ficheroPDF);
            // enviar_correo(nombreFichero);
            document.close();
            file.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        */
        
        
        
        Document document=new Document();
        String cadenaFile=sesion.replaceAll("-", "_").replaceFirst(":", "_");
        try (FileOutputStream fos = new FileOutputStream("C:\\Users\\David\\Desktop\\"+cadenaFile+"_"+filas+"_"+columnas+".pdf")) {
         PdfWriter.getInstance(document, fos);
        document.open();
 
        String cadenaFila=""+filas;
        String cadenaCol=""+columnas;
        String cadenaSala=""+sala;
        if (sala<9) cadenaSala="0"+sala;
        if (filas<9) cadenaFila="0"+filas;
        if (columnas<9) cadenaCol="0"+columnas;
        String cadena=sesion+cadenaSala+cadenaFila+cadenaCol;
        BarcodeQRCode barcodeQrcode = new BarcodeQRCode(cadena, 1, 1, null);
        Image qrcodeImage = barcodeQrcode.getImage();
        //      qrcodeImage.setAbsolutePosition(50, 50);
        qrcodeImage.scalePercent(300);
        document.add(qrcodeImage);
        document.add(new Paragraph("sala: "+sala));
        document.add(new Paragraph("Sesion: "+sesion));
        document.add(new Paragraph("Fila: "+filas));
        document.add(new Paragraph("Columna: "+columnas));
        document.add(new Paragraph(new Date().toString()));
        document.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ControladorSala.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ControladorSala.class.getName()).log(Level.SEVERE, null, ex);
        } catch (DocumentException ex) {
            Logger.getLogger(ControladorSala.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
