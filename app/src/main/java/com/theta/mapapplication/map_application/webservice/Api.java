package com.theta.mapapplication.map_application.webservice;

/**
 * Created by ashish on 19/12/17.
 */

public class Api {
    //Dump Condition
    public static final int ConnectionTimeout = 240000; // = 240 seconds
    public static final int ConnectionSoTimeout = 60000; // = 60 seconds


    //Response Codes
    public static final int ResponseOk = 200;
    public static final int ResponseCreated = 201;
    public static final int ResponseNoContent = 204;
    public static final int ResponsePageError = 400;
    public static final int ResponseUnauthorized = 401;
    public static final int ResponseServerError = 500;
    /*Local Url and port for socket*/
    public static final String SocketMainUrl = "http://dev.thetatechnolabs.com:3002";
    //public static final String SocketMainPort = "3002";

    public static final String SocketActionGetKey = "get_geolocation_";
    public static final int SocketDriverID = 99;
}
