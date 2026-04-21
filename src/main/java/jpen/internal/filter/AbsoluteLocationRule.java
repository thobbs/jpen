/* [{
Copyright 2010 Nicolas Carranza <nicarran at gmail.com>

This file is part of jpen.

jpen is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License,
or (at your option) any later version.

jpen is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with jpen.  If not, see <http://www.gnu.org/licenses/>.
}] */
package jpen.internal.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class AbsoluteLocationRule implements RelativeLocationFilter.Rule {
  private static final Logger L = LoggerFactory.getLogger(AbsoluteLocationRule.class);

  private int missedPoints;

  // @Override
  public void reset() {
    missedPoints = -1;
  }

  // @Override
  public RelativeLocationFilter.State evalFilterNextState(RelativeLocationFilter filter) {
    missedPoints++;
    if (!filter.samplePoint.isComplete) return null;
    float maxDeviation = Math.max(filter.absDeviation.x, filter.absDeviation.y);
    if (maxDeviation < 1.5f) {
      L.trace("absolute device detected, missedPoints={}", missedPoints);
      return RelativeLocationFilter.State.ABSOLUTE;
    }
    return null;
  }
}
