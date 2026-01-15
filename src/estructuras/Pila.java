package estructuras;

public class Pila<T> {

    NodoD inicio, fin;
    private int tamaño;

    public void push(T x) {
        push(new NodoD(x));
    }

    public void push(NodoD nuevo) {
        if (inicio == null) {//Si la pila no existe
            inicio = fin = nuevo;
            tamaño = 1;
        } else {//Agregar al final de la pila
            fin.next = nuevo;
            nuevo.previous = fin;
            fin = nuevo;
            tamaño++;
        }
    }

    public NodoD pop() {
        NodoD temp = null;
        if (tamaño == 1) {
            temp = inicio;
            inicio = fin = null;
            tamaño = 0;
        }
        if (tamaño > 1) {
            temp = fin;
            fin = fin.previous;
            temp.previous = fin.next = null;
            tamaño--;
        }
        return temp;
    }

    public boolean isEmpty() {
        return (tamaño == 0) ? true : false;
    }

    public T peek() {
        if (!isEmpty()) {
            return fin.valor;
        } else {
            return (T) "Lista Vacia";
        }
    }
    
    @Override
    public String toString() {//recorrer cada elemento de la lista para formar el String
        String cadena = "Pila (" + tamaño + ") = {";
        NodoD cursor = inicio;
        while (cursor != null) {
            cadena += cursor.toString() + ", ";
            cursor = cursor.next;
        }
        return cadena + "\b\b}";
    }

    public String imprimir() {
        String cadena = "Pila de Verificacion de Tipos(" + tamaño + ") = {";
        NodoD cursor = inicio;
        while (cursor != null) {
            cadena += cursor.imprimir() + ", ";
            cursor = cursor.next;
        }
        return cadena + "\b\b}";
    }
////////////////////////////////////////////////////////////////////////////////
    public class NodoD {

        public T valor;
        private NodoD next;
        private NodoD previous;

        public NodoD(T monto) {
            valor = monto;
        }

        @Override
        public String toString() {
            return "" + valor;
        }
        
        public String imprimir() {
            String[] valores = (String[]) valor;
            return "[" + valores[0] + ", " + valores[1] + "]";
        }
    }
}