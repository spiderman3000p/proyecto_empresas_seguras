package org.opencv.javacv.facerecognition.Models;

/**
 * Created by Rafa on 09/08/2018.
 */

public class Radio {
    private int usuario_id, puesto_id;
    private boolean responde;
    private String timestamp;
    private Usuario usuario;

    public int getUsuario_id() {
        return usuario_id;
    }

    public void setUsuario_id(int usuario_id) {
        this.usuario_id = usuario_id;
    }

    public int getPuesto_id() {
        return puesto_id;
    }

    public void setPuesto_id(int puesto_id) {
        this.puesto_id = puesto_id;
    }

    public boolean isResponde() {
        return responde;
    }

    public void setResponde(boolean responde) {
        this.responde = responde;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
}
