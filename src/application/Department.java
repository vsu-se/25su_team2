package application;

public class Department {

	private final String code;
    private final String name;

    public Department(String code, String name) {
        if (code == null || code.isEmpty()) {
            throw new IllegalArgumentException("Dept code required");
        }
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    @Override
    public String toString() {
        return name;  // used for display
    }
}
