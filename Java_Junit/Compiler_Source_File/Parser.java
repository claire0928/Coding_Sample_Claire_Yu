package cop5556fa17;


import cop5556fa17.AST.*;
import cop5556fa17.Scanner.Token;

import java.util.ArrayList;

import static cop5556fa17.Scanner.Kind.*;


public class Parser {

	@SuppressWarnings("serial")
	public class SyntaxException extends Exception {
		Token t;

		public SyntaxException(Token t, String message) {
			super(message);
			this.t = t;
		}

	}


	Scanner scanner;
	Token t;

	Parser(Scanner scanner) {
		this.scanner = scanner;
		t = scanner.nextToken();
	}

	/**
	 * Main method called by compiler to parser input.
	 * Checks for EOF
	 * 
	 * @throws SyntaxException
	 */
	public Program parse() throws SyntaxException {
		Program p = program();
		matchEOF();
		return p;
	}
	

	/**
	 * Program ::=  IDENTIFIER   ( Declaration SEMI | Statement SEMI )*   
	 * 
	 * Program is start symbol of our grammar.
	 * 
	 * @throws SyntaxException
	 */
    Program program() throws SyntaxException {
        // Program ::=  IDENTIFIER   ( Declaration SEMI | Statement SEMI )*
        ArrayList<ASTNode> nodes=new ArrayList<>();
        Program p;
        Token first=t;
        Token ident = t;
		match(IDENTIFIER);
		while (t.kind==KW_int||t.kind==KW_boolean||t.kind==KW_image||
				t.kind==KW_url||t.kind==KW_file||t.kind==IDENTIFIER){
			if (t.kind==KW_int||t.kind==KW_boolean||t.kind==KW_image||
					t.kind==KW_url||t.kind==KW_file){
				if (t.kind==KW_int||t.kind==KW_boolean){
                    Declaration_Variable d = VariableDeclaration();
                    nodes.add(d);
				}
				else if (t.kind==KW_image){
                    Declaration_Image i = ImageDeclaration();
                    nodes.add(i);
				}
				else if (t.kind==KW_url||t.kind==KW_file){
                    Declaration_SourceSink s = SourceSinkDeclaration();
                    nodes.add(s);
				}
			}
			else if (t.kind==IDENTIFIER){
                // Statement  ::= AssignmentStatement|ImageOutStatement|ImageInStatement
                Token curFirst = t;
                match(IDENTIFIER);
                if(t.kind==LSQUARE || t.kind==OP_ASSIGN) {
                    // AssignmentStatement ::= Lhs OP_ASSIGN Expression
                    // Lhs ::= IDENTIFIER (LSQUARE LhsSelector RSQUARE|ε)
                    // AssignmentStatement ::= IDENTIFIER (LSQUARE LhsSelector RSQUARE|ε) OP_ASSIGN Expression
                    Statement_Assign sa;
                    Token name=curFirst;
                    LHS l;
                    Index index;
                    Expression e;
                    if (t.kind==LSQUARE){
                        match(LSQUARE);
                        index = LhsSelector();
                        match(RSQUARE);
                    }
                    else index =null;
                    l= new LHS(curFirst,name,index);
                    match(OP_ASSIGN);
                    e = expression();
                    sa = new Statement_Assign(curFirst, l, e);
                    nodes.add(sa);
                }
                if(t.kind==OP_LARROW) {
                    // ImageInStatement ::= IDENTIFIER OP_LARROW Source
                    Statement_In si;
                    Token name=curFirst;
                    Source s;
                    match(OP_LARROW);
                    s = source();
                    si = new Statement_In(first, name, s);
                    nodes.add(si);
                }
                if(t.kind==OP_RARROW) {
                    // ImageOutStatement ::= IDENTIFIER OP_RARROW Sink
                    Statement_Out so;
                    Token name=curFirst;
                    Sink s;
                    match(OP_RARROW);
                    s = sink();
                    so = new Statement_Out(first, name, s);
                    nodes.add(so);
                }
            }
			match(SEMI);
		}
		p=new Program(first,ident,nodes);
		return p;
	}

    Declaration_Variable VariableDeclaration() throws SyntaxException {
        //VariableDeclaration  ::=  VarType IDENTIFIER  (  OP_ASSIGN  Expression  | ε )
        Declaration_Variable d;
        Token first = t;
        Token type;
        Token name;
        Expression e;
        if (t.kind==KW_int){
            type = t;
            match(KW_int);
        }
        else if (t.kind==KW_boolean){
            type = t;
            match(KW_boolean);
        }
        else throw new SyntaxException(t,"unexpected VarType " + t.kind);
        name = t;
        match(IDENTIFIER);
        if (t.kind==OP_ASSIGN){
            match(OP_ASSIGN);
            e = expression();
            d = new Declaration_Variable(first,type,name,e);
        }
        else d = new Declaration_Variable(first,type,name,null);
        return d;
    }

    Declaration_Image ImageDeclaration() throws SyntaxException {
        // ImageDeclaration ::=  KW_image  (LSQUARE Expression COMMA Expression RSQUARE | ε)
        // IDENTIFIER ( OP_LARROW Source | ε )
        Declaration_Image image;
        Token first = t;
        Token ident;
        Expression e0;
        Expression e1;
        Source s;
        match(KW_image);
        if(t.kind==LSQUARE){
            match(LSQUARE);
            e0 = expression();
            match(COMMA);
            e1 = expression();
            match(RSQUARE);
        }
        else e0 = e1 = null;
        ident = t;
        match(IDENTIFIER);
        if(t.kind==OP_LARROW){
            match(OP_LARROW);
            s = source();
        }
        else s = null;
        image = new Declaration_Image(first,e0,e1,ident,s);
        return image;
    }

    Declaration_SourceSink SourceSinkDeclaration() throws SyntaxException {
        // SourceSinkDeclaration ::= SourceSinkType IDENTIFIER  OP_ASSIGN  Source
        Declaration_SourceSink sink;
        Token first = t;
        Token type;
        Token ident;
        Source s;
        if (t.kind==KW_url){
            type = t;
            match(KW_url);
        }
        else if (t.kind==KW_file){
            type = t;
            match(KW_file);
        }
        else throw new SyntaxException(t,"unexpected SourceSinkType " + t.kind);
        ident = t;
        match(IDENTIFIER);
        match(OP_ASSIGN);
        s = source();
        sink = new Declaration_SourceSink(first,type,ident,s);
        return sink;
    }

	Index LhsSelector() throws SyntaxException {
        // LhsSelector ::= LSQUARE  ( XySelector  | RaSelector  )   RSQUAR
        Index index;
		match(LSQUARE);
		if (t.kind==KW_x){
			index = XySelector();
		}
		else if (t.kind==KW_r){
			index = RaSelector();
		}
		else throw new SyntaxException(t,"unexpected LhsSelector " + t.kind);
		match(RSQUARE);
		return index;
	}

	Index XySelector() throws SyntaxException {
        // XySelector ::= KW_x COMMA KW_y
        Index index;
        Expression e0;
        Expression e1;
        Token first = t;
        if(t.kind == KW_x){
            e0 = expression();
        }
        else throw new SyntaxException(t,"unexpected XySelector x " + t.kind);
		match(COMMA);
        if(t.kind == KW_y){
            e1 = expression();
        }
        else throw new SyntaxException(t,"unexpected XySelector y " + t.kind);
		index = new Index(first,e0,e1);
        return index;
	}

	Index RaSelector() throws SyntaxException {
        // RaSelector ::= KW_r COMMA KW_a
        Index index;
        Token first = t;
        Expression e0;
        Expression e1;
        if(t.kind == KW_r){
            e0 = expression();
        }
        else throw new SyntaxException(t,"unexpected RaSelector r " + t.kind);
        match(COMMA);
        if(t.kind == KW_a){
            e1 = expression();
        }
        else throw new SyntaxException(t,"unexpected RaSelector a " + t.kind);
        index = new Index(first,e0,e1);
        return index;
	}

	Sink sink() throws SyntaxException {
        // Sink ::= IDENTIFIER
        // Sink ::= KW_SCREEN
        Sink_Ident s;
        Sink_SCREEN k;
        Token first = t;
        if (t.kind==IDENTIFIER) {
            s=new Sink_Ident(first,t);
            match(IDENTIFIER);
            return s;
        }
        else if(t.kind==KW_SCREEN) {
            k=new Sink_SCREEN(first);
            match(KW_SCREEN);
            return k;
        }
		else throw new SyntaxException(t,"expect file but saw " + t.kind);
	}

	Source source() throws SyntaxException {
        // Source ::= STRING_LITERAL
        // Source ::= OP_AT Expression
        // Source ::= IDENTIFIER
        Source_StringLiteral sl;
        Source_CommandLineParam sc;
        Source_Ident si;
        Token first = t;

        if (t.kind==STRING_LITERAL){
            sl = new Source_StringLiteral(first,t.getText());
			match(STRING_LITERAL);
			return sl;
		}
		else if (t.kind==OP_AT){
            Expression e;
			match(OP_AT);
			e = expression();
            sc = new Source_CommandLineParam(first,e);
            return sc;
		}
		else if (t.kind==IDENTIFIER){
		    si = new Source_Ident(first,t);
			match(IDENTIFIER);
			return si;
		}
		else throw new SyntaxException(t,"unexpected Source " + t.kind);
    }

	/**
	 * Expression ::=  OrExpression  OP_Q  Expression OP_COLON Expression    | OrExpression
	 * 
	 * Our test cases may invoke this routine directly to support incremental development.
	 * 
	 * @throws SyntaxException
	 */
	Expression expression() throws SyntaxException {
		//Expression ::=  OrExpression  OP_Q  Expression OP_COLON Expression
        //Expression ::=  OrExpression
        Expression_Conditional e;
        Expression condition;
        Expression te;
        Expression fe;
        Token first = t;
        condition = OrExpression();
        if (t.kind==OP_Q){
            match(OP_Q);
            te = expression();
            match(OP_COLON);
            fe = expression();
            e=new Expression_Conditional(first,condition,te,fe);
            return e;
        }
        else return condition;
	}

	Expression OrExpression() throws SyntaxException {
	    // OrExpression ::= AndExpression   (  OP_OR  AndExpression)*
        Token first = t;
        Expression e0;
        Token op = t;
        Expression e1 = null;
		e0 = AndExpression();
		while (t.kind==OP_OR){
		    op = t;
			match(OP_OR);
			e1 = AndExpression();
		}
        if(e1!=null)return new Expression_Binary(first,e0,op,e1);
        else return e0;
    }

	Expression AndExpression() throws SyntaxException {
	    // AndExpression ::= EqExpression ( OP_AND  EqExpression )*
        Token first = t;
        Expression e0;
        Token op = t;
        Expression e1 = null;
		e0 = EqExpression();
		while (t.kind==OP_AND){
            op = t;
			match(OP_AND);
			e1 = EqExpression();
		}
        if(e1!=null)return new Expression_Binary(first,e0,op,e1);
        else return e0;
    }

	Expression EqExpression() throws SyntaxException {
	    // EqExpression ::= RelExpression  (  (OP_EQ | OP_NEQ )  RelExpression )*
        Token first = t;
        Expression e0;
        Token op = t;
        Expression e1 = null;
        e0 = RelExpression();
		while (t.kind==OP_EQ||t.kind==OP_NEQ){
			if (t.kind==OP_EQ){
                op = t;
				match(OP_EQ);
			}
			else if (t.kind==OP_NEQ){
                op = t;
				match(OP_NEQ);
			}
			e1 = RelExpression();
		}
        if(e1!=null)return new Expression_Binary(first,e0,op,e1);
        else return e0;
    }

	Expression RelExpression() throws SyntaxException {
	    // RelExpression ::= AddExpression (  ( OP_LT  | OP_GT |  OP_LE  | OP_GE )   AddExpression)*
        Token first = t;
        Expression e0;
        Token op = t;
        Expression e1 = null;
		e0 = AddExpression();
		while (t.kind==OP_LT||t.kind==OP_GT||t.kind==OP_LE||t.kind==OP_GE){
			if (t.kind==OP_LT){
                op = t;
				match(OP_LT);
			}
			else if (t.kind==OP_GT){
                op = t;
				match(OP_GT);
			}
			else if (t.kind==OP_LE){
                op = t;
				match(OP_LE);
			}
			else if (t.kind==OP_GE){
                op = t;
				match(OP_GE);
			}
			e1 = AddExpression();
		}
        if(e1!=null)return new Expression_Binary(first,e0,op,e1);
        else return e0;
    }

	Expression AddExpression() throws SyntaxException {
	    // AddExpression ::= MultExpression   (  (OP_PLUS | OP_MINUS ) MultExpression )*
        Token first = t;
        Expression e0;
        Token op = t;
        Expression e1 = null;
		e0 = MultExpression();
		while (t.kind==OP_PLUS||t.kind==OP_MINUS){
			if (t.kind==OP_PLUS){
                op = t;
				match(OP_PLUS);
			}
			else if (t.kind==OP_MINUS){
                op = t;
				match(OP_MINUS);
			}
			e1 = MultExpression();
		}
        if(e1!=null)return new Expression_Binary(first,e0,op,e1);
        else return e0;
    }

	Expression MultExpression() throws SyntaxException {
	    // MultExpression := UnaryExpression ( ( OP_TIMES | OP_DIV  | OP_MOD ) UnaryExpression )*
        Token first = t;
        Expression e0;
        Token op = t;
        Expression e1 = null;
		e0 = UnaryExpression();
		while (t.kind==OP_TIMES||t.kind==OP_DIV||t.kind==OP_MOD){
			if (t.kind==OP_TIMES){
                op = t;
				match(OP_TIMES);
			}
			else if (t.kind==OP_DIV){
                op = t;
				match(OP_DIV);
			}
			else if (t.kind==OP_MOD){
                op = t;
				match(OP_MOD);
			}
			e1 = UnaryExpression();
		}
		if(e1!=null)return new Expression_Binary(first,e0,op,e1);
        else return e0;
    }

	Index Selector() throws SyntaxException {
	    // Selector ::=  Expression COMMA Expression
        Index index;
        Expression e0;
        Expression e1;
        Token first = t;
        e0 = expression();
        match(COMMA);
        e1 = expression();
        index = new Index(first,e0,e1);
        return index;
	}

	Expression FunctionApplication() throws SyntaxException {
	    // FunctionApplication ::= FunctionName LPAREN Expression RPAREN
        // FunctionApplication ::=  FunctionName  LSQUARE Selector RSQUARE
        Expression_FunctionAppWithExprArg ee;
        Expression_FunctionAppWithIndexArg ei;
        Token first = t;
        Scanner.Kind function;
        Expression arge;
        Index argi;
        if(t.kind==KW_sin){function = t.kind;match(KW_sin);}
        else if(t.kind==KW_cos){function = t.kind;match(KW_cos);}
        else if(t.kind==KW_atan){function = t.kind;match(KW_atan);}
        else if(t.kind==KW_abs){function = t.kind;match(KW_abs);}
        else if(t.kind==KW_cart_x){function = t.kind;match(KW_cart_x);}
        else if(t.kind==KW_cart_y){function = t.kind;match(KW_cart_y);}
        else if(t.kind==KW_polar_a){function = t.kind;match(KW_polar_a);}
        else if(t.kind==KW_polar_r){function = t.kind;match(KW_polar_r);}
        else throw new SyntaxException(t,"unexpected FunctionName " + t.kind);

		if (t.kind==LPAREN){
			match(LPAREN);
			arge = expression();
			match(RPAREN);
            return new Expression_FunctionAppWithExprArg(first,function,arge);
		}
		else if (t.kind==LSQUARE){
			match(LSQUARE);
			argi = Selector();
			match(RSQUARE);
            return new Expression_FunctionAppWithIndexArg(first,function,argi);
		}
		else throw new SyntaxException(t,"unexpected FunctionApplication " + t.kind);
	}

	Expression IdentOrPixelSelectorExpression() throws SyntaxException {
	    // IdentOrPixelSelectorExpression::=  IDENTIFIER LSQUARE Selector RSQUARE
        // IdentOrPixelSelectorExpression::=  IDENTIFIER
        Token first = t;
        Token name = null;
        Index i;
        if(t.kind==IDENTIFIER){name = t;match(IDENTIFIER);}
        if (t.kind==LSQUARE){
            match(LSQUARE);
            i = Selector();
            match(RSQUARE);
            return new Expression_PixelSelector(first,name,i);
        }
        else return new Expression_Ident(first,name);
	}

	Expression primary() throws SyntaxException {
	    // Primary ::= INTEGER_LITERAL | LPAREN Expression RPAREN | FunctionApplication | BOOLEAN_LITERAL
        Token first = t;
        if (t.kind==INTEGER_LITERAL){
            int num = t.intVal();
			match(INTEGER_LITERAL);
            return new Expression_IntLit(first,num);
		}
		else if (t.kind==BOOLEAN_LITERAL){
            boolean bool;
            if(t.getText().equals("true")) bool=true;
            else bool=false;
			match(BOOLEAN_LITERAL);
            return new Expression_BooleanLit(first,bool);
		}
		else if (t.kind==LPAREN){
			match(LPAREN);
			Expression e=expression();
			match(RPAREN);
            return e;
		}
		else if (t.kind==KW_sin||t.kind==KW_cos||t.kind==KW_atan||t.kind==KW_abs
				||t.kind==KW_cart_x||t.kind==KW_cart_y||t.kind==KW_polar_a||t.kind==KW_polar_r){
            return FunctionApplication();
		}
		else throw new SyntaxException(t,"unexpected primary " + t.kind);
	}

	Expression UnaryExpression() throws SyntaxException {
	    // UnaryExpression ::= OP_PLUS UnaryExpression|OP_MINUS UnaryExpression|UnaryExpressionNotPlusMinus
        Token first = t;
        Token op;
        Expression e;
        if (t.kind==OP_PLUS){
            op = t;
			match(OP_PLUS);
			e = UnaryExpression();
			return new Expression_Unary(first,op,e);
		}
		else if (t.kind==OP_MINUS){
            op = t;
			match(OP_MINUS);
			e = UnaryExpression();
            return new Expression_Unary(first,op,e);
		}
		else if (t.kind==OP_EXCL||t.kind==INTEGER_LITERAL||t.kind==LPAREN||t.kind==KW_sin||
				t.kind==KW_cos||t.kind==KW_atan||t.kind==KW_abs||t.kind==KW_cart_x||
				t.kind==KW_cart_y||t.kind==KW_polar_a||t.kind==KW_polar_r||t.kind==IDENTIFIER||
				t.kind==KW_x||t.kind==KW_y||t.kind==KW_r||t.kind==KW_a||t.kind==KW_X||t.kind==KW_Y||
				t.kind==KW_Z||t.kind==KW_A||t.kind==KW_R||t.kind==KW_DEF_X||t.kind==KW_DEF_Y||t.kind==BOOLEAN_LITERAL
				){
            return UnaryExpressionNotPlusMinus();
		}
		else throw new SyntaxException(t,"unexpected UnaryExpression " + t.kind);
    }

    Expression UnaryExpressionNotPlusMinus() throws SyntaxException {
	    // UnaryExpressionNotPlusMinus ::=  OP_EXCL  UnaryExpression
        // UnaryExpressionNotPlusMinus ::=  Primary
        // UnaryExpressionNotPlusMinus ::=   IdentOrPixelSelectorExpression
        // UnaryExpressionNotPlusMinus ::=  KW_x | KW_y | KW_r | KW_a | KW_X |
        // KW_Y | KW_Z | KW_A | KW_R | KW_DEF_X | KW_DEF_Y
        Token first = t;
        Token op;
        Expression e;
        Expression_PredefinedName name;
		if (t.kind==OP_EXCL){
		    op = t;
			match(OP_EXCL);
			e = UnaryExpression();
            return new Expression_Unary(first,op,e);
		}
		else if (t.kind==INTEGER_LITERAL||t.kind==LPAREN||t.kind==KW_sin||
				t.kind==KW_cos||t.kind==KW_atan||t.kind==KW_abs||t.kind==KW_cart_x||
				t.kind==KW_cart_y||t.kind==KW_polar_a||t.kind==KW_polar_r||t.kind==BOOLEAN_LITERAL){
            return primary();
		}
		else if (t.kind==IDENTIFIER){
			return IdentOrPixelSelectorExpression();
		}
		else if (t.kind == KW_x){name = new Expression_PredefinedName(first,t.kind);match(KW_x);}
		else if (t.kind == KW_y){name = new Expression_PredefinedName(first,t.kind);match(KW_y);}
		else if (t.kind == KW_r) {name = new Expression_PredefinedName(first,t.kind);match(KW_r);}
		else if (t.kind == KW_a) {name = new Expression_PredefinedName(first,t.kind);match(KW_a);}
		else if (t.kind == KW_X) {name = new Expression_PredefinedName(first,t.kind);match(KW_X);}
		else if (t.kind == KW_Y) {name = new Expression_PredefinedName(first,t.kind);match(KW_Y);}
		else if (t.kind == KW_Z) {name = new Expression_PredefinedName(first,t.kind);match(KW_Z);}
		else if (t.kind == KW_A) {name = new Expression_PredefinedName(first,t.kind);match(KW_A);}
		else if (t.kind == KW_R) {name = new Expression_PredefinedName(first,t.kind);match(KW_R);}
		else if (t.kind == KW_DEF_X) {name = new Expression_PredefinedName(first,t.kind);match(KW_DEF_X);}
		else if (t.kind == KW_DEF_Y) {name = new Expression_PredefinedName(first,t.kind);match(KW_DEF_Y);}
		else throw new SyntaxException(t,"unexpected UnaryExpressionNotPlusMinus " + t.kind);
		return name;
	}

	/**
	 * Only for check at end of program. Does not "consume" EOF so no attempt to get
	 * nonexistent next Token.
	 * 
	 * @return
	 * @throws SyntaxException
	 */
	private Token matchEOF() throws SyntaxException {
		if (t.kind == EOF) {
			return t;
		}
		String message =  "Expected EOF at " + t.line + ":" + t.pos_in_line;
		throw new SyntaxException(t, message);
	}

	private Token consume() throws SyntaxException {
		Token tmp = t;
		t = scanner.nextToken();
		return tmp;
	}

	private Token match(Scanner.Kind kind) throws SyntaxException {
		if (t.kind == kind) {
			return consume();
		}
		throw new SyntaxException(t,"saw " + t.kind +" "+ "expected " + kind);
	}
}
