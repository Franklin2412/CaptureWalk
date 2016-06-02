package com.payapp.uttrakhandtransportcorporation;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.payapp.uttrakhandtransportcorporation.Model.Constants;
import com.payu.india.Model.PaymentParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class ResultActivity extends AppCompatActivity implements View.OnClickListener {

    private String mTransactionStatus, mTransactionId, mAmount,m_status;
    private TextView mTransactionIdTextView, mTransactionStatusTextView, mTransactionStatusMessageTextView,
            mAmountTextView, mTransactionDetailedMessageTextView;
    private ImageView mResultImageView;
    private LinearLayout mLinearLayoutTransactionDetails;
    private Button mBtnDoAnotherTransaction;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mTransactionIdTextView = (TextView) findViewById(R.id.textview_result_transactionid);
        mTransactionStatusTextView = (TextView) findViewById(R.id.textview_transaction_status);
        mAmountTextView = (TextView) findViewById(R.id.textview_result_amount);
        mTransactionDetailedMessageTextView = (TextView) findViewById(R.id.textview_transaction_result_detailed_message);
        mTransactionStatusMessageTextView = (TextView) findViewById(R.id.textview_result_status_message);
        mResultImageView = (ImageView) findViewById(R.id.imageview_result);
        mLinearLayoutTransactionDetails = (LinearLayout) findViewById(R.id.linear_layout_result_details);
        mBtnDoAnotherTransaction = (Button) findViewById(R.id.btn_do_another_transaction);
        mBtnDoAnotherTransaction.setOnClickListener(this);

        String result = getIntent().getExtras().getString("payu_response");
        if (!(result.equals(""))) {
            try {

                JSONObject jsonObject = new JSONObject(result);
                mTransactionStatus = jsonObject.getString("status");
                mTransactionId = jsonObject.getString("txnid");
                mAmount = jsonObject.getString("amount");
                //name=jsonObject.getString("fname");
                if(mTransactionStatus.equals("success")){
                    m_status="succeeded.";
                }else {
                    m_status="failed.";
                }


                if (mTransactionStatus.equals("success")) {
                    mResultImageView.setImageResource(R.drawable.ic_success);
                    mTransactionStatusMessageTextView.setText(getString(R.string.transaction_successful));
                    mTransactionDetailedMessageTextView.setText(getString(R.string.thank_you_message_success));
                    send_sms();
                } else if (mTransactionStatus.equals("failure")) {
                    mResultImageView.setImageResource(R.drawable.ic_failure);
                    mTransactionStatusMessageTextView.setText(getString(R.string.transaction_falied));
                    mTransactionDetailedMessageTextView.setText(getString(R.string.regret_to_inform_failed_transaction));
                    send_sms();
                }
                mLinearLayoutTransactionDetails.setVisibility(View.VISIBLE);
                mTransactionIdTextView.setText(mTransactionId);
                mAmountTextView.setText(mAmount);
                mTransactionStatusTextView.setText(mTransactionStatus);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            mResultImageView.setImageResource(R.drawable.ic_drop_transaction);
            mTransactionStatusMessageTextView.setText(getString(R.string.transaction_dropped));
            mTransactionDetailedMessageTextView.setText(getString(R.string.regret_to_inform_dropped_transaction));
            mLinearLayoutTransactionDetails.setVisibility(View.GONE);
        }

        Log.d("Payu_Response", result);

    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.btn_do_another_transaction) {
            finish();
        }

    }


        //** Send _Sms Api Call After completing success transaction.

       private void send_sms() {

      /*  final String postParams = "var1=" + Constants.d_number  +  "&command=send_sms" + "&hash=" + Constants.sms_Hash +
                "&var2=Dear customer, BankName is working fine now. You may now visit MerchantWebsiteURL to complete your order. Team PayU." +
                "&var3=PAYUIB" + "&key=gtKFFx";*/

           final String postParams = "var1=" + Constants.d_number  +  "&command=send_sms" + "&hash=" + Constants.sms_Hash +
                   "&var2=" + "Transaction No. " + mTransactionId + " for Rs " + mAmount + " done for " + "UTC" +  " has " + m_status
                  + "&var3=PAYUIB" + "&key=gtKFFx";

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    //  https://mobiledev.payu.in/admin/wis.php?action=add&uid=124&mid=457&token=74588&cvvhash=0123456789031

                    URL url = new URL("https://test.payu.in/merchant/postservice?");

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
}
