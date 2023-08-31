package uow.msc.project.hracache.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;

/**
 * Author: Chulaka Lahiru - 2019515 - W1762231
 */
@Entity
@Table(name = "MetaData")
@Getter
@Setter
@ToString
@NoArgsConstructor
public class MetaDataEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    private Long id;

    private String requestType;

    private String requestStatus;

    private String dateTime;

    private double hitRate;

    private double delay;

}
