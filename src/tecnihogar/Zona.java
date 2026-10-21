package tecnihogar;


public enum Zona {
    METROPOLITANA("Metropolitana"),
    PERIFERICA("Periférica");

    private final String etiqueta;

    Zona(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    @Override
    public String toString() {
        return etiqueta;
    }
}
