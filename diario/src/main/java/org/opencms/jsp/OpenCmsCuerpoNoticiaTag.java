package org.opencms.jsp;

import java.io.IOException;
import java.util.Iterator;

import jakarta.servlet.jsp.JspException;

import com.tfsla.utils.StringUtils;
import com.tfsla.utils.StringParser;

/**
 * Separa el cuerpo de una noticia en dos partes insertando un banner en el medio. Si el cuerpo de la noticia es
 * menor que una determinada cantidad de párrafos, el banner se muestra al final. Si es mayor, se muestra esa
 * cantidad de párrafos, luego el banner entre separadores y luego se termina de imprimir el cuerpo.
 * 
 * @author mpotelfeola
 */
public class OpenCmsCuerpoNoticiaTag extends AbstractOpenCmsTag {

    /**
     * Token separador de párrafos en el cuerpo de la noticia.
     */
    private static String token = "<br><br>";
    private String cantParrafos;
    private StringParser parser;
    private String cuerpo;
    private String espacio;

    @Override
    public int doStartTag() throws JspException {
        this.cuerpo = StringUtils.normalizeBr(this.getContent());
        this.parser = StringParser.newInstance(this.getCuerpo(), token, true);

        if (this.getParser().size() <= this.getCantidadParrafos()) {
            this.imprimirCuerpoCompleto();
            this.imprimirBanner();
            return SKIP_BODY;
        }

        this.imprimirPrincipioCuerpo();
        this.imprimirBannerConSeparadores();
        this.imprimirFinCuerpo();

        return SKIP_BODY;
    }

    // ****************************************************************
    // ** Métodos Privados Helpers Para Cuando No Se Corta El Cuerpo **
    // ****************************************************************

    /**
     * Imprime el cuerpo completo de la noticia con el banner al final. Debe cerrar el div luego del cuerpo por como
     * está armado el template.
     */
    private void imprimirCuerpoCompleto() throws JspException {
        this.imprimir(this.getCuerpo());
        this.imprimir("<br><br>");
    }

    /**
     * Imprime el banner en la JSP. Sólo se imprime el banner, sin separadores.
     */
    private void imprimirBanner() throws JspException {
        this.imprimir(this.getBannerCode());
    }

    // *************************************************************
    // ** Métodos Privados Helpers Para Cuando Se Corta El Cuerpo **
    // *************************************************************

    /**
     * Imprime los primeros párrafos del cuerpo, que se mostrarán antes del banner.
     */
    private void imprimirPrincipioCuerpo() throws JspException {
        Iterator<String> iter = this.getParser().iterator();
        for (int index = 1; iter.hasNext() && index <= this.getCantidadParrafos(); index++) {
            this.imprimir(iter.next());
        }
    }

    /**
     * Imprime los últimos párrafos del cuerpo, luego del banner.
     */
    private void imprimirFinCuerpo() throws JspException {
        Iterator<String> iter = this.getParser().iterator();
        for (int index = 1; iter.hasNext(); index++) {
            if (index > this.getCantidadParrafos()) {
                this.imprimir(iter.next());
            }
            else {
                iter.next();
            }
        }
    }

    /**
     * Imprime el banner incluyendo los separadores para continuar el artículo.
     */
    private void imprimirBannerConSeparadores() throws JspException {
        this.imprimir(this.getSeparadorInicialCode());
        this.imprimirBanner();
        this.imprimir("<br><br>");
        this.imprimir(this.getSeparadorFinalCode());
    }

    // ******************************
    // ** Métodos Privados Helpers **
    // ******************************

    /**
     * Imprime un String en la JSP.
     * 
     * @param contenido
     *            String a imprimir
     */
    private void imprimir(String contenido) throws JspException {
        try {
            this.getPageContext().getOut().print(contenido);
        }
        catch (IOException e) {
            if (getLog().isErrorEnabled()) {
                getLog().error(Messages.get().getBundle().key(Messages.LOG_ERR_JSP_BEAN_0), e);
            }
            throw new JspException(e);
        }
    }

    /**
     * Devuelve un StringTokenizer que separa el cuerpo en párrafos.
     */
    private StringParser getParser() {
        return this.parser;
    }

    /**
     * Devuelve el código del banner a ser insertado en la página. REFACTORME Esto está hardcodeado con un ejemplo
     * para el prototipo. Cuando tengamos el script de OA habrá que reemplazarlo por ese c�digo.
     */
    private String getBannerCode() {
        return "<!-- banner AltaVisual -->\n" +
        "<script type=\"text/javascript\" language=\"JavaScript\">eplAD(\"" + this.getEspacio() + "\");</script>\n" +
        "<!-- fin banner AltaVisual -->\n\n";
    }

    /**
     * Devuelve el c�digo para agregar el separador inicial antes del banner.
     */
    private String getSeparadorInicialCode() {
        return "<p align=\"right\"><a href=\"#sigue\">sigue</a></p>";
    }

    /**
     * Devuelve el código para agregar el separador final luego del banner.
     */
    private String getSeparadorFinalCode() {
        return "<a name=\"sigue\" id=\"sigue\"/>";
    }

    // ***************
    // ** Accessors **
    // ***************

    public int getCantidadParrafos() {
        return new Integer(this.cantParrafos).intValue();
    }

    public String getCantParrafos() {
        return this.cantParrafos;
    }

    public void setCantParrafos(String cantParrafos) {
        this.cantParrafos = cantParrafos;
    }

    public String getCuerpo() {
        return this.cuerpo;
    }
    
    public String getEspacio() {
        return espacio;
    }

    public void setEspacio(String espacio) {
        this.espacio = espacio;
    }
    
}
