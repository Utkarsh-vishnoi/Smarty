package com.example.utkarsh.thingspeakandroid;

import com.example.utkarsh.thingspeakandroid.model.Channel;
import com.example.utkarsh.thingspeakandroid.model.ChannelFeed;
import com.example.utkarsh.thingspeakandroid.model.Feed;
import com.example.utkarsh.thingspeakandroid.model.PublicChannels;
import com.example.utkarsh.thingspeakandroid.model.StatusUpdates;

import java.util.List;
import java.util.Map;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;
import retrofit.http.QueryMap;

public interface ThingSpeakService {

    @GET("/channels/{id}/feeds.json")
    void getChannelFeed(@Path("id") Long channelId, @QueryMap Map<String, String> params, Callback<ChannelFeed> callback);

    @GET("/channels/{id}/feeds/last.json")
    void getLastEntryInChannelFeed(@Path("id") Long channelId, @QueryMap Map<String, String> params, Callback<Feed> callback);

    @GET("/channels/{id}/feeds/{entry}.json")
    void getSpecificEntryInChannelFeed(@Path("id") Long channelId, @Path("entry") Long entryId, @QueryMap Map<String, String> params, Callback<Feed> callback);

    @GET("/channels/{id}/fields/{field}.json")
    void getChannelFieldFeed(@Path("id") Long channelId, @Path("field") Integer fieldId, @QueryMap Map<String, String> params, Callback<ChannelFeed> callback);

    @GET("/channels/{id}/status.json")
    void getStatusUpdates(@Path("id") Long channelId, @QueryMap Map<String, String> params, Callback<StatusUpdates> callback);

    @GET("/channels/public.json")
    void listPublicChannels(@Query("page") Integer page, @Query("tag") String tag, @Query("username") String username,
                            @Query("latitude") Float latitude, @Query("longitude") Float longitude, @Query("distance") Float distance,
                            Callback<PublicChannels> callback);

    @GET("/channels.json")
    void listMyChannels(@Query("api_key") String apiKey, Callback<List<Channel>> callback);

}
