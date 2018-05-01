package com.example.utkarsh.thingspeakandroid.model;

import java.util.List;

public class StatusUpdates {
    private Channel channel;
    private List<StatusFeed> feeds;

    /***
     * Get the basic information of the Channel.
     *
     * @return the Channel
     */
    public Channel getChannel() {
        return channel;
    }

    /***
     * Get the feed entries of the Channel's status updates.
     *
     * @return the feed entries
     */
    public List<StatusFeed> getFeeds() {
        return feeds;
    }
}
