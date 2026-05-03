package com.batch.api.resource;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

import java.time.LocalDateTime;

/**
 * Mapeia exceções para respostas JSON padronizadas.
 * O frontend Angular espera um body com detalhes do erro.
 */
@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Exception> {

    private static final Logger LOG = Logger.getLogger(GlobalExceptionMapper.class);

    @Override
    public Response toResponse(Exception exception) {

        if (exception instanceof NotFoundException nfe) {
            return errorResponse(Response.Status.NOT_FOUND, nfe.getMessage());
        }

        if (exception instanceof WebApplicationException wae) {
            int status = wae.getResponse().getStatus();
            return errorResponse(Response.Status.fromStatusCode(status), wae.getMessage());
        }

        LOG.errorf(exception, "Erro não tratado: %s", exception.getMessage());
        return errorResponse(Response.Status.INTERNAL_SERVER_ERROR,
                "Erro interno do servidor. Consulte os logs para mais detalhes.");
    }

    private Response errorResponse(Response.Status status, String message) {
        var body = new ErrorResponse(
            status.getStatusCode(),
            status.getReasonPhrase(),
            message,
            LocalDateTime.now().toString()
        );
        return Response.status(status)
                .type(MediaType.APPLICATION_JSON)
                .entity(body)
                .build();
    }

    public record ErrorResponse(
        int status,
        String error,
        String message,
        String timestamp
    ) {}
}
