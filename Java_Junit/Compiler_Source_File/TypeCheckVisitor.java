package cop5556fa17;

import cop5556fa17.AST.*;
import cop5556fa17.Scanner.Token;

import java.net.URL;

public class TypeCheckVisitor implements ASTVisitor {

		@SuppressWarnings("serial")
		public static class SemanticException extends Exception {
			Token t;

			public SemanticException(Token t, String message) {
				super("line " + t.line + " pos " + t.pos_in_line + ": "+  message);
				this.t = t;
			}
		}

	SymbolTable symtab = new SymbolTable();
	/**
	 * The program name is only used for naming the class.  It does not rule out
	 * variables with the same name.  It is returned for convenience.
	 * 
	 * @throws Exception 
	 */
	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		for (ASTNode node: program.decsAndStatements) {
			node.visit(this, arg);
		}
		return program.name;
	}

	@Override
	public Object visitDeclaration_Variable(
			Declaration_Variable declaration_Variable, Object arg)
			throws Exception {
        if(symtab.lookupDec(declaration_Variable.getName())!=null) {
            throw new SemanticException(declaration_Variable.getFirstToken(), "illegal declaration_Variable");
        }
		Expression expression = declaration_Variable.getE();
		String name = declaration_Variable.getName();
        declaration_Variable.setTypename(TypeUtils.getType(declaration_Variable.getFirstToken()));
		if(expression==null) {
            symtab.insert(name,declaration_Variable);
            return null;
        }
        expression.visit(this,arg);
		if(declaration_Variable.getTypename()!=expression.getTypename()) {
            throw new SemanticException(declaration_Variable.getFirstToken(), "illegal declaration_Variable: type not equal");
        }
        symtab.insert(name,declaration_Variable);
		return null;
	}

	@Override
	public Object visitExpression_Binary(Expression_Binary expression_Binary,
			Object arg) throws Exception {
		Expression e0 = expression_Binary.e0;
		Expression e1 = expression_Binary.e1;
		Scanner.Kind op = expression_Binary.op;
		e0.visit(this,arg);
		e1.visit(this,arg);
		if(e0.getTypename()!=e1.getTypename()){
			throw new SemanticException(expression_Binary.getFirstToken(),"illegal expression_Binary: type not equal");
		}
		if(op== Scanner.Kind.OP_EQ || op== Scanner.Kind.OP_NEQ) {
			expression_Binary.setTypename(TypeUtils.Type.BOOLEAN);
		} else if(op==Scanner.Kind.OP_GE||op==Scanner.Kind.OP_GT
				||op==Scanner.Kind.OP_LE||op==Scanner.Kind.OP_LT
				&& e0.getTypename()== TypeUtils.Type.INTEGER) {
			expression_Binary.setTypename(TypeUtils.Type.BOOLEAN);
		} else if (op==Scanner.Kind.OP_AND||op==Scanner.Kind.OP_OR
				&&(e0.getTypename()== TypeUtils.Type.INTEGER||e0.getTypename()== TypeUtils.Type.BOOLEAN)) {
			expression_Binary.setTypename(e0.getTypename());
		} else if (op==Scanner.Kind.OP_DIV||op==Scanner.Kind.OP_MINUS
				||op==Scanner.Kind.OP_MOD||op==Scanner.Kind.OP_PLUS
				||op==Scanner.Kind.OP_POWER||op==Scanner.Kind.OP_TIMES && e0.getTypename()== TypeUtils.Type.INTEGER) {
			expression_Binary.setTypename(TypeUtils.Type.INTEGER);
		} else {
			expression_Binary.setTypename(null); // not very sure about cube notion
		}
        if(expression_Binary.getTypename()==null) {
            throw new SemanticException(expression_Binary.getFirstToken(),"illegal expression_Binary: no such type");
        }
		return null;
	}

	@Override
	public Object visitExpression_Unary(Expression_Unary expression_Unary,
			Object arg) throws Exception {
		Scanner.Kind op = expression_Unary.op;
		Expression expression = expression_Unary.e;
		expression.visit(this,arg);
		if(op==Scanner.Kind.OP_EXCL
				&& (expression.getTypename()== TypeUtils.Type.BOOLEAN
				||expression.getTypename()== TypeUtils.Type.INTEGER)) {
			expression_Unary.setTypename(expression.getTypename());
		} else if (op==Scanner.Kind.OP_PLUS||op==Scanner.Kind.OP_MINUS
				&& expression.getTypename()== TypeUtils.Type.INTEGER) {
			expression_Unary.setTypename(TypeUtils.Type.INTEGER);
		} else {
			expression_Unary.setTypename(null); // not very sure about cube notion
		}
		if(expression_Unary.getTypename()==null)
			throw new SemanticException(expression_Unary.getFirstToken(),"illegal expression_Unary:no such type");
		return null;
	}

	@Override
	public Object visitIndex(Index index, Object arg) throws Exception {
		Expression e0=index.getE0();
		Expression e1=index.getE1();
		e0.visit(this,null);
		e1.visit(this,null);
		if(e0.getTypename()!= TypeUtils.Type.INTEGER || e1.getTypename()!= TypeUtils.Type.INTEGER){
			throw new SemanticException(index.getFirstToken(),"illegal index");
		}
		index.setCartesian(!(e0.getTypename() == TypeUtils.Type.valueOf("KW_r")
				&& e1.getTypename() == TypeUtils.Type.valueOf("KW_a")));
		return null;
	}

	@Override
	public Object visitExpression_PixelSelector(
			Expression_PixelSelector expression_PixelSelector, Object arg)
			throws Exception {
	    String name = expression_PixelSelector.getName();
	    Index index = expression_PixelSelector.getIndex();
	    if (symtab.lookupDec(name)==null)
            throw new SemanticException(expression_PixelSelector.getFirstToken(),"illegal expression_PixelSelector: no such name");
        TypeUtils.Type nameType = symtab.lookupType(name);
	    if (nameType == TypeUtils.Type.IMAGE) {
	        expression_PixelSelector.setTypename(TypeUtils.Type.INTEGER);
        } else if (index==null) {
	        expression_PixelSelector.setTypename(nameType);
        } else {
	        expression_PixelSelector.setTypename(null);
        }
		if (expression_PixelSelector.getTypename()==null)
			throw new SemanticException(expression_PixelSelector.getFirstToken(),"illegal expression_PixelSelector: no such type");
		return null;
	}

	@Override
	public Object visitExpression_Conditional(
			Expression_Conditional expression_Conditional, Object arg)
			throws Exception {
		Expression et = expression_Conditional.getTrueExpression();
		Expression ec = expression_Conditional.getCondition();
		Expression ef = expression_Conditional.getFalseExpression();
		et.visit(this,arg);
        ec.visit(this,arg);
        ef.visit(this,arg);
		if(ec.getTypename()!= TypeUtils.Type.BOOLEAN) {
			throw new SemanticException(expression_Conditional.getFirstToken(),"illegal expression_Conditional");
		} else if (et.getTypename()!=ef.getTypename()) {
			throw new SemanticException(expression_Conditional.getFirstToken(),"illegal expression_Conditional: type not equal");
		}
		expression_Conditional.setTypename(et.getTypename());
		return null;
	}

	@Override
	public Object visitDeclaration_Image(Declaration_Image declaration_Image,
			Object arg) throws Exception {
		if(symtab.lookupDec(declaration_Image.getName())!=null)
			throw new SemanticException(declaration_Image.getFirstToken(),"illegal Declaration_Image");
		symtab.insert(declaration_Image.getName(),declaration_Image);
		declaration_Image.setTypename(TypeUtils.Type.IMAGE);
		Expression xSize = declaration_Image.getxSize();
		Expression ySize = declaration_Image.getySize();
		if (xSize!=null) xSize.visit(this,arg);
        if (ySize!=null) ySize.visit(this,arg);
		if ((xSize!=null && ySize==null)||(xSize==null && ySize!=null)){
            throw new SemanticException(declaration_Image.getFirstToken(),"illegal Declaration_Image: illegal x and y: not equal!");
        } else if (xSize==null&&ySize==null) {
		    return null;
        } else if (xSize.getTypename()!= TypeUtils.Type.INTEGER||ySize.getTypename()!= TypeUtils.Type.INTEGER) {
            throw new SemanticException(declaration_Image.getFirstToken(),"illegal Declaration_Image: illegal x and y: not integer!");
        }
		return null;
	}

	@Override
	public Object visitSource_StringLiteral(
			Source_StringLiteral source_StringLiteral, Object arg)
			throws Exception {
		boolean isValidURL;
		String fileOrUrl = source_StringLiteral.getFileOrUrl();
		try {
			URL url = new java.net.URL(fileOrUrl);
			url.toURI();
			isValidURL = true;
		} catch (Exception exception)
		{
			isValidURL = false;
		}
		if (isValidURL) {
			source_StringLiteral.setTypename(TypeUtils.Type.URL);
		} else {
			source_StringLiteral.setTypename(TypeUtils.Type.FILE);
		}
		return null;
	}

	@Override
	public Object visitSource_CommandLineParam(
			Source_CommandLineParam source_CommandLineParam, Object arg)
			throws Exception {
		Expression e = source_CommandLineParam.getParamNum();
		e.visit(this,arg);
		source_CommandLineParam.setTypename(null);
        if(source_CommandLineParam.getParamNum().getTypename()!= TypeUtils.Type.INTEGER)
            throw new SemanticException(source_CommandLineParam.getFirstToken(),"illegal source_CommandLineParam");
        return null;
	}

	@Override
	public Object visitSource_Ident(Source_Ident source_Ident, Object arg)
			throws Exception {
		String name = source_Ident.getName();
		if(symtab.lookupDec(name)==null) {
			throw new SemanticException(source_Ident.getFirstToken(),"illegal source_Ident: no such name");
		}
		if(symtab.lookupType(name)== TypeUtils.Type.URL || symtab.lookupType(name)== TypeUtils.Type.FILE) {
			source_Ident.setTypename(symtab.lookupType(name));
		} else {
			throw new SemanticException(source_Ident.getFirstToken(),"illegal source_Ident: illegal type");
		}
		return null;
	}

	@Override
	public Object visitDeclaration_SourceSink(
			Declaration_SourceSink declaration_SourceSink, Object arg)
			throws Exception {
        if(symtab.lookupDec(declaration_SourceSink.getName())!=null) {
            throw new SemanticException(declaration_SourceSink.getFirstToken(), "illegal declaration_SourceSink");
        }
		String name = declaration_SourceSink.getName();
		Source source = declaration_SourceSink.getSource();
		source.visit(this,arg);
        declaration_SourceSink.setTypename(TypeUtils.getType(declaration_SourceSink.getFirstToken()));
        if(source.getTypename()!=declaration_SourceSink.getTypename() && source.getTypename()!=null) {
			throw new SemanticException(declaration_SourceSink.getFirstToken(), "illegal declaration_SourceSink: type not equal");
		}
		symtab.insert(name,declaration_SourceSink);
		return null;
	}

	@Override
	public Object visitExpression_IntLit(Expression_IntLit expression_IntLit,
			Object arg) throws Exception {
		expression_IntLit.setTypename(TypeUtils.Type.INTEGER);
		return null;
	}

	@Override
	public Object visitExpression_FunctionAppWithExprArg(
			Expression_FunctionAppWithExprArg expression_FunctionAppWithExprArg,
			Object arg) throws Exception {
		Expression expression = expression_FunctionAppWithExprArg.getArg();
		expression.visit(this,arg);
		if(expression.getTypename()!= TypeUtils.Type.INTEGER)
			throw new SemanticException(expression_FunctionAppWithExprArg.getFirstToken(),"illegal expression_FunctionAppWithExprArg");
		expression_FunctionAppWithExprArg.setTypename(TypeUtils.Type.INTEGER);
		return null;
	}

	@Override
	public Object visitExpression_FunctionAppWithIndexArg(
			Expression_FunctionAppWithIndexArg expression_FunctionAppWithIndexArg,
			Object arg) throws Exception {
		expression_FunctionAppWithIndexArg.setTypename(TypeUtils.Type.INTEGER);
		return null;
	}

	@Override
	public Object visitExpression_PredefinedName(
			Expression_PredefinedName expression_PredefinedName, Object arg)
			throws Exception {
		expression_PredefinedName.setTypename(TypeUtils.Type.INTEGER);
		return null;
	}

	@Override
	public Object visitStatement_Out(Statement_Out statement_Out, Object arg)
			throws Exception {
	    String name = statement_Out.getName();
	    Sink sink = statement_Out.getSink();
	    sink.visit(this,arg);
		if ((symtab.lookupDec(name)!=null)&&
                (((symtab.lookupType(name) == TypeUtils.Type.INTEGER
                        ||symtab.lookupType(name) == TypeUtils.Type.BOOLEAN)
                        && sink.getTypename() == TypeUtils.Type.SCREEN)
                        ||(symtab.lookupType(name) == TypeUtils.Type.IMAGE
                        &&(sink.getTypename() == TypeUtils.Type.FILE
                        ||sink.getTypename() == TypeUtils.Type.SCREEN)))) {
		    statement_Out.setDec(symtab.lookupDec(name));
        }
        return null;
	}

	@Override
	public Object visitStatement_In(Statement_In statement_In, Object arg)
			throws Exception {
		String name = statement_In.getName();
		Source source = statement_In.getSource();
        source.visit(this,arg);
//		if(symtab.lookupDec(name)==null) {
//			throw new SemanticException(statement_In.getFirstToken(), "illegal statement_In: no such name");
//		} else if(source.getTypename()!=symtab.lookupType(name)) {
//			throw new SemanticException(statement_In.getFirstToken(), "illegal statement_In: type not equal");
//		}
		statement_In.setDec(symtab.lookupDec(name));
		return null;
	}

	@Override
	public Object visitStatement_Assign(Statement_Assign statement_Assign,
			Object arg) throws Exception {
		Expression e = statement_Assign.getE();
		LHS lhs = statement_Assign.getLHS();
		e.visit(this,arg);
		lhs.visit(this,arg);
		if(e.getFirstToken().kind== Scanner.Kind.BOOLEAN_LITERAL){
			e.setTypename(TypeUtils.Type.BOOLEAN);
		}
		if(e.getFirstToken().kind== Scanner.Kind.INTEGER_LITERAL){
			e.setTypename(TypeUtils.Type.INTEGER);
		}
        if(lhs.getTypename()== TypeUtils.Type.IMAGE && e.getTypename()== TypeUtils.Type.INTEGER){
            statement_Assign.setCartesian(lhs.isCartesian());
            return null;
        }
		if (e.getTypename()!=lhs.getTypename()) {
			throw new SemanticException(statement_Assign.getFirstToken(), "illegal statement_Assign: type not equal");
		}
		statement_Assign.setCartesian(lhs.isCartesian());
		return null;
	}

	@Override
	public Object visitLHS(LHS lhs, Object arg) throws Exception {
		if(symtab.lookupDec(lhs.getName())==null) {
			throw new SemanticException(lhs.getFirstToken(), "illegal lhs: no such name");
		}
		lhs.setDec(symtab.lookupDec(lhs.getName()));
		lhs.setTypename(lhs.getDec().getTypename());
		if(lhs.index!=null){
			lhs.index.visit(this, null);
			lhs.setCartesian(lhs.index.isCartesian());
		}
		return null;
	}

	@Override
	public Object visitSink_SCREEN(Sink_SCREEN sink_SCREEN, Object arg)
			throws Exception {
		sink_SCREEN.setTypename(TypeUtils.Type.SCREEN);
		return null;
	}

	@Override
	public Object visitSink_Ident(Sink_Ident sink_Ident, Object arg)
			throws Exception {
		if(symtab.lookupDec(sink_Ident.getName())==null) {
			throw new SemanticException(sink_Ident.getFirstToken(), "illegal sink_Ident: no such name");
		} else if(symtab.lookupType(sink_Ident.getName())!= TypeUtils.Type.FILE) {
			throw new SemanticException(sink_Ident.getFirstToken(), "illegal sink_Ident: type is not FILE");
		}
		sink_Ident.setTypename(symtab.lookupType(sink_Ident.getName()));
		return null;
	}

	@Override
	public Object visitExpression_BooleanLit(
			Expression_BooleanLit expression_BooleanLit, Object arg)
			throws Exception {
		expression_BooleanLit.setTypename(TypeUtils.Type.BOOLEAN);
		return null;
	}

	@Override
	public Object visitExpression_Ident(Expression_Ident expression_Ident,
			Object arg) throws Exception {
		if(symtab.lookupDec(expression_Ident.getName())==null)
			throw new SemanticException(expression_Ident.getFirstToken(),"illegal expression_Ident: no such name");
		expression_Ident.setTypename(symtab.lookupType(expression_Ident.getName()));
		return null;
	}

}
