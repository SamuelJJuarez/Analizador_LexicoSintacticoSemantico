package packages;

import estructuras.Lista;
import estructuras.Nodo;
import estructuras.Pila;
import estructuras.Pila.NodoD;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class AnalizadorSintactico {

    //Variables
    Lista gramatica;
    Lista ladosDerechos;
    Lista terminales;
    Lista noTerminales;
    String simboloInicial;
    MatrizPredictiva matrizObj;
    int[][] matrizPredictiva;
    AnalizadorLexico analizadorLexico;
    Pila pilaSemantica = new Pila();

    //ESTRUCTURAS PARA CODIGO INTERMEDIO ---
    Lista codigoIntermedio = new Lista(); // Guardará arrays String[]{operador, arg1, arg2, res}
    Pila pilaOperandos = new Pila();      // Guardará IDs, Literales y Temporales (T1, T2...)
    Pila pilaOperadores = new Pila();     // Guardará +, -, *, /
    int contadorTemporales = 1;           // Para generar T1, T2, T3
    String idObjetivoAsignacion = "";     // Para recordar a quién asignar el resultado (a = ...)

    boolean esSimbolo = false;
    String tipo;
    String lexema;

    boolean asignacion = false;
    String temp;
    String tipoTemp;
    Pila pilaAsignacion = new Pila();

    boolean errorSintactico = false;

    public AnalizadorSintactico(GeneradorEstructuras estructuras, MatrizPredictiva matriz, AnalizadorLexico lexico) {
        gramatica = estructuras.gramatica;
        ladosDerechos = estructuras.ladoDerecho;
        terminales = estructuras.terminales;
        noTerminales = estructuras.noTerminales;
        simboloInicial = estructuras.simboloInicial;
        matrizObj = matriz;
        matrizPredictiva = matrizObj.matrizPredictiva;
        analizadorLexico = lexico;

        pilaSemantica.push(simboloInicial);

    }

    public void LLDriver() {
        String a = tokenAValorTerminal(analizadorLexico.analisisLexico());
        String x = pilaSemantica.peek().toString();

        // Bandera para saber si estamos antes o después del '='
        boolean leyendoLadoIzquierdo = true;

        while (!pilaSemantica.isEmpty()) {

            // Verificamos si x es No Terminal
            if (noTerminales.repetido(x)) {
                int posX = matrizObj.getFila(x);
                int posA = matrizObj.getColumna(a);

                if (matrizPredictiva[posX][posA] != 0) {
                    int numeroRegla = matrizPredictiva[posX][posA]; 
                    pilaSemantica.pop();

                    // --- LOGICA ARITMETICA (Regla 14: <exprf> -> epsilon) ---
                    if (numeroRegla == 14) {
                        generarCuadruploAritmetico();
                    }
                    // --------------------------------------------------------

                    Nodo cursor = ladosDerechos.getInicio();
                    for (int i = 0; i < numeroRegla - 1; i++) {
                        cursor = cursor.der;
                    }

                    if (!(cursor.datos.length == 1 && cursor.datos[0].equals("ε"))) {
                        for (int i = cursor.datos.length - 1; i >= 0; i--) {
                            pilaSemantica.push(cursor.datos[i]);
                        }
                    }
                    x = pilaSemantica.peek().toString();

                } else {
                    System.out.println("Error Sintáctico: Se esperaba " + x + " pero llegó " + a);
                    break;
                }

            } else { // Else x es Terminal
                
                esSimbolo(x); // Actualiza la variable global 'tipo' y 'lexema'


                // 1. Asignación (=)
                if (x.equals("=")) {
                    leyendoLadoIzquierdo = false;
                    
                    // Lógica Tipos (Original)
                    pilaAsignacion.push(new String[]{temp, tipoTemp}); 
                    pilaAsignacion.push(new String[]{x, x});
                }
                
                // 2. Operadores (+, -, *, /)
                else if (x.equals("+") || x.equals("-") || x.equals("*") || x.equals("/")) {
                    // Lógica Cuádruplos
                    pilaOperadores.push(lexema);
                    
                    // Lógica Tipos: También debemos guardar el operador para que no se pierda la cuenta
                    if (!pilaAsignacion.isEmpty()) {
                        pilaAsignacion.push(new String[]{x, x});
                    }
                }

                // 3. Operandos (Identificadores y Literales)
                else if (x.equals("id") || x.equals("litbinaria") || x.equals("litoctal") || x.equals("lithexa")) {
                    
                    // A) Lógica Cuádruplos
                    if (leyendoLadoIzquierdo) {
                        idObjetivoAsignacion = lexema;
                    } else {
                        pilaOperandos.push(lexema);
                    }

                    // B) Lógica Tipos (Original restaurada)
                    if (!pilaAsignacion.isEmpty()) {
                        if (x.equals("id")) {
                            // En caso de ID, necesitamos el tipo que obtuvimos de la tabla de símbolos
                            pilaAsignacion.push(new String[]{x, tipo}); 
                        } else {
                            // En caso de literal (litbinaria, etc), el 'tipo' ya viene correcto de esSimbolo()
                            pilaAsignacion.push(new String[]{x, tipo}); 
                        }
                    }
                }
                
                // 4. Punto y Coma (;) - Fin de sentencia
                else if (x.equals(";")) {
                    
                    // A) Lógica Cuádruplos: Generar asignación final
                    if (!pilaOperandos.isEmpty()) {
                        String resultadoFinal = pilaOperandos.pop().toString();
                        codigoIntermedio.add(new String[]{"=", resultadoFinal, "null", idObjetivoAsignacion});
                    }
                    leyendoLadoIzquierdo = true; 

                    // B) Lógica Tipos: Preparar y llamar a verificación
                    if (!pilaAsignacion.isEmpty()) {
                        pilaAsignacion.push(new String[]{";", ";"}); 
                        verificacionTipos();
                    }
                }
                
                // Agregamos un simbolo a la variable temporal por si empieza una asignacion (Lógica original)
                if (esSimbolo) {
                    temp = x;
                    tipoTemp = tipo;
                }

                // --------------------------------------------------------------

                // Emparejamiento (Match)
                if (x.equals(a)) {
                    pilaSemantica.pop();
                    if (!pilaSemantica.isEmpty()) {
                        a = tokenAValorTerminal(analizadorLexico.analisisLexico());
                        x = pilaSemantica.peek().toString();
                    }
                } else {
                    System.out.println("Error: Se esperaba " + x);
                    break;
                }
            }
        }
        
        // Imprimir resultados al final
        System.out.println("\n--- CODIGO INTERMEDIO GENERADO ---");
        System.out.println("OP \t ARG1 \t ARG2 \t RES");
        Nodo cursorCI = codigoIntermedio.getInicio();
        while(cursorCI != null) {
            String[] c = cursorCI.datos;
            System.out.println(c[0] + " \t " + c[1] + " \t " + c[2] + " \t " + c[3]);
            cursorCI = cursorCI.der;
        }
        
        guardarCodigoIntermedio();
    }

    public void esSimbolo(String x) {

        switch (x) {

            // Identificador y literales
            case "id":

                if (esSimbolo) {
                    analizadorLexico.reemplazarTipoEnTablaSimbolos(lexema, tipo);
                }

                return;

            case "litbinaria":

                esSimbolo = false;
                tipo = "litbinaria";
                return;

            case "litoctal":

                esSimbolo = false;
                tipo = "litoctal";
                return;

            case "lithexa":

                esSimbolo = false;
                tipo = "lithexa";
                return;

            case "programa":

                esSimbolo = true;
                tipo = "programa";
                return;

            case "binario":

                esSimbolo = true;
                tipo = "binario";
                return;

            case "octal":

                esSimbolo = true;
                tipo = "octal";
                return;

            case "hexad":

                esSimbolo = true;
                tipo = "hexad";
                return;

            case "leer":

                esSimbolo = false;
                tipo = "leer";
                return;

            case "escribir":

                esSimbolo = false;
                tipo = "escribir";
                return;

            case "finprograma":

                esSimbolo = false;
                tipo = "finprograma";
                return;

            default:
                return;
        }
    }

    public String tokenAValorTerminal(String[] token) {
        String atributo = token[1]; // Número del atributo del token

        lexema = token[0];

        switch (atributo) {

            // Identificador y literales
            case "310":
                return "id";
            case "280":
                return "litbinaria";
            case "290":
                return "litoctal";
            case "300":
                return "lithexa";

            // Palabras reservadas
            case "256":
                return "programa";
            case "257":
                return "binario";
            case "258":
                return "octal";
            case "259":
                return "hexad";
            case "260":
                return "leer";
            case "261":
                return "escribir";
            case "262":
                return "finprograma";

            // Caracteres especiales
            case "59":
                return ";";
            case "61":
                return "=";
            case "43":
                return "+";
            case "45":
                return "-";
            case "47":
                return "/";
            case "42":
                return "*";
            case "44":
                return ",";
            case "40":
                return "(";
            case "41":
                return ")";

            // Fin de entrada
            case "999":
                return "$";

            default:
                System.err.println("Atributo no reconocido: " + atributo);
                return "ERROR";
        }
    }

    //Metodo para verificar que todas las variables sean del mismo tipo
    public void verificacionTipos() {
        //Imprimimos la pila de Asignacion
        System.out.println(pilaAsignacion.imprimir());

        //Quitamos el ;
        NodoD cursor = pilaAsignacion.pop();
        cursor = pilaAsignacion.pop();
        String[] tipoAsignacion = (String[]) cursor.valor;
        String tipoA = "";

        switch (tipoAsignacion[1]) {
            case "binario":
                tipoA = "Binarios";
                break;
            case "litbinaria":
                tipoA = "Binarios";
                break;
            case "octal":
                tipoA = "Octales";
                break;
            case "litoctal":
                tipoA = "Octales";
                break;
            case "hexad":
                tipoA = "Hexadecimales";
                break;
            case "lithexa":
                tipoA = "Hexadecimales";
                break;
            default:
                throw new AssertionError();
        }

        while (!pilaAsignacion.isEmpty()) {
            String[] valores = (String[]) cursor.valor;
            if (valores[0].equals("id")) {
                switch (tipoA) {
                    case "Binarios":
                        if (!valores[1].equals("binario") && !valores[1].equals("litbinaria")) {
                            errorSintactico = true;
                        }
                        break;
                    case "Octales":
                        if (!valores[1].equals("octal") && !valores[1].equals("litoctal")) {
                            errorSintactico = true;
                        }
                        break;
                    case "Hexadecimales":
                        if (!valores[1].equals("hexad") && !valores[1].equals("lithexa")) {
                            errorSintactico = true;
                        }
                        break;
                }
            }

            cursor = pilaAsignacion.pop();
        }

    }

    private void generarCuadruploAritmetico() {
        // Mientras haya operadores pendientes en la pila
        while (!pilaOperadores.isEmpty()) {
            String operador = pilaOperadores.pop().toString();

            // Necesitamos 2 operandos para +, -, *, /
            if (!pilaOperandos.isEmpty()) {
                String operando2 = pilaOperandos.pop().toString(); // El último en entrar es el de la derecha

                if (!pilaOperandos.isEmpty()) {
                    String operando1 = pilaOperandos.pop().toString(); // El penúltimo es el de la izquierda

                    // Generar Temporal
                    String temporal = "T" + contadorTemporales++;

                    // Crear Cuadruplo: { +, a, b, T1 }
                    codigoIntermedio.add(new String[]{operador, operando1, operando2, temporal});

                    // El resultado (T1) se convierte en operando para la siguiente operación
                    pilaOperandos.push(temporal);
                } else {
                    // Caso raro: operador unario o error de pila, devolvemos el op2
                    pilaOperandos.push(operando2);
                }
            }
        }
    }

    // Método para guardar el código intermedio en un archivo TXT
    public void guardarCodigoIntermedio() {
        // Ruta relativa igual a la que usas en tu Entrada.txt
        String rutaArchivo = "src/archivos/CodigoIntermedio.txt";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(rutaArchivo))) {
            
            // Escribir encabezado
            writer.write("OP\tARG1\tARG2\tRES");
            writer.newLine();
            writer.write("------------------------------------");
            writer.newLine();

            // Recorrer la lista de código intermedio
            Nodo cursor = codigoIntermedio.getInicio();
            while (cursor != null) {
                String[] datos = cursor.datos;
                
                // Formatear la línea con tabuladores para que se vea ordenado
                // datos[0]=OP, datos[1]=ARG1, datos[2]=ARG2, datos[3]=RES
                String linea = datos[0] + "\t" + datos[1] + "\t" + datos[2] + "\t" + datos[3];
                
                writer.write(linea);
                writer.newLine(); // Salto de línea
                
                cursor = cursor.der;
            }
            
            System.out.println("Archivo de Código Intermedio generado exitosamente en: " + rutaArchivo);

        } catch (IOException e) {
            System.err.println("Error al guardar el archivo de Código Intermedio: " + e.getMessage());
        }
    }
    
}
