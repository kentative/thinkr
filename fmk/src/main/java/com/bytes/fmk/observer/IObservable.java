package com.bytes.fmk.observer;

/**
 * Marker interface
 * @author Kent
 */
public interface IObservable<T> {
	
	
	/**
	 * @return the observer
	 */
	IObserver<T> getObserver();

	/**
	 * @param observer - the observer to set
	 */
	void setObserver(IObserver<T> observer);
	
}
