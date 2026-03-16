package gnu.project.pbl2.common.exception;


import gnu.project.pbl2.common.error.ErrorCode;

public class NotFoundException extends BusinessException {

    public NotFoundException(ErrorCode errorCode) {
        super(errorCode.getMessage(), errorCode);
    }

}
