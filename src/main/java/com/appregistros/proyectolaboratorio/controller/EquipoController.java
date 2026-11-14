package com.appregistros.proyectolaboratorio.controller;

import com.appregistros.proyectolaboratorio.Dao.EquipoDao;
import com.appregistros.proyectolaboratorio.Dao.TipoEquipoDao;
import com.appregistros.proyectolaboratorio.model.Equipo;
import com.appregistros.proyectolaboratorio.model.TipoEquipo;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;

import java.util.ArrayList;
import java.util.List;

public class EquipoController {

    private final EquipoDao equipoDao = new EquipoDao();
    private final TipoEquipoDao tipoEquipoDao = new TipoEquipoDao();

    @FXML
    private TextField txtId;
    @FXML
    private TextField txtCodigo;
    @FXML
    private TextField txtNombre;
    @FXML
    private TextField txtMarca;
    @FXML
    private ComboBox<TipoEquipo> cmbTipo;
    @FXML
    private ComboBox<String> cmbEstado;
    @FXML
    private TextField txtCantidad;
    @FXML
    private TextField txtBuscar;
    @FXML
    private ComboBox<TipoEquipo> cmbFiltroTipo;
    @FXML
    private TableView<Equipo> tblEquipos;
    @FXML
    private TableColumn<Equipo, String> colCodigo;
    @FXML
    private TableColumn<Equipo, String> colNombre;
    @FXML
    private TableColumn<Equipo, String> colMarca;
    @FXML
    private TableColumn<Equipo, String> colEstado;
    @FXML
    private TableColumn<Equipo, Integer> colCantidad;
    @FXML
    private TableColumn<Equipo, String> colTipo;

    @FXML
    public void initialize() {
        configurarCombos();
        configurarTabla();
        cargarTipos();
        cargarEquipos();
    }

    private void configurarCombos() {
        cmbEstado.getItems().setAll("Disponible", "Prestado", "En mantenimiento", "Dañado", "Baja");

        cmbTipo.setConverter(new StringConverter<>() {
            @Override
            public String toString(TipoEquipo tipoEquipo) {
                return tipoEquipo == null ? "" : tipoEquipo.getNombre();
            }

            @Override
            public TipoEquipo fromString(String string) {
                return null;
            }
        });

        cmbFiltroTipo.setConverter(new StringConverter<>() {
            @Override
            public String toString(TipoEquipo tipoEquipo) {
                return tipoEquipo == null ? "Todos" : tipoEquipo.getNombre();
            }

            @Override
            public TipoEquipo fromString(String string) {
                return null;
            }
        });
    }

    private void configurarTabla() {
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colMarca.setCellValueFactory(new PropertyValueFactory<>("marca"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colTipo.setCellValueFactory(cellData -> {
            TipoEquipo tipoEquipo = cellData.getValue().getTipoEquipo();
            return new javafx.beans.property.SimpleStringProperty(tipoEquipo == null ? "" : tipoEquipo.getNombre());
        });

        tblEquipos.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                cargarDatosEquipo(newSelection);
            }
        });
    }

    private void cargarTipos() {
        List<TipoEquipo> tipos = tipoEquipoDao.listarTodos();
        List<TipoEquipo> opcionesFiltro = new ArrayList<>();
        opcionesFiltro.add(null);
        opcionesFiltro.addAll(tipos);

        cmbTipo.getItems().setAll(tipos);
        cmbFiltroTipo.getItems().setAll(opcionesFiltro);
        cmbFiltroTipo.setValue(null);
    }

    private void cargarEquipos() {
        List<Equipo> equipos = equipoDao.listarTodos();
        tblEquipos.setItems(FXCollections.observableArrayList(equipos));
    }

    private void cargarEquiposFiltrados() {
        String busqueda = txtBuscar.getText() == null ? "" : txtBuscar.getText().trim().toLowerCase();
        List<Equipo> equipos = equipoDao.listarTodos();
        ObservableList<Equipo> resultado = FXCollections.observableArrayList();

        for (Equipo equipo : equipos) {
            boolean coincideNombre = busqueda.isEmpty() || equipo.getNombre().toLowerCase().contains(busqueda);
            boolean coincideTipo = cmbFiltroTipo.getValue() == null || equipo.getTipoEquipo() != null && equipo.getTipoEquipo().getIdTipo().equals(cmbFiltroTipo.getValue().getIdTipo());
            if (coincideNombre && coincideTipo) {
                resultado.add(equipo);
            }
        }

        tblEquipos.setItems(resultado);
    }

    private void cargarDatosEquipo(Equipo equipo) {
        txtId.setText(equipo.getIdEquipo() == null ? "" : String.valueOf(equipo.getIdEquipo()));
        txtCodigo.setText(equipo.getCodigo());
        txtNombre.setText(equipo.getNombre());
        txtMarca.setText(equipo.getMarca() == null ? "" : equipo.getMarca());
        txtCantidad.setText(equipo.getCantidad() == null ? "" : String.valueOf(equipo.getCantidad()));
        cmbTipo.setValue(equipo.getTipoEquipo());
        cmbEstado.setValue(equipo.getEstado() == null ? "" : equipo.getEstado());
    }

    @FXML
    public void Guardar() {
        if (!validarCampos()) {
            return;
        }

        String codigo = txtCodigo.getText().trim();
        if (equipoDao.buscarPorCodigo(codigo) != null) {
            mostrarAlerta(Alert.AlertType.ERROR, "Código duplicado", "Ya existe un equipo con ese código.");
            return;
        }

        Equipo equipo = new Equipo();
        equipo.setCodigo(codigo);
        equipo.setNombre(txtNombre.getText().trim());
        equipo.setMarca(txtMarca.getText().trim());
        equipo.setTipoEquipo(cmbTipo.getValue());
        equipo.setEstado(cmbEstado.getValue());
        equipo.setCantidad(Integer.parseInt(txtCantidad.getText().trim()));

        equipoDao.guardar(equipo);
        limpiarFormulario();
        cargarEquipos();
        mostrarAlerta(Alert.AlertType.INFORMATION, "Equipo guardado", "El equipo se guardó correctamente.");
    }

    @FXML
    public void Actualizar() {
        if (txtId.getText() == null || txtId.getText().isBlank()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Selección requerida", "Seleccione un equipo de la tabla para actualizar.");
            return;
        }

        if (!validarCampos()) {
            return;
        }

        Long id = Long.parseLong(txtId.getText().trim());
        Equipo equipo = equipoDao.buscarPorId(id);
        if (equipo == null) {
            mostrarAlerta(Alert.AlertType.ERROR, "No encontrado", "No se encontró el equipo seleccionado.");
            return;
        }

        String codigo = txtCodigo.getText().trim();
        Equipo equipoConCodigo = equipoDao.buscarPorCodigo(codigo);
        if (equipoConCodigo != null && !equipoConCodigo.getIdEquipo().equals(id)) {
            mostrarAlerta(Alert.AlertType.ERROR, "Código duplicado", "Ya existe otro equipo con ese código.");
            return;
        }

        equipo.setCodigo(codigo);
        equipo.setNombre(txtNombre.getText().trim());
        equipo.setMarca(txtMarca.getText().trim());
        equipo.setTipoEquipo(cmbTipo.getValue());
        equipo.setEstado(cmbEstado.getValue());
        equipo.setCantidad(Integer.parseInt(txtCantidad.getText().trim()));

        equipoDao.guardar(equipo);
        limpiarFormulario();
        cargarEquipos();
        mostrarAlerta(Alert.AlertType.INFORMATION, "Equipo actualizado", "El equipo se actualizó correctamente.");
    }

    @FXML
    public void Eliminar() {
        if (txtId.getText() == null || txtId.getText().isBlank()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Selección requerida", "Seleccione un equipo para eliminar.");
            return;
        }

        Long id = Long.parseLong(txtId.getText().trim());
        equipoDao.eliminar(id);
        limpiarFormulario();
        cargarEquipos();
        mostrarAlerta(Alert.AlertType.INFORMATION, "Equipo eliminado", "El equipo fue eliminado correctamente.");
    }

    @FXML
    public void Limpiar() {
        limpiarFormulario();
    }

    @FXML
    public void onBuscar() {
        cargarEquiposFiltrados();
    }

    private boolean validarCampos() {
        if (txtCodigo.getText() == null || txtCodigo.getText().trim().isEmpty()) {
            mostrarAlerta(Alert.AlertType.ERROR, "Campo requerido", "El código es obligatorio.");
            return false;
        }
        if (txtNombre.getText() == null || txtNombre.getText().trim().isEmpty()) {
            mostrarAlerta(Alert.AlertType.ERROR, "Campo requerido", "El nombre es obligatorio.");
            return false;
        }
        if (cmbTipo.getValue() == null) {
            mostrarAlerta(Alert.AlertType.ERROR, "Campo requerido", "Debe seleccionar un tipo de equipo.");
            return false;
        }
        if (cmbEstado.getValue() == null || cmbEstado.getValue().isBlank()) {
            mostrarAlerta(Alert.AlertType.ERROR, "Campo requerido", "Debe seleccionar un estado.");
            return false;
        }
        if (txtCantidad.getText() == null || txtCantidad.getText().trim().isEmpty()) {
            mostrarAlerta(Alert.AlertType.ERROR, "Campo requerido", "La cantidad es obligatoria.");
            return false;
        }

        try {
            Integer.parseInt(txtCantidad.getText().trim());
        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Cantidad inválida", "La cantidad debe ser un número entero.");
            return false;
        }

        return true;
    }

    private void limpiarFormulario() {
        txtId.clear();
        txtCodigo.clear();
        txtNombre.clear();
        txtMarca.clear();
        txtCantidad.clear();
        cmbTipo.setValue(null);
        cmbEstado.setValue(null);
        tblEquipos.getSelectionModel().clearSelection();
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
