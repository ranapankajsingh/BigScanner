package co.big.hrd;

public record Employee(int id, String firstName, String lastName,
                       double salary, Integer managerId) {
}