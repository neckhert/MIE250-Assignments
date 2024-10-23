import java.util.Scanner;

class Outputs{
    //This class is used to get the outputs for the Catch_Error function to return later on
    boolean isValid;
    double output;
    public Outputs(boolean isValid, double output){ //Constructor to help with building the class
        this.isValid=isValid;
        this.output=output;
    }
}
public class asst1_eckhertn{
    public static void main(String[] args){
        Scanner input = new Scanner(System.in); //Initialize our scanner to get user inputs

        System.out.print("WELCOME TO THE SPRING WEIGHT CALCULATOR (0 TO QUIT, 1 TO PROCEED) ");
        int start = Integer.parseInt(input.nextLine()); //Scan for 0 or 1 to start calculator (don't need to check for a valid input)
        System.out.println();
        
        if (start==1){
            double D=asst1_eckhertn.Get_Valid_Input('D', input);//Get input for coil diameter D
            double d=asst1_eckhertn.Get_Valid_Input('d', input);//Get input for wire diameter d
            int N = (int)asst1_eckhertn.Get_Valid_Input('N', input);//Get input for for number of turns N
            System.out.printf("Weight: %.2f kgm/s^2", asst1_eckhertn.Calculate_Weight(D,d,N));//Print our final calculated value for weight
        }
        System.out.println("\nGOODBYE!");//Inform user the program has concluded
        input.close();//Close the scanner
    }

    public static double Calculate_Weight(double D, double d, int N ){
        /*Input is three doubles: D the coil diameter, d the wire diameter, and N the number of turns
        Output is a double W which is the weight of the wire*/

        double mass = (N+2)*D*(d*d);//Calculates mass using the formula
        double weight=mass*9.81;//gets the weight
        weight=(int)(weight*100);//truncates the weight
        return ((double)weight)/100;
    }

    public static double Get_Valid_Input(char input_type, Scanner input){
        /*Input is the scanner to get user input, and an input type signifying the kind of input we are receiving (D=coil diameter, d=wire diameter, N=number of turns)
         Output is a valid value for the specified input type*/

        double output=0; //Initialize an output value to return
        Outputs result; //Initialize a "Outputs" class to help make sure we get a valid value
        switch(input_type) { //Check for each case of input_type based on how the function was called
            case 'D':    
                do{
                    System.out.print("Enter coil diameter D (m): ");//Print the message for the user to give input
                    result=asst1_eckhertn.Catch_Error(0.25,1.3,'d',input);//call the Catch_Error function with the max and min of coil diameter and the data type of d or double
                    output=result.output;//set output to the value that Catch_Error scanned for
                }while(!result.isValid); //check if Catch_Error found any errors and if it did repeat the loop
                System.out.println();  
                break;//end the case
            case 'd':
                do{
                    System.out.print("Enter wire diameter d (m): ");//Print the message for the user to give input
                    result=asst1_eckhertn.Catch_Error(0.05,2,'d', input); //call the Catch_Error function with the max and min of wire diameter and the data type of d or double
                    output=result.output;//set output to the value that Catch_Error scanned for
                }while(!result.isValid); //check if Catch_Error found any errors and if it did repeat the loop
                System.out.println();
                break;//end the case
            case 'N':
                do{
                    System.out.print("Enter number of turns N: ");//Print the message for the user to give input
                    result=asst1_eckhertn.Catch_Error(1,15,'n', input);//call the Catch_Error function with the max and min of number of turns and the data type of n or int
                    output=result.output;//set output to the value that Catch_Error scanned for
                }while(!result.isValid); //check if Catch_Error found any errors and if it did repeat the loop
                System.out.println();   
                break;//end the case
        }
        return output;//return the valid output for the specified input type
    }

    public static Outputs Catch_Error(double min, double max, char type, Scanner input){
        /*Inputs are the min and max possible values of the input we're scanning for, the type of value we're scanning for (n=int, d=double), and the scanner to get user input
         Output is an outputs class with its output double value being the scanned input and its isValid boolean value telling whether the scanned value creates and error (false) or not (true).*/
        
        Outputs returns = new Outputs(true,0);//Initialize our outputs class assuming that the input will be valid
        try{
            returns.output = Double.parseDouble(input.nextLine());//try to scan for the users input, then we will check if it creates an error

            if (returns.output<0){//if the return is negative we get the correct error based on datatype
                if(type=='d'){
                    throw new IllegalArgumentException("\nENTER A POSITIVE INPUT");
                }else{
                    throw new IllegalArgumentException("\nN SHOULD BE A POSITIVE INTEGER");
                }
            }
            if (type=='n' && returns.output % 1 != 0){//if the input is not an integer for "n" datatype then we get the correct error
                throw new IllegalArgumentException("\nN SHOULD BE AN INTEGER");
            }
            if(min>returns.output || returns.output>max){//if the input is out of bounds we get the correct error
                throw new IllegalArgumentException("\nINPUT MUST BE WITHIN BOUNDS");
            }
        }catch(NumberFormatException e){//If the input is not a number we catch the error and print the correct error message
            System.out.println("\nENTER A VALID INPUT");
            returns.isValid=false;//change our isValid value to false, signifying we need to check for the value again
        }catch (IllegalArgumentException e) { //Catch and print the correct error based on the illegal argument
            System.out.println(e.getMessage());
            returns.isValid=false;//change our isValid value to false, signifying we need to check for the value again
        }
        return returns; //returns the outputs class with the scanned output value and the isValid boolean telling if the value is valid or not
    }
}