import java.util.*;

// Following is the semantics class:
// The meaning M of a Statement is a State
// The meaning M of a Expression is a Value

public class Semantics {

    State M(Program p) {
        return initialState(p.globals);
    }

    State initialState(Declarations d) {
        // Declaration = VariableRef v, Type t
        State state = new State();
        for (Declaration decl : d)
            if (decl instanceof VariableDeclare) {
                VariableDeclare vd = (VariableDeclare) decl;
                ArrayList<Value> val = new ArrayList<Value>();
                val.add(Value.mkValue(vd.type));
                state.put(vd.variable, val);
            }

        return state;
    }

    State M(Statement s, State state) {
        //Statement = Skip | Assignment | Conditional | Loop | Block
        if (s instanceof Skip) return M((Skip) s, state);
        if (s instanceof Assignment) return M((Assignment) s, state);
        if (s instanceof Conditional) return M((Conditional) s, state);
        if (s instanceof Loop) return M((Loop) s, state);
        if (s instanceof Block) return M((Block) s, state);
        throw new IllegalArgumentException("should never reach here");
    }

    State M(Skip s, State state) {
        //Skip =
        return state;
    }

    State M(Assignment a, State state) {
        //Assignment = VariableRef target; Expression source
        return a.target instanceof Variable ? state.update(a.target, M(a.source, state)) : state;
    }

    State M(Block b, State state) {
        //Block = Statement*
        for (Statement s : b.statements)
            state = M(s, state);
        return state;
    }

    State M(Conditional c, State state) {
        //Conditional = Expression test; Statement thenBranch, elseBranch
        if (M(c.test, state).boolValue())
            return M(c.thenBranch, state);
        else
            return M(c.elseBranch, state);
    }

    State M(Loop l, State state) {
        //Loop = Expression test; Statement body
        if (M(l.test, state).boolValue())
            return M(l, M(l.body, state));
        else return state;
    }

    Value M(Expression e, State state) {
        //Expression = Value | VariableRef | Binary | Unary
        //VariableRef = Variable | ArrayRef

        //Value = return object
        if (e instanceof Value)
            return (Value) e;


        //Binary = Operator op; Expression term1, term2
        if (e instanceof Binary) {
            Binary b = (Binary) e;
            return applyBinary(b.op, M(b.term1, state), M(b.term2, state));
        }

        //Unary = Operator op; Expression term
        if (e instanceof Unary) {
            Unary u = (Unary) e;
            return applyUnary(u.op, M(u.term, state));
        }
        throw new IllegalArgumentException("should never reach here");
    }

    Value applyBinary(Operator op, Value v1, Value v2) {
        StaticTypeCheck.check(!v1.isUndef() && !v2.isUndef(), "reference to undef value");

        Value v = switch (op.val) {
            // Integer
            case Operator.INT_PLUS -> new IntValue(v1.intValue() + v2.intValue());
            case Operator.INT_MINUS -> new IntValue(v1.intValue() - v2.intValue());
            case Operator.INT_TIMES -> new IntValue(v1.intValue() * v2.intValue());
            case Operator.INT_DIV -> new IntValue(v1.intValue() / v2.intValue());
            case Operator.INT_EQ -> new BoolValue(v1.intValue() == v2.intValue());
            case Operator.INT_NE -> new BoolValue(v1.intValue() != v2.intValue());
            case Operator.INT_LT -> new BoolValue(v1.intValue() < v2.intValue());
            case Operator.INT_LE -> new BoolValue(v1.intValue() <= v2.intValue());
            case Operator.INT_GT -> new BoolValue(v1.intValue() > v2.intValue());
            case Operator.INT_GE -> new BoolValue(v1.intValue() >= v2.intValue());

            // Float
            case Operator.FLOAT_PLUS -> new FloatValue(v1.floatValue() + v2.floatValue());
            case Operator.FLOAT_MINUS -> new FloatValue(v1.floatValue() - v2.floatValue());
            case Operator.FLOAT_TIMES -> new FloatValue(v1.floatValue() * v2.floatValue());
            case Operator.FLOAT_DIV -> new FloatValue(v1.floatValue() / v2.floatValue());
            case Operator.FLOAT_EQ -> new BoolValue(v1.floatValue() == v2.floatValue());
            case Operator.FLOAT_NE -> new BoolValue(v1.floatValue() != v2.floatValue());
            case Operator.FLOAT_LT -> new BoolValue(v1.floatValue() < v2.floatValue());
            case Operator.FLOAT_LE -> new BoolValue(v1.floatValue() <= v2.floatValue());
            case Operator.FLOAT_GT -> new BoolValue(v1.floatValue() > v2.floatValue());
            case Operator.FLOAT_GE -> new BoolValue(v1.floatValue() >= v2.floatValue());

            // Character
            case Operator.CHAR_EQ -> new BoolValue(v1.charValue() == v2.charValue());
            case Operator.CHAR_NE -> new BoolValue(v1.charValue() != v2.charValue());
            case Operator.CHAR_LT -> new BoolValue(v1.charValue() < v2.charValue());
            case Operator.CHAR_LE -> new BoolValue(v1.charValue() <= v2.charValue());
            case Operator.CHAR_GT -> new BoolValue(v1.charValue() > v2.charValue());
            case Operator.CHAR_GE -> new BoolValue(v1.charValue() >= v2.charValue());

            // Boolean
            case Operator.BOOL_EQ -> new BoolValue(v1.boolValue() == v2.boolValue());
            case Operator.BOOL_NE -> new BoolValue(v1.boolValue() != v2.boolValue());
            case Operator.BOOL_LT -> new BoolValue(!v1.boolValue() && v2.boolValue());
            case Operator.BOOL_LE -> new BoolValue(!v1.boolValue() || v2.boolValue());
            case Operator.BOOL_GT -> new BoolValue(v1.boolValue() && !v2.boolValue());
            case Operator.BOOL_GE -> new BoolValue(v1.boolValue() || !v2.boolValue());
            case Operator.AND -> new BoolValue(v1.boolValue() && v2.boolValue());
            case Operator.OR -> new BoolValue(v1.boolValue() || v2.boolValue());

            // Should never reach default
            default -> null;
        };

        if (v != null)
            return v;
        throw new IllegalArgumentException("should never reach here");
    }

    Value applyUnary(Operator op, Value v) {
        StaticTypeCheck.check(!v.isUndef(), "reference to undef value");
        Value value = switch (op.val) {
            case Operator.NOT -> new BoolValue(!v.boolValue());
            case Operator.INT_NEG -> new IntValue(-v.intValue());
            case Operator.FLOAT_NEG -> new FloatValue(-v.floatValue());
            case Operator.I2F -> new FloatValue((float) v.intValue());
            case Operator.F2I -> new IntValue((int) v.floatValue());
            case Operator.C2I -> new IntValue((int) v.charValue());
            case Operator.I2C -> new CharValue((char) v.intValue());
            default -> null;
        };

        if (value != null)
            return value;
        throw new IllegalArgumentException("should never reach here");
    }

    public static void main(String args[]) {
        Parser parser = new Parser(new Lexer(args[0]));
        Program program = parser.program();
        program.display(0);
        System.out.println("\nBegin type checking");
        System.out.println("Type map : ");
        TypeMap map = StaticTypeCheck.typing(program.globals);
        map.display();
        StaticTypeCheck.V(program, map);
        Program out = TypeTransformer.T(program, map);
        System.out.println("Output AST");
        out.display(0);
        Semantics semantics = new Semantics();
        State state = semantics.M(out);
        System.out.println("Final State");
        state.display();
    }
}
