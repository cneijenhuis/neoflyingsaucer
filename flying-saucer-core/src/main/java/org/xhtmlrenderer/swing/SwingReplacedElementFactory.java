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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package org.xhtmlrenderer.swing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.xhtmlrenderer.extend.ReplacedElementFactory;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.swing.ImageReplacedElement;
import com.github.neoflyingsaucer.extend.output.FSImage;
import com.github.neoflyingsaucer.extend.output.ReplacedElement;
import com.github.neoflyingsaucer.extend.useragent.ImageResourceI;
import com.github.neoflyingsaucer.extend.useragent.Optional;
import com.github.neoflyingsaucer.extend.useragent.UserAgentCallback;

import java.util.HashMap;
import java.util.Map;

/**
 * A ReplacedElementFactory where Elements are replaced by Swing components.
 */
@Deprecated
public class SwingReplacedElementFactory implements ReplacedElementFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(SwingReplacedElementFactory.class);
    /**
     * Cache of image components (ReplacedElements) for quick lookup, keyed by Element.
     */
    protected Map<CacheKey, ReplacedElement> imageComponents;

    @Deprecated
    public SwingReplacedElementFactory() 
    {
    }

    /**
     * {@inheritDoc}
     */
    @Deprecated
    public ReplacedElement createReplacedElement(
            final LayoutContext context,
            final BlockBox box,
            final UserAgentCallback uac,
            final int cssWidth,
            final int cssHeight
    ) {
        final Element e = box.getElement();

        if (e == null) {
            return null;
        }

        if (context.getNamespaceHandler().isImageElement(e)) {
            return replaceImage(uac, context, e, cssWidth, cssHeight);
        } else {
        	return null;
        }
    }

    /**
     * Handles replacement of image elements in the document. May return the same ReplacedElement for a given image
     * on multiple calls. Image will be automatically scaled to cssWidth and cssHeight assuming these are non-zero
     * positive values. The element is assume to have a src attribute (e.g. it's an <code>img</code> element)
     *
     * @param uac       Used to retrieve images on demand from some source.
     * @param context
     * @param elem      The element with the image reference
     * @param cssWidth  Target width of the image
     * @param cssHeight Target height of the image @return A ReplacedElement for the image; will not be null.
     * @return
     */
    protected ReplacedElement replaceImage(final UserAgentCallback uac, final LayoutContext context, final Element elem, final int cssWidth, final int cssHeight) 
    {
    	ReplacedElement re = null;
        final Optional<String> oImageSrc = context.getNamespaceHandler().getImageSourceURI(elem);

        if (!oImageSrc.isPresent() || oImageSrc.get().isEmpty())
        {
        	LOGGER.warn("No source provided for img element.");
        	return newIrreplaceableImageElement(cssWidth, cssHeight);
        }

        String imageSrc = oImageSrc.get();

        // lookup in cache, or instantiate
      	// TODO: Make sure we have the correct base uri.
        Optional<String> ruri = uac.resolveURI(context.getSharedContext().getBaseURL(), imageSrc);

        if (ruri.isPresent())
        {
        	re = null;// lookupImageReplacedElement(elem, ruri.get(), cssWidth, cssHeight);
           
        	if (re == null) {
        		LOGGER.debug("Swing: Image " + ruri + " requested at " + " to " + cssWidth + ", " + cssHeight);

            	//ImageResourceI imageResource = uac.getImageResourceCache().get(ruri.get(), cssWidth, cssHeight);
            	// TODO: ImageResource may be null.
            		
        		Optional<ImageResourceI> img = uac.getImageResource(ruri.get());
        		
        		if (img.isPresent())
        		{
        			FSImage image = context.getSharedContext().resolveImage(img.get());
        			re = new ImageReplacedElement(image, cssWidth, cssHeight);
        			storeImageReplacedElement(elem, re, ruri.get(), cssWidth, cssHeight);
        		}
            }
            else
            {
            	return newIrreplaceableImageElement(cssWidth, cssHeight);
            }
        }
        return re;
    }

    private ReplacedElement lookupImageReplacedElement(final Element elem, final String ruri, final int cssWidth, final int cssHeight) {
        if (imageComponents == null) {
            return null;
        }
        final CacheKey key = new CacheKey(elem, ruri, cssWidth, cssHeight);
        return imageComponents.get(key);
    }



    /**
     * Returns a ReplacedElement for some element in the stream which should be replaceable, but is not. This might
     * be the case for an element like img, where the source isn't provided.
     *
     * @param cssWidth  Target width for the element.
     * @param cssHeight Target height for the element
     * @return A ReplacedElement to substitute for one that can't be generated.
     */
    protected ReplacedElement newIrreplaceableImageElement(final int cssWidth, final int cssHeight) {
        ReplacedElement mre;
        mre = new ImageReplacedElement(null, cssWidth, cssHeight);
        return mre;
    }

    /**
     * Adds a ReplacedElement containing an image to a cache of images for quick lookup.
     *
     * @param e   The element under which the image is keyed.
     * @param cc  The replaced element containing the image, or another ReplacedElement to be used in its place
     * @param uri
     * @param cssWidth
     * @param cssHeight
     */
    protected void storeImageReplacedElement(final Element e, final ReplacedElement cc, final String uri, final int cssWidth, final int cssHeight) {
        if (imageComponents == null) {
            imageComponents = new HashMap<CacheKey, ReplacedElement>();
        }
        final CacheKey key = new CacheKey(e, uri, cssWidth, cssHeight);
        imageComponents.put(key, cc);
    }

    /**
     * Retrieves a ReplacedElement for an image from cache, or null if not found.
     *
     * @param e   The element by which the image is keyed
     * @param uri
     * @return The ReplacedElement for the image, or null if there is none.
     */
    protected ReplacedElement lookupImageReplacedElement(final Element e, final String uri) {
        return lookupImageReplacedElement(e, uri, -1, -1);
    }

    private static class CacheKey {
        final Element elem;
        final String uri;
        final int width;
        final int height;

        public CacheKey(final Element elem, final String uri, final int width, final int height) {
            this.uri = uri;
            this.width = width;
            this.height = height;
            this.elem = elem;
        }

        public boolean equals(final Object o) {
            if (this == o) return true;
            if (!(o instanceof CacheKey)) return false;

            final CacheKey cacheKey = (CacheKey) o;

            if (height != cacheKey.height) return false;
            if (width != cacheKey.width) return false;
            if (!elem.equals(cacheKey.elem)) return false;
            if (!uri.equals(cacheKey.uri)) return false;

            return true;
        }

        public int hashCode() {
            int result = elem.hashCode();
            result = 31 * result + uri.hashCode();
            result = 31 * result + width;
            result = 31 * result + height;
            return result;
        }
    }

	@Override
	@Deprecated
	public void reset() { }

	@Override
	@Deprecated
	public void remove(Element e)
	{
        if (imageComponents != null)
            imageComponents.remove(e);
	}

}
