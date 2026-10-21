package tecnihogar;

import java.util.Locale;


final class Formato {

    private Formato() {
    }

    static String dinero(double monto) {
        return String.format(Locale.US, "Q%,.2f", monto);
    }

    static String horas(double horas) {
        return String.format(Locale.US, "%.2f h", horas);
    }

    static String dias(int dias) {
        return dias + (dias == 1 ? " día" : " días");
    }
}
