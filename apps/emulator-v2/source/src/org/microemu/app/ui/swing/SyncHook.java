package org.microemu.app.ui.swing;

/** Che779 V2: nhan su kien phim/chuot da xu ly o mot tab de lap lai o cac tab khac. */
public interface SyncHook {
	void onEvent(java.awt.Component source, java.awt.AWTEvent event);
}
