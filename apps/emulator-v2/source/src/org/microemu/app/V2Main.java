package org.microemu.app;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.Box;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.ButtonGroup;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.DefaultListModel;
import java.awt.Color;
import java.awt.Font;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.microemu.app.ui.swing.SwingDeviceComponent;
import org.microemu.app.ui.swing.SwingDisplayComponent;
import org.microemu.app.ui.swing.SyncHook;
import org.microemu.app.v2.Slot;
import org.microemu.log.Logger;

/** Che779 EmulatorV2: nhieu tab game trong mot cua so (che do Tab hoac Luoi). */
public class V2Main extends Main {
	public static final List<Slot> SLOTS = new CopyOnWriteArrayList<Slot>();
	private static final Map<Slot, SlotView> VIEWS = new HashMap<Slot, SlotView>();
	private static int nextId = 1;
	private static volatile V2Main me;

	private static JPanel host;
	private static JTabbedPane tabs;
	private static JPanel grid;
	private static boolean gridMode = true;
	private static volatile Slot selected;
	static final Color BG = new Color(0x14161D), TILE = new Color(0x1C1E27), CARD = new Color(0x242733),
			ACCENT = new Color(0x00AEEF), HEAD = new Color(0x1E2A44), FG = new Color(0xE6E8EF), DIM = new Color(0x8A8FA3);
	private static volatile boolean syncAll = true;
	public static volatile boolean syncKeys = true, syncMouse = true;
	private static volatile boolean replaying = false;
	private static int defW = 176, defH = 208;

	// bo nho
	private static volatile int heapThreshold = 0; // % (0 = tat)
	private static long lastCpuTime;
	private static long lastCpuWall = System.nanoTime();

	public V2Main() {
		super();
		me = this;
		setTitle(Che779.NAME + " " + Che779.VERSION);
		setResizable(true);
		Che779.setIpActive(false);
		initLogging();
		ProxyManager.onSlotChange = new Runnable() {
			public void run() {
				saveSlotProxies();
			}
		};
		getContentPane().setBackground(BG);
		setTransferHandler(new DropHandler(null));
		((javax.swing.JComponent) getContentPane()).setTransferHandler(new DropHandler(null));
		buildMenus();
		Main.repaintAll = new Runnable() {
			public void run() {
				for (Slot s : SLOTS) {
					if (s.isRunning()) {
						s.repaintGame();
					}
				}
			}
		};
		SwingDeviceComponent.syncHook = new SyncHook() {
			public void onEvent(Component source, java.awt.AWTEvent e) {
				replay(source, e);
			}
		};
		SwingDisplayComponent.syncHook = SwingDeviceComponent.syncHook;
		startMemoryTimer();
	}

	@Override
	protected Component createContents(Container parent) {
		// Main yeu cau devicePanel; tab that nam trong cac Slot.
		devicePanel = new SwingDeviceComponent();
		host = new JPanel(new BorderLayout());
		tabs = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
		tabs.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				Component c = tabs.getSelectedComponent();
				if (c instanceof SlotView) {
					((SlotView) c).focusGame();
				}
			}
		});
		grid = new WrapGrid();
		installDrop(host, null);
		installDrop(grid, null);
		installDrop(tabs, null);
		host.setBackground(BG);
		tabs.setBackground(BG);
		tabs.setForeground(FG);
		host.add(tabs, BorderLayout.CENTER);
		return host;
	}

	/** Chon thanh phan nhan chuot (man hinh game hay khung thiet bi) tai toa do p cua devicePanel; q = toa do tuong doi. */
	static Component mouseTarget(Slot s, java.awt.Point p, java.awt.Point q) {
		Component dc = s.devicePanel.getDisplayComponent() instanceof Component ? (Component) s.devicePanel.getDisplayComponent() : null;
		if (dc != null && dc.getBounds().contains(p)) {
			q.setLocation(p.x - dc.getX(), p.y - dc.getY());
			return dc;
		}
		q.setLocation(p);
		return s.devicePanel;
	}

	/** Khung chua man hinh game: tu thu phong cho vua o tab va chuyen toa do chuot. */
	static final class ScaleHolder extends JPanel {
		final Slot slot;
		final SwingDeviceComponent dev;
		double sc = 1;
		int ox, oy, pw = 1, ph = 1;

		ScaleHolder(final Slot s) {
			super(null);
			this.slot = s;
			this.dev = s.devicePanel;
			setOpaque(true);
			setBackground(CARD);
			dev.setOpaque(false);
			dev.repaintTarget = this;
			((SwingDisplayComponent) dev.getDisplayComponent()).repaintTarget = this;
			add(dev);
			MouseAdapter ma = new MouseAdapter() {
				public void mousePressed(MouseEvent e) {
					dev.requestFocusInWindow();
					fwd(e);
				}

				public void mouseReleased(MouseEvent e) {
					fwd(e);
				}

				public void mouseDragged(MouseEvent e) {
					fwd(e);
				}

				public void mouseMoved(MouseEvent e) {
					fwd(e);
				}

				public void mouseClicked(MouseEvent e) {
					fwd(e);
				}
			};
			addMouseListener(ma);
			addMouseMotionListener(ma);
			addMouseWheelListener(new java.awt.event.MouseWheelListener() {
				public void mouseWheelMoved(java.awt.event.MouseWheelEvent e) {
					if (slot.state != Slot.State.RUNNING) {
						return;
					}
					int x = (int) Math.round((e.getX() - ox) / sc), y = (int) Math.round((e.getY() - oy) / sc);
					java.awt.Point q = new java.awt.Point();
					Component t = mouseTarget(slot, new java.awt.Point(x, y), q);
					t.dispatchEvent(new java.awt.event.MouseWheelEvent(t, e.getID(), e.getWhen(), e.getModifiers(), q.x, q.y,
							e.getClickCount(), false, e.getScrollType(), e.getScrollAmount(), e.getWheelRotation()));
				}
			});
		}

		private void calc() {
			Dimension d = dev.getPreferredSize();
			pw = Math.max(1, d.width);
			ph = Math.max(1, d.height);
			double w = Math.max(1, getWidth() - 8), h = Math.max(1, getHeight() - 8);
			sc = Math.max(0.1, Math.min(Math.min(w / pw, h / ph), 3.0));
			ox = (int) ((getWidth() - pw * sc) / 2);
			oy = (int) ((getHeight() - ph * sc) / 2);
			if (dev.getWidth() != pw || dev.getHeight() != ph || dev.getX() != 0 || dev.getY() != 0) {
				dev.setBounds(0, 0, pw, ph);
			}
		}

		public void doLayout() {
			calc();
		}

		public Dimension getPreferredSize() {
			Dimension d = dev.getPreferredSize();
			return new Dimension(d.width + 8, d.height + 8);
		}

		public Dimension getMinimumSize() {
			return new Dimension(40, 40);
		}

		protected void paintChildren(java.awt.Graphics g) {
			// man hinh game duoc ve truc tiep trong paintComponent (khong ve component con)
		}

		protected void paintComponent(java.awt.Graphics g) {
			super.paintComponent(g);
			calc();
			java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
			try {
				// he so DPI cua Windows (125% = 1.25...): tinh theo diem anh vat ly de khong bi nhoe
				double tx = Math.abs(g2.getTransform().getScaleX());
				if (tx < 0.5) {
					tx = 1;
				}
				double phys = sc * tx;
				g2.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING, java.awt.RenderingHints.VALUE_RENDER_QUALITY);
				g2.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,
						phys >= 1 ? java.awt.RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR
								: java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
				g2.translate(ox, oy);
				int k = phys > 1.001 ? (int) Math.ceil(phys - 0.001) : 1;
				((SwingDisplayComponent) dev.getDisplayComponent()).paintScaled(g2, (int) Math.round(pw * sc), (int) Math.round(ph * sc), k);
			} finally {
				g2.dispose();
			}
		}

		void fwd(MouseEvent e) {
			if (slot.state != Slot.State.RUNNING) {
				return;
			}
			int x = (int) Math.round((e.getX() - ox) / sc), y = (int) Math.round((e.getY() - oy) / sc);
			java.awt.Point q = new java.awt.Point();
			Component t = mouseTarget(slot, new java.awt.Point(x, y), q);
			t.dispatchEvent(new MouseEvent(t, e.getID(), e.getWhen(), e.getModifiers(), q.x, q.y, e.getClickCount(),
					e.isPopupTrigger(), e.getButton()));
		}
	}

	// ------------------------------------------------------------------ tab
	/** So thu tu hien thi cua tab (1..N, tu dong danh lai khi xoa/them). */
	static int num(Slot s) {
		return SLOTS.indexOf(s) + 1;
	}

	public static Slot addSlot() {
		return addSlot(-1);
	}

	/** forcedId: id noi bo giu nguyen (de du lieu RMS/proxy cua tab khong bi lech khi khoi phuc). */
	public static Slot addSlot(int forcedId) {
		if (forcedId <= 0 || forcedId >= nextId && false) {
			forcedId = nextId;
		}
		for (Slot o : SLOTS) {
			if (o.id == forcedId) {
				forcedId = nextId;
			}
		}
		nextId = Math.max(nextId, forcedId + 1);
		final Slot s = new Slot(forcedId, defW, defH);
		SLOTS.add(s);
		try {
			s.ensureDevice();
			s.initPanel();
		} catch (Throwable t) {
			Logger.error("Không tạo được tab", t);
		}
		SlotView v = new SlotView(s);
		VIEWS.put(s, v);
		s.addListener(new Slot.Listener() {
			public void slotChanged(Slot sl) {
				onSlotChanged(sl);
			}
		});
		rebuild();
		saveTabs();
		if (!gridMode) {
			tabs.setSelectedComponent(v);
		}
		return s;
	}

	static void onSlotChanged(Slot s) {
		SlotView v = VIEWS.get(s);
		if (v != null) {
			v.refresh();
			v.paintImmediately(0, 0, v.getWidth(), v.getHeight());
		}
		if (!gridMode) {
			int i = tabs.indexOfComponent(v);
			if (i >= 0) {
				tabs.setTitleAt(i, tabTitle(s));
			}
		}
		saveTabs();
	}

	static String tabTitle(Slot s) {
		String n = s.name;
		if (n != null && n.length() > 16) {
			n = n.substring(0, 15) + "…";
		}
		return "Tab " + num(s) + ": " + n;
	}

	public static void removeSlot(final Slot s) {
		if (SLOTS.size() <= 1) {
			JOptionPane.showMessageDialog(me, "Không thể xóa Tab cuối cùng! Cần duy trì ít nhất 1 Tab.", "Thông báo",
					JOptionPane.WARNING_MESSAGE);
			return;
		}
		SLOTS.remove(s);
		VIEWS.remove(s);
		ProxyManager.clearSlotProxy(s.id);
		if (selected == s) {
			selected = null;
		}
		Thread t = new Thread(new Runnable() {
			public void run() {
				s.dispose();
			}
		}, "slot-" + s.id + "-dispose");
		t.setDaemon(true);
		t.start();
		rebuild();
		saveTabs();
	}

	static void select(Slot s) {
		Slot old = selected;
		selected = s;
		if (old != null && VIEWS.get(old) != null) {
			VIEWS.get(old).paintBorder();
		}
		if (s != null && VIEWS.get(s) != null) {
			VIEWS.get(s).paintBorder();
		}
	}

	static void removeSelected() {
		Slot s = selected != null && SLOTS.contains(selected) ? selected : (SLOTS.isEmpty() ? null : SLOTS.get(SLOTS.size() - 1));
		if (s != null) {
			removeSlot(s);
		}
	}

	static void closeAllGames() {
		if (JOptionPane.showConfirmDialog(me, "Đóng tất cả game đang chạy?", "Xác nhận", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
			for (Slot s : SLOTS) {
				s.close();
			}
		}
	}

	static void resizeDialog() {
		JPanel p = new JPanel(new GridLayout(0, 2, 6, 6));
		javax.swing.JSpinner w = new javax.swing.JSpinner(new javax.swing.SpinnerNumberModel(defW, 64, 1280, 1));
		javax.swing.JSpinner h = new javax.swing.JSpinner(new javax.swing.SpinnerNumberModel(defH, 64, 1280, 1));
		p.add(new JLabel("Rộng (px):"));
		p.add(w);
		p.add(new JLabel("Cao (px):"));
		p.add(h);
		if (JOptionPane.showConfirmDialog(me, p, "Kích thước màn hình mọi tab", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
			defW = ((Number) w.getValue()).intValue();
			defH = ((Number) h.getValue()).intValue();
			for (Slot s : SLOTS) {
				s.resize(defW, defH);
			}
			saveTabs();
			host.revalidate();
		}
	}

	static void rebuild() {
		host.removeAll();
		if (gridMode) {
			int n = Math.max(1, SLOTS.size());
			int cols = (int) Math.ceil(Math.sqrt(n));
			int rows = (int) Math.ceil(n / (double) cols);
			grid.removeAll();
			for (Slot s : SLOTS) {
				grid.add(VIEWS.get(s));
			}
			JScrollPane sp = new JScrollPane(grid, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
					JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			sp.setBorder(null);
			sp.getViewport().setBackground(BG);
			sp.getVerticalScrollBar().setUnitIncrement(24);
			host.add(sp, BorderLayout.CENTER);
		} else {
			Component sel = tabs.getSelectedComponent();
			tabs.removeAll();
			for (Slot s : SLOTS) {
				SlotView v = VIEWS.get(s);
				tabs.addTab(tabTitle(s), v);
			}
			if (sel != null && tabs.indexOfComponent(sel) >= 0) {
				tabs.setSelectedComponent(sel);
			}
			host.add(tabs, BorderLayout.CENTER);
		}
		for (SlotView v : VIEWS.values()) {
			v.refresh();
		}
		host.revalidate();
		host.repaint();
		if (me != null) {
			me.refreshExtra();
		}
	}

	static Slot current() {
		if (selected != null && SLOTS.contains(selected)) {
			return selected;
		}
		if (!gridMode) {
			Component c = tabs.getSelectedComponent();
			if (c instanceof SlotView) {
				return ((SlotView) c).slot;
			}
		} else {
			Component f = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
			for (Slot s : SLOTS) {
				if (f != null && SwingUtilities.isDescendingFrom(f, VIEWS.get(s))) {
					return s;
				}
			}
		}
		return SLOTS.isEmpty() ? null : SLOTS.get(0);
	}

	static void openInto(Slot s, String path) {
		s.open(path);
	}

	static void chooseAndOpen(Slot s) {
		JFileChooser fc = new JFileChooser(lastDir());
		fc.setFileFilter(new FileNameExtensionFilter("Game Java (*.jar, *.jad)", "jar", "jad"));
		if (fc.showOpenDialog(me) == JFileChooser.APPROVE_OPTION) {
			File f = fc.getSelectedFile();
			Config.setRecentDirectory("recentJadDirectory", f.getParent());
			openInto(s, f.getAbsolutePath());
		}
	}

	private static File lastDir() {
		String d = Config.getRecentDirectory("recentJadDirectory");
		return d == null ? null : new File(d);
	}

	// ------------------------------------------------------------------ luu tab
	private static File tabsFile() {
		File d = Che779.dataDir();
		return d == null ? null : new File(d, "tabs.txt");
	}

	private static File proxyFile() {
		File d = Che779.dataDir();
		return d == null ? null : new File(d, "tab_proxies.txt");
	}

	static volatile boolean loadingProxies;

	/** Luu proxy rieng tung tab: id|D (truc tiep) hoac id|P|host|port|user|pass|type. */
	static void saveSlotProxies() {
		if (loadingProxies) {
			return;
		}
		try {
			File f = proxyFile();
			if (f == null) {
				return;
			}
			f.getParentFile().mkdirs();
			PrintWriter w = new PrintWriter(new OutputStreamWriter(new java.io.FileOutputStream(f), "UTF-8"));
			try {
				for (Slot s : SLOTS) {
					if (!ProxyManager.hasSlotSetting(s.id)) {
						continue;
					}
					ProxyManager.Proxy p = ProxyManager.slotProxyOf(s.id);
					if (p == null) {
						w.println(s.id + "|D");
					} else {
						w.println(s.id + "|P|" + p.host + "|" + p.port + "|" + p.user + "|" + p.pass + "|" + p.type);
					}
				}
			} finally {
				w.close();
			}
		} catch (Throwable t) {
			Logger.debug("saveSlotProxies", t.toString());
		}
	}

	static void loadSlotProxies() {
		File f = proxyFile();
		if (f == null || !f.exists()) {
			return;
		}
		loadingProxies = true;
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
			try {
				String line;
				while ((line = br.readLine()) != null) {
					String[] p = line.split("\\|", -1);
					if (p.length < 2) {
						continue;
					}
					int id = Integer.parseInt(p[0]);
					boolean exists = false;
					for (Slot s : SLOTS) {
						if (s.id == id) {
							exists = true;
						}
					}
					if (!exists) {
						continue;
					}
					if (p[1].equals("D")) {
						ProxyManager.setSlotProxy(id, null);
					} else if (p.length >= 7) {
						int port = Integer.parseInt(p[3]);
						ProxyManager.Proxy found = null;
						for (ProxyManager.Proxy x : ProxyManager.all()) {
							if (x.host.equals(p[2]) && x.port == port && x.user.equals(p[4])) {
								found = x;
							}
						}
						if (found == null) {
							found = new ProxyManager.Proxy();
							found.host = p[2];
							found.port = port;
							found.user = p[4];
							found.pass = p[5];
							found.type = p[6];
							ProxyManager.all().add(found);
							ProxyManager.save();
						}
						ProxyManager.setSlotProxy(id, found);
					}
				}
			} finally {
				br.close();
			}
		} catch (Throwable t) {
			Logger.debug("loadSlotProxies", t.toString());
		} finally {
			loadingProxies = false;
		}
		for (SlotView sv : VIEWS.values()) {
			sv.refresh();
		}
	}

	static void saveTabs() {
		try {
			File f = tabsFile();
			if (f == null) {
				return;
			}
			f.getParentFile().mkdirs();
			PrintWriter w = new PrintWriter(new OutputStreamWriter(new java.io.FileOutputStream(f), "UTF-8"));
			try {
				w.println("ui=2");
				w.println("grid=" + gridMode);
				w.println("size=" + defW + "x" + defH);
				for (Slot s : SLOTS) {
					w.println("tab|" + s.id + "|" + (s.jarPath == null ? "" : s.jarPath) + "|" + s.getWidthPx() + "|"
							+ s.getHeightPx());
				}
			} finally {
				w.close();
			}
		} catch (Throwable t) {
			Logger.debug("saveTabs", t.toString());
		}
	}

	static List<String[]> loadTabs() {
		List<String[]> r = new ArrayList<String[]>();
		boolean ui2 = false;
		try {
			File f = tabsFile();
			if (f == null || !f.exists()) {
				return r;
			}
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
			try {
				String line;
				while ((line = br.readLine()) != null) {
					if (line.equals("ui=2")) {
						ui2 = true;
					} else if (line.startsWith("grid=")) {
						if (ui2) {
							gridMode = line.endsWith("true");
						}
					} else if (line.startsWith("size=") && ui2) {
						String[] p = line.substring(5).split("x");
						defW = Integer.parseInt(p[0]);
						defH = Integer.parseInt(p[1]);
					} else if (line.startsWith("tab|")) {
						r.add(line.split("\\|", -1));
					}
				}
			} finally {
				br.close();
			}
		} catch (Throwable t) {
			Logger.debug("loadTabs", t.toString());
		}
		if (!ui2) {
			for (String[] t : r) {
				t[3] = String.valueOf(defW);
				t[4] = String.valueOf(defH);
			}
		}
		return r;
	}

	// ------------------------------------------------------------------ dong bo
	private static void replay(Component source, java.awt.AWTEvent e) {
		if (Boolean.getBoolean("che779.dbg") && e instanceof MouseEvent && e.getID() == MouseEvent.MOUSE_PRESSED) {
			System.out.println("ST replay? replaying=" + replaying + " syncAll=" + syncAll + " mouse=" + syncMouse + " src=" + source.getClass().getSimpleName());
		}
		if (replaying || !syncAll) {
			return;
		}
		Slot from = null;
		for (Slot s : SLOTS) {
			if (SwingUtilities.isDescendingFrom(source, s.devicePanel)) {
				from = s;
				break;
			}
		}
		if (Boolean.getBoolean("che779.dbg") && e instanceof MouseEvent && e.getID() == MouseEvent.MOUSE_PRESSED) {
			System.out.println("ST from=" + (from == null ? null : from.id + " sync=" + from.syncEnabled));
		}
		if (from == null || !from.syncEnabled) {
			return;
		}
		replaying = true;
		try {
			for (Slot to : SLOTS) {
				if (to == from || !to.syncEnabled || !to.isRunning()) {
					continue;
				}
				if (e instanceof KeyEvent && syncKeys) {
					KeyEvent k = (KeyEvent) e;
					if (k.getID() == KeyEvent.KEY_PRESSED || k.getID() == KeyEvent.KEY_RELEASED
							|| k.getID() == KeyEvent.KEY_TYPED) {
						to.fireKey(k.getID(), k.getKeyCode(), k.getKeyChar());
					}
				} else if (e instanceof MouseEvent && syncMouse) {
					MouseEvent m = (MouseEvent) e;
					java.awt.Point p = SwingUtilities.convertPoint(source, m.getPoint(), from.devicePanel);
					java.awt.Point q = new java.awt.Point();
					Component c = mouseTarget(to, p, q);
					if (m instanceof java.awt.event.MouseWheelEvent) {
						java.awt.event.MouseWheelEvent w = (java.awt.event.MouseWheelEvent) m;
						c.dispatchEvent(new java.awt.event.MouseWheelEvent(c, w.getID(), w.getWhen(), w.getModifiers(), q.x, q.y,
								w.getClickCount(), false, w.getScrollType(), w.getScrollAmount(), w.getWheelRotation()));
					} else {
						c.dispatchEvent(new MouseEvent(c, m.getID(), m.getWhen(), m.getModifiers(), q.x, q.y,
								m.getClickCount(), false, m.getButton()));
					}
				}
			}
		} catch (Throwable t) {
			Logger.debug("sync", t.toString());
			if (Boolean.getBoolean("che779.dbg")) {
				t.printStackTrace();
			}
		} finally {
			replaying = false;
		}
	}

	// ------------------------------------------------------------------ bo nho / CPU
	private void startMemoryTimer() {
		new javax.swing.Timer(2000, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				refreshExtra();
				if (heapThreshold > 0) {
					Runtime rt = Runtime.getRuntime();
					long used = rt.totalMemory() - rt.freeMemory();
					if (used * 100L / rt.maxMemory() >= heapThreshold) {
						try {
							com.che779.emu.CleanRam.run();
						} catch (Throwable t) {
						}
					}
				}
			}
		}).start();
	}

	private void refreshExtra() {
		Runtime rt = Runtime.getRuntime();
		long used = (rt.totalMemory() - rt.freeMemory()) >> 20;
		long max = rt.maxMemory() >> 20;
		double cpu = 0;
		try {
			java.lang.management.OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
			if (os instanceof com.sun.management.OperatingSystemMXBean) {
				cpu = ((com.sun.management.OperatingSystemMXBean) os).getProcessCpuLoad() * 100;
			}
		} catch (Throwable t) {
		}
		int running = 0;
		for (Slot s : SLOTS) {
			if (s.isRunning()) {
				running++;
			}
		}
		int nsync = 0;
		for (Slot s : SLOTS) {
			if (s.syncEnabled) {
				nsync++;
			}
		}
		Che779.extraStatus = "Sync: phím " + (syncKeys ? "BẬT" : "TẮT") + ", chuột " + (syncMouse ? "BẬT" : "TẮT") + " (" + nsync
				+ " tab), tab: " + running + "/" + SLOTS.size() + ", CPU " + (cpu < 0 ? 0 : (int) cpu) + "%, HEAP "
				+ used + "/" + max + "MB";
		Main.refreshStatusBar();
	}

	// ------------------------------------------------------------------ menu
	static JCheckBoxMenuItem keyMenu, mouseMenu;
	static javax.swing.JToggleButton keyBtn, mouseBtn;

	static void styleSync(javax.swing.JToggleButton b, String name, boolean on) {
		b.setText(name + (on ? ": BẬT" : ": TẮT"));
		b.setSelected(on);
		b.setOpaque(true);
		b.setContentAreaFilled(false);
		b.setBackground(on ? new Color(0x1F9D55) : new Color(0x4A4F5E));
		b.setForeground(Color.WHITE);
		b.setFont(b.getFont().deriveFont(Font.BOLD, 11f));
		b.setBorder(BorderFactory.createLineBorder(on ? new Color(0x16743F) : new Color(0x2C2F3C)));
		b.setToolTipText(name + " giữa các tab đã tích Sync: " + (on ? "đang BẬT (bấm để tắt)" : "đang TẮT (bấm để bật)"));
	}

	static void syncButtonsRefresh() {
		if (keyMenu != null) {
			keyMenu.setSelected(syncKeys);
			mouseMenu.setSelected(syncMouse);
		}
		if (keyBtn != null) {
			styleSync(keyBtn, "Phím", syncKeys);
			styleSync(mouseBtn, "Chuột", syncMouse);
		}
		for (SlotView sv : VIEWS.values()) {
			sv.refresh();
		}
		if (me != null) {
			me.refreshExtra();
		}
	}

	static void setAllSync(boolean on) {
		for (Slot s : SLOTS) {
			s.syncEnabled = on;
		}
		for (SlotView v : VIEWS.values()) {
			v.refresh();
		}
	}

	private void buildMenus() {
		menuFile.removeAll();
		menuFile.setText("File");
		JMenuItem add = new JMenuItem("Thêm Game (tab mới)");
		add.setAccelerator(javax.swing.KeyStroke.getKeyStroke(KeyEvent.VK_T, ActionEvent.CTRL_MASK));
		add.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Slot s = addSlot();
				chooseAndOpen(s);
			}
		});
		menuFile.add(add);
		JMenuItem addEmpty = new JMenuItem("Thêm tab trống");
		addEmpty.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addSlot();
			}
		});
		menuFile.add(addEmpty);
		JMenuItem open = new JMenuItem("Mở game vào tab đang chọn...");
		open.setAccelerator(javax.swing.KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
		open.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Slot s = current();
				if (s != null) {
					chooseAndOpen(s);
				}
			}
		});
		menuFile.add(open);
		JMenuItem close = new JMenuItem("Đóng game của tab");
		close.setAccelerator(javax.swing.KeyStroke.getKeyStroke(KeyEvent.VK_W, ActionEvent.CTRL_MASK));
		close.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Slot s = current();
				if (s != null) {
					s.close();
				}
			}
		});
		menuFile.add(close);
		JMenuItem del = new JMenuItem("Xóa tab");
		del.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				removeSelected();
			}
		});
		menuFile.add(del);
		JMenuItem closeAll = new JMenuItem("Đóng tất cả game");
		closeAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				for (Slot s : SLOTS) {
					s.close();
				}
			}
		});
		menuFile.add(closeAll);
		JMenuItem logItem = new JMenuItem("Xem log tab đang chọn...");
		logItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Slot s = current();
				if (s != null) {
					showLog(s);
				}
			}
		});
		menuFile.add(logItem);
		JMenuItem list = new JMenuItem("Danh sách tab...");
		list.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				StringBuilder sb = new StringBuilder();
				for (Slot s : SLOTS) {
					sb.append("Tab ").append(num(s)).append(": ").append(s.name).append("  [").append(s.state).append("]  ")
							.append(s.getWidthPx()).append("x").append(s.getHeightPx()).append('\n');
				}
				JOptionPane.showMessageDialog(V2Main.this, sb.length() == 0 ? "Chưa có tab." : sb.toString(),
						"Danh sách tab", JOptionPane.INFORMATION_MESSAGE);
			}
		});
		menuFile.add(list);
		menuFile.addSeparator();
		JMenuItem exit = new JMenuItem("Thoát");
		exit.setAccelerator(javax.swing.KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
		exit.addActionListener(menuExitListener);
		menuFile.add(exit);

		// ---- gop "Options" vao menu Che779 (V2 khong dung menu cua ban thuong)
		javax.swing.JMenuBar bar = getJMenuBar();
		bar.remove(menuOptions);
		final JCheckBoxMenuItem tabMode = new JCheckBoxMenuItem("Dạng Tab", !gridMode);
		final JCheckBoxMenuItem gridItem = new JCheckBoxMenuItem("Dạng Lưới (xem tất cả)", gridMode);
		tabMode.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				gridMode = false;
				tabMode.setSelected(true);
				gridItem.setSelected(false);
				rebuild();
				saveTabs();
			}
		});
		gridItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				gridMode = true;
				tabMode.setSelected(false);
				gridItem.setSelected(true);
				rebuild();
				saveTabs();
			}
		});
		JMenu size = new JMenu("Kích thước màn hình tab");
		int[][] presets = { { 176, 208 }, { 176, 220 }, { 240, 320 }, { 320, 240 }, { 360, 640 } };
		for (final int[] p : presets) {
			JMenuItem it = new JMenuItem(p[0] + " x " + p[1]);
			it.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					defW = p[0];
					defH = p[1];
					for (Slot s : SLOTS) {
						s.resize(p[0], p[1]);
					}
					saveTabs();
				}
			});
			size.add(it);
		}
		JMenuItem custom = new JMenuItem("Tùy chỉnh...");
		custom.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				resizeDialog();
			}
		});
		size.add(custom);

		JMenu m = menuChe779;
		m.setText("Options");
		for (int i = m.getItemCount() - 1; i >= 0; i--) {
			JMenuItem it = m.getItem(i);
			if (it != null && it.getText() != null
					&& (it.getText().startsWith("Clean RAM") || it.getText().startsWith("Quản lý Proxy"))) {
				m.remove(i);
			}
		}
		for (int i = m.getItemCount() - 1; i >= 0; i--) {
			JMenuItem it = m.getItem(i);
			if (it != null && it.getText() != null
					&& (it.getText().startsWith("Kích hoạt thay IP") || it.getText().startsWith("Thay Ip"))) {
				m.remove(i);
			}
		}
		JMenuItem pmgr = new JMenuItem("Proxy Management...");
		pmgr.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showProxyManager();
			}
		});
		m.insert(tabMode, 0);
		m.insert(gridItem, 1);
		m.insert(size, 2);
		m.insertSeparator(3);
		m.addSeparator();
		m.add(pmgr);
		m.add(menuMIDletNetworkConnection);
		m.add(menuLogConsole);
		bar.remove(m);

		// Dong bo + proxy + bo nho (menu moi)
		JMenu sync = new JMenu("Đồng bộ");
		final JCheckBoxMenuItem keyOn = new JCheckBoxMenuItem("Đồng bộ phím (đang BẬT khi có dấu ✓)", syncKeys);
		keyOn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				syncKeys = keyOn.isSelected();
				syncButtonsRefresh();
			}
		});
		final JCheckBoxMenuItem mouseOn = new JCheckBoxMenuItem("Đồng bộ chuột (đang BẬT khi có dấu ✓)", syncMouse);
		mouseOn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				syncMouse = mouseOn.isSelected();
				syncButtonsRefresh();
			}
		});
		keyMenu = keyOn;
		mouseMenu = mouseOn;
		sync.add(keyOn);
		sync.add(mouseOn);
		sync.addSeparator();
		JMenuItem allOn = new JMenuItem("Tích Sync tất cả tab");
		allOn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setAllSync(true);
			}
		});
		sync.add(allOn);
		JMenuItem allOff = new JMenuItem("Bỏ Sync tất cả tab");
		allOff.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setAllSync(false);
			}
		});
		sync.add(allOff);
		JMenuItem sel = new JMenuItem("Chọn tab tham gia đồng bộ...");
		sel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JPanel p = new JPanel(new GridLayout(0, 1));
				List<JCheckBox> boxes = new ArrayList<JCheckBox>();
				for (Slot s : SLOTS) {
					JCheckBox b = new JCheckBox("Tab " + num(s) + ": " + s.name, s.syncEnabled);
					boxes.add(b);
					p.add(b);
				}
				if (JOptionPane.showConfirmDialog(V2Main.this, p, "Đồng bộ theo tab", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
					for (int i = 0; i < boxes.size(); i++) {
						SLOTS.get(i).syncEnabled = boxes.get(i).isSelected();
					}
					for (SlotView v : VIEWS.values()) {
						v.refresh();
					}
				}
			}
		});
		sync.add(sel);
		bar.add(sync, 1);

		JMenu proxy = new JMenu("Proxy tab");
		JMenuItem pm = new JMenuItem("Proxy Management...");
		pm.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showProxyManager();
			}
		});
		proxy.add(pm);
		JMenuItem rr = new JMenuItem("Chia proxy lần lượt cho các tab");
		rr.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				List<ProxyManager.Proxy> pl = new ArrayList<ProxyManager.Proxy>(ProxyManager.all());
				if (pl.isEmpty()) {
					JOptionPane.showMessageDialog(V2Main.this,
							"Chưa có proxy. Vào Che779 > Quản lý Proxy để thêm/nhập danh sách.");
					return;
				}
				int i = 0;
				for (Slot s : SLOTS) {
					ProxyManager.setSlotProxy(s.id, pl.get(i++ % pl.size()));
				}
				Main.statusMessage("Đã chia " + pl.size() + " proxy cho " + SLOTS.size() + " tab (áp dụng cho kết nối mới)");
			}
		});
		proxy.add(rr);
		JMenuItem clr = new JMenuItem("Bỏ proxy riêng của các tab (dùng chung)");
		clr.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				for (Slot s : SLOTS) {
					ProxyManager.clearSlotProxy(s.id);
				}
			}
		});
		proxy.add(clr);
		bar.add(proxy, 2);

		JMenu mem = new JMenu("Bộ nhớ");
		JMenuItem gc = new JMenuItem("Dọn RAM ngay");
		gc.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				com.che779.emu.CleanRam.run();
				refreshExtra();
			}
		});
		mem.add(gc);
		JMenu auto = new JMenu("Tự dọn khi HEAP vượt ngưỡng");
		int[] th = { 0, 60, 70, 80, 90 };
		final List<JCheckBoxMenuItem> its = new ArrayList<JCheckBoxMenuItem>();
		for (final int t : th) {
			final JCheckBoxMenuItem it = new JCheckBoxMenuItem(t == 0 ? "Tắt" : t + "%", t == heapThreshold);
			its.add(it);
			it.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					heapThreshold = t;
					for (JCheckBoxMenuItem x : its) {
						x.setSelected(x == it);
					}
				}
			});
			auto.add(it);
		}
		mem.add(auto);
		bar.add(mem, 3);
		bar.add(m, 4);

		// thanh cong cu ben phai (giong V2 goc)
		bar.add(Box.createHorizontalGlue());
		JButton bx = flat("✕", new Color(0xD92D20));
		bx.setToolTipText("Đóng tất cả game");
		bx.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				closeAllGames();
			}
		});
		JButton bl = flat("☰", Color.DARK_GRAY);
		bl.setToolTipText("Chuyển Lưới / Tab");
		bl.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				gridMode = !gridMode;
				rebuild();
				saveTabs();
			}
		});
		JButton bp = flat("+", Color.DARK_GRAY);
		bp.setToolTipText("Thêm tab (Ctrl+T: thêm và chọn game)");
		bp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addSlot();
			}
		});
		JButton bm = flat("-", Color.DARK_GRAY);
		bm.setToolTipText("Xóa tab đang chọn");
		bm.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				removeSelected();
			}
		});
		keyBtn = new javax.swing.JToggleButton("Phím");
		mouseBtn = new javax.swing.JToggleButton("Chuột");
		for (javax.swing.JToggleButton tb : new javax.swing.JToggleButton[] { keyBtn, mouseBtn }) {
			tb.setFocusable(false);
			tb.setMargin(new Insets(0, 4, 0, 4));
			tb.setPreferredSize(new Dimension(92, 24));
			tb.setMaximumSize(new Dimension(92, 24));
		}
		styleSync(keyBtn, "Phím", syncKeys);
		styleSync(mouseBtn, "Chuột", syncMouse);
		keyBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				syncKeys = keyBtn.isSelected();
				syncButtonsRefresh();
			}
		});
		mouseBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				syncMouse = mouseBtn.isSelected();
				syncButtonsRefresh();
			}
		});
		bar.add(keyBtn);
		bar.add(Box.createHorizontalStrut(2));
		bar.add(mouseBtn);
		bar.add(Box.createHorizontalStrut(10));
		bar.add(bx);
		bar.add(Box.createHorizontalStrut(4));
		bar.add(bl);
		bar.add(Box.createHorizontalStrut(4));
		bar.add(bp);
		bar.add(Box.createHorizontalStrut(4));
		bar.add(bm);
		bar.add(Box.createHorizontalStrut(4));

		// nut Resize o thanh trang thai: doi kich thuoc man hinh moi tab
		for (ActionListener l : resizeButton.getActionListeners()) {
			resizeButton.removeActionListener(l);
		}
		resizeButton.setVisible(true);
		resizeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				resizeDialog();
			}
		});
	}

	// ------------------------------------------------------------------ log theo tab
	private static final Map<Integer, java.util.LinkedList<String>> LOGS = new HashMap<Integer, java.util.LinkedList<String>>();
	private static java.io.Writer logFile;

	static synchronized void addLog(Integer sid, String line) {
		int key = sid == null ? 0 : sid.intValue();
		java.util.LinkedList<String> l = LOGS.get(key);
		if (l == null) {
			l = new java.util.LinkedList<String>();
			LOGS.put(key, l);
		}
		l.add(line);
		while (l.size() > 300) {
			l.removeFirst();
		}
		try {
			if (logFile != null) {
				logFile.write("[tab#" + key + "] " + line + "\n");
				logFile.flush();
			}
		} catch (Throwable t) {
		}
	}

	static void initLogging() {
		try {
			File d = Che779.dataDir();
			if (d != null) {
				d.mkdirs();
				logFile = new OutputStreamWriter(new java.io.FileOutputStream(new File(d, "Che779V2.log")), "UTF-8");
			}
		} catch (Throwable t) {
		}
		Logger.addAppender(new org.microemu.log.LoggerAppender() {
			public void append(org.microemu.log.LoggingEvent e) {
				StringBuilder sb = new StringBuilder();
				sb.append(e.getLevel() >= org.microemu.log.LoggingEvent.ERROR ? "ERROR " : e.getLevel() == org.microemu.log.LoggingEvent.WARN ? "WARN " : "")
						.append(e.getMessage());
				Throwable t = e.getThrowable();
				if (t != null) {
					sb.append("\n    ").append(t);
					StackTraceElement[] st = t.getStackTrace();
					for (int i = 0; i < Math.min(st.length, 10); i++) {
						sb.append("\n        at ").append(st[i]);
					}
				}
				addLog(org.microemu.MIDletBridge.getSlotId(), sb.toString());
			}
		});
		final java.io.PrintStream oerr = System.err;
		System.setErr(new java.io.PrintStream(new java.io.OutputStream() {
			private final java.io.ByteArrayOutputStream buf = new java.io.ByteArrayOutputStream();

			public void write(int b) {
				oerr.write(b);
				if (b == '\n') {
					flushLine();
				} else {
					buf.write(b);
				}
			}

			private void flushLine() {
				try {
					String s = buf.toString("UTF-8").trim();
					buf.reset();
					if (s.length() > 0) {
						addLog(org.microemu.MIDletBridge.getSlotId(), "stderr: " + s);
					}
				} catch (Throwable t) {
				}
			}
		}, true));
	}

	static String logText(Slot s) {
		StringBuilder sb = new StringBuilder();
		synchronized (V2Main.class) {
			java.util.LinkedList<String> l = LOGS.get(s.id);
			if (l != null) {
				for (String x : l) {
					sb.append(x).append('\n');
				}
			}
		}
		if (sb.length() == 0) {
			sb.append("(chưa có log cho tab này)\n");
		}
		File d = Che779.dataDir();
		sb.append("\nFile log đầy đủ: ").append(d == null ? "?" : new File(d, "Che779V2.log").getPath());
		return sb.toString();
	}

	static void showLog(Slot s) {
		javax.swing.JTextArea ta = new javax.swing.JTextArea(logText(s), 22, 80);
		ta.setEditable(false);
		ta.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		ta.setCaretPosition(ta.getText().length());
		JOptionPane.showMessageDialog(me, new JScrollPane(ta), "Log Tab " + num(s) + " (" + s.name + ")", JOptionPane.PLAIN_MESSAGE);
	}

	// ------------------------------------------------------------------ keo tha file
	static final class DropHandler extends javax.swing.TransferHandler {
		private final Slot target;

		DropHandler(Slot target) {
			this.target = target;
		}

		public boolean canImport(TransferSupport s) {
			return s.isDataFlavorSupported(java.awt.datatransfer.DataFlavor.javaFileListFlavor);
		}

		public boolean importData(TransferSupport s) {
			if (!canImport(s)) {
				return false;
			}
			try {
				List files = (List) s.getTransferable().getTransferData(java.awt.datatransfer.DataFlavor.javaFileListFlavor);
				List<String> paths = new ArrayList<String>();
				for (Object o : files) {
					File f = (File) o;
					String n = f.getName().toLowerCase();
					if (n.endsWith(".jar") || n.endsWith(".jad")) {
						paths.add(f.getAbsolutePath());
					}
				}
				if (paths.isEmpty()) {
					return false;
				}
				dropFiles(target, paths);
				return true;
			} catch (Exception e) {
				Logger.error("Kéo thả file", e);
				return false;
			}
		}
	}

	/** Tha vao o: game dau vao o do; cac game con lai vao cac o trong ke tiep, het o trong thi them tab. */
	static void dropFiles(Slot target, List<String> paths) {
		int i = 0;
		if (target != null) {
			target.open(paths.get(i++));
		}
		while (i < paths.size()) {
			Slot s = null;
			for (Slot c : SLOTS) {
				if (c != target && c.state == Slot.State.EMPTY && c.jarPath == null) {
					s = c;
					break;
				}
			}
			if (s == null) {
				s = addSlot();
			}
			s.open(paths.get(i++));
		}
	}

	static void installDrop(Component c, Slot target) {
		if (c instanceof javax.swing.JComponent) {
			((javax.swing.JComponent) c).setTransferHandler(new DropHandler(target));
		}
	}

	// ------------------------------------------------------------------ view cua tab
	static JButton flat(String text, Color fg) {
		JButton b = new JButton(text);
		b.setFocusable(false);
		b.setMargin(new Insets(0, 0, 0, 0));
		b.setPreferredSize(new Dimension(34, 24));
		b.setForeground(fg);
		b.setFont(b.getFont().deriveFont(Font.BOLD, 13f));
		return b;
	}

	static final class SlotView extends JPanel {
		final Slot slot;
		private final JLabel title = new JLabel();
		private final JCheckBox syncB = new JCheckBox("Sync", true);
		private final JButton proxyB = new JButton("P");
		private final JButton closeB = new JButton("×");
		private final java.awt.CardLayout cards = new java.awt.CardLayout();
		private final JPanel body = new JPanel(cards);
		private final JLabel bigIcon = new JLabel("+", SwingConstants.CENTER);
		private final JLabel bigTitle = new JLabel("Thêm Game", SwingConstants.CENTER);
		private final JLabel bigSub = new JLabel("", SwingConstants.CENTER);
		private final JButton actionB = new JButton("Khởi động lại game");
		private final JButton logB = new JButton("Xem log");
		private final JPanel card = new JPanel(new java.awt.GridBagLayout());
		private final ScaleHolder holder;

		SlotView(final Slot s) {
			super(new BorderLayout());
			this.slot = s;
			setBackground(TILE);
			// ---- header
			JPanel head = new JPanel(new BorderLayout(4, 0));
			head.setBackground(TILE);
			head.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
			title.setForeground(FG);
			title.setFont(title.getFont().deriveFont(Font.BOLD, 12f));
			head.add(title, BorderLayout.CENTER);
			JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
			right.setOpaque(false);
			syncB.setOpaque(false);
			syncB.setForeground(FG);
			syncB.setFocusable(false);
			syncB.setFont(syncB.getFont().deriveFont(Font.BOLD, 11f));
			closeB.setFocusable(false);
			closeB.setMargin(new Insets(0, 0, 0, 0));
			closeB.setPreferredSize(new Dimension(22, 22));
			closeB.setToolTipText("Đóng game của tab");
			proxyB.setFocusable(false);
			proxyB.setMargin(new Insets(0, 0, 0, 0));
			proxyB.setPreferredSize(new Dimension(22, 22));
			proxyB.setFont(proxyB.getFont().deriveFont(Font.BOLD, 11f));
			proxyB.setToolTipText("Proxy cho tab này");
			right.add(syncB);
			right.add(proxyB);
			right.add(closeB);
			head.add(right, BorderLayout.EAST);
			add(head, BorderLayout.NORTH);

			// ---- the trang thai (+ Them Game ...)
			card.setBackground(CARD);
			JPanel col = new JPanel();
			col.setOpaque(false);
			col.setLayout(new javax.swing.BoxLayout(col, javax.swing.BoxLayout.Y_AXIS));
			bigIcon.setFont(bigIcon.getFont().deriveFont(Font.BOLD, 44f));
			bigIcon.setForeground(ACCENT);
			bigTitle.setForeground(FG);
			bigTitle.setFont(bigTitle.getFont().deriveFont(Font.BOLD, 14f));
			bigSub.setForeground(DIM);
			bigIcon.setAlignmentX(0.5f);
			bigTitle.setAlignmentX(0.5f);
			bigSub.setAlignmentX(0.5f);
			actionB.setAlignmentX(0.5f);
			actionB.setFocusable(false);
			logB.setAlignmentX(0.5f);
			logB.setFocusable(false);
			logB.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					showLog(s);
				}
			});
			col.add(bigIcon);
			col.add(bigTitle);
			col.add(bigSub);
			col.add(Box.createVerticalStrut(8));
			col.add(actionB);
			col.add(Box.createVerticalStrut(4));
			col.add(logB);
			card.add(col);
			holder = new ScaleHolder(s);
			body.setBackground(TILE);
			body.setBorder(BorderFactory.createEmptyBorder(0, 6, 6, 6));
			body.add(card, "card");
			body.add(holder, "game");
			add(body, BorderLayout.CENTER);

			// ---- su kien
			MouseAdapter pick = new MouseAdapter() {
				public void mousePressed(MouseEvent e) {
					select(s);
					if (s.state == Slot.State.RUNNING) {
						s.devicePanel.requestFocusInWindow();
					}
				}
			};
			addMouseListener(pick);
			card.addMouseListener(pick);
			head.addMouseListener(pick);
			holder.addMouseListener(pick);
			card.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					if (s.state == Slot.State.EMPTY) {
						chooseAndOpen(s);
					}
				}
			});
			card.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
			actionB.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (s.jarPath != null) {
						s.open(s.jarPath);
					}
				}
			});
			closeB.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					s.close();
				}
			});
			syncB.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					s.syncEnabled = syncB.isSelected();
				}
			});
			proxyB.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					showProxyMenu(s, proxyB);
				}
			});
			s.syncEnabled = false;
			syncB.setSelected(false);
			for (Component dc : new Component[] { this, card, head, body, holder, s.devicePanel }) {
				installDrop(dc, s);
			}
			paintBorder();
			refresh();
		}

		void paintBorder() {
			boolean sel = selected == slot;
			setBorder(BorderFactory.createLineBorder(sel ? ACCENT : new Color(0x2C2F3C), sel ? 2 : 1));
			title.setOpaque(sel);
			title.setBackground(HEAD);
		}

		public Dimension getPreferredSize() {
			return new Dimension(Math.max(176, slot.getWidthPx()) + 34, Math.max(150, slot.getHeightPx()) + 60);
		}

		/** Kich thuoc toi thieu de hien du man hinh game that. */
		Dimension needed() {
			return new Dimension(slot.getWidthPx() + 22, slot.getHeightPx() + 50);
		}

		public Dimension getMinimumSize() {
			return new Dimension(80, 100);
		}

		void focusGame() {
			slot.devicePanel.requestFocusInWindow();
		}

		void refresh() {
			String st;
			switch (slot.state) {
			case LOADING:
				st = "Đang nạp";
				break;
			case RUNNING:
				st = slot.name;
				break;
			case ENDED:
				st = "Đã thoát";
				break;
			case ERROR:
				st = "Lỗi";
				break;
			default:
				st = "Trống";
			}
			String shown = st != null && st.length() > 22 ? st.substring(0, 21) + "…" : st;
			title.setText("Tab " + num(slot) + ": [" + shown + "]");
			actionB.setVisible(false);
			logB.setVisible(slot.state == Slot.State.ENDED || slot.state == Slot.State.ERROR);
			switch (slot.state) {
			case RUNNING:
				cards.show(body, "game");
				break;
			case LOADING:
				bigIcon.setText("…");
				bigTitle.setText("Đang nạp game...");
				bigSub.setText(slot.name);
				cards.show(body, "card");
				break;
			case ENDED:
				bigIcon.setText("↻");
				bigTitle.setText("Game đã thoát");
				bigSub.setText(slot.name);
				actionB.setVisible(slot.jarPath != null);
				cards.show(body, "card");
				break;
			case ERROR:
				bigIcon.setText("!");
				bigTitle.setText("Tab gặp sự cố");
				String er = String.valueOf(slot.error);
				bigSub.setText("<html><div style='width:140px;text-align:center'>" + escape(er.length() > 90 ? er.substring(0, 90) : er) + "</div></html>");
				actionB.setVisible(slot.jarPath != null);
				cards.show(body, "card");
				break;
			default:
				bigIcon.setText("+");
				bigTitle.setText("Thêm Game");
				bigSub.setText("Tab " + num(slot));
				cards.show(body, "card");
			}
			bigIcon.setForeground(slot.state == Slot.State.ERROR ? new Color(0xE5484D) : ACCENT);
			closeB.setVisible(slot.state != Slot.State.EMPTY);
			syncB.setSelected(slot.syncEnabled);
			boolean eff = slot.syncEnabled && (syncKeys || syncMouse);
			syncB.setForeground(eff ? new Color(0x3DDC84) : (slot.syncEnabled ? new Color(0xE0A030) : FG));
			syncB.setToolTipText(!slot.syncEnabled ? "Tab này KHÔNG tham gia đồng bộ"
					: (eff ? "Tab này đang đồng bộ" : "Đã tích nhưng Phím và Chuột đều đang TẮT (xem nút góc phải)"));
			boolean px = ProxyManager.hasSlotSetting(slot.id) && ProxyManager.slotProxyOf(slot.id) != null;
			proxyB.setBackground(px ? ACCENT : null);
			proxyB.setForeground(px ? Color.WHITE : null);
			revalidate();
			repaint();
		}

		private static String escape(String s) {
			return s == null ? "" : s.replace("&", "&amp;").replace("<", "&lt;");
		}
	}

	/**
	 * Luoi co gian theo khung nhin: chon so cot de moi o to nhat co the (o trong co the nho dan nhu ban goc);
	 * chi hien thanh cuon doc khi khong the nho hon nua (co game dang chay can kich thuoc that).
	 */
	static final class WrapGrid extends JPanel implements Scrollable {
		private static final int GAP = 6;

		WrapGrid() {
			super(null);
			setBackground(BG);
			setLayout(new LayoutManager() {
				public void addLayoutComponent(String n, Component c) {
				}

				public void removeLayoutComponent(Component c) {
				}

				public Dimension preferredLayoutSize(Container p) {
					int w = p.getParent() != null ? p.getParent().getWidth() : 800;
					int h = p.getParent() != null ? p.getParent().getHeight() : 600;
					double[] d = fit((WrapGrid) p, w, h);
					int rows = (int) d[3];
					return new Dimension(w, (int) (GAP + rows * (d[1] + GAP)));
				}

				public Dimension minimumLayoutSize(Container p) {
					return new Dimension(100, 100);
				}

				public void layoutContainer(Container p) {
					int vw = p.getWidth();
					int vh = p.getParent() instanceof javax.swing.JViewport ? p.getParent().getHeight() : p.getHeight();
					double[] d = fit((WrapGrid) p, vw, vh);
					int cols = (int) d[2];
					int cw = (int) d[0], ch = (int) d[1];
					int i = 0;
					for (Component c : p.getComponents()) {
						int r = i / cols, k = i % cols;
						c.setBounds(GAP + k * (cw + GAP), GAP + r * (ch + GAP), cw, ch);
						i++;
					}
				}
			});
		}

		/** {cellW, cellH, cols, rows} */
		static double[] fit(WrapGrid g, int W, int H) {
			int n = Math.max(1, g.getComponentCount());
			int natW = 1, natH = 1, minW = 90, minH = 110;
			for (Component c : g.getComponents()) {
				Dimension d = c.getPreferredSize();
				natW = Math.max(natW, d.width);
				natH = Math.max(natH, d.height);

			}
			double bestScore = -1;
			int bestCols = -1;
			for (int cols = 1; cols <= n; cols++) {
				int rows = (n + cols - 1) / cols;
				double cw = (W - GAP * (cols + 1)) / (double) cols;
				double ch = (H - GAP * (rows + 1)) / (double) rows;
				if (cw < minW || ch < minH) {
					continue;
				}
				double score = Math.min(cw / natW, ch / natH);
				if (score > bestScore) {
					bestScore = score;
					bestCols = cols;
				}
			}
			if (bestCols > 0) {
				int rows = (n + bestCols - 1) / bestCols;
				return new double[] { (W - GAP * (bestCols + 1)) / (double) bestCols,
						(H - GAP * (rows + 1)) / (double) rows, bestCols, rows };
			}
			// khong vua: giu kich thuoc toi thieu, nhieu cot nhat co the, cuon doc
			int cols = Math.max(1, Math.min(n, (W - GAP) / (minW + GAP)));
			int rows = (n + cols - 1) / cols;
			double cw = (W - GAP * (cols + 1)) / (double) cols;
			return new double[] { Math.max(cw, minW), minH, cols, rows };
		}

		public Dimension getPreferredScrollableViewportSize() {
			return getPreferredSize();
		}

		public int getScrollableUnitIncrement(Rectangle r, int o, int d) {
			return 24;
		}

		public int getScrollableBlockIncrement(Rectangle r, int o, int d) {
			return Math.max(24, r.height - 24);
		}

		public boolean getScrollableTracksViewportWidth() {
			return true;
		}

		public boolean getScrollableTracksViewportHeight() {
			if (!(getParent() instanceof javax.swing.JViewport)) {
				return false;
			}
			int W = getParent().getWidth(), H = getParent().getHeight();
			double[] d = fit(this, W, H);
			return GAP + d[3] * (d[1] + GAP) <= H + 1;
		}
	}

	// ------------------------------------------------------------------ proxy
	static void showProxyMenu(final Slot s, Component anchor) {
		JPopupMenu pm = new JPopupMenu();
		JLabel t = new JLabel("  Proxy cho Tab " + num(s));
		t.setForeground(ACCENT);
		t.setFont(t.getFont().deriveFont(Font.BOLD));
		pm.add(t);
		pm.addSeparator();
		ButtonGroup g = new ButtonGroup();
		boolean has = ProxyManager.hasSlotSetting(s.id);
		ProxyManager.Proxy cur = ProxyManager.slotProxyOf(s.id);
		JRadioButtonMenuItem direct = new JRadioButtonMenuItem("Trực tiếp (Không dùng Proxy)", has && cur == null);
		direct.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ProxyManager.setSlotProxy(s.id, null);
				VIEWS.get(s).refresh();
			}
		});
		g.add(direct);
		pm.add(direct);
		JRadioButtonMenuItem shared = new JRadioButtonMenuItem("Dùng chung (theo cài đặt Che779)", !has);
		shared.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ProxyManager.clearSlotProxy(s.id);
				VIEWS.get(s).refresh();
			}
		});
		g.add(shared);
		pm.add(shared);
		List<ProxyManager.Proxy> list = new ArrayList<ProxyManager.Proxy>(ProxyManager.all());
		if (!list.isEmpty()) {
			pm.addSeparator();
		}
		for (final ProxyManager.Proxy px : list) {
			JRadioButtonMenuItem it = new JRadioButtonMenuItem(px.label() + (px.status == null ? "" : "  (" + px.status + ")"), cur == px);
			it.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					ProxyManager.setSlotProxy(s.id, px);
					VIEWS.get(s).refresh();
				}
			});
			g.add(it);
			pm.add(it);
		}
		pm.addSeparator();
		JMenuItem mg = new JMenuItem("⚙ Quản lý danh sách Proxy...");
		mg.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showProxyManager();
			}
		});
		pm.add(mg);
		pm.show(anchor, 0, anchor.getHeight());
	}

	static void showProxyManager() {
		new ProxyDialog(me).setVisible(true);
		for (SlotView v : VIEWS.values()) {
			v.refresh();
		}
	}

	static ProxyManager.Proxy parseProxyLine(String line) {
		line = line.trim();
		if (line.length() == 0 || line.startsWith("#")) {
			return null;
		}
		try {
			String user = "", pass = "", host, port;
			if (line.contains("|")) {
				return ProxyManager.parseSpec(line);
			}
			if (line.contains("@")) {
				String[] a = line.split("@", 2);
				String[] up = a[0].split(":", 2);
				user = up[0];
				pass = up.length > 1 ? up[1] : "";
				String[] hp = a[1].split(":");
				host = hp[0];
				port = hp[1];
			} else {
				String[] p = line.split(":");
				host = p[0];
				port = p[1];
				if (p.length >= 4) {
					user = p[2];
					pass = p[3];
				}
			}
			ProxyManager.Proxy x = new ProxyManager.Proxy();
			x.host = host.trim();
			x.port = Integer.parseInt(port.trim());
			x.user = user;
			x.pass = pass;
			return x.host.length() > 0 ? x : null;
		} catch (Exception e) {
			return null;
		}
	}

	static final class ProxyDialog extends JDialog {
		private final DefaultListModel model = new DefaultListModel();
		private final JList list = new JList(model);
		private final List<JComboBox> combos = new ArrayList<JComboBox>();
		private final JPanel right = new JPanel(new GridLayout(0, 2, 6, 6));

		ProxyDialog(java.awt.Frame owner) {
			super(owner, "Proxy Management", true);
			setLayout(new BorderLayout(6, 6));
			JPanel left = new JPanel(new BorderLayout(4, 4));
			left.setBorder(BorderFactory.createTitledBorder("Proxies"));
			left.add(new JScrollPane(list), BorderLayout.CENTER);
			JPanel btn = new JPanel(new GridLayout(3, 2, 4, 4));
			JButton add = new JButton("Add"), edit = new JButton("Edit"), del = new JButton("Delete"),
					chk = new JButton("Check"), imp = new JButton("Import List (.txt)");
			btn.add(add);
			btn.add(edit);
			btn.add(del);
			btn.add(chk);
			btn.add(imp);
			left.add(btn, BorderLayout.SOUTH);
			JPanel rp = new JPanel(new BorderLayout());
			rp.setBorder(BorderFactory.createTitledBorder("Gán Proxy cho từng Tab"));
			JPanel rwrap = new JPanel(new BorderLayout());
			rwrap.add(right, BorderLayout.NORTH);
			rp.add(new JScrollPane(rwrap), BorderLayout.CENTER);
			javax.swing.JSplitPane sp = new javax.swing.JSplitPane(javax.swing.JSplitPane.HORIZONTAL_SPLIT, left, rp);
			sp.setResizeWeight(0.5);
			add(sp, BorderLayout.CENTER);
			JButton close = new JButton("Close");
			JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
			south.add(close);
			add(south, BorderLayout.SOUTH);
			close.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					dispose();
				}
			});
			add.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					String v = JOptionPane.showInputDialog(ProxyDialog.this,
							"Nhập proxy: host:port  hoặc  host:port:user:pass  hoặc  user:pass@host:port", "");
					ProxyManager.Proxy p = v == null ? null : parseProxyLine(v);
					if (v != null && p == null) {
						JOptionPane.showMessageDialog(ProxyDialog.this, "Định dạng proxy không hợp lệ.");
					}
					if (p != null) {
						ProxyManager.all().add(p);
						ProxyManager.save();
						reload();
					}
				}
			});
			edit.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int i = list.getSelectedIndex();
					if (i < 0) {
						return;
					}
					ProxyManager.Proxy o = ProxyManager.all().get(i);
					String cur = o.host + ":" + o.port + (o.user.length() > 0 ? ":" + o.user + ":" + o.pass : "");
					String v = JOptionPane.showInputDialog(ProxyDialog.this, "Sửa proxy:", cur);
					ProxyManager.Proxy p = v == null ? null : parseProxyLine(v);
					if (p != null) {
						p.type = o.type;
						ProxyManager.all().set(i, p);
						ProxyManager.save();
						reload();
					}
				}
			});
			del.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int[] idx = list.getSelectedIndices();
					for (int k = idx.length - 1; k >= 0; k--) {
						ProxyManager.Proxy gone = ProxyManager.all().remove(idx[k]);
						for (Slot s : SLOTS) {
							if (ProxyManager.slotProxyOf(s.id) == gone) {
								ProxyManager.clearSlotProxy(s.id);
							}
						}
					}
					ProxyManager.save();
					reload();
				}
			});
			chk.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					final int[] idx = list.getSelectedIndices().length > 0 ? list.getSelectedIndices() : allIdx();
					new Thread(new Runnable() {
						public void run() {
							for (int i : idx) {
								if (i >= ProxyManager.all().size()) {
									continue;
								}
								ProxyManager.Proxy p = ProxyManager.all().get(i);
								p.status = "Đang kiểm tra...";
								SwingUtilities.invokeLater(new Runnable() {
									public void run() {
										reloadKeep();
									}
								});
								p.status = ProxyManager.check(p);
								SwingUtilities.invokeLater(new Runnable() {
									public void run() {
										reloadKeep();
									}
								});
							}
							ProxyManager.save();
						}
					}, "proxy-check").start();
				}
			});
			imp.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					JFileChooser fc = new JFileChooser();
					fc.setFileFilter(new FileNameExtensionFilter("Danh sách proxy (*.txt)", "txt"));
					if (fc.showOpenDialog(ProxyDialog.this) == JFileChooser.APPROVE_OPTION) {
						int n = 0;
						try {
							BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fc.getSelectedFile()), "UTF-8"));
							try {
								String ln;
								while ((ln = br.readLine()) != null) {
									ProxyManager.Proxy p = parseProxyLine(ln);
									if (p != null) {
										ProxyManager.all().add(p);
										n++;
									}
								}
							} finally {
								br.close();
							}
						} catch (Exception ex) {
							JOptionPane.showMessageDialog(ProxyDialog.this, "Không đọc được file: " + ex);
						}
						ProxyManager.save();
						reload();
						JOptionPane.showMessageDialog(ProxyDialog.this, "Đã nhập " + n + " proxy.");
					}
				}
			});
			reload();
			setSize(820, 480);
			setLocationRelativeTo(owner);
		}

		private int[] allIdx() {
			int[] r = new int[ProxyManager.all().size()];
			for (int i = 0; i < r.length; i++) {
				r[i] = i;
			}
			return r;
		}

		private void reloadKeep() {
			int[] sel = list.getSelectedIndices();
			fillList();
			list.setSelectedIndices(sel);
		}

		private void fillList() {
			model.clear();
			for (ProxyManager.Proxy p : ProxyManager.all()) {
				model.addElement(p.label() + "   —   " + (p.status == null ? "" : p.status));
			}
		}

		private void reload() {
			fillList();
			right.removeAll();
			combos.clear();
			final List<ProxyManager.Proxy> pl = new ArrayList<ProxyManager.Proxy>(ProxyManager.all());
			for (final Slot s : SLOTS) {
				right.add(new JLabel("Tab " + num(s) + (s.state == Slot.State.RUNNING ? ": " + s.name : "")));
				final JComboBox cb = new JComboBox();
				cb.addItem("(Dùng chung)");
				cb.addItem("Trực tiếp (Không Proxy)");
				for (ProxyManager.Proxy x : pl) {
					cb.addItem(x.label());
				}
				if (ProxyManager.hasSlotSetting(s.id)) {
					ProxyManager.Proxy cur = ProxyManager.slotProxyOf(s.id);
					int idx = cur == null ? 1 : pl.indexOf(cur) + 2;
					cb.setSelectedIndex(idx < 1 ? 0 : idx);
				}
				cb.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						int k = cb.getSelectedIndex();
						if (k == 0) {
							ProxyManager.clearSlotProxy(s.id);
						} else if (k == 1) {
							ProxyManager.setSlotProxy(s.id, null);
						} else if (k - 2 < pl.size()) {
							ProxyManager.setSlotProxy(s.id, pl.get(k - 2));
						}
					}
				});
				right.add(cb);
			}
			right.revalidate();
			right.repaint();
		}
	}

	// ------------------------------------------------------------------ IPC
	public static boolean ipcAddTab(String path) {
		final String p = path;
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				Slot s = addSlot();
				if (p != null && p.length() > 0) {
					s.open(p);
				}
			}
		});
		return true;
	}

	public static boolean ipcOpen(final int tab, final String path) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				for (Slot s : SLOTS) {
					if (num(s) == tab) {
						s.open(path);
						return;
					}
				}
			}
		});
		return true;
	}

	public static String ipcSlots() {
		StringBuilder sb = new StringBuilder("[");
		boolean first = true;
		for (Slot s : SLOTS) {
			if (!first) {
				sb.append(',');
			}
			first = false;
			sb.append("{\"id\":").append(num(s)).append(",\"state\":\"").append(s.state).append("\",\"name\":\"")
					.append(String.valueOf(s.name).replace("\\", "\\\\").replace("\"", "'")).append("\"}");
		}
		return sb.append(']').toString();
	}

	// ------------------------------------------------------------------ main
	public static void main(String[] args) {
		Che779.NAME = "Che779 EmulatorV2";
		Che779.APP_DIR = "Che779-EmulatorV2";
		Config.FILE_NAME = "Che779V2.xml";
		org.microemu.device.DeviceFactory.slotMode = true;
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception ex) {
			Logger.error(ex);
		}
		final List<String> params = new ArrayList<String>();
		for (String a : args) {
			params.add(a);
		}
		final V2Main app = new V2Main();
		Main.instance = app;
		final List<String[]> saved = loadTabs();
		final boolean hasArgs = !params.isEmpty();
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					for (String[] t : saved) {
						if (hasArgs) {
							break;
						}
						int sid = -1;
						try {
							sid = Integer.parseInt(t[1]);
						} catch (Throwable e) {
						}
						Slot s = addSlot(sid);
						try {
							s.resize(Integer.parseInt(t[3]), Integer.parseInt(t[4]));
						} catch (Throwable e) {
						}
						if (t[2].length() > 0 && params.isEmpty()) {
							s.open(t[2]);
						}
					}
					for (String a : params) {
						if (!a.startsWith("--")) {
							addSlot().open(a);
						}
					}
					if (SLOTS.isEmpty()) {
						for (int i = 0; i < 12; i++) {
							addSlot();
						}
					}
					rebuild();
					loadSlotProxies();
					java.awt.Dimension scr = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
					app.setSize(Math.min(scr.width - 40, Math.max(700, (defW + 24) * 5 + 40)),
							Math.min(scr.height - 80, Math.max(560, (defH + 60) * 3 + 120)));
					app.setLocationRelativeTo(null);
					app.setVisible(true);
				}
			});
		} catch (Exception e) {
			Logger.error(e);
		}
	}
}
