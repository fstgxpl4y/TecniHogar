package tecnihogar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Coordina órdenes, cuadrillas, jornadas e ingresos. Todas las validaciones van antes de los cambios. */
public class SistemaTecniHogar {

    private final List<OrdenServicio> ordenes = new ArrayList<>();
    private final List<Cuadrilla> cuadrillas = new ArrayList<>();
    private final List<Jornada> jornadas = new ArrayList<>();
    private final RegistroIngresos registroIngresos = new RegistroIngresos();
    private int siguienteIdJornada = 1;

    // ---------- Cotización y registro ----------

    /** Cotizar no guarda nada ni toca los ingresos. */
    public double cotizarCosto(OrdenServicio orden) {
        validarOrden(orden);
        return orden.calcularCosto();
    }

    public int cotizarDiasAtencion(OrdenServicio orden) {
        validarOrden(orden);
        return orden.calcularDiasAtencion();
    }

    /** Registra una orden aceptada y cobra su costo completo. */
    public void registrarOrden(OrdenServicio orden) {
        validarOrden(orden);
        if (existeOrden(orden.getCodigo())) {
            throw new IllegalArgumentException("Ya existe una orden con el código "
                    + orden.getCodigo() + ".");
        }
        ordenes.add(orden);
        registroIngresos.registrarIngresoRegistro(orden);
    }

    public void registrarCuadrilla(Cuadrilla cuadrilla) {
        if (cuadrilla == null) {
            throw new IllegalArgumentException("La cuadrilla indicada no existe.");
        }
        if (existeCuadrilla(cuadrilla.getCodigo())) {
            throw new IllegalArgumentException("Ya existe una cuadrilla con el código "
                    + cuadrilla.getCodigo() + ".");
        }
        cuadrillas.add(cuadrilla);
    }

    // ---------- Jornadas ----------

    public Jornada crearJornada(String codigoCuadrilla) {
        Cuadrilla cuadrilla = buscarCuadrilla(codigoCuadrilla);
        if (cuadrillaOcupada(cuadrilla)) {
            throw new IllegalStateException("La cuadrilla " + cuadrilla.getCodigo()
                    + " ya tiene una jornada sin cerrar.");
        }
        Jornada jornada = new Jornada(siguienteIdJornada, cuadrilla);
        siguienteIdJornada++;
        jornadas.add(jornada);
        return jornada;
    }

    /** Aquí se evita la doble asignación: se busca otra jornada abierta que reserve la orden. */
    public void asignarOrden(int idJornada, String codigoOrden) {
        Jornada jornada = buscarJornada(idJornada);
        OrdenServicio orden = buscarOrden(codigoOrden);
        for (Jornada otra : jornadas) {
            if (otra.reservaOrden(orden)) {
                throw new IllegalStateException("La orden " + orden.getCodigo()
                        + " ya está asignada a la jornada " + otra.getId() + ", que sigue abierta.");
            }
        }
        // La compatibilidad con la cuadrilla y la capacidad se comprueban dentro de la jornada.
        jornada.agregarOrden(orden);
    }

    public void retirarOrden(int idJornada, String codigoOrden) {
        Jornada jornada = buscarJornada(idJornada);
        OrdenServicio orden = buscarOrden(codigoOrden);
        jornada.retirarOrden(orden);
    }

    /** Aquí se registra el cargo de Q40 de las visitas adicionales, justo al iniciar la jornada. */
    public void iniciarJornada(int idJornada) {
        Jornada jornada = buscarJornada(idJornada);
        jornada.iniciar();
        for (OrdenServicio orden : jornada.getOrdenesAsignadas()) {
            if (orden.getVisitasRealizadas() > 1) {
                registroIngresos.registrarIngresoVisitaAdicional(orden);
            }
        }
    }

    public void registrarResultado(int idJornada, String codigoOrden, ResultadoVisita resultado) {
        Jornada jornada = buscarJornada(idJornada);
        OrdenServicio orden = buscarOrden(codigoOrden);
        jornada.registrarResultado(orden, resultado);
    }

    // ---------- Consultas y reportes ----------

    public List<OrdenServicio> consultarOrdenes() {
        return Collections.unmodifiableList(ordenes);
    }

    public List<Cuadrilla> consultarCuadrillas() {
        return Collections.unmodifiableList(cuadrillas);
    }

    public List<Jornada> consultarJornadas() {
        return Collections.unmodifiableList(jornadas);
    }

    public List<Jornada> consultarJornadasAbiertas() {
        List<Jornada> abiertas = new ArrayList<>();
        for (Jornada jornada : jornadas) {
            if (jornada.isAbierta()) {
                abiertas.add(jornada);
            }
        }
        return abiertas;
    }

    /** Cantidad de órdenes por categoría y estado. Las categorías salen de las propias órdenes. */
    public Map<String, Map<EstadoOrden, Integer>> contarOrdenesPorCategoriaYEstado() {
        Map<String, Map<EstadoOrden, Integer>> conteo = new LinkedHashMap<>();
        for (OrdenServicio orden : ordenes) {
            Map<EstadoOrden, Integer> porEstado = conteo.computeIfAbsent(orden.getCategoria(), c -> {
                Map<EstadoOrden, Integer> inicial = new EnumMap<>(EstadoOrden.class);
                for (EstadoOrden estado : EstadoOrden.values()) {
                    inicial.put(estado, 0);
                }
                return inicial;
            });
            porEstado.merge(orden.getEstado(), 1, Integer::sum);
        }
        return conteo;
    }

    public RegistroIngresos getRegistroIngresos() {
        return registroIngresos;
    }

    public OrdenServicio buscarOrden(String codigo) {
        for (OrdenServicio orden : ordenes) {
            if (orden.getCodigo().equalsIgnoreCase(textoLimpio(codigo))) {
                return orden;
            }
        }
        throw new IllegalArgumentException("No existe una orden con el código " + textoLimpio(codigo) + ".");
    }

    public Cuadrilla buscarCuadrilla(String codigo) {
        for (Cuadrilla cuadrilla : cuadrillas) {
            if (cuadrilla.getCodigo().equalsIgnoreCase(textoLimpio(codigo))) {
                return cuadrilla;
            }
        }
        throw new IllegalArgumentException("No existe una cuadrilla con el código "
                + textoLimpio(codigo) + ".");
    }

    public Jornada buscarJornada(int id) {
        for (Jornada jornada : jornadas) {
            if (jornada.getId() == id) {
                return jornada;
            }
        }
        throw new IllegalArgumentException("No existe la jornada " + id + ".");
    }

    public boolean cuadrillaOcupada(Cuadrilla cuadrilla) {
        for (Jornada jornada : jornadas) {
            if (jornada.getCuadrilla() == cuadrilla && jornada.isAbierta()) {
                return true;
            }
        }
        return false;
    }

    // ---------- Datos iniciales ----------

    /**
     * Carga las órdenes y cuadrillas de inicio directamente en las listas. No pasa por
     * registrarOrden, así que los ingresos siguen en cero.
     */
    public void cargarDatosIniciales() {
        ordenes.add(new Diagnostico("DIAG-001", "Carlos Méndez", "6a avenida 12-34, zona 1",
                1.0, Zona.METROPOLITANA));
        ordenes.add(new Diagnostico("DIAG-002", "Rosa Pérez", "Aldea El Carmen, km 18 carretera a El Salvador",
                1.5, Zona.PERIFERICA));
        ordenes.add(new Instalacion("INST-001", "Luis Gómez", "12 calle 5-60, zona 10",
                2.5, Zona.METROPOLITANA, 5));
        ordenes.add(new Instalacion("INST-002", "Ana Morales", "Colonia Villa Hermosa, San Miguel Petapa",
                3.2, Zona.PERIFERICA, 3));
        ordenes.add(new InstalacionElectricaCertificada("ELEC-001", "Marta Ruiz", "3a avenida 8-21, zona 15",
                2.5, Zona.METROPOLITANA, 5, 2000.0));
        ordenes.add(new InstalacionElectricaCertificada("ELEC-002", "Jorge López", "Lo de Bran, Mixco",
                3.0, Zona.PERIFERICA, 4, 1500.0));
        ordenes.add(new MantenimientoAireAcondicionado("AIRE-001", "Sofía Castillo", "7a avenida 20-10, zona 9",
                2.0, Zona.METROPOLITANA, 12000));
        ordenes.add(new MantenimientoAireAcondicionado("AIRE-002", "Pedro Alvarado", "Diagonal 6 10-65, zona 10",
                3.0, Zona.METROPOLITANA, 36000));

        cuadrillas.add(new Cuadrilla("CUA-01", 8.0, false));
        cuadrillas.add(new Cuadrilla("CUA-02", 6.0, true));
        cuadrillas.add(new Cuadrilla("CUA-03", 4.0, false));
    }

    // ---------- Auxiliares privados ----------

    private void validarOrden(OrdenServicio orden) {
        if (orden == null) {
            throw new IllegalArgumentException("La orden indicada no existe.");
        }
    }

    private boolean existeOrden(String codigo) {
        for (OrdenServicio orden : ordenes) {
            if (orden.getCodigo().equalsIgnoreCase(textoLimpio(codigo))) {
                return true;
            }
        }
        return false;
    }

    private boolean existeCuadrilla(String codigo) {
        for (Cuadrilla cuadrilla : cuadrillas) {
            if (cuadrilla.getCodigo().equalsIgnoreCase(textoLimpio(codigo))) {
                return true;
            }
        }
        return false;
    }

    private String textoLimpio(String texto) {
        return texto == null ? "" : texto.trim();
    }
}
