package main.java;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import io.smallrye.mutiny.Multi;

public class ValidationProcessor {

    @Incoming("validation-request")
    @Outgoing("validation-response")
    public Multi<ValidationResponse> validateTextMessages(Multi<ValidationRequest> requests) {
        return requests.onItem().transform(request -> {
            boolean valid = !request.text().contains("hftm sucks");
            return new ValidationResponse(request.id(), valid);
        });
    }
}
