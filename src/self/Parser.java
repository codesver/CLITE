package self;

public class Parser {
    // Recursive descent parser that inputs a C++Lite program and
    // generates its abstract syntax. Each method corresponds to
    // a concrete syntax grammar rule, which appears as a comment
    // at the beginning of the method.

    Token token; // current token from the input stream
    Lexer lexer;

    public Parser(Lexer ts) { // Open the C++Lite source program
        lexer = ts; // as a token stream, and
        token = lexer.next(); // retrieve its first self.Token
    }

    private String match(TokenType t) {
        String value = token.value();
        if (token.type().equals(t))
            token = lexer.next();
        else
            error(t);
        return value;
    }

    private void error(TokenType tok) {
        System.err.println("Syntax error: expecting: " + tok + "; saw: " + token);
        System.exit(1);
    }

    private void error(String tok) {
        System.err.println("Syntax error: expecting: " + tok + "; saw: " + token);
        System.exit(1);
    }

    public Program program() {
        // self.Program --> void main ( ) '{' self.Declarations Statements '}'
        TokenType[] header = { TokenType.Int, TokenType.Main, TokenType.LeftParen, TokenType.RightParen };
        for (int i = 0; i < header.length; i++) // bypass "int main ( )"
            match(header[i]);
        match(TokenType.LeftBrace);
        Declarations declarations = declarations();
        Block block = new Block();
        Statement statement;
        while(isStatement()) {
            statement = statement();
            block.members.add(statement);
        }
        match(TokenType.RightBrace);
        return new Program(declarations, block); // student exercise
    }

    private Declarations declarations() {
        // self.Declarations --> { self.Declaration }
        Declarations declarations = new Declarations();
        while (isType())
            declaration(declarations);
        return declarations; // student exercise
    }

    private void declaration(Declarations ds) {
        // self.Declaration --> self.Type Identifier { , Identifier } ;
        Type type = type();
        Variable variable = new Variable(match(TokenType.Identifier));
        Declaration declaration = new Declaration(variable, type);
        ds.add(declaration);
        while (token.type().equals(TokenType.Comma)) {
            token = lexer.next();
            variable = new Variable(match(TokenType.Identifier));
            declaration = new Declaration(variable, type);
            ds.add(declaration);
        }
        match(TokenType.Semicolon);
    }

    private Type type() {
        // self.Type --> int | bool | float | char
        Type type = null;
        if (token.type().equals(TokenType.Int))
            type = Type.INT;
        else if (token.type().equals(TokenType.Bool))
            type = Type.BOOL;
        else if (token.type().equals(TokenType.Float))
            type = Type.FLOAT;
        else if (token.type().equals(TokenType.Char))
            type = Type.CHAR;
        else
            error("Error in self.Type construction");
        token = lexer.next();
        return type;
    }

    private Statement statement() {
        // self.Statement --> ; | self.Block | self.Assignment | IfStatement | WhileStatement
        Statement statement = null;
        if (token.type().equals(TokenType.Semicolon))
            statement = new Skip();
        else if (token.type().equals(TokenType.LeftBrace))
            statement = statements();
        else if (token.type().equals(TokenType.If))
            statement = ifStatement();
        else if (token.type().equals(TokenType.While))
            statement = whileStatement();
        else if (token.type().equals(TokenType.Identifier))
            statement = assignment();
        return statement;
    }

    private Block statements() {
        // self.Block --> '{' Statements '}'
        Block block = new Block();
        match(TokenType.LeftBrace);
        while (isStatement()) {
            Statement statement = statement();
            block.members.add(statement);
        }
        return block;
    }

    private Assignment assignment() {
        // self.Assignment --> Identifier = self.Expression ;
        Variable target = new Variable(match(TokenType.Identifier));
        match(TokenType.Assign);
        Expression source = expression();
        match(TokenType.Semicolon);
        return new Assignment(target, source);
    }

    private Conditional ifStatement() {
        // IfStatement --> if ( self.Expression ) self.Statement [ else self.Statement ]
        Conditional conditional;
        match(TokenType.If);
        match(TokenType.LeftParen);
        Expression test = expression();
        match(TokenType.RightParen);
        Statement statement = statement();
        if (token.type().equals(TokenType.Else)) {
            Statement elseStatement = statement();
            conditional = new Conditional(test, statement, elseStatement);
        } else
            conditional = new Conditional(test, statement);
        return conditional;
    }

    private Loop whileStatement() {
        // WhileStatement --> while ( self.Expression ) self.Statement
        match(TokenType.While);
        match(TokenType.LeftParen);
        Expression test = expression();
        match(TokenType.RightParen);
        Statement body = statement();
        return new Loop(test, body);
    }

    private Expression expression() {
        // self.Expression --> Conjunction { || Conjunction }
        Expression expression = conjunction();
        while (token.type().equals(TokenType.Or)) {
            Operator operator = new Operator(match(token.type()));
            Expression expressionOther = expression();
            expression = new Binary(operator, expression, expressionOther);
        }
        return expression;
    }

    private Expression conjunction() {
        // Conjunction --> Equality { && Equality }
        Expression conjunction = equality();
        while (token.type().equals(TokenType.And)) {
            Operator operator = new Operator(match(token.type()));
            Expression conjunctionOther = conjunction();
            conjunction = new Binary(operator, conjunction, conjunctionOther);
        }
        return conjunction;
    }

    private Expression equality() {
        // Equality --> Relation [ EquOp Relation ]
        Expression equality = relation();
        while (isEqualityOp()) {
            Operator operator = new Operator(match(token.type()));
            Expression equalityOther = relation();
            equality = new Binary(operator, equality, equalityOther);
        }
        return equality;
    }

    private Expression relation() {
        // Relation --> Addition [RelOp Addition]
        Expression relation = addition();
        while (isRelationalOp()) {
            Operator operator = new Operator(match(token.type()));
            Expression relationOther = addition();
            relation = new Binary(operator, relation, relationOther);
        }
        return relation;
    }

    private Expression addition() {
        // Addition --> Term { AddOp Term }
        Expression addition = term();
        while (isAddOp()) {
            Operator op = new Operator(match(token.type()));
            Expression additionOther = term();
            addition = new Binary(op, addition, additionOther);
        }
        return addition;
    }

    private Expression term() {
        // Term --> Factor { MultiplyOp Factor }
        Expression term = factor();
        while (isMultiplyOp()) {
            Operator operator = new Operator(match(token.type()));
            Expression termOther = factor();
            term = new Binary(operator, term, termOther);
        }
        return term;
    }

    private Expression factor() {
        // Factor --> [ UnaryOp ] Primary
        if (isUnaryOp()) {
            Operator operator = new Operator(match(token.type()));
            Expression term = primary();
            return new Unary(operator, term);
        } else
            return primary();
    }

    private Expression primary() {
        // Primary --> Identifier | Literal | ( self.Expression )
        // | self.Type ( self.Expression )
        Expression expression = null;
        if (token.type().equals(TokenType.Identifier)) {
            expression = new Variable(match(TokenType.Identifier));
        } else if (isLiteral()) {
            expression = literal();
        } else if (token.type().equals(TokenType.LeftParen)) {
            token = lexer.next();
            expression = expression();
            match(TokenType.RightParen);
        } else if (isType()) {
            Operator operator = new Operator(match(token.type()));
            match(TokenType.LeftParen);
            Expression term = expression();
            match(TokenType.RightParen);
            expression = new Unary(operator, term);
        } else
            error("Identifier | Literal | ( | self.Type");
        return expression;
    }

    private Value literal() {
        Value value = null;
        String str = token.value();
        if (token.type().equals(TokenType.IntLiteral)) {
            value = new IntValue(Integer.parseInt(str));
            token = lexer.next();
        } else if (token.type().equals(TokenType.FloatLiteral)) {
            value = new FloatValue(Float.parseFloat(str));
            token = lexer.next();
        } else if (token.type().equals(TokenType.CharLiteral)) {
            value = new CharValue(str.charAt(0));
            token = lexer.next();
        } else if (token.type().equals(TokenType.True)) {
            value = new BoolValue(true);
            token = lexer.next();
        } else if (token.type().equals(TokenType.False)) {
            value = new BoolValue(false);
            token = lexer.next();
        } else
            error("Error in literal value contruction");
        return value;
    }

    private boolean isAddOp() {
        return token.type().equals(TokenType.Plus)
                || token.type().equals(TokenType.Minus);
    }

    private boolean isMultiplyOp() {
        return token.type().equals(TokenType.Multiply)
                || token.type().equals(TokenType.Divide);
    }

    private boolean isUnaryOp() {
        return token.type().equals(TokenType.Not)
                || token.type().equals(TokenType.Minus);
    }

    private boolean isEqualityOp() {
        return token.type().equals(TokenType.Equals)
                || token.type().equals(TokenType.NotEqual);
    }

    private boolean isRelationalOp() {
        return token.type().equals(TokenType.Less)
                || token.type().equals(TokenType.LessEqual)
                || token.type().equals(TokenType.Greater)
                || token.type().equals(TokenType.GreaterEqual);
    }

    private boolean isType() {
        return token.type().equals(TokenType.Int)
                || token.type().equals(TokenType.Bool)
                || token.type().equals(TokenType.Float)
                || token.type().equals(TokenType.Char);
    }

    private boolean isLiteral() {
        return token.type().equals(TokenType.IntLiteral)
                || isBooleanLiteral()
                || token.type().equals(TokenType.FloatLiteral)
                || token.type().equals(TokenType.CharLiteral);
    }

    private boolean isBooleanLiteral() {
        return token.type().equals(TokenType.True)
                || token.type().equals(TokenType.False);
    }

    private boolean isStatement() {
        return token.type().equals(TokenType.Semicolon)
                || token.type().equals(TokenType.LeftBrace)
                || token.type().equals(TokenType.If)
                || token.type().equals(TokenType.While)
                || token.type().equals(TokenType.Identifier);
    }

    public static void main(String args[]) {
        Parser parser = new Parser(new Lexer(args[0]));
        Program prog = parser.program();
        prog.display(0); // display abstract syntax tree
    } // main

} // self.Parser
