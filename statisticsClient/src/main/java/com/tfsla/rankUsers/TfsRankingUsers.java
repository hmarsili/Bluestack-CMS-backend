

/**
 * TfsRankingUsers.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.0  Built on : May 17, 2011 (04:19:43 IST)
 */

    package com.tfsla.rankUsers;

    /*
     *  TfsRankingUsers java interface
     */

    public interface TfsRankingUsers {
          
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
         */
        public void  removeStatistics(
         com.tfsla.rankUsers.RemoveStatistics removeStatistics0

        ) throws java.rmi.RemoteException
        
        ;

        

        /**
          * Auto generated method signature
          * 
                    * @param getStatistics1
                
         */

         
                     public com.tfsla.rankUsers.GetStatisticsResponse getStatistics(

                        com.tfsla.rankUsers.GetStatistics getStatistics1)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getStatistics1
            
          */
        public void startgetStatistics(

            com.tfsla.rankUsers.GetStatistics getStatistics1,

            final com.tfsla.rankUsers.TfsRankingUsersCallbackHandler callback)

            throws java.rmi.RemoteException;

     
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
         */
        public void  addHit(
         com.tfsla.rankUsers.AddHit addHit3

        ) throws java.rmi.RemoteException
        
        ;

        

        
       //
       }
    