package com.batch.api.resource;

import com.batch.api.model.BatchDTOs.*;
import com.batch.api.service.BatchService;
import jakarta.inject.Inject;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;

/**
 * Endpoints REST consumidos pelo frontend Angular Spring Batch Dashboard.
 *
 * Base URL: /batch
 *
 * GET  /batch/executions              → lista paginada de JobExecutions
 * GET  /batch/executions/{id}         → JobExecution por ID (com steps)
 * GET  /batch/executions/{id}/steps   → steps de uma execução
 * GET  /batch/jobs                    → nomes de jobs distintos
 * GET  /batch/summary                 → totais para o dashboard
 * POST /batch/executions/{id}/restart → reiniciar uma execução
 */
@Path("/batch")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Batch", description = "Monitoramento de Jobs Spring Batch")
public class BatchResource {

    @Inject
    BatchService batchService;

    // ----------------------------------------------------------------
    // GET /batch/executions
    // ----------------------------------------------------------------
    @GET
    @Path("/executions")
    @Operation(
        summary = "Listar execuções de jobs",
        description = "Retorna lista paginada de JobExecutions com filtros opcionais."
    )
    @APIResponse(
        responseCode = "200",
        description = "Lista paginada de execuções",
        content = @Content(schema = @Schema(implementation = PagedResponseDoc.class))
    )
    public Response getJobExecutions(
            @Parameter(description = "Filtrar pelo nome do job (suporta substring)")
            @QueryParam("jobName") String jobName,

            @Parameter(description = "Filtrar por status: COMPLETED, FAILED, STARTED, STOPPED, ABANDONED, UNKNOWN")
            @QueryParam("status") String status,

            @Parameter(description = "Data/hora de início mínima (yyyy-MM-dd ou yyyy-MM-dd'T'HH:mm:ss)")
            @QueryParam("startDateFrom") String startDateFrom,

            @Parameter(description = "Data/hora de início máxima (yyyy-MM-dd ou yyyy-MM-dd'T'HH:mm:ss)")
            @QueryParam("startDateTo") String startDateTo,

            @Parameter(description = "Número da página (começa em 0)")
            @QueryParam("page") @DefaultValue("0") @Min(0) int page,

            @Parameter(description = "Tamanho da página (máx 200)")
            @QueryParam("size") @DefaultValue("20") @Min(1) @Max(200) int size) {

        PagedResponse<JobExecutionDTO> result =
                batchService.getJobExecutions(jobName, status, startDateFrom, startDateTo, page, size);

        return Response.ok(result).build();
    }

    // ----------------------------------------------------------------
    // GET /batch/executions/{id}
    // ----------------------------------------------------------------
    @GET
    @Path("/executions/{id}")
    @Operation(
        summary = "Buscar execução por ID",
        description = "Retorna os detalhes completos de uma JobExecution incluindo seus StepExecutions."
    )
    @APIResponse(responseCode = "200", description = "JobExecution encontrada")
    @APIResponse(responseCode = "404", description = "JobExecution não encontrada")
    public Response getJobExecution(
            @Parameter(description = "ID da JobExecution", required = true)
            @PathParam("id") Long id) {

        JobExecutionDTO dto = batchService.getJobExecution(id);
        return Response.ok(dto).build();
    }

    // ----------------------------------------------------------------
    // GET /batch/executions/{id}/steps
    // ----------------------------------------------------------------
    @GET
    @Path("/executions/{jobExecutionId}/steps")
    @Operation(
        summary = "Listar steps de uma execução",
        description = "Retorna todos os StepExecutions associados a uma JobExecution."
    )
    @APIResponse(responseCode = "200", description = "Lista de StepExecutions")
    @APIResponse(responseCode = "404", description = "JobExecution não encontrada")
    public Response getStepExecutions(
            @Parameter(description = "ID da JobExecution", required = true)
            @PathParam("jobExecutionId") Long jobExecutionId) {

        List<StepExecutionDTO> steps = batchService.getStepExecutions(jobExecutionId);
        return Response.ok(steps).build();
    }

    // ----------------------------------------------------------------
    // GET /batch/jobs
    // ----------------------------------------------------------------
    @GET
    @Path("/jobs")
    @Operation(
        summary = "Listar nomes de jobs",
        description = "Retorna lista de nomes de jobs distintos cadastrados no sistema."
    )
    @APIResponse(responseCode = "200", description = "Array de nomes de jobs")
    public Response getJobNames() {
        List<String> names = batchService.getJobNames();
        return Response.ok(names).build();
    }

    // ----------------------------------------------------------------
    // GET /batch/summary
    // ----------------------------------------------------------------
    @GET
    @Path("/summary")
    @Operation(
        summary = "Resumo do dashboard",
        description = "Retorna totais consolidados para exibição nos cards do dashboard."
    )
    @APIResponse(responseCode = "200", description = "Resumo com totais e taxa de sucesso")
    public Response getDashboardSummary() {
        DashboardSummary summary = batchService.getDashboardSummary();
        return Response.ok(summary).build();
    }

    // ----------------------------------------------------------------
    // POST /batch/executions/{id}/restart
    // ----------------------------------------------------------------
    @POST
    @Path("/executions/{id}/restart")
    @Operation(
        summary = "Reiniciar uma execução",
        description = "Reinicia uma JobExecution com status FAILED ou STOPPED."
    )
    @APIResponse(responseCode = "204", description = "Restart solicitado com sucesso")
    @APIResponse(responseCode = "404", description = "JobExecution não encontrada")
    @APIResponse(responseCode = "409", description = "Status incompatível com restart")
    public Response restartJobExecution(
            @Parameter(description = "ID da JobExecution", required = true)
            @PathParam("id") Long id) {

        batchService.restartJobExecution(id);
        return Response.noContent().build();
    }

    // ----------------------------------------------------------------
    // Schema helper para documentação OpenAPI
    // ----------------------------------------------------------------
    @Schema(name = "PagedResponse")
    static class PagedResponseDoc {
        public List<JobExecutionDTO> content;
        public long totalElements;
        public int totalPages;
        public int size;
        public int number;
    }
}
