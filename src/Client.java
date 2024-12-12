import javax.swing.*;
import java.io.*;
import java.net.*;
import java.util.List;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
/**
 * Client Class
 * <p>
 * Purdue University -- CS18000 -- Phase 2
 *
 * @author Nathan Wan
 * @version Nov 17, 2024
 * */

public class Client implements Runnable {
    private static final String[] MENU = new String[]{"1 - Add a friend",
            "2 - Remove a friend", "3 - Block a user", "4 - Unblock a user",
            "5 - View a user profile", "6 - View feed",
            "7 - Create a post", "8 - Delete a post",
            "9 - Edit a post", "10 - Create a comment",
            "11 - Delete a comment", "12 - Edit a comment",
            "13 - Upvote a post",
            "14 - Downvote a post", "15 - Change password", "16 - Exit"};
    private JFrame frame;
    private CardLayout cardLayout;
    private JPanel cardPanel;
    private String pfpPath = "";
    private Socket socket = null;
    private BufferedReader reader = null;
    private PrintWriter writer = null;
    private List<Integer> postNums = new ArrayList<>();
    boolean runLoop = true;


    public void run() {
        String hostName = "localhost";
        int portNumber = 4242;
        try {
            socket = new Socket(hostName, portNumber);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream());
            System.out.println("Connection Established");

            frame = new JFrame("Twitter Client");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1000, 500);
            frame.setLocationRelativeTo(null);

            cardLayout = new CardLayout();
            cardPanel = new JPanel(cardLayout);

            cardPanel.add(createWelcomePage(), "WelcomePage");
            cardPanel.add(createLoginPage(), "LoginPage");
            cardPanel.add(createSignUpPage(), "SignUpPage");

            frame.add(cardPanel);
            frame.setVisible(true);
            socket.setKeepAlive(true);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Connection Failed", "Search Engine",
                    JOptionPane.ERROR_MESSAGE);
        } finally {
            // try {
            //     if (reader != null) {
            //         reader.close();
            //     }
            //     if (writer != null) {
            //         writer.close();
            //     }
            //     if (socket != null) {
            //         socket.close();
            //     }
            // } catch (IOException ex) {
            //     JOptionPane.showMessageDialog(null, "Error closing resources", "Search Engine",
            //             JOptionPane.ERROR_MESSAGE);
            // }
        }
    }
    public static void main(String[] args) {
        Thread thread = new Thread(new Client());
        thread.start();
    }
    private JPanel createWelcomePage() {
        // Use BorderLayout for the main panel
        JPanel panel = new JPanel(new BorderLayout());

        // Create the label
        JLabel welcomeLabel = new JLabel("Welcome to Twitter!");
        welcomeLabel.setFont(new Font("Roboto", Font.PLAIN, 70));
        welcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);  // Center the label horizontally

        // Create the buttons
        JButton loginButton = new JButton("Login");
        JButton signUpButton = new JButton("Sign Up");

        // Add action listeners to switch to the login or sign-up page
        loginButton.addActionListener(e -> cardLayout.show(cardPanel, "LoginPage"));
        signUpButton.addActionListener(e -> cardLayout.show(cardPanel, "SignUpPage"));

        // Create a panel to hold the buttons and use FlowLayout to center them horizontally
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 20));
        buttonPanel.add(loginButton);
        buttonPanel.add(signUpButton);

        // Add the welcome label to the center of the BorderLayout panel
        panel.add(welcomeLabel, BorderLayout.CENTER);

        // Add the button panel to the bottom of the BorderLayout panel
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }
    private JPanel createLoginPage() {
        JPanel panel = new JPanel();
        JTextField usernameField = new JTextField(10);
        JPasswordField passwordField = new JPasswordField(10);
        JButton loginButton = new JButton("Login");

        // Add action listener for login
        loginButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            sendLoginRequest(username, password);
        });

        panel.add(new JLabel("Username:"));
        panel.add(usernameField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);
        panel.add(loginButton);

        return panel;
    }
    private JPanel createSignUpPage() {
        JPanel panel = new JPanel(new BorderLayout());

        // Create text fields with 10 columns
        JTextField firstNameField = new JTextField(5);
        JTextField lastNameField = new JTextField(5);
        JTextField userField = new JTextField(5);
        JPasswordField passField = new JPasswordField(5);

        // Create a sub-panel for the text fields and labels (input panel)
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.X_AXIS));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Add labels and text fields to the input panel
        inputPanel.add(new JLabel("First Name:"));
        inputPanel.add(firstNameField);
        inputPanel.add(new JLabel("Last Name:"));
        inputPanel.add(lastNameField);
        inputPanel.add(new JLabel("Username:"));
        inputPanel.add(userField);
        inputPanel.add(new JLabel("Password:"));
        inputPanel.add(passField);

        // Create sign-up button and add to the inputPanel
        JButton signUpButton = new JButton("Sign Up");
        inputPanel.add(signUpButton);  // Add the button after the fields

        // Create image label and upload button
        JLabel imageLabel = new JLabel("No Image Selected", SwingConstants.CENTER);
        imageLabel.setPreferredSize(new Dimension(200, 200)); // Set fixed size for the label
        JButton uploadButton = new JButton("Upload Profile Picture");

        // Action listener for image upload
        uploadButton.addActionListener(e -> {
            String path = promptForPicture("Select Profile Picture");
            ImageIcon profileImageIcon = new ImageIcon(path);

            if (profileImageIcon != null) {
                // Resize the image to fit the label
                Image img = profileImageIcon.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH);
                profileImageIcon = new ImageIcon(img);
                imageLabel.setIcon(profileImageIcon);
                imageLabel.setText(""); // Remove "No Image Selected" text
                pfpPath = path; // Store the profile picture path
            } else {
                JOptionPane.showMessageDialog(panel, "No image was selected.");
            }
        });

        // Action listener for sign-up
        signUpButton.addActionListener(e -> {
            String firstName = firstNameField.getText();
            String lastName = lastNameField.getText();
            String username = userField.getText();
            String password = new String(passField.getPassword());
            sendSignUpRequest(firstName, lastName, username, password, pfpPath);
            cardPanel.add(createMainPage(), "MainPage");
            cardLayout.show(cardPanel, "MainPage");
        });

        // Create a panel for the image label and upload button
        JPanel imagePanel = new JPanel();
        imagePanel.setLayout(new BorderLayout());
        imagePanel.add(imageLabel, BorderLayout.CENTER);
        imagePanel.add(uploadButton, BorderLayout.SOUTH);

        // Add image panel to the NORTH region
        panel.add(imagePanel, BorderLayout.NORTH);

        // Add the input panel (containing text fields and labels) to the center region
        panel.add(inputPanel, BorderLayout.SOUTH);

        return panel;
    }



    private JPanel createMainPage() {
        List<String> usernames = new ArrayList<>();

        writer.println("Option 6"); writer.flush();
        try {
            String line = reader.readLine();
            System.out.println(line);
            if (line != null && !line.isEmpty()) {
                String[] nums = line.split(",");
                for (String n : nums) {postNums.add(Integer.parseInt(n));}
            }
        } catch (Exception e) {}

        System.out.println(postNums.size());

        writer.println("Get Users");
        writer.flush();
        try {
            String username = reader.readLine();
            while (username != null && !username.equals("stop")) {
                if (username.equals("No other users")) {break;}
                usernames.add(username);
                username = reader.readLine();
            }
        } catch (IOException e) {}
        
        // Use BorderLayout for the main panel
        JPanel panel = new JPanel(new BorderLayout());
        // Create the page label
        JLabel pageLabel = new JLabel("Main Page", SwingConstants.CENTER);
        pageLabel.setFont(new Font("Roboto", Font.BOLD, 50));

        JButton exitButton = new JButton("Exit");
        exitButton.addActionListener(e -> {
            sendExitMessage();
        });

        // Create the feed panel to hold all posts
        JPanel feedPanel = new JPanel();
        feedPanel.setLayout(new BoxLayout(feedPanel, BoxLayout.Y_AXIS));  // Stack posts vertically


        // Loop through the sorted posts and create a panel for each one
        for (int postNum : postNums) {
            // Create a panel for this post
            JPanel postPanel = new JPanel();
            postPanel.setLayout(new BorderLayout());

            writer.println("Post Info"); writer.flush(); writer.println(String.valueOf(postNum)); writer.flush();
            String[] info = null;
            try {
                String s = reader.readLine(); System.out.println(s);
                info = s.split(",");} catch (Exception e) {}
            

            // Load the image and create an ImageIcon
            JLabel imageLabel = null;
            if (info[0] != null) {ImageIcon imageIcon = new ImageIcon(info[0]);  // Path to image (ensure image exists)
            imageLabel = new JLabel(imageIcon);}

            // Create a label for the caption
            JLabel captionLabel = new JLabel(info[1]);
            captionLabel.setFont(new Font("Roboto", Font.PLAIN, 16));

            // Create a label for the votes
            JLabel voteLabel = new JLabel("Upvotes: " + info[2] + " | Downvotes: " + info[3]);
            voteLabel.setFont(new Font("Roboto", Font.PLAIN, 14));

            // Create upvote and downvote buttons
            JButton upvoteButton = new JButton("Upvote");
            JButton downvoteButton = new JButton("Downvote");

            // Add action listeners to the buttons to handle votes
            upvoteButton.addActionListener(e -> {
                try {
                    writer.println("Option 13"); writer.flush(); writer.println(String.valueOf(postNums.indexOf(postNum))); writer.flush();
                    reader.readLine();
                    updateFeed(feedPanel);} catch (Exception er) {}
            });

            downvoteButton.addActionListener(e -> {
                try {
                    writer.println("Option 14"); writer.flush(); writer.println(String.valueOf(postNums.indexOf(postNum))); writer.flush();
                    reader.readLine();
                    updateFeed(feedPanel);} catch (Exception er) {}
            });

            // Create a panel for the buttons (upvote and downvote)
            JPanel votePanel = new JPanel();
            votePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
            votePanel.add(upvoteButton);
            votePanel.add(downvoteButton);

            // Add the image, caption, votes, and vote buttons to the post panel
            postPanel.add(imageLabel, BorderLayout.NORTH);
            postPanel.add(captionLabel, BorderLayout.CENTER);
            postPanel.add(voteLabel, BorderLayout.SOUTH);
            postPanel.add(votePanel, BorderLayout.SOUTH);

            JPanel commentLabel = new JPanel();

            int i = 4;
            while (i < info.length) {
                JLabel comment = new JLabel(info[i]);
                commentLabel.add(comment);
                i++;
            }

            postPanel.add(commentLabel, BorderLayout.EAST);

            // Add the post panel to the feed
            feedPanel.add(postPanel);
        }

        // Make the feed scrollable
        JScrollPane scrollPane = new JScrollPane(feedPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        JPanel sidePanel = new JPanel();
        sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));
        sidePanel.setBackground(Color.LIGHT_GRAY);
        sidePanel.setBorder(BorderFactory.createTitledBorder("Actions"));

        JButton addFriend = new JButton("Add Friend"); sidePanel.add(addFriend);
        addFriend.addActionListener(e -> {
            String friendUsername = (String) JOptionPane.showInputDialog(null, "Select which user to add",
                    "Add Friend", JOptionPane.QUESTION_MESSAGE, null, usernames.toArray(), null);

            writer.write("Option 1");
            writer.println();
            writer.flush();
            writer.write(friendUsername);
            writer.println();
            writer.flush();
            try {JOptionPane.showMessageDialog(null, reader.readLine(), "Add Friend", JOptionPane.INFORMATION_MESSAGE);}
            catch (IOException er) {};

            updateFeed(feedPanel);


        });
        JButton removeFriend = new JButton("Remove Friend"); sidePanel.add(removeFriend);
        removeFriend.addActionListener(e -> {
            String friendUsername = (String) JOptionPane.showInputDialog(null, "Select which user to remove as a friend",
                    "Remove Friend", JOptionPane.QUESTION_MESSAGE, null, usernames.toArray(), null);


                writer.write("Option 2");
                writer.println();
                writer.flush();
                writer.write(friendUsername);
                writer.println();
                writer.flush();
                try {JOptionPane.showMessageDialog(null, reader.readLine(), "Remove Friend", JOptionPane.INFORMATION_MESSAGE);}
                catch (IOException er) {};

                updateFeed(feedPanel);
            

        });
        JButton blockUser = new JButton("Block a User"); sidePanel.add(blockUser);
        blockUser.addActionListener(e -> {
            String blockUsername = (String) JOptionPane.showInputDialog(null, "Select which user to block",
                    "Block User", JOptionPane.QUESTION_MESSAGE, null, usernames.toArray(), null);

                writer.write("Option 3");
                writer.println();
                writer.flush();
                writer.write(blockUsername);
                writer.println();
                writer.flush();
                try {JOptionPane.showMessageDialog(null, reader.readLine(), "Block User", JOptionPane.INFORMATION_MESSAGE);}
                catch (IOException er) {};

                updateFeed(feedPanel);
            
        });
        JButton unBlockUser = new JButton("Unblock a User");  sidePanel.add(unBlockUser);
        unBlockUser.addActionListener(e -> {
            String blockUsername = (String) JOptionPane.showInputDialog(null, "Select which user to unblock",
                    "Unblock User", JOptionPane.QUESTION_MESSAGE, null, usernames.toArray(), null);

                writer.write("Option 4");
                writer.println();
                writer.flush();
                writer.write(blockUsername);
                writer.println();
                writer.flush();
                try {JOptionPane.showMessageDialog(null, reader.readLine(), "Unblock User", JOptionPane.INFORMATION_MESSAGE);}
                catch (IOException er) {};

                updateFeed(feedPanel);
            
        });
        JButton viewAUserProfile = new JButton("View a User Profile"); sidePanel.add(viewAUserProfile);
        viewAUserProfile.addActionListener(e -> {
            writer.println("Get Username"); writer.flush();
            String username = null;
            try {username = (String) reader.readLine();} catch (Exception er) {}
            usernames.add(username);
            String viewProfile = (String) JOptionPane.showInputDialog(null, "Select which user to view",
                    "User Profile", JOptionPane.QUESTION_MESSAGE, null, usernames.toArray(), null);
            usernames.remove(username);
                writer.write("Option 5");
                writer.println();
                writer.flush();
                writer.write(viewProfile);
                writer.println();
                writer.flush();
                
                try {
                String userProfile = "";

                ImageIcon icon = null; 
                String line = reader.readLine();
                int count = 1;
                while (line.equals("stop") == false) {
                    
                    if (count == 4) {
                        System.out.println(line);
                        line = line.substring(17);
                        if (line.equals("null") == false) {                        icon = new ImageIcon(line);}

                    } else {userProfile += line + "\n";}
                    line = reader.readLine();
                    count++;
                    
                }

                System.out.println(userProfile);
                if (icon != null){
                JOptionPane.showMessageDialog(null,
                        new JLabel(userProfile, icon, JLabel.LEFT),
                        "View Profile", JOptionPane.INFORMATION_MESSAGE);}
                        
                else {
                JOptionPane.showMessageDialog(null, userProfile, "View Profile", JOptionPane.INFORMATION_MESSAGE);}}
                catch (Exception er) {};
            
        
        });
        JButton createPost = new JButton("Create a Post"); sidePanel.add(createPost);
        createPost.addActionListener(e -> {
            writer.println("Option 7"); writer.flush();
            String caption = (String) JOptionPane.showInputDialog(null, "Enter Post Caption", "Create Post", JOptionPane.QUESTION_MESSAGE);
            String image = promptForPicture("Post Picture");
            writer.println(caption); writer.flush();
            writer.println(image); writer.flush();

            updateFeed(feedPanel);
            
            
        });
        JButton deletePost = new JButton("Delete a Post"); sidePanel.add(deletePost);
        deletePost.addActionListener(e -> {
            writer.println("Option 8"); writer.flush();
            String number = (String)JOptionPane.showInputDialog(null, "What post number would you like to delete (starting at 0)", "Delete Post", JOptionPane.QUESTION_MESSAGE);
            writer.println(number); writer.flush();

            try{
                JOptionPane.showMessageDialog(null, reader.readLine(), "Delete Post", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception er) {}

            updateFeed(feedPanel);

        });
        JButton editPost = new JButton("Edit a Post");
        sidePanel.add(editPost);
        editPost.addActionListener(e -> {
            writer.println("Option 9"); writer.flush();
            String number = (String)JOptionPane.showInputDialog(null, "What post number would you like to edit (starting at 0)", "Edit Post", JOptionPane.QUESTION_MESSAGE);
            writer.println(number); writer.flush();
            String caption = (String) JOptionPane.showInputDialog(null, "Enter New Post Caption", "Edit Post", JOptionPane.QUESTION_MESSAGE);
            writer.println(caption); writer.flush();

            try{
                JOptionPane.showMessageDialog(null, reader.readLine(), "Edit Post", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception er) {}

            updateFeed(feedPanel);
        });
        JButton createAComment = new JButton("Create a Comment");
        sidePanel.add(createAComment);
        createAComment.addActionListener(e -> {
            writer.println("Option 10"); writer.flush();
            String number = (String)JOptionPane.showInputDialog(null, "What post number would you like to comment on (starting at 0)", "Enter Comment", JOptionPane.QUESTION_MESSAGE);
            writer.println(number); writer.flush();
            String comment =(String) JOptionPane.showInputDialog(null, "Enter Comment", "New Comment", JOptionPane.QUESTION_MESSAGE);
            writer.println(comment); writer.flush();

            try{
                JOptionPane.showMessageDialog(null, reader.readLine(), "Enter Comment", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception er) {}

            updateFeed(feedPanel);
        });
        JButton deleteAComment = new JButton("Delete a Comment");
        sidePanel.add(deleteAComment);
        deleteAComment.addActionListener(e -> {
            writer.println("Option 11"); writer.flush();
            String number = (String)JOptionPane.showInputDialog(null, "What post number would you like to delete the comment on (starting at 0)", "Delete Comment", JOptionPane.QUESTION_MESSAGE);
            writer.println(number); writer.flush();
            String comment = (String) JOptionPane.showInputDialog(null, "What comment number would you like to delete (starting at 0)", "Delete Comment", JOptionPane.QUESTION_MESSAGE);
            writer.println(comment); writer.flush();

            try{
                JOptionPane.showMessageDialog(null, reader.readLine(), "Delete Comment", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception er) {}

            updateFeed(feedPanel);
        });
        JButton editComment = new JButton("Edit a Comment");
        sidePanel.add(editComment);
        editComment.addActionListener(e -> {
            writer.println("Option 12"); writer.flush();
            String number = (String)JOptionPane.showInputDialog(null, "What post number would you like to edit the comment on (starting at 0)", "Edit Comment", JOptionPane.QUESTION_MESSAGE);
            writer.println(number); writer.flush();
            String commentNum = (String) JOptionPane.showInputDialog(null, "What comment number would you like to edit (starting at 0)", "Edit Comment", JOptionPane.QUESTION_MESSAGE);
            writer.println(commentNum); writer.flush();
            String comment =(String) JOptionPane.showInputDialog(null, "Enter New Comment", "New Comment", JOptionPane.QUESTION_MESSAGE);
            writer.println(comment); writer.flush();

            try{
                JOptionPane.showMessageDialog(null, reader.readLine(), "Edit Comment", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception er) {}

            updateFeed(feedPanel);
        });



        // Add components to the main panel
        panel.add(pageLabel, BorderLayout.NORTH);  // Page title at the top
        panel.add(scrollPane, BorderLayout.CENTER);// Feed in the center
        panel.add(exitButton, BorderLayout.SOUTH);
        panel.add(sidePanel, BorderLayout.WEST);
        return panel;

    }



    // Method to update the feed after voting
    private void updateFeed(JPanel feedPanel) {
        postNums = new ArrayList<>();
        // Clear the current feed
        feedPanel.removeAll();

        writer.println("Option 6"); writer.flush();
        try {
            String line = reader.readLine();
            System.out.println("update line:" + line);
            if (line != null && !line.isEmpty()) {
                String[] nums = line.split(",");
                for (String n : nums) {
                    postNums.add(Integer.parseInt(n));
                }
            }
        } catch (Exception e) {}

        // Rebuild the feed with updated posts
        for (int postNum : postNums) {
            // Create a panel for this post
            JPanel postPanel = new JPanel();
            postPanel.setLayout(new BorderLayout());

            writer.println("Post Info"); writer.flush(); writer.println(String.valueOf(postNum)); writer.flush();
            String[] info = null;
            try {
                String s = reader.readLine(); System.out.println(s);
                info = s.split(",");} catch (Exception e) {}
            

            // Load the image and create an ImageIcon
            JLabel imageLabel = null;
            if (info[0] != null) {ImageIcon imageIcon = new ImageIcon(info[0]);  // Path to image (ensure image exists)
            imageLabel = new JLabel(imageIcon);}

            // Create a label for the caption
            JLabel captionLabel = new JLabel(info[1]);
            captionLabel.setFont(new Font("Roboto", Font.PLAIN, 16));

            // Create a label for the votes
            JLabel voteLabel = new JLabel("Upvotes: " + info[2] + " | Downvotes: " + info[3]);
            voteLabel.setFont(new Font("Roboto", Font.PLAIN, 14));

            // Create upvote and downvote buttons
            JButton upvoteButton = new JButton("Upvote");
            JButton downvoteButton = new JButton("Downvote");

            // Add action listeners to the buttons to handle votes
            upvoteButton.addActionListener(e -> {
                try {
                writer.println("Option 13"); writer.flush(); writer.println(String.valueOf(postNums.indexOf(postNum))); writer.flush();
                reader.readLine();
                updateFeed(feedPanel);} catch (Exception er) {}
            });

            downvoteButton.addActionListener(e -> {
                try {
                    writer.println("Option 14"); writer.flush(); writer.println(String.valueOf(postNums.indexOf(postNum))); writer.flush();
                    reader.readLine();
                    updateFeed(feedPanel);} catch (Exception er) {}
            });

            // Create a panel for the buttons (upvote and downvote)
            JPanel votePanel = new JPanel();
            votePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
            votePanel.add(upvoteButton);
            votePanel.add(downvoteButton);

            // Add the image, caption, votes, and vote buttons to the post panel
            postPanel.add(imageLabel, BorderLayout.NORTH);
            postPanel.add(captionLabel, BorderLayout.CENTER);
            postPanel.add(voteLabel, BorderLayout.SOUTH);
            postPanel.add(votePanel, BorderLayout.SOUTH);

            JPanel commentLabel = new JPanel();

            int i = 4;
            while (i < info.length) {
                JLabel comment = new JLabel(info[i]);
                commentLabel.add(comment);
                i++;
            }

            postPanel.add(commentLabel, BorderLayout.EAST);

            // Add the post panel to the feed
            feedPanel.add(postPanel);
        }

        // Refresh the feed
        feedPanel.revalidate();
        feedPanel.repaint();
    }




    // Method to prompt for a profile picture
    private String promptForPicture(String dialog) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle(dialog);
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Image files", "jpg", "jpeg", "png", "gif"));

        int result = fileChooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                return selectedFile.getAbsolutePath();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Error loading image: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                return null;
            }
        }
        return null;
    }
    private void sendLoginRequest(String username, String password) {
        try{

            // Send login request
            writer.println("Login");
            writer.flush();
            writer.println(username);
            writer.flush();
            writer.println(password);
            writer.flush();

            // Wait for server response
            String response = reader.readLine();
            SwingUtilities.invokeLater(() -> {
                if ("true".equals(response)) {
                    JOptionPane.showMessageDialog(null, "Login successful", "Success", JOptionPane.INFORMATION_MESSAGE);
                    cardPanel.add(createMainPage(), "MainPage");
                    cardLayout.show(cardPanel, "MainPage");
                } else {
                    JOptionPane.showMessageDialog(null, "Login failed", "Error", JOptionPane.ERROR_MESSAGE);
                }
            });

        } catch (IOException e) {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(null, "Connection error", "Error", JOptionPane.ERROR_MESSAGE);
            });
        }
    }
    private void sendSignUpRequest(String first, String last, String username, String password, String path) {
        try {
            // Send login request
            writer.println("Sign Up");
            writer.flush();
            writer.println(first);
            writer.flush();
            writer.println(last);
            writer.flush();
            writer.println(username);
            writer.flush();
            String userResponse = reader.readLine();
            SwingUtilities.invokeLater(() -> {
                if ("false".equals(userResponse)) {
                    JOptionPane.showMessageDialog(null, "Username Invalid", "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
            writer.println(password);
            writer.flush();
            String passResponse = reader.readLine();
            SwingUtilities.invokeLater(() -> {
                if ("false".equals(passResponse)) {
                    JOptionPane.showMessageDialog(null, "Password Invalid", "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(null, "Account Successfully Created", "Twitter", JOptionPane.INFORMATION_MESSAGE);
                }
            });
            writer.println(path);
            writer.flush();




        } catch (IOException e) {
            e.printStackTrace();
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(null, "Connection error", "Error", JOptionPane.ERROR_MESSAGE);
            });
        }
    }

    private void sendExitMessage() {
        try {

            // Send login request
            writer.println("Option 16"); writer.flush();
            runLoop = false;
            frame.setVisible(false);
            System.exit(0);
        } catch (Exception e) {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(null, "Connection error", "Error", JOptionPane.ERROR_MESSAGE);
            });
        }

    }



}






