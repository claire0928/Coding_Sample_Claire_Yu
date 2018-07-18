package cop5556fa17;

import cop5556fa17.AST.Declaration;
import java.util.HashMap;

public class SymbolTable {

	HashMap<String,Declaration> map;

	public boolean insert(String ident, Declaration dec){
        Declaration value = map.get(ident);
        if (value != null) {
            return false;
        } else {
            map.put(ident,dec);
        }
		return true;
	}
	
	public Declaration lookupDec(String ident){
        Declaration ent=map.get(ident);
		return ent;
	}

    public TypeUtils.Type lookupType(String ident){
        Declaration ent=map.get(ident);
        return ent.getTypename();
    }
     public boolean contain(String ident){
        return map.containsKey(ident);
     }
		
	public SymbolTable() {
        map=new HashMap<>();
	}

	@Override
	public String toString() {
		return "";
	}
}
