package com.bytes.fmk.payload.processor;

/**
 * Created by Kent on 4/14/2017.
 */

public class ProcessorEvent<T> {

    public enum Type {
        Default, // The default type, (i.e., based on T)
        Snackbar,
        Fighters,
        StartMatch,
        GameResults,
        ActionResults,
        ActionResults_P1 // Phase 1 implementation (deprecated) 
    }

    private Type type;

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    private T data;

    public ProcessorEvent() {
        this(Type.Default, null);
    }

    /**
     * @param data
     */
    public ProcessorEvent(T data) {
       this(Type.Default, data);
    }

    /**
     *
     * @param type
     * @param data
     */
    public ProcessorEvent(Type type, T data) {
        this.type = type;
        this.data = data;
    }
}
