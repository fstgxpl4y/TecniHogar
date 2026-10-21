package tecnihogar;

import java.util.Locale;

/** Da formato a montos y horas para que todo se muestre igual. */
final class Formato {

    private Formato() {
    }

    /** Monto con dos decimales, por ejemplo Q1,234.50. */
    static String dinero(double monto) {
        return String.format(Locale.US, "Q%,.2f", monto);
    }

    /** Horas con dos decimales, por ejemplo 2.50 h. */
    static String horas(double horas) {
        return String.format(Locale.US, "%.2f h", horas);
    }

    /** Días con singular o plural, por ejemplo "1 día" o "3 días". */
    static String dias(int dias) {
        return dias + (dias == 1 ? " día" : " días");
    }
}
