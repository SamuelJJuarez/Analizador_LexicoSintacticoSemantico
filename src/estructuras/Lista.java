package estructuras;

public class Lista <T> {

    private Nodo inicio, fin;
    private int tamaño;

    public void add(T nombre){
        add(new Nodo(nombre));
    }
    
    public void add(String nombre) {
        add(new Nodo(nombre));
    }

    public void add(String[] arreglo) {
        add(new Nodo(arreglo));
    }

    public void add(String[][] matriz) {
        add(new Nodo(matriz));
    }
    
    public void add(Nodo nodo) {
        if (nodo != null) {
            if (inicio == null) {
                fin = inicio = nodo;
            } else {
                fin.der = nodo;
                fin = fin.der;
            }
            tamaño++;
        }
    }

    public Nodo remove() {
        Nodo temp = null;
        if (inicio != null) {
            //copiar direccion de inicio
            temp = inicio;
            //recorrer inicio a la derecha
            inicio = inicio.der;
            //eliminar la conexion der de temp
            temp.der = null;
            //disminuir el tamano de la lista
            tamaño--;
            //retornar el nodo temp
        }
        return temp;
    }

    public boolean isEmpty() {
        return (tamaño == 0);
    }

    public int size() {
        return tamaño;
    }

    public Nodo getInicio() {
        return inicio;
    }

    //Buscamos si algo es parte de la lista
    public boolean repetido(String buscar) {
        Nodo cursor = inicio;
        if (cursor == null) {
            return false;
        }
        while (cursor != null) {            
            for (int i = 0; i < tamaño; i++) {
                if (buscar.equals(cursor.name)) {
                    return true;
                }
            }
            cursor = cursor.der;
        }
        return false;
    }
            
    //Buscamos si algo es parte de la lista
    public int buscar(String buscar) {
        int numcursor = 1;
        Nodo cursor = inicio;
        if (cursor == null) {
            return 0;
        }
        while (cursor != null) {            
            for (int i = 0; i < tamaño; i++) {
                if (buscar.equals(cursor.name)) {
                    return numcursor;
                }
            }
            cursor = cursor.der;
            numcursor++;
        }
        return 0;
    }
    
    public String imprimir() {//recorrer cada elemento de la lista para formar el String
        String cadena = "Lista (" + tamaño + ") {\n";
        Nodo cursor = inicio;
        while (cursor != null) {
            cadena += cursor.imprimir() + "\n";
            cursor = cursor.der;
        }
        return cadena + "}";
    }

    public String imprimirArreglo() {//recorrer cada elemento de la lista para formar el String
        String cadena = "Lista (" + tamaño + ") {\n";
        Nodo cursor = inicio;
        while (cursor != null) {
            cadena += cursor.imprimirArreglo() + "\n";
            cursor = cursor.der;
        }
        return cadena + "}";
    }

    public String imprimirPalabrasReservadas() {
        String cadena = "Tabla de Palabras Reservadas:\n";
        Nodo cursor = inicio;
        while (cursor != null) {
            cadena += cursor.toString();
            cursor = cursor.der;
        }
        return cadena;
    }

    public String imprimirTablaDeSimbolos() {
        String cadena = "Tabla de Simbolos:\n";
        Nodo cursor = inicio;
        while (cursor != null) {
            cadena += cursor.getStrings();
            cursor = cursor.der;
        }
        return cadena;
    }

    public String imprimirTokens() {
        String cadena = "TABLA DE TOKENS:\n\nClasificacion\t\t\tAtributo\t\t\tLexema\t\t\tNum. Linea\n\n";
        Nodo cursor = inicio;
        while (cursor != null) {
            String clasificacion = "";

            int attr = Integer.parseInt(cursor.datos[0]);

            switch (attr) {
                case 256:
                    clasificacion = "Palabra Reservada";
                    break;
                case 257:
                    clasificacion = "Palabra Reservada";
                    break;
                case 258:
                    clasificacion = "Palabra Reservada";
                    break;
                case 259:
                    clasificacion = "Palabra Reservada";
                    break;
                case 260:
                    clasificacion = "Palabra Reservada";
                    break;
                case 261:
                    clasificacion = "Palabra Reservada";
                    break;
                case 262:
                    clasificacion = "Palabra Reservada";
                    break;
                case 280:
                    clasificacion = "Binario\t\t";
                    break;
                case 290:
                    clasificacion = "Octal\t\t";
                    break;
                case 300:
                    clasificacion = "Hexadecimal\t";
                    break;
                case 310:
                    clasificacion = "Identificador\t";
                    break;
                default:
                    if (attr >= 1 && attr <= 255) { // caracteres ASCII
                        
                        char simbolo = (char) attr; // Convertimos el código a caracter

                        switch (simbolo) {
                            case '+':
                                clasificacion = "Suma\t\t";
                                break;
                            case '-':
                                clasificacion = "Resta\t\t";
                                break;
                            case '*':
                                clasificacion = "Multiplicacion\t";
                                break;
                            case '/':
                                clasificacion = "Division\t";
                                break;
                            case '=':
                                clasificacion = "Asignacion\t";
                                break;
                            default:
                                clasificacion = "Caracter Especial " + simbolo;
                                break;
                        }
                    } else {
                        clasificacion = "Desconocido";
                    }
            }

            cadena += clasificacion + " " + cursor.toString();
            cursor = cursor.der;
        }
        return cadena;
    }
    
    public String imprimirTokens2() {
        String cadena = "Tabla de Tokens:\n";
        Nodo cursor = inicio;
        while (cursor != null) {
            String clasificacion = "";

            int attr = Integer.parseInt(cursor.datos[1]);

            switch (attr) {
                case 256:
                    clasificacion = "Palabra Reservada";
                    break;
                case 257:
                    clasificacion = "Palabra Reservada";
                    break;
                case 258:
                    clasificacion = "Palabra Reservada";
                    break;
                case 259:
                    clasificacion = "Palabra Reservada";
                    break;
                case 260:
                    clasificacion = "Palabra Reservada";
                    break;
                case 261:
                    clasificacion = "Palabra Reservada";
                    break;
                case 262:
                    clasificacion = "Palabra Reservada";
                    break;
                case 280:
                    clasificacion = "Binario";
                    break;
                case 290:
                    clasificacion = "Octal";
                    break;
                case 300:
                    clasificacion = "Hexadecimal";
                    break;
                case 310:
                    clasificacion = "Identificador";
                    break;
                default:
                    if (attr >= 1 && attr <= 255) { // caracteres ASCII 
                        clasificacion = "Caracter Especial" + (char) attr;
                    } else {
                        clasificacion = "Desconocido";
                    }
            }

            cadena += clasificacion + " " + cursor.toString();
            cursor = cursor.der;
        }
        return cadena;
    }

    public String imprimirErrores() {
        String cadena = "Tabla de Errores:\n";
        Nodo cursor = inicio;
        while (cursor != null) {
            cadena += cursor.toString();
            cursor = cursor.der;
        }
        return cadena;
    }
}
