package symboltable;import syntaxtree.Type;public class RamVariable {    String id;    Type type;    public RamVariable(String id, Type type) {        this.id = id;        this.type = type;    }    public String getId() {        return id;    }    public Type type() {        return type;    }        public String toString()     {        if (type instanceof syntaxtree.IdentifierType)            return ((syntaxtree.IdentifierType) type).s + " " + id;        else            return type.getClass() + " " + id;    }} // Variable