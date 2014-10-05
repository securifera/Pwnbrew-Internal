///*
//
//Copyright (C) 2013-2014, Securifera, Inc 
//
//All rights reserved. 
//
//Redistribution and use in source and binary forms, with or without modification, 
//are permitted provided that the following conditions are met:
//
//    * Redistributions of source code must retain the above copyright notice,
//	this list of conditions and the following disclaimer.
//
//    * Redistributions in binary form must reproduce the above copyright notice,
//	this list of conditions and the following disclaimer in the documentation 
//	and/or other materials provided with the distribution.
//
//    * Neither the name of Securifera, Inc nor the names of its contributors may be 
//	used to endorse or promote products derived from this software without specific
//	prior written permission.
//
//THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS 
//OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY 
//AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER 
//OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
//CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
//SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON 
//ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT 
//(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, 
//EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//
//================================================================================
//
//Pwnbrew is provided under the 3-clause BSD license above.
//
//The copyright on this package is held by Securifera, Inc
//
//*/
//
//
///*
//* Bundle.java
//*
//* Created on June 23, 2013, 9:42:54 AM
//*/
//
//package pwnbrew.xmlBase;
//
//import pwnbrew.exception.XmlBaseCreationException;
//import pwnbrew.library.LibraryItemController;
//import pwnbrew.controllers.JobSetController;
//import pwnbrew.controllers.JobController;
//import pwnbrew.xmlBase.job.Job;
//import java.util.ArrayList;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Map;
//import pwnbrew.gui.MainGui;
//import pwnbrew.logging.LoggableException;
//import pwnbrew.misc.FilenameFilterTool;
//
///**
// *
// *  
// */
//public class Bundle extends XmlBase {
//    
//    private final List<XmlBase> theBundleList = new ArrayList<>();
//
//    // Here is a FilenameFilter_tool which can be used such that File.list() and File.listOfFile()
//    // will return only files of this class.
//    //
//    public static final String FilenameSuffixStr= "_bundle.xml";
//    public static final FilenameFilterTool theFilenameFilterTool= new FilenameFilterTool( FilenameSuffixStr );
//
//
//    // ==========================================================================
//    /**
//    * Adds and updates local support objects, determining the appropriate manner
//    * in which to do so according to the class of the <code>passedGRB</code> argument.
//    *
//    * @param passedGRB the support object to be added/updated
//    */
//    @Override
//    public void addUpdateComponent( XmlBase passedGRB ) {
//
//        theBundleList.add( passedGRB );
//
//    }/* END addUpdateComponent() */
//
//    // ==========================================================================
//    /**
//    * Adds a XmlBase and its supporting objects to the bundle
//    *
//     * @param theParent
//    * @param passedControllerList the support object to be added/updated
//    *
//    * @return  a <code>String</code> (that is currently only <code>null</code> and
//    *          not being used for anything)
//     * @throws pwnbrew.logging.LoggableException
//    */
//    public String addToBundle(MainGui theParent, List<LibraryItemController> passedControllerList ) throws LoggableException {
//
//        try {
//     
//            //Currently only add xmlbase objects to bundles
//            for( LibraryItemController aController : passedControllerList ){
//                XmlBase theXB = (XmlBase)aController.getObject();
//                if(passedControllerList instanceof JobController){
//
//                    Job clonedJob = (Job) XmlBaseFactory.clone(theXB);
//                    clonedJob.setAttribute(Job.theLastRunResult, "");
//                    clonedJob.setAttribute(Job.theLastRunDate, "");
//                    addToBundle( clonedJob );
//
//                } else if(passedControllerList instanceof JobSetController){
//
//                    JobSetController aJobSetController = (JobSetController)passedControllerList;
//                    theBundleList.add(theXB);
//
//                    List<LibraryItemController> childControllers = aJobSetController.getChildren();
//                    for(LibraryItemController anotherController : childControllers){
//
//                        if( anotherController instanceof JobController ){
//
//                            JobController aJobController = (JobController)anotherController;
//                            Job theJob = (Job)aJobController.getObject();
//                            XmlBase clonedJob = XmlBaseFactory.clone(theJob);
//
//                            clonedJob.setAttribute(Job.theLastRunResult, "");
//                            clonedJob.setAttribute(Job.theLastRunDate, "");
//                            addToBundle((Job)clonedJob);
//                        }
//                    }
//
//                }    
//            }    
//
//        } catch (XmlBaseCreationException ex) {
//            throw new LoggableException(ex);
//        }
//
//        return null;
//
//    }
//
//    // ==========================================================================
//    /**
//    * Returns the list of XmlBases
//    *
//    *
//    * @return  a List of XmlBase
//    */
//    public List<XmlBase> getBundleList() {
//
//        return theBundleList;
//
//    }/* END getBundleList() */
//
//    // ==========================================================================
//    /**
//    * Adds a Task and its supporting objects to the bundle
//    *
//    * @param passedJob the passed job
//    *
//    */
//    private void addToBundle( Job passedJob ) throws LoggableException {
//
//        if(passedJob != null && !theBundleList.contains(passedJob)){
//
//            theBundleList.add(passedJob);
//
//            //Get the FileContents associated with the Job
//            Map<String, FileContentRef> fileMap = passedJob.getFileContentRefMap();
//            for( Iterator<FileContentRef> anIter = fileMap.values().iterator(); 
//                       anIter.hasNext(); ) { 
//        
//                FileContentRef aFileContentRef = anIter.next();
//                FileContent aFileContent = aFileContentRef.getFileContent();
//                if(aFileContent != null && !theBundleList.contains(aFileContent)){
//                    theBundleList.add(aFileContent);
//                }
//            }
//        }
//    }
//
//    // ==========================================================================
//    /**
//    * Returns a list of this object's subcomponents that should be added to its
//    * XML data.
//    * <p>
//    * NOTE: This overrides a method in {@link XmlBase}.
//    *
//    * @return an {@link ArrayList} of the {@link XmlBase} components for this
//    * object
//    */
//    @Override
//    public List<XmlBase> getXmlComponents() {
//
//        List<XmlBase> rtnList = super.getXmlComponents();
//
//        if( theBundleList != null && theBundleList.size() > 0 ) { //If there are XmlBase objects
//            rtnList.addAll( theBundleList ); //Add the XmlBase objects
//        }
//
//        return  rtnList;
//
//    }
//
//}/* END CLASS Bundle */
