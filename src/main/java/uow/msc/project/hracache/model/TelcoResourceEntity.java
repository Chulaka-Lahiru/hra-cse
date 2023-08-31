package uow.msc.project.hracache.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Author: Chulaka Lahiru - 2019515 - W1762231
 */
@Entity
@Table(name = "TelcoProducts")
@Getter
@Setter
@ToString
@NoArgsConstructor
public class TelcoResourceEntity extends BaseTelcoResourceEntity {

    private String telecomProduct;

    private long rating;

    private String category;

    private String priority;

}
