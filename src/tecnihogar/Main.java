package tecnihogar;

/** Punto de entrada del programa. */
public class Main {

    public static void main(String[] args) {
        SistemaTecniHogar sistema = new SistemaTecniHogar();
        sistema.cargarDatosIniciales();
        MenuConsola menu = new MenuConsola(sistema);
        menu.iniciar();
    }
}
