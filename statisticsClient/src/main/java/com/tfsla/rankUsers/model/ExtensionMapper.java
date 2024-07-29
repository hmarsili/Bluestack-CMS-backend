
/**
 * ExtensionMapper.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.0  Built on : May 17, 2011 (04:21:18 IST)
 */

        
            package com.tfsla.rankUsers.model;
        
            /**
            *  ExtensionMapper class
            */
            @SuppressWarnings({"unchecked","unused"})
        
        public  class ExtensionMapper{

          public static java.lang.Object getTypeObject(java.lang.String namespaceURI,
                                                       java.lang.String typeName,
                                                       javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{

              
                  if (
                  "http://model.rankUsers.tfsla.com/xsd".equals(namespaceURI) &&
                  "TfsUserRankResults".equals(typeName)){
                   
                            return  com.tfsla.rankUsers.model.TfsUserRankResults.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://model.statistics.tfsla.com/xsd".equals(namespaceURI) &&
                  "TfsHitUser".equals(typeName)){
                   
                            return  com.tfsla.statistics.model.TfsHitUser.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://model.statistics.tfsla.com/xsd".equals(namespaceURI) &&
                  "TfsUserStatsOptions".equals(typeName)){
                   
                            return  com.tfsla.statistics.model.TfsUserStatsOptions.Factory.parse(reader);
                        

                  }

              
             throw new org.apache.axis2.databinding.ADBException("Unsupported type " + namespaceURI + " " + typeName);
          }

        }
    