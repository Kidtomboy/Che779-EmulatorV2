package org.microemu.app.v2;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

import javax.swing.SwingUtilities;

import org.microemu.DisplayAccess;
import org.microemu.MIDletAccess;
import org.microemu.MIDletBridge;
import org.microemu.MIDletContext;
import org.microemu.app.Che779;
import org.microemu.app.ui.StatusBarListener;
import org.microemu.app.ui.ResponseInterfaceListener;
import org.microemu.app.ui.swing.SlotBinder;
import org.microemu.app.ui.swing.SwingDeviceComponent;
import org.microemu.app.ui.swing.SwingDisplayComponent;
import org.microemu.app.util.MIDletResourceLoader;
import org.microemu.device.Device;
import org.microemu.device.DeviceFactory;
import org.microemu.device.impl.DeviceDisplayImpl;
import org.microemu.device.impl.DeviceImpl;
import org.microemu.device.impl.Rectangle;
import org.microemu.device.impl.SoftButton;
import org.microemu.device.j2se.J2SEDevice;
import org.microemu.log.Logger;

/**
 * Che779 V2: mot tab (slot) chay mot game rieng trong cung JVM. Thiet bi, emulator, RMS, class loader va proxy
 * deu gan theo tab; khi xu ly su kien tren EDT, {@link #enter()} gan ngu canh cua tab vao luong.
 */
public class Slot implements SlotBinder {
	public enum State { EMPTY, LOADING, RUNNING, ENDED, ERROR }

	public interface Listener {
		void slotChanged(Slot s);
	}

	/** Nap jar tuan tu (Launcher giu danh sach MIDlet o bien tinh). */
	static final Object LOAD_LOCK = new Object();

	public final int id;
	public volatile String name = "Trống";
	public volatile String jarPath;
	public volatile State state = State.EMPTY;
	public volatile String error;
	public volatile boolean syncEnabled = true;

	public final SwingDeviceComponent devicePanel = new SwingDeviceComponent();
	public final SlotContext ctx;
	volatile SlotCommon common;
	volatile Device device;
	volatile ClassLoader gameLoader;
	private volatile int width, height;
	private final List<Listener> listeners = new ArrayList<Listener>();

	public Slot(int id, int width, int height) {
		this.id = id;
		this.width = width;
		this.height = height;
		this.ctx = new SlotContext(this);
		devicePanel.addKeyListener(devicePanel);
		devicePanel.setFocusable(true);
		devicePanel.binder = this;
		((SwingDisplayComponent) devicePanel.getDisplayComponent()).binder = this;
	}

	public void addListener(Listener l) {
		synchronized (listeners) {
			listeners.add(l);
		}
	}

	private void fire() {
		final Listener[] ls;
		synchronized (listeners) {
			ls = listeners.toArray(new Listener[0]);
		}
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				for (Listener l : ls) {
					l.slotChanged(Slot.this);
				}
			}
		});
	}

	// ------------------------------------------------------------------ ngu canh luong
	public Object enter() {
		Object[] tok = { DeviceFactory.threadDevice.get(), MIDletBridge.threadEmulator.get(), MIDletBridge.getSlotId(),
				MIDletBridge.getThreadMIDletContext(), MIDletResourceLoader.threadClassLoader.get() };
		apply();
		return tok;
	}

	void apply() {
		if (device != null) {
			DeviceFactory.setThreadDevice(device);
		}
		if (common != null) {
			MIDletBridge.threadEmulator.set(common);
		}
		MIDletBridge.setSlotId(Integer.valueOf(id));
		MIDletContext c = MIDletBridge.getSlotContext(id);
		MIDletBridge.setThreadMIDletContext(c);
		if (gameLoader != null) {
			MIDletResourceLoader.threadClassLoader.set(gameLoader);
		}
	}

	public void exit(Object token) {
		Object[] t = (Object[]) token;
		if (t == null) {
			return;
		}
		DeviceFactory.threadDevice.set(t[0]);
		MIDletBridge.threadEmulator.set(t[1]);
		MIDletBridge.threadSlotId.set(t[2]);
		MIDletBridge.setThreadMIDletContext((MIDletContext) t[3]);
		MIDletResourceLoader.threadClassLoader.set(t[4]);
	}

	public void runBound(Runnable r) {
		Object t = enter();
		try {
			r.run();
		} finally {
			exit(t);
		}
	}

	// ------------------------------------------------------------------ nap / dong game
	public void open(final String path) {
		if (disposed) {
			return;
		}
		final File f = new File(path);
		if (!f.exists()) {
			error = "Không tìm thấy file: " + path;
			state = State.ERROR;
			fire();
			return;
		}
		jarPath = path;
		name = f.getName();
		state = State.LOADING;
		error = null;
		fire();
		Thread t = new Thread(new Runnable() {
			public void run() {
				try {
					synchronized (LOAD_LOCK) {
						if (disposed) {
							return;
						}
						closeGameInternal();
						setup();
						apply();
						common.prepareNextLoad();
						List<String> params = new ArrayList<String>(Arrays.asList(path));
						common.initParams(params, null, J2SEDevice.class);
						gameLoader = (ClassLoader) MIDletResourceLoader.threadClassLoader.get();
						common.initMIDlet(true);
						gameLoader = (ClassLoader) MIDletResourceLoader.threadClassLoader.get();
					}
					if (state == State.LOADING) {
						state = State.RUNNING;
					}
					String sn = common.getSuiteName();
					if (sn != null && sn.length() > 0) {
						name = sn.replaceAll("_tab" + id + "$", "");
					}
				} catch (Throwable e) {
					Logger.error("Tab " + id + ": lỗi nạp game", e);
					error = String.valueOf(e);
					state = State.ERROR;
				}
				fire();
			}
		}, "slot-" + id + "-loader");
		t.setDaemon(true);
		t.start();
	}

	private volatile boolean panelInit;
	private volatile boolean disposed;

	/** Tao thiet bi + trinh dieu khien rieng cho tab (lan dau). Khong can EDT. */
	public synchronized void ensureDevice() throws Exception {
		Object old = DeviceFactory.threadDevice.get();
		try {
			if (device == null) {
				ctx.getDeviceFontManager().init();
				DeviceImpl dev = DeviceImpl.create(ctx, getClass().getClassLoader(), DeviceImpl.RESIZABLE_LOCATION, J2SEDevice.class);
				applySize(dev, width, height);
				DeviceFactory.setDevice(dev);
				device = dev;
			}
			if (common == null) {
				common = new SlotCommon(ctx, this);
				common.setStatusBarListener(new StatusBarListener() {
					public void statusBarChanged(String text) {
					}
				});
				common.setResponseInterfaceListener(new ResponseInterfaceListener() {
					public void stateChanged(boolean s) {
					}
				});
				DeviceFactory.setThreadDevice(device);
				MIDletBridge.threadEmulator.set(common);
				MIDletBridge.setSlotId(Integer.valueOf(id));
				common.loadImplementationsFromConfig();
			}
		} finally {
			DeviceFactory.threadDevice.set(old);
		}
	}

	/** Khoi tao man hinh (phai chay tren EDT). */
	public void initPanel() {
		if (panelInit) {
			return;
		}
		panelInit = true;
		runBound(new Runnable() {
			public void run() {
				devicePanel.init();
				devicePanel.revalidate();
			}
		});
	}

	private void setup() throws Exception {
		ensureDevice();
		if (!panelInit) {
			if (SwingUtilities.isEventDispatchThread()) {
				initPanel();
			} else {
				SwingUtilities.invokeAndWait(new Runnable() {
					public void run() {
						initPanel();
					}
				});
			}
		}
		apply();
	}

	private static void applySize(Device dev, int w, int h) {
		DeviceDisplayImpl dd = (DeviceDisplayImpl) dev.getDeviceDisplay();
		int menuh = 0;
		Enumeration en = dev.getSoftButtons().elements();
		while (en.hasMoreElements()) {
			SoftButton b = (SoftButton) en.nextElement();
			Rectangle p = b.getPaintable();
			p.y = h - p.height;
			menuh = p.height;
		}
		dd.setDisplayPaintable(new Rectangle(0, 0, w, h - menuh));
		dd.setDisplayRectangle(new Rectangle(0, 0, w, h));
	}

	/** Doi kich thuoc man hinh cua tab (game dang chay duoc bao sizeChanged). */
	public void resize(final int w, final int h) {
		width = w;
		height = h;
		if (device == null) {
			return;
		}
		Runnable r = new Runnable() {
			public void run() {
				runBound(new Runnable() {
					public void run() {
						applySize(device, w, h);
						((SwingDisplayComponent) devicePanel.getDisplayComponent()).init();
						devicePanel.init();
						MIDletContext c = MIDletBridge.getSlotContext(id);
						MIDletAccess ma = c == null ? null : c.getMIDletAccess();
						DisplayAccess da = ma == null ? null : ma.getDisplayAccess();
						if (da != null) {
							da.sizeChanged();
							DeviceDisplayImpl dd = (DeviceDisplayImpl) device.getDeviceDisplay();
							dd.repaint(0, 0, dd.getFullWidth(), dd.getFullHeight());
						}
						devicePanel.revalidate();
					}
				});
			}
		};
		if (SwingUtilities.isEventDispatchThread()) {
			r.run();
		} else {
			SwingUtilities.invokeLater(r);
		}
	}

	public int getWidthPx() {
		return width;
	}

	public int getHeightPx() {
		return height;
	}

	/** Dong game dang chay (tab tro ve trang thai Trong). */
	public void close() {
		new Thread(new Runnable() {
			public void run() {
				synchronized (LOAD_LOCK) {
					closeGameInternal();
				}
				state = State.EMPTY;
				name = "Trống";
				jarPath = null;
				fire();
			}
		}, "slot-" + id + "-close").start();
	}

	private void closeGameInternal() {
		if (common == null) {
			return;
		}
		Object t = enter();
		try {
			MIDletContext c = MIDletBridge.getSlotContext(id);
			if (c != null && c.getMIDletAccess() != null) {
				try {
					c.getMIDletAccess().destroyApp(true);
				} catch (Throwable e) {
					Logger.debug("destroyApp", e.toString());
				}
				MIDletBridge.destroyMIDletContext(c);
			}
			MIDletBridge.clear();
			MIDletBridge.removeSlot(id);
		} finally {
			exit(t);
		}
	}

	/** Duoc goi khi game tu thoat. */
	void onMidletEnded() {
		MIDletBridge.removeSlot(id);
		state = State.ENDED;
		fire();
	}

	/** Giai phong khi xoa tab. */
	public void dispose() {
		synchronized (LOAD_LOCK) {
			if (disposed) {
				return;
			}
			disposed = true;
			closeGameInternal();
		}
	}

	public boolean isRunning() {
		return state == State.RUNNING;
	}

	/** Gui vao bang cach goi truc tiep KeyListener cua man hinh thiet bi (khong can focus). */
	public void sendKey(final int vk, final char ch, final boolean press, final boolean release, final int holdMs) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if (press) {
					fireKey(java.awt.event.KeyEvent.KEY_PRESSED, vk, ch);
				}
				if (release) {
					if (press && holdMs > 0) {
						javax.swing.Timer tm = new javax.swing.Timer(holdMs, new java.awt.event.ActionListener() {
							public void actionPerformed(java.awt.event.ActionEvent e) {
								fireKey(java.awt.event.KeyEvent.KEY_RELEASED, vk, ch);
							}
						});
						tm.setRepeats(false);
						tm.start();
					} else {
						fireKey(java.awt.event.KeyEvent.KEY_RELEASED, vk, ch);
					}
				}
			}
		});
	}

	/** Goi truc tiep KeyListener (EDT) trong ngu canh tab. */
	public void fireKey(int kid, int vk, char ch) {
		java.awt.event.KeyEvent ev = new java.awt.event.KeyEvent(devicePanel, kid, System.currentTimeMillis(), 0,
				kid == java.awt.event.KeyEvent.KEY_TYPED ? java.awt.event.KeyEvent.VK_UNDEFINED : vk, ch);
		Object t = enter();
		try {
			if (Boolean.getBoolean("che779.dbg")) System.err.println("[slot " + id + "] ctx=" + MIDletBridge.getSlotContext(id) + " cur=" + MIDletBridge.getCurrentMIDlet() + " listeners=" + devicePanel.getKeyListeners().length + " da=" + (MIDletBridge.getMIDletAccess()==null?null:MIDletBridge.getMIDletAccess().getDisplayAccess()) + " cur=" + (MIDletBridge.getMIDletAccess()==null||MIDletBridge.getMIDletAccess().getDisplayAccess()==null?null:MIDletBridge.getMIDletAccess().getDisplayAccess().getCurrent()));
			for (java.awt.event.KeyListener l : devicePanel.getKeyListeners()) {
				if (kid == java.awt.event.KeyEvent.KEY_PRESSED) {
					l.keyPressed(ev);
				} else if (kid == java.awt.event.KeyEvent.KEY_TYPED) {
					l.keyTyped(ev);
				} else {
					l.keyReleased(ev);
				}
			}
			Che779.touch();
		} finally {
			exit(t);
		}
	}

	/** Go mot ky tu (pressed + typed + released). Goi tren EDT. */
	public void typeChar(char ch) {
		int vk = java.awt.event.KeyEvent.getExtendedKeyCodeForChar(ch);
		fireKey(java.awt.event.KeyEvent.KEY_PRESSED, vk, ch);
		fireKey(java.awt.event.KeyEvent.KEY_TYPED, vk, ch);
		fireKey(java.awt.event.KeyEvent.KEY_RELEASED, vk, ch);
	}

	/** Chup man hinh tab (anh man hinh game). */
	public BufferedImage snapshot() {
		int w = Math.max(1, devicePanel.getWidth());
		int h = Math.max(1, devicePanel.getHeight());
		BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		java.awt.Graphics2D g = bi.createGraphics();
		devicePanel.paint(g);
		g.dispose();
		return bi;
	}

	/** Ve lai man hinh game ngay (hien/an man hinh Sleep). */
	public void repaintGame() {
		runBound(new Runnable() {
			public void run() {
				MIDletContext c = MIDletBridge.getSlotContext(id);
				MIDletAccess ma = c == null ? null : c.getMIDletAccess();
				if (ma != null && ma.getDisplayAccess() != null) {
					ma.getDisplayAccess().repaint();
				}
			}
		});
	}
}
