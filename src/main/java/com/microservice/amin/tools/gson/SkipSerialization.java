/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.microservice.amin.tools.gson;


import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Los campos anotados no se van a serializar como json
 */
@Target(ElementType.FIELD)
public @interface SkipSerialization {

}