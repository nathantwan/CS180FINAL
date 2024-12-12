import java.io.*;
import java.util.*;
import javax.swing.ImageIcon;

/**
 * User class
 * <p>
 * Purdue University -- CS18000 -- Team Project
 *
 * @author Yajushi 
 * @version Nov 3, 2024
 */
public class User {
    private ArrayList<User> friends = new ArrayList<User>();
    private ArrayList<User> blocked = new ArrayList<User>();

    private String firstName;
    private String lastName;
    private String username;
    private String password;
    
    private String profilePicture;

    private static ArrayList<String> usernameArray = new ArrayList<String>();
    private final Object obj = new Object();

    public User(String firstName, String lastName, String username, String password, String profilePicture) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.password = password;
        this.profilePicture = profilePicture;
        synchronized (obj) {
            usernameArray.add(username);
        }
        
    }

    public User(Scanner s) {
        String input;
        System.out.println("Create a new user profile!");

        //Get first and last name
        System.out.println("Enter your first and last name: ");
        while (true) {
            synchronized (obj) {
                input = s.nextLine();
            }
            
            if (input == null || input.indexOf(" ") == -1) {
                System.out.println("Error: Invalid Input! Please enter your first and last name.");
            } else {
                firstName = input.substring(0, input.indexOf(" "));
                lastName = input.substring(input.indexOf(" ") + 1);
                break;
            }
        }
        System.out.println("Welcome " + firstName + " " + lastName);

        //Get username
        System.out.println("Enter your desired username: ");
        while (true) {
            synchronized (obj) {
                input = s.nextLine();
            }
            
            if (input == null || input.indexOf(" ") != -1) {
                System.out.println("Error: Invalid Input! Please enter a valid username.");
            } else if (usernameArray.contains(input)) { //ensure username not taken
                System.out.println("This username is taken. Please enter another username.");
            } else {
                username = input;
                synchronized (obj) {
                    usernameArray.add(username);
                }
                break;
            }
        }
        System.out.println("Your username has been set to " + username);

        //Get password
        System.out.println("Enter your password: ");
        while (true) {
            synchronized (obj) {
                input = s.nextLine();
            }
            if (input == null || input.indexOf(" ") != -1) {
                System.out.println("Error: Invalid Input! Please enter a valid password.");
            } else if (input.length() < 7 || input.length() > 12) { //ensure username not taken
                System.out.println("Please enter a password 7 - 12 characters long");
            } else {
                password = input;
                break;
            }
        }
        System.out.println("Your password has been saved");

        //upload picture
        System.out.println("Enter the path for your profile picture");
        synchronized (obj) {
            input = s.nextLine();
        }
        profilePicture = input;
    }

    public String getPassword() { //can be used for password protection login
        return password;
    }
    public String getUsername() {
        return username;
    }
    public String getFirstName() {
        return firstName;
    }
    public String getLastName() {
        return lastName;
    }
    public String getName() {
        String name = firstName + " " + lastName;
        return name;
    }
    public String getProfilePicture() {
        return profilePicture;
    }
    public ArrayList<User> getFriends() {
        return friends;
    }
    public ArrayList<User> getBlocked() {
        return blocked;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public void setFriends(ArrayList<User> friends) {
        this.friends = friends;
    }
    public void setBlocked(ArrayList<User> blocked) {
        this.blocked = blocked;
    }
    public void setProfilePicture(String profilePic) {
        this.profilePicture = profilePic;
    }
    public boolean equals(Object other) { //use for user search
        if (!(other instanceof User)) {
            return false;
        }

        User o = (User) other;
        return o.getUsername().equals(username);
    }
    
    //return true if password changed
    public boolean setPassword(String oldPass, String newPass) {
        if (oldPass.equals(password)) { //old password must match in order to change password
            password = newPass;
            return true;
        }
        return false;
    }

    //adding a friend
    public void addFriend(User f) {
        if (!friends.contains(f)) {
            friends.add(f);
        }
        if (blocked.contains(f)) { //unblocks the user if they were previously blocked
            blocked.remove(f);
        }
    }

    public void blockUser(User b) {
        if (!blocked.contains(b)) {
            blocked.add(b);
        }
        if (friends.contains(b)) { //removes blocked user from friends list
            friends.remove(b);
        }
    }

    public void unblock(User b) {
        if (blocked.contains(b)) {
            blocked.remove(b);
        }
    }

    public void removeFriend(User f) {
        if (friends.contains(f)) {
            friends.remove(f);
        }
    }

    public String toString() { //user profile
        String s = "------------\n";
        s += "Name: " + getName() + "\n";
        s += "Username: " + username + "\n";
        String im = (profilePicture == null) ? "null" : profilePicture;
        s += "Profile Picture: " + im + "\n";
        s += "Friends: ";
        if (friends.size() == 0) {
            s += "None\n";
        }
        for (int i = 0 ; i < friends.size(); i++) {
            if (i == friends.size() - 1) {
                s += friends.get(i).getName() + "\n";
            } else {
                s += friends.get(i).getName() + ", ";
            }
        }
        s += "------------";

        return s;
    } 
    
    public ArrayList<Post> displayFeed(ArrayList<Post> posts) {
        ArrayList<Post> feed = new ArrayList<Post>();
        for (Post p : posts) {
            if (friends.contains(p.getUser()) || p.getUser().getUsername().equals(username)) {
                feed.add(p);
            }
        }
        return feed;
    }
    
    public void writeFile() {
        String fileName = username + ".txt";
        try {
            PrintWriter pw = new PrintWriter(new FileOutputStream(new File(fileName), false));
            pw.println(username);
            pw.println("FRIENDS");
            for (User f : friends) {
                pw.println(f.getUsername());
            }
            pw.println("BLOCKED");
            for (User b : blocked) {
                pw.println(b.getUsername());
            }
            pw.close();
        } catch (FileNotFoundException e) {
            System.out.println("ERROR: File not created");
        }

    }
    
}
