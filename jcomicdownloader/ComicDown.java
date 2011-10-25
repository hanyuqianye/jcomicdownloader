/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jcomicdownloader;


import jcomicdownloader.tools.*;
import jcomicdownloader.module.*;
import jcomicdownloader.enums.*;


/**
 *
 * @author user
 */
public class ComicDown {
    public static void main( String[] args ) {
        Thread mainRun = new Run( args, RunModeEnum.DOWNLOAD_MODE );

        mainRun.setName( Common.consoleThreadName );
        mainRun.start();
	}
}
