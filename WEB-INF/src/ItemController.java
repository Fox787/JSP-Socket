import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/*
 * @Author Connor Parker
 * @ID 20140728
 * CSE3OAD
 * */
public class ItemController {

	private FridgeDSC fridgeDSC;

	public ItemController(String dbHost, String dbUserName, String dbPassword) throws Exception {
		fridgeDSC = new FridgeDSC(dbHost, dbUserName, dbPassword);

		try {
			fridgeDSC.connect();
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
	}

	public List<Item> get() throws Exception {
	fridgeDSC.connect();
	return fridgeDSC.getAllItems();
	}

	// To perform some quick tests
	public static void main(String [] args) throws Exception {
		// CONSIDER testing each of the above methods here
		// NOTE: this is not a required task, but will help you test your Task 2 requirements
		try {
			ItemController ic = new ItemController("127.0.0.1:3306/fridgedb?'", "root", "");;
			System.out.println(ic.get());


		} catch (Exception exp) {
			exp.printStackTrace();
		}

	}
}