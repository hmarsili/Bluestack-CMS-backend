package org.opencms.jsp;


/**
 * Muestra el primer elemento de una contenido con cardinalidad mayor a 1
 * 
 * <code>
 * 	<tfs:first element="imagenes">
 *		<cms:contentshow element="imagen" /><br>
 *	</tfs:first>
 * </code>
 * @author lgassman
 */
public class FirstTag extends CmsJspTagContentLoop {


	private static final long serialVersionUID = 5475521407163877556L;

	private boolean hasMore = true;
	
	@Override
	public boolean hasMoreContent() {
		boolean out = this.hasMore;
		if(this.hasMore) {
			out = super.hasMoreContent();
			this.hasMore = false;
		}
		return out;
	}
	
	@Override
	public void release() {
		super.release();
		this.hasMore = true;
	}
}
