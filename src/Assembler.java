import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Assembler {

    // Define the register mapping
    private static final HashMap<String, Integer> registerMap = new HashMap<String, Integer>() {{
        put("$t0", 0);
        put("$S0", 1);
        put("$S1", 2);
        put("$S2", 3);
    }};

    private static final HashMap<String, String> opCodeMap = new HashMap<String, String>() {{
        put("ADD", "0000");
        put("SUB", "0001");
        put("AND", "0010");
        put("ADDi", "0011");
        put("sll", "0100");
        put("lw", "0101");
        put("sw", "0110");
        put("J", "0111");
        put("Beq", "1000");
        put("slt", "1001");
        put("slti", "1010");
        put("IN", "1011");
        put("OUT", "1100");
    }};

    public static void main(String[] args) {

        List<String> instructions = new ArrayList<>();

        String fileName = "input.txt";

        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null)
            {
                //System.out.println(line);
                instructions.add(line);
            }
        }
        catch (Exception e)
        {
            System.out.println("File could not be read: " + fileName);;
        }

        // Parse the assembly code and translate it into machine code
        List<String> machineCode = new ArrayList<>();


        for (String instruction : instructions) {
            machineCode.add(generateMachineCode(instruction));
        }

        String outputFileName = "Output.txt";
        writeMachineCode(machineCode, outputFileName);
    }

    private static String generateMachineCode(String instruction) {

        String str = instruction.replaceAll("[(),]", " ");

        String[] tokens = str.split("\\s+");

        // Parse the instruction and its arguments
        String opcode = tokens[0];

        int[] args = new int[tokens.length - 1];
        for (int i = 1; i < tokens.length; i++) {
            args[i - 1] = parseArgument(tokens[i]);
        }

        String op = parseOpCode(opcode);

        // Generate the machine code for the instruction

        if (opcode.equals("ADD") || opcode.equals("SUB") || opcode.equals("AND") || opcode.equals("slt"))
        {
            String rs = String.format("%2s", Integer.toBinaryString(args[2])).replaceAll(" ", "0");
            String rt = String.format("%2s", Integer.toBinaryString(args[1])).replaceAll(" ", "0");
            String rd = String.format("%2s", Integer.toBinaryString(args[0])).replaceAll(" ", "0");

            return (op + rs + rt + rd);
        }
        else if (opcode.equals("ADDi") || opcode.equals("sll") || opcode.equals("Beq") || opcode.equals("slti"))
        {
            String rs = String.format("%2s", Integer.toBinaryString(args[1])).replaceAll(" ", "0");
            String rd = String.format("%2s", Integer.toBinaryString(args[0])).replaceAll(" ", "0");
            String imm = String.format("%2s", Integer.toBinaryString(args[2])).replaceAll(" ", "0");

            return (op + rs + rd + imm);
        }
        else if (opcode.equals("lw") || opcode.equals("sw")) {
            String rd = String.format("%2s", Integer.toBinaryString(args[0])).replaceAll(" ", "0");
            String offset = String.format("%2s", Integer.toBinaryString(args[1])).replaceAll(" ", "0");
            String rs = String.format("%2s", Integer.toBinaryString(args[2])).replaceAll(" ", "0");

            return (op + rs + rd + offset);
        }
        else if (opcode.equals("J"))
        {
            String target = String.format("%6s", Integer.toBinaryString(args[0])).replaceAll(" ", "0");

            return (op + target);
        }
        else if (opcode.equals("IN"))
        {
            String rd = String.format("%2s", Integer.toBinaryString(args[0])).replaceAll(" ", "0");

            return (op + "00" + rd + "00");
        }
        else if (opcode.equals("OUT"))
        {
            String rs = String.format("%2s", Integer.toBinaryString(args[0])).replaceAll(" ", "0");

            return (op + rs + "0000");
        }
        else
        {
            throw new IllegalArgumentException("Invalid instruction: " + instruction);
        }
    }

    private static int parseArgument(String arg) {

        //System.out.println(arg);
        if (arg.startsWith("$"))
        {
            // Register argument
            if (registerMap.containsKey(arg)) {
                return registerMap.get(arg);
            }
            else
            {
                throw new IllegalArgumentException("Invalid register: " + arg);
            }
        }
        else if (arg.matches("-?\\d+"))
        {
            // Decimal immediate value
            return Integer.parseInt(arg);
        }
        else
        {
            throw new IllegalArgumentException("Invalid argument: " + arg);
        }
    }

    private static String parseOpCode(String arg) {
        if (!opCodeMap.containsKey(arg))
        {
            throw new IllegalArgumentException("Invalid OpCode: " + arg);
        }

        return String.format("%4s", opCodeMap.get(arg)).replaceAll(" ", "0") ;
    }

    private static void writeMachineCode(List<String> machineCode, String fileName) {
        try (PrintWriter writer = new PrintWriter(new FileOutputStream(fileName))) {
            for (String binary : machineCode) {
                int decimal = Integer.parseInt(binary, 2);
                System.out.println(binary);
                String hex = String.format("%3s", Integer.toHexString(decimal)).replaceAll(" ", "0");
                writer.println(hex);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}