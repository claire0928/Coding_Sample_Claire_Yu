package cop5556fa17;

import cop5556fa17.Scanner.Token;

public class TypeUtils {
	
	public static enum Type {
		INTEGER("I"),
		BOOLEAN("Z"),
		IMAGE("Ljava/awt/image/BufferedImage;"),
		URL("Ljava/net/URL;"),
		FILE("Ljava/io/File;"),
		SCREEN("Lcop5556sp17/PLPRuntimeScreen;"),
        KW_r("I"),
        KW_a("I"),
        KW_R("I"),
        KW_A("I"),
        KW_x("I"),
        KW_X("I"),
        KW_y("I"),
        KW_Y("I"),
        KW_DEF_X("I"),
        KW_DEF_Y("I"),
        KW_cart_x("I"),
        KW_cart_y("I"),
        KW_polar_a("I"),
        KW_polar_r("I"),
		NONE(null);

        public boolean isType(Type... types){
            for (Type type: types){
                if (type.equals(this)) return true;
            }
            return false;
        }

        Type(String jvmType){
            this.jvmType = jvmType;
        }

        String jvmType;

        public String getJVMTypeDesc() {
            return jvmType;
        }

        //precondition: is not I or Z
        public String getJVMClass(){
            return jvmType.substring(1,jvmType.length()-1);  //removes L and ;
        }


    }


	public static Type getType(Token token){
		switch (token.kind){
		case KW_int: {return Type.INTEGER;} 
		case KW_boolean: {return Type.BOOLEAN;} 
		case KW_image: {return Type.IMAGE;} 
		case KW_url: {return Type.URL;} 
		case KW_file: {return Type.FILE;}

		    case KW_a:{return Type.INTEGER;}
		    case KW_r:{return Type.INTEGER;}
            case KW_A:{return Type.INTEGER;}
            case KW_R:{return Type.INTEGER;}
            case KW_X:{return Type.INTEGER;}
            case KW_Y:{return Type.INTEGER;}
            case KW_x:{return Type.INTEGER;}
            case KW_y:{return Type.INTEGER;}
            case KW_cart_x:{return Type.INTEGER;}
            case KW_cart_y:{return Type.INTEGER;}
            case KW_DEF_X:{return Type.INTEGER;}
            case KW_DEF_Y:{return Type.INTEGER;}

		default :
				break; 
		}
		assert false;  //should not reach here
		return null;  
	}
}
