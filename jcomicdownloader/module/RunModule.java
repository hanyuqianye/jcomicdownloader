/*
----------------------------------------------------------------------------------------------------
Program Name : JComicDownloader
Authors  : surveyorK
Last Modified : 2011/10/25
----------------------------------------------------------------------------------------------------
ChangeLog:
1.14: 修復若沒有下載成功仍會產生空壓縮檔的bug。
1.12: 部分網站邊解析邊下載，所以不用做最後的整本下載。
----------------------------------------------------------------------------------------------------
 */
package jcomicdownloader.module;

import jcomicdownloader.tools.*;
import jcomicdownloader.*;
import jcomicdownloader.enums.*;

import java.io.*;
import java.util.*;
import javax.swing.JOptionPane;
import jcomicdownloader.Flag;
import jcomicdownloader.SetUp;

/**
 *
 * @author user
 */
public class RunModule {

    private String indexName = "";
    private String indexEncodeName = "";

    public RunModule() {
    }

    public synchronized void runMainProcess( ParseOnlineComicSite parse,
            String urlString ) {
        parse.printLogo();

        if ( urlString.matches( "(?s).*.htm" )
                || urlString.matches( "(?s).*.html" )
                || urlString.matches( "(?s).*.php" )
                || urlString.matches( "(?s).*.asp" )
                || urlString.matches( "(?s).*.jsp" )
                || urlString.matches( "(?s).*/" )
                || urlString.matches( "(?s).*\\?(?s).*" ) ); else {
            urlString += "/";
        }

        Common.debugPrintln( "目前解析位址：" + urlString );

        if ( parse.isSingleVolumePage( urlString ) ) { // ex. http://www.89890.com/comic/7953/
            // the urlString is single volume page
            Common.debugPrintln( "單集頁面（single volume）" );
            Common.isMainPage = false;

            if ( parse.getRunMode() == RunModeEnum.DOWNLOAD_MODE ) {
                if ( parse.getTitle() == null || parse.getTitle().equals( "" ) ) {
                    parse.setTitle( parse.getTitleOnSingleVolumePage( urlString ) );
                }
                Common.debugPrintln( "漫畫名稱: " + parse.getTitle() );
                //Common.nowTitle = parse.getTitle();

                System.out.println( "++" + parse.getRunMode() + "++" );
                //if ( Flag.allowDownloadFlag && !Flag.downloadingFlag )

                runSingle( parse, urlString, new String( parse.getTitle() ) );
            }
        } else { // the urlString is main page
            Common.debugPrintln( "全集頁面（main page）" );
            Common.isMainPage = true;

            String allPageString = parse.getAllPageString( urlString );

            // 臨時作法：如果處理EH或EX時已經有標題名稱，就不用再解析標題名稱
           //if ( ( parse.siteID == Site.EH || parse.siteID == Site.EX ) && parse.getTitle() != null );
           //else
           if ( parse.getTitle() == null || parse.getTitle().equals( "" ) ) 
                parse.setTitle( parse.getTitleOnMainPage( urlString, allPageString ) );
            Common.debugPrintln( "漫畫名稱: " + parse.getTitle() );
            //Common.nowTitle = parse.getTitle();

            Common.debugPrint( "開始解析解析各集位址和各集名稱：" );
            List<List<String>> combinationList = null;
            combinationList = parse.getVolumeTitleAndUrlOnMainPage( urlString, allPageString );
            
            if ( combinationList == null ) {
                JOptionPane.showMessageDialog( ComicDownGUI.mainFrame, "此頁面沒有可下載的集數！",
                    "提醒訊息", JOptionPane.INFORMATION_MESSAGE );
                return;
            }
            List<String> volumeList = combinationList.get( 0 );
            List<String> urlList = combinationList.get( 1 );

            Common.debugPrint( "  ......解析各集位址和各集名稱完畢!!" );

            parse.deleteTempFile( parse.getTempFileNames() );

            parse.outputVolumeAndUrlList( volumeList, urlList );

            //if ( Flag.allowDownloadFlag && !Flag.downloadingFlag ) {
            if ( parse.getRunMode() == RunModeEnum.DOWNLOAD_MODE ) {
                runSingle( parse, urlString, new String( parse.getTitle() ) );
                System.out.println( "-------------------------------" );
            }
        }

        if ( Flag.allowDownloadFlag ) {
            System.out.println( "\nAll downloads are done!\n" );
        }
    }

    private synchronized void runSingle( ParseOnlineComicSite parse, String urlString, String titleString ) {
        if ( Run.isAlive ) {
            Flag.downloadingFlag = true;

            parse.setURL( urlString );
            parse.setTitle( titleString );

            parse.setParameters();

            if ( parse.siteID == Site.EH || parse.siteID == Site.EX ) {
                parse.setDownloadDirectory( SetUp.getOriginalDownloadDirectory()
                        + parse.getTitle() + Common.getSlash() );
            } else {
                parse.setDownloadDirectory( SetUp.getOriginalDownloadDirectory()
                        + parse.getTitle() + Common.getSlash()
                        + parse.getWholeTitle() + Common.getSlash() );
            }

            // 若已存在同檔名壓縮檔，則不解析下載網址
            if ( !existZipFile( parse.getDownloadDirectory() ) ) {
                parse.parseComicURL(); // EH在此已經開始下載了。
            }
            String[] urls = parse.getComicURL();

            if ( SetUp.getOutputUrlFile() ) {
                Common.debugPrintln( "允許輸出圖片位址" );
                Common.outputUrlFile( urls, parse.getDownloadDirectory() );  // output url file
            }

            if ( !isDownloadBefore( parse.getSiteID() ) && SetUp.getDownloadPicFile() ) {
                System.out.println( "Download Directory : " + parse.getDownloadDirectory() );
                System.out.println( "\nReady to download ...\n" );

                // 如果已經有同檔名壓縮檔存在，就假設已經下載完畢而不下載。
                if ( !existZipFile( parse.getDownloadDirectory() ) ) {
                    Common.debugPrintln( "開始下載整集：" );
                    Common.downloadManyFile( urls, parse.getDownloadDirectory(),
                            SetUp.getPicFrontName(), "jpg" );
                }
            }

            if ( new File( parse.getDownloadDirectory() ).exists() ) // 存在下載圖檔資料夾
            {
                followingWork( parse );
            } else {
                if ( !existZipFile( parse.getDownloadDirectory() ) ) { // 已有壓縮檔當然不用產生資料夾
                    Flag.downloadErrorFlag = true; // 發生錯誤
                    Common.errorReport( "ERROR： 沒有產生" + parse.getDownloadDirectory() );
                }
            }
            Flag.downloadingFlag = false;
        }
    }

    // 有些站在解析圖片網址的同時就在下載了，那就不用再進入到整本下載區
    public boolean isDownloadBefore( int siteID ) {
        if ( siteID == Site.EH
                || siteID == Site.EX
                || siteID == Site.JUMPCN
                || siteID == Site.KUKU
                || siteID == Site.DMEDEN
                || siteID == Site.MANGAFOX
                || siteID == Site.XINDM
                || siteID == Site.WY
                || siteID == Site.GOOGLE_PIC
                || siteID == Site.CITY_MANGA
                || siteID == Site.BAIDU
                || siteID == Site.BENGOU 
                || siteID == Site.EMLAND ) {
            return true;
        } else {
            return false;
        }
    }

    public boolean existZipFile( String downloadPicDirectory ) {
        String file;
        if ( downloadPicDirectory.lastIndexOf( Common.getSlash() ) == downloadPicDirectory.length() - 1 ) {
            file = downloadPicDirectory.substring( 0, downloadPicDirectory.length() - 1 ) + ".zip";
        } else {
            file = downloadPicDirectory + ".zip";
        }

        //Common.debugPrintln( "將處理的壓縮檔名稱：" + file );
        if ( new File( file ).exists() && new File( file ).length() > 1024 ) {
            Common.debugPrintln( file + "已經存在!" );
            return true;
        } else {
            return false;
        }
    }

    public synchronized void followingWork( ParseOnlineComicSite parse ) { // compress or not & delete or not
        if ( Run.isAlive ) {
            File downloadPath = new File( parse.getDownloadDirectory() );

            if ( SetUp.getAutoCompress() ) { // compress to zip file or not
                File zipFile = new File( downloadPath.getParent() + "/"
                        + parse.getWholeTitle() + ".zip" );

                if ( downloadPath.list().length < 1 ) {
                    Common.debugPrintln( "不產生壓縮檔（" + downloadPath.getAbsolutePath() + "資料夾內沒有任何檔案）" );
                } else if ( zipFile.exists() && zipFile.length() > 1024 ) {
                    Common.debugPrintln( "不產生壓縮檔（" + zipFile.getAbsolutePath() + "已存在）" );
                } else {
                    Common.compress( downloadPath, zipFile );
                    System.out.println( zipFile.getAbsolutePath() + " made!" );
                }

            }

            if ( SetUp.getDeleteOriginalPic() ) { // delete the whole folder or not
                Common.deleteFolder( parse.getDownloadDirectory() );

                System.out.println( parse.getDownloadDirectory() + " deletion!" );
            }
        }
    }
}
