package packages;

public class main {

    //Main
    public static void main(String[] args) {
        /*
        1.- Generar las estructuras
        2.- Generar la Matriz Predictiva
        3.- Correr analizador lexico
        4.- Correr analizador sintactico
        5. - Optimización de código
         */
        GeneradorEstructuras estructuras = new GeneradorEstructuras();
        MatrizPredictiva matriz = new MatrizPredictiva(estructuras);
//        matriz.imprimirFirsts();
//        matriz.imprimirFollows();
        matriz.imprimirMatrizPredictiva();
        AnalizadorLexico lexico = new AnalizadorLexico();
        AnalizadorSintactico sintactico = new AnalizadorSintactico(estructuras, matriz, lexico);

        sintactico.LLDriver();

        // --- ETAPA DE OPTIMIZACIÓN ---
        // Instanciamos el optimizador
        OptimizadorCodigo optimizador = new OptimizadorCodigo();
        
        // Le pasamos la lista de código intermedio generada por el sintáctico
        // NOTA: Asegúrate de que 'codigoIntermedio' en AnalizadorSintactico sea 'public' 
        // o crea un getter para acceder a ella: sintactico.getCodigoIntermedio()
        optimizador.optimizar(sintactico.codigoIntermedio);

    }

}
