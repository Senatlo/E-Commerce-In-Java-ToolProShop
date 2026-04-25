import com.shopping.cart.ShoppingCart;
import com.shopping.dao.ProductDAO;
import com.shopping.dao.UserDAO;
import com.shopping.model.Product;
import com.shopping.model.Role;
import com.shopping.model.User;
import com.shopping.order.OrderProcessor;
import com.shopping.util.DatabaseConnection;
import com.shopping.catalog.ProductManager;


public class WorkflowTest {
    public static void main(String[] args) {
        try {
            System.out.println("Starting Test...");
            DatabaseConnection.getInstance().initializeDatabase();
            System.out.println("DB Inited");

            ProductDAO prodDao = new ProductDAO();
            System.out.println("Products loaded: " + prodDao.getAllProducts().size());

            UserDAO userDao = new UserDAO();
            User user = userDao.authenticate("shopper", "shop123");
            System.out.println("User auth: " + (user != null));

            ShoppingCart cart = new ShoppingCart(prodDao);
            Product drill = prodDao.getAllProducts().get("TOOL-001");
            System.out.println("Drill retrieved: " + (drill != null));
            cart.addProduct(drill.getId(), 1);

            OrderProcessor proc = new OrderProcessor(prodDao);
            System.out.println("Checking out...");
            proc.checkout(cart, user.getId(), user.getUsername());
            
            System.out.println("SUCCESS. No bugs in flow.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
