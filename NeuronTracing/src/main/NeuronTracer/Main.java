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
/*
    static ArrayList<Branch> mainTracer(int red, int xOffsetFromEye, int yOffsetFromEye, int startOfBranchRed, int sideToRemove, BufferedImage image, BufferedImage output, BufferedImage whiteOutput, BufferedImage blackOutput) {
        ArrayList<Pair> directions = new ArrayList<Pair>();
        Pair<Integer,Integer> N = new Pair<Integer,Integer>(0,-1);
        Pair<Integer,Integer> NE = new Pair<Integer,Integer>(1,-1);
        Pair<Integer,Integer> E = new Pair<Integer,Integer>(1,0);
        Pair<Integer,Integer> SE = new Pair<Integer,Integer>(1,1);
        Pair<Integer,Integer> S = new Pair<Integer,Integer>(0,1);
        Pair<Integer,Integer> SW = new Pair<Integer,Integer>(-1,1);
        Pair<Integer,Integer> W = new Pair<Integer,Integer>(-1,0);
        Pair<Integer,Integer> NW = new Pair<Integer,Integer>(-1,-1);
        //setting the direction array
        //start from index 1 through 8,  indexes 0 and 9 are extra for the plus 1, minus 1
        //if index == 0, set to 8 / if index == 9, set to 1
        directions.add(NW); directions.add(N); directions.add(NE); directions.add(E); directions.add(SE); directions.add(S); directions.add(SW); directions.add(W); directions.add(NW); directions.add(N);

        //--------------------------------------------------------------------------------------Start of finding the eye and branches
        //used to sort how the starting branches
        ArrayList<Trips> tempPoints = new ArrayList<Trips>();
        ArrayList<Trips> startingPoints = new ArrayList<Trips>();
        ArrayList<ArrayList<Trips>> listOfStartingPoints = new ArrayList<ArrayList<Trips>>();
        ArrayList<Trips> startingBranchPoints = new ArrayList<Trips>();

        //used to hold the full starting points for validation against the offset branches at the beginning
        ArrayList<Trips> knownPoints = new ArrayList<Trips>();

        //holds all the branches
        ArrayList<Branch> branches = new ArrayList<Branch>();

        int count = 0; int x = 0; int y = 0;
        boolean containsPink = false;
        //looking to see if the image has pink in it. If it does, we're assuming the user has highlighted the cell body with pink
        outerloop:
        for (y = 0; y < image.getHeight(); y++){
            for (x = 0; x < image.getWidth(); x++){
                if (ImageUtils.isPink(image.getRGB(x, y))) {
                    containsPink = true;
                    break outerloop;
                }
            }
        }

        ArrayList<Branch> startingBranches;
        //testing with (251,32,255) pink value
        //gets starting branches from the eye // offset when searching from the eye
        if (containsPink)
            startingBranches = getStartingBranchesPink(directions, knownPoints, image, output, xOffsetFromEye, yOffsetFromEye, startOfBranchRed, x, y);
        else
            startingBranches = getStartingBranches(directions, knownPoints, image, output, xOffsetFromEye, yOffsetFromEye, startOfBranchRed);


        if (sideToRemove != 0) {
            for (int i = 0; i < startingBranches.size(); i++) {
                if (startingBranches.get(i).getSide() == sideToRemove) {
                    startingBranches.remove(i);
                    i--;
                }
            }
        }

        //cleans up overlapping and repeated branches
        if (startingBranches.size() > 1)
            startingBranches = validateStartingBranches(startingBranches, image);

        for (Branch b : startingBranches) {
            b.printBranch(output, false);
            branches.add(b);
        }

        for (Branch i : startingBranches) {
            listOfStartingPoints.clear();
            int dir = 7;
            switch (i.getSide()) {
                case 1: dir = 7;
                    break;
                case 2: dir = 3;
                    break;
                case 3: dir = 1;
                    break;
                case 4: dir = 5;
                    break;
            }
            if (i.getSide() != 0) {
                startingBranchPoints = (findBranches(directions, null, i.getStartingPoint().x, i.getStartingPoint().y, dir,image, red, 0));
                knownPoints.addAll(startingBranchPoints);
                knownPoints.addAll(i.getPoints());

                //don't want to add the branch we want removed to the rest of the image, but we want to add it to the known points
                if (i.getSide() != sideToRemove) {
                    //going through each point on the found starting branch and trying to find separate branches
                    for (Trips point : startingBranchPoints) {
                        for (int d = 1; d <= 8; d++) {
                            tempPoints = findBranches(directions, knownPoints, point.x, point.y, d,image, red, 0);
                            if (tempPoints.size() > 20 && validateSeperateBranch(knownPoints, tempPoints)) {
                                listOfStartingPoints.add(tempPoints);
                            }
                        }
                    }

                    //removing overlapping offset branches
                    listOfStartingPoints = removeRepeatStartingPoints(listOfStartingPoints, image);

                    //populating list of main.NeuronTracer.Trips for starting points for the offsets
                    for (ArrayList<Trips> list : listOfStartingPoints) {
                        i.addBreakPoint(list.get(0));
                    }

                    //adding back the beginning points of the starting points we didn't want to find offsets for
                    i.addToPoints(startingBranchPoints);

                    //breaking up main branch based off starting points found from it
                    branches.addAll(i.breakUpBranch());
                }

            }
        }

        //--------------------------------------------------------------------------main.NeuronTracer.Main loop--------------------------------------------------------------------------
        ArrayList<Branch> branchesToBreak = new ArrayList<Branch>();
        ArrayList<Branch> nextRoundOfBranchesToBreak = new ArrayList<Branch>();

        branchesToBreak.addAll(branches);
        count = 0;

        do {
            //finding new branches from the existing ones
            while (!branchesToBreak.isEmpty()) {
                for (Trips s : branchesToBreak.get(0).getBreakPoints()) {
                    Branch tempB = new Branch(findBranches(directions, knownPoints, s.x, s.y, s.d,image, red, 0));
                    if (tempB.getPoints().size() > 5 && validateSeperateBranch(branchesToBreak.get(0).getBreakPoints(),tempB.getPoints())){
                        nextRoundOfBranchesToBreak.add(tempB); //temp array to hold set of branches for the next round
                        knownPoints.addAll(tempB.getPoints());
                    }
                }
                branchesToBreak.remove(0);
            }

            for (Branch b : nextRoundOfBranchesToBreak) {
                listOfStartingPoints.clear();

                for (Trips point : b.getPoints()) {
                    for (int d = 1; d <= 8; d++) {
                        tempPoints = findBranches(directions, knownPoints, point.x, point.y, d,image, red, 0);
                        if (tempPoints.size() > 20 && validateSeperateBranch(knownPoints, tempPoints)) {
                            listOfStartingPoints.add(tempPoints);
                        }
                    }
                }
                //removing overlapping offset branches
                listOfStartingPoints = removeRepeatStartingPoints(listOfStartingPoints, image);

                //populating list of main.NeuronTracer.Trips for starting points for the offsets
                for (ArrayList<Trips> list : listOfStartingPoints) {
                    b.addBreakPoint(list.get(0));
                }

                //breaking up main branch based off starting points found from it
                branches.addAll(b.breakUpBranch());
            }

            branchesToBreak.addAll(nextRoundOfBranchesToBreak);
            nextRoundOfBranchesToBreak.clear();
            count++;

        } while (!branchesToBreak.isEmpty());

        //removing tiny branches that somehow got in there
        removeTinyBranches(branches);

        //combining split branches
        mergeSplitBranches(branches);

        //just removing overlapping points
        removeOverlappingPoints(branches);


        int totalPixels = 0;
        double totalMicrons = 0;
        double averageMicrons = 0;
        int totalBranches = branches.size();

        for (Branch b : branches) {
            b.printBranch(output, false);
            b.printBranch(whiteOutput, false);
            b.printBranch(blackOutput, true);
            totalPixels += b.getPoints().size();
        }

        totalMicrons = totalPixels / 2.643;
        averageMicrons = totalMicrons / totalBranches;

        System.out.println("Using a scale of 2.643 pixels/micron.");
        System.out.println("Total number of branches: " + totalBranches);
        System.out.println("Total number of pixels: " + totalPixels);
        System.out.println("Total number of microns: " + totalMicrons);
        System.out.println("Average size of each branch: " + averageMicrons);

        return branches;
    }
*/
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

                Tracer tracer = new Tracer(red, xOffsetFromEye, yOffsetFromEye, startOfBranchRed, sideToRemove, image, output, whiteOutput, blackOutput);

                branches = tracer.createBranches();

                //getting file location to save the output files
                String outputLocation = fileLocation.substring(0, fileLocation.indexOf(".jpg"));

                //--------------------------------------------------------------------------------------Saving Files
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
}





























