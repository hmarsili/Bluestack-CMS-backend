
/**
 * ExtensionMapper.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.0  Built on : May 17, 2011 (04:21:18 IST)
 */

        
            package com.tfsla.statistics.model;
        
            /**
            *  ExtensionMapper class
            */
            @SuppressWarnings({"unchecked","unused"})
        
        public  class ExtensionMapper{

          public static java.lang.Object getTypeObject(java.lang.String namespaceURI,
                                                       java.lang.String typeName,
                                                       javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{

              
                  if (
                  "http://model.rankViews.tfsla.com/xsd".equals(namespaceURI) &&
                  "TfsRankResults".equals(typeName)){
                   
                            return  com.tfsla.rankViews.model.TfsRankResults.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://model.statistics.tfsla.com/xsd".equals(namespaceURI) &&
                  "TfsStatisticsOptions".equals(typeName)){
                   
                            return  com.tfsla.statistics.model.TfsStatisticsOptions.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://model.statistics.tfsla.com/xsd".equals(namespaceURI) &&
                  "TfsHitPage".equals(typeName)){
                   
                            return  com.tfsla.statistics.model.TfsHitPage.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://model.statistics.tfsla.com/xsd".equals(namespaceURI) &&
                  "TfsKeyValue".equals(typeName)){
                   
                            return  com.tfsla.statistics.model.TfsKeyValue.Factory.parse(reader);
                        

                  }

              
             throw new org.apache.axis2.databinding.ADBException("Unsupported type " + namespaceURI + " " + typeName);
          }

        }
    