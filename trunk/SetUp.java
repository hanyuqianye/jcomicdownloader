/*
----------------------------------------------------------------------------------------------------
Program Name : JComicDownloader
Authors  : surveyorK
Last Modified : 2011/12/5
----------------------------------------------------------------------------------------------------
ChangeLog:
2.03: 增加下載失敗後重新嘗試次數(retryTimes)的選項
1.12: 修復tempDirectory最後出現兩個斜線的bug。
1.09: 加入是否保留書籤和記錄的選項
----------------------------------------------------------------------------------------------------
 */
package jcomicdownloader;

import java.awt.Font;
import jcomicdownloader.tools.*;

import java.io.*;

/**
 * 預設值的設置，讀寫設定檔
 * */
public class SetUp { // read setup file, and then setup

    private static String setFileName;
    private static String downloadDirectory;
    private static String tempDirectory;
    private static String recordFileDirectory; // 設定檔和記錄檔的資料夾
    private static String originalDownloadDirectory;
    private static String picFrontName;
    private static String volume; // which volume the comic page is
    private static String wholeTitle; // title name and volume or chapter
    private static int fileNameLength; // length of file name
    private static String skinClassName; // 預設版面的類別名稱
    private static boolean outputUrlFile; // 是否輸出位址檔案(*.txt)
    private static boolean downloadPicFile; // 是否下載圖片
    private static boolean openDebugMessageWindow; // 是否開啟除錯訊息視窗
    private static boolean showDoneMessageAtSystemTray; // 縮小在工具列時是否顯示下載完成訊息
    private static boolean keepUndoneDownloadMission; // 是否保留未完成任務到下一次開啟
    private static boolean keepDoneDownloadMission; // 是否保留已完成任務到下一次開啟
    private static boolean autoAddMission; // 是否複製網址後就自動解析並詢問欲下載集數
    private static boolean keepBookmark; // 是否保留書籤
    private static boolean keepRecord; // 是否保留記錄
    private static boolean choiceAllVolume; // 是否預設勾選全部集數
    private static String defaultFontName; // 使用者字型名稱（若沒設就直接用系統預設）
    private static int defaultFontSize; // 使用者字型大小（若沒設就直接用系統預設）
    private static String ehMemberID; // EH會員ID
    private static String ehMemberPasswordHash; // EH會員密碼Hash
    private static int timeoutTimer; // 逾時計時器的倒數時間
    private static int retryTimes; // 下載失敗重試次數
    private static String openPicFileProgram; // 預設開啟圖片檔的程式
    private static String openZipFileProgram; // 預設開啟壓縮檔的程式
    public static boolean assignDownloadPath;
    public static boolean autoCompress;
    public static boolean deleteOriginalPic;
    public static int beginVolume;
    public static int endVolume;
    private static String proxyServer; // Http Proxy Server
    private static String proxyPort; // Http Proxy Server Port
    public static boolean addSchedule;
    public static boolean isError;

    /**
     *
     * @author user
     */
    public SetUp() {

        // 預設值
        setFileName = "set.ini";
        downloadDirectory = new String( Common.downloadDirectory );
        originalDownloadDirectory = new String( Common.downloadDirectory );
        tempDirectory = new String( Common.tempDirectory );
        recordFileDirectory = new String( new File( "" ).getAbsolutePath() ) + Common.getSlash(); // 預設設定檔放在同個資料夾
        picFrontName = "";
        volume = "0";
        fileNameLength = 3;
        assignDownloadPath = false;
        autoCompress = true;
        deleteOriginalPic = false;
        skinClassName = "com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel";
        outputUrlFile = false;
        downloadPicFile = true;
        openDebugMessageWindow = false;

        showDoneMessageAtSystemTray = true;
        keepUndoneDownloadMission = true;
        keepDoneDownloadMission = false;
        autoAddMission = false; // 是否複製網址後就自動解析並詢問欲下載集數
        keepBookmark = true; // 是否保留書籤
        keepRecord = true; // 是否保留記錄
        choiceAllVolume = false; // 是否預設勾選全部集數
        retryTimes = 0;
        openPicFileProgram = ""; // 預設開啟圖片檔的程式
        openZipFileProgram = ""; // 預設開啟壓縮檔的程式

        proxyServer = ""; // 預設沒有掛上代理伺服器
        proxyPort = "";

        addSchedule = false;
        isError = false;

        beginVolume = 1;
        endVolume = 1;
        defaultFontName = new Font( null ).getName(); // 使用者字型名稱
        defaultFontSize = 18; // 使用者字型大小

        ehMemberID = "0";
        ehMemberPasswordHash = "NULL";

        timeoutTimer = 0;

        Common.closeHttpProxy(); // 預設為關閉代理伺服器。
    }

    // 將目前的設定寫入到設定檔(set.ini)
    public static void writeSetFile() {
        Common.debugPrintln( "寫入新的設定檔" );
        String setString;
        setString = "\n# 下載位置"
                + "\ndownloadDirectory = " + originalDownloadDirectory
                + "\n# 暫存檔位置"
                + "\ntempDirectory = " + tempDirectory
                + "\n# 紀錄檔位置"
                + "\nrecordFileDirectory = " + recordFileDirectory
                + "\n# 下載完畢後自動產生壓縮檔"
                + "\nautoCompress = " + autoCompress
                + "\n# 自動刪除原始圖檔？"
                + "\ndeleteOriginalPic = " + deleteOriginalPic
                + "\n# 檔名的基本長度（不足補零）"
                + "\nfileNameLength = " + fileNameLength
                + "\n# 版面名稱"
                + "\nskinClassName = " + skinClassName
                + "\n# 是否輸出圖片位址？（輸出資料夾與下載位置相同）"
                + "\noutputUrlFile = " + outputUrlFile
                + "\n# 是否下載圖片？"
                + "\ndownloadPicFile = " + downloadPicFile
                + "\n# 是否一併開啟除錯訊息視窗？"
                + "\nopenDebugMessageWindow = " + openDebugMessageWindow
                + "\n# 縮小在工具列時是否顯示下載完成訊息？"
                + "\nshowDoneMessageAtSystemTray = " + showDoneMessageAtSystemTray
                + "\n# 是否保留未完成任務到下一次開啟？"
                + "\nkeepUndoneDownloadMission = " + keepUndoneDownloadMission
                + "\n# 是否保留已完成任務到下一次開啟？"
                + "\nkeepDoneDownloadMission = " + keepDoneDownloadMission
                + "\n# 是否複製網址後就自動解析並詢問欲下載集數？"
                + "\nautoAddMission = " + autoAddMission
                + "\n# 是否保留書籤？"
                + "\nkeepBookmark = " + keepBookmark
                + "\n# 是否保留記錄？"
                + "\nkeepRecord = " + keepRecord
                + "\n# 是否預設勾選全部集數？"
                + "\nchoiceAllVolume = " + choiceAllVolume
                + "\n# 代理伺服器位址（proxy sever address）"
                + "\nproxyServer = " + proxyServer
                + "\n# 代理伺服器連接阜（proxy server port）"
                + "\nproxyPort = " + proxyPort
                + "\n# 預設字型名稱"
                + "\ndefaultFontName = " + defaultFontName
                + "\n# 預設字體大小"
                + "\ndefaultFontSize = " + defaultFontSize
                + "\n# EH會員ID"
                + "\nehMemberID = " + ehMemberID
                + "\n# EH會員密碼Hash"
                + "\nehMemberPasswordHash = " + ehMemberPasswordHash
                + "\n# 逾時計時器倒數秒數（0代表不限）"
                + "\ntimeoutTimer = " + timeoutTimer
                + "\n# 下載失敗重新嘗試下載的次數"
                + "\nretryTimes = " + retryTimes
                + "\n# 預設開啟圖片檔的程式"
                + "\nopenPicFileProgram = " + openPicFileProgram
                + "\n# 預設開啟壓縮檔的程式"
                + "\nopenZipFileProgram = " + openZipFileProgram
                + "\n";

        Common.outputFile( setString, Common.getNowAbsolutePath(), setFileName );
    }

    // 印出目前設定值，除錯用
    public void showSetUpParameter() {
        Common.debugPrintln( "-----------------------" );
        Common.debugPrintln( "downloadDirectory = " + originalDownloadDirectory );
        Common.debugPrintln( "tempDirectory = " + tempDirectory );
        Common.debugPrintln( "recordFileDirectory = " + recordFileDirectory );
        Common.debugPrintln( "autoCompress = " + autoCompress );
        Common.debugPrintln( "deleteOriginalPic = " + deleteOriginalPic );
        Common.debugPrintln( "fileNameLength = " + fileNameLength );
        Common.debugPrintln( "lookAndFeel = " + skinClassName );
        Common.debugPrintln( "outputUrlFile = " + outputUrlFile );
        Common.debugPrintln( "downloadPicFile = " + downloadPicFile );
        Common.debugPrintln( "openDebugMessageWindow = " + openDebugMessageWindow );
        Common.debugPrintln( "showDoneMessageAtSystemTray = " + showDoneMessageAtSystemTray );
        Common.debugPrintln( "keepUndoneDownloadMission = " + keepUndoneDownloadMission );
        Common.debugPrintln( "autoAddMission = " + autoAddMission );
        Common.debugPrintln( "keepBookmark = " + keepBookmark );
        Common.debugPrintln( "keepRecord = " + keepRecord );
        Common.debugPrintln( "choiceAllVolume = " + choiceAllVolume );
        Common.debugPrintln( "proxyServer = " + proxyServer );
        Common.debugPrintln( "proxyPort = " + proxyPort );
        Common.debugPrintln( "defaultFontName = " + defaultFontName );
        Common.debugPrintln( "defaultFontSize = " + defaultFontSize );
        Common.debugPrintln( "ehMemberID = " + ehMemberID );
        Common.debugPrintln( "ehMemberPasswordHash = " + ehMemberPasswordHash );
        Common.debugPrintln( "timeoutTimer = " + timeoutTimer );
        Common.debugPrintln( "retryTimes = " + retryTimes );
        Common.debugPrintln( "openPicFileProgram = " + openPicFileProgram );
        Common.debugPrintln( "openZipFileProgram = " + openZipFileProgram );
        Common.debugPrintln( "-----------------------" );
    }

    // 讀入設定檔並依讀入資料來更新目前設定
    public void readSetFile() {
        Common.debugPrintln( "SET路徑：" + Common.getNowAbsolutePath() + setFileName );
        if ( !new File( Common.getNowAbsolutePath() + setFileName ).exists() ) {
            Common.debugPrintln( "找不到set.ini，故自動產生" );
            writeSetFile();
        }

        String[] lines = Common.getFileStrings( getRecordFileDirectory(), setFileName );

        // 為了讓之前的版本也能產生新的set.ini所做的修改
        boolean existKeepBookmark = false;
        boolean existKeepRecord = false;
        boolean existAutoAddMission = false;
        boolean existProxyServer = false;
        boolean existProxyPort = false;
        boolean existDefaultFontName = false;
        boolean existDefaultFontSize = false;
        boolean existEhMemberID = false;
        boolean existEhMemberPasswordHash = false;
        boolean existSettingFileDirectory = false;
        boolean existTimeoutTimer = false;
        boolean existChoiceAllVolume = false;
        boolean existRetryTimes = false;
        boolean existOpenPicFileProgram = false;
        boolean existOpenZipFileProgram = false;

        for ( int i = 0 ; i < lines.length ; i++ ) {
            try {
                if ( lines[i].length() > 2 && !lines[i].matches( "(?s).*#(?s).*" ) ) {
                    String[] split = lines[i].split( "\\s*=\\s*" );

                    if ( split[0].equals( "downloadDirectory" ) ) {
                        String path = "";
                        // 如果最後已經有斜線，就不另外加斜線了
                        if ( Common.getAbsolutePath( split[1] ).matches( "(?s).*" + Common.getRegexSlash() ) ) {
                            path = Common.getAbsolutePath( split[1] );
                        } else {
                            path = Common.getAbsolutePath( split[1] ) + Common.getSlash();
                        }

                        originalDownloadDirectory = downloadDirectory = path;
                    } else if ( split[0].equals( "tempDirectory" ) ) {
                        String path = "";
                        // 如果最後已經有斜線，就不另外加斜線了 
                        if ( Common.getAbsolutePath( split[1] ).matches( "(?s).*" + Common.getRegexSlash() ) ) {
                            path = Common.getAbsolutePath( split[1] );
                        } else {
                            path = Common.getAbsolutePath( split[1] ) + Common.getSlash();
                        }

                        setTempDirectory( path );
                    } else if ( split[0].equals( "recordFileDirectory" ) ) {
                        existSettingFileDirectory = true;
                        String path = "";
                        // 如果最後已經有斜線，就不另外加斜線了 
                        if ( Common.getAbsolutePath( split[1] ).matches( "(?s).*" + Common.getRegexSlash() ) ) {
                            path = Common.getAbsolutePath( split[1] );
                        } else {
                            path = Common.getAbsolutePath( split[1] ) + Common.getSlash();
                        }

                        recordFileDirectory = path;
                    } else if ( split[0].equals( "autoCompress" ) ) {
                        autoCompress = (new Boolean( split[1] )).booleanValue();
                    } else if ( split[0].equals( "deleteOriginalPic" ) ) {
                        deleteOriginalPic = (new Boolean( split[1] )).booleanValue();
                    } else if ( split[0].equals( "fileNameLength" ) ) {
                        try {
                            // 只接受1~9
                            fileNameLength = Integer.parseInt( String.valueOf( split[1].charAt( 0 ) ) );
                        } catch ( Exception ex ) {
                            fileNameLength = 3; // 預設值
                            ex.printStackTrace();
                        }
                    } else if ( split[0].equals( "skinClassName" ) ) {

                        if ( CommonGUI.getSkinOrderBySkinClassName( split[1] ) != -1 ) {
                            skinClassName = split[1];
                        }

                        if ( CommonGUI.getGTKSkinOrder() != -1 ) // 若有gtk就優先選gtk版面
                        {
                            skinClassName = "com.sun.java.swing.plaf.gtk.GTKLookAndFeel";
                        }

                        ComicDownGUI.setDefaultSkinClassName( skinClassName );
                    } else if ( split[0].equals( "outputUrlFile" ) ) {
                        if ( new Boolean( split[1] ).booleanValue() ) {
                            outputUrlFile = Boolean.valueOf( split[1] );
                        }
                    } else if ( split[0].equals( "downloadPicFile" ) ) {
                        //if ( new Boolean( split[1] ).booleanValue() )
                        downloadPicFile = Boolean.valueOf( split[1] );
                    } else if ( split[0].equals( "openDebugMessageWindow" ) ) {
                        if ( new Boolean( split[1] ).booleanValue() ) {
                            openDebugMessageWindow = Boolean.valueOf( split[1] );
                        }
                    } else if ( split[0].equals( "showDoneMessageAtSystemTray" ) ) {
                        if ( split[1].matches( "(?s).*true(?s).*" ) ) {
                            setShowDoneMessageAtSystemTray( true );
                        } else {
                            setShowDoneMessageAtSystemTray( false );
                        }
                    } else if ( split[0].equals( "keepUndoneDownloadMission" ) ) {
                        if ( split[1].matches( "(?s).*true(?s).*" ) ) {
                            setKeepUndoneDownloadMission( true );
                        } else {
                            setKeepUndoneDownloadMission( false );
                        }
                    } else if ( split[0].equals( "keepDoneDownloadMission" ) ) {
                        if ( split[1].matches( "(?s).*true(?s).*" ) ) {
                            setKeepDoneDownloadMission( true );
                        } else {
                            setKeepDoneDownloadMission( false );
                        }
                    } else if ( split[0].equals( "proxyServer" ) ) {
                        existProxyServer = true;
                        if ( split.length > 1 ) {
                            setProxyServer( split[1] );
                        }
                    } else if ( split[0].equals( "proxyPort" ) ) {
                        existProxyPort = true;
                        if ( split.length > 1 && split[1].matches( "\\s*\\d+\\s*" ) ) {
                            setProxyPort( split[1] );
                        } else {
                            setProxyServer( "" );
                        }
                    } else if ( split[0].equals( "autoAddMission" ) ) {
                        existAutoAddMission = true;
                        if ( split[1].matches( "(?s).*true(?s).*" ) ) {
                            setAutoAddMission( true );
                        } else {
                            setAutoAddMission( false );
                        }
                    } else if ( split[0].equals( "keepBookmark" ) ) {
                        existKeepBookmark = true;
                        if ( split[1].matches( "(?s).*true(?s).*" ) ) {
                            setKeepBookmark( true );
                        } else {
                            setKeepBookmark( false );
                        }
                    } else if ( split[0].equals( "keepRecord" ) ) {
                        existKeepRecord = true;
                        if ( split[1].matches( "(?s).*true(?s).*" ) ) {
                            setKeepRecord( true );
                        } else {
                            setKeepRecord( false );
                        }
                    } else if ( split[0].equals( "choiceAllVolume" ) ) {
                        existChoiceAllVolume = true;
                        if ( split[1].matches( "(?s).*true(?s).*" ) ) {
                            setChoiceAllVolume( true );
                        } else {
                            setChoiceAllVolume( false );
                        }
                    } else if ( split[0].equals( "defaultFontName" ) ) {
                        existDefaultFontName = true;
                        setDefaultFontName( split[1] );
                    } else if ( split[0].equals( "defaultFontSize" ) ) {
                        existDefaultFontSize = true;
                        setDefaultFontSize( Integer.parseInt( split[1] ) );
                    } else if ( split[0].equals( "ehMemberID" ) ) {
                        existEhMemberID = true;
                        setEhMemberID( split[1] );
                    } else if ( split[0].equals( "ehMemberPasswordHash" ) ) {
                        existEhMemberPasswordHash = true;
                        setEhMemberPasswordHash( split[1] );
                    } else if ( split[0].equals( "timeoutTimer" ) ) {
                        existTimeoutTimer = true;
                        setTimeoutTimer( Integer.parseInt( split[1] ) );
                    } else if ( split[0].equals( "retryTimes" ) ) {
                        existRetryTimes = true;
                        setRetryTimes( Integer.parseInt( split[1] ) );
                    } else if ( split[0].equals( "openPicFileProgram" ) ) {
                        existOpenPicFileProgram = true;
                        if ( split.length > 1 ) {
                            setOpenPicFileProgram( split[1] );
                        }
                    } else if ( split[0].equals( "openZipFileProgram" ) ) {
                        existOpenZipFileProgram = true;
                        if ( split.length > 1 ) {
                            setOpenZipFileProgram( split[1] );
                        }
                    }
                }
            } catch ( Exception ex ) {
                Common.debugPrintln( "讀取設定檔發生錯誤! 套用預設值!" );
                ex.printStackTrace();
                writeSetFile(); // 以目前設定值對設定檔進行覆寫
            }
        }

        if ( existAutoAddMission
                && existKeepBookmark && existKeepRecord
                && existProxyServer && existProxyPort
                && existDefaultFontName && existDefaultFontSize
                && existEhMemberID && existEhMemberPasswordHash
                && existSettingFileDirectory && existTimeoutTimer
                && existChoiceAllVolume && existRetryTimes
                && existOpenPicFileProgram && existOpenZipFileProgram ) {
            Common.debugPrintln( "設定檔全部讀取完畢" );
        } else {
            Common.debugPrintln( "設定檔缺乏新版參數! 套用預設值!" );
            writeSetFile(); // 以目前設定值對設定檔進行覆寫
        }
        //showSetUpParameter(); // 顯示設定參數，除錯用
    }

    // 設置下載集數，GUI版用不到，目前已廢棄...
    public static void setDownloadVolume( String begin, String end ) {
        beginVolume = Integer.parseInt( begin );
        endVolume = Integer.parseInt( end );
    }

    // 設置新的起始路徑（預設是./down/）
    public static void setOriginalDownloadDirectory( String newOriginalDownloadDirectory ) {
        originalDownloadDirectory = newOriginalDownloadDirectory;
    }

    public static String getOriginalDownloadDirectory() {
        return originalDownloadDirectory;
    }

    // 設置新的圖片存放路徑
    //public static void setDownloadDirectory( String dir ) {
    //    downloadDirectory = originalDownloadDirectory;
    //    downloadDirectory += dir;
    //}
    public static String getDownloadDirectory() {
        return downloadDirectory;
    }

    public static String getRecordFileDirectory() {
        return recordFileDirectory;
    }

    public static void setRecordFileDirectory( String dir ) {
        recordFileDirectory = dir;
    }

    public static String getTempDirectory() {
        return tempDirectory;
    }

    public static void setTempDirectory( String dir ) {
        tempDirectory = dir;
    }

    // 設置版面
    public static void setSkinClassName( String newSkinClassName ) {
        skinClassName = newSkinClassName;
    }

    public static String getSkinClassName() {
        return skinClassName;
    }

    // 設置是否自動產生壓縮檔
    public static void setAutoCompress( boolean newAutoCompress ) {
        autoCompress = newAutoCompress;
    }

    public static boolean getAutoCompress() {
        return autoCompress;
    }

    // 設置是否自動刪除圖檔
    public static void setDeleteOriginalPic( boolean newDeleteOriginalPic ) {
        deleteOriginalPic = newDeleteOriginalPic;
    }

    public static boolean getDeleteOriginalPic() {
        return deleteOriginalPic;
    }

    // 設置是否自動輸出圖檔位址
    public static void setOutputUrlFile( boolean newOutputUrlFile ) {
        outputUrlFile = newOutputUrlFile;
    }

    public static boolean getOutputUrlFile() {
        return outputUrlFile;
    }

    // 是否下載圖片
    public static void setDownloadPicFile( boolean newDownloadPicFile ) {
        downloadPicFile = newDownloadPicFile;
    }

    public static boolean getDownloadPicFile() {
        return downloadPicFile;
    }

    // 設置是否一併開啟除錯訊息視窗
    public static void setOpenDebugMessageWindow( boolean newOpenDebugMessageWindow ) {
        openDebugMessageWindow = newOpenDebugMessageWindow;
    }

    public static boolean getOpenDebugMessageWindow() {
        return openDebugMessageWindow;
    }

    // 縮小在工具列時是否顯示下載完成訊息
    public static void setShowDoneMessageAtSystemTray( boolean newShowDoneMessageAtSystemTray ) {
        showDoneMessageAtSystemTray = newShowDoneMessageAtSystemTray;
    }

    public static boolean getShowDoneMessageAtSystemTray() {
        return showDoneMessageAtSystemTray;
    }

    // 是否保留未完成任務到下一次開啟
    public static void setKeepUndoneDownloadMission( boolean newKeepUndoneDownloadMission ) {
        keepUndoneDownloadMission = newKeepUndoneDownloadMission;
    }

    public static boolean getKeepUndoneDownloadMission() {
        return keepUndoneDownloadMission;
    }

    // 是否保留已完成任務到下一次開啟
    public static void setKeepDoneDownloadMission( boolean newKeepDoneDownloadMission ) {
        keepDoneDownloadMission = newKeepDoneDownloadMission;
    }

    public static boolean getKeepDoneDownloadMission() {
        return keepDoneDownloadMission;
    }

    // 是否自動加入任務（實驗功能）
    public static void setAutoAddMission( boolean newAutoAddMission ) {
        autoAddMission = newAutoAddMission;
    }

    public static boolean getAutoAddMission() {
        return autoAddMission;
    }

    // 是否儲存書籤
    public static void setKeepBookmark( boolean newKeepBookmark ) {
        keepBookmark = newKeepBookmark;
    }

    public static boolean getKeepBookmark() {
        return keepBookmark;
    }

    // 是否儲存記錄
    public static void setKeepRecord( boolean newKeepRecord ) {
        keepRecord = newKeepRecord;
    }

    public static boolean getKeepRecord() {
        return keepRecord;
    }

    // 是否預設勾選全部集數
    public static void setChoiceAllVolume( boolean newChoiceAllVolume ) {
        choiceAllVolume = newChoiceAllVolume;
    }

    public static boolean getChoiceAllVolume() {
        return choiceAllVolume;
    }

    // 設定代理伺服器
    public static String getProxyServer() {
        return proxyServer;
    }

    public static void setProxyServer( String server ) {
        proxyServer = server;
    }

    public static String getProxyPort() {
        return proxyPort;
    }

    public static void setProxyPort( String port ) {
        proxyPort = port;
    }

    public static String getDefaultFontName() {
        return defaultFontName;
    }

    public static void setDefaultFontName( String fontName ) {
        defaultFontName = fontName;
    }

    public static Font getDefaultFont() {
        return new Font( getDefaultFontName(), Font.PLAIN, getDefaultFontSize() );
    }

    public static Font getDefaultBoldFont() {
        return new Font( getDefaultFontName(), Font.BOLD, getDefaultFontSize() );
    }

    public static Font getDefaultFont( int offset ) { // 畢竟主界面按鈕名稱的字體要比較大......
        return new Font( getDefaultFontName(), Font.PLAIN, getDefaultFontSize() + offset );
    }

    public static int getDefaultFontSize() {
        return defaultFontSize;
    }

    public static void setDefaultFontSize( int fontSize ) {
        defaultFontSize = fontSize;
    }

    public static String getEhMemberID() {
        return ehMemberID;
    }

    public static void setEhMemberID( String id ) {
        ehMemberID = id;
    }

    public static String getEhMemberPasswordHash() {
        return ehMemberPasswordHash;
    }

    public static void setEhMemberPasswordHash( String hash ) {
        ehMemberPasswordHash = hash;
    }

    public static int getTimeoutTimer() {
        return timeoutTimer;
    }

    public static void setTimeoutTimer( int timer ) {
        timeoutTimer = timer;
    }

    public static int getRetryTimes() {
        return retryTimes;
    }

    public static void setRetryTimes( int times ) {
        retryTimes = times;
    }

    public static String getOpenPicFileProgram() {
        return openPicFileProgram;
    }

    public static void setOpenPicFileProgram( String program ) {
        openPicFileProgram = program;
    }

    public static String getOpenZipFileProgram() {
        return openZipFileProgram;
    }

    public static void setOpenZipFileProgram( String program ) {
        openZipFileProgram = program;
    }

    // ----------------------------------------------------------------
    public static void setPicFrontName( String front ) {
        picFrontName = front;
    }

    public static String getPicFrontName() {
        return picFrontName;
    }

    public static void setVolume( String v ) {
        volume = v;
    }

    public static void setWholeTitle( String t ) {
        wholeTitle = t;
    }

    public static String getVolume() {
        return volume;
    }

    public static String getWholeTitle() {
        return wholeTitle;
    }

    public static int getFileNameLength() {
        return fileNameLength;
    }
}
