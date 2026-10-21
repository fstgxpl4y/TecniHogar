package tecnihogar;

/** Mantenimiento de un equipo de aire acondicionado. Solo en zona metropolitana. */
public class MantenimientoAireAcondicionado extends OrdenServicio {

    private static final int BTU_MINIMO = 9000;
    private static final int BTU_MAXIMO = 60000;
    // Supuesto: el enunciado no define la tarifa base del mantenimiento, se usa Q100.
    private static final double TARIFA_BASE = 100.0;
    private static final double COSTO_HORA = 110.0;
    private static final double CARGO_GAS_REFRIGERANTE = 80.0;

    private final int capacidadBTU;

    public MantenimientoAireAcondicionado(String codigo, String nombreCliente, String direccion,
                                          double horasEstimadas, Zona zona, int capacidadBTU) {
        super(codigo, nombreCliente, direccion, horasEstimadas, zona);
        if (zona != Zona.METROPOLITANA) {
            throw new IllegalArgumentException(
                    "El mantenimiento de aire acondicionado solo se acepta en zona metropolitana.");
        }
        if (capacidadBTU < BTU_MINIMO || capacidadBTU > BTU_MAXIMO) {
            throw new IllegalArgumentException("La capacidad debe estar entre " + BTU_MINIMO
                    + " y " + BTU_MAXIMO + " BTU.");
        }
        this.capacidadBTU = capacidadBTU;
    }

    public int getCapacidadBTU() {
        return capacidadBTU;
    }

    @Override
    public double calcularCosto() {
        return TARIFA_BASE + COSTO_HORA * getHorasEstimadas() + CARGO_GAS_REFRIGERANTE;
    }

    @Override
    public int calcularDiasAtencion() {
        return 2;
    }

    @Override
    public int getMaxVisitas() {
        return 1;
    }

    @Override
    public boolean esCompatibleCon(Cuadrilla cuadrilla) {
        return cuadrilla.isCertificadaRefrigeracion();
    }

    @Override
    public String getCategoria() {
        return "Mantenimiento de aire acondicionado";
    }
}
