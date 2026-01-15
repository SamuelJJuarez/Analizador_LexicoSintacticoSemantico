package packages;

import estructuras.Lista;
import estructuras.Nodo;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AnalizadorLexico {

    //Variables
    File archivo = new File("..\\AnalizadorLexicoSintactico\\src\\archivos\\Entrada.txt");
    static char[] caracteresSimples = {';', '=', '+', '-', '/', '*', ',', '(', ')'};
    static String[] reservadas = {"programa", "binario", "octal", "hexad", "leer", "escribir", "finprograma"};

    static int nSimbolo = 400;
    static Lista tablaDeSimbolos = new Lista();
    static Lista tablaDePalabrasReservadas = new Lista();
    static Lista tablaDeTokensValidos = new Lista();
    static Lista tablaDeErrores = new Lista();

    Scanner lector;
    String[] token;

    //Variables automata
    int estado = 0;
    int nlinea = 1;
    int i;
    int f;

    String linea;
    String codigoError = "";

    public AnalizadorLexico() {

        generarTablaDePalabrasReservadas();

        try {
            // Ruta del archivo
            lector = new Scanner(archivo);
            linea = lector.nextLine();
            i = 0;
            f = 0;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(AnalizadorLexico.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    //Metodo Principal
    public String[] analisisLexico() {

        // Ciclo para leer el token
        while (true) {

            //Verificar si hay mas caracteres en la linea
            if (i == f && f >= linea.length()) {
                nlinea++;
                i = 0;
                f = 0;

                //Verificar si hay mas lineas que leer
                if (lector.hasNextLine()) {
                    linea = lector.nextLine();
                } else {
                    lector.close();
                    break;
                }

            }

            char c = ' ';

            if (f < linea.length()) {
                c = linea.charAt(f);
            }

            switch (estado) {

                //Estado 0 (inicial)
                case 0:

                    //Caracter
                    if (esChar(c)) {
                        estado = 1;
                        f++;

                        //Numero Binario
                    } else if (esNumB(c)) {
                        estado = 2;
                        f++;

                        //Numero Octal
                    } else if (esNumO(c)) {
                        estado = 4;
                        f++;

                        //Numero Hexadecimal
                    } else if (esNumX(c)) {
                        estado = 5;
                        f++;

                        //Caracter Especial
                    } else if (esCharE(c)) {
                        f++;
                        generarToken(i, f, linea, '6', nlinea);
                        i = f;
                        return token;

                        //Espacios
                    } else if (c == ' ' || c == '\t') {
                        i++;
                        f++;

                        //Otros
                    } else {
                        estado = 6;
                        codigoError = "Caracter no Valido al Inicio de un Token";

                    }
                    break;

                //Estado 1 (palabras)
                case 1:

                    //Caracter
                    if (esChar(c)) {
                        f++;

                        //Otros
                    } else {
                        generarToken(i, f, linea, '2', nlinea);
                        i = f;
                        estado = 0;
                        return token;
                    }
                    break;

                //Estado 2 (Binarios)
                case 2:

                    //Binario
                    if (esNumB(c)) {
                        f++;

                        //Octal
                    } else if (esNumO(c)) {
                        estado = 4;
                        f++;

                        //B
                    } else if (esFinB(c)) {
                        estado = 3;
                        f++;

                        //Hexadecimal
                    } else if (esNumX(c)) {
                        estado = 5;
                        f++;

                        //O
                    } else if (esFinO(c)) {
                        f++;
                        generarToken(i, f, linea, '4', nlinea);
                        i = f;
                        estado = 0;
                        return token;

                        //X
                    } else if (esFinX(c)) {
                        f++;
                        generarToken(i, f, linea, '5', nlinea);
                        i = f;
                        estado = 0;
                        return token;

                        //Otro
                    } else {
                        estado = 6;
                        codigoError = "Numero incorrecto";
                    }
                    break;

                //Estado 3 (B)
                case 3:

                    //Hexadecimal
                    if (esNumX(c)) {
                        estado = 5;
                        f++;

                        //X
                    } else if (esFinX(c)) {
                        f++;
                        generarToken(i, f, linea, '5', nlinea);
                        i = f;
                        estado = 0;
                        return token;

                        //Otros
                    } else {
                        generarToken(i, f, linea, '3', nlinea);
                        i = f;
                        estado = 0;
                        return token;
                    }
                    break;

                //Estado 4 (Octales)
                case 4:

                    //Octal
                    if (esNumO(c) || esNumB(c)) {
                        f++;

                        //Hexadecimal
                    } else if (esNumX(c)) {
                        f++;
                        estado = 5;

                        //O
                    } else if (esFinO(c)) {
                        f++;
                        generarToken(i, f, linea, '4', nlinea);
                        i = f;
                        estado = 0;
                        return token;

                        //X
                    } else if (esFinX(c)) {
                        f++;
                        generarToken(i, f, linea, '5', nlinea);
                        i = f;
                        estado = 0;
                        return token;

                        //Otros
                    } else {
                        estado = 6;
                        codigoError = "Numero incorrecto";
                    }
                    break;

                //Estado 5 (Hexadecimales)
                case 5:

                    //Hexadecimal
                    if (esNumX(c) || esNumO(c) || esNumB(c)) {
                        f++;

                        //X
                    } else if (esFinX(c)) {
                        f++;
                        generarToken(i, f, linea, '5', nlinea);
                        i = f;
                        estado = 0;
                        return token;

                        //Otros
                    } else {
                        estado = 6;
                        codigoError = "Numero Incorrecto";
                    }
                    break;

                //Estado 6 (Error)
                case 6:

                    // Caracter Especial, Espacio o Numero
                    if (esCharE(c) || esNumB(c) || esNumO(c) || esNumX(c) || c == ' ') {
                        generarErrores(i, f, nlinea, linea, codigoError);
                        i = f;
                        estado = 0;
                        return token;

                    } else {
                        f++;
                    }
                    break;

            }

        }//While linea

//        System.out.println("TOKENS\n" + tablaDeTokensValidos.toString());
//        System.out.println("\nSIMBOLOS\n" + tablaDeSimbolos.toString());
//        System.out.println("\nERRORES\n" + tablaDeErrores.toString());
//        System.out.println("\nPALABRAS RESERVADAS\n" + tablaDePalabrasReservadas.toString());
//        System.out.println(tablaDePalabrasReservadas.imprimirPalabrasReservadas());
//        System.out.println(tablaDeSimbolos.imprimirTablaDeSimbolos());
//        System.out.println(tablaDeTokens.imprimirTokens());
//        System.out.println(tablaDeErrores.imprimirErrores());
        return null;
    }

    //Generar tabla de simbolos
    public static void generarTablaDePalabrasReservadas() {
        tablaDePalabrasReservadas.add(new String[]{"programa", "256"});
        tablaDePalabrasReservadas.add(new String[]{"binario", "257"});
        tablaDePalabrasReservadas.add(new String[]{"octal", "258"});
        tablaDePalabrasReservadas.add(new String[]{"hexad", "259"});
        tablaDePalabrasReservadas.add(new String[]{"leer", "260"});
        tablaDePalabrasReservadas.add(new String[]{"escribir", "261"});
        tablaDePalabrasReservadas.add(new String[]{"finprograma", "262"});
    }

    //Metodos de transicion
    public static boolean esChar(char caracter) {
        return caracter >= 'a' && caracter <= 'z';
    }

    public static boolean esNumB(char caracter) {
        return caracter >= '0' && caracter <= '1';
    }

    public static boolean esFinB(char caracter) {
        return caracter == 'B';
    }

    public static boolean esNumO(char caracter) {
        return caracter >= '2' && caracter <= '7';
    }

    public static boolean esFinO(char caracter) {
        return caracter == 'O';
    }

    public static boolean esNumX(char caracter) {
        return caracter >= '8' && caracter <= '9' || caracter >= 'A' && caracter <= 'F';
    }

    public static boolean esFinX(char caracter) {
        return caracter == 'X';
    }

    //Metodos de validacion 
    public static boolean esCharE(char caracter) {

        for (char carSimple : caracteresSimples) {
            if (caracter == carSimple) {
                return true;
            }
        }
        return false;
    }

    public static boolean esReservada(String palabra) {

        for (String reservada : reservadas) {
            if (palabra.equals(reservada)) {
                return true;
            }
        }
        return false;
    }

    //Metodos de atributos
    public static String atributoCharE(char especial) {
        switch (especial) {
            case ';':
                return "59";
            case '=':
                return "61";
            case '+':
                return "43";
            case '-':
                return "45";
            case '/':
                return "47";
            case '*':
                return "42";
            case ',':
                return "44";
            case '(':
                return "40";
            case ')':
                return "41";
            default:
                throw new AssertionError();
        }
    }

    public static String atributoReservada(String palabra) {
        switch (palabra) {
            case "programa":
                return "256";
            case "binario":
                return "257";
            case "octal":
                return "258";
            case "hexad":
                return "259";
            case "leer":
                return "260";
            case "escribir":
                return "261";
            case "finprograma":
                return "262";
            default:
                throw new IllegalArgumentException("Palabra reservada no reconocida: " + palabra);
        }
    }

    //Metodo de generacion de tokens
    public void generarToken(int i, int f, String linea, char id, int nlinea) {
        String lexema = "";
        for (int j = i; j < f; j++) {
            lexema += linea.charAt(j);
        }
        if (esReservada(lexema)) {
            System.out.println("");
            id = '1';
        }

        //Palabras reservadas
        if (id == '1') {
            token = new String[]{lexema, atributoReservada(lexema)};
            tablaDeTokensValidos.add(new String[]{atributoReservada(lexema), lexema, "" + nlinea});

            //Identificador
        } else if (id == '2') {
            token = new String[]{lexema, "310"};
            tablaDeTokensValidos.add(new String[]{"310", lexema, "" + nlinea});

            if (!simboloExistente(lexema)) {
                // lexema - tipo - nSimbolo - repeticiones - lineas - valor
                tablaDeSimbolos.add(new String[]{lexema, "", nSimbolo + "", "1", "" + nlinea, "0.0"});
                //tablaDeSimbolos.add(new String[]{lexema, nSimbolo + ""});
                nSimbolo++;

            } else {
                reemplazarEnTablaSimbolos(lexema, nlinea);
            }

            //Binario
        } else if (id == '3') {
            token = new String[]{lexema, "280"};
            tablaDeTokensValidos.add(new String[]{"280", lexema, "" + nlinea});

            tablaDeSimbolos.add(new String[]{lexema, "litBinario", nSimbolo + "", "1", "" + nlinea, lexema});
            nSimbolo++;

            //Octal
        } else if (id == '4') {
            token = new String[]{lexema, "290"};
            tablaDeTokensValidos.add(new String[]{"290", lexema, "" + nlinea});

            tablaDeSimbolos.add(new String[]{lexema, "litOctal", nSimbolo + "", "1", "" + nlinea, lexema});
            nSimbolo++;

            //Hexadecimal
        } else if (id == '5') {
            token = new String[]{lexema, "300"};
            tablaDeTokensValidos.add(new String[]{"300", lexema, "" + nlinea});

            tablaDeSimbolos.add(new String[]{lexema, "litHexadec", nSimbolo + "", "1", "" + nlinea, lexema});
            nSimbolo++;

            //Caracter Especial
        } else if (id == '6') {
            token = new String[]{lexema, atributoCharE(lexema.charAt(0))};
            tablaDeTokensValidos.add(new String[]{atributoCharE(lexema.charAt(0)), lexema, "" + nlinea});

        } else if (id == '9') {
            token = new String[]{"$", "999"};

        }

    }

    //Metodo de generacion de errores
    public void generarErrores(int i, int f, int nlinea, String linea, String codigoError) {
        String error = "";
        for (int j = i; j < f; j++) {
            error += linea.charAt(j);
        }
//        System.out.println("Error: " + codigoError + " En " + error);
        token = new String[]{error, nlinea + ""};
    }

    //Metodo para saber si un lexema ya esta en la tabla de simbolos
    public boolean simboloExistente(String lexema) {
        Nodo cursor = tablaDeSimbolos.getInicio();
        for (int j = 0; j < tablaDeSimbolos.size(); j++) {
            String[] arreglo = cursor.datos;
            if (arreglo[0].equals(lexema)) {

                return true;
            }
            cursor = cursor.der;
        }
        return false;
    }

    //Metodo para actualizar tabla de simbolos
    public boolean reemplazarEnTablaSimbolos(String lexema, int nlinea) {
        Nodo cursor = tablaDeSimbolos.getInicio();
        for (int j = 0; j < tablaDeSimbolos.size(); j++) {
            String[] arreglo = cursor.datos;
            if (arreglo[0].equals(lexema)) {
                // lexema - tipo - nSimbolo - repeticiones - lineas  - valor

                int reps = Integer.parseInt(arreglo[3]) + 1;
                String lineas = arreglo[4] + ", " + nlinea;

                String[] temp = new String[]{arreglo[0], arreglo[1], arreglo[2], "" + reps, lineas, arreglo[5]};

                cursor.datos = temp;
            }
            cursor = cursor.der;
        }
        return false;
    }

    //Metodo para actualizar tabla de simbolos
    public boolean reemplazarTipoEnTablaSimbolos(String lexema, String tipo) {
        Nodo cursor = tablaDeSimbolos.getInicio();
        for (int j = 0; j < tablaDeSimbolos.size(); j++) {
            String[] arreglo = cursor.datos;
            if (arreglo[0].equals(lexema)) {
                // lexema - tipo - nSimbolo - repeticiones - lineas  - valor

                String vdefault = "";
                
                switch (tipo) {
                    case "programa":
                        vdefault = "-";
                        break;

                    case "binario":
                        vdefault = "0B";
                        break;

                    case "octal":
                        vdefault = "0O";
                        break;

                    case "hexad":
                        vdefault = "0X";
                        
                }
                
                String[] temp = new String[]{arreglo[0], tipo, arreglo[2], arreglo[3], arreglo[4], vdefault};

                cursor.datos = temp;
            }
            cursor = cursor.der;
        }
        return false;
    }

}
