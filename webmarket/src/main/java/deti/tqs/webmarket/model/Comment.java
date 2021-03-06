package deti.tqs.webmarket.model;

import lombok.Data;

import javax.persistence.*;
import java.sql.Timestamp;

@Data
@Entity
@Table(name = "comments")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rider_id", nullable = false)
    private Rider rider;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commenter_id")
    private Customer commenter;

    private Integer stars;

    private String opinion;

    private Timestamp timestamp;

    public Comment() {}

    public Comment(Rider rider, Customer customer, Integer stars, String opinion) {
        this.rider = rider;
        this.commenter = customer;
        this.stars = stars;
        this.opinion = opinion;
        this.timestamp = new Timestamp(System.currentTimeMillis());
    }

}
