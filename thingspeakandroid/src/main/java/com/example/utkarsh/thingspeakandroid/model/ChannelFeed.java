package com.example.utkarsh.thingspeakandroid.model;

import java.util.List;

public class ChannelFeed {
    private Channel channel;
    private List<Feed> feeds;

    /***
     * Get the basic information of the Channel.
     *
     * @return the Channel
     */
    public Channel getChannel() {
        return channel;
    }

    /***
     * Get the feed entries of the Channel.
     *
     * @return the feed entries
     */
    public List<Feed> getFeeds() {
        return feeds;
    }
}
