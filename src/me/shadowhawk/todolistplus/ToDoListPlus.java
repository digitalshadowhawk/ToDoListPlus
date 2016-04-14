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

public class ToDoListPlus
extends JavaPlugin {
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

    public ToDoListPlus() {
        this.pluginDescription = getDescription();
    }

    public void onEnable() {
        this.createConfig();
        this.loadConfig();
        this.createResources();
    }

    public void onDisable() {
    }

    public void sendPluginInfo(String message) {
        this.log.info("[ToDoList] " + message);
    }

    private void createConfig() {
        try {
            File config = new File(getDataFolder(), "config.yml");
            if (!this.getDataFolder().exists()) {
                this.getDataFolder().mkdirs();
            }
            if (!config.exists()) {
                this.sendPluginInfo("Config.yml not found, creating!");
                this.saveDefaultConfig();
            } else {
                this.sendPluginInfo("Config.yml found, loading!");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadConfig() {
        this.config = this.getConfig();
        this.adminlist = this.config.getBoolean("Admin-list");
        this.userlists = this.config.getBoolean("Personal-list");
        this.color = this.getChatColor(this.config.getString("Color"));
    }

    private void createResources() {
        if (this.adminlist) {
            this.admintodof = new File(this.getDataFolder(), "admintodo.yml");
            if (!this.admintodof.exists()) {
                this.sendPluginInfo("Admintodo.yml not found, creating!");
                this.admintodof.getParentFile().mkdirs();
                this.saveResource("admintodo.yml", false);
            } else {
                this.sendPluginInfo("Admintodo.yml found, loading!");
            }
            this.admintodo = new YamlConfiguration();
            try {
                this.admintodo.load(this.admintodof);
            }
            catch (IOException | InvalidConfigurationException e) {
                e.printStackTrace();
            }
        } else {
            this.sendPluginInfo("Admin List disabled, skipping!");
        }
        if (this.userlists) {
            this.usertodof = new File(this.getDataFolder(), "usertodo.yml");
            if (!this.usertodof.exists()) {
                this.sendPluginInfo("Usertodo.yml not found, creating!");
                this.usertodof.getParentFile().mkdirs();
                this.saveResource("usertodo.yml", false);
            } else {
                this.sendPluginInfo("Usertodo.yml found, loading!");
            }
            this.usertodo = new YamlConfiguration();
            try {
                this.usertodo.load(this.usertodof);
            }
            catch (IOException | InvalidConfigurationException e) {
                e.printStackTrace();
            }
        } else {
            this.sendPluginInfo("User List disabled, skipping!");
        }
    }

    public FileConfiguration getAdminConfig() {
        return this.admintodo;
    }

    public FileConfiguration getUserConfig() {
        return this.usertodo;
    }

    /*
     * Enabled aggressive exception aggregation
     */
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        block29 : {
            boolean todo = cmd.getName().equalsIgnoreCase("todo") && this.userlists;
            boolean admintodo = this.adminlist && (cmd.getName().equalsIgnoreCase("admintodo") || cmd.getName().equalsIgnoreCase("todo") && !this.userlists);
            String perm_admin_add = "todolist.admin.add";
            String perm_admin_remove = "todolist.admin.remove";
            String perm_admin_view = "todolist.admin.view";
            String perm_user_add = "todolist.user.add";
            String perm_user_remove = "todolist.user.remove";
            String perm_user_view = "todolist.user.view";
            String uuid = ((Player)sender).getUniqueId().toString();
            if (admintodo || todo) {
                try {
                    boolean add = args[0].equalsIgnoreCase("add");
                    boolean remove = args[0].equalsIgnoreCase("remove");
                    if (add) {
                        if (sender.hasPermission(perm_admin_add) && admintodo) {
                            String msg = "";
                            int c = 1;
                            while (c < args.length) {
                                String c_arg = args[c];
                                msg = msg.concat(String.valueOf(String.valueOf(c_arg)) + " ");
                                ++c;
                            }
                            msg = msg.trim();
                            List<String> t_list = this.getAdminConfig().getStringList("todo");
                            t_list.add(t_list.size(), msg.trim());
                            this.getAdminConfig().set("todo", (Object)t_list);
                            sender.sendMessage(ChatColor.GOLD + "Added \"" + (Object)this.color + msg + ChatColor.GOLD + "\" to the list");
                            try {
                                this.admintodo.save(this.admintodof);
                            }
                            catch (IOException e) {
                                e.printStackTrace();
                            }
                            return true;
                        }
                        if (sender.hasPermission(perm_user_add) && todo) {
                            String msg = "";
                            int c = 1;
                            while (c < args.length) {
                                String c_arg = args[c];
                                msg = msg.concat(String.valueOf(String.valueOf(c_arg)) + " ");
                                ++c;
                            }
                            msg = msg.trim();
                            List<String> t_list = this.getUserConfig().getStringList(String.valueOf(uuid) + ".todo");
                            t_list.add(t_list.size(), msg.trim());
                            this.getUserConfig().set(String.valueOf(uuid) + ".todo", (Object)t_list);
                            sender.sendMessage(ChatColor.GOLD + "Added \"" + (Object)this.color + msg + ChatColor.GOLD + "\" to the list");
                            try {
                                this.usertodo.save(this.usertodof);
                            }
                            catch (IOException e) {
                                e.printStackTrace();
                            }
                            return true;
                        }
                        this.sendNoPermsMsg(sender);
                        break block29;
                    }
                    if (!remove) break block29;
                    if (sender.hasPermission(perm_admin_remove) && admintodo) {
                        try {
                            String index = args[1];
                            List<String> t_list = this.getAdminConfig().getStringList("todo");
                            try {
                                int indx = Integer.parseInt(index) - 1;
                                String task = (String)t_list.get(indx);
                                t_list.remove(indx);
                                this.getAdminConfig().set("todo", (Object)t_list);
                                sender.sendMessage(ChatColor.GOLD + "Removed the task \"" + (Object)this.color + task + ChatColor.GOLD + "\" from the todo list");
                                try {
                                    this.admintodo.save(this.admintodof);
                                }
                                catch (IOException e) {
                                    e.printStackTrace();
                                }
                                return true;
                            }
                            catch (NumberFormatException e) {
                                this.sendWrongCmdMsg(sender, "Invalid number \"" + index + "\"");
                            }
                        }
                        catch (IndexOutOfBoundsException e) {
                            this.sendWrongCmdMsg(sender, "/todo check <number>");
                        }
                        return true;
                    }
                    if (!sender.hasPermission(perm_user_remove) || !todo) break block29;
                    try {
                        String index = args[1];
                        List<String> t_list = this.getUserConfig().getStringList(String.valueOf(uuid) + ".todo");
                        try {
                            int indx = Integer.parseInt(index) - 1;
                            String task = (String)t_list.get(indx);
                            t_list.remove(indx);
                            this.getUserConfig().set(String.valueOf(uuid) + ".todo", (Object)t_list);
                            sender.sendMessage(ChatColor.GOLD + "Removed the task \"" + (Object)this.color + task + ChatColor.GOLD + "\" from the todo list");
                            try {
                                this.usertodo.save(this.usertodof);
                            }
                            catch (IOException e) {
                                e.printStackTrace();
                            }
                            return true;
                        }
                        catch (NumberFormatException e) {
                            this.sendWrongCmdMsg(sender, "Invalid number \"" + index + "\"");
                        }
                    }
                    catch (IndexOutOfBoundsException e) {
                        this.sendWrongCmdMsg(sender, "/todo check <number>");
                    }
                    return true;
                }
                catch (IndexOutOfBoundsException e) {
                    if (sender.hasPermission(perm_admin_view) && admintodo) {
                        this.displayList(sender);
                    } else if (sender.hasPermission(perm_user_view) && todo) {
                        this.displayList(sender, uuid);
                    } else {
                        sender.sendMessage(ChatColor.RED + "You do not have permissions to view the ToDo list!");
                    }
                    return true;
                }
            }
        }
        return true;
    }

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
        if (color.equalsIgnoreCase("red")) {
            return ChatColor.RED;
        }
        if (color.equalsIgnoreCase("white")) {
            return ChatColor.WHITE;
        }
        if (color.equalsIgnoreCase("yellow")) {
            return ChatColor.YELLOW;
        }
        if (color.equalsIgnoreCase("null")) {
            return ChatColor.WHITE;
        }
        return ChatColor.WHITE;
    }

    private void displayList(CommandSender sender) {
        List<String> s_list = this.admintodo.getStringList("todo");
        sender.sendMessage(ChatColor.GREEN + "To-Do List:");
        int c = 1;
        while (c <= s_list.size()) {
            sender.sendMessage(ChatColor.DARK_GRAY + String.valueOf(c) + ". " + (Object)this.color + "    - " + (String)s_list.get(c - 1));
            ++c;
        }
    }

    private void displayList(CommandSender sender, String uuid) {
        List<String> s_list = this.usertodo.getStringList(String.valueOf(uuid) + ".todo");
        sender.sendMessage(ChatColor.GREEN + "To-Do List:");
        int c = 1;
        while (c <= s_list.size()) {
            sender.sendMessage(ChatColor.DARK_GRAY + String.valueOf(c) + ". " + (Object)this.color + "    - " + (String)s_list.get(c - 1));
            ++c;
        }
    }

    public void sendNoPermsMsg(CommandSender sender) {
        sender.sendMessage(ChatColor.RED + "You don't have permission to do this!");
    }

    public void sendWrongCmdMsg(CommandSender sender, String msg) {
        sender.sendMessage(ChatColor.RED + msg);
    }
}