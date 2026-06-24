package com.raizesdonordeste.backend;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OpenApiDocumentationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void deveGerarDocumentoOpenApiComJwtEContratosPrincipais() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi").exists())
                .andExpect(jsonPath("$.info.title")
                        .value("Raizes do Nordeste - API Back-End"))
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.type")
                        .value("http"))
                .andExpect(jsonPath("$.paths['/auth/login'].post.security").doesNotExist())
                .andExpect(jsonPath("$.paths['/clientes'].post.security").doesNotExist())
                .andExpect(jsonPath("$.paths['/clientes/login'].post.security").doesNotExist())
                .andExpect(jsonPath("$.paths['/pedidos'].post.security[0].bearerAuth").exists())
                .andExpect(jsonPath("$.paths['/pagamentos/mock'].post.requestBody.content['application/json'].examples").exists())
                .andExpect(jsonPath("$.components.schemas.ApiErrorResponse").exists())
                .andExpect(jsonPath("$.components.schemas.PedidoDTO.properties.canalPedido.enum[0]")
                        .value("APP"));
    }
}
