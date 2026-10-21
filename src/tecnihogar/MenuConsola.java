package tecnihogar;

import java.util.List;
import java.util.Map;
import java.util.Scanner;


public class MenuConsola {

    private static class FinDeEntrada extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }

    private final SistemaTecniHogar sistema;
    private final Scanner entrada = new Scanner(System.in);

    public MenuConsola(SistemaTecniHogar sistema) {
        this.sistema = sistema;
    }

    public void iniciar() {
        boolean seguir = true;
        while (seguir) {
            try {
                mostrarMenu();
                int opcion = leerOpcion();
                if (opcion == 0) {
                    seguir = false;
                } else {
                    ejecutarOpcion(opcion);
                }
            } catch (FinDeEntrada e) {
                seguir = false;
            }
        }
        System.out.println("Hasta luego.");
    }

    public void mostrarMenu() {
        System.out.println();
        System.out.println("===== TecniHogar =====");
        System.out.println(" 1. Cotizar una orden");
        System.out.println(" 2. Registrar una orden aceptada");
        System.out.println(" 3. Registrar una cuadrilla");
        System.out.println(" 4. Crear una jornada");
        System.out.println(" 5. Asignar una orden a una jornada");
        System.out.println(" 6. Retirar una orden de una jornada");
        System.out.println(" 7. Iniciar una jornada");
        System.out.println(" 8. Registrar el resultado de una visita");
        System.out.println(" 9. Consultar órdenes");
        System.out.println("10. Consultar cuadrillas");
        System.out.println("11. Consultar jornadas");
        System.out.println("12. Reporte: órdenes por categoría y estado");
        System.out.println("13. Reporte: ingresos");
        System.out.println("14. Reporte: jornadas abiertas");
        System.out.println(" 0. Salir");
    }

    public int leerOpcion() {
        String texto = leerLinea("Elija una opción: ");
        try {
            return Integer.parseInt(texto.trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public void ejecutarOpcion(int opcion) {
        try {
            switch (opcion) {
                case 1: cotizarOrden(); break;
                case 2: registrarOrden(); break;
                case 3: registrarCuadrilla(); break;
                case 4: crearJornada(); break;
                case 5: asignarOrden(); break;
                case 6: retirarOrden(); break;
                case 7: iniciarJornada(); break;
                case 8: registrarResultado(); break;
                case 9: consultarOrdenes(); break;
                case 10: consultarCuadrillas(); break;
                case 11: consultarJornadas(); break;
                case 12: reporteOrdenes(); break;
                case 13: reporteIngresos(); break;
                case 14: reporteJornadasAbiertas(); break;
                default: System.out.println("Opción inválida. Elija un número del menú.");
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("No se pudo completar la operación: " + e.getMessage());
        }
    }


    private void cotizarOrden() {
        OrdenServicio orden = leerOrden();
        System.out.println("Categoría: " + orden.getCategoria());
        System.out.println("Costo: " + Formato.dinero(sistema.cotizarCosto(orden)));
        System.out.println("Tiempo estimado de atención: "
                + Formato.dias(sistema.cotizarDiasAtencion(orden)) + " hábiles");
        if (leerSiNo("¿El cliente acepta la cotización? (s/n): ")) {
            sistema.registrarOrden(orden);
            System.out.println("Orden " + orden.getCodigo() + " registrada y cobrada: "
                    + Formato.dinero(orden.calcularCosto()));
        } else {
            System.out.println("La cotización no se registró.");
        }
    }

    private void registrarOrden() {
        OrdenServicio orden = leerOrden();
        sistema.registrarOrden(orden);
        System.out.println("Orden " + orden.getCodigo() + " registrada como pendiente. Se cobró "
                + Formato.dinero(orden.calcularCosto()) + ".");
    }

    private void registrarCuadrilla() {
        String codigo = leerLinea("Código de la cuadrilla: ");
        double capacidad = leerDouble("Capacidad de horas por jornada: ");
        boolean certificada = leerSiNo("¿Está certificada en refrigeración? (s/n): ");
        sistema.registrarCuadrilla(new Cuadrilla(codigo, capacidad, certificada));
        System.out.println("Cuadrilla " + codigo.trim() + " registrada.");
    }

    private void crearJornada() {
        String codigo = leerLinea("Código de la cuadrilla: ");
        Jornada jornada = sistema.crearJornada(codigo);
        System.out.println("Jornada " + jornada.getId() + " creada en preparación para la cuadrilla "
                + jornada.getCuadrilla().getCodigo() + ".");
    }

    private void asignarOrden() {
        int id = leerEntero("Número de jornada: ");
        String codigo = leerLinea("Código de la orden: ");
        sistema.asignarOrden(id, codigo);
        Jornada jornada = sistema.buscarJornada(id);
        System.out.println("Orden asignada. " + jornada.describir());
    }

    private void retirarOrden() {
        int id = leerEntero("Número de jornada: ");
        String codigo = leerLinea("Código de la orden: ");
        sistema.retirarOrden(id, codigo);
        System.out.println("Orden retirada. Sigue pendiente y sin cargos.");
    }

    private void iniciarJornada() {
        int id = leerEntero("Número de jornada: ");
        double antes = sistema.getRegistroIngresos().getIngresosPorVisitasAdicionales();
        sistema.iniciarJornada(id);
        double cargos = sistema.getRegistroIngresos().getIngresosPorVisitasAdicionales() - antes;
        System.out.println("Jornada " + id + " en curso. Las órdenes pasaron a atención.");
        if (cargos > 0) {
            System.out.println("Cargos por visitas adicionales: " + Formato.dinero(cargos));
        }
    }

    private void registrarResultado() {
        int id = leerEntero("Número de jornada: ");
        Jornada jornada = sistema.buscarJornada(id);
        List<OrdenServicio> pendientes = jornada.getOrdenesPendientesDeResultado();
        System.out.println("Órdenes de la jornada sin resultado:");
        for (OrdenServicio orden : pendientes) {
            System.out.println("  " + orden.getCodigo());
        }
        String codigo = leerLinea("Código de la orden: ");
        System.out.println("1. Completada");
        System.out.println("2. Visita fallida");
        int eleccion = leerEntero("Resultado: ");
        ResultadoVisita resultado;
        if (eleccion == 1) {
            resultado = ResultadoVisita.COMPLETADA;
        } else if (eleccion == 2) {
            resultado = ResultadoVisita.VISITA_FALLIDA;
        } else {
            throw new IllegalArgumentException("El resultado debe ser 1 (completada) o 2 (visita fallida).");
        }
        sistema.registrarResultado(id, codigo, resultado);
        OrdenServicio orden = sistema.buscarOrden(codigo);
        System.out.println("Resultado registrado. La orden " + orden.getCodigo() + " quedó "
                + orden.getEstado() + ". La jornada está " + jornada.getEstado() + ".");
    }


    private void consultarOrdenes() {
        System.out.println("--- Órdenes ---");
        for (OrdenServicio orden : sistema.consultarOrdenes()) {
            System.out.println(orden.describir());
        }
    }

    private void consultarCuadrillas() {
        System.out.println("--- Cuadrillas ---");
        for (Cuadrilla cuadrilla : sistema.consultarCuadrillas()) {
            String situacion = sistema.cuadrillaOcupada(cuadrilla) ? "ocupada" : "libre";
            System.out.println(cuadrilla.describir() + " | " + situacion);
        }
    }

    private void consultarJornadas() {
        System.out.println("--- Jornadas ---");
        if (sistema.consultarJornadas().isEmpty()) {
            System.out.println("Todavía no hay jornadas.");
        }
        for (Jornada jornada : sistema.consultarJornadas()) {
            System.out.println(jornada.describir());
            for (OrdenServicio orden : jornada.getOrdenesAsignadas()) {
                ResultadoVisita resultado = jornada.getResultado(orden);
                System.out.println("    " + orden.getCodigo() + " | " + orden.getEstado()
                        + " | resultado: " + (resultado == null ? "sin registrar" : resultado));
            }
        }
    }

    private void reporteOrdenes() {
        System.out.println("--- Órdenes por categoría y estado ---");
        for (Map.Entry<String, Map<EstadoOrden, Integer>> categoria
                : sistema.contarOrdenesPorCategoriaYEstado().entrySet()) {
            System.out.println(categoria.getKey() + ":");
            for (Map.Entry<EstadoOrden, Integer> estado : categoria.getValue().entrySet()) {
                System.out.println("    " + estado.getKey() + ": " + estado.getValue());
            }
        }
    }

    private void reporteIngresos() {
        RegistroIngresos ingresos = sistema.getRegistroIngresos();
        System.out.println("--- Ingresos ---");
        System.out.println("Ingreso total: " + Formato.dinero(ingresos.getIngresoTotal()));
        System.out.println("  Por registro de órdenes: " + Formato.dinero(ingresos.getIngresosPorRegistro()));
        System.out.println("  Por visitas adicionales: "
                + Formato.dinero(ingresos.getIngresosPorVisitasAdicionales()));
        System.out.println("Ingreso por categoría:");
        for (String categoria : sistema.contarOrdenesPorCategoriaYEstado().keySet()) {
            double monto = ingresos.getIngresosPorCategoria().getOrDefault(categoria, 0.0);
            System.out.println("  " + categoria + ": " + Formato.dinero(monto));
        }
    }

    private void reporteJornadasAbiertas() {
        System.out.println("--- Jornadas abiertas ---");
        List<Jornada> abiertas = sistema.consultarJornadasAbiertas();
        if (abiertas.isEmpty()) {
            System.out.println("No hay jornadas abiertas.");
        }
        for (Jornada jornada : abiertas) {
            System.out.println(jornada.describir());
        }
    }


    private OrdenServicio leerOrden() {
        System.out.println("1. Diagnóstico");
        System.out.println("2. Instalación");
        System.out.println("3. Instalación eléctrica certificada");
        System.out.println("4. Mantenimiento de aire acondicionado");
        int categoria = leerEntero("Categoría: ");
        if (categoria < 1 || categoria > 4) {
            throw new IllegalArgumentException("La categoría debe ser un número del 1 al 4.");
        }
        String codigo = leerLinea("Código de la orden: ");
        String cliente = leerLinea("Nombre del cliente: ");
        String direccion = leerLinea("Dirección: ");
        double horas = leerDouble("Horas estimadas: ");
        Zona zona = leerZona();
        switch (categoria) {
            case 1:
                return new Diagnostico(codigo, cliente, direccion, horas, zona);
            case 2:
                return new Instalacion(codigo, cliente, direccion, horas, zona,
                        leerEntero("Cantidad de puntos: "));
            case 3:
                int puntos = leerEntero("Cantidad de puntos: ");
                double materiales = leerDouble("Valor declarado de los materiales (Q): ");
                return new InstalacionElectricaCertificada(codigo, cliente, direccion, horas, zona,
                        puntos, materiales);
            default:
                return new MantenimientoAireAcondicionado(codigo, cliente, direccion, horas, zona,
                        leerEntero("Capacidad del equipo (BTU): "));
        }
    }

    private Zona leerZona() {
        while (true) {
            System.out.println("1. Metropolitana");
            System.out.println("2. Periférica");
            String texto = leerLinea("Zona: ").trim();
            if (texto.equals("1")) {
                return Zona.METROPOLITANA;
            }
            if (texto.equals("2")) {
                return Zona.PERIFERICA;
            }
            System.out.println("Zona inválida. Escriba 1 o 2.");
        }
    }

    private boolean leerSiNo(String mensaje) {
        while (true) {
            String texto = leerLinea(mensaje).trim().toLowerCase();
            if (texto.equals("s") || texto.equals("si") || texto.equals("sí")) {
                return true;
            }
            if (texto.equals("n") || texto.equals("no")) {
                return false;
            }
            System.out.println("Responda con s o n.");
        }
    }

    private double leerDouble(String mensaje) {
        while (true) {
            String texto = leerLinea(mensaje).trim().replace(',', '.');
            try {
                double valor = Double.parseDouble(texto);
                if (Double.isNaN(valor) || Double.isInfinite(valor)) {
                    System.out.println("Escriba un número válido.");
                } else {
                    return valor;
                }
            } catch (NumberFormatException e) {
                System.out.println("Eso no es un número. Intente de nuevo.");
            }
        }
    }

    private int leerEntero(String mensaje) {
        while (true) {
            String texto = leerLinea(mensaje).trim();
            try {
                return Integer.parseInt(texto);
            } catch (NumberFormatException e) {
                System.out.println("Eso no es un número entero. Intente de nuevo.");
            }
        }
    }

    private String leerLinea(String mensaje) {
        System.out.print(mensaje);
        if (!entrada.hasNextLine()) {
            throw new FinDeEntrada();
        }
        return entrada.nextLine();
    }
}
