/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pmovil.jsonapi;

import com.codename1.io.FileSystemStorage;
import com.codename1.io.JSONParser;
import com.codename1.io.Log;
import com.codename1.io.Storage;
import com.codename1.io.Util;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;
import org.bouncycastle.crypto.digests.MD5Digest;

public class ConnectionRequest extends com.codename1.io.ConnectionRequest {

    private JsonSchemaListener listener;
    private boolean hasLastModifiedHeader = false;
    private String md5Url = "";
    private String cacheFileName = "";
    private Map<String, Object> response;
    
    private String responseContentType;
    private Exception responseException;
    
    private static final HashMap<String, String> cache = new HashMap<>();    
    
    private boolean multipart = false;
    
    
    //Copy pasted from MultipartRequest
    private String boundary;
    private final Hashtable args = new Hashtable();
    private final Hashtable filenames = new Hashtable();
    private final Hashtable filesizes = new Hashtable();
    private final Hashtable mimeTypes = new Hashtable();
    private static final String CRLF = "\r\n";
    private long contentLength = -1L;
    private final Vector ignoreEncoding = new Vector();
    
    
    protected final static char[] hexArray = "0123456789ABCDEF".toCharArray();


    public ConnectionRequest() {
        super();
        this.setPost(false);
        this.setWriteRequest(false);
        this.setHttpMethod("GET");
    }
    
    public void setListener(JsonSchemaListener listener){
        this.listener = listener;
    }
    

    public void setMultipart(boolean multipart) {
        this.multipart = multipart;
        if (multipart) {
            this.setPost(true);
            this.setWriteRequest(true);
        } else {
            this.setPost(false);
            this.setWriteRequest(false);
        }
    }

    public boolean getMultipart() {
        return this.multipart;
    }

    @Override
    protected void initConnection(Object connection) {
        Log.p("[ConnectionRequest] Initialize connection");
        md5Url = md5(getUrl());
        cacheFileName = "cache-" + md5Url;
        if (cache.containsKey(md5Url) && Storage.getInstance().exists(cacheFileName)) {
            Log.p("[ConnectionRequest] There is a Last-Modified header for this URL" + getUrl() + ", setting If-Modified-Since with " + cache.get(md5Url), Log.DEBUG);
            addRequestHeader("If-Modified-Since", cache.get(md5Url));
        }
        if (multipart) {
            setPost(true);
            setWriteRequest(true);
            boundary = Long.toString(System.currentTimeMillis(), 16);
            setContentType("multipart/form-data; boundary=" + boundary);
            contentLength = calculateContentLength();
            addRequestHeader("Content-Length", Long.toString(contentLength));
        }
        super.initConnection(connection);
    }

    @Override
    protected void readHeaders(Object connection) throws IOException {
        Log.p("[ConnectionRequest] Reading headers");
        String lastModifiedHeader = getHeader(connection, "Last-Modified");
        responseContentType = getHeader(connection, "Content-Type");
        if (lastModifiedHeader != null) {
            hasLastModifiedHeader = true;
            cache.put(md5Url, lastModifiedHeader);
            Log.p("[ConnectionRequest] Saving last-modified header " + lastModifiedHeader + " for " + getUrl(), Log.DEBUG);
        } else {
            hasLastModifiedHeader = false;
        }
    }

    @Override
    protected void handleErrorResponseCode(int code, final String message) {
        super.handleErrorResponseCode(code, message);
        Log.p("[ConnectionRequest] Error response code from API: " + code);
        setReadResponseForErrors(true);
    }

    @Override
    protected void handleException(Exception err) {
        if (listener != null) {
            listener.onException(this, err);
        }
    }

    @Override
    protected void readResponse(InputStream input) throws IOException {

        try {
            //Saving on storage
            if (this.hasLastModifiedHeader && getResponseCode() != 304) {
                if (Storage.getInstance().exists(cacheFileName)) {
                    Storage.getInstance().deleteStorageFile(cacheFileName);
                }
                Log.p("[ConnectionRequest] Saving cached content on file " + cacheFileName);
                Util.copy(input, Storage.getInstance().createOutputStream(cacheFileName), 1024);
                input = Storage.getInstance().createInputStream(cacheFileName);
            } else if (getResponseCode() == 304) {
                //Reading from Storage
                Log.p("[ConnectionRequest] Getting cached content from file " + cacheFileName);
                input = Storage.getInstance().createInputStream(cacheFileName);
            }

            Log.p("[ConnectionRequest] Code: " + getResponseCode());
            
            if(responseContentType.indexOf("application/json") >= 0){
                JSONParser parser = new JSONParser();
                response = parser.parseJSON(new InputStreamReader(input));
                Log.p("[ConnectionRequest] Request: " + getUrl());
                Log.p(response.toString());
            } else {
                super.readResponse(input);
                responseException = new UnexpectedContentException("Content type is not application/json, probably an error");
            }

        } catch (final IOException ex) {
            responseException = ex;
        }

    }

    @Override
    protected void postResponse() {
        Log.p("[ConnectionRequest] Finished request - starting EDT actions");
        if (listener != null) {
            if(responseException == null){
                listener.onSuccess(response, getResponseCode());
            } else {
                listener.onException(this, responseException);
            }
        }
    }

    /**
     * Returns the boundary string which is normally generated based on system
     * time
     *
     * @return the multipart boundary string
     */
    public String getBoundary() {
        return boundary;
    }

    /**
     * Sets the boundary string, normally you don't need this method. Its useful
     * to workaround server issues only. Notice that this method must be invoked
     * before adding any elements.
     *
     * @param boundary the boundary string
     */
    public void setBoundary(String boundary) {
        this.boundary = boundary;
        setContentType("multipart/form-data; boundary=" + boundary);
    }

    /**
     * Adds a binary argument to the arguments
     *
     * @param name the name of the data
     * @param data the data as bytes
     * @param mimeType the mime type for the content
     */
    public void addData(String name, byte[] data, String mimeType) {
        args.put(name, data);
        mimeTypes.put(name, mimeType);
        if (!filenames.containsKey(name)) {
            filenames.put(name, name);
        }
        filesizes.put(name, String.valueOf(data.length));
    }

    /**
     * Adds a binary argument to the arguments
     *
     * @param name the name of the file data
     * @param filePath the path of the file to upload
     * @param mimeType the mime type for the content
     * @throws IOException if the file cannot be opened
     */
    public void addData(String name, String filePath, String mimeType) throws IOException {
        addData(name, FileSystemStorage.getInstance().openInputStream(filePath),
                FileSystemStorage.getInstance().getLength(filePath), mimeType);
    }

    /**
     * Adds a binary argument to the arguments, notice the input stream will be
     * read only during submission
     *
     * @param name the name of the data
     * @param data the data stream
     * @param dataSize the byte size of the data stream, if the data stream is a
     * file the file size can be obtained using the
     * FileSystemStorage.getInstance().getLength(file) method
     * @param mimeType the mime type for the content
     */
    public void addData(String name, InputStream data, long dataSize, String mimeType) {
        args.put(name, data);
        if (!filenames.containsKey(name)) {
            filenames.put(name, name);
        }
        filesizes.put(name, String.valueOf(dataSize));
        mimeTypes.put(name, mimeType);
    }

    /**
     * Sets the filename for the given argument
     *
     * @param arg the argument name
     * @param filename the file name
     */
    public void setFilename(String arg, String filename) {
        filenames.put(arg, filename);
    }

    /**
     * @inheritDoc
     */
    @Override
    public void addArgumentNoEncoding(String key, String value) {
        args.put(key, value);
        if (!filenames.containsKey(key)) {
            filenames.put(key, key);
        }
        ignoreEncoding.addElement(key);
    }

    /**
     * @inheritDoc
     */
    @Override
    public void addArgument(String key, String value) {
        if (isPost()) {
            args.put(key, value);
            if (!filenames.containsKey(key)) {
                filenames.put(key, key);
            }
        } else {
            super.addArgument(key, value);
        }
    }

    protected long calculateContentLength() {
        long length = 0L;
        Enumeration e = args.keys();

        long dLength = "Content-Disposition: form-data; name=\"\"; filename=\"\"".length() + 2; // 2 = CRLF
        long ctLength = "Content-Type: ".length() + 2; // 2 = CRLF
        long cteLength = "Content-Transfer-Encoding: binary".length() + 4; // 4 = 2 * CRLF
        long bLength = boundary.length() + 4; // -- + boundary + CRLF
        long baseBinaryLength = dLength + ctLength + cteLength + bLength + 2; // 2 = CRLF at end of part 
        dLength = "Content-Disposition: form-data; name=\"\"".length() + 2;  // 2 = CRLF
        ctLength = "Content-Type: text/plain; charset=UTF-8".length() + 4; // 4 = 2 * CRLF
        long baseTextLength = dLength + ctLength + bLength + 2;  // 2 = CRLF at end of part

        while (e.hasMoreElements()) {
            String key = (String) e.nextElement();
            Object value = args.get(key);
            if (value instanceof String) {
                length += baseTextLength;
                length += key.length();
                if (ignoreEncoding.contains(key)) {
                    try {
                        length += value.toString().getBytes("UTF-8").length;
                    } catch (UnsupportedEncodingException ex) {
                        length += value.toString().getBytes().length;
                    }
                } else {
                    length += Util.encodeBody((String) value).length();
                }
            } else {
                length += baseBinaryLength;
                length += key.length();
                length += ((String) filenames.get(key)).length();
                length += ((String) mimeTypes.get(key)).length();
                length += Long.parseLong((String) filesizes.get(key));
            }
        }
        length += bLength + 2; // same as part boundaries, suffixed with: --
        return length;
    }

    /**
     * @inheritDoc
     */
    @Override
    protected void buildRequestBody(OutputStream os) throws IOException {
        if (multipart) {
            Writer writer = new OutputStreamWriter(os, "UTF-8");
            Enumeration e = args.keys();
            while (e.hasMoreElements()) {
                if (shouldStop()) {
                    break;
                }
                String key = (String) e.nextElement();
                Object value = args.get(key);

                writer.write("--");
                writer.write(boundary);
                writer.write(CRLF);
                if (value instanceof String) {
                    writer.write("Content-Disposition: form-data; name=\"");
                    writer.write(key);
                    writer.write("\"");
                    writer.write(CRLF);
                    writer.write("Content-Type: text/plain; charset=UTF-8");
                    writer.write(CRLF);
                    writer.write(CRLF);
                    writer.flush();
                    if (ignoreEncoding.contains(key)) {
                        writer.write((String) value);
                    } else {
                        writer.write(Util.encodeBody((String) value));
                    }
                    //writer.write(CRLF);
                    writer.flush();
                } else {
                    writer.write("Content-Disposition: form-data; name=\"" + key + "\"; filename=\"" + filenames.get(key) + "\"");
                    writer.write(CRLF);
                    writer.write("Content-Type: ");
                    writer.write((String) mimeTypes.get(key));
                    writer.write(CRLF);
                    writer.write("Content-Transfer-Encoding: binary");
                    writer.write(CRLF);
                    writer.write(CRLF);
                    writer.flush();
                    InputStream i;
                    if (value instanceof InputStream) {
                        i = (InputStream) value;
                    } else {
                        i = new ByteArrayInputStream((byte[]) value);
                    }
                    byte[] buffer = new byte[8192];
                    int s = i.read(buffer);
                    while (s > -1) {
                        if (shouldStop()) {
                            break;
                        }
                        os.write(buffer, 0, s);
                        writer.flush();
                        s = i.read(buffer);
                    }
                    // (when passed by stream, leave for caller to clean up).
                    if (!(value instanceof InputStream)) {
                        Util.cleanup(i);
                    }
                    args.remove(key);
                    writer.flush();
                }
                writer.write(CRLF);
                writer.flush();
            }

            writer.write("--" + boundary + "--");
            writer.write(CRLF);
            writer.close();
        } else {
            super.buildRequestBody(os);
        }
    }

    /* (non-Javadoc)
     * @see com.codename1.io.ConnectionRequest#getContentLength()
     */
    @Override
    public int getContentLength() {
        return (int) contentLength;
    }
    
    public static String md5(String text) {
        MD5Digest digest = new MD5Digest();
        byte[] data = text.getBytes();
        digest.update(data, 0, data.length);
        byte[] md5 = new byte[digest.getDigestSize()];
        digest.doFinal(md5, 0);
        return bytesToHex(md5);
    }
    
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xFF;
            hexChars[i * 2] = hexArray[v >>> 4];
            hexChars[i * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
    
    public static interface JsonSchemaListener {
        
        public void onSuccess(Map<String, Object> response, int code);
        public void onException(ConnectionRequest sender, Exception ex);
        
    }

}
