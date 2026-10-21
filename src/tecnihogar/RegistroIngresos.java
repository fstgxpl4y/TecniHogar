package tecnihogar;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;


public class RegistroIngresos {

    static final double CARGO_VISITA_ADICIONAL = 40.0;

    private double ingresosPorRegistro;
    private double ingresosPorVisitasAdicionales;
    private final Map<String, Double> ingresosPorCategoria = new LinkedHashMap<>();

    public RegistroIngresos() {
        this.ingresosPorRegistro = 0;
        this.ingresosPorVisitasAdicionales = 0;
    }


    void registrarIngresoRegistro(OrdenServicio orden) {
        double monto = orden.calcularCosto();
        ingresosPorRegistro += monto;
        ingresosPorCategoria.merge(orden.getCategoria(), monto, Double::sum);
    }


    void registrarIngresoVisitaAdicional(OrdenServicio orden) {
        ingresosPorVisitasAdicionales += CARGO_VISITA_ADICIONAL;
        ingresosPorCategoria.merge(orden.getCategoria(), CARGO_VISITA_ADICIONAL, Double::sum);
    }

    public double getIngresoTotal() {
        return ingresosPorRegistro + ingresosPorVisitasAdicionales;
    }

    public double getIngresosPorRegistro() {
        return ingresosPorRegistro;
    }

    public double getIngresosPorVisitasAdicionales() {
        return ingresosPorVisitasAdicionales;
    }

    public Map<String, Double> getIngresosPorCategoria() {
        return Collections.unmodifiableMap(ingresosPorCategoria);
    }
}
