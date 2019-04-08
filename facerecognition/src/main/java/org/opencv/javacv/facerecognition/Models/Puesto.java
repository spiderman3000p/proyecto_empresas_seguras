package org.opencv.javacv.facerecognition.Models;

/**
 * Created by Rafa on 09/08/2018.
 */

public class Puesto {
    private int id, compania_id;
    private String estado, nominativo, descripcion, nombre;
    private Compania compania;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCompania_id() {
        return compania_id;
    }

    public void setCompania_id(int compania_id) {
        this.compania_id = compania_id;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getNominativo() {
        return nominativo;
    }

    public void setNominativo(String nominativo) {
        this.nominativo = nominativo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
