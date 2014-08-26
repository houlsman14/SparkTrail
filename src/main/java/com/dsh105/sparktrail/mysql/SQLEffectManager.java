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

public class SQLEffectManager {

      public static SQLEffectManager instance;

      public SQLEffectManager() {
            instance = this;
      }

      private void createData(String playerName) {
            if (ConfigOptions.instance.useSql()) {
                  Connection con = null;
                  PreparedStatement statement = null;

                  if (SparkTrailPlugin.getInstance().dbPool != null) {
                        try {
                              con = SparkTrailPlugin.getInstance().dbPool.getConnection();

                              statement = con.prepareStatement("INSERT INTO PlayerEffects (PlayerName, Effects) VALUES (?, ?);");
                              statement.setString(1, playerName);

                              if (statement != null) {
                                    statement.setString(2, "");
                                    statement.executeUpdate();
                              }

                        } catch (SQLException e) {
                        } finally {
                              try {
                                    if (statement != null) {
                                          statement.close();
                                    }
                                    if (con != null) {
                                          con.close();
                                    }
                              } catch (SQLException ignored) {
                              }
                        }
                  }
            }
      }

      public void update(EffectHolder eh) {
            if (ConfigOptions.instance.useSql()) {
                  Connection con = null;
                  PreparedStatement statement = null;

                  if (eh.getEffects().size() <= 0) {
                        return;
                  }

                  if (SparkTrailPlugin.getInstance().dbPool != null) {
                        try {
                              SparkTrailPlugin.getInstance().SQLH.createData(eh.getDetails().playerName);

                              con = SparkTrailPlugin.getInstance().dbPool.getConnection();
                              String data = DataFactory.serialiseEffects(eh.getEffects(), false, false, true);

                              if (eh.getEffectType().equals(EffectHolder.EffectType.PLAYER)) {
                                    statement = con.prepareStatement("UPDATE PlayerEffects SET Effects = ? WHERE PlayerName = ?");
                                    statement.setString(2, eh.getDetails().playerName);
                              }

                              if (statement != null) {
                                    statement.setString(1, data);
                                    statement.executeUpdate();
                              }

                        } catch (SQLException e) {
                              e.printStackTrace();
                        } finally {
                              try {
                                    if (statement != null) {
                                          statement.close();
                                    }
                                    if (con != null) {
                                          con.close();
                                    }
                              } catch (SQLException ignored) {
                              }
                        }
                  }
            }
      }

      public EffectHolder load(String playerName) {
            if (ConfigOptions.instance.useSql()) {
                  Connection con = null;
                  PreparedStatement statement = null;

                  if (SparkTrailPlugin.getInstance().dbPool != null) {
                        try {
                              con = SparkTrailPlugin.getInstance().dbPool.getConnection();
                              statement = con.prepareStatement("SELECT * FROM PlayerEffects WHERE PlayerName = ?;");
                              statement.setString(1, playerName);
                              ResultSet rs = statement.executeQuery();
                              while (rs.next()) {
                                    EffectManager.getInstance().clearFromMemory(EffectManager.getInstance().getEffect(playerName));
                                    EffectHolder eh = EffectCreator.createPlayerHolder(playerName);
                                    String effects = rs.getString("Effects");
                                    DataFactory.addEffectsFrom(effects, eh);
                              }

                        } catch (SQLException e) {
                              e.printStackTrace();
                        } finally {
                              try {
                                    if (statement != null) {
                                          statement.close();
                                    }
                                    if (con != null) {
                                          con.close();
                                    }
                              } catch (SQLException ignored) {
                              }
                        }
                  }
            }
            return null;
      }
}
