import java.util.Scanner;
import java.io.*;
import java.lang.Math;
import java.math.*;

//Parent class for all of my objective functions
abstract class ObjectiveFunction{

    private String name; //name of the type of objective function

    //constructor to initialize the name of the objective function based on its type
    public ObjectiveFunction(String name){
        this.name=name;
    }
    
    abstract double compute(double[] variables); 
    abstract double[] computeGradient(double[] variables);
    
    //function that returns the bounds of the functions (all functions in this lab use -5,5)
    double[] getBounds(){
        double[] outputs={-5.0,5.0};
        return outputs;
    }

    //getter that returns the name/type of the function
    String getName(){
        return this.name;
    }

}

class QuadraticFunction extends ObjectiveFunction{

    public QuadraticFunction(){
        super("Quadratic");
    }

    @Override
    double compute(double[] variables){
        double output=0;
        for (double variable : variables){
            output+=Math.pow(variable,2);
        }
        return output;
    }

    @Override
    double[] computeGradient(double[] variables){
        double[] outputs = new double[variables.length];
        for (int i=0; i<variables.length; i++){
            outputs[i]=2*variables[i];
        }
        return outputs;
    }
}

class RosenbrockFunction extends ObjectiveFunction{

    public RosenbrockFunction(){
        super("Rosenbrock");
    }

    @Override
    double compute(double[] variables){
        double output=0;

        for(int i=0; i<variables.length-1; i++){
            double x = variables[i];
            x*=x;
            x=variables[i+1]-x;
            x*=x;
            x*=100;
            x+=(1-variables[i])*(1-variables[i]);
            output+=x;
        }
        
        return output;
    }
    @Override
    double[] computeGradient(double[] variables){
        double[] outputs = new double[variables.length];
        for (int i=0; i<variables.length; i++){
            if (i==variables.length-1){
                outputs[i]=(variables[i]-(variables[i-1]*variables[i-1]));
                outputs[i]*=200;
            }else{
                outputs[i]=variables[i+1]-(variables[i]*variables[i]);
                outputs[i]*=-400*variables[i];
                outputs[i]-=2*(1-variables[i]);
            }
        }
        return outputs;
    }

}

class Rosenbrock_Bonus extends ObjectiveFunction{
    public Rosenbrock_Bonus(){
        super("Rosenbrock_Bonus");
    }

    @Override
    double compute(double [] variables){
        double output=0;

        for(int i=0; i<variables.length-1; i++){
            double x = variables[i];
            x*=x;
            x=variables[i+1]-x;
            x*=x;
            x*=100;
            x+=(1-variables[i])*(1-variables[i]);
            output+=x;
        }
        
        return output;
    }

    @Override
    double[] computeGradient(double[] variables){
        double[] outputs = new double[variables.length];
        for (int i=0; i<variables.length; i++){
            
            if (i==0){
                outputs[i]=-400*(variables[i+1]-Math.pow(variables[i],2))+2*(variables[i]-1);
            }else if(i==variables.length-1 && i!=2){
                outputs[i]=200*(variables[i]-Math.pow(variables[i-1],2));
            }else{
                outputs[i]=-400*(variables[i+1]-Math.pow(variables[i],2))+2*(variables[i]-1)+200*(variables[i]-Math.pow(variables[i-1],2));
            }
        }
        return outputs;
    }
}

class OptimizationInputs{
    
    ObjectiveFunction function;
    int dimensionality;
    int iterations;
    double tolerance;
    double stepSize;
    boolean isValid;
    double[] variables; 

}

class Output{
    static public void printOutputs(int iteration, double OFValue, double[] x, String prompt, double gradientnorm, String fileName){
        System.out.println();
        if (iteration==1){
            System.out.print("\n\nOptimization Process:");
            System.out.print("\nIteration 1:");
        }else{
            System.out.printf("\n\nIteration %d:", iteration);
        }
        System.out.printf("\nObjective Function Value: %.5f", OFValue);
        System.out.print("\nx-values: ");
        for (double value : x){
            System.out.printf("%.5f ", value);
        }
        if (iteration != 1){
        System.out.printf("\nCurrent Tolerance: %.5f", gradientnorm);
        }
        if (prompt != null){
            System.out.print("\n"+prompt);
        }
    }

    static public void printOutputs(int iteration, double OFValue, double[] x, String prompt, double gradientnorm, String fileName, PrintWriter writer){
        if (iteration==1){
            writer.write("\n\nOptimization Process:");
            writer.write("\nIteration 1:");
        }else{
            writer.format("\n\nIteration %d:", iteration);
        }
        writer.format("\nObjective Function Value: %.5f", OFValue);
        writer.format("\nx-values: ");
        for (double value : x){
            writer.format("%.5f ", value);
        }
        if (iteration != 1){
            writer.format("\nCurrent Tolerance: %.5f", gradientnorm);
        }
        if (prompt != null){
            writer.write("\n"+prompt);
        }
    }
}

class SteepestDescentOptimizer{

    static double floorTo5Decimals(double value){
        BigDecimal bd = new BigDecimal(value).setScale(5, RoundingMode.FLOOR);
        return bd.doubleValue();
        //double scale = Math.pow(10,5);
        //return Math.round(value * scale)/scale;
    }
    static double[] optimizeSteepestDescent(ObjectiveFunction objectiveFunction, double[] variables, int numIterations, double tolerance, double stepSize, int dimensionality, String fileName){
        
        double[] gradients=objectiveFunction.computeGradient(variables);
        double gradientnorm=0;
        PrintWriter writer;
        try{
            if(fileName != null){
                writer = new PrintWriter(fileName);
                writer.format("Objective Function: %s", objectiveFunction.getName());
                writer.format("\nDimensionality: %d", variables.length);
                writer.write("\nInitial Point: ");
                for (double variable : variables){
                    writer.format("%.1f ", variable);
                }
                writer.format("\nIterations: %d", numIterations);
                writer.format("\nTolerance: %.5f", tolerance);
                writer.format("\nStep Size: %.5f", stepSize);
            }else{
                writer = null;
                System.out.printf("Objective Function: %s", objectiveFunction.getName());
                System.out.printf("\nDimensionality: %d", variables.length);
                System.out.print("\nInitial Point: ");
                for (double variable : variables){
                    System.out.printf("%.1f ", variable);
                }
                System.out.printf("\nIterations: %d", numIterations);
                System.out.printf("\nTolerance: %.5f", tolerance);
                System.out.printf("\nStep Size: %.5f", stepSize);
            }
            for (int i=0; i<numIterations; i++){ 
                int iteration = i+1;
                String prompt = null; 
                for (int j =0; j<variables.length; j++){
                    variables[j] = floorTo5Decimals(variables[j]);
                    gradients[j] = floorTo5Decimals(gradients[j]);
                }
                gradientnorm = floorTo5Decimals(gradientnorm);

                if(iteration != 1 && gradientnorm<tolerance){
                    prompt = "\nConvergence reached after "+ iteration + " iterations.\n";
                    i=numIterations-1;
                }else if(i==numIterations-1){
                    prompt = "\nMaximum iterations reached without satisfying the tolerance.\n";
                }
                if(fileName != null){
                    Output.printOutputs(iteration, floorTo5Decimals(objectiveFunction.compute(variables)), variables, prompt, floorTo5Decimals(gradientnorm), fileName, writer);
                    if (i==numIterations-1){
                        writer.write("\nOptimization process completed.\n");
                        writer.close();
                    }
                }else{
                    Output.printOutputs(iteration, floorTo5Decimals(objectiveFunction.compute(variables)), variables, prompt, floorTo5Decimals(gradientnorm), fileName);
                    if (i==numIterations-1){
                        System.out.print("\nOptimization process completed.\n");
                    }        
                }

                for (int j=0; j<variables.length; j++){
                    variables[j]=variables[j]-(stepSize*gradients[j]);
                }
                gradientnorm=computeGradientNorm(gradients);
                gradients=objectiveFunction.computeGradient(variables);
            }
            
        }catch (FileNotFoundException e){}
        
        return variables;
    }   

    static double computeGradientNorm(double[] gradients){
        
        double output=0;
        
        for(double gradient : gradients){
            output+=gradient*gradient;
        }

        return Math.sqrt(output);
    }

    static int getValidatedInput(Scanner scanner, String prompt){
        boolean isValid=true;
        int input=0;
        String outarg = "Please enter a valid input (0 or 1).";
        do{
            System.out.println(prompt);
            isValid=true;
            try{
                input=Integer.parseInt(scanner.nextLine());

                if (input != 1 && input != 0){
                    throw new IllegalArgumentException(outarg);
                }
            }catch(NumberFormatException e){
                System.out.println(outarg);
                isValid=false;

            }catch(IllegalArgumentException e){
                System.out.println(e.getMessage());
                isValid=false;
            }
        }while(!isValid);
        return input;
    }

    static double checkBounds(double[] variables, double[] bounds){
        double check = 0;

        for (int i=0; i<variables.length; i++){
            if (variables[i]<bounds[0]||variables[i]>bounds[1]){
                check=variables[i];
            }
        }

        return check;
    }

    static OptimizationInputs getManualInput(Scanner scanner){
        OptimizationInputs inputs = new OptimizationInputs();
        String functionName;
        inputs.isValid=true;
        
        try{
            System.out.println("Enter the choice of objective function (quadratic or rosenbrock):");
            functionName=scanner.nextLine();
            System.out.println("Enter the dimensionality of the problem:");
            inputs.dimensionality=Integer.parseInt(scanner.nextLine());
            System.out.println("Enter the number of iterations:");
            inputs.iterations=Integer.parseInt(scanner.nextLine());
            System.out.println("Enter the tolerance:");
            inputs.tolerance=Double.parseDouble(scanner.nextLine());
            System.out.println("Enter the step size:");
            inputs.stepSize=Double.parseDouble(scanner.nextLine());
            
            if (functionName.equals("rosenbrock")){
                inputs.function= new RosenbrockFunction();
            }else if(functionName.equals("quadratic")){
                inputs.function= new QuadraticFunction();
            }else if(functionName.equals("rosenbrock_bonus")){
                inputs.function = new Rosenbrock_Bonus();
            }else{
                throw new IllegalArgumentException("Unknown objective function.");
            }
        }catch(IllegalArgumentException e){
            System.out.printf("Error: " + e.getMessage());
            inputs.isValid=false;
        }

        if (inputs.isValid == true){
            try{
                System.out.printf("Enter the initial point as %d space-separated values:\n", inputs.dimensionality);
                inputs.variables=getVarsfromString(scanner.nextLine());
                if(inputs.variables.length != inputs.dimensionality){
                    throw new IllegalArgumentException("Initial point dimensionality mismatch.");
                }

                if(checkBounds(inputs.variables, inputs.function.getBounds())!=0){
                    throw new IllegalArgumentException("Bounds Exception");
                }
            }catch(IllegalArgumentException e){
                if (e.getMessage()=="Bounds Exception"){
                double[] bounds= inputs.function.getBounds();
                System.out.printf("Error: Initial point %.1f is outside the bounds [%.1f, %.1f].", checkBounds(inputs.variables, bounds), bounds[0], bounds[1]);
                inputs.isValid=false;
                }else{
                    System.out.printf("Error: " + e.getMessage());
                    inputs.isValid=false;
                }
            }
        }
        return inputs;
    }

    static OptimizationInputs getFileInput(String configFile){
        OptimizationInputs inputs = new OptimizationInputs();
        String functionName;
        inputs.isValid=true;

        try{
            BufferedReader reader = new BufferedReader(new FileReader(configFile));
            functionName=reader.readLine();
            inputs.dimensionality=Integer.parseInt(reader.readLine());
            inputs.iterations=Integer.parseInt(reader.readLine());
            inputs.tolerance=Double.parseDouble(reader.readLine());
            inputs.stepSize=Double.parseDouble(reader.readLine());
            if (functionName.equals("rosenbrock")){
                inputs.function= new RosenbrockFunction();
            }else if(functionName.equals("quadratic")){
                inputs.function= new QuadraticFunction();
            }else{
                reader.close();
                throw new IllegalArgumentException("Unknown objective function.");
            }
            inputs.variables=getVarsfromString(reader.readLine());
                if(inputs.variables.length != inputs.dimensionality){
                    reader.close();
                    throw new IllegalArgumentException("Initial point dimensionality mismatch.");
                }

                if(checkBounds(inputs.variables, inputs.function.getBounds())!=0){
                    reader.close();
                    throw new IllegalArgumentException("Bounds Exception");
                }
                reader.close();
        }catch(FileNotFoundException e){
            System.out.println("Error reading the file.");
            inputs.isValid = false;
        }catch(IOException e){
            System.out.println("Error reading the file.");
            inputs.isValid = false;
        }catch(IllegalArgumentException e){
            if (e.getMessage()=="Bounds Exception"){
                    double[] bounds= inputs.function.getBounds();
                    System.out.printf("Error: Initial point %.1f is outside the bounds [%.1f, %.1f].", checkBounds(inputs.variables, bounds), bounds[0], bounds[1]);
                    inputs.isValid=false;
                }else{
                    System.out.printf("Error: " + e.getMessage());
                    inputs.isValid=false;
                }
        }
        return inputs;
    }

    static double[] getVarsfromString(String varsString){
        String[] separatedVarsString = varsString.split(" ");
        double [] outputs= new double[separatedVarsString.length];

        for (int i =0; i<separatedVarsString.length; i++){
            outputs[i]=Double.parseDouble(separatedVarsString[i]);         
        }

        return outputs;
    }

}

public class asst2_eckhertn{
    
    public static void main(String[] args){

        Scanner scanner = new Scanner(System.in);

        int start=SteepestDescentOptimizer.getValidatedInput(scanner, "Press 0 to exit or 1 to enter the program:"); //Prompt the user if they want to enter the program

        if (start == 1){
            OptimizationInputs inputs;
            String outputFile;
            int inputform=SteepestDescentOptimizer.getValidatedInput(scanner, "Press 0 for .txt input or 1 for manual input:");
            int outputform=SteepestDescentOptimizer.getValidatedInput(scanner, "Press 0 for .txt output or 1 for console output:");
            if (inputform==1){
                inputs = SteepestDescentOptimizer.getManualInput(scanner);
            }else{
                System.out.println("Please provide the path to the config file: ");
                String configfile =scanner .nextLine();
                inputs = SteepestDescentOptimizer.getFileInput(configfile);
            }
            if (inputs.isValid==true){
                if(outputform==1){
                    outputFile = null;
                }else{
                    System.out.println("Please provide the path for the ouput file: ");
                    outputFile=scanner.nextLine();
                }
        
                SteepestDescentOptimizer.optimizeSteepestDescent(inputs.function, inputs.variables, inputs.iterations, inputs.tolerance, inputs.stepSize, inputs.dimensionality, outputFile);
            }
        }else{
            System.out.println("Exiting Program...");
        }
    }
}