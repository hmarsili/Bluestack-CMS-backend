package com.tfsla.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import org.opencms.util.CmsStringUtil;

import com.tfsla.exceptions.ApplicationException;

public class DateUtils {

    public static Date getMinDate() {
        return new Date(0);
    }

    public static Date getMaxDate() {
        return new Date(Long.MAX_VALUE);
    }

    public static Calendar getCalendar(String date, DateFormat dateFormat) {
        try {
            return getCalendar(dateFormat.parse(date));
        } catch (ParseException e) {
            throw new ApplicationException("No se pudo formatear el string "
                    + date + " con el formato " + dateFormat, e);
        }
    }

    public static Calendar getCalendar(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar;
    }

    public static Calendar getMinCalendar() {
        return getCalendar(getMinDate());
    }

    public static Calendar getMaxCalendar() {
        return getCalendar(getMaxDate());
    }

    /**
     * @param fechaCierre
     * @return true si fechaCierre es anterior o igual a la fecha actual
     */
    public static boolean esAntesDeHoy(String fechaCierre) {
        long fechaCierreLong = Long.parseLong(fechaCierre);
        long fechaActualLong = Long.parseLong(today());

        return fechaCierreLong <= fechaActualLong;
    }

    public static String today() {
        return String.valueOf(new Date().getTime());
    }

    /**
     * @param fecha1
     * @param fecha2
     * @return true si fecha1 es anterior o igual a fecha2, false si no lo es
     */
    public static boolean esAnterior(String fecha1, String fecha2) {
        long fecha1Long = Long.parseLong(fecha1);
        long fecha2Long = Long.parseLong(fecha2);
        return fecha1Long <= fecha2Long;
    }

    /**
     * Metodo util para comprar fechas de OpenCMS por null, ya que a veces usa
     * cero, a veces null y aveces y string vacio para representar una fecha
     * nula...
     * 
     * @param fecha
     * @return true si la fecha es null, string vacio o string "0" (cero)
     */
    public static boolean isEmptyOrNullOrZero(String fecha) {
        return CmsStringUtil.isEmptyOrWhitespaceOnly(fecha)
                || "0".equals(fecha);
    }

}
