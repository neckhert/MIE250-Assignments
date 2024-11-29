import java.io.*;
import java.lang.Math;
import java.util.ArrayList;

//Matrix Parent class that represents all square matrices
class Matrix {
    String name; // name of the matrix
    int n; //dim of matrix (num rows or num cols)
    double[] values;//values that populate the matrix
    String error = null;//does the matrix have an associated error

    //constructor with name (for non regular matrices)
    public Matrix(String name, int n) {
        this.name = name;
        this.n = n;
        this.values = new double[n * n];
    }

    //constructor w/o name (for regular matrices)
    public Matrix(int n) {
        this.name = "regular";
        this.n = n;
        this.values = new double[n * n];
    }

    //writes matrix to a file using a buffered writer
    public void writeMatrix(BufferedWriter writer) throws IOException {

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                writer.write(String.format("%.1f ", values[n * i + j]));
            }
            writer.write("\n");
        }
    }
}

//lower child class of matrix that is used for the lower triangle matrix in LU factorization
class Lower extends Matrix {

    //populates the matrixes values with the identity matrix and calls super constructor
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

//upper chiled class of matrix that is used for the upper triangle matrix in LU factorization
class Upper extends Matrix {

    //calls parent constructor
    public Upper(int n) {
        super("Upper", n);
    }
}

//difference matrix class that represents the difference betwenn two matrices
class Difference extends Matrix {
    //calls parent constructor
    public Difference(int n) {
        super("Difference", n);
    }

    //override the writeMatrix function to print to 4 decimal places
    @Override
    public void writeMatrix(BufferedWriter writer) throws IOException {

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                writer.write(String.format("%.4f ", values[n * i + j]));
            }
            writer.write("\n");
        }
    }

    //returns the frobenius norm of the difference matrix (the tolerance)
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

//class used to hold and transfer input information 
class ioInformation {
    String inputFile="input.txt";//input file name
    boolean isInputFile=false;//is there a given input file or do we use default
    String outputFile="output.txt";//output file
    String executionMode="sequential";//execution mode (automatically set to sequential)
}

//class that implements matrix population and performing doolittle LU factorization
class Doolittle {
    
    //reads input matrix and populates a new matrix with values
    Matrix readInputMatrix(String inputFile) throws IOException {

        BufferedReader reader = new BufferedReader(new FileReader(inputFile));//initialize reader to read file
        ArrayList<String> rows = new ArrayList<String>(); //use array list since we don't know the size of the input matrix
        String nextLine = reader.readLine(); //reads the first row of the array
        int n = 0; //counter to count the number of rows

        //while there are still rows to read keep adding them to the arraylist and incrementing the counter
        while (nextLine != null) {
            rows.add(n, nextLine);
            n++;
            nextLine = reader.readLine();
        }
        
        reader.close();

        Matrix A = new Matrix(n);//create a new matrix

        //populates matrix with values from the input file arrayList
        for (int i = 0; i < n; i++) {
            double[] colValues = getVarsfromString(rows.get(i));
            
            //checks if the matrix is square and creates sets error accordingly
            if (colValues.length != n) {
                A.error = "\nError: Matrix must be square.";
                break;
            }
            for (int j = 0; j < n; j++) {
                A.values[n * i + j] = colValues[j];
            }
        }

        return A;//return populated matrix

    }

    //method for performing the doolittle method for LU factorization
    void dolittleAlgorithm(Matrix A, Lower L, Upper U) {
        
        int n = A.n;//set our n value to use in loops

        //iterates through each row and updates the values for the upper matrix first and then the lower matrix
        for (int i = 0; i < n; i++) {
            
            //iterates through columns and updates upper matrix values
            for (int j = 0; j < n; j++) {
                double nextVal = 0;
                for (int k = 0; k < i; k++) {
                    nextVal += L.values[n * i + k] * U.values[n * k + j];
                }
                U.values[n * i + j] = A.values[n * i + j] - nextVal;
            }

            //iterates through columns and updates lower matrix values
            for (int j = i; j < n; j++) {
                double nextVal = 0;
                for (int k = 0; k < i; k++) {
                    nextVal += L.values[j * n + k] * U.values[k * n + i];
                }
                L.values[n * j + i] = (A.values[j * n + i] - nextVal) / U.values[i * n + i];
            }

            //check if matrix is singular and set the proper error if it is
            if (U.values[i * n + i] == 0) {
                U.error = "\nError: Matrix is singular, cannot perform decomposition.";
                break;
            }
        }
    }

    //method that multiplies values of two square matrices together
    double[] matMultSquares(double[] A, double[] B) {
        int n = (int)Math.sqrt(A.length);//set n for loop iteration
        
        double[] C = new double[n * n];//initialize product matrix values

        //iterate through matrix rows and columns to perform matrix multiplication
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                C[n * i + j] = 0;
                for (int k = 0; k < n; k++) {
                    C[n * i + j] += A[i * n + k] * B[k * n + j];
                }
            }
        }

        //return the values of the product matrix
        return C;
    }

    //computes the difference matrix between the original matrix A and its LU factorization upper and lower matrices
    Difference computeDifferenceMatrix(Matrix A, Lower L, Upper U) {
        int n = A.n;//set n for loop iteration

        //initialize a difference matrix
        Difference D = new Difference(n);

        //compute the product L*U
        double[] B = matMultSquares(L.values, U.values);

        //compute the difference for each element in A and LU
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                D.values[i * n + j] = A.values[i * n + j] - B[i * n + j];
            }
        }

        //return the difference matrix
        return D;
    }

    //method that writes outputs to the output file
    void writeOutputs(ioInformation io, Matrix A, Lower L, Upper U, Difference D) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(io.outputFile));//initialize writer to write values
            //check if default input file is being used and output the proper message
            if (!io.isInputFile){
                writer.write("No input file specified. Using default: input.txt\n");
            }

            //write file and execution mode information
            writer.write(String.format("Input file: %s", io.inputFile));
            writer.write(String.format("\nOutput file: %s", io.outputFile));
            writer.write(String.format("\nExecution mode: %s\n", io.executionMode));

            //check for square matrix error and output the error message if error is found o/w continue with outputs
            if (A.error != null) {
                writer.write(A.error);
            } else {
                //write matrix A
                writer.write("\nMatrix A:\n");
                A.writeMatrix(writer);
                
                //check for singular matrix error and output the error message if error is found o/w continue with outputs
                if (U.error != null) {
                    writer.write(U.error);
                } else {

                    //write matrix L
                    writer.write("\nFinal Matrix L:\n");
                    L.writeMatrix(writer);

                    //write matrix U
                    writer.write("\nFinal Matrix U:\n");
                    U.writeMatrix(writer);

                    //write difference matrix and tolerance
                    writer.write("\nDifference Matrix (A - LU):\n");
                    D.writeMatrix(writer);
                    writer.write(String.format("\nTolerance (difference between A and LU): %.4f", D.getTolerance()));
                    writer.write(String.format("\n\nDecomposition complete. Results written to %s", io.outputFile));
                }
            }
            writer.close();
        
        } catch (IOException e) {
            //catch any input output errors
            System.out.println(e.getMessage());
        }
    }

    // method that takes a string and returns an array by seperating each value by spaces
    double[] getVarsfromString(String varsString) {
        String[] separatedVarsString = varsString.split(" ");// creates an array of strings by splitting the current string at every space
        double[] outputs = new double[separatedVarsString.length];// initializing a new array of doubles with the same size as the string array

        for (int i = 0; i < separatedVarsString.length; i++) {
            outputs[i] = Double.parseDouble(separatedVarsString[i]);// convert every string in the string array to a double in the double array
        }

        return outputs;// return the double array
    }
}

//child class of Doolittle that implements parallel execution for the doolittle algorithm method
class parallelDoolittle extends Doolittle{
    
    //overrides dolittleAlgorithm method to implement parallel execution
    @Override
    void dolittleAlgorithm(Matrix A, Lower L, Upper U) {
        int n = A.n;//sets n for iteration

        int numThreads = Runtime.getRuntime().availableProcessors();//set num threads according to available processors
        Thread[] threads = new Thread[numThreads];//create threads
        
        int chunkSize = n / numThreads;//define chunk size for each thread

        //set the start and end row for each thread
        for (int i=0; i < numThreads; i++){
            final int startRow = i* chunkSize;
            final int endRow = (i==numThreads-1) ? n : startRow + chunkSize;

            //divide the execution of LU factorization across the threads and then everything is the same as sequential doolittle
            threads[i] = new Thread(()->{
                for (int row = startRow; row < endRow; row++) {
                    //compute U values for the row
                    for (int j = 0; j < n; j++) {
                        double nextVal = 0;
                        for (int k = 0; k < row; k++) {
                        nextVal += L.values[n * row + k] * U.values[n * k + j];
                    }
                        U.values[n * row + j] = A.values[n * row + j] - nextVal;
                    }

                    //Compute L values for the row
                    for (int j = row; j < n; j++) {
                        double nextVal = 0;
                        for (int k = 0; k < row; k++) {
                        nextVal += L.values[j * n + k] * U.values[k * n + row];
                        }
                        L.values[n * j + row] = (A.values[j * n + row] - nextVal) / U.values[row * n + row];
                    }

                    //check for singular matrix
                    if (U.values[row * n + row] == 0) {
                        U.error = "\nError: Matrix is singular, cannot perform decomposition.";
                        break;
                    } 
                }   
            });
            threads[i].start();//run the threads
        }

        //join the threads back together and catch possible errors
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
            ioInformation io = new ioInformation();//initialize a new object for input output info
            
            //check if the user wants to use  different input file and set file accordingly
            if (args.length != 0){
                io.inputFile = args[0];
                io.isInputFile=true;
            }
            
            BufferedReader reader = new BufferedReader(new FileReader("config.txt")); //initialize reader to read for execution mode
            String line = reader.readLine();
            
            //check if execution mode is parallel and update executionMode accordingly
            if (line.equals("parallel_execution=true")){
                io.executionMode="parallel";
            }
            reader.close();
    
            Doolittle alg; //create a doolittle alg and set based on execution type
            if (io.executionMode.equals("parallel")){
                alg = new parallelDoolittle();
            }else{
                alg = new Doolittle();
            }
            
            //read input matrix and initialize lower and upper matrices
            Matrix A = alg.readInputMatrix(io.inputFile);
            Lower L = new Lower(A.n);
            Upper U = new Upper(A.n);

            //compute LU factorization, difference and write outputs
            alg.dolittleAlgorithm(A, L, U);
            Difference D = alg.computeDifferenceMatrix(A, L, U);
            alg.writeOutputs(io, A, L, U, D);
        } catch (IOException e) {
            //catch any possible input output errors
            System.out.println(e.getMessage());
        }
    }
}