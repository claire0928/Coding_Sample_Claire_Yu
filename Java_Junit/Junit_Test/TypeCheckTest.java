import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class TypeCheckTest {

	// set Junit to be able to catch exceptions
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	// To make it easy to print objects and turn this output on and off
	static final boolean doPrint = true;
	private void show(Object input) {
		if (doPrint) {
			System.out.println(input.toString());
		}
	}
	
	
	/**
	 * Scans, parses, and type checks given input String.
	 * 
	 * Catches, prints, and then rethrows any exceptions that occur.
	 * 
	 * @param input
	 * @throws Exception
	 */
	void typeCheck(String input) throws Exception {
		show(input);
		try {
			Scanner scanner = new Scanner(input).scan();
			ASTNode ast = new Parser(scanner).parse();
			show(ast);
			ASTVisitor v = new TypeCheckVisitor();
			ast.visit(v, null);
		} catch (Exception e) {
			show(e);
			throw e;
		}
	}

	/**
	 * Simple test case with an almost empty program.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testSmallest() throws Exception {
		String input = "n"; //Smallest legal program, only has a name
		show(input); // Display the input
		Scanner scanner = new Scanner(input).scan(); // Create a Scanner and
														// initialize it
		show(scanner); // Display the Scanner
		Parser parser = new Parser(scanner); // Create a parser
		ASTNode ast = parser.parse(); // Parse the program
		TypeCheckVisitor v = new TypeCheckVisitor();
		String name = (String) ast.visit(v, null);
		show("AST for program " + name);
		show(ast);
	}
	
	/**
	 * This test should pass with a fully implemented assignment
	 * @throws Exception
	 */
	 @Test
	 public void testDec1() throws Exception {
	 String input = "prog int k = 10;";
	 typeCheck(input);
	 }

	@Test
	public void testDec2() throws Exception {
		String input = "prog image [1,2] pic1;";
		typeCheck(input);
	}

    @Test
    public void testDecx0() throws Exception {
        String input = "prog ident -> SCREEN;";
        typeCheck(input);
    }

    @Test
    public void testDecx1() throws Exception {
        String input = "prog int un = +10;";
        typeCheck(input);
    }

    @Test
    public void testDecx2() throws Exception {
        String input = "prog boolean k = 3 >= 4;";
        typeCheck(input);
    }

    @Test
    public void testDecx3() throws Exception {
        String input = "prog boolean k = 3 == 4;";
        typeCheck(input);
    }

    @Test
    public void imageGenRed() throws Exception {
        String prog = "imageGenRed";
        String input = prog
                + "\nimage[512,512] g; \n"
                + "g[[x,y]] = 16711680;"
                + "g -> SCREEN;\n"
                ;
        typeCheck(input);
    }

    @Test
    public void testDecx5() throws Exception {
        String input = "prog int k = 45 / 56;";
        typeCheck(input);
    }

    @Test
    public void testDecx6() throws Exception {
        String input = "prog int k = 5 % 10;";
        typeCheck(input);
    }

    @Test
    public void testDecx7() throws Exception {
        String input = "prog boolean k = 5 > 6 ? true : false;";
        typeCheck(input);
    }

    @Test
    public void testDecx8() throws Exception {
        String input = "prog boolean k = 5 == 6 ? 1 < 2 : 1 > 3;";
        typeCheck(input);
    }

    @Test
    public void testDecx9() throws Exception {
        String input = "prog int k = 5 > 6 ? 1 : 0;";
        typeCheck(input);
    }

    @Test
    public void testDecx10() throws Exception {
        String input = "prog int pixel; file f =  \"file\"; image img <- f; pixel = img [8,9];";
        typeCheck(input);
    }

    @Test
    public void testDec5() throws Exception {
        String input = "prog boolean abc; abc = true;";
        typeCheck(input);
    }

    @Test
    public void testDec6() throws Exception {
        String input = "prog int m = 2<1?-1:0;";
        typeCheck(input);
    }

    @Test
    public void testDec7() throws Exception {
        String input = "prog int m = 1; int n = 2; int c = m+n;";
        typeCheck(input);
    }

    @Test
    public void testDec8() throws Exception {
        String input = "prog int m = sin(4);";
        typeCheck(input);
    }

    @Test
    public void testDec9() throws Exception {
        String input = "prog int m = cos[1,2];";
        typeCheck(input);
    }

    @Test
    public void testDec10() throws Exception {
        String input = "prog image pixel; int m = pixel[1,2];";
        typeCheck(input);
    }

    @Test
    public void testDec11() throws Exception {
        String input = "p int n; n = sin(30)/cos(40);\n";
        typeCheck(input);
    }

    @Test
    public void testDec12() throws Exception {
        String input = "prog file f = \"file\"; image img <- f; img -> SCREEN;";
        typeCheck(input);
    }

    @Test
    public void testDec13() throws Exception {
        String input = "prog int answer;  answer -> SCREEN; ";
        typeCheck(input);
    }

    @Test
    public void testDec14() throws Exception {
        String input = "prog file s = \"some source\"; image i; i -> s; ";
        typeCheck(input);
    }

    /**
	  * This program does not declare k. The TypeCheckVisitor should
	  * throw a SemanticException in a fully implemented assignment.
	  * @throws Exception
	  */
	 @Test
	 public void testUndec() throws Exception {
	 String input = "prog k = 42;";
	 thrown.expect(SemanticException.class);
	 typeCheck(input);
	 }

    @Test
    public void testUndec2() throws Exception {
        String input = "prog int m = !x; m = true;";
        thrown.expect(SemanticException.class);
        typeCheck(input);
    }

    @Test
    public void testUndec3() throws Exception {
        String input = "prog url s1 = baidu;";
        thrown.expect(SemanticException.class);
        typeCheck(input);
    }

    @Test
    public void testUndec4() throws Exception {
        String input = "prog int ooo = 2;file pic1 = @ooo; file pic2 =  \"goo/*gle\"; pic1 <- pic2;";
        thrown.expect(SemanticException.class);
        typeCheck(input);
    }

    @Test
    public void testUndec5() throws Exception {
        String input = "prog file f = @3;";
        thrown.expect(SemanticException.class);
        typeCheck(input);
    }

    @Test
    public void testUndec6() throws Exception {
        String input = "prog int k = abc;";
        thrown.expect(SemanticException.class);
        typeCheck(input);
    }

    @Test
    public void testUndec7() throws Exception {
        String input = "prog int k = true;";
        thrown.expect(SemanticException.class);
        typeCheck(input);
    }

    @Test
    public void testUndec8() throws Exception {
        String input = "prog boolean k = 123;";
        thrown.expect(SemanticException.class);
        typeCheck(input);
    }

    @Test
    public void testUndec9() throws Exception {
        String input = "prog int k = k + 1 ;";
        thrown.expect(SemanticException.class);
        typeCheck(input);
    }

    @Test
    public void testUndec10() throws Exception {
        String input = "prog boolean bool = true; boolean bool2; bool -> bool2;";
        thrown.expect(SemanticException.class);
        typeCheck(input);
    }

    @Test
    public void testUndec11() throws Exception {
        String input = "prog int i; \n  i = 5 > 4;";
        thrown.expect(SemanticException.class);
        typeCheck(input);
    }


    @Test
    public void testUndec12() throws Exception {
        String input = "prog boolean k = 5 ? true : false;";
        thrown.expect(SemanticException.class);
        typeCheck(input);
    }

}
