package com.payapp.uttrakhandtransportcorporation.Model;

import com.payu.india.Payu.PayuConstants;

/**
 * Created by rahul.hooda on 5/2/16.
 */

public class Constants {

    public static String SURL = "http://utc.payu.in/utc_response.php";
    public static String FURL = "http://utc.payu.in/utc_response.php";
    public static int ENV = PayuConstants.MOBILE_STAGING_ENV;
    public static String MERCHANT_KEY = "gtKFFx";
    public static String SALT = "eCwWELxi";
    public static String PRODUCT_INFO = "productinfo";
    public static String FIRST_NAME = "Uttrakhand Transaport Corporation";
    public static String EMAIL = "contact@utc.gov.in";
    public static String OFFER_KEY = "";
    public static boolean smsPermission = true;
    public static int storeOneClickHash = PayuConstants.STORE_ONE_CLICK_HASH_SERVER;
    public static String sms_Hash;
    public static String d_number;
    //public static String send_sms_url= "info.payu.in/postservice.php?";

}
