/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appagenda;

import entidades.Persona;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javax.persistence.EntityManager;
import javax.persistence.Query;

/**
 * FXML Controller class
 *
 * @author Luis
 */
public class AgendaViewController implements Initializable {

    /**
     * Initializes the controller class.
    */
    // se nos genera automaticamente y en las interrogaciones
    //en el caso del tableView se mostraran objetos de tipo persona
    //y en las columnas tambien se debera indicar el tipo de dato
    
    private EntityManager entityManager;
    @FXML
    private TableView<Persona> table_persona;
    @FXML
    private TableColumn<Persona, String> columnNombre;
    @FXML
    private TableColumn<Persona, String> columnApellidos;
    @FXML
    private TableColumn<Persona, String> columnEmail;
    @FXML
    private TableColumn<Persona, String> columnProvincia;
    @FXML
    private Label lb_nombre;
    @FXML
    private Label lb_apellidos;
    @FXML
    private TextField tf_nombre;
    @FXML
    private TextField tf_apellidos;
    
    private Persona personaSeleccionada;
    
    @FXML
    private AnchorPane rootAgendaView;
    @FXML
    private Button btn_ingresar;
    @FXML
    private Button btn_nuevo;
    @FXML
    private Button btn_editar;
    @FXML
    private Button btn_suprimir;
  
    
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        //Asociamos cada una de las columnas a su propiedad especifica
        columnNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        columnApellidos.setCellValueFactory(new PropertyValueFactory<>("apellidos"));
        columnEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        columnProvincia.setCellValueFactory(
            cellData->{
                SimpleStringProperty property=new SimpleStringProperty();
                    if (cellData.getValue().getProvincia()!=null){
                     property.setValue(cellData.getValue().getProvincia().getNombre());
                    }
            return property;
            });
        
        //Cuando seleccionemos una fila se guardaran dichos datos de esa persona en una variable tipo persona
        table_persona.getSelectionModel().selectedItemProperty().addListener(
                (observable,oldValue,newValue)->{
                    personaSeleccionada=newValue;
                    //Ademas mostraremos sus datos en los textField 
                    if (personaSeleccionada != null){
                        tf_nombre.setText(personaSeleccionada.getNombre());
                        tf_apellidos.setText(personaSeleccionada.getApellidos());
                        } else {
                        tf_nombre.setText("");
                        tf_apellidos.setText("");
                        }

                });
    }

    //seteamos el entity manager
    public void setEntityManager(EntityManager entityManager){
        
        this.entityManager = entityManager;
    }
    
    //Metodo para cargar toda la lista de Personas
    public void cargarTodasPersonas(){
        
        Query queryPersonaFindAll = entityManager.createNamedQuery("Persona.findAll");
        List<Persona> listPersona = queryPersonaFindAll.getResultList();
        
        //Set items requiere convertir la lista de tipo List a observableArrayList
        table_persona.setItems(FXCollections.observableArrayList(listPersona));  
        
    }

    //Metodo boton guardar acceso rapido a datos de la tabla
    @FXML
    private void botonGuardar(ActionEvent event) {
        
        if(personaSeleccionada != null){
            //recogemos los datos del textField
            personaSeleccionada.setNombre(tf_nombre.getText());
            personaSeleccionada.setApellidos(tf_apellidos.getText());
            
            //introducimos los datos en la base de datos
            entityManager.getTransaction().begin();
            entityManager.merge(personaSeleccionada);
            entityManager.getTransaction().commit();
            
            //Actualizamos el table view con los nuevos valores
            int numFilaSeleccionada = table_persona.getSelectionModel().getSelectedIndex();
            table_persona.getItems().set(numFilaSeleccionada,personaSeleccionada);
            
            //hacemos que el foco vuelva a la tabla y no se quede en el boton guardar
            TablePosition pos = new TablePosition(table_persona,numFilaSeleccionada,null);
            table_persona.getFocusModel().focus(pos);
            table_persona.requestFocus();

        }
    }

    @FXML
    private void onActionButtonNuevo(ActionEvent event) {
        
        try{
            // Cargar la vista de detalle
            FXMLLoader fxmlLoader=new
            FXMLLoader(getClass().getResource("ventanaFormulario.fxml"));
            Parent rootDetalleView=fxmlLoader.load();
            // Ocultar la vista de la lista
            rootAgendaView.setVisible(false);
             //Añadir la vista detalle al StackPane principal para que se muestre
            StackPane rootMain = (StackPane) rootAgendaView.getScene().getRoot();
            rootMain.getChildren().add(rootDetalleView);
            
            VentanaFormularioController ventanaDetalle = (VentanaFormularioController) fxmlLoader.getController();
            ventanaDetalle.setRootAgendaView(rootAgendaView);
            
            //Intercambio de datos funcionales con el detalle
            ventanaDetalle.setTableViewPrevio(table_persona);
            
            // Para el botón Nuevo:
            personaSeleccionada = new Persona();
            ventanaDetalle.setPersona(entityManager,personaSeleccionada,true);
            
            //mostramos datos
            ventanaDetalle.mostrarDatos();
            
        } catch (IOException ex){
            Logger.getLogger(AgendaViewController.class.getName()).log(Level.SEVERE,null,ex);
        }

    }

    @FXML
    private void onActionButtonEditar(ActionEvent event) {
        
         try{
            // Cargar la vista de detalle
            FXMLLoader fxmlLoader=new
            FXMLLoader(getClass().getResource("ventanaFormulario.fxml"));
            Parent rootDetalleView=fxmlLoader.load();
            // Ocultar la vista de la lista
            rootAgendaView.setVisible(false);
             //Añadir la vista detalle al StackPane principal para que se muestre
            StackPane rootMain = (StackPane) rootAgendaView.getScene().getRoot();
            rootMain.getChildren().add(rootDetalleView);
            
            VentanaFormularioController ventanaDetalle = (VentanaFormularioController) fxmlLoader.getController();
            ventanaDetalle.setRootAgendaView(rootAgendaView);
            
            //Intercambio de datos funcionales con el detalle
            ventanaDetalle.setTableViewPrevio(table_persona);
            
            // Para el botón Editar
            ventanaDetalle.setPersona(entityManager,personaSeleccionada,false);
            
            //mostramos datos
            ventanaDetalle.mostrarDatos();

            
        } catch (IOException ex){
            Logger.getLogger(AgendaViewController.class.getName()).log(Level.SEVERE,null,ex);
        }
    }

    @FXML
    private void onActionButtonSuprimir(ActionEvent event) {
        
        //Con este codigo nos saltara una ventana de emergencia cuando le demos a borrar una persona
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Confirmar");
        alert.setHeaderText("¿Desea suprimir el siguiente registro?");
        alert.setContentText(personaSeleccionada.getNombre() + " " + personaSeleccionada.getApellidos());
        Optional<ButtonType> result = alert.showAndWait();
        
        if (result.get() == ButtonType.OK){
            // Acciones a realizar si el usuario acepta, borraremos la persona seleccionada
            entityManager.getTransaction().begin();
            entityManager.merge(personaSeleccionada);
            entityManager.remove(personaSeleccionada);
            entityManager.getTransaction().commit();
            
            table_persona.getItems().remove(personaSeleccionada);
            table_persona.getFocusModel().focus(null);
            table_persona.requestFocus();

        } 
        else {
            // Acciones a realizar si el usuario cancela
            int numFilaSeleccionada= table_persona.getSelectionModel().getSelectedIndex();
            table_persona.getItems().set(numFilaSeleccionada,personaSeleccionada);
            TablePosition pos = new TablePosition(table_persona,numFilaSeleccionada,null);
            table_persona.getFocusModel().focus(pos);
            table_persona.requestFocus();

        }

    }
    
}
