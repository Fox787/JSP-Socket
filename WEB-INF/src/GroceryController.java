import java.time.LocalDate;
import java.util.List;


/*
 * @Author Connor Parker
 * @ID 20140728
 * CSE3OAD
 * */
public class GroceryController {

	private FridgeDSC fridgeDSC;

	public GroceryController(String dbHost, String dbUserName, String dbPassword) throws Exception {
		fridgeDSC = new FridgeDSC(dbHost, dbUserName, dbPassword);

		try {
			fridgeDSC.connect();
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
	}

	public List<Grocery> get() throws Exception {
	return fridgeDSC.getAllGroceries();
	}

	public Grocery get(int id) throws Exception {
		return fridgeDSC.searchGrocery(id);
	}

	public int add(Grocery g) throws Exception {
        try {
            Validator.validate(g);
        }catch (ValidationException ve) {
            ve.printStackTrace();
        }

        //Precondtitons : Grocery doesn't exist in db
        Grocery temp = get(g.getId());
        boolean pre = (temp == null);
        if(!pre){
             String msg = ("Id exists");
             System.out.println("\nError "+ msg);
        }else {
            fridgeDSC.connect();
            return fridgeDSC.addGrocery(g.getItemName(), g.getQuantity(), g.getSection());
        }
        return -1;
	}
	public Grocery update(int id) throws Exception {
        fridgeDSC.useGrocery(id);
        return fridgeDSC.searchGrocery(id);

	}

	public int delete(int id) throws Exception {
        //Check item exists
        fridgeDSC.connect();
        Grocery g = fridgeDSC.searchGrocery(id);
        boolean pre = (g == null);
        int result = -1;
        if(pre){
            System.out.println("id doesn't exist");
        }{
            result = fridgeDSC.removeGrocery(id);
            fridgeDSC.disconnect();
        }
		return result;
	}

	// To perform some quick tests
	public static void main(String [] args) throws Exception {
		// CONSIDER testing each of the above methods here
		// NOTE: this is not a required task, but will help you test your Task 2 requirements
		try {
			GroceryController gc = new GroceryController("127.0.0.1:3306/fridgedb?'", "root", "");

			System.out.println(gc.get());
			System.out.println(gc.get(5));
			int id = gc.add(new Grocery(40,gc.fridgeDSC.searchItem("Oranges"),(LocalDate.of(2020,10,20)),20,FridgeDSC.SECTION.CRISPER));
			System.out.println(gc.update(id));
			System.out.println(gc.delete(id));

		} catch (Exception exp) {
			exp.printStackTrace();
		}

	}
}