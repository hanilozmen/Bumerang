package com.kokteyl.bumerang.sample.network;


import com.google.gson.JsonObject;
import com.kokteyl.android.bumerang.annotations.Body;
import com.kokteyl.android.bumerang.annotations.DELETE;
import com.kokteyl.android.bumerang.annotations.FormURLEncoded;
import com.kokteyl.android.bumerang.annotations.GET;
import com.kokteyl.android.bumerang.annotations.Header;
import com.kokteyl.android.bumerang.annotations.Headers;
import com.kokteyl.android.bumerang.annotations.POST;
import com.kokteyl.android.bumerang.annotations.PUT;
import com.kokteyl.android.bumerang.annotations.Path;
import com.kokteyl.android.bumerang.core.ResponseListener;
import com.kokteyl.android.bumerang.request.Request;
import com.kokteyl.android.bumerang.response.Response;

import java.util.List;
import java.util.Map;

public interface MyAPI {

    // START TEST https://jsonplaceholder.typicode.com/
    //
    @GET("todos/{id}")
    public Request getItem(@Body RequestModel obj, @Path("id") String id, ResponseListener<Response<ResponseModel>> listener);

    @GET("todos")
    public Request getItems(ResponseListener<Response<List<ResponseModel>>> listener);

    @POST("posts")
    public Request postItemForm(@Headers Map<String, String> headers, @Body RequestModel obj, ResponseListener<Response<PostResponseModel>> listener);

    @FormURLEncoded //application/x-www-form-url-encoded
    @POST("posts")
    public Request postItemForm(@Headers Map<String, String> headers, @Body JsonObject obj, ResponseListener<Response<PostResponseModel>> listener);

    //Default: application/json
    @POST("posts")
    public Request postItem(@Headers Map<String, String> headers, @Body JsonObject obj, ResponseListener<Response<PostResponseModel>> listener);

    //Default: application/json
    @POST("posts")
    public Request postItem(@Headers Map<String, String> headers, @Body RequestModel obj, ResponseListener<Response<PostResponseModel>> listener);

    //@Timeout(read = 200, connect = 200)
    @PUT("posts/{id}")
    public Request putItem(@Path("id") String id, ResponseListener<Response<ResponseModel>> listener);

    @DELETE("posts/{id}")
    public Request deleteItem(@Header("token") String headerValue, @Path("id") String id, ResponseListener<Response<ResponseModel>> listener);

}
