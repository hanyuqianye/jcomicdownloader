/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jcomicdownloader.tools;

import java.util.logging.Level;
import java.util.logging.Logger;
import jcomicdownloader.encode.*;
import jcomicdownloader.module.*;
import jcomicdownloader.*;

import java.io.*;
import java.util.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.*;
import java.util.zip.*;
import javax.swing.JOptionPane;
import jcomicdownloader.enums.DownTableEnum;
 
public class Common {
    public static String tempDirectory = getNowAbsolutePath() + "temp" + getSlash();
    public static String downloadDirectory = getNowAbsolutePath() + "down" + getSlash();
    public static String tempVolumeFileName = "temp_volume.txt";
    public static String tempUrlFileName = "temp_url.txt";
    public static String tempVolumeInformationFileName = "temp_volume_information.txt";

    public static boolean isMainPage = false;
    public static int missionCount = 0;
    public static boolean downloadLock = false;
    public static Thread downloadThread;
    public static boolean urlIsUnknown = false;

    public static String prevClipString; // 用來檢查剪貼簿，若沒有變化就不要貼上輸入欄了
    public static String consoleThreadName = "Thread-console-version";
    public static String setFileName = "set.ini";
    public static int reconnectionTimes = 3; // 嘗試重新連線的最高次數

    public static String getZero() {
        int length = SetUp.getFileNameLength();

        String zero = "";
        for ( int i = 0; i < length; i ++ )
            zero += "0";

        return zero;
    }

    public static void errorReport( String errorString ) {
        System.out.println( errorString );
        Run.isLegal = false;
    }

    public static void debugPrintln( String print ) { // for debug
        if ( Debug.debugMode )
            System.out.println( print );
    }
    public static void debugPrint( String print ) { // for debug
        if ( Debug.debugMode )
            System.out.print( print );
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
        String nowDownloadTitle = pathStrings[pathStrings.length-2];
        String nowDownloadVolume = pathStrings[pathStrings.length-1];

        String mainMessage = "下載 " + nowDownloadTitle + " / " + nowDownloadVolume + " ";

        for ( int i = 1; i <= webSite.length && Run.isAlive; i ++ ) {
            // 察知此圖片的副檔名(因為會呼叫downloadManyFile的都是下載圖片)
            String[] tempStrings = webSite[i-1].split( "/|\\." );

            if ( tempStrings[tempStrings.length-1].length() == 3 ||  // ex. jgp, png
                tempStrings[tempStrings.length-1].length() == 4  ) // ex. jpeg
                extensionName = tempStrings[tempStrings.length-1];
            
            String fileName = picFrontName + formatter.format( i ) + "." + extensionName;
            String nextFileName = picFrontName + formatter.format( i + 1 ) + "." + extensionName;
            if ( webSite[i-1] != null ) {
                // if not all download, the last file needs to re-download
                if ( !new File( outputDirectory + nextFileName ).exists() ) {
                    CommonGUI.stateBarMainMessage = mainMessage;
                    CommonGUI.stateBarDetailMessage = "  :  " + "共" + webSite.length + "頁" +
                                                 "，第" + i + "頁下載中";

                    if ( Common.withGUI() )
                        ComicDownGUI.trayIcon.setToolTip( CommonGUI.stateBarMainMessage +
                                                      CommonGUI.stateBarDetailMessage );

                    CommonGUI.stateBarDetailMessage += " : " + fileName;
                   
                    downloadFile( webSite[i-1], outputDirectory, fileName, false, "" );

                }
                System.out.print( i + " " );
            }
        }
    }

    public static void slowDownloadFile( String webSite, String outputDirectory, String outputFileName, 
                                          int delayMillisecond, boolean needCookie, String cookieString ) {
        try {
            Thread.currentThread().sleep( delayMillisecond );
        } catch ( InterruptedException ex ) { ex.printStackTrace(); }
        
        downloadFile( webSite, outputDirectory, outputFileName, needCookie, cookieString );

    }

    public static void downloadFile( String webSite, String outputDirectory, String outputFileName, 
                                     boolean needCookie, String cookieString ) {
        // downlaod file by URL

        if ( CommonGUI.stateBarDetailMessage == null ) {
            CommonGUI.stateBarMainMessage = "下載網頁進行分析 : ";
            CommonGUI.stateBarDetailMessage = outputFileName + " ";
        }

        if ( Run.isAlive ) {
            try {

                URL url = new URL( webSite );

                HttpURLConnection connection =  (HttpURLConnection) url.openConnection();
                
                if ( needCookie ) {
                    connection.setRequestMethod("GET");
                    connection.setDoOutput(true);
                    connection.setRequestProperty("Cookie", cookieString );
                }    
                        
                int responseCode = 0;
                
                try {
                    connection.connect();
                } catch ( java.net.ConnectException ex ) {
                    if ( connection.getResponseCode() != 200 )
                        try {
                            Thread.sleep( 1000 ); // 每次暫停一秒再重新連線
                        } catch ( InterruptedException iex ) { }
                        Common.debugPrintln( "重新嘗試連線......" );
                        if ( Common.withGUI() ) {
                            ComicDownGUI.stateBar.setText( "重新嘗試連線......" );
                        connection.connect(); // 第二次嘗試連線
                    }
                    
                }
                    
                if ( connection.getResponseCode() != 200 ) {
                    Common.debugPrintln( "第二次失敗，不再重試!" );

                    return;
                }

                Common.checkDirectory( outputDirectory );

                //OutputStream os = response.getOutputStream();
                OutputStream os = new FileOutputStream( outputDirectory + outputFileName );
                InputStream is = connection.getInputStream();

                int fileSize = connection.getContentLength() / 1000;

                int fileGotSize = 0;
                Common.debugPrint( "(" + fileSize + " k) " );

                byte[] r = new byte[1024];
                int len = 0;
                while ( ( len = is.read( r ) ) > 0 && Run.isAlive ) {
                    os.write( r, 0, len );
                    fileGotSize += ( len / 1000 );

                    if ( Common.withGUI() ) {
                        int percent = 100;
                        if ( fileSize > 0 )
                            percent = ( fileGotSize * 100 ) / fileSize;
                        String downloadText = fileSize + "Kb ( " + percent + "% ) " ;

                        ComicDownGUI.stateBar.setText( CommonGUI.stateBarMainMessage +
                                                        CommonGUI.stateBarDetailMessage +
                                                       " : " + downloadText );

                    }
                }
                
                is.close();
                os.flush();
                os.close();

                if ( Common.withGUI() )
                    ComicDownGUI.stateBar.setText( CommonGUI.stateBarMainMessage +
                                                   CommonGUI.stateBarDetailMessage +
                                                   " : " + fileSize + "Kb ( 100% ) " );


                connection.disconnect();

                Common.debugPrintln( webSite + " downloads successful!" ); // for debug

            } catch ( Exception e ) { e.printStackTrace(); }

            CommonGUI.stateBarDetailMessage = null;
        }
    }

    public static boolean isLegalURL( String webSite ) {
        String regex = "^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
        if ( webSite.matches( regex ) )
            return true;
        else
            return false;

    }


    // -----------------------------------



    public static void compress( File source, File destination ) { //  compress to zip
        try {
            // Deflater.NO_COMPRESSION: 沒有壓縮，僅儲存
            compress( source, destination, null, Deflater.NO_COMPRESSION );
        } catch ( Exception e ) { e.printStackTrace(); }
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
            for ( int length; ( length = fis.read( buffer ) ) > 0; ) {
                zos.write( buffer, 0, length );
            }
            fis.close();
            zos.closeEntry();
        } else if ( source.isDirectory() ) {
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
        try {
            deleteAllFile(folderPath); // delete all the file in dir
            String filePath = folderPath;
            filePath = filePath.toString();
            File myFilePath = new File( filePath );
            myFilePath.delete(); // delete empty dir
        } catch ( Exception e ) { e.printStackTrace(); }
    }

    public static boolean deleteAllFile( String path ) {
        boolean flag = false;
        File file = new File( path );
        if ( !file.exists() )
            return flag;

        if ( !file.isDirectory() )
            return flag;

        String[] tempList = file.list();
        File temp = null;
        for ( int i = 0; i < tempList.length; i++ ) {
            if ( path.endsWith( File.separator ) )
                temp = new File(path + tempList[i]);
            else
                temp = new File( path + File.separator + tempList[i] );

            if ( temp.isFile() )
                temp.delete();
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
        } catch ( IOException e ){ e.printStackTrace(); }
    }

    public static void outputFile( String[] outputStrings, String filePath, String fileName ) {
        StringBuffer sb = new StringBuffer();
        for ( int i = 0; i < outputStrings.length; i ++ )
            sb.append( outputStrings[i] + "\n" );

        outputFile( sb.toString(), filePath, fileName );
    }

    public static void outputFile( List outputList, String filePath, String fileName ) {
        StringBuffer sb = new StringBuffer();
        for ( int i = 0; i < outputList.size(); i ++ )
            sb.append( outputList.get( i ) + "\n" );

        outputFile( sb.toString(), filePath, fileName );
    }

    public static void outputUrlFile( String[] urlStrings, String oldDownloadPath ) {
        String[] dirStrings = oldDownloadPath.split( "[\\\\]|/" );

        String urlFileName = dirStrings[dirStrings.length-1] + ".txt";
        String downloadPath = "";

        for ( int i = 0; i < dirStrings.length - 1; i ++ ) {
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

                InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "UTF8");

                int ch = 0;
                while((ch = inputStreamReader.read()) != -1) {
                    sb.append( (char) ch );
                }

            } catch ( IOException e ){ e.printStackTrace(); }
        }
        else
            Common.errorReport( "沒有找到" + filePath + fileName + "此一檔案" );

        return sb.toString();
    }

    public static String[] getFileStrings( String filePath, String fileName ) {
        String[] tempStrings = getFileString( filePath, fileName ).split( "\\n|\\r" );

        return tempStrings;
        //return correctStrings;
    }

    public static String GBK2Unicode(String str)
    {
          StringBuffer result = new StringBuffer();
          for (int i = 0; i < str.length(); i++)
          {
           char chr1 = str.charAt(i);
           if(!isNeedConvert(chr1))
           {
            result.append(chr1);
            continue;
           }
           result.append("&#x" + Integer.toHexString((int)chr1) + ";");
          }
          return result.toString();
    }

    public static boolean isNeedConvert(char para)
    {
        return ((para&(0x00FF))!=para);
    }

    public static String getTraditionalChinese( String gbString ) {
        // Simplified Chinese To Traditional Chinese
        Zhcode mycode = new Zhcode();

        //
        return mycode.convertString( gbString , mycode.GB2312, mycode.BIG5).replaceAll( "[\\\\]ufffd", "_" );
    }

    public static String getSimplifiedChinese( String gbString ) {
        Zhcode mycode = new Zhcode();
        return mycode.convertString( gbString, mycode.GB2312, mycode.BIG5);
    }

    public static String getUtf8toBig5( String utf8 ) {
        Zhcode mycode = new Zhcode();
        return mycode.convertString( utf8, mycode.UTF8, mycode.BIG5);
    }
    public static String getBig5toUtf8( String big5 ) {
        Zhcode mycode = new Zhcode();
        return mycode.convertString( big5, mycode.BIG5, mycode.UTF8);
    }
    public static String getGB2312toUtf8( String gb ) {
        Zhcode mycode = new Zhcode();
        return mycode.convertString( gb, mycode.BIG5, mycode.UTF8);
    }

    public static void newEncodeFile( String directory, String fileName, String encodeFileName ) {
        Zhcode mycode = new Zhcode();
        mycode.convertFile( directory + fileName,
                            directory + encodeFileName,
                            mycode.GB2312,
                            mycode.UTF8);
    }

    public static String getConnectStrings( String[] strings ) {
        String str = "";

        for ( int i = 0; i < strings.length; i ++ )
            str += strings[i] + "####";

        return str;
    }

    public static String[] getSeparateStrings( String connectString ) {
        return connectString.split( "####" );
    }

    public static int getTrueCountFromStrings( String[] strings ) {
        int count = 0;
        for ( String str : strings )
            if ( str.equals( "true" ) )
                count ++;
        return count;
    }

    public static String[] getCopiedStrings( String[] copiedStrings ) {
        String[] newStrings = new String[copiedStrings.length];

        for ( int i = 0; i < copiedStrings.length; i ++ )
            newStrings[i] = copiedStrings[i];

        return newStrings;
    }
    public static String getStringRemovedIllegalChar( String oldString ) {
       // "\/:*?"<>|"
        String newString = "";

        for ( int i = 0; i < oldString.length(); i ++ ) {
            if ( oldString.charAt( i ) == '\\' ||
                 oldString.charAt( i ) == '/' ||
                 oldString.charAt( i ) == ':' ||
                 oldString.charAt( i ) == '*' ||
                 oldString.charAt( i ) == '?' ||
                 oldString.charAt( i ) == '"' ||
                 oldString.charAt( i ) == '<' ||
                 oldString.charAt( i ) == '>' ||
                 oldString.charAt( i ) == '|' ) {

                newString += String.valueOf( '_' );
            }
            else
                newString += String.valueOf( oldString.charAt( i ) );
        }

        return newString;
    }

    public static boolean withGUI() { // check the running app is GUI version or console version
        if ( Thread.currentThread().getName().equals( consoleThreadName ) )
            return false;
        else
            return true;
    }

    public static String getStoredFileName( String outputDirectory,
                                            String defaultFileName,
                                            String defaultExtensionName ) {
        int indexNameNo = 0;
        boolean over = false;
        while ( over ) {
            File tempFile = new File( outputDirectory + defaultFileName +
                                      indexNameNo + "." + defaultExtensionName );
            if ( tempFile.exists() && ( !tempFile.canRead() || !tempFile.canWrite() ) )
                indexNameNo ++;
            else
                over = true;
        }

        return defaultFileName + indexNameNo + "." + defaultExtensionName;
    }

    public static String getAbsolutePath( String relativePath ) {
        return new File( relativePath ).getAbsolutePath();
    }

    public static boolean isWindows() { // windows
        String os = System.getProperty("os.name").toLowerCase();
        return (os.indexOf("win") >= 0);
    }

    public static boolean isMac() { // Mac
        String os = System.getProperty("os.name").toLowerCase();
        return (os.indexOf("mac") >= 0);
    }

    public static boolean isUnix() { // linux or unix
        String os = System.getProperty("os.name").toLowerCase();
        return (os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0);

    }

    public static String getSlash() {
        if ( Common.isWindows() )
            return "\\";
        else
            return "/";
    }

    public static String getNowAbsolutePath() {
        return getAbsolutePath( "" ) + getSlash();
    }

    // 取得string中第order個keyword的位置
    public static int getIndexOfOrderKeyword( String string, String keyword, int order ) {
        int index = 0;
        for ( int i = 0; i < order && index >= 0; i ++ ) {
            index ++;
            index = string.indexOf( keyword, index );
        }

        return index;
    }


    // 找出從baseIndex開始，keyword1和keyword2在string中的位置（index），並回傳較小的index
    public static int getSmallerIndexOfTwoKeyword( String string, int baseIndex, String keyword1, String keyword2 ) {
        int index1 = string.indexOf( keyword1, baseIndex );
        int index2 = string.indexOf( keyword2, baseIndex );

        return index1 < index2 ? index1 : index2;
    }

    // 寫出目前的下載任務清單
    public static void outputDownTableFile( DataTableModel downTableModel ) {
        StringBuffer sb = new StringBuffer();
        for ( int row = 0; row < Common.missionCount; row ++ ) {
            // 有勾選下載才會儲存！
            if ( downTableModel.getValueAt( row, DownTableEnum.YES_OR_NO ).toString().equals( "true" ) ) {
                if ( SetUp.getKeepUndoneDownloadMission() ) { // 保存未完成任務
                    if ( !downTableModel.getValueAt( row, DownTableEnum.STATE ).toString().equals( "下載完畢" ) ) {
                        for ( int col = 0; col < ComicDownGUI.getDefaultColumns().size(); col ++ ) {
                            sb.append( downTableModel.getRealValueAt( row, col ).toString() );
                            sb.append( "@@@@@@" );
                        }
                        sb.append( ComicDownGUI.downTableUrlStrings[row] );
                        sb.append( "%%%%%%" );
                    }
                }
                if ( SetUp.getKeepDoneDownloadMission() ) { // 保存已完成任務
                    if ( downTableModel.getValueAt( row, DownTableEnum.STATE ).toString().equals( "下載完畢" ) ) {
                        for ( int col = 0; col < ComicDownGUI.getDefaultColumns().size(); col ++ ) {
                            sb.append( downTableModel.getRealValueAt( row, col ).toString() );
                            sb.append( "@@@@@@" );
                        }
                        sb.append( ComicDownGUI.downTableUrlStrings[row] );
                        sb.append( "%%%%%%" );
                    }
                }
            }
        }

        sb.append( "_OVER_" );
        outputFile( sb.toString(), getNowAbsolutePath(), "downloadList.dat" );
    }

    // 讀入之前儲存的下載任務清單
    public static DataTableModel inputDownTableFile() {
        String dataString = getFileString( getNowAbsolutePath(), "downloadList.dat" );

        if ( !dataString.matches( "\\s*_OVER_\\s*" ) ) { // 之前有記錄下載清單
            String[] rowStrings = dataString.split( "%%%%%%" );
            Common.debugPrintln( "將讀入下載任務數量: " + rowStrings.length );
            DataTableModel downTableModel = new DataTableModel( ComicDownGUI.getDefaultColumns(),
                                                                rowStrings.length - 1 );

            for ( int row = 0; row < rowStrings.length - 1; row ++ ) {
                String[] colStrings = rowStrings[row].split( "@@@@@@" );

                for ( int col = 0; col < ComicDownGUI.getDefaultColumns().size(); col ++ ) {
                    if ( col == DownTableEnum.YES_OR_NO )
                        downTableModel.setValueAt( Boolean.valueOf( colStrings[col] ), row, col );
                    else if ( col == DownTableEnum.ORDER )
                        downTableModel.setValueAt( new Integer( row + 1 ), row, col );
                    else
                        downTableModel.setValueAt( colStrings[col], row, col );
                }
                ComicDownGUI.downTableUrlStrings[row] = colStrings[ComicDownGUI.getDefaultColumns().size()];
                Common.missionCount ++;

            }

            return downTableModel;
        }
        else
            return new DataTableModel( ComicDownGUI.getDefaultColumns(), 0 );

    }
    
    public static void deleteFile( String filePath, String fileName ) {
        File file = new File( filePath + fileName );
        
        if ( file.exists() && file.isFile() ) {
            Common.debugPrintln( "刪除暫存檔案：" + fileName );
            file.delete();
        }
       
    }
    
    public static void setHttpProxy( String proxyServer, String proxyPort ) {
        Properties systemProperties = System.getProperties(); 
        systemProperties.setProperty( "http.proxyHost", proxyServer ); 
        systemProperties.setProperty( "http.proxyPort", proxyPort ); 
    }
    
    public static void closeHttpProxy() {
       Properties systemProperties = System.getProperties(); 
       systemProperties.setProperty( "proxySet", "false" );
    }

}

