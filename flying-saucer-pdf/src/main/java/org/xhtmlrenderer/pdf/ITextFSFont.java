/*
 * {{{ header & license
 * Copyright (c) 2006 Wisconsin Court System
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package org.xhtmlrenderer.pdf;

import org.xhtmlrenderer.pdf.ITextFontResolver.FontDescription;

import com.github.neoflyingsaucer.extend.output.FSFont;

public class ITextFSFont implements FSFont {
    private final FontDescription _font;
    private final float _size;
    
    public ITextFSFont(final FontDescription font, final float size) {
        _font = font;
        _size = size;
    }

    public float getSize2D() {
    	return _size;
    }
    
    public FontDescription getFontDescription() {
        return _font;
    }
}
