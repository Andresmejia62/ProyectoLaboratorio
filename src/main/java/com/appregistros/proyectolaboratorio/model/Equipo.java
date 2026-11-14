package com.appregistros.proyectolaboratorio.model;

import jakarta.persistence.*;

@Entity
@Table(name = "equipo", schema = "inventario")
public class Equipo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_equipo")
    private Long idEquipo;

    @Column(name = "codigo", nullable = false, unique = true, length = 10)
    private String codigo;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "marca", length = 100)
    private String marca;

    @Column(name = "estado", nullable = false, length = 20)
    private String estado;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_tipo", nullable = false,
                foreignKey = @ForeignKey(name = "fk_equipo_tipo_equipo"))
    private TipoEquipo tipoEquipo;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    public Equipo() {
    }

    public Equipo(String codigo, String nombre, String marca, String estado, TipoEquipo tipoEquipo, Integer cantidad) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.marca = marca;
        this.estado = estado;
        this.tipoEquipo = tipoEquipo;
        this.cantidad = cantidad;
    }

    public Long getIdEquipo() {
        return idEquipo;
    }

    public void setIdEquipo(Long idEquipo) {
        this.idEquipo = idEquipo;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public TipoEquipo getTipoEquipo() {
        return tipoEquipo;
    }

    public void setTipoEquipo(TipoEquipo tipoEquipo) {
        this.tipoEquipo = tipoEquipo;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }
}
