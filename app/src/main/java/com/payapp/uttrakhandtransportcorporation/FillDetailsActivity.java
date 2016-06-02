package com.payapp.uttrakhandtransportcorporation;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.payapp.uttrakhandtransportcorporation.Model.Constants;
import com.payu.india.CallBackHandler.OnetapCallback;
import com.payu.india.Extras.PayUChecksum;
import com.payu.india.Interfaces.OneClickPaymentListener;
import com.payu.india.Model.PaymentParams;
import com.payu.india.Model.PayuConfig;
import com.payu.india.Model.PayuHashes;
import com.payu.india.Model.PostData;
import com.payu.india.Payu.Payu;
import com.payu.india.Payu.PayuConstants;
import com.payu.india.Payu.PayuErrors;
import com.payu.payuui.Activity.PayUBaseActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Pattern;

public class FillDetailsActivity extends AppCompatActivity implements OneClickPaymentListener {

    private TextInputLayout mInputLayoutEmail, mInputLayoutYourMobile, mInputLayoutDriverMobile, mInputLayoutAmount;
    private EditText mEditTextEmail, mEditTextYourMobile, mEditTextDriverMobile, mEditTextAmount;
    private Button mBtnPayNow;
    private PaymentParams mPaymentParams;
    private PayuConfig payuConfig;
    private PayUChecksum checksum;
    private PostData postData;
    private String TxnId = "" + System.currentTimeMillis();
    private Intent intent;
    private String var1;
    private String TAG = "FillDetailsActivity";
    private String send_sms_hash;

    private final int MY_PERMISSIONS_REQUEST_GET_ACCOUNTS = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fill_details);

        //This callback is set for using the One Tap Payment feature
        OnetapCallback.setOneTapCallback(this);

        //This initializes the PayU Configurations and instances
        Payu.setInstance(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mInputLayoutEmail = (TextInputLayout) findViewById(R.id.input_layout_email);
        mInputLayoutYourMobile = (TextInputLayout) findViewById(R.id.input_layout_your_mobile);
        mInputLayoutDriverMobile = (TextInputLayout) findViewById(R.id.input_layout_driver_mobile);
        mInputLayoutAmount = (TextInputLayout) findViewById(R.id.input_layout_amount);
        mEditTextEmail = (EditText) findViewById(R.id.edittext_email);
        mEditTextYourMobile = (EditText) findViewById(R.id.edittext_your_mobile);
        mEditTextDriverMobile = (EditText) findViewById(R.id.edittext_driver_mobile);
        mEditTextAmount = (EditText) findViewById(R.id.edittext_amount);
        mBtnPayNow = (Button) findViewById(R.id.btn_pay_now);
        mBtnPayNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validInformation())
                    preparePaymentFlow();
            }
        });

        mEditTextEmail.addTextChangedListener(new MyTextWatcher(mEditTextEmail));
        mEditTextYourMobile.addTextChangedListener(new MyTextWatcher(mEditTextYourMobile));
        mEditTextDriverMobile.addTextChangedListener(new MyTextWatcher(mEditTextDriverMobile));
        mEditTextAmount.addTextChangedListener(new MyTextWatcher(mEditTextAmount));

        // Here, this is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.GET_ACCOUNTS)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.GET_ACCOUNTS)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.GET_ACCOUNTS},
                        MY_PERMISSIONS_REQUEST_GET_ACCOUNTS);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            //sets the primary email
            mEditTextEmail.setText(getPrimaryEmailAddress());
        }
    }

    /**
     * This function is used to get primary email on device
     *
     * @return
     */
    public String getPrimaryEmailAddress() {
        Pattern emailPattern = Patterns.EMAIL_ADDRESS; // API level 8+
        Account[] accounts = AccountManager.get(this).getAccounts();
        for (Account account : accounts) {
            if (emailPattern.matcher(account.name).matches()) {
                return account.name;
            }
        }
        return "";
    }

    private class MyTextWatcher implements TextWatcher {

        private View view;

        private MyTextWatcher(View view) {
            this.view = view;
        }

        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            Log.d(TAG, "onTextChanged() Called...");
        }

        public void afterTextChanged(Editable editable) {
            Log.d(TAG, "afterTextChanged() Called...");
            switch (view.getId()) {
                case R.id.edittext_email:
                    validateEmail();
                    break;
                case R.id.edittext_your_mobile:
                    validatePhoneNumber(mEditTextYourMobile, mInputLayoutYourMobile);
                    break;
                case R.id.edittext_driver_mobile:
                    validatePhoneNumber(mEditTextDriverMobile, mInputLayoutDriverMobile);
                    break;
                case R.id.edittext_amount:
                    validateAmount();
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * This function validates email address.
     *
     * @return a boolean value, true if email is valid else false.
     */
    private boolean validateEmail() {
        String email = mEditTextEmail.getText().toString().trim();

        if (email.isEmpty() || !isValidEmail(email)) {
            mInputLayoutEmail.setError(getString(R.string.error_valid_email));
            requestFocus(mEditTextEmail);
            return false;
        } else {
            //mInputLayoutEmail.setError(null);
            mInputLayoutEmail.setErrorEnabled(false);
            mInputLayoutEmail.setError(null);
        }

        return true;
    }

    /**
     * This function validates phone number.
     *
     * @return a boolean value, true if phone number is valid else false.
     */
    private boolean validatePhoneNumber(EditText editText, TextInputLayout inputLayout) {
        String phoneNumber = editText.getText().toString().trim();

        if (phoneNumber.isEmpty() || !isValidPhone(phoneNumber)) {
            inputLayout.setError(getString(R.string.error_valid_phone_number));
            requestFocus(editText);
            return false;
        } else {
            inputLayout.setErrorEnabled(false);
            inputLayout.setError(null);
        }
        return true;
    }

    /**
     * This function checks if an email is valid by matching its pattern from android.util package.
     *
     * @return a boolean value, true if email is valid else false.
     */
    private static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /**
     * This function checks if an phone number is valid by matching its pattern from android.util package.
     *
     * @return a boolean value, true if phone number is valid else false.
     */
    private static boolean isValidPhone(String phoneNumber) {
        String regexStr = "^{6,9}+[0-9]{10,13}$";

        if (phoneNumber.matches(regexStr) == false) {
            return false;
        }
        return true;
    }

    /**
     * This function validates the amount
     */
    public boolean validateAmount() {
        if(!(mEditTextAmount.getText().toString().equals(""))) {
            int amount = Integer.parseInt(mEditTextAmount.getText().toString());
            if (mEditTextAmount.getText().toString().isEmpty() || amount < 0) {
                mInputLayoutAmount.setError(getString(R.string.error_valid_email));
                requestFocus(mEditTextAmount);
                return false;
            } else {
                mInputLayoutAmount.setErrorEnabled(false);
                mInputLayoutAmount.setError(null);
            }
            return true;
        }
        else
            return false;
    }

    /**
     * This function validate the filled data before processing for Payment
     */
    private boolean validInformation() {
        if (!validateEmail()) {
            Toast.makeText(FillDetailsActivity.this, getString(R.string.toast_invalid_email_address), Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!validatePhoneNumber(mEditTextYourMobile, mInputLayoutYourMobile)) {
            Toast.makeText(FillDetailsActivity.this, getString(R.string.toast_invalid_your_mobile_number), Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!validatePhoneNumber(mEditTextDriverMobile, mInputLayoutDriverMobile)) {
            Toast.makeText(FillDetailsActivity.this, getString(R.string.toast_invalid_driver_mobile_number), Toast.LENGTH_SHORT).show();
            return false;
        }
        if ((mEditTextYourMobile.getText().toString()).equals((mEditTextDriverMobile).getText().toString())) {
            Toast.makeText(FillDetailsActivity.this, getString(R.string.toast_both_numbers_cannot_be_same), Toast.LENGTH_SHORT).show();
            return false;
        }
        if (mEditTextAmount.getText().toString().isEmpty()) {
            Toast.makeText(FillDetailsActivity.this, getString(R.string.toast_amount_cannot_be_empty), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    /*
    * This method fetches the data from edit text and sends the data for hash generation
    * */
    public void preparePaymentFlow() {

        //Initialse the intent
        intent = new Intent(this, PayUBaseActivity.class);

        //get details from edit text
        String amount = mEditTextAmount.getText().toString();
        String customerEmail = mEditTextEmail.getText().toString();
        String yourMobile = mEditTextYourMobile.getText().toString();
        String driverMobile = mEditTextDriverMobile.getText().toString();
        Constants.d_number=driverMobile;

        //userCredentials to use store card feature
        String userCredentials = Constants.MERCHANT_KEY + ":" + yourMobile;
        var1 = userCredentials;

        //Initialize Payment Params and PayuConfig
        mPaymentParams = new PaymentParams();
        payuConfig = new PayuConfig();

        mPaymentParams.setKey(Constants.MERCHANT_KEY);
        mPaymentParams.setAmount(amount);
        mPaymentParams.setProductInfo(Constants.PRODUCT_INFO);
        mPaymentParams.setFirstName(Constants.FIRST_NAME);
        mPaymentParams.setEmail(Constants.EMAIL);
        mPaymentParams.setTxnId(TxnId);
        mPaymentParams.setSurl(Constants.SURL);
        mPaymentParams.setFurl(Constants.FURL);
        mPaymentParams.setUdf1(customerEmail);
        mPaymentParams.setUdf2(yourMobile);
        mPaymentParams.setUdf3(driverMobile);
        mPaymentParams.setUdf4("");
        mPaymentParams.setUdf5("");
        mPaymentParams.setUserCredentials(userCredentials);

        intent.putExtra(PayuConstants.SALT, Constants.SALT);
        intent.putExtra(PayuConstants.SMS_PERMISSION, Constants.smsPermission);

        //set the configuration environment
        payuConfig.setEnvironment(Constants.ENV);

        // generate hash from server
        // if (null == Constants.SALT)
        generateHashFromServer(mPaymentParams);
        // else
        //     generateHashFromSDK(mPaymentParams, intent.getStringExtra(PayuConstants.SALT));
    }

    /******************************
     * Server hash generation
     ********************************/
    // lets generate hashes from server
    public void generateHashFromServer(PaymentParams mPaymentParams) {
        // lets create the post params
        StringBuffer postParamsBuffer = new StringBuffer();
        postParamsBuffer.append(concatParams(PayuConstants.KEY, mPaymentParams.getKey()));
        postParamsBuffer.append(concatParams(PayuConstants.AMOUNT, mPaymentParams.getAmount()));
        postParamsBuffer.append(concatParams(PayuConstants.TXNID, mPaymentParams.getTxnId()));
        postParamsBuffer.append(concatParams(PayuConstants.EMAIL, null == mPaymentParams.getEmail() ? "" : mPaymentParams.getEmail()));
        postParamsBuffer.append(concatParams(PayuConstants.PRODUCT_INFO, mPaymentParams.getProductInfo()));
        postParamsBuffer.append(concatParams(PayuConstants.FIRST_NAME, null == mPaymentParams.getFirstName() ? "" : mPaymentParams.getFirstName()));
        postParamsBuffer.append(concatParams(PayuConstants.UDF1, mPaymentParams.getUdf1() == null ? "" : mPaymentParams.getUdf1()));
        postParamsBuffer.append(concatParams(PayuConstants.UDF2, mPaymentParams.getUdf2() == null ? "" : mPaymentParams.getUdf2()));
        postParamsBuffer.append(concatParams(PayuConstants.UDF3, mPaymentParams.getUdf3() == null ? "" : mPaymentParams.getUdf3()));
        postParamsBuffer.append(concatParams(PayuConstants.UDF4, mPaymentParams.getUdf4() == null ? "" : mPaymentParams.getUdf4()));
        postParamsBuffer.append(concatParams(PayuConstants.UDF5, mPaymentParams.getUdf5() == null ? "" : mPaymentParams.getUdf5()));
        postParamsBuffer.append(concatParams(PayuConstants.USER_CREDENTIALS, mPaymentParams.getUserCredentials() == null ? PayuConstants.DEFAULT : mPaymentParams.getUserCredentials()));

        // for offer_key
        if (null != mPaymentParams.getOfferKey())
            postParamsBuffer.append(concatParams(PayuConstants.OFFER_KEY, mPaymentParams.getOfferKey()));
        // for check_isDomestic
        //if(null != cardBin)
        //    postParamsBuffer.append(concatParams("card_bin", cardBin));

        String postParams = postParamsBuffer.charAt(postParamsBuffer.length() - 1) == '&' ? postParamsBuffer.substring(0, postParamsBuffer.length() - 1).toString() : postParamsBuffer.toString();
        // make api call
        GetHashesFromServerTask getHashesFromServerTask = new GetHashesFromServerTask();
        getHashesFromServerTask.execute(postParams);
    }

    /*
    * This function concatinate params into a form like, key=value&
    * */
    protected String concatParams(String key, String value) {
        return key + "=" + value + "&";
    }

    /**
     * An Async task to fetch hashes from Server
     */
    class GetHashesFromServerTask extends AsyncTask<String, String, PayuHashes> {

        @Override
        protected PayuHashes doInBackground(String... postParams) {
            PayuHashes payuHashes = new PayuHashes();
            try {

                URL url = new URL("");

                // get the payuConfig first
                String postParam = postParams[0];

                byte[] postParamsByte = postParam.getBytes("UTF-8");

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestProperty("Content-Length", String.valueOf(postParamsByte.length));
                conn.setDoOutput(true);
                conn.getOutputStream().write(postParamsByte);

                InputStream responseInputStream = conn.getInputStream();
                StringBuffer responseStringBuffer = new StringBuffer();
                byte[] byteContainer = new byte[1024];
                for (int i; (i = responseInputStream.read(byteContainer)) != -1; ) {
                    responseStringBuffer.append(new String(byteContainer, 0, i));
                }

                JSONObject response = new JSONObject(responseStringBuffer.toString());

                Iterator<String> payuHashIterator = response.keys();
                while (payuHashIterator.hasNext()) {
                    String key = payuHashIterator.next();
                    switch (key) {
                        case "payment_hash":
                            payuHashes.setPaymentHash(response.getString(key));
                            break;
                        case "get_merchant_ibibo_codes_hash": //
                            payuHashes.setMerchantIbiboCodesHash(response.getString(key));
                            break;
                        case "vas_for_mobile_sdk_hash":
                            payuHashes.setVasForMobileSdkHash(response.getString(key));
                            break;
                        case "payment_related_details_for_mobile_sdk_hash":
                            payuHashes.setPaymentRelatedDetailsForMobileSdkHash(response.getString(key));
                            break;
                        case "delete_user_card_hash":
                            payuHashes.setDeleteCardHash(response.getString(key));
                            break;
                        case "get_user_cards_hash":
                            payuHashes.setStoredCardsHash(response.getString(key));
                            break;
                        case "edit_user_card_hash":
                            payuHashes.setEditCardHash(response.getString(key));
                            break;
                        case "save_user_card_hash":
                            payuHashes.setSaveCardHash(response.getString(key));
                            break;
                        case "check_offer_status_hash":
                            payuHashes.setCheckOfferStatusHash(response.getString(key));
                            break;
                        case "check_isDomestic_hash":
                            payuHashes.setCheckIsDomesticHash(response.getString(key));
                            break;
                        case "send_sms_hash":
                            Constants.sms_Hash=response.getString("send_sms_hash");

                            break;
                        default:
                            break;
                    }
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return payuHashes;
        }

        @Override
        protected void onPostExecute(PayuHashes payuHashes) {
            super.onPostExecute(payuHashes);
            launchSdkUI(payuHashes);
        }
    }

    /**
     * This method launches the PayU Ui and initiates the Payment Flow
     *
     * @param payuHashes
     */
    public void launchSdkUI(PayuHashes payuHashes) {

        intent.putExtra(PayuConstants.PAYU_CONFIG, payuConfig);
        intent.putExtra(PayuConstants.PAYMENT_PARAMS, mPaymentParams);
        intent.putExtra(PayuConstants.PAYU_HASHES, payuHashes);

        /**
         *  just for testing, dont do this in production.
         *  i need to generate hash for {@link com.payu.india.Tasks.GetTransactionInfoTask} since it requires transaction id, i don't generate hash from my server
         *  merchant should generate the hash from his server.
         *
         */
        intent.putExtra(PayuConstants.SALT, Constants.SALT);
        intent.putExtra(PayuConstants.STORE_ONE_CLICK_HASH, Constants.storeOneClickHash);

        if (Constants.storeOneClickHash == PayuConstants.STORE_ONE_CLICK_HASH_SERVER)
            fetchMerchantHashes(intent);
        else
            startActivityForResult(intent, PayuConstants.PAYU_REQUEST_CODE);
    }

    /******************************
     * Client hash generation
     ***********************************/
    // Do not use this, you may use this only for testing.
    // lets generate hashes.
    // This should be done from server side..
    // Do not keep salt anywhere in app.
    public void generateHashFromSDK(PaymentParams mPaymentParams, String Salt) {
        PayuHashes payuHashes = new PayuHashes();
        postData = new PostData();

        // payment Hash;
        checksum = null;
        checksum = new PayUChecksum();
        checksum.setAmount(mPaymentParams.getAmount());
        checksum.setKey(mPaymentParams.getKey());
        checksum.setTxnid(mPaymentParams.getTxnId());
        checksum.setEmail(mPaymentParams.getEmail());
        checksum.setSalt(Constants.SALT);
        checksum.setProductinfo(mPaymentParams.getProductInfo());
        checksum.setFirstname(mPaymentParams.getFirstName());
        checksum.setUdf1(mPaymentParams.getUdf1());
        checksum.setUdf2(mPaymentParams.getUdf2());
        checksum.setUdf3(mPaymentParams.getUdf3());
        checksum.setUdf4(mPaymentParams.getUdf4());
        checksum.setUdf5(mPaymentParams.getUdf5());

        postData = checksum.getHash();
        if (postData.getCode() == PayuErrors.NO_ERROR) {
            payuHashes.setPaymentHash(postData.getResult());
        }

        // checksum for payemnt related details
        // var1 should be either user credentials or default
        var1 = var1 == null ? PayuConstants.DEFAULT : var1;

        if ((postData = calculateHash(Constants.MERCHANT_KEY, PayuConstants.PAYMENT_RELATED_DETAILS_FOR_MOBILE_SDK, var1, Constants.SALT)) != null && postData.getCode() == PayuErrors.NO_ERROR) // Assign post data first then check for success
            payuHashes.setPaymentRelatedDetailsForMobileSdkHash(postData.getResult());
        //vas
        if ((postData = calculateHash(Constants.MERCHANT_KEY, PayuConstants.VAS_FOR_MOBILE_SDK, PayuConstants.DEFAULT, Constants.SALT)) != null && postData.getCode() == PayuErrors.NO_ERROR)
            payuHashes.setVasForMobileSdkHash(postData.getResult());

        // getIbibocodes
        if ((postData = calculateHash(Constants.MERCHANT_KEY, PayuConstants.GET_MERCHANT_IBIBO_CODES, PayuConstants.DEFAULT, Constants.SALT)) != null && postData.getCode() == PayuErrors.NO_ERROR)
            payuHashes.setMerchantIbiboCodesHash(postData.getResult());

        if (!var1.contentEquals(PayuConstants.DEFAULT)) {
            // get user card
            if ((postData = calculateHash(Constants.MERCHANT_KEY, PayuConstants.GET_USER_CARDS, var1, Constants.SALT)) != null && postData.getCode() == PayuErrors.NO_ERROR) // todo rename storedc ard
                payuHashes.setStoredCardsHash(postData.getResult());
            // save user card
            if ((postData = calculateHash(Constants.MERCHANT_KEY, PayuConstants.SAVE_USER_CARD, var1, Constants.SALT)) != null && postData.getCode() == PayuErrors.NO_ERROR)
                payuHashes.setSaveCardHash(postData.getResult());
            // delete user card
            if ((postData = calculateHash(Constants.MERCHANT_KEY, PayuConstants.DELETE_USER_CARD, var1, Constants.SALT)) != null && postData.getCode() == PayuErrors.NO_ERROR)
                payuHashes.setDeleteCardHash(postData.getResult());
            // edit user card
            if ((postData = calculateHash(Constants.MERCHANT_KEY, PayuConstants.EDIT_USER_CARD, var1, Constants.SALT)) != null && postData.getCode() == PayuErrors.NO_ERROR)
                payuHashes.setEditCardHash(postData.getResult());
        }

        if (mPaymentParams.getOfferKey() != null) {
            postData = calculateHash(Constants.MERCHANT_KEY, PayuConstants.OFFER_KEY, mPaymentParams.getOfferKey(), Constants.SALT);
            if (postData.getCode() == PayuErrors.NO_ERROR) {
                payuHashes.setCheckOfferStatusHash(postData.getResult());
            }
        }

        if (mPaymentParams.getOfferKey() != null && (postData = calculateHash(Constants.MERCHANT_KEY, PayuConstants.CHECK_OFFER_STATUS, mPaymentParams.getOfferKey(), Constants.SALT)) != null && postData.getCode() == PayuErrors.NO_ERROR) {
            payuHashes.setCheckOfferStatusHash(postData.getResult());
        }

        // we have generated all the hases now lest launch sdk's ui
        launchSdkUI(payuHashes);
    }

    // deprecated, should be used only for testing.
    private PostData calculateHash(String key, String command, String var1, String salt) {
        checksum = null;
        checksum = new PayUChecksum();
        checksum.setKey(key);
        checksum.setCommand(command);
        checksum.setVar1(var1);
        checksum.setSalt(salt);
        return checksum.getHash();
    }

    /**
     * This function stores the merchant hash and card token received after making a successful transaction
     * with a card opting it for One Tap Card in future payments. These card token and merchant hash are stored
     * on the merchant server.
     *
     * @param cardToken    card token received in transaction response
     * @param merchantHash merchant hash received in transaction response
     */
    private void storeMerchantHash(String cardToken, String merchantHash) {

        final String postParams = "merchant_key=" + Constants.MERCHANT_KEY + "&user_credentials=" + var1 + "&card_token=" + cardToken + "&merchant_hash=" + merchantHash;

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    //  https://mobiledev.payu.in/admin/wis.php?action=add&uid=124&mid=457&token=74588&cvvhash=0123456789031

                    URL url = new URL("https://payu.herokuapp.com/store_merchant_hash");

                    byte[] postParamsByte = postParams.getBytes("UTF-8");

                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    conn.setRequestProperty("Content-Length", String.valueOf(postParamsByte.length));
                    conn.setDoOutput(true);
                    conn.getOutputStream().write(postParamsByte);

                    InputStream responseInputStream = conn.getInputStream();
                    StringBuffer responseStringBuffer = new StringBuffer();
                    byte[] byteContainer = new byte[1024];
                    for (int i; (i = responseInputStream.read(byteContainer)) != -1; ) {
                        responseStringBuffer.append(new String(byteContainer, 0, i));
                    }

                    JSONObject response = new JSONObject(responseStringBuffer.toString());


                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (ProtocolException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                this.cancel(true);
            }
        }.execute();
    }

    /**
     * This function is used to fetch merchant hash and card token stored on the merchant server.
     *
     * @param intent intent to PayuBaseActivity.java
     */
    private void fetchMerchantHashes(final Intent intent) {
        // now make the api call.
        final String postParams = "merchant_key=" + Constants.MERCHANT_KEY + "&user_credentials=" + var1;
        final Intent baseActivityIntent = intent;
        new AsyncTask<Void, Void, HashMap<String, String>>() {

            @Override
            protected HashMap<String, String> doInBackground(Void... params) {
                try {
                    //  https://mobiled ev.payu.in/admin/wis.php?action=add&uid=124&mid=457&token=74588&cvvhash=0123456789031

                    URL url = new URL("https://payu.herokuapp.com/get_merchant_hashes");

                    byte[] postParamsByte = postParams.getBytes("UTF-8");

                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    conn.setRequestProperty("Content-Length", String.valueOf(postParamsByte.length));
                    conn.setDoOutput(true);
                    conn.getOutputStream().write(postParamsByte);

                    InputStream responseInputStream = conn.getInputStream();
                    StringBuffer responseStringBuffer = new StringBuffer();
                    byte[] byteContainer = new byte[1024];
                    for (int i; (i = responseInputStream.read(byteContainer)) != -1; ) {
                        responseStringBuffer.append(new String(byteContainer, 0, i));
                    }

                    JSONObject response = new JSONObject(responseStringBuffer.toString());

                    HashMap<String, String> cardTokens = new HashMap<String, String>();
                    JSONArray oneClickCardsArray = response.getJSONArray("data");
                    int arrayLength;
                    if ((arrayLength = oneClickCardsArray.length()) >= 1) {
                        for (int i = 0; i < arrayLength; i++) {
                            cardTokens.put(oneClickCardsArray.getJSONArray(i).getString(0), oneClickCardsArray.getJSONArray(i).getString(1));
                        }
                        return cardTokens;
                    }
                    // pass these to next activity

                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (ProtocolException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(HashMap<String, String> oneClickTokens) {
                super.onPostExecute(oneClickTokens);

                baseActivityIntent.putExtra(PayuConstants.ONE_CLICK_CARD_TOKENS, oneClickTokens);
                startActivityForResult(baseActivityIntent, PayuConstants.PAYU_REQUEST_CODE);
            }
        }.execute();
    }

    /**
     * This function is used to delete the merchant hash and card token stored on merchant server.
     *
     * @param cardToken card token whose merchant hash is to be deleted
     */
    private void deleteMerchantHash(String cardToken) {

        final String postParams = "card_token=" + cardToken;

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    //  https://mobiledev.payu.in/admin/wis.php?action=add&uid=124&mid=457&token=74588&cvvhash=0123456789031

                    URL url = new URL("https://payu.herokuapp.com/delete_merchant_hash");

                    byte[] postParamsByte = postParams.getBytes("UTF-8");

                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    conn.setRequestProperty("Content-Length", String.valueOf(postParamsByte.length));
                    conn.setDoOutput(true);
                    conn.getOutputStream().write(postParamsByte);

                    InputStream responseInputStream = conn.getInputStream();
//                    StringBuffer responseStringBuffer = new StringBuffer();
//                    byte[] byteContainer = new byte[1024];
//                    for (int i; (i = responseInputStream.read(byteContainer)) != -1; ) {
//                        responseStringBuffer.append(new String(byteContainer, 0, i));
//                    }
//
//                    JSONObject response = new JSONObject(responseStringBuffer.toString());


//                } catch (JSONException e) {
//                    e.printStackTrace();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (ProtocolException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                this.cancel(true);
            }
        }.execute();
    }


    public HashMap<String, String> getAllOneClickHashHelper(String merchantKey, String userCredentials) {

        // now make the api call.
        final String postParams = "merchant_key=" + merchantKey + "&user_credentials=" + userCredentials;
        final Intent baseActivityIntent = intent;
        HashMap<String, String> cardTokens = new HashMap<String, String>();
        ;

        try {
            //  https://mobiled ev.payu.in/admin/wis.php?action=add&uid=124&mid=457&token=74588&cvvhash=0123456789031

            URL url = new URL("https://payu.herokuapp.com/get_merchant_hashes");

            byte[] postParamsByte = postParams.getBytes("UTF-8");

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Content-Length", String.valueOf(postParamsByte.length));
            conn.setDoOutput(true);
            conn.getOutputStream().write(postParamsByte);

            InputStream responseInputStream = conn.getInputStream();
            StringBuffer responseStringBuffer = new StringBuffer();
            byte[] byteContainer = new byte[1024];
            for (int i; (i = responseInputStream.read(byteContainer)) != -1; ) {
                responseStringBuffer.append(new String(byteContainer, 0, i));
            }

            JSONObject response = new JSONObject(responseStringBuffer.toString());

            JSONArray oneClickCardsArray = response.getJSONArray("data");
            int arrayLength;
            if ((arrayLength = oneClickCardsArray.length()) >= 1) {
                for (int i = 0; i < arrayLength; i++) {
                    cardTokens.put(oneClickCardsArray.getJSONArray(i).getString(0), oneClickCardsArray.getJSONArray(i).getString(1));
                }

            }
            // pass these to next activity

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cardTokens;
    }


    /**
     * Returns a HashMap object of cardToken and one click hash from merchant server.
     * <p/>
     * This method will be called as a async task, regardless of merchant implementation.
     * Hence, not to call this function as async task.
     * The function should return a cardToken and corresponding one click hash as a hashMap.
     *
     * @param userCreds a string giving the user credentials of user.
     * @return the Hash Map of cardToken and one Click hash.
     **/

    @Override
    public HashMap<String, String> getAllOneClickHash(String userCreds) {
        // 1. GET http request from your server
        // GET params - merchant_key, user_credentials.
        // 2. In response we get a
        // this is a sample code for fetching one click hash from merchant server.
        return getAllOneClickHashHelper(Constants.MERCHANT_KEY, userCreds);
    }

    @Override
    public void getOneClickHash(String cardToken, String merchantKey, String userCredentials) {

    }

    /**
     * This method will be called as a async task, regardless of merchant implementation.
     * Hence, not to call this function as async task.
     * This function save the oneClickHash corresponding to its cardToken
     *
     * @param cardToken    a string containing the card token
     * @param oneClickHash a string containing the one click hash.
     **/

    @Override
    public void saveOneClickHash(String cardToken, String oneClickHash) {
        // 1. POST http request to your server
        // POST params - merchant_key, user_credentials,card_token,merchant_hash.
        // 2. In this POST method the oneclickhash is stored corresponding to card token in merchant server.
        // this is a sample code for storing one click hash on merchant server.

        storeMerchantHash(cardToken, oneClickHash);

    }

    /**
     * This method will be called as a async task, regardless of merchant implementation.
     * Hence, not to call this function as async task.
     * This function delete’s the oneClickHash from the merchant server
     *
     * @param cardToken       a string containing the card token
     * @param userCredentials a string containing the user credentials.
     **/

    @Override
    public void deleteOneClickHash(String cardToken, String userCredentials) {

        // 1. POST http request to your server
        // POST params  - merchant_hash.
        // 2. In this POST method the oneclickhash is deleted in merchant server.
        // this is a sample code for deleting one click hash from merchant server.

        deleteMerchantHash(cardToken);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_GET_ACCOUNTS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    //sets the primary email
                    mEditTextEmail.setText(getPrimaryEmailAddress());

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if (requestCode == PayuConstants.PAYU_REQUEST_CODE) {
            if (data != null) {

/*                new AlertDialog.Builder(this)
                        .setCancelable(false)
                        .setMessage("Payu's Data : " + data.getStringExtra("payu_response") + "\n\n\n Merchant's Data: " + data.getStringExtra("result"))
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                Intent intent = new Intent(FillDetailsActivity.this, ResultActivity.class);
                                startActivity(intent);
                            }
                        }).show();*/

                Intent intent = new Intent(FillDetailsActivity.this, ResultActivity.class);
                intent.putExtra("payu_response", data.getStringExtra("payu_response"));
                startActivity(intent);

            } else {
                Intent intent = new Intent(FillDetailsActivity.this, ResultActivity.class);
                intent.putExtra("payu_response", "");
                startActivity(intent);
                Toast.makeText(this, "Could not receive data", Toast.LENGTH_LONG).show();
            }
        }
    }
}

