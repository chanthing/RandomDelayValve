package com.vmware.stg.demo.tcvalve;

import java.io.*;
import java.util.*;

import javax.servlet.*;

import org.apache.catalina.*;
import org.apache.catalina.connector.*;
import org.apache.catalina.valves.ValveBase;




/**
 * A Tomcat valve that introduces processing delay into random requests
 *
 * The attributes are:
 *
 *	pctDelayedRequests - the percentage of requests that will have a delay added
 *	maxDelay - the maximum amount of time (in msec) that will be added
 *	minDelay - the minimum amount of time (in msec) that will be added
 * 
 */
public class RandomDelayValve extends ValveBase {

    /** The percentage of requests that will have a delay added. */
    private int percentDelayed = 0;

    /** The maximum amount of delay to add (in milliseconds) */
    private int maxDelay = 10000;

    /** The minimum amount of delay to add (in milliseconds) */
    private int minDelay = 5000;

    private static final int DEFAULT_DELAY_SCALE_FACTOR = 2;
    private static Random random = new Random();
    private boolean maxDelaySet = false;
    private boolean minDelaySet = false;
	

    /**
     *   If selected as a "slow" request (via random number and percentage parameter), 
     *   add a certain amount of delay to simulate a long-running request.
     */
    public void invoke(Request request, Response response) throws IOException,
		    ServletException {

	/*
	    Use percentage to find out if this request is delayed.
	*/
	boolean willBeDelayed = (random.nextInt(100) > percentDelayed); 

	if (willBeDelayed) {
	    int delay;

	    delay = random.nextInt(maxDelay - minDelay) + minDelay; 

	    /*
		Report delay
	    */
	    if (containerLog.isInfoEnabled()) {
		containerLog.info("Delaying request " 
					+ request.getDecodedRequestURI()
					+ " for " + delay + " ms.");
	    }

	    /*
		Delay request by sleeping
	    */
	    try {
		Thread.sleep(delay);
	    } catch (InterruptedException ie) {
		containerLog.error("Got an exception while sleeping.");
	    }
	}

	/*
	     Invoke next valve or true processing
	*/
	getNext().invoke(request, response);

    }


    /**
     * Set percentDelayed
     */
    public void setPercentDelayed(int pctDelay) throws OutOfRangeException {
	if (pctDelay < 0) {
	    throw new OutOfRangeException("PercentDelayed must be positive");
	} else if (pctDelay > 100) {
	    throw new OutOfRangeException("PercentDelayed must be <= 100");
	}
	percentDelayed = pctDelay;
    }


    /**
     * Set maxDelay
     */
    public void setMaxDelay(int delay) throws OutOfRangeException {
	if (delay < 0) {
	    throw new OutOfRangeException("maxDelay must be positive.");
	}
	if (minDelaySet) {
	    if (delay < minDelay) {
		throw new OutOfRangeException("maxDelay must be >= minDelay");
	    }
	} else {
	    minDelay = delay / DEFAULT_DELAY_SCALE_FACTOR;
	}
	maxDelay = delay;
	maxDelaySet = true;
    }


    /**
     * Set minDelay
     */
    public void setMinDelay(int delay) throws OutOfRangeException {
	if (delay < 0) {
	    throw new OutOfRangeException("minDelay must be positive.");
	}
	if (maxDelaySet) {
	    if (delay > maxDelay) {
	   	throw new OutOfRangeException("minDelay must be <= maxDelay"); 
	    }
	} else {
	    maxDelay = delay * DEFAULT_DELAY_SCALE_FACTOR;
	}
	minDelay = delay;
	minDelaySet = true;
    }

	
    /**
     * @see org.apache.catalina.valves.ValveBase#getInfo()
     */
    public String getInfo() {
	return getClass() + "/1.0";
    }
}
