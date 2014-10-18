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
package com.dsh105.sparktrail.mysql;

import com.dsh105.sparktrail.SparkTrailPlugin;
import com.dsh105.sparktrail.config.ConfigOptions;
import com.dsh105.sparktrail.data.DataFactory;
import com.dsh105.sparktrail.data.EffectCreator;
import com.dsh105.sparktrail.data.EffectManager;
import com.dsh105.sparktrail.trail.EffectHolder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.Cleanup;

public class SQLEffectManager {

      public static SQLEffectManager instance;

      public SQLEffectManager() {
            instance = this;
      }

      private void createData(String playerName) {
            if (ConfigOptions.instance.useSql()) {
                  if (SparkTrailPlugin.getInstance().dbPool != null) {
                        try {
                              @Cleanup
                              Connection con = SparkTrailPlugin.getInstance().dbPool.getConnection();
                              @Cleanup
                              PreparedStatement statement = con.prepareStatement("INSERT INTO PlayerEffects (PlayerName, Effects) VALUES (?, ?);");
                              statement.setString(1, playerName);
                              statement.setString(2, "");
                              statement.executeUpdate();
                        } catch (SQLException e) {
                        }
                  }
            }
      }

      public void updateAsync(String player_name, EffectHolder eh) {
            if (ConfigOptions.instance.useSql()) {
                  if (SparkTrailPlugin.getInstance().dbPool != null) {
                        try {
                              createData(player_name);
                              if (eh == null || eh.getEffects() == null || eh.getEffects().isEmpty()) {
                                    @Cleanup
                                    Connection con = SparkTrailPlugin.getInstance().dbPool.getConnection();
                                    @Cleanup
                                    PreparedStatement statement = con.prepareStatement("UPDATE PlayerEffects SET Effects = ? WHERE PlayerName = ?");
                                    statement.setString(2, player_name);
                                    statement.setString(1, "");
                                    statement.executeUpdate();
                                    return;
                              }

                              if (eh.getEffectType().equals(EffectHolder.EffectType.PLAYER)) {
                                    @Cleanup
                                    Connection con = SparkTrailPlugin.getInstance().dbPool.getConnection();
                                    String data = DataFactory.serialiseEffects(eh.getEffects(), false, false, true);
                                    @Cleanup
                                    PreparedStatement statement = con.prepareStatement("UPDATE PlayerEffects SET Effects = ? WHERE PlayerName = ?");
                                    statement.setString(2, eh.getDetails().playerName);
                                    statement.setString(1, data);
                                    statement.executeUpdate();
                              }
                        } catch (SQLException e) {
                              e.printStackTrace();
                        }
                  }
            }
      }

      public EffectHolder loadAsync(final String playerName) {
            if (ConfigOptions.instance.useSql()) {
                  if (SparkTrailPlugin.getInstance().dbPool != null) {
                        try {
                              @Cleanup
                              Connection con = SparkTrailPlugin.getInstance().dbPool.getConnection();
                              @Cleanup
                              PreparedStatement statement = con.prepareStatement("SELECT * FROM PlayerEffects WHERE PlayerName = ?;");
                              statement.setString(1, playerName);
                              final ResultSet rs = statement.executeQuery();

                              SparkTrailPlugin.getInstance().getServer().getScheduler().runTask(SparkTrailPlugin.getInstance(), new Runnable() {
                                    public void run() {
                                          try {
                                                while (rs.next()) {
                                                      EffectManager.getInstance().clearFromMemory(EffectManager.getInstance().getEffect(playerName));
                                                      EffectHolder eh = EffectCreator.createPlayerHolder(playerName);
                                                      String effects = rs.getString("Effects");
                                                      DataFactory.addEffectsFrom(effects, eh);
                                                }

                                                if (!rs.isClosed()) {
                                                      rs.close();
                                                }
                                          } catch (SQLException ex) {
                                                ex.printStackTrace();
                                          }
                                    }
                              });
                        } catch (SQLException e) {
                              e.printStackTrace();
                        }
                  }
            }
            return null;
      }
}
