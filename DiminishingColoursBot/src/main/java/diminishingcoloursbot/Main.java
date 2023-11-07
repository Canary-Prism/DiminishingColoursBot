package diminishingcoloursbot;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

public class Main {

    private Bot bot;
    {{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}

    private String working_directory;

    private final static String[] CHANGELOG = {
        "fixed the bot not responding to /rolecolor get if the user has no colour role",
    };
    private final static String VERSION = "1.2.2";
    
    private JFrame frame = new JFrame("DiminishingColoursBot");
    private final static int ROOM = 20;
    
    private JLabel dud = new JLabel();

    private BotRoleColorHistory savehistory = null;
    
    public static void main(String args[]) {
        new Main().load();
    }
    
    public Main() {
        //here, we assign the name of the OS, according to Java, to a variable...
        String OS = (System.getProperty("os.name")).toUpperCase();
        if (OS.contains("WIN")) {
            working_directory = System.getenv("AppData");
        }
        else {
            //in either case, we would start in the user's home directory
            working_directory = System.getProperty("user.home");
            //if we are on a Mac, we are not done, we look for "Application Support"
            working_directory += "/Library/Application Support";
        }

        working_directory += "/DiminishedColoursBot";
        //we are now free to set the working_directory to the subdirectory that is our 
        //folder.
        
        //i'll keep this in case i add icons later
        /* 
        frame.setIconImage(new ImageIcon(this.getClass().getClassLoader().getResource("icon/WebToonLink.png")).getImage());


        final Taskbar tb = Taskbar.getTaskbar();
        if (System.getProperty("os.name").contains("Mac"))
            tb.setIconImage(new ImageIcon(Main.class.getClassLoader().getResource("icon/WebToonLink.png")).getImage());

        */
        folder = new File(working_directory);
        bot_file = new File(working_directory + "/Bot.json");
        color_file = new File(working_directory + "/Colors.json");
        
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        main_label.setBounds(Main.ROOM + 10, Main.ROOM + 0, 200, 100);
        main_label.setVerticalAlignment(SwingConstants.TOP);
        
        main_reload_button.setBounds(Main.ROOM + 0, Main.ROOM + 110, 200, 30);
        main_reload_button.addActionListener((e) -> bot.reloadAllSlashCommands());
        
        main_about_button.setBounds(Main.ROOM + 0, Main.ROOM + 140, 200, 30);
        main_about_button.addActionListener(this::aboutMenu);
        
        main_token_button.setBounds(Main.ROOM + 0, Main.ROOM + 170, 200, 30);
        main_token_button.addActionListener(this::tokenMenu);
        
        about_label.setBounds(Main.ROOM + 0, Main.ROOM + 0, 450, 250);
        about_label.setVerticalAlignment(SwingConstants.TOP);
        
        about_changelog_button.setBounds(Main.ROOM + 0, Main.ROOM + 200, 100, 30);
        about_changelog_button.addActionListener(this::changelogMenu);
        
        about_version_label.setBounds(Main.ROOM + 100, Main.ROOM + 200, 100, 30);
        
        about_back_button.setBounds(Main.ROOM + 350, Main.ROOM + 200, 100, 30);
        about_back_button.addActionListener(this::mainMenu);
        

        String temp = "<html><h1>Changelog</h1>" + Main.VERSION;
        for (int i = 0; i < Main.CHANGELOG.length; i++) {
            temp += "<br />•" + Main.CHANGELOG[i];
        }
        temp += "</html>";
        
        changelog_label.setText(temp);
        changelog_label.setVerticalAlignment(SwingConstants.TOP);
        
        changelog_back_button.addActionListener((e) -> {frame.setResizable(false);mainMenu(e);});
        

        
        token_field.setBounds(Main.ROOM + 0, Main.ROOM + 0, 500, 30);
        
        token_cancel_button.setBounds(Main.ROOM + 200, Main.ROOM + 30, 100, 30);
        token_cancel_button.addActionListener((e) -> {
            token_field.setText(token);
        });
        
        token_save_button.setBounds(Main.ROOM + 300, Main.ROOM + 30, 200, 30);
        token_save_button.addActionListener((e) -> {
            token = token_field.getText();
            save(token);
            start();
        });

        token_back_button.setBounds(Main.ROOM + 0, Main.ROOM + 30, 100, 30);
        token_back_button.addActionListener(this::mainMenu);

        loading_label.setBounds(Main.ROOM + 0, Main.ROOM + 0, 190, 100);
        loading_label.setVerticalAlignment(SwingConstants.TOP);

        frame.setVisible(true);
    }

    /**
     * Loads the bot
     * shows a loading screen and attempts to read the save files
     */
    private void load() {
        loadingMenu(null);

        loadSaves();

        if (token == null || token.isEmpty()) {
            tokenMenu(null);
        } else
            start();
    }

    private void start() {
        loadingMenu(null);

        //if save data is present, pass it along to the bot
        if (savehistory != null)
            bot = new Bot(token, this, savehistory);
        else
            bot = new Bot(token, this);

        bot.start();
        
        mainMenu(null);
        System.out.println("Bot Started");
    }

    private File folder, bot_file, color_file;

    private String token;

    private String buffer;

    private void loadSaves() {
        try {
            if (!folder.exists())
                folder.mkdirs(); //create the folder if it doesn't exist
            else {
                if (bot_file.exists()) {
                    try {
                        buffer = "";
                        Files.readAllLines(bot_file.toPath(), StandardCharsets.UTF_8).forEach((line) -> buffer += line);
                        JSONObject contents = new JSONObject(buffer);
                        token = contents.getString("token");
                        token_field.setText(token);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    bot_file.createNewFile();
                }
                if (color_file.exists()) {
                    try {
                        buffer = "";
                        Files.readAllLines(color_file.toPath(), StandardCharsets.UTF_8).forEach((line) -> buffer += line);
                        JSONArray contents = new JSONArray(buffer);
                        savehistory = new BotRoleColorHistory();
                        contents.forEach((e) -> {
                            var save_server = (JSONObject) e;
                            var server = new BotRoleColorHistory.ServerRoleColorHistory(save_server.getLong("server_id"));
                            for (var u : save_server.getJSONArray("users")) {
                                var user = (JSONObject) u;
                                server.fromSave(
                                    user.getLong("id"), 
                                    List.of(
                                        user.getJSONArray("colors").optString(0), 
                                        user.getJSONArray("colors").optString(1),
                                        user.getJSONArray("colors").optString(2)
                                    ).toArray(new String[0]),
                                    List.of(
                                        user.getJSONArray("imposeds").getBoolean(0),
                                        user.getJSONArray("imposeds").getBoolean(1),
                                        user.getJSONArray("imposeds").getBoolean(2)
                                    ).toArray()
                                );
                            }
                            savehistory.add(server);
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Cannot Access Save Data", "sadness", JOptionPane.ERROR_MESSAGE);
            System.exit(-1);
        }
    }

    protected void save(String token) {
        try (FileWriter fw = new FileWriter(bot_file)) {
            new JSONWriter(fw)
            .object()
                .key("token")
                .value(token)
            .endObject();

            fw.close();

        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Cannot Access Save Data", "sadness", JOptionPane.ERROR_MESSAGE);
            System.exit(-1);
        }
    }
    
    private Color[] save_colors;
    private boolean[] save_imposeds;


    protected void save(BotRoleColorHistory history) {
        try (FileWriter fw = new FileWriter(color_file)) {
            var writer = new JSONWriter(fw);

            writer
            .array();
            for (var server : history.getAllServers()) {
                writer
                .object()
                    .key("server_id")
                    .value(server.getServerId())
                    .key("users")
                    .array();

                for (var item : server.getAllUsers()) {
                    save_colors = item.getColors();
                    save_imposeds = item.getImposeds();
                    writer
                        .object()
                            .key("id")
                            .value(item.getUserId())
                            .key("colors")
                            .array();
    
                    for (int i = 0; i < 3; i++) {
                        try {
                            writer
                                .value(String.format("#%02x%02x%02x", save_colors[i].getRed(), save_colors[i].getGreen(), save_colors[i].getBlue()));
                        } catch (NullPointerException e) {
                            writer
                                .value(null);
                        }
                    }
                    
                    writer
                            .endArray()
                            .key("imposeds")
                            .array()
                                .value(save_imposeds[0])
                                .value(save_imposeds[1])
                                .value(save_imposeds[2])
                            .endArray()
                        .endObject();
                }
                writer
                    .endArray()
                .endObject();
            }
            writer
            .endArray();

            fw.close();

        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Cannot Access Save Data", "sadness", JOptionPane.ERROR_MESSAGE);
            System.exit(-1);
        }
    }
    
    private JLabel main_label = new JLabel("<html><h1>DiminishingColoursBot</h1>control panel</html>");

    private JButton main_reload_button = new JButton("Reload Slash Commands");
    private JButton main_about_button = new JButton("About");
    private JButton main_token_button = new JButton("Bot Token");
    
    private void mainMenu(ActionEvent e) {
        frame.getContentPane().removeAll();
        
        frame.getContentPane().add(main_label);

        frame.getContentPane().add(main_reload_button);
        frame.getContentPane().add(main_about_button);
        frame.getContentPane().add(main_token_button);
        
        frame.getContentPane().add(dud);
        
        frame.setSize(200 + Main.ROOM * 2, 220 + Main.ROOM * 2);
    }
    

    
    

    private JLabel about_label = new JLabel("<html><h1>About</h1>I tried balancing colour options and role limits<br>this bot will try  its best to give people rolecolors that are close as possible to their original selection without eating up all the role space</html>");
    private JButton about_changelog_button = new JButton("Changelog");
    private JLabel about_version_label = new JLabel(Main.VERSION);
    private JButton about_back_button = new JButton("Back");
    
    private void aboutMenu(ActionEvent e) {
        frame.getContentPane().removeAll();
        
        frame.getContentPane().add(about_label);
        frame.getContentPane().add(about_changelog_button);
        frame.getContentPane().add(about_version_label);
        frame.getContentPane().add(about_back_button);
        
        frame.getContentPane().add(dud);
        
        frame.setSize(450 + Main.ROOM * 2, 250 + Main.ROOM * 2);
    }
    

    private JLabel changelog_label = new JLabel();
    private JButton changelog_back_button = new JButton("back");
    
    private void changelogMenu(ActionEvent e) {
        frame.getContentPane().removeAll();
        
        frame.getContentPane().add(changelog_back_button, BorderLayout.PAGE_END);
        frame.getContentPane().add(changelog_label);
        
        frame.setResizable(true);
        frame.setSize(400 + Main.ROOM * 2, Main.CHANGELOG.length * 30 + 140 + Main.ROOM * 2);
    }
    

    private JTextField token_field = new JTextField();
    private JButton token_cancel_button = new JButton("Cancel");
    private JButton token_save_button = new JButton("Save and Reload");
    private JButton token_back_button = new JButton("Back");
    
    private void tokenMenu(ActionEvent e) {
        frame.getContentPane().removeAll();
        
        frame.getContentPane().add(token_field);
        frame.getContentPane().add(token_cancel_button);
        frame.getContentPane().add(token_save_button);
        frame.getContentPane().add(token_back_button);
        
        frame.getContentPane().add(dud);
        
        frame.setSize(500 + Main.ROOM * 2, 80 + Main.ROOM * 2);
    }
    
    private JLabel loading_label = new JLabel("<html><h1>Loading Bot...</h1>please wait</html>");
    
    private void loadingMenu(ActionEvent e) {
        frame.getContentPane().removeAll();
        
        frame.getContentPane().add(loading_label);
        
        frame.getContentPane().add(dud);
        
        frame.setSize(190 + Main.ROOM * 2, 100 + Main.ROOM * 2);
        frame.repaint();
    }
}