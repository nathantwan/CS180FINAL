import javax.swing.ImageIcon;
import java.util.Scanner;
import java.util.ArrayList;
import java.io.*;

/**
 * Post class
 * <p>
 * Purdue University -- CS18000 -- Team Project
 *
 * @author Nathan Wan
 * @version Nov 3, 2024
 */

public class Post {
    private String caption;
    private String image;
    private int upvote;
    private int downvote;
    private User user;
    private static int counter = 1;
    private int postNumber;
    private ArrayList<Comment> comments = new ArrayList<>();
    public final Object obj = new Object();

    //write a constructor that intializes all fields
    public Post(String caption, String image, User user) throws InvalidPostException {
        if (caption == null || caption.isEmpty() || user == null) {
            throw new InvalidPostException("Invalid Post");
        }
        this.caption = caption;
        this.image = image;
        this.user = user;
        this.upvote = 0;
        this.downvote = 0;
        synchronized (obj) {
            this.postNumber = counter;
            counter++;
        }
    }

    public Post(String caption, String image, User user, int upvote, int downvote) throws InvalidPostException {
        if (caption == null || caption.isEmpty() || user == null) {
            throw new InvalidPostException("Invalid Post");
        }
        this.caption = caption;
        this.image = image;
        this.user = user;
        this.upvote = upvote;
        this.downvote = downvote;
        synchronized (obj) {
            this.postNumber = counter;
            counter++;
        }
    }

    // if only User parameter given, prompt user to onput fields
    public Post(User user, Scanner scan) {
        while (caption == null || caption.length() == 0) {
            System.out.println("Enter your caption:");
            synchronized (obj) {
                caption = scan.nextLine();
            }
            if (caption == null || caption.length() == 0) {
                System.out.println("Enter a valid caption");
            }
        }
        upvote = 0;
        downvote = 0;
        this.user = user;

    }

    //getters and setters
    public String getCaption() {
        return caption;
    }

    public String getImage() {
        return image;
    }

    public int getUpvote() {
        return upvote;
    }

    public int getDownvote() {
        return downvote;
    }

    public int getTotalVotes() {
        return upvote - downvote;
    }

    public User getUser() {
        return user;
    }

    public void editPost(String newCaption) {
        this.caption = newCaption;
    }

    public void incrementUpvote() {
        this.upvote++;
    }

    public void incrementDownvote() {
        this.downvote++;
    }

    public void setPost(String text, String pic) {
        this.caption = text;
        this.image = pic;
    }

    public ArrayList<Comment> getComments() {
        return comments;
    }
    
    public void addComment(String text, User postOwner, User commenter, Post post) {
        try {
            Comment comment = new Comment(text, postOwner, commenter, post);
            comments.add(comment);
        } catch (InvalidCommentException e) {
            System.out.println("Invalid Comment");
        }
    }

    public void deleteComment(Comment comment, User userInfo) {
        if (!(comment.getPostOwner().equals(userInfo)) && !(comment.getCommenter().equals(userInfo))) {
            System.out.println("You do not have permission");
            return;
        }
        int index = -1;
        for (int i = 0; i < comments.size(); i++) {
            if (comments.get(i).equals(comment)) {
                index = i;
                break;
            }
        }
        if (!(index == -1)) {
            comments.remove(index);
        } else {
            System.out.println("Comment does not exist");
        }
    }

    public void editComment(String text, Comment comment, User userInfo) throws InvalidCommentException {
        int index = -1;
        if (!(comment.getCommenter().equals(userInfo))) {
            System.out.println("You do not have permission!");
            return;
        }
        for (int i = 0; i < comments.size(); i++) {
            if (comments.get(i).equals(comment)) {
                index = i;
                break;
            }
        }
        if (!(index == -1)) {
            comments.get(index).setComment(text);
        } else {
            System.out.println("Comment does not exist");
        }


    }

    public boolean equals(Object o) {
        if (!(o instanceof Post)) {
            return false;
        }
        Post compare = (Post) o;

        // if (compare.getImage() == null) {
        //     if (image == null) {return compare.getCaption().equals(caption) && compare.getUser().equals(user);}
        //     return false;
        // }
        // if (image == null) {
        //     return false;
        // }

        if (image == null && compare.getImage() == null) {
            return compare.getCaption().equals(caption) && compare.getUser().equals(user);
        }
        if (image == null || compare.getImage() == null) {return false;}
        return compare.getCaption().equals(caption) &&
                compare.getImage().equals(image) && compare.getUser().equals(user);
    }

    public String toString() { 
        String s = "------------\n";
        s += "User: " + user.getUsername();
        s += "Profile Picture: " + user.getProfilePicture();
        s += "Caption: " + caption + "\n";
        String im = (image == null) ? "null" : image;
        s += "Image: " + im + "\n";
        s += "Comments: ";
        if (comments.size() == 0) {
            s += "None\n";
        }
        for (int i = 0; i < comments.size(); i++) {
            if (i == comments.size() - 1) {
                s += comments.get(i).getText() + "\n";
            } else {
                s += comments.get(i).getText() + ", ";
            }
        }
        s += "------------";

        return s;
    }

    public int getPostNumber() {
        return postNumber;
    }
    public String writePost() {
        String fileName = "Post" + postNumber + ".txt";
        File f = new File(fileName);
        try (PrintWriter pw = new PrintWriter(new FileWriter(f));
        PrintWriter pw2 = new PrintWriter(new FileWriter(new File("posts.txt")))) {
            String im = (image == null) ? "null" : image;
            pw.write(caption + ", " +  im + ", " 
                     + upvote + ", " + downvote + ", " + user.getUsername() + ", " + postNumber + "\n");
            if (!(comments == null || comments.size() == 0)) {
                for (Comment comment : comments) {
                    pw.write(comment.getText() + 
                             ", " + comment.getPostOwner().getUsername() 
                             + ", " + comment.getCommenter().getUsername() + "\n");
                }
            }
            pw2.write(fileName);


        } catch (Exception e) {
            System.out.println("File not created");
        }
        return fileName;

    }

}
