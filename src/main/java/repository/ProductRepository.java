import model.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ProductRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // Calls procedure PROD_UPDATE_DESC_SP
    public void updateDescription(int id, String description) {
        jdbcTemplate.update("CALL PROD_UPDATE_DESC_SP(?, ?)", id, description);
    }

    // Calls procedure PROD_ADD_SP
    public void addProduct(Product product) {
        jdbcTemplate.update("CALL PROD_ADD_SP(?, ?, ?, ?, ?)",
                product.getName(),
                product.getDescription(),
                product.getImage(),
                product.getPrice(),
                product.getStatus());
    }

    // Calls function CK_SALE_SF
    public String checkSale(int productId, String date) {
        return jdbcTemplate.queryForObject("SELECT CK_SALE_SF(?, ?)",
                new Object[]{productId, date}, String.class);
    }
}
