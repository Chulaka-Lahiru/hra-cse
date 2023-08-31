package uow.msc.project.hracache.service.util;

import lombok.Data;

/**
 * Author: Chulaka Lahiru - 2019515 - W1762231
 */
@Data
public class RequestType {

    public enum RequestTypeList {
        POST, PUT, GET, DELETE
    }

}
