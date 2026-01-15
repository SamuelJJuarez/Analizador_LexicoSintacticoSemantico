package packages;

import estructuras.*;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class GeneradorEstructuras {

    //Variables
    String rutaArchivo = "src/archivos/Gramatica.txt";
    Lista gramatica = new Lista();
    Lista ladoDerecho = new Lista();
    Lista terminales = new Lista();
    Lista noTerminales = new Lista();
    String simboloInicial;

    //Constructor
    public GeneradorEstructuras() {
        gramatica = estructuraGramatica(rutaArchivo);
        ladoDerecho = estructuraLadoDerecho(gramatica);
        noTerminales = estructuraNoTerminales(gramatica);
        terminales = estructuraTerminales(gramatica);
        simboloInicial = encontrarInicial(noTerminales, gramatica);

        System.out.println("Gramatica: " + gramatica.imprimirArreglo() + "\n");
        System.out.println("Lados Derechos: " + ladoDerecho.imprimirArreglo() + "\n");
        System.out.println("Simbolos No Terminales: " + noTerminales.imprimir() + "\n");
        System.out.println("Simbolos Terminales: " + terminales.imprimir() + "\n");
        System.out.println("Simbolo Inicial:\n" + simboloInicial);

    }

    //Estructura Gramatica
    public Lista estructuraGramatica(String rutaArchivo) {
        Lista gramatica = new Lista();
        try (Scanner scanner = new Scanner(new File(rutaArchivo))) {
            //Leemos todas las lineas
            while (scanner.hasNextLine()) {
                String linea = scanner.nextLine();
                //Separamos la linea en un arreglo
                String[] produccion = separar(linea);
                //Añadimos la linea a la lista
                gramatica.add(produccion);
            }
        } catch (IOException e) {
            System.err.println("Ocurrió un error al leer el archivo: " + e.getMessage());
        }
        return gramatica;
    }

    //Estructura lados derechos
    public Lista estructuraLadoDerecho(Lista gramatica) {
        Lista ladosDerechos = new Lista();
        Nodo cursor = gramatica.getInicio();

        //Pasamos por todas las producciones
        while (cursor != null) {
            String[] ladosDer = new String[cursor.datos.length - 1];

            //Pasamos por todo el arreglo de esa produccion
            for (int j = 1; j < cursor.datos.length; j++) {
                ladosDer[j - 1] = cursor.datos[j];
            }
            ladosDerechos.add(ladosDer);
            cursor = cursor.der;
        }

        return ladosDerechos;
    }

    // Estructura No Terminales
    public Lista estructuraNoTerminales(Lista gramatica) {
        Lista noTerminales = new Lista();
        Nodo cursor = gramatica.getInicio();

        // Leemos todas las líneas de la gramática
        while (cursor != null) {
            String linea = cursor.name;

            //Verificamos que no hayamos agregado ya el no terminal
            if (!noTerminales.repetido(cursor.datos[0])) {
                //Agregamos el no Terminal
                noTerminales.add(cursor.datos[0]);
            }

            cursor = cursor.der;
        }

        return noTerminales;
    }

    // Estructuras Terminales
    public Lista estructuraTerminales(Lista gramatica) {
        Lista terminales = new Lista();
        Nodo cursor = gramatica.getInicio();

        while (cursor != null) {
            // Buscamos en todos los elementos de una producción
            String[] elementos = cursor.datos;

            for (int i = 0; i < elementos.length; i++) {
                String elemento = elementos[i];

                // Ignorar el símbolo de palabra vacía ε
                if (elemento.equals("ε")) {
                    continue;
                }

                // Si no es noTerminal y no está ya en la lista de terminales, lo agregamos
                if (!noTerminales.repetido(elemento) && !terminales.repetido(elemento)) {
                    terminales.add(elemento);
                }
            }

            cursor = cursor.der;
        }

        return terminales;
    }

    //Simbolo inicial
    public String encontrarInicial(Lista noTerminales, Lista gramatica) {
        Nodo cursorNoT = noTerminales.getInicio();

        // Recorremos cada no terminal
        while (cursorNoT != null) {
            String posibleInicial = cursorNoT.name;
            boolean encontradoEnDerecha = false;

            // Recorremos todas las producciones
            Nodo cursorProd = gramatica.getInicio();
            while (cursorProd != null) {
                String[] produccion = cursorProd.datos;

                // Revisamos solo el lado derecho (desde la posición 1)
                for (int i = 1; i < produccion.length; i++) {
                    if (posibleInicial.equals(produccion[i])) {
                        encontradoEnDerecha = true;
                        break;
                    }
                }

                if (encontradoEnDerecha) {
                    break;
                }

                cursorProd = cursorProd.der;
            }

            // Si no lo encontramos en ningún lado derecho, es el símbolo inicial
            if (!encontradoEnDerecha) {
                return posibleInicial;
            }

            cursorNoT = cursorNoT.der;
        }

        return null; // No se encontró símbolo inicial válido
    }

    //Separamos una produccion en sus diferentes componentes
    public String[] separar(String linea) {

        Lista lista = new Lista();

        int i = linea.indexOf('<'); // Buscar el primer no terminal

        while (i < linea.length()) {
            char actual = linea.charAt(i);

            // Saltar espacios
            if (Character.isWhitespace(actual)) {
                i++;
                continue;
            }

            if (actual == '-' && (i + 1 < linea.length()) && linea.charAt(i + 1) == '>') {
                i += 2; // Saltamos los dos caracteres
                continue;
            }

            switch (actual) {
                case '<': {
                    // Leer hasta el '>'
                    String token = "";
                    while (i < linea.length() && linea.charAt(i) != '>') {
                        token += linea.charAt(i);
                        i++;
                    }
                    if (i < linea.length() && linea.charAt(i) == '>') {
                        token += '>';
                        i++;
                    }
                    lista.add(token);
                    break;
                }
                case ';':
                case '=':
                case '+':
                case '-':
                case '*':
                case '/':
                case ',':
                case '(':
                case ')': {
                    // Caracter especial como token individual
                    lista.add(String.valueOf(actual));
                    i++;
                    break;
                }
                default: {
                    // Leer palabra (identificadores, palabras reservadas)
                    String palabra = "";
                    while (i < linea.length()) {
                        char c = linea.charAt(i);
                        if (Character.isWhitespace(c) || c == '<'
                                || c == ';' || c == '=' || c == '+' || c == '-'
                                || c == '*' || c == '/' || c == ',' || c == '(' || c == ')') {
                            break;
                        }
                        palabra += c;
                        i++;
                    }
                    if (!palabra.isEmpty()) {
                        lista.add(palabra);
                    }
                    break;
                }
            }
        }

        String[] produccion = new String[lista.size()];
        Nodo cursor = lista.getInicio();
        for (int j = 0; j < lista.size(); j++) {
            produccion[j] = cursor.name;
            cursor = cursor.der;
        }
        return produccion;
    }

}
