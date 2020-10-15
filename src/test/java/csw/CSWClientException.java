package csw;

public class CSWClientException extends Exception {
    public CSWClientException(String errorMessage, Throwable err){
        super(errorMessage, err);
    }
    public CSWClientException(String errorMessage){
        super(errorMessage);
    }
}
