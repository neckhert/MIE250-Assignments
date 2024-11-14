import java.io.*;
import java.lang.Math;
import java.util.ArrayList;

class Matrix {
    String name;
    int n;
    double[] values;
    String error = null;

    public Matrix(String name, int n) {
        this.name = name;
        this.n = n;
        this.values = new double[n * n];
    }

    public Matrix(int n) {
        this.name = "regular";
        this.n = n;
        this.values = new double[n * n];
    }

    public void writeMatrix(BufferedWriter writer) throws IOException {

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                writer.write(String.format("%.1f ", values[n * i + j]));
            }
            writer.write("\n");
        }
    }
}

class Lower extends Matrix {

    public Lower(int n) {
        super("Lower", n);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j) {
                    values[n * i + j] = 1;
                }
            }
        }
    }
}

class Upper extends Matrix {

    public Upper(int n) {
        super("Upper", n);
    }

    String error = null;
}

class Difference extends Matrix {
    public Difference(int n) {
        super("Difference", n);
    }

    @Override
    public void writeMatrix(BufferedWriter writer) throws IOException {

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                writer.write(String.format("%.4f ", values[n * i + j]));
            }
            writer.write("\n");
        }
    }

    public double getTolerance() {
        double output = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                output += Math.pow(values[n * i + j], 2);
            }
        }
        return Math.sqrt(output);
    }
}

class ioInformation {
    String inputFile;
    boolean isInputFile=false;
    String outputFile;
    String executionMode="standard";
}

class Doolittle {
    Matrix readInputMatrix(String inputFile) throws IOException {

        BufferedReader reader = new BufferedReader(new FileReader(inputFile));
        ArrayList<String> rows = new ArrayList<String>();
        String nextLine = reader.readLine();
        int n = 0;
        while (nextLine != null) {
            rows.add(n, nextLine);
            n++;
            nextLine = reader.readLine();
        }
        reader.close();

        Matrix A = new Matrix(n);
        for (int i = 0; i < n; i++) {
            double[] colValues = getVarsfromString(rows.get(i));
            if (colValues.length != n) {
                A.error = "\nError: Matrix must be square.";
                break;
            }
            for (int j = 0; j < n; j++) {
                A.values[n * i + j] = colValues[j];
            }
        }

        return A;

    }

    void dolittleAlgorithm(Matrix A, Lower L, Upper U) {
        int n = A.n;

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                double nextVal = 0;
                for (int k = 0; k < i; k++) {
                    nextVal += L.values[n * i + k] * U.values[n * k + j];
                }
                U.values[n * i + j] = A.values[n * i + j] - nextVal;
            }

            for (int j = i; j < n; j++) {
                double nextVal = 0;
                for (int k = 0; k < i; k++) {
                    nextVal += L.values[j * n + k] * U.values[k * n + i];
                }
                L.values[n * j + i] = (A.values[j * n + i] - nextVal) / U.values[i * n + i];
            }

            if (U.values[i * n + i] == 0) {
                U.error = "\nError: Matrix is singular, cannot perform decomposition.";
                break;
            }
        }
    }

    double[] matMultSquares(double[] A, double[] B) {
        int n = (int) Math.sqrt(A.length);
        double[] C = new double[n * n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                C[n * i + j] = 0;
                for (int k = 0; k < n; k++) {
                    C[n * i + j] += A[i * n + k] * B[k * n + j];
                }
            }
        }
        return C;
    }

    Difference computeDifferenceMatrix(Matrix A, Lower L, Upper U) {
        int n = A.n;
        Difference D = new Difference(n);
        double[] B = matMultSquares(L.values, U.values);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                D.values[i * n + j] = A.values[i * n + j] - B[i * n + j];
            }
        }

        return D;
    }

    void writeOutputs(ioInformation io, Matrix A, Lower L, Upper U, Difference D) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(io.outputFile));
            if (!io.isInputFile){
                writer.write("No input file specified. Using default: input.txt\n");
            }
            writer.write(String.format("Input file: %s", io.inputFile));
            writer.write(String.format("\nOutput file: %s", io.outputFile));
            writer.write(String.format("\nExecution mode: %s\n", io.executionMode));
            if (A.error != null) {
                writer.write(A.error);
            } else {
                writer.write("\nMatrix A:\n");
                A.writeMatrix(writer);
                if (U.error != null) {
                    writer.write(U.error);
                } else {
                    writer.write("\nFinal Matrix L:\n");
                    L.writeMatrix(writer);
                    writer.write("\nFinal Matrix U:\n");
                    U.writeMatrix(writer);
                    writer.write("\nDifference Matrix (A - LU):\n");
                    D.writeMatrix(writer);
                    writer.write(String.format("\nTolerance (difference between A and LU): %.4f", D.getTolerance()));
                    writer.write(String.format("\n\nDecomposition complete. Results written to %s", io.outputFile));
                }
            }
            writer.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    // method that takes a string and returns an array by seperating each value by
    // spaces
    double[] getVarsfromString(String varsString) {
        String[] separatedVarsString = varsString.split(" ");// creates an array of strings by splitting the current
                                                             // string at every space
        double[] outputs = new double[separatedVarsString.length];// initializing a new array of doubles with the same
                                                                  // size as the string array

        for (int i = 0; i < separatedVarsString.length; i++) {
            outputs[i] = Double.parseDouble(separatedVarsString[i]);// convert every string in the string array to a
                                                                    // double in the double array
        }

        return outputs;// return the double array
    }
}

class parallelDoolittle extends Doolittle{
    
    @Override
    void dolittleAlgorithm(Matrix A, Lower L, Upper U) {
        int n = A.n;
        int numThreads = Runtime.getRuntime().availableProcessors();
        Thread[] threads = new Thread[numThreads];
        
        int chunkSize = n / numThreads;

        for (int i=0; i < numThreads; i++){
            final int startRow = i* chunkSize;
            final int endRow = (i==numThreads-1) ? n : startRow + chunkSize;

            threads[i] = new Thread(()->{
                for (int row = startRow; row < endRow; row++) {
                    for (int j = 0; j < n; j++) {
                        double nextVal = 0;
                        for (int k = 0; k < row; k++) {
                        nextVal += L.values[n * row + k] * U.values[n * k + j];
                    }
                    U.values[n * row + j] = A.values[n * row + j] - nextVal;
                    }

                    for (int j = row; j < n; j++) {
                        double nextVal = 0;
                        for (int k = 0; k < row; k++) {
                        nextVal += L.values[j * n + k] * U.values[k * n + row];
                        }
                        L.values[n * j + row] = (A.values[j * n + row] - nextVal) / U.values[row * n + row];
                    }

                    if (U.values[row * n + row] == 0) {
                        U.error = "\nError: Matrix is singular, cannot perform decomposition.";
                        break;
                    }
                }   
            });
            threads[i].start();
        }

        for (Thread thread : threads){
            try{
                thread.join();
            }catch(InterruptedException e){
                e.printStackTrace();
            }
        }
    }
}
    

public class asst3_eckhertn {
    public static void main(String[] args) {
        try {
            ioInformation io = new ioInformation();
            io.inputFile = "input.txt";
            if (args.length != 0){
                io.inputFile = args[0];
                io.isInputFile=true;
            }
            io.outputFile = "output.txt";
            BufferedReader reader = new BufferedReader(new FileReader("config.txt"));   
            String line = reader.readLine();
            if (line.equals("parallel_execution=true")){
                io.executionMode="parallel";
            }
            reader.close();
            Doolittle alg;
            if (io.executionMode.equals("parallel")){
                alg = new parallelDoolittle();
            }else{
                alg = new Doolittle();
            }
            Matrix A = alg.readInputMatrix(io.inputFile);
            Lower L = new Lower(A.n);
            Upper U = new Upper(A.n);
            alg.dolittleAlgorithm(A, L, U);
            Difference D = alg.computeDifferenceMatrix(A, L, U);
            alg.writeOutputs(io, A, L, U, D);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}