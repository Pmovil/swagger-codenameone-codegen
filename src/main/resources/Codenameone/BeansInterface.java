/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pmovil.jsonapi;

import java.util.Map;

/**
 *
 * @author ivan
 */
public interface BeansInterface {
    
    public void update(Map<String, Object> result, boolean cached) throws UnexpectedJsonException;
}
