package tecnihogar;


public enum EstadoJornada {
    PREPARACION("Preparación"),
    EN_CURSO("En curso"),
    CERRADA("Cerrada");

    private final String etiqueta;

    EstadoJornada(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    @Override
    public String toString() {
        return etiqueta;
    }
}
