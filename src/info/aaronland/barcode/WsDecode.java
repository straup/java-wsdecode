package info.aaronland.barcode;

/*

  $Id: WsDecode.java,v 1.9 2008/02/24 17:31:59 asc Exp $

  http://aaronland.info/java/ws-decode/
  Copyright (c) 2008 Aaron Straup Cope. All Rights Reserved.

  This code is free software; you can redistribute it and/or modify it
  under the terms of the GNU General Public License version 2 only, as
  published by the Free Software Foundation.  Sun designates this
  particular file as subject to the "Classpath" exception as provided
  by Sun in the LICENSE file that accompanied this code.
 
  This code is distributed in the hope that it will be useful, but WITHOUT
  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
  version 2 for more details (a copy is included in the LICENSE file that
  accompanied this code).
 
  You should have received a copy of the GNU General Public License version
  2 along with this work; if not, write to the Free Software Foundation,
  Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.

  http://code.google.com/p/zxing/
  http://javablog.co.uk/2007/10/27/http-server-api-backport-to-java-5/
  http://java.sun.com/javase/6/docs/jre/api/net/httpserver/spec/com/sun/net/httpserver/HttpExchange.html

  curl -v -H 'Content-Type: image/png' -H 'Expect:' --data-binary \
  	'@/Users/asc/Desktop/qr.png' http://127.0.0.1:9955/decode

*/

import com.sun.net.httpserver.*;

import java.util.concurrent.*;
import java.io.*;
import java.net.*;
import java.util.*;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import javax.imageio.ImageIO;

import com.google.zxing.*;
import com.google.zxing.Reader;
import com.google.zxing.client.j2se.BufferedImageMonochromeBitmapSource;

public class WsDecode {

    private static Reader barcodeReader;

    private static String serverAddress;
    private static String serverName = "ws-decode";
    private static int serverPort = 9955;
    private static double serverVersion = 0.11;

    private static int maxPostSize = 1024 * 512;

    //

    public static void main (String args[]) throws Exception {

        if (args.length != 0) {
            serverPort = Integer.parseInt (args[0]);
        }

        Handler handler = new Handler();
        barcodeReader = new MultiFormatReader();

        InetSocketAddress addr = new InetSocketAddress (serverPort);
        HttpServer server = HttpServer.create (addr, 0);
        HttpContext ctx = server.createContext ("/", handler);
        
        ExecutorService executor = Executors.newCachedThreadPool();
        server.setExecutor (executor);
        server.start ();
        
        int port = server.getAddress().getPort();
        serverAddress = "http://localhost:" + port;

        System.out.println (serverName + " server running on port : " + port);   
        System.out.println ("documentation and usage is available at " + serverAddress + "/\n");
    }

    //

    static class Handler implements HttpHandler {

        public void handle (HttpExchange t) throws IOException {

            String uri = t.getRequestURI().toString();
            String addr = t.getRemoteAddress().getHostName();

            System.out.println ("[" + addr + "] " + uri);
            
            if (uri.equals("/")){
                usage(t);
                return;
            }
            
            if (uri.startsWith("decode", 1)){
                decode(t);
                return;
            }

            int status = 404;
            String rsp = "IN UR BARCODEZ HIDIN FROM U";
            send_response(t, status, rsp);
            return;                
        }
    }

    //

    private static void usage (HttpExchange t) throws IOException {
        String msg =
            serverName + "\n" +
            "------------------------------------------------------\n\n" +
            serverName + " is a bare-bones HTTP interface for decoding 2D barcodes. It works like this:\n\n" +
            "You send a (binary) POST request to " + serverAddress + "/decode containing a PNG or JPG\n" +
            "file and the server will send back a plain-text version of its (the barcode in the image,\n" +
            "of course) message body.\n\n" +
            "------------------------------------------------------\n" +
            "EXAMPLE\n" +
            "------------------------------------------------------\n\n" +
            "\tcurl -v -H 'Content-Type: image/jpeg' -H 'Expect:' --data-binary \\\n" +
            "\t  '@/Users/asc/Desktop/aa.jpg' http://127.0.0.1:9955/decode\n\n" +
            "\tAbout to connect() to 127.0.0.1 port 9955\n" +
            "\t* Trying 127.0.0.1...\n" +
            "\t* connected\n" +
            "\t* Connected to 127.0.0.1 (127.0.0.1) port 9955\n" +
            "\t> POST /decode HTTP/1.1\n" +
            "\tUser-Agent: curl/7.13.1 (powerpc-apple-darwin8.0) libcurl/7.13.1 OpenSSL/0.9.7l zlib/1.2.3\n" +
            "\tHost: 127.0.0.1:9955\n" +
            "\tPragma: no-cache\n" +
            "\tAccept: */*\n" +
            "\tContent-Type: image/jpeg\n" +
            "\tContent-Length: 4930\n\n" +
            "\t< HTTP/1.1 200 OK\n" +
            "\t< Date: Fri, 22 Feb 2008 06:50:39 GMT\n" +
            "\t< Content-length: 28\n" +
            "\t* Connection #0 to host 127.0.0.1 left intact\n" +
            "\t* Closing connection #0\n\n" +
            "\thttp://aaronland.info/weblog\n\n" +
            "That's it. It will not make you a pony. No. No ponies for you.\n\n" +
            "------------------------------------------------------\n" +
            "ERRORS\n" +
            "------------------------------------------------------\n\n" +
            "Errors are returned with the HTTP status code 500. Specific error codes\n" +
            "and messages are returned both in the message body as XML and in the\n" +
            "'X-ErrorCode' and 'X-ErrorMessage' headers.\n\n" +
            "------------------------------------------------------\n" +
            "NOTES\n" +
            "------------------------------------------------------\n\n" +
            "By default, " + serverName + " runs on port " + serverPort + ". You can override\n" +
            "this value by specifying your own port number as the first\n" +
            "(command-line) argument when you start the server or by\n" +
            "setting the 'PORT' variable in the start.sh script.\n\n" +
            "The maximum size of a file that you may POST to " + serverName + "\n" +
            "is 512kb.\n\n" +
            "There is no logging to speak of.\n\n" +
            "------------------------------------------------------\n" +
            "SEE ALSO\n" +
            "------------------------------------------------------\n\n" +
            "http://code.google.com/p/zxing/\n" +
            "http://javablog.co.uk/2007/10/27/http-server-api-backport-to-java-5/\n" +
            "http://www.aaronland.info/weblog/2008/02/05/fox#ws-decode\n" +
            "http://www.aaronland.info/papernet/\n" +
            "\n" + 
            "------------------------------------------------------\n" +
            "VERSION\n" +
            "------------------------------------------------------\n\n" +
            serverVersion + "\n\n" +
            "------------------------------------------------------\n" +
            "LICENSE\n" +
            "------------------------------------------------------\n\n" +
            "Copyright (c) 2008 Aaron Straup Cope. All Rights Reserved.\n\n" +
            "This is free software. You may redistribute it and/or modify it\n" +
            "under the same terms as the GPL license.\n\n" + 
            "\n";

        int status = 200;
        send_response(t, status, msg);
    }

    //

    private static void decode (HttpExchange t) throws IOException {

        if (! t.getRequestMethod().equals ("POST")) {
            int errcode = 100;
            String errmsg = "Method Not Allowed";
            send_error(t, errcode, errmsg);
            return;
        }

        // i has an image?

        Headers headers = t.getRequestHeaders();
        String ctype = headers.getFirst("Content-Type");

        String [] parts = ctype.split("/");

        if (! parts[0].equals("image")) {
            int errcode = 200;
            String errmsg = "Invalid content-type";
            send_error(t, errcode, errmsg);
            return;
        }

        String ext;
        String fmt;

        if (parts[1].equals("png")) {
            ext = ".png";
            fmt = "png";
        }

        else if ((parts[1].equals("jpg") || (parts[1].equals("jpeg")))) {
            ext = ".jpg";
            fmt = "jpeg";
        }

        else {
            int errcode = 210;
            String errmsg = "Invalid image format";
            send_error(t, errcode, errmsg);
            return;
        }

        // final sanity check on size
        
        InputStream is = t.getRequestBody();        
        int sz = is.available();   

        if (sz > maxPostSize){
            is.close();

            int errcode = 220;
            String errmsg = "File too large";
            send_error(t, errcode, errmsg);
            return;            
        }

        // write tempfile

        BufferedImage im = ImageIO.read(is);
        is.close();
        
        File tmpfile = File.createTempFile("qrcode", ext);
        ImageIO.write(im, fmt, tmpfile);                 

        BufferedImage image = ImageIO.read(tmpfile);
        MonochromeBitmapSource source = new BufferedImageMonochromeBitmapSource(image);
        
        // System.out.println ("unlink tmpfile " + tmpfile);   
        tmpfile.delete();

        //

        String msg;
        
        try {
            Result result = barcodeReader.decode(source);           
            msg = result.getText();
        }
        
        catch (com.google.zxing.ReaderException e) {
            int errcode = 300;
            String errmsg = e.toString();
            send_error(t, errcode, errmsg);
            return;
        }
        
        catch ( Exception e) {
            int errcode = 310;
            String errmsg = e.toString();
            send_error(t, errcode, errmsg);
            return;
        }
        
        //
        
        int status = 200;
        send_response(t, status, msg);
    }

    private static void send_error (HttpExchange t, int errcode,  String errmsg) throws IOException {

        String rsp = 
            "<?xml version=\"1.0\" ?>\n" + 
            "<error code=\"" + errcode + "\">" + errmsg + "</error>";

        Headers headers = t.getResponseHeaders();
        headers.set("X-ErrorCode", String.valueOf(errcode));
        headers.set("X-ErrorMessage", errmsg);
        headers.set("Content-Type", "text/xml");

        int status = 500;
        send_response(t, status, rsp);        
        return;
    }

    private static void send_response (HttpExchange t, int status, String msg) throws IOException {

        Headers headers = t.getResponseHeaders();
        headers.set("X-Ws-Decode-Version", String.valueOf(serverVersion));

        OutputStream os = t.getResponseBody();
        t.sendResponseHeaders (status, msg.length());
        os.write(msg.getBytes());
        t.close();
    }

}