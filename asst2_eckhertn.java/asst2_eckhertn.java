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
    
    abstract double compute(double[] variables); //abstract function to calculate function values
    abstract double[] computeGradient(double[] variables); //abstract function to calculate function gradients
    
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

    //constructor that sets the name of Objective Function to reflect that its quadratic
    public QuadraticFunction(){
        super("Quadratic");
    }

    //overrides the compute method to compute according to the quadratic function
    @Override
    double compute(double[] variables){
        double output=0;
        for (double variable : variables){
            output+=Math.pow(variable,2);
        }
        return output;
    }

    //overrides the computeGradient method to compute according to the quadratic function gradients
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

    //constructor that sets the name of Objective Function to reflect that its rosenbrock
    public RosenbrockFunction(){
        super("Rosenbrock");
    }

    //overrides the compute method according to the rosenbrock function
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

    //overrides the computeGradient method according to the incorrect rosenbrock gradients provided in the assignment instructions
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
    
    //constructor that sets the name of Objective Function to reflect that its rosenbrock bonus
    public Rosenbrock_Bonus(){
        super("Rosenbrock_Bonus");
    }

    //overrides the compute method according to the rosenbrock function
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

    //overrides the computeGradient method according to the correct rosenbrock gradients
    @Override
    double[] computeGradient(double[] variables){
        double[] outputs = new double[variables.length];
        
        outputs[0]=-400*variables[0]*(variables[1]-Math.pow(variables[0],2))-2*(1-variables[0]);
        for (int i=1; i<variables.length-1; i++){
            outputs[i]=-400*variables[i]*(variables[i+1]-Math.pow(variables[i],2))-2*(1-variables[i])+200*(variables[i]-Math.pow(variables[i-1],2));
        }
        outputs[variables.length-1]=200*(variables[variables.length-1]-Math.pow(variables[variables.length-2],2));
        return outputs;
    }
}

//class that is used later as the output for the getFileInput and getManualInput methods so that they can pass the inputted values to my main
class OptimizationInputs{
    
    ObjectiveFunction function; //the function that will be minimized
    int dimensionality; //the dimensionality, d, of the function
    int iterations; //the maximum number of iterations of gradient descent
    double tolerance; //the minimum tolerance epsilon, where when the magnitude of the gradient is less than it the optimizer will stop
    double stepSize; //the learning rate which scales the gradient during gradient descent
    boolean isValid; //records whether or not the input recieved is valid
    double[] variables; //the initial starting point values to begin minimization at

}

class Output{

    //method that handles outputs for console
    static public void printOutputs(int iteration, double OFValue, double[] x, String prompt, double gradientnorm, String fileName){
        System.out.println();//prints newline
        
        //prints header "Optimization process:" during first iteration
        if (iteration==1){
            System.out.print("\n\nOptimization process:");
            System.out.print("\nIteration 1:");
        }else{
            System.out.printf("\n\nIteration %d:", iteration);//prints current iteration number
        }
        System.out.printf("\nObjective Function Value: %.5f", OFValue);//prints current Objective Function value
        System.out.print("\nx-values: ");
        
        //prints current x-values
        for (double value : x){
            System.out.printf("%.5f ", SteepestDescentOptimizer.floorTo5Decimals(value));
        }

        //prints current tolerance/gradient magnitude value
        if (iteration != 1){
        System.out.printf("\nCurrent Tolerance: %.5f", gradientnorm);
        }

        //if there is an additional output, then it is printed
        if (prompt != null){
            System.out.print("\n"+prompt);
        }
    }

    //method that handles outputs to a file 
    static public void printOutputs(int iteration, double OFValue, double[] x, String prompt, double gradientnorm, String fileName, PrintWriter writer){
        
        //writes header "Optimization process:" during first iteration
        if (iteration==1){
            writer.write("\n\nOptimization process:");
            writer.write("\nIteration 1:");
        }else{
            writer.format("\n\nIteration %d:", iteration);//writes current iteration number
        }
        writer.format("\nObjective Function Value: %.5f", OFValue);//writes current objective function value
        writer.format("\nx-values: ");

        //writes current rounded x-values
        for (double value : x){
            writer.format("%.5f ", SteepestDescentOptimizer.floorTo5Decimals(value));
        }

        //writes current tolerance/gradient magnitude value
        if (iteration != 1){
            writer.format("\nCurrent Tolerance: %.5f", gradientnorm);
        }

        //if there is an additional output, then it is written
        if (prompt != null){
            writer.write("\n"+prompt);
        }
    }
}

//actual optimizer class that handles optimization and inputs
class SteepestDescentOptimizer{

    //rounding method used in order to match assignment outputs
    static double floorTo5Decimals(double value){
        BigDecimal bd = new BigDecimal(value).setScale(5, RoundingMode.FLOOR);
        return bd.doubleValue();
    }

    //optimizer method that applies steepest descent to objective function values
    static double[] optimizeSteepestDescent(ObjectiveFunction objectiveFunction, double[] variables, int numIterations, double tolerance, double stepSize, int dimensionality, String fileName){
        
        double[] gradients=objectiveFunction.computeGradient(variables); //obtains the initial gradients for objective function values
        double gradientnorm=0;//initializes a variable to represent the gradients magnitude
        PrintWriter writer;//creates a writer to be used for file output
        try{
            //if the fileName is not null (it holds an actual value), then we initialize writer and write initial info to the file
            if(fileName != null){
                writer = new PrintWriter(fileName);//initializes writer as a new PrintWriter
                writer.format("Objective Function: %s", objectiveFunction.getName());//writes the objective function name
                writer.format("\nDimensionality: %d", variables.length);//writes the dimensionality
                writer.write("\nInitial Point: ");

                //writes the initial point values
                for (double variable : variables){
                    writer.format("%.1f ", variable);
                }

                writer.format("\nIterations: %d", numIterations);//writes the max number of iterations
                writer.format("\nTolerance: %.5f", tolerance);//writes the minimum tolerance
                writer.format("\nStep Size: %.5f", stepSize);//writes the learning rate
            
            //if the fileName is null, we print initial info to the console
            }else{
                writer = null;//set the writer to null to avoid errors
                System.out.printf("Objective Function: %s", objectiveFunction.getName());//prints the objective function name
                System.out.printf("\nDimensionality: %d", variables.length);//prints the dimensionality
                System.out.print("\nInitial Point: ");

                //prints the initial point values
                for (double variable : variables){
                    System.out.printf("%.1f ", variable);
                }

                System.out.printf("\nIterations: %d", numIterations);//prints the max number of iterations
                System.out.printf("\nTolerance: %.5f", tolerance);//prints the minimum tolerance
                System.out.printf("\nStep Size: %.5f", stepSize);//prints the learning rate
            }
            
            //Begins optimization by repeating gradient descent until the max iterations is reached or the gradient norm passes below the min tolerance
            for (int i=0; i<numIterations; i++){ 
                
                int iteration = i+1;//set the iteration as 1 more than i (for outputs), since Java is zero indexed
                String prompt = null;//set the prompt as null until there is an actual prompt

                //check if the gradient norm has passed under the tolerance (not for iteration 1 since there is no gradient norm yet)
                if(iteration != 1 && gradientnorm<tolerance){
                    prompt = "\nConvergence reached after "+ iteration + " iterations.\n";//set the prompt to show that convergence was reached
                    i=numIterations-1;//set i to max iterations -1 to break the loop on next iteration
                
                //check if we have reached the last iteration
                }else if(i==numIterations-1){
                    prompt = "\nMaximum iterations reached without satisfying the tolerance.\n";//set the prompt to show that we have reached the max iterations without convergance
                }

                //if fileName is not null, we write our outputs for the current iteration to the file
                if(fileName != null){
                    Output.printOutputs(iteration, floorTo5Decimals(objectiveFunction.compute(variables)), variables, prompt, gradientnorm, fileName, writer);//writes outputs to file
                    
                    //if we have reached the maximum number of iterations or convergence, then we write the final message 
                    if (i==numIterations-1){
                        writer.write("\nOptimization process completed.\n");//write final message
                        writer.close();//close the writer to avoid error and since we no longer use it
                    }

                //if we are printing to console, we need to print the outputs for the current iteration to the console
                }else{
                    Output.printOutputs(iteration, floorTo5Decimals(objectiveFunction.compute(variables)), variables, prompt, gradientnorm, fileName);//prints outputs to console
                    
                    //if we have reached the maximum number of iterations or convergence, then we print the final message
                    if (i==numIterations-1){
                        System.out.print("\nOptimization process completed.\n");//print final message
                    }        
                }

                //update the variables using gradient descent and round them for the next iteration 
                for (int j=0; j<variables.length; j++){
                    variables[j]=variables[j]-(stepSize*gradients[j]);
                    variables[j] = floorTo5Decimals(variables[j]);
                }

                gradientnorm=floorTo5Decimals(computeGradientNorm(gradients));//calculates and rounds the current gradient norm
                gradients=objectiveFunction.computeGradient(variables);//calculates the gradients for the new variable values
            }
        
        //catches any errors relating to the print writer and file output    
        }catch (IOException e){
            System.out.println("Error writing the file.");
        }
        
        return variables;//returns the optimized point
    }   

    //method that is used to compute the magnitude of the gradients
    static double computeGradientNorm(double[] gradients){
        
        double output=0;
        
        for(double gradient : gradients){
            output+=gradient*gradient;//add the every value squared
        }

        return Math.sqrt(output);//return the square root of every value squared
    }

    //method that gets a valid input of either 1 or 0 for the initial menu prompts
    static int getValidatedInput(Scanner scanner, String prompt){
        boolean isValid; //variable that tells whether the input is valid or not
        int input=0;// saves the value of the input (either 1 or 0)
        String outarg = "Please enter a valid input (0 or 1).";//error argument that is used in catch-throw statements
        
        //while the input isn't valid we continue to prompt the user for a valid input
        do{
            System.out.println(prompt);//print the prompt we're asking for
            isValid=true;//sets isValid to true before getting input
            try{
                input=Integer.parseInt(scanner.nextLine());//scans for user input

                //if the input is an int but not binary we throw a new exception
                if (input != 1 && input != 0){
                    throw new IllegalArgumentException(outarg);
                }
            //if the input is not an int we catch the error
            }catch(NumberFormatException e){
                System.out.println(outarg);//print the error argument
                isValid=false;//sets isValid to false forcing the loop to retry for correct input

            }catch(IllegalArgumentException e){
                System.out.println(e.getMessage());//print the error argument
                isValid=false;//sets isValid to false forcing the loop to retry for correct input
            }
        }while(!isValid);
        
        return input;//return the binary input value
    }

    //method that checks if the variables are within the bounds and returns the value of the variable that violates the bounds (if there is one)
    static double checkBounds(double[] variables, double[] bounds){
        double check = 0;//initialize a variable that represents any violating values

        //iterate through all the variables and check whether or not they violate the bounds, if they do set our output value to their value
        for (int i=0; i<variables.length; i++){
            if (variables[i]<bounds[0]||variables[i]>bounds[1]){
                check=variables[i];
            }
        }

        return check; //return 0 if there are no bounds violations and a non-zero value if there is
    }

    //method that returns and handles manual user inputs via the console
    static OptimizationInputs getManualInput(Scanner scanner){    
        OptimizationInputs inputs = new OptimizationInputs();//create a new instance of the OptimizationInputs class to return from our function
        String functionName;//initialize a string to hold our function name
        inputs.isValid=true;//set a boolean to check whether the input is valid or not (true for now)
        
        try{
            System.out.println("Enter the choice of objective function (quadratic or rosenbrock):");//prompt user to enter objective function name
            functionName=scanner.nextLine();//scan for objective function name
            System.out.println("Enter the dimensionality of the problem:");//prompt user to enter dimensionality
            inputs.dimensionality=Integer.parseInt(scanner.nextLine());//scan for dimensionality
            System.out.println("Enter the number of iterations:");//prompt user to enter num of iterations
            inputs.iterations=Integer.parseInt(scanner.nextLine());//scan for iterations
            System.out.println("Enter the tolerance:");//prompt user to enter the tolerance
            inputs.tolerance=Double.parseDouble(scanner.nextLine());//scan for tolerance
            System.out.println("Enter the step size:");//prompt user to enter learning rate
            inputs.stepSize=Double.parseDouble(scanner.nextLine());//scan for learning rate
            
            //check whether the inputted function name was valid and if not throw an exception
            if (functionName.equals("rosenbrock")){
                inputs.function= new RosenbrockFunction();
            }else if(functionName.equals("quadratic")){
                inputs.function= new QuadraticFunction();
            }else if(functionName.equals("rosenbrock_bonus")){
                inputs.function = new Rosenbrock_Bonus();
            }else{
                throw new IllegalArgumentException("Unknown objective function.");
            }
        
        //catch if the function name is invalid and print the error message
        }catch(IllegalArgumentException e){
            System.out.printf("Error: " + e.getMessage());
            inputs.isValid=false;//set inputs.isValid to false to end the code
        }

        //if all previous inputs are valid, we continue to scan for the remaining values
        if (inputs.isValid == true){
            try{
                System.out.printf("Enter the initial point as %d space-separated values:\n", inputs.dimensionality);//prompt user to enter initial values
                inputs.variables=getVarsfromString(scanner.nextLine());//scan for initial variables and convert them into an array from a string
                
                if(inputs.variables.length != inputs.dimensionality){
                    throw new IllegalArgumentException("Initial point dimensionality mismatch.");
                }

                //check that all inputs are within bounds and throw an error accordingly
                if(checkBounds(inputs.variables, inputs.function.getBounds())!=0){
                    throw new IllegalArgumentException("Bounds Exception");
                }
            
            //catch any thrown errors and print the error message accordingly
            }catch(IllegalArgumentException e){
                if (e.getMessage()=="Bounds Exception"){
                    double[] bounds= inputs.function.getBounds();//initialize bounds to print in the error message
                    System.out.printf("Error: Initial point %.1f is outside the bounds [%.1f, %.1f].", checkBounds(inputs.variables, bounds), bounds[0], bounds[1]);
                    inputs.isValid=false;//set inputs.isValid to false to end the code
                }else{
                    System.out.printf("Error: " + e.getMessage());
                    inputs.isValid=false;//set inputs.isValid to false to end the code
                }
            }
        }
        
        return inputs;//return the given inputs
    }

    //method that handles and return file inputs
    static OptimizationInputs getFileInput(String configFile){
        OptimizationInputs inputs = new OptimizationInputs();//create a new instance of the OptimizationInputs class to return from our function
        String functionName;//initialize a string to hold our function name
        inputs.isValid=true;//set a boolean to check whether the input is valid or not (true for now)

        try{
            BufferedReader reader = new BufferedReader(new FileReader(configFile));//initialize a Buffered File Reader to read input file
            functionName=reader.readLine();//read the function name
            inputs.dimensionality=Integer.parseInt(reader.readLine());//read the dimensionality
            inputs.iterations=Integer.parseInt(reader.readLine());//read the maximum number of iterations
            inputs.tolerance=Double.parseDouble(reader.readLine());//read the minimum tolerance
            inputs.stepSize=Double.parseDouble(reader.readLine());//read the learning rate
            
            //check if the file's function name is valid and if not throw the proper error exception
            if (functionName.equals("rosenbrock")){
                inputs.function= new RosenbrockFunction();
            }else if(functionName.equals("quadratic")){
                inputs.function= new QuadraticFunction();
            }else if(functionName.equals("rosenbrock_bonus")){
                inputs.function = new Rosenbrock_Bonus();
            }else{
                reader.close();//close the reader first since we no longer need it
                throw new IllegalArgumentException("Unknown objective function.");
            }
            
            inputs.variables=getVarsfromString(reader.readLine());//read the variables from the file and convert them to an array from a string

            if(inputs.variables.length != inputs.dimensionality){
                reader.close();//close the reader first since we no longer need it
                throw new IllegalArgumentException("Initial point dimensionality mismatch.");
            }
            
            //check that all inputs are within bounds and throw an error accordingly
            if(checkBounds(inputs.variables, inputs.function.getBounds())!=0){
                reader.close();//close the reader first since we no longer need it
                throw new IllegalArgumentException("Bounds Exception");
            }
            
            reader.close();//if we reach end of try statements with no throw, then we close the reader since we no longer need it
        
        //catch any errors with reading the file, such as file not found and print the proper error message
        }catch(IOException e){
            System.out.println("Error reading the file.");
            inputs.isValid = false;//set inputs.isValid to false to end the code
        
        //catch any invalid inputs and print the appropriate error message
        }catch(IllegalArgumentException e){
            if (e.getMessage()=="Bounds Exception"){
                    double[] bounds= inputs.function.getBounds();//initialize bounds to print in the error message
                    System.out.printf("Error: Initial point %.1f is outside the bounds [%.1f, %.1f].", checkBounds(inputs.variables, bounds), bounds[0], bounds[1]);
                    inputs.isValid=false;//set inputs.isValid to false to end the code
                }else{
                    System.out.printf("Error: " + e.getMessage());
                    inputs.isValid=false;//set inputs.isValid to false to end the code
                }
        }
        return inputs;//return the read inputs
    }

    //method that takes a string and returns an array by seperating each value by spaces
    static double[] getVarsfromString(String varsString){
        String[] separatedVarsString = varsString.split(" ");//creates an array of strings by splitting the current string at every space
        double [] outputs= new double[separatedVarsString.length];//initializing a new array of doubles with the same size as the string array

        for (int i =0; i<separatedVarsString.length; i++){
            outputs[i]=Double.parseDouble(separatedVarsString[i]);//convert every string in the string array to a double in the double array         
        }

        return outputs;//return the double array
    }

}

public class asst2_eckhertn{
    
    public static void main(String[] args){

        Scanner scanner = new Scanner(System.in);//initialize a scanner to get user input from the console

        int start=SteepestDescentOptimizer.getValidatedInput(scanner, "Press 0 to exit or 1 to enter the program:"); //Prompt the user if they want to enter the program

        //if the user wants to enter then we begin the program
        if (start == 1){
            OptimizationInputs inputs; //initializes object to hold the input values
            String outputFile; //creates a variable for the outputFile (if it is null then output is done to the console)
            int inputform=SteepestDescentOptimizer.getValidatedInput(scanner, "Press 0 for .txt input or 1 for manual input:");//scan for the input form (0 for txt 1 for manual)
            int outputform=SteepestDescentOptimizer.getValidatedInput(scanner, "Press 0 for .txt output or 1 for console output:");//scan for the output form (0 for txt 1 for console)
            
            //if input form is manual use getManualInput method
            if (inputform==1){
                inputs = SteepestDescentOptimizer.getManualInput(scanner);
            
            //if input form is txt use getFileInput method
            }else{
                System.out.println("Please provide the path to the config file: ");//prompt user for the input file name
                String configfile =scanner.nextLine();//scan for the input file name
                inputs = SteepestDescentOptimizer.getFileInput(configfile);
            }
            
            //if the inputs are valid we can continue to optimization and outputs
            if (inputs.isValid==true){
                
                //if output form is console, set outputFile to null
                if(outputform==1){
                    outputFile = null;
                
                //if output form is txt scan for the output file name/path
                }else{
                    System.out.println("Please provide the path for the output file: ");
                    outputFile=scanner.nextLine();
                }
                

                SteepestDescentOptimizer.optimizeSteepestDescent(inputs.function, inputs.variables, inputs.iterations, inputs.tolerance, inputs.stepSize, inputs.dimensionality, outputFile);//call optimization method with given inputs
                scanner.close();//closes scanner since we no longer need it
            }
        
        //if the user doesn't want to enter we close the scanner and terminate the code
        }else{
            System.out.println("Exiting Program...");
            scanner.close();//closes scanner since we no longer need it
        }
    }
}