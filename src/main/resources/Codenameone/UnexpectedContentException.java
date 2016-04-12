/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pmovil.jsonapi;

/**
 *
 * @author ivan
 */
public class UnexpectedContentException extends Exception {
    public UnexpectedContentException(String message){
        super(message);
    }
}
