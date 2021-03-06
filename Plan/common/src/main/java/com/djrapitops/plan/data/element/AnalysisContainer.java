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
package com.djrapitops.plan.data.element;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

/**
 * Container used to parse data for Server page.
 * <p>
 * Similar to InspectContainer, but can contain data for each player for a bigger Player data table.
 * <p>
 * Can contain values: addValue("Total Examples", 1) parses into ("Total Examples: 1")
 * Html: addHtml(key, "{@code <html>}") parses into ("{@code <html>}")
 * Tables: addTable(key, TableContainer) parses into ("{@code <table>...</table}")
 * Player Data for a big table: {@code addTableData("header", Map<UUID, value>)} parses a new column to Plugin data player table.
 * <p>
 * Has methods for adding icons to Strings:
 * getWithIcon("text", "cube") parses into {@code "<i class=\"fa fa-cube\"></i> text"}
 * getWithColoredIcon("text", "cube", "light-green") parses into {@code "<i class=\"col-light-green fa fa-cube\"></i> text"}
 *
 * @author Rsl1122
 * @see TableContainer
 * @see InspectContainer
 */
public final class AnalysisContainer extends InspectContainer {

    private Map<String, Map<UUID, ? extends Serializable>> playerTableValues;

    public AnalysisContainer() {
        playerTableValues = new TreeMap<>();
    }

    public Map<String, Map<UUID, ? extends Serializable>> getPlayerTableValues() {
        return playerTableValues;
    }

    public void addPlayerTableValues(String columnName, Map<UUID, ? extends Serializable> values) {
        playerTableValues.put(columnName, values);
    }

    public boolean hasPlayerTableValues() {
        return !playerTableValues.isEmpty();
    }
}
