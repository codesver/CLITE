// StaticTypeCheck.java

// Static type checking for Clite is defined by the functions
// V and the auxiliary functions typing and typeOf.  These
// functions use the classes in the Abstract Syntax of Clite.

public class StaticTypeCheck {
    private static Type returnType;
    private static boolean returnFound = false;
    private static TypeMap functionMap = new TypeMap();
    private static Functions dtFunction = new Functions();

    public static TypeMap typing(Declarations d) {
        TypeMap map = new TypeMap();
        for (Declaration di : d)
            if (di instanceof VariableDeclare) {
                VariableDeclare vd = (VariableDeclare) di;
                map.put(vd.variable, vd.type);
            }

        return map;
    }

    public static void check(boolean test, String msg) {
        if (test) return;
        System.err.println(msg);
        System.exit(1);
    }

    public static void V(Declarations d) {
        for (int i = 0; i < d.size() - 1; i++)
            for (int j = i + 1; j < d.size(); j++) {
                Declaration di = d.get(i);
                Declaration dj = d.get(j);
                check(!(di.variable.equals(dj.variable)),
                        "duplicate declaration: " + dj.variable);
            }
    }

    public static void V(Function f) {

        Declarations ds = new Declarations();
        ds.addAll(f.params);
        ds.addAll(f.locals);
        V(ds);
    }

    public static void V(Program p, TypeMap GM) {
        System.out.println("Globals = {");
        GM.display(null, functionMap);
        dtFunction.addAll(p.functions);
        Declarations ds = new Declarations();
        ds.addAll(p.globals);
        for (int i = 0; i < p.functions.size(); i++) {

            Variable fl = new Variable(p.functions.get(i).id);
            ds.add(new VariableDeclare(fl, p.functions.get(i).type));
            functionMap.put(fl, p.functions.get(i).type);
        }
        V(ds);
        for (Function func : p.functions) {
            V(func);
        }
        V(p.functions, GM);
    }

    public static void V(Functions f, TypeMap tm) {
        for (Function func : f) {
            TypeMap fMap = new TypeMap();
            fMap.putAll(tm);
            fMap.putAll(typing(func.params));
            fMap.putAll(typing(func.locals));

            V(func, fMap);
            fMap.putAll(functionMap);
            System.out.println("Function " + func.id + " = {");
            fMap.display(f, functionMap);
        }
    }

    public static void V(Function f, TypeMap tm) {
        returnType = f.type;
        returnFound = false;
        V(f.body, tm);


        if (!(returnType.equals(Type.VOID)) && !f.id.equals("main")) {
            check((returnFound == true),
                    f.id + " is a non-Void function with no Return Statement");
        }
    }

    public static void V(Statement s, TypeMap tm) {
        if (s == null)
            throw new IllegalArgumentException("error: null statement");
        if (s instanceof Skip) return;
        if (s instanceof Assignment) {
            Assignment a = (Assignment) s;
            V(a.target, tm);
            V(a.source, tm);
            Type ttype = (Type) tm.get(a.target);
            Type srctype = typeOf(a.source, tm, functionMap);
            if (ttype != srctype) {
                if (ttype == Type.FLOAT)
                    check(srctype == Type.INT
                            , "mixed mode assignment to " + a.target);
                else if (ttype == Type.INT)
                    check(srctype == Type.CHAR
                            , "mixed mode assignment to " + a.target);
                else
                    check(false
                            , "mixed mode assignment to " + a.target);
            }
            return;
        }
        if (s instanceof Conditional) {
            Conditional c = (Conditional) s;
            V(c.test, tm);
            check(typeOf(c.test, tm, functionMap) == Type.BOOL,
                    "non-bool test in conditional");
            V(c.thenBranch, tm);
            V(c.elseBranch, tm);
            return;
        }
        if (s instanceof Loop) {
            Loop l = (Loop) s;
            V(l.test, tm);
            check(typeOf(l.test, tm, functionMap) == Type.BOOL,
                    "loop has non-bool test");
            V(l.body, tm);
            return;
        }
        if (s instanceof Block) {
            Block b = (Block) s;
            for (int j = 0; j < b.statements.size(); j++)
                V((Statement) (b.statements.get(j)), tm);
            return;
        }
        if (s instanceof Return) {
            check(!(returnType.equals(Type.VOID)),
                    "Return is not a valid Statement in a Void Function");
            Return r = (Return) s;
            check(returnType.equals(typeOf(r.retVal, tm, functionMap)),
                    "The returned type does not match the fuction type;");
            returnFound = true;
            return;
        }
        if (s instanceof CallStatement) {
            CallStatement c = (CallStatement) s;
            check((functionMap.get(new Variable(c.id))).equals(Type.VOID),
                    "Statement Calls can only be to Void statements");
            for (Function func : dtFunction) {
                if (func.id.equals(c.id)) {
                    check(c.args.size() == func.params.size(),
                            "Arguments and Parameters are different size.");
                    for (int i = 0; i < c.args.size(); i++) {
                        Type ti = ((Type) func.params.get(i).type);
                        Type tj = typeOf(c.args.get(i), tm, functionMap);
                        check(ti.equals(tj)
                                , func.params.get(i).type + " is not equal to " + typeOf(c.args.get(i), tm, functionMap));
                    }
                }
            }
            return;
        }
        throw new IllegalArgumentException("should never reach here");
    }

    public static void V(Expression e, TypeMap tm) {
        if (e instanceof Value)
            return;
        if (e instanceof Variable) {
            Variable v = (Variable) e;
            check(tm.containsKey(v), "undeclared variable: " + v);
            return;
        }
        if (e instanceof CallExpression) {
            CallExpression c = (CallExpression) e;
            check(!(functionMap.get(new Variable(c.id))).equals(Type.VOID),
                    "Expression Calls must have a return type.");
            for (Function func : dtFunction) {
                if (func.id.equals(c.id)) {
                    check(c.args.size() == func.params.size(),
                            "Arguments and Parameters are different size.");
                    for (int i = 0; i < c.args.size(); i++) {
                        Type ti = ((Type) func.params.get(i).type);
                        Type tj = typeOf(c.args.get(i), tm, functionMap);
                        check(ti.equals(tj)
                                , func.params.get(i).type + " is not equal to " + typeOf(c.args.get(i), tm, functionMap));
                    }
                }
            }
            return;
        }
        if (e instanceof Binary) {
            Binary b = (Binary) e;
            Type typ1 = typeOf(b.term1, tm, functionMap);
            Type typ2 = typeOf(b.term2, tm, functionMap);
            V(b.term1, tm);
            V(b.term2, tm);
            if (b.op.ArithmeticOp())
                if (b.op.ModOp())
                    check(typ1 == typ2 && typ1 == Type.INT
                            , "type error for " + b.op);
                else
                    check(typ1 == typ2 &&
                                    (typ1 == Type.INT || typ1 == Type.FLOAT)
                            , "type error for " + b.op);
            else if (b.op.RelationalOp())
                check(typ1 == typ2, "type error for " + b.op);
            else if (b.op.BooleanOp())
                check(typ1 == Type.BOOL && typ2 == Type.BOOL,
                        b.op + ": non-bool operand");
            else
                throw new IllegalArgumentException("should never reach here");
            return;
        }
        if (e instanceof Unary) {
            Unary u = (Unary) e;
            Type typ1 = typeOf(u.term, tm, functionMap);
            V(u.term, tm);
            if (u.op.NotOp())
                check(typ1 == Type.BOOL, "! has non-bool operand");
            else if (u.op.NegateOp())
                check(typ1 == Type.INT || typ1 == Type.FLOAT
                        , "Unary - has non-int/float operand");
            else if (u.op.floatOp())
                check(typ1 == Type.INT, "float() has non-int operand");
            else if (u.op.charOp())
                check(typ1 == Type.INT, "char() has non-int operand");
            else if (u.op.intOp())
                check(typ1 == Type.FLOAT || typ1 == Type.CHAR
                        , "int() has non-float/char operand");
            else
                throw new IllegalArgumentException("should never reach here");
            return;
        }
        throw new IllegalArgumentException("should never reach here");
    }

    public static Type typeOf(Expression e, TypeMap tm, TypeMap fm) {
        if (e instanceof Value) return ((Value) e).type;
        if (e instanceof Variable) {
            Variable v = (Variable) e;
            check(tm.containsKey(v), "undefined variable: " + v);
            return (Type) tm.get(v);
        }
        if (e instanceof CallExpression) {
            CallExpression c = (CallExpression) e;
            if (functionMap.isEmpty()) {
                functionMap = new TypeMap();
                functionMap.putAll(fm);
            }
            check(functionMap.containsKey(new Variable(c.id)), "undefined variable: " + c.id);
            return (Type) functionMap.get(new Variable(c.id));
        }
        if (e instanceof Binary) {
            Binary b = (Binary) e;
            if (b.op.ArithmeticOp())
                if (typeOf(b.term1, tm, functionMap) == Type.FLOAT)
                    return (Type.FLOAT);
                else return (Type.INT);
            if (b.op.RelationalOp() || b.op.BooleanOp())
                return (Type.BOOL);
        }
        if (e instanceof Unary) {
            Unary u = (Unary) e;
            if (u.op.NotOp()) return (Type.BOOL);
            else if (u.op.NegateOp()) return typeOf(u.term, tm, functionMap);
            else if (u.op.intOp()) return (Type.INT);
            else if (u.op.floatOp()) return (Type.FLOAT);
            else if (u.op.charOp()) return (Type.CHAR);
        }
        throw new IllegalArgumentException("should never reach here");
    }

    public static void main(String args[]) {
        Parser parser = new Parser(new Lexer(args[0]));
        Program program = parser.program();
        program.display();
        System.out.println("\nBegin type checking...");
        System.out.println("Type map : ");
        TypeMap map = typing(program.globals);
        map.display();
        V(program, map);
        System.out.println("Type check success");
    } // main
} // class StaticTypeCheck
