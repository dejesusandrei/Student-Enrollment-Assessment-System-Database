/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package main;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */

import java.sql.*;
import java.io.*;
import java.util.*;

public class Main {
    private static final String MY_DB = "jdbc:sqlite:ColmEnrollmentAssessmentSystem.db";
    private static final Scanner scan = new Scanner(System.in);
    public static void main(String []args) {
        Users.createUsersTable();
        Student.createStudentTable();
        Subject.createSubjectTable();
        GradeRecord.createGradeRecordTable();
        Enrollment.createEnrollmentTable();
        System.out.println();

        int errorCount = 0; 
        System.out.println("=========================================");
        System.out.println("          LOGIN TO THE SYSTEM            ");
        System.out.println("=========================================");

        do {
            System.out.print("Enter username: ");
            String username = scan.nextLine();
            System.out.print("Enter password: ");
            String password = scan.nextLine();

            String adminUsername = Users.getAdminUsername(username);
            String adminPassword = Users.getAdminPassword(password);
            
            if(username.isEmpty() || password.isEmpty()){
                errorCount++; // +1
                System.out.println("Please fill up the fields. Please try again");
            }
            else if(adminUsername.equals("Group1rootsystem") && adminPassword.equals("group1system12345")){
                System.out.println("Welcome back to the system admin!");
                break;
                // Break the loop and  makapag proceed sa admin panel
            }
            else{
                errorCount++; // +1 
                System.out.println("Incorrect username or password. Please try again");
            }

            System.out.println();
        }while (errorCount < 3);

        if(errorCount == 3){
            System.out.println("------------------ Exiting to the system -------------------");
            System.out.println("Access temporarily locked due to multiple failed attempts.");
            System.out.println("Exiting the system for your security.");
            System.exit(0); // Terminate yung program
        }

        System.out.println();

        // ADMIN LOGIN
        adminLogin();
    }

    // ADMIN LOGIN
    private static void adminLogin(){
        ReportGenerator reportGenerator = new ReportGenerator();
        System.out.println("=========================================");
        System.out.println("  COLM ENROLLMENT AND ASSESSMENT SYSTEM  ");
        System.out.println("=========================================");

        int choice;
        String exitChoice = " ";

        do{
            showMenu();

            System.out.print("Enter choice: ");
            choice = scan.nextInt(); scan.nextLine();

            switch(choice){
                case 1: Student.addStudent(scan); break;
                case 2: Subject.addSubjectToStudent(scan); break;
                case 3: GradeRecord.assignGrade(scan); break;
                case 4: Enrollment.computeAverage(scan); break;
                case 5: Student.searchStudent(scan); break;
                case 6: Enrollment.displayEnrollmentDetails(scan); break;
                case 7: reportGenerator.generateReport(scan); break;
                case 8: exitSystem(exitChoice); break;
                default:
                    System.out.println("Invalid choice. Choose between ( 1 - 8 ).");
                    break;
            }
            System.out.println("--------------------------------------------------------------------");
            System.out.println();
            
        }while(!exitChoice.equalsIgnoreCase("Yes"));
    }

    // SHOW MENU
    private static void showMenu(){
        System.out.println("1. Add Student");
        System.out.println("2. Enroll Student in Subject");
        System.out.println("3. Assign Grades");
        System.out.println("4. Compute Final Grade");
        System.out.println("5. Search Student");
        System.out.println("6. Display Student Report");
        System.out.println("7. Generate Report Text File");
        System.out.println("8. Exit");
    }

    // EXIT SYSTEM
    private static void exitSystem(String exitStatement){
        System.out.print ("Are you sure do you want to exit? ( Yes / No ): ");
        exitStatement = scan.nextLine();

        if(exitStatement.equalsIgnoreCase("Yes")){
            System.out.println("--- Exiting to the system ---");
            System.out.println("Programmed and Developed by:");
            System.out.println("De Jesus Andrei");
            System.out.println("Jumagdao Maureen");
            System.out.println("Miranda Mark Justin");
            System.out.println("Sacdalan Marjea");
            System.out.println("Tismo John Lawrence");
            System.exit(0);
        }

    }

    // GET My Database
    static String getMyDB(){
        return MY_DB;
    }

}

// DATABASE CONNECTION
class DatabaseConnection{
    // CONNECT DB
    // Connection = Connection sa database and method name connect()
    // throws SQLException = Pwede mag-error kapag kumokonek sa database like(wrong pass,  databseURL).
    static Connection connect() throws SQLException {
        // getting the String database on Main
        // then store it to new variable MY_DB
        String MY_DB = Main.getMyDB();
        return DriverManager.getConnection(MY_DB);
    }
}

// INTERFACE
interface ReportPrintable{
    void generateReport(Scanner scan);
}

// Report Generator implements ReportPrintable
class ReportGenerator implements ReportPrintable{
    // TEXT GENERATOR
    @Override
    public void generateReport(Scanner scan){
        System.out.println("--- Generate TXT Report ---");
        String fileName = "StudentReport.txt";

        System.out.println ("1. Enter Student ID");
        System.out.println ("2. Enter Student Name");
        System.out.println ("3. Display All Student Reports");

        System.out.print ("Enter choice: ");
        int choice = scan.nextInt(); scan.nextLine();

        if(choice == 1){
            System.out.print ("Enter Student ID: ");
            int studentId = scan.nextInt(); scan.nextLine();

            // GET STUDENT_ID
            int student_id = Student.getStudentId(studentId);
            if(student_id == -1){ System.out.println ("Student ID not found."); return;}

            // LEFT JOIN GINAMIT KASI MAGANDA ITO PARA SA MGA WALA PANG GRADES, MAGIGING NULL SYA
            String sql = " SELECT s.id AS student_id," +
                    "s.name AS student_name, " +
                    "s.age, " +
                    "s.gender ," +
                    "s.course, " +
                    "s.year_level, " +
                    "s.scholarShip," +
                    "IIF(UPPER(s.scholarShip) LIKE '%NONE%', '0%', '50%') AS discount_rate, " +
                    "sub.subject_code, " +
                    "sub.subject_title, " +
                    "sub.units, " +
                    "gr.prelim, " +
                    "gr.midterm, " +
                    "gr.finalExam, " +
                    "gr.finalGrade "+
                    "FROM students s " +
                    "LEFT JOIN enrollment e ON s.id = e.student_id "+
                    "LEFT JOIN subjects sub ON e.subject_id = sub.subject_id "+
                    "LEFT JOIN gradeRecord gr ON e.enrollment_id = gr.enrollment_id "+
                    "WHERE s.id = ?";

            try(Connection conn = DatabaseConnection.connect();
                PreparedStatement pstm = conn.prepareStatement(sql)){
                pstm.setInt(1, student_id);
                ResultSet rs = pstm.executeQuery();
                PrintWriter writer = new PrintWriter(new FileWriter(fileName));
                boolean hasRecord = false;

                writer.println ("==============================================================================================  COLM STUDENT REPORT  ===========================================================================================================");
                writer.printf("%-5s %-25s %-7s %-10s %-10s %-12s %-15s %-15s %-15s %-30s %-10s %-10s %-10s %-10s %-10s%n", "ID", "NAME", "AGE", "GENDER", "COURSE", "YEAR LEVEL", "SCHOLARSHIP", "DISCOUNT RATE", "SUBJECT CODE", "SUBJECT TITLE", "UNITS", "PRELIM", "MIDTERM", "FINAL", "FINAL GRADE");

                while(rs.next()){
                    hasRecord = true;
                    writer.printf("%-5d %-25s %-7d %-10s %-10s %-12s %-15s %-15s %-15s %-30s %-10d %-10.2f %-10.2f %-10.2f %-10.2f%n",
                            rs.getInt("student_id"),
                            rs.getString("student_name"),
                            rs.getInt("age"),
                            rs.getString("gender"),
                            rs.getString("course"),
                            rs.getString("year_level"),
                            rs.getString("scholarShip"),
                            rs.getString("discount_rate"),
                            rs.getString("subject_code"),
                            rs.getString("subject_title"),
                            rs.getInt("units"),
                            rs.getDouble("prelim"),
                            rs.getDouble("midterm"), 
                            rs.getDouble("finalExam"),
                            rs.getDouble("finalGrade")
                    );
                }
                writer.close();

                if(!hasRecord){System.out.println ("No records found on database");}
                System.out.println ("Report generated successfully: " + fileName);

                writer.println ("===========================================================================================================================================================================================================================");
            }catch(SQLException e){
                System.out.println ("Database error in viewing records: " + e.getMessage());
            }catch(IOException e){
                System.out.println ("File error while generating report: " + e.getMessage() );
            }

        }else if(choice == 2){
            System.out.print ("Enter Student Name: ");
            String studentName = scan.nextLine();

            // GET STUDENT_ID
            int student_id = Student.getStudentId(studentName);
            if(student_id == -1){ System.out.println ("Student name not found."); return;}

            // LEFT JOIN GINAMIT KASI MAGANDA ITO PARA SA MGA WALA PANG GRADES, MAGIGING NULL SYA
            String sql = " SELECT s.id AS student_id," +
                    "s.name AS student_name, " +
                    "s.age, " +
                    "s.gender ," +
                    "s.course, " +
                    "s.year_level, " +
                    "s.scholarShip," +
                    "IIF(UPPER(s.scholarShip) LIKE '%NONE%', '0%', '50%') AS discount_rate, " +
                    "sub.subject_code, " +
                    "sub.subject_title, " +
                    "sub.units, " +
                    "gr.prelim, " +
                    "gr.midterm, " +
                    "gr.finalExam, " +
                    "gr.finalGrade "+
                    "FROM students s " +
                    "LEFT JOIN enrollment e ON s.id = e.student_id "+
                    "LEFT JOIN subjects sub ON e.subject_id = sub.subject_id "+
                    "LEFT JOIN gradeRecord gr ON e.enrollment_id = gr.enrollment_id "+
                    "WHERE s.id = ?";

            try(Connection conn = DatabaseConnection.connect();
                PreparedStatement pstm = conn.prepareStatement(sql)){
                pstm.setInt(1, student_id);
                ResultSet rs = pstm.executeQuery();
                PrintWriter writer = new PrintWriter(new FileWriter(fileName));
                boolean hasRecord = false;

                writer.println ("==============================================================================================  COLM STUDENT REPORT  ===========================================================================================================");
                writer.printf("%-5s %-25s %-7s %-10s %-10s %-12s %-15s %-15s %-15s %-30s %-10s %-10s %-10s %-10s %-10s%n", "ID", "NAME", "AGE", "GENDER", "COURSE", "YEAR LEVEL", "SCHOLARSHIP", "DISCOUNT RATE", "SUBJECT CODE", "SUBJECT TITLE", "UNITS", "PRELIM", "MIDTERM", "FINAL", "FINAL GRADE");

                while(rs.next()){
                    hasRecord = true;
                    writer.printf("%-5d %-25s %-7d %-10s %-10s %-12s %-15s %-15s %-15s %-30s %-10d %-10.2f %-10.2f %-10.2f %-10.2f%n",
                            rs.getInt("student_id"),
                            rs.getString("student_name"),
                            rs.getInt("age"),
                            rs.getString("gender"),
                            rs.getString("course"),
                            rs.getString("year_level"),
                            rs.getString("scholarShip"),
                            rs.getString("discount_rate"),
                            rs.getString("subject_code"),
                            rs.getString("subject_title"),
                            rs.getInt("units"),
                            rs.getDouble("prelim"),
                            rs.getDouble("midterm"),
                            rs.getDouble("finalExam"),
                            rs.getDouble("finalGrade")
                    );
                }
                writer.close();

                if(!hasRecord){System.out.println ("No records found on database");}
                System.out.println ("Report generated successfully: " + fileName);

                writer.println ("===========================================================================================================================================================================================================================");
            }catch(SQLException e){
                System.out.println ("Database error in viewing records: " + e.getMessage());
            }catch(IOException e){
                System.out.println ("File error while generating report: " + e.getMessage() );
            }
        }else if(choice == 3){
            // LEFT JOIN GINAMIT KASI MAGANDA ITO PARA SA MGA WALA PANG GRADES, MAGIGING NULL SYA
            String sql = " SELECT s.id AS student_id," +
                    "s.name AS student_name, " +
                    "s.age, " +
                    "s.gender ," +
                    "s.course, " +
                    "s.year_level, " +
                    "s.scholarShip," +
                    "IIF(UPPER(s.scholarShip) LIKE '%NONE%', '0%', '50%') AS discount_rate, " +
                    "sub.subject_code, " +
                    "sub.subject_title, " +
                    "sub.units, " +
                    "gr.prelim, " +
                    "gr.midterm, " +
                    "gr.finalExam, " +
                    "gr.finalGrade "+
                    "FROM students s " +
                    "LEFT JOIN enrollment e ON s.id = e.student_id "+
                    "LEFT JOIN subjects sub ON e.subject_id = sub.subject_id "+
                    "LEFT JOIN gradeRecord gr ON e.enrollment_id = gr.enrollment_id ";

            try(Connection conn = DatabaseConnection.connect();
                PreparedStatement pstm = conn.prepareStatement(sql)){
                ResultSet rs = pstm.executeQuery();
                PrintWriter writer = new PrintWriter(new FileWriter(fileName));
                boolean hasRecord = false;

                writer.println ("==============================================================================================  COLM STUDENT REPORT  ===========================================================================================================");
                writer.printf("%-5s %-25s %-7s %-10s %-10s %-12s %-15s %-15s %-15s %-30s %-10s %-10s %-10s %-10s %-10s%n", "ID", "NAME", "AGE", "GENDER", "COURSE", "YEAR LEVEL", "SCHOLARSHIP", "DISCOUNT RATE", "SUBJECT CODE", "SUBJECT TITLE", "UNITS", "PRELIM", "MIDTERM", "FINAL", "FINAL GRADE");

                while(rs.next()){
                    hasRecord = true;
                    writer.printf("%-5d %-25s %-7d %-10s %-10s %-12s %-15s %-15s %-15s %-30s %-10d %-10.2f %-10.2f %-10.2f %-10.2f%n",
                            rs.getInt("student_id"),
                            rs.getString("student_name"),
                            rs.getInt("age"),
                            rs.getString("gender"),
                            rs.getString("course"),
                            rs.getString("year_level"),
                            rs.getString("scholarShip"),
                            rs.getString("discount_rate"),
                            rs.getString("subject_code"),
                            rs.getString("subject_title"),
                            rs.getInt("units"),
                            rs.getDouble("prelim"),
                            rs.getDouble("midterm"),
                            rs.getDouble("finalExam"),
                            rs.getDouble("finalGrade")
                    );
                }
                writer.close();

                if(!hasRecord){System.out.println ("No records found on database");}
                System.out.println ("Report generated successfully: " + fileName);

                writer.println ("===========================================================================================================================================================================================================================");
            }catch(SQLException e){
                System.out.println ("Database error in viewing records: " + e.getMessage());
            }catch(IOException e){
                System.out.println ("File error while generating report: " + e.getMessage() );
            }
        }else{
            System.out.println("Invalid choice. Choose between ( 1 - 3 ).");
        }
    }
}

// USERS
class Users{
    public static void createUsersTable(){
        String sqlUsers = "CREATE TABLE IF NOT EXISTS users (users_id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT NOT NULL, password TEXT NOT NULL)";

        try(Connection conn = DatabaseConnection.connect();
            PreparedStatement pstm = conn.prepareStatement(sqlUsers)){
            pstm.execute();
            System.out.println("Users table ready");
        }catch (SQLException e){
            throw new RuntimeException(e);
        }
    }

    public static String getAdminUsername(String username){
        String sql = "SELECT username FROM users WHERE username = ?";

        try(Connection conn = DatabaseConnection.connect();
            PreparedStatement pstm = conn.prepareStatement(sql)){
            pstm.setString(1,username);
            
            ResultSet rs = pstm.executeQuery();

            if(rs.next()){
                return rs.getString("username");
            }

        }catch (SQLException e){
            System.out.println ("Error getting user: " + e.getMessage());
        }
        return "null";
    }

    public static String getAdminPassword(String pasword){
        String sql = "SELECT password FROM users WHERE password = ?";

        try(Connection conn = DatabaseConnection.connect();
            PreparedStatement pstm = conn.prepareStatement(sql)){
            pstm.setString(1,pasword);
            ResultSet rs = pstm.executeQuery();

            if(rs.next()){
                return rs.getString("password");
            }

        }catch (SQLException e){
            System.out.println ("Error getting user: " + e.getMessage());
        }
        return "null";
    }

}

// STUDENT
class Student{
    // STUDENT TABLE
    public static void createStudentTable(){
        // So sa string nato meron tayong CREATE TABLE > IF NOT EXIST: gagawin lang yung table kapag wala pa.
        String sqlStudent = "CREATE TABLE IF NOT EXISTS students ( id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, age INTEGER NOT NULL, gender TEXT NOT NULL," +
                "course TEXT NOT NULL, year_level TEXT NOT NULL, scholarShip TEXT)";

        // PreparedStatement = ginnagamit to para makapag execute ng SQL query sa Database ng mas safe
        // Take note PreparedStatement: ginamit natin to para sa security and para makaiwas sa SQL Injection
        // DataBaseConnection name ng class. Then call the method connect para makakonek tayo sa database
        // prepareStatement(sqlStudent) = dito natin ilalagay yung ginawa nating table, insert ng value and  etc.
        // execute() = gagawin na yung table
        try(Connection conn = DatabaseConnection.connect();
            PreparedStatement pstm = conn.prepareStatement(sqlStudent)){
            pstm.execute();
            System.out.println("Student table ready");

            // throws SQLException = Pwede mag-error kapag kumokonek sa database like(wrong pass,  databseURL).
            // dito na ginagamit yung SQLException para malaman natin kung ano error sa system and database.
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // ADD STUDENTS
    public static void addStudent(Scanner scan){
        System.out.println("--- ADD STUDENT ---");
        System.out.print("Enter Full Name: ");
        String fullName = scan.nextLine();
        System.out.print("Enter Age: ");
        int age = scan.nextInt(); scan.nextLine();
        System.out.print("Enter Gender: ");
        String gender = scan.nextLine();
        System.out.print("Enter Course: ");
        String course = scan.nextLine();
        System.out.print("Enter Year Level: ");
        String yearLevel = scan.nextLine();
        System.out.print("Scholarship Type ( Mayor, Gov, Mall / None ): ");
        String scholarshipType = scan.nextLine();

        String sql = "INSERT INTO students(name, age, gender, course, year_level, scholarShip) VALUES(?,?,?,?,?,?)";
        
        // Checking the inputs if they fill up the requiremnts correctly
        if(
                fullName == null || fullName.isEmpty() || age <= 5  || age >= 110 ||
                gender == null || gender.isEmpty() || course == null || course.isEmpty() ||
                yearLevel == null || yearLevel.isEmpty() || scholarshipType == null || scholarshipType.isEmpty())
        {
            System.out.println("Please fill up all required fields correctly.");
        }
        else{
            try(Connection conn = DatabaseConnection.connect();
                PreparedStatement pstm = conn.prepareStatement(sql)){
                pstm.setString(1, fullName);
                pstm.setInt(2, age);
                pstm.setString(3, gender);
                pstm.setString(4, course);
                pstm.setString(5, yearLevel);
                pstm.setString(6, scholarshipType);

                int rows = pstm.executeUpdate(); // 1
                System.out.println(rows + " Student added successfully!.");

            }catch (SQLException e){
                System.out.println("Error adding student: "+ e.getMessage());
            }
        }
    }

    // GET STUDENT_ID
    // OVERLOADING
    public static int getStudentId(int student_id){
        String sql = "SELECT id FROM students WHERE id = ?";

        try(Connection conn = DatabaseConnection.connect();
            PreparedStatement pstm = conn.prepareStatement(sql)){
            pstm.setInt(1, student_id);
            
            ResultSet rs = pstm.executeQuery();

            if(rs.next()){
                return rs.getInt("id");
            }
        }catch(Exception e){
            System.out.println ("Error gettting student id: " + e.getMessage());
        }
        return -1;
    }

    // GET STUDENT_ID BY STUDENT_NAME
    // OVERLOADING
    public static int getStudentId(String student_name){
        String sql = "SELECT id FROM students WHERE name = ?";

        try(Connection conn = DatabaseConnection.connect();
            PreparedStatement pstm = conn.prepareStatement(sql)){
            pstm.setString(1, student_name);
            ResultSet rs = pstm.executeQuery();

            if(rs.next()){
                return rs.getInt("id");
            }
        }catch(Exception e){
            System.out.println ("Error gettting student id: " + e.getMessage());
        }
        return -1;
    }

    // SEARCH STUDENT
    public static void searchStudent(Scanner scan){
        System.out.println ("---- SEARCH STUDENT -----");
        showStudents();
        System.out.println ("========================================");

        System.out.print ("Enter Sudent ID: ");
        int studentId = scan.nextInt();
        System.out.println ();

        String sql = "SELECT name, course, year_level FROM students WHERE  id = ?";

        try(Connection conn = DatabaseConnection.connect();
            PreparedStatement pstm = conn.prepareStatement(sql)){
            pstm.setInt(1, studentId);
            
            ResultSet rs  = pstm.executeQuery();
            
            boolean hasFound = false;

            while(rs.next()){
                System.out.println ("================================ STUDENT FOUND ================================");
                System.out.printf("%-20s %-10s %13s%n", "NAME", "COURSE", "YEAR LEVEL");
                hasFound = true;
                System.out.printf ("%-20s %-10s %13s%n" ,
                        rs.getString("name"),
                        rs.getString("course"),
                        rs.getString("year_level"));
            }

            if(!hasFound){System.out.println ("Student ID not found.");}
        }catch(SQLException e){
            System.out.println ("Database error viewing students: " + e.getMessage());
        }
    }

    // SHOW STUDENTS FOR ASSIGN GRADES
    public static void showStudents(){
        String sql = "SELECT id, name FROM students";
        System.out.println ("========================================");
        System.out.printf("%-15s %-15s%n", "STUDENT ID", "NAME");
        System.out.println ("========================================");

        try(Connection conn = DatabaseConnection.connect();
            PreparedStatement pstm = conn.prepareStatement(sql);
            ResultSet rs = pstm.executeQuery()){

            boolean hasRecord = false;
            while(rs.next()){
                hasRecord = true;
                System.out.printf("%-15s %-15s%n",
                rs.getInt("id"),
                rs.getString("name"));
            }

            if(!hasRecord) System.out.println ("No records found");
        }catch(SQLException e){
            System.out.println ("Database no records: " + e.getMessage());
        }
    }

    // DisplayInfo of students
    public static void displayInfo(){
        String sql = "SELECT * FROM students";

        System.out.println ("=========================================  STUDENT REPORT  =========================================");
        System.out.printf("%-5s %-25s %-7s %-10s %-10s %-12s %-15s%n", "ID", "NAME", "AGE", "GENDER", "COURSE", "YEAR LEVEL", "SCHOLARSHIP");

        try(Connection conn = DatabaseConnection.connect();
            PreparedStatement pstm = conn.prepareStatement(sql)){
            boolean hasRecord = false;
            ResultSet rs = pstm.executeQuery();
            while(rs.next()){
                hasRecord = true;
                System.out.printf("%-5d %-25s %-7d %-10s %-10s %-12s %-15%ns",
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("age"),
                        rs.getString("gender"),
                        rs.getString("course"),
                        rs.getString("year_level"),
                        rs.getString("scholarShip"));
            }
            if(!hasRecord){System.out.println ("No records found on database");}

            System.out.println ("================================================================================================");
        }catch(SQLException e){
            System.out.println ("Database error in viewing records: " + e.getMessage());
        }
    }

}

// SUBJECTS
class Subject{
    // SUBJECT TABLE
    public static void createSubjectTable(){
        String sqlSubject = "CREATE TABLE IF NOT EXISTS subjects (subject_id INTEGER PRIMARY KEY AUTOINCREMENT, subject_code TEXT NOT NULL, subject_title TEXT NOT NULL, units INTEGER NOT NULL, UNIQUE(subject_code, subject_title))";

        try(Connection conn = DatabaseConnection.connect();
            PreparedStatement pstm = conn.prepareStatement(sqlSubject)){
            pstm.execute();
            System.out.println("Subject table ready.");
        }catch (SQLException e){
            System.out.println("Error creating subject table: " + e.getMessage());
        }
    }

    // ADD SUBJECTS
    public static void addSubjectToStudent(Scanner scan){
        System.out.println ("--- ENROLL STUDENT IN SUBJECT ---");
        Student.showStudents();
        System.out.println ("========================================");
        
        System.out.print ("Enter Student ID: ");
        int studentId = scan.nextInt(); scan.nextLine();

        // get Student ID
        int getStudent_Id = Student.getStudentId(studentId); // -1
        if(getStudent_Id == -1){ System.out.println ("Student ID not found"); return;}

        System.out.print ("Enter Subject Code: ");
        String subjectCode = scan.nextLine();
        System.out.print ("Enter Subject Title: ");
        String subjectTitle = scan.nextLine();
        System.out.print ("Enter Subject Unit: ");
        int subjectUnit = scan.nextInt();
        
        if(
            subjectCode == null || subjectCode.isEmpty() || subjectTitle == null || subjectTitle.isEmpty() 
            || subjectUnit <= 0)
        {
            System.out.println("Please fill up all required fields correctly.");
            return;
        }
        else{
            // get Subject ID
            int subjectId = getSubjectId(subjectCode); // -1 

            if(subjectId == -1){
                // Prevent duplicates subjects
                // Pag wala pang subs mag a-add ng bagong subject
                String sql = "INSERT INTO subjects (subject_code, subject_title, units)VALUES (?,?,?)";

                try(Connection conn = DatabaseConnection.connect();
                    PreparedStatement pstm = conn.prepareStatement(sql)){
                    pstm.setString(1, subjectCode);
                    pstm.setString(2, subjectTitle);
                    pstm.setInt(3, subjectUnit);

                    int rows = pstm.executeUpdate();
                    System.out.println (rows + " Student subject added successfully!");
                }catch(SQLException e){
                    System.out.println ("Error adding subject: " + e.getMessage());
                }

                // get Subject ID para malaman ano yung new ID ng subject
                subjectId = getSubjectId(subjectCode);
            }

            // INSERT TO ENROLLMENT TABLE
            String sqlEnroll = "INSERT INTO enrollment(student_id, subject_id) VALUES(?, ?)";

            try(Connection conn = DatabaseConnection.connect();
                PreparedStatement pstm = conn.prepareStatement(sqlEnroll)) {
                pstm.setInt(1, getStudent_Id);
                pstm.setInt(2, subjectId);

                int rows = pstm.executeUpdate(); // 1
                System.out.println(rows + " Enrollment record added successfully!");
            } catch(SQLException e) {
                System.out.println("Error adding enrollment: " + e.getMessage());
                return;
            }
        }

    }

    // GET SUBJECT_ID
    public static int getSubjectId(String subjectCode){
        String sql = "SELECT subject_id FROM subjects WHERE subject_code = ? ";

        try(Connection conn = DatabaseConnection.connect();
            PreparedStatement pstm = conn.prepareStatement(sql)){
            pstm.setString(1, subjectCode);
            
            ResultSet rs = pstm.executeQuery();
            if(rs.next()){
                return rs.getInt("subject_id");
            }
        }catch(Exception e){
            System.out.println ("Error gettting subject id: " + e.getMessage());
        }
        return -1;
    }

}

// GRADE RECORD
class GradeRecord{
    // GRADERECORDS TABLE
    public  static void createGradeRecordTable(){
        String  sqlGradeRecord = "CREATE TABLE IF NOT EXISTS gradeRecord (gradeRecord_id INTEGER PRIMARY KEY AUTOINCREMENT, enrollment_id INTEGER NOT NULL , prelim REAL NOT NULL, midterm REAL NOT NULL, finalExam REAL NOT NULL, finalGrade REAL NOT NULL,FOREIGN KEY (enrollment_id) REFERENCES enrollment(enrollment_id))";

        try(Connection conn = DatabaseConnection.connect();
            PreparedStatement pstm = conn.prepareStatement(sqlGradeRecord)){
            pstm.execute();
            System.out.println("Grade record table ready.");
        }catch (SQLException e){
            System.out.println("Error creating Grade record table: " + e.getMessage());
        }
    }

    // ASSIGN GRADES TO STUDENTS
    public static void assignGrade(Scanner scan){
        System.out.println ("--- ASSIGN GRADES ---");

        Student.showStudents(); // DISPLAY THE STUDENTS, JUST IN CASE ADMIN FORGETS THEIR ID's
        System.out.println ("========================================");
        System.out.println ();

        System.out.print ("Enter Student ID: ");
        int studentId = scan.nextInt(); scan.nextLine();

        // get Student ID
        int getStudent_Id = Student.getStudentId(studentId);
        if(getStudent_Id == -1){ System.out.println ("Student ID not found"); return;}

        System.out.print ("Enter Subject Code: ");
        String subjectCode = scan.nextLine();

        // get Subject ID
        int subjectId = Subject.getSubjectId(subjectCode);
        if(subjectId == -1){ System.out.println ("Subject Code not found"); return;}

        // GET STUDENT_ID and SUBJECT_ID
        int enrollment_id = Enrollment.getEnrollmentIdBySubjectAndStudent(getStudent_Id, subjectId);

        System.out.println (); // space

        System.out.print ("Enter Prelim Grade: ");
        double prelim = scan.nextDouble();
        System.out.print ("Enter Midterm Grade: ");
        double midterm = scan.nextDouble();
        System.out.print ("Enter Final Grade: ");
        double finalExam = scan.nextDouble();

        if((prelim < 0 || prelim > 100) || (midterm < 0 || midterm > 100) || (finalExam < 0 || finalExam > 100)){
            System.out.println("Invalid input of grades.");
        }else{
            double finalGrade = Math.round((prelim + midterm + finalExam) / 3.0 * 100.0) / 100.0;

            // INSERT A NEW GRADE RECORD
            String sqlGrade = "INSERT INTO gradeRecord(enrollment_id, prelim, midterm, finalExam, finalGrade)VALUES(?,?,?,?,?)";

            try(Connection conn = DatabaseConnection.connect();
                PreparedStatement pstm = conn.prepareStatement(sqlGrade)){
                pstm.setInt(1, enrollment_id);
                pstm.setDouble(2, prelim);
                pstm.setDouble(3, midterm);
                pstm.setDouble(4, finalExam);
                pstm.setDouble(5, finalGrade);
                
                int rows = pstm.executeUpdate();
                System.out.println (rows + " Grade record added successfully!.");
            }catch(SQLException e){
                System.out.println ("Error adding grades: " + e.getMessage());
            }
        }
    }

}

// ENROLLMENT
class Enrollment{
    // ENROLLMENT TABLE
    public static  void createEnrollmentTable(){
        String sqlEnrollment = "CREATE TABLE IF NOT EXISTS enrollment (enrollment_id INTEGER PRIMARY KEY AUTOINCREMENT, student_id INTEGER, subject_id INTEGER, FOREIGN KEY (student_id) REFERENCES students(id), FOREIGN KEY (subject_id) REFERENCES subjects(subject_id), UNIQUE(student_id, subject_id))";
        try(Connection conn = DatabaseConnection.connect();
            PreparedStatement pstm = conn.prepareStatement(sqlEnrollment)){
            pstm.execute();
            System.out.println("Enrollment table ready.");
        }catch (SQLException e){
            System.out.println("Error creating enrollment table: " + e.getMessage());
        }
    }

    // GET ENROLLMENT_ID BY SUB_ID and STUDENT_ID
    public static int getEnrollmentIdBySubjectAndStudent(int student_id, int subject_id){
        String sql = "SELECT enrollment_id FROM enrollment WHERE student_id = ? AND subject_id = ?";

        try(Connection conn = DatabaseConnection.connect();
            PreparedStatement pstm = conn.prepareStatement(sql)){
            pstm.setInt(1, student_id);
            pstm.setInt(2, subject_id);
            ResultSet rs = pstm.executeQuery();

            if(rs.next()){
                return rs.getInt("enrollment_id");
            }
        }catch(Exception e){
            System.out.println ("Error gettting enrollment id: " + e.getMessage());
        }
        return -1;
    }

    // COMPUTE FINAL AVERAGE
    public static void computeAverage(Scanner scan){
        System.out.println ("----- COMPUTE FINAL GRADE -----");
        Student.showStudents();
        System.out.println ("========================================");

        System.out.print ("Enter Student ID: ");
        int studentId = scan.nextInt();

        // GET STUDENT ID
        int student_id = Student.getStudentId(studentId);
        if(student_id == -1){ System.out.println ("Student ID not found."); return; }

        String sql = "SELECT s.id AS student_id, " +
                "s.name AS student_name, " +
                "AVG(gr.finalGrade) AS average_grade, " +
                "CASE WHEN AVG(gr.finalGrade) >= 87 THEN 'Qualified' ELSE 'Not Qualified' END AS deans_list " +
                "FROM students s "+
                "LEFT JOIN enrollment e ON s.id = e.student_id " +
                "LEFT JOIN gradeRecord gr ON e.enrollment_id = gr.enrollment_id " +
                "WHERE s.id = ?";

        System.out.println ("================================ COLM STUDENT REPORT ================================");
        System.out.printf ("%-5s %-25s %-15s %-15s%n", "ID", "STUDENT NAME", "AVERAGE", "DEAN'S LIST");

        try(Connection conn = DatabaseConnection.connect();
            PreparedStatement pstm = conn.prepareStatement(sql)){
            pstm.setInt(1, student_id);
            
            ResultSet rs = pstm.executeQuery();

            while(rs.next()){
                System.out.printf ("%-5d %-25s %-15.2f %-15s%n ",
                        rs.getInt("student_id"),
                        rs.getString("student_name"),
                        rs.getDouble("average_grade"),
                        rs.getString("deans_list"));
            }
        }catch(SQLException e){
            System.out.println ("Database error in calculating average: " + e.getMessage());
        }
    }

    // SHOW STUDENT REPORT
    public static void displayEnrollmentDetails(Scanner scan){
        System.out.println ("---- STUDENT REPORT ----");
        System.out.println ("1. Enter Student ID");
        System.out.println ("2. Enter Student Name");
        System.out.println ("3. Display All Student Reports");

        System.out.print ("Enter choice: ");
        int choice = scan.nextInt(); scan.nextLine();

        if(choice == 1){
            System.out.print ("Enter Student ID: ");
            int studentId = scan.nextInt(); scan.nextLine();

            // GET STUDENT_ID
            int student_id = Student.getStudentId(studentId);
            if(student_id == -1){ System.out.println ("Student ID not found."); return;}

            System.out.println ("==============================================================================================  COLM STUDENT REPORT  ===========================================================================================================");
            System.out.printf("%-5s %-25s %-7s %-10s %-10s %-12s %-15s %-15s %-15s %-30s %-10s %-10s %-10s %-10s %-10s%n", "ID", "NAME", "AGE", "GENDER", "COURSE", "YEAR LEVEL", "SCHOLARSHIP", "DISCOUNT RATE", "SUBJECT CODE", "SUBJECT TITLE", "UNITS", "PRELIM", "MIDTERM", "FINAL", "FINAL GRADE");

            // LEFT JOIN GINAMIT KASI MAGANDA ITO PARA SA MGA WALA PANG GRADES, MAGIGING NULL SYA
            String sql = " SELECT s.id AS student_id," +
                    "s.name AS student_name, " +
                    "s.age, " +
                    "s.gender ," +
                    "s.course, " +
                    "s.year_level, " +
                    "s.scholarShip," +
                    "IIF(UPPER(s.scholarShip) LIKE '%NONE%', '0%', '50%') AS discount_rate, " +
                    "sub.subject_code, " +
                    "sub.subject_title, " +
                    "sub.units, " +
                    "gr.prelim, " +
                    "gr.midterm, " +
                    "gr.finalExam, " +
                    "gr.finalGrade "+
                    "FROM students s " +
                    "LEFT JOIN enrollment e ON s.id = e.student_id "+
                    "LEFT JOIN subjects sub ON e.subject_id = sub.subject_id "+
                    "LEFT JOIN gradeRecord gr ON e.enrollment_id = gr.enrollment_id "+
                    "WHERE s.id = ?";

            try(Connection conn = DatabaseConnection.connect();
                PreparedStatement pstm = conn.prepareStatement(sql)){
                pstm.setInt(1, student_id);
                ResultSet rs = pstm.executeQuery();
                boolean hasRecord = false;

                while(rs.next()){
                    hasRecord = true;
                    System.out.printf("%-5d %-25s %-7d %-10s %-10s %-12s %-15s %-15s %-15s %-30s %-10d %-10.2f %-10.2f %-10.2f %-10.2f%n",
                            rs.getInt("student_id"),
                            rs.getString("student_name"),
                            rs.getInt("age"),
                            rs.getString("gender"),
                            rs.getString("course"),
                            rs.getString("year_level"),
                            rs.getString("scholarShip"),
                            rs.getString("discount_rate"),
                            rs.getString("subject_code"),
                            rs.getString("subject_title"),
                            rs.getInt("units"),
                            rs.getDouble("prelim"),
                            rs.getDouble("midterm"),
                            rs.getDouble("finalExam"),
                            rs.getDouble("finalGrade")
                    );
                }
                if(!hasRecord){System.out.println ("No records found on database");}

                System.out.println ("===========================================================================================================================================================================================================================");
            }catch(SQLException e){
                System.out.println ("Database error in viewing records: " + e.getMessage());
            }

        }
        else if(choice == 2){
            System.out.print ("Enter Student Name: ");
            String studentName = scan.nextLine();

            // GET THE STUDENT_ID by NAME
            int studentId = Student.getStudentId(studentName);
            if(studentId == -1){ System.out.println ("Student name not found"); return; }

            System.out.println ("============================================================================================== COLM STUDENT REPORT ===========================================================================================================");
            System.out.printf("%-5s %-25s %-7s %-10s %-10s %-12s %-15s %-15s %-15s %-30s %-10s %-10s %-10s %-10s %-10s%n", "ID", "NAME", "AGE", "GENDER", "COURSE", "YEAR LEVEL", "SCHOLARSHIP", "DISCOUNT RATE", "SUBJECT CODE", "SUBJECT TITLE", "UNITS", "PRELIM", "MIDTERM", "FINAL", "FINAL GRADE");

            // LEFT JOIN GINAMIT KASI MAGANDA ITO PARA SA MGA WALA PANG GRADES, MAGIGING NULL SYA
            String sql = " SELECT s.id AS student_id," +
                    "s.name AS student_name, " +
                    "s.age, " +
                    "s.gender ," +
                    "s.course, " +
                    "s.year_level, " +
                    "s.scholarShip," +
                    "IIF(UPPER(s.scholarShip) LIKE '%NONE%', '0%', '50%') AS discount_rate, " +
                    "sub.subject_code, " +
                    "sub.subject_title, " +
                    "sub.units, " +
                    "gr.prelim, " +
                    "gr.midterm, " +
                    "gr.finalExam, " +
                    "gr.finalGrade " +
                    "FROM students s " +
                    "LEFT JOIN enrollment e ON s.id = e.student_id "+
                    "LEFT JOIN subjects sub ON e.subject_id = sub.subject_id "+
                    "LEFT JOIN gradeRecord gr ON e.enrollment_id = gr.enrollment_id "+
                    "WHERE s.id = ?";

            try(Connection conn = DatabaseConnection.connect();
                PreparedStatement pstm = conn.prepareStatement(sql)){
                pstm.setInt(1, studentId);
                ResultSet rs = pstm.executeQuery();
                boolean hasRecord = false;

                while(rs.next()){
                    hasRecord = true;
                    System.out.printf("%-5d %-25s %-7d %-10s %-10s %-12s %-15s %-15s %-15s %-30s %-10d %-10.2f %-10.2f %-10.2f %-10.2f%n",
                            rs.getInt("student_id"),
                            rs.getString("student_name"),
                            rs.getInt("age"),
                            rs.getString("gender"),
                            rs.getString("course"),
                            rs.getString("year_level"),
                            rs.getString("scholarShip"),
                            rs.getString("discount_rate"),
                            rs.getString("subject_code"),
                            rs.getString("subject_title"),
                            rs.getInt("units"),
                            rs.getDouble("prelim"),
                            rs.getDouble("midterm"),
                            rs.getDouble("finalExam"),
                            rs.getDouble("finalGrade")
                    );
                }
                if(!hasRecord){System.out.println ("No records found on database");}

                System.out.println ("=========================================================================================================================================================================================================================");
            }catch(SQLException e){
                System.out.println ("Database error in viewing records: " + e.getMessage());
            }

        }
        else if(choice == 3){
            System.out.println ("============================================================================================== COLM STUDENT REPORT ===========================================================================================================");
            System.out.printf("%-5s %-25s %-7s %-10s %-10s %-12s %-15s %-15s %-15s %-30s %-10s %-10s %-10s %-10s %-10s%n", "ID", "NAME", "AGE", "GENDER", "COURSE", "YEAR LEVEL", "SCHOLARSHIP", "DISCOUNT RATE", "SUBJECT CODE", "SUBJECT TITLE", "UNITS", "PRELIM", "MIDTERM", "FINAL", "FINAL GRADE");

            String sql = " SELECT s.id AS student_id," +
                    "s.name AS student_name, " +
                    "s.age, " +
                    "s.gender ," +
                    "s.course, " +
                    "s.year_level, " +
                    "s.scholarShip," +
                    "IIF(UPPER(s.scholarShip) LIKE '%NONE%', '0%', '50%') AS discount_rate, " +
                    "sub.subject_code, " +
                    "sub.subject_title, " +
                    "sub.units, " +
                    "gr.prelim, " +
                    "gr.midterm, " +
                    "gr.finalExam, " +
                    "gr.finalGrade " +
                    "FROM students s " +
                    "LEFT JOIN enrollment e ON s.id = e.student_id "+
                    "LEFT JOIN subjects sub ON e.subject_id = sub.subject_id "+
                    "LEFT JOIN gradeRecord gr ON e.enrollment_id = gr.enrollment_id ";

            try(Connection conn = DatabaseConnection.connect();
                PreparedStatement pstm = conn.prepareStatement(sql)){
                ResultSet rs = pstm.executeQuery();
                boolean hasRecord = false;

                while(rs.next()){
                    hasRecord = true;
                    System.out.printf("%-5d %-25s %-7d %-10s %-10s %-12s %-15s %-15s %-15s %-30s %-10d %-10.2f %-10.2f %-10.2f %-10.2f%n",
                            rs.getInt("student_id"),
                            rs.getString("student_name"),
                            rs.getInt("age"),
                            rs.getString("gender"),
                            rs.getString("course"),
                            rs.getString("year_level"),
                            rs.getString("scholarShip"),
                            rs.getString("discount_rate"),
                            rs.getString("subject_code"),
                            rs.getString("subject_title"),
                            rs.getInt("units"),
                            rs.getDouble("prelim"),
                            rs.getDouble("midterm"),
                            rs.getDouble("finalExam"),
                            rs.getDouble("finalGrade")
                    );
                }
                if(!hasRecord){System.out.println ("No records found on database");}

                System.out.println ("=========================================================================================================================================================================================================================");
            }catch(SQLException e){
                System.out.println ("Database error in viewing records: " + e.getMessage());
            }

        }
        else{
            System.out.println("Invalid choice. Choose between ( 1 - 3 ).");
        }

    }
}