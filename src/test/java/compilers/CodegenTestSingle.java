package compilers;

import java.io.*;

import visitor.*;

import java.util.Scanner;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;


/**
 *
 * @author richardburns
 */
public class CodegenTestSingle {

    @Test(timeout=20000)
    public void singleTest() throws FileNotFoundException, IOException, ParseException {
        String fileName = System.getProperty("fileName");
        String args[] = {fileName};

        InputStream is = new FileInputStream(new File(args[0]));
        RamParser parser = new RamParser(is);
        syntaxtree.Program root = parser.Goal();
                    
        System.out.println("AST Created ...");

        // build symbol table
        BuildSymbolTableVisitor v = new BuildSymbolTableVisitor();  
        root.accept(v); 
        System.out.println("Symbol Table built ...");
                    
        System.out.println("Generating Assembly Code ...");

        // prepare to capture System.output
        PrintStream originalOut = System.out;
        OutputStream os = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(os);
        System.setOut(ps);
        try {
            root.accept(new visitor.CodeGenerator(System.out, v.getSymTab()));
        } finally {
            // restore normal System.output operation
            System.setOut(originalOut);            
        }

        System.out.println("Saving Assembly File ...");
        PrintWriter writer = new PrintWriter(fileName+".s");
        writer.print(os + System.getProperty("line.separator"));
        writer.close();
                    
        System.out.println("Running Assembly File in MIPS Simulator ...");
        Process p = Runtime.getRuntime().exec("java -jar " + "lib/Mars4_5.jar" + " " + fileName+".s" + " " + "me");
        System.out.println("java -jar " + "lib/Mars4_5.jar" + " " + fileName+".s" + " " + "me");
        BufferedReader br1 = new BufferedReader(new InputStreamReader(p.getInputStream()));
        BufferedReader br2 = new BufferedReader(new InputStreamReader(p.getErrorStream()));

        System.out.println("Saving Assembly Output ...");
        writer = new PrintWriter(fileName+".s"+".output");
        String s;
        while ((s = br1.readLine()) != null)    // write stdout
        {
            writer.print(s + System.getProperty("line.separator"));
            System.out.println(s);
        }
        while ((s = br2.readLine()) != null)    // write stderr
        {
            // writer.print(s + System.getProperty("line.separator"));
            //System.err.println(s);
            System.err.print(s);
        }                    
        writer.close();

        System.out.println("Comparing Against Expected Output ...");
        // String s1 = org.apache.commons.io.FileUtils.readFileToString(new java.io.File(filePath+".correct"));
        // String s2 = org.apache.commons.io.FileUtils.readFileToString(new java.io.File(filePath+".py"+".output"));
        String s1 = new Scanner(new File(fileName+".correct")).useDelimiter("\\Z").next();
        String s2 = new Scanner(new File(fileName+".s"+".output")).useDelimiter("\\Z").next();
        s1 = s1.replaceAll("\\r\\n?", "\n");  // normalize line endings for Windows vs. Unix
        s2 = s2.replaceAll("\\r\\n?", "\n");
        assertEquals(s1, s2);         
    }

}
