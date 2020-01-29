package com.kokteyl.bumerang.sample;


import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.kokteyl.android.bumerang.core.Bumerang;
import com.kokteyl.android.bumerang.core.BumerangLog;
import com.kokteyl.android.bumerang.image.BumerangImageView;
import com.kokteyl.android.bumerang.request.Request;
import com.kokteyl.android.bumerang.response.Response;
import com.kokteyl.android.bumerang.response.ResponseListener;
import com.kokteyl.bumerang.R;
import com.kokteyl.bumerang.sample.network.MyAPI;
import com.kokteyl.bumerang.sample.network.PostResponseModel;
import com.kokteyl.bumerang.sample.network.RequestModel;
import com.kokteyl.bumerang.sample.network.ResponseModel;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TestActivity extends Activity {
    TextView requestTextView;
    TextView responseTextView;

    MyAPI api = (MyAPI) Bumerang.get().initAPI(MyAPI.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        requestTextView = findViewById(R.id.request);
        responseTextView = findViewById(R.id.response);
        testRequests();
        testImageView();

    }


    void testImageView() {
        BumerangImageView bumerangImageView = (BumerangImageView) findViewById(R.id.test_image);
        bumerangImageView.setImageUrl("https://image.shutterstock.com/image-photo/beautiful-water-drop-on-dandelion-260nw-789676552.jpg");
    }

    void testRequests() {
        RequestModel model = new RequestModel("ModelTit", 2019, new RequestModel.SubItem(Arrays.asList("test1", "test2"), 1001, true));
        JsonObject sampleJson = new JsonObject();
        sampleJson.addProperty("title", "JsonTit");
        sampleJson.addProperty("year", 0);

        getItem(model);
        getItems();
        postItem(null, sampleJson);
        model.subItem = null;
        postItemFormEncoded(model);
        deleteItem("1");
        putItem("1");
    }

    void getItem(RequestModel model) {
        Request request = api.getItem("todo_1",null, "1", new ResponseListener<Response<ResponseModel>>() {
            @Override
            public void onSuccess(Response<ResponseModel> response) {
            }

            @Override
            public void onError(Response<ResponseModel> response) {

            }
        });
    }

    void getItems() {
        api.getItems(new ResponseListener<Response<List<ResponseModel>>>() {
            @Override
            public void onSuccess(Response<List<ResponseModel>> response) {
            }

            @Override
            public void onError(Response<List<ResponseModel>> response) {

            }
        });
    }

    void postItem(RequestModel model, final JsonObject json) {
        Map<String, String> headerMap = new HashMap<String, String>();
        headerMap.put("token", "post json header");
        final Request request = api.postItem( headerMap, json, new ResponseListener<Response<PostResponseModel>>() {
            @Override
            public void onSuccess(Response<PostResponseModel> response) {
                PostResponseModel respModel = response.getResponse(PostResponseModel.class);
                responseTextView.setText(response.toString());
            }

            @Override
            public void onError(Response<PostResponseModel> response) {
                if(response.getCache() != null) {
                    PostResponseModel cachedObj = response.getCache().getResponse(PostResponseModel.class);
                }
            }
        });
        requestTextView.setText(request.toString());
    }

    void postItemFormEncoded(RequestModel model) {
        Map<String, String> headerMap = new HashMap<String, String>();
        headerMap.put("token", "postEncoded header");
        api.postItemForm(headerMap, model, new ResponseListener<Response<PostResponseModel>>() {
            @Override
            public void onSuccess(Response<PostResponseModel> response) {
                PostResponseModel respModel = response.getResponse(PostResponseModel.class);
                BumerangLog.i(respModel.toString());
            }

            @Override
            public void onError(Response<PostResponseModel> response) {
            }
        });
    }

    void deleteItem(String id) {
        api.deleteItem("Token delete 12", id, new ResponseListener<Response<ResponseModel>>() {
            @Override
            public void onSuccess(Response<ResponseModel> response) {
            }

            @Override
            public void onError(Response<ResponseModel> response) {
            }
        });
    }

    void putItem(String id) {
        api.putItem(id, new ResponseListener<Response<ResponseModel>>() {
            @Override
            public void onSuccess(Response<ResponseModel> response) {
            }

            @Override
            public void onError(Response<ResponseModel> response) {

            }
        });
    }


}
