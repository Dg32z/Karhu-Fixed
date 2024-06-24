/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.ChatColor
 *  org.bukkit.command.Command
 *  org.bukkit.command.CommandExecutor
 *  org.bukkit.command.CommandMap
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 *  org.bukkit.help.GenericCommandHelpTopic
 *  org.bukkit.help.HelpTopic
 *  org.bukkit.help.HelpTopicComparator
 *  org.bukkit.help.IndexHelpTopic
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.plugin.SimplePluginManager
 *  org.bukkit.plugin.java.JavaPlugin
 *  org.spigotmc.SpigotConfig
 */
package me.liwk.karhu.util.framework;

import me.liwk.karhu.Karhu;
import me.liwk.karhu.manager.alert.AlertsManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.help.GenericCommandHelpTopic;
import org.bukkit.help.HelpTopic;
import org.bukkit.help.HelpTopicComparator;
import org.bukkit.help.IndexHelpTopic;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.spigotmc.SpigotConfig;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class CommandFramework
implements CommandExecutor {
    private Map<String, Map.Entry<Method, Object>> commandMap = new HashMap<String, Map.Entry<Method, Object>>();
    private CommandMap map;
    private String newAliases;
    private JavaPlugin plugin;
    private List<File> files = new ArrayList<File>();

    public CommandFramework(JavaPlugin plugin) {
        this.plugin = plugin;
        if (plugin.getServer().getPluginManager() instanceof SimplePluginManager) {
            SimplePluginManager manager = (SimplePluginManager)plugin.getServer().getPluginManager();
            try {
                Field field = SimplePluginManager.class.getDeclaredField("commandMap");
                field.setAccessible(true);
                this.map = (CommandMap)field.get(manager);
            }
            catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException var4) {
                Karhu.getInstance().printCool("&b> &cCommandMap not found, couldn't add commands");
            }
        }
    }

    public void unregisterCommands(Object obj) {
        for (Method m : obj.getClass().getMethods()) {
            if (m.getAnnotation(Command.class) == null) continue;
            Command command = m.getAnnotation(Command.class);
            this.commandMap.remove(command.name().toLowerCase());
            this.commandMap.remove(this.plugin.getName() + ":" + command.name().toLowerCase());
            this.map.getCommand(command.name().toLowerCase()).unregister(this.map);
        }
    }

    public void registerHelp() {
        TreeSet help = new TreeSet(HelpTopicComparator.helpTopicComparatorInstance());
        this.commandMap.keySet().stream().filter(s -> !s.contains(".")).map(s -> this.map.getCommand(s)).map(cmd -> new GenericCommandHelpTopic(cmd)).forEachOrdered(topicx -> help.add(topicx));
        IndexHelpTopic topic = new IndexHelpTopic(this.plugin.getName(), "All commands for " + this.plugin.getName(), null, help, "Below is a list of all " + this.plugin.getName() + " commands:");
        Bukkit.getServer().getHelpMap().addTopic((HelpTopic)topic);
    }

    private void defaultCommand(CommandArgs args) {
        args.getSender().sendMessage("Unknown Command.");
    }

    boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                this.deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }

    public void registerCommand(Command command, String label, Method m, Object obj) {
        this.commandMap.put(label.toLowerCase(), new AbstractMap.SimpleEntry<Method, Object>(m, obj));
        this.commandMap.put(this.plugin.getName() + ':' + label.toLowerCase(), new AbstractMap.SimpleEntry<Method, Object>(m, obj));
        String cmdLabel = label.replace(".", ",").split(",")[0].toLowerCase();
        if (this.map.getCommand(cmdLabel) == null) {
            BukkitCommand cmd = new BukkitCommand(cmdLabel, this, (Plugin)this.plugin);
            this.map.register(this.plugin.getName(), (org.bukkit.command.Command)cmd);
        }
        if (!command.description().isEmpty() && cmdLabel.equals(label)) {
            this.map.getCommand(cmdLabel).setDescription(command.description());
        }
        if (!command.usage().isEmpty() && cmdLabel.equals(label)) {
            this.map.getCommand(cmdLabel).setUsage(command.usage());
        }
        CommandManager1_19.syncCommand();
    }

    public boolean onCommand(CommandSender sender, org.bukkit.command.Command cmd, String label, String[] args) {
        return this.handleCommand(sender, cmd, label, args);
    }

    public boolean handleCommand(CommandSender sender, org.bukkit.command.Command cmd, String label, String[] args) {
        for (int i = args.length; i >= 0; --i) {
            Player player;
            StringBuilder buffer = new StringBuilder();
            buffer.append(label.toLowerCase());
            for (int x = 0; x < i; ++x) {
                buffer.append(".").append(args[x].toLowerCase());
            }
            String cmdLabel = buffer.toString();
            if (!this.commandMap.containsKey(cmdLabel)) continue;
            Method method = this.commandMap.get(cmdLabel).getKey();
            Object methodObject = this.commandMap.get(cmdLabel).getValue();
            Command command = method.getAnnotation(Command.class);
            if (sender instanceof Player && !(player = (Player)sender).hasPermission("karhu.staff") && !player.isOp() && !AlertsManager.ADMINS.contains(player.getUniqueId())) {
                if (cmdLabel.equalsIgnoreCase("karhu")) {
                    sender.sendMessage(SpigotConfig.unknownCommandMessage);
                } else {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)Karhu.getInstance().getConfigManager().getNoPermission()));
                }
                return true;
            }
            try {
                method.invoke(methodObject, new CommandArgs(sender, cmd, label, args, cmdLabel.split("\\.").length - 1));
            }
            catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException exception) {
                // empty catch block
            }
            return true;
        }
        this.defaultCommand(new CommandArgs(sender, cmd, label, args, 0));
        return true;
    }

    public void registerCommands(Object obj) {
        for (Method m : obj.getClass().getMethods()) {
            if (m.getAnnotation(Command.class) == null) continue;
            Command command = m.getAnnotation(Command.class);
            if (m.getParameterTypes().length > 1 || m.getParameterTypes()[0] != CommandArgs.class) continue;
            String commandName = command.name();
            if (commandName.equalsIgnoreCase("karhu") && Karhu.getInstance().getConfigManager().getName().equalsIgnoreCase("vengeance")) {
                commandName = "vengeance";
            }
            this.registerCommand(command, commandName, m, obj);
            if (Karhu.getInstance().getConfigManager().getName().equalsIgnoreCase("karhu") || !command.name().equalsIgnoreCase("karhu")) {
                return;
            }
            if (command.aliases().length < 1) {
                this.newAliases = Karhu.getInstance().getConfigManager().getName().toLowerCase();
            }
            this.registerCommand(command, this.newAliases, m, obj);
        }
    }
}

