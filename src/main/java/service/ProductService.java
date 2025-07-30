import model.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import repository.ProductRepository;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    public void updateProductDescription(int id, String description) {
        productRepository.updateDescription(id, description);
    }

    public void addProduct(Product product) {
        productRepository.addProduct(product);
    }

    public String checkProductSale(int productId, String date) {
        return productRepository.checkSale(productId, date);
    }
}
