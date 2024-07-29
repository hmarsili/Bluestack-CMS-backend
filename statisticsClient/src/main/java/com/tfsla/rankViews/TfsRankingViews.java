

/**
 * TfsRankingViews.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.0  Built on : May 17, 2011 (04:19:43 IST)
 */

    package com.tfsla.rankViews;

    /*
     *  TfsRankingViews java interface
     */

    public interface TfsRankingViews {
          

        /**
          * Auto generated method signature
          * 
                    * @param getStatistics63
                
         */

         
                     public com.tfsla.rankViews.GetStatisticsResponse getStatistics(

                        com.tfsla.rankViews.GetStatistics getStatistics63)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getStatistics63
            
          */
        public void startgetStatistics(

            com.tfsla.rankViews.GetStatistics getStatistics63,

            final com.tfsla.rankViews.TfsRankingViewsCallbackHandler callback)

            throws java.rmi.RemoteException;

     
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
         */
        public void  addCustomEvents(
         com.tfsla.rankViews.AddCustomEvents addCustomEvents65

        ) throws java.rmi.RemoteException
        
        ;

        
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
         */
        public void  addHit(
         com.tfsla.rankViews.AddHit addHit66

        ) throws java.rmi.RemoteException
        
        ;

        /**
         * Auto generated method signature for Asynchronous Invocations
         * 
         */
        public void  putTags(
         com.tfsla.rankViews.PutTags putTags72

        ) throws java.rmi.RemoteException
        
        ;

        
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
         */
        public void  addComentario(
         com.tfsla.rankViews.AddComentario addComentario67

        ) throws java.rmi.RemoteException
        
        ;

        
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
         */
        public void  addHits(
         com.tfsla.rankViews.AddHits addHits68

        ) throws java.rmi.RemoteException
        
        ;

        
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
         */
        public void  addValoracion(
         com.tfsla.rankViews.AddValoracion addValoracion69

        ) throws java.rmi.RemoteException
        
        ;

        
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
         */
        public void  removeStatistics(
         com.tfsla.rankViews.RemoveStatistics removeStatistics70

        ) throws java.rmi.RemoteException
        
        ;

        
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
         */
        public void  addRecomendacion(
         com.tfsla.rankViews.AddRecomendacion addRecomendacion71

        ) throws java.rmi.RemoteException
        
        ;

        

        
       //
       }
    