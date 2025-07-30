import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class BasketRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void addItemToBasket(int basketId, int productId, int quantity, double price) {
        jdbcTemplate.update("CALL BASKET_ADD_SP(?, ?, ?, ?)", basketId, productId, quantity, price);
    }

    public String checkBasketStock(int basketId) {
        return jdbcTemplate.queryForObject(
                "SELECT CHECK_BASKET_STOCK(?)",  // assuming a function
                new Object[]{basketId},
                String.class
        );
    }

    public double getTotalSpending(int shopperId) {
        return jdbcTemplate.queryForObject(
                "SELECT TOT_PURCH_SF(?)",
                new Object[]{shopperId},
                Double.class
        );
    }
}
