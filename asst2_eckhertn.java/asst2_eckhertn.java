import java.util.Scanner;
import java.lang.Math;

abstract class ObjectiveFunction{
    
    String name;

    public ObjectiveFunction(String name){
        this.name=name;
    }
    
    abstract double compute(double[] variables);
    abstract double[] computeGradient(double[] variables);
        
    double[] getBounds(){
        double[] outputs={-5.0,5.0};
        return outputs;
    }

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
        for (int i=0; i<variables.length; i++){
            output+=variables[i]*variables[i];
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
        super("rosenbrock");
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

class OptimizationInputs{
    
    ObjectiveFunction function;
    int dimensionality;
    int iterations;
    double tolerance;
    double stepSize;
    boolean isValid;
    double[] variables; 

}

abstract class Output{
    abstract void printOutputs(int iteration, double OFValue, double[] x, String prompt);
}

class ConsoleOutput extends Output{
    
    @Override
    public void printOutputs(int iteration, double OFValue, double[] x, String prompt){
        System.out.println();
        if (iteration==1){
            System.out.print("\nOptimization Process:");
        }
        System.out.printf("\nIteration %d:", iteration);
        System.out.printf("\nObjective Function Value: %.5f", OFValue);
        System.out.print("\nx-values: ");
        for (double value : x){
            System.out.printf("%.5f ", value);
        }
        if (prompt != null){
            System.out.print("\n"+prompt);
        }
    }

}

class FileOutput extends Output{
    public void printOutputs(int iteration, double OFValue, double[] x, String prompt){}
}

class SteepestDescentOptimizer{

    static double[] optimizeSteepestDescent(ObjectiveFunction objectiveFunction, double[] variables, int iterations, double tolerance, double stepSize, int dimensionality, int outputType){
        
        double[] gradients=objectiveFunction.computeGradient(variables);
        double gradientnorm=computeGradientNorm(gradients);
        Output output;
        if (outputType==1){
            output = new ConsoleOutput();
        }else{
            output = new FileOutput();
        }
        for (int i=0; i<iterations; i++){ 
            String prompt = null; 
           if (gradientnorm>=tolerance){
                for (int j=0; j<variables.length; j++){
                    variables[j]=variables[j]-(stepSize*gradients[j]);
                }
                gradients=objectiveFunction.computeGradient(variables);
                gradientnorm=computeGradientNorm(gradients);
            }else{
                prompt = "\nConvergence reached after "+ (i) + " iterations.\n";
            }
            if(i==iterations-1){
                prompt = "\nMaximum iterations reached without satisfying the tolerance.\n";
            }

            output.printOutputs(i+1, objectiveFunction.compute(variables), variables, prompt);
        }
        
        return variables;
    }

    static double computeGradientNorm(double[] gradients){
        
        double output=0;
        
        for(int i=0; i<gradients.length; i++){
            output+=gradients[i]*gradients[i];
        }

        return Math.sqrt(output);
    }

    static int getValidatedInput(Scanner scanner, String prompt){
        System.out.println(prompt);
        boolean isValid=true;
        int input=0;
        String outarg = "Please enter a valid input (0 or 1).";
        do{
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
            System.out.print("\nEnter the choice of objective function (quadratic or rosenbrock):");
            functionName=scanner.nextLine();
            System.out.println(functionName);
            System.out.print("\nEnter the dimensionality of the problem:");
            inputs.dimensionality=Integer.parseInt(scanner.nextLine());
            System.out.print("\nEnter the number of iterations:");
            inputs.iterations=Integer.parseInt(scanner.nextLine());
            System.out.print("\nEnter the tolerance:");
            inputs.tolerance=Double.parseDouble(scanner.nextLine());
            System.out.print("\nEnter the step size:");
            inputs.stepSize=Double.parseDouble(scanner.nextLine());
            
            if (functionName.equals("rosenbrock")){
                inputs.function= new RosenbrockFunction();
            }else if(functionName.equals("quadratic")){
                inputs.function= new QuadraticFunction();
                System.out.println(functionName);
            }else{
                throw new IllegalArgumentException("Unknown objective function.");
            }
        }catch(IllegalArgumentException e){
            System.out.printf("Error: " + e.getMessage());
            inputs.isValid=false;
        }

        if (inputs.isValid == true){
            try{
                System.out.printf("\nEnter the initial point as %d space-separated values:", inputs.dimensionality);
                inputs.variables=getVarsfromString(scanner.nextLine());

                if(inputs.variables.length != inputs.dimensionality){
                    throw new IllegalArgumentException("Initial point dimensionality mismatch.");
                }

                double[] bounds={-5,5};

                if(checkBounds(inputs.variables, bounds)!=0){
                    throw new IllegalArgumentException("Bounds Exception");
                }
            }catch(IllegalArgumentException e){
                if (e.getMessage()=="Bounds Exception"){
                double[] bounds={-5,-5};
                System.out.printf("Error: Initial point %.1f is outside the bounds [%.1f, %.1f]", checkBounds(inputs.variables, bounds), bounds[0], bounds[1]);
                inputs.isValid=false;
                }else{
                    System.out.printf("Error: " + e.getMessage());
                    inputs.isValid=false;
                }
            }
        }
        return inputs;
    }

    static OptimizationInputs getFileInput(Scanner scanner){
        OptimizationInputs inputs = new OptimizationInputs();
        String functionName;
        inputs.isValid=true;
        
        try{
            functionName=scanner.nextLine();
            System.out.println(functionName);
            inputs.dimensionality=Integer.parseInt(scanner.nextLine());
            inputs.iterations=Integer.parseInt(scanner.nextLine());
            inputs.tolerance=Double.parseDouble(scanner.nextLine());
            inputs.stepSize=Double.parseDouble(scanner.nextLine());
            if (functionName.equals("rosenbrock")){
                inputs.function= new RosenbrockFunction();
            }else if(functionName.equals("quadratic")){
                inputs.function= new QuadraticFunction();
                System.out.println(functionName);
            }else{
                throw new IllegalArgumentException("Unknown objective function.");
            }
        }catch(IllegalArgumentException e){
            System.out.printf("Error: " + e.getMessage());
            inputs.isValid=false;
        }

        if (inputs.isValid == true){
            try{
                System.out.printf("\nEnter the initial point as %d space-separated values:", inputs.dimensionality);
                inputs.variables=getVarsfromString(scanner.nextLine());

                if(inputs.variables.length != inputs.dimensionality){
                    throw new IllegalArgumentException("Initial point dimensionality mismatch.");
                }

                double[] bounds={-5,5};

                if(checkBounds(inputs.variables, bounds)!=0){
                    throw new IllegalArgumentException("Bounds Exception");
                }
            }catch(IllegalArgumentException e){
                if (e.getMessage()=="Bounds Exception"){
                double[] bounds={-5,-5};
                System.out.printf("Error: Initial point %.1f is outside the bounds [%.1f, %.1f]", checkBounds(inputs.variables, bounds), bounds[0], bounds[1]);
                inputs.isValid=false;
                }else{
                    System.out.printf("Error: " + e.getMessage());
                    inputs.isValid=false;
                }
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

        int start=SteepestDescentOptimizer.getValidatedInput(scanner, "Press 0 to exit or 1 to enter the program:");
        OptimizationInputs inputs;

        if (start == 1){
            int inputform=SteepestDescentOptimizer.getValidatedInput(scanner, "Press 0 for .txt input or 1 for manual input:");
            int outputform=SteepestDescentOptimizer.getValidatedInput(scanner, "Press 0 for .txt output or 1 for console output:");
            if (inputform==1){
                inputs = SteepestDescentOptimizer.getManualInput(scanner);
            }else{
                inputs = SteepestDescentOptimizer.getFileInput(scanner);
            }
            if (inputs.isValid==true){
                SteepestDescentOptimizer.optimizeSteepestDescent(inputs.function, inputs.variables, inputs.iterations, inputs.tolerance, inputs.stepSize, inputs.dimensionality, outputform);
            }
        }


        System.out.println("\nExiting Program...");

    }

}