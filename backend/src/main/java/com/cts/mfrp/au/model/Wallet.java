@Entity
@Table(name = "wallets")
@Data
public class Wallet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int walletId;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    private float availableBalance = 0.0f;
    private float frozenBalance = 0.0f;
    private java.time.LocalDateTime lastUpdated;
}