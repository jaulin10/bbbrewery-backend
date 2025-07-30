import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BasketService {

    @Autowired
    private BasketRepository basketRepository;

    public void addItemToBasket(int basketId, int productId, int quantity, double price) {
        basketRepository.addItemToBasket(basketId, productId, quantity, price);
    }

    public String checkBasketStock(int basketId) {
        return basketRepository.checkBasketStock(basketId);
    }

    public double getTotalSpending(int shopperId) {
        return basketRepository.getTotalSpending(shopperId);
    }
}
