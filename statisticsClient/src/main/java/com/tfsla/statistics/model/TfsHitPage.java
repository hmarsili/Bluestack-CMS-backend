
/**
 * TfsHitPage.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.0  Built on : May 17, 2011 (04:21:18 IST)
 */

            
                package com.tfsla.statistics.model;

import java.util.Arrays;
            

            /**
            *  TfsHitPage bean class
            */
            @SuppressWarnings({"unchecked","unused"})
        
        public  class TfsHitPage
        implements org.apache.axis2.databinding.ADBBean{
        /* This type was generated from the piece of schema that had
                name = TfsHitPage
                Namespace URI = http://model.statistics.tfsla.com/xsd
                Namespace Prefix = 
                */
            

                        /**
                        * field for URL
                        */

                        
                                    protected java.lang.String localURL ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localURLTracker = false ;

                           public boolean isURLSpecified(){
                               return localURLTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getURL(){
                               return localURL;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param URL
                               */
                               public void setURL(java.lang.String param){
                            localURLTracker = true;
                                   
                                            this.localURL=param;
                                    

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
                        * field for Cantidad
                        */

                        
                                    protected int localCantidad ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localCantidadTracker = false ;

                           public boolean isCantidadSpecified(){
                               return localCantidadTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return int
                           */
                           public  int getCantidad(){
                               return localCantidad;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Cantidad
                               */
                               public void setCantidad(int param){
                            
                                       // setting primitive attribute tracker to true
                                       localCantidadTracker =
                                       param != java.lang.Integer.MIN_VALUE;
                                   
                                            this.localCantidad=param;
                                    

                               }
                            

                        /**
                        * field for CantidadValoracion
                        */

                        
                                    protected int localCantidadValoracion ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localCantidadValoracionTracker = false ;

                           public boolean isCantidadValoracionSpecified(){
                               return localCantidadValoracionTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return int
                           */
                           public  int getCantidadValoracion(){
                               return localCantidadValoracion;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param CantidadValoracion
                               */
                               public void setCantidadValoracion(int param){
                            
                                       // setting primitive attribute tracker to true
                                       localCantidadValoracionTracker =
                                       param != java.lang.Integer.MIN_VALUE;
                                   
                                            this.localCantidadValoracion=param;
                                    

                               }
                            

                        /**
                        * field for Comentarios
                        */

                        
                                    protected int localComentarios ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localComentariosTracker = false ;

                           public boolean isComentariosSpecified(){
                               return localComentariosTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return int
                           */
                           public  int getComentarios(){
                               return localComentarios;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Comentarios
                               */
                               public void setComentarios(int param){
                            
                                       // setting primitive attribute tracker to true
                                       localComentariosTracker =
                                       param != java.lang.Integer.MIN_VALUE;
                                   
                                            this.localComentarios=param;
                                    

                               }
                            

                        /**
                        * field for Custom1
                        */

                        
                                    protected int localCustom1 ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localCustom1Tracker = false ;

                           public boolean isCustom1Specified(){
                               return localCustom1Tracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return int
                           */
                           public  int getCustom1(){
                               return localCustom1;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Custom1
                               */
                               public void setCustom1(int param){
                            
                                       // setting primitive attribute tracker to true
                                       localCustom1Tracker =
                                       param != java.lang.Integer.MIN_VALUE;
                                   
                                            this.localCustom1=param;
                                    

                               }
                            

                        /**
                        * field for Custom10
                        */

                        
                                    protected int localCustom10 ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localCustom10Tracker = false ;

                           public boolean isCustom10Specified(){
                               return localCustom10Tracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return int
                           */
                           public  int getCustom10(){
                               return localCustom10;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Custom10
                               */
                               public void setCustom10(int param){
                            
                                       // setting primitive attribute tracker to true
                                       localCustom10Tracker =
                                       param != java.lang.Integer.MIN_VALUE;
                                   
                                            this.localCustom10=param;
                                    

                               }
                            

                        /**
                        * field for Custom2
                        */

                        
                                    protected int localCustom2 ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localCustom2Tracker = false ;

                           public boolean isCustom2Specified(){
                               return localCustom2Tracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return int
                           */
                           public  int getCustom2(){
                               return localCustom2;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Custom2
                               */
                               public void setCustom2(int param){
                            
                                       // setting primitive attribute tracker to true
                                       localCustom2Tracker =
                                       param != java.lang.Integer.MIN_VALUE;
                                   
                                            this.localCustom2=param;
                                    

                               }
                            

                        /**
                        * field for Custom3
                        */

                        
                                    protected int localCustom3 ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localCustom3Tracker = false ;

                           public boolean isCustom3Specified(){
                               return localCustom3Tracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return int
                           */
                           public  int getCustom3(){
                               return localCustom3;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Custom3
                               */
                               public void setCustom3(int param){
                            
                                       // setting primitive attribute tracker to true
                                       localCustom3Tracker =
                                       param != java.lang.Integer.MIN_VALUE;
                                   
                                            this.localCustom3=param;
                                    

                               }
                            

                        /**
                        * field for Custom4
                        */

                        
                                    protected int localCustom4 ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localCustom4Tracker = false ;

                           public boolean isCustom4Specified(){
                               return localCustom4Tracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return int
                           */
                           public  int getCustom4(){
                               return localCustom4;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Custom4
                               */
                               public void setCustom4(int param){
                            
                                       // setting primitive attribute tracker to true
                                       localCustom4Tracker =
                                       param != java.lang.Integer.MIN_VALUE;
                                   
                                            this.localCustom4=param;
                                    

                               }
                            

                        /**
                        * field for Custom5
                        */

                        
                                    protected int localCustom5 ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localCustom5Tracker = false ;

                           public boolean isCustom5Specified(){
                               return localCustom5Tracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return int
                           */
                           public  int getCustom5(){
                               return localCustom5;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Custom5
                               */
                               public void setCustom5(int param){
                            
                                       // setting primitive attribute tracker to true
                                       localCustom5Tracker =
                                       param != java.lang.Integer.MIN_VALUE;
                                   
                                            this.localCustom5=param;
                                    

                               }
                            

                        /**
                        * field for Custom6
                        */

                        
                                    protected int localCustom6 ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localCustom6Tracker = false ;

                           public boolean isCustom6Specified(){
                               return localCustom6Tracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return int
                           */
                           public  int getCustom6(){
                               return localCustom6;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Custom6
                               */
                               public void setCustom6(int param){
                            
                                       // setting primitive attribute tracker to true
                                       localCustom6Tracker =
                                       param != java.lang.Integer.MIN_VALUE;
                                   
                                            this.localCustom6=param;
                                    

                               }
                            

                        /**
                        * field for Custom7
                        */

                        
                                    protected int localCustom7 ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localCustom7Tracker = false ;

                           public boolean isCustom7Specified(){
                               return localCustom7Tracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return int
                           */
                           public  int getCustom7(){
                               return localCustom7;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Custom7
                               */
                               public void setCustom7(int param){
                            
                                       // setting primitive attribute tracker to true
                                       localCustom7Tracker =
                                       param != java.lang.Integer.MIN_VALUE;
                                   
                                            this.localCustom7=param;
                                    

                               }
                            

                        /**
                        * field for Custom8
                        */

                        
                                    protected int localCustom8 ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localCustom8Tracker = false ;

                           public boolean isCustom8Specified(){
                               return localCustom8Tracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return int
                           */
                           public  int getCustom8(){
                               return localCustom8;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Custom8
                               */
                               public void setCustom8(int param){
                            
                                       // setting primitive attribute tracker to true
                                       localCustom8Tracker =
                                       param != java.lang.Integer.MIN_VALUE;
                                   
                                            this.localCustom8=param;
                                    

                               }
                            

                        /**
                        * field for Custom9
                        */

                        
                                    protected int localCustom9 ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localCustom9Tracker = false ;

                           public boolean isCustom9Specified(){
                               return localCustom9Tracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return int
                           */
                           public  int getCustom9(){
                               return localCustom9;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Custom9
                               */
                               public void setCustom9(int param){
                            
                                       // setting primitive attribute tracker to true
                                       localCustom9Tracker =
                                       param != java.lang.Integer.MIN_VALUE;
                                   
                                            this.localCustom9=param;
                                    

                               }
                            

                        /**
                        * field for GeneralRank
                        */

                        
                                    protected float localGeneralRank ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localGeneralRankTracker = false ;

                           public boolean isGeneralRankSpecified(){
                               return localGeneralRankTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return float
                           */
                           public  float getGeneralRank(){
                               return localGeneralRank;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param GeneralRank
                               */
                               public void setGeneralRank(float param){
                            
                                       // setting primitive attribute tracker to true
                                       localGeneralRankTracker =
                                       !java.lang.Float.isNaN(param);
                                   
                                            this.localGeneralRank=param;
                                    

                               }
                            

                        /**
                        * field for Recomendacion
                        */

                        
                                    protected int localRecomendacion ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localRecomendacionTracker = false ;

                           public boolean isRecomendacionSpecified(){
                               return localRecomendacionTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return int
                           */
                           public  int getRecomendacion(){
                               return localRecomendacion;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Recomendacion
                               */
                               public void setRecomendacion(int param){
                            
                                       // setting primitive attribute tracker to true
                                       localRecomendacionTracker =
                                       param != java.lang.Integer.MIN_VALUE;
                                   
                                            this.localRecomendacion=param;
                                    

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
                        * field for Valoracion
                        */

                        
                                    protected int localValoracion ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localValoracionTracker = false ;

                           public boolean isValoracionSpecified(){
                               return localValoracionTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return int
                           */
                           public  int getValoracion(){
                               return localValoracion;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Valoracion
                               */
                               public void setValoracion(int param){
                            
                                       // setting primitive attribute tracker to true
                                       localValoracionTracker =
                                       param != java.lang.Integer.MIN_VALUE;
                                   
                                            this.localValoracion=param;
                                    

                               }
                            

                        /**
                        * field for Values
                        * This was an Array!
                        */

                        
                                    protected com.tfsla.statistics.model.TfsKeyValue[] localValues ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localValuesTracker = false ;

                           public boolean isValuesSpecified(){
                               return localValuesTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return com.tfsla.statistics.model.TfsKeyValue[]
                           */
                           public  com.tfsla.statistics.model.TfsKeyValue[] getValues(){
                               return localValues;
                           }

                           
                        


                               
                              /**
                               * validate the array for Values
                               */
                              protected void validateValues(com.tfsla.statistics.model.TfsKeyValue[] param){
                             
                              }


                             /**
                              * Auto generated setter method
                              * @param param Values
                              */
                              public void setValues(com.tfsla.statistics.model.TfsKeyValue[] param){
                              
                                   validateValues(param);

                               localValuesTracker = true;
                                      
                                      this.localValues=param;
                              }

                               
                             
                             /**
                             * Auto generated add method for the array for convenience
                             * @param param com.tfsla.statistics.model.TfsKeyValue
                             */
                             public void addValues(com.tfsla.statistics.model.TfsKeyValue param){
                                   if (localValues == null){
                                   localValues = new com.tfsla.statistics.model.TfsKeyValue[]{};
                                   }

                            
                                 //update the setting tracker
                                localValuesTracker = true;
                            

                               java.util.List list =
                            org.apache.axis2.databinding.utils.ConverterUtil.toList(localValues);
                               list.add(param);
                               this.localValues =
                             (com.tfsla.statistics.model.TfsKeyValue[])list.toArray(
                            new com.tfsla.statistics.model.TfsKeyValue[list.size()]);

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
                           namespacePrefix+":TfsHitPage",
                           xmlWriter);
                   } else {
                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                           "TfsHitPage",
                           xmlWriter);
                   }

               
                   }
                if (localURLTracker){
                                    namespace = "http://model.statistics.tfsla.com/xsd";
                                    writeStartElement(null, namespace, "URL", xmlWriter);
                             

                                          if (localURL==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localURL);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localAutorTracker){
                                    namespace = "http://model.statistics.tfsla.com/xsd";
                                    writeStartElement(null, namespace, "autor", xmlWriter);
                             

                                          if (localAutor==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localAutor);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localCantidadTracker){
                                    namespace = "http://model.statistics.tfsla.com/xsd";
                                    writeStartElement(null, namespace, "cantidad", xmlWriter);
                             
                                               if (localCantidad==java.lang.Integer.MIN_VALUE) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("cantidad cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localCantidad));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localCantidadValoracionTracker){
                                    namespace = "http://model.statistics.tfsla.com/xsd";
                                    writeStartElement(null, namespace, "cantidadValoracion", xmlWriter);
                             
                                               if (localCantidadValoracion==java.lang.Integer.MIN_VALUE) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("cantidadValoracion cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localCantidadValoracion));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localComentariosTracker){
                                    namespace = "http://model.statistics.tfsla.com/xsd";
                                    writeStartElement(null, namespace, "comentarios", xmlWriter);
                             
                                               if (localComentarios==java.lang.Integer.MIN_VALUE) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("comentarios cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localComentarios));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localCustom1Tracker){
                                    namespace = "http://model.statistics.tfsla.com/xsd";
                                    writeStartElement(null, namespace, "custom1", xmlWriter);
                             
                                               if (localCustom1==java.lang.Integer.MIN_VALUE) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("custom1 cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localCustom1));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localCustom10Tracker){
                                    namespace = "http://model.statistics.tfsla.com/xsd";
                                    writeStartElement(null, namespace, "custom10", xmlWriter);
                             
                                               if (localCustom10==java.lang.Integer.MIN_VALUE) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("custom10 cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localCustom10));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localCustom2Tracker){
                                    namespace = "http://model.statistics.tfsla.com/xsd";
                                    writeStartElement(null, namespace, "custom2", xmlWriter);
                             
                                               if (localCustom2==java.lang.Integer.MIN_VALUE) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("custom2 cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localCustom2));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localCustom3Tracker){
                                    namespace = "http://model.statistics.tfsla.com/xsd";
                                    writeStartElement(null, namespace, "custom3", xmlWriter);
                             
                                               if (localCustom3==java.lang.Integer.MIN_VALUE) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("custom3 cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localCustom3));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localCustom4Tracker){
                                    namespace = "http://model.statistics.tfsla.com/xsd";
                                    writeStartElement(null, namespace, "custom4", xmlWriter);
                             
                                               if (localCustom4==java.lang.Integer.MIN_VALUE) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("custom4 cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localCustom4));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localCustom5Tracker){
                                    namespace = "http://model.statistics.tfsla.com/xsd";
                                    writeStartElement(null, namespace, "custom5", xmlWriter);
                             
                                               if (localCustom5==java.lang.Integer.MIN_VALUE) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("custom5 cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localCustom5));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localCustom6Tracker){
                                    namespace = "http://model.statistics.tfsla.com/xsd";
                                    writeStartElement(null, namespace, "custom6", xmlWriter);
                             
                                               if (localCustom6==java.lang.Integer.MIN_VALUE) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("custom6 cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localCustom6));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localCustom7Tracker){
                                    namespace = "http://model.statistics.tfsla.com/xsd";
                                    writeStartElement(null, namespace, "custom7", xmlWriter);
                             
                                               if (localCustom7==java.lang.Integer.MIN_VALUE) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("custom7 cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localCustom7));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localCustom8Tracker){
                                    namespace = "http://model.statistics.tfsla.com/xsd";
                                    writeStartElement(null, namespace, "custom8", xmlWriter);
                             
                                               if (localCustom8==java.lang.Integer.MIN_VALUE) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("custom8 cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localCustom8));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localCustom9Tracker){
                                    namespace = "http://model.statistics.tfsla.com/xsd";
                                    writeStartElement(null, namespace, "custom9", xmlWriter);
                             
                                               if (localCustom9==java.lang.Integer.MIN_VALUE) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("custom9 cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localCustom9));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localGeneralRankTracker){
                                    namespace = "http://model.statistics.tfsla.com/xsd";
                                    writeStartElement(null, namespace, "generalRank", xmlWriter);
                             
                                               if (java.lang.Float.isNaN(localGeneralRank)) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("generalRank cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localGeneralRank));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localRecomendacionTracker){
                                    namespace = "http://model.statistics.tfsla.com/xsd";
                                    writeStartElement(null, namespace, "recomendacion", xmlWriter);
                             
                                               if (localRecomendacion==java.lang.Integer.MIN_VALUE) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("recomendacion cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localRecomendacion));
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
                             } if (localValoracionTracker){
                                    namespace = "http://model.statistics.tfsla.com/xsd";
                                    writeStartElement(null, namespace, "valoracion", xmlWriter);
                             
                                               if (localValoracion==java.lang.Integer.MIN_VALUE) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("valoracion cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localValoracion));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localValuesTracker){
                                       if (localValues!=null){
                                            for (int i = 0;i < localValues.length;i++){
                                                if (localValues[i] != null){
                                                 localValues[i].serialize(new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd","values"),
                                                           xmlWriter);
                                                } else {
                                                   
                                                            writeStartElement(null, "http://model.statistics.tfsla.com/xsd", "values", xmlWriter);

                                                           // write the nil attribute
                                                           writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                           xmlWriter.writeEndElement();
                                                    
                                                }

                                            }
                                     } else {
                                        
                                                writeStartElement(null, "http://model.statistics.tfsla.com/xsd", "values", xmlWriter);

                                               // write the nil attribute
                                               writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                               xmlWriter.writeEndElement();
                                        
                                    }
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

                 if (localURLTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd",
                                                                      "URL"));
                                 
                                         elementList.add(localURL==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localURL));
                                    } if (localAutorTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd",
                                                                      "autor"));
                                 
                                         elementList.add(localAutor==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localAutor));
                                    } if (localCantidadTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd",
                                                                      "cantidad"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localCantidad));
                            } if (localCantidadValoracionTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd",
                                                                      "cantidadValoracion"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localCantidadValoracion));
                            } if (localComentariosTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd",
                                                                      "comentarios"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localComentarios));
                            } if (localCustom1Tracker){
                                      elementList.add(new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd",
                                                                      "custom1"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localCustom1));
                            } if (localCustom10Tracker){
                                      elementList.add(new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd",
                                                                      "custom10"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localCustom10));
                            } if (localCustom2Tracker){
                                      elementList.add(new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd",
                                                                      "custom2"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localCustom2));
                            } if (localCustom3Tracker){
                                      elementList.add(new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd",
                                                                      "custom3"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localCustom3));
                            } if (localCustom4Tracker){
                                      elementList.add(new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd",
                                                                      "custom4"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localCustom4));
                            } if (localCustom5Tracker){
                                      elementList.add(new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd",
                                                                      "custom5"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localCustom5));
                            } if (localCustom6Tracker){
                                      elementList.add(new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd",
                                                                      "custom6"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localCustom6));
                            } if (localCustom7Tracker){
                                      elementList.add(new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd",
                                                                      "custom7"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localCustom7));
                            } if (localCustom8Tracker){
                                      elementList.add(new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd",
                                                                      "custom8"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localCustom8));
                            } if (localCustom9Tracker){
                                      elementList.add(new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd",
                                                                      "custom9"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localCustom9));
                            } if (localGeneralRankTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd",
                                                                      "generalRank"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localGeneralRank));
                            } if (localRecomendacionTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd",
                                                                      "recomendacion"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localRecomendacion));
                            } if (localSitioTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd",
                                                                      "sitio"));
                                 
                                         elementList.add(localSitio==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localSitio));
                                    } if (localTipoContenidoTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd",
                                                                      "tipoContenido"));
                                 
                                         elementList.add(localTipoContenido==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localTipoContenido));
                                    } if (localValoracionTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd",
                                                                      "valoracion"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localValoracion));
                            } if (localValuesTracker){
                             if (localValues!=null) {
                                 for (int i = 0;i < localValues.length;i++){

                                    if (localValues[i] != null){
                                         elementList.add(new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd",
                                                                          "values"));
                                         elementList.add(localValues[i]);
                                    } else {
                                        
                                                elementList.add(new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd",
                                                                          "values"));
                                                elementList.add(null);
                                            
                                    }

                                 }
                             } else {
                                 
                                        elementList.add(new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd",
                                                                          "values"));
                                        elementList.add(localValues);
                                    
                             }

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
        public static TfsHitPage parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
            TfsHitPage object =
                new TfsHitPage();

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
                    
                            if (!"TfsHitPage".equals(type)){
                                //find namespace for the prefix
                                java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                                return (TfsHitPage)com.tfsla.statistics.model.ExtensionMapper.getTypeObject(
                                     nsUri,type,reader);
                              }
                        

                  }
                

                }

                

                
                // Note all attributes that were handled. Used to differ normal attributes
                // from anyAttributes.
                java.util.Vector handledAttributes = new java.util.Vector();
                

                
                    
                    reader.next();
                
                        java.util.ArrayList list21 = new java.util.ArrayList();
                    
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd","URL").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setURL(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
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
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd","cantidad").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setCantidad(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToInt(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                               object.setCantidad(java.lang.Integer.MIN_VALUE);
                                           
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd","cantidadValoracion").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setCantidadValoracion(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToInt(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                               object.setCantidadValoracion(java.lang.Integer.MIN_VALUE);
                                           
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd","comentarios").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setComentarios(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToInt(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                               object.setComentarios(java.lang.Integer.MIN_VALUE);
                                           
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd","custom1").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setCustom1(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToInt(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                               object.setCustom1(java.lang.Integer.MIN_VALUE);
                                           
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd","custom10").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setCustom10(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToInt(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                               object.setCustom10(java.lang.Integer.MIN_VALUE);
                                           
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd","custom2").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setCustom2(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToInt(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                               object.setCustom2(java.lang.Integer.MIN_VALUE);
                                           
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd","custom3").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setCustom3(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToInt(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                               object.setCustom3(java.lang.Integer.MIN_VALUE);
                                           
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd","custom4").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setCustom4(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToInt(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                               object.setCustom4(java.lang.Integer.MIN_VALUE);
                                           
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd","custom5").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setCustom5(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToInt(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                               object.setCustom5(java.lang.Integer.MIN_VALUE);
                                           
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd","custom6").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setCustom6(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToInt(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                               object.setCustom6(java.lang.Integer.MIN_VALUE);
                                           
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd","custom7").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setCustom7(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToInt(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                               object.setCustom7(java.lang.Integer.MIN_VALUE);
                                           
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd","custom8").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setCustom8(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToInt(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                               object.setCustom8(java.lang.Integer.MIN_VALUE);
                                           
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd","custom9").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setCustom9(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToInt(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                               object.setCustom9(java.lang.Integer.MIN_VALUE);
                                           
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd","generalRank").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setGeneralRank(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToFloat(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                               object.setGeneralRank(java.lang.Float.NaN);
                                           
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd","recomendacion").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setRecomendacion(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToInt(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                               object.setRecomendacion(java.lang.Integer.MIN_VALUE);
                                           
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
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd","valoracion").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setValoracion(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToInt(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                               object.setValoracion(java.lang.Integer.MIN_VALUE);
                                           
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd","values").equals(reader.getName())){
                                
                                    
                                    
                                    // Process the array and step past its final element's end.
                                    
                                                          nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                          if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                              list21.add(null);
                                                              reader.next();
                                                          } else {
                                                        list21.add(com.tfsla.statistics.model.TfsKeyValue.Factory.parse(reader));
                                                                }
                                                        //loop until we find a start element that is not part of this array
                                                        boolean loopDone21 = false;
                                                        while(!loopDone21){
                                                            // We should be at the end element, but make sure
                                                            while (!reader.isEndElement())
                                                                reader.next();
                                                            // Step out of this element
                                                            reader.next();
                                                            // Step to next element event.
                                                            while (!reader.isStartElement() && !reader.isEndElement())
                                                                reader.next();
                                                            if (reader.isEndElement()){
                                                                //two continuous end elements means we are exiting the xml structure
                                                                loopDone21 = true;
                                                            } else {
                                                                if (new javax.xml.namespace.QName("http://model.statistics.tfsla.com/xsd","values").equals(reader.getName())){
                                                                    
                                                                      nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                                      if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                                          list21.add(null);
                                                                          reader.next();
                                                                      } else {
                                                                    list21.add(com.tfsla.statistics.model.TfsKeyValue.Factory.parse(reader));
                                                                        }
                                                                }else{
                                                                    loopDone21 = true;
                                                                }
                                                            }
                                                        }
                                                        // call the converter utility  to convert and set the array
                                                        
                                                        object.setValues((com.tfsla.statistics.model.TfsKeyValue[])
                                                            org.apache.axis2.databinding.utils.ConverterUtil.convertToArray(
                                                                com.tfsla.statistics.model.TfsKeyValue.class,
                                                                list21));
                                                            
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



	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((localAutor == null) ? 0 : localAutor.hashCode());
		result = prime * result
				+ ((localSitio == null) ? 0 : localSitio.hashCode());
		result = prime
				* result
				+ ((localTipoContenido == null) ? 0 : localTipoContenido
						.hashCode());
		result = prime * result
				+ ((localURL == null) ? 0 : localURL.hashCode());
		result = prime * result + Arrays.hashCode(localValues);
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
		TfsHitPage other = (TfsHitPage) obj;
		if (localAutor == null) {
			if (other.localAutor != null)
				return false;
		} else if (!localAutor.equals(other.localAutor))
			return false;
		if (localSitio == null) {
			if (other.localSitio != null)
				return false;
		} else if (!localSitio.equals(other.localSitio))
			return false;
		if (localTipoContenido == null) {
			if (other.localTipoContenido != null)
				return false;
		} else if (!localTipoContenido.equals(other.localTipoContenido))
			return false;
		if (localURL == null) {
			if (other.localURL != null)
				return false;
		} else if (!localURL.equals(other.localURL))
			return false;
		if (!Arrays.equals(localValues, other.localValues))
			return false;
		return true;
	}

        

        }
           
    