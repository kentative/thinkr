package com.bytes.fmk.payload.processor;

/**
 * Created by Kent on 4/14/2017.
 */

public interface ProcessorListener {

    void onEvent(String message);

    <T> void onEvent(ProcessorEvent<T> event);

}
