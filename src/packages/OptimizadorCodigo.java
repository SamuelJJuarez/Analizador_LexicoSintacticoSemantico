package packages;

import estructuras.Lista;
import estructuras.Nodo;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class OptimizadorCodigo {

    // Lista que contendrá el código resultante
    public Lista codigoOptimizado;

    public OptimizadorCodigo() {
        this.codigoOptimizado = new Lista();
    }

    /**
     * Método principal que orquesta las fases y genera el archivo.
     */
    public Lista optimizar(Lista codigoOriginal) {
        System.out.println("\n--- INICIANDO OPTIMIZACION ---");

        // FASE 1: Optimización Algebraica (Mirilla)
        // Convierte operaciones como 'x + 0' en asignaciones 'x'
        Lista codigoFase1 = optimizacionAlgebraica(codigoOriginal);

        // FASE 2: Eliminación de Código Muerto (Global)
        // Elimina temporales que se calculan pero nunca se usan. Se repite hasta que no haya cambios.
        Lista codigoFase2 = eliminarCodigoMuerto(codigoFase1);

        this.codigoOptimizado = codigoFase2;

        // Imprimir en consola y generar archivo
        imprimirCodigoOptimizado();
        generarArchivoOptimizado();
        
        return this.codigoOptimizado;
    }

    /**
     * FASE 1: Reducción Algebraica
     * Busca patrones: x+0, 0+x, x*1, 1*x, x-0, x/1
     * Transforma la operación en una asignación simple (=) para preservar el flujo.
     */
    private Lista optimizacionAlgebraica(Lista codigo) {
        Lista nuevaLista = new Lista();
        Nodo cursor = codigo.getInicio();

        while (cursor != null) {
            String[] cuadruplo = cursor.datos;
            String op = cuadruplo[0];
            String arg1 = cuadruplo[1];
            String arg2 = cuadruplo[2];
            String res = cuadruplo[3];

            // Por defecto, copiamos el cuádruplo tal cual
            String[] nuevoCuadruplo = cuadruplo;
            boolean optimizado = false;

            // CASO SUMA (+): x + 0  ó  0 + x
            if (op.equals("+")) {
                if (esCero(arg2)) { // x + 0 -> = x null res
                    System.out.println(">> Op. Algebraica: " + res + " = " + arg1 + " + 0  -->  " + res + " = " + arg1);
                    nuevoCuadruplo = new String[]{"=", arg1, "null", res};
                    optimizado = true;
                } else if (esCero(arg1)) { // 0 + x -> = x null res
                    System.out.println(">> Op. Algebraica: " + res + " = 0 + " + arg2 + "  -->  " + res + " = " + arg2);
                    nuevoCuadruplo = new String[]{"=", arg2, "null", res};
                    optimizado = true;
                }
            }
            // CASO RESTA (-): x - 0
            else if (op.equals("-")) {
                if (esCero(arg2)) { // x - 0 -> = x null res
                    System.out.println(">> Op. Algebraica: " + res + " = " + arg1 + " - 0  -->  " + res + " = " + arg1);
                    nuevoCuadruplo = new String[]{"=", arg1, "null", res};
                    optimizado = true;
                }
            }
            // CASO MULTIPLICACION (*): x * 1  ó  1 * x
            else if (op.equals("*")) {
                if (esUno(arg2)) { // x * 1 -> = x null res
                    System.out.println(">> Op. Algebraica: " + res + " = " + arg1 + " * 1  -->  " + res + " = " + arg1);
                    nuevoCuadruplo = new String[]{"=", arg1, "null", res};
                    optimizado = true;
                } else if (esUno(arg1)) { // 1 * x -> = x null res
                    System.out.println(">> Op. Algebraica: " + res + " = 1 * " + arg2 + "  -->  " + res + " = " + arg2);
                    nuevoCuadruplo = new String[]{"=", arg2, "null", res};
                    optimizado = true;
                }
            }
            // CASO DIVISION (/): x / 1
            else if (op.equals("/")) {
                if (esUno(arg2)) { // x / 1 -> = x null res
                    System.out.println(">> Op. Algebraica: " + res + " = " + arg1 + " / 1  -->  " + res + " = " + arg1);
                    nuevoCuadruplo = new String[]{"=", arg1, "null", res};
                    optimizado = true;
                }
            }

            nuevaLista.add(nuevoCuadruplo);
            cursor = cursor.der;
        }
        return nuevaLista;
    }

    /**
     * FASE 2: Eliminación de Código Muerto
     * Elimina instrucciones que calculan un Temporal (T1, T2...) que nunca se usa posteriormente.
     * Es recursivo/iterativo: si T2 usaba T1, y borramos T2, ahora T1 puede quedar inútil y borrarse también.
     */
    private Lista eliminarCodigoMuerto(Lista codigo) {
        boolean cambios = true;
        Lista listaActual = codigo;

        while (cambios) {
            cambios = false;
            Lista listaLimpia = new Lista();
            Nodo cursor = listaActual.getInicio();

            while (cursor != null) {
                String[] cuadruplo = cursor.datos;
                String res = cuadruplo[3]; // Variable resultado

                // Solo analizamos si el resultado es una Variable Temporal (T1, T2...)
                // Las variables del usuario (a, b, x) NO se tocan.
                if (esTemporal(res)) {
                    
                    // Buscamos si este temporal 'res' se usa en el resto del código actual
                    if (esUtilizado(res, listaActual)) {
                        listaLimpia.add(cuadruplo); // Se usa, se queda.
                    } else {
                        System.out.println(">> Código Muerto Eliminado: " + cuadruplo[0] + " \t " + cuadruplo[1] + " \t " + cuadruplo[2] + " \t " + res);
                        cambios = true; // Hubo un cambio, debemos repetir el ciclo
                    }
                } else {
                    // Si no es temporal (es variable de usuario o null), se conserva siempre
                    listaLimpia.add(cuadruplo);
                }
                cursor = cursor.der;
            }
            
            // Si hubo cambios, la lista limpia pasa a ser la lista actual para la siguiente pasada
            if (cambios) {
                listaActual = listaLimpia;
            }
        }
        
        return listaActual;
    }

    // --- MÉTODOS AUXILIARES Y DE ARCHIVOS ---

    private void generarArchivoOptimizado() {
        String rutaArchivo = "src/archivos/SalidaOptimizacion.txt";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(rutaArchivo))) {
            
            writer.write("OP\tARG1\tARG2\tRES");
            writer.newLine();
            writer.write("------------------------------------");
            writer.newLine();

            Nodo cursor = codigoOptimizado.getInicio();
            while (cursor != null) {
                String[] datos = cursor.datos;
                // Formato tabulado
                String linea = datos[0] + "\t" + datos[1] + "\t" + datos[2] + "\t" + datos[3];
                writer.write(linea);
                writer.newLine();
                cursor = cursor.der;
            }
            
            System.out.println("\nArchivo Optimizado generado exitosamente en: " + rutaArchivo);

        } catch (IOException e) {
            System.err.println("Error al guardar el archivo optimizado: " + e.getMessage());
        }
    }

    private boolean esTemporal(String s) {
        // Asumimos que los temporales inician con 'T' y siguen con dígitos (ej: T1, T10)
        if (s == null || s.length() < 2) return false;
        return s.charAt(0) == 'T' && Character.isDigit(s.charAt(1));
    }

    private boolean esUtilizado(String temporal, Lista codigo) {
        Nodo cursor = codigo.getInicio();
        int usos = 0;

        while (cursor != null) {
            String[] c = cursor.datos;
            String arg1 = c[1];
            String arg2 = c[2];

            // Verificamos si aparece como operando 1 u operando 2
            // OJO: No contamos cuando aparece en RES, porque eso es su definición, no su uso.
            if ((arg1 != null && arg1.equals(temporal)) || 
                (arg2 != null && arg2.equals(temporal))) {
                usos++;
            }
            cursor = cursor.der;
        }
        return usos > 0;
    }

    private boolean esCero(String s) {
        return s != null && (s.equals("0") || s.equals("0B") || s.equals("0O") || s.equals("0X"));
    }

    private boolean esUno(String s) {
        return s != null && (s.equals("1") || s.equals("1B") || s.equals("1O") || s.equals("1X"));
    }

    public void imprimirCodigoOptimizado() {
        System.out.println("\n--- CODIGO INTERMEDIO FINAL (OPTIMIZADO) ---");
        System.out.println("OP \t ARG1 \t ARG2 \t RES");
        System.out.println("------------------------------------");
        Nodo cursor = codigoOptimizado.getInicio();
        while (cursor != null) {
            String[] c = cursor.datos;
            System.out.println(c[0] + " \t " + c[1] + " \t " + c[2] + " \t " + c[3]);
            cursor = cursor.der;
        }
    }
}
