package org.microemu.app.v2;

import java.io.InputStream;

import org.microemu.DisplayComponent;
import org.microemu.MIDletBridge;
import org.microemu.app.ui.Message;
import org.microemu.device.DeviceDisplay;
import org.microemu.device.EmulatorContext;
import org.microemu.device.FontManager;
import org.microemu.device.InputMethod;
import org.microemu.device.j2se.J2SEDeviceDisplay;
import org.microemu.device.j2se.J2SEFontManager;
import org.microemu.device.j2se.J2SEInputMethod;

/** Che779 V2: ngu canh thiet bi rieng cua tung tab (man hinh, ban phim, font). */
public class SlotContext implements EmulatorContext {
	private final Slot slot;
	private final InputMethod inputMethod = new J2SEInputMethod();
	private final DeviceDisplay deviceDisplay = new J2SEDeviceDisplay(this);
	private final FontManager fontManager = new J2SEFontManager();

	public SlotContext(Slot slot) {
		this.slot = slot;
	}

	public DisplayComponent getDisplayComponent() {
		return slot.devicePanel.getDisplayComponent();
	}

	public InputMethod getDeviceInputMethod() {
		return inputMethod;
	}

	public DeviceDisplay getDeviceDisplay() {
		return deviceDisplay;
	}

	public FontManager getDeviceFontManager() {
		return fontManager;
	}

	public InputStream getResourceAsStream(Class origClass, String name) {
		return MIDletBridge.getCurrentMIDlet().getClass().getResourceAsStream(name);
	}

	public boolean platformRequest(final String url) {
		new Thread(new Runnable() {
			public void run() {
				Message.info("MIDlet yêu cầu mở địa chỉ: " + url);
			}
		}).start();
		return false;
	}
}
