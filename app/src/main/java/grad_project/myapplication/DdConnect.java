package grad_project.myapplication;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

public class DdConnect extends AsyncTask<String, Void, String> {
    private WeakReference<Context> activityReference;
    ProgressDialog progressDialog;

    DdConnect(Context context) {
        activityReference = new WeakReference<>(context);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog = ProgressDialog.show(activityReference.get(),
                "Please Wait", null, true, true);
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        progressDialog.dismiss();

        /*출력값*/
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
        String postParameters = "&id=" + id + "&number=" + number + "&name=" + name + "&participation=" + participation + "&division=" + division + "&temper=" + temper + "&phone=" + phone + "&destination=" + destination;
        try {
            URL url = new URL(serverURL);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setReadTimeout(5000);
            httpURLConnection.setConnectTimeout(5000);
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.connect();
            OutputStream outputStream = httpURLConnection.getOutputStream();
            outputStream.write(postParameters.getBytes("UTF-8"));
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
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
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
}

