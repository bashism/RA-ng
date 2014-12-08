package edu.duke.ra.core.result;

/**
 * A bundling of a return value and an associated error, to get around
 * the fact that we can't throw java exceptions in QueryResults, because
 * they will be eventually returned as JSON
 *
 */
public class ValueWithError<ValueType>{
    private ValueType value;
    private boolean hasError;
    private ErrorResult error;

    public ValueWithError(ValueType value, ErrorResult error){
        this.value = value;
        if (error == null) {
            hasError = false;
        }
        else {
            hasError = true;
        }
        this.error = error;
    }
    public boolean hasError(){
        return hasError;
    }
    public IQueryResult error(){
        return error;
    }
    public ValueType value(){
        return value;
    }
}
