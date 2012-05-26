/*
 ----------------------------------------------------------------------------------------------------
 Program Name : JComicDownloader
 Authors  : surveyorK
 Last Modified : 2012/5/13
 ----------------------------------------------------------------------------------------------------
 ChangeLog:
 *  4.0: 1. 新增對imanhua的支援。
 ----------------------------------------------------------------------------------------------------
 */
package jcomicdownloader.module;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import jcomicdownloader.tools.*;
import jcomicdownloader.enums.*;
import java.util.*;
import jcomicdownloader.SetUp;
import jcomicdownloader.encode.Zhcode;

public class ParseImanhua extends ParseOnlineComicSite {

    private int radixNumber; // use to figure out the name of pic
    private String jsName;
    protected String indexName;
    protected String indexEncodeName;
    protected String baseURL;

    /**

     @author user
     */
    public ParseImanhua() {
        siteID = Site.IMANHUA;
        indexName = Common.getStoredFileName( SetUp.getTempDirectory(), "index_imanhua_parse_", "html" );
        indexEncodeName = Common.getStoredFileName( SetUp.getTempDirectory(), "index_imanhua_encode_parse_", "html" );

        jsName = "index_imanhua.js";
        radixNumber = 1591371; // default value, not always be useful!!

        baseURL = "http://www.imanhua.com";
    }

    public ParseImanhua( String webSite, String titleName ) {
        this();
        this.webSite = webSite;
        this.title = titleName;
    }

    @Override
    public void setParameters() {
        Common.debugPrintln( "開始解析各參數 :" );
        Common.debugPrintln( "開始解析title和wholeTitle :" );

        if ( getWholeTitle() == null || getWholeTitle().equals( "" ) ) {
            // 因為正常解析不需要用到單集頁面，所以給此兩行放進來
            Common.downloadFile( webSite, SetUp.getTempDirectory(), indexName, false, "" );
            Common.newEncodeFile( SetUp.getTempDirectory(), indexName, indexEncodeName );

            String allPageString = Common.getFileString( SetUp.getTempDirectory(), indexEncodeName );

            int beginIndex = allPageString.indexOf( "<h1" );
            beginIndex = allPageString.indexOf( ">", beginIndex ) + 1;
            int endIndex = allPageString.indexOf( "<", beginIndex );
            String tempTitleString = allPageString.substring( beginIndex, endIndex ).replaceAll( "&nbsp;", "" );

            setWholeTitle( getVolumeWithFormatNumber( Common.getStringRemovedIllegalChar(
                Common.getTraditionalChinese( tempTitleString.trim() ) ) ) );
        }

        Common.debugPrintln( "作品名稱(title) : " + getTitle() );
        Common.debugPrintln( "章節名稱(wholeTitle) : " + getWholeTitle() );
    }

    @Override
    public void parseComicURL() { // parse URL and save all URLs in comicURL  //
        // 先取得前面的下載伺服器網址

        int beginIndex = 0;
        int endIndex = 0;
        
        Common.downloadFile( webSite, SetUp.getTempDirectory(), indexName, false, "" );
        Common.newEncodeFile( SetUp.getTempDirectory(), indexName, indexEncodeName );
        String allPageString = Common.getFileString( SetUp.getTempDirectory(), indexEncodeName );


        Common.debugPrint( "開始解析這一集有幾頁 : " );

        beginIndex = allPageString.indexOf( "[\"", beginIndex ) + 2;

        String[] picNames; // 存放此集所有圖片檔名
        if ( allPageString.indexOf( "|", beginIndex ) > 0 ) {
            // 第一種格式，副檔名與檔名分開
            // ex. http://www.imanhua.com/comic/1034/list_33255.html

            // 先取得副檔名
            endIndex = allPageString.indexOf( "|", beginIndex );
            beginIndex = allPageString.lastIndexOf( "'", endIndex ) + 1;
            String extention = allPageString.substring( beginIndex, endIndex );
            
            // ex. http://www.imanhua.com/comic/69/list_5701.html
            if ( !extention.matches( "[a-zA-Z]+" ) ) {
                if ( allPageString.indexOf( "jpg" ) > 0 ) {
                    extention = "jpg";
                }
                else {
                    extention = "png";
                }
            }
            
            Common.debugPrintln( "副檔名：" + extention );

            // 再取得集數中所有圖片檔名
            beginIndex = allPageString.indexOf( "|", beginIndex ) + 1;
            endIndex = allPageString.indexOf( "'", beginIndex );
            String tempString = allPageString.substring( beginIndex, endIndex );
            String[] tempPicNames = tempString.split( "\\|" );

            tempString = "";
            for ( int i = 0; i < tempPicNames.length && Run.isAlive; i++ ) {
                if ( tempPicNames[i].matches( "\\w+_.*" )
                    || tempPicNames[i].matches( "\\d+" )
                    || tempPicNames[i].matches( "\\d+\\w+" ) ) {
                    tempString += tempPicNames[i] + "." + extention + "|";
                }
            }

            picNames = tempString.split( "\\|" );
            Arrays.sort( picNames ); // 作排序
        }
        else {
            // 第二種格式，副檔名與檔名合在一起
            // ex. http://www.imanhua.com/comic/432/list_59406.html
            endIndex = allPageString.indexOf( "\"]", beginIndex );
            String tempString = allPageString.substring( beginIndex, endIndex );
            String[] tempPicNames = tempString.split( "\",\"" );

            tempString = "";
            for ( int i = 0; i < tempPicNames.length; i++ ) {
                if ( tempPicNames[i].matches( "imanhua_.*" )
                    || tempPicNames[i].matches( "\\d+\\.\\w+" )
                    || tempPicNames[i].matches( "\\d+\\w+\\.\\w+" ) 
                    || tempPicNames[i].matches( ".*/.*.\\w+" ) ) {
                    tempString += tempPicNames[i] + "|";
                }
            }

            picNames = tempString.split( "\\|" );
            Arrays.sort( picNames ); // 作排序
        }

        Common.debugPrintln( "開始解析中間部份的位址" );

        String midURL = "";
        // ex.中間網址是pictures，而非Files/Images/
        if ( allPageString.indexOf( "|pictures|" ) > 0 ) {
            midURL = "pictures/";
        }
        else {
            midURL = "Files/Images/";
        }
        
        // 先解析第一個數字 
        beginIndex = webSite.indexOf( "comic/" );
        beginIndex = webSite.indexOf( "/", beginIndex ) + 1;
        endIndex = webSite.indexOf( "/", beginIndex );
        String firstNumber = webSite.substring( beginIndex, endIndex );

        // 再解析第二個數字
        beginIndex = webSite.indexOf( "list_" );
        beginIndex = webSite.indexOf( "_", beginIndex ) + 1;
        endIndex = webSite.indexOf( ".", beginIndex );
        String secondNumber = webSite.substring( beginIndex, endIndex );

        // 圖片基本位址
        String baseURL1 = "http://t4.imanhua.com/";
        String baseURL2 = "http://t5.imanhua.com/";
        String baseURL3 = "http://t6.imanhua.com/";

        totalPage = picNames.length;
        Common.debugPrintln( "共 " + totalPage + " 頁" );
        comicURL = new String[totalPage];

        int p = 0; // 目前頁數
        for ( int i = 0; i < totalPage && Run.isAlive; i++ ) {
            if ( picNames[i].matches( ".*/.*" ) ) { // 檔名已包含後方位址
                // ex. http://www.imanhua.com/comic/69/list_5707.html
                comicURL[i] = baseURL1 + picNames[i];
            }
            else {
                comicURL[i] = baseURL1 + midURL
                    + firstNumber + "/" + secondNumber + "/" + picNames[i];
            }

            // 使用最簡下載協定，加入refer始可下載
            singlePageDownloadUsingSimple( getTitle(), getWholeTitle(),
                comicURL[i], totalPage, i + 1, comicURL[i] );

            //Common.debugPrintln( ( ++ p ) + " " + comicURL[p - 1] ); // debug
        }
        //System.exit( 0 ); // debug
    }

    public void showParameters() { // for debug
        Common.debugPrintln( "----------" );
        Common.debugPrintln( "totalPage = " + totalPage );
        Common.debugPrintln( "webSite = " + webSite );
        Common.debugPrintln( "----------" );
    }

    @Override
    public String getAllPageString( String urlString ) {
        String indexName = Common.getStoredFileName( SetUp.getTempDirectory(), "index_imanhua_", "html" );
        String indexEncodeName = Common.getStoredFileName( SetUp.getTempDirectory(), "index_imanhua_encode_", "html" );

        Common.downloadFile( urlString, SetUp.getTempDirectory(), indexName, false, "" );
        Common.newEncodeFile( SetUp.getTempDirectory(), indexName, indexEncodeName, Zhcode.GB2312 );

        return Common.getFileString( SetUp.getTempDirectory(), indexEncodeName );
    }

    @Override
    public boolean isSingleVolumePage( String urlString ) {
        // ex. http://www.imanhua.com/comic/1034/list_33204.html
        if ( urlString.matches( "(?).*/list_(?).*" ) ) {
            return true;
        }
        else {
            return false;
        }
    }

    public String getMainUrlFromSingleVolumeUrl( String volumeURL ) {
        // ex. http://www.imanhua.com/comic/1034/list_33204.html轉為
        //    http://www.imanhua.com/comic/1034/

        int endIndex = Common.getIndexOfOrderKeyword( volumeURL, "/", 5 );
        String mainPageURL = volumeURL.substring( 0, endIndex );

        Common.debugPrintln( "MAIN_URL: " + mainPageURL );

        return mainPageURL;
    }

    @Override
    public String getTitleOnSingleVolumePage( String urlString ) {
        String mainUrlString = getMainUrlFromSingleVolumeUrl( urlString );

        return getTitleOnMainPage( mainUrlString, getAllPageString( mainUrlString ) );
    }

    @Override
    public String getTitleOnMainPage( String urlString, String allPageString ) {
        int beginIndex = allPageString.indexOf( "<h1>" );
        beginIndex = allPageString.indexOf( ">", beginIndex ) + 1;
        int endIndex = allPageString.indexOf( "</h1>", beginIndex );
        String title = allPageString.substring( beginIndex, endIndex ).trim();

        return Common.getStringRemovedIllegalChar( Common.getTraditionalChinese( title ) );
    }

    @Override
    public List<List<String>> getVolumeTitleAndUrlOnMainPage( String urlString, String allPageString ) {
        // combine volumeList and urlList into combinationList, return it.

        List<List<String>> combinationList = new ArrayList<List<String>>();
        List<String> urlList = new ArrayList<String>();
        List<String> volumeList = new ArrayList<String>();

        int beginIndex = allPageString.indexOf( "id='subBookList'" );
        int endIndex = allPageString.indexOf( "</ul>", beginIndex );

        String tempString = allPageString.substring( beginIndex, endIndex );

        int volumeCount = tempString.split( " href=" ).length - 1;
        totalVolume = volumeCount;
        Common.debugPrintln( "共有" + totalVolume + "集" );

        String volumeTitle = "";
        beginIndex = endIndex = 0;
        for ( int i = 0; i < volumeCount; i++ ) {
            // 取得單集位址
            beginIndex = tempString.indexOf( " href=", beginIndex );
            beginIndex = tempString.indexOf( "\"", beginIndex ) + 1;
            endIndex = tempString.indexOf( "\"", beginIndex );
            urlList.add( baseURL + tempString.substring( beginIndex, endIndex ) );
            // 取得單集名稱
            beginIndex = tempString.indexOf( ">", beginIndex ) + 1;
            endIndex = tempString.indexOf( "</a>", beginIndex );
            volumeTitle = tempString.substring( beginIndex, endIndex );
            volumeTitle = volumeTitle.replaceAll( "<.*>", "" );
            volumeList.add( getVolumeWithFormatNumber( Common.getStringRemovedIllegalChar(
                Common.getTraditionalChinese( volumeTitle.trim() ) ) ) );
        }

        combinationList.add( volumeList );
        combinationList.add( urlList );

        return combinationList;
    }

    @Override
    public void outputVolumeAndUrlList( List<String> volumeList, List<String> urlList ) {
        Common.outputFile( volumeList, SetUp.getTempDirectory(), Common.tempVolumeFileName );
        Common.outputFile( urlList, SetUp.getTempDirectory(), Common.tempUrlFileName );
    }

    @Override
    public String[] getTempFileNames() {
        return new String[]{indexName, indexEncodeName, jsName};
    }

    @Override
    public void printLogo() {
        System.out.println( " ______________________________" );
        System.out.println( "|                            " );
        System.out.println( "| Run the imanhua module:     " );
        System.out.println( "|_______________________________\n" );
    }
}
