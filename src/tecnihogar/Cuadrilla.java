package tecnihogar;

import java.util.Locale;


public class Cuadrilla {

    private final String codigo;
    private final double capacidadHorasJornada;
    private final boolean certificadaRefrigeracion;

    public Cuadrilla(String codigo, double capacidadHorasJornada, boolean certificadaRefrigeracion) {
        if (codigo == null || codigo.trim().isEmpty()) {
            throw new IllegalArgumentException("El código de la cuadrilla no puede estar vacío.");
        }
        if (Double.isNaN(capacidadHorasJornada) || Double.isInfinite(capacidadHorasJornada)
                || capacidadHorasJornada <= 0) {
            throw new IllegalArgumentException("La capacidad de la cuadrilla debe ser mayor que cero.");
        }
        this.codigo = codigo.trim();
        this.capacidadHorasJornada = capacidadHorasJornada;
        this.certificadaRefrigeracion = certificadaRefrigeracion;
    }

    public String getCodigo() {
        return codigo;
    }

    public double getCapacidadHorasJornada() {
        return capacidadHorasJornada;
    }

    public boolean isCertificadaRefrigeracion() {
        return certificadaRefrigeracion;
    }

    public String describir() {
        return String.format(Locale.US, "%s | capacidad %s | certificada en refrigeración: %s",
                codigo, Formato.horas(capacidadHorasJornada), certificadaRefrigeracion ? "sí" : "no");
    }

    @Override
    public String toString() {
        return codigo;
    }
}
