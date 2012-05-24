package org.eclipse.riena.navigation.ui.e4.rendering;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;

class FillData {

	int defaultWidth = -1, defaultHeight = -1;
	int currentWhint, currentHhint, currentWidth = -1, currentHeight = -1;

	Point computeSize(Control control, int wHint, int hHint, boolean flushCache) {
		if (flushCache)
			flushCache();
		if (wHint == SWT.DEFAULT && hHint == SWT.DEFAULT) {
			if (defaultWidth == -1 || defaultHeight == -1) {
				Point size = control.computeSize(wHint, hHint, flushCache);
				defaultWidth = size.x;
				defaultHeight = size.y;
			}
			return new Point(defaultWidth, defaultHeight);
		}
		if (currentWidth == -1 || currentHeight == -1 || wHint != currentWhint || hHint != currentHhint) {
			Point size = control.computeSize(wHint, hHint, flushCache);
			currentWhint = wHint;
			currentHhint = hHint;
			currentWidth = size.x;
			currentHeight = size.y;
		}
		return new Point(currentWidth, currentHeight);
	}

	void flushCache() {
		defaultWidth = defaultHeight = -1;
		currentWidth = currentHeight = -1;
	}
}
