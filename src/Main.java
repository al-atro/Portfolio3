import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import java.util.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;

public class Main extends Application {
    private Model model=new Model();
    private Controller con=new Controller(model,this);
    private TextField field=new TextField();
    private TextArea area=new TextArea();
    ComboBox<String> lecturer = new ComboBox<>();
    ComboBox<String> courses = new ComboBox<>();
    ComboBox<String> rooms = new ComboBox<>();
    ComboBox<String> timeslot = new ComboBox<>();
    Button button = new Button("Add lecturer");
    Button button2 = new Button("Find room");
    Button button3 = new Button("Find lecturer");
    Button button4 = new Button("Find course");
    Button button5 = new Button("Add to schedule");
    Button button6 = new Button("Add course");
    Button button7 = new Button("Assign lecturer");
    Button button8 = new Button("Expected number of students");
    Text welcome = new Text();
    Text intro = new Text();
    Text intro2 = new Text();
    void setArea(String s){area.setText(s);}
    void clearField(){field.setText("");}
    @Override
    public void start(Stage stage) {
        con.initArea();
        field.setOnAction(e->con.enterText(field.getText()));
        VBox root = new VBox(welcome,courses,lecturer,rooms,timeslot,intro,button2,button3,button4,intro2,field,button,button6,button5,button7,button8,area);
        lecturer.getItems().addAll(model.getLecturer());
        lecturer.setPromptText("Lecturer");
        courses.getItems().addAll(model.getCourse());
        courses.setPromptText("Course");
        rooms.getItems().addAll(model.getRoom());
        rooms.setPromptText("Room");
        timeslot.getItems().addAll(model.getTimeslot());
        timeslot.setPromptText("Timeslot");
        button.setOnAction(e->con.addLecturer(field.getText()));
        button2.setOnAction(e->con.findRoom(courses.getValue(), timeslot.getValue()));
        button3.setOnAction(e->con.findLecturer(courses.getValue()));
        button4.setOnAction(e->con.findCourse(timeslot.getValue()));
        button5.setOnAction(e->con.assignTimeslot(timeslot.getValue(), courses.getValue(), rooms.getValue()));
        button6.setOnAction(e->con.addCourse(field.getText()));
        button7.setOnAction(e->con.assignLecturer(courses.getValue(), lecturer.getValue()));
        button8.setOnAction(e->con.assignStud(courses.getValue(), field.getText()));
        welcome.setText("Welcome to the Course Management System");
        welcome.setFont(Font.font ("Verdana", 20));
        intro.setText(model.introText());
        intro.setFont(Font.font ("Verdana", 12));
        intro2.setText(model.intro2Text());
        intro2.setFont(Font.font ("Verdana", 12));
        Scene scene = new Scene(root, 550, 650);
        stage.setTitle("Course Management system");
        stage.setScene(scene);
        stage.show();
    }
    public static void main(String[] args) {
        launch(args);
    }
}

class Controller{
    Model model;
    Main view;
    Controller(Model model, Main view){
        this.model=model; this.view=view;
    }
    void initArea(){
        String toarea="";
        for(String t:model.get())toarea+=t+"\n";
        view.setArea(toarea);
    }
    void enterText(String s){
        model.add(s);
        view.clearField();
        String toarea="";
        for(String t:model.get())toarea+=t+"\n";
        view.setArea(toarea);
    }
    void addLecturer(String s){
        if(model.hasLecturer(s)){
            view.setArea("Cannot insert lecturer (repeat) "+s);
        } else {
            model.addLecturer(s);
            view.lecturer.getItems().add(s);
            view.setArea("Lecturer added:  "+s);
        }
    }
    void findRoom(String course, String timeslot){
        String room=model.findRoom(course, timeslot);
        if(room.equals(""))view.setArea("No Room");
        else view.setArea("Room: "+room);
    }

    void findLecturer(String c){
        String lecturer=model.findLecturer(c);
        if(lecturer.equals(""))view.setArea("No Lecturer");
        else view.setArea("The lecturer of this course is "+lecturer);
    }

    void findCourse(String c){
        String course=model.findCourse(c);
        if(course.equals(""))view.setArea("No course takes place at this timeslot");
        else view.setArea("The course that takes place at this timeslot is "+course);
    }

    /*
    void assignRoom(String course, String room){
        if (Integer.parseInt(model.studInCourse(course)) > Integer.parseInt(model.studInRoom(room))){
            model.addRoomsCourses(course, room);
            view.setArea("The room is assigned. Warning: the expected amount of students is larger, \nthan the capacity of the room!");
        } else {
            view.setArea("The room is assigned.");
            model.addRoomsCourses(course, room);}
    }

     */

    void assignTimeslot(String timeslot, String course, String room){
        if (
               model.roomFromTimeslot(timeslot).contains(room)
            ){
            view.setArea("The timeslot cannot be assigned. \nTwo courses cannot take place in the same room at the same time.");
            }
        else if (
                model.lecturerFromTimeslot(timeslot).contains(model.findLecturer(course))
                ) {
                view.setArea("The timeslot cannot be assigned. \nOne lecturer cannot give two lectures at the same time.");
                }
        else if(
                (Integer.parseInt(model.studInCourse(course)) > Integer.parseInt(model.studInRoom(room)))
        ){
            model.addTimeslotCourse(timeslot, course, room);
            view.setArea("The timeslot is assigned. Warning: the expected amount of students is larger, \nthan the capacity of the room!");
        }
        else {
            model.addTimeslotCourse(timeslot, course, room);
            view.setArea("The timeslot is assigned.");
        }
    }

    void addCourse(String s){
        if(model.hasCourse(s)){
            view.setArea("Cannot insert course (repeat) "+s);
        } else {
            model.addCourseUser(s);
            view.courses.getItems().add(s);
            view.setArea("Course added:  "+s);
        }
    }

    void assignLecturer(String course, String lecturer){
        model.assignLecturer(course, lecturer);
        view.setArea("Lecturer assigned to "+course+": "+lecturer);
    }

    void assignStud(String course, String stud){
        model.assignStud(course, stud);
        view.setArea("The expected amount of student in the course "+course+" is now "+stud);
    }


}

class Model{
    MyDB db=new MyDB();
    Model(){
/*
        db.cmd("drop table if exists lst1;");
        db.cmd("create table if not exists lst1 "+
                "(fld1 integer primary key autoincrement, fld2 text);");

        db.cmd("drop table if exists Lecturer;");
        db.cmd("create table if not exists Lecturer "+
                "(id integer primary key autoincrement, name text);");
        addLecturer("Cameron Diaz");
        addLecturer("Jane Fonda");
        addLecturer("Brad Pitt");

        db.cmd("drop table if exists Courses;");
        db.cmd("create table if not exists Courses "+
                "(id integer primary key autoincrement, name text, stud integer, lecturerid integer, foreign key (lecturerid) references Lecturer (id));");
        addCourse("Software Development", "60", "Brad Pitt");
        addCourse("Essential Computing", "50", "Cameron Diaz");
        addCourse("Genetics", "80", "Jane Fonda");

        db.cmd("drop table if exists Rooms;");
        db.cmd("create table if not exists Rooms "+
                "(id integer primary key autoincrement, name text, stud integer);");
        addRoom("1.1","150");
        addRoom("9.2","50");
        addRoom("5.5","20");
        addRoom("10.1","70");
        addRoom("6.7","40");

        db.cmd("drop table if exists Timeslot;");
        db.cmd("create table if not exists Timeslot "+
                "(id integer primary key autoincrement, name text);");
        String[] days = {"Monday","Tuesday","Wednesday","Thursday","Friday"};
        for(String day:days){addTimeslot(day+" AM");addTimeslot(day+" PM");}

        db.cmd("drop table if exists TimeslotCourses;");
        db.cmd("create table if not exists TimeslotCourses "+
                "(timeslotid integer not null, courseid integer not null, roomid integer, foreign key (timeslotid) references Timeslot (id), foreign key (courseid) references Courses (id), foreign key (roomid) references Rooms (id));");
        addTimeslotCourse("Monday AM", "Software Development", "10.1");
        addTimeslotCourse("Monday PM", "Genetics", "1.1");
        addTimeslotCourse("Tuesday PM", "Essential Computing", "9.2");
        addTimeslotCourse("Thursday AM", "Software Development", "10.1");
        addTimeslotCourse("Thursday PM", "Genetics", "1.1");
        addTimeslotCourse("Friday AM", "Essential Computing", "9.2");

        /*
        db.cmd("drop table if exists RoomsCourses;");
        db.cmd("create table if not exists RoomsCourses "+
                "(roomid integer, courseid integer, foreign key (roomid) references Rooms (id), foreign key (courseid) references Courses (id));");
        addRoomsCourses("1.1", "Genetics");
        addRoomsCourses("9.2", "Essential Computing");
        addRoomsCourses("10.1", "Software Development");

         */

    }




    void addLecturer(String s){  db.cmd("insert into Lecturer (name) values ('"+s+"');");}
    ArrayList<String> getLecturer(){return db.query("select name from Lecturer;","name");}

    boolean hasLecturer(String s){
        ArrayList<String> lst= db.query("select name from Lecturer where name = '"+s+"';","name");
        System.out.println(lst);
        return lst.size()>0;
        //return getLecturer().contains(s);
    }
    boolean hasCourse(String s){
        ArrayList<String> lst= db.query("select name from Courses where name = '"+s+"';","name");
        System.out.println(lst);
        return lst.size()>0;
        //return getLecturer().contains(s);
    }
    boolean LecturerAssigned(String s){
        ArrayList<String> lst= db.query("select Lecturer.name from Lecturer\n" +
                "inner join Courses on Lecturer.id = Courses.lecturerid\n" +
                "where Courses.name = '"+s+"'","name");
        System.out.println(lst);
        return lst.size()>0;
        //return getLecturer().contains(s);
    }
    String getLecturerID(String name){
        ArrayList<String> ids = db.query("select id from Lecturer where name = '"+name+"';","name");
        if (ids.size()>0) {
            return ids.get(0);
        } else {return "";}
    }

    void addRoom(String s,String stud){db.cmd("insert into Rooms (name,stud) values ('"+s+"',"+stud+");");}
    ArrayList<String> getRoom(){return db.query("select name from Rooms;","name");}

    void addCourse(String s, String stud, String lecname){ db.cmd("insert into Courses (name,stud, lecturerid) values ('"+s+"',"+stud+",(select id from Lecturer where name = '"+lecname+"'));");}
    ArrayList<String> getCourse(){
        return db.query("select name from Courses;","name");
    }

    void addCourseUser(String s){ db.cmd("insert into Courses (name) values ('"+s+"');");}

    String studInCourse(String s){
        ArrayList<String> lst= db.query("select stud from Courses where name = '"+s+"';","stud");
        if (lst.size()>0) {
            return lst.get(0);
        } else {return "";}
    }

    String studInRoom(String s){
        ArrayList<String> lst= db.query("select stud from Rooms where name = '"+s+"';","stud");
        if (lst.size()>0) {
            return lst.get(0);
        } else {return "";}
    }

    ArrayList<String> roomFromTimeslot(String s){
        ArrayList<String> lst= db.query("select Rooms.name from Rooms" +
                " inner join TimeslotCourses on Rooms.id = TimeslotCourses.roomid" +
                " inner join Timeslot on Timeslot.id = TimeslotCourses.timeslotid" +
                " where Timeslot.name = '"+s+"'", "name");
        return lst;
    }

    ArrayList<String> lecturerFromTimeslot(String s){
        ArrayList<String> lst= db.query("select Lecturer.name from Lecturer\n" +
                "inner join Courses on Lecturer.id = Courses.lecturerid\n" +
                "inner join TimeslotCourses on Courses.id = TimeslotCourses.courseid\n" +
                "inner join Timeslot on TimeslotCourses.timeslotid = Timeslot.id\n" +
                "where Timeslot.name = '"+s+"'", "name");
        return lst;
    }

    void addTimeslotCourse(String timeslot, String course, String room){db.cmd("insert into TimeslotCourses(timeslotid, courseid, roomid) values ((select id from Timeslot where name = '"+
            timeslot+"'), (select id from Courses where name = '"+course+"'), (select id from Rooms where name = '"+room+"'))");}

    void addRoomsCourses(String room, String course){db.cmd("insert into RoomsCourses(roomid, courseid) values ((select id from Rooms where name = '"+
            room+"'), (select id from Courses where name = '"+course+"'))");}


    String findRoom(String course, String timeslot){
        ArrayList<String> lst= db.query(
                "select Rooms.name from Rooms\n" +
                        "inner join TimeslotCourses on Rooms.id = TimeslotCourses.roomid\n" +
                        "inner join Timeslot on TimeslotCourses.timeslotid = Timeslot.id\n" +
                        "inner join Courses on TimeslotCourses.courseid = Courses.id\n" +
                        "where Courses.name = '"+course+"' and Timeslot.name = '"+timeslot+"'", "name");
        System.out.println(lst);
        if(lst.size()==0)return "";
        else return lst.get(0);
    }


    String findLecturer(String c){
        ArrayList<String> lst= db.query(
                "select Lecturer.name from Lecturer inner join Courses on Courses.lecturerid = Lecturer.id where Courses.name = '"+c+"'","name");
        System.out.println(lst);
        if(lst.size()==0)return "";
        else return lst.get(0);
    }

    String findCourse(String c){
        ArrayList<String> lst= db.query(
                "select Courses.name from ((Courses inner join TimeslotCourses on Courses.id = TimeslotCourses.courseid)" +
                        " inner join Timeslot on Timeslot.id = TimeslotCourses.timeslotid) where Timeslot.name = '"+c+"'","name");
        System.out.println(lst);
        if(lst.size()==0)return "";
        else return lst.get(0);
    }

    void addTimeslot(String s){ // remember to sanitize your data!
        db.cmd("insert into Timeslot (name) values ('"+s+"');");
    }
    ArrayList<String> getTimeslot(){
        return db.query("select name from Timeslot;","name");
    }

    void assignLecturer(String course, String lecturer){
        db.cmd("update Courses set lecturerid = (select id from Lecturer where name = '"+lecturer+"') where name = '"+course+"'");
    }
    void assignStud(String course, String stud){
        db.cmd("update Courses set stud = "+stud+" where name = '"+course+"'");
    }

    void add(String s){ // remember to sanitize your data!
        db.cmd("insert into lst1 (fld2) values ('"+s+"');");
    }
    ArrayList<String> get(){
        return db.query("select fld2 from lst1 order by fld1;","fld2");
    }
    public String introText(){
        return " To find a room in which a course is taking place, choose a course and a timeslot" +
                "\n and press \"Find room\". " +
                "\n To find a lecturer assigned to the course, choose a course and press \"Find lecturer\"." +
                "\n To check if a course is assigned to a timeslot, choose a timeslot and press \"Find course\".";
    }

    public String intro2Text(){
        return " To add a lecturer to the system, type the lecturer's name and press \"Add lecturer\"." +
                "\n To add a course to the system, type the name of the course and press \"Add course\"." +
                "\n To add a course to the schedule, choose a course, a room and a timeslot" +
                "\n and press \"Add to schedule\"." +
                "\n To assign a lecturer to a course, choose a course and a lecturer and press " +
                "\n \"Assign lecturer\"." +
                "\n To specify the expected number of students, choose a course, type a" +
                "\n number of students and press \"Expected number of students\".";
    }
}

class MyDB{
    Connection conn = null;
    MyDB(){
        if(conn==null)open();
    }
    public void open(){
        try {
            String url = "jdbc:sqlite:listdb.db";
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println("cannot open");
            if (conn != null) close();
        };
    }
    public void close(){
        try {
            if (conn != null) conn.close();
        } catch (SQLException e ) {
            System.out.println("cannot close");
        }
        conn=null;
    }
    public void cmd(String sql){
        if(conn==null)open();
        if(conn==null){System.out.println("No connection");return;}
        Statement stmt=null;
        try {
            stmt = conn.createStatement();
            stmt.executeUpdate(sql);
        } catch (SQLException e ) {
            System.out.println("Error in statement "+sql);
        }
        try {
            if (stmt != null) { stmt.close(); }
        } catch (SQLException e ) {
            System.out.println("Error in statement "+sql);
        }
    }
    public ArrayList<String> query(String query,String fld){
        ArrayList<String> res=new ArrayList<>();
        if(conn==null)open();
        if(conn==null){System.out.println("No connection");return res;}
        Statement stmt=null;
        try {
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                String name = rs.getString(fld);
                res.add(name);
            }
        } catch (SQLException e ) {
            System.out.println("Error in statement "+query+" "+fld);
        }
        try {
            if (stmt != null) { stmt.close(); }
        } catch (SQLException e ) {
            System.out.println("Error in statement "+query+" "+fld);
        }
        return res;
    }
}