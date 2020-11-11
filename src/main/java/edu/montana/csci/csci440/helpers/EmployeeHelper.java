package edu.montana.csci.csci440.helpers;

import edu.montana.csci.csci440.model.Employee;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class EmployeeHelper {
    public static String makeEmployeeTree() {
        // TODO, change this to use a single query operation to get all employees
       Employee employee = Employee.find(1); // root employee
        // and use this data structure to maintain reference information needed to build the tree structure
        Map<Long, List<Employee>> employeeMap = new HashMap<>();
        List<Employee> all = Employee.all();
        for (Employee emp : all) {
            Long reportsTo = employee.getReportsTo();
            List <Employee> employees = employeeMap.get(reportsTo);
            if(employees == null){
                employees = new LinkedList<>();
                employeeMap.put(reportsTo,employees);
            }
            employees.add(emp);
        }
        return "<ul>" + makeTree(employee, employeeMap) + "</ul>";
    }

    // TODO - currently this method just usese the employee.getReports() function, which
    //  issues a query.  Change that to use the employeeMap variable instead
    public static String makeTree(Employee employee, Map<Long, List<Employee>> employeeMap) {
        String list = "<li><a href='/employees" + employee.getEmployeeId() + "'>"
                + employee.getEmail() + "</a><ul>";
        List<Employee> reports  = employee.getReports();
        for (Employee report : reports) {
           list += makeTree(employee, employeeMap);
        }
        return list + "</ul></li>";
    }
}
