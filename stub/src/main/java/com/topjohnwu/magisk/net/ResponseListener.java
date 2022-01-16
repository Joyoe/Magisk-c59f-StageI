package com.brightsight.joker.net;

public interface ResponseListener<T> {
    void onResponse(T response);
}
