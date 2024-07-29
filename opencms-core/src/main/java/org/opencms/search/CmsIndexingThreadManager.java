/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/search/CmsIndexingThreadManager.java,v $
 * Date   : $Date: 2011/03/23 14:52:48 $
 * Version: $Revision: 1.36 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2011 Alkacon Software GmbH (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.search;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.report.CmsLogReport;
import org.opencms.report.I_CmsReport;
import org.opencms.search.documents.I_CmsDocumentFactory;
import org.opencms.search.fields.CmsSearchField;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.logging.Log;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;

/**
 * Implements the management of indexing threads.<p>
 * 
 * @author Carsten Weinholz 
 * @author Alexander Kandzior
 * 
 * @version $Revision: 1.36 $ 
 * 
 * @since 6.0.0 
 */
public class CmsIndexingThreadManager {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsIndexingThreadManager.class);

    /** Number of threads abandoned. */
    private int m_abandonedCounter;

    /** The time the last error was written to the log. */
    private long m_lastLogErrorTime;

    /** The time the last warning was written to the log. */
    private long m_lastLogWarnTime;

    /** Number of thread returned. */
    private int m_returnedCounter;

    /** Overall number of threads started. */
    private int m_startedCounter;

    /** Timeout for abandoning threads. */
    private long m_timeout;

    /** The maximum number of modifications before a commit in the search index is triggered. */
    private int m_maxModificationsBeforeCommit;

    /**
     * Creates and starts a thread manager for indexing threads.<p>
     * 
     * @param timeout timeout after a thread is abandoned
     * @param maxModificationsBeforeCommit the maximum number of modifications before a commit in the search index is triggered
     */
    public CmsIndexingThreadManager(long timeout, int maxModificationsBeforeCommit) {

        m_timeout = timeout;
        m_maxModificationsBeforeCommit = maxModificationsBeforeCommit;
    }

    /**
     * Creates and starts a new indexing thread for a resource.<p>
     * 
     * After an indexing thread was started, the manager suspends itself 
     * and waits for an amount of time specified by the <code>timeout</code>
     * value. If the timeout value is reached, the indexing thread is
     * aborted by an interrupt signal.<p>
     * 
     * @param cms the cms object
     * @param writer the write to write the index
     * @param res the resource
     * @param index the index
     * @param report the report to write the indexing progress to
     */
    public void createIndexingThread(
        CmsObject cms,
        IndexWriter writer,
        CmsResource res,
        CmsSearchIndex index,
        I_CmsReport report) {

        // check if this resource should be excluded from the index, if so skip it
        boolean excludeFromIndex = false;
        try {
            // do property lookup with folder search
            excludeFromIndex = Boolean.valueOf(
                cms.readPropertyObject(res, CmsPropertyDefinition.PROPERTY_SEARCH_EXCLUDE, true).getValue()).booleanValue();
        } catch (CmsException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_UNABLE_TO_READ_PROPERTY_1, res.getRootPath()));
            }
        }

        if (!excludeFromIndex) {
            // check if any resource default locale has a match with the index locale, if not skip resource
            List<Locale> locales = OpenCms.getLocaleManager().getDefaultLocales(cms, res);
            Locale match = OpenCms.getLocaleManager().getFirstMatchingLocale(
                Collections.singletonList(index.getLocale()),
                locales);
            excludeFromIndex = (match == null);
        }
        
        I_CmsDocumentFactory documentType = null;
        if (!excludeFromIndex) {
            // don't get document type if excluded from index, this will lead to exclusion of resource
            documentType = index.getDocumentFactory(res);
        }
        if (documentType == null) {
            // this resource is not contained in the given search index
            m_startedCounter++;
            m_returnedCounter++;
            if (report != null) {
                report.println(
                    org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_SKIPPED_0),
                    I_CmsReport.FORMAT_NOTE);
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_SKIPPED_1, res.getRootPath()));
            }

            // no need to continue
            return;
        }

        // extract the content from the resource in a separate Thread
        CmsIndexingThread thread = new CmsIndexingThread(cms, writer, res, documentType, index, report);
        m_startedCounter++;
        thread.setPriority(Thread.NORM_PRIORITY);
        
        Date now = new Date();
        DateFormat format = new SimpleDateFormat("MM:ss SSS");
        thread.start();
        
//        //Only for debug
//        long timeout = m_timeout;
//        
//        java.util.Random ran = new java.util.Random();
//        if (ran.nextInt(5) >=3) timeout = 2;
//        //end!
        
        try {
        	
//        	if (ran.nextInt(5) >=3) thread.interrupt();
//        	thread.join(timeout);
        	
        	thread.join(m_timeout);
        } catch (InterruptedException e) {
        	LOG.error("Interrupted during content extraction. Time elapsed since thread start: " + format.format(now) + " - " + format.format(new Date()) , e);
        }
        if (thread.isAlive()) {
            if (LOG.isWarnEnabled()) {
                LOG.warn(Messages.get().getBundle().key(Messages.LOG_INDEXING_TIMEOUT_1, res.getRootPath()));
            }
            if (report != null) {
                report.println();
                report.print(
                    org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_FAILED_0),
                    I_CmsReport.FORMAT_WARNING);
                report.println(
                    Messages.get().container(Messages.RPT_SEARCH_INDEXING_TIMEOUT_1, res.getRootPath()),
                    I_CmsReport.FORMAT_WARNING);
            }
            m_abandonedCounter++;
            thread.logStatus();
            thread.interrupt();
        } else {
            m_returnedCounter++;
        }
        
        Document doc = thread.getResult();
        Term pathTerm = new Term(CmsSearchField.FIELD_PATH, res.getRootPath());
        
        if (doc != null) {
            // write the document to the index
        	try {
        		
        		if (!index.isOpen()) {
        			LOG.error("The index"  + index.getName() + " was closed. Trying to reopen it ", writer.getTragicException());
					try {				
						writer = index.reopen();
					} catch (CmsIndexException e) { 
						 LOG.error("failed during updating the index " + index.getName() + " with resource " + res.getRootPath(), e);
						 throw e;
					}
        		}
        		
        		
                //Agregado si tiene que excluir por condicion avanzada del indice
                if (index.excludeResource(cms, res)) {
                    // this resource is not contained in the given search index
    				writer.deleteDocuments(pathTerm);
                    
                    
                    if (report != null) {
                        report.println(
                            org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_EXCLUDED_0),
                            I_CmsReport.FORMAT_NOTE);
                    }
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(Messages.get().getBundle().key(Messages.LOG_EXCLUDED_1, res.getRootPath()));
                    }
                	
                }
                else {

					writer.updateDocument(pathTerm, doc);
					
					report.println(
		                    org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_OK_0),
		                    I_CmsReport.FORMAT_OK);
	
	                if (LOG.isDebugEnabled()) {
	                    LOG.debug(Messages.get().getBundle().key(Messages.LOG_WRITE_SUCCESS_0));
	                }
                }
			} catch (IOException e) {
				// TODO Auto-generated catch block
				report.println(org.opencms.report.Messages.get().container(
                        org.opencms.report.Messages.RPT_FAILED_0), I_CmsReport.FORMAT_ERROR);
                    report.println(org.opencms.report.Messages.get().container(
                        org.opencms.report.Messages.RPT_ARGUMENT_1,
                        e.toString()), I_CmsReport.FORMAT_ERROR);
                    
                 LOG.error("failed during updating index " + index.getName() + " with resource " + res.getRootPath(), e);
			} catch (CmsIndexException e) {
				report.println(org.opencms.report.Messages.get().container(
                        org.opencms.report.Messages.RPT_FAILED_0), I_CmsReport.FORMAT_ERROR);
                    report.println(org.opencms.report.Messages.get().container(
                        org.opencms.report.Messages.RPT_ARGUMENT_1,
                        e.toString()), I_CmsReport.FORMAT_ERROR);			
                    LOG.error("failed during updating index " + index.getName() + " with resource " + res.getRootPath(), e);
			}
        }
        else {
        	try {
				writer.deleteDocuments(pathTerm);
				//writer.commit();
				//index.searchManagerRestore();
				
			} catch (IOException e) {
				// TODO Auto-generated catreport.println(org.opencms.report.Messages.get().container(
				report.println(org.opencms.report.Messages.get().container(
                        org.opencms.report.Messages.RPT_FAILED_0), I_CmsReport.FORMAT_ERROR);
            report.println(org.opencms.report.Messages.get().container(
                org.opencms.report.Messages.RPT_ARGUMENT_1,
                e.toString()), I_CmsReport.FORMAT_ERROR);e.printStackTrace();
			}
        }
        
        if ((m_startedCounter % m_maxModificationsBeforeCommit) == 0) {
            try {
            	if (LOG.isWarnEnabled()) {
            		LOG.warn(Messages.get().getBundle().key(
                            Messages.LOG_IO_INDEX_WRITER_COMMIT_START_1,
                            index.getName(),
                            index.getPath(),
                            new java.util.Date().toString())
            				);
                }
            	
        		if (!index.isOpen()) {
        			LOG.error("The index"  + index.getName() + " was closed. Trying to reopen it ", writer.getTragicException());
					try {				
						writer = index.reopen();
					} catch (CmsIndexException e) { 
						 LOG.error("failed during commiting the index " + index.getName() + " with resource " + res.getRootPath(), e);
					}
        		}
                writer.commit();
                
                if (LOG.isWarnEnabled()) {
            		LOG.warn(Messages.get().getBundle().key(
                            Messages.LOG_IO_INDEX_WRITER_COMMIT_STOP_1,
                            index.getName(),
                            index.getPath(),
                            new java.util.Date().toString())
            				);
                }
            } catch (IOException e) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn(Messages.get().getBundle().key(
                        Messages.LOG_IO_INDEX_WRITER_COMMIT_2,
                        index.getName(),
                        index.getPath()), e);
                }
            }
        }
    }

    /**
     * Gets the current thread (file) count.<p>
     * 
     * @return the current thread count
     */
    public int getCounter() {

        return m_startedCounter;
    }

    /**
     * Returns if the indexing manager still have indexing threads.<p>
     * 
     * @return true if the indexing manager still have indexing threads
     */
    public boolean isRunning() {

        if (m_lastLogErrorTime <= 0) {
            m_lastLogErrorTime = System.currentTimeMillis();
            m_lastLogWarnTime = m_lastLogErrorTime;
        } else {
            long currentTime = System.currentTimeMillis();
            if ((currentTime - m_lastLogWarnTime) > 30000) {
                // write warning to log after 30 seconds
                if (LOG.isWarnEnabled()) {
                    LOG.warn(Messages.get().getBundle().key(
                        Messages.LOG_WAITING_ABANDONED_THREADS_2,
                        new Integer(m_abandonedCounter),
                        new Integer((m_startedCounter - m_returnedCounter))));
                }
                m_lastLogWarnTime = currentTime;
            }
            if ((currentTime - m_lastLogErrorTime) > 600000) {
                // write error to log after 10 minutes
                LOG.error(Messages.get().getBundle().key(
                    Messages.LOG_WAITING_ABANDONED_THREADS_2,
                    new Integer(m_abandonedCounter),
                    new Integer((m_startedCounter - m_returnedCounter))));
                m_lastLogErrorTime = currentTime;
            }
        }

        boolean result = (m_returnedCounter + m_abandonedCounter < m_startedCounter);
        if (result && LOG.isInfoEnabled()) {
            // write a note to the log that all threads have finished
            LOG.info(Messages.get().getBundle().key(Messages.LOG_THREADS_FINISHED_0));
        }
        return result;
    }

    /**
     * Writes statistical information to the report.<p>
     * 
     * The method reports the total number of threads started
     * (equals to the number of indexed files), the number of returned
     * threads (equals to the number of successfully indexed files),
     * and the number of abandoned threads (hanging threads reaching the timeout).
     * 
     * @param report the report to write the statistics to
     */
    public void reportStatistics(I_CmsReport report) {

        if (report != null) {
            CmsMessageContainer message = Messages.get().container(
                Messages.RPT_SEARCH_INDEXING_STATS_4,
                new Object[] {
                    new Integer(m_startedCounter),
                    new Integer(m_returnedCounter),
                    new Integer(m_abandonedCounter),
                    report.formatRuntime()});

            report.println(message);
            if (!(report instanceof CmsLogReport) && LOG.isInfoEnabled()) {
                // only write to the log if report is not already a log report
                LOG.info(message.key());
            }
        }
    }

    /**
     * Starts the thread manager to look for non-terminated threads<p>
     * The thread manager looks all 10 minutes if threads are not returned
     * and reports the number to the log file.
     * 
     * @see java.lang.Runnable#run()
     */
    public void run() {

        int max = 20;

        try {
            // wait 30 seconds for the initial indexing
            Thread.sleep(30000);
            while ((m_startedCounter > m_returnedCounter) && (max-- > 0)) {
                Thread.sleep(30000);
                // wait 30 seconds before we start checking for "dead" index threads
                if (LOG.isWarnEnabled()) {
                    LOG.warn(Messages.get().getBundle().key(
                        Messages.LOG_WAITING_ABANDONED_THREADS_2,
                        new Integer(m_abandonedCounter),
                        new Integer((m_startedCounter - m_returnedCounter))));
                }
            }
        } catch (Exception exc) {
            // ignore
        }

        if (max > 0) {
            if (LOG.isInfoEnabled()) {
                LOG.info(Messages.get().getBundle().key(Messages.LOG_THREADS_FINISHED_0));
            }
        } else {
            LOG.error(Messages.get().getBundle().key(
                Messages.LOG_THREADS_FINISHED_0,
                new Integer(m_startedCounter - m_returnedCounter)));
        }
    }
}