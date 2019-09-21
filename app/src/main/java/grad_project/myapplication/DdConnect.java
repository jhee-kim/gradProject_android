package grad_project.myapplication;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 회원가입 : add_audience.php                 input : 회원가입정보         return : id or 0
 로그인 : login.php                          input : number, name         return : id or 0
 회원가입 정보 : get_audience.php            input : id                   return : { "result": [ { "number": "군번", "name": "이름", "participation": "관람구분(0:일반관람, 1:전시관람)", "division": "국군구분(0:육군, 1:해군, 2:공군, 3:해병대", "temper": "소속", "phone": "전화번호", "destination": "행선지" } ] }
 관람 구분 : get_participation.php           input : id                   return : participation or 0          일반관람 : 0, 전시관람 : 1
 시작 여부 : get_isStart.php                 input : id                   return : start_date or 0
 관람종료 : set_isEnd.php                    input : id                   return : 1 or 0
 종료 여부 : get_isEnd.php                   input : id                   return : end_date or 0
 관람 초기화 : set_isStart.php               input : id                   return : 1 or 0

 전시관 개설 여부 : get_exhibition.php       input : non                  return : { "result": [ { "number": "1", "isOpen": "1" }, { "number": "2", "isOpen": "0" }, { "number": "3", "isOpen": "1" }, { "number": "4", "isOpen": "1" }, { "number": "5", "isOpen": "1" }, { "number": "6", "isOpen": "1" } ] }
 전시관 QR코드 : get_qr.php                  input : non                  return : { "result": [ { "number": "1", "address": "" }, { "number": "2", "address": "" }, { "number": "3", "address": "" }, { "number": "4", "address": "" }, { "number": "5", "" }, { "number": "6", "address": "" } ] }
 전시관 MAC : get_mac.php                    input : non                  return : { "result": [ { "number": "1", "mac": "0" }, { "number": "2", "mac": "0" }, { "number": "3", "mac": "0" }, { "number": "4", "mac": "0" }, { "number": "5", "mac": "0" }, { "number": "6", "mac": "0" } ] }

 해설사 스케줄 : get_narrator.php            input : non                  return : { "result": [ { "first_time": "0", "second_time": "0" } ] }            X : 0, O : 1

 설문지 : get_survey.php                     input : non                  return : { "result": [ { "is_exist": "1", "url": "www.naver.com" } ] }
**/

public class DdConnect extends AsyncTask<String, Void, String> {
    private static final String BASE_PATH = "http://35.221.108.183/android/";
    static final String ADD_AUDIENCE = BASE_PATH + "add_audience.php";
    static final String LOGIN = BASE_PATH + "login.php";
    static final String GET_AUDIENCE = BASE_PATH + "get_audience.php";
    static final String GET_PARTICIPATION = BASE_PATH + "get_participation.php";
    static final String GET_ISSTART = BASE_PATH + "get_isStart.php";
    static final String SET_ISEND = BASE_PATH + "set_isEnd.php";
    static final String GET_ISEND = BASE_PATH + "get_isEnd.php";
    static final String GET_EXHIBITION = BASE_PATH + "get_exhibition.php";
    static final String GET_QR = BASE_PATH + "get_qr.php";
//    static final String GET_MAC = BASE_PATH + "get_mac.php";
    static final String GET_NARRATOR = BASE_PATH + "get_narrator.php";
    static final String GET_SURVEY = BASE_PATH + "get_survey.php";
    static final String SET_ISSTART = BASE_PATH + "set_isStart.php";

    private WeakReference<Context> activityReference = null;
    private ProgressDialog progressDialog;

    DdConnect() {}
    DdConnect(Context context) {
        activityReference = new WeakReference<>(context);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (activityReference != null) {
            progressDialog = ProgressDialog.show(activityReference.get(),
                    "Please Wait", null, true, true);
        }
    }

    @Override
    protected String doInBackground(String... params) {
        String serverURL = params.length>0?params[0]:"";
        String id = params.length>1?params[1]:"";
        String number = params.length>2?params[2]:"";
        String name = params.length>3?params[3]:"";
        String participation = params.length>4?params[4]:"";
        String division = params.length>5?params[5]:"";
        String temper = params.length>6?params[6]:"";
        String phone = params.length>7?params[7]:"";
        String destination = params.length>8?params[8]:"";
        String postParameters = "&id=" + id +
                                "&number=" + number +
                                "&name=" + name +
                                "&participation=" + participation +
                                "&division=" + division +
                                "&temper=" + temper +
                                "&phone=" + phone +
                                "&destination=" + destination;
        try {
            URL url = new URL(serverURL);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setReadTimeout(5000);
            httpURLConnection.setConnectTimeout(5000);
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.connect();
            OutputStream outputStream = httpURLConnection.getOutputStream();
            outputStream.write(postParameters.getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
            outputStream.close();
            int responseStatusCode = httpURLConnection.getResponseCode();
            InputStream inputStream;
            if(responseStatusCode == HttpURLConnection.HTTP_OK) {
                inputStream = httpURLConnection.getInputStream();
            }
            else{
                inputStream = httpURLConnection.getErrorStream();
            }
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder sb = new StringBuilder();
            String line;
            while((line = bufferedReader.readLine()) != null){
                sb.append(line);
            }
            bufferedReader.close();
            return sb.toString();
        } catch (Exception e) {
            return "-1";
        }
    }
    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        if (activityReference != null) {
            progressDialog.dismiss();
        }
    }
}

