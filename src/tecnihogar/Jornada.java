package tecnihogar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Jornada de trabajo de una cuadrilla: se prepara, se inicia y se cierra. */
public class Jornada {

    private static final double MARGEN = 1e-9;

    private final int id;
    private final Cuadrilla cuadrilla;
    private final List<OrdenServicio> ordenesAsignadas = new ArrayList<>();
    private final Map<OrdenServicio, ResultadoVisita> resultadosRegistrados = new HashMap<>();
    private EstadoJornada estado;

    public Jornada(int id, Cuadrilla cuadrilla) {
        if (cuadrilla == null) {
            throw new IllegalArgumentException("La jornada necesita una cuadrilla.");
        }
        this.id = id;
        this.cuadrilla = cuadrilla;
        this.estado = EstadoJornada.PREPARACION;
    }

    public int getId() {
        return id;
    }

    public Cuadrilla getCuadrilla() {
        return cuadrilla;
    }

    public EstadoJornada getEstado() {
        return estado;
    }

    public boolean isAbierta() {
        return estado != EstadoJornada.CERRADA;
    }

    /** Historial completo de órdenes de la jornada, incluso las que ya tienen resultado. */
    public List<OrdenServicio> getOrdenesAsignadas() {
        return Collections.unmodifiableList(ordenesAsignadas);
    }

    /** Órdenes de la jornada que todavía esperan su resultado. */
    public List<OrdenServicio> getOrdenesPendientesDeResultado() {
        List<OrdenServicio> pendientes = new ArrayList<>();
        for (OrdenServicio orden : ordenesAsignadas) {
            if (!resultadosRegistrados.containsKey(orden)) {
                pendientes.add(orden);
            }
        }
        return pendientes;
    }

    /** Una orden está reservada mientras la jornada siga abierta y no tenga resultado en ella. */
    public boolean reservaOrden(OrdenServicio orden) {
        return isAbierta() && ordenesAsignadas.contains(orden)
                && !resultadosRegistrados.containsKey(orden);
    }

    public ResultadoVisita getResultado(OrdenServicio orden) {
        return resultadosRegistrados.get(orden);
    }

    /** Suma de horas estimadas. Se calcula cada vez para que nunca quede desactualizada. */
    public double getHorasAsignadas() {
        double total = 0;
        for (OrdenServicio orden : ordenesAsignadas) {
            total += orden.getHorasEstimadas();
        }
        return total;
    }

    public double getCapacidadDisponible() {
        return cuadrilla.getCapacidadHorasJornada() - getHorasAsignadas();
    }

    public boolean puedeAgregarOrden(OrdenServicio orden) {
        return motivoRechazo(orden) == null;
    }

    /** Devuelve el motivo por el que no se puede agregar la orden, o null si sí se puede. */
    private String motivoRechazo(OrdenServicio orden) {
        if (orden == null) {
            return "La orden indicada no existe.";
        }
        if (estado != EstadoJornada.PREPARACION) {
            return "La jornada " + id + " está " + estado
                    + ". Solo se pueden asignar órdenes mientras está en preparación.";
        }
        if (orden.getEstado() != EstadoOrden.PENDIENTE) {
            return "La orden " + orden.getCodigo() + " está " + orden.getEstado()
                    + ". Solo se pueden asignar órdenes pendientes.";
        }
        if (ordenesAsignadas.contains(orden)) {
            return "La orden " + orden.getCodigo() + " ya está en la jornada " + id + ".";
        }
        if (!orden.esCompatibleCon(cuadrilla)) {
            return "La cuadrilla " + cuadrilla.getCodigo() + " no cumple los requisitos para atender "
                    + "la orden " + orden.getCodigo() + " (" + orden.getCategoria()
                    + "). Revisar su certificación.";
        }
        double horasConOrden = getHorasAsignadas() + orden.getHorasEstimadas();
        if (horasConOrden > cuadrilla.getCapacidadHorasJornada() + MARGEN) {
            return "La orden " + orden.getCodigo() + " supera la capacidad de la cuadrilla "
                    + cuadrilla.getCodigo() + ": con ella serían " + Formato.horas(horasConOrden)
                    + " y la capacidad es " + Formato.horas(cuadrilla.getCapacidadHorasJornada()) + ".";
        }
        return null;
    }

    public void agregarOrden(OrdenServicio orden) {
        String motivo = motivoRechazo(orden);
        if (motivo != null) {
            throw new IllegalStateException(motivo);
        }
        ordenesAsignadas.add(orden);
    }

    public void retirarOrden(OrdenServicio orden) {
        if (estado != EstadoJornada.PREPARACION) {
            throw new IllegalStateException("La jornada " + id + " está " + estado
                    + ". Solo se pueden retirar órdenes durante la preparación.");
        }
        if (orden == null || !ordenesAsignadas.contains(orden)) {
            throw new IllegalArgumentException("La orden no está asignada a la jornada " + id + ".");
        }
        ordenesAsignadas.remove(orden);
    }

    /** Valida todo primero y recién después inicia las visitas, para no dejar cambios a medias. */
    public void iniciar() {
        if (estado != EstadoJornada.PREPARACION) {
            throw new IllegalStateException("La jornada " + id + " ya está " + estado
                    + " y no se puede iniciar.");
        }
        if (ordenesAsignadas.isEmpty()) {
            throw new IllegalStateException("No se puede iniciar una jornada vacía.");
        }
        for (OrdenServicio orden : ordenesAsignadas) {
            if (orden.getEstado() != EstadoOrden.PENDIENTE) {
                throw new IllegalStateException("La orden " + orden.getCodigo() + " está "
                        + orden.getEstado() + " y no puede iniciar la visita.");
            }
        }
        for (OrdenServicio orden : ordenesAsignadas) {
            orden.iniciarVisita();
        }
        estado = EstadoJornada.EN_CURSO;
    }

    public void registrarResultado(OrdenServicio orden, ResultadoVisita resultado) {
        if (estado != EstadoJornada.EN_CURSO) {
            throw new IllegalStateException("La jornada " + id + " está " + estado
                    + ". Solo se pueden registrar resultados mientras está en curso.");
        }
        if (orden == null || !ordenesAsignadas.contains(orden)) {
            throw new IllegalArgumentException("La orden no pertenece a la jornada " + id + ".");
        }
        if (resultado == null) {
            throw new IllegalArgumentException("Hay que indicar el resultado de la visita.");
        }
        if (resultadosRegistrados.containsKey(orden)) {
            throw new IllegalStateException("La orden " + orden.getCodigo()
                    + " ya tiene un resultado registrado en la jornada " + id + ".");
        }
        orden.registrarResultado(resultado);
        resultadosRegistrados.put(orden, resultado);
        if (resultadosRegistrados.size() == ordenesAsignadas.size()) {
            estado = EstadoJornada.CERRADA;
        }
    }

    public String describir() {
        return String.format("Jornada %d | cuadrilla %s | %s | %d órdenes | horas asignadas %s | "
                        + "capacidad %s | disponible %s",
                id, cuadrilla.getCodigo(), estado, ordenesAsignadas.size(),
                Formato.horas(getHorasAsignadas()),
                Formato.horas(cuadrilla.getCapacidadHorasJornada()),
                Formato.horas(getCapacidadDisponible()));
    }

    @Override
    public String toString() {
        return "Jornada " + id;
    }
}
