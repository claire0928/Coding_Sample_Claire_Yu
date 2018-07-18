package cop5556fa17.AST;

import cop5556fa17.Scanner.Token;
import cop5556fa17.TypeUtils;

public abstract class Expression extends ASTNode {

	TypeUtils.Type typename;
	public void setTypename(TypeUtils.Type name){ typename = name; }
	public TypeUtils.Type getTypename(){ return typename; }

    int slot=-1;
    public  int getSlot(){
        return slot;
    }
    public void setSlot(int s){
        slot=s;
    }

	public Expression(Token firstToken) {
		super(firstToken);
	}

    @Override
    abstract public Object visit(ASTVisitor v, Object arg) throws Exception;

}
