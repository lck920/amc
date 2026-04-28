/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package validation;
import java.util.List;

public class ValidationResult {
    private final boolean ok;
    private final List<String> errors;

    private ValidationResult(boolean ok, List<String> errors) {
        this.ok = ok;
        this.errors = errors;
    }

    public static ValidationResult ok() {
        return new ValidationResult(true, List.of());
    }

    public static ValidationResult fail(List<String> errors) {
        return new ValidationResult(false, List.copyOf(errors));
    }

    public boolean isOk() { return ok; }
    public List<String> getErrors() { return errors; }
}
