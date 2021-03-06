/*
 * This file is part of SparkTrail 3.
 *
 * SparkTrail 3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SparkTrail 3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SparkTrail 3.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.dsh105.sparktrail.menu;

import com.dsh105.sparktrail.SparkTrailPlugin;
import com.dsh105.sparktrail.api.event.MenuOpenEvent;
import com.dsh105.sparktrail.conversation.InputFactory;
import com.dsh105.sparktrail.conversation.TimeoutFunction;
import com.dsh105.sparktrail.data.EffectManager;
import com.dsh105.sparktrail.trail.Effect;
import com.dsh105.sparktrail.trail.EffectHolder;
import com.dsh105.sparktrail.trail.ParticleType;
import com.dsh105.sparktrail.util.Lang;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.UUID;

public class ParticleMenu extends Menu {

      public static HashMap<String, ParticleMenu> openMenus = new HashMap<String, ParticleMenu>();
      protected MenuIcon[] endItems;

      Inventory inv;
      private int size;
      public EffectHolder.EffectType effectType;

      public ParticleMenu(Player viewer, String playerName) {
            this(viewer, EffectHolder.EffectType.PLAYER, "Trail GUI");
            this.playerName = playerName;
            Player p = Bukkit.getPlayerExact(playerName);
            setItems();
      }

      private ParticleMenu(Player viewer, EffectHolder.EffectType effectType, String name) {
            this.size = 45;
            this.effectType = effectType;
            this.viewer = viewer.getName();
            this.inv = Bukkit.createInventory(viewer, size, name);

            endItems = new MenuIcon[]{
                  new MenuIcon(this, Material.BOOK, (short) 0, ChatColor.GOLD + "Close"),
                  new MenuIcon(this, Material.WATCH, (short) 0, ChatColor.GOLD + "Timeout") {
                        @Override
                        public void onClick() {
                              if (this.getViewer() != null) {
                                    //Lang.sendTo(this.getViewer(), Lang.ENTER_TIMEOUT.toString());
                                    InputFactory.promptInt(this.getViewer(), new TimeoutFunction(this.getViewer()));
                                    //MenuChatListener.AWAITING_TIMEOUT_INPUT.add(this.getViewer().getName());
                              }
                        }
                  },
                  new MenuIcon(this, Material.WOOL, (short) 5, ChatColor.GOLD + "Start") {
                        @Override
                        public void onClick() {
                              if (this.getViewer() != null) {
                                    EffectHolder eh = EffectManager.getInstance().createFromFile(this.getViewer().getName());
                                    if (eh == null || eh.getEffects().isEmpty()) {
                                          Lang.sendTo(this.getViewer(), Lang.NO_EFFECTS_TO_LOAD.toString());
                                          EffectManager.getInstance().clear(eh);
                                          return;
                                    }
                                    Lang.sendTo(this.getViewer(), Lang.EFFECTS_LOADED.toString());
                              }
                        }
                  },
                  new MenuIcon(this, Material.WOOL, (short) 14, ChatColor.GOLD + "Stop") {
                        @Override
                        public void onClick() {
                              EffectHolder eh = EffectManager.getInstance().getEffect(this.getViewer().getName());
                              if (eh == null) {
                                    Lang.sendTo(this.getViewer(), Lang.NO_ACTIVE_EFFECTS.toString());
                                    return;
                              }
                              EffectManager.getInstance().remove(eh.getDetails().playerName, eh);
                              Lang.sendTo(this.getViewer(), Lang.EFFECTS_STOPPED.toString());
                        }
                  },
                  new MenuIcon(this, Material.WOOL, (short) 0, ChatColor.GOLD + "Clear") {
                        @Override
                        public void onClick() {
                              EffectHolder eh = EffectManager.getInstance().getEffect(this.getViewer().getName());
                              if (eh == null) {
                                    Lang.sendTo(this.getViewer(), Lang.NO_ACTIVE_EFFECTS.toString());
                                    return;
                              }
                              EffectManager.getInstance().clear(eh);
                              Lang.sendTo(this.getViewer(), Lang.EFFECTS_CLEARED.toString());
                        }
                  }};
      }

      public void setItems() {
            EffectHolder eh = null;
            if (effectType == EffectHolder.EffectType.PLAYER) {
                  eh = EffectManager.getInstance().getEffect(this.playerName);
            }

            int i = 0;
            for (ParticleType pt : ParticleType.values()) {
                  if (pt == ParticleType.SOUND) {
                        continue;
                  }
                  if (pt.requiresDataMenu() && pt != ParticleType.BLOCKBREAK && pt != ParticleType.FIREWORK) {
                        inv.setItem(i++, pt.getMenuItem());
                  } else {
                        boolean hasEffect = false;
                        if (eh != null && !(eh.getEffects() == null || eh.getEffects().isEmpty())) {
                              for (Effect e : eh.getEffects()) {
                                    if (e != null) {
                                          if (e.getParticleType() == pt) {
                                                hasEffect = true;
                                          }
                                    }
                              }
                        }
                        inv.setItem(i++, pt.getMenuItem(!hasEffect));
                  }
            }

            int index = 0;
            for (int slot = size - 1; index < endItems.length; slot--) {
                  inv.setItem(slot, endItems[index++].getStack());
            }
      }

      public void open(boolean sendMessage) {
            if (this.getViewer() == null) {
                  return;
            }
            MenuOpenEvent menuEvent = new MenuOpenEvent(this.getViewer(), MenuOpenEvent.MenuType.MAIN);
            SparkTrailPlugin.getInstance().getServer().getPluginManager().callEvent(menuEvent);
            if (menuEvent.isCancelled()) {
                  return;
            }
            this.getViewer().openInventory(this.inv);
            if (sendMessage) {
                  Lang.sendTo(this.getViewer(), Lang.OPEN_MENU.toString());
            }
            openMenus.put(this.viewer, this);
      }
}
