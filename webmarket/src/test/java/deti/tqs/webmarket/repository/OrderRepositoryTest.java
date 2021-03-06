package deti.tqs.webmarket.repository;

import deti.tqs.webmarket.model.Customer;
import deti.tqs.webmarket.model.Order;
import deti.tqs.webmarket.model.User;
import deti.tqs.webmarket.util.Utils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import javax.persistence.PersistenceException;


@DataJpaTest
class OrderRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;
    @Autowired
    private OrderRepository orderRepository;

    private Customer customer;
    private Order order;
    private Order order2;

    @BeforeEach
    void setUp() {
        this.entityManager.clear();

        var user1 = new User(
                "Urlando",
                "urlando@gmail.com",
                "CUSTOMER",
                "password",
                "93555555"
        );

        this.customer = new Customer(
                user1,
                "Back street",
                "Very good restaurant, you can trust",
                "Restaurant",
                "PT50000201231234567890154"
        );

        this.order = new Order(
                "MBWAY",
                19.99,
                this.customer,
                "Rua da Pereira, 15, Anadia 1111-111"
        );
        this.order.setOrderTimestamp(Utils.parseTimestamp(
                "2021-05-26 00:00:00"
        ));

        this.order2 = new Order(
                "PAYPAL",
                21.14,
                this.customer,
                "Rua da Macieira, 16, Anadia 1111-112"
        );
        this.order2.setOrderTimestamp(Utils.parseTimestamp(
                "2021-05-29 00:00:00"
        ));

        this.customer.getOrders().add(this.order);
        this.customer.getOrders().add(this.order2);

        this.entityManager.persist(this.customer);
        this.entityManager.persist(this.order);
        this.entityManager.persist(this.order2);
        this.entityManager.flush();
    }

    @Test
    void returnTheOrdersUsingTheCustomerUsername() {
        var user2 = new User(
                "Frank Sinatra",
                "franky@gmail.com",
                "CUSTOMER",
                "password",
                "93555555"
        );

        var customer2 = new Customer(
                user2,
                "Back street",
                "Very good restaurant, you can trust",
                "Restaurant",
                "PT50000201231234567890154"
        );

        var frankOrder = new Order(
                "MBWAY",
                1.99,
                customer2,
                "Rua da Lagosta, 15, Anadia 1111-111"
        );
        customer2.getOrders().add(frankOrder);

        this.entityManager.persist(user2);
        this.entityManager.persist(customer2);
        this.entityManager.persist(frankOrder);
        this.entityManager.flush();

        Assertions.assertThat(
                this.orderRepository.findOrdersByCustomer_User_Username(
                        this.customer.getUser().getUsername()
                )
        ).contains(this.order, this.order2).doesNotContain(frankOrder);

        Assertions.assertThat(
                this.orderRepository.findOrdersByCustomer_User_Username(
                        user2.getUsername()
                )
        ).contains(frankOrder).doesNotContain(this.order, this.order2);
    }

    @Test
    void whenPaymentMethodEqualsCash_thenAExceptionShouldBeRaised() {
        var order10 = new Order(
                "CASH",
                20.15,
                this.customer,
                "Rua da Nogueira, 17, Anadia 1111-113"
        );
        Assertions.assertThatThrownBy(
                () -> this.entityManager.persistAndFlush(
                        order10
                )
        ).isInstanceOf(PersistenceException.class);
    }

    @Test
    void whenChangeOrderStatusWrong_thenExceptionShouldBeRaised() {
        var res = this.orderRepository.findById(this.order.getId()).get();

        // update status of order to invalid value
        res.setStatus("INVALID");

        Assertions.assertThatThrownBy(
                () -> this.entityManager.persistAndFlush(res)
        ).isInstanceOf(PersistenceException.class);
    }

    @Test
    void getOrderAfterTimestampTest() {
        Assertions.assertThat(
                this.orderRepository.findOrdersByOrderTimestampAfter(
                        Utils.parseTimestamp(
                                "2021-05-27 00:00:00"
                        )
                )
        ).contains(this.order2).doesNotContain(this.order);
    }

    @Test
    void getOrderBeforeTimestampTest() {
        Assertions.assertThat(
                this.orderRepository.findOrdersByOrderTimestampBefore(
                        Utils.parseTimestamp(
                                "2021-05-27 00:00:00"
                        )
                )
        ).contains(this.order).doesNotContain(this.order2);
    }

    @Test
    void whenQueryingAllTheOrdersMadeByACostumer_thenAllOrdersShouldBeReturned() {
        Assertions.assertThat(
                this.orderRepository.findOrdersByCustomer(this.customer)
        ).contains(this.order, this.order2);
    }

}