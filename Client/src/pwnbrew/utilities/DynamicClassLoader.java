/*

Copyright (C) 2013-2014, Securifera, Inc 

All rights reserved. 

The copyright on this package is held by Securifera, Inc

*/


package pwnbrew.utilities;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.AllPermission;
import java.security.CodeSource;
import java.security.Permissions;
import java.security.ProtectionDomain;
import java.security.cert.Certificate;
import java.util.Stack;
import java.util.logging.Level;
import pwnbrew.log.RemoteLog;

public class DynamicClassLoader extends ClassLoader {
    
    private Stack classDefStack = new Stack();
    private static final String NAME_Class = DynamicClassLoader.class.getSimpleName();    
    private boolean initialized = false;
    
    //==================================================================
    /**
     * Constructor
     */
    public DynamicClassLoader(){
        super();
    }

    //==================================================================
    /**
     * 
     * @return 
     */
    public Stack getClassDefStack() {
        return (Stack) classDefStack.clone();
    }

    //==================================================================
    /**
     * 
     * @param passedStack 
     */
    public void setClassDefStack(Stack passedStack) {
        classDefStack = (Stack) passedStack.clone();
    }
    
    
    
    //=============================================================
    /**
     * 
     * @param byteArr 
     */
    public void loadClass( byte[] byteArr ){
        
        //Load the class and resolve it
        try {
            
            Permissions localPermissions = new Permissions();
            localPermissions.add(new AllPermission());
            ProtectionDomain localProtectionDomain = new ProtectionDomain(new CodeSource(new URL("file:///"), new Certificate[0]), localPermissions);

            Class localClass = defineClass(null, byteArr, 0, byteArr.length, localProtectionDomain);
            resolveClass( localClass );
     
             
            //If not initialized, load all the other classes
            if( !initialized ){
                Stack tempStack = new Stack();
                while(!classDefStack.isEmpty()){
                    
                    byte[] classBytes = (byte[]) classDefStack.pop();
                    tempStack.push(classBytes);
                    
                    localClass = defineClass(null, classBytes, 0, classBytes.length, localProtectionDomain);
                    resolveClass( localClass );

                }        
                
                //Set stack
                classDefStack = tempStack;
                               
                //Set flag
                initialized = true;
            }
            
            //Add the new one
            classDefStack.push(byteArr);
            
        } catch( MalformedURLException ex){
            RemoteLog.log(Level.SEVERE, NAME_Class, "loadJar()", ex.getMessage(), ex );
        } 
       
    }
    
}
