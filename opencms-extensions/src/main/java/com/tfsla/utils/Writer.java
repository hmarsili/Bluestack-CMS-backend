package com.tfsla.utils;

import java.io.IOException;

import jakarta.servlet.jsp.JspWriter;


import com.tfsla.tags.AbstractTag;
import com.tfsla.tags.BodyTag;

/**
 * Es un writer que no tira exceptions chequeadas 
 * @author Leo
 */
public class Writer {
	//no me puedo construirlo directamente el OUT porque no me garantiza que siempre sea el mismo
	private JspWriterContainer container;
	
	public Writer(final AbstractTag tag) {
		this.container = new JspWriterContainer(){
			public JspWriter getWriter() {
				return tag.getPageContext().getOut();
			}};
	}
	
	public Writer(final BodyTag tag) {
		this.container = new JspWriterContainer() {
			public JspWriter getWriter() {
				return tag.getBodyContent().getEnclosingWriter();
			}};
	}
	
	private JspWriter getInternal() {
		return this.container.getWriter();
	}
	

	public java.io.Writer append(char arg0) {
		try {
			return this.getInternal().append(arg0);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}




	public java.io.Writer append(CharSequence arg0, int arg1, int arg2) {
		try {
			return this.getInternal().append(arg0, arg1, arg2);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public java.io.Writer append(CharSequence arg0) {
		try {
			return this.getInternal().append(arg0);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void clear() {
		try {
			this.getInternal().clear();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void clearBuffer() {
		try {
			this.getInternal().clearBuffer();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void close() {
		try {
			this.getInternal().close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void flush() {
		try {
			this.getInternal().flush();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public int getBufferSize() {
		return this.getInternal().getBufferSize();
	}

	public int getRemaining() {
		return this.getInternal().getRemaining();
	}

	public boolean isAutoFlush() {
		return this.getInternal().isAutoFlush();
	}

	public void newLine() {
		try {
			this.getInternal().newLine();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void print(boolean arg0) {
		try {
			this.getInternal().print(arg0);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void print(char arg0) {
		try {
			this.getInternal().print(arg0);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void print(char[] arg0) {
		try {
			this.getInternal().print(arg0);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void print(double arg0) {
		try {
			this.getInternal().print(arg0);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void print(float arg0)  {
		try {
			this.getInternal().print(arg0);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void print(int arg0) {
		try {
			this.getInternal().print(arg0);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void print(long arg0)  {
		try {
			this.getInternal().print(arg0);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void print(Object arg0)  {
		try {
			this.getInternal().print(arg0);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void print(String arg0) {
		try {
			this.getInternal().print(arg0);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void println() {
		try {
			this.getInternal().println();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void println(boolean arg0) {
		try {
			this.getInternal().println(arg0);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void println(char arg0) {
		try {
			this.getInternal().println(arg0);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void println(char[] arg0) {
		try {
			this.getInternal().println(arg0);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void println(double arg0) {
		try {
			this.getInternal().println(arg0);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void println(float arg0) {
		try {
			this.getInternal().println(arg0);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void println(int arg0) {
		try {
			this.getInternal().println(arg0);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void println(long arg0) {
		try {
			this.getInternal().println(arg0);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void println(Object arg0) {
		try {
			this.getInternal().println(arg0);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void println(String arg0) {
		try {
			this.getInternal().println(arg0);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public String toString() {
		return this.getInternal().toString();
	}

	public void write(char[] arg0, int arg1, int arg2) {
		try {
			this.getInternal().write(arg0, arg1, arg2);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void write(char[] arg0) {
		try {
			this.getInternal().write(arg0);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void write(int arg0) {
		try {
			this.getInternal().write(arg0);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void write(String arg0, int arg1, int arg2) {
		try {
			this.getInternal().write(arg0, arg1, arg2);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void write(String arg0) {
		try {
			this.getInternal().write(arg0);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	


}
