
package symboltable;


public class ErrorMsg {
    
    public boolean anyErrors = false;
    
    public void complain(String msg) {
        anyErrors = true;
        System.out.println(msg);
    }
    
}
