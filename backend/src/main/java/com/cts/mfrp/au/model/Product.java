@Entity
@Table(name = "products")
@Data
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int productId;

    @ManyToOne
    @JoinColumn(name = "seller_id")
    private User seller;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    private String productName;
    private String description;
    private String imageUrl;
    private float startingPrice;
    private String verificationStatus;
    private String adminRemarks;
    private java.time.LocalDateTime submittedAt;
}