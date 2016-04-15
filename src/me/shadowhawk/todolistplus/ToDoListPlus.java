package me.shadowhawk.todolistplus;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

public class ToDoListPlus extends JavaPlugin {
    public PluginDescriptionFile pluginDescription;
    public FileConfiguration config;
    private boolean adminlist;
    private boolean userlists;
    private ChatColor color;
    public final Logger log = Logger.getLogger("Minecraft");
    private File admintodof;
    private FileConfiguration admintodo;
    private File usertodof;
    private FileConfiguration usertodo;

    /**
     * ToDoListPlus Constructor
     * gets the plugin description
     */
    public ToDoListPlus() {
        pluginDescription = getDescription();
    }

    /**
     * Method onEnable:
     * sends a message to the console signifying being enabled
     * attempts to call configuration and resource creation methods when the plugin is started
     */
    public void onEnable() {
        sendPluginInfo("Starting!");
        createConfig();
        createResources();
    }

    /**
     * Method onDisable:
     * sends a message to the console signifying being disabled
     */
    public void onDisable() {
        sendPluginInfo("Shutting down!");
    }

    /**
     * Method sendPluginInfo:
     * forwards a specified message to the console
     *
     * @param message the massage to be sent to the console
     */
    public void sendPluginInfo(String message) {
        log.info("[ToDoList] " + message);
    }

    /**
     * Method createConfig:
     * creates the configuration file and directory if they don't already exist
     */
    private void createConfig() {
        try {
            //define the file
            File config = new File(getDataFolder(), "config.yml");
            //create the data folder if it doesn't exist
            if (!getDataFolder().exists()) {
                getDataFolder().mkdirs();
            }
            //create the config file if it doesn't exist
            if (!config.exists()) {
                sendPluginInfo("Config.yml not found, creating!");
                saveDefaultConfig();
            } else {
                sendPluginInfo("Config.yml found, loading!");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        //call the load config method to set plugin instance variables
        loadConfig();
    }

    /**
     * Method loadConfig:
     * reads the configuration file and saves relevant entries to class variables
     */
    private void loadConfig() {
        //set plugin instance variables
	config = getConfig();
        adminlist = config.getBoolean("Admin-list");
        userlists = config.getBoolean("Personal-list");
        color = getChatColor(config.getString("Color"));
    }

    /**
     * Method createResources:
     * attempts to create relevant resource files if they don't already exist
     */
    private void createResources() {
        if (adminlist) {
            admintodof = new File(getDataFolder(), "admintodo.yml");
            if (!admintodof.exists()) {
                sendPluginInfo("Admintodo.yml not found, creating!");
                admintodof.getParentFile().mkdirs();
                saveResource("admintodo.yml", false);
            } else {
                sendPluginInfo("Admintodo.yml found, loading!");
            }
            admintodo = new YamlConfiguration();
            try {
                admintodo.load(admintodof);
            }
            catch (IOException | InvalidConfigurationException e) {
                e.printStackTrace();
            }
        } else {
            sendPluginInfo("Admin List disabled, skipping!");
        }
        if (userlists) {
            usertodof = new File(getDataFolder(), "usertodo.yml");
            if (!usertodof.exists()) {
                sendPluginInfo("Usertodo.yml not found, creating!");
                usertodof.getParentFile().mkdirs();
                saveResource("usertodo.yml", false);
            } else {
                sendPluginInfo("Usertodo.yml found, loading!");
            }
            usertodo = new YamlConfiguration();
            try {
                usertodo.load(usertodof);
            }
            catch (IOException | InvalidConfigurationException e) {
                e.printStackTrace();
            }
        } else {
            sendPluginInfo("User List disabled, skipping!");
        }
    }

    /**
     * Method getAdminConfig:
     * returns the configuration of the file containing the shared list
     * 
     * @return returns the admin file configuration
     */
    public FileConfiguration getAdminConfig() {
        return admintodo;
    }
    
    /**
     * Method getUserConfig:
     * returns the configuration of the file containing the individual lists
     * 
     * @return returns the user file configuration
     */
    public FileConfiguration getUserConfig() {
        return usertodo;
    }

    /**
     * Method onCommand:
     *
     * @param sender the source of the command sent to the plugin
     * @param cmd the command sent to the plugin
     * @param commandLabel unused command alias
     * @param args the arguments required for the command to do more than just display a list
     * @return returns true if the command completed successfully
     */
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        {
            //determines which command was entered
            //for admintodo, it allows itself to be true if the simple "todo" command is entered if the user lists are disabled
            boolean todo = cmd.getName().equalsIgnoreCase("todo") && userlists;
            boolean admintodo = adminlist && (cmd.getName().equalsIgnoreCase("admintodo") || (cmd.getName().equalsIgnoreCase("todo") && !userlists));
            //sets the permissions to variables
            String perm_admin_add = "todolist.admin.add";
            String perm_admin_remove = "todolist.admin.remove";
            String perm_admin_view = "todolist.admin.view";
            String perm_user_add = "todolist.user.add";
            String perm_user_remove = "todolist.user.remove";
            String perm_user_view = "todolist.user.view";
            //gets the sender's UUID
            String uuid = "";
            int argsToRemove = 1;
            if(sender instanceof Player){
        	uuid = ((Player)sender).getUniqueId().toString();
            }
            
            //if either command is executed, then process the arguments
            if ((admintodo || todo) && (sender instanceof Player)) {
                try {
                    //checks if the first argument is to add or remove an element from a list
                    boolean add = args[0].equalsIgnoreCase("add");
                    boolean remove = args[0].equalsIgnoreCase("remove");
                    
                    //if the player entered "add"
                    if (add) {
                        if (sender.hasPermission(perm_admin_add) && admintodo) {
                            return addToDo(removeFirstArgs(args, 1), "todo", getAdminConfig(), admintodof, sender);
                        }
                        if (sender.hasPermission(perm_user_add) && todo) {
                            return addToDo(removeFirstArgs(args, argsToRemove), uuid + ".todo", getUserConfig(), usertodof, sender);
                        }
                        //if the player has permission for neither case, returns a message to the user
                        sendNoPermsMsg(sender);
                    } 
                    //if the player entered "remove"
                    else if (remove) {
                        if (sender.hasPermission(perm_admin_remove) && admintodo) {
                            return removeToDo(Integer.parseInt(args[1]), "todo", getAdminConfig(), admintodof, sender);
                        }
                        if (sender.hasPermission(perm_user_remove) && todo) {     
                            return removeToDo(Integer.parseInt(args[1]), uuid + ".todo", getUserConfig(), usertodof, sender);
                        }
                        //if the player has permission for neither case, returns a message to the user
                        sendNoPermsMsg(sender);
                    }
                }
                catch (IndexOutOfBoundsException e) {
                    //displays the todo list if none of the arguments match given cases
                    if (sender.hasPermission(perm_admin_view) && admintodo) {
                        displayList(sender);
                    } else if (sender.hasPermission(perm_user_view) && todo) {
                        displayList(sender, uuid);
                    } else {
                	//if the player has permission for neither case, returns a message to the user
                        sender.sendMessage(ChatColor.RED + "You do not have permissions to view this list!");
                    }
                    return true;
                }
            }
            else{
        	sendPluginInfo("Calling ToDoListPlus from the console is not currently supported!");
            }
        }
        return true;
    }

    /**
     * Method getChatColor:
     * converts from a String color/format name to a ChatColor
     *
     * @param color the String to be converted
     * @return a ChatColor
     */
    public ChatColor getChatColor(String color) {
        if (color.equalsIgnoreCase("aqua")) {
            return ChatColor.AQUA;
        }
        if (color.equalsIgnoreCase("black")) {
            return ChatColor.BLACK;
        }
        if (color.equalsIgnoreCase("blue")) {
            return ChatColor.BLUE;
        }
        if (color.equalsIgnoreCase("dark_aqua")) {
            return ChatColor.DARK_AQUA;
        }
        if (color.equalsIgnoreCase("dark_blue")) {
            return ChatColor.DARK_BLUE;
        }
        if (color.equalsIgnoreCase("dark_gray") || color.equalsIgnoreCase("dark_grey")) {
            return ChatColor.DARK_GRAY;
        }
        if (color.equalsIgnoreCase("dark_green")) {
            return ChatColor.DARK_GREEN;
        }
        if (color.equalsIgnoreCase("dark_purple")) {
            return ChatColor.DARK_PURPLE;
        }
        if (color.equalsIgnoreCase("dark_red")) {
            return ChatColor.DARK_RED;
        }
        if (color.equalsIgnoreCase("gold")) {
            return ChatColor.GOLD;
        }
        if (color.equalsIgnoreCase("gray") || color.equalsIgnoreCase("grey")) {
            return ChatColor.GRAY;
        }
        if (color.equalsIgnoreCase("green")) {
            return ChatColor.GREEN;
        }
        if (color.equalsIgnoreCase("light_purple")) {
            return ChatColor.LIGHT_PURPLE;
        }
        if (color.equalsIgnoreCase("magic")) {
            return ChatColor.MAGIC;
        }
        if (color.equalsIgnoreCase("italic")) {
            return ChatColor.ITALIC;
        }
        if (color.equalsIgnoreCase("underline")) {
            return ChatColor.UNDERLINE;
        }
        if (color.equalsIgnoreCase("bold")) {
            return ChatColor.BOLD;
        }
        if (color.equalsIgnoreCase("strike")) {
            return ChatColor.STRIKETHROUGH;
        }
        if (color.equalsIgnoreCase("red")) {
            return ChatColor.RED;
        }
        if (color.equalsIgnoreCase("white")) {
            return ChatColor.WHITE;
        }
        if (color.equalsIgnoreCase("yellow")) {
            return ChatColor.YELLOW;
        }
        if (color.equalsIgnoreCase("null")||color.equalsIgnoreCase("reset")) {
            return ChatColor.RESET;
        }
        return ChatColor.WHITE;
    }

    /**
     * Method displayList:
     * displays the shared list stored in the admin to-do file
     *
     * @param sender the person to whom the list is shown
     */
    private void displayList(CommandSender sender) {
        //makes a List of type String from the shared list entry "todo"
	List<String> s_list = getAdminConfig().getStringList("todo");
        sender.sendMessage(ChatColor.GREEN + "To-Do List:");
        for (int c = 1; c <= s_list.size(); c++) {
            sender.sendMessage(ChatColor.DARK_GRAY + String.valueOf(c) + ". " + color + "    - " + (String)s_list.get(c - 1));
        }
    }

    /**
     * Method displayList:
     * displays a specified list from the user list file based on uuid
     *
     * @param sender the person to whom the list is shown
     * @param uuid the uuid entry to search for in the file
     */
    private void displayList(CommandSender sender, String uuid) {
        //makes a List of Type String from the "todo" entry under the entry matching the UUID input in the user file
	List<String> s_list = getUserConfig().getStringList(String.valueOf(uuid) + ".todo");
        sender.sendMessage(ChatColor.GREEN + "To-Do List:");
        for (int c = 1; c <= s_list.size(); c++) {
            sender.sendMessage(ChatColor.DARK_GRAY + String.valueOf(c) + ". " + color + "    - " + (String)s_list.get(c - 1));
        }
    }

    /**
     * Method sendNoPermsMsg:
     * uses the sendWrongCmdMsg method to inform the sender that they have insufficient
     * permissions to complete the command as entered
     *
     * @param sender the person to whom the no permissions message is sent
     */
    public void sendNoPermsMsg(CommandSender sender) {
        sendWrongCmdMsg(sender,"You don't have permission to do this!");
    }

    /**
     * Method sendWrongCmdMsg:
     * tells the sender a specified command
     *
     * @param sender The person to whom the error message is sent
     * @param msg the message to be sent
     */
    public void sendWrongCmdMsg(CommandSender sender, String msg) {
        sender.sendMessage(ChatColor.RED + msg);
    }

    /**
     * Method removeFirstArgs:
     * removes the first element from an array of type String for a specified number of iterations
     *
     * @param args the string of arguments to remove the first element of
     * @param iterations how many elements should be removed from the beginning of the list
     * @return an array of type String, containing all but the first element of the input String array
     */
    public String[] removeFirstArgs(String[] args, int iterations)
    {
        if(iterations == 0)
        {
            return args;    
        }
        else
        {
            String[] newArgs = new String[args.length-1];
            for(int i = 1; i < args.length; i++)
            {
                newArgs[i-1] = args[i];
            }
            return removeFirstArgs(newArgs, iterations - 1);
        }
    }

    /**
     * Method addToDo:
     * adds an entry to a specified to-do list
     *
     * @param args contains the message to append to the end of the list
     * @param filePath the path within the resource file at which the list containing the entry is found
     * @param config the configuration of the file which can be edited then saved to a file
     * @param file the file from which the configuration is read and to which it will be saved
     * @param sender allows this method to send a message on completion of removal
     * @return returns true when completed
     */
    public boolean addToDo(String args[], String filePath, FileConfiguration config, File file, CommandSender sender)
    {
        String msg = "";
        int c = 0;
        while (c < args.length) {
            String c_arg = args[c];
            msg = msg.concat(String.valueOf(String.valueOf(c_arg)) + " ");
            ++c;
        }
        msg = msg.trim();
        List<String> t_list = config.getStringList(filePath);
        t_list.add(t_list.size(), msg.trim());
        config.set(filePath, (Object)t_list);
        sender.sendMessage(ChatColor.GOLD + "Added \"" + color + msg + ChatColor.GOLD + "\" to the list");
        try {
            config.save(file);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * Method removeToDo:
     * Removes an entry from a specified to-do list
     *
     * @param index the index of the entry to remove
     * @param filePath the path within the resource file at which the list containing the entry is found
     * @param config the configuration of the file which can be edited then saved to a file
     * @param file the file from which the configuration is read and to which it will be saved
     * @param sender allows this method to send a message on completion of removal
     * @return returns true when completed
     */
    public boolean removeToDo(int index, String filePath, FileConfiguration config, File file, CommandSender sender)
    {
        try {
            List<String> t_list = config.getStringList(filePath);
            try {
                //int indx = Integer.parseInt(index) - 1;
                String task = (String)t_list.get(index);
                t_list.remove(index);
                config.set(filePath, (Object)t_list);
                sender.sendMessage(ChatColor.GOLD + "Removed the task \"" + color + task + ChatColor.GOLD + "\" from the todo list");
                try {
                    config.save(file);
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
            }
            catch (NumberFormatException e) {
                sendWrongCmdMsg(sender, "Invalid number \"" + index + "\"");
            }
        }
        catch (IndexOutOfBoundsException e) {
            sendWrongCmdMsg(sender, "/todo remove <number>");
        }
        return true;
    }
}