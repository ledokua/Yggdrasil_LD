package net.ledok.prime.api.dto;

// Note: Replaced Lombok with standard Java to avoid extra setup.
public class PrimeStatusDTO {

    private String status;
    private Long expires;
    private String prefix;

    public String getStatus() {
        return status;
    }

    public boolean isPrime() {
        return status != null && status.equalsIgnoreCase("prime");
    }
}
