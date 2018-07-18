package cop5556fa17;

import cop5556fa17.AST.*;
import org.objectweb.asm.*;

import java.util.ArrayList;

import static cop5556fa17.ImageSupport.*;
import static cop5556fa17.RuntimeFunctions.*;
//import cop5556fa17.image.ImageFrame;
//import cop5556fa17.image.ImageSupport;

public class CodeGenVisitor implements ASTVisitor, Opcodes {

	/**
	 * All methods and variable static.
	 */


	/**
	 * @param DEVEL
	 *            used as parameter to genPrint and genPrintTOS
	 * @param GRADE
	 *            used as parameter to genPrint and genPrintTOS
	 * @param sourceFileName
	 *            name of source file, may be null.
	 */
	public CodeGenVisitor(boolean DEVEL, boolean GRADE, String sourceFileName) {
		super();
		this.DEVEL = DEVEL;
		this.GRADE = GRADE;
		this.sourceFileName = sourceFileName;
	}

	ClassWriter cw;
	String className;
	String classDesc;
	String sourceFileName;

	MethodVisitor mv; // visitor of method currently under construction

	/** Indicates whether genPrint and genPrintTOS should generate code. */
	final boolean DEVEL;
	final boolean GRADE;
	


	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		className = program.name;  
		classDesc = "L" + className + ";";
		String sourceFileName = (String) arg;
		cw.visit(52, ACC_PUBLIC + ACC_SUPER, className, null, "java/lang/Object", null);
		cw.visitSource(sourceFileName, null);
		// create main method
		mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
		// initialize
		mv.visitCode();		
		//add label before first instruction
		Label mainStart = new Label();
		mv.visitLabel(mainStart);		
		// if GRADE, generates code to add string to log
//		CodeGenUtils.genLog(GRADE, mv, "entering main");

		// visit decs and statements to add field to class
		//  and instructions to main method, respectivley
		ArrayList<ASTNode> decsAndStatements = program.decsAndStatements;
		for (ASTNode node : decsAndStatements) {
			node.visit(this, arg);
		}

		//generates code to add string to log
//		CodeGenUtils.genLog(GRADE, mv, "leaving main");
		
		//adds the required (by the JVM) return statement to main
		mv.visitInsn(RETURN);
		
		//adds label at end of code
		Label mainEnd = new Label();
		mv.visitLabel(mainEnd);
		
		//handles parameters and local variables of main. Right now, only args
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, mainStart, mainEnd, 0);

        mv.visitLocalVariable("X", "I", null, mainStart, mainEnd, 1);
        mv.visitLocalVariable("Y", "I", null, mainStart, mainEnd, 2);
        mv.visitLocalVariable("x", "I", null, mainStart, mainEnd, 3);
        mv.visitLocalVariable("y", "I", null, mainStart, mainEnd, 4);
        mv.visitLocalVariable("r", "I", null, mainStart, mainEnd, 5);
        mv.visitLocalVariable("a", "I", null, mainStart, mainEnd, 6);
        mv.visitLocalVariable("R", "I", null, mainStart, mainEnd, 7);
        mv.visitLocalVariable("A", "I", null, mainStart, mainEnd, 8);

        //Sets max stack size and number of local vars.
		//Because we use ClassWriter.COMPUTE_FRAMES as a parameter in the constructor,
		//asm will calculate this itself and the parameters are ignored.
		//If you have trouble with failures in this routine, it may be useful
		//to temporarily set the parameter in the ClassWriter constructor to 0.
		//The generated classfile will not be correct, but you will at least be
		//able to see what is in it.
		mv.visitMaxs(0, 0);

		//terminate construction of main method
		mv.visitEnd();
		
		//terminate class construction
		cw.visitEnd();

		//generate classfile as byte array and return
		return cw.toByteArray();
	}

	@Override
	public Object visitDeclaration_Variable(Declaration_Variable declaration_Variable, Object arg) throws Exception {

	    String fieldName = declaration_Variable.getName();  //name of the field
        Expression expression = declaration_Variable.getE();
        Object initValue;

        if(declaration_Variable.getTypename() == TypeUtils.Type.INTEGER) {
            initValue = new Integer(0);
        } else if(declaration_Variable.getTypename() == TypeUtils.Type.BOOLEAN) {
            initValue = new Boolean(false);
        } else {
            throw new RuntimeException("Unexpected type in variable declaration: " + declaration_Variable.getTypename());
        }

        if(expression!=null) {
            expression.visit(this, null);
            mv.visitFieldInsn(PUTSTATIC, className, fieldName, declaration_Variable.getTypename().jvmType);
        }
        FieldVisitor fv;
        fv = cw.visitField(ACC_STATIC, fieldName, declaration_Variable.getTypename().jvmType, null, initValue);
        fv.visitEnd();
        return null;
	}

	@Override
	public Object visitExpression_Binary(Expression_Binary expression_Binary, Object arg) throws Exception {
		// TODO
        Expression e0 = expression_Binary.getE0();
        Expression e1 = expression_Binary.getE1();
        Scanner.Kind op = expression_Binary.getOp();
        e0.visit(this,arg);
        e1.visit(this,arg);
        switch(op){

            case OP_AND:
                mv.visitInsn(IAND);
                break;

            case OP_OR:
                mv.visitInsn(IOR);
                break;

            case OP_EQ:{
                Label l1 = new Label();
                Label l2 = new Label();
                if((e0.getTypename().equals(TypeUtils.Type.BOOLEAN)||e0.getTypename().equals(TypeUtils.Type.INTEGER))
                        &&(e1.getTypename().equals(TypeUtils.Type.BOOLEAN)||e1.getTypename().equals(TypeUtils.Type.INTEGER)))
                    mv.visitJumpInsn(IF_ICMPEQ, l1);
                else
                    mv.visitJumpInsn(IF_ACMPEQ, l1);
                mv.visitLdcInsn(false);
                mv.visitJumpInsn(GOTO, l2);
                mv.visitLabel(l1);
                mv.visitLdcInsn(true);
                mv.visitLabel(l2);
            }
            break;
            case OP_NEQ:{
                Label l1 = new Label();
                Label l2 = new Label();
                if((e0.getTypename().equals(TypeUtils.Type.BOOLEAN)||e0.getTypename().equals(TypeUtils.Type.INTEGER))
                        &&(e1.getTypename().equals(TypeUtils.Type.BOOLEAN)||expression_Binary.e1.getTypename().equals(TypeUtils.Type.INTEGER)))
                    mv.visitJumpInsn(IF_ICMPNE, l1);
                else
                    mv.visitJumpInsn(IF_ACMPNE, l1);
                mv.visitLdcInsn(false);
                mv.visitJumpInsn(GOTO, l2);
                mv.visitLabel(l1);
                mv.visitLdcInsn(true);
                mv.visitLabel(l2);
            }
            break;
            case OP_LT:{
                Label l1 = new Label();
                Label l2 = new Label();
                mv.visitJumpInsn(IF_ICMPLT,l1);
                mv.visitLdcInsn(false);
                mv.visitJumpInsn(GOTO, l2);
                mv.visitLabel(l1);
                mv.visitLdcInsn(true);
                mv.visitLabel(l2);
            }
            break;
            case OP_GT:{
                Label l1 = new Label();
                Label l2 = new Label();
                mv.visitJumpInsn(IF_ICMPGT,l1);
                mv.visitLdcInsn(false);
                mv.visitJumpInsn(GOTO, l2);
                mv.visitLabel(l1);
                mv.visitLdcInsn(true);
                mv.visitLabel(l2);
            }
            break;
            case OP_LE:{
                Label l1 = new Label();
                Label l2 = new Label();
                mv.visitJumpInsn(IF_ICMPLE,l1);
                mv.visitLdcInsn(false);
                mv.visitJumpInsn(GOTO, l2);
                mv.visitLabel(l1);
                mv.visitLdcInsn(true);
                mv.visitLabel(l2);
            }
            break;
            case OP_GE:{
                Label l1 = new Label();
                Label l2 = new Label();
                mv.visitJumpInsn(IF_ICMPGE,l1);
                mv.visitLdcInsn(false);
                mv.visitJumpInsn(GOTO, l2);
                mv.visitLabel(l1);
                mv.visitLdcInsn(true);
                mv.visitLabel(l2);
            }
            break;
            case OP_PLUS:
                mv.visitInsn(IADD);
                break;
            case OP_MINUS:
                mv.visitInsn(ISUB);
                break;
            case OP_TIMES:
                mv.visitInsn(IMUL);
                break;
            case OP_DIV:
                mv.visitInsn(IDIV);
                break;
            case OP_MOD:
                mv.visitInsn(IREM);
                break;
            default:
                throw new RuntimeException("Unexpected operator in binary expression: " + String.valueOf(expression_Binary.op));

        }
        return null;
	}

	@Override
	public Object visitExpression_Unary(Expression_Unary expression_Unary, Object arg) throws Exception {
		// TODO
        Scanner.Kind op = expression_Unary.getOp();
        Expression expression = expression_Unary.getE();
        expression.visit(this,arg);
        switch(op){
            case OP_PLUS:
                break;
            case OP_MINUS:{
                mv.visitInsn(INEG);
            }
            break;
            case OP_EXCL:{
                if(expression.getTypename().equals(TypeUtils.Type.BOOLEAN)){
                    Label l1 = new Label();
                    Label l2 = new Label();
                    mv.visitJumpInsn(IFEQ, l1);
                    mv.visitLdcInsn(false);
                    mv.visitJumpInsn(GOTO, l2);
                    mv.visitLabel(l1);
                    mv.visitLdcInsn(true);
                    mv.visitLabel(l2);
                }else{
                    mv.visitLdcInsn(Integer.MAX_VALUE);
                    mv.visitInsn(IXOR);
                }
            }
            break;
            default:
                throw new RuntimeException("Unexpected operator in unary expression: " + String.valueOf(op));
        }
		return null;
	}

	// generate code to leave the two values on the stack
	@Override
	public Object visitIndex(Index index, Object arg) throws Exception {
		// TODO HW6
        Expression e0 = index.getE0();
        Expression e1 = index.getE1();
        e0.visit(this,arg);
        e1.visit(this,arg);
        if(!index.isCartesian()){
        }
        return null;
	}

	@Override
	public Object visitExpression_PixelSelector(Expression_PixelSelector expression_PixelSelector, Object arg)
			throws Exception {
		// TODO HW6
        mv.visitFieldInsn(GETSTATIC,className,expression_PixelSelector.getName(),ImageDesc);
        expression_PixelSelector.getIndex().visit(this,arg);
        mv.visitMethodInsn(INVOKESTATIC, "cop5556fa17/ImageSupport", "getPixel", getPixelSig,false);
        return null;
    }

	@Override
	public Object visitExpression_Conditional(Expression_Conditional expression_Conditional, Object arg)
			throws Exception {
		// TODO
        Expression ec=expression_Conditional.getCondition();
        ec.visit(this,arg);
        Label l1=new Label();
        Label l2=new Label();

        mv.visitJumpInsn(IFEQ, l1);
        expression_Conditional.trueExpression.visit(this, arg);
        mv.visitJumpInsn(GOTO, l2);
        mv.visitLabel(l1);
        expression_Conditional.falseExpression.visit(this, arg);
        mv.visitLabel(l2);
		return null;
	}


	@Override
	public Object visitDeclaration_Image(Declaration_Image declaration_Image, Object arg) throws Exception {
		// TODO HW6

        Source source = declaration_Image.getSource();
        if(source!=null) {
            mv.visitLdcInsn(source.getFirstToken().getText());
            if(declaration_Image.getxSize()==null || declaration_Image.getySize()==null) {
                mv.visitLdcInsn(0);
                mv.visitVarInsn(ISTORE, 1);
                mv.visitLdcInsn(0);
                mv.visitVarInsn(ISTORE, 2);
                mv.visitInsn(ACONST_NULL);
                mv.visitInsn(ACONST_NULL);
            } else {
                declaration_Image.getxSize().visit(this,arg);
                mv.visitMethodInsn(INVOKESTATIC,"java/lang/Integer","valueOf","(I)Ljava/lang/Integer;",false);
                mv.visitVarInsn(ISTORE, 1);
                declaration_Image.getySize().visit(this,arg);
                mv.visitMethodInsn(INVOKESTATIC,"java/lang/Integer","valueOf","(I)Ljava/lang/Integer;",false);
                mv.visitVarInsn(ISTORE, 2);
                declaration_Image.getxSize().visit(this,arg);
                declaration_Image.getySize().visit(this,arg);
            }
            mv.visitMethodInsn(INVOKESTATIC, "cop5556fa17/ImageSupport", "readImage", readImageSig,false);
            mv.visitFieldInsn(PUTSTATIC,className,declaration_Image.getName(),ImageDesc);
        } else {
            if(declaration_Image.getxSize()==null || declaration_Image.getySize()==null) {
                mv.visitLdcInsn(0);
                mv.visitVarInsn(ISTORE, 1);
                mv.visitLdcInsn(0);
                mv.visitVarInsn(ISTORE, 2);
                mv.visitLdcInsn(256);
                mv.visitLdcInsn(256);
            } else {
                declaration_Image.getxSize().visit(this,arg); //int
                mv.visitVarInsn(ISTORE, 1);
                declaration_Image.getySize().visit(this,null);
                mv.visitVarInsn(ISTORE, 2);
                declaration_Image.getxSize().visit(this,arg);
                declaration_Image.getySize().visit(this,arg);
            }
            mv.visitMethodInsn(INVOKESTATIC, "cop5556fa17/ImageSupport", "makeImage", makeImageSig,false);
            mv.visitFieldInsn(PUTSTATIC,className,declaration_Image.getName(),ImageDesc);
        }
        FieldVisitor fv;
        fv = cw.visitField (ACC_STATIC, declaration_Image.getName(), ImageDesc,null, null);
        fv.visitEnd();
        return null;
	}
	
  
	@Override
	public Object visitSource_StringLiteral(Source_StringLiteral source_StringLiteral, Object arg) throws Exception {
		// TODO HW6
        String s = source_StringLiteral.getFileOrUrl();
        mv.visitLdcInsn(s);
        return null;
	}


	@Override
	public Object visitSource_CommandLineParam(Source_CommandLineParam source_CommandLineParam, Object arg)
			throws Exception {
		// TODO
        Expression expression = source_CommandLineParam.getParamNum();
        expression.visit(this,null);
        Expression_IntLit e = (Expression_IntLit) expression;
        mv.visitVarInsn(ALOAD, 0); // load local variable arg at slot 0 on stack
        int index = e.value;
        mv.visitLdcInsn(index); // load the index on stack
        mv.visitInsn(AALOAD);
        return null;
	}

	@Override
	public Object visitSource_Ident(Source_Ident source_Ident, Object arg) throws Exception {
		// TODO HW6
        String name = source_Ident.getName();
        mv.visitLdcInsn(name);
        return null;
	}


	@Override
	public Object visitDeclaration_SourceSink(Declaration_SourceSink declaration_SourceSink, Object arg)
			throws Exception {
		// TODO HW6
        String name = declaration_SourceSink.getName();
        FieldVisitor fv;
        if(declaration_SourceSink.getSource()!=null) {
            declaration_SourceSink.getSource().visit(this, null);
            mv.visitFieldInsn(PUTSTATIC, className, declaration_SourceSink.getName(), "Ljava/lang/String;");
        }
        fv = cw.visitField(ACC_STATIC, name,"Ljava/lang/String;",null, declaration_SourceSink.getSource().getFirstToken().getText());
        fv.visitEnd();
        return null;
	}
	

	@Override
	public Object visitExpression_IntLit(Expression_IntLit expression_IntLit, Object arg) throws Exception {
        mv.visitLdcInsn(expression_IntLit.value);
		return null;
	}

	@Override
	public Object visitExpression_FunctionAppWithExprArg(
			Expression_FunctionAppWithExprArg expression_FunctionAppWithExprArg, Object arg) throws Exception {
		// TODO HW6
        expression_FunctionAppWithExprArg.arg.visit(this,arg);
        if(expression_FunctionAppWithExprArg.function== Scanner.Kind.KW_abs) {
            mv.visitMethodInsn(INVOKESTATIC,"cop5556fa17/RuntimeFunctions","abs",absSig,false);
        } else if(expression_FunctionAppWithExprArg.function== Scanner.Kind.KW_log) {
            mv.visitMethodInsn(INVOKESTATIC,"cop5556fa17/RuntimeFunctions","log",logSig,false);
        } else {
            throw new RuntimeException("Unexpected operator in slot kind: " +
                    String.valueOf(expression_FunctionAppWithExprArg.function));
        }
        return null;
	}

	@Override
	public Object visitExpression_FunctionAppWithIndexArg(
			Expression_FunctionAppWithIndexArg expression_FunctionAppWithIndexArg, Object arg) throws Exception {
		// TODO HW6
        expression_FunctionAppWithIndexArg.arg.getE0().visit(this,arg);
        expression_FunctionAppWithIndexArg.arg.getE1().visit(this,arg);
        switch (expression_FunctionAppWithIndexArg.function) {
            case KW_cart_x:
                mv.visitMethodInsn(INVOKESTATIC,"cop5556fa17/RuntimeFunctions","cart_x",cart_xSig,false);
                break;
            case KW_cart_y:
                mv.visitMethodInsn(INVOKESTATIC,"cop5556fa17/RuntimeFunctions","cart_y",cart_ySig,false);
                break;
            case KW_polar_a:
                mv.visitMethodInsn(INVOKESTATIC,"cop5556fa17/RuntimeFunctions","polar_a",polar_aSig,false);
                break;
            case KW_polar_r:
                mv.visitMethodInsn(INVOKESTATIC,"cop5556fa17/RuntimeFunctions","polar_r",polar_rSig,false);
                break;
        }
        return null;
	}

	@Override
	public Object visitExpression_PredefinedName(Expression_PredefinedName expression_PredefinedName, Object arg)
			throws Exception {
		// TODO HW6
        Scanner.Kind kind = expression_PredefinedName.kind;
        switch (kind) {
            case KW_DEF_X:
                mv.visitLdcInsn(256);
                break;
            case KW_DEF_Y:
                mv.visitLdcInsn(256);
                break;
            case KW_Z:
                mv.visitLdcInsn(0xFFFFFF);
                break;
            case KW_x:
                mv.visitVarInsn(ILOAD,3);
                break;
            case KW_y:
                mv.visitVarInsn(ILOAD,4);
                break;
            case KW_X:
                mv.visitVarInsn(ILOAD,1);
                break;
            case KW_Y:
                mv.visitVarInsn(ILOAD,2);
                break;
            case KW_r:
                mv.visitVarInsn(ILOAD,5);
                break;
            case KW_a:
                mv.visitVarInsn(ILOAD,6);
                break;
            case KW_R:
                mv.visitVarInsn(ILOAD,7);
                break;
            case KW_A:
                mv.visitVarInsn(ILOAD,8);
                break;
            default:
                throw new RuntimeException("Unexpected operator in slot kind: " + String.valueOf(kind));
        }
        return null;
	}

	/** For Integers and booleans, the only "sink" is the screen, so generate code to print to console.
	 * For Images, load the Image onto the stack and visit the Sink which will generate the code to handle the image.
	 */
	@Override
	public Object visitStatement_Out(Statement_Out statement_Out, Object arg) throws Exception {
		// TODO in HW5:  only INTEGER and BOOLEAN
		// TODO HW6 remaining cases

        if(statement_Out.getDec().getTypename()== TypeUtils.Type.INTEGER) {
            mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
            mv.visitFieldInsn(GETSTATIC, className, statement_Out.getName(),"I");
            CodeGenUtils.genLogTOS(GRADE,mv, TypeUtils.Type.INTEGER);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(I)V",false);
        } else if(statement_Out.getDec().getTypename()== TypeUtils.Type.BOOLEAN) {
            mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
            mv.visitFieldInsn(GETSTATIC, className, statement_Out.getName(),"Z");
            CodeGenUtils.genLogTOS(GRADE,mv, TypeUtils.Type.BOOLEAN);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Z)V",false);
        } else if(statement_Out.getDec().getTypename()== TypeUtils.Type.IMAGE) {
            mv.visitFieldInsn(GETSTATIC,className,statement_Out.getName(),ImageDesc);
            CodeGenUtils.genLogTOS(GRADE,mv, TypeUtils.Type.IMAGE);
            statement_Out.getSink().visit(this,arg);
        } else if(statement_Out.getDec().getTypename()== TypeUtils.Type.URL) {

        } else if(statement_Out.getDec().getTypename()== TypeUtils.Type.FILE) {

        } else {
            throw new RuntimeException("Unexpected type in statement_out: " + statement_Out.getDec().getTypename());
        }
        return null;
	}

	/**
	 * Visit source to load rhs, which will be a String, onto the stack
	 * 
	 *  In HW5, you only need to handle INTEGER and BOOLEAN
	 *  Use java.lang.Integer.parseInt or java.lang.Boolean.parseBoolean 
	 *  to convert String to actual type. 
	 *  
	 *  TODO HW6 remaining types
	 */
	@Override
	public Object visitStatement_In(Statement_In statement_In, Object arg) throws Exception {
		// TODO (see comment )
        Source source = statement_In.getSource();
        source.visit(this,null);

        if(statement_In.getDec().getTypename().isType(TypeUtils.Type.INTEGER)) {
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "parseInt", "(Ljava/lang/String;)I",false);
            mv.visitFieldInsn(PUTSTATIC, className, statement_In.name, "I");
        }
        else if(statement_In.getDec().getTypename().isType(TypeUtils.Type.BOOLEAN)){
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "parseBoolean", "(Ljava/lang/String;)Z",false);
            mv.visitFieldInsn(PUTSTATIC, className, statement_In.name, "Z");
        }else if (statement_In.getDec().getTypename().isType(TypeUtils.Type.IMAGE)){
            Label l1 = new Label();
            Label l2 = new Label();
            mv.visitVarInsn(ILOAD,1);
            mv.visitJumpInsn(IFEQ, l1);

            mv.visitVarInsn(ILOAD,1);
            mv.visitMethodInsn(INVOKESTATIC,"java/lang/Integer","valueOf","(I)Ljava/lang/Integer;",false);
            mv.visitVarInsn(ILOAD,2);
            mv.visitMethodInsn(INVOKESTATIC,"java/lang/Integer","valueOf","(I)Ljava/lang/Integer;",false);

            mv.visitJumpInsn(GOTO, l2);
            mv.visitLabel(l1);
            mv.visitInsn(ACONST_NULL);
            mv.visitInsn(ACONST_NULL);
            mv.visitLabel(l2);

            mv.visitMethodInsn(INVOKESTATIC, "cop5556fa17/ImageSupport", "readImage", readImageSig,false);
            mv.visitFieldInsn(PUTSTATIC,className,statement_In.getName(),ImageDesc);
        }else if (statement_In.getDec().getTypename().isType(TypeUtils.Type.FILE)){

        }else if (statement_In.getDec().getTypename().isType(TypeUtils.Type.URL)){

        }else {
            throw new RuntimeException("Unexpected type in statement_In: " + source.getTypename());
        }
        return null;
	}

	
	/**
	 * In HW5, only handle INTEGER and BOOLEAN types.
	 */
	@Override
	public Object visitStatement_Assign(Statement_Assign statement_Assign, Object arg) throws Exception {
		//TODO  (see comment)
        if(statement_Assign.getLHS().getTypename() == TypeUtils.Type.INTEGER
                || statement_Assign.getLHS().getTypename() == TypeUtils.Type.BOOLEAN) {
            statement_Assign.e.visit(this, arg);
            statement_Assign.lhs.visit(this, arg);
        } else if(statement_Assign.getLHS().getTypename() == TypeUtils.Type.IMAGE) {

            Label l0 = new Label();
            Label l1 = new Label();
            Label l2 = new Label();
            Label l3 = new Label();
            Label l4 = new Label();
            Label l5 = new Label();
            Label l6 = new Label();
            Label l7 = new Label();

            mv.visitFieldInsn(GETSTATIC, className, statement_Assign.lhs.getName(), ImageDesc);
            mv.visitMethodInsn(INVOKESTATIC,"cop5556fa17/ImageSupport","getX",getXSig,false);
            mv.visitVarInsn(ISTORE,1);
            mv.visitFieldInsn(GETSTATIC, className, statement_Assign.lhs.getName(), ImageDesc);
            mv.visitMethodInsn(INVOKESTATIC,"cop5556fa17/ImageSupport","getY",getYSig,false);
            mv.visitVarInsn(ISTORE,2);

            mv.visitLabel(l0);
            mv.visitLineNumber(3,l0);
            mv.visitLdcInsn(0);
            mv.visitVarInsn(ISTORE,3);
            mv.visitLabel(l1);
            mv.visitVarInsn(ILOAD, 3);
            mv.visitVarInsn(ILOAD, 1); //X
            mv.visitJumpInsn(IF_ICMPGE,l2);
            mv.visitLabel(l3);
            mv.visitLineNumber(4,l3);
            mv.visitLdcInsn(0);
            mv.visitVarInsn(ISTORE,4);
            mv.visitLabel(l4);
            mv.visitVarInsn(ILOAD, 4);
            mv.visitVarInsn(ILOAD, 2); // Y
            mv.visitJumpInsn(IF_ICMPGE,l5);
            mv.visitLabel(l6);
            mv.visitLineNumber(5,l6);

            mv.visitVarInsn(ILOAD, 3); //should be slot x and y
            mv.visitVarInsn(ILOAD, 4);
            mv.visitMethodInsn(INVOKESTATIC,"cop5556fa17/RuntimeFunctions","polar_r",polar_rSig,false);
            mv.visitVarInsn(ISTORE, 5); //should be r
            mv.visitVarInsn(ILOAD, 3); //should be slot x and y
            mv.visitVarInsn(ILOAD, 4);
            mv.visitMethodInsn(INVOKESTATIC,"cop5556fa17/RuntimeFunctions","polar_a",polar_aSig,false);
            mv.visitVarInsn(ISTORE, 6); //should be a

            statement_Assign.e.visit(this, arg);
            statement_Assign.lhs.visit(this, arg);

            mv.visitLabel(l7);
            mv.visitLineNumber(4,l7);
            mv.visitIincInsn(4,1);
            mv.visitJumpInsn(GOTO, l4);

            mv.visitLabel(l5);
            mv.visitLineNumber(3,l5);
            mv.visitIincInsn(3,1);
            mv.visitJumpInsn(GOTO, l1);

            mv.visitLabel(l2);
            mv.visitLineNumber(8,l2);
        } else {
            throw new RuntimeException("Unexpected type in statement_Assign: " + sourceFileName);
        }
        return null;
    }

	/**
	 * In HW5, only handle INTEGER and BOOLEAN types.
	 */
	@Override
	public Object visitLHS(LHS lhs, Object arg) throws Exception {
		//TODO  (see comment)
        TypeUtils.Type type = lhs.getTypename();
        switch(type){
            case INTEGER:
                mv.visitFieldInsn(PUTSTATIC, className, lhs.name, "I");
                break;
            case BOOLEAN:
                mv.visitFieldInsn(PUTSTATIC, className, lhs.name, "Z");
                break;
            case IMAGE:
                mv.visitFieldInsn(GETSTATIC,className,lhs.name,ImageDesc);
                mv.visitVarInsn(ILOAD,3);
                mv.visitVarInsn(ILOAD,4);
                mv.visitMethodInsn(INVOKESTATIC,"cop5556fa17/ImageSupport","setPixel",setPixelSig,false);
                break;
            case FILE:
            case URL:
            default:
                throw new RuntimeException("Unexpected type in LHS: " + type);
        }

        return null;
	}
	

	@Override
	public Object visitSink_SCREEN(Sink_SCREEN sink_SCREEN, Object arg) throws Exception {
		//TODO HW6
        mv.visitMethodInsn(INVOKESTATIC, "cop5556fa17/ImageSupport", "makeFrame", makeFrameSig,false);
        mv.visitInsn(POP);
        return null;
	}

	@Override
	public Object visitSink_Ident(Sink_Ident sink_Ident, Object arg) throws Exception {
		//TODO HW6
        mv.visitFieldInsn(GETSTATIC,className,sink_Ident.name,"Ljava/lang/String;");
        mv.visitMethodInsn(INVOKESTATIC, "cop5556fa17/ImageSupport", "write", writeSig,false);
        return null;
    }

	@Override
	public Object visitExpression_BooleanLit(Expression_BooleanLit expression_BooleanLit, Object arg) throws Exception {
        mv.visitLdcInsn(expression_BooleanLit.isValue());
		return null;
	}

	@Override
	public Object visitExpression_Ident(Expression_Ident expression_Ident,
			Object arg) throws Exception {
		//TODO
        TypeUtils.Type type = expression_Ident.getTypename();

        switch(type){
            case INTEGER:
                mv.visitFieldInsn(GETSTATIC, className, expression_Ident.name, "I");
                break;
            case BOOLEAN:
                mv.visitFieldInsn(GETSTATIC, className, expression_Ident.name, "Z");
                break;
            case IMAGE:
            case FILE:
            case URL:
                throw new RuntimeException("Unexpected type in Expression_Ident in file or url: " + type);
            default:
                throw new RuntimeException("Unexpected type in Expression_Ident: " + type);
        }
		return null;
	}

}
