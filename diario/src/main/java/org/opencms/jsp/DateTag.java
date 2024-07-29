package org.opencms.jsp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Contiene el comportamiento común para todos los tags que manipulan fechas.
 * 
 * @author mpotelfeola
 */
public abstract class DateTag extends AbstractOpenCmsTag {

    private String language = "es";
    private String country = "AR";
    private String dateFormat = "yyyy-MM-dd HH:mm";

    public DateTag() {
        super();
    }

    @Override
    public int doStartTag() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(this.getDateFormat(), new Locale(this
                .getLanguage(), this.getCountry()));
        this.getWriter().print(simpleDateFormat.format(this.getDate()));
        return SKIP_BODY;
    }

    /**
     * Se debe sobreescribir este método para obtener la fecha deseada según el caso en las distintas
     * implementaciones.
     */
    protected abstract Date getDate();

    @Override
    public void release() {
        super.release();
        this.language = "es";
        this.country = "AR";
        this.dateFormat = "yyyy-MM-dd HH:mm";
    }

    public String getDateFormat() {
        return this.dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}
