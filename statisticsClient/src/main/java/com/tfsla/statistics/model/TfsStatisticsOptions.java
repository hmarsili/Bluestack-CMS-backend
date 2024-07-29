
/**
 * TfsStatisticsOptions.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.0  Built on : May 17, 2011 (04:21:18 IST)
 */

            
                package com.tfsla.statistics.model;

import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;

import net.sf.ehcache.Cache;

import com.tfsla.rankViews.service.TfsCacheManager;
            

            /**
            *  TfsStatisticsOptions bean class
            */
            @SuppressWarnings({"unchecked","unused"})
        
        public  class TfsStatisticsOptions
        implements org.apache.axis2.databinding.ADBBean{
        /* This type was generated from the piece of schema that had
                name = TfsStatisticsOptions
                Namespace URI = http://model.statistics.tfsla.com/xsd
                Namespace Prefix = 
                */
            

            	public static final int RANK_HITS =1;
            	public static final int RANK_COMENTARIOS =2;
            	public static final int RANK_VALORACIONES_CANTIDAD =3;
            	public static final int RANK_VALORACIONES_PROMEDIO =4;
            	public static final int RANK_VALORACIONES_POSITIVO =5;
            	public static final int RANK_VALORACIONES_NEGATIVO =6;
            	public static final int RANK_RECOMENDACION =7;

            	public static final int RANK_CUSTOM1 =12;
            	public static final int RANK_CUSTOM2 =13;
            	public static final int RANK_CUSTOM3 =14;
            	public static final int RANK_CUSTOM4 =15;
            	public static final int RANK_CUSTOM5 =16;
            	public static final int RANK_CUSTOM6 =17;
            	public static final int RANK_CUSTOM7 =18;
            	public static final int RANK_CUSTOM8 =19;
            	public static final int RANK_CUSTOM9 =20;
            	public static final int RANK_CUSTOM10 =21;

            	public static final int RANK_GENERAL =22;

            	public void enableFechaCreacion()
            	{
            		setCampoFechaRecurso("FECHACREACION");
            	}
            	public void enableFechaModificacion()
            	{
            		setCampoFechaRecurso("FECHAMODIFICACION");
            	}
            	public void enableFechaUltimaModificacion()
            	{
            		setCampoFechaRecurso("FECHAULTIMAMODIFICACION");
            	}
            	public void disableFecha()
            	{
            		localCampoFechaRecurso = null;
            		localCampoFechaRecursoTracker = false;
            	}

            	private long getCacheToleranceRangeSeconds()
            	{
            		Cache cache = null;
            		if (
            				(getUrl()!=null && !getUrl().trim().equals("")) || 
            				(getUrls()!=null && getUrls().length>0))
            			cache = TfsCacheManager.getInstance().getCache("pageStats");
            		else
            			cache = TfsCacheManager.getInstance().getCache("stats");
            		
            		if (cache==null)
            			return 0;
            		return cache.getCacheConfiguration().getTimeToLiveSeconds();
}

            	@Override
            	public int hashCode() {
            		final int PRIME = 31;
            		int result = 1;
            		result = PRIME * result + ((localAutor == null) ? 0 : localAutor.hashCode());
            		result = PRIME * result + ((localCampoFechaRecurso == null) ? 0 : localCampoFechaRecurso.hashCode());
            		result = PRIME * result + ((Integer)localEdicion).hashCode();

            		if (localFromDateRecurso != null) {
            			long l = localFromDateRecurso.getTime();
            			l = l / (10000*getCacheToleranceRangeSeconds());
            			result = PRIME * result +  (int)l ^ (int)(l >> 32);
            		}

            		if (localFrom != null) {
            			long l = localFrom.getTime();
            			l = l / (10000*getCacheToleranceRangeSeconds());
            			result = PRIME * result +  (int)l ^ (int)(l >> 32);
            		}

            		//result = PRIME * result + ((localFrom == null) ? 0 : localFrom.hashCode());

            		result = PRIME * result + ((Integer)localPage).hashCode();
            		result = PRIME * result + ((Integer)localCount).hashCode();
            		result = PRIME * result + ((Integer)localRankMode).hashCode();
            		result = PRIME * result + ((localSeccion == null) ? 0 : localSeccion.hashCode());
            		result = PRIME * result + (localShowCantidadValoracion ? 1231 : 1237);
            		result = PRIME * result + (localShowComentarios ? 1231 : 1237);
            		result = PRIME * result + (localShowHits ? 1231 : 1237);
            		result = PRIME * result + (localShowRecomendacion ? 1231 : 1237);
            		result = PRIME * result + (localShowValoracion ? 1231 : 1237);
            		result = PRIME * result + (localShowCustom1 ? 1231 : 1237);
            		result = PRIME * result + (localShowCustom2 ? 1231 : 1237);
            		result = PRIME * result + (localShowCustom3 ? 1231 : 1237);
            		result = PRIME * result + (localShowCustom4 ? 1231 : 1237);
            		result = PRIME * result + (localShowCustom5 ? 1231 : 1237);
            		result = PRIME * result + (localShowCustom6 ? 1231 : 1237);
            		result = PRIME * result + (localShowCustom7 ? 1231 : 1237);
            		result = PRIME * result + (localShowCustom8 ? 1231 : 1237);
            		result = PRIME * result + (localShowCustom9 ? 1231 : 1237);
            		result = PRIME * result + (localShowCustom10 ? 1231 : 1237);
            		result = PRIME * result + (localShowGeneralRank ? 1231 : 1237);
            		result = PRIME * result + ((localSitio == null) ? 0 : localSitio.hashCode());
            		result = PRIME * result + ((Integer)localSlotId).hashCode();
            		result = PRIME * result + Arrays.hashCode(localTags);
            		result = PRIME * result + ((localTipoContenido == null) ? 0 : localTipoContenido.hashCode());
            		result = PRIME * result + ((Integer)localTipoEdicion).hashCode();

            		if (localToDateRecurso != null) {
            			long l = localToDateRecurso.getTime();
            			l = l / (10000*getCacheToleranceRangeSeconds());
            			result = PRIME * result + (int)l ^ (int)(l >> 32);
            		}
            		if (localTo != null) {
            			long l = localTo.getTime();
            			l = l / (10000*getCacheToleranceRangeSeconds());
            			result = PRIME * result + (int)l ^ (int)(l >> 32);
            		}
            		//result = PRIME * result + ((localTo == null) ? 0 : localTo.hashCode());

            		result = PRIME * result + Arrays.hashCode(localUrls);
            		result = PRIME * result + ((localUrl == null) ? 0 : localUrl.hashCode());
            		return result;
            	}

            	@Override
            	public boolean equals(Object obj) {
            		if (this == obj)
            			return true;
            		if (obj == null)
            			return false;
            		if (getClass() != obj.getClass())
            			return false;
            		final TfsStatisticsOptions other = (TfsStatisticsOptions) obj;
            		if (localAutor == null) {
            			if (other.localAutor != null)
            				return false;
            		} else if (!localAutor.equals(other.localAutor))
            			return false;
            		if (localCampoFechaRecurso == null) {
            			if (other.localCampoFechaRecurso != null)
            				return false;
            		} else if (!localCampoFechaRecurso.equals(other.localCampoFechaRecurso))
            			return false;
            		if (localEdicion!=other.localEdicion)
            			return false;
            		if (localFrom == null) {
            			if (other.localFrom != null)
            				return false;
            			//            		} else if (!from.equals(other.from))
            		} else if (Math.abs(localFrom.getTime() - other.localFrom.getTime())>getCacheToleranceRangeSeconds()*1000)
            			return false;
            		if (localFromDateRecurso == null) {
            			if (other.localFromDateRecurso != null)
            				return false;
            			//            		} else if (!from.equals(other.from))
            		} else if (Math.abs(localFromDateRecurso.getTime() - other.localFromDateRecurso.getTime())>getCacheToleranceRangeSeconds()*1000)
            			return false;
            		if (localPage!=other.localPage)
            			return false;
            		if (localCount!=other.localCount)
            			return false;
            		if (localRankMode != other.localRankMode)
            			return false;
            		if (localSeccion == null) {
            			if (other.localSeccion != null)
            				return false;
            		} else if (!localSeccion.equals(other.localSeccion))
            			return false;
            		if (localShowCantidadValoracion != other.localShowCantidadValoracion)
            			return false;
            		if (localShowComentarios != other.localShowComentarios)
            			return false;
            		if (localShowHits != other.localShowHits)
            			return false;
            		if (localShowRecomendacion != other.localShowRecomendacion)
            			return false;
            		if (localShowValoracion != other.localShowValoracion)
            			return false;
            		if (localShowCustom1 != other.localShowCustom1)
            			return false;
            		if (localShowCustom2 != other.localShowCustom2)
            			return false;
            		if (localShowCustom3 != other.localShowCustom3)
            			return false;
            		if (localShowCustom4 != other.localShowCustom4)
            			return false;
            		if (localShowCustom5 != other.localShowCustom5)
            			return false;
            		if (localShowCustom6 != other.localShowCustom6)
            			return false;
            		if (localShowCustom7 != other.localShowCustom7)
            			return false;
            		if (localShowCustom8 != other.localShowCustom8)
            			return false;
            		if (localShowCustom9 != other.localShowCustom9)
            			return false;
            		if (localShowCustom10 != other.localShowCustom10)
            			return false;
            		if (localShowGeneralRank != other.localShowGeneralRank)
            			return false;
            		if (localSitio == null) {
            			if (other.localSitio != null)
            				return false;
            		} else if (!localSitio.equals(other.localSitio))
            			return false;
            		if (localSlotId != other.localSlotId)
            			return false;
            		if (!Arrays.equals(localTags, other.localTags))
            			return false;
            		if (localTipoContenido == null) {
            			if (other.localTipoContenido != null)
            				return false;
            		} else if (!localTipoContenido.equals(other.localTipoContenido))
            			return false;
            		if (localTipoEdicion!=other.localTipoEdicion)
            			return false;
            		if (localTo == null) {
            			if (other.localTo != null)
            				return false;
            			//            		} else if (!to.equals(other.to))
            		} else if (Math.abs(localTo.getTime() - other.localTo.getTime())>getCacheToleranceRangeSeconds()*1000)
            			return false;
            		if (localToDateRecurso == null) {
            			if (other.localToDateRecurso != null)
            				return false;
            			//            		} else if (!from.equals(other.from))
            		} else if (Math.abs(localToDateRecurso.getTime() - other.localToDateRecurso.getTime())>getCacheToleranceRangeSeconds()*1000)
            			return false;

            		if (localUrl == null) {
            			if (other.localUrl != null)
            				return false;
            		} else if (!localUrl.equals(other.localUrl))
            			return false;
            		if (!Arrays.equals(localUrls, other.localUrls))
            			return false;

            		return true;
            	}

                        /**
                        * field for Autor
                        */

                        
                                    protected java.lang.String localAutor ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localAutorTracker = false ;

                           public boolean isAutorSpecified(){
                               return localAutorTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getAutor(){
                               return localAutor;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Autor
                               */
                               public void setAutor(java.lang.String param){
                            localAutorTracker = true;
                                   
                                            this.localAutor=param;
                                    

                               }
                            

                        /**
                        * field for CampoFechaRecurso
                        */

                        
                                    protected java.lang.String localCampoFechaRecurso ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localCampoFechaRecursoTracker = false ;

                           public boolean isCampoFechaRecursoSpecified(){
                               return localCampoFechaRecursoTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getCampoFechaRecurso(){
                               return localCampoFechaRecurso;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param CampoFechaRecurso
                               */
                               public void setCampoFechaRecurso(java.lang.String param){
                            localCampoFechaRecursoTracker = true;
                                   
                                            this.localCampoFechaRecurso=param;
                                    

                               }
                            

                        /**
                        * field for Count
                        */

                        
                                    protected int localCount ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localCountTracker = false ;

                           public boolean isCountSpecified(){
                               return localCountTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return int
                           */
                           public  int getCount(){
                               return localCount;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Count
                               */
                               public void setCount(int param){
                            
                                       // setting primitive attribute tracker to true
                                       localCountTracker =
                                       param != java.lang.Integer.MIN_VALUE;
                                   
                                            this.localCount=param;
                                    

                               }
                            

                        /**
                        * field for Edicion
                        */

                        
                                    protected int localEdicion ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localEdicionTracker = false ;

                           public boolean isEdicionSpecified(){
                               return localEdicionTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return int
                           */
                           public  int getEdicion(){
                               return localEdicion;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Edicion
                               */
                               public void setEdicion(int param){
                            
                                       // setting primitive attribute tracker to true
                                       localEdicionTracker =
                                       param != java.lang.Integer.MIN_VALUE;
                                   
                                            this.localEdicion=param;
                                    

                               }
                            

                        /**
                        * field for From
                        */

                        
                                    protected java.util.Date localFrom ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localFromTracker = false ;

                           public boolean isFromSpecified(){
                               return localFromTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.util.Date
                           */
                           public  java.util.Date getFrom(){
                               return localFrom;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param From
                               */
                               public void setFrom(java.util.Date param){
                            localFromTracker = true;
                                   
                                            this.localFrom=param;
                                    

                               }
                            

                        /**
                        * field for FromDateRecurso
                        */

                        
                                    protected java.util.Date localFromDateRecurso ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localFromDateRecursoTracker = false ;

                           public boolean isFromDateRecursoSpecified(){
                               return localFromDateRecursoTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.util.Date
                           */
                           public  java.util.Date getFromDateRecurso(){
                               return localFromDateRecurso;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param FromDateRecurso
                               */
                               public void setFromDateRecurso(java.util.Date param){
                            localFromDateRecursoTracker = true;
                                   
                                            this.localFromDateRecurso=param;
                                    

                               }
                            

                        /**
                        * field for Page
                        */

                        
                                    protected int localPage ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localPageTracker = false ;

                           public boolean isPageSpecified(){
                               return localPageTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return int
                           */
                           public  int getPage(){
                               return localPage;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Page
                               */
                               public void setPage(int param){
                            
                                       // setting primitive attribute tracker to true
                                       localPageTracker =
                                       param != java.lang.Integer.MIN_VALUE;
                                   
                                            this.localPage=param;
                                    

                               }
                            

                        /**
                        * field for RankMode
                        */

                        
                                    protected int localRankMode ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localRankModeTracker = false ;

                           public boolean isRankModeSpecified(){
                               return localRankModeTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return int
                           */
                           public  int getRankMode(){
                               return localRankMode;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param RankMode
                               */
                               public void setRankMode(int param){
                            
                                       // setting primitive attribute tracker to true
                                       localRankModeTracker =
                                       param != java.lang.Integer.MIN_VALUE;
                                   
                                            this.localRankMode=param;
                                    

                               }
                            

                        /**
                        * field for Seccion
                        */

                        
                                    protected java.lang.String localSeccion ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localSeccionTracker = false ;

                           public boolean isSeccionSpecified(){
                               return localSeccionTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getSeccion(){
                               return localSeccion;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Seccion
                               */
                               public void setSeccion(java.lang.String param){
                            localSeccionTracker = true;
                                   
                                            this.localSeccion=param;
                                    

                               }
                            

                        /**
                        * field for ShowCantidadValoracion
                        */

                        
                                    protected boolean localShowCantidadValoracion ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localShowCantidadValoracionTracker = false ;

                           public boolean isShowCantidadValoracionSpecified(){
                               return localShowCantidadValoracionTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return boolean
                           */
                           public  boolean getShowCantidadValoracion(){
                               return localShowCantidadValoracion;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ShowCantidadValoracion
                               */
                               public void setShowCantidadValoracion(boolean param){
                            
                                       // setting primitive attribute tracker to true
                                       localShowCantidadValoracionTracker =
                                       true;
                                   
                                            this.localShowCantidadValoracion=param;
                                    

                               }
                            

                        /**
                        * field for ShowComentarios
                        */

                        
                                    protected boolean localShowComentarios ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localShowComentariosTracker = false ;

                           public boolean isShowComentariosSpecified(){
                               return localShowComentariosTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return boolean
                           */
                           public  boolean getShowComentarios(){
                               return localShowComentarios;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ShowComentarios
                               */
                               public void setShowComentarios(boolean param){
                            
                                       // setting primitive attribute tracker to true
                                       localShowComentariosTracker =
                                       true;
                                   
                                            this.localShowComentarios=param;
                                    

                               }
                            

                        /**
                        * field for ShowCustom1
                        */

                        
                                    protected boolean localShowCustom1 ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localShowCustom1Tracker = false ;

                           public boolean isShowCustom1Specified(){
                               return localShowCustom1Tracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return boolean
                           */
                           public  boolean getShowCustom1(){
                               return localShowCustom1;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ShowCustom1
                               */
                               public void setShowCustom1(boolean param){
                            
                                       // setting primitive attribute tracker to true
                                       localShowCustom1Tracker =
                                       true;
                                   
                                            this.localShowCustom1=param;
                                    

                               }
                            

                        /**
                        * field for ShowCustom10
                        */

                        
                                    protected boolean localShowCustom10 ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localShowCustom10Tracker = false ;

                           public boolean isShowCustom10Specified(){
                               return localShowCustom10Tracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return boolean
                           */
                           public  boolean getShowCustom10(){
                               return localShowCustom10;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ShowCustom10
                               */
                               public void setShowCustom10(boolean param){
                            
                                       // setting primitive attribute tracker to true
                                       localShowCustom10Tracker =
                                       true;
                                   
                                            this.localShowCustom10=param;
                                    

                               }
                            

                        /**
                        * field for ShowCustom2
                        */

                        
                                    protected boolean localShowCustom2 ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localShowCustom2Tracker = false ;

                           public boolean isShowCustom2Specified(){
                               return localShowCustom2Tracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return boolean
                           */
                           public  boolean getShowCustom2(){
                               return localShowCustom2;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ShowCustom2
                               */
                               public void setShowCustom2(boolean param){
                            
                                       // setting primitive attribute tracker to true
                                       localShowCustom2Tracker =
                                       true;
                                   
                                            this.localShowCustom2=param;
                                    

                               }
                            

                        /**
                        * field for ShowCustom3
                        */

                        
                                    protected boolean localShowCustom3 ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localShowCustom3Tracker = false ;

                           public boolean isShowCustom3Specified(){
                               return localShowCustom3Tracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return boolean
                           */
                           public  boolean getShowCustom3(){
                               return localShowCustom3;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ShowCustom3
                               */
                               public void setShowCustom3(boolean param){
                            
                                       // setting primitive attribute tracker to true
                                       localShowCustom3Tracker =
                                       true;
                                   
                                            this.localShowCustom3=param;
                                    

                               }
                            

                        /**
                        * field for ShowCustom4
                        */

                        
                                    protected boolean localShowCustom4 ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localShowCustom4Tracker = false ;

                           public boolean isShowCustom4Specified(){
                               return localShowCustom4Tracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return boolean
                           */
                           public  boolean getShowCustom4(){
                               return localShowCustom4;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ShowCustom4
                               */
                               public void setShowCustom4(boolean param){
                            
                                       // setting primitive attribute tracker to true
                                       localShowCustom4Tracker =
                                       true;
                                   
                                            this.localShowCustom4=param;
                                    

                               }
                            

                        /**
                        * field for ShowCustom5
                        */

                        
                                    protected boolean localShowCustom5 ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localShowCustom5Tracker = false ;

                           public boolean isShowCustom5Specified(){
                               return localShowCustom5Tracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return boolean
                           */
                           public  boolean getShowCustom5(){
                               return localShowCustom5;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ShowCustom5
                               */
                               public void setShowCustom5(boolean param){
                            
                                       // setting primitive attribute tracker to true
                                       localShowCustom5Tracker =
                                       true;
                                   
                                            this.localShowCustom5=param;
                                    

                               }
                            

                        /**
                        * field for ShowCustom6
                        */

                        
                                    protected boolean localShowCustom6 ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localShowCustom6Tracker = false ;

                           public boolean isShowCustom6Specified(){
                               return localShowCustom6Tracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return boolean
                           */
                           public  boolean getShowCustom6(){
                               return localShowCustom6;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ShowCustom6
                               */
                               public void setShowCustom6(boolean param){
                            
                                       // setting primitive attribute tracker to true
                                       localShowCustom6Tracker =
                                       true;
                                   
                                            this.localShowCustom6=param;
                                    

                               }
                            

                        /**
                        * field for ShowCustom7
                        */

                        
                                    protected boolean localShowCustom7 ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localShowCustom7Tracker = false ;

                           public boolean isShowCustom7Specified(){
                               return localShowCustom7Tracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return boolean
                           */
                           public  boolean getShowCustom7(){
                               return localShowCustom7;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ShowCustom7
                               */
                               public void setShowCustom7(boolean param){
                            
                                       // setting primitive attribute tracker to true
                                       localShowCustom7Tracker =
                                       true;
                                   
                                            this.localShowCustom7=param;
                                    

                               }
                            

                        /**
                        * field for ShowCustom8
                        */

                        
                                    protected boolean localShowCustom8 ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localShowCustom8Tracker = false ;

                           public boolean isShowCustom8Specified(){
                               return localShowCustom8Tracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return boolean
                           */
                           public  boolean getShowCustom8(){
                               return localShowCustom8;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ShowCustom8
                               */
                               public void setShowCustom8(boolean param){
                            
                                       // setting primitive attribute tracker to true
                                       localShowCustom8Tracker =
                                       true;
                                   
                                            this.localShowCustom8=param;
                                    

                               }
                            

                        /**
                        * field for ShowCustom9
                        */

                        
                                    protected boolean localShowCustom9 ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localShowCustom9Tracker = false ;

                           public boolean isShowCustom9Specified(){
                               return localShowCustom9Tracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return boolean
                           */
                           public  boolean getShowCustom9(){
                               return localShowCustom9;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ShowCustom9
                               */
                               public void setShowCustom9(boolean param){
                            
                                       // setting primitive attribute tracker to true
                                       localShowCustom9Tracker =
                                       true;
                                   
                                            this.localShowCustom9=param;
                                    

                               }
                            

                        /**
                        * field for ShowGeneralRank
                        */

                        
                                    protected boolean localShowGeneralRank ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localShowGeneralRankTracker = false ;

                           public boolean isShowGeneralRankSpecified(){
                               return localShowGeneralRankTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return boolean
                           */
                           public  boolean getShowGeneralRank(){
                               return localShowGeneralRank;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ShowGeneralRank
                               */
                               public void setShowGeneralRank(boolean param){
                            
                                       // setting primitive attribute tracker to true
                                       localShowGeneralRankTracker =
                                       true;
                                   
                                            this.localShowGeneralRank=param;
                                    

                               }
                            

                        /**
                        * field for ShowHits
                        */

                        
                                    protected boolean localShowHits ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localShowHitsTracker = false ;

                           public boolean isShowHitsSpecified(){
                               return localShowHitsTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return boolean
                           */
                           public  boolean getShowHits(){
                               return localShowHits;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ShowHits
                               */
                               public void setShowHits(boolean param){
                            
                                       // setting primitive attribute tracker to true
                                       localShowHitsTracker =
                                       true;
                                   
                                            this.localShowHits=param;
                                    

                               }
                            

                        /**
                        * field for ShowRecomendacion
                        */

                        
                                    protected boolean localShowRecomendacion ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localShowRecomendacionTracker = false ;

                           public boolean isShowRecomendacionSpecified(){
                               return localShowRecomendacionTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return boolean
                           */
                           public  boolean getShowRecomendacion(){
                               return localShowRecomendacion;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ShowRecomendacion
                               */
                               public void setShowRecomendacion(boolean param){
                            
                                       // setting primitive attribute tracker to true
                                       localShowRecomendacionTracker =
                                       true;
                                   
                                            this.localShowRecomendacion=param;
                                    

                               }
                            

                        /**
                        * field for ShowValoracion
                        */

                        
                                    protected boolean localShowValoracion ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localShowValoracionTracker = false ;

                           public boolean isShowValoracionSpecified(){
                               return localShowValoracionTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return boolean
                           */
                           public  boolean getShowValoracion(){
                               return localShowValoracion;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ShowValoracion
                               */
                               public void setShowValoracion(boolean param){
                            
                                       // setting primitive attribute tracker to true
                                       localShowValoracionTracker =
                                       true;
                                   
                                            this.localShowValoracion=param;
                                    

                               }
                            

                        /**
                        * field for Sitio
                        */

                        
                                    protected java.lang.String localSitio ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localSitioTracker = false ;

                           public boolean isSitioSpecified(){
                               return localSitioTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getSitio(){
                               return localSitio;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Sitio
                               */
                               public void setSitio(java.lang.String param){
                            localSitioTracker = true;
                                   
                                            this.localSitio=param;
                                    

                               }
                            

                        /**
                        * field for SlotId
                        */

                        
                                    protected int localSlotId ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localSlotIdTracker = false ;

                           public boolean isSlotIdSpecified(){
                               return localSlotIdTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return int
                           */
                           public  int getSlotId(){
                               return localSlotId;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param SlotId
                               */
                               public void setSlotId(int param){
                            
                                       // setting primitive attribute tracker to true
                                       localSlotIdTracker =
                                       param != java.lang.Integer.MIN_VALUE;
                                   
                                            this.localSlotId=param;
                                    

                               }
                            

                        /**
                        * field for Tags
                        * This was an Array!
                        */

                        
                                    protected java.lang.String[] localTags ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localTagsTracker = false ;

                           public boolean isTagsSpecified(){
                               return localTagsTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String[]
                           */
                           public  java.lang.String[] getTags(){
                               return localTags;
                           }

                           
                        


                               
                              /**
                               * validate the array for Tags
                               */
                              protected void validateTags(java.lang.String[] param){
                             
                              }


                             /**
                              * Auto generated setter method
                              * @param param Tags
                              */
                              public void setTags(java.lang.String[] param){
                              
                                   validateTags(param);

                               localTagsTracker = true;
                                      
                                      this.localTags=param;
                              }

                               
                             
                             /**
                             * Auto generated add method for the array for convenience
                             * @param param java.lang.String
                             */
                             public void addTags(java.lang.String param){
                                   if (localTags == null){
                                   localTags = new java.lang.String[]{};
                                   }

                            
                                 //update the setting tracker
                                localTagsTracker = true;
                            

                               java.util.List list =
                            org.apache.axis2.databinding.utils.ConverterUtil.toList(localTags);
                               list.add(param);
                               this.localTags =
                             (java.lang.String[])list.toArray(
                            new java.lang.String[list.size()]);

                             }
                             

                        /**
                        * field for TipoContenido
                        */

                        
                                    protected java.lang.String localTipoContenido ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localTipoContenidoTracker = false ;

                           public boolean isTipoContenidoSpecified(){
                               return localTipoContenidoTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getTipoContenido(){
                               return localTipoContenido;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param TipoContenido
                               */
                               public void setTipoContenido(java.lang.String param){
                            localTipoContenidoTracker = true;
                                   
                                            this.localTipoContenido=param;
                                    

                               }
                            

                        /**
                        * field for TipoEdicion
                        */

                        
                                    protected int localTipoEdicion ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localTipoEdicionTracker = false ;

                           public boolean isTipoEdicionSpecified(){
                               return localTipoEdicionTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return int
                           */
                           public  int getTipoEdicion(){
                               return localTipoEdicion;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param TipoEdicion
                               */
                               public void setTipoEdicion(int param){
                            
                                       // setting primitive attribute tracker to true
                                       localTipoEdicionTracker =
                                       param != java.lang.Integer.MIN_VALUE;
                                   
                                            this.localTipoEdicion=param;
                                    

                               }
                            

                        /**
                        * field for To
                        */

                        
                                    protected java.util.Date localTo ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localToTracker = false ;

                           public boolean isToSpecified(){
                               return localToTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.util.Date
                           */
                           public  java.util.Date getTo(){
                               return localTo;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param To
                               */
                               public void setTo(java.util.Date param){
                            localToTracker = true;
                                   
                                            this.localTo=param;
                                    

                               }
                            

                        /**
                        * field for ToDateRecurso
                        */

                        
                                    protected java.util.Date localToDateRecurso ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localToDateRecursoTracker = false ;

                           public boolean isToDateRecursoSpecified(){
                               return localToDateRecursoTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.util.Date
                           */
                           public  java.util.Date getToDateRecurso(){
                               return localToDateRecurso;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ToDateRecurso
                               */
                               public void setToDateRecurso(java.util.Date param){
                            localToDateRecursoTracker = true;
                                   
                                            this.localToDateRecurso=param;
                                    

                               }
                            

                        /**
                        * field for Url
                        */

                        
                                    protected java.lang.String localUrl ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localUrlTracker = false ;

                           public boolean isUrlSpecified(){
                               return localUrlTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getUrl(){
                               return localUrl;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Url
                               */
                               public void setUrl(java.lang.String param){
                            localUrlTracker = true;
                                   
                                            this.localUrl=param;
                                    

                               }
                            

                        /**
                        * field for Urls
                        * This was an Array!
                        */

                        
                                    protected java.lang.String[] localUrls ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localUrlsTracker = false ;

                           public boolean isUrlsSpecified(){
                               return localUrlsTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String[]
                           */
                           public  java.lang.String[] getUrls(){
                               return localUrls;
                           }

                           
                        


                               
                              /**
                               * validate the array for Urls
                               */
                              protected void validateUrls(java.lang.String[] param){
                             
                              }


                             /**
                              * Auto generated setter method
                              * @param param Urls
                              */
                              public void setUrls(java.lang.String[] param){
                              
                                   validateUrls(param);

                               localUrlsTracker = true;
                                      
                                      this.localUrls=param;
                              }

                               
                             
                             /**
                             * Auto generated add method for the array for convenience
                             * @param param java.lang.String
                             */
                             public void addUrls(java.lang.String param){
                                   if (localUrls == null){
                                   localUrls = new java.lang.String[]{};
                                   }

                            
                                 //update the setting tracker
                                localUrlsTracker = true;
                            

                               java.util.List list =
                            org.apache.axis2.databinding.utils.ConverterUtil.toList(localUrls);
                               list.add(param);
                               this.localUrls =
                             (java.lang.String[])list.toArray(
                            new java.lang.String[list.size()]);

                             }
                             

                        /**
                        * field for UseCachedresults
                        */

                        
                                    protected boolean localUseCachedresults ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localUseCachedresultsTracker = false ;

                           public boolean isUseCachedresultsSpecified(){
                               return localUseCachedresultsTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return boolean
                           */
                           public  boolean getUseCachedresults(){
                               return localUseCachedresults;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param UseCachedresults
                               */
                               public void setUseCachedresults(boolean param){
                            
                                       // setting primitive attribute tracker to true
                                       localUseCachedresultsTracker =
                                       true;
                                   
                                            this.localUseCachedresults=param;
                                    

                               }
                            

     
     
        /**
        *
        * @param parentQName
        * @param factory
        * @return org.apache.axiom.om.OMElement
        */
       public org.apache.axiom.om.OMElement getOMElement (
               final javax.xml.namespace.QName parentQName,
               final org.apache.axiom.om.OMFactory factory) throws org.apache.axis2.databinding.ADBException{


        
               org.apache.axiom.om.OMDataSource dataSource =
                       new org.apache.axis2.databinding.ADBDataSource(this,parentQName);
               return factory.createOMElement(dataSource,parentQName);
            
        }

         public void serialize(final javax.xml.namespace.QName parentQName,
                                       javax.xml.stream.XMLStreamWriter xmlWriter)
                                throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException{
                           serialize(parentQName,xmlWriter,false);
         }

         public void serialize(final javax.xml.namespace.QName parentQName,
                               javax.xml.stream.XMLStreamWriter xmlWriter,
                               boolean serializeType)
            throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException{
            
                


                java.lang.String prefix = null;
                java.lang.String namespace = null;
                

                    prefix = parentQName.getPrefix();
                    namespace = parentQName.getNamespaceURI();
                    writeStartElement(prefix, namespace, parentQName.getLocalPart(), xmlWriter);
                
                  if (serializeType){
               

                   java.lang.String namespacePrefix = registerPrefix(xmlWriter,"http://model.statistics.tfsla.com/xsd");
                   if ((namespacePrefix != null) && (namespacePrefix.trim().length() > 0)){
                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                           namespacePrefix+":TfsStatisticsOptions",
                           xmlWriter);
                   } else {
                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                           "TfsStatisticsOptions",
                           xmlWriter);
                   }

               
                   }
                if (localAutorTracker){
                                    namespace = "http://model.statistics.tfsla.com/xsd";
                                    writeStartElement(null, namespace, "autor", xmlWriter);
                             

                                          if (localAutor==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localAutor);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localCampoFechaRecursoTracker){
                                    namespace = "http://model.statistics.tfsla.com/xsd";
                                    writeStartElement(null, namespace, "campoFechaRecurso", xmlWriter);
                             

                                          if (localCampoFechaRecurso==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localCampoFechaRecurso);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localCountTracker){
                                    namespace = "http://model.statistics.tfsla.com/xsd";
                                    writeStartElement(null, namespace, "count", xmlWriter);
                             
                                               if (localCount==java.lang.Integer.MIN_VALUE) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("count cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localCount));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localEdicionTracker){
                                    namespace = "http://model.statistics.tfsla.com/xsd";
                                    writeStartElement(null, namespace, "edicion", xmlWriter);
                             
                                               if (localEdicion==java.lang.Integer.MIN_VALUE) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("edicion cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localEdicion));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localFromTracker){
                                    namespace = "http://model.statistics.tfsla.com/xsd";
                                    writeStartElement(null, namespace, "from", xmlWriter);
                             

                                          if (localFrom==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                      		Calendar calendarFrom = new GregorianCalendar();
                                      		calendarFrom.setTime(localFrom);
                                        	    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(calendarFrom));
    
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localFromDateRecursoTracker){
                                    namespace = "http://model.statistics.tfsla.com/xsd";
                                    writeStartElement(null, namespace, "fromDateRecurso", xmlWriter);
                             

                                          if (localFromDateRecurso==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                        		Calendar calendarFrom = new GregorianCalendar();
                                          		calendarFrom.setTime(localFromDateRecurso);
                                            	    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(calendarFrom));
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localPageTracker){
                                    namespace = "http://model.statistics.tfsla.com/xsd";
                                    writeStartElement(null, namespace, "page", xmlWriter);
                             
                                               if (localPage==java.lang.Integer.MIN_VALUE) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("page cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localPage));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localRankModeTracker){
                                    namespace = "http://model.statistics.tfsla.com/xsd";
                                    writeStartElement(null, namespace, "rankMode", xmlWriter);
                             
                                               if (localRankMode==java.lang.Integer.MIN_VALUE) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("rankMode cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localRankMode));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localSeccionTracker){
                                    namespace = "http://model.statistics.tfsla.com/xsd";
                                    writeStartElement(null, namespace, "seccion", xmlWriter);
                             

                                          if (localSeccion==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localSeccion);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localShowCantidadValoracionTracker){
                                    namespace = "http://model.statistics.tfsla.com/xsd";
                                    writeStartElement(null, namespace, "showCantidadValoracion", xmlWriter);
                             
                                               if (false) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("showCantidadValoracion cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localShowCantidadValoracion));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localShowComentariosTracker){
                                    namespace = "http://model.statistics.tfsla.com/xsd";
                                    writeStartElement(null, namespace, "showComentarios", xmlWriter);
                             
                                               if (false) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("showComentarios cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localShowComentarios));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localShowCustom1Tracker){
                                    namespace = "http://model.statistics.tfsla.com/xsd";
                                    writeStartElement(null, namespace, "showCustom1", xmlWriter);
                             
                                               if (false) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("showCustom1 cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localShowCustom1));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localShowCustom10Tracker){
                                    namespace = "http://model.statistics.tfsla.com/xsd";
                                    writeStartElement(null, namespace, "showCustom10", xmlWriter);
                             
                                               if (false) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("showCustom10 cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localShowCustom10));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localShowCustom2Tracker){
                                    namespace = "http://model.statistics.tfsla.com/xsd";
                                    writeStartElement(null, namespace, "showCustom2", xmlWriter);
                             
                                               if (false) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("showCustom2 cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localShowCustom2));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localShowCustom3Tracker){
                                    namespace = "http://model.statistics.tfsla.com/xsd";
                                    writeStartElement(null, namespace, "showCustom3", xmlWriter);
                             
                                               if (false) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("showCustom3 cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localShowCustom3));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localShowCustom4Tracker){
                                    namespace = "http://model.statistics.tfsla.com/xsd";
                                    writeStartElement(null, namespace, "showCustom4", xmlWriter);
                             
                                               if (false) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("showCustom4 cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localShowCustom4));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localShowCustom5Tracker){
                                    namespace = "http://model.statistics.tfsla.com/xsd";
                                    writeStartElement(null, namespace, "showCustom5", xmlWriter);
                             
                                               if (false) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("showCustom5 cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localShowCustom5));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localShowCustom6Tracker){
                                    namespace = "http://model.statistics.tfsla.com/xsd";
                                    writeStartElement(null, namespace, "showCustom6", xmlWriter);
                             
                                               if (false) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("showCustom6 cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localShowCustom6));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localShowCustom7Tracker){
                                    namespace = "http://model.statistics.tfsla.com/xsd";
                                    writeStartElement(null, namespace, "showCustom7", xmlWriter);
                             
                                               if (false) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("showCustom7 cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localShowCustom7));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localShowCustom8Tracker){
                                    namespace = "http://model.statistics.tfsla.com/xsd";
                                    writeStartElement(null, namespace, "showCustom8", xmlWriter);
                             
                                               if (false) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("showCustom8 cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localShowCustom8));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localShowCustom9Tracker){
                                    namespace = "http://model.statistics.tfsla.com/xsd";
                                    writeStartElement(null, namespace, "showCustom9", xmlWriter);
                             
                                               if (false) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("showCustom9 cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localShowCustom9));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localShowGeneralRankTracker){
                                    namespace = "http://model.statistics.tfsla.com/xsd";
                                    writeStartElement(null, namespace, "showGeneralRank", xmlWriter);
                             
                                               if (false) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("showGeneralRank cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localShowGeneralRank));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localShowHitsTracker){
                                    namespace = "http://model.statistics.tfsla.com/xsd";
                                    writeStartElement(null, namespace, "showHits", xmlWriter);
                             
                                               if (false) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("showHits cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localShowHits));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localShowRecomendacionTracker){
                                    namespace = "http://model.statistics.tfsla.com/xsd";
                                    writeStartElement(null, namespace, "showRecomendacion", xmlWriter);
                             
                                               if (false) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("showRecomendacion cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localShowRecomendacion));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localShowValoracionTracker){
                                    namespace = "http://model.statistics.tfsla.com/xsd";
                                    writeStartElement(null, namespace, "showValoracion", xmlWriter);
                             
                                               if (false) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("showValoracion cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localShowValoracion));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localSitioTracker){
                                    namespace = "http://model.statistics.tfsla.com/xsd";
                                    writeStartElement(null, namespace, "sitio", xmlWriter);
                             

                                          if (localSitio==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localSitio);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localSlotIdTracker){
                                    namespace = "http://model.statistics.tfsla.com/xsd";
                                    writeStartElement(null, namespace, "slotId", xmlWriter);
                             
                                               if (localSlotId==java.lang.Integer.MIN_VALUE) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("slotId cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localSlotId));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localTagsTracker){
                             if (localTags!=null) {
                                   namespace = "http://model.statistics.tfsla.com/xsd";
                                   for (int i = 0;i < localTags.length;i++){
                                        
                                            if (localTags[i] != null){
                                        
                                                writeStartElement(null, namespace, "tags", xmlWriter);

                                            
                                                        xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localTags[i]));
                                                    
                                                xmlWriter.writeEndElement();
                                              
                                                } else {
                                                   
                                                           // write null attribute
                                                            namespace = "http://model.statistics.tfsla.com/xsd";
                                                            writeStartElement(null, namespace, "tags", xmlWriter);
                                                            writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                            xmlWriter.writeEndElement();
                                                       
                                                }

                                   }
                             } else {
                                 
                                         // write the null attribute
                                        // write null attribute
                                           writeStartElement(null, "http://model.statistics.tfsla.com/xsd", "tags", xmlWriter);

                                           // write the nil attribute
                                           writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                           xmlWriter.writeEndElement();
                                    
                             }

                        } if (localTipoContenidoTracker){
                                    namespace = "http://model.statistics.tfsla.com/xsd";
                                    writeStartElement(null, namespace, "tipoContenido", xmlWriter);
                             

                                          if (localTipoContenido==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localTipoContenido);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localTipoEdicionTracker){
                                    namespace = "http://model.statistics.tfsla.com/xsd";
                                    writeStartElement(null, namespace, "tipoEdicion", xmlWriter);
                             
                                               if (localTipoEdicion==java.lang.Integer.MIN_VALUE) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("tipoEdicion cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localTipoEdicion));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localToTracker){
                                    namespace = "http://model.statistics.tfsla.com/xsd";
                                    writeStartElement(null, namespace, "to", xmlWriter);
                             

                                          if (localTo==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{


                                      		Calendar calendarTo = new GregorianCalendar();
                                      		calendarTo.setTime(localTo);
                                        	    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(calendarTo));
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localToDateRecursoTracker){
                                    namespace = "http://model.statistics.tfsla.com/xsd";
                                    writeStartElement(null, namespace, "toDateRecurso", xmlWriter);
                             

                                          if (localToDateRecurso==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        		Calendar calendarTo = new GregorianCalendar();
                                          		calendarTo.setTime(localToDateRecurso);
                                            	    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(calendarTo));
    
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localUrlTracker){
                                    namespace = "http://model.statistics.tfsla.com/xsd";
                                    writeStartElement(null, namespace, "url", xmlWriter);
                             

                                          if (localUrl==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localUrl);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localUrlsTracker){
                             if (localUrls!=null) {
                                   namespace = "http://model.statistics.tfsla.com/xsd";
                                   for (int i = 0;i < localUrls.length;i++){
                                        
                                            if (localUrls[i] != null){
                                        
                                                writeStartElement(null, namespace, "urls", xmlWriter);

                                            
                                                        xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localUrls[i]));
                                                    
                                                xmlWriter.writeEndElement();
                                              
                                                } else {
                                                   
                                                           // write null attribute
                                                            namespace = "http://model.statistics.tfsla.com/xsd";
                                                            writeStartElement(null, namespace, "urls", xmlWriter);
                                                            writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                            xmlWriter.writeEndElement();
                                                       
                                                }

                                   }
                             } else {
                                 
                                         // write the null attribute
                                        // write null attribute
                                           writeStartElement(null, "http://model.statistics.tfsla.com/xsd", "urls", xmlWriter);

                                           // write the nil attribute
                                           writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                           xmlWriter.writeEndElement();
                                    
                             }

                        } if (localUseCachedresultsTracker){
                                    namespace = "http://model.statistics.tfsla.com/xsd";
                                    writeStartElement(null, namespace, "useCachedresults", xmlWriter);
                             
                                               if (false) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("useCachedresults cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localUseCachedresults));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             }
                    xmlWriter.writeEndElement();
               

        }

        private static java.lang.String generatePrefix(java.lang.String namespace) {
            if(namespace.equals("http://model.statistics.tfsla.com/xsd")){
                return "";
            }
            return org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
        }

        /**
         * Utility method to write an element start tag.
         */
        private void writeStartElement(java.lang.String prefix, java.lang.String namespace, java.lang.String localPart,
                                       javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
            java.lang.String writerPrefix = xmlWriter.getPrefix(namespace);
            if (writerPrefix != null) {
                xmlWriter.writeStartElement(namespace, localPart);
            } else {
                if (namespace.length() == 0) {
                    prefix = "";
                } else if (prefix == null) {
                    prefix = generatePrefix(namespace);
                }

                xmlWriter.writeStartElement(prefix, localPart, namespace);
                xmlWriter.writeNamespace(prefix, namespace);
                xmlWriter.setPrefix(prefix, namespace);
            }
        }
        
        /**
         * Util method to write an attribute with the ns prefix
         */
        private void writeAttribute(java.lang.String prefix,java.lang.String namespace,java.lang.String attName,
                                    java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
            if (xmlWriter.getPrefix(namespace) == null) {
                xmlWriter.writeNamespace(prefix, namespace);
                xmlWriter.setPrefix(prefix, namespace);
            }
            xmlWriter.writeAttribute(namespace,attName,attValue);
        }

        /**
         * Util method to write an attribute without the ns prefix
         */
        private void writeAttribute(java.lang.String namespace,java.lang.String attName,
                                    java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
            if (namespace.equals("")) {
                xmlWriter.writeAttribute(attName,attValue);
            } else {
                registerPrefix(xmlWriter, namespace);
                xmlWriter.writeAttribute(namespace,attName,attValue);
            }
        }


           /**
             * Util method to write an attribute without the ns prefix
             */
            private void writeQNameAttribute(java.lang.String namespace, java.lang.String attName,
                                             javax.xml.namespace.QName qname, javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {

                java.lang.String attributeNamespace = qname.getNamespaceURI();
                java.lang.String attributePrefix = xmlWriter.getPrefix(attributeNamespace);
                if (attributePrefix == null) {
                    attributePrefix = registerPrefix(xmlWriter, attributeNamespace);
                }
                java.lang.String attributeValue;
                if (attributePrefix.trim().length() > 0) {
                    attributeValue = attributePrefix + ":" + qname.getLocalPart();
                } else {
                    attributeValue = qname.getLocalPart();
                }

                if (namespace.equals("")) {
                    xmlWriter.writeAttribute(attName, attributeValue);
                } else {
                    registerPrefix(xmlWriter, namespace);
                    xmlWriter.writeAttribute(namespace, attName, attributeValue);
                }
            }
        /**
         *  method to handle Qnames
         */

        private void writeQName(javax.xml.namespace.QName qname,
                                javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
            java.lang.String namespaceURI = qname.getNamespaceURI();
            if (namespaceURI != null) {
                java.lang.String prefix = xmlWriter.getPrefix(namespaceURI);
                if (prefix == null) {
                    prefix = generatePrefix(namespaceURI);
                    xmlWriter.writeNamespace(prefix, namespaceURI);
                    xmlWriter.setPrefix(prefix,namespaceURI);
                }

                if (prefix.trim().length() > 0){
                    xmlWriter.writeCharacters(prefix + ":" + org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
                } else {
                    // i.e this is the default namespace
                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
                }

            } else {
                xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
            }
        }

        private void writeQNames(javax.xml.namespace.QName[] qnames,
                                 javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {

            if (qnames != null) {
                // we have to store this data until last moment since it is not possible to write any
                // namespace data after writing the charactor data
                java.lang.StringBuffer stringToWrite = new java.lang.StringBuffer();
                java.lang.String namespaceURI = null;
                java.lang.String prefix = null;

                for (int i = 0; i < qnames.length; i++) {
                    if (i > 0) {
                        stringToWrite.append(" ");
                    }
                    namespaceURI = qnames[i].getNamespaceURI();
                    if (namespaceURI != null) {
                        prefix = xmlWriter.getPrefix(namespaceURI);
                        if ((prefix == null) || (prefix.length() == 0)) {
                            prefix = generatePrefix(namespaceURI);
                            xmlWriter.writeNamespace(prefix, namespaceURI);
                            xmlWriter.setPrefix(prefix,namespaceURI);
                        }

                        if (prefix.trim().length() > 0){
                            stringToWrite.append(prefix).append(":").append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                        } else {
                            stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                        }
                    } else {
                        stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                    }
                }
                xmlWriter.writeCharacters(stringToWrite.toString());
            }

        }


        /**
         * Register a namespace prefix
         */
        private java.lang.String registerPrefix(javax.xml.stream.XMLStreamWriter xmlWriter, java.lang.String namespace) throws javax.xml.stream.XMLStreamException {
            java.lang.String prefix = xmlWriter.getPrefix(namespace);
            if (prefix == null) {
                prefix = generatePrefix(namespace);
                while (xmlWriter.getNamespaceContext().getNamespaceURI(prefix) != null) {
                    prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
                }
                xmlWriter.writeNamespace(prefix, namespace);
                xmlWriter.setPrefix(prefix, namespace);
            }
            return prefix;
        }


  
        /**
        * databinding method to get an XML representation of this object
        *
        */
        public javax.xml.stream.XMLStreamReader getPullParser(javax.xml.namespace.QName qName)
                    throws org.apache.axis2.databinding.ADBException{


        
                 java.util.ArrayList elementList = new java.util.ArrayList();
                 java.util.ArrayList attribList = new java.util.ArrayList();

                 if (localAutorTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd",
                                                                      "autor"));
                                 
                                         elementList.add(localAutor==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localAutor));
                                    } if (localCampoFechaRecursoTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd",
                                                                      "campoFechaRecurso"));
                                 
                                         elementList.add(localCampoFechaRecurso==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localCampoFechaRecurso));
                                    } if (localCountTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd",
                                                                      "count"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localCount));
                            } if (localEdicionTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd",
                                                                      "edicion"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localEdicion));
                            } if (localFromTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd",
                                                                      "from"));
                                 
                                         elementList.add(localFrom==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localFrom));
                                    } if (localFromDateRecursoTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd",
                                                                      "fromDateRecurso"));
                                 
                                         elementList.add(localFromDateRecurso==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localFromDateRecurso));
                                    } if (localPageTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd",
                                                                      "page"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localPage));
                            } if (localRankModeTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd",
                                                                      "rankMode"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localRankMode));
                            } if (localSeccionTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd",
                                                                      "seccion"));
                                 
                                         elementList.add(localSeccion==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localSeccion));
                                    } if (localShowCantidadValoracionTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd",
                                                                      "showCantidadValoracion"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localShowCantidadValoracion));
                            } if (localShowComentariosTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd",
                                                                      "showComentarios"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localShowComentarios));
                            } if (localShowCustom1Tracker){
                                      elementList.add(new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd",
                                                                      "showCustom1"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localShowCustom1));
                            } if (localShowCustom10Tracker){
                                      elementList.add(new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd",
                                                                      "showCustom10"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localShowCustom10));
                            } if (localShowCustom2Tracker){
                                      elementList.add(new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd",
                                                                      "showCustom2"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localShowCustom2));
                            } if (localShowCustom3Tracker){
                                      elementList.add(new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd",
                                                                      "showCustom3"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localShowCustom3));
                            } if (localShowCustom4Tracker){
                                      elementList.add(new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd",
                                                                      "showCustom4"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localShowCustom4));
                            } if (localShowCustom5Tracker){
                                      elementList.add(new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd",
                                                                      "showCustom5"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localShowCustom5));
                            } if (localShowCustom6Tracker){
                                      elementList.add(new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd",
                                                                      "showCustom6"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localShowCustom6));
                            } if (localShowCustom7Tracker){
                                      elementList.add(new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd",
                                                                      "showCustom7"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localShowCustom7));
                            } if (localShowCustom8Tracker){
                                      elementList.add(new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd",
                                                                      "showCustom8"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localShowCustom8));
                            } if (localShowCustom9Tracker){
                                      elementList.add(new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd",
                                                                      "showCustom9"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localShowCustom9));
                            } if (localShowGeneralRankTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd",
                                                                      "showGeneralRank"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localShowGeneralRank));
                            } if (localShowHitsTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd",
                                                                      "showHits"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localShowHits));
                            } if (localShowRecomendacionTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd",
                                                                      "showRecomendacion"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localShowRecomendacion));
                            } if (localShowValoracionTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd",
                                                                      "showValoracion"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localShowValoracion));
                            } if (localSitioTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd",
                                                                      "sitio"));
                                 
                                         elementList.add(localSitio==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localSitio));
                                    } if (localSlotIdTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd",
                                                                      "slotId"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localSlotId));
                            } if (localTagsTracker){
                            if (localTags!=null){
                                  for (int i = 0;i < localTags.length;i++){
                                      
                                         if (localTags[i] != null){
                                          elementList.add(new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd",
                                                                              "tags"));
                                          elementList.add(
                                          org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localTags[i]));
                                          } else {
                                             
                                                    elementList.add(new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd",
                                                                              "tags"));
                                                    elementList.add(null);
                                                
                                          }
                                      

                                  }
                            } else {
                              
                                    elementList.add(new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd",
                                                                              "tags"));
                                    elementList.add(null);
                                
                            }

                        } if (localTipoContenidoTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd",
                                                                      "tipoContenido"));
                                 
                                         elementList.add(localTipoContenido==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localTipoContenido));
                                    } if (localTipoEdicionTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd",
                                                                      "tipoEdicion"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localTipoEdicion));
                            } if (localToTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd",
                                                                      "to"));
                                 
                                         elementList.add(localTo==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localTo));
                                    } if (localToDateRecursoTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd",
                                                                      "toDateRecurso"));
                                 
                                         elementList.add(localToDateRecurso==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localToDateRecurso));
                                    } if (localUrlTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd",
                                                                      "url"));
                                 
                                         elementList.add(localUrl==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localUrl));
                                    } if (localUrlsTracker){
                            if (localUrls!=null){
                                  for (int i = 0;i < localUrls.length;i++){
                                      
                                         if (localUrls[i] != null){
                                          elementList.add(new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd",
                                                                              "urls"));
                                          elementList.add(
                                          org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localUrls[i]));
                                          } else {
                                             
                                                    elementList.add(new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd",
                                                                              "urls"));
                                                    elementList.add(null);
                                                
                                          }
                                      

                                  }
                            } else {
                              
                                    elementList.add(new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd",
                                                                              "urls"));
                                    elementList.add(null);
                                
                            }

                        } if (localUseCachedresultsTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd",
                                                                      "useCachedresults"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localUseCachedresults));
                            }

                return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(qName, elementList.toArray(), attribList.toArray());
            
            

        }

  

     /**
      *  Factory class that keeps the parse method
      */
    public static class Factory{

        
        

        /**
        * static method to create the object
        * Precondition:  If this object is an element, the current or next start element starts this object and any intervening reader events are ignorable
        *                If this object is not an element, it is a complex type and the reader is at the event just after the outer start element
        * Postcondition: If this object is an element, the reader is positioned at its end element
        *                If this object is a complex type, the reader is positioned at the end element of its outer element
        */
        public static TfsStatisticsOptions parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
            TfsStatisticsOptions object =
                new TfsStatisticsOptions();

            int event;
            java.lang.String nillableValue = null;
            java.lang.String prefix ="";
            java.lang.String namespaceuri ="";
            try {
                
                while (!reader.isStartElement() && !reader.isEndElement())
                    reader.next();

                
                if (reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","type")!=null){
                  java.lang.String fullTypeName = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                        "type");
                  if (fullTypeName!=null){
                    java.lang.String nsPrefix = null;
                    if (fullTypeName.indexOf(":") > -1){
                        nsPrefix = fullTypeName.substring(0,fullTypeName.indexOf(":"));
                    }
                    nsPrefix = nsPrefix==null?"":nsPrefix;

                    java.lang.String type = fullTypeName.substring(fullTypeName.indexOf(":")+1);
                    
                            if (!"TfsStatisticsOptions".equals(type)){
                                //find namespace for the prefix
                                java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                                return (TfsStatisticsOptions)com.tfsla.statistics.model.ExtensionMapper.getTypeObject(
                                     nsUri,type,reader);
                              }
                        

                  }
                

                }

                

                
                // Note all attributes that were handled. Used to differ normal attributes
                // from anyAttributes.
                java.util.Vector handledAttributes = new java.util.Vector();
                

                
                    
                    reader.next();
                
                        java.util.ArrayList list28 = new java.util.ArrayList();
                    
                        java.util.ArrayList list34 = new java.util.ArrayList();
                    
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd","autor").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setAutor(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd","campoFechaRecurso").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setCampoFechaRecurso(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd","count").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setCount(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToInt(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                               object.setCount(java.lang.Integer.MIN_VALUE);
                                           
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd","edicion").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setEdicion(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToInt(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                               object.setEdicion(java.lang.Integer.MIN_VALUE);
                                           
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd","from").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setFrom(
                                            		  org.apache.axis2.databinding.utils.ConverterUtil.convertToDateTime(content).getTime());
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd","fromDateRecurso").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setFromDateRecurso(
                                            		  org.apache.axis2.databinding.utils.ConverterUtil.convertToDateTime(content).getTime());
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd","page").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setPage(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToInt(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                               object.setPage(java.lang.Integer.MIN_VALUE);
                                           
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd","rankMode").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setRankMode(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToInt(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                               object.setRankMode(java.lang.Integer.MIN_VALUE);
                                           
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd","seccion").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setSeccion(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd","showCantidadValoracion").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setShowCantidadValoracion(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToBoolean(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd","showComentarios").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setShowComentarios(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToBoolean(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd","showCustom1").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setShowCustom1(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToBoolean(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd","showCustom10").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setShowCustom10(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToBoolean(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd","showCustom2").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setShowCustom2(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToBoolean(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd","showCustom3").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setShowCustom3(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToBoolean(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd","showCustom4").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setShowCustom4(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToBoolean(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd","showCustom5").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setShowCustom5(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToBoolean(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd","showCustom6").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setShowCustom6(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToBoolean(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd","showCustom7").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setShowCustom7(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToBoolean(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd","showCustom8").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setShowCustom8(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToBoolean(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd","showCustom9").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setShowCustom9(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToBoolean(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd","showGeneralRank").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setShowGeneralRank(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToBoolean(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd","showHits").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setShowHits(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToBoolean(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd","showRecomendacion").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setShowRecomendacion(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToBoolean(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd","showValoracion").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setShowValoracion(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToBoolean(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd","sitio").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setSitio(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd","slotId").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setSlotId(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToInt(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                               object.setSlotId(java.lang.Integer.MIN_VALUE);
                                           
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd","tags").equals(reader.getName())){
                                
                                    
                                    
                                    // Process the array and step past its final element's end.
                                    
                                              nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                              if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                  list28.add(null);
                                                       
                                                  reader.next();
                                              } else {
                                            list28.add(reader.getElementText());
                                            }
                                            //loop until we find a start element that is not part of this array
                                            boolean loopDone28 = false;
                                            while(!loopDone28){
                                                // Ensure we are at the EndElement
                                                while (!reader.isEndElement()){
                                                    reader.next();
                                                }
                                                // Step out of this element
                                                reader.next();
                                                // Step to next element event.
                                                while (!reader.isStartElement() && !reader.isEndElement())
                                                    reader.next();
                                                if (reader.isEndElement()){
                                                    //two continuous end elements means we are exiting the xml structure
                                                    loopDone28 = true;
                                                } else {
                                                    if (new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd","tags").equals(reader.getName())){
                                                         
                                                          nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                          if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                              list28.add(null);
                                                                   
                                                              reader.next();
                                                          } else {
                                                        list28.add(reader.getElementText());
                                                        }
                                                    }else{
                                                        loopDone28 = true;
                                                    }
                                                }
                                            }
                                            // call the converter utility  to convert and set the array
                                            
                                                    object.setTags((java.lang.String[])
                                                        list28.toArray(new java.lang.String[list28.size()]));
                                                
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd","tipoContenido").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setTipoContenido(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd","tipoEdicion").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setTipoEdicion(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToInt(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                               object.setTipoEdicion(java.lang.Integer.MIN_VALUE);
                                           
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd","to").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setTo(
                                            		  org.apache.axis2.databinding.utils.ConverterUtil.convertToDateTime(content).getTime());
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd","toDateRecurso").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setToDateRecurso(
                                            		  org.apache.axis2.databinding.utils.ConverterUtil.convertToDateTime(content).getTime());
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd","url").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setUrl(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd","urls").equals(reader.getName())){
                                
                                    
                                    
                                    // Process the array and step past its final element's end.
                                    
                                              nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                              if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                  list34.add(null);
                                                       
                                                  reader.next();
                                              } else {
                                            list34.add(reader.getElementText());
                                            }
                                            //loop until we find a start element that is not part of this array
                                            boolean loopDone34 = false;
                                            while(!loopDone34){
                                                // Ensure we are at the EndElement
                                                while (!reader.isEndElement()){
                                                    reader.next();
                                                }
                                                // Step out of this element
                                                reader.next();
                                                // Step to next element event.
                                                while (!reader.isStartElement() && !reader.isEndElement())
                                                    reader.next();
                                                if (reader.isEndElement()){
                                                    //two continuous end elements means we are exiting the xml structure
                                                    loopDone34 = true;
                                                } else {
                                                    if (new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd","urls").equals(reader.getName())){
                                                         
                                                          nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                          if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                              list34.add(null);
                                                                   
                                                              reader.next();
                                                          } else {
                                                        list34.add(reader.getElementText());
                                                        }
                                                    }else{
                                                        loopDone34 = true;
                                                    }
                                                }
                                            }
                                            // call the converter utility  to convert and set the array
                                            
                                                    object.setUrls((java.lang.String[])
                                                        list34.toArray(new java.lang.String[list34.size()]));
                                                
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd","useCachedresults").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setUseCachedresults(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToBoolean(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                  
                            while (!reader.isStartElement() && !reader.isEndElement())
                                reader.next();
                            
                                if (reader.isStartElement())
                                // A start element we are not expecting indicates a trailing invalid property
                                throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getName());
                            



            } catch (javax.xml.stream.XMLStreamException e) {
                throw new java.lang.Exception(e);
            }

            return object;
        }

        }//end of factory class

        

        }
           
    