package cl.duoc.cashin.AuthService.Exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String msg) {
        super(msg);
        // pasa el mensaje a RuntimeException
        // luego ex.getMessage() retorna este mensaje
    }
}
