package com.mycompany.life_swift;

import java.sql.*;

public class Life_Swift {
Connection conn;
  public static Connection connectDB(){
   
   try{
       Class.forName("com.mysql.cj.jdbc.Driver");
       Connection conn=DriverManager.getConnection("jdbc:mysql://localhost:3306/life_swift","root","");
       System.out.println("Connection Successful");
       return conn;
   } catch(Exception e){
       System.out.println(e);
       return null;
   }     
  } 
}
