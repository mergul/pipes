package com.streams.pipes.chat;

import java.util.Date;

public interface ChatRoomMessageListener<T> {

	void onPostMessage(T msg, String id, Date date, String event);
}
