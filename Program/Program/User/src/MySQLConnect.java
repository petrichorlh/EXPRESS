import java.sql.*;
import javax.swing.*;

public class MySQLConnect {
    Connection conn=null;
    public static Connection ConnectDb(){
        try{
        Class.forName("com.mysql.jdbc.Driver");
        Connection conn=DriverManager.getConnection("jdbc:mysql://localhost/delivery","root","123456");
        System.out.println("Connection Success");
        
        return conn;
        }
        catch(Exception e){
        JOptionPane.showMessageDialog(null, e);
        return null;
        }
    }
}