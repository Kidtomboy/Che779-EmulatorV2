/*
 *  MicroEmulator
 *  Copyright (C) 2001-2007 Bartek Teodorczyk <barteo@barteo.net>
 *  Copyright (C) 2007-2007 Vlad Skarzhevskyy
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
 *
 *  Contributor(s):
 *    3GLab
 *    
 *  @version $Id$    
 */

package org.microemu;

import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;

import javax.microedition.io.ConnectionNotFoundException;
import javax.microedition.lcdui.game.GameCanvas;
import javax.microedition.midlet.MIDlet;

import org.microemu.app.launcher.Launcher;

/**
 * 
 * Enables access to MIDlet and MIDletAccess by threadLocal
 *
 */
public class MIDletBridge {

	static MicroEmulator emulator = null;

	/** Che779 V2: emulator / slot gan theo luong. */
	public static final InheritableThreadLocal threadEmulator = new InheritableThreadLocal();

	public static final InheritableThreadLocal threadSlotId = new InheritableThreadLocal();

	static final Map /*<Integer, MIDletContext>*/ slotContexts = java.util.Collections.synchronizedMap(new java.util.HashMap());

	static final Map /*<MIDletContext, Integer>*/ contextSlot = java.util.Collections.synchronizedMap(new WeakHashMap());

	public static void setSlotId(Integer id) {
		threadSlotId.set(id);
	}

	public static Integer getSlotId() {
		return (Integer) threadSlotId.get();
	}

	public static MIDletContext getSlotContext(int id) {
		return (MIDletContext) slotContexts.get(Integer.valueOf(id));
	}

	public static void removeSlot(int id) {
		slotContexts.remove(Integer.valueOf(id));
	}

	static ThreadLocal /*<MIDletContext>*/ threadMIDletContexts = null;
	
	static final Map /*<MIDlet, MIDletContext>*/ midletContexts = java.util.Collections.synchronizedMap(new WeakHashMap());

	static MIDlet currentMIDlet = null;

	public static void setMicroEmulator(MicroEmulator emulator) {
		MIDletBridge.emulator = emulator;
		threadEmulator.set(emulator);
		
		if (threadMIDletContexts == null) { 
			threadMIDletContexts = new ThreadLocal();
		}
	}
	
	public static MicroEmulator getMicroEmulator() {
		MicroEmulator t = (MicroEmulator) threadEmulator.get();
		return t != null ? t : emulator;
	}
	
	public static MIDletContext getThreadMIDletContext() {
		return threadMIDletContexts == null ? null : (MIDletContext) threadMIDletContexts.get();
	}

	public static void setThreadMIDletContext(MIDletContext midletContext) {
		threadMIDletContexts.set(midletContext);
	}
	
	public static void registerMIDletAccess(MIDletAccess accessor) {
		MIDletContext c = (MIDletContext)threadMIDletContexts.get();
		if (c == null) {
			//throw new Error("setThreadMIDletContext should be called");
			c = new MIDletContext();
			setThreadMIDletContext(c);
		}
		c.setMIDletAccess(accessor);
		registerMIDletContext(c);
	}
	
	public static void registerMIDletContext(MIDletContext midletContext) {
		midletContexts.put(midletContext.getMIDlet(), midletContext);
		Integer sid = getSlotId();
		if (sid != null) {
			contextSlot.put(midletContext, sid);
			slotContexts.put(sid, midletContext);
		}
	}

	public static MIDletContext getMIDletContext(MIDlet midlet) {
		return (MIDletContext)midletContexts.get(midlet);
	}
	
	public static MIDletContext getMIDletContext() {
		MIDletContext c = (MIDletContext)threadMIDletContexts.get();
		if (c != null) {
			return c;
		}
		Integer sid = getSlotId();
		if (sid != null) {
			return (MIDletContext) slotContexts.get(sid);
		}
		return getMIDletContext(currentMIDlet);
	}

	public static void setCurrentMIDlet(MIDlet midlet) {
		currentMIDlet = midlet;
	}

	public static MIDlet getCurrentMIDlet() {
		MIDletContext c = getMIDletContext();
		if (c == null) {
			return null;
		}
		return c.getMIDlet();
	}
	
	public static MIDletAccess getMIDletAccess() {
		MIDletContext c = getMIDletContext();
		if (c == null) {
			return null;
		}
		return c.getMIDletAccess();
	}

	public static MIDletAccess getMIDletAccess(MIDlet midlet) {
		return getMIDletContext(midlet).getMIDletAccess();
	}

	
	public static RecordStoreManager getRecordStoreManager() {
		return getMicroEmulator().getRecordStoreManager();
	}

	public static String getAppProperty(String key) {
		return getMicroEmulator().getAppProperty(key);
	}
	
	public static InputStream getResourceAsStream(Class origClass, String name) {
		return getMicroEmulator().getResourceAsStream(origClass, name);
	}


	public static void notifyDestroyed() {
		MIDletContext midletContext = getMIDletContext();
		getMicroEmulator().notifyDestroyed(midletContext);
		destroyMIDletContext(midletContext);
	}

	public static void destroyMIDletContext(MIDletContext midletContext) {
		if (midletContext == null) {
			return;
		}
		getMicroEmulator().destroyMIDletContext(midletContext);
		synchronized (midletContexts) {
			for (Iterator i = midletContexts.entrySet().iterator(); i.hasNext();) {
				Map.Entry entry = (Map.Entry) i.next();
				if (entry.getValue() == midletContext) {
					i.remove();
					break;
				}
			}
		}
	}
	
	public static int checkPermission(String permission) {
		return getMicroEmulator().checkPermission(permission);
	}
	
	public static boolean platformRequest(String URL) throws ConnectionNotFoundException {
		return getMicroEmulator().platformRequest(URL);
	}

	public static void clear() {
		Integer sid = getSlotId();
		if (sid != null) {
			synchronized (midletContexts) {
				for (Iterator i = midletContexts.entrySet().iterator(); i.hasNext();) {
					Map.Entry entry = (Map.Entry) i.next();
					Integer cs = (Integer) contextSlot.get(entry.getValue());
					if (cs != null && cs.equals(sid)) {
						i.remove();
					}
				}
			}
			slotContexts.remove(sid);
			return;
		}
		currentMIDlet = null;
		
		// Preserve only Launcher Context. Iteration of a synchronized map must hold its monitor.
		synchronized (midletContexts) {
			for (Iterator i = midletContexts.entrySet().iterator(); i.hasNext();) {
				Map.Entry entry = (Map.Entry) i.next();
				MIDlet test = ((MIDletContext) entry.getValue()).getMIDlet();
				if (test instanceof Launcher) {
					Object key = entry.getKey();
					Object value = entry.getValue();
					midletContexts.clear();
					midletContexts.put(key, value);
					return;
				}
			}
			midletContexts.clear();
		}
	}
	
	static final Map /*<GameCanvas, GameCanvasKeyAccess>*/ gameCanvasAccesses = java.util.Collections.synchronizedMap(new WeakHashMap());	

	public static GameCanvasKeyAccess getGameCanvasKeyAccess(GameCanvas gameCanvas) {
		return (GameCanvasKeyAccess) gameCanvasAccesses.get(gameCanvas);
	}

	public static void registerGameCanvasKeyAccess(GameCanvas gameCanvas, GameCanvasKeyAccess access) {
		gameCanvasAccesses.put(gameCanvas, access);
	}



}
