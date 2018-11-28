package com.bytes.fmk.observer;

public interface IObserver<T> {

	void notify(T data, Context context);
}
