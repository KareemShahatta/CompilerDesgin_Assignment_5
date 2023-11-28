package visitor;
import syntaxtree.*;
import symboltable.*;
import java.util.Enumeration;


public class CodeGenerator extends DepthFirstVisitor {
    private int labelCounter = 0;
    private java.io.PrintStream out;
    private static int nextLabelNum = 0;
    private Table symTable;  
    
    private StringBuilder dataString = new StringBuilder("");
    
    public CodeGenerator(java.io.PrintStream o, Table st) {
        out = o; 
        symTable = st;
    }

    private void emit(String s) {
        out.println("\t" + s);
    }

    private void emitLabel(String l) {
        out.println(l + ":");
    }
    
    private void emitComment(String s) {
        out.println("\t" + "#" + s);
    }
    
    // MainClass m;
    // ClassDeclList cl;
    // NOTE I added an extra portion here to define the space label ("/n")
    public void visit(Program n) {

        emit(".data");
        emit("space: .asciiz \"\\n\"");
        emit(".text");
        emit(".globl main");
        
        n.m.accept(this);
        for ( int i = 0; i < n.cl.size(); i++ ) {
            n.cl.elementAt(i).accept(this);
        }
        
        emit("");
        emit(".data");
        out.println(dataString.toString());
    }
    
    // Identifier i1, i2;
    // Statement s;
    public void visit(MainClass n) {
        symTable.addClass(n.i1.toString());
        TypeCheckVisitor.currClass = symTable.getClass(n.i1.toString());
        symTable.getClass(n.i1.s).addMethod("main", new IdentifierType("void"));
        TypeCheckVisitor.currMethod = symTable.getClass(n.i1.toString()).getMethod("main");
        symTable.getMethod("main", 
                TypeCheckVisitor.currClass.getId()).addParam(n.i2.toString(), new IdentifierType("String[]"));

        emitLabel("main");
        
        emitComment("begin prologue -- main");
        emit("subu $sp, $sp, 24    # stack frame is at least 24 bytes");
        emit("sw $fp, 4($sp)       # save caller's frame pointer");
        emit("sw $ra, 0($sp)       # save return address");
        
        emit("addi $fp, $sp, 20    # set up main's frame pointer");       
        emitComment("end prologue -- main");
        
        n.s.accept(this);
        
        emitComment("begin epilogue -- main");
        emit("lw $ra, 0($sp)       # restore return address");
        emit("lw $fp, 4($sp)       # restore caller's frame pointer");
        emit("addi $sp, $sp, 24    # pop the stack"); 
        emitComment("end epilogue -- main");
        
        /*
        emit("jr $ra");   // SPIM: how to end programs
        emit("\n");       // SPIM: how to end programs 
        */
        
        emit("li $v0, 10");   // MARS: how to end programs
        emit("syscall");      // MARS: how to end programs
        
        TypeCheckVisitor.currMethod = null;
        
    }

    // int i;
    public void visit(IntegerLiteral n) {
        emit("li $v0, "+n.i+"            # load literal "+n.i+" into $v0");
    }

    // Exp e;
    public void visit(Print n)
    {
        n.e.accept(this); //Call IntegerLiteral and stores the value in $v0
        emit("move $a0, $v0        # move value from $v0 to $a0");
        emit("li $v0, 1            # load the literal 1 into $v0");
        emit("syscall");
    }

    // Exp e;
    public void visit(Println n)
    {
        n.e.accept(this); //Call IntegerLiteral and stores the value in $v0
        emit("move $a0, $v0        # move value from $v0 to $a0");
        emit("li $v0, 1            # load the literal 1 into $v0");
        emit("syscall");

        emit("li $v0, 4            # set type of print to be string");
        emit("la $a0, space        # load the space label onto first argument to print");
        emit("syscall");
    }

    // Exp e1;
    // Exp e2;
    public void visit(Plus n)
    {
        n.e1.accept(this); //Call IntegerLiteral and stores the value in $v0
        emit("sub  $sp, $sp ,4     # add 1 word to the stack (PUSH)");
        emit("sw $v0, ($sp)        # saves the value of $v0 in the stack");

        n.e2.accept(this); //Call IntegerLiteral and stores the value in $v0
        emit("lw $v1, ($sp)        # loads the value of $v1 from the stack");
        emit("add  $sp, $sp ,4     # remove 1 word to the stack (POP)");
        emit("add $v0 $v1, $v0     # adds the value of $v0 and $v1 and saves it in $v0");
    }

    // Exp e1;
    // Exp e2;
    public void visit(Minus n)
    {
        n.e1.accept(this); //Call IntegerLiteral and stores the value in $v0
        emit("sub  $sp, $sp ,4     # add 1 word to the stack (PUSH)");
        emit("sw $v0, ($sp)        # saves the value of $v0 in the stack");

        n.e2.accept(this); //Call IntegerLiteral and stores the value in $v0
        emit("lw $v1, ($sp)        # loads the value of $v1 from the stack");
        emit("add  $sp, $sp ,4     # remove 1 word to the stack (POP)");
        emit("sub $v0 $v1, $v0     # subtract the value of $v0 and $v1 and saves it in $v0");
    }

    // Exp e1;
    // Exp e2;
    public void visit(Times n)
    {
        n.e1.accept(this); //Call IntegerLiteral and stores the value in $v0
        emit("add  $sp, $sp ,4     # shift the stack one block to the right (PUSH)");
        emit("sw $v0, ($sp)        # saves the value of $v0 in the stack");

        n.e2.accept(this); //Call IntegerLiteral and stores the value in $v0
        emit("lw $v1, ($sp)        # loads the value of $v1 from the stack");
        emit("sub  $sp, $sp ,4     # shift the stack one block to the left (POP)");
        emit("mul $v0 $v1, $v0     # multiply the value of $v0 and $v1 and saves it in $v0");
    }

    // true 1;
    public void visit(True n) {
        emit("li $v0, 1            # load true value 1 into $v0");
    }

    // false 0;
    public void visit(False n) {
        emit("li $v0, 0            # load false value 0 into $v0");
    }

    // Exp e;
    // Stm s1;
    // Stm s2;
    public void visit(If n)
    {
        int value = ++labelCounter;

        n.e.accept(this); //Call IntegerLiteral and stores the value in $v0
        emit("blez $v0 If_False_" + value + "  # Check if $v0 is false then execute false block"); //(BLEZ) Branch Less or Equal Zero

        n.s1.accept(this); //writes the true block
        emit("jal If_Done_" + value + "           # Jumps to label If_Done");

        emitLabel("If_False_" + value);
        n.s2.accept(this); //writes the false block

        emitLabel("If_Done_" + value);
    }

    // Exp e1;
    // Exp e2;
    public void visit(And n)
    {
        int value = ++labelCounter;

        n.e1.accept(this); // Call IntegerLiteral and stores the value in $v0
        emit("blez $v0 And_False_" + value + "     # Check if $v0 is false then short circuit and ignore 2nd expression");
        n.e2.accept(this); // Continue evaluation

        emitLabel("And_False_"  + value);
    }

    // Exp e1;
    // Exp e2;
    public void visit(Or n)
    {
        int value = ++labelCounter;

        n.e1.accept(this); // Call IntegerLiteral and stores the value in $v0
        emit("blez $v0 Or_False_" + value + "     #Check if $v0 is false to continue to 2nd statement of or");
        emit("jal Or_Done_" + value + "           # Jumps ot label isDone");

        emitLabel("Or_False_"  + value);
        n.e2.accept(this); // Continue evaluation

        emitLabel("Or_Done_" + value);
    }

    // Exp e1;
    // Exp e2;
    public void visit(LessThan n)
    {
        int value = ++labelCounter;

        n.e1.accept(this); //Call IntegerLiteral and stores the value in $v0
        emit("sub  $sp, $sp ,4     # add 1 word to the stack (PUSH)");
        emit("sw $v0, ($sp)         # saves the value of $v0 in the stack");

        n.e2.accept(this); //Call IntegerLiteral and stores the value in $v0
        emit("lw $v1, ($sp)        # loads the value of $v1 from the stack");
        emit("add  $sp, $sp ,4     # remove 1 word to the stack (POP)");

        emit("blt $v1 $v0 LessThan_True_" + value + "   #Check if $v1 is less than $v0 (e1 < e2)");
        emit("li $v0, 0            # loads the value of $v0 to be 0 (false)");
        emit("jal LessThan_Done_" + value + "  # Jumps to label LessThan_Done");

        emitLabel("LessThan_True_"  + value);
        emit("li $v0, 1            # loads the value of $v0 to be 1 (true)");

        emitLabel("LessThan_Done_" + value);
    }

    // Exp e1;
    // Exp e2;
    public void visit(Equals n)
    {
        int value = ++labelCounter;

        n.e1.accept(this); //Call IntegerLiteral and stores the value in $v0
        emit("sub  $sp, $sp ,4     # add 1 word to the stack (PUSH)");
        emit("sw $v0, ($sp)        # saves the value of $v0 in the stack");

        n.e2.accept(this); //Call IntegerLiteral and stores the value in $v0
        emit("lw $v1, ($sp)        # loads the value of $v1 from the stack");
        emit("add  $sp, $sp ,4     # remove 1 word to the stack (POP)");

        emit("beq $v1 $v0 Equals_True_" + value + "   #check if $v1 is equal to $v0 (e1 = e2)");
        emit("li $v0, 0            # loads the value of $v0 to be 0 (false)");
        emit("jal Equals_Done_" + value + "           # jumps to label Equals_Done");

        emitLabel("Equals_True_"  + value);
        emit("li $v0, 1            # loads the value of $v0 to be 1 (true)");

        emitLabel("Equals_Done_" + value);
    }

    // Exp e;
    public void visit(Not n)
    {
        int value = ++labelCounter;
        n.e.accept(this); //Call IntegerLiteral and stores the value in $v0
        emit("blez $v0 Not_Switch_True_" + value + "   #check the value of $v1 to switch it correctly");
        emit("li $v0, 0            # loads the value of $v0 to be 0 (false)");
        emit("jal Not_SwitchDone_" + value + "           # jumps to label Not_SwitchDone");

        emitLabel("Not_Switch_True_"  + value);
        emit("li $v0, 1            # loads the value of $v0 to be 1 (true)");

        emitLabel("Not_SwitchDone_" + value);
    }

    // Exp e;
    // Identifier i;
    // ExpList el;
    public void visit(Call n)
    {
        emitComment("Preparing to call method " +n.i.toString());

        int args = 3;
        while(args >= 0)
        {
            emit("sub  $sp, $sp ,4     # add 1 word to the stack (PUSH)");
            emit("sw $a" + args + ", ($sp)        # saves the value of $a" + args + " in the stack");
            args--;
        }

        emit("jal " + n.i.toString());
    }

    // Exp e;
    // Type t;
    // Identifier i;
    // FormalList fl;
    // VarDecList sl;
    // StatementList sl;
    public void visit(MethodDecl n)
    {
        String method = n.i.toString();
        int additionalValue = (n.fl.size()* 4) + 24;

        emitLabel(method);

        emitComment("begin prologue -- " + method);
        emit("subu $sp, $sp, " + additionalValue + "    # new stack frame has a value of " + additionalValue+ " bytes");
        emit("sw $fp, 4($sp)       # save caller's frame pointer");
        emit("sw $ra, 0($sp)       # save return address");
//        emit("addi $fp, $sp, 20    # set up main's frame pointer");
        emitComment("end prologue -- " + method);

        n.e.accept(this); //return int?

        emitComment("begin epilogue -- " + method);
        emit("lw $ra, 0($sp)       # restore return address");
        emit("lw $fp, 4($sp)       # restore caller's frame pointer");
        emit("addi $sp, $sp, " + additionalValue + "    # pop the stack");
        emitComment("end epilogue -- " + method);

        emit("jr $ra");
    }

    // Identifier i;
    // MethodList fl;
    // VarDecList vl;
    public void visit(ClassDeclSimple n)
    {
        for(int i = 0 ; i < n.ml.size() ; i++)
        {
            n.ml.elementAt(i).accept(this);
        }

        TypeCheckVisitor.currClass = symTable.getClass(n.i.toString());
    }

    /*//@TODO MAYBE THIS CODE NEED REWORK FOR (add $fp, $fp, $v0)
    String s;
    public void visit(Identifier n)
    {
        System.out.println("Identifier: " + n.toString());
        System.out.println("Identifier String: " + n.s);

        //n.accept(this);  // Gets the Identifier's value from the symbol table and stores in $v0

        emit("add  $fp, $v0, $fp        # calculate the address of the identifier by adding it to the frame pointer");

    }

    //@TODO MAYBE THIS CODE NEED REWORK FOR (add $fp, $fp, $v0)
    // String s;
    public void visit(IdentifierExp n)
    {
        n.accept(this);  // Gets the Identifier's value from the symbol table and stores in $v0

        emit("add  $fp, $v0, $fp        # calculate the address of the identifier by adding it to the frame pointer");
        emit("lw  $v0, ($fp)            # load the value of $fp into $v0");

    }

    // Exp e;
    // Identifier i;
    public void visit(Assign n)
    {
        n.e.accept(this); // Compute the right hand side expression and stores the value in $v0

        emit("add  $sp, $sp ,4     # shift the stack one block to the right (PUSH)");
        emit("sw $v0, ($sp)        # saves the value of $v0 in the stack");

        n.i.accept(this); // Compute the identifier and stores the value in $v0


        emit("lw $v1, ($sp)        # loads the value of $v1 from the stack");
        emit("sub  $sp, $sp ,4     # shift the stack one block to the left (POP)");

        emit("sw $v1 ($v0)     # saves the value of the expression into the identifier");
    }*/
}
