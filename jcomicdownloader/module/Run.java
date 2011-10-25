/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jcomicdownloader.module;

import jcomicdownloader.tools.*;
import jcomicdownloader.enums.*;
import jcomicdownloader.*;

/**
*
* @author user 執行主類別
*/
public class Run extends Thread { // main class to run whole program
    private String[] args;
    private String webSite;
    private String title; // 下載網址指定已知漫畫名稱，避免重複分析
    private int runMode; // 只分析、只下載或分析兼下載

    public static boolean isAlive;
    public static boolean isLegal;

    public Run( int runMode ) {
        isAlive = true;
        isLegal = true;
        this.runMode = runMode;
    }
    public Run( String[] originalArgs, int runMode ) {
        this( runMode );

        args = originalArgs;

        if ( !Common.withGUI() ) {
            SetUp set = new SetUp();
            set.readSetFile(); // set up the file name and directory
        }
        //test( args );

        webSite = "";
    }

    public Run( String onlyURL, String title, int runMode ) {
        this( runMode );
        
        this.title = title;
        this.runMode = runMode;

        String[] url = new String[1];
        url[0] = onlyURL;

        args = url;

        SetUp set = new SetUp();
        set.readSetFile(); // 讀入設置檔的設置參數

        //test( args );

        webSite = "";
    }
    
    public void resetArgs( String[] newArgs ) {
        args = newArgs;
    }
    
    public String getTitle() {
        return title;
    }


    // 解析輸入網址並做後續處理
    public void run() {
        Common.debugPrintln( "開始解析單一位址：" );
        if ( args.length == 0 )
            Common.errorReport( "WRONG: No URL of comic !!" );
        else if ( args.length > 4 )
            Common.errorReport( "WRONG: Too many args !!" );
        else if ( Common.isLegalURL( args[0] ) ) {
            if ( args.length == 1 ) { // ComicDown URL
                webSite = args[0];
            }
            else if ( args.length == 2 &&
                      args[1].equals( "add" ) ) { // ComicDown URL add
                webSite = args[0];
                SetUp.addSchedule = true;
            }
            else if ( args.length == 3 &&
                      args[1].matches( "\\d+" ) &&
                      args[2].matches( "\\d+" ) ) { // ComicDown URL beginVolume endVolume
                webSite = args[0];
                SetUp.setDownloadVolume( args[1], args[2] );
            }
            else if ( args.length == 4 &&
                      args[1].matches( "\\d+" ) &&
                      args[2].matches( "\\d+" ) &&
                      args[3].equals( "add" ) ) { // ComicDown URL beginVolume endVolume
                webSite = args[0];
                SetUp.setDownloadVolume( args[1], args[2] );
                SetUp.addSchedule = true;
            }
            else
                Common.errorReport( "WRONG: illegal parameters !!" );
        }
        else
            Common.errorReport( "WRONG: illegal URL :  [" + args[0] + "]" );


        if ( isAlive && isLegal ) {
             ParseWebPage pw =  new ParseWebPage( webSite );

            if ( pw.getSiteID() == Site.CC ) {
                ParseOnlineComicSite parse = new ParseCC();
                parse.setTitle( title );
                parse.setRunMode( runMode );
                new RunModule().runMainProcess( parse, webSite );
                title = parse.getTitle();
            }
            else if ( pw.getSiteID() == Site.KUKU ) {
                ParseOnlineComicSite parse = new ParseKUKU();
                parse.setTitle( title );
                parse.setRunMode( runMode );
                new RunModule().runMainProcess( parse, webSite );
                title = parse.getTitle();
            }
            else if ( pw.getSiteID() == Site.EH ) {
                ParseOnlineComicSite parse = new ParseEH();
                parse.setTitle( title );
                parse.setRunMode( runMode );
                new RunModule().runMainProcess( parse, webSite );
                title = parse.getTitle();
            }

            // 九九系列網站
            else if ( pw.getSiteID() == Site.NINENINE_COMIC ) {
                ParseOnlineComicSite parse = new Parse99Comic();
                parse.setTitle( title );
                parse.setRunMode( runMode );
                new RunModule().runMainProcess( parse, webSite );
                title = parse.getTitle();
            }
            else if ( pw.getSiteID() == Site.NINENINE_MANGA ) {
                ParseOnlineComicSite parse = new Parse99Manga();
                parse.setTitle( title );
                parse.setRunMode( runMode );
                new RunModule().runMainProcess( parse, webSite );
                title = parse.getTitle();
            }
            else if ( pw.getSiteID() == Site.NINENINE_99770 ) {
                ParseOnlineComicSite parse = new Parse99770();
                parse.setTitle( title );
                parse.setRunMode( runMode );
                new RunModule().runMainProcess( parse, webSite );
                title = parse.getTitle();
            }
            else if ( pw.getSiteID() == Site.NINENINE_MH ) {
                ParseOnlineComicSite parse = new Parse99Mh();
                parse.setTitle( title );
                parse.setRunMode( runMode );
                new RunModule().runMainProcess( parse, webSite );
                title = parse.getTitle();
            }
            else if ( pw.getSiteID() == Site.NINENINE_COCO ) {
                ParseOnlineComicSite parse = new ParseCoco();
                parse.setTitle( title );
                parse.setRunMode( runMode );
                new RunModule().runMainProcess( parse, webSite );
                title = parse.getTitle();
            }
            else if ( pw.getSiteID() == Site.NINENINE_1MH ) {
                ParseOnlineComicSite parse = new Parse1Mh();
                parse.setTitle( title );
                parse.setRunMode( runMode );
                new RunModule().runMainProcess( parse, webSite );
                title = parse.getTitle();
            }
            else if ( pw.getSiteID() == Site.NINENINE_3G ) {
                ParseOnlineComicSite parse = new Parse3G();
                parse.setTitle( title );
                parse.setRunMode( runMode );
                new RunModule().runMainProcess( parse, webSite );
                title = parse.getTitle();
            }

            else // Site.UNKNOWN
                Common.urlIsUnknown = true;
        }
    }


    public void test( String[] args ) {

    }
}
