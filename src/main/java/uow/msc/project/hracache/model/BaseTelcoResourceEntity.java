package uow.msc.project.hracache.model;

import lombok.Data;

import javax.persistence.*;

/**
 * Author: Chulaka Lahiru - 2019515 - W1762231
 */
@Data
@MappedSuperclass
public abstract class BaseTelcoResourceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    private Long id;

    @Column(name = "created_at", updatable = false)
    private String createdDate;

    @Column(name = "modified_at")
    private String modifiedDate;

}
