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

import com.dsh105.dshutils.util.EnumUtil;
import com.dsh105.dshutils.util.StringUtil;
import com.dsh105.sparktrail.chat.BlockData;
import com.dsh105.sparktrail.trail.EffectHolder;
import com.dsh105.sparktrail.trail.ParticleDetails;
import com.dsh105.sparktrail.trail.ParticleType;
import com.dsh105.sparktrail.trail.type.*;
import com.dsh105.sparktrail.trail.type.Sound;
import com.dsh105.sparktrail.util.FireworkColour;
import com.dsh105.sparktrail.util.FireworkType;
import org.bukkit.*;
import java.util.*;

public class DataFactory {

      public static BlockData findBlockData(String msg) {
            if (msg.contains(" ")) {
                  String[] split = msg.split(" ");
                  if (!StringUtil.isInt(split[0])) {
                        return null;
                  }
                  if (!StringUtil.isInt(split[1])) {
                        return null;
                  }
                  return new BlockData(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
            } else {
                  if (!StringUtil.isInt(msg)) {
                        return null;
                  }
                  return new BlockData(Integer.parseInt(msg), 0);
            }
      }

      public static FireworkEffect generateFireworkEffectFrom(String msg) {
            FireworkEffect fe = null;
            ArrayList<Color> colours = new ArrayList<Color>();
            FireworkEffect.Type type = FireworkEffect.Type.BALL;
            boolean flicker = false;
            boolean trail = false;
            if (msg.equalsIgnoreCase("random")) {
                  Random r = new Random();
                  int colourAmount = r.nextInt(17);
                  for (int i = 0; i < colourAmount; i++) {
                        FireworkColour fireworkColour = FireworkColour.values()[i];
                        if (colours.contains(fireworkColour.getColor())) {
                              i--;
                        } else {
                              colours.add(fireworkColour.getColor());
                        }
                  }
                  type = FireworkEffect.Type.values()[r.nextInt(4)];
                  flicker = r.nextBoolean();
                  trail = r.nextBoolean();
            } else {
                  String[] split = msg.split(" ");

                  for (String s : split) {
                        if (s.equalsIgnoreCase("flicker")) {
                              flicker = true;
                        }
                        if (s.equalsIgnoreCase("trail")) {
                              trail = true;
                        }

                        if (EnumUtil.isEnumType(FireworkColour.class, s.toUpperCase())) {
                              colours.add(FireworkColour.valueOf(s.toUpperCase()).getColor());
                        }

                        if (EnumUtil.isEnumType(FireworkType.class, s.toUpperCase())) {
                              type = FireworkType.valueOf(s.toUpperCase()).getFireworkType();
                        }
                  }

            }

            if (colours.isEmpty()) {
                  colours.add(Color.WHITE);
            }

            fe = FireworkEffect.builder().withColor(colours).withFade(colours).with(type).flicker(flicker).trail(trail).build();

            if (fe == null) {
                  fe = FireworkEffect.builder().withColor(Color.WHITE).withFade(Color.WHITE).build();
            }
            return fe;
      }

      public static String serialiseFireworkEffect(FireworkEffect fe, String separator) {
            List<Color> colours = fe.getColors();
            FireworkEffect.Type type = fe.getType();
            boolean flicker = fe.hasFlicker();
            boolean trail = fe.hasTrail();

            String s = "";
            for (Color c : colours) {
                  s += FireworkColour.getByColor(c).toString() + separator;
            }
            s += FireworkType.getByType(type).toString().toLowerCase() + (flicker ? separator + "flicker" : "") + (trail ? separator + "trail" : "");
            return s;
      }

      public static FireworkEffect deserialiseFireworkEffect(String s, String separator) {
            FireworkEffect fe;
            ArrayList<Color> colours = new ArrayList<Color>();
            FireworkEffect.Type type = FireworkEffect.Type.BALL;
            boolean flicker = false;
            boolean trail = false;

            String[] split = s.split(separator);
            for (int i = 0; i < split.length; i++) {
                  if (s.equalsIgnoreCase("flicker")) {
                        flicker = true;
                  }
                  if (s.equalsIgnoreCase("trail")) {
                        trail = true;
                  }

                  if (EnumUtil.isEnumType(FireworkColour.class, s.toUpperCase())) {
                        colours.add(FireworkColour.valueOf(s.toUpperCase()).getColor());
                  }

                  if (EnumUtil.isEnumType(FireworkType.class, s.toUpperCase())) {
                        type = FireworkType.valueOf(s.toUpperCase()).getFireworkType();
                  }
            }

            fe = FireworkEffect.builder().withColor(colours).withFade(colours).with(type).flicker(flicker).trail(trail).build();
            return fe;
      }

      public static String serialiseEffects(HashSet<com.dsh105.sparktrail.trail.Effect> effects, boolean capitalise, boolean includeSpace, boolean includeData) {
            if (effects.isEmpty()) {
                  return "";
            }
            StringBuilder builder = new StringBuilder();
            for (com.dsh105.sparktrail.trail.Effect e : effects) {
                  String s = capitalise ? StringUtil.capitalise(e.getParticleType().toString()) : e.getParticleType().toString().toLowerCase();
                  builder.append(s);
                  if (includeData) {
                        ParticleType pt = e.getParticleType();
                        if (e == null || pt == null) {
                              continue;
                        }

                        try {
                              if (e.getParticleType().requiresDataMenu()) {
                                    builder.append(";");
                                    if (pt == ParticleType.CRITICAL) {
                                          builder.append(((Critical) e).criticalType.toString());
                                    } else if (pt == ParticleType.FIREWORK) {
                                          builder.append(serialiseFireworkEffect(((Firework) e).fireworkEffect, "-"));
                                    } else if (pt == ParticleType.BLOCKBREAK) {
                                          builder.append(((BlockBreak) e).idValue).append("-").append(((BlockBreak) e).metaValue);
                                    } else if (pt == ParticleType.ITEMSPRAY) {
                                          builder.append(((ItemSpray) e).idValue).append("-").append(((BlockBreak) e).metaValue);
                                    } else if (pt == ParticleType.POTION) {
                                          builder.append(((Potion) e).potionType.toString());
                                    } else if (pt == ParticleType.SMOKE) {
                                          builder.append(((Smoke) e).smokeType.toString());
                                    } else if (pt == ParticleType.SWIRL) {
                                          builder.append(((Swirl) e).swirlType.toString());
                                    } else if (pt == ParticleType.SOUND) {
                                          builder.append(((Sound) e).sound.toString());
                                    }
                              }
                        } catch (Exception exception) {
                              exception.printStackTrace();
                              continue;
                        }
                  }
                  builder.append(",").append(includeSpace ? " " : "");
            }
            return builder.toString();
      }

      public static void deserialiseEffect(String s, EffectHolder holder) {
            ParticleType pt = null;
            if (s.contains(";")) {
                  String[] split = s.split(";");
                  String effect = split[0];
                  if (EnumUtil.isEnumType(ParticleType.class, effect.toUpperCase())) {
                        pt = ParticleType.valueOf(effect.toUpperCase());
                  }

                  if (pt != null) {
                        String data = split[1];
                        for (Object[] o : pt.getDataFrom(data)) {
                              holder.addEffect(new ParticleDetails(pt).set(o), false);
                        }
                  }
            } else if (EnumUtil.isEnumType(ParticleType.class, s.toUpperCase())) {
                  pt = ParticleType.valueOf(s.toUpperCase());
                  if (pt != null) {
                        holder.addEffect(pt, false);
                  }
            }
      }

      public static EffectHolder addEffectsFrom(String s, EffectHolder holder) {
            if (s.contains(",")) {
                  String[] split = s.split(",");
                  for (int i = 0; i < split.length; i++) {
                        deserialiseEffect(split[i], holder);
                  }
            } else {
                  deserialiseEffect(s, holder);
            }
            return holder;
      }

}
