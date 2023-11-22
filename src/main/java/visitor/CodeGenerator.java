package visitor;
import syntaxtree.*;
import symboltable.*;
import java.util.Enumeration;


public class CodeGenerator extends DepthFirstVisitor {

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
    public void visit(Program n) {
        
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
    
    // TODO
    // Exp e;
    public void visit(Print n) {

    }
    

    // int i;
    public void visit(IntegerLiteral n) {
        emit("li $v0, "+n.i+"        # load literal "+n.i+" into $v0");
    }    
    
}
