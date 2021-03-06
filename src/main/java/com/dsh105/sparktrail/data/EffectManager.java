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
package com.dsh105.sparktrail.data;

import com.dsh105.dshutils.config.YAMLConfig;
import com.dsh105.dshutils.logger.Logger;
import com.dsh105.dshutils.util.EnumUtil;
import com.dsh105.sparktrail.SparkTrailPlugin;
import com.dsh105.sparktrail.config.ConfigOptions;
import com.dsh105.sparktrail.mysql.SQLEffectManager;
import com.dsh105.sparktrail.trail.Effect;
import com.dsh105.sparktrail.trail.EffectHolder;
import com.dsh105.sparktrail.trail.ParticleDetails;
import com.dsh105.sparktrail.trail.ParticleType;
import com.dsh105.sparktrail.trail.type.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class EffectManager {

      private static EffectManager instance;
      private HashSet<EffectHolder> effects = new HashSet<EffectHolder>();

      public EffectManager() {
            instance = this;
      }

      public static EffectManager getInstance() {
            return instance;
      }

      public void addHolder(EffectHolder effectHolder) {
            this.effects.add(effectHolder);
      }

      public void clearFromMemory(EffectHolder holder) {
            if (holder != null) {
                  holder.stop();
                  this.effects.remove(holder);
            }
      }

      public void clearEffects() {
            Iterator<EffectHolder> i = effects.iterator();
            while (i.hasNext()) {
                  EffectHolder e = i.next();
                  save(e);
                  SQLEffectManager.instance.updateAsync(e.getDetails().playerName, e);
                  e.stop();
                  i.remove();
            }
      }

      public HashSet<EffectHolder> getEffectHolders() {
            return this.effects;
      }

      public void save(EffectHolder e) {
            if (e == null || ConfigOptions.instance.useSql()) {
                  return;
            }

            if (e.isPersistent()) {
                  clearFromFile(e);
                  if (e.getEffects().isEmpty()) {
                        return;
                  }
                  YAMLConfig config = SparkTrailPlugin.getInstance().getConfig(SparkTrailPlugin.ConfigType.DATA);
                  String path = "effects.";
                  if (e.getEffectType() == EffectHolder.EffectType.PLAYER) {
                        path = path + "player." + e.getDetails().playerName + ".";
                  }

                  for (Effect effect : e.getEffects()) {
                        if (effect == null || effect.getParticleType() == null) {
                              continue;
                        }
                        if (effect.getParticleType().requiresDataMenu()) {
                              ParticleType pt = effect.getParticleType();
                              String value = null;
                              if (pt == ParticleType.BLOCKBREAK) {
                                    value = ((BlockBreak) effect).idValue + "," + ((BlockBreak) effect).metaValue;
                              } else if (pt == ParticleType.CRITICAL) {
                                    value = ((Critical) effect).criticalType.toString();
                              } else if (pt == ParticleType.FIREWORK) {
                                    value = DataFactory.serialiseFireworkEffect(((Firework) effect).fireworkEffect, ",");
                              } /*else if (pt == ParticleType.NOTE) {
                               value = ((Note) effect).noteType.toString();
                               }*/ else if (pt == ParticleType.POTION) {
                                    value = ((Potion) effect).potionType.toString();
                              } else if (pt == ParticleType.SMOKE) {
                                    value = ((Smoke) effect).smokeType.toString();
                              } else if (pt == ParticleType.SWIRL) {
                                    value = ((Swirl) effect).swirlType.toString();
                              }
                              if (value != null) {
                                    config.set(path + effect.getParticleType().toString().toLowerCase(), value);
                              }
                        } else {
                              config.set(path + effect.getParticleType().toString().toLowerCase(), "none");
                        }
                  }

                  config.saveConfig();
            }
      }

      public void clearFromFile(EffectHolder e) {
            if (e != null) {
                  YAMLConfig config = SparkTrailPlugin.getInstance().getConfig(SparkTrailPlugin.ConfigType.DATA);
                  String path = "effects.";
                  if (e.getEffectType() == EffectHolder.EffectType.PLAYER) {
                        path = path + "player." + e.getDetails().playerName;
                  }
                  config.set(path, null);
                  config.saveConfig();
            }
      }

      public void clear(EffectHolder e) {
            clearFromFile(e);
            clearFromMemory(e);
      }

      public EffectHolder createFromFile(String playerName) {
            //Player p = Bukkit.getPlayerExact(playerName);
        /*if (p == null) {
             return null;
             }*/
            String path = "effects.player." + playerName;
            YAMLConfig config = SparkTrailPlugin.getInstance().getConfig(SparkTrailPlugin.ConfigType.DATA);
            if (config.get(path) == null) {
                  return null;
            }
            EffectHolder eh = EffectCreator.createPlayerHolder(playerName);
            return createFromFile(path, eh);
      }

      private EffectHolder createFromFile(String path, EffectHolder eh) {
            YAMLConfig config = SparkTrailPlugin.getInstance().getConfig(SparkTrailPlugin.ConfigType.DATA);
            if (config.get(path) == null) {
                  return null;
            }
            ConfigurationSection cs = config.getConfigurationSection(path);
            for (String key : cs.getKeys(false)) {
                  if (EnumUtil.isEnumType(ParticleType.class, key.toUpperCase())) {
                        ParticleType pt = ParticleType.valueOf(key.toUpperCase());
                        if (pt.requiresDataMenu()) {
                              ParticleDetails pd = new ParticleDetails(pt);
                              String value = config.getString(path + "." + key);
                              if (pt == ParticleType.BLOCKBREAK) {
                                    try {
                                          pd.blockId = Integer.parseInt(value.split(",")[0]);
                                          pd.blockMeta = Integer.parseInt(value.split(",")[1]);
                                    } catch (Exception e) {
                                          Logger.log(Logger.LogLevel.WARNING, "Error creating Integer for Effect (" + key + "). Either SparkTrail didn't save properly or the data file was edited.", true);
                                          return null;
                                    }
                              } else if (pt == ParticleType.CRITICAL) {
                                    try {
                                          pd.criticalType = Critical.CriticalType.valueOf(value);
                                    } catch (Exception e) {
                                          Logger.log(Logger.LogLevel.WARNING, "Error creating Effect (" + key + "). Either SparkTrail didn't save properly or the data file was edited.", true);
                                          return null;
                                    }
                              } else if (pt == ParticleType.FIREWORK) {
                                    pd.fireworkEffect = DataFactory.deserialiseFireworkEffect(value, ",");
                              } /*else if (pt == ParticleType.NOTE) {
                               try {
                               pd.noteType = Note.NoteType.valueOf(value);
                               } catch (Exception e) {
                               Logger.log(Logger.LogLevel.WARNING, "Error creating Effect (" + key + "). Either SparkTrail didn't save properly or the data file was edited.", true);
                               return null;
                               }
                               }*/ else if (pt == ParticleType.POTION) {
                                    try {
                                          pd.potionType = Potion.PotionType.valueOf(value);
                                    } catch (Exception e) {
                                          Logger.log(Logger.LogLevel.WARNING, "Error creating Effect (" + key + "). Either SparkTrail didn't save properly or the data file was edited.", true);
                                          return null;
                                    }
                              } else if (pt == ParticleType.SMOKE) {
                                    try {
                                          pd.smokeType = Smoke.SmokeType.valueOf(value);
                                    } catch (Exception e) {
                                          Logger.log(Logger.LogLevel.WARNING, "Error creating Effect (" + key + "). Either SparkTrail didn't save properly or the data file was edited.", true);
                                          return null;
                                    }
                              } else if (pt == ParticleType.SWIRL) {
                                    try {
                                          UUID uuid = null;
                                          if (eh.getEffectType().equals(EffectHolder.EffectType.PLAYER)) {
                                                Player p = Bukkit.getPlayerExact(key);
                                                if (p == null) {
                                                      continue;
                                                }
                                                uuid = p.getUniqueId();
                                          }
                                          if (uuid == null) {
                                                continue;
                                          }
                                          pd.swirlType = Swirl.SwirlType.valueOf(value);
                                    } catch (Exception e) {
                                          Logger.log(Logger.LogLevel.WARNING, "Error creating Effect (" + key + "). Either SparkTrail didn't save properly or the data file was edited.", true);
                                          return null;
                                    }
                              }
                              eh.addEffect(pd, false);
                        } else {
                              eh.addEffect(pt, false);
                        }
                  } else {
                        Logger.log(Logger.LogLevel.WARNING, "Error creating Effect (" + key + "). Either SparkTrail didn't save properly or the data file was edited.", true);
                        return null;
                  }
            }
            return eh;
      }

      public EffectHolder getEffect(Location l) {
            return this.getEffect(l.getWorld(), (int) l.getX(), (int) l.getY(), (int) l.getZ());
      }

      public EffectHolder getEffect(World world, int x, int y, int z) {
            for (EffectHolder e : effects) {
                  if (e.world.equals(world) && e.locX == x && e.locY == y && e.locZ == z) {
                        return e;
                  }
            }
            return null;
      }

      public EffectHolder getEffect(String playerName) {
            for (EffectHolder e : effects) {
                  if (e.getDetails().playerName != null && e.getDetails().playerName.equals(playerName)) {
                        return e;
                  }
            }
            return null;
      }

      public void remove(final String player_name, final EffectHolder e) {
            save(e);

            SparkTrailPlugin.getInstance().getServer().getScheduler().runTaskAsynchronously(SparkTrailPlugin.getInstance(), new Runnable() {
                  public void run() {
                        SQLEffectManager.instance.updateAsync(player_name, e);

                        SparkTrailPlugin.getInstance().getServer().getScheduler().runTask(SparkTrailPlugin.getInstance(), new Runnable() {
                              public void run() {
                                    clearFromMemory(e);
                              }
                        });
                  }
            });
      }
}
