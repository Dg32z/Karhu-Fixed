/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.command.Command
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 */
package me.liwk.karhu.util.framework;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandArgs {
    private final CommandSender sender;
    private final Command command;
    private final String label;
    private final String[] args;

    protected CommandArgs(CommandSender sender, Command command, String label, String[] args, int subCommand) {
        String[] modArgs = new String[args.length - subCommand];
        for (int i = 0; i < args.length - subCommand; ++i) {
            modArgs[i] = args[i + subCommand];
        }
        StringBuilder buffer = new StringBuilder();
        buffer.append(label);
        for (int x = 0; x < subCommand; ++x) {
            buffer.append(".").append(args[x]);
        }
        String cmdLabel = buffer.toString();
        this.sender = sender;
        this.command = command;
        this.label = cmdLabel;
        this.args = modArgs;
    }

    public int length() {
        return this.args.length;
    }

    public String getLabel() {
        return this.label;
    }

    public Player getPlayer() {
        return this.sender instanceof Player ? (Player)this.sender : null;
    }

    public String[] getArgs() {
        return this.args;
    }

    public String getArgs(int index) {
        return this.args[index];
    }

    public Command getCommand() {
        return this.command;
    }

    public CommandSender getSender() {
        return this.sender;
    }

    public boolean isPlayer() {
        return this.sender instanceof Player;
    }
}

