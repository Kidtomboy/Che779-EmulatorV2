package org.microemu.app.v2;

import org.microemu.MIDletContext;
import org.microemu.app.Common;
import org.microemu.device.EmulatorContext;

/** Che779 V2: bo nap MIDlet cua mot tab. */
public class SlotCommon extends Common {
	private final Slot slot;

	public SlotCommon(EmulatorContext ctx, Slot slot) {
		super(ctx);
		this.slot = slot;
		this.autoStartFirst = true;
	}

	/** Moi tab co thu muc RMS rieng (du cung 1 game cung khong lan du lieu). */
	public String getSuiteName() {
		String n = super.getSuiteName();
		return (n == null ? "game" : n) + "_tab" + slot.id;
	}

	protected boolean onMidletEnded(MIDletContext ctx) {
		slot.onMidletEnded();
		return true;
	}

	public void prepareNextLoad() {
		this.autoStartFirst = true;
		this.autoStarted = false;
	}
}
