package syntaxtree;
import visitor.Visitor;
import visitor.TypeVisitor;

public class Println extends Statement {
  public Exp e;

  public Println(Exp ae) {
    e=ae; 
  }

  public void accept(Visitor v) {
    v.visit(this);
  }

  public Type accept(TypeVisitor v) {
    return v.visit(this);
  }
}
