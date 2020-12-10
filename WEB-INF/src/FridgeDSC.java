import java.sql.*;
import java.util.*;
import java.io.File;
import java.io.PrintWriter;
import java.util.Scanner;
import java.time.LocalDate;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import com.mysql.jdbc.Driver;

/*
 * @Author Connor Parker
 * @ID 20140728
 * CSE3OAD
 * */
public class FridgeDSC {

    // the date format we will be using across the application
    public static final String DATE_FORMAT = "dd/MM/yyyy";

    /*
        FREEZER, // freezing cold
        MEAT, // MEAT cold
        COOLING, // general fridge area
        CRISPER // veg and fruits section

        note: Enums are implicitly public static final
    */
    public enum SECTION {
        FREEZER,
        MEAT,
        COOLING,
        CRISPER
    };

    private Connection connection;
    private Statement statement;
    private PreparedStatement preparedStatement;

    private String dbUserName;
    private String dbPassword;
    private String dbURL;

    public FridgeDSC(String dbHost, String dbUserName, String dbPassword) {
        this.dbUserName = dbUserName;
        this.dbPassword = dbPassword;
        dbURL = "jdbc:mysql://" + String.join("/", dbHost, this.dbUserName);
    }

    public void connect() throws SQLException {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection(dbURL, dbUserName, dbPassword);
            statement = connection.createStatement();
        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
        }
    }

    public void disconnect() throws SQLException {
        if (preparedStatement != null) preparedStatement.close();
        if (statement != null) statement.close();
        if (connection != null) connection.close();
    }


    public Item searchItem(String name) throws SQLException {
        String queryString = "SELECT * FROM item WHERE name = ?";
        preparedStatement = connection.prepareStatement(queryString);
        preparedStatement.setString(1, name);
        ResultSet rs = preparedStatement.executeQuery();

        Item item = null;

        if (rs.next()) { // i.e. the item exists
            boolean expires = rs.getBoolean(2);
            item = new Item(name, expires);
        }

        return item;
    }

    public Grocery searchGrocery(int id) throws SQLException {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(DATE_FORMAT);
        String queryString = "SELECT * FROM grocery WHERE id = ?";
        preparedStatement = connection.prepareStatement(queryString);
        preparedStatement.setInt(1, id);
        ResultSet rs = preparedStatement.executeQuery();

        Grocery grocery = null;

        if (rs.next()) { // i.e. the grocery exists
            String itemName = rs.getString(2);
            Item item = searchItem(itemName);
            if (item == null) {
                System.err.println("[WARNING] Item: '" + itemName + "'' does not exist!");
            }
            LocalDate date = LocalDate.parse(rs.getString(3), dtf);
            int quantity = rs.getInt(4);
            FridgeDSC.SECTION section = SECTION.valueOf(rs.getString(5));

            grocery = new Grocery(id, item, date, quantity, section);

        }

        return grocery;
    }

    public List<Item> getAllItems() throws Exception {
        String queryString = "SELECT * FROM item";
        ResultSet rs = statement.executeQuery(queryString);

        List<Item> items = new ArrayList<Item>();

        while (rs.next()) { // i.e. items exists
            String name = rs.getString(1);
            boolean expires = rs.getBoolean(2);
            items.add(new Item(name, expires));
        }

        return items;
    }

    public List<Grocery> getAllGroceries() throws SQLException {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(DATE_FORMAT);
        String queryString = "SELECT * FROM grocery";
        ResultSet rs = statement.executeQuery(queryString);

        List<Grocery> groceries = new ArrayList<Grocery>();

        while (rs.next()) { // i.e. groceries exists
            int id = rs.getInt(1);
            String itemName = rs.getString(2);
            Item item = searchItem(itemName);
            if (item == null) {
                System.err.println("[WARNING] Item: '" + itemName + "'' does not exist!");
                continue;
            }
            LocalDate date = LocalDate.parse(rs.getString(3), dtf);
            int quantity = rs.getInt(4);
            SECTION section = SECTION.valueOf(rs.getString(5));

            groceries.add(new Grocery(id, item, date, quantity, section));
        }

        return groceries;
    }


    public int addGrocery(String name, int quantity, SECTION section) throws Exception {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(DATE_FORMAT);
        LocalDate date = LocalDate.now();
        String dateStr = date.format(dtf);

        // NOTE: should we check if itemName (argument name) exists in item table?
        //		--> adding a groceries with a non-existing item name should through an exception


        String command = "INSERT INTO grocery VALUES(?, ?, ?, ?, ?)";
        preparedStatement = connection.prepareStatement(command);

        preparedStatement.setInt(1, 0);
        preparedStatement.setString(2, name);
        preparedStatement.setString(3, dateStr);
        preparedStatement.setInt(4, quantity);
        preparedStatement.setString(5, section.toString());

        preparedStatement.executeUpdate();

        ResultSet rs = statement.executeQuery("SELECT LAST_INSERT_ID()");
        rs.next();
        int newId = rs.getInt(1);

        return newId;
    }

    public Grocery useGrocery(int id) throws Exception {
        Grocery g = searchGrocery(id);

        if (g == null)
            return null;

        if (g.getQuantity() <= 1)
            throw new UpdateNotAllowedException("There is only one: " + g.getItemName() + " (bought on " + g.getDateStr() + ") - use DELETE instead.");

        String queryString =
                "UPDATE grocery " +
                        "SET quantity = quantity - 1 " +
                        "WHERE quantity > 1 " +
                        "AND id = " + id + ";";

        if (statement.executeUpdate(queryString) > 0)
            return searchGrocery(id);
        else return null;
    }

    public int removeGrocery(int id) throws Exception {
        String queryString = "SELECT COUNT(*) FROM grocery WHERE id = ?";
        preparedStatement = connection.prepareStatement(queryString);
        preparedStatement.setInt(1, id);
        ResultSet rs = preparedStatement.executeQuery();

        // are there any results
        boolean pre = rs.next();
        if (!pre) { // no, throw error
            throw new RuntimeException("The grocery does not exist!");
        }

        // there are results, proceed with delete
        return statement.executeUpdate("DELETE FROM grocery WHERE id = " + id);
    }

    // STATIC HELPERS -------------------------------------------------------

    public static long calcDaysAgo(LocalDate date) {
        return Math.abs(Duration.between(LocalDate.now().atStartOfDay(), date.atStartOfDay()).toDays());
    }

    public static String calcDaysAgoStr(LocalDate date) {
        String formattedDaysAgo;
        long diff = calcDaysAgo(date);

        if (diff == 0)
            formattedDaysAgo = "today";
        else if (diff == 1)
            formattedDaysAgo = "yesterday";
        else formattedDaysAgo = diff + " days ago";

        return formattedDaysAgo;
    }


    public static void main(String[] args) throws Exception {
        FridgeDSC dsc = new FridgeDSC("127.0.0.1:3306/fridgedb?'", "root", "");
        try {
            dsc.connect();
            List<Grocery> list = dsc.getAllGroceries();
            System.out.println(list);
            System.out.println(dsc.getAllGroceries());
            System.out.println(dsc.removeGrocery(455));
            System.out.println(dsc.useGrocery(455));
            System.out.println(dsc.useGrocery(19));
        } catch (Exception exp) {
            exp.printStackTrace();
        }
    }
}