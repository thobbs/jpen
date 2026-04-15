/* [{
Copyright 2007, 2008 Nicolas Carranza <nicarran at gmail.com>

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
package jpen.provider;

import java.util.List;
import jpen.PKind;
import jpen.Pen;
import jpen.PenDevice;
import jpen.PenManager;
import jpen.PenProvider;

public abstract class AbstractPenDevice implements PenDevice {

  private byte id;
  private String physicalId;
  private final PenProvider provider;
  private int kindTypeNumber = PKind.Type.CURSOR.ordinal();
  private boolean enabled;

  protected AbstractPenDevice(PenProvider provider) {
    this.provider = provider;
  }

  public byte getId() {
    return id;
  }

  public void penManagerSetId(byte id) {
    this.id = id;
  }

  public PenProvider getProvider() {
    return provider;
  }

  public int getKindTypeNumber() {
    return kindTypeNumber;
  }

  public void setKindTypeNumber(int kindTypeNumber) {
    if (kindTypeNumber < 0) throw new IllegalArgumentException("PKind.Type must be >= 0");
    this.kindTypeNumber = kindTypeNumber;
  }

  public boolean getEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public String getPhysicalId() {
    String physicalId = this.physicalId;
    if (physicalId == null)
      synchronized (this) {
        physicalId = this.physicalId;
        if (physicalId == null) physicalId = this.physicalId = evalPhysicalId();
      }
    return physicalId;
  }

  protected String evalPhysicalId() {
    return getName().trim() + "@" + provider.getConstructor().getName().trim();
  }

  public final PenManager getPenManager() {
    return provider.getConstructor().getPenManager();
  }

  public final Pen getPen() {
    return getPenManager().pen;
  }

  public boolean getUseFractionalMovements() {
    return true;
  }

  public void penManagerSetUseFractionalMovements(boolean useFractionalMovement) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String toString() {
    return "[PenDevice: provider="
        + getProvider()
        + ", name="
        + getName()
        + ", kind="
        + getOrNull(PKind.Type.VALUES, getKindTypeNumber())
        + "("
        + getKindTypeNumber()
        + ")]";
  }

  private static final <T> T getOrNull(List<T> l, int index) {
    if (index < 0 || index >= l.size()) return null;
    return l.get(index);
  }
}
