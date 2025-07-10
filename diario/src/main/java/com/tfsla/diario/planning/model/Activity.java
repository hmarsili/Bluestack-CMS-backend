package com.tfsla.diario.planning.model;

import org.apache.commons.logging.Log;
import org.opencms.main.CmsLog;


public class Activity {

	protected static final Log LOG = CmsLog.getLog(Activity.class);

	private String siteName; // se obtiene del json authentication
	private int publication;  // se obtiene del json authentication
	private int id;  // Autoincrementable, devuelve el back al insertar una actividad. 
	private String title; //OBLIGATORIO
	private String description;
	private String userName; //OBLIGATORIO usuario que se selecciona en el combo. (username)
	private String userCreation; //OBLIGATORIO usuario logueado en el sistema (username)
	private String color;

	private String type_recurrence; // OBLIGATORIO. Las opciones son: RECURRENCE -  DATE_EXACT - PERSONAL;
	private int personal_days; //dias  --> cuado es RECURRENCE 1 = diaria y 7 = semanal // PERSONAL cada 1, 2 ó 3 dias. 
	private String repeat_type; //--> cuando type_recurrence = PERSONAL, las opciones son: D (days) - W (week) - M (mount) - Y (year)
	private String repeat_day; // --> cuando type_recurrence = PERSONAL, las opciones son: 1(domingo), 2 (lunes), 3(martes), 4(miercoles), 5(Jueves), 6(Viernes), 7(sábado)
	private int repeat_end; // --> cuando type_recurrence = PERSONAL, las opciones son: 0 (never), 1 (date), 2(latter)
	private long start_date ; //cuando type_recurrence = DATE_EXACT ó type_recurrence = RECURRENCE ó type_recurrence = PERSONAL && repeart_end != 0.
	private int repeat_end_days; // --> cuando type_recurrence = PERSONAL, Cantidad de dias que elimian la recurrencia.
	private long date_end; //cuando type_recurrence = DATE_EXACT ó type_recurrence = RECURRENCE ó type_recurrence = PERSONAL && repeart_end != 0.
	private String type_of_month; // --> cuando type_recurrence = PERSONAL, hace referencia al tipo de recurrencia para cuando es por mes ordinalTime = por cardinalidad ej: primer lunes de cada mes ó 4to jueves de cada mes;  timeMonth = todos los 28)
	private int week_position; //cuando type_recurrence = PERSONAL && type_of_month  =ordinalTime. Hace referencia a la cardinalidad de la fecha cuando se elije por mes

	
	public String getSiteName() {
		return siteName;
	}
	public void setSiteName(String siteName) {
		this.siteName = siteName;
	}
	public int getPublication() {
		return publication;
	}
	public void setPublication(int publication) {
		this.publication = publication;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getUserCreation() {
		return userCreation;
	}
	public void setUserCreation(String userCreation) {
		this.userCreation = userCreation;
	}
	public String getColor() {
		return color;
	}
	public void setColor(String color) {
		this.color = color;
	}
	public String getType_recurrence() {
		return type_recurrence;
	}
	public void setType_recurrence(String type) {
		this.type_recurrence = type;
	}
	public int getPersonal_days() {
		return personal_days;
	}
	public void setPersonal_days(int personal_days) {
		this.personal_days = personal_days;
	}
	public String getRepeat_type() {
		return repeat_type;
	}
	public void setRepeat_type(String repeat_type) {
		this.repeat_type = repeat_type;
	}
	public String getRepeat_day() {
		return repeat_day;
	}
	public void setRepeat_day(String repeart_day) {
		this.repeat_day = repeart_day;
	}
	public int getRepeat_end() {
		return repeat_end;
	}
	public void setRepeat_end(int repeart_end) {
		this.repeat_end = repeart_end;
	}
	public long getStart_date() {
		return start_date ;
	}
	public void setStart_date(long start_date ) {
		this.start_date  = start_date ;
	}
	public int getRepeat_end_days() {
		return repeat_end_days;
	}
	public void setRepeat_end_days(int repeat_end_days) {
		this.repeat_end_days = repeat_end_days;
	}
	public long getDate_end() {
		return date_end;
	}
	public void setDate_end(long date_end) {
		this.date_end = date_end;
	}
	public String getType_of_month() {
		return type_of_month;
	}
	public void setType_of_month(String type_of_month) {
		this.type_of_month = type_of_month;
	}
	public int getWeek_position() {
		return week_position;
	}
	public void setWeek_position(int week_position) {
		this.week_position = week_position;
	}
	
	
}