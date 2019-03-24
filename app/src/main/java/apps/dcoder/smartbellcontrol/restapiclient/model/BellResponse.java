package apps.dcoder.smartbellcontrol.restapiclient.model;

public class BellResponse {
    private String successMessage;
    private String errorMessage;

    public BellResponse() {
        successMessage = null;
        errorMessage = null;
    }

    public String getSuccessMessage() {
        return successMessage;
    }

    public void setSuccessMessage(String successMessage) {
        this.successMessage = successMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
