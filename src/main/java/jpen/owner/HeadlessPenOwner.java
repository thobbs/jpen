/* [{
Copyright 2026 JPen Team

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
package jpen.owner;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.Collection;
import jpen.PenEvent;
import jpen.PenProvider;
import jpen.provider.osx.CocoaProvider;
import jpen.provider.wintab.WintabProvider;
import jpen.provider.xinput.XinputProvider;

/**
 * A {@link PenOwner} for headless / non-GUI processes. Unlike {@link ScreenPenOwner}, it does not
 * tie the {@link jpen.PenManager}'s paused state to AWT active-window focus — it unpauses the
 * manager as soon as it is installed and leaves it running, so events flow without any AWT Window
 * being present. Its {@link PenClip} covers all screen coordinates.
 */
public class HeadlessPenOwner implements PenOwner {

  public Collection<PenProvider.Constructor> getPenProviderConstructors() {
    return Arrays.asList(
        new PenProvider.Constructor[] {
          new XinputProvider.Constructor(),
          new WintabProvider.Constructor(),
          new CocoaProvider.Constructor()
        });
  }

  public void setPenManagerHandle(PenManagerHandle penManagerHandle) {
    synchronized (penManagerHandle.getPenSchedulerLock()) {
      penManagerHandle.setPenManagerPaused(false);
    }
  }

  private final PenClip penClip =
      new PenClip() {
        public void evalLocationOnScreen(Point locationOnScreen) {
          locationOnScreen.x = locationOnScreen.y = 0;
        }

        public boolean contains(Point2D.Float point) {
          return true;
        }
      };

  public PenClip getPenClip() {
    return penClip;
  }

  public boolean isDraggingOut() {
    return false;
  }

  public Object evalPenEventTag(PenEvent ev) {
    return null;
  }

  public boolean enforceSinglePenManager() {
    return false;
  }
}
