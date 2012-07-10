

// www.99comic.com 變繁體版了，所以套用cococomic的方式解析
public class Parse99Comic extends Parse99manga {

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


// www.99comic.com 變繁體版了，所以套用cococomic的方式解析
class Parse99ComicTC extends Parse99CocoTC {

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
