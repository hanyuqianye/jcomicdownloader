public class Parse99Coco extends Parse99manga {

    public Parse99Coco() {
        super();
        siteID = Site.NINENINE_COCO;
        
        jsURL = "http://cococomic.com/v3/i3.js";
    }
    
    @Override // 下載網址指向的網頁，全部存入String後回傳
    public String getAllPageString( String urlString ) {
        String indexName = Common.getStoredFileName( SetUp.getTempDirectory(), "index_99_", "html" );
        String indexEncodeName = Common.getStoredFileName( SetUp.getTempDirectory(), "index_99_encode_", "html" );

        System.out.println( "URL: " + urlString );
        Common.downloadFile( urlString, SetUp.getTempDirectory(), indexName, false, "" );

        Common.newEncodeFile( SetUp.getTempDirectory(), indexName, indexEncodeName, Zhcode.GB2312 );
        return Common.getFileString( SetUp.getTempDirectory(), indexEncodeName );

    }
    
    @Override // 從主頁面取得title(作品名稱)
    public String getTitleOnMainPage( String urlString, String allPageString ) {
        Common.debugPrintln( "開始由主頁面位址取得title：" );
        // ex. http://cococomic.com/comic/6177/

        
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

    @Override // 從主頁面取得所有集數名稱和網址
    public List<List<String>> getVolumeTitleAndUrlOnMainPage( String urlString, String allPageString ) {
        // combine volumeList and urlList into combinationList, return it.
        // ex. <li><a href=/manga/4142/84144.htm?s=4 target=_blank>bakuman151集</a>
        //     <a href="javascript:ShowA(4142,84144,4);" class=Showa>加速A</a>
        //     <a href="javascript:ShowB(4142,84144,4);" class=Showb>加速B</a></li>

      
        List<List<String>> combinationList = new ArrayList<List<String>>();
        List<String> urlList = new ArrayList<String>();
        List<String> volumeList = new ArrayList<String>();
        
        
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
    public void printLogo() {
        System.out.println( " ___________________________" );
        System.out.println( "|                         " );
        System.out.println( "| Run the cococomic module: " );
        System.out.println( "|____________________________\n" );
    }
}


// cococomic變繁體版了，解析方法全面翻新
class Parse99CocoTC extends Parse99manga {

    public Parse99CocoTC() {
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