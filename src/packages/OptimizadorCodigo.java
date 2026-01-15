package packages;

import estructuras.Lista;
import estructuras.Nodo;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

// Clase principal del optimizador de código intermedio
public class OptimizadorCodigo {

    // Lista que contendrá el código resultante
    public Lista codigoOptimizado;

    // Constructor para inicializar la lista
    public OptimizadorCodigo() {
        this.codigoOptimizado = new Lista();
    }

    // Método principal para optimizar el código intermedio
    public Lista optimizar(Lista codigoOriginal) {
        System.out.println("\n--- INICIANDO OPTIMIZACION ---");

        // 1. Optimización algebraica
        // Convierte operaciones como 'x + 0' en asignaciones 'x'
        Lista codigoFase1 = optimizacionAlgebraica(codigoOriginal);

        // 2. Propagación de copias
        // Esta fase permite que asignaciones como 'T1 = x' (resultado de la fase 1)
        // propaguen el valor 'x' a las siguientes instrucciones, dejando a 'T1' sin uso
        // para que la siguiente fase lo pueda eliminar
        Lista codigoFase1_5 = propagacionDeCopias(codigoFase1);

        // 3. Eliminación de instrucciones en donde el valor calculado no sea utilizado
        // Elimina temporales que se calculan pero nunca se usan
        Lista codigoFase2 = eliminarCodigoMuerto(codigoFase1_5);

        this.codigoOptimizado = codigoFase2;

        // Imprimir en consola y generar archivo
        imprimirCodigoOptimizado();
        generarArchivoOptimizado();
        
        return this.codigoOptimizado;
    }

    // Optimización algebraica
    // Busca patrones: x+0, 0+x, x*1, 1*x, x-0, x/1
    // Transforma la operación en una asignación simple para preservar el flujo
    private Lista optimizacionAlgebraica(Lista codigo) {
        Lista nuevaLista = new Lista(); 
        Nodo cursor = codigo.getInicio(); // Apuntador al inicio de la lista

        while (cursor != null) {
            String[] cuadruplo = cursor.datos; // [OP, ARG1, ARG2, RES]
            String op = cuadruplo[0];
            String arg1 = cuadruplo[1];
            String arg2 = cuadruplo[2];
            String res = cuadruplo[3];

            // Por defecto, se copia el cuádruplo tal cual
            String[] nuevoCuadruplo = cuadruplo;
            boolean optimizado = false; // Bandera para saber si se optimizó

            // Suma: x + 0  ó  0 + x
            if (op.equals("+")) {
                if (esCero(arg2)) { // Si x + 0 entonces = x null res
                    System.out.println("--- Op. Algebraica: " + res + " = " + arg1 + " + 0  -->  " + res + " = " + arg1);
                    nuevoCuadruplo = new String[]{"=", arg1, "null", res};
                    optimizado = true;
                } else if (esCero(arg1)) { // Si 0 + x entonces = x null res
                    System.out.println(">> Op. Algebraica: " + res + " = 0 + " + arg2 + "  -->  " + res + " = " + arg2);
                    nuevoCuadruplo = new String[]{"=", arg2, "null", res};
                    optimizado = true;
                }
            }
            // Resta: x - 0
            else if (op.equals("-")) {
                if (esCero(arg2)) { // Si x - 0 entonces = x null res
                    System.out.println("--- Op. Algebraica: " + res + " = " + arg1 + " - 0  -->  " + res + " = " + arg1);
                    nuevoCuadruplo = new String[]{"=", arg1, "null", res};
                    optimizado = true;
                }
            }
            // Multiplicación: x * 1  ó  1 * x
            else if (op.equals("*")) {
                if (esUno(arg2)) { // Si x * 1 entonces = x null res
                    System.out.println("--- Op. Algebraica: " + res + " = " + arg1 + " * 1  -->  " + res + " = " + arg1);
                    nuevoCuadruplo = new String[]{"=", arg1, "null", res};
                    optimizado = true;
                } else if (esUno(arg1)) { // Si 1 * x entonces = x null res
                    System.out.println("--- Op. Algebraica: " + res + " = 1 * " + arg2 + "  -->  " + res + " = " + arg2);
                    nuevoCuadruplo = new String[]{"=", arg2, "null", res};
                    optimizado = true;
                }
            }
            // División: x / 1
            else if (op.equals("/")) {
                if (esUno(arg2)) { // Si x / 1 entonces = x null res
                    System.out.println("--- Op. Algebraica: " + res + " = " + arg1 + " / 1  -->  " + res + " = " + arg1);
                    nuevoCuadruplo = new String[]{"=", arg1, "null", res};
                    optimizado = true;
                }
            }

            nuevaLista.add(nuevoCuadruplo); // Agrega el cuádruplo (optimizado o no) a la nueva lista
            cursor = cursor.der; // Avanza al siguiente cuádruplo
        }
        return nuevaLista; // Retorna la lista optimizada
    }

    // Optimización de propagación de copias
    // Reemplaza usos de temporales asignados directamente con su valor original
    // Ejemplo: Si tenemos T1 = X, y luego T2 = T1 + 5, se reemplaza T1 por X en la segunda instrucción
    // dejando T1 sin uso
    private Lista propagacionDeCopias(Lista codigo) {
        Nodo cursor = codigo.getInicio(); // Apuntador al inicio de la lista
        while (cursor != null) {
            String[] c = cursor.datos;
            // Buscamos asignaciones simples: RES = ARG1 (donde ARG1 no es null, ARG2 es null y OP es =)
            // Ejemplo: (=, X, null, T1) lo que significa T1 = X
            if (c[0].equals("=") && c[1] != null && c[2].equals("null")) {
                String temporal = c[3]; // La variable destino (Tx)
                String valorReal = c[1]; // El valor original (X)
                
                // Solo propagamos si el destino es un temporal (no se quitan variables del usuario)
                if (esTemporal(temporal)) {
                    System.out.println("--- Propagando copia: Reemplazando usos de " + temporal + " por " + valorReal);
                    reemplazarUsos(codigo, temporal, valorReal);
                }
            }
            cursor = cursor.der; // Avanza al siguiente cuádruplo
        }
        return codigo; // Retorna la lista con las propagaciones realizadas
    }

    // Optimización de eliminación de instrucciones en donde el valor calculado no sea utilizado
    // Busca temporales que se calculan pero nunca se usan
    // Es iterativo: si T2 usaba T1, y se borra T2, ahora T1 puede quedar inútil y borrarse también
    private Lista eliminarCodigoMuerto(Lista codigo) {
        boolean cambios = true; // Bandera para saber si hubo cambios en la pasada
        Lista listaActual = codigo; // Comenzamos con la lista original

        while (cambios) { // Repetimos hasta que no haya más cambios
            cambios = false;
            Lista listaLimpia = new Lista(); // Nueva lista optimizada
            Nodo cursor = listaActual.getInicio(); // Apuntador al inicio de la lista

            while (cursor != null) { // Recorremos cada cuádruplo
                String[] cuadruplo = cursor.datos;
                String res = cuadruplo[3]; // Variable resultado RES

                // Solo analizamos si el resultado es una variable temporal
                if (esTemporal(res)) {
                    
                    // Buscamos si este temporal se usa en el resto del código actual
                    if (esUtilizado(res, listaActual)) {
                        listaLimpia.add(cuadruplo); // Si se usa, se queda
                    } else {
                        System.out.println("--- Código no usado eliminado: " + cuadruplo[0] + " \t " + cuadruplo[1] + " \t " + cuadruplo[2] + " \t " + res);
                        cambios = true; // Si hubo un cambio se debe repetir el ciclo
                    }
                } else {
                    // Si no es temporal se conserva 
                    listaLimpia.add(cuadruplo);
                }
                cursor = cursor.der; // Avanza al siguiente cuádruplo
            }
            
            // Si hubo cambios, la lista limpia pasa a ser la lista actual para la siguiente pasada
            if (cambios) {
                listaActual = listaLimpia;
            }
        }
        
        return listaActual; // Retorna la lista optimizada final
    }

    // Genera un archivo de texto con el código optimizado
    private void generarArchivoOptimizado() {
        String rutaArchivo = "src/archivos/CodigoOptimizado.txt"; // Ruta del archivo de salida

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(rutaArchivo))) {
            
            writer.write("OP\tARG1\tARG2\tRES"); // Encabezado
            writer.newLine();
            writer.write("------------------------------------"); // Separador
            writer.newLine();

            Nodo cursor = codigoOptimizado.getInicio(); // Apuntador al inicio de la lista
            while (cursor != null) { // Recorremos cada cuádruplo
                String[] datos = cursor.datos;
                // Formato tabulado
                String linea = datos[0] + "\t" + datos[1] + "\t" + datos[2] + "\t" + datos[3];
                writer.write(linea); // Escribimos la línea en el archivo
                writer.newLine();
                cursor = cursor.der;
            }
            
            System.out.println("\nArchivo optimizado generado exitosamente en: " + rutaArchivo); // Mensaje de archivo generado

        } catch (IOException e) {
            System.err.println("Error al guardar el archivo optimizado: " + e.getMessage());
        }
    }

    // Verifica si una variable es un temporal
    private boolean esTemporal(String s) {
        // Asumimos que los temporales inician con T y le siguen dígitos
        if (s == null || s.length() < 2) return false;
        return s.charAt(0) == 'T' && Character.isDigit(s.charAt(1)); 
    }

    // Busca si una variable se usa en alguna parte de la lista (como ARG1 o ARG2)
    private boolean esUtilizado(String temporal, Lista codigo) {
        Nodo cursor = codigo.getInicio(); // Apuntador al inicio de la lista
        int usos = 0; // Contador de usos encontrados

        while (cursor != null) { // Recorremos cada cuádruplo
            String[] c = cursor.datos;
            String arg1 = c[1];
            String arg2 = c[2];

            // Verificamos si aparece como operando 1 u operando 2
            // No se cuenta cuando aparece en RES, porque eso es su definición, no su uso
            if ((arg1 != null && arg1.equals(temporal)) || 
                (arg2 != null && arg2.equals(temporal))) {
                usos++;
            }
            cursor = cursor.der; // Avanza al siguiente cuádruplo
        }
        return usos > 0; // Retorna true si se encontró al menos un uso
    }

    // Ayudante para reemplazar usos de una variable vieja por una nueva en toda la lista
    private void reemplazarUsos(Lista codigo, String viejo, String nuevo) {
        Nodo aux = codigo.getInicio(); // Apuntador al inicio de la lista
        while (aux != null) { // Recorremos cada cuádruplo
            // Reemplazar en operador 1
            if (aux.datos[1] != null && aux.datos[1].equals(viejo)) {
                aux.datos[1] = nuevo;
            }
            // Reemplazar en operador 2
            if (aux.datos[2] != null && aux.datos[2].equals(viejo)) {
                aux.datos[2] = nuevo;
            }
            aux = aux.der; // Avanza al siguiente cuádruplo
        }
    }

    // Verifica si una cadena representa el valor cero en diferentes formatos
    private boolean esCero(String s) {
        return s != null && (s.equals("0") || s.equals("0B") || s.equals("0O") || s.equals("0X"));
    }

    // Verifica si una cadena representa el valor uno en diferentes formatos
    private boolean esUno(String s) {
        return s != null && (s.equals("1") || s.equals("1B") || s.equals("1O") || s.equals("1X"));
    }

    // Imprime el código optimizado en consola
    public void imprimirCodigoOptimizado() {
        System.out.println("\n--- CODIGO INTERMEDIO FINAL (OPTIMIZADO) ---");
        System.out.println("OP \t ARG1 \t ARG2 \t RES");
        System.out.println("------------------------------------");
        Nodo cursor = codigoOptimizado.getInicio(); // Apuntador al inicio de la lista
        while (cursor != null) { // Recorremos cada cuádruplo
            String[] c = cursor.datos;
            System.out.println(c[0] + " \t " + c[1] + " \t " + c[2] + " \t " + c[3]);
            cursor = cursor.der; // Avanza al siguiente cuádruplo
        }
    }
}