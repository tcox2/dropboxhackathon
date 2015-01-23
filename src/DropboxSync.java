import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.dropbox.core.DbxAppInfo;
import com.dropbox.core.DbxAuthFinish;
import com.dropbox.core.DbxClient;
import com.dropbox.core.DbxEntry;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.DbxWebAuthNoRedirect;


public class DropboxSync {
	
    final static String APP_KEY = "5qrvnudv5gltoy8";
    final static String APP_SECRET = "ystltok87pzc6ue";
    final static String ACCESS_TOKEN = "";

    public static void main(String[] args) throws IOException, DbxException {
        DbxAppInfo appInfo = new DbxAppInfo(APP_KEY, APP_SECRET);

        DbxRequestConfig config = new DbxRequestConfig(
        		"JavaTutorial/1.0", Locale.getDefault().toString());
        String accessToken = ACCESS_TOKEN;
        
        if (accessToken.isEmpty())
        {
        	DbxWebAuthNoRedirect webAuth = new DbxWebAuthNoRedirect(config, appInfo);
        	
        	String authorizeUrl = webAuth.start();
        	System.out.println("1. Go to: " + authorizeUrl);
        	System.out.println("2. Click \"Allow\" (you might have to log in first)");
        	System.out.println("3. Copy the authorization code.");
        	String code = new BufferedReader(new InputStreamReader(System.in)).readLine().trim();
        	
        	DbxAuthFinish authFinish = webAuth.finish(code);
        	accessToken = authFinish.accessToken;
        	System.out.println(accessToken);
        }
        
        DbxClient client = new DbxClient(config, accessToken);
        System.out.println("Linked account: " + client.getAccountInfo().displayName);
        
        final Date start = new Date();
        
        List<IDropboxFile> allFilesInDropbox = traverseHierarchy(client, "/");
        
        final Date end = new Date();
        
        System.out.println(end.getTime() - start.getTime());
        System.out.println(allFilesInDropbox.size());
    }
    
    private static List<IDropboxFile> traverseHierarchy(DbxClient client, String parentPath) throws DbxException {
    	List<IDropboxFile> files = new ArrayList<>();
    	
        DbxEntry.WithChildren listing = client.getMetadataWithChildren(parentPath);
        
        System.out.println(String.format("Files in path %s:", parentPath));
        for (DbxEntry child : listing.children) {
            
            if (child.isFile()) {
            	System.out.println("	" + child.name + ": " + child.toString());
            	DropboxFile file = new DropboxFile(child.asFile(), "");

            	System.out.println("filename: " + file.filename());
            	System.out.println("hash: " + file.hash());
            	System.out.println("last modified: " + file.lastModified());
            	System.out.println("path: " + file.path());
            	System.out.println("size: " + file.size());
            	
            	files.add(file);
            }
            else if (child.isFolder()) {
            	files.addAll(traverseHierarchy(client, child.asFolder().path));
            }
        }
        
        return files;
    }
}
