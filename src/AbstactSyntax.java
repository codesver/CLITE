import java.util.*;

class Program {
    // Program = Declarations globals ; Functions functions
    Declarations globals;
    Functions functions;

    public Program(Declarations globals, Functions functions) {
        this.globals = globals;
        this.functions = functions;
    }

    public void display(int k) {
        for (int w = 0; w < k; ++w) System.out.print("\t");
        System.out.println("Program : ");
        globals.display(++k);
        functions.display(k);
    }
}

class Functions extends ArrayList<Function> {
    public void display(int k) {
        for (int w = 0; w < k; ++w) System.out.print("\t");
        System.out.println("Functions : ");
        for (Function function : this) {
            function.display(k + 1);
            System.out.println();
        }
    }
}

class Function {
    Type type;
    String id;
    Declarations params, locals;
    Block body;

    public Function(Type type, String id, Declarations params, Declarations locals, Block body) {
        this.type = type;
        this.id = id;
        this.params = params;
        this.locals = locals;
        this.body = body;
    }

    public void display(int k) {
        for (int w = 0; w < k; ++w) System.out.print("\t");
        System.out.println("Function = " + id + "; Return type = " + type.getId());
        k++;
        for (int w = 0; w < k; ++w) System.out.print("\t");
        System.out.println("Parameters : ");
        params.display(k + 1);
        for (int w = 0; w < k; ++w) System.out.print("\t");
        System.out.println("Locals : ");
        locals.display(k + 1);
        body.display(k);
    }
}


class Declarations extends ArrayList<Declaration> {
    // Declarations = Declaration*

    public void display(int k) {
        for (int w = 0; w < k; ++w) System.out.print("\t");
        System.out.println("Declarations: ");
        for (int w = 0; w < k; ++w) System.out.print("\t");
        System.out.print("\tDeclaration = ");
        for (int i = 0; i < size(); i++) get(i).display();
        System.out.println("");
    }
}

abstract class Declaration {
    // Declaration = VariableDeclare
    Variable variable;
    Type type;

    public void display() {
    }
}

class VariableDeclare extends Declaration {
    // VariableDecl = Variable v; Type t

    VariableDeclare() {
    }

    VariableDeclare(Variable variable, Type type) {
        this.variable = variable;
        this.type = type;
    }

    VariableDeclare(String id, Type type) {
        variable = new Variable(id);
        this.type = type;
    }

    public void display() {
        System.out.print("<" + variable + ", " + type.getId() + ">");
    }
}

class Type {
    // Type = int | bool | char | float | void
    final static Type INT = new Type("int");
    final static Type BOOL = new Type("bool");
    final static Type FLOAT = new Type("float");
    final static Type CHAR = new Type("char");
    final static Type VOID = new Type("void");

    protected String id;

    protected Type(String t) {
        id = t;
    }

    public String getId() {
        return id;
    }

}

abstract class Statement {
    // Statement = Skip | Block | Assignment | Conditional | Loop | Return | Call
    public void display(int k) {
        for (int w = 0; w < k; ++w) System.out.print("\t");
        System.out.println(getClass().toString().substring(6) + " : ");
    }
}

class Skip extends Statement {
    public void display(int k) {
        super.display(k);
    }
}

class Block extends Statement {
    // Block = Statement*
    public ArrayList<Statement> statements = new ArrayList<Statement>();

    public void display(int k) {
        super.display(k);
        for (Statement statement : statements)
            statement.display(k + 1);
    }
}

class Assignment extends Statement {
    // Assignment = VariableRef target; Expression source
    VariableRef target;
    Expression source;

    public Assignment(VariableRef target, Expression source) {
        this.target = target;
        this.source = source;
    }

    public void display(int k) {
        super.display(k);
        target.display(k + 1);
        source.display(k + 1);
    }
}

class Conditional extends Statement {
    // Conditional = Expression test; Statement thenBranch, elseBranch
    Expression test;
    Statement thenBranch, elseBranch;

    public Conditional(Expression test, Statement thenBranch) {
        this.test = test;
        this.thenBranch = thenBranch;
        elseBranch = new Skip();
    }

    public Conditional(Expression test, Statement thenBranch, Statement elseBranch) {
        this.test = test;
        this.thenBranch = thenBranch;
        this.elseBranch = elseBranch;
    }

    public void display(int k) {
        super.display(k);
        test.display(k + 1);
        thenBranch.display(k + 1);
        assert elseBranch != null : "else branch cannot be null";
        elseBranch.display(k + 1);
    }
}

class Loop extends Statement {
    // Loop = Expression test; Statement body
    Expression test;
    Statement body;

    public Loop(Expression test, Statement body) {
        this.test = test;
        this.body = body;
    }

    public void display(int k) {
        super.display(k);
        test.display(k + 1);
        body.display(k + 1);
    }
}

class Return extends Statement {
    Variable target;
    Expression retVal;

    public Return(Variable target, Expression retVal) {
        this.target = target;
        this.retVal = retVal;
    }

    public void display(int k) {
        super.display(k);
        target.display(k + 1);
        retVal.display(k + 1);
    }
}

class CallStatement extends Statement {
    String id;
    Expressions args;

    public CallStatement(String id, Expressions args) {
        this.id = id;
        this.args = args;
    }

    public void display(int k) {
        for (int w = 0; w < k; ++w) System.out.print("\t");
        System.out.println(getClass().toString().substring(6) + ": " + id);
        args.display(k + 1);
    }
}

class Expressions extends ArrayList<Expression> {
    public void display(int k) {
        for (int w = 0; w < k; ++w) System.out.print("\t");
        System.out.println("Arguments : ");
        for (Expression exp : this)
            exp.display(k + 1);
    }
}

abstract class Expression {
    // Expression = VariableRef | Value | Binary | Unary | Call

    public void display(int k) {
        for (int w = 0; w < k; ++w) System.out.print("\t");
        System.out.print(getClass().toString().substring(6) + " : ");
    }
}

abstract class VariableRef extends Expression {
    // VariableRef = Variable
    String id;

    public String id() {
        return id;
    }

    public void display(int k) {
        super.display(k);
    }
}

class Variable extends VariableRef {
    // Variable = String id

    Variable(String s) {
        id = s;
    }

    public String toString() {
        return id;
    }

    public boolean equals(Object obj) {
        String s = ((Variable) obj).id;
        return id.equals(s);
    }

    public int hashCode() {
        return id.hashCode();
    }

    public void display(int k) {
        super.display(k);
        System.out.println(id);
    }
}


abstract class Value extends Expression {
    // Value = IntValue | BoolValue | CharValue | FloatValue
    protected Type type;
    protected boolean undef = true;

    int intValue() {
        assert false : "should never reach here";
        return 0;
    }

    boolean boolValue() {
        assert false : "should never reach here";
        return false;
    }

    char charValue() {
        assert false : "should never reach here";
        return ' ';
    }

    float floatValue() {
        assert false : "should never reach here";
        return 0.0f;
    }

    boolean isUndef() {
        return undef;
    }

    Type type() {
        return type;
    }

    static Value mkValue(Type type) {
        if (type == Type.INT) return new IntValue();
        if (type == Type.BOOL) return new BoolValue();
        if (type == Type.CHAR) return new CharValue();
        if (type == Type.FLOAT) return new FloatValue();
        throw new IllegalArgumentException("Illegal type in mkValue");
    }
}

class IntValue extends Value {
    private int value = 0;

    IntValue() {
        type = Type.INT;
    }

    IntValue(int v) {
        this();
        value = v;
        undef = false;
    }

    int intValue() {
        assert !undef : "reference to undefined int value";
        return value;
    }

    public String toString() {
        if (undef) return "undef";
        return "" + value;
    }

    public void display(int k) {
        super.display(k);
        System.out.println(value);
    }
}

class BoolValue extends Value {
    private boolean value = false;

    BoolValue() {
        type = Type.BOOL;
    }

    BoolValue(boolean v) {
        this();
        value = v;
        undef = false;
    }

    boolean boolValue() {
        assert !undef : "reference to undefined bool value";
        return value;
    }

    int intValue() {
        assert !undef : "reference to undefined bool value";
        return value ? 1 : 0;
    }

    public String toString() {
        if (undef) return "undef";
        return "" + value;
    }

    public void display(int k) {
        super.display(k);
        System.out.println(value);
    }
}

class CharValue extends Value {
    private char value = ' ';

    CharValue() {
        type = Type.CHAR;
    }

    CharValue(char v) {
        this();
        value = v;
        undef = false;
    }

    char charValue() {
        assert !undef : "reference to undefined char value";
        return value;
    }

    public String toString() {
        if (undef) return "undef";
        return "" + value;
    }

    public void display(int k) {
        super.display(k);
        System.out.println(value);
    }
}

class FloatValue extends Value {
    private float value = 0;

    FloatValue() {
        type = Type.FLOAT;
    }

    FloatValue(float v) {
        this();
        value = v;
        undef = false;
    }

    float floatValue() {
        assert !undef : "reference to undefined float value";
        return value;
    }

    public String toString() {
        if (undef) return "undef";
        return "" + value;
    }

    public void display(int k) {
        super.display(k);
        System.out.println(value);
    }
}

class Binary extends Expression {
    // Binary = Operator op; Expression term1, term2
    Operator op;
    Expression term1, term2;

    public Binary(Operator op, Expression term1, Expression term2) {
        this.op = op;
        this.term1 = term1;
        this.term2 = term2;
    }

    public void display(int k) {
        super.display(k);
        op.display(k + 1);
        term1.display(k + 1);
        term2.display(k + 1);
    }
}

class Unary extends Expression {
    // Unary = Operator op; Expression term
    Operator op;
    Expression term;

    public Unary(Operator op, Expression term) {
        this.op = op;
        this.term = term;
    }

    public void display(int k) {
        super.display(k);
        op.display(k + 1);
        term.display(k + 1);
    }
}

class CallExpression extends Expression {
    String id;
    Expressions args;

    public CallExpression(String id, Expressions args) {
        this.id = id;
        this.args = args;
    }

    public void display(int k) {
        for (int w = 0; w < k; ++w) System.out.print("\t");
        System.out.println(getClass().toString().substring(6) + ": " + id);
        args.display(k + 1);
    }
}

class Operator {
    // Operator = BooleanOp | RelationalOp | ArithmeticOp | UnaryOp
    // BooleanOp = && | ||
    final static String AND = "&&";
    final static String OR = "||";
    // RelationalOp = < | <= | == | != | >= | >
    final static String LT = "<";
    final static String LE = "<=";
    final static String EQ = "==";
    final static String NE = "!=";
    final static String GT = ">";
    final static String GE = ">=";
    // ArithmeticOp = + | - | * | / | %
    final static String PLUS = "+";
    final static String MINUS = "-";
    final static String TIMES = "*";
    final static String DIV = "/";
    final static String MOD = "%";
    // UnaryOp = ! | -
    final static String NOT = "!";
    final static String NEG = "neg";
    // CastOp = int | float | char
    final static String INT = "int";
    final static String FLOAT = "float";
    final static String CHAR = "char";
    // Typed Operators
    // RelationalOp = < | <= | == | != | >= | >
    final static String INT_LT = "INT<";
    final static String INT_LE = "INT<=";
    final static String INT_EQ = "INT==";
    final static String INT_NE = "INT!=";
    final static String INT_GT = "INT>";
    final static String INT_GE = "INT>=";
    // ArithmeticOp = + | - | * | / | %
    final static String INT_PLUS = "INT+";
    final static String INT_MINUS = "INT-";
    final static String INT_TIMES = "INT*";
    final static String INT_DIV = "INT/";
    final static String INT_MOD = "INT%";
    // UnaryOp = -
    final static String INT_NEG = "INTNEG";
    // RelationalOp = < | <= | == | != | >= | >
    final static String FLOAT_LT = "FLOAT<";
    final static String FLOAT_LE = "FLOAT<=";
    final static String FLOAT_EQ = "FLOAT==";
    final static String FLOAT_NE = "FLOAT!=";
    final static String FLOAT_GT = "FLOAT>";
    final static String FLOAT_GE = "FLOAT>=";
    // ArithmeticOp = + | - | * | /
    final static String FLOAT_PLUS = "FLOAT+";
    final static String FLOAT_MINUS = "FLOAT-";
    final static String FLOAT_TIMES = "FLOAT*";
    final static String FLOAT_DIV = "FLOAT/";
    // UnaryOp = -
    final static String FLOAT_NEG = "FLOATNEG";
    // RelationalOp = < | <= | == | != | >= | >
    final static String CHAR_LT = "CHAR<";
    final static String CHAR_LE = "CHAR<=";
    final static String CHAR_EQ = "CHAR==";
    final static String CHAR_NE = "CHAR!=";
    final static String CHAR_GT = "CHAR>";
    final static String CHAR_GE = "CHAR>=";
    // RelationalOp = < | <= | == | != | >= | >
    final static String BOOL_LT = "BOOL<";
    final static String BOOL_LE = "BOOL<=";
    final static String BOOL_EQ = "BOOL==";
    final static String BOOL_NE = "BOOL!=";
    final static String BOOL_GT = "BOOL>";
    final static String BOOL_GE = "BOOL>=";
    // Type specific cast
    final static String I2F = "I2F";
    final static String F2I = "F2I";
    final static String C2I = "C2I";
    final static String I2C = "I2C";

    String val;

    Operator(String s) {
        val = s;
    }

    public String toString() {
        return val;
    }

    public boolean equals(Object obj) {
        return val.equals(obj);
    }

    boolean BooleanOp() {
        return val.equals(AND) || val.equals(OR);
    }

    boolean RelationalOp() {
        return val.equals(LT) || val.equals(LE) || val.equals(EQ)
                || val.equals(NE) || val.equals(GT) || val.equals(GE)
                || val.equals(FLOAT_LT) || val.equals(FLOAT_LE)
                || val.equals(FLOAT_EQ) || val.equals(FLOAT_NE)
                || val.equals(FLOAT_GT) || val.equals(FLOAT_GE)
                || val.equals(INT_LT) || val.equals(INT_LE)
                || val.equals(INT_EQ) || val.equals(INT_NE)
                || val.equals(INT_GT) || val.equals(INT_GE)
                || val.equals(CHAR_LT) || val.equals(CHAR_LE)
                || val.equals(CHAR_EQ) || val.equals(CHAR_NE)
                || val.equals(CHAR_GT) || val.equals(CHAR_GE)
                || val.equals(BOOL_LT) || val.equals(BOOL_LE)
                || val.equals(BOOL_EQ) || val.equals(BOOL_NE)
                || val.equals(BOOL_GT) || val.equals(BOOL_GE);
    }

    boolean ArithmeticOp() {
        return val.equals(PLUS) || val.equals(MINUS)
                || val.equals(TIMES) || val.equals(DIV) || val.equals(MOD)
                || val.equals(INT_PLUS) || val.equals(INT_MINUS)
                || val.equals(INT_TIMES) || val.equals(INT_DIV)
                || val.equals(INT_MOD)
                || val.equals(FLOAT_PLUS) || val.equals(FLOAT_MINUS)
                || val.equals(FLOAT_TIMES) || val.equals(FLOAT_DIV);
    }

    boolean ModOp() {
        return val.equals(MOD) || val.equals(INT_MOD);
    }

    boolean NotOp() {
        return val.equals(NOT);
    }

    boolean NegateOp() {
        return val.equals(NEG) || val.equals(INT_NEG) || val.equals(FLOAT_NEG);
    }

    boolean intOp() {
        return val.equals(INT) || val.equals(C2I) || val.equals(F2I);
    }

    boolean floatOp() {
        return val.equals(FLOAT) || val.equals(I2F);
    }

    boolean charOp() {
        return val.equals(CHAR) || val.equals(I2C);
    }

    boolean I2FOp() {
        return val.equals(I2F);
    }

    boolean F2IOp() {
        return val.equals(F2I);
    }

    boolean C2IOp() {
        return val.equals(C2I);
    }

    boolean I2COp() {
        return val.equals(I2C);
    }

    final static String intMap[][] = {
            {PLUS, INT_PLUS}, {MINUS, INT_MINUS},
            {TIMES, INT_TIMES}, {DIV, INT_DIV}, {MOD, INT_MOD},
            {EQ, INT_EQ}, {NE, INT_NE}, {LT, INT_LT},
            {LE, INT_LE}, {GT, INT_GT}, {GE, INT_GE},
            {NEG, INT_NEG}, {FLOAT, I2F}, {CHAR, I2C}
    };

    final static String floatMap[][] = {
            {PLUS, FLOAT_PLUS}, {MINUS, FLOAT_MINUS},
            {TIMES, FLOAT_TIMES}, {DIV, FLOAT_DIV},
            {EQ, FLOAT_EQ}, {NE, FLOAT_NE}, {LT, FLOAT_LT},
            {LE, FLOAT_LE}, {GT, FLOAT_GT}, {GE, FLOAT_GE},
            {NEG, FLOAT_NEG}, {INT, F2I}
    };

    final static String charMap[][] = {
            {EQ, CHAR_EQ}, {NE, CHAR_NE}, {LT, CHAR_LT},
            {LE, CHAR_LE}, {GT, CHAR_GT}, {GE, CHAR_GE},
            {INT, C2I}
    };

    final static String boolMap[][] = {
            {AND, AND}, {OR, OR},
            {EQ, BOOL_EQ}, {NE, BOOL_NE}, {LT, BOOL_LT},
            {LE, BOOL_LE}, {GT, BOOL_GT}, {GE, BOOL_GE}
    };

    final static private Operator map(String[][] tmap, String op) {
        for (int i = 0; i < tmap.length; i++)
            if (tmap[i][0].equals(op))
                return new Operator(tmap[i][1]);
        assert false : "should never reach here";
        return null;
    }

    final static public Operator intMap(String op) {
        return map(intMap, op);
    }

    final static public Operator floatMap(String op) {
        return map(floatMap, op);
    }

    final static public Operator charMap(String op) {
        return map(charMap, op);
    }

    final static public Operator boolMap(String op) {
        return map(boolMap, op);
    }

    public void display(int k) {
        System.out.println();
        for (int w = 0; w < k; ++w) System.out.print("\t");
        System.out.println(getClass().toString().substring(6) + ": " + val);
    }
}