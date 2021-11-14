/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appagenda;

import entidades.Persona;
import entidades.Provincia;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.RollbackException;

/**
 * FXML Controller class
 *
 * @author Luis
 */
public class VentanaFormularioController implements Initializable {

    @FXML
    private Label lb_telefono;
    @FXML
    private Label lb_email;
    @FXML
    private Label lb_fechanac;
    @FXML
    private Label lb_numhijos;
    @FXML
    private Label lb_estadocivil;
    @FXML
    private Label lb_salario;
    @FXML
    private Label lb_jubilacion;
    @FXML
    private Label ln_provincia;
    @FXML
    private TextField tf_email;
    @FXML
    private DatePicker datepicker_fechanac;
    @FXML
    private TextField tf_numhijos;
    @FXML
    private RadioButton rb_soltero;
    @FXML
    private RadioButton rb_casado;
    @FXML
    private RadioButton rb_viudo;
    @FXML
    private TextField tf_salario;
    @FXML
    private CheckBox ch_jubilado;
    @FXML
    private ComboBox<Provincia> cb_provincia;
    @FXML
    private Label lb_imagen;
    @FXML
    private Button btn_examinar;
    @FXML
    private Button btn_guardar;
    @FXML
    private Button btn_cancelar;
    
    @FXML
    private AnchorPane rootPersonaDetalleView;
    
    @FXML
    private TextField tf_nombre;
    @FXML
    private TextField tf_apellidos;
    @FXML
    private TextField tf_telefono;
    
    
    private Pane rootAgendaView;
    private TableView tableViewPrevio;
    private Persona persona;
    private EntityManager entityManager;
    private boolean nuevaPersona;
    @FXML
    private Label lb_apellidos;
    @FXML
    private Label lb_nombre;
    
    public static final char CASADO='C';
    public static final char SOLTERO='S';
    public static final char VIUDO='V';
    
    public static final String CARPETA_FOTOS="src/appagenda/Fotos";
    @FXML
    private ImageView iv_imagen;
    @FXML
    private Button btn_suprimirFoto;

    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }   
    
     @FXML
    private void buttonGuardar(ActionEvent event) {
        boolean errorFormato = false;
        
        //Actualizamos los datos de la persona en cuestion
        if(!errorFormato){
            
            try{
                persona.setNombre(tf_nombre.getText());
                persona.setApellidos(tf_apellidos.getText());
                persona.setTelefono(tf_telefono.getText());
                persona.setEmail(tf_email.getText());
                
                //numero hijos
                if (!tf_numhijos.getText().isEmpty()){
                    try {
                        persona.setNumHijos(Short.valueOf(tf_numhijos.getText()));
                    } 
                    catch(NumberFormatException ex){
                        errorFormato = true;
                        Alert alert = new Alert(AlertType.INFORMATION, "Número de hijos no válido");
                        alert.showAndWait();
                        tf_numhijos.requestFocus();
                    }
                }
                
                //salario
                if (!tf_salario.getText().isEmpty()){
                    try {
                        persona.setSalario(BigDecimal.valueOf(Double.valueOf(tf_salario.getText()).doubleValue()));
                    } 
                    catch(NumberFormatException ex) {
                        errorFormato = true;
                        Alert alert = new Alert(AlertType.INFORMATION, "Salario no válido");
                        alert.showAndWait();
                        tf_salario.requestFocus();
                    }
                }
                
                //estado civil
                persona.setJubilado(ch_jubilado.isSelected());
                
                if (rb_casado.isSelected()){
                persona.setEstadoCivil(CASADO);
                } 
                else if (rb_soltero.isSelected()){
                persona.setEstadoCivil(SOLTERO);
                } 
                else if (rb_viudo.isSelected()){
                persona.setEstadoCivil(VIUDO);
                }
                
                //Fechas
                if (datepicker_fechanac.getValue() != null){
                    LocalDate localDate = datepicker_fechanac.getValue();
                    ZonedDateTime zonedDateTime =
                    localDate.atStartOfDay(ZoneId.systemDefault());
                    Instant instant = zonedDateTime.toInstant();
                    Date date = Date.from(instant);
                    persona.setFechaNacimiento(date);
                } 
                else {
                    persona.setFechaNacimiento(null);
                }
                
                //provincias
                if (cb_provincia.getValue() != null){
                    persona.setProvincia(cb_provincia.getValue());
                } 
                else {
                    Alert alert = new Alert(AlertType.INFORMATION,"Debe indicar una provincia");
                    alert.showAndWait();
                    errorFormato = true;
                }


                

                

                
                if (nuevaPersona){
                    entityManager.persist(persona);
                } 
                else {
                    entityManager.merge(persona);
                }
                entityManager.getTransaction().commit();

                //actualizamos los datos en el table view, si se trata de una nueva persona se selecciona la ultima fila, y si se trata de una existente se selecciona la linea en cuestion
                int numFilaSeleccionada;
                if (nuevaPersona){
                    tableViewPrevio.getItems().add(persona);
                    numFilaSeleccionada = tableViewPrevio.getItems().size()- 1;
                    tableViewPrevio.getSelectionModel().select(numFilaSeleccionada);
                    tableViewPrevio.scrollTo(numFilaSeleccionada);
                } 
                else {
                    numFilaSeleccionada=
                    tableViewPrevio.getSelectionModel().getSelectedIndex();
                    tableViewPrevio.getItems().set(numFilaSeleccionada,persona);
                }
                TablePosition pos = new TablePosition(tableViewPrevio,numFilaSeleccionada,null);
                tableViewPrevio.getFocusModel().focus(pos);
                tableViewPrevio.requestFocus();

                //volvemos a mostrar la agendaView
                StackPane rootMain =
                (StackPane) rootPersonaDetalleView.getScene().getRoot();
                rootMain.getChildren().remove(rootPersonaDetalleView);
                rootAgendaView.setVisible(true);
                
            }
             catch (RollbackException ex) { // Los datos introducidos no cumplen
                
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setHeaderText("No se han podido guardar los cambios.Compruebe que los datos cumplen los requisitos");
                alert.setContentText(ex.getLocalizedMessage());
                alert.showAndWait();
                }


        }
        
    }

    @FXML
    private void buttonCancelar(ActionEvent event) {
        
        //si le damos al boton cancelar no se insertara la nueva persona en la tabla y se nos devuelve a la fila qe estuviera seleccionada
        entityManager.getTransaction().rollback();
        int numFilaSeleccionada = tableViewPrevio.getSelectionModel().getSelectedIndex();
        TablePosition pos = new TablePosition(tableViewPrevio,numFilaSeleccionada,null);
        tableViewPrevio.getFocusModel().focus(pos);
        tableViewPrevio.requestFocus();
        
        //volvemos a mostrar la agendaView
        StackPane rootMain =
        (StackPane) rootPersonaDetalleView.getScene().getRoot();
        rootMain.getChildren().remove(rootPersonaDetalleView);
        rootAgendaView.setVisible(true);
    }
    
     public void setRootAgendaView(Pane rootAgendaView){
        this.rootAgendaView = rootAgendaView;
    }
     
    public void setTableViewPrevio(TableView tableViewPrevio){
        this.tableViewPrevio = tableViewPrevio;
        
    }
    
    public void setPersona(EntityManager entityManager, Persona persona,Boolean nuevaPersona){
        this.entityManager = entityManager;
        entityManager.getTransaction().begin();
        
        if (!nuevaPersona){
            this.persona=entityManager.find(Persona.class,persona.getId());
        } 
        else {
            this.persona=persona;
        }
            this.nuevaPersona=nuevaPersona;
    }
    
    public void mostrarDatos(){
        tf_nombre.setText(persona.getNombre());
        tf_apellidos.setText(persona.getApellidos());
        tf_telefono.setText(persona.getTelefono());
        tf_email.setText(persona.getEmail());
        // Falta implementar el código para el resto de controles
        
        if (persona.getNumHijos() != null){
            tf_numhijos.setText(persona.getNumHijos().toString());
        }
        if (persona.getSalario() != null){
            tf_salario.setText(persona.getSalario().toString());
        }
        if (persona.getJubilado() != null){
            ch_jubilado.setSelected(persona.getJubilado());
        }
        
        //estado civil
        if (persona.getEstadoCivil() != null){
            switch(persona.getEstadoCivil()){
            case CASADO:
            rb_casado.setSelected(true);
            break;
            case SOLTERO:
            rb_soltero.setSelected(true);
            break;
            case VIUDO:
            rb_viudo.setSelected(true);
            break;
            }
        }
        
        //fecha nacimiento
        if (persona.getFechaNacimiento() != null){
            Date date=persona.getFechaNacimiento();
            Instant instant=date.toInstant();
            ZonedDateTime zdt=instant.atZone(ZoneId.systemDefault());
            LocalDate localDate=zdt.toLocalDate();
            datepicker_fechanac.setValue(localDate);
        }


        //Provincia
        Query queryProvinciaFindAll= entityManager.createNamedQuery("Provincia.findAll");
        List listProvincia = queryProvinciaFindAll.getResultList();
        cb_provincia.setItems(FXCollections.observableList(listProvincia));
        
        
        if (persona.getProvincia() != null){
            cb_provincia.setValue(persona.getProvincia());
        }
        
        //Determinamos como se muestran los datos de provincia en este caso CA-CADIZ, codigo y nombre
        cb_provincia.setCellFactory(
            (ListView<Provincia> l)-> new ListCell<Provincia>(){
            @Override
                protected void updateItem(Provincia provincia, boolean empty){
                    super.updateItem(provincia, empty);
                    if (provincia == null || empty){
                    setText("");
                    } else {
                    setText(provincia.getCodigo()+"-"+provincia.getNombre());
                    }
                }
            });
        
        //Determinamos como se vera el combobox cuando todavia no se haya seleccionado nada en el
        cb_provincia.setConverter(new StringConverter<Provincia>(){
            @Override
            public String toString(Provincia provincia){
                if (provincia == null){
                    return "";
                } 
                else {
                    return provincia.getCodigo()+"-"+provincia.getNombre();
                }
            }

            @Override
            public Provincia fromString(String string) {
                return null; //To change body of generated methods, choose Tools | Templates.
            }
        });
        
        
        //Mostramos imagenes
        if (persona.getFoto() != null){
            String imageFileName=persona.getFoto();
            File file = new File(CARPETA_FOTOS+"/"+imageFileName);
                if (file.exists()){
                    Image image = new Image(file.toURI().toString());
                    iv_imagen.setImage(image);
                } 
                else {
                    Alert alert=new Alert(AlertType.INFORMATION,"No se encuentra la imagen en " + file.toURI().toString());
                    alert.showAndWait();
                }
            }
        
        
        
        
        
        
        
        
    }

    @FXML
    private void ButtonExaminar(ActionEvent event) {
        File carpetaFotos = new File(CARPETA_FOTOS);
        if (!carpetaFotos.exists()){
            carpetaFotos.mkdir();
        }
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar imagen");
        fileChooser.getExtensionFilters().addAll(
        new FileChooser.ExtensionFilter("Imágenes (jpg, png)", "*.jpg","*.png"),
        new FileChooser.ExtensionFilter("Todos los archivos","*.*"));
        File file = fileChooser.showOpenDialog(
        rootPersonaDetalleView.getScene().getWindow());
        if (file != null){
            try {
                    Files.copy(file.toPath(),new File(CARPETA_FOTOS+
                "/"+file.getName()).toPath());
                persona.setFoto(file.getName());
                Image image = new Image(file.toURI().toString());
                iv_imagen.setImage(image);
            } catch (FileAlreadyExistsException ex){
                Alert alert = new Alert(AlertType.WARNING,"Nombre de archivo duplicado");
                alert.showAndWait();
            } catch (IOException ex){
                Alert alert = new Alert(AlertType.WARNING,"No se ha podido guardar la imagen");
                alert.showAndWait();
            }

        }

    }

    @FXML
    private void ButtonSuprimirFoto(ActionEvent event) {
        
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Confirmar supresión de imagen");
        alert.setHeaderText("¿Desea SUPRIMIR el archivo asociado a la imagen,\n"+ "quitar la foto pero MANTENER el archivo, \no CANCELAR laoperación?");
        alert.setContentText("Elija la opción deseada:");
        ButtonType buttonTypeEliminar = new ButtonType("Suprimir");
        ButtonType buttonTypeMantener = new ButtonType("Mantener");
        ButtonType buttonTypeCancel = new ButtonType("Cancelar",
        ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(buttonTypeEliminar, buttonTypeMantener,
        buttonTypeCancel);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == buttonTypeEliminar){
            String imageFileName = persona.getFoto();
            File file = new File(CARPETA_FOTOS + "/" + imageFileName);
            if (file.exists()) {
                file.delete();
            }
            persona.setFoto(null);
            iv_imagen.setImage(null);
        } 
        else if(result.get() == buttonTypeMantener) {
            persona.setFoto(null);
            iv_imagen.setImage(null);
        } 
        
    }


    
}
