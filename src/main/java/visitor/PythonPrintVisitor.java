package visitor;

import syntaxtree.*;

public class PythonPrintVisitor implements Visitor {

    int tabs;
    boolean classLevel;
    boolean inCall;
    java.util.List<String> currentLocals;

    
    public PythonPrintVisitor() {
        tabs = 0;
        classLevel = false;
        inCall = false;
        currentLocals = new java.util.LinkedList<String>();
    }
    
    
    public void printTabs() {
        for (int i = 1; i <= tabs; i++)
            System.out.print("\t");
    }
    
  // MainClass m;
  // ClassDeclList cl;
  public void visit(Program n) {
    for ( int i = 0; i < n.cl.size(); i++ ) {
        System.out.println();
        n.cl.elementAt(i).accept(this);
    }
    n.m.accept(this);   // print main at end, after classes
  }
  
  // Identifier i1,i2;
  // Statement s;
  public void visit(MainClass n) {
    System.out.print("if __name__ == '__main__':\n");
    tabs++;
    n.s.accept(this);
    tabs--;
    System.out.print("\n");
  }

  // Identifier i;
  // VarDeclList vl;
  // MethodDeclList ml;
  public void visit(ClassDeclSimple n) {
    System.out.print("class ");
    inCall = true;
    n.i.accept(this);
    inCall = false;
    System.out.println("():");
    tabs++;
    
    if (n.vl.size() > 0) {
        printTabs();
        classLevel = true;
        System.out.println("def __init__ (self):");
        tabs++;
        for ( int i = 0; i < n.vl.size(); i++ ) {
            printTabs();
            n.vl.elementAt(i).accept(this);
            if ( i+1 < n.vl.size() ) { System.out.println(); }
        }
        tabs--;
        classLevel = false;
    }
    
    for ( int i = 0; i < n.ml.size(); i++ ) {
        System.out.println();
        n.ml.elementAt(i).accept(this);
    }
    System.out.println();
    tabs--;
  }
 
  // Identifier i;
  // Identifier j;
  // VarDeclList vl;
  // MethodDeclList ml;
  public void visit(ClassDeclExtends n) { }

  // Type t;
  // Identifier i;
  public void visit(VarDecl n) {
    
    if (classLevel) {
        n.i.accept(this);       // only declare class level variables
        System.out.print(" = None");
    } 
  }

  // Type t;
  // Identifier i;
  // FormalList fl;
  // VarDeclList vl;
  // StatementList sl;
  // Exp e;
  public void visit(MethodDecl n) {
    printTabs();
    System.out.print("def ");
    inCall = true;
    n.i.accept(this);
    System.out.print("(self");
    for ( int i = 0; i < n.fl.size(); i++ ) {
        System.out.print(", ");
        n.fl.elementAt(i).accept(this);
        currentLocals.add(n.fl.elementAt(i).i.s);
        // if (i+1 < n.fl.size()) { System.out.print(", "); }
    }
    System.out.println("):");
    inCall = false;
    tabs++;
    
    for ( int i = 0; i < n.vl.size(); i++ ) {
        currentLocals.add(n.vl.elementAt(i).i.s);
    }
    
    for ( int i = 0; i < n.sl.size(); i++ ) {
        n.sl.elementAt(i).accept(this);
        if ( i < n.sl.size() ) { System.out.println(""); }
    }
    printTabs();
    System.out.print("return ");
    n.e.accept(this);
    tabs--;
    currentLocals.clear();
  }

  // Type t;
  // Identifier i;
  public void visit(Formal n) {
    //n.t.accept(this);
    //System.out.print(" ");
    n.i.accept(this);
  }

  public void visit(IntArrayType n) {  }

  public void visit(BooleanType n) { }
   
  public void visit(IntegerType n) { }

  // String s;
  public void visit(IdentifierType n) {
    System.out.print(n.s);
  }

  // StatementList sl;
  public void visit(Block n) {
    for ( int i = 0; i < n.sl.size(); i++ ) {
        n.sl.elementAt(i).accept(this);
        System.out.println();
    }
  }

  // Exp e;
  // Statement s1,s2;
  public void visit(If n) {
    printTabs();
    System.out.print("if ");
    n.e.accept(this);
    System.out.println(":");
    tabs++;
    n.s1.accept(this);
    System.out.println();
    tabs--;
    printTabs();
    System.out.println("else:");
    tabs++;
    n.s2.accept(this);
    System.out.println();
    tabs--;
  }

  // Exp e;
  // Statement s;
  public void visit(While n) {
    printTabs();
    System.out.print("while ");
    n.e.accept(this);
    System.out.println(":");
    tabs++;
    n.s.accept(this);
    tabs--;
  }
  

  // ExpList el;
  public void visit(Print n) {
    printTabs();
    System.out.print("print (");
    n.e.accept(this);
    /*
    for (int i = 0; i < n.el.size(); i++ ) {
        n.el.elementAt(i).accept(this);
        if (i != n.el.size() - 1)
            System.out.print(", ");
    }
    */
    // System.out.print(",");            // print without newline in python 2.x
    System.out.println(",end=\"\")");    // print without newline in python 3.x
  }
  
  // ExpList el;
  public void visit(Println n) {
    printTabs();
    System.out.print("print (");
    n.e.accept(this);
    /*
    for (int i = 0; i < n.el.size(); i++ ) {
        n.el.elementAt(i).accept(this);
        if (i != n.el.size() - 1)
            System.out.print(", ");
    } 
    */
    System.out.println(")");
  }  
  
  // Identifier i;
  // Exp e;
  public void visit(Assign n) {
    printTabs();
    n.i.accept(this);
    System.out.print(" = ");
    n.e.accept(this);
  }

  // Identifier i;
  // Exp e1,e2;
  public void visit(ArrayAssign n) {
    printTabs();
    n.i.accept(this);
    System.out.print("[");
    n.e1.accept(this);
    System.out.print("] = ");
    n.e2.accept(this);
  }

  // Exp e1,e2;
  public void visit(And n) {
    System.out.print("(");
    n.e1.accept(this);
    System.out.print(" and ");
    n.e2.accept(this);
    System.out.print(")");
  }
  
  // Exp e1,e2;
  public void visit(Or n) {
    System.out.print("(");
    n.e1.accept(this);
    System.out.print(" or ");
    n.e2.accept(this);
    System.out.print(")");
  }  

  // Exp e1,e2;
  public void visit(LessThan n) {
    System.out.print("(");
    n.e1.accept(this);
    System.out.print(" < ");
    n.e2.accept(this);
    System.out.print(")");
  } 
  
  // Exp e1,e2;
  public void visit(GreaterThan n) {
    System.out.print("(");
    n.e1.accept(this);
    System.out.print(" > ");
    n.e2.accept(this);
    System.out.print(")");
  }
  
  // Exp e1,e2;
  public void visit(LessThanOrEqualTo n) {
    System.out.print("(");
    n.e1.accept(this);
    System.out.print(" <= ");
    n.e2.accept(this);
    System.out.print(")");
  } 
  
  // Exp e1,e2;
  public void visit(GreaterThanOrEqualTo n) {
    System.out.print("(");
    n.e1.accept(this);
    System.out.print(" >= ");
    n.e2.accept(this);
    System.out.print(")");
  }
  
  // Exp e1,e2;
  public void visit(Equals n) {
    System.out.print("(");
    n.e1.accept(this);
    System.out.print(" == ");
    n.e2.accept(this);
    System.out.print(")");
  } 
  
  // Exp e1,e2;
  public void visit(NotEquals n) {
    System.out.print("(");
    n.e1.accept(this);
    System.out.print(" != ");
    n.e2.accept(this);
    System.out.print(")");
  } 

  // Exp e1,e2;
  public void visit(Plus n) {
    System.out.print("(");
    n.e1.accept(this);
    System.out.print(" + ");
    n.e2.accept(this);
    System.out.print(")");
  }
  
  // Identifier i;
  // Exp e;
  public void visit(PlusEquals n) {
    printTabs();      
    n.i.accept(this);
    System.out.print(" += ");
    n.e.accept(this);
  }  

  // Exp e1,e2;
  public void visit(Minus n) {
    System.out.print("(");
    n.e1.accept(this);
    System.out.print(" - ");
    n.e2.accept(this);
    System.out.print(")");
  }
  
  // Identifier i;
  // Exp e;
  public void visit(MinusEquals n) {
    printTabs();      
    n.i.accept(this);
    System.out.print(" -= ");
    n.e.accept(this);
  } 

  // Exp e1,e2;
  public void visit(Times n) {
    System.out.print("(");
    n.e1.accept(this);
    System.out.print(" * ");
    n.e2.accept(this);
    System.out.print(")");
  }

  // Exp e1,e2;
  public void visit(ArrayLookup n) {
    n.e1.accept(this);
    System.out.print("[");
    n.e2.accept(this);
    System.out.print("]");
  }

  // Exp e;
  public void visit(ArrayLength n) {
    System.out.print("len(");
    n.e.accept(this);
    System.out.print(")");
  }

  // Exp e;
  // Identifier i;
  // ExpList el;
  public void visit(Call n) {
    n.e.accept(this);
    System.out.print(".");
    inCall = true;
    n.i.accept(this);
    inCall = false;
    System.out.print("(");
    for ( int i = 0; i < n.el.size(); i++ ) {
        n.el.elementAt(i).accept(this);
        if ( i+1 < n.el.size() ) { System.out.print(", "); }
    }
    System.out.print(")");
  }

  // int i;
  public void visit(IntegerLiteral n) {
    System.out.print(n.i);
  }

  public void visit(True n) {
    System.out.print("True");
  }

  public void visit(False n) {
    System.out.print("False");
  }

  // String s;
  public void visit(IdentifierExp n) {
    if (!currentLocals.contains(n.s) && !inCall)
        System.out.print("self.");      
    System.out.print(n.s);
  }

  public void visit(This n) {
    System.out.print("self");
  }

  // Exp e;
  public void visit(NewArray n) {
    System.out.print("[None]*");
    //System.out.print("new int [");
    n.e.accept(this);
    //System.out.print("]");
  }

  // Identifier i;
  public void visit(NewObject n) {
    System.out.print(n.i.s);
    System.out.print("()");
  }

  // Exp e;
  public void visit(Not n) {
    System.out.print("not");
    System.out.print(" ");
    n.e.accept(this);
  }

  // String s;
  public void visit(Identifier n) {
    if (!currentLocals.contains(n.s) && !inCall)
        System.out.print("self.");
    System.out.print(n.s);
  }
}
