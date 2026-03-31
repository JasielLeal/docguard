package br.com.harmony.DocGuard.application.services.enterprise.create;


import br.com.harmony.DocGuard.domain.model.Enterprise;
import br.com.harmony.DocGuard.domain.model.User;
import br.com.harmony.DocGuard.infrastructure.config.ApiException;
import br.com.harmony.DocGuard.infrastructure.config.ApiResponse;
import br.com.harmony.DocGuard.infrastructure.repository.enterprise.EnterpriseRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;

@Service
public class CreateEnterpriseService {

    private final EnterpriseRepository enterpriseRepository;

    public CreateEnterpriseService(EnterpriseRepository enterpriseRepository) {
        this.enterpriseRepository = enterpriseRepository;
    }

    @Transactional
    public ApiResponse<Void> execute(CreateEnterpriseRequest request, User authenticatedUser) {

        // Remove pontuação do CNPJ caso venha formatado
        String cnpj = request.getCnpj().replaceAll("[^0-9]", "");

        if (enterpriseRepository.findByCnpj(cnpj).isPresent()) {
            throw new ApiException("Enterprise with this CNPJ already exists", HttpStatus.BAD_REQUEST);
        }

        // Busca dados na API
        CnpjResponse cnpjData = fetchCnpjData(cnpj);

        Enterprise enterprise = new Enterprise();
        enterprise.setLegalName(cnpjData.getRazaoSocial());
        enterprise.setTradeName(cnpjData.getNomeFantasia());
        enterprise.setCnpj(cnpj);
        enterprise.setRegistrationStatus(cnpjData.getSituacaoCadastral());
        enterprise.setActivityStartDate(cnpjData.getDataInicioAtividade());
        enterprise.setLegalNature(cnpjData.getNaturezaJuridica());
        enterprise.setCompanySize(cnpjData.getPorteEmpresa());
        enterprise.setPrimaryCnae(cnpjData.getCnaePrincipal());
        enterprise.setStreet(cnpjData.getLogradouro());
        enterprise.setNumber(cnpjData.getNumero());
        enterprise.setComplement(cnpjData.getComplemento());
        enterprise.setDistrict(cnpjData.getBairro());
        enterprise.setZipCode(cnpjData.getCep());
        enterprise.setCity(cnpjData.getMunicipio());
        enterprise.setState(cnpjData.getUf());
        enterprise.setEmail(cnpjData.getEmail());
        enterprise.setPhone(cnpjData.getPhone());
        enterprise.setCreatedAt(LocalDateTime.now());
        enterprise.setUser(authenticatedUser);

        enterpriseRepository.save(enterprise);

        return new ApiResponse<>(true, "Enterprise created successfully", null);
    }

    private CnpjResponse fetchCnpjData(String cnpj) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.opencnpj.org/" + cnpj))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 404) {
                throw new ApiException("CNPJ not found", HttpStatus.NOT_FOUND);
            }

            if (response.statusCode() != 200) {
                throw new ApiException("Error fetching CNPJ data", HttpStatus.BAD_GATEWAY);
            }

            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(response.body(), CnpjResponse.class);

        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Error fetching CNPJ data", HttpStatus.BAD_GATEWAY);
        }
    }
}
