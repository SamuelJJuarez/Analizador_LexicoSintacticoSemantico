package packages;

public class main {

    //Main
    public static void main(String[] args) {
        /*
        1.- Generar las estructuras
        2.- Generar la Matriz Predictiva
        3.- Correr analizador lexico
        4.- Correr analizador sintactico
         */
        GeneradorEstructuras estructuras = new GeneradorEstructuras();
        MatrizPredictiva matriz = new MatrizPredictiva(estructuras);
//        matriz.imprimirFirsts();
//        matriz.imprimirFollows();
        matriz.imprimirMatrizPredictiva();
        AnalizadorLexico lexico = new AnalizadorLexico();
        AnalizadorSintactico sintactico = new AnalizadorSintactico(estructuras, matriz, lexico);

        sintactico.LLDriver();

    }

}
