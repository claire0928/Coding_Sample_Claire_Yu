package cop5556fa17.AST;

import cop5556fa17.Scanner.Token;
import cop5556fa17.TypeUtils;

public abstract class Sink extends ASTNode {
	
	public Sink(Token firstToken) {
		super(firstToken);
	}

	TypeUtils.Type typename;
	public void setTypename(TypeUtils.Type name){ typename = name; }
	public TypeUtils.Type getTypename(){ return typename; }


}
