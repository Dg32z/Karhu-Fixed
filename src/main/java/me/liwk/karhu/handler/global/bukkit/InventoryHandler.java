/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Sound
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.EventPriority
 *  org.bukkit.event.Listener
 *  org.bukkit.event.inventory.InventoryClickEvent
 *  org.bukkit.event.inventory.InventoryCloseEvent
 */
package me.liwk.karhu.handler.global.bukkit;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import me.liwk.karhu.Karhu;
import me.liwk.karhu.util.gui.Button;
import me.liwk.karhu.util.gui.Gui;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class InventoryHandler
implements Listener {
    @EventHandler(priority=EventPriority.HIGHEST)
    public void onInvClose(InventoryCloseEvent e) {
        Gui gui;
        Player player = (Player)e.getPlayer();
        if (e.getView().getTitle().contains("§r") && (gui = Gui.getGui(player)) != null) {
            gui.close(player);
        }
    }

    @EventHandler
    public void onInvClick(InventoryClickEvent e) {
        if (e.getClickedInventory() != null && e.getCurrentItem() != null) {
            Player player = (Player)e.getWhoClicked();
            Gui gui = Gui.getGui(player);
            if (player.getOpenInventory().getTitle().contains("§r") && gui != null) {
                e.setCancelled(true);
            }
            if (gui != null) {
                for (Button b : gui.getButtons()) {
                    if (!b.item.clone().equals((Object)e.getCurrentItem())) continue;
                    e.setCancelled(true);
                    player.playSound(player.getLocation(), Karhu.SERVER_VERSION.isNewerThan(ServerVersion.V_1_8_8) ? Sound.valueOf((String)"ENTITY_CHICKEN_EGG") : Sound.valueOf((String)"CHICKEN_EGG_POP"), 0.5f, 1.0f);
                    b.onClick(player, e.getClick());
                }
            }
        }
    }
}

