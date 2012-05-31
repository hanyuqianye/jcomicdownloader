/*
----------------------------------------------------------------------------------------------------
Program Name : JComicDownloader
Authors  : surveyorK
Last Modified : 2011/12/25
----------------------------------------------------------------------------------------------------
ChangeLog:
2.11: 1. 修改下載機制，增加讀取GZIPInputStreamCommon.getStringUsingDefaultLanguage( 串流的選項（178.com專用）
 *   2. 修復重試後無法下載中間漏頁的問題。（ex. 5.jpg 7.jpg 8.jpg，中間遺漏6.jpg）
2.01: 1. 修改下載機制，不下載青蛙圖（檔案大小10771 bytes）
2.0 : 1. 加入下載快速模式，專用於google圖片下載
1.09: 1. 加入書籤表格和紀錄表格相關的公用方法
 *    2. 以getReomvedUnnecessaryWord()拿掉多餘標題字尾
1.08: 若下載圖檔時發現檔案只有21或22kb，則懷疑是盜連警示圖片，於一秒後重新連線一次
----------------------------------------------------------------------------------------------------
 */
package jcomicdownloader.tools;

import java.awt.Color;
import java.awt.Font;
import jcomicdownloader.table.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import jcomicdownloader.encode.*;
import jcomicdownloader.module.*;
import jcomicdownloader.*;

import java.io.*;
import java.util.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.*;
import java.util.zip.*;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import jcomicdownloader.enums.*;

/**

大部分的通用方法都放在這邊，全宣告為靜態，方便使用。
 */
public class Common {

    public static String recordDirectory = getNowAbsolutePath();
    public static String tempDirectory = getNowAbsolutePath() + "temp" + getSlash();
    public static String downloadDirectory = getNowAbsolutePath() + "down" + getSlash();
    public static String tempVolumeFileName = "temp_volume.txt";
    public static String tempUrlFileName = "temp_url.txt";
    public static String tempVolumeInformationFileName = "temp_volume_information.txt";
    public static boolean isMainPage = false;
    public static int missionCount = 0; // 目前任務總數
    public static int bookmarkCount = 0; // 目前書籤總數
    public static int recordCount = 0; // 目前記錄總數
    public static boolean downloadLock = false;
    public static Thread downloadThread;
    public static boolean urlIsUnknown = false;
    public static String prevClipString; // 用來檢查剪貼簿，若沒有變化就不要貼上輸入欄了
    public static String consoleThreadName = "Thread-console-version";
    public static String setFileName = "set.ini";
    public static int reconnectionTimes = 3; // 嘗試重新連線的最高次數
    public static String defaultAudioString = "使用預設音效";
    public static String defaultSingleDoneAudio = "single_done.wav";
    public static String defaultAllDoneAudio = "all_done.wav";
    public static String mainIcon = "main_icon.png";
    public static String playAudioPic = "play.png";

    public static String getZero() {
        int length = SetUp.getFileNameLength();

        String zero = "";
        for ( int i = 0 ; i < length ; i++ ) {
            zero += "0";
        }

        return zero;
    }

    public static String getZero( int zeroAmount ) {
        String zero = "";
        for ( int i = 0 ; i < zeroAmount ; i++ ) {
            zero += "0";
        }

        return zero;
    }

    public static void errorReport( String errorString ) {
        System.out.println( "ERROR: " + errorString );
        Run.isLegal = false;
    }

    public static void debugPrintln( String print ) { // for debug
        print = Common.getStringUsingDefaultLanguage( print ); // 使用預設語言 

        if ( Debug.debugMode ) {
            System.out.println( print );
        }
    }

    public static void debugPrint( String print ) { // for debug
        print = Common.getStringUsingDefaultLanguage( print ); // 使用預設語言 

        if ( Debug.debugMode ) {
            System.out.print( print );
        }
    }

    public static void processPrintln( String print ) { // for debug
        System.out.println( print );
    }

    public static void processPrint( String print ) { // for debug
        System.out.print( print );
    }

    public static void checkDirectory( String dir ) {
        // check if dir exists or not, if not exist, create one.
        if ( !new File( dir ).exists() ) {
            new File( dir ).mkdirs();
        }
    }

    public static void downloadManyFile( String[] webSite, String outputDirectory,
            String picFrontName, String extensionName ) {
        NumberFormat formatter = new DecimalFormat( Common.getZero() );

        // if we want to check "\", cannot use [\\], should use [\\\\] ...
        String[] pathStrings = outputDirectory.split( "[\\\\]|/" );
        String nowDownloadTitle = pathStrings[pathStrings.length - 2];
        String nowDownloadVolume = pathStrings[pathStrings.length - 1];

        String mainMessage = "下載 " + nowDownloadTitle + " / " + nowDownloadVolume + " ";

        for ( int i = 1 ; i <= webSite.length && Run.isAlive ; i++ ) {
            // 察知此圖片的副檔名(因為會呼叫downloadManyFile的都是下載圖片)
            String[] tempStrings = webSite[i - 1].split( "/|\\." );

            if ( tempStrings[tempStrings.length - 1].length() == 3 || // ex. jgp, png
                    tempStrings[tempStrings.length - 1].length() == 4 ) // ex. jpeg
            {
                extensionName = tempStrings[tempStrings.length - 1];
            }

            String fileName = picFrontName + formatter.format( i ) + "." + extensionName;
            String nextFileName = picFrontName + formatter.format( i + 1 ) + "." + extensionName;
            if ( webSite[i - 1] != null ) {
                // if not all download, the last file needs to re-download
                if ( !new File( outputDirectory + nextFileName ).exists()
                        || !new File( outputDirectory + fileName ).exists() ) {
                    CommonGUI.stateBarMainMessage = mainMessage;
                    CommonGUI.stateBarDetailMessage = "  :  " + "共" + webSite.length + "頁"
                            + "，第" + i + "頁下載中";

                    if ( Common.withGUI() && ComicDownGUI.trayIcon != null ) {
                        ComicDownGUI.trayIcon.setToolTip( CommonGUI.stateBarMainMessage
                                + CommonGUI.stateBarDetailMessage );
                    }

                    CommonGUI.stateBarDetailMessage += " : " + fileName;

                    downloadFile( webSite[i - 1], outputDirectory, fileName, false, "", "", false, SetUp.getRetryTimes(), false, false );

                }
                System.out.print( i + " " );
            }
        }
    }

    public static void slowDownloadFile( String webSite, String outputDirectory, String outputFileName,
            int delayMillisecond, boolean needCookie, String cookieString ) {
        try {
            Thread.currentThread().sleep( delayMillisecond );
        }
        catch ( InterruptedException ex ) {
            Common.hadleErrorMessage( ex, "下載過程中無法等待預定秒數" );
        }

        downloadFile( webSite, outputDirectory, outputFileName, needCookie, cookieString, "", false, SetUp.getRetryTimes(), false, false );

    }

    public static String[] getCookieStringsTest( String urlString, String postString ) {
        String[] tempCookieStrings = null;

        try {
            URL url = new URL( urlString );
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty( "User-Agent", "User-Agent: Mozilla/5.0 (Windows; U; Windows NT 6.1; zh-TW; rv:1.9.2.8) Gecko/20100722 Firefox/3.6.8" );
            connection.setDoInput( true );
            connection.setDoOutput( true );
            connection.setRequestMethod( "POST" );
            connection.getOutputStream().write( postString.getBytes() );
            connection.getOutputStream().flush();
            connection.getOutputStream().close();
            int code = connection.getResponseCode();
            System.out.println( "code   " + code );

            tempCookieStrings = tryConnect( connection );
        }
        catch ( Exception ex ) {
            Common.hadleErrorMessage( ex, "無法正確設置connection" );
        }

        String[] cookieStrings = tempCookieStrings;
        int cookieCount = 0;
        if ( tempCookieStrings != null ) {
            for ( int i = 0 ; i < tempCookieStrings.length ; i++ ) {
                if ( tempCookieStrings[i] != null ) {
                    cookieStrings[cookieCount++] = tempCookieStrings[i]; // 把cookie都集中到前面
                    System.out.println( cookieCount + " " + tempCookieStrings[i] );
                }
            }
        }

        return cookieStrings;
    }

    // 將所有cookies串起來
    public static String getCookieString( String urlString ) {
        String[] cookies = getCookieStrings( urlString );

        String cookie = "";
        for ( int i = 0 ; i < cookies.length && cookies[i] != null ; i++ ) {
            cookie += cookies[i] + "; ";
        }
        return cookie;
    }

    public static String[] getCookieStrings( String urlString ) {
        String[] tempCookieStrings = null;

        try {
            URL url = new URL( urlString );
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty( "User-Agent", "User-Agent: Mozilla/5.0 (Windows; U; Windows NT 6.1; zh-TW; rv:1.9.2.8) Gecko/20100722 Firefox/3.6.8" );
            tempCookieStrings = tryConnect( connection );
        }
        catch ( Exception ex ) {
            Common.hadleErrorMessage( ex, "無法正確設置connection" );
        }



        String[] cookieStrings = tempCookieStrings;
        int cookieCount = 0;
        if ( tempCookieStrings != null ) {

            for ( int i = 0 ; i < tempCookieStrings.length ; i++ ) {
                if ( tempCookieStrings[i] != null ) {
                    cookieStrings[cookieCount] = tempCookieStrings[i]; // 把cookie都集中到前面
                    System.out.println( cookieCount + ": " + tempCookieStrings[i] );
                    cookieCount++;
                }
            }
            Common.debugPrintln( "共有" + cookieCount + "串cookie" );
        }

        return cookieStrings;
    }

    // 普通下載模式，連線失敗會嘗試再次連線
    public static void downloadFile( String webSite, String outputDirectory, String outputFileName,
            boolean needCookie, String cookieString ) {
        downloadFile( webSite, outputDirectory, outputFileName, needCookie, cookieString, "", false, SetUp.getRetryTimes(), false, false );
    }

    // 普通下載模式，連線失敗會嘗試再次連線
    public static void downloadFile( String webSite, String outputDirectory, String outputFileName,
            boolean needCookie, String cookieString, String referURL ) {
        downloadFile( webSite, outputDirectory, outputFileName, needCookie, cookieString, referURL, false, SetUp.getRetryTimes(), false, false );
    }

    // 解壓縮下載模式，連線失敗會嘗試再次連線，且收取資料串流後會以Gzip解壓縮
    public static void downloadGZIPInputStreamFile( String webSite, String outputDirectory, String outputFileName,
            boolean needCookie, String cookieString ) {
        downloadFile( webSite, outputDirectory, outputFileName, needCookie, cookieString, "", false, SetUp.getRetryTimes(), true, false );
    }

    // 直接下載模式，不管Run.isAlive值為何都可以直接下載
    public static void downloadFileByForce( String webSite, String outputDirectory, String outputFileName,
            boolean needCookie, String cookieString ) {
        downloadFile( webSite, outputDirectory, outputFileName, needCookie, cookieString, "", false, SetUp.getRetryTimes(), false, true );
    }

    // 加速下載模式，連線失敗就跳過
    public static void downloadFileFast( String webSite, String outputDirectory, String outputFileName,
            boolean needCookie, String cookieString ) {
        downloadFile( webSite, outputDirectory, outputFileName, needCookie, cookieString, "", true, SetUp.getRetryTimes(), false, false );
    }

    // 查看此url是否有轉向其他url
    public static void testConnection( String url ) {
        try {
            URLConnection con = new URL( url ).openConnection();
            System.out.println( "orignal url: " + con.getURL() );
            con.connect();
            System.out.println( "connected url: " + con.getURL() );
            InputStream is = con.getInputStream();
            System.out.println( "redirected url: " + con.getURL() );
            is.close();
        }
        catch ( Exception ex ) {
            Common.hadleErrorMessage( ex, "無法正確設置connection" );
        }


        try {
            HttpURLConnection con = (HttpURLConnection) (new URL( url ).openConnection());
            con.setInstanceFollowRedirects( false );
            con.connect();
            int responseCode = con.getResponseCode();
            System.out.println( responseCode );
            String location = con.getHeaderField( "Location" );
            System.out.println( location );
        }
        catch ( Exception ex ) {
            Common.hadleErrorMessage( ex, "無法正確設置connection" );
        }

    }

    public static void downloadFile( String webSite, String outputDirectory, String outputFileName,
            boolean needCookie, String cookieString, String referURL, boolean fastMode, int retryTimes,
            boolean gzipEncode, boolean forceDownload ) {
        // downlaod file by URL

        int fileGotSize = 0;

        if ( CommonGUI.stateBarDetailMessage == null ) {
            CommonGUI.stateBarMainMessage = "下載網頁進行分析 : ";
            CommonGUI.stateBarDetailMessage = outputFileName + " ";
        }

        if ( Run.isAlive || forceDownload ) { // 當允許下載或強制下載時才執行連線程序
            try {

                ComicDownGUI.stateBar.setText( webSite + " 連線中..." );

                // google圖片下載時因為有些連線很久沒回應，所以要設置計時器，預防連線時間過長
                Timer timer = new Timer();
                if ( SetUp.getTimeoutTimer() > 0 ) {
                    // 預設(getTimeoutTimer()*1000)秒會timeout
                    timer.schedule( new TimeoutTask(), SetUp.getTimeoutTimer() * 1000 );
                }

                URL url = new URL( webSite );
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                // 偽裝成瀏覽器
                connection.setRequestProperty( "User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows 2000)" );
                //connection.setRequestProperty( "User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.1; zh-TW; rv:1.9.2.8) Gecko/20100722 Firefox/3.6.8" );

                // connection.setRequestMethod( "GET" ); // 默认是GET 
                //connection.setRequestProperty( "User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows 2000)" );
                //connection.setRequestProperty("User-Agent","Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; InfoPath.1; CIBA)");
                //connection.setFollowRedirects( true );
                //connection.setDoOutput( true ); // 需要向服务器写数据
                connection.setDoInput( true ); //

                // dm5加這一行無法下載...
                //connection.setUseCaches( false ); // // Post 请求不能使用缓存 
                connection.setAllowUserInteraction( false );

                //connection.setInstanceFollowRedirects( false ); // 不轉址


                if ( referURL != null && !referURL.equals( "" ) ) {
                    //Common.debugPrintln( "設置Referer=" + referURL );
                    connection.setRequestProperty( "Referer", "referURL" );
                }

                // 设定传送的内容类型是可序列化的java对象   
                // (如果不设此项,在传送序列化对象时,当WEB服务默认的不是这种类型时可能抛java.io.EOFException)
                connection.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded" );
                //connection.setRequestProperty( "Accept-Language", "zh-cn" );
                // connection.setRequestProperty("Content-Length", ""+data.length());
                connection.setRequestProperty( "Cache-Control", "no-cache" );
                connection.setRequestProperty( "Pragma", "no-cache" );
                //connection.setRequestProperty( "Host", "biz.finance.sina.com.cn" );
                connection.setRequestProperty( "Accept", "text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2" );
                connection.setRequestProperty( "Connection", "keep-alive" );

                connection.setConnectTimeout( 10000 ); // 與主機連接時間不能超過十秒


                if ( needCookie ) {
                    connection.setRequestMethod( "GET" );
                    connection.setDoOutput( true );
                    connection.setRequestProperty( "Cookie", cookieString );
                }

                int responseCode = 0;

                // 快速模式不下載青蛙圖！（其檔案大小就是10771......）
                if ( (fastMode && connection.getResponseCode() != 200)
                        || (fastMode && connection.getContentLength() == 10771) ) {
                    return;
                }

                tryConnect( connection );


                int fileSize = connection.getContentLength() / 1000;

                if ( Common.isPicFileName( outputFileName )
                        && (fileSize == 21 || fileSize == 22) ) { // 連到99系列的盜連圖
                    Common.debugPrintln( "似乎連到盜連圖，停一秒後重新連線......" );
                    try {
                        Thread.sleep( 1000 ); // 每次暫停一秒再重新連線
                    }
                    catch ( InterruptedException iex ) {
                        Common.hadleErrorMessage( iex, "無法等待既定秒數" );
                    }
                    tryConnect( connection );
                }
                // 內部伺服器發生錯誤，讀取getErrorStream() 
                if ( connection.getResponseCode() == 500 ) {
                }
                else if ( connection.getResponseCode() != 200 ) {
                    //Common.debugPrintln( "第二次失敗，不再重試!" );
                    Common.errorReport( "錯誤回傳碼(responseCode): "
                            + connection.getResponseCode() + " : " + webSite );
                    return;
                }

                Common.checkDirectory( outputDirectory ); // 檢查有無目標資料夾，若無則新建一個　

                //OutputStream os = response.getOutputStream();
                OutputStream os = new FileOutputStream( outputDirectory + outputFileName );
                InputStream is = null;

                if ( connection.getResponseCode() == 500 ) {
                    is = connection.getErrorStream(); // xindm
                }
                else if ( gzipEncode && fileSize < 17 ) // 178漫畫小於17kb就認定為已經壓縮過的
                {
                    try {
                        is = new GZIPInputStream( connection.getInputStream() ); // ex. 178.com
                    }
                    catch ( IOException ex ) {
                        is = connection.getInputStream(); // 其他漫畫網
                    }
                }
                else {
                    is = connection.getInputStream(); // 其他漫畫網
                }
                Common.debugPrint( "(" + fileSize + " k) " );
                String fileSizeString = fileSize > 0 ? "" + fileSize : " ? ";

                byte[] r = new byte[1024];
                int len = 0;

                while ( (len = is.read( r )) > 0 && (Run.isAlive || forceDownload) ) {
                    // 快速模式下，檔案小於1mb且連線超時 -> 切斷連線
                    if ( fileSize > 1024 || !Flag.timeoutFlag ) // 預防卡住的機制
                    {
                        os.write( r, 0, len );
                    }
                    else {
                        break;
                    }

                    fileGotSize += (len / 1000);

                    if ( Common.withGUI() ) {
                        int percent = 100;
                        String downloadText = "";
                        if ( fileSize > 0 ) {
                            percent = (fileGotSize * 100) / fileSize;
                            downloadText = fileSizeString + "Kb ( " + percent + "% ) ";
                        }
                        else {
                            downloadText = fileSizeString + " Kb ( " + fileGotSize + "Kb ) ";
                        }

                        ComicDownGUI.stateBar.setText( CommonGUI.stateBarMainMessage
                                + CommonGUI.stateBarDetailMessage
                                + " : " + downloadText );
                    }
                }

                is.close();
                os.flush();
                os.close();

                if ( Common.withGUI() ) {
                    ComicDownGUI.stateBar.setText( CommonGUI.stateBarMainMessage
                            + CommonGUI.stateBarDetailMessage
                            + " : " + fileSizeString + "Kb ( 100% ) " );
                }

                connection.disconnect();


                // 若真實下載檔案大小比預估來得小，則視設定值決定要重新嘗試幾次
                int realFileGotSize = (int) new File( outputDirectory + outputFileName ).length() / 1000;
                if ( realFileGotSize + 1 < fileGotSize && retryTimes > 0 ) {
                    String messageString = realFileGotSize + " < " + fileGotSize
                            + " -> 等待兩秒後重新嘗試下載" + outputFileName + "（" + retryTimes
                            + "/" + SetUp.getRetryTimes() + "）";
                    Common.debugPrintln( messageString );
                    ComicDownGUI.stateBar.setText( messageString );
                    Thread.sleep( 2000 ); // 每次暫停一秒再重新連線

                    downloadFile( webSite, outputDirectory, outputFileName,
                            needCookie, cookieString, referURL, fastMode, retryTimes - 1, gzipEncode, false );


                }

                if ( fileSize < 1024 && Flag.timeoutFlag ) {
                    new File( outputDirectory + outputFileName ).delete();
                    Common.debugPrintln( "刪除不完整檔案：" + outputFileName );

                    ComicDownGUI.stateBar.setText( "下載逾時，跳過" + outputFileName );

                }

                timer.cancel(); // 連線結束同時也關掉計時器

                Flag.timeoutFlag = false; // 歸回初始值

                Common.debugPrintln( webSite + " downloads successful!" ); // for debug

            }
            catch ( Exception e ) {
                Common.hadleErrorMessage( e, "無法正確下載" + webSite );
            }

            CommonGUI.stateBarDetailMessage = null;
        }
    }

    public static boolean urlIsOK( String urlString ) {

        boolean isOK = false;
        try {

            URL url = new URL( urlString );

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty( "User-Agent", "User-Agent: Mozilla/5.0 (Windows; U; Windows NT 6.1; zh-TW; rv:1.9.2.8) Gecko/20100722 Firefox/3.6.8" );

            connection.connect();

            if ( connection.getResponseCode() == HttpURLConnection.HTTP_OK ) {
                isOK = true;
                Common.debugPrintln( urlString + " 測試連線結果: OK" );
            }
            else {
                isOK = false;
                Common.debugPrintln( urlString + " 測試連線結果: 不OK ( " + connection.getResponseCode() + " )" );
            }
            connection.disconnect();

        }
        catch ( Exception e ) {
            Common.hadleErrorMessage( e, "無法正確設置connection" );
        }
        return isOK;
    }

    public static String[] tryConnect( HttpURLConnection connection ) {
        return tryConnect( connection, null );
    }

    public static String[] tryConnect( HttpURLConnection connection, String postString ) {
        String[] cookieStrings = new String[100];
        try {
            connection.connect();
            String headerName = "";
            for ( int i = 1 ; (headerName = connection.getHeaderFieldKey( i )) != null ; i++ ) {
                if ( headerName.equals( "Set-Cookie" ) ) {

                    cookieStrings[i - 1] = new String( connection.getHeaderField( i ) );
                    //System.out.println( i + " " + cookieStrings[i-1] );
                }
                else {
                    if ( headerName.matches( "Content-Length" ) ) {
                        System.out.println( headerName + " = " + connection.getHeaderField( i ) );
                    }
                    //System.out.println( headerName + " = " + connection.getHeaderField( i ) );
                }
            }
        }
        catch ( Exception ex ) {
            try {
                if ( connection.getResponseCode() != 200 && !Flag.timeoutFlag ) {
                    try {
                        Thread.sleep( 1000 ); // 每次暫停一秒再重新連線
                    }
                    catch ( InterruptedException iex ) {
                    }
                    Common.debugPrintln( "重新嘗試連線......" );
                    if ( Common.withGUI() ) {
                        ComicDownGUI.stateBar.setText( "重新嘗試連線......" );
                        connection.connect(); // 第二次嘗試連線
                    }
                }
            }
            catch ( Exception exx ) {
                exx.printStackTrace();
            }
        }
        return cookieStrings;
    }

    public static boolean isLegalURL( String webSite ) {

        boolean theURLisLegal = true;

        try {
            URL url = new URL( webSite );
        }
        catch ( MalformedURLException ex ) {
            theURLisLegal = false;
        }

        //String regex = "^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+\\+&@!#/%=~_|]";

        if ( theURLisLegal ) {
            return true;
        }
        else {
            return false;
        }

    }

    // -----------------------------------
    public static void compress( File source, File destination ) { //  compress to zip
        try {
            // Deflater.NO_COMPRESSION: 沒有壓縮，僅儲存
            compress( source, destination, null, Deflater.NO_COMPRESSION );
        }
        catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    public static void compress( File source, File destination,
            String comment, int level ) throws IOException {
        ZipOutputStream zos = new ZipOutputStream( new FileOutputStream( destination ) );
        zos.setComment( comment );
        zos.setLevel( level );
        compress( zos, source.getParent(), source );
        zos.flush();
        zos.close();
    }

    private static void compress( ZipOutputStream zos, String rootpath,
            File source ) throws IOException {
        // 下面這行原本用來取得壓縮檔中的圖片資料夾名稱，但會有亂碼，所以直接放外面。
        //String filename = source.toString().substring(rootpath.length() + 1);
        if ( source.isFile() ) {
            ZipEntry zipEntry = new ZipEntry( source.getName() );//filename );
            zos.putNextEntry( zipEntry );
            FileInputStream fis = new FileInputStream( source );
            byte[] buffer = new byte[1024];
            for ( int length ; (length = fis.read( buffer )) > 0 ; ) {
                zos.write( buffer, 0, length );
            }
            fis.close();
            zos.closeEntry();
        }
        else if ( source.isDirectory() ) {
            // 下面這三行是把資料夾加入到壓縮檔裡面，因為有亂碼，所以拿掉。
            //ZipEntry zipEntry = new ZipEntry( filename + "/" );
            //zos.putNextEntry( zipEntry );
            //zos.closeEntry();
            File[] files = source.listFiles();
            for ( File file : files ) {
                compress( zos, rootpath, file );
            }
        }
    }

    public static void deleteFolder( String folderPath ) {
        Common.debugPrintln( "刪除資料夾：" + folderPath );

        try {
            deleteAllFile( folderPath ); // delete all the file in dir
            String filePath = folderPath;
            filePath = filePath.toString();
            File myFilePath = new File( filePath );
            myFilePath.delete(); // delete empty dir
        }
        catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    public static boolean deleteAllFile( String path ) {
        boolean flag = false;
        File file = new File( path );
        if ( !file.exists() ) {
            return flag;
        }

        if ( !file.isDirectory() ) {
            return flag;
        }

        String[] tempList = file.list();
        File temp = null;
        for ( int i = 0 ; i < tempList.length ; i++ ) {
            if ( path.endsWith( File.separator ) ) {
                temp = new File( path + tempList[i] );
            }
            else {
                temp = new File( path + File.separator + tempList[i] );
            }

            if ( temp.isFile() ) {
                temp.delete();
            }
            if ( temp.isDirectory() ) {
                deleteAllFile( path + "/" + tempList[i] ); // first delete all files in dir
                deleteFolder( path + "/" + tempList[i] ); // and then delete the dir
                flag = true;
            }
        }
        return flag;
    }

    public static BufferedReader getBufferedReader( String filePath ) throws IOException {
        FileReader fr = new FileReader( filePath );
        return new BufferedReader( fr );
    }

    public static void outputFile( String ouputText, String filePath, String fileName ) {
        checkDirectory( filePath );

        try {
            FileOutputStream fout = new FileOutputStream( filePath + fileName );
            DataOutputStream dataout = new DataOutputStream( fout );
            byte[] data1 = ouputText.getBytes( "UTF-8" );
            dataout.write( data1 );
            fout.close();
            Common.debugPrintln( "寫出 " + filePath + fileName + " 檔案" );
        }
        catch ( IOException e ) {
            e.printStackTrace();
        }
    }

    public static void outputFile( String[] outputStrings, String filePath, String fileName ) {
        StringBuffer sb = new StringBuffer();
        for ( int i = 0 ; i < outputStrings.length ; i++ ) {
            sb.append( outputStrings[i] + "\n" );
        }

        outputFile( sb.toString(), filePath, fileName );
    }

    public static void outputFile( List outputList, String filePath, String fileName ) {
        StringBuffer sb = new StringBuffer();
        for ( int i = 0 ; i < outputList.size() ; i++ ) {
            sb.append( outputList.get( i ) + "\n" );
        }

        outputFile( sb.toString(), filePath, fileName );
    }

    public static void outputUrlFile( String[] urlStrings, String oldDownloadPath ) {
        String[] dirStrings = oldDownloadPath.split( "[\\\\]|/" );

        String urlFileName = dirStrings[dirStrings.length - 1] + ".txt";
        String downloadPath = "";

        for ( int i = 0 ; i < dirStrings.length - 1 ; i++ ) {
            downloadPath += dirStrings[i] + "/";
        }

        Common.processPrint( "輸出位址文件檔: " + urlFileName );
        outputFile( urlStrings, downloadPath, urlFileName );
    }

    public static String getFileString( String filePath, String fileName ) {
        String str = "";
        StringBuffer sb = new StringBuffer( "" );

        if ( new File( filePath + fileName ).exists() ) {
            try {
                FileInputStream fileInputStream = new FileInputStream( filePath + fileName );

                InputStreamReader inputStreamReader = new InputStreamReader( fileInputStream, "UTF8" );

                int ch = 0;
                while ( (ch = inputStreamReader.read()) != -1 ) {
                    sb.append( (char) ch );
                }

                fileInputStream.close(); // 加這句才能讓official.html刪除，還在實驗中

            }
            catch ( IOException e ) {
                Common.hadleErrorMessage( e, "無法讀入" + filePath + fileName );
                e.printStackTrace();
            }
        }
        else {
            Common.errorReport( "沒有找到" + filePath + fileName + "此一檔案" );
        }

        return sb.toString();
    }

    public static String[] getFileStrings( String filePath, String fileName ) {
        String[] tempStrings = getFileString( filePath, fileName ).split( "\\n|\\r" );

        return tempStrings;
        //return correctStrings;
    }

    public static String GBK2Unicode( String str ) {
        StringBuffer result = new StringBuffer();
        for ( int i = 0 ; i < str.length() ; i++ ) {
            char chr1 = str.charAt( i );
            if ( !isNeedConvert( chr1 ) ) {
                result.append( chr1 );
                continue;
            }
            result.append( "&#x" + Integer.toHexString( (int) chr1 ) + ";" );
        }
        return result.toString();
    }

    public static boolean isNeedConvert( char para ) {
        return ((para & (0x00FF)) != para);
    }

    public static String getStringUsingDefaultLanguage( String string ) {
        if ( SetUp.getDefaultLanguage() == LanguageEnum.TRADITIONAL_CHINESE ) {
            return string;
        }
        else if ( SetUp.getDefaultLanguage() == LanguageEnum.SIMPLIFIED_CHINESE ) {
            return Common.getSimplifiedChinese( string );
        }
        else {
            return string;
        }
    }

    public static String getTraditionalChinese( String gbString ) {
        // Simplified Chinese To Traditional Chinese
        Zhcode mycode = new Zhcode();

        if ( SetUp.getDefaultLanguage() == LanguageEnum.TRADITIONAL_CHINESE ) {
            return mycode.convertString( gbString, mycode.GB2312, mycode.BIG5 ).replaceAll( "[\\\\]ufffd", "_" );
        }
        else {
            return gbString;
        }
    }

    public static String getSimplifiedChinese( String big5String ) {
        Zhcode mycode = new Zhcode();
        String gbString = mycode.convertString( big5String, mycode.BIG5, mycode.GB2312 );
        return gbString.replace( "\\u51ea", "止" ).replace( "\\u9ed2", "黑" );
    }

    public static String getUtf8toUnicode( String utf8 ) {
        Zhcode mycode = new Zhcode();
        return mycode.convertString( utf8, mycode.UTF8, mycode.UNICODE );
    }

    public static String getUtf8toBig5( String utf8 ) {
        Zhcode mycode = new Zhcode();
        return mycode.convertString( utf8, mycode.UTF8, mycode.BIG5 );
    }

    public static String getUtf8toGB2312( String utf8 ) {
        Zhcode mycode = new Zhcode();
        return mycode.convertString( utf8, mycode.UTF8, mycode.GB2312 );
    }

    public static String getBig5toUtf8( String big5 ) {
        Zhcode mycode = new Zhcode();
        return mycode.convertString( big5, mycode.BIG5, mycode.UTF8 );
    }

    public static String getGB2312toUtf8( String gb ) {
        Zhcode mycode = new Zhcode();
        return mycode.convertString( gb, mycode.BIG5, mycode.UTF8 );
    }

    public static void newEncodeFile( String directory, String fileName, String encodeFileName ) {
        Common.newEncodeFile( directory, fileName, encodeFileName, Zhcode.GB2312 );
    }

    public static void newEncodeFile( String directory, String fileName, String encodeFileName, int encode ) {


        Zhcode mycode = new Zhcode();
        mycode.convertFile( directory + fileName,
                directory + encodeFileName,
                encode,
                mycode.UTF8 );
    }

    public static void newEncodeBIG5File( String directory, String fileName, String encodeFileName ) {
        Zhcode mycode = new Zhcode();
        mycode.convertFile( directory + fileName,
                directory + encodeFileName,
                mycode.BIG5,
                mycode.UTF8 );
    }

    public static String getConnectStrings( String[] strings ) {
        String str = "";

        for ( int i = 0 ; i < strings.length ; i++ ) {
            str += strings[i] + "####";
        }

        return str;
    }

    public static String[] getSeparateStrings( String connectString ) {
        return connectString.split( "####" );
    }

    public static int getTrueCountFromStrings( String[] strings ) {
        int count = 0;
        for ( String str : strings ) {
            if ( str.equals( "true" ) ) {
                count++;
            }
        }
        return count;
    }

    public static String[] getCopiedStrings( String[] copiedStrings ) {
        String[] newStrings = new String[copiedStrings.length];

        for ( int i = 0 ; i < copiedStrings.length ; i++ ) {
            newStrings[i] = copiedStrings[i];
        }

        return newStrings;
    }

    public static String getStringReplaceHttpCode( String oldString ) {
        String string = oldString;
        string = string.replace( "&#039;", "'" );
        string = string.replace( "&lt;", "＜" );
        string = string.replace( "&gt;", "＞" );
        string = string.replace( "&amp;", "&" );
        string = string.replace( "&nbsp;", "　" );
        string = string.replace( "&quot;", "''" );
        string = string.replaceAll( "&#8226;", "•" );
        return string;
    }

    public static String getStringRemovedIllegalChar( String oldString ) {
        // "\/:*?"<>|"
        oldString = getStringReplaceHttpCode( oldString ); // 先經過html字符編碼轉換
        String newString = "";

        for ( int i = 0 ; i < oldString.length() ; i++ ) {
            if ( oldString.charAt( i ) == '\\'
                    || oldString.charAt( i ) == '/'
                    || oldString.charAt( i ) == '*'
                    || oldString.charAt( i ) == '"'
                    || oldString.charAt( i ) == '<'
                    || oldString.charAt( i ) == '>'
                    || oldString.charAt( i ) == '|'
                    || oldString.charAt( i ) == '.' ) {

                newString += String.valueOf( '_' );
            }
            else if ( oldString.charAt( i ) == '?'
                    || oldString.charAt( i ) == ':' ) {
                newString += String.valueOf( ' ' );
            }
            else if ( oldString.charAt( i ) == '&' ) {
                newString += String.valueOf( '＆' );
            }
            else if ( oldString.charAt( i ) == '\'' ) {
            }
            else {
                newString += String.valueOf( oldString.charAt( i ) );
            }
        }

        return Common.getReomvedUnnecessaryWord( newString );
    }

    public static String getReomvedUnnecessaryWord( String title ) {

        if ( title.matches( "(?s).+九九漫畫" ) ) // 拿掉多餘字尾
        {
            title = title.substring( 0, title.length() - 4 );
        }
        else if ( title.matches( "(?s).+手機漫畫" ) ) // 拿掉多餘字尾
        {
            title = title.substring( 0, title.length() - 4 );
        }
        else if ( title.matches( "(?s).+第一漫畫" ) ) // 拿掉多餘字尾
        {
            title = title.substring( 0, title.length() - 4 );
        }
        else if ( title.matches( "(?s).+漫畫" ) ) // 拿掉[漫畫]字尾
        {
            title = title.substring( 0, title.length() - 2 );
        }

        return title.trim();
    }

    public static boolean withGUI() { // check the running app is GUI version or console version
        if ( Thread.currentThread().getName().equals( consoleThreadName ) ) {
            return false;
        }
        else {
            return true;
        }
    }

    public static String getStoredFileName( String outputDirectory,
            String defaultFileName,
            String defaultExtensionName ) {
        int indexNameNo = 0;
        boolean over = false;
        while ( over ) {
            File tempFile = new File( outputDirectory + defaultFileName
                    + indexNameNo + "." + defaultExtensionName );
            if ( tempFile.exists() && (!tempFile.canRead() || !tempFile.canWrite()) ) {
                indexNameNo++;
            }
            else {
                over = true;
            }
        }

        return defaultFileName + indexNameNo + "." + defaultExtensionName;
    }

    public static String getAbsolutePath( String relativePath ) {
        return new File( relativePath ).getAbsolutePath();
    }

    public static boolean isWindows() { // windows
        String os = System.getProperty( "os.name" ).toLowerCase();
        return (os.indexOf( "win" ) >= 0);
    }

    public static boolean isMac() { // Mac
        String os = System.getProperty( "os.name" ).toLowerCase();
        return (os.indexOf( "mac" ) >= 0);
    }

    public static boolean isUnix() { // linux or unix
        String os = System.getProperty( "os.name" ).toLowerCase();
        return (os.indexOf( "nix" ) >= 0 || os.indexOf( "nux" ) >= 0);

    }

    public static String getSlash() {
        if ( Common.isWindows() ) {
            return "\\";
        }
        else {
            return "/";
        }
    }

    public static String getRegexSlash() { // \\要轉為\\\\
        if ( Common.isWindows() ) {
            return "\\\\";
        }
        else {
            return "/";
        }
    }

    // 廢棄
    public static String getNowAbsolutePathOld() {
        return new File( "" ).getAbsolutePath() + getSlash();
    }

    // 取得string中第order個keyword的位置
    public static int getIndexOfOrderKeyword( String string, String keyword, int order ) {
        int index = 0;
        for ( int i = 0 ; i < order && index >= 0 ; i++ ) {
            index++;
            index = string.indexOf( keyword, index );
        }

        return index;
    }

    // 取得string中從beginIndex開始數起來第order個keyword的位置
    public static int getIndexOfOrderKeyword( String string, String keyword, int order, int beginIndex ) {
        String newString = string.substring( beginIndex, string.length() );

        int index = 0;
        for ( int i = 0 ; i < order && index >= 0 ; i++ ) {
            index++;
            index = newString.indexOf( keyword, index );
        }

        return index;
    }

    // 找出從beginIndex開始，keyword1和keyword2在string中的位置（index），並回傳較小的index
    public static int getSmallerIndexOfTwoKeyword( String string, int beginIndex, String keyword1, String keyword2 ) {
        int index1 = string.indexOf( keyword1, beginIndex );
        int index2 = string.indexOf( keyword2, beginIndex );

        if ( index1 < 0 ) {
            return index2;
        }
        else if ( index2 < 0 ) {
            return index1;
        }
        else {
            return index1 < index2 ? index1 : index2;
        }
    }

    // 找出keyword1和keyword2在string中的位置（index），並回傳較大的index
    public static int getBiggerIndexOfTwoKeyword( String string, String keyword1, String keyword2 ) {
        int index1 = string.lastIndexOf( keyword1 );
        int index2 = string.lastIndexOf( keyword2 );

        if ( index1 < 0 ) {
            return index2;
        }
        else if ( index2 < 0 ) {
            return index1;
        }
        else {
            return index1 > index2 ? index1 : index2;
        }
    }

    // 寫出目前的下載任務清單
    public static void outputDownTableFile( DownloadTableModel downTableModel ) {
        StringBuffer sb = new StringBuffer();
        for ( int row = 0 ; row < Common.missionCount ; row++ ) {
            // 有勾選下載才會儲存！ -> 即使沒有勾選還是儲存！
            //if ( downTableModel.getValueAt( row, DownTableEnum.YES_OR_NO ).toString().equals( "true" ) ) {
            if ( SetUp.getKeepUndoneDownloadMission() ) { // 保存未完成任務
                if ( !downTableModel.getValueAt( row, DownTableEnum.STATE ).toString().equals( "下載完畢" ) ) {
                    for ( int col = 0 ; col < ComicDownGUI.getDownloadColumns().size() ; col++ ) {
                        sb.append( downTableModel.getRealValueAt( row, col ).toString() );
                        sb.append( "@@@@@@" );
                    }
                    sb.append( ComicDownGUI.downTableUrlStrings[row] );
                    sb.append( "%%%%%%" );
                }
            }
            if ( SetUp.getKeepDoneDownloadMission() ) { // 保存已完成任務
                if ( downTableModel.getValueAt( row, DownTableEnum.STATE ).toString().equals( "下載完畢" ) ) {
                    for ( int col = 0 ; col < ComicDownGUI.getDownloadColumns().size() ; col++ ) {
                        sb.append( downTableModel.getRealValueAt( row, col ).toString() );
                        sb.append( "@@@@@@" );
                    }
                    sb.append( ComicDownGUI.downTableUrlStrings[row] );
                    sb.append( "%%%%%%" );
                }
            }
            //}
        }

        sb.append( "_OVER_" );
        outputFile( sb.toString(), SetUp.getRecordFileDirectory(), "downloadList.dat" );
    }

    // 寫出目前的書籤清單
    public static void outputBookmarkTableFile( BookmarkTableModel bookmarkTableModel ) {
        StringBuffer sb = new StringBuffer();
        for ( int row = 0 ; row < Common.bookmarkCount ; row++ ) {
            if ( SetUp.getKeepBookmark() ) { // 保存書籤
                for ( int col = 0 ; col < ComicDownGUI.getBookmarkColumns().size() ; col++ ) {
                    sb.append( bookmarkTableModel.getValueAt( row, col ).toString() );
                    sb.append( "@@@@@@" );
                }
                sb.append( String.valueOf( bookmarkTableModel.getValueAt( row, RecordTableEnum.URL ) ) );
                sb.append( "%%%%%%" );
            }
        }

        sb.append( "_OVER_" );
        outputFile( sb.toString(), SetUp.getRecordFileDirectory(), "bookmarkList.dat" );
    }

    // 寫出目前的記錄清單
    public static void outputRecordTableFile( RecordTableModel recordTableModel ) {
        StringBuffer sb = new StringBuffer();
        for ( int row = 0 ; row < Common.recordCount ; row++ ) {
            if ( SetUp.getKeepRecord() ) { // 保存記錄
                for ( int col = 0 ; col < ComicDownGUI.getRecordColumns().size() ; col++ ) {
                    sb.append( recordTableModel.getValueAt( row, col ).toString() );
                    sb.append( "@@@@@@" );
                }
                sb.append( recordTableModel.getValueAt( row, RecordTableEnum.URL ).toString() );
                sb.append( "%%%%%%" );
            }
        }

        sb.append( "_OVER_" );
        outputFile( sb.toString(), SetUp.getRecordFileDirectory(), "recordList.dat" );
    }

    // 讀入之前儲存的下載任務清單
    public static DownloadTableModel inputDownTableFile() {
        String dataString = getFileString( SetUp.getRecordFileDirectory(), "downloadList.dat" );

        if ( !dataString.matches( "\\s*_OVER_\\s*" ) ) { // 之前有記錄下載清單
            String[] rowStrings = dataString.split( "%%%%%%" );
            Common.debugPrint( "將讀入下載任務數量: " + (rowStrings.length - 1) );
            DownloadTableModel downTableModel = new DownloadTableModel( ComicDownGUI.getDownloadColumns(),
                    rowStrings.length - 1 );
            try {
                for ( int row = 0 ; row < rowStrings.length - 1 ; row++ ) {
                    String[] colStrings = rowStrings[row].split( "@@@@@@" );

                    for ( int col = 0 ; col < ComicDownGUI.getDownloadColumns().size() ; col++ ) {
                        if ( col == DownTableEnum.YES_OR_NO ) {
                            downTableModel.setValueAt( Boolean.valueOf( colStrings[col] ), row, col );
                        }
                        else if ( col == DownTableEnum.ORDER ) {
                            downTableModel.setValueAt( new Integer( row + 1 ), row, col );
                        }
                        else {
                            downTableModel.setValueAt( colStrings[col], row, col );
                        }
                    }
                    ComicDownGUI.downTableUrlStrings[row] = colStrings[ComicDownGUI.getDownloadColumns().size()];
                    Common.missionCount++;

                }
                Common.debugPrintln( "   ... 讀入完畢!!" );
            }
            catch ( Exception ex ) {
                Common.hadleErrorMessage( ex, "無法讀入下載清單" );
                cleanDownTable();
                new File( "downloadList.dat" ).delete();
            }


            return downTableModel;
        }
        else {
            return new DownloadTableModel( ComicDownGUI.getDownloadColumns(), 0 );
        }

    }

    // 讀入之前儲存的書籤清單
    public static BookmarkTableModel inputBookmarkTableFile() {
        String dataString = getFileString( SetUp.getRecordFileDirectory(), "bookmarkList.dat" );

        if ( !dataString.matches( "\\s*_OVER_\\s*" ) ) { // 之前有記錄下載清單
            String[] rowStrings = dataString.split( "%%%%%%" );
            Common.debugPrint( "將讀入書籤數量: " + (rowStrings.length - 1) );
            BookmarkTableModel tableModel = new BookmarkTableModel( ComicDownGUI.getBookmarkColumns(),
                    rowStrings.length - 1 );
            try {
                for ( int row = 0 ; row < rowStrings.length - 1 ; row++ ) {
                    String[] colStrings = rowStrings[row].split( "@@@@@@" );

                    for ( int col = 0 ; col < ComicDownGUI.getBookmarkColumns().size() ; col++ ) {
                        //Common.debugPrint( colStrings[col] + " " );
                        if ( col == BookmarkTableEnum.ORDER ) {
                            tableModel.setValueAt( new Integer( row + 1 ), row, col );
                        }
                        else {
                            tableModel.setValueAt( colStrings[col], row, col );
                        }
                    }
                    Common.bookmarkCount++;

                    //Common.debugPrintln( " 讀取OK！" );
                }
                Common.debugPrintln( "   ... 讀入完畢!!" );
            }
            catch ( Exception ex ) {
                Common.hadleErrorMessage( ex, "無法讀入書籤清單" );
            }

            return tableModel;
        }
        else {
            return new BookmarkTableModel( ComicDownGUI.getBookmarkColumns(), 0 );
        }

    }

    // 讀入之前儲存的記錄清單
    public static RecordTableModel inputRecordTableFile() {
        String dataString = getFileString( SetUp.getRecordFileDirectory(), "recordList.dat" );

        if ( !dataString.matches( "\\s*_OVER_\\s*" ) ) { // 之前有記錄下載清單
            String[] rowStrings = dataString.split( "%%%%%%" );
            Common.debugPrint( "將讀入記錄數量: " + (rowStrings.length - 1) );
            RecordTableModel tableModel = new RecordTableModel( ComicDownGUI.getRecordColumns(),
                    rowStrings.length - 1 );
            try {
                for ( int row = 0 ; row < rowStrings.length - 1 ; row++ ) {
                    String[] colStrings = rowStrings[row].split( "@@@@@@" );

                    for ( int col = 0 ; col < ComicDownGUI.getRecordColumns().size() ; col++ ) {
                        //Common.debugPrint( colStrings[col] + " " );

                        if ( col == RecordTableEnum.ORDER ) {
                            tableModel.setValueAt( new Integer( row + 1 ), row, col );
                        }
                        else {
                            tableModel.setValueAt( colStrings[col], row, col );
                        }
                    }
                    Common.recordCount++;

                    //Common.debugPrintln( " 讀取OK！" );
                }
                Common.debugPrintln( "   ... 讀入完畢!!" );
            }
            catch ( Exception ex ) {
                Common.hadleErrorMessage( ex, "無法讀入紀錄清單" );
            }

            return tableModel;
        }
        else {
            return new RecordTableModel( ComicDownGUI.getRecordColumns(), 0 );
        }

    }

    public static void deleteFile( String filePath, String fileName ) {
        File file = new File( filePath + fileName );

        if ( file.exists() && file.isFile() ) {
            Common.debugPrintln( "刪除暫存檔案：" + fileName );
            file.delete();
        }
    }

    public static void deleteFile( String fileName ) {
        File file = new File( fileName );

        if ( file.exists() && file.isFile() ) {
            Common.debugPrintln( "刪除暫存檔案：" + fileName );
            file.delete();
        }
    }

    public static void setHttpProxy( String proxyServer, String proxyPort ) {
        Properties systemProperties = System.getProperties();
        systemProperties.setProperty( "proxySet", "true" );
        systemProperties.setProperty( "http.proxyHost", proxyServer );
        systemProperties.setProperty( "http.proxyPort", proxyPort );
    }

    public static void closeHttpProxy() {
        Properties systemProperties = System.getProperties();
        systemProperties.setProperty( "proxySet", "false" );
    }

    public static boolean isPicFileName( String fileName ) {
        if ( fileName.matches( "(?s).*\\.jpg" )
                || fileName.matches( "(?s).*\\.JPG" )
                || fileName.matches( "(?s).*\\.png" )
                || fileName.matches( "(?s).*\\.PNG" )
                || fileName.matches( "(?s).*\\.gif" )
                || fileName.matches( "(?s).*\\.GIF" )
                || fileName.matches( "(?s).*\\.jpeg" )
                || fileName.matches( "(?s).*\\.JPEG" )
                || fileName.matches( "(?s).*\\.bmp" )
                || fileName.matches( "(?s).*\\.BMP" ) ) {
            return true;
        }
        else {
            return false;
        }

    }

    // direcotory裏面第p張圖片是否存在
    public static boolean existPicFile( String directory, int p ) {
        NumberFormat formatter = new DecimalFormat( Common.getZero() );
        String fileName = formatter.format( p );
        if ( new File( directory + fileName + ".jpg" ).exists()
                || new File( directory + fileName + ".JPG" ).exists()
                || new File( directory + fileName + ".png" ).exists()
                || new File( directory + fileName + ".PNG" ).exists()
                || new File( directory + fileName + ".gif" ).exists()
                || new File( directory + fileName + ".GIF" ).exists()
                || new File( directory + fileName + ".jpeg" ).exists()
                || new File( directory + fileName + ".JPEG" ).exists()
                || new File( directory + fileName + ".bmp" ).exists()
                || new File( directory + fileName + ".BMP" ).exists() ) {
            return true;
        }
        else {
            return false;
        }

    }

    public static void cleanDownTable() {
        DefaultTableModel table = ComicDownGUI.downTableModel;
        if ( table != null ) {
            int downListCount = table.getRowCount();
            while ( table.getRowCount() > 1 ) {
                table.removeRow( table.getRowCount() - 1 );
                Common.missionCount--;
            }
            if ( Common.missionCount > 0 ) {
                table.removeRow( 0 );
            }
            ComicDownGUI.mainFrame.repaint(); // 重繪

            Common.missionCount = 0;
            Common.processPrintln( "因讀入錯誤，將全部任務清空" );
            ComicDownGUI.stateBar.setText( "下載任務檔格式錯誤，無法讀取!!" );
        }
    }

    public static String getHtmlStringWithColor( String string, String color ) {
        return "<html><font color=" + color + string + "</font></html>";
    }

    // 字串A裡面有幾個字串B
    public static int getAmountOfString( String aString, String bString ) {
        int bLength = bString.length();

        int conformTimes = 0; // 符合次數
        for ( int i = 0 ; i < aString.length() ; i += bLength ) {
            if ( aString.substring( i, i + bLength ).equals( bString ) ) {
                conformTimes++;
            }
        }

        //System.out.println( bString + "符合次數: " + conformTimes );

        return conformTimes;
    }

    public static String getNowAbsolutePath() {
        if ( Common.isUnix() ) {

            String apath = Common.class.getProtectionDomain().getCodeSource().getLocation().getPath();
            try {
                apath = URLDecoder.decode( apath, "UTF-8" );
            }
            catch ( UnsupportedEncodingException ex ) {
                Common.hadleErrorMessage( ex, "無法將網址轉為utf8編碼" );
            }


            String absolutePath;
            if ( apath.endsWith( ".jar" ) ) {
                absolutePath = apath.replaceAll( "([^/\\\\]+).jar$", "" );
            }
            else {
                absolutePath = new File( "" ).getAbsolutePath() + Common.getSlash();
            }

            return absolutePath;
        }
        else {
            return new File( "" ).getAbsolutePath() + getSlash();
        }

    }

    public static void playSingleDoneAudio() {
        playSingleDoneAudio( SetUp.getSingleDoneAudioFile() );
    }

    public static void playSingleDoneAudio( String fileString ) {
        if ( new File( fileString ).exists() ) {
            playAudio( fileString, false );
        }
        else {
            playAudio( Common.defaultSingleDoneAudio, true );
        }
    }

    public static void playAllDoneAudio() {
        playAllDoneAudio( SetUp.getAllDoneAudioFile() );
    }

    public static void playAllDoneAudio( String fileString ) {
        if ( new File( fileString ).exists() ) {
            playAudio( fileString, false );
        }
        else {
            playAudio( Common.defaultAllDoneAudio, true );
        }
    }

    // 播放音效
    private static void playAudio( final String audioFileString, final boolean defaultResource ) {
        Thread playThread = new Thread( new Runnable() {

            public void run() {
                try {
                    AudioInputStream ais;

                    if ( defaultResource ) { // 預設音效
                        URL audioFileURL = new CommonGUI().getResourceURL( audioFileString );
                        ais = AudioSystem.getAudioInputStream( audioFileURL );
                    }
                    else { // 外部音效
                        File audioFile = new File( audioFileString );
                        ais = AudioSystem.getAudioInputStream( audioFile );
                    }

                    AudioFormat af = ais.getFormat();
                    DataLine.Info inf = new DataLine.Info( SourceDataLine.class, af );
                    SourceDataLine sdl = (SourceDataLine) AudioSystem.getLine( inf );
                    sdl.open( af );
                    sdl.start();
                    byte[] buf = new byte[65536];
                    for ( int n = 0 ; (n = ais.read( buf, 0, buf.length )) > 0 ; ) {
                        sdl.write( buf, 0, n );
                    }
                    sdl.drain();
                    sdl.close();
                }
                catch ( Exception e ) {
                    Common.hadleErrorMessage( e, "無法播放" + audioFileString );
                }
            }
        } );
        playThread.start();
    }

    static public String getFixedChineseURL( String url ) {
        // ex. "收?的十二月" should be changed into
        //     "%E6%94%B6%E8%8E%B7%E7%9A%84%E5%8D%81%E4%BA%8C%E6%9C%88"

        try {
            String temp = "";

            for ( int k = 0 ; k < url.length() ; k++ ) {
                // \u0080-\uFFFF -> 中日韓3byte以上的字符
                if ( url.substring( k, k + 1 ).matches( "(?s).*[\u0080-\uFFFF]+(?s).*" ) ) {
                    temp += URLEncoder.encode( url.substring( k, k + 1 ), "UTF-8" );
                }
                else {
                    temp += url.substring( k, k + 1 );
                }

            }
            url = temp;
        }
        catch ( Exception e ) {
             Common.hadleErrorMessage( e, "無法將中文網址轉為正確網址編碼" );
        }

        url = url.replaceAll( "\\s", "%20" );
        //url = fixSpecialCase( url );

        return url;
    }

    // 非windows系統時的操作
    // openFileManger: 開啟檔案總管
    public static void runCmd( String program, String file, boolean openFileManger ) {
        String path = file;
        String cmd = program;

        // 檔案不存在就只顯示訊息而不繼續操作
        if ( !new File( file ).exists() ) {
            String nowSkinName = UIManager.getLookAndFeel().getName(); // 目前使用中的面板名稱
            String colorString = "blue";
            if ( nowSkinName.equals( "HiFi" ) || nowSkinName.equals( "Noire" ) ) {
                colorString = "yellow";
            }

            CommonGUI.showMessageDialog( ComicDownGUI.mainFrame, "<html><font color=" + colorString + ">"
                    + file + "</font>" + "不存在，無法開啟</html>",
                    "提醒訊息", JOptionPane.INFORMATION_MESSAGE );
            return;
        }

        String[] fileList = new File( file ).list();
        System.out.println( file );

        String firstCompressFileName = "";
        boolean existCompressFile = false;
        for ( int i = 0 ; i < fileList.length ; i++ ) {
            System.out.println( "FILE: " + fileList[i] );
            if ( fileList[i].matches( "(?s).*\\.zip" )
                    || fileList[i].matches( "(?s).*\\.cbz" ) ) {
                firstCompressFileName = fileList[i];
                existCompressFile = true;
                break;
            }
        }

        if ( !openFileManger ) {
            if ( existCompressFile ) {
                // 資料夾內存在壓縮檔
                path = file + Common.getSlash() + firstCompressFileName;
            }
            else {
                String[] picList = new File( file + Common.getSlash() + fileList[0] ).list();
                String firstPicFileInFirstVolume = "";

                if ( picList != null ) {
                    firstPicFileInFirstVolume = picList[0];
                }

                path = file + Common.getSlash() + fileList[0]
                        + Common.getSlash() + firstPicFileInFirstVolume;
            }
        }

        Common.debugPrintln( "開啟命令：" + cmd + path );

        try {
            String[] cmds = new String[] { cmd, path };
            Runtime.getRuntime().exec( cmds, null, new File( Common.getNowAbsolutePath() ) );
            //Runtime.getRuntime().exec(cmd + path);

        }
        catch ( IOException ex ) {
            Common.hadleErrorMessage( ex, "無法執行此命令：" + cmd + " " + path );
        }
    }

    // 解決非ANSI字會變成？而無法使用程式開啟的問題
    // 出處：http://stackoverflow.com/questions/1876507/java-runtime-exec-on-windows-fails-with-unicode-in-arguments
    public static void runUnansiCmd( String program, String file ) {
        if ( !new File( file ).exists() ) {
            //String nowSkinName = UIManager.getLookAndFeel().getName(); // 目前使用中的面板名稱
            // 取得介面設定值（不用UIManager.getLookAndFeel().getName()是因為這樣才能讀到_之後的參數）
            String nowSkinName = SetUp.getSkinClassName();
            String colorString = "blue";
            if ( CommonGUI.isDarkSytleSkin( nowSkinName ) ) {
                colorString = "yellow"; // 暗色風格界面用黃色比較看得清楚
            }

            CommonGUI.showMessageDialog( ComicDownGUI.mainFrame, "<html><font color=" + colorString + ">"
                    + file + "</font>" + "不存在，無法開啟</html>",
                    "提醒訊息", JOptionPane.INFORMATION_MESSAGE );
            return;
        }

        file = "\"" + file + "\"";

        String[] cmd = new String[] { program, file };
        Map<String, String> newEnv = new HashMap<String, String>();
        newEnv.putAll( System.getenv() );
        String[] i18n = new String[cmd.length + 2];
        i18n[0] = "cmd";
        i18n[1] = "/C";
        i18n[2] = cmd[0];
        for ( int counter = 1 ; counter < cmd.length ; counter++ ) {
            String envName = "JENV_" + counter;
            i18n[counter + 2] = "%" + envName + "%";
            newEnv.put( envName, cmd[counter] );
        }
        cmd = i18n;

        ProcessBuilder pb = new ProcessBuilder( cmd );
        Map<String, String> env = pb.environment();
        env.putAll( newEnv );
        try {
            final Process p = pb.start();
        }
        catch ( IOException ex ) {
            Common.hadleErrorMessage( ex, "無法執行此命令：" + cmd );
        }
    }

    public static void downloadPost( String webSite, String outputDirectory,
            String outputFileName, boolean needCookie, String cookieString, String postString, String referURL ) {
        // downlaod file by URL

        boolean gzipEncode = false;
        int retryTimes = 0;
        boolean forceDownload = false;
        boolean fastMode = false;

        int fileGotSize = 0;


        if ( CommonGUI.stateBarDetailMessage == null ) {
            CommonGUI.stateBarMainMessage = "下載網頁進行分析 : ";
            CommonGUI.stateBarDetailMessage = outputFileName + " ";
        }

        if ( Run.isAlive || forceDownload ) { // 當允許下載或強制下載時才執行連線程序
            try {

                ComicDownGUI.stateBar.setText( webSite + " 連線中..." );

                URL url = new URL( webSite );
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                // 偽裝成瀏覽器

                connection.setDoOutput( true );
                connection.setDoInput( true );
                ((HttpURLConnection) connection).setRequestMethod( "POST" );
                connection.setUseCaches( false );
                connection.setAllowUserInteraction( true );
                HttpURLConnection.setFollowRedirects( true );
                connection.setInstanceFollowRedirects( true );

                connection.setRequestProperty(
                        "User-agent",
                        "Mozilla/5.0 (Windows; U; Windows NT 6.0; zh-TW; rv:1.9.1.2) "
                        + "Gecko/20090729 Firefox/3.5.2 GTB5 (.NET CLR 3.5.30729)" );
                connection.setRequestProperty( "Accept",
                        "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8" );
                connection.setRequestProperty( "Accept-Language",
                        "zh-tw,en-us;q=0.7,en;q=0.3" );
                connection.setRequestProperty( "Accept-Charse",
                        "Big5,utf-8;q=0.7,*;q=0.7" );
                if ( cookieString != null ) {
                    connection.setRequestProperty( "Cookie", cookieString );
                }
                if ( referURL != null ) {
                    connection.setRequestProperty( "Referer", referURL );
                }

                connection.setRequestProperty( "Content-Type",
                        "application/x-www-form-urlencoded" );


                connection.setRequestProperty( "Content-Length", String.valueOf( postString.getBytes().length ) );


                // google圖片下載時因為有些連線很久沒回應，所以要設置計時器，預防連線時間過長
                Timer timer = new Timer();
                if ( SetUp.getTimeoutTimer() > 0 ) {
                    // 預設(getTimeoutTimer()*1000)秒會timeout
                    timer.schedule( new TimeoutTask(), SetUp.getTimeoutTimer() * 1000 );
                }

                Common.checkDirectory( outputDirectory ); // 檢查有無目標資料夾，若無則新建一個　

                //OutputStream os = response.getOutputStream();
                OutputStream os = new FileOutputStream( outputDirectory + outputFileName );
                InputStream is = null;


                java.io.DataOutputStream dos = new java.io.DataOutputStream(
                        connection.getOutputStream() );
                dos.writeBytes( postString );
                dos.close();

                //tryConnect( connection );

                if ( connection.getResponseCode() != 200 ) {
                    //Common.debugPrintln( "第二次失敗，不再重試!" );
                    Common.errorReport( "錯誤回傳碼(responseCode): "
                            + connection.getResponseCode() + " : " + webSite );
                    return;
                }

                is = connection.getInputStream();

                int fileSize = connection.getContentLength() / 1000;
                Common.debugPrint( "(" + fileSize + " k) " );
                String fileSizeString = fileSize > 0 ? "" + fileSize : " ? ";

                byte[] r = new byte[1024];
                int len = 0;

                while ( (len = is.read( r )) > 0 && (Run.isAlive || forceDownload) ) {
                    // 快速模式下，檔案小於1mb且連線超時 -> 切斷連線
                    if ( fileSize > 1024 || !Flag.timeoutFlag ) // 預防卡住的機制
                    {
                        os.write( r, 0, len );
                    }
                    else {
                        break;
                    }

                    fileGotSize += (len / 1000);

                    if ( Common.withGUI() ) {
                        int percent = 100;
                        String downloadText = "";
                        if ( fileSize > 0 ) {
                            percent = (fileGotSize * 100) / fileSize;
                            downloadText = fileSizeString + "Kb ( " + percent + "% ) ";
                        }
                        else {
                            downloadText = fileSizeString + " Kb ( " + fileGotSize + "Kb ) ";
                        }

                        ComicDownGUI.stateBar.setText( CommonGUI.stateBarMainMessage
                                + CommonGUI.stateBarDetailMessage
                                + " : " + downloadText );
                    }
                }

                is.close();
                os.flush();
                os.close();




                if ( Common.withGUI() ) {
                    ComicDownGUI.stateBar.setText( CommonGUI.stateBarMainMessage
                            + CommonGUI.stateBarDetailMessage
                            + " : " + fileSizeString + "Kb ( 100% ) " );
                }

                connection.disconnect();


                // 若真實下載檔案大小比預估來得小，則視設定值決定要重新嘗試幾次
                int realFileGotSize = (int) new File( outputDirectory + outputFileName ).length() / 1000;
                if ( realFileGotSize + 1 < fileGotSize && retryTimes > 0 ) {
                    String messageString = realFileGotSize + " < " + fileGotSize
                            + " -> 等待兩秒後重新嘗試下載" + outputFileName + "（" + retryTimes
                            + "/" + SetUp.getRetryTimes() + "）";
                    Common.debugPrintln( messageString );
                    ComicDownGUI.stateBar.setText( messageString );
                    Thread.sleep( 2000 ); // 每次暫停一秒再重新連線

                    downloadFile( webSite, outputDirectory, outputFileName,
                            needCookie, cookieString, referURL, fastMode, retryTimes - 1, gzipEncode, false );


                }

                if ( fileSize < 1024 && Flag.timeoutFlag ) {
                    new File( outputDirectory + outputFileName ).delete();
                    Common.debugPrintln( "刪除不完整檔案：" + outputFileName );

                    ComicDownGUI.stateBar.setText( "下載逾時，跳過" + outputFileName );

                }

                timer.cancel(); // 連線結束同時也關掉計時器

                Flag.timeoutFlag = false; // 歸回初始值

                Common.debugPrintln( webSite + " downloads successful!" ); // for debug

            }
            catch ( Exception e ) {
                Common.hadleErrorMessage( e, "無法正確下載" + webSite );
            }

            CommonGUI.stateBarDetailMessage = null;
        }
    }

    public static void urlConnection( String urlString ) {
        //urlString = "http://www.coderanch.com/t/207232/sockets/java/httpURLConnection-content-length-always";

        try {
            URL url = new URL( urlString );
            URLConnection yc = url.openConnection();
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                    yc.getInputStream() ) );
            String inputLine;



            while ( (inputLine = in.readLine()) != null ) {
                System.out.println( inputLine );
            }
            in.close();

            Common.debugPrintln( "連線結束" );

        }
        catch ( Exception ex ) {
            Common.errorReport( "無法連線" );
        }

    }

    public static void simpleDownloadFile( String webSite,
            String outputDirectory, String outputFileName ) {
        simpleDownloadFile( webSite, outputDirectory, outputFileName, null, null );
    }

    public static void simpleDownloadFile( String webSite,
            String outputDirectory, String outputFileName, String referString ) {
        simpleDownloadFile( webSite, outputDirectory, outputFileName, null, referString );
    }

    public static void simpleDownloadFile( String webSite,
            String outputDirectory, String outputFileName, String cookieString, String referString ) {
        try {
            URL url = new URL( webSite );
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            
            connection.setRequestMethod( "GET" );
            connection.setDoOutput( true );
 
            connection.setRequestProperty( "If-None-Match", "634737568694022579-0-634737559529942483--0-2_20" );

            if ( referString != null && !"".equals( referString ) ) { // 設置refer
                connection.setRequestProperty( "Referer", referString );
            }
            
            if ( cookieString != null && !"".equals( cookieString ) ) { // 設置cookie
                connection.setRequestProperty( "Cookie", cookieString );
            }

            ComicDownGUI.stateBar.setText( webSite + " 連線中..." );

            //tryConnect( connection );

            int fileSize = connection.getContentLength() / 1000;

            if ( connection.getResponseCode() != 200 ) {
                //Common.debugPrintln( "第二次失敗，不再重試!" );
                Common.errorReport( "錯誤回傳碼(responseCode): "
                        + connection.getResponseCode() + " : " + webSite );
                return;
            }

            Common.checkDirectory( outputDirectory ); // 檢查有無目標資料夾，若無則新建一個　

            //OutputStream os = response.getOutputStream();
            OutputStream os = new FileOutputStream( outputDirectory + outputFileName );
            InputStream is = null;


            is = connection.getInputStream(); // 其他漫畫網

            Common.debugPrint( "(" + fileSize + " k) " );
            String fileSizeString = fileSize > 0 ? "" + fileSize : " ? ";



            byte[] r = new byte[1024];
            int len = 0;

            int fileGotSize = 0;
            while ( (len = is.read( r )) > 0 && (Run.isAlive) ) {
                // 快速模式下，檔案小於1mb且連線超時 -> 切斷連線
                if ( fileSize > 1024 || !Flag.timeoutFlag ) // 預防卡住的機制
                {
                    os.write( r, 0, len );
                }
                else {
                    break;
                }

                fileGotSize += (len / 1000);

                if ( Common.withGUI() ) {
                    int percent = 100;
                    String downloadText = "";
                    if ( fileSize > 0 ) {
                        percent = (fileGotSize * 100) / fileSize;
                        downloadText = fileSizeString + "Kb ( " + percent + "% ) ";
                    }
                    else {
                        downloadText = fileSizeString + " Kb ( " + fileGotSize + "Kb ) ";
                    }

                    ComicDownGUI.stateBar.setText( CommonGUI.stateBarMainMessage
                            + CommonGUI.stateBarDetailMessage
                            + " : " + downloadText );
                }
            }

            is.close();
            os.flush();
            os.close();

            if ( Common.withGUI() ) {
                ComicDownGUI.stateBar.setText( CommonGUI.stateBarMainMessage
                        + CommonGUI.stateBarDetailMessage
                        + " : " + fileSizeString + "Kb ( 100% ) " );
            }

            connection.disconnect();

            Flag.timeoutFlag = false; // 歸回初始值

            Common.debugPrintln( webSite + " downloads successful!" ); // for debug

        }
        catch ( MalformedURLException e ) {
            Common.hadleErrorMessage( e, "無法正確下載" + webSite );
        }
        catch ( IOException e ) {
            // TODO Auto-generated catch block
            Common.hadleErrorMessage( e, "無法正確下載" + webSite );
        }
    }

    // 從java.awt.Color[r=255,g=175,b=175]轉為Color
    public static Color getColor( String colorString ) {

        String[] tempStrings = colorString.split( "=|,|\\[|\\]" );

        int r = Integer.parseInt( tempStrings[2] );
        int g = Integer.parseInt( tempStrings[4] );
        int b = Integer.parseInt( tempStrings[6] );

        //Common.debugPrintln( "取得色碼(r, g, b): " + r + " " + g + " " + b );

        return new Color( r, g, b );
    }

    // 測試印出
    public static void print( String... string ) {
        for ( String s : string ) {
            Common.debugPrintln( s );
        }

        System.exit( 0 );
    }
    
    // 回傳文字檔案預設輸出格式的副檔名
    public static String getDefaultTextExtension() {
        if ( SetUp.getDefaultTextOutputFormat() == FileFormatEnum.HTML ) {
            return "html";
        }
        else {
            return "txt";
        }
        
    }
    
    // 將<>標籤都拿掉
    public static String replaceTag( String text ) {
        return text.replaceAll( "<[^<>]+>", "" ); // 將所有標籤去除
    }
    
    // 處理錯誤訊息的步驟
    public static void hadleErrorMessage( Exception ex, String tipString ) {
        tipString += "！\n\n";
        System.err.println( tipString );
        ex.printStackTrace();
        Common.outputErrorMessage( ex, tipString );
    }
    
    // 輸出錯誤訊息到檔案
    public static void outputErrorMessage( Exception ex, String tipString ) {
        String timeString = new Date().toString(); // 取得當前時間的字串
        timeString = Common.getStringRemovedIllegalChar( timeString ); // 拿掉不合法字元
        String outputFileName = "error_report_" + timeString + ".txt";
        String outputPath = Common.getNowAbsolutePath() + "ErrorRecord" + Common.getSlash();
        String outputMessage = "錯誤提示：\n" + tipString + 
                                                  "錯誤原因：\n" + ex.getMessage() + "\n\n" + 
                                                  "錯誤發生地點：\n";
        
        outputMessage = Common.getStringUsingDefaultLanguage( outputMessage );
        
        StackTraceElement[] stack = ex.getStackTrace();
        for ( int i = 0; i < stack.length; i ++ ) {
            outputMessage += stack[i].toString() + "\n";
        }
        
        Common.outputFile( outputMessage, outputPath, outputFileName  );
    }
}

class TimeoutTask extends TimerTask {

    public void run() {
        Common.debugPrintln( "超過下載時限，終止此次連線!" );
        Flag.timeoutFlag = true;
    }
}
