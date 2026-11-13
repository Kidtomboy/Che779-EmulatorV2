/*
 *  MicroEmulator
 *  Copyright (C) 2002 Bartek Teodorczyk <barteo@barteo.net>
 *
 *  It is licensed under the following two licenses as alternatives:
 *    1. GNU Lesser General Public License (the "LGPL") version 2.1 or any newer version
 *    2. Apache License (the "AL") Version 2.0
 *
 *  You may not use this file except in compliance with at least one of
 *  the above two licenses.
 *
 *  You may obtain a copy of the LGPL at
 *      http://www.gnu.org/licenses/old-licenses/lgpl-2.1.txt
 *
 *  You may obtain a copy of the AL at
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the LGPL or the AL for the specific language governing permissions and
 *  limitations.
 */
 
package org.microemu.device;


public class DeviceFactory 
{
  private static Device device;

  /** Che779 V2: moi tab (slot) co thiet bi rieng, gan theo luong (InheritableThreadLocal). */
  public static final InheritableThreadLocal threadDevice = new InheritableThreadLocal();

  public static volatile boolean slotMode = false;

  public static void setThreadDevice(Device d) {
    threadDevice.set(d);
  }
  
  
  public static Device getDevice()
  {
    Device t = (Device) threadDevice.get();
    return t != null ? t : device;
  }
  
  
  public static void setDevice(Device device)
  {
	  if (slotMode) {
		  device.init();
		  threadDevice.set(device);
	  } else {
		  if (DeviceFactory.device != null) {
			  DeviceFactory.device.destroy();
		  }
		  device.init();
		  DeviceFactory.device = device;
	  }
	  // Che779: che do mau (xam/den) de giam RAM
	  try {
		  int c = org.microemu.app.Che779.getColor();
		  org.microemu.device.impl.DeviceDisplayImpl dd = (org.microemu.device.impl.DeviceDisplayImpl) device.getDeviceDisplay();
		  dd.setIsColor(c == 0);
		  if (c != 0) {
			  dd.setNumColors(c);
		  }
	  } catch (Throwable t) {
	  }
  }
  
}
