package tecnihogar;

/** Revisión de un problema antes de repararlo. */
public class Diagnostico extends OrdenServicio {

    private static final double MAX_HORAS = 1.5;
    private static final double TARIFA_METROPOLITANA = 150.0;
    private static final double TARIFA_PERIFERICA = 225.0;

    public Diagnostico(String codigo, String nombreCliente, String direccion,
                       double horasEstimadas, Zona zona) {
        super(codigo, nombreCliente, direccion, horasEstimadas, zona);
        if (horasEstimadas > MAX_HORAS) {
            throw new IllegalArgumentException("Un diagnóstico no puede pasar de " + MAX_HORAS
                    + " horas estimadas.");
        }
    }

    @Override
    public double calcularCosto() {
        return getZona() == Zona.METROPOLITANA ? TARIFA_METROPOLITANA : TARIFA_PERIFERICA;
    }

    @Override
    public int calcularDiasAtencion() {
        return getZona() == Zona.METROPOLITANA ? 1 : 2;
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
        return "Diagnóstico";
    }
}
