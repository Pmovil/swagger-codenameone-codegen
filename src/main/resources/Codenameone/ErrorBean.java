package com.pmovil.jsonapi;

import java.util.Map;

/**
 *
 * @author Net
 */
public class ErrorBean implements BeansInterface {

    public enum Status {
        GENERAL_ERROR
    };
    
    protected String status = "";
    protected String error = "";


    public void update(Map<String, Object> result, boolean cached) throws UnexpectedJsonException {
        status = (String)result.get("status");
        error = (String)result.get("error");

    }

    public String getStatus() {
        return status;
    }
    
    public boolean is(Status s) {
        return status.equals(s.toString());
    }
    
    public String getError() {
        return error;
    }

}
