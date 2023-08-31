package uow.msc.project.hracache.service.util;

import lombok.Data;

/**
 * Author: Chulaka Lahiru - 2019515 - W1762231
 */
@Data
public class RequestStatusType {

    public enum RequestStatusTypeList {
        SUCCESS, IN_PROGRESS, FAILED
    }

}
