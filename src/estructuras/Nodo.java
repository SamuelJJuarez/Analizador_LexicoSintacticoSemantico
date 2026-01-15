package estructuras;

public class Nodo <T>{

    public String[][] matriz;
    public String[] datos;
    public String name;
    public Nodo der;
    public T dato;
    
    public Nodo() {

    }

    public Nodo(T x) {
        dato = x;
    }
    
    public Nodo(String[][] nombre) {
        matriz = nombre;
    }

    public Nodo(String[] nombre) {
        datos = nombre;
    }

    public Nodo(String nombre) {
        name = nombre;
    }

    public String imprimir() {
        return name + " ";
    }

    public String imprimirArreglo() {
        String salida = "";
        for (int i = 0; i < datos.length; i++) {
            salida += "[" + datos[i] + "]";
        }
        return salida + "\n";
    }

    public String imprimirMatriz() {
        String salida = "";
        for (int i = 0; i < matriz.length; i++) {
            salida += "[";
            for (int j = 0; j < matriz[0].length; j++) {
                salida += matriz[i][j];
            }
            salida += "]";
        }
        return salida + "\n";
    }

    @Override
    public String toString() {
        String salida = "";
        salida += "\t\t" + datos[0] + "\t\t\t" + datos[1] + "\t\t\t\t" + datos[2] + "\n";
        return salida;
    }
    
    public String getStrings() {
        String salida = "";
        for (int i = 0; i < datos.length; i++) {
            salida += ( "\t\t" + datos[i]);
        }
        salida += "\n";
        return salida;
    }
}
