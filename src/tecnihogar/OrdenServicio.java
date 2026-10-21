package tecnihogar;


public abstract class OrdenServicio {

    private final String codigo;
    private final String nombreCliente;
    private final String direccion;
    private final double horasEstimadas;
    private final Zona zona;
    private EstadoOrden estado;
    private int visitasRealizadas;

    protected OrdenServicio(String codigo, String nombreCliente, String direccion,
                            double horasEstimadas, Zona zona) {
        if (codigo == null || codigo.trim().isEmpty()) {
            throw new IllegalArgumentException("El código de la orden no puede estar vacío.");
        }
        if (nombreCliente == null || nombreCliente.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del cliente no puede estar vacío.");
        }
        if (direccion == null || direccion.trim().isEmpty()) {
            throw new IllegalArgumentException("La dirección no puede estar vacía.");
        }
        if (Double.isNaN(horasEstimadas) || Double.isInfinite(horasEstimadas) || horasEstimadas <= 0) {
            throw new IllegalArgumentException("Las horas estimadas deben ser un número mayor que cero.");
        }
        if (zona == null) {
            throw new IllegalArgumentException("Hay que indicar la zona del servicio.");
        }
        this.codigo = codigo.trim();
        this.nombreCliente = nombreCliente.trim();
        this.direccion = direccion.trim();
        this.horasEstimadas = horasEstimadas;
        this.zona = zona;
        this.estado = EstadoOrden.PENDIENTE;
        this.visitasRealizadas = 0;
    }


    public abstract double calcularCosto();

    public abstract int calcularDiasAtencion();

    public abstract int getMaxVisitas();

    public abstract boolean esCompatibleCon(Cuadrilla cuadrilla);

    public abstract String getCategoria();

    public String getCodigo() {
        return codigo;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public String getDireccion() {
        return direccion;
    }

    public double getHorasEstimadas() {
        return horasEstimadas;
    }

    public Zona getZona() {
        return zona;
    }

    public EstadoOrden getEstado() {
        return estado;
    }

    public int getVisitasRealizadas() {
        return visitasRealizadas;
    }


    void iniciarVisita() {
        if (estado != EstadoOrden.PENDIENTE) {
            throw new IllegalStateException("La orden " + codigo + " está " + estado
                    + " y no puede iniciar una visita.");
        }
        if (visitasRealizadas >= getMaxVisitas()) {
            throw new IllegalStateException("La orden " + codigo + " ya agotó sus visitas.");
        }
        visitasRealizadas++;
        estado = EstadoOrden.EN_ATENCION;
    }


    void registrarResultado(ResultadoVisita resultado) {
        if (estado != EstadoOrden.EN_ATENCION) {
            throw new IllegalStateException("La orden " + codigo + " está " + estado
                    + " y no tiene una visita en curso.");
        }
        if (resultado == ResultadoVisita.COMPLETADA) {
            estado = EstadoOrden.COMPLETADA;
        } else if (visitasRealizadas >= getMaxVisitas()) {
            estado = EstadoOrden.CANCELADA;
        } else {
            estado = EstadoOrden.PENDIENTE;
        }
    }


    public String describir() {
        return String.format("%s | %s | %s | %s | %s | %s | %s | visitas %d/%d | %s | %s",
                codigo, getCategoria(), nombreCliente, direccion, zona,
                Formato.horas(horasEstimadas), estado, visitasRealizadas, getMaxVisitas(),
                Formato.dinero(calcularCosto()), Formato.dias(calcularDiasAtencion()));
    }

    @Override
    public String toString() {
        return codigo + " (" + getCategoria() + ")";
    }
}
