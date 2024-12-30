package src;

import java.sql.*;
import java.util.Scanner;

public class HotelReservationSystem
{
    private static final String url = "jdbc:mysql://localhost:3306/hotel_db";
    private static final String username = "root";
    private static final String password = "alok0987";
    public static void main(String[] args)
    {
        // load JDBC drivers
        try
        {
            Class.forName("com.mysql.cj.jdbc.Driver");
        }
        catch(ClassNotFoundException e)
        {
            System.out.println(e.getMessage());
        }
        //Create connection with Database
        try
        {
            Connection con = DriverManager.getConnection(url,username,password);
            Statement stmt = con.createStatement();
            while(true)
            {
                System.out.println();
                System.out.println("HOTEL RESERVATION SYSTEM");
                Scanner sc = new Scanner(System.in);
                System.out.println("1:- Reserve a room.");
                System.out.println("2:- View Reservation.");
                System.out.println("3:- Get Room Number.");
                System.out.println("4:- Update Reservation.");
                System.out.println("5:- Delete Reservation.");
                System.out.println("0:- Exit.");
                System.out.print("Choose an option: ");
                int choice = sc.nextInt();
                switch(choice)
                {
                    case 1:
                        reserveRoom(stmt,sc);
                        break;
                    case 2:
                        viewReservation(stmt);
                        break;
                    case 3:
                        getRoomNumber(stmt,sc);
                        break;
                    case 4:
                        updateReservation(con,stmt,sc);
                        break;
                    case 5:
                        deleteReservation(con,stmt,sc);
                        break;
                    case 0:
                        exit();
                        sc.close();
                        return;
                    default:
                        System.out.println("Invalid choice. Try again!");
                }
            }

        }
        catch(SQLException | InterruptedException e)
        {
            System.out.println(e.getMessage());
        }
    }
    private static void reserveRoom(Statement stmt,Scanner sc)  {
        try {
            System.out.print("Enter guest name: ");
            String guestName = sc.next();
            sc.nextLine();
            System.out.print("Enter room number: ");
            int roomNumber = sc.nextInt();
            System.out.print("Enter contact number: ");
            String contactNumber=sc.next();
            String query ="insert into reservation (guest_name,room_number,contact_number)"+
                    "values('"+guestName+"',"+ roomNumber +",'"+contactNumber+"')";
            try {
                int affectedRows = stmt.executeUpdate(query);
                if (affectedRows > 0) {
                    System.out.println("Reservation successfully!");
                } else {
                    System.out.println("Reservation failed.");
                }
            } finally {}
        } catch(SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    private static void viewReservation(Statement stmt) throws SQLException
    {
        String query = "select reservation_id,guest_name,room_number,contact_number,reservation_date from reservation";

        try(ResultSet rs = stmt.executeQuery(query))
        {
            System.out.println("Current Reservations:");
            System.out.println("+----------------+---------------+----------------+--------------------+-----------------------+");
            System.out.println("| Reservation ID | Guest         | Room Number    |  Contact Number    | Reservation Date      |");
            System.out.println("+----------------+---------------+----------------+--------------------+-----------------------+");
            while(rs.next())
            {
                int reservationId = rs.getInt("reservation_id");
                String guestName = rs.getString("guest_name");
                int roomNumber = rs.getInt("room_number");
                String contactNumber = rs.getString("contact_number");
                String reservationDate = rs.getTimestamp("reservation_date").toString();
                System.out.printf("| %-14d | %-13s | %-14d | %-18s | %-19s |\n",
                        reservationId,guestName,roomNumber,contactNumber,reservationDate);

            }
            System.out.println("+----------------+---------------+----------------+--------------------+-----------------------+");
        }
    }
    private static void getRoomNumber(Statement stmt,Scanner sc)
    {
        try
        {
            System.out.print("Enter reservation ID: ");
            int reservationId = sc.nextInt();
            System.out.print("Enter guest name: ");
            String guestName = sc.next();

            String query = "select room_number from reservation "+
                    "where reservation_id = "+reservationId+
                    " and guest_name = '"+guestName+"'";
            try(ResultSet rs = stmt.executeQuery(query))
            {
                if(rs.next())
                {
                    int roomNumber = rs.getInt("room_number");
                    System.out.println("Room number for Reservation ID "+reservationId+
                            "and Guest "+guestName+ " is: "+roomNumber);
                }
                else
                {
                    System.out.println("Reservation not found for given ID and guest name.");
                }
            }
        }
        catch(SQLException e)
        {
            System.out.println(e.getMessage());
        }
    }
    private static void updateReservation(Connection con,Statement stmt,Scanner sc) throws SQLException
    {
        try
        {
            System.out.println("Enter reservation ID to update: ");
            int reservationId = sc.nextInt();
            sc.nextLine();
            if(!reservationExists(con,reservationId))
            {
                System.out.println("Reservation not found for the given ID. ");
                return;
            }
            System.out.print("Enter new guest name: ");
            String newGuestName = sc.nextLine();
            System.out.print("enter new room number: ");
            int newRoomNumber = sc.nextInt();
            System.out.print("Enter new contact number: ");
            String newContactNumber = sc.next();

            String query = "update reservation set guest_name = '" +newGuestName+ "'," +
                    "room_number = " +newRoomNumber+ ","+
                    " contact_number = '"+newContactNumber+"'"+
                    "where reservation_id= " +reservationId;
            try
            {
                int affectedRows = stmt.executeUpdate(query);
                if(affectedRows>0)
                {
                    System.out.println("Reservation updated successfully!");
                }
                else {
                    System.out.println("Reservation update failed.");
                }
            } finally {}
        }
        catch(SQLException e)
        {
            System.out.println(e.getMessage());
        }
    }
    private static void deleteReservation(Connection con,Statement stmt,Scanner sc)
    {
        try
        {
            System.out.println("Enter reservation ID to delete. ");
            int reservationId = sc.nextInt();
            if(!reservationExists(con,reservationId))
            {
                System.out.println("Reservation not found for the given ID. ");
                return;
            }
            String query = "delete from reservation where reservation_id = "+reservationId;

            try
            {
                int affectedRows = stmt.executeUpdate(query);
                if(affectedRows>0)
                {
                    System.out.println("Reservation deleted successfully!");
                }
                else
                {
                    System.out.println("Reservation deletion failed.");
                }
            }finally {}
        }
        catch(SQLException e)
        {
            System.out.println(e.getMessage());
        }
    }
    private static boolean reservationExists(Connection con,int reservationId) throws SQLException
    {
        try
        {
            String query = " select reservation_id from reservation where reservation_id = "+reservationId;

            try(Statement stmt = con.createStatement())
            {
                ResultSet rs = stmt.executeQuery(query);
                return rs.next();
            }
        }
        catch(SQLException e)
        {
            System.out.println(e.getMessage());
            return false;
        }
    }
    public static void exit() throws InterruptedException {
        System.out.print("Exiting System");
        int i=6;
        while(i!=0)
        {
            System.out.print(".");
            Thread.sleep(300);
            i--;
        }
        System.out.println();
        System.out.println("ThankYou For Using Hotel Reservation System!!!!");
    }
}
