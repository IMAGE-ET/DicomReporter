/********************************************************************
* Copyright (c) 2018, Institute of Cancer Research
* All rights reserved.
* 
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions
* are met:
* 
* (1) Redistributions of source code must retain the above copyright
*     notice, this list of conditions and the following disclaimer.
* 
* (2) Redistributions in binary form must reproduce the above
*     copyright notice, this list of conditions and the following
*     disclaimer in the documentation and/or other materials provided
*     with the distribution.
* 
* (3) Neither the name of the Institute of Cancer Research nor the
*     names of its contributors may be used to endorse or promote
*     products derived from this software without specific prior
*     written permission.
* 
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
* "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
* LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
* FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
* COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
* INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
* (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
* SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
* HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
* STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
* ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
* OF THE POSSIBILITY OF SUCH DAMAGE.
*********************************************************************/

/********************************************************************
* @author Simon J Doran
* Java class: GetDicomReport.java
* First created on Mar 23, 2018 at 13:41:00 PM
* 
* Parse a directory tree for DICOM files and report the values of
* specified DICOM tags for each patient/study/series.
*********************************************************************/
package dicomreporter;

import etherj.PathScan;
import etherj.dicom.DicomReceiver;
import etherj.dicom.DicomToolkit;
import etherj.dicom.Patient;
import etherj.dicom.PatientRoot;
import etherj.dicom.Series;
import etherj.dicom.SopInstance;
import etherj.dicom.Study;
import java.io.IOException;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DicomReporter
{
   private static final Logger logger =
		LoggerFactory.getLogger(DicomReporter.class);

	private final DicomToolkit dcmTk = DicomToolkit.getDefaultToolkit();
   
   private final String SCAN_PATH_ROOT = "/Users/simond/data/Treviso_sample";
 
   
   public static void main(String[] args)
   {
      DicomReporter dr = new DicomReporter();
      logger.info("Started");
      dr.run();
      logger.info("Finished");
   }
  
   
   private void run()
   {
      PatientRoot pRoot  = scanPath(SCAN_PATH_ROOT);
      String      output = createReport(pRoot);
   }
   
   
   private PatientRoot scanPath(String path)
	{
		logger.info("DICOM search: " + path);
		
		DicomReceiver         dcmRec   = new DicomReceiver();
		PathScan<DicomObject> pathScan = dcmTk.createPathScan();
		
		pathScan.addContext(dcmRec);
		PatientRoot root = null;
		try
		{
			pathScan.scan(path, true);
			root = dcmRec.getPatientRoot();
		}
		catch (IOException ex)
		{
			logger.warn(ex.getMessage(), ex);
		}
		return root;
	}
   
   private String createReport(PatientRoot pRoot)
   {
      StringBuilder sb = new StringBuilder();
      
      for (Patient p : pRoot.getPatientList())
      {
         logger.info("\n\nProcessing patient " + p.getName());
         for (Study st : p.getStudyList())
         {
            logger.info("\nProcessing DICOM study " + st.getId());
            for (Series se : st.getSeriesList())
            {
               logger.info("\nProcessing DICOM series " + se.getDescription() + se.getStudyUid());

               // Just report from the first file of each series.
               boolean isFirstSop = true;
               for (SopInstance sop : se.getSopInstanceList())
               {
                  //logger.info("\nProcessing DICOM SOP Instance " + sop.getUid());
                  if (isFirstSop)
                  {
                     isFirstSop = false;
                     DicomObject dco = sop.getDicomObject();
                     
                     // Echo time is just an example - you can report on anything
                     // in the DICOM file. The Tag object from DCM4CHE is really useful.
                     sb.append("MR sequence echo time = " + dco.getFloat(Tag.EchoTime) +"\n");
                  }
               }
            }
         }
      }
      return sb.toString();
   }
   
}
