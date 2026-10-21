package tecnihogar;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PruebasTecniHogar {

    private static final Zona M = Zona.METROPOLITANA;
    private static final Zona P = Zona.PERIFERICA;
    private static int total = 0;
    private static int fallidas = 0;
    private static String grupo = "";



    private static void grupo(String nombre) {
        grupo = nombre;
    }

    private static void caso(String id, String entrada, String esperado, String obtenido, boolean ok) {
        total++;
        if (!ok) {
            fallidas++;
        }
        System.out.println("CASO|" + id + "|" + grupo + "|" + entrada + "|" + esperado + "|" + obtenido
                + "|" + (ok ? "EXITOSA" : "FALLIDA"));
    }

    private static void igual(String id, String entrada, String esperado, String obtenido) {
        caso(id, entrada, esperado, obtenido, esperado.equals(obtenido));
    }

    private static String intentar(Runnable accion) {
        try {
            accion.run();
            return null;
        } catch (IllegalArgumentException | IllegalStateException e) {
            return e.getMessage();
        }
    }

    private static void rechazo(String id, String entrada, String esperado, String clave, Runnable accion) {
        String mensaje = intentar(accion);
        caso(id, entrada, esperado, mensaje == null ? "NO fue rechazada" : "Rechazada: " + mensaje,
                mensaje != null && mensaje.contains(clave));
    }

    private static void rechazoSinCambios(String id, String entrada, String esperado, String clave,
                                          SistemaTecniHogar s, Runnable accion) {
        String antes = foto(s);
        String mensaje = intentar(accion);
        boolean igualAntes = antes.equals(foto(s));
        String obtenido = mensaje == null ? "NO fue rechazada"
                : "Rechazada: " + mensaje + (igualAntes ? " | Estado e ingresos sin cambios"
                : " | EL ESTADO CAMBIÓ");
        caso(id, entrada, esperado, obtenido, mensaje != null && mensaje.contains(clave) && igualAntes);
    }

    private static String foto(SistemaTecniHogar s) {
        StringBuilder sb = new StringBuilder();
        RegistroIngresos r = s.getRegistroIngresos();
        sb.append(r.getIngresoTotal()).append('/').append(r.getIngresosPorRegistro()).append('/')
                .append(r.getIngresosPorVisitasAdicionales()).append(r.getIngresosPorCategoria());
        for (OrdenServicio o : s.consultarOrdenes()) {
            sb.append('|').append(o.getCodigo()).append(':').append(o.getEstado()).append(':')
                    .append(o.getVisitasRealizadas());
        }
        for (Jornada j : s.consultarJornadas()) {
            sb.append("|J").append(j.getId()).append(':').append(j.getEstado()).append(':')
                    .append(j.getOrdenesAsignadas());
        }
        sb.append("|C").append(s.consultarCuadrillas().size());
        return sb.toString();
    }

    private static SistemaTecniHogar nuevo() {
        SistemaTecniHogar s = new SistemaTecniHogar();
        s.cargarDatosIniciales();
        return s;
    }

    private static Diagnostico diag(String codigo, double horas, Zona zona) {
        return new Diagnostico(codigo, "Cliente de prueba", "Dirección de prueba", horas, zona);
    }

    private static Instalacion inst(String codigo, double horas, Zona zona, int puntos) {
        return new Instalacion(codigo, "Cliente de prueba", "Dirección de prueba", horas, zona, puntos);
    }

    private static InstalacionElectricaCertificada elec(String codigo, double horas, Zona zona,
                                                        int puntos, double materiales) {
        return new InstalacionElectricaCertificada(codigo, "Cliente de prueba", "Dirección de prueba",
                horas, zona, puntos, materiales);
    }

    private static MantenimientoAireAcondicionado aire(String codigo, double horas, Zona zona, int btu) {
        return new MantenimientoAireAcondicionado(codigo, "Cliente de prueba", "Dirección de prueba",
                horas, zona, btu);
    }

    private static String q(double monto) {
        return Formato.dinero(monto);
    }

    private static int ciclo(SistemaTecniHogar s, String cuadrilla, String orden, ResultadoVisita resultado) {
        Jornada j = s.crearJornada(cuadrilla);
        s.asignarOrden(j.getId(), orden);
        s.iniciarJornada(j.getId());
        s.registrarResultado(j.getId(), orden, resultado);
        return j.getId();
    }

    private static String estadoOrden(SistemaTecniHogar s, String codigo) {
        OrdenServicio o = s.buscarOrden(codigo);
        return o.getEstado() + " (visitas " + o.getVisitasRealizadas() + "/" + o.getMaxVisitas() + ")";
    }

    private static String cargos(SistemaTecniHogar s) {
        return q(s.getRegistroIngresos().getIngresosPorVisitasAdicionales());
    }

    private static String ingresos(SistemaTecniHogar s) {
        RegistroIngresos r = s.getRegistroIngresos();
        return "total " + q(r.getIngresoTotal()) + " (registro " + q(r.getIngresosPorRegistro())
                + " + visitas adicionales " + q(r.getIngresosPorVisitasAdicionales()) + ")";
    }


    public static void main(String[] args) {
        reglasDeCobro();
        tiemposYVisitas();
        validacionesDeDatos();
        capacidadYCertificacion();
        asignacionesYEstados();
        retirosSinCobros();
        politicaDeVisitas();
        ingresosYCotizaciones();
        reportes();
        cierreDeJornadas();
        polimorfismo();
        System.out.println("RESUMEN|" + total + "|" + (total - fallidas) + "|" + fallidas);
        if (fallidas > 0) {
            System.exit(1);
        }
    }

    private static void reglasDeCobro() {
        grupo("A. Reglas de cobro");
        igual("A01", "Diagnóstico, zona metropolitana, 1.0 h", "Q150.00",
                q(diag("T", 1.0, M).calcularCosto()));
        igual("A02", "Diagnóstico, zona periférica, 1.0 h", "Q225.00",
                q(diag("T", 1.0, P).calcularCosto()));
        igual("A03", "Diagnóstico, metropolitana, 1.5 h (límite)", "Se acepta y cuesta Q150.00 (las horas no cambian el costo)",
                "Se acepta y cuesta " + q(diag("T", 1.5, M).calcularCosto()) + " (las horas no cambian el costo)");

        Instalacion i4 = inst("T", 2.5, M, 5);
        igual("A04", "Instalación, metropolitana, 2.5 h estimadas, 5 puntos",
                "Horas por puntos 3.75; se facturan 4 h; costo Q460.00",
                "Horas por puntos " + String.format(Locale.US, "%.2f", i4.getCantidadPuntos() * 0.75)
                        + "; se facturan " + i4.calcularHorasFacturables() + " h; costo " + q(i4.calcularCosto()));
        Instalacion i5 = inst("T", 3.2, P, 3);
        igual("A05", "Instalación, periférica, 3.2 h estimadas, 3 puntos (2.25 h por puntos)",
                "La fracción sube: se facturan 4 h; costo Q535.00",
                "La fracción sube: se facturan " + i5.calcularHorasFacturables() + " h; costo " + q(i5.calcularCosto()));
        Instalacion i6 = inst("T", 2.0, M, 1);
        igual("A06", "Instalación, metropolitana, 2.0 h exactas, 1 punto",
                "Una hora exacta no sube: se facturan 2 h; costo Q280.00",
                "Una hora exacta no sube: se facturan " + i6.calcularHorasFacturables() + " h; costo " + q(i6.calcularCosto()));
        Instalacion i7 = inst("T", 1.0, M, 4);
        igual("A07", "Instalación, metropolitana, 1.0 h estimada, 4 puntos (3.00 h por puntos)",
                "Mandan los puntos: se facturan 3 h; costo Q370.00",
                "Mandan los puntos: se facturan " + i7.calcularHorasFacturables() + " h; costo " + q(i7.calcularCosto()));
        Instalacion i8 = inst("T", 2.2, M, 1);
        igual("A08", "Instalación, metropolitana, 2.2 h estimadas, 1 punto",
                "Fracción de hora: se facturan 3 h; costo Q370.00",
                "Fracción de hora: se facturan " + i8.calcularHorasFacturables() + " h; costo " + q(i8.calcularCosto()));
        igual("A09", "Misma instalación del A04 pero en zona periférica (2.5 h, 5 puntos)",
                "Q535.00, o sea Q75.00 más que la metropolitana",
                q(inst("T", 2.5, P, 5).calcularCosto()) + ", o sea "
                        + q(inst("T", 2.5, P, 5).calcularCosto() - i4.calcularCosto()) + " más que la metropolitana");

        InstalacionElectricaCertificada e10 = elec("T", 2.5, M, 5, 2000);
        igual("A10", "Eléctrica certificada, metropolitana, 2.5 h, 5 puntos, materiales Q2,000",
                "Q680.00", q(e10.calcularCosto()));
        igual("A11", "Mismos datos del A10: desglose sobre el costo de la instalación normal (Q460.00)",
                "Certificado Q120.00; garantía 5 % = Q100.00",
                "Certificado " + q(e10.calcularCosto() - i4.calcularCosto() - e10.calcularGarantia())
                        + "; garantía 5 % = " + q(e10.calcularGarantia()));
        igual("A12", "Eléctrica certificada, periférica, 3.0 h, 4 puntos, materiales Q1,500",
                "Q640.00 (Q445.00 de instalación + Q120.00 + Q75.00)",
                q(elec("T", 3.0, P, 4, 1500).calcularCosto()) + " ("
                        + q(inst("T", 3.0, P, 4).calcularCosto()) + " de instalación + Q120.00 + "
                        + q(elec("T", 3.0, P, 4, 1500).calcularGarantia()) + ")");

        igual("A13", "Aire acondicionado, metropolitana, 2.0 h, 12,000 BTU",
                "Q400.00 (Q100.00 + Q220.00 + Q80.00)", q(aire("T", 2.0, M, 12000).calcularCosto())
                        + " (Q100.00 + " + q(110 * 2.0) + " + Q80.00)");
        igual("A14", "Aire acondicionado, metropolitana, 3.0 h, 36,000 BTU", "Q510.00",
                q(aire("T", 3.0, M, 36000).calcularCosto()));
        igual("A15", "Aire acondicionado, 2.0 h, 60,000 BTU (la capacidad no cambia el costo)", "Q400.00",
                q(aire("T", 2.0, M, 60000).calcularCosto()));
        igual("A16", "Aire acondicionado, 0.5 h, 9,000 BTU (hora fraccionaria sin redondear)", "Q235.00",
                q(aire("T", 0.5, M, 9000).calcularCosto()));
    }

    private static void tiemposYVisitas() {
        grupo("B. Tiempos de atención y máximos de visitas");
        Object[][] casos = {
                {"B01", "Diagnóstico metropolitano", diag("T", 1.0, M), "días=1; máximo de visitas=3"},
                {"B02", "Diagnóstico periférico", diag("T", 1.0, P), "días=2; máximo de visitas=3"},
                {"B03", "Instalación metropolitana", inst("T", 1.0, M, 1), "días=3; máximo de visitas=3"},
                {"B04", "Instalación periférica", inst("T", 1.0, P, 1), "días=5; máximo de visitas=3"},
                {"B05", "Eléctrica certificada metropolitana (3 + 2)", elec("T", 1.0, M, 1, 100), "días=5; máximo de visitas=2"},
                {"B06", "Eléctrica certificada periférica (5 + 2)", elec("T", 1.0, P, 1, 100), "días=7; máximo de visitas=2"},
                {"B07", "Aire acondicionado metropolitano", aire("T", 1.0, M, 9000), "días=2; máximo de visitas=1"},
        };
        for (Object[] c : casos) {
            OrdenServicio o = (OrdenServicio) c[2];
            igual((String) c[0], (String) c[1], (String) c[3],
                    "días=" + o.calcularDiasAtencion() + "; máximo de visitas=" + o.getMaxVisitas());
        }
    }

    private static void validacionesDeDatos() {
        grupo("C. Validación de datos");
        rechazo("C01", "Orden con código vacío (solo espacios)", "Rechazada: código vacío", "código",
                () -> diag("   ", 1.0, M));
        SistemaTecniHogar s = nuevo();
        rechazoSinCambios("C02", "Registrar una orden con el código repetido DIAG-001",
                "Rechazada: código repetido, sin cambios en órdenes ni ingresos", "Ya existe", s,
                () -> s.registrarOrden(diag("DIAG-001", 1.0, M)));
        rechazo("C03", "Instalación con 0 horas estimadas", "Rechazada: horas no positivas", "horas",
                () -> inst("T", 0, M, 3));
        rechazo("C04", "Diagnóstico con -2 horas", "Rechazada: horas no positivas", "horas",
                () -> diag("T", -2, M));
        rechazo("C05", "Diagnóstico de 1.6 horas", "Rechazada: más de 1.5 horas", "1.5",
                () -> diag("T", 1.6, M));
        rechazo("C06", "Mantenimiento de aire en zona periférica", "Rechazada: solo zona metropolitana",
                "zona metropolitana", () -> aire("T", 2.0, P, 12000));
        rechazo("C07", "Aire acondicionado de 8,999 BTU", "Rechazada: fuera de 9,000 a 60,000", "BTU",
                () -> aire("T", 2.0, M, 8999));
        rechazo("C08", "Aire acondicionado de 60,001 BTU", "Rechazada: fuera de 9,000 a 60,000", "BTU",
                () -> aire("T", 2.0, M, 60001));
        igual("C09", "Aire acondicionado de 9,000 y de 60,000 BTU (límites)", "Ambos se aceptan",
                (aire("T", 2.0, M, 9000).getCapacidadBTU() == 9000
                        && aire("T", 2.0, M, 60000).getCapacidadBTU() == 60000) ? "Ambos se aceptan" : "Alguno se rechazó");
        rechazo("C10", "Instalación con 0 puntos", "Rechazada: puntos no positivos", "puntos",
                () -> inst("T", 2.0, M, 0));
        rechazo("C11", "Instalación con -3 puntos", "Rechazada: puntos no positivos", "puntos",
                () -> inst("T", 2.0, M, -3));
        rechazo("C12", "Eléctrica certificada con materiales de Q0", "Rechazada: materiales no positivos",
                "materiales", () -> elec("T", 2.0, M, 3, 0));
        rechazo("C13", "Eléctrica certificada con materiales de -Q100", "Rechazada: materiales no positivos",
                "materiales", () -> elec("T", 2.0, M, 3, -100));
        rechazo("C14", "Cuadrilla con código vacío", "Rechazada: código vacío", "código",
                () -> new Cuadrilla("", 5.0, false));
        rechazo("C15", "Cuadrilla con capacidad 0", "Rechazada: capacidad no positiva", "capacidad",
                () -> new Cuadrilla("CUA-X", 0, false));
        rechazo("C16", "Cuadrilla con capacidad -4", "Rechazada: capacidad no positiva", "capacidad",
                () -> new Cuadrilla("CUA-X", -4, false));
        rechazoSinCambios("C17", "Registrar una cuadrilla con el código repetido CUA-01",
                "Rechazada: código repetido, sin cambios", "Ya existe", s,
                () -> s.registrarCuadrilla(new Cuadrilla("CUA-01", 5.0, false)));
        SistemaTecniHogar s2 = nuevo();
        s2.registrarCuadrilla(new Cuadrilla("CUA-04", 5.0, false));
        igual("C18", "Registrar una cuadrilla nueva CUA-04 (5.0 h, sin certificación)", "Quedan 4 cuadrillas",
                "Quedan " + s2.consultarCuadrillas().size() + " cuadrillas");
    }

    private static void capacidadYCertificacion() {
        grupo("D. Capacidad y certificación");
        SistemaTecniHogar s = nuevo();
        Jornada j = s.crearJornada("CUA-03");
        s.asignarOrden(j.getId(), "INST-001");
        igual("D01", "CUA-03 (4.0 h): asignar INST-001 (2.5 h)", "Se asigna; horas asignadas 2.50 h; disponible 1.50 h",
                "Se asigna; horas asignadas " + Formato.horas(j.getHorasAsignadas()) + "; disponible "
                        + Formato.horas(j.getCapacidadDisponible()));
        rechazoSinCambios("D02", "Misma jornada: asignar INST-002 (3.2 h), serían 5.7 h sobre 4.0 h",
                "Rechazada por capacidad; la jornada sigue con 1 orden", "capacidad", s,
                () -> s.asignarOrden(j.getId(), "INST-002"));

        SistemaTecniHogar s2 = nuevo();
        s2.registrarCuadrilla(new Cuadrilla("CUA-T", 3.5, false));
        Jornada jt = s2.crearJornada("CUA-T");
        s2.asignarOrden(jt.getId(), "DIAG-001");
        s2.asignarOrden(jt.getId(), "ELEC-001");
        igual("D03", "CUA-T (3.5 h): DIAG-001 (1.0 h) + ELEC-001 (2.5 h) = 3.5 h exactas",
                "Se aceptan las 2; disponible 0.00 h",
                "Se aceptan las " + jt.getOrdenesAsignadas().size() + "; disponible "
                        + Formato.horas(jt.getCapacidadDisponible()));

        SistemaTecniHogar s3 = nuevo();
        s3.registrarCuadrilla(new Cuadrilla("CUA-U", 3.0, false));
        Jornada ju = s3.crearJornada("CUA-U");
        s3.asignarOrden(ju.getId(), "INST-001");
        OrdenServicio inst001 = s3.buscarOrden("INST-001");
        igual("D04", "CUA-U (3.0 h): asignar INST-001 (2.5 h estimadas, 4 h facturables)",
                "Se acepta: la capacidad usa horas estimadas, no facturables",
                ju.getOrdenesAsignadas().contains(inst001)
                        ? "Se acepta: la capacidad usa horas estimadas, no facturables" : "Se rechazó");

        SistemaTecniHogar s4 = nuevo();
        Jornada j1 = s4.crearJornada("CUA-01");
        rechazoSinCambios("D05", "AIRE-001 a CUA-01 (no certificada en refrigeración)",
                "Rechazada por certificación", "certificación", s4, () -> s4.asignarOrden(j1.getId(), "AIRE-001"));
        Jornada j2 = s4.crearJornada("CUA-02");
        s4.asignarOrden(j2.getId(), "AIRE-001");
        igual("D06", "AIRE-001 a CUA-02 (certificada)", "Se asigna",
                j2.getOrdenesAsignadas().size() == 1 ? "Se asigna" : "Se rechazó");

        s4.asignarOrden(j1.getId(), "DIAG-001");
        s4.asignarOrden(j1.getId(), "INST-001");
        s4.asignarOrden(j1.getId(), "ELEC-001");
        igual("D07", "DIAG-001, INST-001 y ELEC-001 a CUA-01 (no certificada, 8.0 h)",
                "Las 3 se asignan (6.00 h)",
                "Las " + j1.getOrdenesAsignadas().size() + " se asignan (" + Formato.horas(j1.getHorasAsignadas()) + ")");

        s4.asignarOrden(j2.getId(), "AIRE-002");
        rechazoSinCambios("D08", "CUA-02 (6.0 h) ya con AIRE-001 (2.0) y AIRE-002 (3.0): asignar DIAG-002 (1.5), serían 6.5 h",
                "Rechazada por capacidad aunque la cuadrilla sí está certificada", "capacidad", s4,
                () -> s4.asignarOrden(j2.getId(), "DIAG-002"));
    }

    private static void asignacionesYEstados() {
        grupo("E. Asignaciones y restricciones de estado");
        SistemaTecniHogar s = nuevo();
        Jornada j1 = s.crearJornada("CUA-01");
        s.asignarOrden(j1.getId(), "INST-001");
        Jornada j2 = s.crearJornada("CUA-02");
        rechazoSinCambios("E01", "INST-001 ya está en la jornada 1 (abierta); asignarla a la jornada 2",
                "Rechazada: orden asignada a otra jornada abierta", "ya está asignada a la jornada 1", s,
                () -> s.asignarOrden(j2.getId(), "INST-001"));
        rechazoSinCambios("E02", "Asignar INST-001 otra vez a la jornada 1", "Rechazada: orden repetida", "ya está asignada", s,
                () -> s.asignarOrden(j1.getId(), "INST-001"));
        rechazoSinCambios("E03", "Crear otra jornada para CUA-01, que ya tiene una abierta",
                "Rechazada: cuadrilla con jornada sin cerrar", "ya tiene una jornada sin cerrar", s,
                () -> s.crearJornada("CUA-01"));
        igual("E03b", "Después del rechazo, crear jornada para CUA-03", "Recibe el número 3 (el rechazo no gastó número)",
                "Recibe el número " + s.crearJornada("CUA-03").getId() + " (el rechazo no gastó número)");

        SistemaTecniHogar s2 = nuevo();
        int idCerrada = ciclo(s2, "CUA-01", "DIAG-001", ResultadoVisita.COMPLETADA);
        igual("E04", "Cerrar la jornada de CUA-01 y crear otra para la misma cuadrilla",
                "Jornada " + idCerrada + " cerrada, cuadrilla libre y nueva jornada creada",
                "Jornada " + idCerrada + " " + s2.buscarJornada(idCerrada).getEstado().toString().toLowerCase()
                        + ", cuadrilla " + (s2.cuadrillaOcupada(s2.buscarCuadrilla("CUA-01")) ? "ocupada" : "libre")
                        + " y nueva jornada " + (s2.crearJornada("CUA-01").getId() == 2 ? "creada" : "no creada"));

        SistemaTecniHogar s3 = nuevo();
        Jornada v = s3.crearJornada("CUA-01");
        rechazoSinCambios("E05", "Iniciar una jornada sin órdenes", "Rechazada: jornada vacía", "vacía", s3,
                () -> s3.iniciarJornada(v.getId()));
        igual("E05b", "Estado de la jornada vacía después del rechazo", "Preparación", v.getEstado().toString());

        SistemaTecniHogar s4 = nuevo();
        Jornada c = s4.crearJornada("CUA-01");
        s4.asignarOrden(c.getId(), "DIAG-001");
        s4.asignarOrden(c.getId(), "DIAG-002");
        s4.iniciarJornada(c.getId());
        rechazoSinCambios("E06", "Asignar INST-001 a la jornada 1, que ya está en curso",
                "Rechazada: solo en preparación", "preparación", s4, () -> s4.asignarOrden(c.getId(), "INST-001"));
        rechazoSinCambios("E07", "Retirar DIAG-001 de la jornada en curso", "Rechazada: solo en preparación",
                "preparación", s4, () -> s4.retirarOrden(c.getId(), "DIAG-001"));
        rechazoSinCambios("E08", "Iniciar otra vez la jornada que ya está en curso", "Rechazada: ya iniciada",
                "ya está", s4, () -> s4.iniciarJornada(c.getId()));
        Jornada otra = s4.crearJornada("CUA-02");
        rechazoSinCambios("E09", "DIAG-001 está En atención; asignarla a otra jornada", "Rechazada: orden en atención",
                "ya está asignada", s4, () -> s4.asignarOrden(otra.getId(), "DIAG-001"));
        s4.registrarResultado(c.getId(), "DIAG-001", ResultadoVisita.COMPLETADA);
        rechazoSinCambios("E10", "Registrar de nuevo el resultado de DIAG-001 en la misma jornada",
                "Rechazada: resultado ya registrado", "ya tiene un resultado", s4,
                () -> s4.registrarResultado(c.getId(), "DIAG-001", ResultadoVisita.VISITA_FALLIDA));
        rechazoSinCambios("E11", "Asignar DIAG-001 (Completada) a otra jornada", "Rechazada: orden completada",
                "Completada", s4, () -> s4.asignarOrden(otra.getId(), "DIAG-001"));
        rechazoSinCambios("E12", "Registrar resultado de INST-001, que no está en la jornada 1",
                "Rechazada: la orden no pertenece a la jornada", "no pertenece", s4,
                () -> s4.registrarResultado(c.getId(), "INST-001", ResultadoVisita.COMPLETADA));

        SistemaTecniHogar s5 = nuevo();
        Jornada p = s5.crearJornada("CUA-01");
        s5.asignarOrden(p.getId(), "DIAG-001");
        rechazoSinCambios("E13", "Registrar resultado antes de iniciar la jornada", "Rechazada: jornada no en curso",
                "en curso", s5, () -> s5.registrarResultado(p.getId(), "DIAG-001", ResultadoVisita.COMPLETADA));
        rechazoSinCambios("E14", "Asignar a la jornada 99 (no existe)", "Rechazada: jornada inexistente",
                "No existe la jornada 99", s5, () -> s5.asignarOrden(99, "DIAG-002"));
        rechazoSinCambios("E15", "Asignar la orden ZZZ-999 (no existe)", "Rechazada: orden inexistente",
                "No existe una orden", s5, () -> s5.asignarOrden(p.getId(), "ZZZ-999"));
        rechazoSinCambios("E16", "Retirar DIAG-002, que no está en la jornada", "Rechazada: la orden no está en la jornada",
                "no está asignada", s5, () -> s5.retirarOrden(p.getId(), "DIAG-002"));
        rechazoSinCambios("E17", "Crear jornada para la cuadrilla CUA-77 (no existe)", "Rechazada: cuadrilla inexistente",
                "No existe una cuadrilla", s5, () -> s5.crearJornada("CUA-77"));

        SistemaTecniHogar s6 = nuevo();
        Jornada a = s6.crearJornada("CUA-01");
        s6.asignarOrden(a.getId(), "DIAG-001");
        s6.asignarOrden(a.getId(), "DIAG-002");
        s6.iniciarJornada(a.getId());
        s6.registrarResultado(a.getId(), "DIAG-001", ResultadoVisita.VISITA_FALLIDA);
        Jornada b = s6.crearJornada("CUA-02");
        s6.asignarOrden(b.getId(), "DIAG-001");
        igual("E18", "DIAG-001 falla en la jornada 1 (que sigue en curso por DIAG-002) y se asigna a la jornada 2",
                "Se acepta: la orden volvió a Pendiente y ya no está reservada; la jornada 1 sigue en curso",
                "Se " + (b.getOrdenesAsignadas().size() == 1 ? "acepta" : "rechaza") + ": la orden volvió a "
                        + s6.buscarOrden("DIAG-001").getEstado() + " y ya no está reservada; la jornada 1 sigue "
                        + a.getEstado().toString().toLowerCase());
    }

    private static void retirosSinCobros() {
        grupo("F. Retiros y asignaciones sin cobros");
        SistemaTecniHogar s = nuevo();
        String antes = ingresos(s);
        Jornada j = s.crearJornada("CUA-01");
        s.asignarOrden(j.getId(), "INST-001");
        igual("F01", "Asignar INST-001 a una jornada en preparación",
                "Los ingresos no cambian (" + antes + ") y la orden sigue Pendiente",
                "Los ingresos " + (antes.equals(ingresos(s)) ? "no cambian" : "CAMBIARON") + " (" + ingresos(s)
                        + ") y la orden sigue " + s.buscarOrden("INST-001").getEstado());
        s.retirarOrden(j.getId(), "INST-001");
        igual("F02", "Retirar INST-001 durante la preparación",
                "Vuelve libre y Pendiente, horas asignadas 0.00 h, ingresos sin cambio",
                "Vuelve " + (j.getOrdenesAsignadas().isEmpty() ? "libre" : "no libre") + " y "
                        + s.buscarOrden("INST-001").getEstado() + ", horas asignadas "
                        + Formato.horas(j.getHorasAsignadas()) + ", ingresos "
                        + (antes.equals(ingresos(s)) ? "sin cambio" : "CAMBIARON"));
        Jornada j2 = s.crearJornada("CUA-02");
        s.asignarOrden(j2.getId(), "INST-001");
        igual("F03", "Asignar a la jornada 2 la orden que se retiró", "Se asigna sin problema",
                j2.getOrdenesAsignadas().size() == 1 ? "Se asigna sin problema" : "Se rechazó");
    }

    private static void politicaDeVisitas() {
        grupo("G. Visitas, cargo de Q40 y cancelación");
        SistemaTecniHogar s = nuevo();
        Jornada j1 = s.crearJornada("CUA-01");
        s.asignarOrden(j1.getId(), "DIAG-001");
        igual("G01", "Diagnóstico: asignar DIAG-001 a una jornada (todavía sin iniciar)",
                "Cargos por visitas adicionales Q0.00", "Cargos por visitas adicionales " + cargos(s));
        s.iniciarJornada(j1.getId());
        igual("G02", "Iniciar la jornada (primera visita)", "En atención (visitas 1/3); cargos Q0.00",
                estadoOrden(s, "DIAG-001") + "; cargos " + cargos(s));
        s.registrarResultado(j1.getId(), "DIAG-001", ResultadoVisita.VISITA_FALLIDA);
        igual("G03", "Visita 1 fallida", "Pendiente (visitas 1/3); cargos Q0.00",
                estadoOrden(s, "DIAG-001") + "; cargos " + cargos(s));
        Jornada j2 = s.crearJornada("CUA-01");
        s.asignarOrden(j2.getId(), "DIAG-001");
        igual("G04", "Asignar DIAG-001 a una segunda jornada (sin iniciar)", "El cargo todavía no se cobra: Q0.00",
                "El cargo todavía no se cobra: " + cargos(s));
        s.iniciarJornada(j2.getId());
        igual("G05", "Iniciar la segunda jornada (segunda visita)", "En atención (visitas 2/3); cargos Q40.00",
                estadoOrden(s, "DIAG-001") + "; cargos " + cargos(s));
        s.registrarResultado(j2.getId(), "DIAG-001", ResultadoVisita.VISITA_FALLIDA);
        int j3 = ciclo(s, "CUA-01", "DIAG-001", ResultadoVisita.VISITA_FALLIDA);
        igual("G06", "Tercera visita fallida (límite de 3)", "Cancelada (visitas 3/3); cargos Q80.00",
                estadoOrden(s, "DIAG-001") + "; cargos " + cargos(s));
        Jornada jn = s.crearJornada("CUA-02");
        rechazoSinCambios("G07", "Asignar la orden cancelada DIAG-001 a otra jornada", "Rechazada: orden cancelada",
                "Cancelada", s, () -> s.asignarOrden(jn.getId(), "DIAG-001"));

        SistemaTecniHogar s2 = nuevo();
        ciclo(s2, "CUA-01", "DIAG-002", ResultadoVisita.VISITA_FALLIDA);
        ciclo(s2, "CUA-01", "DIAG-002", ResultadoVisita.VISITA_FALLIDA);
        ciclo(s2, "CUA-01", "DIAG-002", ResultadoVisita.COMPLETADA);
        igual("G08", "Diagnóstico: falla 2 veces y se completa en la tercera visita",
                "Completada (visitas 3/3); cargos Q80.00",
                estadoOrden(s2, "DIAG-002") + "; cargos " + cargos(s2));

        SistemaTecniHogar s3 = nuevo();
        ciclo(s3, "CUA-01", "INST-001", ResultadoVisita.VISITA_FALLIDA);
        String tras1 = estadoOrden(s3, "INST-001");
        ciclo(s3, "CUA-01", "INST-001", ResultadoVisita.VISITA_FALLIDA);
        String tras2 = estadoOrden(s3, "INST-001");
        ciclo(s3, "CUA-01", "INST-001", ResultadoVisita.VISITA_FALLIDA);
        igual("G09", "Instalación: tres visitas fallidas seguidas",
                "Pendiente (visitas 1/3), Pendiente (visitas 2/3), Cancelada (visitas 3/3); cargos Q80.00",
                tras1 + ", " + tras2 + ", " + estadoOrden(s3, "INST-001") + "; cargos " + cargos(s3));

        SistemaTecniHogar s4 = nuevo();
        ciclo(s4, "CUA-01", "ELEC-001", ResultadoVisita.VISITA_FALLIDA);
        String e1 = estadoOrden(s4, "ELEC-001");
        ciclo(s4, "CUA-01", "ELEC-001", ResultadoVisita.VISITA_FALLIDA);
        igual("G10", "Eléctrica certificada: dos visitas fallidas (límite de 2)",
                "Pendiente (visitas 1/2), luego Cancelada (visitas 2/2); cargos Q40.00",
                e1 + ", luego " + estadoOrden(s4, "ELEC-001") + "; cargos " + cargos(s4));

        SistemaTecniHogar s5 = nuevo();
        ciclo(s5, "CUA-02", "AIRE-001", ResultadoVisita.VISITA_FALLIDA);
        igual("G11", "Aire acondicionado: una visita fallida (límite de 1)", "Cancelada (visitas 1/1); cargos Q0.00",
                estadoOrden(s5, "AIRE-001") + "; cargos " + cargos(s5));
        ciclo(s5, "CUA-02", "AIRE-002", ResultadoVisita.COMPLETADA);
        igual("G12", "Aire acondicionado: visita completada", "Completada (visitas 1/1); cargos Q0.00",
                estadoOrden(s5, "AIRE-002") + "; cargos " + cargos(s5));
        if (j3 < 0) {
            System.out.println("Nunca ocurre");
        }
    }

    private static void ingresosYCotizaciones() {
        grupo("H. Ingresos, cotizaciones y operaciones rechazadas");
        SistemaTecniHogar s = nuevo();
        long pendientes = s.consultarOrdenes().stream().filter(o -> o.getEstado() == EstadoOrden.PENDIENTE).count();
        igual("H01", "Estado al iniciar el sistema",
                "8 órdenes, todas pendientes; 3 cuadrillas; ingresos total Q0.00",
                s.consultarOrdenes().size() + " órdenes, " + (pendientes == 8 ? "todas pendientes" : "no todas pendientes")
                        + "; " + s.consultarCuadrillas().size() + " cuadrillas; ingresos " + ingresos(s).split(" \\(")[0]);
        long certificadas = s.consultarCuadrillas().stream().filter(Cuadrilla::isCertificadaRefrigeracion).count();
        igual("H02", "Cuadrillas iniciales según la certificación", "1 certificada y 2 no certificadas",
                certificadas + " certificada y " + (s.consultarCuadrillas().size() - certificadas) + " no certificadas");

        String antes = foto(s);
        OrdenServicio cot = elec("COT-1", 2.5, M, 5, 2000);
        String cotizacion = q(s.cotizarCosto(cot)) + ", " + s.cotizarDiasAtencion(cot) + " días";
        igual("H03", "Cotizar una eléctrica certificada (2.5 h, 5 puntos, Q2,000), sin registrarla",
                "Q680.00, 5 días; órdenes e ingresos sin cambios",
                cotizacion + "; órdenes e ingresos " + (antes.equals(foto(s)) ? "sin cambios" : "CAMBIARON"));

        s.registrarOrden(diag("DIAG-900", 1.0, M));
        igual("H04", "Registrar la orden aceptada DIAG-900 (Diagnóstico metropolitano)",
                "Queda Pendiente; ingreso por registro Q150.00; categoría Diagnóstico Q150.00; visitas adicionales Q0.00",
                "Queda " + s.buscarOrden("DIAG-900").getEstado() + "; ingreso por registro "
                        + q(s.getRegistroIngresos().getIngresosPorRegistro()) + "; categoría Diagnóstico "
                        + q(s.getRegistroIngresos().getIngresosPorCategoria().get("Diagnóstico"))
                        + "; visitas adicionales " + cargos(s));
        s.registrarOrden(elec("ELEC-900", 2.5, M, 5, 2000));
        igual("H05", "Registrar también ELEC-900 (Q680.00)", "Ingreso total Q830.00; 10 órdenes",
                "Ingreso total " + q(s.getRegistroIngresos().getIngresoTotal()) + "; "
                        + s.consultarOrdenes().size() + " órdenes");

        Jornada j = s.crearJornada("CUA-03");
        s.asignarOrden(j.getId(), "INST-001");
        rechazoSinCambios("H06", "Asignación rechazada por capacidad (INST-002 en CUA-03)",
                "Rechazada; ingresos y estados sin cambios", "capacidad", s, () -> s.asignarOrden(j.getId(), "INST-002"));
        Jornada vacia = s.crearJornada("CUA-01");
        rechazoSinCambios("H07", "Inicio rechazado de una jornada vacía", "Rechazada; ingresos y estados sin cambios",
                "vacía", s, () -> s.iniciarJornada(vacia.getId()));
        s.retirarOrden(j.getId(), "INST-001");
        igual("H08", "Retirar una orden durante la preparación", "Ingresos siguen en Q830.00",
                "Ingresos siguen en " + q(s.getRegistroIngresos().getIngresoTotal()));
        RegistroIngresos r = s.getRegistroIngresos();
        igual("H09", "Total contra sus partes", "Total Q830.00 = Q830.00 + Q0.00",
                "Total " + q(r.getIngresoTotal()) + " = " + q(r.getIngresosPorRegistro()) + " + "
                        + q(r.getIngresosPorVisitasAdicionales()));
    }

    private static void reportes() {
        grupo("I. Reportes");
        SistemaTecniHogar s0 = nuevo();
        igual("I01", "Órdenes por categoría y estado al iniciar",
                "Cada categoría tiene 2 pendientes y 0 en los demás estados",
                conteo(s0).equals("Diagnóstico {Pendiente=2, En atención=0, Completada=0, Cancelada=0}; "
                        + "Instalación {Pendiente=2, En atención=0, Completada=0, Cancelada=0}; "
                        + "Instalación eléctrica certificada {Pendiente=2, En atención=0, Completada=0, Cancelada=0}; "
                        + "Mantenimiento de aire acondicionado {Pendiente=2, En atención=0, Completada=0, Cancelada=0}")
                        ? "Cada categoría tiene 2 pendientes y 0 en los demás estados" : conteo(s0));

        SistemaTecniHogar s = nuevo();
        s.registrarOrden(diag("DIAG-900", 1.0, M));
        s.registrarOrden(elec("ELEC-900", 2.5, M, 5, 2000));
        Jornada j1 = s.crearJornada("CUA-01");
        s.asignarOrden(j1.getId(), "DIAG-001");
        s.asignarOrden(j1.getId(), "INST-001");
        s.iniciarJornada(j1.getId());
        s.registrarResultado(j1.getId(), "DIAG-001", ResultadoVisita.VISITA_FALLIDA);
        s.registrarResultado(j1.getId(), "INST-001", ResultadoVisita.COMPLETADA);
        Jornada j2 = s.crearJornada("CUA-01");
        s.asignarOrden(j2.getId(), "DIAG-001");
        s.iniciarJornada(j2.getId());
        s.registrarResultado(j2.getId(), "DIAG-001", ResultadoVisita.VISITA_FALLIDA);
        Jornada j3 = s.crearJornada("CUA-02");
        s.asignarOrden(j3.getId(), "AIRE-001");
        s.asignarOrden(j3.getId(), "ELEC-900");
        s.iniciarJornada(j3.getId());
        s.registrarResultado(j3.getId(), "AIRE-001", ResultadoVisita.VISITA_FALLIDA);
        s.registrarResultado(j3.getId(), "ELEC-900", ResultadoVisita.COMPLETADA);
        Jornada j4 = s.crearJornada("CUA-03");
        s.asignarOrden(j4.getId(), "DIAG-002");
        s.asignarOrden(j4.getId(), "DIAG-001");
        Jornada j5 = s.crearJornada("CUA-01");
        s.asignarOrden(j5.getId(), "INST-002");
        Jornada j6 = s.crearJornada("CUA-02");
        s.asignarOrden(j6.getId(), "AIRE-002");
        s.iniciarJornada(j6.getId());

        igual("I02", "Escenario completo (6 jornadas, ver sección de pruebas): órdenes por categoría y estado",
                "Diagnóstico {Pendiente=3, En atención=0, Completada=0, Cancelada=0}; "
                        + "Instalación {Pendiente=1, En atención=0, Completada=1, Cancelada=0}; "
                        + "Instalación eléctrica certificada {Pendiente=2, En atención=0, Completada=1, Cancelada=0}; "
                        + "Mantenimiento de aire acondicionado {Pendiente=0, En atención=1, Completada=0, Cancelada=1}",
                conteo(s));
        RegistroIngresos r = s.getRegistroIngresos();
        StringBuilder porCat = new StringBuilder();
        for (String categoria : s.contarOrdenesPorCategoriaYEstado().keySet()) {
            porCat.append("; ").append(categoria).append(' ')
                    .append(q(r.getIngresosPorCategoria().getOrDefault(categoria, 0.0)));
        }
        igual("I03", "Escenario completo: reporte de ingresos",
                "Total Q870.00; registro Q830.00; visitas adicionales Q40.00; Diagnóstico Q190.00; Instalación Q0.00; "
                        + "Instalación eléctrica certificada Q680.00; Mantenimiento de aire acondicionado Q0.00",
                "Total " + q(r.getIngresoTotal()) + "; registro " + q(r.getIngresosPorRegistro())
                        + "; visitas adicionales " + q(r.getIngresosPorVisitasAdicionales()) + porCat);
        StringBuilder abiertas = new StringBuilder();
        for (Jornada j : s.consultarJornadasAbiertas()) {
            abiertas.append(abiertas.length() == 0 ? "" : "; ").append("J").append(j.getId()).append(' ')
                    .append(j.getCuadrilla().getCodigo()).append(' ').append(j.getEstado()).append(' ')
                    .append(Formato.horas(j.getHorasAsignadas())).append(" disponible ")
                    .append(Formato.horas(j.getCapacidadDisponible()));
        }
        igual("I04", "Escenario completo: jornadas abiertas (las jornadas 1, 2 y 3 ya cerraron)",
                "J4 CUA-03 Preparación 2.50 h disponible 1.50 h; J5 CUA-01 Preparación 3.20 h disponible 4.80 h; "
                        + "J6 CUA-02 En curso 3.00 h disponible 3.00 h",
                abiertas.toString());
        igual("I05", "Escenario completo: estado de DIAG-001 y AIRE-001",
                "DIAG-001 Pendiente (visitas 2/3); AIRE-001 Cancelada (visitas 1/1)",
                "DIAG-001 " + estadoOrden(s, "DIAG-001") + "; AIRE-001 " + estadoOrden(s, "AIRE-001"));
    }

    private static String conteo(SistemaTecniHogar s) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Map<EstadoOrden, Integer>> e : s.contarOrdenesPorCategoriaYEstado().entrySet()) {
            sb.append(sb.length() == 0 ? "" : "; ").append(e.getKey()).append(' ').append(e.getValue());
        }
        return sb.toString();
    }

    private static void cierreDeJornadas() {
        grupo("J. Cierre de jornadas y cuadrillas");
        SistemaTecniHogar s = nuevo();
        Jornada j = s.crearJornada("CUA-01");
        s.asignarOrden(j.getId(), "DIAG-001");
        s.asignarOrden(j.getId(), "INST-001");
        s.iniciarJornada(j.getId());
        s.registrarResultado(j.getId(), "DIAG-001", ResultadoVisita.COMPLETADA);
        String parcial = j.getEstado() + ", cuadrilla " + (s.cuadrillaOcupada(j.getCuadrilla()) ? "ocupada" : "libre");
        s.registrarResultado(j.getId(), "INST-001", ResultadoVisita.COMPLETADA);
        igual("J01", "Jornada con 2 órdenes: registrar 1 resultado y luego el otro",
                "Con 1 resultado: En curso, cuadrilla ocupada. Con 2 resultados: Cerrada, cuadrilla libre",
                "Con 1 resultado: " + parcial + ". Con 2 resultados: " + j.getEstado() + ", cuadrilla "
                        + (s.cuadrillaOcupada(j.getCuadrilla()) ? "ocupada" : "libre"));
        igual("J02", "Jornadas abiertas después del cierre", "0 jornadas abiertas",
                s.consultarJornadasAbiertas().size() + " jornadas abiertas");
    }

    private static void polimorfismo() {
        grupo("K. Polimorfismo");
        int encontrados = 0;
        StringBuilder donde = new StringBuilder();
        Path carpeta = Paths.get("src", "tecnihogar");
        try (DirectoryStream<Path> archivos = Files.newDirectoryStream(carpeta, "*.java")) {
            for (Path archivo : archivos) {
                String texto = new String(Files.readAllBytes(archivo), StandardCharsets.UTF_8);
                if (texto.contains("instanceof") || texto.contains("getClass()")) {
                    encontrados++;
                    donde.append(archivo.getFileName()).append(' ');
                }
            }
        } catch (IOException e) {
            caso("K01", "Buscar instanceof y getClass() en src/tecnihogar", "0 usos", "No se pudo leer la carpeta: "
                    + e.getMessage(), false);
            return;
        }
        igual("K01", "Buscar instanceof y getClass() en los archivos de src/tecnihogar", "0 archivos con esos usos",
                encontrados + " archivos con esos usos" + (encontrados > 0 ? ": " + donde : ""));
        List<OrdenServicio> todas = List.of(diag("T1", 1.0, M), inst("T2", 2.5, M, 5), elec("T3", 2.5, M, 5, 2000),
                aire("T4", 2.0, M, 12000));
        StringBuilder sb = new StringBuilder();
        for (OrdenServicio o : todas) {
            sb.append(sb.length() == 0 ? "" : "; ").append(o.getCategoria()).append(' ').append(q(o.calcularCosto()));
        }
        igual("K02", "Una lista de OrdenServicio con las 4 categorías, llamando solo calcularCosto() y getCategoria()",
                "Diagnóstico Q150.00; Instalación Q460.00; Instalación eléctrica certificada Q680.00; "
                        + "Mantenimiento de aire acondicionado Q400.00", sb.toString());
    }
}
