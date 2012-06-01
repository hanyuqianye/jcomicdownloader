/*
----------------------------------------------------------------------------------------------------
Program Name : JComicDownloader
Authors  : surveyorK
Last Modified : 2012/6/1
----------------------------------------------------------------------------------------------------
ChangeLog:
 *  4.06: 1. 新增對17kkmh.的支援。
----------------------------------------------------------------------------------------------------
 */
package jcomicdownloader.module;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import jcomicdownloader.tools.*;
import jcomicdownloader.enums.*;
import java.util.*;
import jcomicdownloader.SetUp;
import jcomicdownloader.encode.Encoding;
import jcomicdownloader.encode.Zhcode;

public class ParseKKMH extends ParseOnlineComicSite {

    private int radixNumber; // use to figure out the name of pic
    private String jsName;
    protected String indexName;
    protected String indexEncodeName;
    protected String baseURL;

    /**
    
    @author user
     */
    public ParseKKMH() {
        siteID = Site.KKMH;
        indexName = Common.getStoredFileName( SetUp.getTempDirectory(), "index_kkmh_parse_", "html" );
        indexEncodeName = Common.getStoredFileName( SetUp.getTempDirectory(), "index_kkmh_encode_parse_", "html" );

        jsName = "index_kkmh.js";
        radixNumber = 11371; // default value, not always be useful!!

        baseURL = "http://www.17kkmh.com";
    }

    public ParseKKMH( String webSite, String titleName ) {
        this();
        this.webSite = webSite;
        this.title = titleName;
    }

    @Override
    public void setParameters() {
        Common.debugPrintln( "開始解析各參數 :" );
        Common.debugPrintln( "開始解析title和wholeTitle :" );

        Common.downloadFile( webSite, SetUp.getTempDirectory(), indexName, false, "" );
        Common.newEncodeFile( SetUp.getTempDirectory(), indexName, indexEncodeName, Encoding.GB2312 );

        if ( getWholeTitle() == null || getWholeTitle().equals( "" ) ) {
            // 因為正常解析不需要用到單集頁面，所以給此兩行放進來

            String allPageString = Common.getFileString( SetUp.getTempDirectory(), indexEncodeName );

            int beginIndex = allPageString.indexOf( " jiename" );
            beginIndex = allPageString.indexOf( "\"", beginIndex ) + 1;
            int endIndex = allPageString.indexOf( "\"", beginIndex );
            String tempTitleString = allPageString.substring( beginIndex, endIndex );

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
        String tempString = "";

        String allPageString = Common.getFileString( SetUp.getTempDirectory(), indexEncodeName );

        Common.debugPrint( "開始解析這一集有幾頁 : " );

        beginIndex = allPageString.indexOf( " imgallpage" );
        beginIndex = allPageString.indexOf( "\"", beginIndex ) + 1;
        endIndex = allPageString.indexOf( "\"", beginIndex );
        tempString = allPageString.substring( beginIndex, endIndex );

        // 看有幾張圖片
        totalPage = Integer.parseInt( tempString );
        Common.debugPrintln( "共 " + totalPage + " 頁" );
        comicURL = new String[totalPage];

        // 取得jieid
        beginIndex = allPageString.indexOf( " jieid" );
        beginIndex = allPageString.indexOf( "\"", beginIndex ) + 1;
        endIndex = allPageString.indexOf( "\"", beginIndex );
        String jieid = allPageString.substring( beginIndex, endIndex );

        String basePicURL = baseURL + "/intro_view/" + jieid + "/";

        beginIndex = endIndex = 0;
        String picURL = "";
        String nexthtm = ""; // 下一頁網頁名稱
        String nextPageURL = ""; // 下一頁網頁位址
        for ( int p = 0 ; p < totalPage && Run.isAlive ; p++ ) {
            beginIndex = allPageString.indexOf( "id=\"viewimg\"" );
            beginIndex = allPageString.indexOf( "src=", beginIndex );
            beginIndex = allPageString.indexOf( "=", beginIndex ) + 1;
            endIndex = allPageString.indexOf( ">", beginIndex );
            picURL = allPageString.substring( beginIndex, endIndex );
            picURL = replacePicURL( picURL );
            comicURL[p] = picURL;

            // 每解析一張圖片就下載一張
            singlePageDownload( getTitle(), getWholeTitle(), comicURL[p], totalPage, p + 1, 0 );

            // 解析下一頁網頁位址
            beginIndex = allPageString.indexOf( " backhtm" );
            if ( beginIndex > 0 ) {
                beginIndex = allPageString.indexOf( "\"", beginIndex ) + 1;
                endIndex = allPageString.indexOf( "\"", beginIndex );
                nexthtm = allPageString.substring( beginIndex, endIndex );
                nextPageURL = basePicURL + nexthtm;
                Common.debugPrintln( "下一頁網頁位址：" + nextPageURL ); // debug

                allPageString = getAllPageString( nextPageURL );
            }

            
        }
        //System.exit( 0 ); // debug
    }

    // 以正確的伺服器位址取代原本錯誤的位址
    public String replacePicURL( String picURL ) {
        picURL = picURL.replace( "www.17kk.net", "image.17kkmh.com/image1.17kk" );
        picURL = picURL.replace( "image.17kk.net", "image.17kkmh.com/image1.17kk" );
        picURL = picURL.replace( "image3.17kk.net", "image.17kkmh.com/image3.17kk" );

        picURL = picURL.replace( "image4.17kk.net", "image.17kkmh.com/image6.17kk" );
        picURL = picURL.replace( "image6.17kk.net", "image.17kkmh.com/image6.17kk" );
        picURL = picURL.replace( "image0.17kk.net", "image.17kkmh.com/image0.17kk" );
        picURL = picURL.replace( "image2.17kk.net", "image.17kkmh.com/image2.17kk" );

        picURL = picURL.replace( "image7.17kk.net", "image.17kkmh.com/image7.17kk" );
        picURL = picURL.replace( "comiclist.17kk.net", "image.17kkmh.com" );

        return picURL;
    }

    public void showParameters() { // for debug
        Common.debugPrintln( "----------" );
        Common.debugPrintln( "totalPage = " + totalPage );
        Common.debugPrintln( "webSite = " + webSite );
        Common.debugPrintln( "----------" );
    }

    @Override
    public String getAllPageString( String urlString ) {
        String indexName = Common.getStoredFileName( SetUp.getTempDirectory(), "index_kkmh_", "html" );
        String indexEncodeName = Common.getStoredFileName( SetUp.getTempDirectory(), "index_kkmh_encode_", "html" );

        Common.downloadFile( urlString, SetUp.getTempDirectory(), indexName, false, "" );
        Common.newEncodeFile( SetUp.getTempDirectory(), indexName, indexEncodeName, Encoding.GB2312 );

        return Common.getFileString( SetUp.getTempDirectory(), indexEncodeName );
    }

    @Override
    public boolean isSingleVolumePage( String urlString ) {
        // ex. http://www.17kkmh.com/intro_view/40989/1_3e99.htm
        if ( urlString.matches( "(?s).*intro_view(?s).*") ||  urlString.matches( "(?s).*viewRedirect.aspx(?s).*") ) {
            return true;
        }
        else {
            return false;
        }
    }

    public String getMainUrlFromSingleVolumeUrl( String volumeURL ) {
        // ex. http://www.17kkmh.com/intro_view/40989/1_3e99.htm轉為
        //    http://www.17kkmh.com/intro/18066.htm

        String allPageString = getAllPageString( volumeURL );

        int beginIndex = allPageString.indexOf( " bookid" );
        beginIndex = allPageString.indexOf( "\"", beginIndex ) + 1;
        int endIndex = allPageString.indexOf( "\"", beginIndex );
        String mainPageURL = baseURL + "/intro/" + allPageString.substring( beginIndex, endIndex ) + ".htm";

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
        int beginIndex = allPageString.indexOf( " bookname" );
        beginIndex = allPageString.indexOf( "\"", beginIndex ) + 1;
        int endIndex = allPageString.indexOf( "\"", beginIndex );
        String title = allPageString.substring( beginIndex, endIndex ).trim();
        
        if ( title.indexOf( "/" ) > 0 ) {
            title = title.substring( 0, title.indexOf( "/" ) );
        }

        return Common.getStringRemovedIllegalChar( Common.getTraditionalChinese( title ) );
    }

    @Override
    public List<List<String>> getVolumeTitleAndUrlOnMainPage( String urlString, String allPageString ) {
        // combine volumeList and urlList into combinationList, return it.

        List<List<String>> combinationList = new ArrayList<List<String>>();
        List<String> urlList = new ArrayList<String>();
        List<String> volumeList = new ArrayList<String>();

        int beginIndex = allPageString.indexOf( "<div id=\"listread\">" );
        int endIndex = allPageString.indexOf( "</div>", beginIndex );

        String tempString = allPageString.substring( beginIndex, endIndex );

        int volumeCount = tempString.split( " href=" ).length - 1;
        totalVolume = volumeCount;
        Common.debugPrintln( "共有" + totalVolume + "集" );

        String volumeURL = "";
        String volumeTitle = "";
        beginIndex = endIndex = 0;
        for ( int i = 0 ; i < volumeCount ; i++ ) {
            // 取得單集位址
            beginIndex = tempString.indexOf( " href=", beginIndex );
            beginIndex = tempString.indexOf( "\"", beginIndex ) + 1;
            endIndex = tempString.indexOf( "\"", beginIndex );
            volumeURL = baseURL + tempString.substring( beginIndex, endIndex );
            urlList.add(  volumeURL );

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
        return new String[] { indexName, indexEncodeName, jsName };
    }

    @Override
    public void printLogo() {
        System.out.println( " ______________________________" );
        System.out.println( "|                            " );
        System.out.println( "| Run the 17KKMH module:     " );
        System.out.println( "|_______________________________\n" );
    }
}
