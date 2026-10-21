package tecnihogar;

/** Estados por los que pasa una orden de servicio. */
public enum EstadoOrden {
    PENDIENTE("Pendiente"),
    EN_ATENCION("En atención"),
    COMPLETADA("Completada"),
    CANCELADA("Cancelada");

    private final String etiqueta;

    EstadoOrden(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    @Override
    public String toString() {
        return etiqueta;
    }
}
