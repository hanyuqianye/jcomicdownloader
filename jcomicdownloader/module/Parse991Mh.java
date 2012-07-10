



public class Parse991Mh extends Parse99ComicTC {

    public Parse991Mh() {
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
