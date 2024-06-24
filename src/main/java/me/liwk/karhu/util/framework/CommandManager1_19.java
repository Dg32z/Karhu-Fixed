/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.command.Command
 *  org.bukkit.command.CommandMap
 *  org.bukkit.command.SimpleCommandMap
 *  org.bukkit.plugin.java.JavaPlugin
 */
package me.liwk.karhu.util.framework;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class CommandManager1_19 {
    private static final Field knownCommandsField;
    private static final CommandMap bukkitCommandMap;
    private static Method syncCommandsMethod;
    protected final JavaPlugin plugin;
    private final Map<String, Command> registered;

    public CommandManager1_19(JavaPlugin plugin) {
        if (plugin == null) {
            // empty if block
        }
        this.registered = new HashMap<String, Command>();
        this.plugin = plugin;
    }

    public final void register(Command command) {
        String name;
        if (command == null) {
            // empty if block
        }
        if (this.registered.containsKey(name = command.getLabel())) {
            this.plugin.getLogger().log(Level.WARNING, "Duplicated \"{0}\" command ! Ignored", name);
        } else {
            CommandManager1_19.registerCommandToCommandMap(this.plugin.getName(), command);
            this.registered.put(name, command);
        }
    }

    public final void unregister(Command command) {
        if (command == null) {
            // empty if block
        }
        try {
            CommandManager1_19.unregisterFromKnownCommands(command);
            this.registered.remove(command.getLabel());
        }
        catch (ReflectiveOperationException var3) {
            this.plugin.getLogger().log(Level.WARNING, "Something wrong when unregister the command", var3);
        }
    }

    public final void unregister(String command) {
        if (command == null) {
            // empty if block
        }
        if (this.registered.containsKey(command)) {
            this.unregister(this.registered.remove(command));
        }
    }

    public static void syncCommand() {
        if (syncCommandsMethod != null) {
            try {
                syncCommandsMethod.invoke(Bukkit.getServer(), new Object[0]);
            }
            catch (IllegalAccessException | InvocationTargetException var1) {
                Bukkit.getLogger().log(Level.WARNING, "Error when syncing commands", var1);
            }
        }
    }

    public final Map<String, Command> getRegistered() {
        Map<String, Command> var10000 = Collections.unmodifiableMap(this.registered);
        if (var10000 == null) {
            // empty if block
        }
        return var10000;
    }

    public final void unregisterAll() {
        this.registered.values().forEach(command -> {
            try {
                CommandManager1_19.unregisterFromKnownCommands(command);
            }
            catch (ReflectiveOperationException var3) {
                this.plugin.getLogger().log(Level.WARNING, "Something wrong when unregister the command", var3);
            }
        });
        this.registered.clear();
    }

    public static void unregisterFromKnownCommands(Command command) throws IllegalAccessException {
        if (command == null) {
            // empty if block
        }
        Map knownCommands = (Map)knownCommandsField.get(bukkitCommandMap);
        knownCommands.values().removeIf(command::equals);
        command.unregister(bukkitCommandMap);
    }

    public static void registerCommandToCommandMap(String label, Command command) {
        if (label == null) {
            // empty if block
        }
        if (command == null) {
            // empty if block
        }
        bukkitCommandMap.register(label, command);
    }

    static {
        try {
            Method commandMapMethod = Bukkit.getServer().getClass().getMethod("getCommandMap", new Class[0]);
            bukkitCommandMap = (CommandMap)commandMapMethod.invoke(Bukkit.getServer(), new Object[0]);
            knownCommandsField = SimpleCommandMap.class.getDeclaredField("knownCommands");
            knownCommandsField.setAccessible(true);
        }
        catch (ReflectiveOperationException var6) {
            throw new ExceptionInInitializerError(var6);
        }
        try {
            Class<?> craftServer = Bukkit.getServer().getClass();
            syncCommandsMethod = craftServer.getDeclaredMethod("syncCommands", new Class[0]);
        }
        catch (Exception exception) {
        }
        finally {
            if (syncCommandsMethod != null) {
                syncCommandsMethod.setAccessible(true);
            }
        }
    }
}

