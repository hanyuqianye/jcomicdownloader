/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jcomicdownloader.module;

import jcomicdownloader.tools.*;
import jcomicdownloader.enums.*;
import jcomicdownloader.*;

import java.util.*;

public class ParseNINENINE extends ParseOnlineComicSite {
    private String indexName;
    private String indexEncodeName;
    private String jsName;

    private int serverNo; // 下載伺服器的編號
    private String baseURL; // 首頁網址 ex. http://dm.99manga.com
    private String jsURL; // 存放下載伺服器網址的.js檔的網址

    /**
 *
 * @author user
 */
    public ParseNINENINE() {
        siteID = Site.CC;
        indexName = Common.getStoredFileName( Common.tempDirectory, "index_99_parse_", "html" );
        indexEncodeName = Common.getStoredFileName( Common.tempDirectory, "index_99_encode_parse_", "html" );
        jsName = Common.getStoredFileName( Common.tempDirectory, "index_99_parse_", "js" );
    }

    public ParseNINENINE( String webSite, String titleName ) {
        this();
        this.webSite = webSite;
        this.title = titleName;
    }

    public void setURL( String url ) {
        this.webSite = url;
    }
    public void setTitle( String title ) {
        this.title = title;
    }
    public void setWholeTitle( String wholeTitle ) {
        this.wholeTitle = wholeTitle;
    }
    public void setDownloadDirectory( String downloadDirectory ) {
        this.downloadDirectory = downloadDirectory ;
    }

    public void setParameters() { // let all the non-set attributes get values
        Common.downloadFile( webSite, Common.tempDirectory, indexName, false, "" );
        Common.newEncodeFile( Common.tempDirectory, indexName, indexEncodeName );
        String allString = Common.getFileString( Common.tempDirectory, indexEncodeName );

        Common.debugPrintln( "開始解析各參數 :" );


        String[] tempStrings = webSite.split( "=|/" );
        int serverNo = Integer.parseInt( tempStrings[tempStrings.length-1] );
        setServerNo( serverNo );


        int beginIndex = allString.indexOf( "script src=" ) + 11;
        int endIndex = allString.indexOf( ">", beginIndex );

        int endIndexOfBaseURL = Common.getIndexOfOrderKeyword( webSite, "/", 3 );
        baseURL = webSite.substring( 0, endIndexOfBaseURL );
        jsURL = baseURL + allString.substring( beginIndex, endIndex );

        Common.debugPrintln( "基本位址: " + baseURL );
        Common.debugPrintln( "JS檔位址: " + jsURL );


        Common.debugPrintln( "開始解析title和wholeTitle :" );


        Common.debugPrintln( "作品名稱(title) : " + getTitle() );


        String[] tokens = allString.split( "\"|," );

        for ( int i = 0; i < tokens.length; i ++ ) {
            if ( tokens[i].equals( "keywords" ) ) {
                wholeTitle = Common.getTraditionalChinese( tokens[i+2].trim() );
                break;
            }
        }

        setWholeTitle( wholeTitle );
        Common.debugPrintln( "作品+章節名稱(wholeTitle) : " + wholeTitle );

        totalPage = getHowManyKeyWordInString( allString, "|" ) + 1;
        comicURL = new String [totalPage]; // totalPage = amount of comic pic
        SetUp.setWholeTitle( wholeTitle );
    }


    public void parseComicURL() { // parse URL and save all URLs in comicURL
        // 先取得前面的下載伺服器網址
        Common.downloadFile( jsURL, Common.tempDirectory, jsName, false, "" );
        String allJsString = Common.getFileString( Common.tempDirectory, jsName );

        int index = 0;
        for ( int i = 0; i < serverNo; i ++ )
            index = allJsString.indexOf( "ServerList[", index ) + 1;

        int beginIndex = allJsString.indexOf( "\"", index ) + 1;
        int endIndex = allJsString.indexOf( "\"", beginIndex + 1 );

        String baseDownloadURL = allJsString.substring( beginIndex, endIndex );
        Common.debugPrintln( "下載伺服器位址: " + baseDownloadURL );

        // 再取得後面的圖片目錄網址
        String[] lines = Common.getFileStrings( Common.tempDirectory, indexEncodeName );
        index = 0;
        while ( !lines[index].matches( "(?s).*\\|(?s).*" ) )
            index ++;

        beginIndex = lines[index].indexOf( "\"" ) + 1;
        endIndex = lines[index].lastIndexOf( "\"" );


        String tempString = lines[index].substring( beginIndex, endIndex );

        String[] specificDownloadURL = tempString.split( "\\|" );


        for ( int i = 0; i < comicURL.length; i ++ ) {
            comicURL[i] = baseDownloadURL + specificDownloadURL[i];
            //Common.debugPrintln( i + " : " + comicURL[i] ) ;
        }
        //System.exit(0);
    }


    public void showParameters() { // for debug
    }

    // 下載網址指向的網頁，全部存入String後回傳
    public String getAllPageString( String urlString ) {
        String indexName = Common.getStoredFileName( Common.tempDirectory, "index_99_", "html" );
        String indexEncodeName = Common.getStoredFileName( Common.tempDirectory, "index_99_encode_", "html" );

        System.out.println( "URL: " + urlString );
        Common.downloadFile( urlString, Common.tempDirectory, indexName, false, "" );
        Common.newEncodeFile( Common.tempDirectory, indexName, indexEncodeName );

        return Common.getTraditionalChinese( Common.getFileString( Common.tempDirectory, indexEncodeName ) );
    }

    // 從網址判斷是否為單集頁面(true) 還是主頁面(false)
    public boolean isSingleVolumePage( String urlString ) {
        if ( urlString.matches( "(?s).*\\.htm\\?s(?s).*"  ) ) // ex. dm.99manga.com/manga/4142/61660.htm?s=4
            return true;
        else // ex. dm.99manga.com/comic/4142/
            return false;
    }

    // 設定下載伺服器的編號
	public void setServerNo( int no ) {
        serverNo = no;
	}
    // 取得下載伺服器的編號
    public int getServerNo() {
        return serverNo;
    }

    // 從單集頁面取得title(作品名稱)
    public String getTitleOnSingleVolumePage( String urlString ) {

        Common.debugPrintln( "開始由單集頁面位址取得title：" );
        int tempIndex = urlString.indexOf( "s=" );
        int endIndex = urlString.substring( 0, tempIndex ).lastIndexOf( "/" );
        String mainPageURL = urlString.substring( 0, endIndex );
        
        Common.debugPrintln( "替換前URL: " + mainPageURL );
        // 目前共三種轉換情形，單集頁面與主頁面的資料夾名稱差異
        mainPageURL = mainPageURL.replaceAll( "manhua", "comic" );
        mainPageURL = mainPageURL.replaceAll( "Manhua", "Comic" );
        mainPageURL = mainPageURL.replaceAll( "manga", "comic" );
        mainPageURL = mainPageURL.replaceAll( "dm.99comic.com", "dm.99manga.com" ); // 再把http://dm.99manga.com轉回來
        mainPageURL = mainPageURL.replaceAll( "3gcomic.com", "3gmanhua.com" ); // 再把http://3gmanhua.com轉回來
        Common.debugPrintln( "替換後URL: " + mainPageURL );
        
        return getTitleOnMainPage( mainPageURL, getAllPageString( mainPageURL ) );
    }

    // 從主頁面取得title(作品名稱)
    public String getTitleOnMainPage( String urlString, String allPageString ) {
        Common.debugPrintln( "開始由主頁面位址取得title：" );
        
        String[] tokens = allPageString.split( "\\s*=\\s+|\"" );
        String title = "";

        int index = 0;
        for ( ; index < tokens.length; index ++ ) {
            if ( tokens[index].matches( "(?s).*wumiiTitle\\s*" ) ) { // ex. var wumiiTitle = "食夢者";
                title = tokens[index+2];
                break;
            }
        }

        // 第一種方法找不到的時候 ex.http://1mh.com/comic/8308/
        if ( title.equals( "" ) ) {
            int beginIndex = allPageString.indexOf( "<title>" ) + 7;
            int endIndex = Common.getSmallerIndexOfTwoKeyword( allPageString, beginIndex, "<", " " );

            title = allPageString.substring( beginIndex, endIndex );
        }

        return Common.getTraditionalChinese( title );
    }

    // 取得在string中有"幾個"符合keyword的字串
    public static int getHowManyKeyWordInString( String string, String keyword ) {
        int index = 0;
        int keywordCount = 0;
        while ( ( index = string.indexOf( keyword, index ) ) >= 0 ) {
            keywordCount ++;
            index ++;
        }

        return keywordCount;
    }

    // 從主頁面取得所有集數名稱和網址
    public List<List<String>> getVolumeTitleAndUrlOnMainPage( String urlString, String allPageString ) {
        // combine volumeList and urlList into combinationList, return it.
        // ex. <li><a href=/manga/4142/84144.htm?s=4 target=_blank>bakuman151集</a>
        //     <a href="javascript:ShowA(4142,84144,4);" class=Showa>加速A</a>
        //     <a href="javascript:ShowB(4142,84144,4);" class=Showb>加速B</a></li>

        List<List<String>> combinationList = new ArrayList<List<String>>();
        List<String> urlList = new ArrayList<String>();
        List<String> volumeList = new ArrayList<String>();

        int endIndexOfBaseURL = Common.getIndexOfOrderKeyword( urlString, "/", 3 );
        String baseURL = urlString.substring( 0, endIndexOfBaseURL );

        int totalVolume = getHowManyKeyWordInString( allPageString, "ShowA" );
        int index = 0;
        for ( int count = 0; count < totalVolume; count ++ ) {
            index = allPageString.indexOf( "href=/", index );

            int urlBeginIndex = allPageString.indexOf( "/", index );
            int urlEndIndex = Common.getSmallerIndexOfTwoKeyword( allPageString, index, " ", ">" );

            urlList.add( baseURL + allPageString.substring( urlBeginIndex, urlEndIndex ) );

            int volumeBeginIndex = allPageString.indexOf( ">", index ) + 1;
            int volumeEndIndex = allPageString.indexOf( "<", volumeBeginIndex );

            volumeList.add( allPageString.substring( volumeBeginIndex, volumeEndIndex ) );

            index = volumeEndIndex;
        }

        combinationList.add( volumeList );
        combinationList.add( urlList );

        return combinationList;
    }

    public void outputVolumeAndUrlList( List<String> volumeList, List<String> urlList ) {
        Common.outputFile( volumeList, Common.tempDirectory, Common.tempVolumeFileName );
        Common.outputFile( urlList, Common.tempDirectory, Common.tempUrlFileName );
    }


    public void printLogo() {
        System.out.println( " _________________________________" );
        System.out.println( "|                              |" );
        System.out.println( "| Run the NINE NINE module: |" );
        System.out.println( "|__________________________________|\n" );
    }
    
    public String[] getTempFileNames() {
        return new String[] { indexName, indexEncodeName, jsName };
    }
}

class Parse99Comic extends ParseNINENINE {

    public void printLogo() {
        System.out.println( " _______________________________" );
        System.out.println( "|                            |" );
        System.out.println( "| Run the 99 comic module: |" );
        System.out.println( "|________________________________|\n" );
    }

}

class Parse99Manga extends ParseNINENINE {

    public void printLogo() {
        System.out.println( " ________________________________" );
        System.out.println( "|                             |" );
        System.out.println( "| Run the 99 manga module: |" );
        System.out.println( "|_________________________________|\n" );
    }

}

class Parse99770 extends ParseNINENINE {

    public void printLogo() {
        System.out.println( " _____________________________" );
        System.out.println( "|                           |" );
        System.out.println( "| Run the 99770.cc module: |" );
        System.out.println( "|_______________________________|\n" );
    }

}

class Parse99Mh extends ParseNINENINE {

    public void printLogo() {
        System.out.println( " ___________________________" );
        System.out.println( "|                         |" );
        System.out.println( "| Run the 99mh module: |" );
        System.out.println( "|____________________________|\n" );
    }

}

class ParseCoco extends ParseNINENINE {

    public void printLogo() {
        System.out.println( " ________________________________" );
        System.out.println( "|                             |" );
        System.out.println( "| Run the cococomic module: |" );
        System.out.println( "|_________________________________|\n" );
    }

}

class Parse1Mh extends ParseNINENINE {

    public void printLogo() {
        System.out.println( " __________________________" );
        System.out.println( "|                        |" );
        System.out.println( "| Run the 1mh module: |" );
        System.out.println( "|___________________________|\n" );
    }

}

class Parse3G extends ParseNINENINE {

    public void printLogo() {
        System.out.println( " ________________________" );
        System.out.println( "|                      |" );
        System.out.println( "| Run the 3G module: |" );
        System.out.println( "|_________________________|\n" );
    }

        // 從主頁面取得所有集數名稱和網址
    public List<List<String>> getVolumeTitleAndUrlOnMainPage( String urlString, String allPageString ) {
        // combine volumeList and urlList into combinationList, return it.
        // ex. <li><a href=/manga/4142/84144.htm?s=4 target=_blank>bakuman151集</a>
        //     <a href="javascript:ShowA(4142,84144,4);" class=Showa>加速A</a>
        //     <a href="javascript:ShowB(4142,84144,4);" class=Showb>加速B</a></li>

        List<List<String>> combinationList = new ArrayList<List<String>>();
        List<String> urlList = new ArrayList<String>();
        List<String> volumeList = new ArrayList<String>();

        int endIndexOfBaseURL = Common.getIndexOfOrderKeyword( urlString, "/", 3 );
        String baseURL = urlString.substring( 0, endIndexOfBaseURL );

        int totalVolume = getHowManyKeyWordInString( allPageString, " target=" );
        int index = 0;
        for ( int count = 0; count < totalVolume; count ++ ) {
            index = allPageString.indexOf( "href=/", index );

            int urlBeginIndex = allPageString.indexOf( "/", index );
            int urlEndIndex = Common.getSmallerIndexOfTwoKeyword( allPageString, index, " ", ">" );

            urlList.add( baseURL + allPageString.substring( urlBeginIndex, urlEndIndex ) );

            int volumeBeginIndex = allPageString.indexOf( ">", index ) + 1;
            int volumeEndIndex = allPageString.indexOf( "<", volumeBeginIndex );

            volumeList.add( allPageString.substring( volumeBeginIndex, volumeEndIndex ) );

            index = volumeEndIndex;
        }

        combinationList.add( volumeList );
        combinationList.add( urlList );

        return combinationList;
    }
    
    

}
