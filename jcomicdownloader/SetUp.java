/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jcomicdownloader;

import jcomicdownloader.tools.*;

import java.io.*;

/**
 * 預設值的設置，讀寫設定檔
 * */
public class SetUp { // read setup file, and then setup
    private static String setFileName;
    private static String downloadDirectory;
    private static String tempDirectory;
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

        proxyServer = ""; // 預設沒有掛上代理伺服器
        proxyPort = "";

        addSchedule = false;
        isError = false;

        beginVolume = 1;
        endVolume = 1;
        
        Common.closeHttpProxy(); // 預設為關閉代理伺服器。
    }

    // 將目前的設定寫入到設定檔(set.ini)
    public static void writeSetFile() {
        Common.debugPrintln( "寫入新的設定檔" );
        String setString;
        setString = "\n# 下載位置" +
                    "\ndownloadDirectory = " + originalDownloadDirectory +
                    "\n# 暫存檔位置" +
                    "\ntempDirectory = " + tempDirectory +
                    "\n# 下載完畢後自動產生壓縮檔" +
                    "\nautoCompress = " + autoCompress +
                    "\n# 自動刪除原始圖檔？" +
                    "\ndeleteOriginalPic = " + deleteOriginalPic +
                    "\n# 檔名的基本長度（不足補零）" +
                    "\nfileNameLength = " + fileNameLength +
                    "\n# 版面名稱" +
                    "\nskinClassName = " + skinClassName +
                    "\n# 是否輸出圖片位址？（輸出資料夾與下載位置相同）" +
                    "\noutputUrlFile = " + outputUrlFile +
                    "\n# 是否下載圖片？" +
                    "\ndownloadPicFile = " + downloadPicFile +
                    "\n# 是否一併開啟除錯訊息視窗？" +
                    "\nopenDebugMessageWindow = " + openDebugMessageWindow +
                    "\n# 縮小在工具列時是否顯示下載完成訊息？" +
                    "\nshowDoneMessageAtSystemTray = " + showDoneMessageAtSystemTray +
                    "\n# 是否保留未完成任務到下一次開啟？" +
                    "\nkeepUndoneDownloadMission = " + keepUndoneDownloadMission +
                    "\n# 是否保留已完成任務到下一次開啟？" +
                    "\nkeepDoneDownloadMission = " + keepDoneDownloadMission +
                    "\n# 代理伺服器位址（proxy sever address）" +
                    "\nproxyServer = " + proxyServer +
                    "\n# 代理伺服器連接阜（proxy server port）" +
                    "\nproxyPort = " + proxyPort +
                    "\n";

        Common.outputFile( setString, "", setFileName );
    }

    // 印出目前設定值，除錯用
    public void showSetUpParameter() {
        Common.debugPrintln( "-----------------------" );
        Common.debugPrintln( "downloadDirectory = " + Common.downloadDirectory  );
        Common.debugPrintln( "tempDirectory = " + Common.tempDirectory );
        Common.debugPrintln( "autoCompress = " + autoCompress );
        Common.debugPrintln( "deleteOriginalPic = " + deleteOriginalPic );
        Common.debugPrintln( "fileNameLength = " + fileNameLength );
        Common.debugPrintln( "lookAndFeel = " + skinClassName );
        Common.debugPrintln( "outputUrlFile = " + outputUrlFile );
        Common.debugPrintln( "downloadPicFile = " + downloadPicFile );
        Common.debugPrintln( "openDebugMessageWindow = " + openDebugMessageWindow );
        Common.debugPrintln( "showDoneMessageAtSystemTray = " + showDoneMessageAtSystemTray );
        Common.debugPrintln( "keepUndoneDownloadMission = " + keepUndoneDownloadMission );
        Common.debugPrintln( "keepDoneDownloadMission = " + keepDoneDownloadMission );
        Common.debugPrintln( "proxyServer = " + proxyServer );
        Common.debugPrintln( "proxyPort = " + proxyPort );
        Common.debugPrintln( "-----------------------" );
    }

    // 讀入設定檔並依讀入資料來更新目前設定
    public void readSetFile() {
        if ( !new File( setFileName ).exists() ) {
            Common.debugPrintln( "找不到set.ini，故自動產生" );
            writeSetFile();
        }

        String[] lines = Common.getFileStrings( "", setFileName );

        for ( int i = 0; i < lines.length; i ++ ) {
            try {
		if ( lines[i].length() > 2 && !lines[i].matches( "(?s).*#(?s).*" ) ) {
					String[] split = lines[i].split( "\\s*=\\s*" );

					if ( split[0].equals( "downloadDirectory" ) ) {
						String tempDir = "";
						if ( !split[1].matches( "(?s).*/\\d*" ) )
							tempDir = split[1] + Common.getSlash();
						else
							tempDir = split[1];

						Common.downloadDirectory = originalDownloadDirectory = downloadDirectory = Common.getAbsolutePath( split[1] ) + Common.getSlash();
					}
					else if ( split[0].equals( "tempDirectory" ) ) {

						Common.tempDirectory = tempDirectory = Common.getAbsolutePath( split[1] ) + Common.getSlash();
					}
					else if ( split[0].equals( "autoCompress" ) ) {
						autoCompress = ( new Boolean( split[1] ) ).booleanValue();
					}
					else if ( split[0].equals( "deleteOriginalPic" ) ) {
						deleteOriginalPic = ( new Boolean( split[1] ) ).booleanValue();
					}
					else if ( split[0].equals( "fileNameLength" ) ) {
						try {
							// 只接受1~9
						fileNameLength = Integer.parseInt( String.valueOf( split[1].charAt( 0 ) ) );
						} catch ( Exception ex ) {
							fileNameLength = 3; // 預設值
							ex.printStackTrace();
						}
					}
					else if ( split[0].equals( "skinClassName" ) ) {

						if ( CommonGUI.getSkinOrderBySkinClassName( split[1] ) != -1 )
							skinClassName = split[1];

						if ( CommonGUI.getGTKSkinOrder() != -1 ) // 若有gtk就優先選gtk版面
							skinClassName = "com.sun.java.swing.plaf.gtk.GTKLookAndFeel";

						ComicDownGUI.setDefaultSkinClassName( skinClassName );
					}
					else if ( split[0].equals( "outputUrlFile" ) ) {
						if ( new Boolean( split[1] ).booleanValue() )
							outputUrlFile = Boolean.valueOf( split[1] );
					}
					else if ( split[0].equals( "downloadPicFile" ) ) {
						//if ( new Boolean( split[1] ).booleanValue() )
							downloadPicFile = Boolean.valueOf( split[1] );
					}
					else if ( split[0].equals( "openDebugMessageWindow" ) ) {
						if ( new Boolean( split[1] ).booleanValue() )
							openDebugMessageWindow = Boolean.valueOf( split[1] );
					}
					else if ( split[0].equals( "showDoneMessageAtSystemTray" ) ) {
						if ( split[1].matches( "(?s).*true(?s).*" ) )
							setShowDoneMessageAtSystemTray( true );
						else
						    setShowDoneMessageAtSystemTray( false );
					}
					else if ( split[0].equals( "keepUndoneDownloadMission" ) ) {
						if ( split[1].matches( "(?s).*true(?s).*" ) )
							setKeepUndoneDownloadMission( true );
						else
						    setKeepUndoneDownloadMission( false );
					}
					else if ( split[0].equals( "keepDoneDownloadMission" ) ) {
						if ( split[1].matches( "(?s).*true(?s).*" ) )
							setKeepDoneDownloadMission( true );
						else
						    setKeepDoneDownloadMission( false );
					}
					else if ( split[0].equals( "proxyServer" ) ) {
                                                        if ( split.length > 1 )
                                                            setProxyServer( split[1] );
					}
					else if ( split[0].equals( "proxyPort" ) ) {
                                                          if ( split.length > 1 && split[1].matches( "\\s*\\d+\\s*" ) )
                                                              setProxyPort( split[1] );
                                                          else
                                                              setProxyServer( "" );
					}
				}
            }
            catch ( Exception ex ) {
                Common.debugPrintln( "讀取設定檔發生錯誤! 套用預設值!" );
                ex.printStackTrace();
                writeSetFile(); // 以目前設定值對設定檔進行覆寫
            }
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
