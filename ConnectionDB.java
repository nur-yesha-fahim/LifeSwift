
package com.mycompany.software_project;

import java.sql.Connection;
import java.sql.DriverManager;


public class ConnectionDB {
    
   public static Connection connect(){
       try{
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/emrs_db","root","");
            System.out.println("Yeee Connection ho gaya");
            return connection;
      }
       catch(Exception e){
           System.out.println(e);
           return null;
       }
    
   }
    public static void main(String[] args) {
        connect();
    }
}
