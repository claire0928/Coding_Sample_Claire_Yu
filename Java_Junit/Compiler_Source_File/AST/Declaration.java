package cop5556fa17.AST;

import cop5556fa17.Scanner.Token;
import cop5556fa17.TypeUtils;

public abstract class Declaration extends ASTNode {

    public final String name;

	public Declaration(Token firstToken, Token name) {
		super(firstToken);
        this.name = name.getText();
	}

    public String getName() {
        return name;
    }

    TypeUtils.Type typename;
	public void setTypename(TypeUtils.Type name){
		typename=name;
	}
	public TypeUtils.Type getTypename(){
		return typename;
	}

}
