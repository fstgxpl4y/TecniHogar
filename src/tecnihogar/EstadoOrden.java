package tecnihogar;


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
