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
    String outputFile;
    String executionMode;
}

class doDoolittle {
    static Matrix readInputMatrix(String inputFile) throws IOException {

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

    static void dolittleAlgorithm(Matrix A, Lower L, Upper U) {
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

    static double[] matMultSquares(double[] A, double[] B) {
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

    static Difference computeDifferenceMatrix(Matrix A, Lower L, Upper U) {
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

    static void writeOutputs(ioInformation io, Matrix A, Lower L, Upper U, Difference D) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(io.outputFile));
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
    static double[] getVarsfromString(String varsString) {
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

public class asst3_eckhertn {
    public static void main(String[] args) {
        try {
            ioInformation io = new ioInformation();
            io.inputFile = "input.txt";
            io.outputFile = "output.txt";
            io.executionMode = "standard";
            Matrix A = doDoolittle.readInputMatrix("input.txt");
            Lower L = new Lower(A.n);
            Upper U = new Upper(A.n);
            doDoolittle.dolittleAlgorithm(A, L, U);
            Difference D = doDoolittle.computeDifferenceMatrix(A, L, U);
            doDoolittle.writeOutputs(io, A, L, U, D);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}