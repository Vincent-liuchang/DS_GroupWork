/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unimelb.bitbox;

import java.util.logging.Level;
import java.util.logging.Logger;
import unimelb.bitbox.util.Configuration;
import unimelb.bitbox.util.FileSystemManager;

/**
 *
 * @author Administrator
 */
public class Synchronize extends Thread{
    
    ServerMain mainServer;
    public Synchronize(ServerMain mainServer) {
        this.mainServer = mainServer;
    }
    
    @Override
    public void run() {
        for(FileSystemManager.FileSystemEvent event: ServerMain.fileSystemManager.generateSyncEvents()) {
                        System.out.println("synchronized events is:" + event);
			mainServer.processFileSystemEvent(event);
                }
        try {
            Thread.sleep(3000);
            run();
        } catch (InterruptedException ex) {
            Logger.getLogger(Synchronize.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
