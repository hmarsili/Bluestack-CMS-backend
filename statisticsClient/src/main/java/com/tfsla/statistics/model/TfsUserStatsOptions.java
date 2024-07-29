
/**
 * TfsUserStatsOptions.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.0  Built on : May 17, 2011 (04:21:18 IST)
 */

            
                package com.tfsla.statistics.model;

import java.util.Calendar;
import java.util.GregorianCalendar;
            

            /**
            *  TfsUserStatsOptions bean class
            */
            @SuppressWarnings({"unchecked","unused"})
        
        public  class TfsUserStatsOptions
        implements org.apache.axis2.databinding.ADBBean{
        /* This type was generated from the piece of schema that had
                name = TfsUserStatsOptions
                Namespace URI = http://model.statistics.tfsla.com/xsd
                Namespace Prefix = 
                */
            
            	public static final int RANK_GENERAL =1;
            	public static final int RANK_VISITASRECIBIDAS =2;
            	public static final int RANK_NOTASPUBLICADAS =3;
            	public static final int RANK_RECOMENDACIONESRECIBIDAS =4;
            	public static final int RANK_COMENTARIOSRECIBIDOS =5;
            	public static final int RANK_COMENTARIOSREALIZADOS =6;
            	public static final int RANK_COMENTARIOSRECHAZADOS =7;	
            	public static final int RANK_VALORACIONES_CANTIDAD =8;
            	public static final int RANK_VALORACIONES_PROMEDIO =9;
            	public static final int RANK_VALORACIONES_POSITIVO =10;
            	public static final int RANK_VALORACIONES_NEGATIVO =11;

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
                        * field for Grupos
                        * This was an Array!
                        */

                        
                                    protected java.lang.String[] localGrupos ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localGruposTracker = false ;

                           public boolean isGruposSpecified(){
                               return localGruposTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String[]
                           */
                           public  java.lang.String[] getGrupos(){
                               return localGrupos;
                           }

                           
                        


                               
                              /**
                               * validate the array for Grupos
                               */
                              protected void validateGrupos(java.lang.String[] param){
                             
                              }


                             /**
                              * Auto generated setter method
                              * @param param Grupos
                              */
                              public void setGrupos(java.lang.String[] param){
                              
                                   validateGrupos(param);

                               localGruposTracker = true;
                                      
                                      this.localGrupos=param;
                              }

                               
                             
                             /**
                             * Auto generated add method for the array for convenience
                             * @param param java.lang.String
                             */
                             public void addGrupos(java.lang.String param){
                                   if (localGrupos == null){
                                   localGrupos = new java.lang.String[]{};
                                   }

                            
                                 //update the setting tracker
                                localGruposTracker = true;
                            

                               java.util.List list =
                            org.apache.axis2.databinding.utils.ConverterUtil.toList(localGrupos);
                               list.add(param);
                               this.localGrupos =
                             (java.lang.String[])list.toArray(
                            new java.lang.String[list.size()]);

                             }
                             

                        /**
                        * field for Ou
                        */

                        
                                    protected java.lang.String localOu ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localOuTracker = false ;

                           public boolean isOuSpecified(){
                               return localOuTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getOu(){
                               return localOu;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Ou
                               */
                               public void setOu(java.lang.String param){
                            localOuTracker = true;
                                   
                                            this.localOu=param;
                                    

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
                        * field for ShowCantidadValoraciones
                        */

                        
                                    protected boolean localShowCantidadValoraciones ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localShowCantidadValoracionesTracker = false ;

                           public boolean isShowCantidadValoracionesSpecified(){
                               return localShowCantidadValoracionesTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return boolean
                           */
                           public  boolean getShowCantidadValoraciones(){
                               return localShowCantidadValoraciones;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ShowCantidadValoraciones
                               */
                               public void setShowCantidadValoraciones(boolean param){
                            
                                       // setting primitive attribute tracker to true
                                       localShowCantidadValoracionesTracker =
                                       true;
                                   
                                            this.localShowCantidadValoraciones=param;
                                    

                               }
                            

                        /**
                        * field for ShowComentariosRealizados
                        */

                        
                                    protected boolean localShowComentariosRealizados ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localShowComentariosRealizadosTracker = false ;

                           public boolean isShowComentariosRealizadosSpecified(){
                               return localShowComentariosRealizadosTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return boolean
                           */
                           public  boolean getShowComentariosRealizados(){
                               return localShowComentariosRealizados;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ShowComentariosRealizados
                               */
                               public void setShowComentariosRealizados(boolean param){
                            
                                       // setting primitive attribute tracker to true
                                       localShowComentariosRealizadosTracker =
                                       true;
                                   
                                            this.localShowComentariosRealizados=param;
                                    

                               }
                            

                        /**
                        * field for ShowComentariosRechazados
                        */

                        
                                    protected boolean localShowComentariosRechazados ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localShowComentariosRechazadosTracker = false ;

                           public boolean isShowComentariosRechazadosSpecified(){
                               return localShowComentariosRechazadosTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return boolean
                           */
                           public  boolean getShowComentariosRechazados(){
                               return localShowComentariosRechazados;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ShowComentariosRechazados
                               */
                               public void setShowComentariosRechazados(boolean param){
                            
                                       // setting primitive attribute tracker to true
                                       localShowComentariosRechazadosTracker =
                                       true;
                                   
                                            this.localShowComentariosRechazados=param;
                                    

                               }
                            

                        /**
                        * field for ShowComentariosRecibidos
                        */

                        
                                    protected boolean localShowComentariosRecibidos ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localShowComentariosRecibidosTracker = false ;

                           public boolean isShowComentariosRecibidosSpecified(){
                               return localShowComentariosRecibidosTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return boolean
                           */
                           public  boolean getShowComentariosRecibidos(){
                               return localShowComentariosRecibidos;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ShowComentariosRecibidos
                               */
                               public void setShowComentariosRecibidos(boolean param){
                            
                                       // setting primitive attribute tracker to true
                                       localShowComentariosRecibidosTracker =
                                       true;
                                   
                                            this.localShowComentariosRecibidos=param;
                                    

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
                        * field for ShowNotasPublicadas
                        */

                        
                                    protected boolean localShowNotasPublicadas ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localShowNotasPublicadasTracker = false ;

                           public boolean isShowNotasPublicadasSpecified(){
                               return localShowNotasPublicadasTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return boolean
                           */
                           public  boolean getShowNotasPublicadas(){
                               return localShowNotasPublicadas;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ShowNotasPublicadas
                               */
                               public void setShowNotasPublicadas(boolean param){
                            
                                       // setting primitive attribute tracker to true
                                       localShowNotasPublicadasTracker =
                                       true;
                                   
                                            this.localShowNotasPublicadas=param;
                                    

                               }
                            

                        /**
                        * field for ShowRecomendacionesRecibidas
                        */

                        
                                    protected boolean localShowRecomendacionesRecibidas ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localShowRecomendacionesRecibidasTracker = false ;

                           public boolean isShowRecomendacionesRecibidasSpecified(){
                               return localShowRecomendacionesRecibidasTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return boolean
                           */
                           public  boolean getShowRecomendacionesRecibidas(){
                               return localShowRecomendacionesRecibidas;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ShowRecomendacionesRecibidas
                               */
                               public void setShowRecomendacionesRecibidas(boolean param){
                            
                                       // setting primitive attribute tracker to true
                                       localShowRecomendacionesRecibidasTracker =
                                       true;
                                   
                                            this.localShowRecomendacionesRecibidas=param;
                                    

                               }
                            

                        /**
                        * field for ShowValoracionesRecibidas
                        */

                        
                                    protected boolean localShowValoracionesRecibidas ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localShowValoracionesRecibidasTracker = false ;

                           public boolean isShowValoracionesRecibidasSpecified(){
                               return localShowValoracionesRecibidasTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return boolean
                           */
                           public  boolean getShowValoracionesRecibidas(){
                               return localShowValoracionesRecibidas;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ShowValoracionesRecibidas
                               */
                               public void setShowValoracionesRecibidas(boolean param){
                            
                                       // setting primitive attribute tracker to true
                                       localShowValoracionesRecibidasTracker =
                                       true;
                                   
                                            this.localShowValoracionesRecibidas=param;
                                    

                               }
                            

                        /**
                        * field for ShowVisitasRecibidas
                        */

                        
                                    protected boolean localShowVisitasRecibidas ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localShowVisitasRecibidasTracker = false ;

                           public boolean isShowVisitasRecibidasSpecified(){
                               return localShowVisitasRecibidasTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return boolean
                           */
                           public  boolean getShowVisitasRecibidas(){
                               return localShowVisitasRecibidas;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ShowVisitasRecibidas
                               */
                               public void setShowVisitasRecibidas(boolean param){
                            
                                       // setting primitive attribute tracker to true
                                       localShowVisitasRecibidasTracker =
                                       true;
                                   
                                            this.localShowVisitasRecibidas=param;
                                    

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
                        * field for Usuario
                        */

                        
                                    protected java.lang.String localUsuario ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localUsuarioTracker = false ;

                           public boolean isUsuarioSpecified(){
                               return localUsuarioTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getUsuario(){
                               return localUsuario;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Usuario
                               */
                               public void setUsuario(java.lang.String param){
                            localUsuarioTracker = true;
                                   
                                            this.localUsuario=param;
                                    

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
                           namespacePrefix+":TfsUserStatsOptions",
                           xmlWriter);
                   } else {
                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                           "TfsUserStatsOptions",
                           xmlWriter);
                   }

               
                   }
                if (localCountTracker){
                                    namespace = "http://model.statistics.tfsla.com/xsd";
                                    writeStartElement(null, namespace, "count", xmlWriter);
                             
                                               if (localCount==java.lang.Integer.MIN_VALUE) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("count cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localCount));
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
                             } if (localGruposTracker){
                             if (localGrupos!=null) {
                                   namespace = "http://model.statistics.tfsla.com/xsd";
                                   for (int i = 0;i < localGrupos.length;i++){
                                        
                                            if (localGrupos[i] != null){
                                        
                                                writeStartElement(null, namespace, "grupos", xmlWriter);

                                            
                                                        xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localGrupos[i]));
                                                    
                                                xmlWriter.writeEndElement();
                                              
                                                } else {
                                                   
                                                           // write null attribute
                                                            namespace = "http://model.statistics.tfsla.com/xsd";
                                                            writeStartElement(null, namespace, "grupos", xmlWriter);
                                                            writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                            xmlWriter.writeEndElement();
                                                       
                                                }

                                   }
                             } else {
                                 
                                         // write the null attribute
                                        // write null attribute
                                           writeStartElement(null, "http://model.statistics.tfsla.com/xsd", "grupos", xmlWriter);

                                           // write the nil attribute
                                           writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                           xmlWriter.writeEndElement();
                                    
                             }

                        } if (localOuTracker){
                                    namespace = "http://model.statistics.tfsla.com/xsd";
                                    writeStartElement(null, namespace, "ou", xmlWriter);
                             

                                          if (localOu==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localOu);
                                            
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
                             } if (localShowCantidadValoracionesTracker){
                                    namespace = "http://model.statistics.tfsla.com/xsd";
                                    writeStartElement(null, namespace, "showCantidadValoraciones", xmlWriter);
                             
                                               if (false) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("showCantidadValoraciones cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localShowCantidadValoraciones));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localShowComentariosRealizadosTracker){
                                    namespace = "http://model.statistics.tfsla.com/xsd";
                                    writeStartElement(null, namespace, "showComentariosRealizados", xmlWriter);
                             
                                               if (false) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("showComentariosRealizados cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localShowComentariosRealizados));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localShowComentariosRechazadosTracker){
                                    namespace = "http://model.statistics.tfsla.com/xsd";
                                    writeStartElement(null, namespace, "showComentariosRechazados", xmlWriter);
                             
                                               if (false) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("showComentariosRechazados cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localShowComentariosRechazados));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localShowComentariosRecibidosTracker){
                                    namespace = "http://model.statistics.tfsla.com/xsd";
                                    writeStartElement(null, namespace, "showComentariosRecibidos", xmlWriter);
                             
                                               if (false) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("showComentariosRecibidos cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localShowComentariosRecibidos));
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
                             } if (localShowNotasPublicadasTracker){
                                    namespace = "http://model.statistics.tfsla.com/xsd";
                                    writeStartElement(null, namespace, "showNotasPublicadas", xmlWriter);
                             
                                               if (false) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("showNotasPublicadas cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localShowNotasPublicadas));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localShowRecomendacionesRecibidasTracker){
                                    namespace = "http://model.statistics.tfsla.com/xsd";
                                    writeStartElement(null, namespace, "showRecomendacionesRecibidas", xmlWriter);
                             
                                               if (false) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("showRecomendacionesRecibidas cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localShowRecomendacionesRecibidas));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localShowValoracionesRecibidasTracker){
                                    namespace = "http://model.statistics.tfsla.com/xsd";
                                    writeStartElement(null, namespace, "showValoracionesRecibidas", xmlWriter);
                             
                                               if (false) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("showValoracionesRecibidas cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localShowValoracionesRecibidas));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localShowVisitasRecibidasTracker){
                                    namespace = "http://model.statistics.tfsla.com/xsd";
                                    writeStartElement(null, namespace, "showVisitasRecibidas", xmlWriter);
                             
                                               if (false) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("showVisitasRecibidas cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localShowVisitasRecibidas));
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
                             } if (localUseCachedresultsTracker){
                                    namespace = "http://model.statistics.tfsla.com/xsd";
                                    writeStartElement(null, namespace, "useCachedresults", xmlWriter);
                             
                                               if (false) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("useCachedresults cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localUseCachedresults));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localUsuarioTracker){
                                    namespace = "http://model.statistics.tfsla.com/xsd";
                                    writeStartElement(null, namespace, "usuario", xmlWriter);
                             

                                          if (localUsuario==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localUsuario);
                                            
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

                 if (localCountTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd",
                                                                      "count"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localCount));
                            } if (localFromTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd",
                                                                      "from"));
                                 
                                         elementList.add(localFrom==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localFrom));
                                    } if (localGruposTracker){
                            if (localGrupos!=null){
                                  for (int i = 0;i < localGrupos.length;i++){
                                      
                                         if (localGrupos[i] != null){
                                          elementList.add(new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd",
                                                                              "grupos"));
                                          elementList.add(
                                          org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localGrupos[i]));
                                          } else {
                                             
                                                    elementList.add(new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd",
                                                                              "grupos"));
                                                    elementList.add(null);
                                                
                                          }
                                      

                                  }
                            } else {
                              
                                    elementList.add(new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd",
                                                                              "grupos"));
                                    elementList.add(null);
                                
                            }

                        } if (localOuTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd",
                                                                      "ou"));
                                 
                                         elementList.add(localOu==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localOu));
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
                            } if (localShowCantidadValoracionesTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd",
                                                                      "showCantidadValoraciones"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localShowCantidadValoraciones));
                            } if (localShowComentariosRealizadosTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd",
                                                                      "showComentariosRealizados"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localShowComentariosRealizados));
                            } if (localShowComentariosRechazadosTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd",
                                                                      "showComentariosRechazados"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localShowComentariosRechazados));
                            } if (localShowComentariosRecibidosTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd",
                                                                      "showComentariosRecibidos"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localShowComentariosRecibidos));
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
                            } if (localShowNotasPublicadasTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd",
                                                                      "showNotasPublicadas"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localShowNotasPublicadas));
                            } if (localShowRecomendacionesRecibidasTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd",
                                                                      "showRecomendacionesRecibidas"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localShowRecomendacionesRecibidas));
                            } if (localShowValoracionesRecibidasTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd",
                                                                      "showValoracionesRecibidas"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localShowValoracionesRecibidas));
                            } if (localShowVisitasRecibidasTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd",
                                                                      "showVisitasRecibidas"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localShowVisitasRecibidas));
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
                            } if (localToTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd",
                                                                      "to"));
                                 
                                         elementList.add(localTo==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localTo));
                                    } if (localUseCachedresultsTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd",
                                                                      "useCachedresults"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localUseCachedresults));
                            } if (localUsuarioTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd",
                                                                      "usuario"));
                                 
                                         elementList.add(localUsuario==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localUsuario));
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
        public static TfsUserStatsOptions parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
            TfsUserStatsOptions object =
                new TfsUserStatsOptions();

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
                    
                            if (!"TfsUserStatsOptions".equals(type)){
                                //find namespace for the prefix
                                java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                                return (TfsUserStatsOptions)com.tfsla.rankUsers.model.ExtensionMapper.getTypeObject(
                                     nsUri,type,reader);
                              }
                        

                  }
                

                }

                

                
                // Note all attributes that were handled. Used to differ normal attributes
                // from anyAttributes.
                java.util.Vector handledAttributes = new java.util.Vector();
                

                
                    
                    reader.next();
                
                        java.util.ArrayList list3 = new java.util.ArrayList();
                    
                                    
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
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd","grupos").equals(reader.getName())){
                                
                                    
                                    
                                    // Process the array and step past its final element's end.
                                    
                                              nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                              if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                  list3.add(null);
                                                       
                                                  reader.next();
                                              } else {
                                            list3.add(reader.getElementText());
                                            }
                                            //loop until we find a start element that is not part of this array
                                            boolean loopDone3 = false;
                                            while(!loopDone3){
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
                                                    loopDone3 = true;
                                                } else {
                                                    if (new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd","grupos").equals(reader.getName())){
                                                         
                                                          nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                          if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                              list3.add(null);
                                                                   
                                                              reader.next();
                                                          } else {
                                                        list3.add(reader.getElementText());
                                                        }
                                                    }else{
                                                        loopDone3 = true;
                                                    }
                                                }
                                            }
                                            // call the converter utility  to convert and set the array
                                            
                                                    object.setGrupos((java.lang.String[])
                                                        list3.toArray(new java.lang.String[list3.size()]));
                                                
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd","ou").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setOu(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
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
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd","showCantidadValoraciones").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setShowCantidadValoraciones(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToBoolean(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd","showComentariosRealizados").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setShowComentariosRealizados(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToBoolean(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd","showComentariosRechazados").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setShowComentariosRechazados(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToBoolean(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd","showComentariosRecibidos").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setShowComentariosRecibidos(
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
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd","showNotasPublicadas").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setShowNotasPublicadas(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToBoolean(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd","showRecomendacionesRecibidas").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setShowRecomendacionesRecibidas(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToBoolean(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd","showValoracionesRecibidas").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setShowValoracionesRecibidas(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToBoolean(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd","showVisitasRecibidas").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setShowVisitasRecibidas(
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
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd","useCachedresults").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setUseCachedresults(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToBoolean(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd","usuario").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setUsuario(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
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
           
    