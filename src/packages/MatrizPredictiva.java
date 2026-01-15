package packages;

import estructuras.Lista;
import estructuras.Nodo;

public class MatrizPredictiva {

    //Variables
    GeneradorEstructuras estructuras;
    int[][] matrizPredictiva;
    String[] terminales;
    String[] noTerminales;
    Lista firsts;
    Lista follows;

    //Constructor
    public MatrizPredictiva(GeneradorEstructuras estructura) {
        estructuras = estructura;

        //Inicializar listas
        firsts = new Lista();
        follows = new Lista();

        //Crear strings
        crearStringNoTerminales();
        crearStringTerminales();

        //Calcular First y Follow
        calcularFirsts();
        imprimirFirsts();
        calcularFollows();
        imprimirFollows();

        //Construir matriz predictiva 
        construirMatrizPredictiva();

    }

    public void crearStringTerminales() {
        Nodo cursor = estructuras.terminales.getInicio();
        terminales = new String[estructuras.terminales.size()];
        for (int i = 0; i < estructuras.terminales.size(); i++) {
            terminales[i] = cursor.name;
            cursor = cursor.der;
        }
    }

    public void crearStringNoTerminales() {
        Nodo cursor = estructuras.noTerminales.getInicio();
        noTerminales = new String[estructuras.noTerminales.size()];
        for (int i = 0; i < estructuras.noTerminales.size(); i++) {
            noTerminales[i] = cursor.name;
            cursor = cursor.der;
        }
    }

    //Calcular First optimizado
    public void calcularFirsts() {

        //Caso 1: Símbolo terminal
        //Buscamos en todas las gramaticas(del 1 a n)
        Nodo cursor = estructuras.gramatica.getInicio();
        for (int i = 1; i <= estructuras.gramatica.size(); i++) {
            String noTerminal = cursor.datos[0];

            if (estructuras.terminales.repetido(cursor.datos[1]) && cursor.datos[1] != "ε") {
                agregarElementoFirst(noTerminal, cursor.datos[1], i);
            }
            cursor = cursor.der;
        }

        //Caso 2: Épsilon 
        cursor = estructuras.gramatica.getInicio();
        for (int i = 1; i <= estructuras.gramatica.size(); i++) {
            String noTerminal = cursor.datos[0];

            if (cursor.datos[1].equals("ε")) {
                agregarElementoFirst(noTerminal, "ε", i);
            }
            cursor = cursor.der;
        }

        boolean cambios;
        // Caso 3: Símbolo no terminal: repetir mientras haya cambios
        do {
            cambios = false;
            cursor = estructuras.gramatica.getInicio();

            for (int i = 1; i <= estructuras.gramatica.size(); i++) {
                String A = cursor.datos[0];
                String Y = cursor.datos[1];

                if (estructuras.noTerminales.repetido(Y)) {
                    String[][] firstY = obtenerConjuntoFirst(Y);
                    if (firstY != null) {
                        for (int j = 1; j < firstY.length; j++) {
                            String simboloFirst = firstY[j][0];
                            if (!simboloFirst.equals("ε")) {
                                boolean agregado = agregarElementoFirst(A, simboloFirst, i);
                                if (agregado) {
                                    cambios = true;
                                }
                            }
                        }
                    }
                }

                cursor = cursor.der;
            }
        } while (cambios);

    }

    //Calcular Follows
    public void calcularFollows() {

        // Regla 1: finprograma ($) está en Follow(S)
        String S = estructuras.simboloInicial;
        agregarElementoFollow(S, "finprograma");

        boolean cambios;
        do {
            cambios = false;
            Nodo cursor = estructuras.gramatica.getInicio();

            // Ciclo para pasar por todas las reglas gramaticales: A -> α
            while (cursor != null) {
                String A = cursor.datos[0];       // No terminal izquierdo (Cabeza)
                String[] produccion = cursor.datos; // Array completo [A, hijo1, hijo2...]

                // CORRECCIÓN PRINCIPAL: j debe iniciar en 1.
                // j recorre los símbolos del lado derecho (B)
                for (int j = 1; j < produccion.length; j++) {
                    String B = produccion[j];

                    // Solo calculamos Follow para No Terminales
                    if (!estructuras.noTerminales.repetido(B)) {
                        continue;
                    }

                    // Variable para determinar si debemos agregar Follow(A) a Follow(B)
                    // Se asume true inicialmente por si B es el último elemento.
                    boolean agregarFollowA = true; 

                    // Caso 1: A → αBβ (Miramos lo que sigue después de B)
                    for (int k = j + 1; k < produccion.length; k++) {
                        String siguiente = produccion[k];
                        agregarFollowA = false; // Como hay algo después de B, reseteamos a false

                        // Subcaso 1.1: Lo siguiente es un terminal
                        if (estructuras.terminales.repetido(siguiente)) {
                            if (agregarElementoFollow(B, siguiente)) {
                                cambios = true;
                            }
                            // Encontramos un terminal, B ya no está al final "lógico", rompemos el loop interno
                            break; 
                        }

                        // Subcaso 1.2: Lo siguiente es un No Terminal, traemos su FIRST
                        String[][] firstSet = obtenerConjuntoFirst(siguiente);
                        boolean siguienteEsNullable = false;

                        if (firstSet != null) {
                            for (int m = 1; m < firstSet.length; m++) {
                                String simboloFirst = firstSet[m][0];
                                if (!simboloFirst.equals("ε")) {
                                    if (agregarElementoFollow(B, simboloFirst)) {
                                        cambios = true;
                                    }
                                } else {
                                    siguienteEsNullable = true;
                                }
                            }
                        }

                        // Si el siguiente símbolo NO contiene épsilon, terminamos aquí.
                        // Si SÍ contiene épsilon, el bucle 'k' continúa al siguiente símbolo
                        if (!siguienteEsNullable) {
                            break;
                        } else {
                            // Si llegamos al final del loop k y todos eran nullables, esto se volverá true
                            agregarFollowA = true; 
                        }
                    }

                    // Caso 2: A → αB (B es el último) O A → αBβ (donde First(β) contiene ε)
                    // Si agregarFollowA sigue siendo true después del loop 'k', aplicamos la regla
                    if (agregarFollowA) {
                        String[] followA = obtenerConjuntoFollow(A);
                        if (followA != null) {
                            for (int f = 1; f < followA.length; f++) {
                                if (agregarElementoFollow(B, followA[f])) {
                                    cambios = true;
                                }
                            }
                        }
                    }
                }
                cursor = cursor.der;
            }
        } while (cambios);
    }

    //Agregar elemento a First con número de producción
    private boolean agregarElementoFirst(String nt, String elemento, int produccion) {

        Nodo cursor = firsts.getInicio();

        //Checamos todos los first
        while (cursor != null) {

            String[][] conjunto = cursor.matriz;

            if (conjunto[0][0].equals(nt)) {

                //Verificar si el elemento ya existe
                for (int i = 1; i < conjunto.length; i++) {
                    if (conjunto[i][0].equals(elemento)) {
                        return false;
                    }
                }

                //Crear nuevo array 
                String[][] nuevoConjunto = new String[conjunto.length + 1][2];
                for (int i = 0; i < conjunto.length; i++) {
                    nuevoConjunto[i][0] = conjunto[i][0];
                    nuevoConjunto[i][1] = conjunto[i][1];
                }
                nuevoConjunto[conjunto.length][0] = elemento;
                nuevoConjunto[conjunto.length][1] = String.valueOf(produccion);

                cursor.matriz = nuevoConjunto;
                return true;
            }
            cursor = cursor.der;
        }

        //Si no se encontró, crear nuevo conjunto
        String[][] nuevo = new String[2][2];
        nuevo[0][0] = nt;
        nuevo[0][1] = "";
        nuevo[1][0] = elemento;
        nuevo[1][1] = String.valueOf(produccion);
        firsts.add(nuevo);
        return true;
    }

    //Agregar elemento a Follow
    private boolean agregarElementoFollow(String nt, String elemento) {

        Nodo cursor = follows.getInicio();

        //Checamos todos los follows
        while (cursor != null) {

            String[] conjunto = cursor.datos;

            //Cuando encontremos el first correspondiente
            if (conjunto[0].equals(nt)) {

                //Verificar si el elemento ya existe
                for (int i = 1; i < conjunto.length; i++) {
                    if (conjunto[i].equals(elemento)) {
                        return false;
                    }
                }

                //Crear nuevo array 
                String[] nuevoConjunto = new String[conjunto.length + 1];
                for (int i = 0; i < conjunto.length; i++) {
                    nuevoConjunto[i] = conjunto[i];
                    nuevoConjunto[i] = conjunto[i];
                }
                nuevoConjunto[conjunto.length] = elemento;

                cursor.datos = nuevoConjunto;
                return true;
            }
            cursor = cursor.der;
        }

        //Si no se encontró, crear nuevo conjunto
        String[] nuevo = new String[2];
        nuevo[0] = nt;
        nuevo[1] = elemento;
        follows.add(nuevo);
        return true;

    }

    //Obtener conjunto First
    private String[][] obtenerConjuntoFirst(String nt) {
        Nodo cursor = firsts.getInicio();
        while (cursor != null) {
            String[][] conjunto = (String[][]) cursor.matriz;
            if (conjunto[0][0].equals(nt)) {
                return conjunto;
            }
            cursor = cursor.der;
        }
        return null;
    }

    //Obtener conjunto Follow
    private String[] obtenerConjuntoFollow(String nt) {
        Nodo cursor = follows.getInicio();
        while (cursor != null) {
            String[] conjunto = cursor.datos;
            if (conjunto[0].equals(nt)) {
                return conjunto;
            }
            cursor = cursor.der;
        }
        return null;
    }

    //Construir matriz predictiva usando la información de First
    private void construirMatrizPredictiva() {
        matrizPredictiva = new int[noTerminales.length][terminales.length];

        //Inicializar matriz con 0 (error)
        for (int i = 0; i < noTerminales.length; i++) {
            for (int j = 0; j < terminales.length; j++) {
                matrizPredictiva[i][j] = 0;
            }
        }

        //Llenar la matriz usando la información de First
        Nodo cursor = firsts.getInicio();
        while (cursor != null) {
            String[][] conjunto = cursor.matriz;
            String nt = conjunto[0][0];
            int fila = getFila(nt);

            for (int i = 1; i < conjunto.length; i++) {
                String simbolo = conjunto[i][0];
                int produccion = Integer.parseInt(conjunto[i][1]);

                if (!simbolo.equals("ε")) {
                    int columna = getColumna(simbolo);
                    if (columna != -1) {
                        matrizPredictiva[fila][columna] = produccion;
                    }
                }
            }
            cursor = cursor.der;
        }

        cursor = firsts.getInicio();
        // Manejar casos especiales con épsilon usando Follow
        while (cursor != null) {
            String[][] conjunto = cursor.matriz;
            String nt = conjunto[0][0]; // No terminal actual
            int fila = getFila(nt);

            // Obtener producciones épsilon para este no terminal
            String[][] firstConjunto = obtenerConjuntoFirst(nt);
            boolean tieneEpsilon = false;
            int produccionEpsilon = 0;

            if (firstConjunto != null) {
                for (int i = 1; i < firstConjunto.length; i++) {
                    if (firstConjunto[i][0].equals("ε")) { // epsilon
                        tieneEpsilon = true;
                        produccionEpsilon = Integer.parseInt(firstConjunto[i][1]);
                        break;
                    }
                }
            }

            if (tieneEpsilon) {
                // Obtener el conjunto Follow del no terminal actual
                String[] followConjunto = obtenerConjuntoFollow(nt);
                if (followConjunto != null) {
                    for (int i = 1; i < followConjunto.length; i++) {
                        String simboloFollow = followConjunto[i];
                        int columna = getColumna(simboloFollow);

                        // Solo actualizar si no hay producción asignada
                        if (columna != -1 && matrizPredictiva[fila][columna] == 0) {
                            matrizPredictiva[fila][columna] = produccionEpsilon;
                        }
                    }
                }
            }
            cursor = cursor.der;
        }
    }

    //Método para obtener el índice de un terminal
    public int getColumna(String terminal) {
        for (int i = 0; i < terminales.length; i++) {
            if (terminales[i].equals(terminal)) {
                return i;
            }
        }
        return -1;
    }

    //Método para obtener el índice de un no terminal
    public int getFila(String noTerminal) {
        for (int i = 0; i < noTerminales.length; i++) {
            if (noTerminales[i].equals(noTerminal)) {
                return i;
            }
        }
        return -1;
    }

    public void imprimirMatrizPredictiva() {
        System.out.println("");
        System.out.printf("%-15s", "");  // Espacio para la columna de no terminales

        //Imprimir encabezados de columnas (terminales)
        for (String terminal : terminales) {
            System.out.printf("%-12s", terminal);
        }
        System.out.println();

        //Imprimir filas con los nombres de los no terminales
        for (int i = 0; i < matrizPredictiva.length; i++) {
            System.out.printf("%-15s", noTerminales[i]); // Nombre del no terminal

            for (int j = 0; j < matrizPredictiva[i].length; j++) {
                System.out.printf("%-12d", matrizPredictiva[i][j]); // Valor de la celda
            }
            System.out.println();
        }
    }

    //Métodos para depuración
    public void imprimirFirsts() {
        System.out.println("\nConjuntos FIRST:");
        Nodo cursor = firsts.getInicio();
        while (cursor != null) {
            String[][] entry = (String[][]) cursor.matriz;
            System.out.print("FIRST(" + entry[0][0] + ") = {");
            for (int i = 1; i < entry.length; i++) {
                System.out.print("(" + entry[i][0] + "," + entry[i][1] + ")");
                if (i < entry.length - 1) {
                    System.out.print(", ");
                }
            }
            System.out.println("}");
            cursor = cursor.der;
        }
    }

    public void imprimirFollows() {
        System.out.println("\nConjuntos FOLLOW:");
        Nodo cursor = follows.getInicio();
        while (cursor != null) {
            String[] entry = cursor.datos;
            System.out.print("FOLLOW(" + entry[0] + ") = {");
            for (int i = 1; i < entry.length; i++) {
                System.out.print(entry[i]);
                if (i < entry.length - 1) {
                    System.out.print(", ");
                }
            }
            System.out.println("}");
            cursor = cursor.der;
        }
    }
}
