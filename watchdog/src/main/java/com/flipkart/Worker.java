package com.flipkart;

import com.flipkart.dto.Result;

/**
 * Created on 05/03/17 by dark magic.
 */
public interface Worker {
    public void setData(Object... data);

    public Result doWork();
}
