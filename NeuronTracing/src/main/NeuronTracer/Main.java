package main.NeuronTracer;

import main.ImageUtils.ImageDistanceUtils;
import main.ImageUtils.ImageUtils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.awt.Color;
import javax.imageio.ImageIO;
import java.util.Scanner;

public class Main {

    // Gets user inputs and calls MainProgram to do the actual tracing
    public static void main(String[] args) {

        String userInput;
        String fileLocation;
        Scanner input = new Scanner(System.in);
        int numInput = 0;
        ArrayList<Branch> branches = new ArrayList<Branch>();


        while (numInput < 20) {

            System.out.print("Enter the file path or enter 0 to exit: ");
            fileLocation = input.nextLine();
            //fileLocation = "C:\\Users\\rusha\\Desktop\\BridgeTracing\\Tracing\\darkPink.jpg";
            //fileLocation = "C:\\Users\\rusha\\Desktop\\BridgeTracing\\Tracing\\NoTrace\\noTrace5.jpg";

            if (fileLocation.equals("0"))
                break;

            try {
                BufferedImage image;
                image = ImageIO.read(new File(fileLocation));
                //fileLocation = "C:\\Users\\rusha\\Desktop\\BridgeTracing\\BigTest3filled.jpg";
                //image = ImageIO.read(new File("C:\\Users\\rusha\\Desktop\\BridgeTracing\\BigTest3filled.jpg"));
                //fileLocation = "C:\\Users\\rusha\\Desktop\\BridgeTracing\\Tracing\\darkPink.jpg";
                //image = ImageIO.read(new File("C:\\Users\\rusha\\Desktop\\BridgeTracing\\Tracing\\darkPink.jpg"));


                //output image
                BufferedImage whiteOutput = new BufferedImage(image.getWidth(), image.getHeight(),BufferedImage.TYPE_INT_RGB);
                BufferedImage blackOutput = new BufferedImage(image.getWidth(), image.getHeight(),BufferedImage.TYPE_INT_RGB);
                BufferedImage output = new BufferedImage(image.getWidth(), image.getHeight(),BufferedImage.TYPE_INT_RGB);

                ImageUtils.setImageToWhite(whiteOutput, image, true);
                ImageUtils.setImageToWhite(blackOutput, image, false);
                ImageUtils.setImageToWhite(output, image, false);


                int red = 230; //shade of red we use when finding separate branches
                int xOffsetFromEye = 40; //offset box from the eye
                int yOffsetFromEye = 40; //offset box from the eye
                int startOfBranchRed = 220; //shade of red when searching branches to the eye
                int sideToRemove = 0; //which starting branch going to remove

                String tempUserInput = "";


                //getting input from the user
                while (numInput < 20) {
                    System.out.print("Enter 0 to get information about the program and how to tweak values, " +
                            "1 to change the Offset From the Eye,\n" +
                            "2 for the Starting main.NeuronTracer.Branch Shade,\n" +
                            "3 for main.NeuronTracer.Branch Shade,\n" +
                            "any other key to skip:");
                    userInput = input.nextLine();

                    if (userInput.equals("0")) {
                        System.out.println("\nThere are 3 values you can alter to change how the program searches through the image:");
                        System.out.println("Offset From the Eye: The distance from the eye the program will search from to find the starting branches (the blue box around the eye).");
                        System.out.println("If there's problems with finding the branches connected to the eye, tweak this value. Default is 40.");
                        System.out.println("\nStarting main.NeuronTracer.Branch Shade: Certain pixels will be ignored if they aren't dark enough."
                                + "\nTweak this value if changing the Offset From the Eye didn't fix finding the branches connected to the eye. Default is 220" +
                                "(max is 254, the higher the value, the more pixels will be looked at but could produce less acurate results).");
                        System.out.println("\nmain.NeuronTracer.Branch Shade: This determines the shade of pixels that will be looked at for all branches besides the starting ones."
                                + "\nIncreasing this value will let the program look at lighter pixels, but may produce less accurate results. Default is 230\n");
                    } else if (userInput.equals("1")) {
                        System.out.print("Default value is 40. Current x value is " + xOffsetFromEye+". Enter the value you'd like to change the X Offset From the Eye to (must be a non decimal number): ");
                        userInput = input.nextLine();
                        System.out.print("Default value is 40. Current y value is " + yOffsetFromEye+". Enter the value you'd like to change the Y Offset From the Eye to (must be a non decimal number): ");
                        tempUserInput = input.nextLine();

                        try {
                            xOffsetFromEye = Integer.parseInt(userInput);
                            yOffsetFromEye = Integer.parseInt(tempUserInput);
                        } catch (Exception e) {
                            System.out.println("Value entered was not a non decimal number.");
                        }
                        if (xOffsetFromEye > 100)
                            xOffsetFromEye = 100;
                        else if (xOffsetFromEye < 5)
                            xOffsetFromEye = 5;
                        if (yOffsetFromEye > 100)
                            yOffsetFromEye = 100;
                        else if (yOffsetFromEye < 5)
                            yOffsetFromEye = 5;

                    } else if (userInput.equals("2")) {
                        System.out.print("Default value is 220. Current value is " + startOfBranchRed + ". Enter the value you'd like to change Starting main.NeuronTracer.Branch Shade to (must be a non decimal number): ");
                        userInput = input.nextLine();
                        try {
                            startOfBranchRed = Integer.parseInt(userInput);
                        } catch (Exception e) {
                            System.out.println("Value entered was not a non decimal number.");
                        }
                        if (startOfBranchRed > 254)
                            startOfBranchRed = 254;
                        else if (startOfBranchRed < 5)
                            startOfBranchRed = 5;

                    } else if (userInput.equals("3")) {
                        System.out.print("Default value is 240. Current value is " + red + ". Enter the value you'd like to change main.NeuronTracer.Branch Shade to (must be a non decimal number): ");
                        userInput = input.nextLine();
                        try {
                            red = Integer.parseInt(userInput);
                        } catch (Exception e) {
                            System.out.println("Value entered was not a non decimal number.");
                        }
                        if (red > 254)
                            red = 254;
                        else if (red < 5)
                            red = 5;

                    } else
                        break;
                    numInput++;
                }

                //giving the option to not include the whatever branch
                System.out.println("  3");
                System.out.println("1 - 2   These numbers indicate the sides of the eye.");
                System.out.println("  4");
                System.out.print("Enter the side that contains the axon, otherwise enter any other value to include all the sides:");
                userInput = input.nextLine();

                if (userInput.equals("1") ||userInput.equals("2") ||userInput.equals("3") ||userInput.equals("4")) {
                    sideToRemove = Integer.parseInt(userInput);
                }

                System.out.println("Starting tracing. X offset From the Eye: " + xOffsetFromEye + ", Y offset From The Eye: " + yOffsetFromEye+". Starting main.NeuronTracer.Branch Shade: " + startOfBranchRed + ". main.NeuronTracer.Branch Shade: " + red);

                //
                // Start of the actual work
                //
                Tracer tracer = new Tracer(red, xOffsetFromEye, yOffsetFromEye, startOfBranchRed, sideToRemove, image, output, whiteOutput, blackOutput);

                branches = tracer.createBranches();

                //getting file location to save the output files
                String outputLocation = fileLocation.substring(0, fileLocation.indexOf(".jpg"));

                //
                //Saving Files
                //
                File file = new File(outputLocation + "TracedImage.jpg");
                ImageIO.write(output, "jpg", file);
                File cleanedFile = new File(outputLocation + "TracedImageWhite.jpg");
                ImageIO.write(whiteOutput, "jpg", cleanedFile);

                createNDFFile(outputLocation, "Tracings.ndf", branches);

                System.out.println("Images Saved in " + outputLocation);
                break;
            } catch (IOException e){
                System.out.println("Couldn't find image.");
            }
        }
    }

    // Creates the NDF file
    private static void createNDFFile(String outputLocation, String name, ArrayList<Branch> branches) {
        File test = new File(outputLocation + name);

        try {
            FileWriter writer = new FileWriter(test);
            writer.write("// NeuronJ Data File - DO NOT CHANGE\n");
            writer.write("1.4.3\n");
            writer.write("// Parameters\n");
            writer.write("1\n1.0\n0.7\n2\n1100\n3\n5\n1\n");
            writer.write("// Type names and colors\n");
            writer.write("Default\n4\nAxon\n7\nDendrite\n1\nPrimary\n7\nSecondary\n1\nTertiary\n8\n");
            writer.write("Type 06\n4\nType 07\n4\nType 08\n4\nType 09\n4\nType 10\n4\n");
            writer.write("// Cluster names\nDefault\nCluster 01\nCluster 02\nCluster 03\nCluster 04\nCluster 05\nCluster 06\nCluster 07\nCluster 08\nCluster 09\nCluster 10\n");

            int i = 1;
            for (Branch b : branches) {
                writer.write("// Tracing N"+ i+"\n"+i+"\n0\n0\nDefault\n");
                writer.write("// Segment 1 of Tracing N"+i+"\n");
                for (Trips t : b.getPoints()) {
                    writer.write(t.x + "\n" + t.y+"\n");
                }
                i++;
            }
            writer.write("// End of NeuronJ Data File");
            writer.close();
        } catch (IOException e) {
            System.out.println("Error has occured");
        }
    }
}





























