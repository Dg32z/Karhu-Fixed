package me.liwk.karhu.util.framework;

import org.bukkit.Bukkit;
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
   private final Map<String, org.bukkit.command.Command> registered;

   public CommandManager1_19(JavaPlugin plugin) {
      super();
      if (plugin == null) {}

      this.registered = new HashMap<>();
      this.plugin = plugin;
   }

   static {
      try {
         Method commandMapMethod = Bukkit.getServer().getClass().getMethod("getCommandMap");
         bukkitCommandMap = (CommandMap)commandMapMethod.invoke(Bukkit.getServer());
         knownCommandsField = SimpleCommandMap.class.getDeclaredField("knownCommands");
         knownCommandsField.setAccessible(true);
      } catch (ReflectiveOperationException var6) {
         throw new ExceptionInInitializerError(var6);
      }

      try {
         Class<?> craftServer = Bukkit.getServer().getClass();
         syncCommandsMethod = craftServer.getDeclaredMethod("syncCommands");
      } catch (Exception var5) {
      } finally {
         if (syncCommandsMethod != null) {
            syncCommandsMethod.setAccessible(true);
         }
      }
   }

   public final void register(org.bukkit.command.Command command) {
      if (command == null) {}

      String name = command.getLabel();
      if (this.registered.containsKey(name)) {
         this.plugin.getLogger().log(Level.WARNING, "Duplicated \"{0}\" command ! Ignored", name);
      } else {
         registerCommandToCommandMap(this.plugin.getName(), command);
         this.registered.put(name, command);
      }
   }

   public final void unregister(org.bukkit.command.Command command) {
      if (command == null) {}

      try {
         unregisterFromKnownCommands(command);
         this.registered.remove(command.getLabel());
      } catch (ReflectiveOperationException var3) {
         this.plugin.getLogger().log(Level.WARNING, "Something wrong when unregister the command", (Throwable)var3);
      }
   }

   public final void unregister(String command) {
      if (command == null) {}

      if (this.registered.containsKey(command)) {
         this.unregister((org.bukkit.command.Command)this.registered.remove(command));
      }
   }

   public static void syncCommand() {
      if (syncCommandsMethod != null) {
         try {
            syncCommandsMethod.invoke(Bukkit.getServer());
         } catch (InvocationTargetException | IllegalAccessException var1) {
            Bukkit.getLogger().log(Level.WARNING, "Error when syncing commands", (Throwable)var1);
         }
      }
   }

   public final Map<String, org.bukkit.command.Command> getRegistered() {
      Map var10000 = Collections.unmodifiableMap(this.registered);
      if (var10000 == null) {}

      return var10000;
   }

   public final void unregisterAll() {
      this.registered.values().forEach(command -> {
         try {
            unregisterFromKnownCommands(command);
         } catch (ReflectiveOperationException var3) {
            this.plugin.getLogger().log(Level.WARNING, "Something wrong when unregister the command", (Throwable)var3);
         }
      });
      this.registered.clear();
   }

   public static void unregisterFromKnownCommands(org.bukkit.command.Command command) throws IllegalAccessException {
      if (command == null) {}

      Map<?, ?> knownCommands = (Map)knownCommandsField.get(bukkitCommandMap);
      knownCommands.values().removeIf(command::equals);
      command.unregister(bukkitCommandMap);
   }

   public static void registerCommandToCommandMap(String label, org.bukkit.command.Command command) {
      if (label == null) {
      }

      if (command == null) {
      }

      bukkitCommandMap.register(label, command);
   }
}
