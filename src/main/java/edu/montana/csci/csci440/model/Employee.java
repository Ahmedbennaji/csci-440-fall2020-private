package edu.montana.csci.csci440.model;

import edu.montana.csci.csci440.util.DB;

import java.math.BigDecimal;
import java.sql.*;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Employee extends Model {

    private Long employeeId;
    private String firstName;
    private String lastName;
    private String email;
    private String title;
    private Long reportsTo;

    public Employee() {
        // new employee for insert
    }

    private Employee(ResultSet results) throws SQLException {
        firstName = results.getString("FirstName");
        lastName = results.getString("LastName");
        email = results.getString("Email");
        title = results.getString("Title");
        employeeId = results.getLong("EmployeeId");
        reportsTo = results.getLong("ReportsTo");


    }

    public static List<Employee.SalesSummary> getSalesSummaries() {
        //TODO - a GROUP BY query to determine the sales (look at the invoices table), using the SalesSummary class
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement(
"SELECT employees.LastName,employees.FirstName, employees.Email,  ROUND(SUM(invoices.Total),2) AS SalesTotal, COUNT(invoices.Total) AS SalesCount\n" +
        "    FROM (invoices\n" +
        "        INNER JOIN customers c on invoices.CustomerId = c.CustomerId\n" +
        "        INNER JOIN employees  on c.SupportRepId = employees.EmployeeId)\n" +
    //  "     WHERE employees.EmployeeId = 3" +
        "        GROUP BY employees.EmployeeId;"
             )) {
            ResultSet results = stmt.executeQuery();
            List<Employee.SalesSummary> resultList = new LinkedList<>();
            while (results.next()) {
                resultList.add(new Employee.SalesSummary(results));
            }
            return resultList;
        } catch (SQLException sqlException) {

            throw new RuntimeException(sqlException);
        }
        //return Collections.emptyList();
    }

    @Override
    public boolean verify() {
        _errors.clear(); // clear any existing errors

        if (firstName == null || "".equals(firstName)) {
            addError("FirstName can't be null or blank!");
        }
        if (lastName == null || "".equals(lastName)) {
            addError("LastName can't be null!");
        }
        if(email == null ||!email.contains("@") ){
            addError("email can't be null or empty or does not contain @");
        }
        return !hasErrors();
    }

    @Override
    public boolean update() {
        if (verify()) {
            try (Connection conn = DB.connect();
                 PreparedStatement stmt = conn.prepareStatement(
                         "UPDATE employees SET FirstName=?, LastName=?, Email=? WHERE EmployeeId=?")) {
                stmt.setString(1, this.getLastName());
                stmt.setString(2, this.getFirstName());
               // stmt.setString(3,this.getTitle());
                stmt.setString(3, this.getEmail());
                stmt.setLong(4, this.getEmployeeId());
                stmt.executeUpdate();
                return true;
            } catch (SQLException sqlException) {
                throw new RuntimeException(sqlException);
            }
        } else {
            return false;
        }
    }

    @Override
    public boolean create() {
        if (verify()) {
            try (Connection conn = DB.connect();
                 PreparedStatement stmt = conn.prepareStatement(
                         "INSERT INTO employees (LastName, FirstName,Title, Email) VALUES (?, ?, ?, ? )")) {
                stmt.setString(1, this.getLastName());
                stmt.setString(2, this.getFirstName());
                stmt.setString(3,this.getTitle());
                stmt.setString(4, this.getEmail());
                stmt.executeUpdate();
                employeeId = DB.getLastID(conn);
                return true;
            } catch (SQLException sqlException) {
                throw new RuntimeException(sqlException);
            }
        } else {
            return false;
        }
    }

    @Override
    public void delete() {
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement(
                     "DELETE FROM employees WHERE EmployeeID=?")) {
            stmt.setLong(1, this.getEmployeeId());
            stmt.executeUpdate();
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
    }

    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;

    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public List<Customer> getCustomers() {
        return Customer.forEmployee(employeeId);
    }

    public Long getReportsTo() {
        return reportsTo;
    }
    // public void setReportsTo(Long reportsTo) {
    // this.reportsTo = reportsTo;
    //}

    public List<Employee> getReports() {
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT * FROM employees WHERE ReportsTo=?"
             )) {
            stmt.setLong(1, this.getEmployeeId());
            ResultSet results = stmt.executeQuery();
            List<Employee> resultList = new LinkedList<>();
            while (results.next()) {
                resultList.add(new Employee(results));
            }
            return resultList;
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
    }
    public Employee getBoss() {
            return Employee.find(reportsTo);


    }

    public void setReportsTo(Employee employee) {
       reportsTo = employee.employeeId;

    }

    public static List<Employee> all() {
        return all(0, Integer.MAX_VALUE);
    }

    public static List<Employee> all(int page, int count) {
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT * FROM employees LIMIT ? OFFSET ?"
             )) {
            stmt.setInt(1, count );
            stmt.setInt(2,(page-1)*count);
            ResultSet results = stmt.executeQuery();
            List<Employee> resultList = new LinkedList<>();
            while (results.next()) {
                resultList.add(new Employee(results));
            }
            return resultList;
        } catch (SQLException sqlException) {

            throw new RuntimeException(sqlException);
        }
    }

    public static Employee findByEmail(String newEmailAddress) {
       // return Employee.findByEmail(newEmailAddress);
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM employees Where Email = ?")) {
            stmt.setString(1, newEmailAddress);
            ResultSet results = stmt.executeQuery();
            if (results.next()) {
                return new Employee(results);
            } else {
                return null;
            }
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
//   throw new UnsupportedOperationException("Implement me");
    }

    public static Employee find(long employeeId) {
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM employees WHERE EmployeeId=? ")) {
            stmt.setLong(1, employeeId);
            ResultSet results = stmt.executeQuery();
            if (results.next()) {
                return new Employee(results);
            } else {
                return null;
            }
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
    }
        public String getTitle(){
        return title;
       }
    public void setTitle(String programmer) {
        title = programmer;
    }



    public static class SalesSummary {
        private String firstName;
        private String lastName;
        private String email;
        private Long salesCount;
        private BigDecimal salesTotals;
        private SalesSummary(ResultSet results) throws SQLException {
            firstName = results.getString("FirstName");
            lastName = results.getString("LastName");
            email = results.getString("Email");
            salesCount = results.getLong("SalesCount");
            salesTotals = results.getBigDecimal("SalesTotal");
        }

        public String getFirstName() {
            return firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public String getEmail() {
            return email;
        }

        public Long getSalesCount() {
            return salesCount;
        }

        public BigDecimal getSalesTotals() {
            return salesTotals;
        }
    }
}
