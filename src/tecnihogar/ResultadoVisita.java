package tecnihogar;

/** Resultado que se registra al terminar una visita. */
public enum ResultadoVisita {
    COMPLETADA("Completada"),
    VISITA_FALLIDA("Visita fallida");

    private final String etiqueta;

    ResultadoVisita(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    @Override
    public String toString() {
        return etiqueta;
    }
}
