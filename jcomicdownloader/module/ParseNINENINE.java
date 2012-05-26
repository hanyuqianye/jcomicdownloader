/*
----------------------------------------------------------------------------------------------------
Program Name : JComicDownloader
Authors  : surveyorK
Last Modified : 2012/5/03
----------------------------------------------------------------------------------------------------
ChangeLog:
4.01: 1. 修復cococomic簡體版頁面的集數解析問題。
3.19: 1. 修復99manga繁體版因改版而解析錯誤的問題。
3.17: 1. 修復99770和cococomic繁體版頁面的集數解析問題。
      2. 修復99manga簡體版和繁體版頁面的集數解析問題。
      3. 修復1mh的集數解析問題。
3.16: 1. 增加對www.99comic.com繁體版的支援。
3.12: 1. 修復cococomic因網站改版而無法下載的問題。
      2. 修復99770因網站改版而無法下載的問題。
----------------------------------------------------------------------------------------------------
 */
package jcomicdownloader.module;

import jcomicdownloader.tools.*;
import jcomicdownloader.enums.*;
import jcomicdownloader.*;

import java.util.*;
import jcomicdownloader.encode.Zhcode;

public class ParseNINENINE extends ParseOnlineComicSite {

    protected String indexName;
    protected String indexEncodeName;
    protected String jsName;
    protected int serverNo; // 下載伺服器的編號
    protected String baseURL; // 首頁網址 ex. http://dm.99manga.com
    protected String jsURL; // 存放下載伺服器網址的.js檔的網址

    /**
     *
     * @author user
     */
    public ParseNINENINE() {
        indexName = Common.getStoredFileName( SetUp.getTempDirectory(), "index_99_parse_", "html" );
        indexEncodeName = Common.getStoredFileName( SetUp.getTempDirectory(), "index_99_encode_parse_", "html" );
        jsName = Common.getStoredFileName( SetUp.getTempDirectory(), "index_99_parse_", "js" );
    }

    public ParseNINENINE( String webSite, String titleName ) {
        this();
        this.webSite = webSite;
        this.title = titleName;
    }

    @Override
    public void setParameters() { // let all the non-set attributes get values
        Common.downloadFile( webSite, SetUp.getTempDirectory(), indexName, false, "" );
        Common.newEncodeFile( SetUp.getTempDirectory(), indexName, indexEncodeName );
        String allString = Common.getFileString( SetUp.getTempDirectory(), indexEncodeName );

        Common.debugPrintln( "開始解析各參數 :" );


        String[] tempStrings = webSite.split( "=|/" );
        int serverNo = Integer.parseInt( tempStrings[tempStrings.length - 1] );
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

        if ( getWholeTitle() == null || getWholeTitle().equals( "" ) ) {
            String[] tokens = allString.split( "\"|," );

            for ( int i = 0 ; i < tokens.length && Run.isAlive; i++ ) {
                if ( tokens[i].equals( "keywords" ) ) {
                    wholeTitle = Common.getStringRemovedIllegalChar( Common.getTraditionalChinese( tokens[i + 2].trim() ) );
                    break;
                }
            }
            setWholeTitle( wholeTitle );
        }

        Common.debugPrintln( "作品+章節名稱(wholeTitle) : " + getWholeTitle() );

        totalPage = getHowManyKeyWordInString( allString, "|" ) + 1;
        comicURL = new String[totalPage]; // totalPage = amount of comic pic
        SetUp.setWholeTitle( wholeTitle );
    }

    @Override
    public void parseComicURL() { // parse URL and save all URLs in comicURL
        // 先取得前面的下載伺服器網址
        Common.downloadFile( jsURL, SetUp.getTempDirectory(), jsName, false, "" );
        String allJsString = Common.getFileString( SetUp.getTempDirectory(), jsName );

        int index = 0;
        for ( int i = 0 ; i < serverNo ; i++ ) {
            index = allJsString.indexOf( "ServerList[", index ) + 1;
        }

        int beginIndex = allJsString.indexOf( "\"", index ) + 1;
        int endIndex = allJsString.indexOf( "\"", beginIndex + 1 );

        String baseDownloadURL = allJsString.substring( beginIndex, endIndex );
        Common.debugPrintln( "下載伺服器位址: " + baseDownloadURL );

        // 再取得後面的圖片目錄網址
        String[] lines = Common.getFileStrings( SetUp.getTempDirectory(), indexEncodeName );
        index = 0;
        while ( !lines[index].matches( "(?s).*\\|(?s).*" ) ) {
            index++;
        }

        beginIndex = lines[index].indexOf( "\"" ) + 1;
        endIndex = lines[index].lastIndexOf( "\"" );


        String tempString = lines[index].substring( beginIndex, endIndex );

        String[] specificDownloadURL = tempString.split( "\\|" );


        for ( int i = 0 ; i < comicURL.length && Run.isAlive; i++ ) {
            comicURL[i] = baseDownloadURL + specificDownloadURL[i];
            //Common.debugPrintln( i + " : " + comicURL[i] ) ;
        }
        //System.exit(0);
    }

    @Override // 下載網址指向的網頁，全部存入String後回傳
    public String getAllPageString( String urlString ) {
        String indexName = Common.getStoredFileName( SetUp.getTempDirectory(), "index_99_", "html" );
        String indexEncodeName = Common.getStoredFileName( SetUp.getTempDirectory(), "index_99_encode_", "html" );

        System.out.println( "URL: " + urlString );
        Common.downloadFile( urlString, SetUp.getTempDirectory(), indexName, false, "" );

        Common.newEncodeFile( SetUp.getTempDirectory(), indexName, indexEncodeName, Zhcode.GBK );
        return Common.getFileString( SetUp.getTempDirectory(), indexEncodeName );

    }
    @Override // 從網址判斷是否為單集頁面(true) 還是主頁面(false)
    public boolean isSingleVolumePage( String urlString ) {
        if ( urlString.matches( "(?s).*\\.htm\\?s(?s).*" ) ) // ex. dm.99manga.com/manga/4142/61660.htm?s=4
        {
            return true;
        } else // ex. dm.99manga.com/comic/4142/
        {
            return false;
        }
    }

    // 設定下載伺服器的編號
    public void setServerNo( int no ) {
        serverNo = no;
    }
    // 取得下載伺服器的編號

    public int getServerNo() {
        return serverNo;
    }

    @Override // 從單集頁面取得title(作品名稱)
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

    @Override // 從主頁面取得title(作品名稱)
    public String getTitleOnMainPage( String urlString, String allPageString ) {
        Common.debugPrintln( "開始由主頁面位址取得title：" );

        String[] tokens = allPageString.split( "\\s*=\\s+|\"" );
        String title = "";

        int index = 0;
        for ( ; index < tokens.length ; index++ ) {
            if ( tokens[index].matches( "(?s).*wumiiTitle\\s*" ) ) { // ex. var wumiiTitle = "食夢者";
                title = tokens[index + 2];
                break;
            }
        }

        // 第一種方法找不到的時候 ex.http://1mh.com/comic/8308/
        if ( title.equals( "" ) ) {
            int beginIndex = allPageString.indexOf( "<title>" ) + 7;
            int endIndex = Common.getSmallerIndexOfTwoKeyword( allPageString, beginIndex, "<", " " );

            title = allPageString.substring( beginIndex, endIndex );
        }

        return Common.getStringRemovedIllegalChar( Common.getTraditionalChinese( title ) );
    }

    // 取得在string中有"幾個"符合keyword的字串
    public static int getHowManyKeyWordInString( String string, String keyword ) {
        int index = 0;
        int keywordCount = 0;
        while ( (index = string.indexOf( keyword, index )) >= 0 ) {
            keywordCount++;
            index++;
        }

        return keywordCount;
    }

    @Override // 從主頁面取得所有集數名稱和網址
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
        for ( int count = 0 ; count < totalVolume ; count++ ) {
            index = allPageString.indexOf( "href=/", index );

            int urlBeginIndex = allPageString.indexOf( "/", index );
            int urlEndIndex = Common.getSmallerIndexOfTwoKeyword( allPageString, index, " ", ">" );

            urlList.add( baseURL + allPageString.substring( urlBeginIndex, urlEndIndex ) );

            int volumeBeginIndex = allPageString.indexOf( ">", index ) + 1;
            int volumeEndIndex = allPageString.indexOf( "<", volumeBeginIndex );

            String title = allPageString.substring( volumeBeginIndex, volumeEndIndex );

            volumeList.add( getVolumeWithFormatNumber( 
                    Common.getStringRemovedIllegalChar( 
                        Common.getTraditionalChinese( title ) ) ) );

            index = volumeEndIndex;
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
    public void printLogo() {
        System.out.println( " __________________________________" );
        System.out.println( "|                              " );
        System.out.println( "| Run the NINE NINE module: " );
        System.out.println( "|__________________________________\n" );
    }

    @Override
    public String[] getTempFileNames() {
        return new String[] { indexName, indexEncodeName, jsName };
    }
}

class Parse99Comic extends ParseNINENINE {

    public Parse99Comic() {
        super();
        siteID = Site.NINENINE_COMIC;
    }

    @Override
    public void printLogo() {
        System.out.println( " _______________________________" );
        System.out.println( "|                            " );
        System.out.println( "| Run the 99 comic module: " );
        System.out.println( "|________________________________\n" );
    }
}

class Parse99MangaTC extends Parse99ComicTC {

    public Parse99MangaTC() {
        super();
        siteID = Site.NINENINE_MANGA_TC;
        
        baseURL = "http://dm.99manga.com";
    }
    
    @Override // 從網址判斷是否為單集頁面(true) 還是主頁面(false)
    public boolean isSingleVolumePage( String urlString ) {
        // ex. http://dm.99manga.com/page/11843m98671/
        if ( urlString.matches( "(?s).*/page/(?s).*" ) )
        {
            return true;
        } else // ex. http://dm.99manga.com/comic/11843/
        {
            return false;
        }
    }
    
    @Override // 從主頁面取得所有集數名稱和網址
    public List<List<String>> getVolumeTitleAndUrlOnMainPage( String urlString, String allPageString ) {
        
        List<List<String>> combinationList = new ArrayList<List<String>>();
        List<String> urlList = new ArrayList<String>();
        List<String> volumeList = new ArrayList<String>();
        
        int beginIndex = allPageString.indexOf( "class=\"cVol\"" );
        int endIndex = allPageString.indexOf( "Vol_list", beginIndex );

        String tempString = allPageString.substring( beginIndex, endIndex );
        
        totalVolume = tempString.split( "href='/page/" ).length - 1;

        beginIndex = endIndex = 0;
        for ( int count = 0 ; count < totalVolume ; count++ ) {

            beginIndex = tempString.indexOf( "href='/page/", beginIndex );
            beginIndex = tempString.indexOf( "'", beginIndex ) + 1;
            endIndex = tempString.indexOf( "'", beginIndex );
            urlList.add( baseURL + tempString.substring( beginIndex, endIndex ) );

            beginIndex = tempString.indexOf( ">", beginIndex ) + 1;
            endIndex = tempString.indexOf( "<", beginIndex );
            String title = tempString.substring( beginIndex, endIndex );

            volumeList.add( getVolumeWithFormatNumber( 
                    Common.getStringRemovedIllegalChar( 
                        Common.getTraditionalChinese( title.trim() ) ) ) );
        }

        combinationList.add( volumeList );
        combinationList.add( urlList );

        return combinationList;
    }

    @Override
    public void printLogo() {
        System.out.println( " ________________________________" );
        System.out.println( "|                             " );
        System.out.println( "| Run the dm 99 manga module: " );
        System.out.println( "|_________________________________\n" );
    }
}

class Parse99Manga extends ParseNINENINE {

    public Parse99Manga() {
        super();
        siteID = Site.NINENINE_MANGA;
        
        baseURL = "http://99comic.com";
    }

    @Override
    public void printLogo() {
        System.out.println( " ________________________________" );
        System.out.println( "|                             " );
        System.out.println( "| Run the 99 manga module: " );
        System.out.println( "|_________________________________\n" );
    }
}

// www.99comic.com 變繁體版了，所以套用cococomic的方式解析
class Parse99ComicTC extends ParseCocoTC {

    public Parse99ComicTC() {
        super();
        siteID = Site.NINENINE_COMIC_TC;
        jsURL = "http://www.99comic.com/script/viewhtml.js";
        baseURL = "http://www.99comic.com";
    }
    
    @Override // 從網址判斷是否為單集頁面(true) 還是主頁面(false)
    public boolean isSingleVolumePage( String urlString ) {
        // ex. http://www.99comic.com/comics/11728o98066/
        if ( urlString.matches( "(?s).*/comics/(?s).*" ) )
        {
            return true;
        } else // ex. http://www.99comic.com/comic/9911728/
        {
            return false;
        }
    }
    
    @Override // 從主頁面取得title(作品名稱)
    public String getTitleOnMainPage( String urlString, String allPageString ) {
        Common.debugPrintln( "開始由主頁面位址取得title：" );

        int beginIndex = allPageString.indexOf( "<h1>" ) + 5;
        beginIndex = allPageString.indexOf( ">", beginIndex ) + 1;
        int endIndex = allPageString.indexOf( "<", beginIndex );

        title = allPageString.substring( beginIndex, endIndex ).trim().split( "\\s" )[0];


        return Common.getStringRemovedIllegalChar( Common.getTraditionalChinese( title ) );
    }
    
    @Override // 從主頁面取得所有集數名稱和網址
    public List<List<String>> getVolumeTitleAndUrlOnMainPage( String urlString, String allPageString ) {
        
        List<List<String>> combinationList = new ArrayList<List<String>>();
        List<String> urlList = new ArrayList<String>();
        List<String> volumeList = new ArrayList<String>();
        
        int beginIndex = allPageString.indexOf( "class=\"cVol\"" );
        int endIndex = allPageString.indexOf( "Vol_list", beginIndex );

        String tempString = allPageString.substring( beginIndex, endIndex );
        
        totalVolume = tempString.split( "href=" ).length - 1;

        beginIndex = endIndex = 0;
        for ( int count = 0 ; count < totalVolume ; count++ ) {

            beginIndex = tempString.indexOf( "href=", beginIndex );
            beginIndex = tempString.indexOf( "'", beginIndex ) + 1;
            endIndex = tempString.indexOf( "'", beginIndex );
            urlList.add( baseURL + tempString.substring( beginIndex, endIndex ) );

            beginIndex = tempString.indexOf( ">", beginIndex ) + 1;
            endIndex = tempString.indexOf( "<", beginIndex );
            String title = tempString.substring( beginIndex, endIndex );

            volumeList.add( getVolumeWithFormatNumber( 
                    Common.getStringRemovedIllegalChar( 
                        Common.getTraditionalChinese( title.trim() ) ) ) );
        }

        combinationList.add( volumeList );
        combinationList.add( urlList );

        return combinationList;
    }
    
    @Override
    public void setParameters() { // let all the non-set attributes get values

        Common.debugPrintln( "開始解析各參數 :" );

        Common.debugPrintln( "基本位址: " + baseURL );
        Common.debugPrintln( "JS檔位址: " + jsURL );


        Common.debugPrintln( "開始解析title和wholeTitle :" );


        Common.debugPrintln( "作品名稱(title) : " + getTitle() );


        Common.debugPrintln( "作品+章節名稱(wholeTitle) : " + getWholeTitle() );


    }
    
    @Override
    public void parseComicURL() { // parse URL and save all URLs in comicURL
        // 先取得後面的下載伺服器網址
        
        String allPageString = getAllPageString( webSite );
        
        int beginIndex = 0;
        int endIndex = 0;
        
        beginIndex = allPageString.indexOf( "var sFiles" );
        beginIndex = allPageString.indexOf( "\"", beginIndex ) + 1;
        endIndex = allPageString.indexOf( "\"", beginIndex );
        
        String tempString = allPageString.substring( beginIndex, endIndex );
        String[] urlTokens = tempString.split( "\\|" );
        
        // 取得頁數
        comicURL = new String[urlTokens.length];
        
        
        // 再取得後面的下載伺服器網址
        beginIndex = allPageString.indexOf( "var sPath", beginIndex );
        beginIndex = allPageString.indexOf( "\"", beginIndex ) + 1;
        endIndex = allPageString.indexOf( "\"", beginIndex );
        serverNo = Integer.parseInt( 
            allPageString.substring( beginIndex, endIndex ) );

        Common.downloadFile( jsURL, SetUp.getTempDirectory(), jsName, false, "" );
        String allJsString = Common.getFileString( SetUp.getTempDirectory(), jsName );

        beginIndex = allJsString.indexOf( "\"" ) + 1;
        endIndex = allJsString.indexOf( "\"", beginIndex );
        
        tempString = allJsString.substring( beginIndex, endIndex );
        String[] serverTokens = tempString.split( "\\|" );
        baseURL = serverTokens[serverNo - 1];
        
        Common.debugPrintln( "下載伺服器位址: " + baseURL );

        for ( int i = 0 ; i < comicURL.length ; i++ ) {
            comicURL[i] = baseURL + urlTokens[i];
            //Common.debugPrintln( i + " : " + comicURL[i] ) ;
        }
        //System.exit(0);
    }

    @Override
    public void printLogo() {
        System.out.println( " ______________________________" );
        System.out.println( "|                           " );
        System.out.println( "| Run the www.99comic.com module: " );
        System.out.println( "|_______________________________\n" );
    }
}

// mh.99770.cc 變繁體版了，所以套用cococomic的方式解析
class ParseMh99770 extends ParseCocoTC {

    public ParseMh99770() {
        super();
        siteID = Site.NINENINE_MH_99770;
    }

    @Override
    public void printLogo() {
        System.out.println( " ______________________________" );
        System.out.println( "|                           " );
        System.out.println( "| Run the mh.99770.cc module: " );
        System.out.println( "|_______________________________\n" );
    }
}

class Parse99770 extends ParseNINENINE {

    public Parse99770() {
        super();
        siteID = Site.NINENINE_99770;
    }

    @Override
    public void printLogo() {
        System.out.println( " ______________________________" );
        System.out.println( "|                           " );
        System.out.println( "| Run the www.99770.cc module: " );
        System.out.println( "|_______________________________\n" );
    }
}

class Parse99Mh extends ParseNINENINE {

    public Parse99Mh() {
        super();
        siteID = Site.NINENINE_MH;
        
        jsURL = "http://mh.99770.cc/script/ds.js";
    }
    


    @Override
    public void printLogo() {
        System.out.println( " ___________________________" );
        System.out.println( "|                         " );
        System.out.println( "| Run the 99mh module: " );
        System.out.println( "|____________________________\n" );
    }
}

class ParseCoco extends ParseNINENINE {

    public ParseCoco() {
        super();
        siteID = Site.NINENINE_COCO;
        
        jsURL = "http://cococomic.com/v3/i3.js";
    }
    


    @Override
    public void printLogo() {
        System.out.println( " ___________________________" );
        System.out.println( "|                         " );
        System.out.println( "| Run the cococomic module: " );
        System.out.println( "|____________________________\n" );
    }
}

// cococomic變繁體版了，解析方法全面翻新
class ParseCocoTC extends ParseNINENINE {

    public ParseCocoTC() {
        super();
        siteID = Site.NINENINE_COCO_TC;
        
        jsURL = "http://www.cococomic.com/script/ds.js";
    }
    
    @Override // 下載網址指向的網頁，全部存入String後回傳
    public String getAllPageString( String urlString ) {
        String indexName = Common.getStoredFileName( SetUp.getTempDirectory(), "index_99_", "html" );
        String indexEncodeName = Common.getStoredFileName( SetUp.getTempDirectory(), "index_99_encode_", "html" );

        System.out.println( "URL: " + urlString );
        Common.downloadFile( urlString, SetUp.getTempDirectory(), indexName, false, "" );
        
        // 網頁為繁體版utf8，無須轉碼
        return Common.getFileString( SetUp.getTempDirectory(), indexName );

    }
    
    @Override // 從網址判斷是否為單集頁面(true) 還是主頁面(false)
    public boolean isSingleVolumePage( String urlString ) {
        // ex. http://www.cococomic.com/comic/6613/89503/
        if ( Common.getAmountOfString( urlString, "/" ) > 5 )
        {
            return true;
        } else // ex. http://www.cococomic.com/comic/6613/
        {
            return false;
        }
    }
    
    @Override // 從主頁面取得title(作品名稱)
    public String getTitleOnMainPage( String urlString, String allPageString ) {
        Common.debugPrintln( "開始由主頁面位址取得title：" );

        int beginIndex = allPageString.indexOf( "class=\"cTitle\"" );
        beginIndex = allPageString.indexOf( ">", beginIndex ) + 1;
        int endIndex = allPageString.indexOf( "<", beginIndex );

        title = allPageString.substring( beginIndex, endIndex ).trim().split( "\\s" )[0];


        return Common.getStringRemovedIllegalChar( Common.getTraditionalChinese( title ) );
    }
    
    @Override // 從主頁面取得所有集數名稱和網址
    public List<List<String>> getVolumeTitleAndUrlOnMainPage( String urlString, String allPageString ) {
        
        List<List<String>> combinationList = new ArrayList<List<String>>();
        List<String> urlList = new ArrayList<String>();
        List<String> volumeList = new ArrayList<String>();
        
        int beginIndex = allPageString.indexOf( "class=\"cVol\"" );
        int endIndex = allPageString.indexOf( "class=\"cAreaTitle\"", beginIndex );

        String tempString = allPageString.substring( beginIndex, endIndex );
        
        String keyword = "href='http";
        totalVolume = tempString.split( keyword ).length - 1;

        beginIndex = endIndex = 0;
        for ( int count = 0 ; count < totalVolume ; count++ ) {

            beginIndex = tempString.indexOf( keyword, beginIndex );
            beginIndex = tempString.indexOf( "'", beginIndex ) + 1;
            endIndex = tempString.indexOf( "'", beginIndex );
            urlList.add( tempString.substring( beginIndex, endIndex ) );

            beginIndex = tempString.indexOf( ">", beginIndex ) + 1;
            endIndex = tempString.indexOf( "<", beginIndex );
            String title = tempString.substring( beginIndex, endIndex );

            volumeList.add( getVolumeWithFormatNumber( 
                    Common.getStringRemovedIllegalChar( 
                        Common.getTraditionalChinese( title.trim() ) ) ) );
            
        }

        combinationList.add( volumeList );
        combinationList.add( urlList );

        return combinationList;
    }
    
    @Override
    public void setParameters() { // let all the non-set attributes get values

        Common.debugPrintln( "開始解析各參數 :" );

        Common.debugPrintln( "基本位址: " + baseURL );
        Common.debugPrintln( "JS檔位址: " + jsURL );


        Common.debugPrintln( "開始解析title和wholeTitle :" );


        Common.debugPrintln( "作品名稱(title) : " + getTitle() );


        Common.debugPrintln( "作品+章節名稱(wholeTitle) : " + getWholeTitle() );


    }
    
    @Override
    public void parseComicURL() { // parse URL and save all URLs in comicURL
        // 先取得後面的下載伺服器網址
        
        String allPageString = getAllPageString( webSite );
        
        int beginIndex = 0;
        int endIndex = 0;
        
        beginIndex = allPageString.indexOf( "var sFiles" );
        beginIndex = allPageString.indexOf( "\"", beginIndex ) + 1;
        endIndex = allPageString.indexOf( "\"", beginIndex );
        
        String tempString = allPageString.substring( beginIndex, endIndex );
        String[] urlTokens = tempString.split( "\\|" );
        
        // 取得頁數
        comicURL = new String[urlTokens.length];
        
        
        // 再取得後面的下載伺服器網址
        beginIndex = allPageString.indexOf( "var sPath", beginIndex );
        beginIndex = allPageString.indexOf( "\"", beginIndex ) + 1;
        endIndex = allPageString.indexOf( "\"", beginIndex );
        serverNo = Integer.parseInt( 
            allPageString.substring( beginIndex, endIndex ) );

        Common.downloadFile( jsURL, SetUp.getTempDirectory(), jsName, false, "" );
        String allJsString = Common.getFileString( SetUp.getTempDirectory(), jsName );

        beginIndex = allJsString.indexOf( "\"" ) + 1;
        endIndex = allJsString.indexOf( "\"", beginIndex );
        
        tempString = allJsString.substring( beginIndex, endIndex );
        String[] serverTokens = tempString.split( "\\|" );
        baseURL = serverTokens[serverNo - 1];
        
        Common.debugPrintln( "下載伺服器位址: " + baseURL );

        for ( int i = 0 ; i < comicURL.length ; i++ ) {
            comicURL[i] = baseURL + urlTokens[i];
            //Common.debugPrintln( i + " : " + comicURL[i] ) ;
        }
        //System.exit(0);
    }

    @Override
    public void printLogo() {
        System.out.println( " ________________________________" );
        System.out.println( "|                             " );
        System.out.println( "| Run the cococomic TC module: " );
        System.out.println( "|_________________________________\n" );
    }
}

class Parse1Mh extends Parse99ComicTC {

    public Parse1Mh() {
        super();
        siteID = Site.NINENINE_1MH;
        
        baseURL = "http://1mh.com";
    }
    
    @Override // 從網址判斷是否為單集頁面(true) 還是主頁面(false)
    public boolean isSingleVolumePage( String urlString ) {
        // ex. http://1mh.com/page/11842l98825/
        if ( urlString.matches( "(?s).*/page/(?s).*" ) )
        {
            return true;
        } else // ex. http://1mh.com/mh/mh11842/
        {
            return false;
        }
    }


    @Override
    public void printLogo() {
        System.out.println( " __________________________" );
        System.out.println( "|                        " );
        System.out.println( "| Run the 1mh module: " );
        System.out.println( "|___________________________\n" );
    }
}

class Parse3G extends ParseNINENINE {

    public Parse3G() {
        super();
        siteID = Site.NINENINE_3G;
    }

    @Override
    public void printLogo() {
        System.out.println( " ________________________" );
        System.out.println( "|                      " );
        System.out.println( "| Run the 3G module: " );
        System.out.println( "|_________________________\n" );
    }

    @Override // 從主頁面取得所有集數名稱和網址
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
        for ( int count = 0 ; count < totalVolume ; count++ ) {
            index = allPageString.indexOf( "href=/", index );

            int urlBeginIndex = allPageString.indexOf( "/", index );
            int urlEndIndex = Common.getSmallerIndexOfTwoKeyword( allPageString, index, " ", ">" );

            urlList.add( baseURL + allPageString.substring( urlBeginIndex, urlEndIndex ) );

            int volumeBeginIndex = allPageString.indexOf( ">", index ) + 1;
            int volumeEndIndex = allPageString.indexOf( "<", volumeBeginIndex );

            String volumeTitle = allPageString.substring( volumeBeginIndex, volumeEndIndex );
            volumeList.add( getVolumeWithFormatNumber( Common.getStringRemovedIllegalChar(
                    Common.getTraditionalChinese( volumeTitle ) ) ) );

            index = volumeEndIndex;
        }

        combinationList.add( volumeList );
        combinationList.add( urlList );

        return combinationList;
    }
}
