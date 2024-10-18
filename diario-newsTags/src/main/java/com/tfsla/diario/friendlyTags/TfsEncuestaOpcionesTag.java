package com.tfsla.diario.friendlyTags;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.JspTagException;
import jakarta.servlet.jsp.tagext.BodyTagSupport;
import jakarta.servlet.jsp.tagext.Tag;

import org.opencms.main.OpenCms;

import com.tfsla.diario.model.TfsEncuesta;
import com.tfsla.diario.model.TfsEncuestaOpcion;
import com.tfsla.opencmsdev.encuestas.Encuesta;
import com.tfsla.opencmsdev.encuestas.RespuestaEncuestaConVotos;

import edu.emory.mathcs.backport.java.util.Collections;


public class TfsEncuestaOpcionesTag  extends BodyTagSupport implements I_TfsCollectionListTag {

	/**
	 * 
	 */
	private static final long serialVersionUID = -899001963420748773L;
	
	//private int idx = -1;
	private int respuestasValidas = 0;
	private int respuestasMostradas = 0;
	//private List<String> respuestas = null;
	private String[][] respuestas = null;
	private  I_TfsEncuesta encuesta = null;

	private int[] ordenEncuestas = null;
	
	
	private void sortOpciones() {
	
		respuestasValidas = 0;
		if (respuestas!=null)
		for (int j=0; j<respuestas.length;j++)
			if((respuestas[j][0]!=null && !respuestas[j][0].trim().isEmpty()) || (respuestas[j][1]!=null && !respuestas[j][1].trim().isEmpty()))
				respuestasValidas++;

		if (respuestasValidas==0)
			return;

		//Obtengo los valores diferentes para ordenar.
		List<RespuestaEncuestaConVotos> respuestaVotos = encuesta.getResultadosEncuesta().getRespuestas();
				
		ordenEncuestas = new int[respuestasValidas];
		Integer[] votosEncuestas = new Integer[respuestasValidas];
		String[] textoOpciones = new String[respuestasValidas];
		
		int nro=0;
		for (int j=0; j<respuestas.length;j++)
			if((respuestas[j][0]!=null && !respuestas[j][0].trim().isEmpty()) || (respuestas[j][1]!=null && !respuestas[j][1].trim().isEmpty())) {
					ordenEncuestas[nro]=j;
					RespuestaEncuestaConVotos resp = null;
					if (respuestaVotos.size()>j)
						resp = respuestaVotos.get(j);
					
					votosEncuestas[nro] = 0;
					if (resp!=null) {
						votosEncuestas[nro] = resp.getCantVotos();
					}
					
					textoOpciones[nro] = respuestas[j][0];
					
					nro++;
			}
		
		if (encuesta.getEncuesta().getOrdenOpciones().equals(Encuesta.ORDEN_RANKING))
		{
			ArrayIndexComparator comparator = new ArrayIndexComparator(votosEncuestas);
			ordenEncuestas = sortOpciones(comparator,true);
		}
		else if (encuesta.getEncuesta().getOrdenOpciones().equals(Encuesta.ORDEN_ALFABETO))
		{
			ArrayIndexComparator comparator = new ArrayIndexComparator(textoOpciones);
			ordenEncuestas = sortOpciones(comparator,false);
		}
			
	}

	private int[] sortOpciones(ArrayIndexComparator comparator, boolean reverse) {
		int[] opcionesOrdenadas = new int[respuestasValidas];
		
		Integer[] indexes = comparator.createIndexArray();
		
		Comparator sortComporator = comparator;
		if (reverse)
			sortComporator = Collections.reverseOrder(sortComporator);
			
		Arrays.sort(indexes, sortComporator);

		int j=0;
		for (Integer idx : indexes){
			opcionesOrdenadas[j] = ordenEncuestas[idx];
			j++;
		}
		
		return opcionesOrdenadas;
	}
	
	@Override
	public int doStartTag() throws JspException {

		respuestasMostradas = -1;
		//idx = -1;
		encuesta = getCurrentEncuesta(); 
		 if (encuesta!=null & encuesta.getEncuesta()!=null)
			 respuestas = encuesta.getEncuesta().getRespuestas();
		 
		 sortOpciones();
		 
		 return (hasMoreContent() ? EVAL_BODY_INCLUDE : SKIP_BODY );
	}

	protected I_TfsEncuesta getCurrentEncuesta() throws JspTagException {
		// get a reference to the parent "content container" class
	    Tag ancestor = findAncestorWithClass(this, I_TfsEncuesta.class);
	    if (ancestor == null) {
	        throw new JspTagException("Tag Poll not accesible");
	    } 
	    
	    I_TfsEncuesta encuesta = (I_TfsEncuesta) ancestor;
		return encuesta;
	}
	
	private boolean hasMoreContent()
	{
		
		respuestasMostradas++;
		
		if (respuestas==null)
			return false;
		
		if (respuestas.length==0)
			return false;
		
		if (respuestasMostradas==respuestasValidas)
			return false;
		
		addOptionToContext();
		
		return true;
	}

	private void addOptionToContext()
	{
		TfsEncuesta tfsEncuesta = (TfsEncuesta)pageContext.getRequest().getAttribute("poll");
		List<RespuestaEncuestaConVotos> respuestaVotos = encuesta.getResultadosEncuesta().getRespuestas();
		RespuestaEncuestaConVotos resp = null;
		if (respuestaVotos.size()>ordenEncuestas[respuestasMostradas])
			resp = respuestaVotos.get(ordenEncuestas[respuestasMostradas]);		
		TfsEncuestaOpcion option = new TfsEncuestaOpcion(resp, respuestas[ordenEncuestas[respuestasMostradas]][0],respuestas[ordenEncuestas[respuestasMostradas]][1],respuestas[ordenEncuestas[respuestasMostradas]][2]);
		tfsEncuesta.setOption(option);
		//TfsEncuestaOpcion optionImage = new TfsEncuestaOpcion(resp, respuestas[idx][1]);
		tfsEncuesta.setOptionImage(option);
	}
	
	@Override
	public int doAfterBody() throws JspException {

		if (hasMoreContent()) {
			//addOptionToContext();
			return EVAL_BODY_AGAIN;
		}
		if (OpenCms.getSystemInfo().getServletContainerSettings().isReleaseTagsAfterEnd()) {
			release();
		}
		return SKIP_BODY;
	}

	@Override
	public int doEndTag() {

		if (OpenCms.getSystemInfo().getServletContainerSettings().isReleaseTagsAfterEnd()) {
			release();
		}
		return EVAL_PAGE;
	}

	public int getOpcionIndex() {
		return ordenEncuestas[respuestasMostradas];
	}

	public int getIndex() {
		return respuestasMostradas;
	}
	
	public boolean isLast() {
		return (respuestasMostradas+1==respuestasValidas);
	}

	public String getValue()
	{
		return respuestas[ordenEncuestas[respuestasMostradas]][0];
	}
	
	public String getImage()
	{
		return respuestas[ordenEncuestas[respuestasMostradas]][1];
	}

	public String getDescription()
	{
		return respuestas[ordenEncuestas[respuestasMostradas]][2];
	}
	
	public String getCollectionValue(String name) throws JspTagException {
		return null;
	}
	
	public String getCollectionIndexValue(String name, int index)
			throws JspTagException {
		return null;
	}

	public int getCollectionIndexSize(String name, boolean isCollectionPart) {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getCollectionPathName() throws JspTagException {
		// TODO Auto-generated method stub
		return "";
	}
	
	public class ArrayIndexComparator implements Comparator<Integer>
	{
		private final static int TYPE_STRING = 1;
		private final static int TYPE_INTEGER = 2;
	    private final String[] arrayStr;
	    private final Integer[] arrayInt;
	    
	    private final int type;
	    

	    public ArrayIndexComparator(String[] array)
	    {
	        this.arrayStr = array;
	        this.arrayInt = null;
	        type=TYPE_STRING;
	    }

	    public ArrayIndexComparator(Integer[] array)
	    {
	        this.arrayInt = array;
	        this.arrayStr = null;
	        type=TYPE_INTEGER;
	    }


		public Integer[] createIndexArray()
	    {
	    	int length = (type==TYPE_INTEGER ? arrayInt.length : arrayStr.length);
	        Integer[] indexes = new Integer[length];
	        for (int i = 0; i < length; i++)
	        {
	            indexes[i] = i; // Autoboxing
	        }
	        return indexes;
	    }

	    @Override
	    public int compare(Integer index1, Integer index2)
	    {
	    	if (type==TYPE_INTEGER)
	    		// Autounbox from Integer to int to use as array indexes
	    		return arrayInt[index1].compareTo(arrayInt[index2]);
	    	else
	    		return arrayStr[index1].compareTo(arrayStr[index2]);
	    }
	}


}
