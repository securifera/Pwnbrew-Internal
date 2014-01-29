/*

Copyright (C) 2013-2014, Securifera, Inc 

All rights reserved. 

Redistribution and use in source and binary forms, with or without modification, 
are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice,
	this list of conditions and the following disclaimer.

    * Redistributions in binary form must reproduce the above copyright notice,
	this list of conditions and the following disclaimer in the documentation 
	and/or other materials provided with the distribution.

    * Neither the name of Securifera, Inc nor the names of its contributors may be 
	used to endorse or promote products derived from this software without specific
	prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS 
OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY 
AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER 
OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON 
ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT 
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, 
EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

================================================================================

Pwnbrew is provided under the 3-clause BSD license above.

The copyright on this package is held by Securifera, Inc

*/


/*
 * RemoteTask.java
 *
 * Created on Jun 23, 2013, 4:12:31 PM
 */

package pwnbrew.tasks;

import pwnbrew.network.Nic;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import pwnbrew.execution.ExecutableItem;
import pwnbrew.host.Host;
import pwnbrew.logging.Log;
import pwnbrew.logging.LoggableException;
import pwnbrew.misc.Constants;
import pwnbrew.misc.Directories;
import pwnbrew.utilities.FileUtilities;
import pwnbrew.utilities.SocketUtilities;
import pwnbrew.network.control.messages.TaskNew;
import pwnbrew.xmlBase.AttributeCollection;
import pwnbrew.xmlBase.DescriptiveXmlBase;
import pwnbrew.xmlBase.FileContentRef;


/**
 *   - filled in most of internals
 */
public class RemoteTask extends DescriptiveXmlBase implements ExecutableItem{

    private static final String NAME_Class = RemoteTask.class.getSimpleName();
    
    //Task States
    public static final String TASK_INIT = "Initialized";
    public static final String TASK_START = "Start";
    public static final String TASK_STAGING = "Staging";
    public static final String TASK_XFER_FILES = "Transferring Files";
    public static final String TASK_RUNNING = "Running";
    public static final String TASK_XFER_RESULTS = "Transferring Results";
    public static final String TASK_COMPLETED = "Completed";
    public static final String TASK_FAILED = "Failed";
    public static final String TASK_CANCELLED = "Cancelled";

    private static final String ATTRIBUTE_Target = "Target";
    private static final String ATTRIBUTE_Type = "Type";
    private static final String ATTRIBUTE_Command = "Command";
    private static final String ATTRIBUTE_ClientId = "ClientId";
    private static final String ATTRIBUTE_TaskId = "TaskId";
    private static final String ATTRIBUTE_StartTime = "StartTime";
    private static final String ATTRIBUTE_EndTime = "EndTime";
    private static final String ATTRIBUTE_State = "State";
    private static final String ATTRIBUTE_State_Progress = "Progress";
    private static final String ATTRIBUTE_LastRunResult =  "LastRunResult";
    private static final String ATTRIBUTE_IconStr=  "IconStr";
    

    private Map<String, FileContentRef> theFileContentRefMap = null;
    private final List<RemoteTaskListener> theTaskListeners = new ArrayList<>();

    //execution variables
    private boolean rebootOnComplete = false;
    private boolean stopOnError = false;
    private Integer nextTaskId = null;
    
    //private Image theIconImage;
   

    // ==========================================================================
    /**
     * Creates a new instance of {@link Task}.
     */
    public RemoteTask() {

        theAttributeMap.put( ATTRIBUTE_Target,  "" );
        theAttributeMap.put( ATTRIBUTE_Type, "" );
        theAttributeMap.put( ATTRIBUTE_ClientId, "" );
        theAttributeMap.put( ATTRIBUTE_TaskId, Integer.toString(SocketUtilities.getNextId() ) );
        theAttributeMap.put( ATTRIBUTE_StartTime, "" );
        theAttributeMap.put( ATTRIBUTE_EndTime, "" );
        theAttributeMap.put( ATTRIBUTE_State, TASK_INIT );
        theAttributeMap.put( ATTRIBUTE_State_Progress, "0" );
        theAttributeMap.put( ATTRIBUTE_LastRunResult, "" );
        theAttributeMap.put( ATTRIBUTE_IconStr, "" );

        //Add the cmd line array
        theAttributeCollectionMap.put(ATTRIBUTE_Command, new AttributeCollection(ATTRIBUTE_Command, new String[0]));


        theFileContentRefMap = new HashMap<>();
    
    }/* END CONSTRUCTOR() */

    // ==========================================================================
    /**
     * Creates a new instance of {@link Task}.
     * @param taskName
     * @param passedCmdArr
     * @param passedHost
     * @param passedType
     * @param passedIconStr
     */
    public RemoteTask( String taskName, Host passedHost, String passedType, String[] passedCmdArr, String passedIconStr ) {


        String passedTarget = "";
        String passedHostId = "";

        if( passedHost != null){

            //Get the id
            passedHostId = passedHost.getId();

            String theHostname = passedHost.getHostname();
            Map<String, Nic> theNicMap = passedHost.getNicMap();

            try {
                if(!theNicMap.isEmpty()){
                    InetAddress theInet = InetAddress.getByName( theNicMap.values().iterator().next().getIpAddress() );                
                    passedTarget = new StringBuilder().append(theInet.getHostAddress()).append(":").append(theHostname).toString();
                }
            } catch( UnknownHostException ex ){
                ex = null;
            }
        }

        setAttribute(RemoteTask.ATTRIBUTE_Name, taskName);
        theAttributeMap.put( ATTRIBUTE_Target, passedTarget );
        theAttributeMap.put( ATTRIBUTE_Type, passedType );
        theAttributeMap.put( ATTRIBUTE_ClientId, passedHostId );
        theAttributeMap.put( ATTRIBUTE_TaskId, Integer.toString(SocketUtilities.getNextId()) );
        theAttributeMap.put( ATTRIBUTE_StartTime, "" );
        theAttributeMap.put( ATTRIBUTE_EndTime, "" );
        theAttributeMap.put( ATTRIBUTE_State, TASK_INIT );
        theAttributeMap.put( ATTRIBUTE_State_Progress, "0" );
        theAttributeMap.put( ATTRIBUTE_IconStr, passedIconStr );

        //Add the cmd line array
        theAttributeCollectionMap.put(ATTRIBUTE_Command, new AttributeCollection(ATTRIBUTE_Command, passedCmdArr));

        theFileContentRefMap = new HashMap<>();
   
    }
    
    // ==========================================================================
    /**
     *  Get the icon image
     * @return 
     */
    public String getIconString() {
        return getAttribute( ATTRIBUTE_IconStr );
    }

    

    // ==========================================================================
    /**
     * Adds the RemoteTaskListener to the list.
     *
     * @param passedListener
    */
    public void addListener( RemoteTaskListener passedListener ) {
       if(passedListener != null){

          synchronized(theTaskListeners){
             if(!theTaskListeners.contains(passedListener)){
                theTaskListeners.add(passedListener);
             }
          }
       }
    }

     

    // ==========================================================================
    /**
     * Sets the client id to the given {@code String}.
     * <p>
     * If the given {@code String} is null, the command will be set to the empty
     * {@code String}.
     *
     * @param id
    */
    public void setClientId( String id) {
       setAttribute(ATTRIBUTE_ClientId, id );
    }
    
    // ==========================================================================
    /**
     * Sets the command to the given {@code String}.
     * <p>
     * If the given {@code String} is null, the command will be set to the empty
     * {@code String}.
     *
     * @param command
     */
    public void setCommand( String[] command ) {

        if( command == null ) { //If the String is null...
            command = new String[0]; //Use the empty String
        }

        //Add the cmd line array
        theAttributeCollectionMap.put(ATTRIBUTE_Command, new AttributeCollection(ATTRIBUTE_Command, command));
     
    }/* END setCommand( String ) */

    // ==========================================================================
    /**
     * Returns the command.
     *
     * @param passedOsName
     * @return the command
     */
    @Override
    public String[] getCommandArgs( String passedOsName ) {
        String[] theCmdLineArr = null;
        
        AttributeCollection theCollection = theAttributeCollectionMap.get(ATTRIBUTE_Command);
        if(theCollection != null){
           List<String> theCmdLine = theCollection.getCollection();
           if( theCmdLine != null && theCmdLine.size() > 0 ){
               theCmdLineArr = theCmdLine.toArray( new String[theCmdLine.size()]);
           }
        }
        return theCmdLineArr;
        
    }/* END getCommandArgs() */

    // ==========================================================================
    /**
     * Returns a control message for the current state of the task.
     *
     * @param dstHostId
     * @return a Control Message
     * @throws pwnbrew.logging.LoggableException
     */
    public TaskNew getControlMessage( int dstHostId ) throws LoggableException {

        TaskNew taskMsg = null;
        String[] cmdLineStrArr = getCommandArgs("");

        try {
            
            if(cmdLineStrArr != null && cmdLineStrArr.length > 0){
      
               String theTaskId = getTaskId();
               taskMsg = new TaskNew(Integer.valueOf(theTaskId), cmdLineStrArr, theFileContentRefMap.values(), dstHostId );

            }

        } catch (IOException ex){
           throw new LoggableException(ex);
        }

        return taskMsg;

    }/* END getControlMessage() */

     // ==========================================================================
    /**
     * Returns the type of task.
     *
     * @return the type of task
     */
    public String getType() {
        return getAttribute( ATTRIBUTE_Type );
    }/* END getType() */


    // ==========================================================================
    /**
     * Returns the target ip of the task.
     *
     * @return the target of task
     */
    public String getTargetIp() {
        
        //Split the target to get the IP, IP:Hostname
        String target = getAttribute( ATTRIBUTE_Target );
        return target.split(":")[0];
    }/* END getTarget() */

    // ==========================================================================
    /**
     * Returns the time the task ended or was cancelled
     *
     * @return
    */
    public String getEndTime() {
       return getAttribute( ATTRIBUTE_EndTime );
    }/* END getEndTime() */

    // ==========================================================================
    /**
     * Sets the time the task ended or was cancelled
     *
     * @param passedTime
    */
    public void setEndTime(String passedTime ) {
       setAttribute( ATTRIBUTE_EndTime, passedTime );
    }/* END getEndTime() */

     // ==========================================================================
    /**
     * Returns the target hostname of the task.
     *
     * @return the target of task
     */
    public String getTargetHostname() {
        String target = getAttribute( ATTRIBUTE_Target );
        String hostname = "";

        //Split the target to get the hostname, IP:Hostname
        String[] targetArr = target.split(":");
        if(targetArr.length > 1){
           hostname = targetArr[1];
        }

        return hostname;
    }/* END getTarget() */


    // ==========================================================================
    /**
     * Sets the state to the given {@code String}.
     * <p>
     * If the given {@code String} is null, the state will be set to the empty
     * {@code String}.
     *
     * @param state
     */
    public void setState( String state ) {

       setAttribute( ATTRIBUTE_State, state ); //Set the value

    }/* END setState( String ) */
    
    // ==========================================================================
    /**
     * Sets the id of the next task if it is part of a collection
     * <p>
     *
     * @param passedId
    */
    public void setNextTaskId( Integer passedId ) {

       nextTaskId = passedId;

    }/* END setNextTaskId( Integer ) */

     // ==========================================================================
    /**
     * Sets the target of the {@code Task}.
     * <p>
     * If the given {@code String} is null, the type will be set to the empty
     * {@code String}.
     *
     * @param passedHost
     */
    public void setTarget( Host passedHost ) {

        if(passedHost != null){

            String passedTarget = "";
            String theHostname = passedHost.getHostname();
            Map<String, Nic> theNicMap = passedHost.getNicMap();

            try {
                if(!theNicMap.isEmpty()){
                    InetAddress theInet = InetAddress.getByName( theNicMap.values().iterator().next().getIpAddress() );                
                    passedTarget = new StringBuilder().append(theInet.getHostAddress()).append(":").append(theHostname).toString();
                }
            } catch( UnknownHostException ex ){
                ex = null;
            }

            setAttribute( ATTRIBUTE_Target, passedTarget ); //Set the value
        }

    }/* END setTarget( String ) */

    // ==========================================================================
    /**
     * Sets the task id to the given {@code String}.
     * <p>
     * If the given {@code String} is null, the type will be set to the empty
     * {@code String}.
     *
     * @param taskId
     */
    public void setTaskId( String taskId ) {

       setAttribute( ATTRIBUTE_TaskId, taskId ); //Set the value

    }/* END setTaskId( String ) */
    
    // ==========================================================================
    /**
     * Sets flag indicating whether the task should reboot upon completion.
     *
     * @param passedBool
     */
    public void setRebootFlag( boolean passedBool) {

       rebootOnComplete = passedBool; //Set the value

    }/* END setRebootFlag( boolean ) */
    
    
    // ==========================================================================
    /**
     * Sets flag indicating whether the task should stop on error.
     *
     * @param passedBool
     */
    public void setStopFlag( boolean passedBool) {

       stopOnError = passedBool; //Set the value

    }/* END setStopFlag( boolean ) */
    
    
    // ==========================================================================
    /**
     * Sets the type to the given {@code String}.
     * <p>
     * If the given {@code String} is null, the type will be set to the empty
     * {@code String}.
     *
     * @param type
     */
    public void setType( String type ) {

       setAttribute( ATTRIBUTE_Type, type ); //Set the value

    }/* END setType( String ) */


    // ==========================================================================
    /**
     * Sets the progress of the current state to the given {@code String}.
     * <p>
     *
     * @param progress
     */
    public void setStateProgress( int progress ) {

       setAttribute( ATTRIBUTE_State_Progress, Integer.toString(progress) ); //Set the value

    }/* END setStateProgress( int ) */


     // ==========================================================================
    /**
     * Sets the start time of the task.
     * <p>
     *
     * @param startTime
     */
    public void setStartTime( Date startTime ) {

       setAttribute( ATTRIBUTE_StartTime, startTime.toString() ); //Set the value

    }/* END setStartTime( Date ) */

    // ==========================================================================
    /**
     * Returns the state.
     *
     * @return the state
     */
    public String getState() {
        return getAttribute( ATTRIBUTE_State );
    }/* END getState() */

    // ==========================================================================
    /**
     * Returns the progress of the current state.
     *
     * @return the state progress
     */
    public int getStateProgress() {
        return Integer.parseInt( getAttribute( ATTRIBUTE_State_Progress ));
    }/* END getStateProgress() */


    // ==========================================================================
    /**
     * Returns a {@link List} containing the {@link FileContentRef}s.
     *
     * @return a {@code List} containing the {@code FileContentRef}s
     */
    @Override
    public Map<String, FileContentRef> getFileContentRefMap() {
        return new HashMap<>(theFileContentRefMap);
    }/* END getFileContentRefList() */

    // ==========================================================================
    /**
     * Sets the {@link Map} containing the {@link FileContentRef}s.
     *
     * @param passedList
     */
    public void setFileContentRefMap( Map<String, FileContentRef> passedList) {

       if(passedList != null){
          theFileContentRefMap = new HashMap<>(passedList);
       }
       
    }/* END setFileContentRefList() */


    // ==========================================================================
    /**
     * Returns whether to reboot on completion
     *
     * @return 
    */
    public boolean shouldReboot(){
       return rebootOnComplete;
    }
    
     // ==========================================================================
    /**
     * Returns whether to stop on error
     *
     * @return 
    */
    public boolean shouldStopOnError(){
       return stopOnError;
    }

    // ==========================================================================
    /**
     * Returns a list of {@link ITaskListener}'s
     * <p>
     * This method will return at least an empty {@link ArrayList}.
     *
     * @return an {@code List} of {@link ITaskListener}'s
     */
    public List<RemoteTaskListener> getRemoteListeners() {
        
        List<RemoteTaskListener> theListenerList;
        synchronized(theTaskListeners){
            theListenerList = new ArrayList<>(theTaskListeners);
        }
        return theListenerList;
    }


     // ==========================================================================
    /**
     * Returns the task id
     *
     * @return a {@code String} representing the task id.
     */
    public String getTaskId() {
       return getAttribute(ATTRIBUTE_TaskId);
    }
    
    // ==========================================================================
    /**
     * Returns the task id for the next task
     *
     * @return 
    */
    public Integer getNextTaskId() {
       return nextTaskId;
    }

    // ==========================================================================
    /**
     * Returns the client id
     *
     * @return a {@code String} representing the client id.
    */
    public String getClientId() {
       return getAttribute(ATTRIBUTE_ClientId);
    }

    // ==========================================================================
    /**
     * Wrapper for the write self to xml function
     * @throws java.io.IOException
     */
    public void writeSelfToDisk() throws IOException  {
        String taskId = getTaskId();
        
        File clientDir = new File(Directories.getRemoteTasksDirectory(), getTargetHostname());
        File parentDir = new File(clientDir, taskId);
        writeSelfToDisk(parentDir, taskId + ".xml");
    }

    // ==========================================================================
    /**
     * Returns a string of the stdout file for the task if it exists
     *
     * @return a {@code String} representing the stdout from the run task.
     * @throws java.io.IOException
     */
    public List<String> getStdOut() throws IOException {

       List<String> retString = new ArrayList<>();
       String taskId = getTaskId();
       
       File aClientDir = new File(Directories.getRemoteTasksDirectory(), getTargetHostname());
       File parentDir = new File(aClientDir, taskId);


//       File parentDir = new File(Directories.getRemoteTasksDirectory(), taskId);
       if(parentDir.exists()){
           File stdoutFile = new File(parentDir, Constants.STD_OUT_FILENAME);
           if(stdoutFile.exists()){
              retString = FileUtilities.readFileLines(stdoutFile);
           } else {
              throw new FileNotFoundException("File does not exist.");
           }
       }
       
       return retString;
    }

    // ==========================================================================
    /**
     * Returns a string of the stdout file for the task if it exists
     *
     * @return a {@code String} representing the stdout from the run task.
     * @throws java.io.IOException
     */
    public List<String> getStdErr() throws IOException {

       List<String> retString = new ArrayList<>();
       String taskId = getTaskId();
       
       File aClientDir = new File(Directories.getRemoteTasksDirectory(), getTargetHostname());
       File parentDir = new File(aClientDir, taskId);

       if(parentDir.exists()){
           File stderrFile = new File(parentDir, Constants.STD_ERR_FILENAME);
           if(stderrFile.exists()){
              retString = FileUtilities.readFileLines(stderrFile);
           } else {
              throw new FileNotFoundException("File does not exist.");
           }
       }

       return retString;
    }

     /**
     * Comparator to sort by the end date of the task
     *
     */
    public static final Comparator<RemoteTask> DATE_SORT = new Comparator<RemoteTask>(){

        @Override
        public int compare(RemoteTask firstTask, RemoteTask secondTask) {
            if ( firstTask == secondTask ) return 0; //EQUAL;

            String firstTaskET = firstTask.getEndTime();
            String secondTaskET = secondTask.getEndTime();

            if(firstTaskET.isEmpty() && secondTaskET.isEmpty()){
               return 0;
            } else if(firstTaskET.isEmpty()){
               return -1; //LESS THAN
            } else if(secondTaskET.isEmpty()){
               return 1; //GREATER THAN
            }

            try {

                //Compare the two dates if they are not empty
                Date t1Date = new SimpleDateFormat(Constants.FORMAT_SessionDateTime).parse(firstTaskET);
                Date t2Date = new SimpleDateFormat(Constants.FORMAT_SessionDateTime).parse(secondTaskET);

                return t2Date.compareTo(t1Date);
                
            } catch (ParseException ex) {
               Log.log(Level.INFO, NAME_Class, "compare()", ex.getMessage(), ex );
            }

            return 0;
        }

    };

     /**
     * Comparator to sort by the status of the task
     *
     */
    public static final Comparator<RemoteTask> STATUS_SORT = new Comparator<RemoteTask>(){

        @Override
        public int compare(RemoteTask firstTask, RemoteTask secondTask) {
            if ( firstTask == secondTask ) return 0; //EQUAL;

            String firstTaskState = firstTask.getState();
            String secondTaskState = secondTask.getState();

            return firstTaskState.compareTo(secondTaskState);

        }

    };

   
     /**
     * Comparator to sort by the client that the task was sent to
     *
     */
    public static final Comparator<RemoteTask> CLIENT_SORT = new Comparator<RemoteTask>(){

        @Override
        public int compare(RemoteTask firstTask, RemoteTask secondTask) {
            if ( firstTask == secondTask ) return 0; //EQUAL;

            String firstTaskHostname = firstTask.getTargetHostname();
            String secondTaskHostname = secondTask.getTargetHostname();

            return firstTaskHostname.compareTo(secondTaskHostname);
        }

    };

    // ==========================================================================
    /**
     * Determines if the task is running.
     *
     * <tt>false</tt> otherwise
     * @return 
    */
    public boolean isRunning() {
        return ( getAttribute(RemoteTask.ATTRIBUTE_LastRunResult).equals(Constants.LastRunResults_Running) );
    }/* END isRunning() */

    
}
