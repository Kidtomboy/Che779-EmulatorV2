package org.microemu.app.ui.swing;

/** Che779 V2: gan thiet bi / emulator / MIDlet cua mot tab vao luong dang chay (EDT) khi xu ly su kien cua tab do. */
public interface SlotBinder {
	/** Gan ngu canh cua tab vao luong hien tai, tra ve the de khoi phuc. */
	Object enter();

	void exit(Object token);
}
