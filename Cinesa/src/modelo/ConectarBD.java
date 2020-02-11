/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modelo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 * @author David
 */
public class ConectarBD {
    
    Connection cnx;
    Statement st;
    PreparedStatement pstm;
    ResultSet rs;
    ArrayList<Cartelera> listaCartelera = new ArrayList<>();
    ObservableList<Cartelera> listaDatos = FXCollections.observableArrayList();
    ArrayList<Facturado> listaFacturado = new ArrayList<>();
    
    
    public ConectarBD(){
        try {
            Class.forName("com.mysql.jdbc.Driver");
            cnx=DriverManager.getConnection("jdbc:mysql://remotemysql.com/Pr1mdxAdrh", "Pr1mdxAdrh", "fNBUrxid1O");
            st=cnx.createStatement();
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ConectarBD.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(ConectarBD.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public ArrayList<Cartelera> mostrarCartelera(){
        try {
            String cadenaSql = "select distinct(titulo),cartel from cartelera";
            rs = st.executeQuery(cadenaSql);
            while(rs.next()){
                Cartelera cartel = new Cartelera();
                cartel.setTitulo(rs.getString(1));
                cartel.setCartel(rs.getBytes(2));
                listaCartelera.add(cartel);
            }
        } catch (SQLException ex) {
            Logger.getLogger(ConectarBD.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return listaCartelera;
    }
    
    public ArrayList<Cartelera> mostrarTodasSesiones(String idPelicula){
        try {

            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd-hh:mm");
            Date jumanji = new Date();
            String fechaHoy = format.format(jumanji.getTime());
            System.out.println("FECHA HOY: " + fechaHoy);
            String cadenaSql="select * from cartelera where titulo=?";
            PreparedStatement pstm = cnx.prepareStatement(cadenaSql);
            pstm.setString(1,idPelicula);
            rs=pstm.executeQuery();

            while (rs.next()){
		Date sesion = format.parse(rs.getString(3));
		long sd =(sesion.getTime() - jumanji.getTime());
                System.out.println("Fecha Sesion: "+ sd);
		if(sd >= 0){
                    Cartelera cc=new Cartelera(rs.getInt(1),rs.getString(2),rs.getString(3),rs.getInt(4),rs.getBytes(5));
	            System.out.println(cc.getSesion());
	            listaCartelera.add(cc);
                }
            }

        } catch (SQLException ex) {
            Logger.getLogger(ConectarBD.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
	}
       
        return listaCartelera;
    }
    
    public ObservableList<Cartelera> mostrarCarteleraDos(String idPelicula){
        try {
            
            String cadenaSql = "select * from cartelera where titulo=?";
            pstm = cnx.prepareStatement(cadenaSql);
            pstm.setString(1, idPelicula);
            
            rs = pstm.executeQuery();
            while(rs.next()){
                Cartelera cc = new Cartelera(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getInt(4), rs.getBytes(5));
                //System.out.println(cc.getSesion());
                listaDatos.add(cc);
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(ConectarBD.class.getName()).log(Level.SEVERE, null, ex);
        }
        return listaDatos;
    }

    public ArrayList<Facturado> cargarOcupacionSala(int sala, String sesion) {
        try {
            String cadenaSql = "select * from facturacionCine where idSala=? and sesion=?";
            pstm = cnx.prepareStatement(cadenaSql);
            pstm.setInt(1, sala);
            pstm.setString(2, sesion);
            rs = pstm.executeQuery();
            while(rs.next()){
                Facturado f = new Facturado(rs.getInt(1), rs.getInt(2), rs.getInt(3), rs.getInt(4), rs.getString(5));
                listaFacturado.add(f);
            }
        } catch (SQLException ex) {
            Logger.getLogger(ConectarBD.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return listaFacturado;
    }

    public Sala cargarSala(int sala) {
        Sala resultado = null;
        
        try {
            String cadenaSql = "select * from salaCine where idSala=?";
            pstm = cnx.prepareStatement(cadenaSql);
            pstm.setInt(1, sala);
            rs = pstm.executeQuery();
            if(rs.next()){
                resultado = new Sala(rs.getInt(1), rs.getInt(2), rs.getInt(3));
            }
        } catch (SQLException ex) {
            Logger.getLogger(ConectarBD.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return resultado;
    }

    public void insertarButacas(int fila, int columna, int sala, String sesion) {
        try {
            String cadenaSql = "insert into facturacionCine(idSala, fila, columna, sesion, ocupado) values(?, ?, ?, ?, 0)";
            pstm = cnx.prepareStatement(cadenaSql);
            pstm.setInt(1, sala);
            pstm.setInt(2, fila);
            pstm.setInt(3, columna);
            pstm.setString(4, sesion);
            pstm.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(ConectarBD.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public boolean comprobarAsiento(String sesion, int sala, int fila, int columna) {
       boolean resultado = true;
       
       try {
            String cadenaSql = "select * from facturacionCine where sesion=? and idSala=? and fila=? and columna=?";
            pstm = cnx.prepareStatement(cadenaSql);
            pstm.setString(1, sesion);
            pstm.setInt(2, sala);
            pstm.setInt(3, fila);
            pstm.setInt(4, columna);
            rs = pstm.executeQuery();
            while(rs.next()){
                if(rs.getInt(6)==1){
                    resultado = false;
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(ConectarBD.class.getName()).log(Level.SEVERE, null, ex);
        }
       
       return resultado;
    }

    public void ocuparAsiento(String sesion, int sala, int fila, int columna) {
        
        try {
            String cadenaSql = "update facturacionCine set ocupado=1 where sesion=? and idSala=? and fila=? and columna=?";
            pstm = cnx.prepareStatement(cadenaSql);
            pstm.setString(1, sesion);
            pstm.setInt(2, sala);
            pstm.setInt(3, fila);
            pstm.setInt(4, columna);
            pstm.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(ConectarBD.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void insertarPelicula(String titulo, String sesion, int sala, File imagen) {
        
        try {
            FileInputStream ficheroImagen = new FileInputStream(imagen);
            
            String cadenaSql = "insert into cartelera(titulo, sesion, sala, cartel) values(?, ?, ?, ?)";
            pstm = cnx.prepareStatement(cadenaSql);
            pstm.setString(1, titulo);
            pstm.setString(2, sesion);
            pstm.setInt(3, sala);
            pstm.setBinaryStream(4, ficheroImagen, (int)imagen.length());
            pstm.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(ConectarBD.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ConectarBD.class.getName()).log(Level.SEVERE, null, ex);
        }
        }

    public void resetearCartelera() {
        try {
            String cadenaSql="truncate cartelera";
            st=cnx.createStatement();
            st.executeUpdate(cadenaSql);
        } catch (SQLException ex) {
            Logger.getLogger(ConectarBD.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void modificarSalaPelicula(String titulo, String sesion, int sala) {
        try {
            String cadenaSql="update cartelera set sala=? where titulo=? and sesion=?";
            pstm = cnx.prepareStatement(cadenaSql);
            pstm.setInt(1, sala);
            pstm.setString(2, titulo);
            pstm.setString(3, sesion);
            pstm.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(ConectarBD.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
    }

    public boolean comprobarSesionSala(String sesion, int sala) {
        boolean libre = true;
        
        try {
            String cadenaSql="select * from cartelera where sesion=? and sala=?";
            pstm = cnx.prepareStatement(cadenaSql);
            pstm.setString(1, sesion);
            pstm.setInt(2, sala);
            rs = pstm.executeQuery();
            if(rs.next()){
                libre = false;
            }
        } catch (SQLException ex) {
            Logger.getLogger(ConectarBD.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return libre;
    }

    public ArrayList<Facturado> obtenerFacturacionSemanal() {
        ArrayList<Facturado> listaFacturado = new ArrayList<>();
        SimpleDateFormat formato = new SimpleDateFormat("yyyy/MM/dd");
        
        try {
            String cadenaSql = "select * from facturacionCine";
            rs = st.executeQuery(cadenaSql);
            while(rs.next()){
                Facturado f = new Facturado(rs.getInt(1), rs.getInt(2), rs.getInt(3), rs.getInt(4), rs.getString(5));
                listaFacturado.add(f);   
            }
        } catch (SQLException ex) {
            Logger.getLogger(ConectarBD.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return listaFacturado;
    }

    public void insertarFacturacionHistorial(Facturado f) {
        try {
            String cadenaSql = "insert into facturacionCineHistorial(idSala, fila, columna, sesion) values(?, ?, ?, ?)";
            pstm = cnx.prepareStatement(cadenaSql);
            pstm.setInt(1, f.getIdSala());
            pstm.setInt(2, f.getFila());
            pstm.setInt(3, f.getColumna());
            pstm.setString(4, f.getSesion());
            pstm.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(ConectarBD.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void limpiarFacturacionSemanal() {
        try {
            String cadenaSql="truncate facturacionCine";
            st=cnx.createStatement();
            st.executeUpdate(cadenaSql);
        } catch (SQLException ex) {
            Logger.getLogger(ConectarBD.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public ObservableList<String> obtenerSalas() {
        ObservableList<String> listaSalas = FXCollections.observableArrayList();
        try {
            String cadenaSql = "select * from salaCine";
            rs = st.executeQuery(cadenaSql);
            while(rs.next()){
                listaSalas.add("Sala " + rs.getInt(1));
            }
        } catch (SQLException ex) {
            Logger.getLogger(ConectarBD.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return listaSalas;
    }

    public Sala obtenerSala(int idSala) {
        Sala s = new Sala();
        try {
            String cadenaSql = "select * from salaCine where idSala=?";
            pstm = cnx.prepareStatement(cadenaSql);
            pstm.setInt(1, idSala);
            rs = pstm.executeQuery();
            if(rs.next()){
                s.setIdSala(rs.getInt(1));
                s.setFilas(rs.getInt(2));
                s.setColumnas(rs.getInt(3));
            }
        } catch (SQLException ex) {
            Logger.getLogger(ConectarBD.class.getName()).log(Level.SEVERE, null, ex);
        }
        return s;
    }

    public void insertarSala(String idSala, String filas, String columnas) {
        try {
            String cadenaSql = "insert into salaCine(idSala, filas, columnas) values(?, ?, ?)";
            pstm = cnx.prepareStatement(cadenaSql);
            pstm.setInt(1, Integer.parseInt(idSala));
            pstm.setInt(2, Integer.parseInt(filas));
            pstm.setInt(3, Integer.parseInt(columnas));
            pstm.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(ConectarBD.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void modificarSala(String idSala, String filas, String columnas) {
        
        try {
            String cadenaSql = "update salaCine set filas=?, columnas=? where idSala=?";
            pstm = cnx.prepareStatement(cadenaSql);
            pstm.setInt(1, Integer.parseInt(filas));
            pstm.setInt(2, Integer.parseInt(columnas));
            pstm.setInt(3, Integer.parseInt(idSala));
            pstm.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(ConectarBD.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
}
