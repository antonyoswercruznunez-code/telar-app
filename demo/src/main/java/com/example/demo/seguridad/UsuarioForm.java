package com.example.demo.seguridad;

public class UsuarioForm {
    private String nombre;
    private String correo;
    private String clave;
    private String confirmarClave;
    private String rol = "VENDEDOR";

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getClave() { return clave; }
    public void setClave(String clave) { this.clave = clave; }
    public String getConfirmarClave() { return confirmarClave; }
    public void setConfirmarClave(String confirmarClave) { this.confirmarClave = confirmarClave; }
    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
}
