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
package com.dsh105.sparktrail.trail;

import com.dsh105.dshutils.util.StringUtil;
import com.dsh105.sparktrail.SparkTrailPlugin;
import com.dsh105.sparktrail.api.event.EffectPlayEvent;
import com.dsh105.sparktrail.data.DataFactory;
import com.dsh105.sparktrail.trail.type.*;
import com.dsh105.sparktrail.util.PluginHook;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.kitteh.vanish.VanishPlugin;

import java.util.Random;

public abstract class Effect {

      protected Random r = new Random();

      private EffectHolder holder;

      private LastPlayLoc lastPlayLoc;

      protected DisplayType displayType;
      protected ParticleType particleType;
      protected BukkitTask task;

      public Effect(EffectHolder effectHolder, ParticleType particleType) {
            this.holder = effectHolder;
            this.particleType = particleType;

            this.displayType = this.particleType.getDisplayType();
            if (this.displayType == null) {
                  this.displayType = DisplayType.NORMAL;
            }
      }

      public EffectHolder getHolder() {
            return this.holder;
      }

      public EffectHolder.EffectType getEffectType() {
            return this.holder.effectType;
      }

      public int getX() {
            return holder.locX;
      }

      public int getY() {
            return holder.locY;
      }

      public int getZ() {
            return holder.locZ;
      }

      public World getWorld() {
            return holder.world;
      }

      private boolean callPlayEvent() {
            EffectPlayEvent effectPlayEvent = new EffectPlayEvent(this);
            SparkTrailPlugin.getInstance().getServer().getPluginManager().callEvent(effectPlayEvent);
            return effectPlayEvent.isCancelled();
      }

      public boolean play() {
            if (this.getEffectType().equals(EffectHolder.EffectType.PLAYER)) {
                  Player p = Bukkit.getPlayerExact(this.getHolder().getDetails().playerName);
                  if (p == null) {
                        return false;
                  }

                  boolean vanished = false;
                  if (PluginHook.getVNP() != null) {
                        VanishPlugin vnp = PluginHook.getVNP();
                        vanished = vnp.getManager().isVanished(this.getHolder().getDetails().playerName);
                  }
                  if (vanished) {
                        if (this.checkForFeetDisplay(p, true)) {
                              this.playDemo(p);
                        }
                        return false;
                  }

                  if (this.checkForFeetDisplay(p, false)) {
                        return !this.callPlayEvent();
                  }
            }
            return !this.callPlayEvent();
      }

      private boolean checkForFeetDisplay(Player p, boolean defaultFlag) {
            if (this.displayType.equals(DisplayType.FEET)) {
                  if (this.lastPlayLoc == null) {
                        this.lastPlayLoc = new LastPlayLoc(p.getLocation());
                  }
                  if (!this.lastPlayLoc.isSimilar(p.getLocation())) {
                        return !this.callPlayEvent();
                  }
                  return false;
            }
            return defaultFlag;
      }

      public abstract void playDemo(Player p);

      public void stop() {
            if (this.task != null) {
                  this.task.cancel();
            }
      }

      public ParticleType getParticleType() {
            return particleType;
      }

      public String getParticleData() {
            if (this.particleType == ParticleType.BLOCKBREAK) {
                  return "ID: " + ((BlockBreak) this).idValue + ", Meta:" + ((BlockBreak) this).metaValue;
            } else if (this.particleType == ParticleType.CRITICAL) {
                  return StringUtil.capitalise(((Critical) this).criticalType.toString());

            } else if (this.particleType == ParticleType.FIREWORK) {
                  return DataFactory.serialiseFireworkEffect(((Firework) this).fireworkEffect, ",");
            } /*else if (this.particleType == ParticleType.NOTE) {
             return StringUtil.capitalise(((Note) this).noteType.toString());
             }*/ else if (this.particleType == ParticleType.POTION) {
                  return StringUtil.capitalise(((Potion) this).potionType.toString());
            } else if (this.particleType == ParticleType.SMOKE) {
                  return StringUtil.capitalise(((Smoke) this).smokeType.toString());
            } else if (this.particleType == ParticleType.SWIRL) {
                  return StringUtil.capitalise(((Swirl) this).swirlType.toString());
            } else {
                  return "";
            }
      }
}
