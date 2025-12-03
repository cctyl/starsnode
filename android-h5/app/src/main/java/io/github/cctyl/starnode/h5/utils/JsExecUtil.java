package io.github.cctyl.starnode.h5.utils;

import android.util.Log;
import android.webkit.ValueCallback;
import android.webkit.WebView;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class JsExecUtil {

    private WebView webView;

    public JsExecUtil(WebView webView) {
        this.webView = webView;
    }

    public void exec(String methodName, ValueCallback<String> resultCallback) {
        execCommon(methodName, Collections.emptyList(), resultCallback);
    }

    public void exec(String methodName, Object args, ValueCallback<String> resultCallback) {
        execCommon(methodName, Arrays.asList(args), resultCallback);
    }

    public void exec(String methodName, Object arg1, Object arg2, ValueCallback<String> resultCallback) {
        execCommon(methodName, Arrays.asList(arg1, arg2), resultCallback);
    }

    public void exec(String methodName, Object arg1, Object arg2, Object arg3, ValueCallback<String> resultCallback) {
        execCommon(methodName, Arrays.asList(arg1, arg2, arg3), resultCallback);
    }

    public void exec(String methodName, Object arg1, Object arg2, Object arg3, Object arg4, ValueCallback<String> resultCallback) {
        execCommon(methodName, Arrays.asList(arg1, arg2, arg3, arg4), resultCallback);
    }

    public void execCommon(String methodName, List<Object> args, ValueCallback<String> resultCallback) {
        StringBuilder sb = new StringBuilder();
        sb.append("vue.")
                .append(methodName)
                .append("(");
        for (int i = 0; i < args.size(); i++) {

            Object value = args.get(i);

            //字符串类型的处理
            if (
                    value != null &&
                    String.class.equals(value.getClass())

            ) {
                value = handleStringTransfer((String) value);
            }

            //json对象的处理
//            if (value!=null &&
//                    (  JSONArray.class.equals(value.getClass())
//                    ||
//                            JSONObject.class.equals(value.getClass())
//                    )
//            ){
//                value = value.toString();
//            }

            sb.append(value);
            if (i != args.size() - 1) {
                sb.append(",");
            }
        }
        sb.append(")");
        Log.d("---->  JsExecUtil", sb.toString());
        webView.evaluateJavascript(
                sb.toString(),
                resultCallback
        );
    }


    public static String handleStringTransfer(String s) {
        if (s != null) {
            String escapedArg = s.replace("'", "\\'").replace("\"", "\\\"");
            return "'" + escapedArg + "'";
        } else {
            return null;
        }

    }

    public void setData(String key, Object value) {
        //字符串类型的处理
        if (
                value != null &&
                        String.class.equals(value.getClass())

        ) {
            Log.d("---->  JsExecUtil","是string"+value.getClass().getName()     );
            value = handleStringTransfer((String) value);
        }
        String ling = "vue.setData('" + key + "'," + value + ")";
        Log.d("---->  JsExecUtil", ling);
        webView.evaluateJavascript(
                ling,
                null
        );
    }
}
