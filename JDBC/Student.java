import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.sql.*;
import java.util.*;
import org.apache.ibatis.jdbc.ScriptRunner;

public class Student{
    static Connection con;
    static Statement stmt;

    public static void main(String argv[]) {
        login();
        promptForFile();
        printMenu();

    }

    /**
     * Prompt user for name of script file.
     * And execute script file.
     */
    public static void promptForFile() {
        Scanner scan = new Scanner(System.in);
        ScriptRunner scriptRunner = new ScriptRunner(con);
        Reader reader;
        String locationOfFile = "";
        boolean done = false;

        do {
            try {
                System.out.println("Enter fully qualified path of your oracle.sql script file: ");
                locationOfFile = scan.nextLine();

                reader = new BufferedReader(new FileReader(locationOfFile));
                // Run sql script.
                scriptRunner.runScript(reader);
                done = true;
            } catch (FileNotFoundException ex) {
                System.out.println("File not found " + ex.getMessage());
            } catch (Exception ex) {
                System.out.println("Error running script: " + ex.getMessage());
            }
        } while (!done);

    }

    /**
     * Prompt user for username and password to connect to database.
     */
    public static void login() {
        Scanner scan = new Scanner(System.in);
        String username = "";
        String password = "";
        boolean done;

        do {

            System.out.println("Enter your username: ");
            username = scan.nextLine();

            System.out.println("Enter your password: ");
            password = scan.nextLine();

            done = connectToDatabase(username, password);

        } while (!done);

    }

    /**
     * Prompts user to choose option from menu.
     */
    public static void getUserAction() {
        System.out.println();
        System.out.println("1.\tView table contents");
        System.out.println("2.\tSearch by Transaction_ID");
        System.out.println("3.\tSearch by one or more attributes");
        System.out.println("4.\tExit");
    }

    /**
     * Displays menu of options for user to select.
     */
    public static void printMenu() {
        Scanner scan = new Scanner(System.in);
        int option = 0;

        do {
            try {
                // Prompt user to choose option.
                getUserAction();

                // Store users choice.
                option = scan.nextInt();

                switch (option) {
                    case 1:
                        System.out.println("View table contents");
                        viewTableContents();
                        break;
                    case 2:
                        System.out.println("Search by Transaction_ID");
                        searchByTransactionID();
                        break;
                    case 3:
                        System.out.println("Search by one or more attributes");
                        searchByOneOrMoreAttributes();
                        break;
                    case 4:
                        System.out.println("Exit");
                        break;
                    default:
                        System.out.println("The option is invalid.");
                }
            } catch (InputMismatchException ime) {
                System.out.println("Option is invalid, Input must be a number between 1 and 4.");
                scan.nextLine();
            }
        } while (option != 4);
        scan.close();

        try {
            if (con != null) {
                con.close();
            }
        } catch (SQLException ex) {
            System.out.println("Error closing connection: " + ex.getMessage());
        }
    }

    /**
     * Display content of each table.
     */
    public static void viewTableContents() {
        if (promptForYesNo("Product")) {
            showProductTableContent();
        }
        if (promptForYesNo("Customer")) {
            showCustomerTableContent();
        }
        if (promptForYesNo("Transactions")) {
            showTransactionsTableContent();
        }
        if (promptForYesNo("Transaction_Contains")) {
            showTransactionContainsTableContent();
        }
    }

    /**
     * Dynamic query to allow users to select what data they want to view
     * from the Transactions and Transaction_Contains tables.
     */
    public static void searchByOneOrMoreAttributes() {
        StringBuffer buffer = new StringBuffer(100);
        String header = "";
        String query = "";
        int customerID = 0;
        double total = 0.0;
        String UPC = "";
        int quantity = 0;

        System.out.println("\nInput Fields:");

        // Input attributes.
        boolean useCustomerID = promptForYesNo("Customer ID");
        boolean useTotal = promptForYesNo("Total");
        boolean useUPC = promptForYesNo("UPC");
        boolean useQuantity = promptForYesNo("quantity");

        // Check if user wants to enter their Customer ID.
        if (useCustomerID) {
            customerID = getIntInput("Enter Customer ID: ");
        }
        // Check if user wants enter their Total.
        if (useTotal) {
            total = getDoubleInput("Enter total: ");
        }
        // Check if user wants to enter their UPC.
        if (useUPC) {
            System.out.println("Enter _ for single wildcard character, % for 0 or more" +
            " wildcard characters");
            UPC = getStringInput("Enter UPC: ");
        }
        // Check if user wants to enter their Quantity.
        if (useQuantity) {
            quantity = getIntInput("Enter quantity: ");
        }

        if (!useCustomerID && !useTotal && !useUPC && !useQuantity) {
            System.out.println("Error, no input fields specified.");
            return;
        }

        System.out.println("\nOutput Fields:");

        // Output attributes.
        boolean displayTID = promptForYesNo("transaction_ID");
        boolean displayCID = promptForYesNo("customer_ID");
        boolean displayTDate = promptForYesNo("transaction_date");
        boolean displayPMethod = promptForYesNo("payment_method");
        boolean displayTotal = promptForYesNo("total");
        boolean displayUPC = promptForYesNo("UPC");
        boolean displayQuantity = promptForYesNo("quantity");

        if (!displayTID && !displayCID && !displayTDate && !displayPMethod && !displayTotal && !displayUPC &&
            !displayQuantity) {
            System.out.println("Error, no output fields specified.");
            return;
        }

        System.out.println();
        boolean useDistinct = promptForYesNo("DISTINCT");

        // Add SELECT clause to query.
        buffer.append("SELECT ");
        if (useDistinct) {
            buffer.append("DISTINCT ");
        }
        // Add Transaction ID to SELECT clause.
        if (displayTID) {
            buffer.append("T.transaction_ID");
        }
        // Add Customer ID to SELECT clause.
        if (displayCID) {
            if (displayTID) {
                buffer.append(", ");
            }
            buffer.append("T.customer_ID");
        }
        // Add Transaction Date to SELECT clause.
        if (displayTDate) {
            if (displayTID || displayCID) {
                buffer.append(", ");
            }
            buffer.append("T.transaction_date");
        }
        // Add payment method to SELECT clause.
        if (displayPMethod) {
            if (displayTID || displayCID || displayTDate) {
                buffer.append(", ");
            }
            buffer.append("T.payment_method");
        }
        // Add total to SELECT clause.
        if (displayTotal) {
            if (displayTID || displayCID || displayTDate || displayPMethod) {
                buffer.append(", ");
            }
            buffer.append("T.total");
        }
        // Add UPC to SELECT clause.
        if (displayUPC) {
            if (displayTID || displayCID || displayTDate || displayPMethod || displayTotal) {
                buffer.append(", ");
            }
            buffer.append("TC.UPC");
        }
        // Add quantity to SELECT clause.
        if (displayQuantity) {
            if (displayTID || displayCID || displayTDate || displayPMethod || displayTotal || displayUPC) {
                buffer.append(", ");
            }
            buffer.append("TC.quantity");
        }

        // Add FROM clause to query.
        buffer.append(" FROM Transactions T, Transaction_Contains TC");

        // Add WHERE clause to query.
        buffer.append(" WHERE T.transaction_ID = TC.transaction_ID");
        // Add Customer ID to WHERE clause.
        if (useCustomerID) {
            buffer.append(" AND T.customer_ID = ");
            buffer.append(customerID);
        }
        // Add Total to WHERE clause.
        if (useTotal) {
            buffer.append(" AND T.total = ");
            buffer.append(total);
        }
        // Add UPC to WHERE clause.
        if (useUPC) {
            buffer.append(" AND TC.UPC LIKE ");
            buffer.append('\'');
            buffer.append(UPC);
            buffer.append('\'');
        }
        // Add Quantity to WHERE clause.
        if (useQuantity) {
            buffer.append(" AND TC.quantity = ");
            buffer.append(quantity);
        }

        query = buffer.toString();


        if (displayTID) {
            header = String.format("%15s", "transaction_ID");
        }
        if (displayCID) {
            if (displayTID) {
                header += " ";
            }
            header += String.format("%15s", "customer_ID");
        }
        if (displayTDate) {
            if (displayTID || displayCID) {
                header += " ";
            }
            header += String.format("%20s", "transaction_date");
        }
        if (displayPMethod) {
            if (displayTID || displayCID || displayTDate) {
                header += " ";
            }
            header += String.format("%15s", "payment_method");
        }
        if (displayTotal) {
            if (displayTID || displayCID || displayTDate || displayPMethod) {
                header += " ";
            }
            header += String.format("%10s", "total");
        }
        if (displayUPC) {
            if (displayTID || displayCID || displayTDate || displayPMethod || displayTotal) {
                header += " ";
            }
            header += String.format("%15s", "UPC");
        }
        if (displayQuantity) {
            if (displayTID || displayCID || displayTDate || displayPMethod || displayTotal || displayUPC) {
                header += " ";
            }
            header += String.format("%15s", "quantity");
        }

        // Print the column headers for the users specified data.
        System.out.println(header);


        try {
            ResultSet resultSet = stmt.executeQuery(query);
            // Print query results.
            while (resultSet.next()) {
                // Display the Transaction ID column in table.
                if (displayTID) {
                    System.out.format("%15s ", resultSet.getString("transaction_ID"));
                }
                // Display the Customer ID column in table.
                if (displayCID) {
                    System.out.format("%15s ", resultSet.getString("customer_ID"));
                }
                // Display the Transaction Date column in table.
                if (displayTDate) {
                    System.out.format("%20s ", resultSet.getDate("transaction_date"));
                }
                // Display the Payment Method column in table.
                if (displayPMethod) {
                    System.out.format("%15s ", resultSet.getString("payment_method"));
                }
                // Display the Total column in table.
                if (displayTotal) {
                    System.out.format("%10s ", resultSet.getString("total"));
                }
                // Display the UPC column in table.
                if (displayUPC) {
                    System.out.format("%15s ", resultSet.getString("UPC"));
                }
                // Display the Quantity column in table.
                if (displayQuantity) {
                    System.out.format("%15s ", resultSet.getString("quantity"));
                }
                System.out.println();
            }
            resultSet.close();
        } catch (Exception ex) {
            System.out.println("Error: executing query " + ex.getMessage());
        }

    }

    /**
     * Get integer value from user input.
     * @param prompt prompt user for input
     * @return integer input
     */
    public static int getIntInput(String prompt) {
        Scanner scan = new Scanner(System.in);
        int input = 0;

        do {
            try {
                System.out.println(prompt);

                input = scan.nextInt();
                return input;

            } catch (InputMismatchException ime) {
                System.out.println("Invalid Input, Input must be an integer.");
                scan.nextLine();
            }
        } while (true);
    }

    /**
     * Get double value from user input.
     * @param prompt prompt user for input
     * @return double input
     */
    public static double getDoubleInput(String prompt) {
        Scanner scan = new Scanner(System.in);
        double input = 0;

        do {
            try {
                System.out.println(prompt);

                input = scan.nextDouble();
                return input;

            } catch (InputMismatchException ime) {
                System.out.println("Invalid Input, Input must be a double.");
                scan.nextLine();
            }
        } while (true);
    }

    /**
     * Get string value from user input.
     * @param prompt prompt user for input
     * @return string input
     */
    public static String getStringInput(String prompt) {
        Scanner scan = new Scanner(System.in);
        String input = "";

        do {
            try {
                System.out.println(prompt);

                input = scan.nextLine();
                return input;

            } catch (InputMismatchException ime) {
                System.out.println("Invalid Input, Input must be a string.");
                scan.nextLine();
            }
        } while (true);
    }


    /**
     * Search by a transaction ID to view the contents of the transactions table and the average quantity.
     */
    public static void searchByTransactionID() {
        PreparedStatement pstmt;
        ResultSet resultSet;
        String sql;
        boolean dataFound = false;

        // Get transaction ID.
        int tid = getIntInput("Enter Transaction ID: ");

        System.out.format("%15s %15s %20s %15s %10s %15s\n", "transaction_ID", "customer_ID", "transaction_date",
                         "payment_method", "total", "avg_quantity");

        try {

            sql = "SELECT T.transaction_ID, T.customer_ID, T.transaction_date, " +
                    "T.payment_method, T.total, AVG(TC.quantity) AS \"avg_quantity\" " +
                    "FROM Transactions T, Transaction_Contains TC " +
                    "WHERE T.transaction_ID = TC.transaction_ID AND T.transaction_ID = ? " +
                    "GROUP BY T.transaction_ID, T.customer_ID, T.transaction_date, T.payment_method, T.total";

            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, tid);

            resultSet = pstmt.executeQuery();
            // For each row, get data from the prepared statement.
            while (resultSet.next()) {
                dataFound = true;
                System.out.format("%15s ", resultSet.getString("transaction_ID"));
                System.out.format("%15s ", resultSet.getString("customer_ID"));
                System.out.format("%20s ", resultSet.getDate("transaction_date"));
                System.out.format("%15s ", resultSet.getString("payment_method"));
                System.out.format("%10s ", resultSet.getString("total"));
                System.out.format("%15s ", resultSet.getString("avg_quantity"));
                System.out.println();
            }
            // Prompt error message if data was not found in database.
            if (!dataFound) {
                System.out.println("No data found.");
            }
            resultSet.close();
            pstmt.close();
        } catch (SQLException sqle) {
            System.out.println("Database error " + sqle.getMessage());
        }

    }

    /**
     * Prompt user for yes no.
     * @param prompt to display
     * @return true if user selects "yes", otherwise false
     */
    public static boolean promptForYesNo(String prompt) {

        Scanner scan = new Scanner(System.in);
        String option = null;

        do {
            try {
                System.out.println(prompt + " (Yes/No)");

                // Store users choice.
                option = scan.nextLine();
                option = option.toLowerCase();
                switch (option) {
                    case "yes":
                        return true;
                    case "no":
                        return false;
                    default:
                        System.out.println("The option is invalid.");
                }
            } catch (InputMismatchException ime) {
                System.out.println("Invalid Input, Input must be either Yes or No.");
                scan.nextLine();
            }
        } while (true);
    }


    public static boolean connectToDatabase(String username, String password) {
	    String driverPrefixURL="jdbc:oracle:thin:@";
	    String jdbc_url="artemis.vsnet.gmu.edu:1521/vse18c.vsnet.gmu.edu";

        try {
	        //Register Oracle driver
            DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
        } catch (Exception e) {
            System.out.println("Failed to load JDBC/ODBC driver.");
            return false;
        }

       try {
            System.out.println(driverPrefixURL+jdbc_url);
            con=DriverManager.getConnection(driverPrefixURL+jdbc_url, username, password);
            DatabaseMetaData dbmd=con.getMetaData();
            stmt=con.createStatement();

            System.out.println("Connected.");

            if(dbmd==null){
                System.out.println("No database meta data");
            }
            else {
                System.out.println("Database Product Name: "+dbmd.getDatabaseProductName());
                System.out.println("Database Product Version: "+dbmd.getDatabaseProductVersion());
                System.out.println("Database Driver Name: "+dbmd.getDriverName());
                System.out.println("Database Driver Version: "+dbmd.getDriverVersion());
                System.out.println();
            }
        }catch( Exception e) {
           e.printStackTrace();
           return false;
       }

       return true;

    }// End of connectToDatabase()

    /**
     * Displays Product table content.
     */
    public static void showProductTableContent() {
        System.out.println("Product");
        System.out.format("%10s %20s %40s %70s %15s %10s %10s\n", "UPC", "brand", "product_name",
                         "product_description", "category", "marked_price", "quantity");

        try {
            ResultSet resultSet = stmt.executeQuery("SELECT * FROM Product");
            while (resultSet.next()) {
                System.out.format("%10s ", resultSet.getString("UPC"));
                System.out.format("%20s ", resultSet.getString("brand"));
                System.out.format("%40s ", resultSet.getString("product_name"));
                System.out.format("%70s ", resultSet.getString("product_description"));
                System.out.format("%15s ", resultSet.getString("category"));
                System.out.format("%10s ", resultSet.getString("marked_price"));
                System.out.format("%10s ", resultSet.getString("quantity"));
                System.out.println();
            }
            resultSet.close();
        } catch (Exception ex) {
            System.out.println("Error: executing query " + ex.getMessage());
        }

    }

    /**
     * Displays Customer table content.
     */
    public static void showCustomerTableContent() {
        System.out.println("Customer");
        System.out.format("%15s %15s %10s %10s %10s %10s\n", "customer_ID", "first_name", "last_name",
                         "age", "gender", "zip_code");

        try {
            ResultSet resultSet = stmt.executeQuery("SELECT * FROM Customer");
            while (resultSet.next()) {
                System.out.format("%15s ", resultSet.getString("customer_ID"));
                System.out.format("%15s ", resultSet.getString("first_name"));
                System.out.format("%10s ", resultSet.getString("last_name"));
                System.out.format("%10s ", resultSet.getString("age"));
                System.out.format("%10s ", resultSet.getString("gender"));
                System.out.format("%10s ", resultSet.getString("zip_code"));
                System.out.println();
            }
            resultSet.close();
        } catch (Exception ex) {
            System.out.println("Error: executing query " + ex.getMessage());
        }
    }

    /**
     * Display Transactions table content.
     */
    public static void showTransactionsTableContent() {
        System.out.println("Transactions");
        System.out.format("%15s %15s %20s %15s %10s\n", "transaction_ID", "customer_ID", "transaction_date",
                         "payment_method", "total");

        try {
            ResultSet resultSet = stmt.executeQuery("SELECT * FROM Transactions");
            while (resultSet.next()) {
                System.out.format("%15s ", resultSet.getString("transaction_ID"));
                System.out.format("%15s ", resultSet.getString("customer_ID"));
                System.out.format("%20s ", resultSet.getDate("transaction_date"));
                System.out.format("%15s ", resultSet.getString("payment_method"));
                System.out.format("%10s ", resultSet.getString("total"));
                System.out.println();
            }
            resultSet.close();
        } catch (Exception ex) {
            System.out.println("Error: executing query " + ex.getMessage());
        }
    }

    /**
     * Display Transaction Contains table content.
     */
    public static void showTransactionContainsTableContent() {
        System.out.println("Transaction_Contains");
        System.out.format("%15s %15s %15s\n", "transaction_ID", "UPC", "quantity");

        try {
            ResultSet resultSet = stmt.executeQuery("SELECT * FROM Transaction_Contains");
            while (resultSet.next()) {
                System.out.format("%15s ", resultSet.getString("transaction_ID"));
                System.out.format("%15s ", resultSet.getString("UPC"));
                System.out.format("%15s ", resultSet.getString("quantity"));
                System.out.println();
            }
            resultSet.close();
        } catch (Exception ex) {
            System.out.println("Error: executing query " + ex.getMessage());
        }
    }

}// End of class

