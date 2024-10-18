package com.tfsla.opencmsdev.encuestas;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.jsp.JspException;

import org.opencms.file.CmsObject;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.i18n.CmsMessages;
import org.opencms.i18n.CmsMultiMessages;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsStringUtil;
import org.opencms.widgets.CmsCalendarWidget;
import org.opencms.widgets.I_CmsWidgetDialog;
import org.opencms.widgets.I_CmsWidgetParameter;
import org.opencms.workplace.editors.CmsXmlContentEditor;

import com.tfsla.collections.CollectionFactory;
import com.tfsla.exceptions.ApplicationException;
import com.tfsla.utils.StringUtils;

//TODO REFACTORME esto esta copiado de TfsContenidoEditor, salvo porque no tiene el parametro tempFile
//lo tuve que copiar porque si publicaba de nuevo el jar dejaba de andar todo por el workflow
public class TfsEncuestasContenidoEditor {

	private String url;

	private Map<String, String> values = CollectionFactory.createMap();

	private CmsJspActionElement actionElement;

	public TfsEncuestasContenidoEditor(CmsJspActionElement actionElement, String url) {
		this.url = url;
		this.actionElement = actionElement;
	}

	public TfsEncuestasContenidoEditor(CmsJspActionElement jsp, String encuestaURL, Encuesta encuesta) {
		this(jsp, encuestaURL);
		configureEditor(this, encuesta);
	}

	public static void configureEditor(TfsEncuestasContenidoEditor editor, Encuesta encuesta) {
		editor.addOpenCmsStringValue("pregunta", encuesta.getPregunta());

		editor.addOpenCmsStringValue("grupo", encuesta.getGrupo());

		editor.addOpenCmsDateValue("fechaCierre", encuesta.getFechaCierre());

		editor.addOpenCmsDateValue("fechaDespublicacion", encuesta.getFechaDespublicacion());

		editor.addOpenCmsStringValue("imagenAMostrar", encuesta.getImagenAMostrar());

		editor.addOpenCmsStringValue("textoAclaratorio", encuesta.getTextoAclaratorio());

		editor.addOpenCmsBooleanValue("respuestaExcluyente", encuesta.isRespuestaExcluyente());
		
		editor.addOpenCmsBooleanValue("usuariosRegistrados", encuesta.isUsuariosRegistrados());

		//editor.addListValue("respuesta", encuesta.getRespuestas());

		editor.addOpenCmsStringValue("estado", encuesta.getEstado());

		editor.addOpenCmsStringValue("fechaCreacion", encuesta.getFechaCreacion());

		editor.addOpenCmsStringValue("usuarioPublicador", encuesta.getUsuarioPublicador());

		editor.addOpenCmsStringValue("fechaPublicacion", encuesta.getFechaPublicacion());
		
		editor.addOpenCmsStringValue("tags", encuesta.getTags());
		
	    /* Categorias */
		int iniCat = encuesta.getcategorias().size()-1;
		int maxCat = Encuesta.MAX_CATEGORIAS;
		
		List<String> Categorias = encuesta.getcategorias();
		List<String> Subs = new ArrayList<String>();
		
		for (int i=0; i<= iniCat; i++){
			Subs.add(Categorias.get(i));
		}
		
		for (int j=iniCat; j<= maxCat; j++){
			Subs.add("");
		}
		
		editor.addListValue("categorias", Subs);

		// ***************************
		// ** optimizacion de performance
		// ***************************
		editor.addOpenCmsStringValue(Encuesta.ESTADO_GRUPO_PROPERTY, encuesta.getEstadoYGrupo());
	}

	public void save() {
		try {
			CmsXmlContentEditor editor = new CmsXmlContentEditor(new CmsJspActionElement(this.actionElement
				.getJspContext(), this.decorate(this.actionElement.getRequest()), this.actionElement.getResponse()));
			editor.actionSave();
			editor.actionClear(true);
		}
		catch (JspException e) {
			throw new ApplicationException("No se puede guardar el cambio en " + this.url, e);
		}
	}

	// por el momento no manejo la cardinalidad, si algun dia se necesita, se
	// refactoriza este metodo
	public TfsEncuestasContenidoEditor addOpenCmsStringValue(String field, String value) {
		if (value != null)
			try {
				this.values.put("OpenCmsString." + field + "_1_.0", StringUtils.encodeURL(value));
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
		return this;
	}

	public TfsEncuestasContenidoEditor addOpenCmsBooleanValue(String field, boolean value) {
		this.values.put("OpenCmsBoolean." + field + "_1_.0", value + "");
		return this;
	}

    public CmsMultiMessages retriveMessages(CmsObject cms, Locale locale) {
	    if (locale==null)
	    	locale = cms.getRequestContext().getLocale();

    	// initialize messages            
	    CmsMessages messages = OpenCms.getWorkplaceManager().getMessages(locale);
	    // generate a new multi messages object and add the messages from the workplace
	    
	    CmsMultiMessages m_messages = new CmsMultiMessages(locale);
	    m_messages.addMessages(messages);
	    
	    return m_messages;
    }

    public static String getCalendarLocalizedTime(Locale locale, CmsMessages messages, long timestamp) {

        // get the current date & time 
        TimeZone zone = TimeZone.getDefault();
        GregorianCalendar cal = new GregorianCalendar(zone, locale);
        cal.setTimeInMillis(timestamp);
        // format it nicely according to the localized pattern
        DateFormat df = new SimpleDateFormat(
            CmsCalendarWidget.getCalendarJavaDateFormat(messages.key(org.opencms.workplace.Messages.GUI_CALENDAR_DATE_FORMAT_0)
                + " "
                + messages.key(org.opencms.workplace.Messages.GUI_CALENDAR_TIME_FORMAT_0)));
        return df.format(cal.getTime());
    }

    public String getWidgetStringValue(String result) {

    	CmsObject cms = actionElement.getCmsObject();
    	
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(result) && !"0".equals(result)) {
            try {
                result = getCalendarLocalizedTime(
                	actionElement.getRequestContext().getLocale(),
                	retriveMessages(actionElement.getCmsObject(), actionElement.getRequestContext().getLocale()),
                    Long.parseLong(result));
            } catch (NumberFormatException e) {
                if (!CmsMacroResolver.isMacro(result, CmsMacroResolver.KEY_CURRENT_TIME)) {
                    // neither long nor macro, show empty value
                    result = "";
                }
            }
        } else {
            result = "";
        }
        return result;
    }

	public TfsEncuestasContenidoEditor addOpenCmsDateValue(String field, String value) {
		if (value != null) {
			try {

				String dateFormat = "M/d/yyyy hh:mm a";
				if (actionElement.getRequestContext().getLocale().getLanguage().equalsIgnoreCase("es")) {
					dateFormat = "d/M/yyyy hh:mm a";
				}

				this.values.put("OpenCmsDateTime." + field + "_1_.0", getWidgetStringValue(value));
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return this;
	}

	public TfsEncuestasContenidoEditor addOpenCmsVfsFileValue(String field, String value) {
		if (value != null) {
			try {
				this.values.put("OpenCmsVfsFile." + field + "_1_.0", StringUtils.encodeURL(value));
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return this;
	}

	public TfsEncuestasContenidoEditor addListValue(String field, List<String> values) {
		if (values != null) {

			for (int i = 1; i < values.size() + 1; i++) {
				try {
					this.values.put("OpenCmsString." + field + "_" + i + "_." + (i - 1), StringUtils.encodeURL(values
						.get(i - 1)));
				}
				catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}
		return this;
	}

	@SuppressWarnings("unchecked")
	private HttpServletRequest decorate(final HttpServletRequest request) {
		return new HttpServletRequestWrapper(request) {
			@SuppressWarnings("serial")
			private Map map = new HashMap(request.getParameterMap()) {
				public Object put(Object key, Object value) {
					return super.put(key, new String[] { (String) value });
				}

				@Override
				public Object get(Object key) {
					try {
						Object value = super.get(key);
						if (value instanceof String[] && ((String[]) value).length == 1) {
							return new String[] { StringUtils.decodeURL(((String[]) value)[0]) };
						}
						else {
							return value;
						}
					}
					catch (Exception e) {
						throw new RuntimeException("Error al decodificar el parametro del request key=[" + key
							+ "] value=[" + super.get(key) + "]", e);
					}
				}
			};
			{
				this.map.putAll(TfsEncuestasContenidoEditor.this.values);
				this.map.put("resource", TfsEncuestasContenidoEditor.this.url);

				this.map.put("elementlanguage", "en");
				this.map.put("oldelementlanguage", "en");
				this.map.put("action", "null");
				this.map.put("editastext", "null");

				this.map.put("directedit", "null");
				this.map.put("backlink", "");
				this.map.put("modified", "null");
				this.map.put("elementindex", "");

				this.map.put("elementname", "");
			}

			public Map getParameterMap() {
				return this.map;
			}
		};
	}
}