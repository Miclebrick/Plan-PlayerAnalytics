/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.db.access.queries.objects;

import com.djrapitops.plan.data.container.BaseUser;
import com.djrapitops.plan.db.access.Query;
import com.djrapitops.plan.db.access.QueryAllStatement;
import com.djrapitops.plan.db.access.QueryStatement;
import com.djrapitops.plan.db.sql.parsing.Select;
import com.djrapitops.plan.db.sql.tables.UserInfoTable;
import com.djrapitops.plan.db.sql.tables.UsersTable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

/**
 * Queries for {@link BaseUser} objects.
 *
 * @author Rsl1122
 */
public class BaseUserQueries {

    private BaseUserQueries() {
        /* Static method class */
    }

    /**
     * Query database for common user information.
     * <p>
     * Only one {@link BaseUser} per player exists unlike {@link com.djrapitops.plan.data.container.UserInfo} which is available per server.
     *
     * @return Map: Player UUID - BaseUser
     */
    public static Query<Collection<BaseUser>> fetchAllBaseUsers() {
        String sql = Select.all(UsersTable.TABLE_NAME).toString();

        return new QueryAllStatement<Collection<BaseUser>>(sql, 20000) {
            @Override
            public Collection<BaseUser> processResults(ResultSet set) throws SQLException {
                Collection<BaseUser> users = new HashSet<>();
                while (set.next()) {
                    UUID playerUUID = UUID.fromString(set.getString(UsersTable.USER_UUID));
                    String name = set.getString(UsersTable.USER_NAME);
                    long registered = set.getLong(UsersTable.REGISTERED);
                    int kicked = set.getInt(UsersTable.TIMES_KICKED);

                    users.add(new BaseUser(playerUUID, name, registered, kicked));
                }
                return users;
            }
        };
    }

    /**
     * Query database for common user information of a player.
     * <p>
     * Only one {@link BaseUser} per player exists unlike {@link com.djrapitops.plan.data.container.UserInfo} which is available per server.
     *
     * @param playerUUID UUID of the Player.
     * @return Optional: BaseUser if found, empty if not.
     */
    public static Query<Optional<BaseUser>> fetchBaseUserOfPlayer(UUID playerUUID) {
        String sql = Select.all(UsersTable.TABLE_NAME).where(UsersTable.USER_UUID + "=?").toString();

        return new QueryStatement<Optional<BaseUser>>(sql, 20000) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, playerUUID.toString());
            }

            @Override
            public Optional<BaseUser> processResults(ResultSet set) throws SQLException {
                if (set.next()) {
                    UUID playerUUID = UUID.fromString(set.getString(UsersTable.USER_UUID));
                    String name = set.getString(UsersTable.USER_NAME);
                    long registered = set.getLong(UsersTable.REGISTERED);
                    int kicked = set.getInt(UsersTable.TIMES_KICKED);

                    return Optional.of(new BaseUser(playerUUID, name, registered, kicked));
                }
                return Optional.empty();
            }
        };
    }

    /**
     * Query database for common user information for players that have played on a specific server.
     * <p>
     * Only one {@link BaseUser} per player exists unlike {@link com.djrapitops.plan.data.container.UserInfo} which is available per server.
     * <p>
     * This will fetch BaseUsers for which UserInfo object also exists on the server.
     *
     * @param serverUUID UUID of the Plan server.
     * @return Collection: BaseUsers
     */
    public static Query<Collection<BaseUser>> fetchServerBaseUsers(UUID serverUUID) {
        String sql = "SELECT " +
                UsersTable.TABLE_NAME + "." + UsersTable.USER_UUID + ", " +
                UsersTable.USER_NAME + ", " +
                UsersTable.TABLE_NAME + "." + UsersTable.REGISTERED + ", " +
                UsersTable.TIMES_KICKED +
                " FROM " + UsersTable.TABLE_NAME +
                " INNER JOIN " + UserInfoTable.TABLE_NAME + " on " +
                UsersTable.TABLE_NAME + "." + UsersTable.USER_UUID + "=" + UserInfoTable.TABLE_NAME + "." + UserInfoTable.USER_UUID +
                " WHERE " + UserInfoTable.SERVER_UUID + "=?";
        return new QueryStatement<Collection<BaseUser>>(sql, 1000) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, serverUUID.toString());
            }

            @Override
            public Collection<BaseUser> processResults(ResultSet set) throws SQLException {
                Collection<BaseUser> users = new HashSet<>();
                while (set.next()) {
                    UUID playerUUID = UUID.fromString(set.getString(UsersTable.USER_UUID));
                    String name = set.getString(UsersTable.USER_NAME);
                    long registered = set.getLong(UsersTable.REGISTERED);
                    int kicked = set.getInt(UsersTable.TIMES_KICKED);

                    users.add(new BaseUser(playerUUID, name, registered, kicked));
                }
                return users;
            }
        };
    }

}