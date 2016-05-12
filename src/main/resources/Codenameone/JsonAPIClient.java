package com.pmovil.jsonapi;

import com.codename1.components.SliderBridge;
import com.codename1.io.FileSystemStorage;
import com.codename1.io.Log;
import com.codename1.io.NetworkManager;
import com.codename1.io.Util;
import com.codename1.ui.Display;
import com.codename1.ui.Slider;
import java.io.ByteArrayInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class JsonAPIClient {

    private final String url;
    public static int defaultTimeout = 30000;

    public JsonAPIClient(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

        
    public ConnectionRequest prepare(String action, String path, Slider progress, List<Pair> queryParams, final HashMap<String, ArrayList<String>> postFiles, boolean duplicate, boolean failSilently, int timeout, byte priority) throws IOException {
        final ConnectionRequest connectionRequest = new ConnectionRequest();

        Log.p("[JsonAPIClient] preparing " + action + " on " + path);

        String params = "";
        Iterator<Pair> queryParamsIterator = queryParams.iterator();
        if (queryParamsIterator.hasNext()) {
            params = "?";
        }
        while (queryParamsIterator.hasNext()) {
            Pair param = queryParamsIterator.next();
            params += Util.encodeUrl(param.getName()) + "=" + Util.encodeUrl(param.getValue());
            if (queryParamsIterator.hasNext()) {
                params += "&";
            }
        }
        connectionRequest.setUrl(url + path + params);
        boolean shouldWrite = postFiles != null && !postFiles.isEmpty();
        connectionRequest.setPost(shouldWrite);
        connectionRequest.setMultipart(shouldWrite);
        connectionRequest.setHttpMethod(action);
        connectionRequest.setTimeout(timeout);
        connectionRequest.setPriority(priority);
        connectionRequest.setReadResponseForErrors(true);
        connectionRequest.setFailSilently(failSilently);
        connectionRequest.setDuplicateSupported(duplicate);

        if (progress != null) {
            SliderBridge.bindProgress(connectionRequest, progress);
        }

        if (connectionRequest.isPost()) {
            Log.p("[JsonAPIClient] Posting files");
            for (Entry<String, ArrayList<String>> entrySet : postFiles.entrySet()) {
                if (entrySet.getValue().get(0).equals("application/json")) {
                    connectionRequest.addArgument(entrySet.getKey(), entrySet.getValue().get(1), entrySet.getValue().get(0));
                } else {
                    InputStream is;
                    long length = FileSystemStorage.getInstance().getLength(entrySet.getValue().get(1));
                    if (length <= 0) {
                        // no file length available, lets try an alternate method
                        InputStream aux = FileSystemStorage.getInstance().openInputStream(entrySet.getValue().get(1));
                        long buf = 0;
                        byte[] buftemp = new byte[8192];
                        for (length = 0; buf > -1; length += buf) {
                            buf = aux.read(buftemp);
                        }
                        Util.cleanup(aux);
                        if (!Display.getInstance().getPlatformName().equals("win")) {
                            System.gc();
                            System.gc();
                        }
                        is = FileSystemStorage.getInstance().openInputStream(entrySet.getValue().get(1));
                        connectionRequest.addData((String) entrySet.getKey(), is, length, entrySet.getValue().get(0));
                    } else {
                        is = FileSystemStorage.getInstance().openInputStream(entrySet.getValue().get(1));
                        connectionRequest.addData((String) entrySet.getKey(), is, length, entrySet.getValue().get(0));
                    }
                }
            }
        }
        return connectionRequest;
    }

    public void send(final ConnectionRequest connectionRequest, final BeanListener listener, final ArrayList<BeansInterface> beans) {

        connectionRequest.setListener(new ConnectionRequest.JsonSchemaListener() {
            @Override
            public void onSuccess(final Map<String, Object> response, int code) {
                try {
                    if (code == 200 || code == 304) {
                        if (beans.size() == 2) {
                            beans.get(1).update((HashMap) response, code == 304);
                            listener.onSuccess(beans.get(1), code == 304);
//                        } else if (bean.length > 2) {
//                            ArrayBean arrayBean = new ArrayBean();
//                            arrayBean.update(response, code == 304);
//                            listener.onSuccess(arrayBean, code == 304);
                        } else {
                            DummyBean dummyBean = new DummyBean();
                            dummyBean.update(response, code == 304);
                            listener.onSuccess(dummyBean, code == 304);
                        }
                    } else {
                        beans.get(0).update((HashMap)response, false);
                        listener.onErrorResponse(beans.get(0), code);
                    }
                } catch (UnexpectedJsonException e) {
                    Log.p("[JsonAPIClient] Unespected API response: " + response, Log.ERROR);
                    Log.e(e);
                    listener.onMalformedResponse(connectionRequest, e);
                }
            }

            @Override
            public void onException(final ConnectionRequest sender, final Exception ex) {
                Display.getInstance().callSerially(() -> {
                    listener.onNetworkException(sender, ex);
                });
            }
        });

        Log.p("[JsonAPIClient] Adding to " + connectionRequest.getUrl() + " queue");
        NetworkManager.getInstance().addToQueue(connectionRequest);


    }

    public static interface BeanListener {
        public void onSuccess(BeansInterface bean, boolean cached);
        public void onErrorResponse(BeansInterface errorBean, int errorCode);
        public void onMalformedResponse(com.codename1.io.ConnectionRequest sender, UnexpectedJsonException err);
        public void onNetworkException(com.codename1.io.ConnectionRequest sender, Throwable err);
    }
}
