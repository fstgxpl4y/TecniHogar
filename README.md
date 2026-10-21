# TecniHogar

Desde la carpeta src tecnihogar ubicar la clase Main esta se le da ejecutar y empieza el programa

El menú se repite hasta que el usuario elige 0 para salir. Una opción que no sea número, un número fuera del menú o una entrada con formato incorrecto no cierran el programa: se muestra un mensaje y se vuelve a pedir el dato o al menú.

•	Tarifa base del mantenimiento: Q100, la tarifa base metropolitana de la instalación, porque el enunciado no la define. Está como constante TARIFA_BASE y falta confirmarla con la catedrática.
•	Códigos: se les quitan los espacios de los extremos y se comparan sin distinguir mayúsculas, así que "diag-001" y "DIAG-001" son el mismo código.
•	Valor de materiales: debe ser mayor que cero, igual que las demás cantidades.
•	Horas asignadas de una jornada: cuentan todas las órdenes que tiene la jornada, incluso una con visita fallida que ya volvió a pendiente, hasta que la jornada se cierra.
•	Cotización: el menú pide el código al principio porque la orden necesita uno para crearse. Si el código ya existe, la cotización se muestra igual y el rechazo ocurre al registrar.
•	Capacidad: se compara con un margen de 0.000000001 para que las sumas con decimales no fallen por redondeo. Los montos no se redondean en los cálculos, solo al mostrarlos con dos decimales.
