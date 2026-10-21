package tecnihogar;

/** Instalación de puntos. Es la base de la instalación eléctrica certificada. */
public class Instalacion extends OrdenServicio {

    private static final double TARIFA_METROPOLITANA = 100.0;
    private static final double TARIFA_PERIFERICA = 175.0;
    private static final double COSTO_HORA_FACTURABLE = 90.0;
    private static final double HORAS_POR_PUNTO = 0.75;

    private final int cantidadPuntos;

    public Instalacion(String codigo, String nombreCliente, String direccion,
                       double horasEstimadas, Zona zona, int cantidadPuntos) {
        super(codigo, nombreCliente, direccion, horasEstimadas, zona);
        if (cantidadPuntos <= 0) {
            throw new IllegalArgumentException("La cantidad de puntos debe ser un entero positivo.");
        }
        this.cantidadPuntos = cantidadPuntos;
    }

    public int getCantidadPuntos() {
        return cantidadPuntos;
    }

    /** Mayor entre las horas estimadas y las horas por puntos, redondeado hacia arriba. */
    protected int calcularHorasFacturables() {
        double horasPorPuntos = cantidadPuntos * HORAS_POR_PUNTO;
        return (int) Math.ceil(Math.max(getHorasEstimadas(), horasPorPuntos));
    }

    @Override
    public double calcularCosto() {
        double tarifaBase = getZona() == Zona.METROPOLITANA ? TARIFA_METROPOLITANA : TARIFA_PERIFERICA;
        return tarifaBase + COSTO_HORA_FACTURABLE * calcularHorasFacturables();
    }

    @Override
    public int calcularDiasAtencion() {
        return getZona() == Zona.METROPOLITANA ? 3 : 5;
    }

    @Override
    public int getMaxVisitas() {
        return 3;
    }

    @Override
    public boolean esCompatibleCon(Cuadrilla cuadrilla) {
        return true;
    }

    @Override
    public String getCategoria() {
        return "Instalación";
    }
}
