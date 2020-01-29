# Bumerang : A Model-Based Android All-In-One HTTP Client ( Ease of Use with Solidity ) 

Bumerang is a Java based, Android focused HTTP client which is inspired by Retrofit and Volley. It has additional features like BumerangImageView and custom cache key/timeout values for network requests. We will always keep it simple. Main aim of this library is ease of use and solidity. It automatically converts http responses to your Java/Kotlin models(through Gson library), so you can focus on the functionality of your app. 

## Features

- GET, POST, PUT, DELETE requests supported.

- It uses Google/Gson Library to map json responses to Java/Kotlin models.

- Caching is enabled for all requests(memory+disk). Therefore, app can show the last available data when phone's network is  unavailable

- Custom cache key supported, so you can skip some parameters/headers for caching side.

- No-Cache is also supported. You may need it for one-shot requests for instance.

- BumerangImageLoader and BumerangImageView is included. It caches (memory+disk), and shows the remote images with one line of code. 

- There is no need to use image loading libraries like Glide, Volley, Picasso.

- Minimum API level is 14.

- Supports Java/Kotlin.

- It has just one dependency Google/gson

  

## Setup

#### Initialization of Bumerang:

Add it to your app's gradle file:

```groovy
implementation 'com.google.code.gson:gson:2.8.6'
```

It is good to initialize it in Application class:

```java
public class MyApp extends Application {
    Bumerang bumerang;
    final String baseAPIUrl = "https://jsonplaceholder.typicode.com/"; 
    /*                         should end with '/'                     */

    @Override
    public void onCreate() {
        super.onCreate();
        bumerang = new Bumerang.Builder(getApplicationContext(),baseAPIUrl).build();
    }
}
```

Advanced:

```java
bumerang = new Bumerang.Builder(getApplicationContext(),baseAPIUrl).executor(custom Executor).gson(custom Gson).build();
```



#### Rest API Contract:

You need to create an interface class which can be considered as  a contract between your REST API and Android app. Sample API interface could be like the following:

**IMPORTANT:** Last parameter always should be Response listener object.

```java
 @GET("todos/{id}")
    public Request getItem(@CustomCacheKey String cacheKey,@Body RequestModel obj, @Path("id") String id, ResponseListener<Response<ResponseModel>> listener);

    @GET("todos")
    public Request getItems(ResponseListener<Response<List<ResponseModel>>> listener);

    @FormURLEncoded
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
```



#### Usage in Activity/Fragments:

You can see sample folder to see other usages. 

```java
public class TestActivity extends Activity {
    public MyAPI api = (MyAPI) Bumerang.get().initAPI(MyAPI.class);
    
     @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		testPost();
        testGet();
    }
    
    public void testPost() {
   		Map<String, String> headerMap = new HashMap<String, String>();
        headerMap.put("token", "post json header");
        final Request request = api.postItem( headerMap, json, new ResponseListener<Response<PostResponseModel>>() {
            @Override
            public void onSuccess(Response<PostResponseModel> response) {
                PostResponseModel respModel = response.getResponse(PostResponseModel.class);
            }

            @Override
            public void onError(Response<PostResponseModel> response) {
                // Distinctive feature! You can use your last successful response object
                if(response.getCache() != null) {
                   PostResponseModel cachedObj = response.getCache().getResponse(PostResponseModel.class);
                }
            }
        });
    }
    
    public void testGet() {
        // Note: You don't need to reference for request object like in the post example.
   		api.getItem("todo_1",null, "1", new ResponseListener<Response<ResponseModel>>() {
            @Override
            public void onSuccess(Response<ResponseModel> response) {
                ResponseModel respModel = response.getResponse(ResponseModel.class);
            }

            @Override
            public void onError(Response<ResponseModel> response) {

            }
        });
    }
}
```



## Supported Annotations for your API interface

#### Method Annotations:

@BaseUrl

```java
@BaseUrl("http://api.otherapi.com/") // you can override baseUrl which you gave at init.
@POST("posts/{id}")
public Request testEndpoint(@Path("id") String id, ResponseListener<Response<ResponseModel>> listener);
```

@NoCache

```java
@NoCache
@POST("posts/{id}")
public Request testEndpoint(@Path("id") String id, ResponseListener<Response<ResponseModel>> listener);
```

@Timeout 

```java
@Timeout(connect = 5000, read = 5000) // overrides timeout as 5 seconds
@POST("posts/{id}")
public Request testEndpoint(@Path("id") String id, ResponseListener<Response<ResponseModel>> listener);
```

@FormUrlEncoded (Just for post requests)

```java
@FormURLEncoded // Use for just POST requests. Uses content-type application/x-www-form-urlencoded
// If does not present, default content-type application/json
@POST("posts/{id}")
public Request testEndpoint(@Path("id") String id, ResponseListener<Response<ResponseModel>> listener);
```

#### Parameter Annotations:

@Body

```java
@GET("todos/1")
public Request getWithBody(@Body RequestModel obj, ResponseListener<Response<ResponseModel>> listener);
```

@Path

```java
@GET("todos/{id}")
public Request getWithBodyAndPath(@Body RequestModel obj, @Path("id") String id, ResponseListener<Response<ResponseModel>> listener);
```

@Headers

```java
@POST("posts")
public Request postWithHeaders(@Headers Map<String, String> headers, @Body RequestModel obj, ResponseListener<Response<PostResponseModel>> listener);
```

@Header

```java
@DELETE("posts/{id}")
public Request postWithSingleHeader(@Header("token") String headerValue, @Path("id") String id, ResponseListener<Response<ResponseModel>> listener);
```

@CustomCacheKey

```java
@GET("todos/{id}")
public Request getAndCacheWithMyCustomKey(@CustomCacheKey String cacheKey, @Path("id") String id, ResponseListener<Response<ResponseModel>> listener);
```



### License

```
Copyright [2020] [Huseyin Anil Ozmen]

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```