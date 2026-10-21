package tecnihogar;

/** Instalación que además lleva certificado y garantía sobre los materiales. */
public class InstalacionElectricaCertificada extends Instalacion {

    private static final double COSTO_CERTIFICADO = 120.0;
    private static final double PORCENTAJE_GARANTIA = 5.0;

    private final double valorMateriales;

    public InstalacionElectricaCertificada(String codigo, String nombreCliente, String direccion,
                                           double horasEstimadas, Zona zona, int cantidadPuntos,
                                           double valorMateriales) {
        super(codigo, nombreCliente, direccion, horasEstimadas, zona, cantidadPuntos);
        if (Double.isNaN(valorMateriales) || Double.isInfinite(valorMateriales) || valorMateriales <= 0) {
            throw new IllegalArgumentException("El valor de los materiales debe ser mayor que cero.");
        }
        this.valorMateriales = valorMateriales;
    }

    public double getValorMateriales() {
        return valorMateriales;
    }

    /** 5 % del valor declarado de los materiales. */
    public double calcularGarantia() {
        return valorMateriales * PORCENTAJE_GARANTIA / 100.0;
    }

    @Override
    public double calcularCosto() {
        return super.calcularCosto() + COSTO_CERTIFICADO + calcularGarantia();
    }

    @Override
    public int calcularDiasAtencion() {
        return super.calcularDiasAtencion() + 2;
    }

    @Override
    public int getMaxVisitas() {
        return 2;
    }

    @Override
    public String getCategoria() {
        return "Instalación eléctrica certificada";
    }
}
